package app.template.extension.extension;

import android.content.Context;
import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Network allowlist policy engine — injected into patched apps by
 * NetworkAllowlistPatch. Every DNS-resolution call site in the app's dex is
 * rewritten to route through these static methods (same signature — both are
 * static, so no receiver is prepended). Policy: allowlisted hosts resolve
 * for real; everything else resolves to 0.0.0.0 (the Morning-Entree hosts
 * sink), so no traffic leaves the device except to the hosts you list.
 *
 * Entries are suffix-matched case-insensitively: "example.com" allows the
 * apex and every subdomain (www.example.com, api.example.com, ...).
 *
 * ponytail: linear scan per lookup — fine for hand-sized allowlists;
 * a trie if you ever paste thousands of entries.
 */
public final class NetworkGuardHelper {

    private static final String TAG = "NetworkGuard";
    private static volatile Set<String> allowedHosts = Collections.emptySet();

    private NetworkGuardHelper() {
    }

    public static void init(Context context, String csv) {
        if (csv == null || csv.trim().isEmpty()) return;
        Set<String> parsed = new HashSet<>();
        for (String entry : csv.split(",")) {
            String host = entry.trim().toLowerCase(Locale.ROOT);
            if (!host.isEmpty()) parsed.add(host);
        }
        allowedHosts = parsed;
    }

    private static boolean hostAllowed(String host) {
        if (host == null) return false;
        String h = host.trim().toLowerCase(Locale.ROOT);
        if (h.isEmpty()) return false;
        if (allowedHosts.contains(h)) return true;
        for (String allowed : allowedHosts) {
            if (h.endsWith("." + allowed)) return true;
        }
        return false;
    }

    public static InetAddress getByName(String host) throws UnknownHostException {
        if (hostAllowed(host)) {
            return InetAddress.getByName(host);
        }
        Log.w(TAG, "DNS sink: " + host);
        // Keep the queried name on the sink address so error surfaces stay
        // meaningful; never returns null (call sites deref the result).
        return InetAddress.getByAddress(host, new byte[]{0, 0, 0, 0});
    }

    public static InetAddress[] getAllByName(String host) throws UnknownHostException {
        if (hostAllowed(host)) {
            return InetAddress.getAllByName(host);
        }
        Log.w(TAG, "DNS sink: " + host);
        // Non-empty array: callers iterate and may index [0].
        return new InetAddress[]{ InetAddress.getByAddress(host, new byte[]{0, 0, 0, 0}) };
    }
}
