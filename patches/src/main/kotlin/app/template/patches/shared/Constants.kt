package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
 // Business Calendar 2 2.55.6 — Play-flavor split bundle (.apks).
 val BUSINESS_CALENDAR_2_COMPATIBILITY = Compatibility(
  name = "Business Calendar 2",
  packageName = "com.appgenix.bizcal",
  apkFileType = ApkFileType.APKS,
  appIconColor = 0xD32F2F,
  targets = listOf(AppTarget(version = "2.55.6", versionCode = 255601))
 )

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

 // Nova Launcher 8.1.6 ships as a plain APK (single dex, no splits).
 // The manifest versionName is literally "81006 (8.1.6)".
 val NOVA_LAUNCHER_COMPATIBILITY = Compatibility(
  name = "Nova Launcher",
  packageName = "com.teslacoilsw.launcher",
  apkFileType = ApkFileType.APK,
  appIconColor = 0xE53935,
  targets = listOf(AppTarget(version = "81006 (8.1.6)", versionCode = 81006))
 )
}
