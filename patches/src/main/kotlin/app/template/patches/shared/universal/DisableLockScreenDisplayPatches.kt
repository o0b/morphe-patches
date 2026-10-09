package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal lock-screen display disable patches.
 *
 * android:showWhenLocked and android:turnScreenOn (API 27+, declared on
 * <activity>) let an activity draw over the lock screen and wake the
 * screen by itself — the alarm/call-screen UX, and equally the
 * stalkerware/scareware pattern: fake lock-screen overlays and pushed
 * content while the phone is locked, plus ad screen-wake behavior.
 *
 * Absent means false already, so each patch only flips activities that
 * explicitly declared the attribute.
 *
 * ponytail: this strips the declarative form only — apps can also call
 * setShowWhenLocked()/setTurnScreenOn() or add the window flags at
 * runtime. A bytecode companion walking those invocations is the upgrade
 * path if a runtime offender ever matters.
 */

/**
 * Creates a universal patch that sets an activity-level attribute to false
 * on every activity that explicitly declared it.
 */
private fun disableActivityAttributePatch(
    patchName: String,
    attribute: String,
    why: String,
) = resourcePatch(
    name = patchName,
    description = "Disables $attribute on every activity that declared it. $why",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val activities = doc.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as? Element ?: continue
                if (activity.hasAttribute(attribute)) activity.setAttribute(attribute, "false")
            }
        }
    }
}

@Suppress("unused")
val disableShowWhenLockedPatch = disableActivityAttributePatch(
    patchName = "Disable show-when-locked",
    attribute = "android:showWhenLocked",
    why = "No app screen can display over the lock screen anymore. Stalkerware and scareware " +
        "use lock-screen overlays to fake unlock prompts and push alerts while the phone is " +
        "locked; alarm, incoming-call and 2FA screens lose their over-lock UX.",
)

@Suppress("unused")
val disableTurnScreenOnPatch = disableActivityAttributePatch(
    patchName = "Disable turn-screen-on",
    attribute = "android:turnScreenOn",
    why = "No activity can wake the screen by itself anymore. Ad and spyware screen-wake " +
        "behavior dies with it — and so do alarm and incoming-call screens.",
)
