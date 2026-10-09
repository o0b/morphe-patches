package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Disable cleartext traffic" patch.
 *
 * Sets android:usesCleartextTraffic="false" on <application> — the mirror of
 * the vomw/Doom "Override certificate pinning" patch, which sets it true for
 * proxy inspection. Attribute-setting idiom matches their universal manifest
 * patches (Enable Android debugging / Predictive back gesture).
 *
 * Forces all of the app's traffic to HTTPS: plaintext HTTP requests then fail
 * at the platform level. Cleartext is readable and injectable by anyone on
 * the network path (ISP, Wi-Fi operator, MITM), and it's how some ad and
 * analytics SDKs leak data outside HTTPS. No-op for apps that already
 * default to false (target SDK 28+ without an explicit true).
 *
 * ponytail: a declared android:networkSecurityConfig overrides this manifest
 * flag entirely — upgrade path is patching that config's
 * cleartextTrafficPermitted entries if it ever matters.
 */
@Suppress("unused")
val disableCleartextTrafficPatch = resourcePatch(
    name = "Disable cleartext traffic",
    description = "Sets android:usesCleartextTraffic=false on the application element, forcing all " +
        "traffic to HTTPS. Plaintext HTTP can be read and injected by anyone on the network. " +
        "Apps that legitimately use plain HTTP (local device dashboards, IoT) lose those requests.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:usesCleartextTraffic", "false")
        }
    }
}
