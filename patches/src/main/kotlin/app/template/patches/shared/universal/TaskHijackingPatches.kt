package app.template.patches.shared.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Prevent task hijacking" master patch.
 *
 * Modeled on kveld9's Universal Offline Mode pattern (kveld9/kveld-morphe-patches,
 * GPL-3.0): one master patch whose gear toggles select the mitigations — the
 * OWASP MASTG StrandHogg set, all on by default.
 *
 * StrandHogg (Promon, 2019/2020; StrandHogg 2.0 = CVE-2020-0102): a malicious
 * app already on the device declares the victim's task affinity and uses
 * re-parenting to interleave its own phishing UI when the victim's task comes
 * to the front. These toggles harden the patched app as the VICTIM:
 *
 *   1. Clear task affinity — android:taskAffinity="" leaves an attacker
 *      nothing to clone (the default affinity is the package name).
 *   2. Disable re-parenting — allowTaskReparenting=false closes the door
 *      StrandHogg 2.0 used to slide in front.
 *   3. Force launcher singleTask — pins the root activity so nothing else
 *      can share its task.
 *
 * Honest scope: per-activity affinities an app declared itself survive the
 * application-level clear, and only launcher activities get the singleTask
 * pin. CVE-2020-0102 itself was fixed in Android 11 — on Android 11+
 * (GrapheneOS included) this is residual hygiene for OEM variance and generic
 * task hijacking, not a live-CVE fix.
 */
@Suppress("unused")
val preventTaskHijackingPatch = resourcePatch(
    name = "Prevent task hijacking (StrandHogg)",
    description = "Hardens the app against task-hijacking overlays (StrandHogg): clears " +
        "the cloneable task affinity, disables activity re-parenting and pins launcher " +
        "activities to singleTask so no other app can interleave phishing UI in front of " +
        "this one. Recents behavior can change (relaunch resumes instead of restarting; " +
        "splash launchers may act differently). All mitigations are on by default — open " +
        "the patch options (gear) to deselect.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripTaskAffinity by booleanOption(
        key = "stripTaskAffinity",
        default = true,
        title = "Clear task affinity",
        description = "Sets android:taskAffinity to empty on the application element — " +
            "nothing left for a malicious app to clone.",
    )

    val stripTaskReparenting by booleanOption(
        key = "stripTaskReparenting",
        default = true,
        title = "Disable task re-parenting",
        description = "Sets allowTaskReparenting=false on the application element and on " +
            "every activity that declared it — the mechanism StrandHogg 2.0 used.",
    )

    val forceSingleTaskLauncher by booleanOption(
        key = "forceSingleTaskLauncher",
        default = true,
        title = "Force launcher singleTask",
        description = "Sets launchMode=singleTask on launcher activities, pinning the " +
            "app's root so no other activity can share its task. The highest-impact " +
            "toggle of the three on app behavior.",
    )

    execute {
        var clearedAffinity = false
        var reparentingDisabled = 0
        var launchersPinned = 0

        document("AndroidManifest.xml").use { doc ->
            val application = doc.getElementsByTagName("application").item(0) as? Element

            if (stripTaskAffinity ?: true) {
                application?.setAttribute("android:taskAffinity", "")
                clearedAffinity = application != null
            }

            if (stripTaskReparenting ?: true) {
                application?.setAttribute("android:allowTaskReparenting", "false")
                val activities = doc.getElementsByTagName("activity")
                for (i in 0 until activities.length) {
                    val activity = activities.item(i) as? Element ?: continue
                    if (activity.hasAttribute("android:allowTaskReparenting")) {
                        activity.setAttribute("android:allowTaskReparenting", "false")
                        reparentingDisabled++
                    }
                }
            }

            if (forceSingleTaskLauncher ?: true) {
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

                    if (isLauncher) {
                        activity.setAttribute("android:launchMode", "singleTask")
                        launchersPinned++
                    }
                }
            }
        }

        println(
            "[Prevent task hijacking] affinityCleared=$clearedAffinity " +
                "reparentingDisabled=$reparentingDisabled launchersPinned=$launchersPinned.",
        )
    }
}
