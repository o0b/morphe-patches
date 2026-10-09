# o0b Patches

Morphe patches 

## ❓ About

https://morphe-patches.software/

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=o0b/morphe-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.5.0](https://github.com/o0b/morphe-patches/releases/tag/v1.5.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;89 patches total
<details open>
<summary>📦 Nova Launcher&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**🎯 Supported versions:**

| 81006 (8.1.6) |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Bugsnag](#disable-bugsnag) | No-ops Bugsnag on Nova Launcher 8.1.6: both HTTP deliverers return DELIVERED so no payload ever leaves the device (queued error files are deleted as sent), and Nova's error-reporting flag is forced false — which also disables novalytics usage recording. The client still initializes; crash data stays local-only. |  |
| [Disable Sesame integrations](#disable-sesame-integrations) | Disables Nova Launcher 8.1.6's Sesame third-party integrations (Spotify, OneDrive, Twitch, Slack, Discord, Dropbox, Deezer, GitHub): the integration ingest always returns 0 items, so no background or manual data pulls run and their APIs are never contacted. The Sesame engine and local search indexing continue to work. |  |
| [Disable Sesame search results](#disable-sesame-search-results) | Removes the bundled Sesame (Branch) deep-shortcut results from Nova Launcher's search on 8.1.6 by no-op'ing its results provider. Search still returns apps, contacts and settings. |  |
| [Unlock Prime](#unlock-prime) | Unlocks Nova Launcher Prime on 8.1.6 by forcing the runtime prime flags (Lzg/t1;->y / ->t) to true at every write — the settings initializer and the Prime-unlocker package-change handler. |  |

</details>

<details open>
<summary>📦 meteoblue Weather&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| Cirrus Uncinus 3.1.4 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Premium](#unlock-premium) | Unlocks all meteoblue Weather premium features by making the billing data source's purchase-state flow always report an active purchase. |  |

</details>

<details open>
<summary>📦 Octopi Launcher&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 1.92 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks Octopi Launcher Pro by returning true from the isPro LiveData getter, bypassing all pro feature gates without modifying billing or database logic. |  |

</details>

<details open>
<summary>📦 Wavelet&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 26.05 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Unlocks Wavelet's pro features (Reverberation, Virtualizer, Bass tuner, Equal loudness) by initializing the purchase-verified state flow to true and making the purchase processor always report a verified purchase, which also suppresses the purchase flow. |  |

</details>

<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;82 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Clear task affinity](#clear-task-affinity) | Sets android:taskAffinity to empty on the application element, so no other installed app can clone this app's affinity to interleave phishing UI in front of it — the StrandHogg pattern. Document-centric apps relying on custom per-activity affinities keep them. |  |
| [Disable Android debugging](#disable-android-debugging) | Sets android:debuggable=false on the application element, blocking JDWP debugger attach and `run-as` access to the app's private data over ADB. Apps that rely on debug-only behavior will lose it. |  |
| [Disable ad SDK calls](#disable-ad-sdk-calls) | No-ops common ad SDK load/show/init/fetch methods in bundled ad packages. |  |
| [Disable auto backup](#disable-auto-backup) | Sets android:allowBackup=false on the application element, excluding the app's private data — databases, preferences, tokens — from cloud auto backup, adb backup and device-transfer migrations. Apps that rely on backup-restore for migration will lose that. |  |
| [Disable cleartext traffic](#disable-cleartext-traffic) | Sets android:usesCleartextTraffic=false on the application element, forcing all traffic to HTTPS. Plaintext HTTP can be read and injected by anyone on the network. Apps that legitimately use plain HTTP (local device dashboards, IoT) lose those requests. |  |
| [Disable legacy external storage](#disable-legacy-external-storage) | Sets android:requestLegacyExternalStorage=false on the application element, confining the app to scoped storage — its own directories and media collections instead of raw access to every app's shared files. Only affects apps targeting Android 10; file-manager-style apps lose raw shared-storage paths. |  |
| [Disable show-when-locked](#disable-show-when-locked) | Disables android:showWhenLocked on every activity that declared it. No app screen can display over the lock screen anymore. Stalkerware and scareware use lock-screen overlays to fake unlock prompts and push alerts while the phone is locked; alarm, incoming-call and 2FA screens lose their over-lock UX. |  |
| [Disable task re-parenting](#disable-task-re-parenting) | Sets allowTaskReparenting=false on the application element and on every activity that declared it, closing the re-parenting mechanism a malicious task uses to slide in front of this app — StrandHogg 2.0's route. Apps that rely on cross-task re-parenting lose it. |  |
| [Disable turn-screen-on](#disable-turn-screen-on) | Disables android:turnScreenOn on every activity that declared it. No activity can wake the screen by itself anymore. Ad and spyware screen-wake behavior dies with it — and so do alarm and incoming-call screens. |  |
| [Enable auto backup](#enable-auto-backup) | Sets android:allowBackup=true on the application element, restoring the platform default for apps that opted out — their private data joins cloud auto backup and device transfers. The developer's exclusion of secrets and license tokens is overridden; license checks may break after a restore. |  |
| [Force launcher singleTask](#force-launcher-singletask) | Sets launchMode=singleTask on launcher activities, pinning the app's root so no other activity can share its task. Recents behavior can change: relaunching from the home screen resumes instead of restarting, and splash-screen launchers may behave differently. |  |
| [Remove ACCESS_ADSERVICES_ATTRIBUTION permission](#remove-access-adservices-attribution-permission) | Removes the android.permission.ACCESS_ADSERVICES_ATTRIBUTION permission from the manifest. Android 13+ Privacy Sandbox: lets the app measure ad attribution — install and conversion events matched across apps. Completes the adservices family alongside the TOPICS and AD_ID removal patches. |  |
| [Remove ACCESS_ADSERVICES_TOPICS permission](#remove-access-adservices-topics-permission) | Removes the android.permission.ACCESS_ADSERVICES_TOPICS permission from the manifest. Android 13+ Privacy Sandbox: lets the app read your Topics advertising-interest profile. Removing it opts the app out of interest-based ad profiling. |  |
| [Remove ACCESS_AD_SERVICES_AD_ID permission](#remove-access-ad-services-ad-id-permission) | Removes the android.permission.ACCESS_AD_SERVICES_AD_ID permission from the manifest. Android 13+ Privacy Sandbox: lets the app read the advertising ID used for attribution and profiling. Removing it revokes that access, breaking cross-app ad tracking. |  |
| [Remove ACCESS_BACKGROUND_LOCATION permission](#remove-access-background-location-permission) | Removes the android.permission.ACCESS_BACKGROUND_LOCATION permission from the manifest. Grants a silent 24/7 location trail with no visible app use — the highest-risk location permission. Removing it caps the app to while-in-use location only; weather and map apps that never need background use are unaffected. |  |
| [Remove ACCESS_COARSE_LOCATION permission](#remove-access-coarse-location-permission) | Removes the android.permission.ACCESS_COARSE_LOCATION permission from the manifest. Grants an approximate position stream — lower fidelity than fine location but the same pattern-of-life leak, and it works offline. |  |
| [Remove ACCESS_FINE_LOCATION permission](#remove-access-fine-location-permission) | Removes the android.permission.ACCESS_FINE_LOCATION permission from the manifest. Grants a GNSS pattern-of-life trail — home, work, habits. GPS is receive-only and works fully offline with no network needed. Maps and navigation apps need it. |  |
| [Remove ACCESS_LOCAL_NETWORK permission](#remove-access-local-network-permission) | Removes the android.permission.ACCESS_LOCAL_NETWORK permission from the manifest. Android 16+ gate for local-network access: lets the app scan and inventory your LAN, where every device and vendor is a fingerprintable data point. Removing it blocks local network access; casting and LAN apps will lose it. |  |
| [Remove ACCESS_MEDIA_LOCATION permission](#remove-access-media-location-permission) | Removes the android.permission.ACCESS_MEDIA_LOCATION permission from the manifest. Extracts EXIF GPS from shared media — a location-history leak that bypasses location permissions entirely. Photo apps lose location grouping. |  |
| [Remove ACCESS_NETWORK_STATE permission](#remove-access-network-state-permission) | Removes the android.permission.ACCESS_NETWORK_STATE permission from the manifest. Reveals the current network type and availability, and lets ad SDKs detect active VPNs to work around ad blocking. Pair it with removing INTERNET to fully blind ad and analytics SDKs. |  |
| [Remove ACCESS_WIFI_STATE permission](#remove-access-wifi-state-permission) | Removes the android.permission.ACCESS_WIFI_STATE permission from the manifest. Exposes Wi-Fi connection details: network names and BSSIDs identify your home network, and BSSID-to-location databases can place you physically. Apps that gate large downloads on Wi-Fi may misbehave. |  |
| [Remove ACTIVITY_RECOGNITION permission](#remove-activity-recognition-permission) | Removes the android.permission.ACTIVITY_RECOGNITION permission from the manifest. Builds a gait and motion-state behavioral profile — a cheap, continuous, passive fingerprint of how you move. Fitness step counters need it. |  |
| [Remove AD_ID permission (Google Play Services)](#remove-ad-id-permission-google-play-services) | Removes the com.google.android.gms.permission.AD_ID permission from the manifest. Required since Android 12 for an app to read the Google Play advertising ID — the cross-app identifier ad SDKs profile and attribute against. Removing it makes the app read a zeroed ad ID instead. |  |
| [Remove ANSWER_PHONE_CALLS permission](#remove-answer-phone-calls-permission) | Removes the android.permission.ANSWER_PHONE_CALLS permission from the manifest. Lets the app answer and control incoming calls — a call-hijack primitive used by spyware. No normal app needs it. |  |
| [Remove BIND_ACCESSIBILITY_SERVICE permission](#remove-bind-accessibility-service-permission) | Removes the android.permission.BIND_ACCESSIBILITY_SERVICE permission from the manifest. A system-signature permission no third-party app can ever be granted; declaring it only signals intent to use accessibility scraping — reading all screen content, keystrokes and notifications, the classic stalkerware pattern. Stripping the declaration keeps it inert. |  |
| [Remove BIND_DEVICE_ADMIN permission](#remove-bind-device-admin-permission) | Removes the android.permission.BIND_DEVICE_ADMIN permission from the manifest. A system-signature permission no third-party app can ever be granted; declaring it only signals device-admin intent — remote lock, wipe and enforced policies, the MDM/stalkerware pattern. Stripping the declaration keeps it inert. |  |
| [Remove BIND_INPUT_METHOD permission](#remove-bind-input-method-permission) | Removes the android.permission.BIND_INPUT_METHOD permission from the manifest. A system-signature permission no third-party app can ever be granted; declaring it signals intent to host a keyboard — every keystroke in every app flows through an IME. Stripping the declaration keeps it inert. |  |
| [Remove BIND_NOTIFICATION_LISTENER_SERVICE permission](#remove-bind-notification-listener-service-permission) | Removes the android.permission.BIND_NOTIFICATION_LISTENER_SERVICE permission from the manifest. A system-signature permission no third-party app can ever be granted; declaring it signals intent to host a notification listener — reading every notification, including message contents and 2FA codes. The stalkerware staple; stripping the declaration keeps it inert. |  |
| [Remove BLUETOOTH permission](#remove-bluetooth-permission) | Removes the android.permission.BLUETOOTH permission from the manifest. Legacy Bluetooth access (pre-Android 12): device discovery and scanning reveal nearby Bluetooth devices and their unique MAC addresses, which are used for fingerprinting and location. Apps that use Bluetooth accessories on Android 11 and older lose them. |  |
| [Remove BLUETOOTH_ADMIN permission](#remove-bluetooth-admin-permission) | Removes the android.permission.BLUETOOTH_ADMIN permission from the manifest. Legacy (pre-Android 12) Bluetooth admin: can initiate discovery and make your device discoverable — broadcasting presence over RF with no internet needed. Pairs with removing BLUETOOTH; Android 12+ uses the granular SCAN/CONNECT/ADVERTISE permissions instead. |  |
| [Remove BLUETOOTH_ADVERTISE permission](#remove-bluetooth-advertise-permission) | Removes the android.permission.BLUETOOTH_ADVERTISE permission from the manifest. Broadcasts a presence beacon your device can be tracked by, advertising device identity over RF with no internet needed. Beacon-transmit apps lose the function. |  |
| [Remove BLUETOOTH_CONNECT permission](#remove-bluetooth-connect-permission) | Removes the android.permission.BLUETOOTH_CONNECT permission from the manifest. Opens a direct RF data link to nearby hardware — an exfiltration path that never touches the internet stack. Apps that talk to Bluetooth accessories lose them. |  |
| [Remove BLUETOOTH_SCAN permission](#remove-bluetooth-scan-permission) | Removes the android.permission.BLUETOOTH_SCAN permission from the manifest. Scans for nearby Bluetooth devices and beacons — a retail-tracker presence census that needs no internet to collect. Bluetooth device pairing flows lose discovery. |  |
| [Remove BODY_SENSORS permission](#remove-body-sensors-permission) | Removes the android.permission.BODY_SENSORS permission from the manifest. Reads health signals such as heart rate where sensors exist. Watch and fitness apps need it; everything else rarely does. |  |
| [Remove BODY_SENSORS_BACKGROUND permission](#remove-body-sensors-background-permission) | Removes the android.permission.BODY_SENSORS_BACKGROUND permission from the manifest. Grants continuous background body-sensor reads with no visible app use — the silent variant of BODY_SENSORS. Only health apps with background monitoring declare it. |  |
| [Remove CALL_PHONE permission](#remove-call-phone-permission) | Removes the android.permission.CALL_PHONE permission from the manifest. Lets the app place calls directly without opening the dialer. It is granted automatically at install with no prompt, so stripping the declaration stops any silent dialing (premium-rate fraud, tracking callbacks). Calls placed through the dialer UI are unaffected. |  |
| [Remove CAMERA permission](#remove-camera-permission) | Removes the android.permission.CAMERA permission from the manifest. Grants the camera: capture of faces, documents, screens and surroundings works fully offline and can be exfiltrated later. Camera and QR-scanning apps need it; almost nothing else does. |  |
| [Remove DETECT_SCREEN_CAPTURE permission](#remove-detect-screen-capture-permission) | Removes the android.permission.DETECT_SCREEN_CAPTURE permission from the manifest. Android 15+: lets the app detect when you take a screenshot — banking apps scold you, surveillance apps learn that evidence is being collected. Removing it makes your screenshots silent. |  |
| [Remove FOREGROUND_SERVICE_CAMERA permission](#remove-foreground-service-camera-permission) | Removes the android.permission.FOREGROUND_SERVICE_CAMERA permission from the manifest. Android 14+ install-time grant for camera-type foreground services — a service context that keeps camera capture running behind a persistent-process notification. Camera apps need it; removing it blocks service-context capture for everything else. |  |
| [Remove FOREGROUND_SERVICE_LOCATION permission](#remove-foreground-service-location-permission) | Removes the android.permission.FOREGROUND_SERVICE_LOCATION permission from the manifest. Android 14+ install-time grant for location-type foreground services — continuous background location behind a persistent-process notification. Navigation and fitness-tracker apps need it. |  |
| [Remove FOREGROUND_SERVICE_MICROPHONE permission](#remove-foreground-service-microphone-permission) | Removes the android.permission.FOREGROUND_SERVICE_MICROPHONE permission from the manifest. Android 14+ install-time grant for microphone-type foreground services — background-capable audio capture behind a persistent-process notification. Recorder and call apps need it. |  |
| [Remove GET_ACCOUNTS permission](#remove-get-accounts-permission) | Removes the android.permission.GET_ACCOUNTS permission from the manifest. Enumerates on-device accounts for cross-service identity linkage. Deprecated and mostly inert on modern Android — stripping it is pure hardening. |  |
| [Remove GMS permissions](#remove-gms-permissions) | Strips the Google Play-defined permission declarations from the manifest: advertising ID, Play Billing, install referrer attribution, push (C2DM/FCM) and Google Services Framework sync. Inert on devices without Google Play; on Play devices it kills attribution, billing, push and ad-ID reads. All removals are on by default — open the patch options (gear) to deselect specific ones. | • Remove AD_ID (Google Play Services)<br>• Remove BILLING (Google Play)<br>• Remove BIND_GET_INSTALL_REFERRER_SERVICE<br>• Remove C2DM RECEIVE (push)<br>• Remove READ_GSERVICES (Google Services Framework) |
| [Remove INTERNET permission](#remove-internet-permission) | Removes the android.permission.INTERNET permission from the manifest. Blocks every socket the app opens, so bundled ad, analytics and telemetry SDKs cannot phone home. Also disables any legitimate online features — only enable for apps you want fully offline. |  |
| [Remove MANAGE_EXTERNAL_STORAGE permission](#remove-manage-external-storage-permission) | Removes the android.permission.MANAGE_EXTERNAL_STORAGE permission from the manifest. Unlocks the 'All files access' special grant — read and write over the entire shared storage: every app's documents, photos and downloads. Removing the declaration means the app can never obtain all-files access; file managers need it. |  |
| [Remove MANAGE_MEDIA permission](#remove-manage-media-permission) | Removes the android.permission.MANAGE_MEDIA permission from the manifest. Unlocks the 'Media management' special grant — modify and delete all media on the device without per-item grants. Gallery apps use it for housekeeping; removing the declaration means the app can never obtain it. |  |
| [Remove NEARBY_WIFI_DEVICES permission](#remove-nearby-wifi-devices-permission) | Removes the android.permission.NEARBY_WIFI_DEVICES permission from the manifest. Wi-Fi Aware NAN peer links provide direct device-to-device data transfer with no internet and no access point at all. Casting and smart-home setup flows will degrade. |  |
| [Remove NFC permission](#remove-nfc-permission) | Removes the android.permission.NFC permission from the manifest. Install-time gate for the NFC radio — tag reads and short-range RF peer links that never touch the internet stack. Wallet and payment apps break; almost everything else never touches NFC. |  |
| [Remove OTHER_SENSORS permission](#remove-other-sensors-permission) | Removes the android.permission.OTHER_SENSORS permission from the manifest. Android 15+ access to the 'other' sensors: ambient sensors like the accelerometer and magnetometer enable activity inference, tap logging and device fingerprinting. Removing it blinds sensor-based tracking. |  |
| [Remove PACKAGE_USAGE_STATS permission](#remove-package-usage-stats-permission) | Removes the android.permission.PACKAGE_USAGE_STATS permission from the manifest. Apps declare this signature|appop permission to route you to the Usage Access screen in Settings — granting it hands over your full app-usage history: what you open, when, and for how long. The parental-control/stalkerware channel; stripping the declaration means the app can never receive the grant. |  |
| [Remove POST_NOTIFICATIONS permission](#remove-post-notifications-permission) | Removes the android.permission.POST_NOTIFICATIONS permission from the manifest. Grants a lure and phishing delivery channel into your notification shade — harvested credentials store offline for later use. Apps go quiet; that is the point. |  |
| [Remove READ_CALENDAR permission](#remove-read-calendar-permission) | Removes the android.permission.READ_CALENDAR permission from the manifest. Reads schedules, meeting links, attendees and locations — rich metadata harvested offline for later exfiltration. Calendar apps need it. |  |
| [Remove READ_CALL_LOG permission](#remove-read-call-log-permission) | Removes the android.permission.READ_CALL_LOG permission from the manifest. Reads your call history — a who-talks-to-whom graph captured offline for later exfiltration. Dialer apps need it. |  |
| [Remove READ_CONTACTS permission](#remove-read-contacts-permission) | Removes the android.permission.READ_CONTACTS permission from the manifest. Reads your full social graph — names, numbers, emails, addresses and notes — captured offline for later exfiltration. Messaging apps need it for contact names. |  |
| [Remove READ_EXTERNAL_STORAGE permission](#remove-read-external-storage-permission) | Removes the android.permission.READ_EXTERNAL_STORAGE permission from the manifest. Legacy (API ≤32) shared-storage read, including other apps' Download dropboxes; modern builds migrate to READ_MEDIA_*. Removing it covers older targets. |  |
| [Remove READ_HEALTH_DATA_HISTORY permission](#remove-read-health-data-history-permission) | Removes the android.permission.health.READ_HEALTH_DATA_HISTORY permission from the manifest. Health Connect history access: lets the app read health data from before it was ever installed — reaching back to history you never granted it. Removing it caps the app to data generated after install. |  |
| [Remove READ_HEALTH_DATA_IN_BACKGROUND permission](#remove-read-health-data-in-background-permission) | Removes the android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND permission from the manifest. Health Connect background access: lets the app read your health data — steps, sleep, heart rate — while not in use, granted silently alongside other grants with no separate prompt. Fitness dashboards break; background health surveillance does not. |  |
| [Remove READ_MEDIA_AUDIO permission](#remove-read-media-audio-permission) | Removes the android.permission.READ_MEDIA_AUDIO permission from the manifest. Reads voice notes and recordings offline for later exfiltration. Music and recorder apps need it. |  |
| [Remove READ_MEDIA_IMAGES permission](#remove-read-media-images-permission) | Removes the android.permission.READ_MEDIA_IMAGES permission from the manifest. Reads your entire photo library — including screenshots with PII, bank pages and documents — captured offline for later exfiltration. Gallery and camera apps need it. |  |
| [Remove READ_MEDIA_VIDEO permission](#remove-read-media-video-permission) | Removes the android.permission.READ_MEDIA_VIDEO permission from the manifest. Reads your entire video library offline for later exfiltration. Gallery and camera apps need it. |  |
| [Remove READ_MEDIA_VISUAL_USER_SELECTED permission](#remove-read-media-visual-user-selected-permission) | Removes the android.permission.READ_MEDIA_VISUAL_USER_SELECTED permission from the manifest. Reads the specific media items you granted in the partial-photo-access flow; removing it revokes even that bounded grant. |  |
| [Remove READ_PHONE_NUMBERS permission](#remove-read-phone-numbers-permission) | Removes the android.permission.READ_PHONE_NUMBERS permission from the manifest. Exposes your phone number for identity linkage and SIM-swap prep. Dialer and SMS apps need it; others rarely do. |  |
| [Remove READ_PHONE_STATE permission](#remove-read-phone-state-permission) | Removes the android.permission.READ_PHONE_STATE permission from the manifest. Exposes phone identifiers and call state for identity correlation, harvested offline. Removing it breaks some legacy analytics SDKs by design. |  |
| [Remove READ_SMS permission](#remove-read-sms-permission) | Removes the android.permission.READ_SMS permission from the manifest. Reads message content, including bank and 2FA codes, harvested offline for later exfiltration. Default SMS apps need it. |  |
| [Remove RECEIVE_BOOT_COMPLETED permission](#remove-receive-boot-completed-permission) | Removes the android.permission.RECEIVE_BOOT_COMPLETED permission from the manifest. Granted at install with no prompt: the app auto-starts on every boot — the persistence backbone of always-on background telemetry. Alarm, launcher and messenger apps need it; decide per app. |  |
| [Remove RECEIVE_MMS permission](#remove-receive-mms-permission) | Removes the android.permission.RECEIVE_MMS permission from the manifest. Auto-retrieves MMS payloads — a media-parser attack surface that arrives on the radio path with no internet involved. Default SMS apps need it. |  |
| [Remove RECEIVE_SMS permission](#remove-receive-sms-permission) | Removes the android.permission.RECEIVE_SMS permission from the manifest. Lets the app intercept incoming messages — including OTP codes — the moment they arrive. Default SMS apps need it; nothing else should have it. |  |
| [Remove RECEIVE_WAP_PUSH permission](#remove-receive-wap-push-permission) | Removes the android.permission.RECEIVE_WAP_PUSH permission from the manifest. Receives carrier OMA provisioning and configuration pushes — a silent configuration channel on the radio path that never touches the internet. Almost no legitimate app should declare it. |  |
| [Remove RECORD_AUDIO permission](#remove-record-audio-permission) | Removes the android.permission.RECORD_AUDIO permission from the manifest. Grants the microphone: ambient eavesdropping and long background capture — the stalkerware staple. Audio records to disk offline for later exfiltration; voice apps lose input. |  |
| [Remove REQUEST_INSTALL_PACKAGES permission](#remove-request-install-packages-permission) | Removes the android.permission.REQUEST_INSTALL_PACKAGES permission from the manifest. Unlocks the 'Install unknown apps' special grant — the dropper channel: silently self-update and sideload further APKs outside any store. Removing the declaration means the app can never request installing unknown apps; APK installers obviously need it. |  |
| [Remove SCHEDULE_EXACT_ALARM permission](#remove-schedule-exact-alarm-permission) | Removes the android.permission.SCHEDULE_EXACT_ALARM permission from the manifest. Granted at install: precise scheduled wakeups, the backbone of periodic background telemetry beacons. Android 14+ gates it behind a Settings toggle — this removes the capability entirely. Alarm and calendar apps need it. |  |
| [Remove SEND_SMS permission](#remove-send-sms-permission) | Removes the android.permission.SEND_SMS permission from the manifest. The classic premium-SMS billing fraud channel — monetizes through carrier billing and never touches the internet stack at all. Messaging apps lose in-app sending; they can still share to your SMS app. |  |
| [Remove SYSTEM_ALERT_WINDOW permission](#remove-system-alert-window-permission) | Removes the android.permission.SYSTEM_ALERT_WINDOW permission from the manifest. Unlocks the 'Display over other apps' special grant: overlays drawn on top of other apps enable tapjacking, fake-dialog phishing and ad spam. Removing the declaration means the app can never obtain overlay access. |  |
| [Remove USE_EXACT_ALARM permission](#remove-use-exact-alarm-permission) | Removes the android.permission.USE_EXACT_ALARM permission from the manifest. Granted at install and non-revocable in Settings (normal permissions never show a revoke button): Play policy limits it to alarm and calendar apps, but junk apps declare it to bypass the exact-alarm controls entirely. Removal is the only way to revoke it. |  |
| [Remove USE_SIP permission](#remove-use-sip-permission) | Removes the android.permission.USE_SIP permission from the manifest. Internet telephony. Deprecated and fully neutralized once INTERNET is removed — stripping it hardens against a future re-grant. |  |
| [Remove UWB_RANGING permission](#remove-uwb-ranging-permission) | Removes the android.permission.UWB_RANGING permission from the manifest. Ultra-wideband fine-ranging enables precise proximity and motion analytics with nearby devices, collected fully offline. Digital-key and tracker-tag apps need it. |  |
| [Remove WAKE_LOCK permission](#remove-wake-lock-permission) | Removes the android.permission.WAKE_LOCK permission from the manifest. Granted at install with no prompt: lets the app hold the CPU awake — the enabler of unbounded background processing and periodic telemetry beacons, at the cost of battery. Media and download apps need it. |  |
| [Remove WRITE_CALENDAR permission](#remove-write-calendar-permission) | Removes the android.permission.WRITE_CALENDAR permission from the manifest. Integrity attack vector: injects phantom events with attacker links into the trusted Calendar UI, executed entirely offline. Calendar apps lose write access. |  |
| [Remove WRITE_CALL_LOG permission](#remove-write-call-log-permission) | Removes the android.permission.WRITE_CALL_LOG permission from the manifest. Integrity attack vector: edits or deletes call records to cover an abuser's tracks, executed entirely offline. Dialer apps lose write access. |  |
| [Remove WRITE_CONTACTS permission](#remove-write-contacts-permission) | Removes the android.permission.WRITE_CONTACTS permission from the manifest. Integrity attack vector: can swap bank and support contacts for attacker numbers, executed entirely offline with no network needed. Contact-management apps lose write access. |  |
| [Remove WRITE_EXTERNAL_STORAGE permission](#remove-write-external-storage-permission) | Removes the android.permission.WRITE_EXTERNAL_STORAGE permission from the manifest. Legacy (API ≤32) shared-storage write — an integrity surface over every app's Downloads area. Modern targets no longer use it; stripping it covers older apps. |  |
| [Remove WRITE_SETTINGS permission](#remove-write-settings-permission) | Removes the android.permission.WRITE_SETTINGS permission from the manifest. Unlocks the 'Modify system settings' special grant — an integrity surface over ringtone, do-not-disturb and default-app configuration, the junk-app settings-hijack channel. Removing the declaration means the app can never obtain write access to system settings. |  |

</details>

<!-- PATCHES_END -->

## 📜 License

o0b Patches are licensed under the GNU General Public License v3.0
