package app.template.patches.businesscalendar2.ads

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BUSINESS_CALENDAR_2_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Business Calendar 2 2.55.6 ad removal — probe-derived pins (recon 2026-10-09).
//
// FairBid never starts (no init → no load → no show), so it makes no network
// calls and collects nothing. Probe 4 showed Facebook's Audience Network
// bootstraps itself at process start via its own content provider,
// independent of FairBid — that provider is disabled here too, so neither
// ad SDK runs at all. StoreUtil$1 (the impression listener) is only
// registered via the dead preload path. Independent of the Unlock Pro
// patch; both default to true.

@Suppress("unused")
val businessCalendar2RemoveAdsPatch = bytecodePatch(
 name = "Remove ads",
 description = "Disables Fyber FairBid completely (SDK bootstrap, interstitial preload and " +
  "show) and turns off Facebook Audience Network's auto-initialization provider.",
 default = true,
) {
 compatibleWith(BUSINESS_CALENDAR_2_COMPATIBILITY)

 execute {
  FairBidInitFingerprint.method.returnEarly()
  InterstitialPreloadFingerprint.method.returnEarly()
  InterstitialShowFingerprint.method.returnEarly(false)
  AudienceNetworkInitFingerprint.method.returnEarly(false)
 }
}
