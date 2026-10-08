package app.template.patches.wavelet.premium

import app.morphe.patcher.Fingerprint

// ─── Wavelet 26.05 (versionCode 260508) — recon 2026-10-08, probes 1-4 ─────
//
// R8-minified/obfuscated build; every name below is a build-specific
// obfuscated name pinned exactly like the version is (WAVELET_COMPATIBILITY
// pins 26.05 / 260508 only):
//   Lr5/k;  = PurchaseManager (Play Billing, SKU "wavelet.unlock_all")
//   Lr5/k;->f : Lx7/v0; = purchase-verified MutableStateFlow<Boolean>
//   Lx7/k;  = kotlinx StateFlow helpers (b = MutableStateFlow factory)
//   Lx7/v0; = MutableStateFlow impl (i = compareAndSet)
//   Le6/g1; = purchase DataStore (key l = "purchased")
//
// There is no isPro() getter: the flow IS the gate. Exhaustive field scan
// (round 4): Lr5/k;->f has exactly 6 touchers — <init> (only initializer,
// starts FALSE), e() (only writer, via Lx7/v0;->i compareAndSet plus the
// DataStore persist), f() (restore attempt; early-outs when the flow reads
// TRUE), Lh6/l; (→ "isPurchased" → MainFragment.enablePreferences(Z)),
// Lm5/v; (→ allowPurchases = session && !purchased — hides the buy flow
// once purchased), Lr5/f; (billing connect). The "purchased" DataStore key
// is touched only by write transforms — no reader bypasses the flow.
//
// Fingerprints pin by signature (definingClass + name + parameters), the
// same convention as the meteoblue 3.1.4 fingerprint. The patch bodies
// re-verify the instruction layout and fail loud on token drift.

// Lr5/k;.<init>(Landroid/content/Context;)V
//   sget-object vN, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
//   invoke-static {vN}, Lx7/k;->b(Ljava/lang/Object;)Lx7/v0;
//   move-result-object vN
//   iput-object vN, vP, Lr5/k;->f:Lx7/v0;
internal object PurchaseStateInitFingerprint : Fingerprint(
    definingClass = "Lr5/k;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

// Lr5/k;.e(Ljava/util/List; Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
//
// The suspend purchase processor: verifies each Purchase locally
// ("purchaseState" JSON + orderId probe + SHA1withRSA against the dev's
// hardcoded public key), then publishes the verdict:
//   const/4 v8, <verdict>
//   invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
//   move-result-object v6
//   invoke-virtual {v2, v4, v6}, Lx7/v0;->i(Object;Object)Z  // compareAndSet
// On GMS-less devices e() is unreachable (billing never connects), so the
// init patch alone unlocks — this target exists so that ANY run (empty
// list, billing failure, restored sandboxed Play) keeps true instead of
// flipping the flow back to false.
internal object PurchaseProcessingFingerprint : Fingerprint(
    definingClass = "Lr5/k;",
    name = "e",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/util/List;", "Ld7/c;"),
)
