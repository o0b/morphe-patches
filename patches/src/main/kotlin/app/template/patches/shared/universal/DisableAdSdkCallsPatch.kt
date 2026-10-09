package app.template.patches.shared.universal

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Universal "Disable ad SDK calls" patch.
 *
 * Ported from Doom's universal set (rushiranpise/morphe-patches, preserved
 * in the vomw fork, shared/universal/UniversalReVancedExtraPatches.kt;
 * inspired by ReVanced Patches, GPL-3.0).
 *
 * No-ops common ad SDK load/show/init/fetch methods in bundled ad packages:
 * an immediate return is inserted at method entry, so ad requests, ad
 * rendering and SDK initialization never run. Prefix + method-name matching
 * with no fingerprints — survives SDK version bumps, but broad by design:
 * apps whose ad flow blocks on a no-op'd init may stall their waterfall.
 */
@Suppress("unused")
val disableAdSdkCallsPatch = bytecodePatch(
    name = "Disable ad SDK calls",
    description = "No-ops common ad SDK load/show/init/fetch methods in bundled ad packages.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        val adPackages = listOf(
            "Lcom/applovin/",
            "Lcom/facebook/ads/",
            "Lcom/fyber/inneractive/sdk/",
            "Lcom/google/android/gms/ads/",
            "Lcom/mbridge/msdk/",
            "Lcom/inmobi/ads/",
            "Lcom/smaato/sdk/",
            "Lcom/tradplus/ads/",
            "Lcom/unity3d/ads/",
            "Lcom/unity3d/services/",
            "Lcom/vungle/",
            "Lcom/ironsource/",
            "Lcom/bytedance/sdk/",
            "Lcom/anythink/",
            "Lcom/qq/e/",
            "Lcom/baidu/mobads/",
            "Lcom/kwad/sdk/",
            "Lcom/sigmob/",
            "Lcom/pangle/",
        )
        val voidMethodNames = setOf(
            "loadAd",
            "loadAds",
            "load",
            "show",
            "showAd",
            "fetchAd",
            "init",
            "start",
            "initSDK",
            "initialize",
            "initializeSdk",
            "loadSplashAd",
            "loadRewardVideoAd",
            "loadInterstitialAd",
            "loadBannerAd",
            "loadNativeAd",
            "loadFeedAd",
            "loadNativeExpressAd",
            "loadBannerExpressAd",
            "loadDrawFeedAd",
            "loadExpressDrawFeedAd",
            "loadSplashScreenAd",
            "showSplashView",
            "showSplashClickEyeView",
            "showSplashCardView",
            "showRewardVideoAd",
            "showFullScreenVideoAd",
            "showInterstitialAd",
            "showSplashMiniWindow",
            "showSplashMiniWindowIfNeeded",
            "showNativeAd",
            "negativeFeedback",
            "startLoadAd",
        )
        val objectMethodNames = setOf(
            "getSplashView",
            "getSplashClickEyeView",
            "getSplashCardView",
            "getBannerView",
            "getFeedView",
        )
        classDefForEach { classDef ->
            if (adPackages.none { classDef.type.startsWith(it) }) return@classDefForEach
            mutableClassDefBy(classDef).methods.forEach { method ->
                if (method.name in voidMethodNames && method.returnType == "V" && method.name !in setOf("<init>", "<clinit>")) {
                    method.addInstructions(0, "return-void")
                }
                if (method.name in objectMethodNames && method.returnType.startsWith("L")) {
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                }
                if (method.name in setOf("isInitSuccess", "isSdkReady") && method.returnType == "Z") {
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
            }
        }
    }
}
