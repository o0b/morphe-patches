// recon 2026-10-11: write-side force + eraser kill. Only writers of pref "purchased":
// ta0.e (purchase completed → true, becomes inert) and ta0.f (billing re-verify whose
// 30s-timeout path writes false when Play is unreachable — the sole eraser, killed).
// Register audit (dry-run): App.onCreate regs=9 ins=1, v8=p0, v0/v1 dead at entry;
// ta0.f regs=16 ins=2, v0 dead at entry; Ldf1;->a is f's own success sentinel ([109]).
// Note: the theme picker's isPro() lock is dead code in 30.2 (result discarded, no
// move-result; swatch e=0 always) — documented, no extra seam. The only live consumer
// of "purchased" is the promo card gate in ui/events/a;->Z plus the settings billing row.
package app.template.patches.hurry.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HURRY_COMPATIBILITY

@Suppress("unused")
val hurryProPatch = bytecodePatch(
 name = "Unlock pro features",
 description = "Forces the app's own \"purchased\" preference on at process start and neutralizes the billing re-verify that erases it without Play services.",
 default = true,
) {
 compatibleWith(HURRY_COMPATIBILITY)
 execute {
  AppOnCreateFingerprint.method.addInstructions(
   0,
   """
    sget-object v0, Lcom/samruston/hurry/utils/c;->b:Lqs0;
    const/4 v1, 1
    invoke-static {p0, v0, v1}, Lcom/samruston/hurry/utils/c;->d(Landroid/content/Context;Lqs0;Z)V
   """.trimIndent(),
  )
  RestorePurchasesFingerprint.method.addInstructions(
   0,
   """
    sget-object v0, Ldf1;->a:Ldf1;
    return-object v0
   """.trimIndent(),
  )
 }
}
