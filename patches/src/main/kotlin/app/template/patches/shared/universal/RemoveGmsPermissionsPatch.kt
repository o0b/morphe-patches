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
 * All targets are custom permissions defined by GMS-side packages (Play
 * Store, Play Services, Google Services Framework), never granted where the
 * defining package is absent — dead weight on GMS-free devices like
 * GrapheneOS without sandboxed Play. On Play devices each removal kills its
 * capability: ad-ID reads, in-app billing, license checks, install
 * attribution, push delivery, legacy account scopes, motion profiling,
 * GSF settings sync.
 *
 * Matching is exact for fixed names, plus prefix (GOOGLE_AUTH scope family)
 * and suffix (<package>.permission.C2D_MESSAGE) matchers for the
 * per-app-named families. Only <uses-permission> declarations are stripped;
 * inert <permission> definition elements are left in place.
 */

private const val AD_ID = "com.google.android.gms.permission.AD_ID"
private const val BILLING = "com.android.vending.BILLING"
private const val CHECK_LICENSE = "com.android.vending.CHECK_LICENSE"
private const val INSTALL_REFERRER =
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"
private const val C2DM_RECEIVE = "com.google.android.c2dm.permission.RECEIVE"
private const val C2DM_SEND = "com.google.android.c2dm.permission.SEND"
private const val C2D_MESSAGE_SUFFIX = ".permission.C2D_MESSAGE"
private const val GSF_READ = "com.google.android.providers.gsf.permission.READ_GSERVICES"
private const val LEGACY_ACTIVITY_RECOGNITION =
    "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
private const val GOOGLE_AUTH_PREFIX =
    "com.google.android.googleapps.permission.GOOGLE_AUTH"

@Suppress("unused")
val removeGmsPermissionsPatch = resourcePatch(
    name = "Remove GMS permissions",
    description = "Strips the Google Play-defined permission declarations from the manifest: " +
        "advertising ID, Play Billing, legacy license checks, install referrer attribution, " +
        "push (C2DM), legacy account auth scopes, legacy activity recognition and Google " +
        "Services Framework sync. Inert on devices without Google Play; on Play devices it " +
        "kills the matching capability. All removals are on by default — open the patch " +
        "options (gear) to deselect specific ones.",
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

    val stripCheckLicense by booleanOption(
        key = "stripCheckLicense",
        default = true,
        title = "Remove CHECK_LICENSE (Play Licensing)",
        description = "The legacy License Verification Library gate, still declared by old " +
            "paid apps. On Play devices a stripped declaration makes the license check " +
            "throw — the wall that unlock patches replace. Inert without Play.",
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
        title = "Remove C2DM permissions (push)",
        description = "GCM/FCM push delivery (com.google.android.c2dm.permission.RECEIVE) " +
            "and the sender-side SEND permission. On devices with Play — including " +
            "GrapheneOS's sandboxed Play Services — removal silences the app's push " +
            "notifications.",
    )

    val stripC2dMessage by booleanOption(
        key = "stripC2dMessage",
        default = true,
        title = "Remove C2D_MESSAGE (app-scoped push lock)",
        description = "The per-app signature permission from the classic GCM client setup " +
            "(<package>.permission.C2D_MESSAGE) that locks registration to the app's " +
            "signature. Pairs with push removal; the matching receiver declaration is " +
            "left untouched.",
    )

    val stripGoogleServicesFramework by booleanOption(
        key = "stripGoogleServicesFramework",
        default = true,
        title = "Remove READ_GSERVICES (Google Services Framework)",
        description = "The legacy Google Services Framework account-data gate, still " +
            "declared by play-services era apps.",
    )

    val stripLegacyActivityRecognition by booleanOption(
        key = "stripLegacyActivityRecognition",
        default = true,
        title = "Remove legacy ACTIVITY_RECOGNITION (Google Play Services)",
        description = "The GMS-defined alias fitness apps declared before Android 10 — gait " +
            "and motion profiling via play services. The modern platform " +
            "ACTIVITY_RECOGNITION permission is covered by its own separate patch.",
    )

    val stripGoogleAuth by booleanOption(
        key = "stripGoogleAuth",
        default = true,
        title = "Remove GOOGLE_AUTH scopes (legacy account tokens)",
        description = "The pre-Firebase account auth-scope family (GOOGLE_AUTH and its " +
            "service variants like GOOGLE_AUTH.writely, GOOGLE_AUTH.wise, " +
            "GOOGLE_AUTH.ALL_SERVICES) — account-token gates held by the Google stack. " +
            "Old Google-integrated apps may fail account flows on Play devices once " +
            "stripped; test per app.",
    )

    execute {
        val exact = mutableSetOf<String>()
        val prefixes = mutableSetOf<String>()
        val suffixes = mutableSetOf<String>()

        if (stripAdId ?: true) exact.add(AD_ID)
        if (stripBilling ?: true) exact.add(BILLING)
        if (stripCheckLicense ?: true) exact.add(CHECK_LICENSE)
        if (stripInstallReferrer ?: true) exact.add(INSTALL_REFERRER)
        if (stripPush ?: true) {
            exact.add(C2DM_RECEIVE)
            exact.add(C2DM_SEND)
        }
        if (stripC2dMessage ?: true) suffixes.add(C2D_MESSAGE_SUFFIX)
        if (stripGoogleServicesFramework ?: true) exact.add(GSF_READ)
        if (stripLegacyActivityRecognition ?: true) exact.add(LEGACY_ACTIVITY_RECOGNITION)
        if (stripGoogleAuth ?: true) prefixes.add(GOOGLE_AUTH_PREFIX)

        if (exact.isEmpty() && prefixes.isEmpty() && suffixes.isEmpty()) {
            println("[Remove GMS permissions] Skipped: no removals selected in patch options.")
            return@execute
        }

        fun blocked(name: String): Boolean =
            name in exact || prefixes.any(name::startsWith) || suffixes.any(name::endsWith)

        var removed = 0
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0)
            val permissions = manifest.childNodes
            val toRemove = mutableListOf<Node>()

            for (i in 0 until permissions.length) {
                val node = permissions.item(i) as? Element ?: continue
                if (node.tagName == "uses-permission" && blocked(node.getAttribute("android:name"))) {
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
