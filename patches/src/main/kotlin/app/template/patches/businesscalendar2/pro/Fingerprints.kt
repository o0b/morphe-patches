package app.template.patches.businesscalendar2.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Business Calendar 2 2.55.6 (versionCode 255601) — recon 2026-10-09 ────
// BC2 ships unobfuscated. Pro state is a feature-set bitmask cached in
// ProUtil.sFeatureSet (PACKAGE_FULL = 7), derived on every read from
// SettingsHelper$ProStatus prefs getters:
//   getCompleteProStatus → pref "result" (sticky "full pro purchased")
//   getForceProStatus    → pref "status" (developer force-pro override)
// The read chain (ProUtil.getCurrentFeatureSet / ...AndCheckForProApps)
// checks both before the Pro flavor / trial / legacy com.appgenix.bizcal.pro
// key-app / billing paths, and computeProStatus routes forcePro=true into
// the app's own full-unlock block (sFeatureSet |= 7, setCompleteProStatus,
// hideGoProItemInDrawer) before any downgrade logic. Forcing both true
// makes every isFeatureEnabled / isAnyProFeatureEnabled / isFullProEnabled
// gate pass without Play Billing ever connecting — the GrapheneOS case.

// Target 1: getCompleteProStatus(Context)Z — public static, reads boolean
// "result" from default SharedPreferences. Filters in body order
// (probe-verified): getDefaultSharedPreferences → "result" → getBoolean.
internal object CompleteProStatusFingerprint : Fingerprint(
 name = "getCompleteProStatus",
 definingClass = "Lcom/appgenix/bizcal/data/settings/SettingsHelper\$ProStatus;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Landroidx/preference/PreferenceManager;", name = "getDefaultSharedPreferences"),
  string("result"),
  methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
 ),
)

// Target 2: getForceProStatus(Context)Z — public static, reads boolean
// "status" (const-string/jumbo in this build; the string filter matches
// both const-string variants).
internal object ForceProStatusFingerprint : Fingerprint(
 name = "getForceProStatus",
 definingClass = "Lcom/appgenix/bizcal/data/settings/SettingsHelper\$ProStatus;",
 returnType = "Z",
 accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
 parameters = listOf("Landroid/content/Context;"),
 filters = listOf(
  methodCall(definingClass = "Landroidx/preference/PreferenceManager;", name = "getDefaultSharedPreferences"),
  string("status"),
  methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
 ),
)
