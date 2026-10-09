package app.template.patches.shared.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Disable lock-screen display" master patch.
 *
 * Modeled on kveld9's Universal Offline Mode pattern (kveld9/kveld-morphe-patches,
 * GPL-3.0): one master patch whose gear toggles select the attributes. Both
 * toggles default on, so enabling the patch alone disables both attributes —
 * the single DOM pass replaces the previous two separate patches.
 *
 * android:showWhenLocked and android:turnScreenOn (API 27+, declared on
 * <activity>) let an activity draw over the lock screen and wake the screen
 * by itself — the alarm/call-screen UX, and equally the stalkerware/scareware
 * pattern: fake lock-screen overlays and pushed content while the phone is
 * locked, plus ad screen-wake behavior. Absent means false already, so only
 * activities that explicitly declared an attribute are touched.
 *
 * ponytail: this strips the declarative form only — apps can also call
 * setShowWhenLocked()/setTurnScreenOn() or add the window flags at runtime.
 * A bytecode companion walking those invocations is the upgrade path if a
 * runtime offender ever matters.
 */
private const val SHOW_WHEN_LOCKED = "android:showWhenLocked"
private const val TURN_SCREEN_ON = "android:turnScreenOn"

@Suppress("unused")
val disableLockScreenDisplayPatch = resourcePatch(
    name = "Disable lock-screen display",
    description = "Disables android:showWhenLocked and android:turnScreenOn on every " +
        "activity that declared them, so no app screen can display over the lock screen " +
        "and no activity can wake the screen by itself. Stalkerware and scareware use " +
        "lock-screen overlays to fake unlock prompts and push alerts while the phone is " +
        "locked; alarm, incoming-call and 2FA screens lose their over-lock UX. Both " +
        "removals are on by default — open the patch options (gear) to deselect one.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    val stripShowWhenLocked by booleanOption(
        key = "stripShowWhenLocked",
        default = true,
        title = "Disable show-when-locked",
        description = "No app screen can display over the lock screen anymore — the " +
            "lock-screen overlay channel dies with it.",
    )

    val stripTurnScreenOn by booleanOption(
        key = "stripTurnScreenOn",
        default = true,
        title = "Disable turn-screen-on",
        description = "No activity can wake the screen by itself anymore — ad and " +
            "spyware screen-wake behavior goes with it.",
    )

    execute {
        var changed = 0
        document("AndroidManifest.xml").use { doc ->
            val activities = doc.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as? Element ?: continue
                if ((stripShowWhenLocked ?: true) && activity.hasAttribute(SHOW_WHEN_LOCKED)) {
                    activity.setAttribute(SHOW_WHEN_LOCKED, "false")
                    changed++
                }
                if ((stripTurnScreenOn ?: true) && activity.hasAttribute(TURN_SCREEN_ON)) {
                    activity.setAttribute(TURN_SCREEN_ON, "false")
                    changed++
                }
            }
        }

        if (changed == 0) {
            println("[Disable lock-screen display] No showWhenLocked/turnScreenOn declarations found.")
        } else {
            println("[Disable lock-screen display] Neutralized $changed lock-screen attribute declaration(s).")
        }
    }
}
