// recon 2026-10-11: FirebaseInitProvider is intentionally LEFT ALIVE — ui/events/a;->Z
// and App.onCreate read cached RemoteConfig via Lsz0;/Lr10 without guards and would
// crash without a FirebaseApp. Instead: no-op the app's analytics logger (single
// unguarded route) and cut the RemoteConfig fetch chain at its head with return-void
// before the unique Lsz0;->a sget (dry-run verified exactly one occurrence). The
// image-cache cleanup before the cut point is preserved; the g3 catch handler at the
// chain tail proves the cut is exception-safe. Installations/FID only fire on fetch,
// so this kills the phone-home. Content-based scan survives the pro patch's 3-instruction
// head injection in either application order. Residuals: pairip string pools,
// GMS-dead measurement workers, user-triggered share links only.
package app.template.patches.hurry.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HURRY_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val hurryTelemetryPatch = bytecodePatch(
 name = "Disable Firebase analytics and remote config",
 description = "No-ops the app's FirebaseAnalytics event logger and the startup RemoteConfig fetch (no FID, no config phone-home).",
 default = true,
) {
 compatibleWith(HURRY_COMPATIBILITY)
 execute {
  AnalyticsLogFingerprint.method.returnEarly()
  val fetchIndex = RemoteConfigFetchFingerprint.method.implementation!!.instructions
   .withIndex()
   .first { (_, instruction) ->
    val ref = (instruction as? ReferenceInstruction)?.reference as? FieldReference
    ref?.definingClass == "Lsz0;" && ref?.name == "a"
   }.index
  RemoteConfigFetchFingerprint.method.addInstructions(fetchIndex, "return-void")
 }
}
