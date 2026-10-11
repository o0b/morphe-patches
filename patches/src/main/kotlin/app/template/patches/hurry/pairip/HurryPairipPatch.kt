// recon 2026-10-11: required for any re-signed build. SignatureCheck.verifyIntegrity
// throws SignatureTamperedException from pairip Application.attachBaseContext; without
// it Morphe's re-sign kills the process at bind time. LicenseClient.checkLicense retries
// 3x then shows LicenseActivity (ERROR_DIALOG) and scheduleAppShutdown (30s kill) when
// Play licensing is unreachable — i.e. always on a de-Googled device.
// Intentionally untouched: VMRunner.setContext and StartupLauncher — they decrypt the
// const-string pool (several pref keys load from it) and the manifest receivers call
// VMRunner.invoke directly.
package app.template.patches.hurry.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HURRY_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val hurryPairipPatch = bytecodePatch(
 name = "Disable Pairip integrity and license checks",
 description = "Removes the signature-tamper crash and the Play-licensing error dialog and shutdown that fire without Google Play services.",
 default = true,
) {
 compatibleWith(HURRY_COMPATIBILITY)
 execute {
  SignatureVerifyFingerprint.method.returnEarly()
  LicenseCheckFingerprint.method.returnEarly()
 }
}
