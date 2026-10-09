// recon 2026-10-09: pinned to 1.71.3 (523). Dead-after-kill audit: $6.run / $7.run are
// only ever posted from the no-op'd addAdMob methods; initAdManager / onShowAdVideo only
// call the no-op'd AdManager methods. Known-unknown: promo list-row insertion site is
// undumped — expected suppressed by the pro gates; on-device check confirms.
package app.template.patches.calendarwidget.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALENDAR_WIDGET_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val yourCalendarWidgetAdsPatch = bytecodePatch(
 name = "Your Calendar Widget: Remove ads",
 description = "Kills AdMob at every evidenced seam: auto-init provider (attachInfo), overview + settings + promotion-screen banner loaders, the timed promotion popup, and the rewarded-ad bootstrap + display (AdManager load/show). Works independently of the Pro patch.",
 default = true,
) {
 compatibleWith(CALENDAR_WIDGET_COMPATIBILITY)
 execute {
  MobileAdsInitProviderAttachInfoFingerprint.method.returnEarly()
  OverviewAddAdMobFingerprint.method.returnEarly()
  SettingsAddAdMobFingerprint.method.returnEarly()
  PromotionAddAdMobFingerprint.method.returnEarly()
  ShowPromotionPopupFingerprint.method.returnEarly(false)
  AdManagerLoadFingerprint.method.returnEarly()
  AdManagerShowFingerprint.method.returnEarly()
 }
}
