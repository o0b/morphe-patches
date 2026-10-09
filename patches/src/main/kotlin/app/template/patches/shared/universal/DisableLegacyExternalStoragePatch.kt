package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Disable legacy external storage" patch.
 *
 * Sets android:requestLegacyExternalStorage="false" on <application>,
 * revoking the legacy-storage opt-out so the app runs under scoped
 * storage: its own app-specific directories plus media collections
 * through MediaStore, instead of raw paths over every app's shared files.
 *
 * Absent means false already — this only bites on apps that explicitly
 * opted in, and only when the app targets Android 10 (API 29): targets of
 * API 30+ ignore the flag entirely because scoped storage is enforced
 * regardless. The classic abusers are older sideloaded APKs.
 */
@Suppress("unused")
val disableLegacyExternalStoragePatch = resourcePatch(
    name = "Disable legacy external storage",
    description = "Sets android:requestLegacyExternalStorage=false on the application element, " +
        "confining the app to scoped storage — its own directories and media collections " +
        "instead of raw access to every app's shared files. Only affects apps targeting " +
        "Android 10; file-manager-style apps lose raw shared-storage paths.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:requestLegacyExternalStorage", "false")
        }
    }
}
