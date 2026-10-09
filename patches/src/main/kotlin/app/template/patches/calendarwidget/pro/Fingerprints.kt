// recon 2026-10-09, build 1.71.3 (523), sha256 ff53d3ea24bf9ea86c7c4c301b732b28e27eeaad513f1aae2dcdd778254ab788
package app.template.patches.calendarwidget.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Utility.isProVersion(Context, InAppProduct):Z — central read gate.
// Body (probe2 §3): CalendarWidgetPro || hasProSubscription || ProWithTaskIntegration
// || legacy pro-app (de.mash.android.calendarpro) || per-product setting.
// Forcing true unlocks every product variant per the app's own fallback chain.
// The (Context, I) overload routes through this method via getProductForWidgetId,
// so checkForProVersion()/the per-widget isProVersion cache self-heal.
internal object IsProVersionProductFingerprint : Fingerprint(
 name = "isProVersion",
 definingClass = "Lde/mash/android/calendar/core/utility/Utility;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;", "Lde/mash/android/calendar/core/purchase/InAppProduct;"),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "hasProSubscription"),
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "hasProApp"),
 )
)

// Utility.isProVersion(Context, SQLiteDatabase):Z — DB-migration overload
// (CalendarDatabaseHelper.appVersion37/43). Body (probe2 §3): CalendarWidgetPro
// setting, else legacy pro-app.
internal object IsProVersionDbFingerprint : Fingerprint(
 name = "isProVersion",
 definingClass = "Lde/mash/android/calendar/core/utility/Utility;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;", "Landroid/database/sqlite/SQLiteDatabase;"),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/utility/Utility;", name = "hasProApp"),
 )
)

// Utility.hasTaskAccess(Context):Z — task-feature gate, does NOT route through
// isProVersion. Body (probe2 §3): OR over TaskSubscriptionOneYear ||
// CalendarWidgetProAndTaskSubscriptionOneYear || TaskIntegration ||
// CalendarWidgetProWithTaskIntegration settings. Feeds initSettings (hasTaskSubscription
// cache) and TasksFragment — both confirmed by census (dryrun §5).
internal object HasTaskAccessFingerprint : Fingerprint(
 name = "hasTaskAccess",
 definingClass = "Lde/mash/android/calendar/core/utility/Utility;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Lde/mash/android/calendar/core/settings/SettingsManager;", name = "loadSetting"),
 )
)
