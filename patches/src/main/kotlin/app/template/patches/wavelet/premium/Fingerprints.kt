package app.template.patches.wavelet.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── Wavelet 26.05 (versionCode 260508) — recon 2026-10-08, probes 1-4 ─────
//
// There is no isPro() getter to stub: Wavelet's pro state is a single
// kotlinx MutableStateFlow<Boolean> — Lr5/k;->f (the Play Billing
// "PurchaseManager"), initialized FALSE in <init> and written only by the
// purchase-processing coroutine e() after local verification (purchaseState
// JSON + orderId probe + RSA-SHA1 against the dev's hardcoded public key).
//
// Exhaustive field scan (round 4): Lr5/k;->f has exactly 6 touchers —
// <init>, e(), f() (restore attempt; early-outs when the flow reads TRUE),
// Lh6/l; (→ "isPurchased" → MainFragment.enablePreferences(Z)), and Lm5/v;
// (→ allowPurchases = session && !purchased, which hides the buy flow once
// purchased). The "purchased" DataStore key (Le6/g1;->l) is touched only by
// write transforms — no reader bypasses the flow.
//
// ─── Target 1: PurchaseStateInitFingerprint — Lr5/k;.<init>(Context)V ─────
//
//   sget-object v3, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
//   invoke-static {v3}, Lx7/k;->b(Ljava/lang/Object;)Lx7/v0;  // MutableStateFlow(false)
//   move-result-object v3
//   iput-object v3, v2, Lr5/k;->f:Lx7/v0;
//
// Fingerprint strategy: <init> is the only writer of Lr5/k;->f and the only
// method combining that iput-object with the Lx7/k;->b StateFlow factory —
// exactly 1 match across both dexes of 26.05.
internal object PurchaseStateInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        // iput-object v3, v2, Lr5/k;->f:Lx7/v0;
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "Lr5/k;",
            name = "f",
        ),
        // invoke-static {v3}, Lx7/k;->b(Ljava/lang/Object;)Lx7/v0;
        methodCall(
            definingClass = "Lx7/k;",
            name = "b",
        ),
    ),
)

// ─── Target 2: PurchaseProcessingFingerprint —
// Lr5/k;.e(Ljava/util/List; Lkotlin/coroutines/Continuation;)Ljava/lang/Object; ──
//
//   const/4 v8, 0                       ← verdict register (forced to 1)
//   invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
//   move-result-object v6
//   invoke-virtual {v2, v4, v6}, Lx7/v0;->i(Object;Object)Z  // compareAndSet
//   → and (if changed) persists "purchased"=v8 via the Le6/x0; edit lambda.
//
// On GMS-less devices e() is unreachable (billing never connects), so the
// init patch alone unlocks — this target exists so that ANY run (empty list,
// billing failure, restored sandboxed Play) keeps true instead of flipping
// the flow back to false.
//
// Fingerprint strategy: "purchaseState" + "SHA1withRSA" co-occur only here —
// the embedded Play Billing library contains neither. Parameters pin the
// suspend shape (List, Continuation). Exactly 1 match.
internal object PurchaseProcessingFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;", "Ld7/c;"),
    filters = listOf(
        string("purchaseState"),
        string("SHA1withRSA"),
        // invoke-virtual {v2, v4, v6}, Lx7/v0;->i(Ljava/lang/Object;Ljava/lang/Object;)Z
        methodCall(
            definingClass = "Lx7/v0;",
            name = "i",
        ),
    ),
)
