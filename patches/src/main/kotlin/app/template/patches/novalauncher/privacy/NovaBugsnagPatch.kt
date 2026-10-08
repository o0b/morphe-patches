package app.template.patches.novalauncher.privacy

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.NOVA_LAUNCHER_COMPATIBILITY
import app.template.patches.shared.findInstructionIndicesReversed
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Nova Launcher 8.1.6 Bugsnag no-op — probe-derived pins.
//
// Design: KEEP the client alive (Nova's AllAppsContainerView and the
// AnrPlugin reflective glue call the client directly — nulling it is not
// provably safe) and instead (a) return DELIVERED from both HTTP deliverers
// so no payload ever leaves the device — every caller treats ordinal 0 as
// success: Lc1;->j deletes the "sent" error file (no disk growth), the
// session tracker drops its payload — and (b) force Nova's error-reporting
// flag false at its sole write site, which skips Nova's breadcrumb usage
// and gates off the novalytics recorder (Lod/r; is constructed with that
// flag and its coroutines skip recording when it is false).
//
// Register audit (meteoblue-style injection at index 0; the original body
// becomes dead code): Lb0;->c uses v0..v9 with parameters at v5..v9, so v0
// is a free local; Lb0;->b has 3 parameter slots, so v0 aliases the last
// parameter — dead after the early return. Both writes are linear and
// verifier-safe.

private const val NOVA_ERROR_REPORT_FLAG = "Lcom/teslacoilsw/launcher/NovaApplication;->v:Z"

private fun MutableMethod.returnDelivered() {
    addInstructions(
        0,
        """
        sget-object v0, Lcom/bugsnag/android/e0;->r Lcom/bugsnag/android/e0;
        return-object v0
        """.trimIndent(),
    )
}

@Suppress("unused")
val novaBugsnagPatch = bytecodePatch(
    name = "Disable Bugsnag",
    description = "No-ops Bugsnag on Nova Launcher 8.1.6: both HTTP deliverers return " +
        "DELIVERED so no payload ever leaves the device (queued error files are deleted as " +
        "sent), and Nova's error-reporting flag is forced false — which also disables " +
        "novalytics usage recording. The client still initializes; crash data stays local-only.",
    default = true,
) {
    compatibleWith(NOVA_LAUNCHER_COMPATIBILITY)

    execute {
        val httpDeliver = BugsnagHttpDeliverFingerprint.method
        httpDeliver.returnDelivered()

        // The per-event deliverer Lb0;->b(x0, requestInfo)e0 — same class,
        // located by signature (unique within Lcom/bugsnag/android/b0;).
        val httpClass = mutableClassDefBy(httpDeliver.definingClass)
        val eventDeliver = httpClass.methods.singleOrNull {
            it.name == "b" &&
                it.returnType == "Lcom/bugsnag/android/e0;" &&
                it.parameterTypes.map { param -> param.toString() } ==
                    listOf("Lcom/bugsnag/android/x0;", "Lcom/bugsnag/android/b0;")
        } ?: throw PatchException(
            "Nova 8.1.6: Lcom/bugsnag/android/b0;->b(x0, b0)e0 not found. " +
                "Bugsnag layout changed; re-run the recon probe."
        )
        httpClass.findMutableMethodOf(eventDeliver).returnDelivered()

        // Force v = false before its sput in the start runnable: Bugsnag
        // still starts (callers expect a live client) but Nova's guarded
        // usage and the novalytics recorder see "error reporting disabled".
        val start = BugsnagStartFlagFingerprint.method
        val flagPuts = start.findInstructionIndicesReversed {
            opcode == Opcode.SPUT_BOOLEAN &&
                getReference<FieldReference>()
                    ?.let { "${it.definingClass}->${it.name}:${it.type}" } == NOVA_ERROR_REPORT_FLAG
        }
        if (flagPuts.isEmpty()) {
            throw PatchException(
                "Nova 8.1.6: no sput-boolean on $NOVA_ERROR_REPORT_FLAG in the start runnable"
            )
        }
        flagPuts.forEach { idx ->
            val valueReg = (start.getInstruction(idx) as OneRegisterInstruction).registerA
            start.addInstructions(idx, "const/4 v$valueReg, 0x0")
        }
    }
}
