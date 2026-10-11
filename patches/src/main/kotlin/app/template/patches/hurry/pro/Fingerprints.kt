// recon 2026-10-11: hurry 30.2 (186). Entitlement = pref "purchased" (utils/c.b, pair
// b) — reader utils/c.b(Context, qs0)Z, writers utils/c.d. Verified by full-body dumps
// and dry-run: onCreate pin filter hits idx 7; ta0.f filter hits idx 88 (regs=16 ins=2).
package app.template.patches.hurry.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal object AppOnCreateFingerprint : Fingerprint(
 name = "onCreate",
 definingClass = "Lcom/samruston/hurry/utils/App;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
 filters = listOf(
  methodCall(definingClass = "Lcom/samruston/hurry/utils/c;", name = "b"),
 ),
)

internal object RestorePurchasesFingerprint : Fingerprint(
 name = "f",
 definingClass = "Lta0;",
 returnType = "Ljava/lang/Object;",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
 parameters = listOf("Ltn;"),
 filters = listOf(
  string("acknowledged"),
 ),
)
