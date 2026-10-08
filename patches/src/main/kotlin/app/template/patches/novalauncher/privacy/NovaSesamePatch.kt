package app.template.patches.novalauncher.privacy

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NOVA_LAUNCHER_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.returnEarly

// Nova Launcher 8.1.6 Sesame search disable — probe-derived pins.
//
// Lhh/u1; is the Sesame search-results provider (getName() returns
// "Sesame"); constructed in AppSearchResultsView.v as the first of five
// Lhh/m0; results providers. Its a(Lhh/l; Ldk/d;) is invoked by the
// provider loop in Lai/d;->invokeSuspend, which discards the return value
// unless it equals the COROUTINE_SUSPENDED sentinel — null simply
// advances the loop (probe-verified). The provider has no other external
// callers. The class is resolved via its getName() anchor because all five
// providers implement the same interface-method signature, so the
// signature alone is not unique app-wide.

@Suppress("unused")
val novaSesameSearchPatch = bytecodePatch(
    name = "Disable Sesame search results",
    description = "Removes the bundled Sesame (Branch) deep-shortcut results from Nova " +
        "Launcher's search on 8.1.6 by no-op'ing its results provider. Search still returns " +
        "apps, contacts and settings.",
    default = false, // feature-losing — opt-in
) {
    compatibleWith(NOVA_LAUNCHER_COMPATIBILITY)

    execute {
        val providerClass = mutableClassDefBy(SesameProviderNameFingerprint.method.definingClass)
        val resultsMethod = providerClass.methods.singleOrNull {
            it.name == "a" &&
                it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes.map { param -> param.toString() } == listOf("Lhh/l;", "Ldk/d;")
        } ?: throw PatchException(
            "Nova 8.1.6: a(Lhh/l; Ldk/d;)Ljava/lang/Object; not found in the Sesame " +
                "results provider. Layout changed; re-run the recon probe."
        )

        // null (not COROUTINE_SUSPENDED) tells the caller loop "completed
        // with no results" — proven by the Lai/d; loop handling.
        providerClass.findMutableMethodOf(resultsMethod).returnEarly(null)
    }
}
