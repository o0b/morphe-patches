package app.template.patches.octopilauncher.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Anchor: billing coroutine located by the "pro_snack" SKU string ────────
// The SKU is the real Google Play product ID — stable across releases, unlike
// R8-reshuffled class names. Single string filter is sufficient and stable.
// 1.88 resolved to ln2.v(); 1.92 resolves to Lhq2;->v — same shape, renamed class.
// Shape-unique: only this method is PUBLIC FINAL (Object)Object containing
// the string (verified against 1.92 bytecode, recon 2026-10-08).

internal object ProSnackCoroutineFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("pro_snack"),
    ),
)
