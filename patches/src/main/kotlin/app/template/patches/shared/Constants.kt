package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val OCTOPILAUNCHER_COMPATIBILITY = Compatibility(
        name = "Octopi Launcher",
        packageName = "com.otp.octopilauncher",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6600,
        targets = listOf(AppTarget(version = "1.92", versionCode = 2309))
    )
}
