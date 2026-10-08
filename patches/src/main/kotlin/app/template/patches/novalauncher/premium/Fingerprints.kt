package app.template.patches.novalauncher.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Nova Launcher 8.1.6 (versionCode 81006) — recon 2026-10-08 ─────────────
// Classic unlocker flow: signature-verified com.teslacoilsw.launcher.prime
// app (sig hashCodes -1577060375 / 763259628) + Nova's own license client
// (Lbj/g; binds .prime.NovaLauncherPrimeService; api.novalauncher.com/
// verifyLicense). No "nova_billing"/"subscription_active"/"nova_prime_grant"/
// "last_verified" strings exist in this build — the 8.8.8-era subscription
// and grant layers are absent. The runtime prime flag is
// Lzg/t1;->y:Z (twin: ->t:Z), read directly app-wide from the settings
// singleton Lzg/l2;->a — there is NO central ()Z gate in 8.1.6, so the
// unlock forces the field at its writers.

// Target 1: prime-state initializer — sole method holding
// "ro.razer.internal.api" (unique app-wide):
//   Lzg/t1;->a(Landroid/content/SharedPreferences;)V [public final]
// Derives y from pref "1" (512 = licensed) or sets y=true on Razer builds;
// t = (pref == 512). Writes: y ×3, t ×1.
internal object PrimeStateInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/SharedPreferences;"),
    filters = listOf(
        string("ro.razer.internal.api"),
    ),
)

// Target 2: prime-unlocker package-change handler (runnable posted by the
// PACKAGE_ADDED/REMOVED receiver Lne/c;): verifies the Prime app
// signature and writes the licensed state. The two pinned strings co-occur
// in exactly two methods app-wide; the other (Laa/d;->h(I)V) is excluded by
// the empty parameter pin:
//   Lyd/x0;->run()V [public final]
// Writes: y ×2, t ×1.
internal object PrimeAppChangeFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("com.teslacoilsw.launcher.prime"),
        string("nova_prime"),
    ),
)
