package app.template.patches.shared.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Universal "Remove privacy permissions" master patch.
 *
 * Modeled on kveld9's Universal Privacy Permissions Stripper pattern
 * (kveld9/kveld-morphe-patches, GPL-3.0): one master patch whose gear-dialog
 * boolean toggles each bundle a category of permissions. All toggles default
 * to true, so enabling the patch alone strips every selected category —
 * open the gear to deselect. Replaces the 69 individual per-permission
 * patches of this file; the removal walk is the collect-then-remove DOM
 * walk from the Doom/vomw "Remove internet permission" patch (lineage:
 * adobo → Morning-Entree → rushiranpise/morphe-patches).
 *
 * Semantics per permission: install-time permissions are never granted once
 * stripped; runtime ("dangerous") permissions are auto-denied at request
 * time with no dialog — permanent user denial, applied at install. Special-
 * access declarations mean the app can never reach its Settings grant
 * state. All categories are no-ops on apps that declare nothing from them,
 * and only plain uses-permission tags are scanned — none of these
 * permissions appear in uses-permission-sdk-23/-m.
 */

private val CAMERA_PERMISSIONS = setOf(
    "android.permission.CAMERA",
    "android.permission.FOREGROUND_SERVICE_CAMERA",
)

private val MICROPHONE_PERMISSIONS = setOf(
    "android.permission.RECORD_AUDIO",
    "android.permission.FOREGROUND_SERVICE_MICROPHONE",
)

private val LOCATION_PERMISSIONS = setOf(
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.FOREGROUND_SERVICE_LOCATION",
)

private val CONTACTS_ACCOUNTS_PERMISSIONS = setOf(
    "android.permission.READ_CONTACTS",
    "android.permission.WRITE_CONTACTS",
    "android.permission.GET_ACCOUNTS",
)

private val CALENDAR_PERMISSIONS = setOf(
    "android.permission.READ_CALENDAR",
    "android.permission.WRITE_CALENDAR",
)

private val SMS_MMS_PERMISSIONS = setOf(
    "android.permission.SEND_SMS",
    "android.permission.READ_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.RECEIVE_MMS",
    "android.permission.RECEIVE_WAP_PUSH",
)

private val PHONE_CALLS_PERMISSIONS = setOf(
    "android.permission.CALL_PHONE",
    "android.permission.READ_PHONE_STATE",
    "android.permission.READ_PHONE_NUMBERS",
    "android.permission.ANSWER_PHONE_CALLS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.USE_SIP",
)

private val NEARBY_RADIOS_PERMISSIONS = setOf(
    "android.permission.BLUETOOTH",
    "android.permission.BLUETOOTH_ADMIN",
    "android.permission.BLUETOOTH_SCAN",
    "android.permission.BLUETOOTH_CONNECT",
    "android.permission.BLUETOOTH_ADVERTISE",
    "android.permission.UWB_RANGING",
    "android.permission.NEARBY_WIFI_DEVICES",
    "android.permission.NFC",
)

private val NETWORK_STATE_PERMISSIONS = setOf(
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_LOCAL_NETWORK",
)

private val INTERNET_PERMISSIONS = setOf(
    "android.permission.INTERNET",
)

private val SENSORS_PERMISSIONS = setOf(
    "android.permission.OTHER_SENSORS",
    "android.permission.ACTIVITY_RECOGNITION",
    "android.permission.BODY_SENSORS",
    "android.permission.BODY_SENSORS_BACKGROUND",
)

private val MEDIA_STORAGE_PERMISSIONS = setOf(
    "android.permission.READ_MEDIA_IMAGES",
    "android.permission.READ_MEDIA_VIDEO",
    "android.permission.READ_MEDIA_AUDIO",
    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
    "android.permission.ACCESS_MEDIA_LOCATION",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
)

private val NOTIFICATION_PERMISSIONS = setOf(
    "android.permission.POST_NOTIFICATIONS",
)

private val SPECIAL_ACCESS_PERMISSIONS = setOf(
    "android.permission.PACKAGE_USAGE_STATS",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_MEDIA",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.WRITE_SETTINGS",
)

private val SURVEILLANCE_DECLARATION_PERMISSIONS = setOf(
    "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_DEVICE_ADMIN",
    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
    "android.permission.BIND_INPUT_METHOD",
)

private val PERSISTENCE_WAKEUP_PERMISSIONS = setOf(
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.WAKE_LOCK",
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.USE_EXACT_ALARM",
)

private val HEALTH_DATA_PERMISSIONS = setOf(
    "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND",
    "android.permission.health.READ_HEALTH_DATA_HISTORY",
)

private val AD_PERMISSIONS = setOf(
    "android.permission.ACCESS_ADSERVICES_TOPICS",
    "android.permission.ACCESS_AD_SERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
)

private val SCREENSHOT_DETECTION_PERMISSIONS = setOf(
    "android.permission.DETECT_SCREEN_CAPTURE",
)

@Suppress("unused")
val removePrivacyPermissionsPatch = resourcePatch(
    name = "Remove privacy permissions",
    description = "Strips privacy-invasive permission declarations from the manifest across " +
        "19 categories — camera, microphone, location, contacts, calendar, SMS, phone, " +
        "nearby radios, network state, internet, sensors, media, notifications, special " +
        "access, surveillance declarations, persistence, health, advertising and " +
        "screenshot detection. Install-time permissions are never granted; runtime " +
        "permissions are auto-denied with no dialog. All categories are on by default — " +
        "open the patch options (gear) to deselect the ones the app legitimately needs.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripCamera by booleanOption(
        key = "stripCamera",
        default = true,
        title = "Remove camera access",
        description = "Strips CAMERA and FOREGROUND_SERVICE_CAMERA: capture of faces, " +
            "documents and surroundings — including service-context capture — recorded " +
            "offline for later exfiltration. Camera and QR apps lose capture.",
    )

    val stripMicrophone by booleanOption(
        key = "stripMicrophone",
        default = true,
        title = "Remove microphone access",
        description = "Strips RECORD_AUDIO and FOREGROUND_SERVICE_MICROPHONE: ambient " +
            "eavesdropping and long background capture — the stalkerware staple. " +
            "Recorder, call and voice apps lose input.",
    )

    val stripLocation by booleanOption(
        key = "stripLocation",
        default = true,
        title = "Remove location access",
        description = "Strips ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, " +
            "ACCESS_BACKGROUND_LOCATION and FOREGROUND_SERVICE_LOCATION: a GNSS " +
            "pattern-of-life trail that works fully offline. Maps, navigation and " +
            "fitness trackers break.",
    )

    val stripContactsAccounts by booleanOption(
        key = "stripContactsAccounts",
        default = true,
        title = "Remove contacts & accounts access",
        description = "Strips READ_CONTACTS, WRITE_CONTACTS and GET_ACCOUNTS: your full " +
            "social graph plus on-device account linkage. Messaging apps lose contact " +
            "names; contact-swap integrity attacks go with WRITE.",
    )

    val stripCalendar by booleanOption(
        key = "stripCalendar",
        default = true,
        title = "Remove calendar access",
        description = "Strips READ_CALENDAR and WRITE_CALENDAR: schedules, attendees, " +
            "meeting links and locations harvested offline, plus phantom-event " +
            "injection into the trusted Calendar UI. Calendar apps lose access.",
    )

    val stripSmsMms by booleanOption(
        key = "stripSmsMms",
        default = true,
        title = "Remove SMS & MMS access",
        description = "Strips SEND_SMS, READ_SMS, RECEIVE_SMS, RECEIVE_MMS and " +
            "RECEIVE_WAP_PUSH: premium-SMS billing fraud (carrier-billed, never touches " +
            "the internet), OTP interception and the carrier provisioning push channel. " +
            "Default SMS apps break.",
    )

    val stripPhoneCalls by booleanOption(
        key = "stripPhoneCalls",
        default = true,
        title = "Remove phone & call access",
        description = "Strips CALL_PHONE, READ_PHONE_STATE, READ_PHONE_NUMBERS, " +
            "ANSWER_PHONE_CALLS, READ_CALL_LOG, WRITE_CALL_LOG and USE_SIP: silent " +
            "dialing fraud, identity correlation, call-hijack and call-record " +
            "tampering. Dialers and messengers break.",
    )

    val stripNearbyRadios by booleanOption(
        key = "stripNearbyRadios",
        default = true,
        title = "Remove nearby radios (Bluetooth, UWB, NFC)",
        description = "Strips BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_SCAN, " +
            "BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE, UWB_RANGING, NEARBY_WIFI_DEVICES " +
            "and NFC: beacon presence mapping, discoverability and direct RF " +
            "exfiltration links — all offline-capable. Accessory, wallet and casting " +
            "apps break.",
    )

    val stripNetworkState by booleanOption(
        key = "stripNetworkState",
        default = true,
        title = "Remove network & Wi-Fi state access",
        description = "Strips ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE and " +
            "ACCESS_LOCAL_NETWORK: Wi-Fi network identity and BSSID geolocation, VPN " +
            "detection by ad SDKs, and LAN device inventory. Download managers may " +
            "misbehave.",
    )

    val stripInternet by booleanOption(
        key = "stripInternet",
        default = true,
        title = "Remove internet access",
        description = "Strips INTERNET: blocks every socket, so bundled ad, analytics " +
            "and telemetry SDKs cannot phone home. Only keep this on for apps you want " +
            "fully offline — every online feature dies with it.",
    )

    val stripSensors by booleanOption(
        key = "stripSensors",
        default = true,
        title = "Remove sensor & motion access",
        description = "Strips OTHER_SENSORS, ACTIVITY_RECOGNITION, BODY_SENSORS and " +
            "BODY_SENSORS_BACKGROUND: motion and health signals used for activity " +
            "inference, tap logging, gait profiling and device fingerprinting. " +
            "Fitness apps break.",
    )

    val stripMediaStorage by booleanOption(
        key = "stripMediaStorage",
        default = true,
        title = "Remove media & storage access",
        description = "Strips READ_MEDIA_IMAGES/VIDEO/AUDIO, READ_MEDIA_VISUAL_USER_" +
            "SELECTED, ACCESS_MEDIA_LOCATION, READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_" +
            "STORAGE: your entire media library including PII screenshots, EXIF GPS " +
            "location history and legacy shared-storage integrity. Gallery and camera " +
            "apps break.",
    )

    val stripNotifications by booleanOption(
        key = "stripNotifications",
        default = true,
        title = "Remove notification posting",
        description = "Strips POST_NOTIFICATIONS: the lure and phishing delivery channel " +
            "into your notification shade. Apps go quiet — that is the point.",
    )

    val stripSpecialAccess by booleanOption(
        key = "stripSpecialAccess",
        default = true,
        title = "Remove special-access grant gates",
        description = "Strips PACKAGE_USAGE_STATS, SYSTEM_ALERT_WINDOW, MANAGE_EXTERNAL_" +
            "STORAGE, MANAGE_MEDIA, REQUEST_INSTALL_PACKAGES and WRITE_SETTINGS: the " +
            "Settings-grant screens for usage-history surveillance, overlay " +
            "tapjacking, all-files access, the dropper channel and settings " +
            "hijacking. File managers and installers break.",
    )

    val stripSurveillanceDeclarations by booleanOption(
        key = "stripSurveillanceDeclarations",
        default = true,
        title = "Remove surveillance service declarations (BIND_*)",
        description = "Strips BIND_ACCESSIBILITY_SERVICE, BIND_DEVICE_ADMIN, BIND_" +
            "NOTIFICATION_LISTENER_SERVICE and BIND_INPUT_METHOD: system-signature " +
            "declarations no third-party app can hold — the stalkerware and keylogger " +
            "intent markers. Inert hardening.",
    )

    val stripPersistenceWakeups by booleanOption(
        key = "stripPersistenceWakeups",
        default = true,
        title = "Remove background persistence & wakeups",
        description = "Strips RECEIVE_BOOT_COMPLETED, WAKE_LOCK, SCHEDULE_EXACT_ALARM " +
            "and USE_EXACT_ALARM: boot auto-start, CPU hold and precise periodic " +
            "wakeups — the backbone of always-on background telemetry. Alarm, " +
            "launcher, messenger and media apps may break.",
    )

    val stripHealthData by booleanOption(
        key = "stripHealthData",
        default = true,
        title = "Remove Health Connect access",
        description = "Strips health.READ_HEALTH_DATA_IN_BACKGROUND and health.READ_" +
            "HEALTH_DATA_HISTORY: background health reads granted without a separate " +
            "prompt, and history reaching back before the app was installed. " +
            "Fitness dashboards break.",
    )

    val stripAds by booleanOption(
        key = "stripAds",
        default = true,
        title = "Remove ad & Privacy Sandbox permissions",
        description = "Strips ACCESS_ADSERVICES_TOPICS, ACCESS_AD_SERVICES_AD_ID and " +
            "ACCESS_ADSERVICES_ATTRIBUTION: interest-profile reads, the ad identifier " +
            "and cross-app attribution measurement.",
    )

    val stripScreenshotDetection by booleanOption(
        key = "stripScreenshotDetection",
        default = true,
        title = "Remove screenshot detection",
        description = "Strips DETECT_SCREEN_CAPTURE: the app can no longer detect your " +
            "screenshots — banking scolding and surveillance-evidence detection go " +
            "away. Your captures become silent.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripCamera ?: true) blocked.addAll(CAMERA_PERMISSIONS)
        if (stripMicrophone ?: true) blocked.addAll(MICROPHONE_PERMISSIONS)
        if (stripLocation ?: true) blocked.addAll(LOCATION_PERMISSIONS)
        if (stripContactsAccounts ?: true) blocked.addAll(CONTACTS_ACCOUNTS_PERMISSIONS)
        if (stripCalendar ?: true) blocked.addAll(CALENDAR_PERMISSIONS)
        if (stripSmsMms ?: true) blocked.addAll(SMS_MMS_PERMISSIONS)
        if (stripPhoneCalls ?: true) blocked.addAll(PHONE_CALLS_PERMISSIONS)
        if (stripNearbyRadios ?: true) blocked.addAll(NEARBY_RADIOS_PERMISSIONS)
        if (stripNetworkState ?: true) blocked.addAll(NETWORK_STATE_PERMISSIONS)
        if (stripInternet ?: true) blocked.addAll(INTERNET_PERMISSIONS)
        if (stripSensors ?: true) blocked.addAll(SENSORS_PERMISSIONS)
        if (stripMediaStorage ?: true) blocked.addAll(MEDIA_STORAGE_PERMISSIONS)
        if (stripNotifications ?: true) blocked.addAll(NOTIFICATION_PERMISSIONS)
        if (stripSpecialAccess ?: true) blocked.addAll(SPECIAL_ACCESS_PERMISSIONS)
        if (stripSurveillanceDeclarations ?: true) blocked.addAll(SURVEILLANCE_DECLARATION_PERMISSIONS)
        if (stripPersistenceWakeups ?: true) blocked.addAll(PERSISTENCE_WAKEUP_PERMISSIONS)
        if (stripHealthData ?: true) blocked.addAll(HEALTH_DATA_PERMISSIONS)
        if (stripAds ?: true) blocked.addAll(AD_PERMISSIONS)
        if (stripScreenshotDetection ?: true) blocked.addAll(SCREENSHOT_DETECTION_PERMISSIONS)

        if (blocked.isEmpty()) {
            println("[Remove privacy permissions] Skipped: no categories selected in patch options.")
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
            println("[Remove privacy permissions] None of the selected permission declarations found.")
        } else {
            val shortNames = mutableListOf<String>()
            document("AndroidManifest.xml").use { document ->
                val used = document.getElementsByTagName("uses-permission")
                // recompute from removed list instead:
            }
            println("[Remove privacy permissions] Stripped $removed permission declaration(s).")
        }
    }
}
