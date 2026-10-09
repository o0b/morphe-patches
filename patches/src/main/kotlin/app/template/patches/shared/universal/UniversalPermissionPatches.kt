package app.template.patches.shared.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Universal permission-removal patches — one patch per category, one gear
 * toggle per permission (all on by default), following the kveld9 gear
 * pattern (kveld9/kveld-morphe-patches, GPL-3.0). Replaces the earlier
 * single-master consolidation, which only allowed category-level control.
 *
 * Semantics per permission: install-time permissions are never granted once
 * stripped; runtime ("dangerous") permissions are auto-denied at request
 * time with no dialog — permanent user denial, applied at install.
 * Special-access declarations mean the app can never reach its Settings
 * grant state. All are no-ops on apps that declare nothing from the
 * category, and only plain uses-permission tags are scanned — none of
 * these permissions appear in uses-permission-sdk-23/-m.
 */

/** The proven collect-then-remove walk, shared by all category patches. */
private fun stripSelectedPermissions(doc: Document, blocked: Set<String>): Int {
    val manifest = doc.getElementsByTagName("manifest").item(0)
    val children = manifest.childNodes
    val toRemove = mutableListOf<Node>()

    for (i in 0 until children.length) {
        val node = children.item(i) as? Element ?: continue
        if (node.tagName == "uses-permission" &&
            node.getAttribute("android:name") in blocked
        ) {
            toRemove.add(node)
        }
    }

    toRemove.forEach { manifest.removeChild(it) }
    return toRemove.size
}

// ─── Camera & flashlight ───

@Suppress("unused")
val removeCameraPermissionsPatch = resourcePatch(
    name = "Remove camera & flashlight permissions",
    description = "Strips the app's camera and flashlight permission declarations, capture " +
        "included. All removals are on by default — open the patch options (gear) to keep " +
        "specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripCamera by booleanOption(
        key = "stripCamera",
        default = true,
        title = "CAMERA",
        description = "Capture of faces, documents and surroundings — works fully offline for " +
            "later exfiltration.",
    )

    val stripFgsCamera by booleanOption(
        key = "stripFgsCamera",
        default = true,
        title = "FOREGROUND_SERVICE_CAMERA",
        description = "Android 14+ service-context camera capture behind a persistent-process " +
            "notification.",
    )

    val stripFlashlight by booleanOption(
        key = "stripFlashlight",
        default = true,
        title = "FLASHLIGHT",
        description = "Legacy normal permission, deprecated since Android 6 — the modern torch " +
            "(setTorchMode) needs no permission, so the declaration is dead weight; stripping " +
            "is hygiene. Apps that branch on the permission check may hide their own torch " +
            "button.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripCamera ?: true) blocked.add("android.permission.CAMERA")
        if (stripFgsCamera ?: true) blocked.add("android.permission.FOREGROUND_SERVICE_CAMERA")
        if (stripFlashlight ?: true) blocked.add("android.permission.FLASHLIGHT")
        if (blocked.isEmpty()) { println("[Remove camera & flashlight permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove camera & flashlight permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Microphone & audio state ───

@Suppress("unused")
val removeMicrophonePermissionsPatch = resourcePatch(
    name = "Remove microphone & audio permissions",
    description = "Strips the app's audio declarations — microphone capture, service-context " +
        "capture, and global audio-state modification. All removals are on by default — " +
        "open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripRecordAudio by booleanOption(
        key = "stripRecordAudio",
        default = true,
        title = "RECORD_AUDIO",
        description = "Ambient eavesdropping and long background capture — the stalkerware staple.",
    )

    val stripFgsMicrophone by booleanOption(
        key = "stripFgsMicrophone",
        default = true,
        title = "FOREGROUND_SERVICE_MICROPHONE",
        description = "Android 14+ service-context background audio capture behind a " +
            "persistent-process notification.",
    )

    val stripModifyAudioSettings by booleanOption(
        key = "stripModifyAudioSettings",
        default = true,
        title = "MODIFY_AUDIO_SETTINGS",
        description = "Granted at install with no prompt: the app can modify global audio " +
            "state — volume, ringer mode and audio routing during calls. An integrity surface " +
            "over the device's audio configuration; call and voice apps lose their adjustments.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripRecordAudio ?: true) blocked.add("android.permission.RECORD_AUDIO")
        if (stripFgsMicrophone ?: true) blocked.add("android.permission.FOREGROUND_SERVICE_MICROPHONE")
        if (stripModifyAudioSettings ?: true) blocked.add("android.permission.MODIFY_AUDIO_SETTINGS")
        if (blocked.isEmpty()) { println("[Remove microphone & audio permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove microphone & audio permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Location ───

@Suppress("unused")
val removeLocationPermissionsPatch = resourcePatch(
    name = "Remove location permissions",
    description = "Strips the app's location permission declarations, GNSS and service-context " +
        "capture included. All removals are on by default — open the patch options (gear) to " +
        "keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripFineLocation by booleanOption(
        key = "stripFineLocation",
        default = true,
        title = "ACCESS_FINE_LOCATION",
        description = "A GNSS pattern-of-life trail — home, work, habits — receive-only, works " +
            "fully offline.",
    )

    val stripCoarseLocation by booleanOption(
        key = "stripCoarseLocation",
        default = true,
        title = "ACCESS_COARSE_LOCATION",
        description = "Approximate position stream — the same pattern-of-life leak at lower " +
            "fidelity.",
    )

    val stripBackgroundLocation by booleanOption(
        key = "stripBackgroundLocation",
        default = true,
        title = "ACCESS_BACKGROUND_LOCATION",
        description = "Silent 24/7 location trail with no visible app use — the highest-risk " +
            "location grant.",
    )

    val stripFgsLocation by booleanOption(
        key = "stripFgsLocation",
        default = true,
        title = "FOREGROUND_SERVICE_LOCATION",
        description = "Android 14+ service-context continuous location behind a " +
            "persistent-process notification.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripFineLocation ?: true) blocked.add("android.permission.ACCESS_FINE_LOCATION")
        if (stripCoarseLocation ?: true) blocked.add("android.permission.ACCESS_COARSE_LOCATION")
        if (stripBackgroundLocation ?: true) blocked.add("android.permission.ACCESS_BACKGROUND_LOCATION")
        if (stripFgsLocation ?: true) blocked.add("android.permission.FOREGROUND_SERVICE_LOCATION")
        if (blocked.isEmpty()) { println("[Remove location permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove location permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Contacts & accounts ───

@Suppress("unused")
val removeContactsAccountsPermissionsPatch = resourcePatch(
    name = "Remove contacts & accounts permissions",
    description = "Strips the app's social-graph and account-enum declarations — the read side " +
        "harvests offline, the write side is an integrity attack vector. All removals are on " +
        "by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripReadContacts by booleanOption(
        key = "stripReadContacts",
        default = true,
        title = "READ_CONTACTS",
        description = "Your full social graph — names, numbers, emails, addresses — harvested " +
            "offline.",
    )

    val stripWriteContacts by booleanOption(
        key = "stripWriteContacts",
        default = true,
        title = "WRITE_CONTACTS",
        description = "Integrity attack: swaps bank and support contacts for attacker numbers, " +
            "no network needed.",
    )

    val stripGetAccounts by booleanOption(
        key = "stripGetAccounts",
        default = true,
        title = "GET_ACCOUNTS",
        description = "Enumerates on-device accounts for cross-service identity linkage; " +
            "deprecated, pure hardening.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripReadContacts ?: true) blocked.add("android.permission.READ_CONTACTS")
        if (stripWriteContacts ?: true) blocked.add("android.permission.WRITE_CONTACTS")
        if (stripGetAccounts ?: true) blocked.add("android.permission.GET_ACCOUNTS")
        if (blocked.isEmpty()) { println("[Remove contacts & accounts permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove contacts & accounts permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Calendar ───

@Suppress("unused")
val removeCalendarPermissionsPatch = resourcePatch(
    name = "Remove calendar permissions",
    description = "Strips the app's calendar declarations — the read side harvests schedules " +
        "offline, the write side injects phantom events into the trusted Calendar UI. All " +
        "removals are on by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripReadCalendar by booleanOption(
        key = "stripReadCalendar",
        default = true,
        title = "READ_CALENDAR",
        description = "Schedules, attendees, meeting links and locations — rich metadata " +
            "harvested offline.",
    )

    val stripWriteCalendar by booleanOption(
        key = "stripWriteCalendar",
        default = true,
        title = "WRITE_CALENDAR",
        description = "Integrity attack: injects phantom events with attacker links into the " +
            "trusted Calendar UI.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripReadCalendar ?: true) blocked.add("android.permission.READ_CALENDAR")
        if (stripWriteCalendar ?: true) blocked.add("android.permission.WRITE_CALENDAR")
        if (blocked.isEmpty()) { println("[Remove calendar permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove calendar permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── SMS & MMS ───

@Suppress("unused")
val removeSmsPermissionsPatch = resourcePatch(
    name = "Remove SMS & MMS permissions",
    description = "Strips the app's messaging declarations — premium-SMS fraud, OTP " +
        "interception and the carrier push channel, all of which bypass the internet stack " +
        "entirely. All removals are on by default — open the patch options (gear) to keep " +
        "specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripSendSms by booleanOption(
        key = "stripSendSms",
        default = true,
        title = "SEND_SMS",
        description = "The premium-SMS billing fraud channel — carrier-billed, never touches " +
            "the internet.",
    )

    val stripReadSms by booleanOption(
        key = "stripReadSms",
        default = true,
        title = "READ_SMS",
        description = "Message content, including bank and 2FA codes, harvested offline.",
    )

    val stripReceiveSms by booleanOption(
        key = "stripReceiveSms",
        default = true,
        title = "RECEIVE_SMS",
        description = "Intercepts incoming messages — including OTP codes — the moment they " +
            "arrive.",
    )

    val stripReceiveMms by booleanOption(
        key = "stripReceiveMms",
        default = true,
        title = "RECEIVE_MMS",
        description = "Auto-retrieves MMS payloads — a media-parser attack surface on the " +
            "radio path.",
    )

    val stripReceiveWapPush by booleanOption(
        key = "stripReceiveWapPush",
        default = true,
        title = "RECEIVE_WAP_PUSH",
        description = "Carrier OMA provisioning pushes — a silent configuration channel off " +
            "the internet.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripSendSms ?: true) blocked.add("android.permission.SEND_SMS")
        if (stripReadSms ?: true) blocked.add("android.permission.READ_SMS")
        if (stripReceiveSms ?: true) blocked.add("android.permission.RECEIVE_SMS")
        if (stripReceiveMms ?: true) blocked.add("android.permission.RECEIVE_MMS")
        if (stripReceiveWapPush ?: true) blocked.add("android.permission.RECEIVE_WAP_PUSH")
        if (blocked.isEmpty()) { println("[Remove SMS & MMS permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove SMS & MMS permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Phone & calls ───

@Suppress("unused")
val removePhonePermissionsPatch = resourcePatch(
    name = "Remove phone & call permissions",
    description = "Strips the app's telephony declarations — silent dialing, identity " +
        "correlation, call hijacking and call-record tampering. All removals are on by " +
        "default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripCallPhone by booleanOption(
        key = "stripCallPhone",
        default = true,
        title = "CALL_PHONE",
        description = "Silent dialing without the dialer — premium-rate fraud and tracking " +
            "callbacks.",
    )

    val stripReadPhoneState by booleanOption(
        key = "stripReadPhoneState",
        default = true,
        title = "READ_PHONE_STATE",
        description = "Phone identifiers and call state for identity correlation, harvested " +
            "offline.",
    )

    val stripReadPhoneNumbers by booleanOption(
        key = "stripReadPhoneNumbers",
        default = true,
        title = "READ_PHONE_NUMBERS",
        description = "Your phone number — identity linkage and SIM-swap prep.",
    )

    val stripAnswerPhoneCalls by booleanOption(
        key = "stripAnswerPhoneCalls",
        default = true,
        title = "ANSWER_PHONE_CALLS",
        description = "Answer and control incoming calls — a call-hijack primitive.",
    )

    val stripReadCallLog by booleanOption(
        key = "stripReadCallLog",
        default = true,
        title = "READ_CALL_LOG",
        description = "Your who-talks-to-whom call history, captured offline.",
    )

    val stripWriteCallLog by booleanOption(
        key = "stripWriteCallLog",
        default = true,
        title = "WRITE_CALL_LOG",
        description = "Integrity attack: edits or deletes call records to cover tracks.",
    )

    val stripUseSip by booleanOption(
        key = "stripUseSip",
        default = true,
        title = "USE_SIP",
        description = "Internet telephony; neutralized once INTERNET is removed, stripped as " +
            "hardening.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripCallPhone ?: true) blocked.add("android.permission.CALL_PHONE")
        if (stripReadPhoneState ?: true) blocked.add("android.permission.READ_PHONE_STATE")
        if (stripReadPhoneNumbers ?: true) blocked.add("android.permission.READ_PHONE_NUMBERS")
        if (stripAnswerPhoneCalls ?: true) blocked.add("android.permission.ANSWER_PHONE_CALLS")
        if (stripReadCallLog ?: true) blocked.add("android.permission.READ_CALL_LOG")
        if (stripWriteCallLog ?: true) blocked.add("android.permission.WRITE_CALL_LOG")
        if (stripUseSip ?: true) blocked.add("android.permission.USE_SIP")
        if (blocked.isEmpty()) { println("[Remove phone & call permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove phone & call permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Nearby radios ───

@Suppress("unused")
val removeNearbyRadioPermissionsPatch = resourcePatch(
    name = "Remove nearby radio permissions",
    description = "Strips the app's Bluetooth, UWB, Wi-Fi Aware and NFC declarations — beacon " +
        "presence mapping, discoverability and direct RF exfiltration links, all " +
        "offline-capable. All removals are on by default — open the patch options (gear) to " +
        "keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripBluetooth by booleanOption(
        key = "stripBluetooth",
        default = true,
        title = "BLUETOOTH",
        description = "Legacy (pre-12) Bluetooth access — discovery reveals nearby devices " +
            "and MAC addresses for fingerprinting.",
    )

    val stripBluetoothAdmin by booleanOption(
        key = "stripBluetoothAdmin",
        default = true,
        title = "BLUETOOTH_ADMIN",
        description = "Legacy (pre-12) Bluetooth admin — initiates discovery and makes the " +
            "device discoverable.",
    )

    val stripBluetoothScan by booleanOption(
        key = "stripBluetoothScan",
        default = true,
        title = "BLUETOOTH_SCAN",
        description = "Scans for nearby devices and beacons — a retail-tracker presence " +
            "census, no internet needed.",
    )

    val stripBluetoothConnect by booleanOption(
        key = "stripBluetoothConnect",
        default = true,
        title = "BLUETOOTH_CONNECT",
        description = "Direct RF data link to nearby hardware — an offline exfiltration path.",
    )

    val stripBluetoothAdvertise by booleanOption(
        key = "stripBluetoothAdvertise",
        default = true,
        title = "BLUETOOTH_ADVERTISE",
        description = "Broadcasts a presence beacon your device can be tracked by over RF.",
    )

    val stripUwbRanging by booleanOption(
        key = "stripUwbRanging",
        default = true,
        title = "UWB_RANGING",
        description = "Ultra-wideband fine-ranging — precise proximity and motion analytics, " +
            "collected offline.",
    )

    val stripNearbyWifiDevices by booleanOption(
        key = "stripNearbyWifiDevices",
        default = true,
        title = "NEARBY_WIFI_DEVICES",
        description = "Wi-Fi Aware NAN peer links — direct device-to-device transfer with no " +
            "access point.",
    )

    val stripNfc by booleanOption(
        key = "stripNfc",
        default = true,
        title = "NFC",
        description = "The NFC radio gate — tag reads and short-range RF peer links off the " +
            "internet stack.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripBluetooth ?: true) blocked.add("android.permission.BLUETOOTH")
        if (stripBluetoothAdmin ?: true) blocked.add("android.permission.BLUETOOTH_ADMIN")
        if (stripBluetoothScan ?: true) blocked.add("android.permission.BLUETOOTH_SCAN")
        if (stripBluetoothConnect ?: true) blocked.add("android.permission.BLUETOOTH_CONNECT")
        if (stripBluetoothAdvertise ?: true) blocked.add("android.permission.BLUETOOTH_ADVERTISE")
        if (stripUwbRanging ?: true) blocked.add("android.permission.UWB_RANGING")
        if (stripNearbyWifiDevices ?: true) blocked.add("android.permission.NEARBY_WIFI_DEVICES")
        if (stripNfc ?: true) blocked.add("android.permission.NFC")
        if (blocked.isEmpty()) { println("[Remove nearby radio permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove nearby radio permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Network & Wi-Fi state ───

@Suppress("unused")
val removeNetworkStatePermissionsPatch = resourcePatch(
    name = "Remove network state permissions",
    description = "Strips the app's network-visibility declarations — Wi-Fi identity, VPN " +
        "detection and LAN inventory. All removals are on by default — open the patch options " +
        "(gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripAccessWifiState by booleanOption(
        key = "stripAccessWifiState",
        default = true,
        title = "ACCESS_WIFI_STATE",
        description = "Wi-Fi connection details — network names and BSSIDs identify and " +
            "locate you.",
    )

    val stripAccessNetworkState by booleanOption(
        key = "stripAccessNetworkState",
        default = true,
        title = "ACCESS_NETWORK_STATE",
        description = "Current network type and availability; lets ad SDKs detect active " +
            "VPNs.",
    )

    val stripAccessLocalNetwork by booleanOption(
        key = "stripAccessLocalNetwork",
        default = true,
        title = "ACCESS_LOCAL_NETWORK",
        description = "Android 16+ gate for LAN access — inventories your home network, " +
            "every device a fingerprint.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripAccessWifiState ?: true) blocked.add("android.permission.ACCESS_WIFI_STATE")
        if (stripAccessNetworkState ?: true) blocked.add("android.permission.ACCESS_NETWORK_STATE")
        if (stripAccessLocalNetwork ?: true) blocked.add("android.permission.ACCESS_LOCAL_NETWORK")
        if (blocked.isEmpty()) { println("[Remove network state permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove network state permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Internet (single permission — no gear) ───

@Suppress("unused")
val removeInternetPermissionPatch = resourcePatch(
    name = "Remove INTERNET permission",
    description = "Removes the android.permission.INTERNET permission from the manifest. " +
        "Blocks every socket the app opens, so bundled ad, analytics and telemetry SDKs cannot " +
        "phone home. Also disables any legitimate online features — only enable for apps you " +
        "want fully offline.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        var removed = 0
        document("AndroidManifest.xml").use { doc ->
            removed = stripSelectedPermissions(doc, setOf("android.permission.INTERNET"))
        }
        println("[Remove INTERNET permission] Stripped $removed INTERNET declaration(s).")
    }
}

// ─── Sensors & motion ───

@Suppress("unused")
val removeSensorPermissionsPatch = resourcePatch(
    name = "Remove sensor & motion permissions",
    description = "Strips the app's motion and body-sensor declarations — activity " +
        "inference, tap logging, gait profiling and device fingerprinting. All removals are " +
        "on by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripOtherSensors by booleanOption(
        key = "stripOtherSensors",
        default = true,
        title = "OTHER_SENSORS",
        description = "Android 15+ 'other' sensors — accelerometer, magnetometer: activity " +
            "inference and fingerprinting.",
    )

    val stripActivityRecognition by booleanOption(
        key = "stripActivityRecognition",
        default = true,
        title = "ACTIVITY_RECOGNITION",
        description = "Gait and motion-state profiling — a cheap, continuous passive " +
            "fingerprint.",
    )

    val stripBodySensors by booleanOption(
        key = "stripBodySensors",
        default = true,
        title = "BODY_SENSORS",
        description = "Health signals such as heart rate where sensors exist.",
    )

    val stripBodySensorsBackground by booleanOption(
        key = "stripBodySensorsBackground",
        default = true,
        title = "BODY_SENSORS_BACKGROUND",
        description = "Continuous background body-sensor reads with no visible app use.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripOtherSensors ?: true) blocked.add("android.permission.OTHER_SENSORS")
        if (stripActivityRecognition ?: true) blocked.add("android.permission.ACTIVITY_RECOGNITION")
        if (stripBodySensors ?: true) blocked.add("android.permission.BODY_SENSORS")
        if (stripBodySensorsBackground ?: true) blocked.add("android.permission.BODY_SENSORS_BACKGROUND")
        if (blocked.isEmpty()) { println("[Remove sensor & motion permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove sensor & motion permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Media & storage ───

@Suppress("unused")
val removeMediaStoragePermissionsPatch = resourcePatch(
    name = "Remove media & storage permissions",
    description = "Strips the app's media and shared-storage declarations — the full library " +
        "including PII screenshots, EXIF GPS location history, and legacy shared-storage " +
        "integrity. All removals are on by default — open the patch options (gear) to keep " +
        "specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripReadMediaImages by booleanOption(
        key = "stripReadMediaImages",
        default = true,
        title = "READ_MEDIA_IMAGES",
        description = "Your entire photo library, including PII screenshots, captured offline.",
    )

    val stripReadMediaVideo by booleanOption(
        key = "stripReadMediaVideo",
        default = true,
        title = "READ_MEDIA_VIDEO",
        description = "Your entire video library, harvested offline.",
    )

    val stripReadMediaAudio by booleanOption(
        key = "stripReadMediaAudio",
        default = true,
        title = "READ_MEDIA_AUDIO",
        description = "Voice notes and recordings, harvested offline.",
    )

    val stripReadMediaVisualUserSelected by booleanOption(
        key = "stripReadMediaVisualUserSelected",
        default = true,
        title = "READ_MEDIA_VISUAL_USER_SELECTED",
        description = "The specific media items granted in the partial-photo-access flow.",
    )

    val stripAccessMediaLocation by booleanOption(
        key = "stripAccessMediaLocation",
        default = true,
        title = "ACCESS_MEDIA_LOCATION",
        description = "EXIF GPS from shared media — a location-history leak bypassing " +
            "location permissions.",
    )

    val stripReadExternalStorage by booleanOption(
        key = "stripReadExternalStorage",
        default = true,
        title = "READ_EXTERNAL_STORAGE",
        description = "Legacy (API ≤32) shared-storage read, including other apps' Download " +
            "dropboxes.",
    )

    val stripWriteExternalStorage by booleanOption(
        key = "stripWriteExternalStorage",
        default = true,
        title = "WRITE_EXTERNAL_STORAGE",
        description = "Legacy (API ≤32) shared-storage write — an integrity surface over every " +
            "app's Downloads.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripReadMediaImages ?: true) blocked.add("android.permission.READ_MEDIA_IMAGES")
        if (stripReadMediaVideo ?: true) blocked.add("android.permission.READ_MEDIA_VIDEO")
        if (stripReadMediaAudio ?: true) blocked.add("android.permission.READ_MEDIA_AUDIO")
        if (stripReadMediaVisualUserSelected ?: true) blocked.add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
        if (stripAccessMediaLocation ?: true) blocked.add("android.permission.ACCESS_MEDIA_LOCATION")
        if (stripReadExternalStorage ?: true) blocked.add("android.permission.READ_EXTERNAL_STORAGE")
        if (stripWriteExternalStorage ?: true) blocked.add("android.permission.WRITE_EXTERNAL_STORAGE")
        if (blocked.isEmpty()) { println("[Remove media & storage permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove media & storage permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Notifications (single permission — no gear) ───

@Suppress("unused")
val removeNotificationPermissionPatch = resourcePatch(
    name = "Remove notification permission",
    description = "Removes the android.permission.POST_NOTIFICATIONS permission from the " +
        "manifest — a lure and phishing delivery channel into your notification shade. Apps " +
        "go quiet; that is the point.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        var removed = 0
        document("AndroidManifest.xml").use { doc ->
            removed = stripSelectedPermissions(doc, setOf("android.permission.POST_NOTIFICATIONS"))
        }
        println("[Remove notification permission] Stripped $removed POST_NOTIFICATIONS declaration(s).")
    }
}

// ─── Special-access grant screens ───

@Suppress("unused")
val removeSpecialAccessPermissionsPatch = resourcePatch(
    name = "Remove special-access permissions",
    description = "Strips the app's special-access declarations — usage-history surveillance, " +
        "overlay tapjacking, all-files access, the dropper channel and settings hijacking; " +
        "removal means the app can never reach its Settings grant state. All removals are on " +
        "by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripPackageUsageStats by booleanOption(
        key = "stripPackageUsageStats",
        default = true,
        title = "PACKAGE_USAGE_STATS",
        description = "The Usage Access grant — your full app-usage history; the " +
            "parental-control/stalkerware channel.",
    )

    val stripSystemAlertWindow by booleanOption(
        key = "stripSystemAlertWindow",
        default = true,
        title = "SYSTEM_ALERT_WINDOW",
        description = "The overlay grant — tapjacking, fake-dialog phishing and ad spam over " +
            "other apps.",
    )

    val stripManageExternalStorage by booleanOption(
        key = "stripManageExternalStorage",
        default = true,
        title = "MANAGE_EXTERNAL_STORAGE",
        description = "The All-files-access grant — read and write over the entire shared " +
            "storage.",
    )

    val stripManageMedia by booleanOption(
        key = "stripManageMedia",
        default = true,
        title = "MANAGE_MEDIA",
        description = "The Media-management grant — modify and delete all media without " +
            "per-item grants.",
    )

    val stripRequestInstallPackages by booleanOption(
        key = "stripRequestInstallPackages",
        default = true,
        title = "REQUEST_INSTALL_PACKAGES",
        description = "The dropper channel — silently self-update and sideload further APKs.",
    )

    val stripWriteSettings by booleanOption(
        key = "stripWriteSettings",
        default = true,
        title = "WRITE_SETTINGS",
        description = "The system-settings grant — an integrity surface over ringtone, DND " +
            "and default apps.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripPackageUsageStats ?: true) blocked.add("android.permission.PACKAGE_USAGE_STATS")
        if (stripSystemAlertWindow ?: true) blocked.add("android.permission.SYSTEM_ALERT_WINDOW")
        if (stripManageExternalStorage ?: true) blocked.add("android.permission.MANAGE_EXTERNAL_STORAGE")
        if (stripManageMedia ?: true) blocked.add("android.permission.MANAGE_MEDIA")
        if (stripRequestInstallPackages ?: true) blocked.add("android.permission.REQUEST_INSTALL_PACKAGES")
        if (stripWriteSettings ?: true) blocked.add("android.permission.WRITE_SETTINGS")
        if (blocked.isEmpty()) { println("[Remove special-access permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove special-access permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Signature-declared surveillance signals (inert hardening, BIND_* class) ───

@Suppress("unused")
val removeSurveillanceDeclarationPermissionsPatch = resourcePatch(
    name = "Remove surveillance service declarations",
    description = "Strips the BIND_* family — system-signature declarations no third-party " +
        "app can ever be granted, kept in the manifest only as intent markers for " +
        "accessibility scraping, device admin, notification listening and keylogging. All " +
        "removals are on by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripBindAccessibilityService by booleanOption(
        key = "stripBindAccessibilityService",
        default = true,
        title = "BIND_ACCESSIBILITY_SERVICE",
        description = "System-signature declaration no third-party app can hold — the " +
            "accessibility-scraping stalkerware marker.",
    )

    val stripBindDeviceAdmin by booleanOption(
        key = "stripBindDeviceAdmin",
        default = true,
        title = "BIND_DEVICE_ADMIN",
        description = "System-signature declaration signaling device-admin intent — remote " +
            "lock, wipe, enforced policy.",
    )

    val stripBindNotificationListener by booleanOption(
        key = "stripBindNotificationListener",
        default = true,
        title = "BIND_NOTIFICATION_LISTENER_SERVICE",
        description = "System-signature declaration signaling a notification listener — " +
            "every notification, including 2FA codes.",
    )

    val stripBindInputMethod by booleanOption(
        key = "stripBindInputMethod",
        default = true,
        title = "BIND_INPUT_METHOD",
        description = "System-signature declaration signaling a keyboard — every keystroke " +
            "in every app.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripBindAccessibilityService ?: true) blocked.add("android.permission.BIND_ACCESSIBILITY_SERVICE")
        if (stripBindDeviceAdmin ?: true) blocked.add("android.permission.BIND_DEVICE_ADMIN")
        if (stripBindNotificationListener ?: true) blocked.add("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE")
        if (stripBindInputMethod ?: true) blocked.add("android.permission.BIND_INPUT_METHOD")
        if (blocked.isEmpty()) { println("[Remove surveillance service declarations] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove surveillance service declarations] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Install-time persistence and wakeups ───

@Suppress("unused")
val removePersistenceWakeupPermissionsPatch = resourcePatch(
    name = "Remove persistence & wakeup permissions",
    description = "Strips the app's persistence and wake-scheduling declarations — boot " +
        "auto-start, CPU hold and precise periodic wakeups, the backbone of always-on " +
        "background telemetry. All removals are on by default — open the patch options " +
        "(gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripBootCompleted by booleanOption(
        key = "stripBootCompleted",
        default = true,
        title = "RECEIVE_BOOT_COMPLETED",
        description = "Auto-start on every boot — the persistence backbone of always-on " +
            "telemetry.",
    )

    val stripWakeLock by booleanOption(
        key = "stripWakeLock",
        default = true,
        title = "WAKE_LOCK",
        description = "Holds the CPU awake — the enabler of unbounded background processing " +
            "and beacons.",
    )

    val stripScheduleExactAlarm by booleanOption(
        key = "stripScheduleExactAlarm",
        default = true,
        title = "SCHEDULE_EXACT_ALARM",
        description = "Precise scheduled wakeups — the backbone of periodic telemetry " +
            "beacons.",
    )

    val stripUseExactAlarm by booleanOption(
        key = "stripUseExactAlarm",
        default = true,
        title = "USE_EXACT_ALARM",
        description = "Non-revocable exact-alarm bypass; removal is the only way to revoke it.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripBootCompleted ?: true) blocked.add("android.permission.RECEIVE_BOOT_COMPLETED")
        if (stripWakeLock ?: true) blocked.add("android.permission.WAKE_LOCK")
        if (stripScheduleExactAlarm ?: true) blocked.add("android.permission.SCHEDULE_EXACT_ALARM")
        if (stripUseExactAlarm ?: true) blocked.add("android.permission.USE_EXACT_ALARM")
        if (blocked.isEmpty()) { println("[Remove persistence & wakeup permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove persistence & wakeup permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Health Connect ───

@Suppress("unused")
val removeHealthConnectPermissionsPatch = resourcePatch(
    name = "Remove Health Connect permissions",
    description = "Strips the app's Health Connect declarations — background reads granted " +
        "without a separate prompt, and history reaching back before install. All removals " +
        "are on by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripHealthBackground by booleanOption(
        key = "stripHealthBackground",
        default = true,
        title = "READ_HEALTH_DATA_IN_BACKGROUND",
        description = "Background health reads — steps, sleep, heart rate — granted without a " +
            "separate prompt.",
    )

    val stripHealthHistory by booleanOption(
        key = "stripHealthHistory",
        default = true,
        title = "READ_HEALTH_DATA_HISTORY",
        description = "History access reaching back to health data from before the app was " +
            "installed.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripHealthBackground ?: true) blocked.add("android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND")
        if (stripHealthHistory ?: true) blocked.add("android.permission.health.READ_HEALTH_DATA_HISTORY")
        if (blocked.isEmpty()) { println("[Remove Health Connect permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove Health Connect permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Privacy Sandbox / advertising ───

@Suppress("unused")
val removeAdPermissionsPatch = resourcePatch(
    name = "Remove ad & Privacy Sandbox permissions",
    description = "Strips the app's ad-stack declarations — interest profiling, the " +
        "advertising identifier and cross-app attribution measurement. All removals are on " +
        "by default — open the patch options (gear) to keep specific ones.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripAdTopics by booleanOption(
        key = "stripAdTopics",
        default = true,
        title = "ACCESS_ADSERVICES_TOPICS",
        description = "Privacy Sandbox Topics — your advertising-interest profile.",
    )

    val stripAdId by booleanOption(
        key = "stripAdId",
        default = true,
        title = "ACCESS_AD_SERVICES_AD_ID",
        description = "The Privacy Sandbox advertising ID used for attribution and profiling.",
    )

    val stripAdAttribution by booleanOption(
        key = "stripAdAttribution",
        default = true,
        title = "ACCESS_ADSERVICES_ATTRIBUTION",
        description = "Cross-app attribution measurement — install and conversion matching.",
    )

    execute {
        val blocked = mutableSetOf<String>()
        if (stripAdTopics ?: true) blocked.add("android.permission.ACCESS_ADSERVICES_TOPICS")
        if (stripAdId ?: true) blocked.add("android.permission.ACCESS_AD_SERVICES_AD_ID")
        if (stripAdAttribution ?: true) blocked.add("android.permission.ACCESS_ADSERVICES_ATTRIBUTION")
        if (blocked.isEmpty()) { println("[Remove ad & Privacy Sandbox permissions] Skipped: no permissions selected."); return@execute }

        var removed = 0
        document("AndroidManifest.xml").use { doc -> removed = stripSelectedPermissions(doc, blocked) }
        println("[Remove ad & Privacy Sandbox permissions] Stripped $removed of ${blocked.size} selected permission declaration(s).")
    }
}

// ─── Screenshot detection (single permission — no gear) ───

@Suppress("unused")
val removeScreenshotDetectionPermissionPatch = resourcePatch(
    name = "Remove screenshot detection permission",
    description = "Removes the android.permission.DETECT_SCREEN_CAPTURE permission from the " +
        "manifest (Android 15+) — banking apps scold you and surveillance apps learn that " +
        "evidence is being collected. Removing it makes your screenshots silent. Pair with " +
        "the Universal Screenshot Protection Bypass to also clear FLAG_SECURE.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        var removed = 0
        document("AndroidManifest.xml").use { doc ->
            removed = stripSelectedPermissions(doc, setOf("android.permission.DETECT_SCREEN_CAPTURE"))
        }
        println("[Remove screenshot detection permission] Stripped $removed DETECT_SCREEN_CAPTURE declaration(s).")
    }
}

// ─── Silent downloads (single permission — no gear) ───

@Suppress("unused")
val removeSilentDownloadPermissionPatch = resourcePatch(
    name = "Remove silent download permission",
    description = "Removes android.permission.DOWNLOAD_WITHOUT_NOTIFICATION — the grant apps " +
        "declare to run DownloadManager transfers with hidden visibility, so payload pulls " +
        "(including dropper self-updates) happen with no visible notification. Once stripped, a " +
        "hidden download request fails loudly at enqueue (SecurityException: Invalid value for " +
        "visibility: 2) instead of hiding — the transfer can no longer be invisible. Pairs with " +
        "the REQUEST_INSTALL_PACKAGES toggle in the special-access patch; apps that legitimately " +
        "download quietly in the background (e.g. podcast auto-updates) will surface " +
        "notifications or fail at the attempt — smoke test per app.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        var removed = 0
        document("AndroidManifest.xml").use { doc ->
            removed = stripSelectedPermissions(doc, setOf("android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"))
        }
        println("[Remove silent download permission] Stripped $removed DOWNLOAD_WITHOUT_NOTIFICATION declaration(s).")
    }
}
