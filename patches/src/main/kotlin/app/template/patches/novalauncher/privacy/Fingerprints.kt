package app.template.patches.novalauncher.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Bugsnag / Sesame pins, Nova Launcher 8.1.6 — recon 2026-10-08 ───────────

// Bugsnag is R8-minified in this build: no com.bugsnag.android.Bugsnag
// class — the client holder is Lcom/bugsnag/android/l; (static b / d()),
// the Client is Lcom/bugsnag/android/p;. The manifest
// BugsnagContentProvider does NOT start a client (registers lifecycle
// callbacks only); the sole construction is Nova's Lb8/c0; runnable,
// which then writes Lcom/teslacoilsw/launcher/NovaApplication;->v:Z true.
//
// Delivery funnel (probe-verified caller map):
//   error files: Lc1;->j(File, x0) → Lb0;->b(x0, requestInfo)e0
//                (+ one Lca/a;->run case doing the same)
//   sessions:    Lo;->run case 0 → Ld2;->a(b2) → Lb0;->c(...)e0
//   SDK-internal: Lo;->run case 1 → Lb0;->c(...)e0
// e0 = DeliveryStatus: r = DELIVERED(0), s = UNDELIVERED(1), t = FAILURE(2)
// (probe-verified <clinit>).

// The HTTP deliverer — the only method with this signature + log string
// (unique app-wide):
//   Lcom/bugsnag/android/b0;->c(Ljava/lang/String; [B Ljava/lang/String; Ljava/util/Map;)Lcom/bugsnag/android/e0;
internal object BugsnagHttpDeliverFingerprint : Fingerprint(
    returnType = "Lcom/bugsnag/android/e0;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Ljava/lang/String;", "[B", "Ljava/lang/String;", "Ljava/util/Map;"),
    filters = listOf(
        string("Unexpected error delivering payload"),
    ),
)

// Nova's start runnable — sole method with this log string, ()V — and the
// only writer of Lcom/teslacoilsw/launcher/NovaApplication;->v:Z (the
// "error reporting enabled" flag; the sput sits after monitor-exit, in
// plain linear code).
internal object BugsnagStartFlagFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("Multiple Bugsnag.start calls detected. Ignoring."),
    ),
)

// The Sesame search-results provider names itself: Lhh/u1;->getName()
// returns "Sesame". Used to resolve the provider class; its results method
// a(Lhh/l; Ldk/d;) is then located by signature within that class (all
// five results providers share the same interface-method signature, so
// the signature alone is not unique app-wide).
internal object SesameProviderNameFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("Sesame"),
    ),
)

// The Sesame session facade Lth/r; announces itself in its query methods'
// "awaitInited false" log lines (each unique to one method). This one pins
// Lth/r;->j(Lt7/o; Ljava/lang/String; I Z I)Ljava/util/List; and is used
// only to resolve the class — the ingest method
// Lth/r;->e(Lsj/a; Lfk/c;)Ljava/lang/Object; is then located by signature
// within it (unique in the class: only the ingest takes Lsj/a;).
internal object SesameSessionFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lt7/o;", "Ljava/lang/String;", "I", "Z", "I"),
    filters = listOf(
        string("SSML searchApps called with awaitInited false"),
    ),
)
