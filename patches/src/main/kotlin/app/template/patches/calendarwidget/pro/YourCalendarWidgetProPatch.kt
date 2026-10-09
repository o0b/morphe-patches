// recon 2026-10-09: pinned to 1.71.3 (523). Other builds warn in the Manager
// and these fingerprints fail loud (PatchException) — by design.
package app.template.patches.calendarwidget.pro

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALENDAR_WIDGET_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val yourCalendarWidgetProPatch = bytecodePatch(
 name = "Your Calendar Widget: Unlock Pro",
 description = "Unlocks all Pro/task features by forcing the read-side gates (both Utility.isProVersion overloads + Utility.hasTaskAccess). Erosion-safe: purchase state lives in sticky settings that only these gates read; billing failure paths never bypass them. Without GMS, Play Billing can never resolve purchases.",
 default = true,
) {
 compatibleWith(CALENDAR_WIDGET_COMPATIBILITY)
 execute {
  IsProVersionProductFingerprint.method.returnEarly(true)
  IsProVersionDbFingerprint.method.returnEarly(true)
  HasTaskAccessFingerprint.method.returnEarly(true)
 }
}
