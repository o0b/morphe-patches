package app.template.patches.wavelet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Wavelet 26.05 (versionCode 260508) — recon 2026-10-08, probes 1-4 ─────
//
// Architecture (verified by exhaustive field scans, probes 3-4):
// Wavelet's pro state is a single kotlinx MutableStateFlow<Boolean> on the
// R8-merged "PurchaseManager" class — initialized FALSE in <init>(Context)
// and written only by the suspend purchase processor e(List; Continuation)
// after local verification (purchaseState JSON + orderId probe + RSA
// SHA1withRSA signature against the dev's hardcoded public key), which
// publishes its verdict via Boolean.valueOf → StateFlow.compareAndSet and
// persists "purchased" to DataStore when the value changed. All 6 touchers
// of the flow are downstream consumers: <init>, e(), the restore early-out,
// MainViewModel "isPurchased" → MainFragment.enablePreferences(Z), session
// VM "allowPurchases" = session && !purchased, and the billing connector.
//
// Fingerprint strategy (merged design; string-anchor idea credit:
// Epxec/android-patches' Wavelet patch):
// pin ONLY developer-owned artifacts that survive R8 renames:
//   - "purchaseState" + "SHA1withRSA" — the local purchase-verification
//     strings, unique to this method across both dexes (probes 2/4),
//     declared in bytecode order (loop verification precedes signature
//     verification).
//   - signature shape with stable tokens only: (List, L)Object, public
//     final — the Continuation parameter is obfuscated and declared as
//     bare "L" per Morphe's fingerprinting docs.
// The PurchaseManager class, its <init>(Context), the FALSE-initialized
// flow factory call, and the verdict publish are located at patch time by
// framework-stable shape. NOTHING in this patch references an obfuscated
// name: on a new Wavelet release, bump WAVELET_COMPATIBILITY and re-apply;
// every step asserts layout and fails loud if the shape moved.

internal object PurchaseProcessingFingerprint : Fingerprint(
    // Lr5/k;.e(Ljava/util/List; Lkotlin/coroutines/Continuation;)Ljava/lang/Object; in 26.05.
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;", "L"), // suspend fun: List + obfuscated Continuation
    filters = listOf(
        string("purchaseState"),
        string("SHA1withRSA"),
    ),
)
