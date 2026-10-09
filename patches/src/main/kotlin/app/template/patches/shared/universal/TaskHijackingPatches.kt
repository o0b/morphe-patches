package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal task-hijacking (StrandHogg) hardening patches.
 *
 * StrandHogg (Promon, 2019/2020; StrandHogg 2.0 = CVE-2020-0102): a
 * malicious app already on the device declares the victim's task affinity
 * and uses re-parenting to interleave its own phishing UI when the
 * victim's task comes to the front — you tap the banking app icon and see
 * the attacker's fake login. These three patches harden the patched app
 * as the VICTIM, the OWASP MASTG mitigation set:
 *
 *   1. Clear task affinity — android:taskAffinity="" leaves an attacker
 *      nothing to clone (the default affinity is the package name).
 *   2. Disable re-parenting — allowTaskReparenting=false closes the door
 *      StrandHogg 2.0 used to slide in front.
 *   3. Force launcher singleTask — pins the root activity so nothing else
 *      can share its task.
 *
 * Honest scope: the per-activity affinities an app declared itself
 * survive the application-level clear, and only launcher activities get
 * the singleTask pin — other exported activities keep their launch modes.
 * CVE-2020-0102 itself was fixed in Android 11; on Android 11+ (GrapheneOS
 * included) classic StrandHogg is already dead at the OS level, so this
 * file is residual-hygiene for OEM variance and generic task hijacking,
 * not a live-CVE fix.
 */

@Suppress("unused")
val clearTaskAffinityPatch = resourcePatch(
    name = "Clear task affinity",
    description = "Sets android:taskAffinity to empty on the application element, so no other " +
        "installed app can clone this app's affinity to interleave phishing UI in front of " +
        "it — the StrandHogg pattern. Document-centric apps relying on custom per-activity " +
        "affinities keep them.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:taskAffinity", "")
        }
    }
}

@Suppress("unused")
val disableTaskReparentingPatch = resourcePatch(
    name = "Disable task re-parenting",
    description = "Sets allowTaskReparenting=false on the application element and on every " +
        "activity that declared it, closing the re-parenting mechanism a malicious task " +
        "uses to slide in front of this app — StrandHogg 2.0's route. Apps that rely on " +
        "cross-task re-parenting lose it.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:allowTaskReparenting", "false")

            val activities = doc.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as? Element ?: continue
                if (activity.hasAttribute("android:allowTaskReparenting")) {
                    activity.setAttribute("android:allowTaskReparenting", "false")
                }
            }
        }
    }
}

@Suppress("unused")
val forceSingleTaskLauncherPatch = resourcePatch(
    name = "Force launcher singleTask",
    description = "Sets launchMode=singleTask on launcher activities, pinning the app's root " +
        "so no other activity can share its task. Recents behavior can change: relaunching " +
        "from the home screen resumes instead of restarting, and splash-screen launchers " +
        "may behave differently.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val activities = doc.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as? Element ?: continue

                var isLauncher = false
                val filters = activity.getElementsByTagName("intent-filter")
                for (j in 0 until filters.length) {
                    val filter = filters.item(j) as? Element ?: continue
                    var hasMain = false
                    var hasLauncher = false
                    val children = filter.childNodes
                    for (k in 0 until children.length) {
                        val child = children.item(k) as? Element ?: continue
                        if (child.tagName == "action" &&
                            child.getAttribute("android:name") == "android.intent.action.MAIN"
                        ) hasMain = true
                        if (child.tagName == "category" &&
                            child.getAttribute("android:name") == "android.intent.category.LAUNCHER"
                        ) hasLauncher = true
                    }
                    if (hasMain && hasLauncher) isLauncher = true
                }

                if (isLauncher) activity.setAttribute("android:launchMode", "singleTask")
            }
        }
    }
}
