// recon 2026-10-11: pairip wrapper, full bodies dumped. verifyIntegrity computes the
// signing cert SHA-256 and throws SignatureTamperedException on mismatch (idx 7 filter);
// checkLicense logs a null-context guard first (idx 2 filter) then binds Play licensing.
package app.template.patches.hurry.pairip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal object SignatureVerifyFingerprint : Fingerprint(
 name = "verifyIntegrity",
 definingClass = "Lcom/pairip/SignatureCheck;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  string("SHA-256"),
 ),
)

internal object LicenseCheckFingerprint : Fingerprint(
 name = "checkLicense",
 definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  string("Cannot check license with null context."),
 ),
)
