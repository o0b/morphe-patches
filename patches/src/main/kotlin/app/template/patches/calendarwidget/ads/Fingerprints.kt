// recon 2026-10-09, build 1.71.3 (523), sha256 ff53d3ea24bf9ea86c7c4c301b732b28e27eeaad513f1aae2dcdd778254ab788
package app.template.patches.calendarwidget.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// GMS MobileAdsInitProvider — AdMob auto-init lives in attachInfo (delegates to
// zzek.attachInfo), NOT in onCreate (that body already returns false; dryrun §2).
// No-op attachInfo → no SDK init at process start.
internal object MobileAdsInitProviderAttachInfoFingerprint : Fingerprint(
 name = "attachInfo",
 definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC),
 parameters = listOf("Landroid/content/Context;", "Landroid/content/pm/ProviderInfo;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/google/android/gms/ads/internal/client/zzek;", name = "attachInfo"),
 )
)

// WidgetInstancesOverviewActivity.addAdMob():V — posts the SMART_BANNER loader (…$6.run).
internal object OverviewAddAdMobFingerprint : Fingerprint(
 name = "addAdMob",
 definingClass = "Lde/mash/android/calendar/core/activities/WidgetInstancesOverviewActivity;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PRIVATE),
 parameters = listOf(),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "isProVersion"),
 )
)

// BasePreferenceFragment.addAdMob():V — settings-screen banner loader (…$7.run →
// MobileAds.initialize + LARGE_BANNER AdView). Body: dryrun §3.
internal object SettingsAddAdMobFingerprint : Fingerprint(
 name = "addAdMob",
 definingClass = "Lde/mash/android/calendar/core/settings/fragments/BasePreferenceFragment;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PROTECTED),
 parameters = listOf(),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "isProVersion"),
 )
)

// PromotionActivity.addAdMob():V — banner on the upsell screen (guards !pro &&
// test-phase-expired; AdView.loadAd). Body: dryrun §3.
internal object PromotionAddAdMobFingerprint : Fingerprint(
 name = "addAdMob",
 definingClass = "Lde/mash/android/calendar/core/promotion/PromotionActivity;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PRIVATE),
 parameters = listOf(),
 filters = listOf(
  methodCall(definingClass = "Lcom/google/android/gms/ads/AdView;", name = "loadAd"),
 )
)

// PromotionPopupManager.showPromotionPopup(Context, I):Z — timed full-screen promo popup.
// Pro path in the original body returns false; returnEarly(false) mirrors the app's own state.
internal object ShowPromotionPopupFingerprint : Fingerprint(
 name = "showPromotionPopup",
 definingClass = "Lde/mash/android/calendar/core/ads/PromotionPopupManager;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC),
 parameters = listOf("Landroid/content/Context;", "I"),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "isProVersion"),
 )
)

// AdManager.loadRewardedVideoAd(Context, String):V — rewarded-ad bootstrap shared by the
// settings and promotion screens. No-op keeps rewardedAd null. Body: dryrun §4.
internal object AdManagerLoadFingerprint : Fingerprint(
 name = "loadRewardedVideoAd",
 definingClass = "Lde/mash/android/calendar/core/ads/AdManager;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;", name = "load"),
 )
)

// AdManager.show(Activity, Z, OnUserEarnedRewardListener):V — display seam for the rewarded
// ad; no-op also kills the "ad needs to load" toast fallback. Body: dryrun §4.
internal object AdManagerShowFingerprint : Fingerprint(
 name = "show",
 definingClass = "Lde/mash/android/calendar/core/ads/AdManager;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/app/Activity;", "Z", "Lcom/google/android/gms/ads/OnUserEarnedRewardListener;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;", name = "show"),
 )
)
