package app.template.patches.businesscalendar2.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Business Calendar 2 2.55.6 (versionCode 255601) — recon 2026-10-09 ────
// Monetization is Fyber FairBid interstitials (with Facebook Audience Network
// mediation); all Play-flavor seams live in StoreUtil (other store flavors
// stub the same methods empty):
//   initializeMobileAds(Activity)V         — FairBid bootstrap
//   preloadInterstitialAd(Activity)V       — gated placement load
//   showInterstitialAd(Activity; String)Z  — gated show
// pauseAds / resumeAds / destroyInterstitialAd / updateConsent are already
// empty flavor stubs and alwaysShowAds is const-false — nothing to patch.
// Probe 4: Facebook's AudienceNetworkContentProvider separately bootstraps
// the FB ad SDK at process start (DynamicLoaderFactory.initialize),
// independent of FairBid — pinned here as well.

// StoreUtil.initializeMobileAds(Activity)V — body order (probe-verified):
// UserInfo.setGdprConsent → FairBid.configureForAppId(R.string.fyber_app_id)
// → FairBid.start(context).
internal object FairBidInitFingerprint : Fingerprint(
 name = "initializeMobileAds",
 definingClass = "Lcom/appgenix/bizcal/util/StoreUtil;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/app/Activity;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/fyber/fairbid/user/UserInfo;", name = "setGdprConsent"),
  methodCall(definingClass = "Lcom/fyber/FairBid;", name = "configureForAppId"),
  methodCall(definingClass = "Lcom/fyber/FairBid;", name = "start"),
 ),
)

// StoreUtil.preloadInterstitialAd(Activity)V — AdsUtil.showAdsForUser gate →
// Fyber Interstitial.request with the placement id.
internal object InterstitialPreloadFingerprint : Fingerprint(
 name = "preloadInterstitialAd",
 definingClass = "Lcom/appgenix/bizcal/util/StoreUtil;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/app/Activity;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/appgenix/bizcal/util/AdsUtil;", name = "showAdsForUser"),
  methodCall(definingClass = "Lcom/fyber/fairbid/ads/Interstitial;", name = "request"),
 ),
)

// StoreUtil.showInterstitialAd(Activity; String)Z — same gates then
// Interstitial.isAvailable / Interstitial.show; forced false so callers
// take the plain "no ad" path they already take for pro users.
internal object InterstitialShowFingerprint : Fingerprint(
 name = "showInterstitialAd",
 definingClass = "Lcom/appgenix/bizcal/util/StoreUtil;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/app/Activity;", "Ljava/lang/String;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/appgenix/bizcal/util/AdsUtil;", name = "showAdsForUser"),
  methodCall(definingClass = "Lcom/appgenix/bizcal/util/StoreUtil;", name = "getFyberPlacementId"),
  methodCall(definingClass = "Lcom/fyber/fairbid/ads/Interstitial;", name = "show"),
 ),
)

// AudienceNetworkContentProvider.onCreate()Z — probe-verified body:
// DynamicLoaderFactory.initialize(context, null, null, true). Providers run
// at process start no matter what, so this is the FB ad SDK's bootstrap
// outside the FairBid flow; the provider normally returns false.
internal object AudienceNetworkInitFingerprint : Fingerprint(
 name = "onCreate",
 definingClass = "Lcom/facebook/ads/AudienceNetworkContentProvider;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC),
 filters = listOf(
  methodCall(definingClass = "Lcom/facebook/ads/internal/dynamicloading/DynamicLoaderFactory;", name = "initialize"),
 ),
)
