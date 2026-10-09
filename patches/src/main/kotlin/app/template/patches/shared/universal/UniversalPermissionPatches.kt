package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Universal "remove <uses-permission>" patches — one patch per permission.
 *
 * Each patch strips its permission's <uses-permission> declaration from the
 * manifest, using the same collect-then-remove DOM walk as the Doom/vomw
 * universal "Remove internet permission" patch (lineage: adobo →
 * Morning-Entree → rushiranpise/morphe-patches).
 *
 * For install-time permissions (INTERNET, CALL_PHONE, ...) a removed
 * declaration means the permission is never granted. For runtime
 * ("dangerous") permissions it means the system auto-denies the app's
 * runtime request without showing a dialog — equivalent to permanent user
 * denial, applied at install before any grant can happen. Apps that
 * hard-require a permission (a camera app without CAMERA) will degrade;
 * every patch stays default-off for that reason.
 *
 * All are no-ops on apps that don't declare the permission: a universal
 * patch must not fail the whole patch job for them. Only the plain
 * uses-permission tag is scanned — none of these permissions ever appear
 * in uses-permission-sdk-23/-m.
 */

/**
 * Creates a universal patch that removes a single <uses-permission> declaration.
 */
private fun removeUsesPermissionPatch(
    patchName: String,
    permission: String,
    why: String,
) = resourcePatch(
    name = patchName,
    description = "Removes the $permission permission from the manifest. $why",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0)
            val permissions = manifest.childNodes
            val toRemove = mutableListOf<Node>()

            for (i in 0 until permissions.length) {
                val node = permissions.item(i) as? Element ?: continue
                if (node.tagName == "uses-permission" &&
                    node.getAttribute("android:name") == permission
                ) {
                    toRemove.add(node)
                }
            }

            toRemove.forEach { manifest.removeChild(it) }
        }
    }
}

// ─── Base install-time and platform permissions ───

@Suppress("unused")
val removeBluetoothPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BLUETOOTH permission",
    permission = "android.permission.BLUETOOTH",
    why = "Legacy Bluetooth access (pre-Android 12): device discovery and scanning reveal " +
        "nearby Bluetooth devices and their unique MAC addresses, which are used for " +
        "fingerprinting and location. Apps that use Bluetooth accessories on Android 11 " +
        "and older lose them.",
)

@Suppress("unused")
val removeCallPhonePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove CALL_PHONE permission",
    permission = "android.permission.CALL_PHONE",
    why = "Lets the app place calls directly without opening the dialer. It is granted " +
        "automatically at install with no prompt, so stripping the declaration stops any " +
        "silent dialing (premium-rate fraud, tracking callbacks). Calls placed through the " +
        "dialer UI are unaffected.",
)

@Suppress("unused")
val removeOtherSensorsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove OTHER_SENSORS permission",
    permission = "android.permission.OTHER_SENSORS",
    why = "Android 15+ access to the 'other' sensors: ambient sensors like the accelerometer " +
        "and magnetometer enable activity inference, tap logging and device fingerprinting. " +
        "Removing it blinds sensor-based tracking.",
)

@Suppress("unused")
val removeAccessWifiStatePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_WIFI_STATE permission",
    permission = "android.permission.ACCESS_WIFI_STATE",
    why = "Exposes Wi-Fi connection details: network names and BSSIDs identify your home " +
        "network, and BSSID-to-location databases can place you physically. Apps that gate " +
        "large downloads on Wi-Fi may misbehave.",
)

@Suppress("unused")
val removeAccessNetworkStatePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_NETWORK_STATE permission",
    permission = "android.permission.ACCESS_NETWORK_STATE",
    why = "Reveals the current network type and availability, and lets ad SDKs detect " +
        "active VPNs to work around ad blocking. Pair it with removing INTERNET to fully " +
        "blind ad and analytics SDKs.",
)

@Suppress("unused")
val removeBindAccessibilityServicePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BIND_ACCESSIBILITY_SERVICE permission",
    permission = "android.permission.BIND_ACCESSIBILITY_SERVICE",
    why = "A system-signature permission no third-party app can ever be granted; declaring " +
        "it only signals intent to use accessibility scraping — reading all screen content, " +
        "keystrokes and notifications, the classic stalkerware pattern. Stripping the " +
        "declaration keeps it inert.",
)

@Suppress("unused")
val removeBindDeviceAdminPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BIND_DEVICE_ADMIN permission",
    permission = "android.permission.BIND_DEVICE_ADMIN",
    why = "A system-signature permission no third-party app can ever be granted; declaring " +
        "it only signals device-admin intent — remote lock, wipe and enforced policies, the " +
        "MDM/stalkerware pattern. Stripping the declaration keeps it inert.",
)

@Suppress("unused")
val removeAccessAdServicesTopicsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_ADSERVICES_TOPICS permission",
    permission = "android.permission.ACCESS_ADSERVICES_TOPICS",
    why = "Android 13+ Privacy Sandbox: lets the app read your Topics advertising-interest " +
        "profile. Removing it opts the app out of interest-based ad profiling.",
)

@Suppress("unused")
val removeAccessLocalNetworkPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_LOCAL_NETWORK permission",
    permission = "android.permission.ACCESS_LOCAL_NETWORK",
    why = "Android 16+ gate for local-network access: lets the app scan and inventory your " +
        "LAN, where every device and vendor is a fingerprintable data point. Removing it " +
        "blocks local network access; casting and LAN apps will lose it.",
)

@Suppress("unused")
val removeAccessAdServicesAdIdPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_AD_SERVICES_AD_ID permission",
    permission = "android.permission.ACCESS_AD_SERVICES_AD_ID",
    why = "Android 13+ Privacy Sandbox: lets the app read the advertising ID used for " +
        "attribution and profiling. Removing it revokes that access, breaking cross-app " +
        "ad tracking.",
)

@Suppress("unused")
val removeInternetPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove INTERNET permission",
    permission = "android.permission.INTERNET",
    why = "Blocks every socket the app opens, so bundled ad, analytics and telemetry SDKs " +
        "cannot phone home. Also disables any legitimate online features — only enable for " +
        "apps you want fully offline.",
)

@Suppress("unused")
val removeGmsAdIdPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove AD_ID permission (Google Play Services)",
    permission = "com.google.android.gms.permission.AD_ID",
    why = "Required since Android 12 for an app to read the Google Play advertising ID — " +
        "the cross-app identifier ad SDKs profile and attribute against. Removing it makes " +
        "the app read a zeroed ad ID instead.",
)

// ─── Camera / microphone ───

@Suppress("unused")
val removeCameraPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove CAMERA permission",
    permission = "android.permission.CAMERA",
    why = "Grants the camera: capture of faces, documents, screens and surroundings works " +
        "fully offline and can be exfiltrated later. Camera and QR-scanning apps need it; " +
        "almost nothing else does.",
)

@Suppress("unused")
val removeRecordAudioPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove RECORD_AUDIO permission",
    permission = "android.permission.RECORD_AUDIO",
    why = "Grants the microphone: ambient eavesdropping and long background capture — the " +
        "stalkerware staple. Audio records to disk offline for later exfiltration; voice " +
        "apps lose input.",
)

// ─── Contacts / calendar / accounts ───

@Suppress("unused")
val removeReadContactsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_CONTACTS permission",
    permission = "android.permission.READ_CONTACTS",
    why = "Reads your full social graph — names, numbers, emails, addresses and notes — " +
        "captured offline for later exfiltration. Messaging apps need it for contact " +
        "names.",
)

@Suppress("unused")
val removeWriteContactsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WRITE_CONTACTS permission",
    permission = "android.permission.WRITE_CONTACTS",
    why = "Integrity attack vector: can swap bank and support contacts for attacker numbers, " +
        "executed entirely offline with no network needed. Contact-management apps lose " +
        "write access.",
)

@Suppress("unused")
val removeGetAccountsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove GET_ACCOUNTS permission",
    permission = "android.permission.GET_ACCOUNTS",
    why = "Enumerates on-device accounts for cross-service identity linkage. Deprecated " +
        "and mostly inert on modern Android — stripping it is pure hardening.",
)

@Suppress("unused")
val removeReadCalendarPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_CALENDAR permission",
    permission = "android.permission.READ_CALENDAR",
    why = "Reads schedules, meeting links, attendees and locations — rich metadata " +
        "harvested offline for later exfiltration. Calendar apps need it.",
)

@Suppress("unused")
val removeWriteCalendarPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WRITE_CALENDAR permission",
    permission = "android.permission.WRITE_CALENDAR",
    why = "Integrity attack vector: injects phantom events with attacker links into the " +
        "trusted Calendar UI, executed entirely offline. Calendar apps lose write access.",
)

// ─── Location ───

@Suppress("unused")
val removeAccessFineLocationPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_FINE_LOCATION permission",
    permission = "android.permission.ACCESS_FINE_LOCATION",
    why = "Grants a GNSS pattern-of-life trail — home, work, habits. GPS is receive-only " +
        "and works fully offline with no network needed. Maps and navigation apps need it.",
)

@Suppress("unused")
val removeAccessCoarseLocationPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_COARSE_LOCATION permission",
    permission = "android.permission.ACCESS_COARSE_LOCATION",
    why = "Grants an approximate position stream — lower fidelity than fine location but " +
        "the same pattern-of-life leak, and it works offline.",
)

@Suppress("unused")
val removeAccessBackgroundLocationPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_BACKGROUND_LOCATION permission",
    permission = "android.permission.ACCESS_BACKGROUND_LOCATION",
    why = "Grants a silent 24/7 location trail with no visible app use — the highest-risk " +
        "location permission. Removing it caps the app to while-in-use location only; " +
        "weather and map apps that never need background use are unaffected.",
)

// ─── SMS / MMS / WAP push ───

@Suppress("unused")
val removeSendSmsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove SEND_SMS permission",
    permission = "android.permission.SEND_SMS",
    why = "The classic premium-SMS billing fraud channel — monetizes through carrier " +
        "billing and never touches the internet stack at all. Messaging apps lose " +
        "in-app sending; they can still share to your SMS app.",
)

@Suppress("unused")
val removeReadSmsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_SMS permission",
    permission = "android.permission.READ_SMS",
    why = "Reads message content, including bank and 2FA codes, harvested offline for " +
        "later exfiltration. Default SMS apps need it.",
)

@Suppress("unused")
val removeReceiveSmsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove RECEIVE_SMS permission",
    permission = "android.permission.RECEIVE_SMS",
    why = "Lets the app intercept incoming messages — including OTP codes — the moment " +
        "they arrive. Default SMS apps need it; nothing else should have it.",
)

@Suppress("unused")
val removeReceiveMmsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove RECEIVE_MMS permission",
    permission = "android.permission.RECEIVE_MMS",
    why = "Auto-retrieves MMS payloads — a media-parser attack surface that arrives on " +
        "the radio path with no internet involved. Default SMS apps need it.",
)

@Suppress("unused")
val removeReceiveWapPushPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove RECEIVE_WAP_PUSH permission",
    permission = "android.permission.RECEIVE_WAP_PUSH",
    why = "Receives carrier OMA provisioning and configuration pushes — a silent " +
        "configuration channel on the radio path that never touches the internet. " +
        "Almost no legitimate app should declare it.",
)

// ─── Phone ───

@Suppress("unused")
val removeReadPhoneStatePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_PHONE_STATE permission",
    permission = "android.permission.READ_PHONE_STATE",
    why = "Exposes phone identifiers and call state for identity correlation, harvested " +
        "offline. Removing it breaks some legacy analytics SDKs by design.",
)

@Suppress("unused")
val removeReadPhoneNumbersPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_PHONE_NUMBERS permission",
    permission = "android.permission.READ_PHONE_NUMBERS",
    why = "Exposes your phone number for identity linkage and SIM-swap prep. Dialer and " +
        "SMS apps need it; others rarely do.",
)

@Suppress("unused")
val removeAnswerPhoneCallsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ANSWER_PHONE_CALLS permission",
    permission = "android.permission.ANSWER_PHONE_CALLS",
    why = "Lets the app answer and control incoming calls — a call-hijack primitive " +
        "used by spyware. No normal app needs it.",
)

@Suppress("unused")
val removeReadCallLogPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_CALL_LOG permission",
    permission = "android.permission.READ_CALL_LOG",
    why = "Reads your call history — a who-talks-to-whom graph captured offline for " +
        "later exfiltration. Dialer apps need it.",
)

@Suppress("unused")
val removeWriteCallLogPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WRITE_CALL_LOG permission",
    permission = "android.permission.WRITE_CALL_LOG",
    why = "Integrity attack vector: edits or deletes call records to cover an abuser's " +
        "tracks, executed entirely offline. Dialer apps lose write access.",
)

@Suppress("unused")
val removeUseSipPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove USE_SIP permission",
    permission = "android.permission.USE_SIP",
    why = "Internet telephony. Deprecated and fully neutralized once INTERNET is removed — " +
        "stripping it hardens against a future re-grant.",
)

// ─── Nearby radios (Android 12+ granular set) ───

@Suppress("unused")
val removeBluetoothScanPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BLUETOOTH_SCAN permission",
    permission = "android.permission.BLUETOOTH_SCAN",
    why = "Scans for nearby Bluetooth devices and beacons — a retail-tracker presence " +
        "census that needs no internet to collect. Bluetooth device pairing flows lose " +
        "discovery.",
)

@Suppress("unused")
val removeBluetoothConnectPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BLUETOOTH_CONNECT permission",
    permission = "android.permission.BLUETOOTH_CONNECT",
    why = "Opens a direct RF data link to nearby hardware — an exfiltration path that " +
        "never touches the internet stack. Apps that talk to Bluetooth accessories " +
        "lose them.",
)

@Suppress("unused")
val removeBluetoothAdvertisePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BLUETOOTH_ADVERTISE permission",
    permission = "android.permission.BLUETOOTH_ADVERTISE",
    why = "Broadcasts a presence beacon your device can be tracked by, advertising " +
        "device identity over RF with no internet needed. Beacon-transmit apps " +
        "lose the function.",
)

@Suppress("unused")
val removeUwbRangingPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove UWB_RANGING permission",
    permission = "android.permission.UWB_RANGING",
    why = "Ultra-wideband fine-ranging enables precise proximity and motion analytics " +
        "with nearby devices, collected fully offline. Digital-key and tracker-tag " +
        "apps need it.",
)

@Suppress("unused")
val removeNearbyWifiDevicesPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove NEARBY_WIFI_DEVICES permission",
    permission = "android.permission.NEARBY_WIFI_DEVICES",
    why = "Wi-Fi Aware NAN peer links provide direct device-to-device data transfer with " +
        "no internet and no access point at all. Casting and smart-home setup flows " +
        "will degrade.",
)

// ─── Activity / body sensors ───

@Suppress("unused")
val removeActivityRecognitionPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACTIVITY_RECOGNITION permission",
    permission = "android.permission.ACTIVITY_RECOGNITION",
    why = "Builds a gait and motion-state behavioral profile — a cheap, continuous, " +
        "passive fingerprint of how you move. Fitness step counters need it.",
)

@Suppress("unused")
val removeBodySensorsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BODY_SENSORS permission",
    permission = "android.permission.BODY_SENSORS",
    why = "Reads health signals such as heart rate where sensors exist. Watch and " +
        "fitness apps need it; everything else rarely does.",
)

@Suppress("unused")
val removeBodySensorsBackgroundPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BODY_SENSORS_BACKGROUND permission",
    permission = "android.permission.BODY_SENSORS_BACKGROUND",
    why = "Grants continuous background body-sensor reads with no visible app use — " +
        "the silent variant of BODY_SENSORS. Only health apps with background " +
        "monitoring declare it.",
)

// ─── Media / storage ───

@Suppress("unused")
val removeReadMediaImagesPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_MEDIA_IMAGES permission",
    permission = "android.permission.READ_MEDIA_IMAGES",
    why = "Reads your entire photo library — including screenshots with PII, bank pages " +
        "and documents — captured offline for later exfiltration. Gallery and camera " +
        "apps need it.",
)

@Suppress("unused")
val removeReadMediaVideoPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_MEDIA_VIDEO permission",
    permission = "android.permission.READ_MEDIA_VIDEO",
    why = "Reads your entire video library offline for later exfiltration. Gallery and " +
        "camera apps need it.",
)

@Suppress("unused")
val removeReadMediaAudioPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_MEDIA_AUDIO permission",
    permission = "android.permission.READ_MEDIA_AUDIO",
    why = "Reads voice notes and recordings offline for later exfiltration. Music and " +
        "recorder apps need it.",
)

@Suppress("unused")
val removeReadMediaVisualUserSelectedPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_MEDIA_VISUAL_USER_SELECTED permission",
    permission = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
    why = "Reads the specific media items you granted in the partial-photo-access flow; " +
        "removing it revokes even that bounded grant.",
)

@Suppress("unused")
val removeAccessMediaLocationPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_MEDIA_LOCATION permission",
    permission = "android.permission.ACCESS_MEDIA_LOCATION",
    why = "Extracts EXIF GPS from shared media — a location-history leak that bypasses " +
        "location permissions entirely. Photo apps lose location grouping.",
)

@Suppress("unused")
val removeReadExternalStoragePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_EXTERNAL_STORAGE permission",
    permission = "android.permission.READ_EXTERNAL_STORAGE",
    why = "Legacy (API ≤32) shared-storage read, including other apps' Download dropboxes; " +
        "modern builds migrate to READ_MEDIA_*. Removing it covers older targets.",
)

@Suppress("unused")
val removeWriteExternalStoragePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WRITE_EXTERNAL_STORAGE permission",
    permission = "android.permission.WRITE_EXTERNAL_STORAGE",
    why = "Legacy (API ≤32) shared-storage write — an integrity surface over every app's " +
        "Downloads area. Modern targets no longer use it; stripping it covers older apps.",
)

// ─── Notifications ───

@Suppress("unused")
val removePostNotificationsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove POST_NOTIFICATIONS permission",
    permission = "android.permission.POST_NOTIFICATIONS",
    why = "Grants a lure and phishing delivery channel into your notification shade — " +
        "harvested credentials store offline for later use. Apps go quiet; that is " +
        "the point.",
)

// ─── Special-access grant screens (declared, then user-granted in Settings) ───

@Suppress("unused")
val removePackageUsageStatsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove PACKAGE_USAGE_STATS permission",
    permission = "android.permission.PACKAGE_USAGE_STATS",
    why = "Apps declare this signature|appop permission to route you to the Usage Access " +
        "screen in Settings — granting it hands over your full app-usage history: what " +
        "you open, when, and for how long. The parental-control/stalkerware channel; " +
        "stripping the declaration means the app can never receive the grant.",
)

@Suppress("unused")
val removeSystemAlertWindowPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove SYSTEM_ALERT_WINDOW permission",
    permission = "android.permission.SYSTEM_ALERT_WINDOW",
    why = "Unlocks the 'Display over other apps' special grant: overlays drawn on top of " +
        "other apps enable tapjacking, fake-dialog phishing and ad spam. Removing the " +
        "declaration means the app can never obtain overlay access.",
)

@Suppress("unused")
val removeManageExternalStoragePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove MANAGE_EXTERNAL_STORAGE permission",
    permission = "android.permission.MANAGE_EXTERNAL_STORAGE",
    why = "Unlocks the 'All files access' special grant — read and write over the entire " +
        "shared storage: every app's documents, photos and downloads. Removing the " +
        "declaration means the app can never obtain all-files access; file managers " +
        "need it.",
)

@Suppress("unused")
val removeManageMediaPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove MANAGE_MEDIA permission",
    permission = "android.permission.MANAGE_MEDIA",
    why = "Unlocks the 'Media management' special grant — modify and delete all media on " +
        "the device without per-item grants. Gallery apps use it for housekeeping; " +
        "removing the declaration means the app can never obtain it.",
)

@Suppress("unused")
val removeRequestInstallPackagesPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove REQUEST_INSTALL_PACKAGES permission",
    permission = "android.permission.REQUEST_INSTALL_PACKAGES",
    why = "Unlocks the 'Install unknown apps' special grant — the dropper channel: " +
        "silently self-update and sideload further APKs outside any store. Removing " +
        "the declaration means the app can never request installing unknown apps; " +
        "APK installers obviously need it.",
)

@Suppress("unused")
val removeWriteSettingsPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WRITE_SETTINGS permission",
    permission = "android.permission.WRITE_SETTINGS",
    why = "Unlocks the 'Modify system settings' special grant — an integrity surface over " +
        "ringtone, do-not-disturb and default-app configuration, the junk-app " +
        "settings-hijack channel. Removing the declaration means the app can never " +
        "obtain write access to system settings.",
)

// ─── Signature-declared surveillance signals (inert hardening, BIND_* class) ───

@Suppress("unused")
val removeBindNotificationListenerServicePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BIND_NOTIFICATION_LISTENER_SERVICE permission",
    permission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
    why = "A system-signature permission no third-party app can ever be granted; declaring " +
        "it signals intent to host a notification listener — reading every notification, " +
        "including message contents and 2FA codes. The stalkerware staple; stripping " +
        "the declaration keeps it inert.",
)

@Suppress("unused")
val removeBindInputMethodPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BIND_INPUT_METHOD permission",
    permission = "android.permission.BIND_INPUT_METHOD",
    why = "A system-signature permission no third-party app can ever be granted; declaring " +
        "it signals intent to host a keyboard — every keystroke in every app flows " +
        "through an IME. Stripping the declaration keeps it inert.",
)

// ─── Install-time persistence and wakeups ───

@Suppress("unused")
val removeReceiveBootCompletedPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove RECEIVE_BOOT_COMPLETED permission",
    permission = "android.permission.RECEIVE_BOOT_COMPLETED",
    why = "Granted at install with no prompt: the app auto-starts on every boot — the " +
        "persistence backbone of always-on background telemetry. Alarm, launcher and " +
        "messenger apps need it; decide per app.",
)

@Suppress("unused")
val removeWakeLockPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove WAKE_LOCK permission",
    permission = "android.permission.WAKE_LOCK",
    why = "Granted at install with no prompt: lets the app hold the CPU awake — the " +
        "enabler of unbounded background processing and periodic telemetry beacons, " +
        "at the cost of battery. Media and download apps need it.",
)

@Suppress("unused")
val removeScheduleExactAlarmPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove SCHEDULE_EXACT_ALARM permission",
    permission = "android.permission.SCHEDULE_EXACT_ALARM",
    why = "Granted at install: precise scheduled wakeups, the backbone of periodic " +
        "background telemetry beacons. Android 14+ gates it behind a Settings toggle — " +
        "this removes the capability entirely. Alarm and calendar apps need it.",
)

@Suppress("unused")
val removeUseExactAlarmPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove USE_EXACT_ALARM permission",
    permission = "android.permission.USE_EXACT_ALARM",
    why = "Granted at install and non-revocable in Settings (normal permissions never " +
        "show a revoke button): Play policy limits it to alarm and calendar apps, but " +
        "junk apps declare it to bypass the exact-alarm controls entirely. Removal is " +
        "the only way to revoke it.",
)

// ─── Android 14+ foreground-service types (service-context capture) ───

@Suppress("unused")
val removeForegroundServiceCameraPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove FOREGROUND_SERVICE_CAMERA permission",
    permission = "android.permission.FOREGROUND_SERVICE_CAMERA",
    why = "Android 14+ install-time grant for camera-type foreground services — a " +
        "service context that keeps camera capture running behind a persistent-process " +
        "notification. Camera apps need it; removing it blocks service-context capture " +
        "for everything else.",
)

@Suppress("unused")
val removeForegroundServiceMicrophonePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove FOREGROUND_SERVICE_MICROPHONE permission",
    permission = "android.permission.FOREGROUND_SERVICE_MICROPHONE",
    why = "Android 14+ install-time grant for microphone-type foreground services — " +
        "background-capable audio capture behind a persistent-process notification. " +
        "Recorder and call apps need it.",
)

@Suppress("unused")
val removeForegroundServiceLocationPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove FOREGROUND_SERVICE_LOCATION permission",
    permission = "android.permission.FOREGROUND_SERVICE_LOCATION",
    why = "Android 14+ install-time grant for location-type foreground services — " +
        "continuous background location behind a persistent-process notification. " +
        "Navigation and fitness-tracker apps need it.",
)

// ─── Radios ───

@Suppress("unused")
val removeNfcPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove NFC permission",
    permission = "android.permission.NFC",
    why = "Install-time gate for the NFC radio — tag reads and short-range RF peer links " +
        "that never touch the internet stack. Wallet and payment apps break; almost " +
        "everything else never touches NFC.",
)

@Suppress("unused")
val removeBluetoothAdminPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove BLUETOOTH_ADMIN permission",
    permission = "android.permission.BLUETOOTH_ADMIN",
    why = "Legacy (pre-Android 12) Bluetooth admin: can initiate discovery and make your " +
        "device discoverable — broadcasting presence over RF with no internet needed. " +
        "Pairs with removing BLUETOOTH; Android 12+ uses the granular SCAN/CONNECT/" +
        "ADVERTISE permissions instead.",
)

// ─── Health Connect ───

@Suppress("unused")
val removeReadHealthDataInBackgroundPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_HEALTH_DATA_IN_BACKGROUND permission",
    permission = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND",
    why = "Health Connect background access: lets the app read your health data — steps, " +
        "sleep, heart rate — while not in use, granted silently alongside other grants " +
        "with no separate prompt. Fitness dashboards break; background health " +
        "surveillance does not.",
)

@Suppress("unused")
val removeReadHealthDataHistoryPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove READ_HEALTH_DATA_HISTORY permission",
    permission = "android.permission.health.READ_HEALTH_DATA_HISTORY",
    why = "Health Connect history access: lets the app read health data from before it " +
        "was ever installed — reaching back to history you never granted it. Removing " +
        "it caps the app to data generated after install.",
)

// ─── Privacy Sandbox completion ───

@Suppress("unused")
val removeAccessAdServicesAttributionPermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove ACCESS_ADSERVICES_ATTRIBUTION permission",
    permission = "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    why = "Android 13+ Privacy Sandbox: lets the app measure ad attribution — install and " +
        "conversion events matched across apps. Completes the adservices family " +
        "alongside the TOPICS and AD_ID removal patches.",
)

// ─── Screenshot detection (pro-privacy removal) ───

@Suppress("unused")
val removeDetectScreenCapturePermissionPatch = removeUsesPermissionPatch(
    patchName = "Remove DETECT_SCREEN_CAPTURE permission",
    permission = "android.permission.DETECT_SCREEN_CAPTURE",
    why = "Android 15+: lets the app detect when you take a screenshot — banking apps " +
        "scold you, surveillance apps learn that evidence is being collected. Removing " +
        "it makes your screenshots silent.",
)
