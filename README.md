# o0b Patches

Morphe patches

## ❓ About

https://morphe-patches.software/

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=o0b/morphe-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.6.1](https://github.com/o0b/morphe-patches/releases/tag/v1.6.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;28 patches total
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
<summary>📦 Business Calendar 2&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 2.55.6 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable telemetry](#disable-telemetry) | Disables Firebase completely (init provider, analytics, Crashlytics, Remote Config wiring, install UUID), turns off the sale promo push notifications, and prevents Fyber's crash handler from installing at process start. |  |
| [Unlock Pro](#unlock-pro) | Unlocks Business Calendar 2 Pro by forcing the complete pro status and the force-pro override to return true, routing every feature gate to the full package (7) without touching billing. |  |

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
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;19 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Amazon IAP components](#disable-amazon-iap-components) | Disables the orphan Amazon In-App Purchasing response receiver and strips the Amazon PrivacyPass attest permission. Inert without the Amazon Appstore installed. |  |
| [Disable Android debugging](#disable-android-debugging) | Sets android:debuggable=false on the application element, blocking JDWP debugger attach and `run-as` access to the app's private data over ADB. Apps that rely on debug-only behavior will lose it. |  |
| [Disable CafeBazaar components](#disable-cafebazaar-components) | Disables the orphan CafeBazaar in-app billing receiver and strips its pay-through permission. Inert without CafeBazaar installed. |  |
| [Disable Google Play billing components](#disable-google-play-billing-components) | Disables the Play Billing library's proxy billing activities, the Play Core dialog wrapper and asset-pack extraction services, and the Pairip license activity. Billing and licensing permission declarations are handled by the Remove GMS permissions master patch — this patch only disables the components. Purchases were already impossible in re-signed patched APKs. |  |
| [Disable Huawei store components](#disable-huawei-store-components) | Disables orphan Huawei HMS, AGConnect and OTA update SDK components: the HMS bridge and enable-service activities, AGConnect service discovery and initializer, Huawei's in-app OTA updater, package installer and file provider, and the account sign-in hub activities. Strips the AppGallery common-data permission. Inert without Huawei AppGallery installed. |  |
| [Disable OneStore components](#disable-onestore-components) | Disables orphan OneStore (Korean GAA SDK) components: the IAP proxy activity and sign-in activity. Inert without OneStore installed. |  |
| [Disable RuStore components](#disable-rustore-components) | Disables orphan RuStore SDK components: the payment activity, pay and imaging content providers, and the metrics event job service. Inert without RuStore installed. |  |
| [Disable Samsung IAP components](#disable-samsung-iap-components) | Disables orphan Samsung In-App Purchase SDK components: the dialog, package-check, account, payment and subscription-plan activities. Strips the Samsung IAP billing permission. Inert without Samsung Galaxy Store installed. |  |
| [Disable Xiaomi store components](#disable-xiaomi-store-components) | Disables orphan Xiaomi billing client and Common IAP components: the proxy billing activities and payment/extra web activities they register. Inert without Xiaomi's store and payment service installed. |  |
| [Disable ad SDK calls](#disable-ad-sdk-calls) | No-ops common ad SDK load/show/init/fetch methods in bundled ad packages. |  |
| [Disable cleartext traffic](#disable-cleartext-traffic) | Sets android:usesCleartextTraffic=false on the application element, forcing all traffic to HTTPS. Plaintext HTTP can be read and injected by anyone on the network. Apps that legitimately use plain HTTP (local device dashboards, IoT) lose those requests. |  |
| [Disable legacy external storage](#disable-legacy-external-storage) | Sets android:requestLegacyExternalStorage=false on the application element, confining the app to scoped storage — its own directories and media collections instead of raw access to every app's shared files. Only affects apps targeting Android 10; file-manager-style apps lose raw shared-storage paths. |  |
| [Disable lock-screen display](#disable-lock-screen-display) | Disables android:showWhenLocked and android:turnScreenOn on every activity that declared them, so no app screen can display over the lock screen and no activity can wake the screen by itself. Stalkerware and scareware use lock-screen overlays to fake unlock prompts and push alerts while the phone is locked; alarm, incoming-call and 2FA screens lose their over-lock UX. Both removals are on by default — open the patch options (gear) to deselect one. | • Disable show-when-locked<br>• Disable turn-screen-on |
| [IPC guard (cross-app exfil barrier)](#ipc-guard-cross-app-exfil-barrier) | A cross-app IPC barrier for suspect apps: outbound intents to other packages are dropped (own-package traffic passes through), ContentResolver access to foreign authorities is blocked, runtime receivers become not-exported, and notification PendingIntents aimed at other apps become no-ops. The manifest sweep (providers unexported, <queries> stripped, HCE removed) always applies. Breaks share sheets, link opens, pickers and cross-app logins unless the destination package is in the allowlist. All guards are on by default — open the patch options (gear) to deselect or allowlist. | • Guard outbound intents<br>• Guard content providers<br>• Force receivers not-exported<br>• Guard PendingIntents<br>• Allowed packages |
| [Override auto backup](#override-auto-backup) | Forces android:allowBackup on the application element in the chosen direction. Disable excludes the app's private data — databases, preferences, tokens — from cloud auto backup, adb backup and device transfers (the privacy direction). Enable restores the platform default for apps that opted out, overriding the developer's protection of secrets and license tokens. Pick the direction in the patch options (gear); default is Disable. | • Backup override |
| [Prevent task hijacking (StrandHogg)](#prevent-task-hijacking-strandhogg) | Hardens the app against task-hijacking overlays (StrandHogg): clears the cloneable task affinity, disables activity re-parenting and pins launcher activities to singleTask so no other app can interleave phishing UI in front of this one. Recents behavior can change (relaunch resumes instead of restarting; splash launchers may act differently). All mitigations are on by default — open the patch options (gear) to deselect. | • Clear task affinity<br>• Disable task re-parenting<br>• Force launcher singleTask |
| [Remove GMS permissions](#remove-gms-permissions) | Strips the Google Play-defined permission declarations from the manifest: advertising ID, Play Billing, legacy license checks, install referrer attribution, push (C2DM), legacy account auth scopes, legacy activity recognition and Google Services Framework sync. Inert on devices without Google Play; on Play devices it kills the matching capability. All removals are on by default — open the patch options (gear) to deselect specific ones. | • Remove AD_ID (Google Play Services)<br>• Remove BILLING (Google Play)<br>• Remove CHECK_LICENSE (Play Licensing)<br>• Remove BIND_GET_INSTALL_REFERRER_SERVICE<br>• Remove C2DM permissions (push)<br>• Remove C2D_MESSAGE (app-scoped push lock)<br>• Remove READ_GSERVICES (Google Services Framework)<br>• Remove legacy ACTIVITY_RECOGNITION (Google Play Services)<br>• Remove GOOGLE_AUTH scopes (legacy account tokens) |
| [Remove privacy permissions](#remove-privacy-permissions) | Strips privacy-invasive permission declarations from the manifest across 19 categories — camera, microphone, location, contacts, calendar, SMS, phone, nearby radios, network state, internet, sensors, media, notifications, special access, surveillance declarations, persistence, health, advertising and screenshot detection. Install-time permissions are never granted; runtime permissions are auto-denied with no dialog. All categories are on by default — open the patch options (gear) to deselect the ones the app legitimately needs. | • Remove camera access<br>• Remove microphone access<br>• Remove location access<br>• Remove contacts & accounts access<br>• Remove calendar access<br>• Remove SMS & MMS access<br>• Remove phone & call access<br>• Remove nearby radios (Bluetooth, UWB, NFC)<br>• Remove network & Wi-Fi state access<br>• Remove internet access<br>• Remove sensor & motion access<br>• Remove media & storage access<br>• Remove notification posting<br>• Remove special-access grant gates<br>• Remove surveillance service declarations (BIND_*)<br>• Remove background persistence & wakeups<br>• Remove Health Connect access<br>• Remove ad & Privacy Sandbox permissions<br>• Remove screenshot detection |
| [Silence app logging](#silence-app-logging) | Nops every logging call site — android.util.Log writers, System.out/err print and println, and Throwable.printStackTrace — so tokens, credentials and PII never reach the logcat ring buffer, where adb, bug reports and forensic capture read them. Bundled wrappers like Timber funnel through android.util.Log and are silenced transitively. Log-based debug features and in-app log viewers go empty — that is the point. | • Silence android.util.Log<br>• Silence System.out / System.err<br>• Silence printStackTrace |

</details>

<!-- PATCHES_END -->

## 📜 License

o0b Patches are licensed under the GNU General Public License v3.0
