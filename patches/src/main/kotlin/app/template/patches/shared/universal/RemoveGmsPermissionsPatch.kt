package app.template.patches.shared.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Universal "Remove GMS permissions" master patch.
 *
 * Modeled on kveld9's Universal Offline Mode / Universal Privacy Permissions
 * Stripper pattern (kveld9/kveld-morphe-patches, GPL-3.0): one master patch
 * whose gear-dialog boolean toggles select which permissions get stripped.
 * All toggles default to true, so enabling the patch alone removes the
 * entire Google Play-defined set — open the gear to deselect specific ones.
 *
 * All five are custom permissions defined by GMS-side packages (Play Store,
 * Play Services, Google Services Framework): never granted where the
 * defining package is absent, so this is dead weight stripped on GMS-free
 * devices like GrapheneOS without sandboxed Play. On Play devices each
 * removal kills its capability: ad-ID reads, in-app billing, install
 * attribution, push delivery, GSF settings sync.
 */

private const val AD_ID = "com.google.android.gms.permission.AD_ID"
private const val BILLING = "com.android.vending.BILLING"
private const val INSTALL_REFERRER =
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"
private const val C2DM_RECEIVE = "com.google.android.c2dm.permission.RECEIVE"
private const val GSF_READ = "com.google.android.providers.gsf.permission.READ_GSERVICES"

@Suppress("unused")
val removeGmsPermissionsPatch = resourcePatch(
    name = "Remove GMS permissions",
    description = "Strips the Google Play-defined permission declarations from the manifest: " +
        "advertising ID, Play Billing, install referrer attribution, push (C2DM/FCM) and " +
        "Google Services Framework sync. Inert on devices without Google Play; on Play " +
        "devices it kills attribution, billing, push and ad-ID reads. All removals are on " +
        "by default — open the patch options (gear) to deselect specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripAdId by booleanOption(
        key = "stripAdId",
        default = true,
        title = "Remove AD_ID (Google Play Services)",
        description = "The cross-app advertising identifier ad SDKs profile against. " +
            "With the declaration gone, apps read a zeroed ID.",
    )

    val stripBilling by booleanOption(
        key = "stripBilling",
        default = true,
        title = "Remove BILLING (Google Play)",
        description = "Play Billing gate for in-app purchases and subscriptions — already " +
            "broken in re-signed patched APKs regardless.",
    )

    val stripInstallReferrer by booleanOption(
        key = "stripInstallReferrer",
        default = true,
        title = "Remove BIND_GET_INSTALL_REFERRER_SERVICE",
        description = "Play Install Referrer API: the campaign and attribution token that " +
            "matches installs to ad clicks.",
    )

    val stripPush by booleanOption(
        key = "stripPush",
        default = true,
        title = "Remove C2DM RECEIVE (push)",
        description = "GCM/FCM push delivery. On devices with Play — including GrapheneOS's " +
            "sandboxed Play Services — removal silences the app's push notifications.",
    )

    val stripGoogleServicesFramework by booleanOption(
        key = "stripGoogleServicesFramework",
        default = true,
        title = "Remove READ_GSERVICES (Google Services Framework)",
        description = "The legacy Google Services Framework account-data gate, still " +
            "declared by play-services era apps.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripAdId ?: true) blocked.add(AD_ID)
        if (stripBilling ?: true) blocked.add(BILLING)
        if (stripInstallReferrer ?: true) blocked.add(INSTALL_REFERRER)
        if (stripPush ?: true) blocked.add(C2DM_RECEIVE)
        if (stripGoogleServicesFramework ?: true) blocked.add(GSF_READ)

        if (blocked.isEmpty()) {
            println("[Remove GMS permissions] Skipped: no removals selected in patch options.")
            return@execute
        }

        var removed = 0
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0)
            val permissions = manifest.childNodes
            val toRemove = mutableListOf<Node>()

            for (i in 0 until permissions.length) {
                val node = permissions.item(i) as? Element ?: continue
                if (node.tagName == "uses-permission" &&
                    node.getAttribute("android:name") in blocked
                ) {
                    toRemove.add(node)
                }
            }

            toRemove.forEach { manifest.removeChild(it) }
            removed = toRemove.size
        }

        if (removed == 0) {
            println("[Remove GMS permissions] No GMS permission declarations found (already GMS-free).")
        } else {
            println("[Remove GMS permissions] Stripped $removed GMS permission declaration(s).")
        }
    }
}
