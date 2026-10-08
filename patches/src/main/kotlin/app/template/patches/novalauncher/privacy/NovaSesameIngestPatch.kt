package app.template.patches.novalauncher.privacy

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NOVA_LAUNCHER_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf

// Nova Launcher 8.1.6 Sesame integration disable — probe-derived pins.
//
// Lth/r; is the Sesame session facade: f (Channel) = init signal, g()/a(d)
// = "awaitInited", f(d) = the single engine init, and
// e(Lsj/a; Lfk/c;) = the integration ingest — which FIRST awaits init and
// whose own "awaitInited false" early-out returns new Integer(0).
//
// Design: return that same Integer(0) unconditionally. Every caller is
// probe-verified to handle it as the designed "0 results refreshed"
// answer: the OAuth-completion flows (La0/h; case 0 → "N results" toast,
// Lb0/h0;) and the periodic per-integration refresh (Lcg/q; ×2). No
// ingestIntegration() call ever runs, so Spotify / OneDrive / Twitch /
// Slack / Discord / Dropbox / Deezer / GitHub are never contacted.
//
// Deliberately NOT touched: the engine init f(d) (local-only indexing —
// contacts, shortcuts, usage stats — keeps Sesame search working) and the
// lateinit accessor c() (throws when uninitialized; some ungated callers
// exist). States stay consistent: g()/a() still report initialized.
//
// Register audit: Lth/r;->e uses v0..v7 with parameters at v0=this,
// v1=Lsj/a;, v2=Lfk/c; — v0 is a free local, and the original first
// instruction already writes it (dead code after the early return).

@Suppress("unused")
val novaSesameIngestPatch = bytecodePatch(
    name = "Disable Sesame integrations",
    description = "Disables Nova Launcher 8.1.6's Sesame third-party integrations " +
        "(Spotify, OneDrive, Twitch, Slack, Discord, Dropbox, Deezer, GitHub): the " +
        "integration ingest always returns 0 items, so no background or manual data " +
        "pulls run and their APIs are never contacted. The Sesame engine and local " +
        "search indexing continue to work.",
    default = false, // feature-losing for integration users — opt-in
) {
    compatibleWith(NOVA_LAUNCHER_COMPATIBILITY)

    execute {
        val sesameClass = mutableClassDefBy(SesameSessionFingerprint.method.definingClass)
        val ingestMethod = sesameClass.methods.singleOrNull {
            it.name == "e" &&
                it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes.map { param -> param.toString() } == listOf("Lsj/a;", "Lfk/c;")
        } ?: throw PatchException(
            "Nova 8.1.6: Sesame ingest e(Lsj/a; Lfk/c;)Ljava/lang/Object; not found in the " +
                "session facade resolved by SesameSessionFingerprint. Layout changed; " +
                "re-run the recon probe."
        )

        // Integer 0 — the method's own "awaitInited false" early-out.
        sesameClass.findMutableMethodOf(ingestMethod).addInstructions(
            0,
            """
            const/4 v0, 0x0
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
            """.trimIndent(),
        )
    }
}
