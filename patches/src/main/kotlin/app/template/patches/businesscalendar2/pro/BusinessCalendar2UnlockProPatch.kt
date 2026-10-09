package app.template.patches.businesscalendar2.pro

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BUSINESS_CALENDAR_2_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Business Calendar 2 2.55.6 Pro unlock — probe-derived pins.
//
// Read-side patch: both prefs getters return true unconditionally, so no
// write path (billing re-verify, trial reset, subscription expiry) can
// erode the unlock and Play Billing never needs to connect. The forced
// states are the app's own: setCompleteProStatus / setForceProStatus exist
// upstream, and computeProStatus handles them as full-pro on every path.
//
// Register audit: both targets are static (Context)Z with locals v0/v1 and
// the parameter in v2 — returnEarly's index-0 injection lands in a free
// local; the original bodies become dead code (no try blocks).

@Suppress("unused")
val businessCalendar2UnlockProPatch = bytecodePatch(
 name = "Unlock Pro",
 description = "Unlocks Business Calendar 2 Pro by forcing the complete pro status and " +
  "the force-pro override to return true, routing every feature gate to the full " +
  "package (7) without touching billing.",
 default = true,
) {
 compatibleWith(BUSINESS_CALENDAR_2_COMPATIBILITY)

 execute {
  CompleteProStatusFingerprint.method.returnEarly(true)
  ForceProStatusFingerprint.method.returnEarly(true)
 }
}
