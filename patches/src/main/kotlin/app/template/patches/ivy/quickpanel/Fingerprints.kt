// recon 2026-11-09
package app.template.patches.ivy.quickpanel

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

internal object QuickPanelViewAnimateInFingerprint : Fingerprint(
 name = "animateIn",
 definingClass = "Lcom/stinger/ivy/quickpanel/QuickPanelView;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC),
 parameters = listOf(),
 filters = listOf(
  methodCall(definingClass = "Lcom/stinger/ivy/rss/RssCrawl;", name = "start"),
 ),
)

internal object QuickPanelViewAnimateOutFingerprint : Fingerprint(
 name = "animateOut",
 definingClass = "Lcom/stinger/ivy/quickpanel/QuickPanelView;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC),
 parameters = listOf("Landroid/view/View;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/stinger/ivy/rss/RssCrawl;", name = "stop"),
 ),
)
