package app.template.extension.extension;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * IPC guard policy engine — injected into patched apps by IpcGuardPatch.
 * Every guarded framework call site is rewritten to route through these
 * static methods (same signature, receiver prepended). Policy: own-package
 * traffic passes through untouched; the patch-option allowlist is the
 * escape hatch; everything else is dropped with a logcat line.
 *
 * API discipline (learned from the failed build): only signatures the
 * compile SDK actually provides are used — registerReceiver delegates via
 * the API-26 flags overload (RECEIVER_NOT_EXPORTED on 33+, 0 below);
 * PendingIntent's only 5-arg form is getActivity(..., Bundle options).
 */
public final class IpcGuardHelper {

    private static final String TAG = "IpcGuard";
    private static volatile String ownPackage = "";
    private static volatile Set<String> allowedPackages = Collections.emptySet();

    // Platform authorities stay reachable — system providers are not collusion channels.
    // ponytail: fixed system list; extend if an app legitimately needs another.
    private static final Set<String> SYSTEM_AUTHORITIES = new HashSet<>(Arrays.asList(
            "settings", "system", "media", "contacts", "calendar", "call_log",
            "downloads", "user_dictionary", "com.android.contacts",
            "com.android.calendar", "com.android.providers.downloads",
            "com.android.providers.media", "com.android.providers.settings",
            "com.android.shell"));

    private IpcGuardHelper() {
    }

    public static void init(Context context, String allowedCsv) {
        if (context != null) ownPackage = context.getPackageName();
        if (allowedCsv != null && !allowedCsv.trim().isEmpty()) {
            Set<String> parsed = new HashSet<>();
            for (String entry : allowedCsv.split(",")) {
                String pkg = entry.trim();
                if (!pkg.isEmpty()) parsed.add(pkg);
            }
            allowedPackages = parsed;
        }
    }

    private static void drop(String sink, String target) {
        Log.w(TAG, "Dropped cross-app " + sink + " -> " + target);
    }

    private static String describe(Intent intent) {
        if (intent == null) return "null";
        if (intent.getComponent() != null) return intent.getComponent().flattenToShortString();
        if (intent.getPackage() != null) return intent.getPackage();
        return String.valueOf(intent.getAction());
    }

    private static boolean intentAllowed(Context context, Intent intent) {
        if (intent == null) return false;
        if (context != null) ownPackage = context.getPackageName();
        String own = ownPackage;

        ComponentName component = intent.getComponent();
        if (component != null) {
            String pkg = component.getPackageName();
            return pkg.equals(own) || allowedPackages.contains(pkg);
        }
        String pkg = intent.getPackage();
        if (pkg != null && !pkg.isEmpty()) {
            return pkg.equals(own) || allowedPackages.contains(pkg);
        }

        String action = intent.getAction();
        if (action == null) return false;
        // Implicit ACTION_VIEW of a web link: force it to the first allowlisted
        // package — the trusted-browser companion rule.
        if (Intent.ACTION_VIEW.equals(action) && !allowedPackages.isEmpty()) {
            Uri data = intent.getData();
            String scheme = data != null ? data.getScheme() : null;
            if ("http".equals(scheme) || "https".equals(scheme)) {
                intent.setPackage(allowedPackages.iterator().next());
                return true;
            }
        }
        return false;
    }

    private static boolean authorityAllowed(String authority) {
        if (authority == null || authority.isEmpty()) return true;
        String own = ownPackage;
        if (!own.isEmpty() && (authority.equals(own) || authority.startsWith(own + "."))) return true;
        if (authority.startsWith("com.android.") || authority.startsWith("android")) return true;
        if (SYSTEM_AUTHORITIES.contains(authority)) return true;
        return allowedPackages.contains(authority);
    }

    private static boolean uriAllowed(Uri uri) {
        if (uri == null) return true;
        if (!"content".equals(uri.getScheme())) return true; // file:/android_asset is not provider IPC
        return authorityAllowed(uri.getAuthority());
    }

    private static Intent sanitizePendingIntent(Context context, Intent intent) {
        if (intent == null) return null;
        if (intentAllowed(context, intent)) return intent;
        drop("PendingIntent", describe(intent));
        // Self-targeted no-op keeps the PendingIntent constructible and the
        // notification valid — the tap just does nothing.
        return new Intent().setPackage(ownPackage);
    }

    // ─── Module A: outbound intent boundary ───

    public static void startActivity(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (intentAllowed(context, intent)) context.startActivity(intent);
        else drop("startActivity", describe(intent));
    }

    public static void startActivity(Context context, Intent intent, Bundle options) {
        if (context == null || intent == null) return;
        if (intentAllowed(context, intent)) context.startActivity(intent, options);
        else drop("startActivity", describe(intent));
    }

    public static ComponentName startService(Context context, Intent intent) {
        if (context == null || intent == null) return null;
        if (intentAllowed(context, intent)) return context.startService(intent);
        drop("startService", describe(intent));
        return null;
    }

    public static ComponentName startForegroundService(Context context, Intent intent) {
        if (context == null || intent == null) return null;
        if (intentAllowed(context, intent)) return context.startForegroundService(intent);
        drop("startForegroundService", describe(intent));
        return null;
    }

    public static boolean stopService(Context context, Intent intent) {
        if (context == null || intent == null) return false;
        if (intentAllowed(context, intent)) return context.stopService(intent);
        drop("stopService", describe(intent));
        return false;
    }

    public static boolean bindService(Context context, Intent intent, ServiceConnection connection, int flags) {
        if (context == null || intent == null) return false;
        if (intentAllowed(context, intent)) return context.bindService(intent, connection, flags);
        drop("bindService", describe(intent));
        return false;
    }

    public static void sendBroadcast(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (intentAllowed(context, intent)) context.sendBroadcast(intent);
        else drop("sendBroadcast", describe(intent));
    }

    public static void sendBroadcast(Context context, Intent intent, String receiverPermission) {
        if (context == null || intent == null) return;
        if (intentAllowed(context, intent)) context.sendBroadcast(intent, receiverPermission);
        else drop("sendBroadcast", describe(intent));
    }

    // ─── Module C: receiver lockdown ───
    // Only the plain (receiver, filter) call sites are rewritten — the
    // perm/handler overloads were removed from the compile SDK, and silently
    // dropping a receiver's broadcast permission would be a security
    // regression, so those rare call sites stay unguarded.
    // ponytail: API < 26 lacks the flags overload — registration is skipped
    // there; modern-device patch set.

    public static BroadcastReceiver registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        if (context == null || Build.VERSION.SDK_INT < 26) return null;
        int flags = Build.VERSION.SDK_INT >= 33 ? Context.RECEIVER_NOT_EXPORTED : 0;
        return context.registerReceiver(receiver, filter, flags);
    }

    // ─── Module B: content provider severance ───

    public static Cursor query(ContentResolver cr, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!uriAllowed(uri)) { drop("query", String.valueOf(uri)); return null; }
        return cr.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Cursor query(ContentResolver cr, Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        if (!uriAllowed(uri)) { drop("query", String.valueOf(uri)); return null; }
        return cr.query(uri, projection, queryArgs, cancellationSignal);
    }

    public static Uri insert(ContentResolver cr, Uri uri, ContentValues values) {
        if (!uriAllowed(uri)) { drop("insert", String.valueOf(uri)); return null; }
        return cr.insert(uri, values);
    }

    public static int update(ContentResolver cr, Uri uri, ContentValues values, String where, String[] selectionArgs) {
        if (!uriAllowed(uri)) { drop("update", String.valueOf(uri)); return 0; }
        return cr.update(uri, values, where, selectionArgs);
    }

    public static int delete(ContentResolver cr, Uri uri, String where, String[] selectionArgs) {
        if (!uriAllowed(uri)) { drop("delete", String.valueOf(uri)); return 0; }
        return cr.delete(uri, where, selectionArgs);
    }

    public static Bundle call(ContentResolver cr, Uri uri, String method, String arg, Bundle extras) {
        if (!uriAllowed(uri)) { drop("call", String.valueOf(uri)); return null; }
        return cr.call(uri, method, arg, extras);
    }

    public static Bundle call(ContentResolver cr, String authority, String method, String arg, Bundle extras) {
        if (!authorityAllowed(authority)) { drop("call", authority); return null; }
        return cr.call(authority, method, arg, extras);
    }

    public static InputStream openInputStream(ContentResolver cr, Uri uri) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openInputStream", String.valueOf(uri)); return null; }
        return cr.openInputStream(uri);
    }

    public static OutputStream openOutputStream(ContentResolver cr, Uri uri) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openOutputStream", String.valueOf(uri)); return null; }
        return cr.openOutputStream(uri);
    }

    public static ParcelFileDescriptor openFileDescriptor(ContentResolver cr, Uri uri, String mode) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openFileDescriptor", String.valueOf(uri)); return null; }
        return cr.openFileDescriptor(uri, mode);
    }

    public static ParcelFileDescriptor openFileDescriptor(ContentResolver cr, Uri uri, String mode, CancellationSignal cancellationSignal) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openFileDescriptor", String.valueOf(uri)); return null; }
        return cr.openFileDescriptor(uri, mode, cancellationSignal);
    }

    public static AssetFileDescriptor openAssetFileDescriptor(ContentResolver cr, Uri uri, String mode) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openAssetFileDescriptor", String.valueOf(uri)); return null; }
        return cr.openAssetFileDescriptor(uri, mode);
    }

    public static AssetFileDescriptor openAssetFileDescriptor(ContentResolver cr, Uri uri, String mode, CancellationSignal cancellationSignal) throws java.io.FileNotFoundException {
        if (!uriAllowed(uri)) { drop("openAssetFileDescriptor", String.valueOf(uri)); return null; }
        return cr.openAssetFileDescriptor(uri, mode, cancellationSignal);
    }

    // ─── Module E: PendingIntent de-weaponization ───
    // The only 5-arg form in the SDK is getActivity(..., Bundle options);
    // getBroadcast/getService/getForegroundService have no 5-arg overload —
    // those call sites (if any) stay unguarded.
    // ponytail: blocked targets become self-targeted no-op PendingIntents —
    // an in-app interstitial Activity showing the destination is the upgrade path.

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getActivity(context, requestCode, sanitizePendingIntent(context, intent), flags);
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags, Bundle options) {
        return PendingIntent.getActivity(context, requestCode, sanitizePendingIntent(context, intent), flags, options);
    }

    public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getBroadcast(context, requestCode, sanitizePendingIntent(context, intent), flags);
    }

    public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getService(context, requestCode, sanitizePendingIntent(context, intent), flags);
    }

    public static PendingIntent getForegroundService(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getForegroundService(context, requestCode, sanitizePendingIntent(context, intent), flags);
    }
}
