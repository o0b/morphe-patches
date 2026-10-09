package app.template.patches.businesscalendar2.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Business Calendar 2 2.55.6 (versionCode 255601) — recon 2026-10-09 ────
// Telemetry map (probes 3+4):
//  • FirebaseInitProvider bootstraps FirebaseApp at process start — and
//    only there: attachInfo() merely validates the manifest authority, so
//    no-oping onCreate() kills the only init path (→ no FID registration,
//    sessions, or measurement components). The census proves every firebase
//    reference in app code routes through FirebaseUtil.
//  • FirebaseUtil.<init> is the app's whole Firebase surface (install UUID,
//    analytics user id/properties, Crashlytics collection + user keys,
//    RemoteConfig fetchAndActivate). Every consumer null-guards the three
//    SDK fields, so an unwired singleton is inert app-wide.
//  • Fyber's UncaughtExceptionHandlerContentProvider installs FairBid's
//    crash handler at process start (wraps the default handler), independent
//    of FairBid.start — disabled here.
//  • PushNotificationsUtil arms/fires Black-Friday-style promo pushes via
//    AlertWorker alarms "push"/"clear_push".
//  • SettingsLogging's 13 log* methods are already gutted in 2.55.6 —
//    nothing to patch there.
//  • RemoteConfigDataDownloadWorker GETs a static config file from
//    appgenix.github.io (no identifiers) — left alone.

// FirebaseInitProvider.onCreate()Z — probe-verified body: sets the
// currentlyInitializing flag, FirebaseApp.initializeApp(getContext()),
// returns false on the normal path.
internal object FirebaseInitProviderFingerprint : Fingerprint(
 name = "onCreate",
 definingClass = "Lcom/google/firebase/provider/FirebaseInitProvider;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC),
)

// Fyber's UncaughtExceptionHandlerContentProvider.onCreate()Z —
// probe-verified body: ContextReference.updateContext + wraps the default
// handler in Lcom/fyber/fairbid/ba; via
// Thread.setDefaultUncaughtExceptionHandler.
internal object FyberCrashHandlerFingerprint : Fingerprint(
 name = "onCreate",
 definingClass = "Lcom/fyber/fairbid/internal/UncaughtExceptionHandlerContentProvider;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC),
 filters = listOf(
  methodCall(definingClass = "Ljava/lang/Thread;", name = "setDefaultUncaughtExceptionHandler"),
 ),
)

// FirebaseUtil.<init>(Context)V [private] — probe-verified body order:
// getUserId → AnalyticsKt.getAnalytics → FirebaseCrashlyticsKt.getCrashlytics
// → RemoteConfigKt.getRemoteConfig → fetchAndActivate. The synthetic
// (Context; DefaultConstructorMarker) constructor is excluded by the
// parameter pin.
internal object FirebaseUtilInitFingerprint : Fingerprint(
 name = "<init>",
 definingClass = "Lcom/appgenix/bizcal/util/firebase/FirebaseUtil;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/appgenix/bizcal/data/settings/SettingsHelper\$Setup;", name = "getUserId"),
  methodCall(definingClass = "Lcom/google/firebase/analytics/AnalyticsKt;", name = "getAnalytics"),
  methodCall(definingClass = "Lcom/google/firebase/crashlytics/FirebaseCrashlyticsKt;", name = "getCrashlytics"),
  methodCall(definingClass = "Lcom/google/firebase/remoteconfig/RemoteConfigKt;", name = "getRemoteConfig"),
 ),
)

// PushNotificationsUtil.schedulePushNotification(Context)V — gates →
// AlertWorker.scheduleAlarm("push").
internal object PushScheduleFingerprint : Fingerprint(
 name = "schedulePushNotification",
 definingClass = "Lcom/appgenix/bizcal/util/PushNotificationsUtil;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/appgenix/bizcal/reminder/alerts/AlertWorker;", name = "scheduleAlarm"),
 ),
)

// PushNotificationsUtil.showPushNotification(Context)V — builds the promo
// notification (NotificationCompat, category "promo", GoProActivity intent)
// and posts it via AlertUtils.notify.
internal object PushShowFingerprint : Fingerprint(
 name = "showPushNotification",
 definingClass = "Lcom/appgenix/bizcal/util/PushNotificationsUtil;",
 returnType = "V",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Lcom/appgenix/bizcal/reminder/alerts/AlertUtils;", name = "notify"),
 ),
)
