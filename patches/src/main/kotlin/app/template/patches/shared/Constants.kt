package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {

    val METEOBLUE_COMPATIBILITY = Compatibility(
        name = "meteoblue Weather",
        packageName = "com.meteoblue.droid",
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x0077CC,
        targets = listOf(AppTarget(version = "Cirrus Uncinus 3.1.4", versionCode = 27041))
    )

    val OCTOPILAUNCHER_COMPATIBILITY = Compatibility(
        name = "Octopi Launcher",
        packageName = "com.otp.octopilauncher",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xFF6600,
        targets = listOf(AppTarget(version = "1.92", versionCode = 2309))
    )

    val WAVELET_COMPATIBILITY = Compatibility(
        name = "Wavelet",
        packageName = "com.pittvandewitt.wavelet",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "26.05", versionCode = 260508))
    )
}
