package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Universal per-store debridger patches — one patch per alternative app store.
 *
 * Modeled on kveld9's NokoPrint Multi-Store Debridger
 * (kveld9/kveld-morphe-patches, GPL-3.0), split into per-store universal
 * patches. Orphan store SDKs are inert without their store installed, but
 * their manifest components remain wakeup, download and attack surface.
 *
 * Mechanism per store: disable matching activities/services/receivers/
 * providers (android:enabled=false — declarations stay structurally intact,
 * providers keep their authorities), and remove the store's permission
 * declarations. Component names are exact-match and SDK-defined — stable
 * across apps bundling the same SDK version, but if an SDK renames a
 * component a patch silently misses it; add the new name (or switch the
 * store to prefix matching) when that happens.
 *
 * Store-specific permissions only — the Google ad/measurement permission
 * family (AD_ID, adservices, referrer, CHECK_LICENSE) belongs to the
 * Remove GMS permissions master patch and is deliberately not duplicated
 * here. All are no-ops on apps bundling no component from their store.
 */

/**
 * Creates a universal patch that disables one store's components and
 * strips its permission declarations.
 */
private fun disableStoreComponentsPatch(
    patchName: String,
    description: String,
    components: Set<String>,
    permissions: Set<String> = emptySet(),
) = resourcePatch(
    name = patchName,
    description = description,
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        var disabled = 0
        var removed = 0

        document("AndroidManifest.xml").use { doc ->
            for (tag in listOf("activity", "service", "receiver", "provider")) {
                val nodes = doc.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val element = nodes.item(i) as? Element ?: continue
                    if (element.getAttribute("android:name") in components) {
                        element.setAttribute("android:enabled", "false")
                        disabled++
                    }
                }
            }

            val manifest = doc.getElementsByTagName("manifest").item(0)
            val children = manifest.childNodes
            val toRemove = mutableListOf<Node>()
            for (i in 0 until children.length) {
                val node = children.item(i) as? Element ?: continue
                if (node.tagName == "uses-permission" &&
                    node.getAttribute("android:name") in permissions
                ) {
                    toRemove.add(node)
                }
            }
            toRemove.forEach { manifest.removeChild(it) }
            removed = toRemove.size
        }

        println("[$patchName] Disabled $disabled component(s), stripped $removed permission(s).")
    }
}

@Suppress("unused")
val disableHuaweiStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable Huawei store components",
    description = "Disables orphan Huawei HMS, AGConnect and OTA update SDK components: " +
        "the HMS bridge and enable-service activities, AGConnect service discovery and " +
        "initializer, Huawei's in-app OTA updater, package installer and file provider, " +
        "and the account sign-in hub activities. Strips the AppGallery common-data " +
        "permission. Inert without Huawei AppGallery installed.",
    components = setOf(
        "com.huawei.hms.activity.BridgeActivity",
        "com.huawei.hms.activity.EnableServiceActivity",
        "com.huawei.agconnect.core.ServiceDiscovery",
        "com.huawei.agconnect.core.provider.AGConnectInitializeProvider",
        "com.huawei.updatesdk.service.otaupdate.AppUpdateActivity",
        "com.huawei.updatesdk.support.pm.PackageInstallerActivity",
        "com.huawei.updatesdk.fileprovider.UpdateSdkFileProvider",
        "com.huawei.hms.account.internal.ui.activity.AccountSignInHubActivity",
        "com.huawei.hms.hwid.internal.ui.activity.HwIdSignInHubActivity",
    ),
    permissions = setOf(
        "com.huawei.appmarket.service.commondata.permission.GET_COMMON_DATA",
    ),
)

@Suppress("unused")
val disableXiaomiStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable Xiaomi store components",
    description = "Disables orphan Xiaomi billing client and Common IAP components: the " +
        "proxy billing activities and payment/extra web activities they register. Inert " +
        "without Xiaomi's store and payment service installed.",
    components = setOf(
        "com.xiaomi.billingclient.ui.ProxyBillingActivity",
        "com.xiaomi.billingclient.ui.ClientPaymentWebActivity",
        "com.xiaomi.billingclient.ui.ClientExtraWebActivity",
        "com.xiaomi.billingclient.floating.WebActivity",
        "com.iap.common.floating.WebActivity",
        "com.iap.common.ui.ClientExtraWebActivity",
        "com.iap.common.ui.ClientPaymentWebActivity",
        "com.iap.common.ui.ProxyBillingActivity",
    ),
)

@Suppress("unused")
val disableSamsungStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable Samsung IAP components",
    description = "Disables orphan Samsung In-App Purchase SDK components: the dialog, " +
        "package-check, account, payment and subscription-plan activities. Strips the " +
        "Samsung IAP billing permission. Inert without Samsung Galaxy Store installed.",
    components = setOf(
        "com.samsung.android.sdk.iap.lib.activity.DialogActivity",
        "com.samsung.android.sdk.iap.lib.activity.CheckPackageActivity",
        "com.samsung.android.sdk.iap.lib.activity.AccountActivity",
        "com.samsung.android.sdk.iap.lib.activity.PaymentActivity",
        "com.samsung.android.sdk.iap.lib.activity.ChangeSubscriptionPlanActivity",
    ),
    permissions = setOf(
        "com.samsung.android.iap.permission.BILLING",
    ),
)

@Suppress("unused")
val disableRustoreStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable RuStore components",
    description = "Disables orphan RuStore SDK components: the payment activity, pay and " +
        "imaging content providers, and the metrics event job service. Inert without " +
        "RuStore installed.",
    components = setOf(
        "ru.rustore.sdk.pay.internal.presentation.ui.PayActivity",
        "ru.rustore.sdk.pay.RuStorePayContentProvider",
        "ru.rustore.sdk.imaging.ImageLoaderContentProvider",
        "ru.rustore.sdk.metrics.internal.presentation.SendMetricsEventJobService",
    ),
)

@Suppress("unused")
val disableOneStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable OneStore components",
    description = "Disables orphan OneStore (Korean GAA SDK) components: the IAP proxy " +
        "activity and sign-in activity. Inert without OneStore installed.",
    components = setOf(
        "com.gaa.sdk.iap.ProxyActivity",
        "com.gaa.sdk.auth.SignInActivity",
    ),
)

@Suppress("unused")
val disableCafeBazaarStoreComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable CafeBazaar components",
    description = "Disables the orphan CafeBazaar in-app billing receiver and strips its " +
        "pay-through permission. Inert without CafeBazaar installed.",
    components = setOf(
        "com.farsitel.bazaar.billing.IABReceiver",
    ),
    permissions = setOf(
        "com.farsitel.bazaar.permission.PAY_THROUGH_BAZAAR",
    ),
)

@Suppress("unused")
val disableAmazonIapComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable Amazon IAP components",
    description = "Disables the orphan Amazon In-App Purchasing response receiver and " +
        "strips the Amazon PrivacyPass attest permission. Inert without the Amazon " +
        "Appstore installed.",
    components = setOf(
        "com.amazon.device.iap.ResponseReceiver",
    ),
    permissions = setOf(
        "com.amazon.privacypass.ATTEST",
    ),
)

@Suppress("unused")
val disableGooglePlayBillingComponentsPatch = disableStoreComponentsPatch(
    patchName = "Disable Google Play billing components",
    description = "Disables the Play Billing library's proxy billing activities, the Play " +
        "Core dialog wrapper and asset-pack extraction services, and the Pairip license " +
        "activity. Billing and licensing permission declarations are handled by the " +
        "Remove GMS permissions master patch — this patch only disables the components. " +
        "Purchases were already impossible in re-signed patched APKs.",
    components = setOf(
        "com.android.billingclient.api.ProxyBillingActivity",
        "com.android.billingclient.api.ProxyBillingActivityV2",
        "com.google.android.play.core.common.PlayCoreDialogWrapperActivity",
        "com.google.android.play.core.assetpacks.AssetPackExtractionService",
        "com.google.android.play.core.assetpacks.ExtractionForegroundService",
        "com.pairip.licensecheck.LicenseActivity",
    ),
)
