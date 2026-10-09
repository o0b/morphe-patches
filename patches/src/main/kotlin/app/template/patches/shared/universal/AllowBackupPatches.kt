package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

/**
 * Universal "Override auto backup" master patch.
 *
 * Modeled on kveld9's Universal Offline Mode pattern (kveld9/kveld-morphe-patches,
 * GPL-3.0) — one master patch whose gear selects the direction. The two original
 * directions are mutually exclusive, so the gear is a values-map stringOption
 * instead of booleans.
 *
 * android:allowBackup gates the app's private data out of the device: cloud
 * Auto Backup (Android 6+), adb backup and device-transfer migrations. The
 * platform default is TRUE (API 23+), so Disable is the functional change for
 * most apps; Enable restores the developer's opt-out, usually to survive
 * device transfers — note devs set false to protect secrets and license
 * tokens, which Enable overrides, and license checks may break after a
 * restore.
 *
 * ponytail: on the Enable side, any android:dataExtractionRules /
 * android:fullBackupContent rules file still constrains what is included.
 * On the Disable side, allowBackup=false wins over the rules entirely.
 */
@Suppress("unused")
val overrideAutoBackupPatch = resourcePatch(
    name = "Override auto backup",
    description = "Forces android:allowBackup on the application element in the chosen " +
        "direction. Disable excludes the app's private data — databases, preferences, " +
        "tokens — from cloud auto backup, adb backup and device transfers (the privacy " +
        "direction). Enable restores the platform default for apps that opted out, " +
        "overriding the developer's protection of secrets and license tokens. Pick the " +
        "direction in the patch options (gear); default is Disable.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val mode by stringOption(
        key = "backupOverride",
        default = "disable",
        values = mapOf(
            "Disable auto backup" to "disable",
            "Enable auto backup" to "enable",
        ),
        title = "Backup override",
        description = "Which direction to force android:allowBackup.",
    )

    execute {
        val selected = mode ?: "disable"
        if (selected !in setOf("disable", "enable")) {
            println("[Override auto backup] Skipped: unknown mode '$selected'.")
            return@execute
        }

        val value = if (selected == "enable") "true" else "false"
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:allowBackup", value)
        }

        println("[Override auto backup] Forced android:allowBackup=\"$value\".")
    }
}
