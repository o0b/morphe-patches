package app.template.patches.wavelet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WAVELET_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Verified against 26.05 bytecode (versionCode 260508, recon 2026-10-08):
// - Patch A: the flow's FALSE sget-object is immediately followed by the
//   Lx7/k;->b factory and the Lr5/k;->f iput (4 contiguous instructions).
// - Patch B: a single Boolean.valueOf + move-result-object feeds the single
//   Lx7/v0;->i compareAndSet; the boxed result register is forced to TRUE.
// - No Pairirp, no signature self-check, no integrity checks, no ads.
// - Fingerprints pin by signature (like the meteoblue 3.1.4 patch); the
//   bodies below re-verify layout and fail loud — re-run the recon probes
//   if tokens move on a rebuild of the same version.

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

        val flowPutIndex = initMethod.instructions
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
        val falseSget = initMethod.instructions.elementAt(flowPutIndex - 3)
        val flowCreate = initMethod.instructions.elementAt(flowPutIndex - 2)
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
        initMethod.addInstructions(
            flowPutIndex - 2,
            "sget-object v$stateReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )

        // ── Patch B: Lr5/k;.e — always report a verified purchase ──────────
        val processMethod = PurchaseProcessingFingerprint.method

        val compareAndSetIndex = processMethod.instructions
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
        //   [compareAndSetIndex-2] invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
        //   [compareAndSetIndex-1] move-result-object v6
        //   [compareAndSetIndex]   invoke-virtual {v2, v4, v6}, Lx7/v0;->i(Object;Object)Z
        val valueOf = processMethod.instructions.elementAt(compareAndSetIndex - 2)
        val boxResult = processMethod.instructions.elementAt(compareAndSetIndex - 1)
        if (valueOf.opcode != Opcode.INVOKE_STATIC ||
            valueOf.methodRef()?.let { it.definingClass == "Ljava/lang/Boolean;" && it.name == "valueOf" } != true ||
            boxResult.opcode != Opcode.MOVE_RESULT_OBJECT
        ) throw PatchException(
            "Wavelet 26.05: unexpected layout before Lx7/v0;->i — expected Boolean.valueOf + " +
                "move-result-object. Tokens moved; re-run the recon probe."
        )

        // Overwrite the boxed result with TRUE between the move-result-object
        // and the compareAndSet — every path through the coroutine converges
        // here, so the flow can never be flipped back to false.
        val boxReg = (boxResult as OneRegisterInstruction).registerA
        processMethod.addInstructions(
            compareAndSetIndex,
            "sget-object v$boxReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )
    }
}
