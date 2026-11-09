// recon 2026-11-09
package app.template.patches.ivy.quickpanel

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.IVY_COMPATIBILITY

@Suppress("unused")
val ivyQuickPanelNoTransitionPatch = bytecodePatch(
 name = "Remove Quick Panel transition animation",
 description = "Removes the code-driven open and close animations from the Quick Panel while keeping the RSS start and stop behavior.",
 default = true,
) {
 compatibleWith(IVY_COMPATIBILITY)

 execute {
  // animateIn(): skip the blur animation, keep RssCrawl.start().
  QuickPanelViewAnimateInFingerprint.method.addInstructions(
   0,
   """
   iget-object v0, v2, Lcom/stinger/ivy/quickpanel/QuickPanelView;->mRss:Lcom/stinger/ivy/rss/RssCrawl;
   invoke-virtual {v0}, Lcom/stinger/ivy/rss/RssCrawl;->start()V
   return-void
   """.trimIndent(),
  )

  // animateOut(): skip the blur animation, keep RssCrawl.stop().
  QuickPanelViewAnimateOutFingerprint.method.addInstructions(
   0,
   """
   iget-object v2, v1, Lcom/stinger/ivy/quickpanel/QuickPanelView;->mRss:Lcom/stinger/ivy/rss/RssCrawl;
   invoke-virtual {v2}, Lcom/stinger/ivy/rss/RssCrawl;->stop()V
   return-void
   """.trimIndent(),
  )
 }
}
