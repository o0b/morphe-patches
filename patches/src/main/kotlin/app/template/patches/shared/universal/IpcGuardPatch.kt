package app.template.patches.shared.universal

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.AccessFlags
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.w3c.dom.Element

/**
 * Universal "IPC guard" master patch — a cross-app exfiltration barrier
 * following kveld9's gear-toggle pattern (kveld9/kveld-morphe-patches,
 * GPL-3.0). One master patch, four gear toggles plus an allowlist, and a
 * baked-in manifest sweep companion:
 *
 *   Module A — intent boundary: every startActivity/startService/
 *   startForegroundService/stopService/bindService/sendBroadcast call site
 *   is rewritten to route through the IpcGuardHelper, which allows
 *   own-package traffic and the option allowlist, and drops everything
 *   else. Implicit ACTION_VIEW web links are forced to the first
 *   allowlisted package (trusted-browser rule).
 *   Module B — provider severance: ContentResolver query/insert/update/
 *   delete/call/open* call sites route through a URI authority allowlist
 *   (own package, platform authorities, user extras).
 *   Module C — receiver lockdown: registerReceiver call sites are rewritten
 *   to add RECEIVER_NOT_EXPORTED on API 33+.
 *   Module E — PendingIntent de-weaponization: getActivity/getBroadcast/
 *   getService/getForegroundService creation sites sanitize their intent —
 *   foreign targets become self-targeted no-ops so notification lures
 *   cannot reach other apps when the system fires the PendingIntent.
 *   Module D — manifest sweep (baked in, NOT toggleable): providers with
 *   exported=true are unexported and grantUriPermissions=false, all
 *   <queries> visibility blocks are removed, and NFC HCE services
 *   (BIND_NFC_SERVICE) are removed. Blanket activity/receiver exported
 *   flips are deliberately NOT done — widgets, share targets and launcher
 *   activities are the deliberate-export surface; inbound receivers are
 *   handled at runtime by Module C.
 *
 * Injection points: policy init is injected at Application.attachBaseContext
 * (onCreate fallbacks), the same site every no-root instrumentation lineage
 * uses. The own package is also captured lazily from any Context that flows
 * through a guard, so a missed init point degrades gracefully.
 *
 * ponytail ceilings: signature-based matching is blind to reflection
 * (Method.invoke), native/packed APKs; flagged and ContextCompat
 * registerReceiver overloads are respected, not overridden.
 */
private const val HELPER = "Lapp/template/extension/extension/IpcGuardHelper;"
private const val CONTEXT = "Landroid/content/Context;"
private const val RESOLVER = "Landroid/content/ContentResolver;"

private class GuardTarget(
    val className: String,
    val methodName: String,
    val parameters: String,
    val returnType: String,
    val receiverParam: String?, // null for static call sites
)

private val OUTBOUND_INTENT_TARGETS = listOf(
    GuardTarget("Landroid/content/Context;", "startActivity", "Landroid/content/Intent;", "V", CONTEXT),
    GuardTarget("Landroid/content/Context;", "startActivity", "Landroid/content/Intent;Landroid/os/Bundle;", "V", CONTEXT),
    GuardTarget("Landroid/app/Activity;", "startActivity", "Landroid/content/Intent;", "V", CONTEXT),
    GuardTarget("Landroid/app/Activity;", "startActivity", "Landroid/content/Intent;Landroid/os/Bundle;", "V", CONTEXT),
    GuardTarget("Landroid/content/ContextWrapper;", "startActivity", "Landroid/content/Intent;", "V", CONTEXT),
    GuardTarget("Landroid/content/Context;", "startService", "Landroid/content/Intent;", "Landroid/content/ComponentName;", CONTEXT),
    GuardTarget("Landroid/content/Context;", "startForegroundService", "Landroid/content/Intent;", "Landroid/content/ComponentName;", CONTEXT),
    GuardTarget("Landroid/content/Context;", "stopService", "Landroid/content/Intent;", "Z", CONTEXT),
    GuardTarget("Landroid/content/Context;", "bindService", "Landroid/content/Intent;Landroid/content/ServiceConnection;I", "Z", CONTEXT),
    GuardTarget("Landroid/content/Context;", "sendBroadcast", "Landroid/content/Intent;", "V", CONTEXT),
    GuardTarget("Landroid/content/Context;", "sendBroadcast", "Landroid/content/Intent;Ljava/lang/String;", "V", CONTEXT),
)

private val CONTENT_RESOLVER_TARGETS = listOf(
    GuardTarget(RESOLVER, "query", "Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;", "Landroid/database/Cursor;", RESOLVER),
    GuardTarget(RESOLVER, "query", "Landroid/net/Uri;", "Landroid/database/Cursor;", RESOLVER),
    GuardTarget(RESOLVER, "query", "Landroid/net/Uri;[Ljava/lang/String;Landroid/os/Bundle;Landroid/os/CancellationSignal;", "Landroid/database/Cursor;", RESOLVER),
    GuardTarget(RESOLVER, "insert", "Landroid/net/Uri;Landroid/content/ContentValues;", "Landroid/net/Uri;", RESOLVER),
    GuardTarget(RESOLVER, "update", "Landroid/net/Uri;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;", "I", RESOLVER),
    GuardTarget(RESOLVER, "delete", "Landroid/net/Uri;Ljava/lang/String;[Ljava/lang/String;", "I", RESOLVER),
    GuardTarget(RESOLVER, "call", "Landroid/net/Uri;Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;", "Landroid/os/Bundle;", RESOLVER),
    GuardTarget(RESOLVER, "call", "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;", "Landroid/os/Bundle;", RESOLVER),
    GuardTarget(RESOLVER, "openInputStream", "Landroid/net/Uri;", "Ljava/io/InputStream;", RESOLVER),
    GuardTarget(RESOLVER, "openOutputStream", "Landroid/net/Uri;", "Ljava/io/OutputStream;", RESOLVER),
    GuardTarget(RESOLVER, "openFileDescriptor", "Landroid/net/Uri;Ljava/lang/String;", "Landroid/os/ParcelFileDescriptor;", RESOLVER),
    GuardTarget(RESOLVER, "openFileDescriptor", "Landroid/net/Uri;Ljava/lang/String;Landroid/os/CancellationSignal;", "Landroid/os/ParcelFileDescriptor;", RESOLVER),
    GuardTarget(RESOLVER, "openAssetFileDescriptor", "Landroid/net/Uri;Ljava/lang/String;", "Landroid/content/res/AssetFileDescriptor;", RESOLVER),
    GuardTarget(RESOLVER, "openAssetFileDescriptor", "Landroid/net/Uri;Ljava/lang/String;Landroid/os/CancellationSignal;", "Landroid/content/res/AssetFileDescriptor;", RESOLVER),
)

private val RECEIVER_TARGETS = listOf(
    GuardTarget("Landroid/content/Context;", "registerReceiver", "Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;", "Landroid/content/BroadcastReceiver;", CONTEXT),
    GuardTarget("Landroid/content/Context;", "registerReceiver", "Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;Ljava/lang/String;", "Landroid/content/BroadcastReceiver;", CONTEXT),
    GuardTarget("Landroid/content/Context;", "registerReceiver", "Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;Ljava/lang/String;Landroid/os/Handler;", "Landroid/content/BroadcastReceiver;", CONTEXT),
)

private val PENDING_INTENT_TARGETS = listOf(
    GuardTarget("Landroid/app/PendingIntent;", "getActivity", "Landroid/content/Context;ILandroid/content/Intent;I", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getActivity", "Landroid/content/Context;ILandroid/content/Intent;ILandroid/os/Handler;", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getBroadcast", "Landroid/content/Context;ILandroid/content/Intent;I", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getBroadcast", "Landroid/content/Context;ILandroid/content/Intent;ILandroid/os/Handler;", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getService", "Landroid/content/Context;ILandroid/content/Intent;I", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getService", "Landroid/content/Context;ILandroid/content/Intent;ILandroid/os/Handler;", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getForegroundService", "Landroid/content/Context;ILandroid/content/Intent;I", "Landroid/app/PendingIntent;", null),
    GuardTarget("Landroid/app/PendingIntent;", "getForegroundService", "Landroid/content/Context;ILandroid/content/Intent;ILandroid/os/Handler;", "Landroid/app/PendingIntent;", null),
)

private val ipcGuardApplicationAttachFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PROTECTED),
    returnType = "V",
    parameters = listOf(CONTEXT),
    custom = { method, classDef ->
        method.name == "attachBaseContext" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val ipcGuardApplicationCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
    },
)

private val ipcGuardActivityCreateFingerprint = Fingerprint(
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

/** Module D — the baked-in manifest sweep. */
private val ipcGuardManifestSweepPatch = resourcePatch(
    description = "IPC guard manifest sweep: unexports providers, strips <queries> and NFC HCE services.",
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            var providers = 0
            val providerNodes = doc.getElementsByTagName("provider")
            for (i in 0 until providerNodes.length) {
                val provider = providerNodes.item(i) as? Element ?: continue
                if (provider.getAttribute("android:exported") == "true") {
                    provider.setAttribute("android:exported", "false")
                    provider.setAttribute("android:grantUriPermissions", "false")
                    providers++
                }
            }

            var queries = 0
            val queryNodes = doc.getElementsByTagName("queries")
            val queryElements = (0 until queryNodes.length).mapNotNull { queryNodes.item(it) }
            queryElements.forEach { node ->
                node.parentNode?.removeChild(node)
                queries++
            }

            var hce = 0
            val serviceNodes = doc.getElementsByTagName("service")
            for (i in 0 until serviceNodes.length) {
                val service = serviceNodes.item(i) as? Element ?: continue
                if (service.getAttribute("android:permission") == "android.permission.BIND_NFC_SERVICE") {
                    service.parentNode?.removeChild(service)
                    hce++
                }
            }

            println("[IPC guard] Manifest sweep: unexported $providers provider(s), removed $queries <queries> block(s), $hce HCE service(s).")
        }
    }
}

@Suppress("unused")
val ipcGuardPatch = bytecodePatch(
    name = "IPC guard (cross-app exfil barrier)",
    description = "A cross-app IPC barrier for suspect apps: outbound intents to other packages are dropped " +
        "(own-package traffic passes through), ContentResolver access to foreign authorities is blocked, " +
        "runtime receivers become not-exported, and notification PendingIntents aimed at other apps become " +
        "no-ops. The manifest sweep (providers unexported, <queries> stripped, HCE removed) always applies. " +
        "Breaks share sheets, link opens, pickers and cross-app logins unless the destination package is in " +
        "the allowlist. All guards are on by default — open the patch options (gear) to deselect or allowlist.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    dependsOn(ipcGuardManifestSweepPatch)
    extendWith("extensions/extension.mpe")

    val guardIntents by booleanOption(
        key = "guardIntents",
        default = true,
        title = "Guard outbound intents",
        description = "Rewrites startActivity/startService/bindService/sendBroadcast call sites through " +
            "the allowlist — the main choke point for explicit cross-app transitions.",
    )

    val guardProviders by booleanOption(
        key = "guardProviders",
        default = true,
        title = "Guard content providers",
        description = "Rewrites ContentResolver query/insert/update/delete/call/open* call sites — " +
            "foreign authorities blocked, own package and platform authorities allowed.",
    )

    val guardReceivers by booleanOption(
        key = "guardReceivers",
        default = true,
        title = "Force receivers not-exported",
        description = "Rewrites registerReceiver call sites to add RECEIVER_NOT_EXPORTED on API 33+, " +
            "closing the invisible-export receiver door.",
    )

    val guardPendingIntents by booleanOption(
        key = "guardPendingIntents",
        default = true,
        title = "Guard PendingIntents",
        description = "Rewrites PendingIntent creation sites — foreign targets become self-targeted " +
            "no-ops, so notification lures cannot reach other apps when the system fires them.",
    )

    val allowedPackages by stringOption(
        key = "ipcGuardAllowedPackages",
        default = "",
        title = "Allowed packages",
        description = "Comma-separated destination packages to allow through the barrier (your browser, " +
            "gallery picker, etc.). The first entry is used as the browser for implicit ACTION_VIEW links.",
    )

    execute {
        // Policy init at the canonical injection point, with the fallback chain
        // vomw's forceDarkTheme uses. Lazy Context capture in the helper covers
        // a missed fingerprint.
        val csv = (allowedPackages ?: "").escapeSmali()
        val initInstructions = """
            const-string v0, "$csv"
            invoke-static {p0, v0}, $HELPER->init(Landroid/content/Context;Ljava/lang/String;)V
        """.trimIndent()
        var initialized =
            ipcGuardApplicationAttachFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null ||
                ipcGuardApplicationCreateFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null ||
                ipcGuardActivityCreateFingerprint.methodOrNull?.addInstructions(0, initInstructions) != null

        fun rewrite(targets: List<GuardTarget>): Int {
            val lookup = targets.associateBy {
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
            return rewritten
        }

        val intents = if (guardIntents ?: true) rewrite(OUTBOUND_INTENT_TARGETS) else 0
        val providers = if (guardProviders ?: true) rewrite(CONTENT_RESOLVER_TARGETS) else 0
        val receivers = if (guardReceivers ?: true) rewrite(RECEIVER_TARGETS) else 0
        val pending = if (guardPendingIntents ?: true) rewrite(PENDING_INTENT_TARGETS) else 0

        println(
            "[IPC guard] init=${if (initialized) "injected" else "lazy (fingerprint missed)"} " +
                "intents=$intents providers=$providers receivers=$receivers pendingIntents=$pending.",
        )

        if (intents + providers + receivers + pending == 0) {
            println("[IPC guard] Warning: no guarded call sites rewritten — packed or reflective build?")
        }
    }
}
