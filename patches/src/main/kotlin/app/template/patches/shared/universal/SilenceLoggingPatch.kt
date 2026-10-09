package app.template.patches.shared.universal

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Universal "Silence app logging" patch.
 *
 * Nops every logging call site in the app's dex so secrets never enter the
 * logcat ring buffer: android.util.Log writers (v/d/i/w/e/wtf/println —
 * matched by name, so every overload, format-string variant and range-form
 * invoke is caught), PrintStream print/println (System.out / System.err)
 * and Throwable.printStackTrace.
 *
 * Why: apps cannot read their own logcat since API 16 — the buffer is
 * readable via adb, bug-report flows and forensic capture, and logging
 * tokens, credentials or PII into it is the classic OWASP M9 leak. The only
 * defense is the line never being written. android.util.Log lives on the
 * boot classpath, so definitions cannot be patched — call sites are
 * rewritten instead. Bundled facades (Timber, androidx, custom wrappers)
 * funnel into android.util.Log from inside the app dex, so they are
 * silenced transitively. Log.isLoggable and Log.getStackTraceString are
 * deliberately left untouched so guard conditions and crash formatting
 * keep working.
 *
 * ponytail ceilings: blind to reflection-invoked logging and packed/native
 * builds; call sites whose move-result targets v256+ are skipped (invalid
 * const/16 encoding — a missed log line beats a broken method); the
 * System-stream toggle rewrites ALL PrintStream call sites, not only
 * System.out/err, hence default-off.
 */
private val LOG_WRITER_METHODS = setOf("v", "d", "i", "w", "e", "wtf", "println")

@Suppress("unused")
val silenceLoggingPatch = bytecodePatch(
    name = "Silence app logging",
    description = "Nops every logging call site — android.util.Log writers, System.out/err " +
        "print and println, and Throwable.printStackTrace — so tokens, credentials and PII " +
        "never reach the logcat ring buffer, where adb, bug reports and forensic capture " +
        "read them. Bundled wrappers like Timber funnel through android.util.Log and are " +
        "silenced transitively. Log-based debug features and in-app log viewers go empty — " +
        "that is the point.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val silenceAndroidLog by booleanOption(
        key = "silenceAndroidLog",
        default = true,
        title = "Silence android.util.Log",
        description = "Nop all Log.v/d/i/w/e/wtf/println call sites — every overload, " +
            "including format-string and throwable variants.",
        required = false,
    )

    val silenceSystemStreams by booleanOption(
        key = "silenceSystemStreams",
        default = false,
        title = "Silence System.out / System.err",
        description = "Nop all PrintStream print/println call sites. Default is false: this " +
            "rewrites every PrintStream call site, not only System.out/err — an app writing " +
            "text files through PrintStream.println would lose those writes.",
        required = false,
    )

    val silenceStackTraces by booleanOption(
        key = "silenceStackTraces",
        default = true,
        title = "Silence printStackTrace",
        description = "Nop Throwable.printStackTrace call sites — exception messages often " +
            "carry the very secret being handled when the error occurred.",
        required = false,
    )

    execute {
        val silenceLog = silenceAndroidLog ?: true
        val silenceStreams = silenceSystemStreams ?: false
        val silenceTraces = silenceStackTraces ?: true

        var logHooks = 0
        var logResultHooks = 0
        var streamHooks = 0
        var traceHooks = 0
        var skippedHighRegister = 0

        classDefForEach { classDef ->
            if (classDef.type.startsWith("Lapp/template/extension/")) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach

                var index = 0
                while (index < instructions.size) {
                    val instruction = instructions[index]
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference

                    if (reference != null) {
                        val className = reference.definingClass
                        val methodName = reference.name

                        // android.util.Log writers — signature-agnostic by name match.
                        if (silenceLog && className == "Landroid/util/Log;" && methodName in LOG_WRITER_METHODS) {
                            val next = instructions.getOrNull(index + 1)
                            if (next != null && next.opcode == Opcode.MOVE_RESULT) {
                                val reg = (next as? OneRegisterInstruction)?.registerA
                                if (reg != null && reg < 256) {
                                    // Log.x return the bytes written: force a 0 into the
                                    // result register so discarded results and checked
                                    // results both behave.
                                    method.replaceInstruction(index, "nop")
                                    method.replaceInstruction(
                                        index + 1,
                                        if (reg < 16) "const/4 v$reg, 0x0" else "const/16 v$reg, 0x0",
                                    )
                                    logHooks++
                                    logResultHooks++
                                    index += 2
                                    continue
                                } else {
                                    skippedHighRegister++
                                    index++
                                    continue
                                }
                            } else {
                                // Result discarded — nop the call alone.
                                method.replaceInstruction(index, "nop")
                                logHooks++
                                index++
                                continue
                            }
                        }

                        if (silenceStreams && className == "Ljava/io/PrintStream;" &&
                            (methodName == "println" || methodName == "print")
                        ) {
                            method.replaceInstruction(index, "nop")
                            streamHooks++
                            index++
                            continue
                        }

                        if (silenceTraces && className == "Ljava/lang/Throwable;" && methodName == "printStackTrace") {
                            method.replaceInstruction(index, "nop")
                            traceHooks++
                            index++
                            continue
                        }
                    }
                    index++
                }
            }
        }

        val total = logHooks + streamHooks + traceHooks
        if (total == 0) {
            println("[Silence app logging] No logging call sites found (or build is packed/reflective).")
        } else {
            val skipNote = if (skippedHighRegister > 0) " ($skippedHighRegister high-register call site(s) left intact)" else ""
            println(
                "[Silence app logging] Nopped $logHooks Log call site(s) ($logResultHooks result-forced), " +
                    "$streamHooks stream print site(s), $traceHooks stack-trace site(s)$skipNote.",
            )
        }
    }
}
