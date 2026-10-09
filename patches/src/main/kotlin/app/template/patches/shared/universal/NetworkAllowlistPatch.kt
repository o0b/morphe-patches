package app.template.patches.shared.universal

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.AccessFlags
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Universal "Network host allowlist" master patch — the allowlist inversion
 * of Morning-Entree's "Block tracking hosts" concept
 * (Entree3k/Morning-Entree-Patches, GPL-3.0). Their blocklist works by
 * rewriting known host string literals to 0.0.0.0; an allowlist cannot work
 * at that layer (a hostname-shaped literal is indistinguishable from a
 * package name), so enforcement moves to the DNS boundary: every
 * InetAddress.getByName/getAllByName call site in the app's dex routes
 * through NetworkGuardHelper, which resolves allowlisted hosts for real
 * and sinks everything else to 0.0.0.0 — catching config-fetched and
 * redirect hosts that literal rewriting can never see.
 *
 * Injection: policy init at Application.attachBaseContext (onCreate
 * fallbacks), the same chain as the IPC guard. Allowlist is baked at patch
 * time — changing it means re-patching the app.
 *
 * ponytail ceilings: enforced only where the app's own code resolves names
 * (bundled okhttp's Dns.SYSTEM is covered transitively). Platform-internal
 * resolution — HttpURLConnection's connect path, new
 * InetSocketAddress(host, port) (resolved inside libcore), WebView's native
 * stack, native sockets and raw-IP connects — bypasses app-dex call sites.
 * For a hard guarantee use Remove INTERNET; this patch is for apps that
 * need a few endpoints to function.
 */
private const val HELPER = "Lapp/template/extension/extension/NetworkGuardHelper;"

private class GuardTarget(
    val className: String,
    val methodName: String,
    val parameters: String,
    val returnType: String,
    val receiverParam: String?, // null for static call sites
)

private val DNS_TARGETS = listOf(
    GuardTarget("Ljava/net/InetAddress;", "getByName", "Ljava/lang/String;", "Ljava/net/InetAddress;", null),
    GuardTarget("Ljava/net/InetAddress;", "getAllByName", "Ljava/lang/String;", "[Ljava/net/InetAddress;", null),
)

private val networkAllowlistApplicationAttachFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PROTECTED),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    custom = { method, classDef ->
        method.name == "attachBaseContext" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val networkAllowlistApplicationCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val networkAllowlistActivityCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    custom = { method, classDef ->
        method.name == "onCreate" &&
            (classDef.superclass?.contains("Activity;") == true ||
                method.implementation?.instructions?.any {
                    val reference = (it as? ReferenceInstruction)?.reference as? MethodReference
                    reference?.name == "onCreate" && reference.parameterTypes == listOf("Landroid/os/Bundle;")
                } == true)
    },
)

private fun String.escapeSmali() = replace("\\", "\\\\").replace("\"", "\\\"")

@Suppress("unused")
val networkAllowlistPatch = bytecodePatch(
    name = "Network host allowlist",
    description = "Blocks all of the app's DNS resolution except the domains you allow: every " +
        "non-allowlisted host resolves to 0.0.0.0, so no traffic leaves the device except to the " +
        "hosts you list in the options (gear). Entries are suffix-matched — example.com also " +
        "allows its subdomains. Unlike literal-based host blocking, this catches hosts the app " +
        "fetches from configuration or redirects, not just shipped literals. The allowlist is " +
        "baked at patch time; re-patch to change it.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    extendWith("extensions/extension.mpe")

    val allowedDomains by stringOption(
        key = "allowedDomains",
        default = "",
        title = "Allowed domains",
        description = "Comma-separated domains allowed through the barrier (e.g. " +
            "'api.example.com, cdn.example.net'). An entry also allows every subdomain. " +
            "Everything else resolves to 0.0.0.0. Leave empty to skip the patch — a full " +
            "offline block is the Remove INTERNET permission patch's job.",
    )

    execute {
        val csv = (allowedDomains ?: "").trim()
        if (csv.isEmpty()) {
            println("[Network host allowlist] Skipped: empty allowlist — use Remove INTERNET for a full offline block.")
            return@execute
        }

        val initInstructions = """
            const-string v0, "${csv.escapeSmali()}"
            invoke-static {p0, v0}, $HELPER->init(Landroid/content/Context;Ljava/lang/String;)V
        """.trimIndent()
        var initialized =
            networkAllowlistApplicationAttachFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null ||
                networkAllowlistApplicationCreateFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null ||
                networkAllowlistActivityCreateFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null

        val lookup = DNS_TARGETS.associateBy {
            "${it.className}->${it.methodName}(${it.parameters})${it.returnType}"
        }
        var rewritten = 0
        classDefForEach { classDef ->
            if (classDef.type.startsWith("Lapp/template/extension/")) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@forEach
                instructions.forEachIndexed { index, instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                        ?: return@forEachIndexed
                    val target = lookup[
                        "${reference.definingClass}->${reference.name}(${reference.parameterTypes.joinToString("")})${reference.returnType}"
                    ] ?: return@forEachIndexed

                    val helperParams =
                        (target.receiverParam?.let { listOf(it) } ?: emptyList()) + reference.parameterTypes
                    val helperSignature =
                        "$HELPER->${target.methodName}(${helperParams.joinToString("")})${reference.returnType}"

                    val replacement = when (instruction) {
                        is Instruction35c -> {
                            val regs = when (instruction.registerCount) {
                                1 -> "v${instruction.registerC}"
                                2 -> "v${instruction.registerC}, v${instruction.registerD}"
                                3 -> "v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}"
                                4 -> "v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}, v${instruction.registerF}"
                                else -> "v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE}, v${instruction.registerF}, v${instruction.registerG}"
                            }
                            "invoke-static {$regs}, $helperSignature"
                        }
                        is Instruction3rc -> {
                            val start = instruction.startRegister
                            val end = start + instruction.registerCount - 1
                            "invoke-static/range {v$start..v$end}, $helperSignature"
                        }
                        else -> return@forEachIndexed
                    }

                    method.replaceInstruction(index, replacement)
                    rewritten++
                }
            }
        }

        println(
            "[Network host allowlist] init=${if (initialized) "injected" else "lazy (fingerprint missed)"} " +
                "dnsCallSites=$rewritten (allowed: $csv).",
        )

        if (rewritten == 0) {
            println("[Network host allowlist] Warning: no DNS call sites rewritten — packed, reflective, or platform-stack-only build?")
        }
    }
}
