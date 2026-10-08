package app.template.patches.meteoblue.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.METEOBLUE_COMPATIBILITY

// Verified against 3.1.4 (27041) bytecode (recon 2026-10-08):
// - 3.1.x rebuilt the billing stack AND shipped it R8-minified/obfuscated:
//   the 3.0.4-era names (BillingRepository, BillingDataSource,
//   StoreState.PurchasedOK, FlowKt.flowOf, Boxing.boxBoolean) no longer exist.
// - Architecture change: there is no isPurchased(): Flow<Boolean> anymore —
//   every consumer collects the per-product purchase-state
//   StateFlow<StoreState> and checks `instanceof PurchasedOK` itself.
// - Token map (R8 names, build 27041):
//     BillingRepository(merged) Lu89;    BillingDataSource Ls20;
//     PurchasableProduct Ly20; (enum)   StoreState Lh20;  PurchasedOK Lf20;
//     StateFlow/MutableStateFlow Ljl5;/Lll5;  factory Lml5;->a(Object)Lll5;
// - ONE injection: Ls20;->c(Ly20;)Ljl5; — the purchase-state getter that BOTH
//   repository accessors (Lu89;->c and Lu89;->j) delegate to, so a single
//   site covers feature gating, the webview isPurchased flag, the store
//   screen, and the persisted purchase flag.
// - Injected body: return Lml5;->a(new Lf20(4102444800000L))
//   = MutableStateFlow(PurchasedOK). Lf20;-><init>(J)V is the constructor the
//   app itself uses on a valid purchase (Ls20;->s builds PurchasedOK from
//   purchaseTime); Lml5;->a(Ljava/lang/Object;)Lll5; is the app's own
//   MutableStateFlow factory — the same call that creates these state flows.
//   purchaseDate 4102444800000L = 2100-01-01 UTC so "valid until" style UI
//   shows a far-future date; the app only checks instanceof PurchasedOK.
// - Register audit: Ls20;->c has registers=6, ins=2 (v4=this, v5=product),
//   so v0..v3 are free locals; the injection uses v0 plus the wide pair
//   v1/v2 and needs outs >= 3 — the original already contains a 4-arg
//   invoke, so no frame surgery. The returned Lll5; implements the method's
//   declared Ljl5; return type.
// - Untouched on purpose: Lu89;->f (product-details flow — buy-button price
//   only) and Lu89;->h (billing-events flow — still emits real Play results,
//   which the StoreViewModel combine uses).

@Suppress("unused")
val meteoblueUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all meteoblue Weather premium features by making the billing " +
        "data source's purchase-state flow always report an active purchase.",
    default = true,
) {
    compatibleWith(METEOBLUE_COMPATIBILITY)

    execute {
        GetStoreStateFlowFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lf20;
                const-wide v1, 0x3BA6A7A000L
                invoke-direct {v0, v1, v2}, Lf20;-><init>(J)V
                invoke-static {v0}, Lml5;->a(Ljava/lang/Object;)Lll5;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )
    }
}
