package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Disable Android debugging" patch.
 *
 * Sets android:debuggable="false" on <application> — the mirror of the
 * vomw/Doom "Enable Android debugging" patch, which sets it true.
 *
 * A debuggable build exposes the app to anyone with ADB access: JDWP
 * debuggers attach at will, and `run-as` shell access reads the app's
 * private data directory — databases, preferences, tokens. Shipping
 * debuggable=true is a standard release-checklist data-leak flag, most
 * often found in sideloaded and modded APKs. Absent means false already,
 * so this is a no-op for apps that never declared it — pure hardening.
 */
@Suppress("unused")
val disableDebuggablePatch = resourcePatch(
    name = "Disable Android debugging",
    description = "Sets android:debuggable=false on the application element, blocking JDWP " +
        "debugger attach and `run-as` access to the app's private data over ADB. Apps that " +
        "rely on debug-only behavior will lose it.",
    default = false, // universal patches must be default false, the patcher warns otherwise
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:debuggable", "false")
        }
    }
}
