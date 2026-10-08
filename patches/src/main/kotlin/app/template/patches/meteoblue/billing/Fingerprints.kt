package app.template.patches.meteoblue.billing

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Anchor: per-product purchase-state flow getter ────────────────────────
// Verified against 3.1.4 (27041) bytecode (recon 2026-10-08).
//
// meteoblue 3.1.x ships R8-minified/obfuscated code, so every name below is a
// build-specific obfuscated name:
//   Ls20;  = BillingDataSource   (per-product state-flow map H, setState p)
//   Ly20;  = PurchasableProduct (enum AD_FREE_SUBSCRIPTION, Play id
//            "meteoblue.adfree.subscribtion.oneyear.oneeuro")
//   Ljl5;  = StateFlow, Lll5; = MutableStateFlow (StateFlowImpl)
//   Lh20;  = StoreState base; Lf20; = StoreState.PurchasedOK (<init>(J)V)
//   Le20;->c = NotPurchasedOK singleton; La20;..Ld20; = error states
// Obfuscated names are pinned exactly like the version is: this fingerprint
// only ever matches build 27041, which METEOBLUE_COMPATIBILITY pins.
//
// Ls20;->c(Ly20;)Ljl5; is the single source of purchase state for the whole
// app — both accessors on the merged BillingRepository (Lu89;) delegate here:
//   Lu89;->c(Ly20;)Lj20;  -> AppMainViewModel, WebViewViewModel, Luw
//   Lu89;->j(Ly20;)Ljl5;  -> StoreViewModel (store screen + persisted
//                            purchase flag, combined with Lu89;->h() events)
// The 3.0.4-era isPurchased(): Flow<Boolean> no longer exists; consumers
// collect this state flow and check `instanceof PurchasedOK` themselves.
// The descriptor + flags are unique on Ls20; (only one method named c).

internal object GetStoreStateFlowFingerprint : Fingerprint(
    definingClass = "Ls20;",
    name = "c",
    returnType = "Ljl5;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ly20;"),
)
