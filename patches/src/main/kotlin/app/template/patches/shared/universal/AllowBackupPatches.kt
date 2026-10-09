package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "allowBackup" patch pair — mirrors of each other.
 *
 * android:allowBackup gates the app's private data out of the device:
 * cloud Auto Backup (Android 6+), adb backup, and device-transfer
 * migrations. The platform default is TRUE (API 23+), so most apps inherit
 * allowBackup=true without declaring it.
 *
 * Disable = stop the app's databases, preferences and tokens from syncing
 * to your account's backup storage. Enable = restore the developer's opt-out,
 * usually to survive device transfers; note devs set false to protect
 * secrets and license tokens, which is exactly what the Enable patch
 * overrides — license checks may break after a restore.
 *
 * ponytail: on the Enable side, any android:dataExtractionRules /
 * android:fullBackupContent rules file still constrains what is included —
 * this patch cannot force excluded data into backup. On the Disable side,
 * allowBackup=false wins over the rules entirely.
 */
@Suppress("unused")
val disableAutoBackupPatch = resourcePatch(
    name = "Disable auto backup",
    description = "Sets android:allowBackup=false on the application element, excluding the " +
        "app's private data — databases, preferences, tokens — from cloud auto backup, " +
        "adb backup and device-transfer migrations. Apps that rely on backup-restore for " +
        "migration will lose that.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:allowBackup", "false")
        }
    }
}

@Suppress("unused")
val enableAutoBackupPatch = resourcePatch(
    name = "Enable auto backup",
    description = "Sets android:allowBackup=true on the application element, restoring the " +
        "platform default for apps that opted out — their private data joins cloud auto " +
        "backup and device transfers. The developer's exclusion of secrets and license " +
        "tokens is overridden; license checks may break after a restore.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:allowBackup", "true")
        }
    }
}
