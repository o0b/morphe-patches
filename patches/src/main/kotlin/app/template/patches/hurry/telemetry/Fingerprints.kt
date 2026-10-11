// recon 2026-10-11: Firebase analytics + remote config (no ad SDK exists in this app —
// the "ads" are the in-app promo card, which hides itself once "purchased" is forced).
// utils/a.a is the ONLY app->FirebaseAnalytics route (unguarded getInstance, idx 12).
// App.onCreate's RemoteConfig fetch tail holds the unique Lsz0;->a sget before the
// fetch chain (filter string at idx 161).
package app.template.patches.hurry.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal object AnalyticsLogFingerprint : Fingerprint(
 name = "a",
 definingClass = "Lcom/samruston/hurry/utils/a;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
 parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/google/firebase/analytics/FirebaseAnalytics;", name = "getInstance"),
 ),
)

internal object RemoteConfigFetchFingerprint : Fingerprint(
 name = "onCreate",
 definingClass = "Lcom/samruston/hurry/utils/App;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
 filters = listOf(
  string("minimum_fetch_interval_in_seconds"),
 ),
)
