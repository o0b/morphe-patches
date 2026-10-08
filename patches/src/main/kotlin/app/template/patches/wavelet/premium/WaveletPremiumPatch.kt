package app.template.patches.wavelet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WAVELET_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.VariableRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Verified against 26.05 bytecode (versionCode 260508, recon 2026-10-08):
// - Patch A: the flow's FALSE sget-object is immediately followed by the
//   Lx7/k;->b factory and the Lr5/k;->f iput (4 contiguous instructions).
// - Patch B: a single Boolean.valueOf feeds the single Lx7/v0;->i
//   compareAndSet; its register also survives to the DataStore write.
// - No Pairirp, no signature self-check, no integrity checks, no ads.
// - Tokens (Lr5/k;, Lx7/k;, Lx7/v0;) rotate per release; the layout
//   assertions below fail loud — re-run the recon probes on update.

private fun Instruction.fieldRef(): FieldReference? =
    (this as? ReferenceInstruction)?.reference as? FieldReference

private fun Instruction.methodRef(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

@Suppress("unused")
val waveletPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Wavelet's pro features (Reverberation, Virtualizer, Bass tuner, Equal loudness) by initializing the purchase-verified state flow to true and making the purchase processor always report a verified purchase, which also suppresses the purchase flow.",
    default = true,
) {
    compatibleWith(WAVELET_COMPATIBILITY)

    execute {
        // ── Patch A: Lr5/k;.<init> — start the purchase state flow as TRUE ──
        val initMethod = PurchaseStateInitFingerprint.method
        val mutableInit = mutableClassDefBy(initMethod.definingClass).findMutableMethodOf(initMethod)

        val flowPutIndex = mutableInit.instructions
            .mapIndexedNotNull { index, instruction ->
                if (instruction.opcode == Opcode.IPUT_OBJECT &&
                    instruction.fieldRef()?.let { it.definingClass == "Lr5/k;" && it.name == "f" } == true
                ) index else null
            }
            .firstOrNull()
            ?: throw PatchException(
                "Wavelet 26.05: no iput-object on Lr5/k;->f in the purchase state initializer."
            )

        if (flowPutIndex < 3) throw PatchException(
            "Wavelet 26.05: unexpected layout around Lr5/k;->f (flowPutIndex=$flowPutIndex)."
        )

        // Expected layout (recon 2026-10-08):
        //   [flowPutIndex-3] sget-object vN, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
        //   [flowPutIndex-2] invoke-static {vN}, Lx7/k;->b(Ljava/lang/Object;)Lx7/v0;
        //   [flowPutIndex-1] move-result-object vN
        //   [flowPutIndex]   iput-object vN, vP, Lr5/k;->f:Lx7/v0;
        val falseSget = mutableInit.instructions.elementAt(flowPutIndex - 3)
        val flowCreate = mutableInit.instructions.elementAt(flowPutIndex - 2)
        if (falseSget.opcode != Opcode.SGET_OBJECT ||
            falseSget.fieldRef()?.let { it.definingClass == "Ljava/lang/Boolean;" && it.name == "FALSE" } != true ||
            flowCreate.methodRef()?.let { it.definingClass == "Lx7/k;" && it.name == "b" } != true
        ) throw PatchException(
            "Wavelet 26.05: unexpected layout around Lr5/k;->f init — expected FALSE sget-object " +
                "followed by the Lx7/k;->b StateFlow factory. Tokens moved; re-run the recon probe."
        )

        // Overwrite the register with TRUE immediately before the factory call;
        // the original FALSE sget-object becomes dead code.
        val stateReg = (falseSget as OneRegisterInstruction).registerA
        mutableInit.addInstructions(
            flowPutIndex - 2,
            "sget-object v$stateReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )

        // ── Patch B: Lr5/k;.e — always report a verified purchase ──────────
        val processMethod = PurchaseProcessingFingerprint.method
        val mutableProcess = mutableClassDefBy(processMethod.definingClass).findMutableMethodOf(processMethod)

        val compareAndSetIndex = mutableProcess.instructions
            .mapIndexedNotNull { index, instruction ->
                if (instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                    instruction.methodRef()?.let { it.definingClass == "Lx7/v0;" && it.name == "i" } == true
                ) index else null
            }
            .firstOrNull()
            ?: throw PatchException(
                "Wavelet 26.05: no Lx7/v0;->i compareAndSet in the purchase processor."
            )

        if (compareAndSetIndex < 2) throw PatchException(
            "Wavelet 26.05: unexpected layout before Lx7/v0;->i (compareAndSetIndex=$compareAndSetIndex)."
        )

        // Expected layout (recon 2026-10-08):
        //   [compareAndSetIndex-2] invoke-static {vN}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
        //   [compareAndSetIndex-1] move-result-object vM
        //   [compareAndSetIndex]   invoke-virtual {..}, Lx7/v0;->i(Object;Object)Z
        val valueOf = mutableProcess.instructions.elementAt(compareAndSetIndex - 2)
        if (valueOf.opcode != Opcode.INVOKE_STATIC ||
            valueOf.methodRef()?.let { it.definingClass == "Ljava/lang/Boolean;" && it.name == "valueOf" } != true
        ) throw PatchException(
            "Wavelet 26.05: unexpected layout before Lx7/v0;->i — expected Boolean.valueOf. " +
                "Tokens moved; re-run the recon probe."
        )

        // vN feeds BOTH the StateFlow compareAndSet and the DataStore
        // "purchased" write (the register survives the suspension via Lr5/i;->h).
        val verdictReg = (valueOf as VariableRegisterInstruction).getRegister(0)
        mutableProcess.addInstructions(compareAndSetIndex - 2, "const/4 v$verdictReg, 0x1")
    }
}
