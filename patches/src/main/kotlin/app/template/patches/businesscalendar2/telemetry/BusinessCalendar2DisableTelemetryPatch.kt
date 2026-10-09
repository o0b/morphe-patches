package app.template.patches.businesscalendar2.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BUSINESS_CALENDAR_2_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Business Calendar 2 2.55.6 telemetry disable — probe-derived pins.
//
// Design: kill Firebase at the only two seams that exist. FirebaseApp never
// initializes (provider no-op → no FID/sessions/measurement components), and
// the FirebaseUtil singleton stays unwired (no install UUID, no analytics,
// no Crashlytics keys, no RemoteConfig fetch). Every FirebaseUtil consumer
// already null-guards the SDK fields, so behavior elsewhere is unchanged;
// Remote Config readers (showAds, skipOnboardingPage, useNewGoProActivity,
// filterOutGoogleBirthdays...) return the same shipped defaults they use
// when the fetch fails. Fyber's process-start crash handler never installs,
// and the promo push notifications are disabled at both schedule and show.
//
// Register/verify audit: FirebaseUtil.<init> gets return-void at index 1 —
// AFTER the invoke-direct Object.<init>() (probe-verified instruction 0), so
// `this` is initialized per the constructor verify rule; every later branch
// and the try region shift uniformly, relative offsets unchanged.
// FirebaseInitProvider.onCreate normally returns false; returnEarly(false)
// preserves that. The two ad/telemetry providers return false as well, and
// neither authority is ever queried.

@Suppress("unused")
val businessCalendar2DisableTelemetryPatch = bytecodePatch(
 name = "Disable telemetry",
 description = "Disables Firebase completely (init provider, analytics, Crashlytics, Remote " +
  "Config wiring, install UUID), turns off the sale promo push notifications, and prevents " +
  "Fyber's crash handler from installing at process start.",
 default = true,
) {
 compatibleWith(BUSINESS_CALENDAR_2_COMPATIBILITY)

 execute {
  FirebaseInitProviderFingerprint.method.returnEarly(false)
  FyberCrashHandlerFingerprint.method.returnEarly(false)
  PushScheduleFingerprint.method.returnEarly()
  PushShowFingerprint.method.returnEarly()

  // Constructor: return-void must sit AFTER the super() call (index 0).
  FirebaseUtilInitFingerprint.method.addInstructions(1, "return-void")
 }
}
