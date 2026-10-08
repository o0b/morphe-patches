# o0b Patches

Morphe patches 

## ❓ About

https://morphe-patches.software/

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=o0b/morphe-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.4.0](https://github.com/o0b/morphe-patches/releases/tag/v1.4.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;7 patches total
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

<!-- PATCHES_END -->

## 📜 License

o0b Patches are licensed under the GNU General Public License v3.0
