package app.template.patches.novalauncher.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.NOVA_LAUNCHER_COMPATIBILITY
import app.template.patches.shared.findInstructionIndicesReversed
import app.template.patches.shared.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Nova Launcher 8.1.6 (81006) Prime unlock — probe-derived pins.
//
// • Runtime prime flag: Lzg/t1;->y:Z — 8.1.6's equivalent of the 8.8.8
//   patch's oy/h2.h. Read inline app-wide (sget Lzg/l2;->a + iget-boolean
//   y); there is no central isPrime() gate to returnEarly.
// • Twin flag Lzg/t1;->t:Z ("full license", pref "1" == 512) — forced too.
// • Writers (exhaustive over probe anchor methods): the settings
//   initializer (y ×3, t ×1) and the prime-app package-change handler
//   (y ×2, t ×1) — 7 forced writes total.
// • The license revocation path (Laa/d;->h(I)V writes pref "1"=0 and
//   nova_prime=false on server refusal) is left untouched: persisted
//   values only ever feed branches whose result register we overwrite
//   before each iput-boolean.
//
// Keep this patch + the compatibility pin 8.1.6-only: PRIME_FLAG_FIELDS is
// build-specific and would (correctly) fail loud on any other version.

private val PRIME_FLAG_FIELDS = setOf("Lzg/t1;->y:Z", "Lzg/t1;->t:Z")

private fun forcePrimeWrites(method: MutableMethod, label: String) {
    val putIndices = method.findInstructionIndicesReversed {
        opcode == Opcode.IPUT_BOOLEAN &&
            getReference<FieldReference>()
                ?.let { "${it.definingClass}->${it.name}:${it.type}" } in PRIME_FLAG_FIELDS
    }
    if (putIndices.isEmpty()) {
        throw PatchException("Nova 8.1.6: no iput-boolean on $PRIME_FLAG_FIELDS in $label")
    }

    putIndices.forEach { idx ->
        val valueReg = (method.getInstruction(idx) as TwoRegisterInstruction).registerA
        method.addInstructions(idx, "const/4 v$valueReg, 0x1")
    }
}

@Suppress("unused")
val novaLauncherPremiumPatch = bytecodePatch(
    name = "Unlock Prime",
    description = "Unlocks Nova Launcher Prime on 8.1.6 by forcing the runtime prime flags " +
        "(Lzg/t1;->y / ->t) to true at every write — the settings initializer and the " +
        "Prime-unlocker package-change handler.",
    default = true,
) {
    compatibleWith(NOVA_LAUNCHER_COMPATIBILITY)

    execute {
        forcePrimeWrites(PrimeStateInitFingerprint.method, "PrimeStateInitFingerprint")
        forcePrimeWrites(PrimeAppChangeFingerprint.method, "PrimeAppChangeFingerprint")
    }
}
