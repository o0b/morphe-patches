package app.template.patches.wavelet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WAVELET_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Wavelet 26.05 Pro Patch — merged design.
//
// Ours (source-side forcing, register-agnostic, fail-loud, no deps) +
// Epxec's (rename-resilient developer-string anchoring):
//   A) <init>(Context): the purchase flow is built from Boolean.FALSE via
//      a static factory; overwrite that register with TRUE immediately
//      before the factory call → the flow starts purchased, and every
//      consumer behaves as a genuine purchase would (isPurchased gate
//      opens, allowPurchases = session && !true hides the buy flow,
//      the restore coroutine early-outs — no startup billing attempt).
//   B) e(): force the published verdict TRUE between Boolean.valueOf and
//      the StateFlow compareAndSet, so a later billing event (empty list,
//      sandboxed-Play restore) can never flip the flow back to false.
// No obfuscated names are pinned: the fingerprint anchors on the dev's
// verification strings; both injection sites are located by framework-
// stable shape (Boolean.FALSE / Boolean.valueOf + opcode windows).
// Every step asserts and fails loud; version pinned via
// WAVELET_COMPATIBILITY — the only artifact that rotates per release.
//
// Verified against 26.05 bytecode (recon 2026-10-08):
// - flow init:  sget-object FALSE → invoke-static → move-result-object → iput-object
// - verdict:    invoke-static Boolean.valueOf(Z) → move-result-object →
//               invoke-virtual (Object;Object)Z
// - No Pairirp, no signature self-check, no integrity checks, no ads.

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
        val processor = PurchaseProcessingFingerprint.method
        val managerClass = mutableClassDefBy(processor.definingClass)

        // ── Patch A: <init>(Context) — start the purchase state flow TRUE ──
        val ctorDef = managerClass.methods
            .singleOrNull {
                it.name == "<init>" &&
                    it.parameterTypes.singleOrNull()?.toString() == "Landroid/content/Context;"
            }
            ?: throw PatchException(
                "Wavelet 26.05: PurchaseManager <init>(Landroid/content/Context;) not found. " +
                    "Class layout changed; re-run the recon probe."
            )
        val ctor = managerClass.findMutableMethodOf(ctorDef)
        val ctorInsns = ctor.instructions.toList()

        // MutableStateFlow(false) init shape (framework-stable, no names):
        //   [i]   sget-object vN, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
        //   [i+1] invoke-static {vN}, <MutableStateFlow factory>
        //   [i+2] move-result-object vN
        //   [i+3] iput-object vN, vP, <purchase flow field>
        val flowInitSites = ctorInsns.indices.filter { i ->
            i + 3 < ctorInsns.size &&
                ctorInsns[i].opcode == Opcode.SGET_OBJECT &&
                ctorInsns[i].fieldRef()?.let {
                    it.definingClass == "Ljava/lang/Boolean;" && it.name == "FALSE"
                } == true &&
                ctorInsns[i + 1].opcode == Opcode.INVOKE_STATIC &&
                ctorInsns[i + 2].opcode == Opcode.MOVE_RESULT_OBJECT &&
                ctorInsns[i + 3].opcode == Opcode.IPUT_OBJECT
        }
        val flowInitIndex = flowInitSites.singleOrNull()
            ?: throw PatchException(
                "Wavelet 26.05: FALSE-initialized state flow not found uniquely in the " +
                    "PurchaseManager constructor (${flowInitSites.size} candidate sites) — " +
                    "expected sget-object FALSE + invoke-static + move-result-object + " +
                    "iput-object. Layout changed; re-run the recon probe."
            )

        // Type consistency: the iput field's type must equal the factory's
        // return type (both are the flow type — stable under R8 renames).
        val flowType = ctorInsns[flowInitIndex + 1].methodRef()?.returnType
        val fieldType = ctorInsns[flowInitIndex + 3].fieldRef()?.type
        if (flowType.isNullOrEmpty() || fieldType != flowType) throw PatchException(
            "Wavelet 26.05: constructor flow-init site does not feed an iput of the " +
                "factory's return type. Layout changed; re-run the recon probe."
        )

        // Overwrite the register with TRUE immediately before the factory call;
        // the original FALSE sget-object becomes dead code.
        val stateReg = (ctorInsns[flowInitIndex] as OneRegisterInstruction).registerA
        ctor.addInstructions(
            flowInitIndex + 1,
            "sget-object v$stateReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )

        // ── Patch B: the processor — always report a verified purchase ────
        val procInsns = processor.instructions.toList()

        // Verdict publish shape (framework-stable, no names):
        //   [j]   invoke-static {vN}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
        //   [j+1] move-result-object vM
        //   [j+2] invoke-virtual {..}, <StateFlow>.compareAndSet(Object;Object)Z
        val verdictSites = procInsns.indices.filter { j ->
            j + 2 < procInsns.size &&
                procInsns[j].opcode == Opcode.INVOKE_STATIC &&
                procInsns[j].methodRef()?.let {
                    it.definingClass == "Ljava/lang/Boolean;" &&
                        it.name == "valueOf" &&
                        it.parameterTypes.singleOrNull()?.toString() == "Z" &&
                        it.returnType == "Ljava/lang/Boolean;"
                } == true &&
                procInsns[j + 1].opcode == Opcode.MOVE_RESULT_OBJECT &&
                procInsns[j + 2].opcode == Opcode.INVOKE_VIRTUAL &&
                procInsns[j + 2].methodRef()?.let {
                    it.returnType == "Z" && it.parameterTypes.size == 2
                } == true
        }
        val verdictIndex = verdictSites.singleOrNull()
            ?: throw PatchException(
                "Wavelet 26.05: verdict publish (Boolean.valueOf → compareAndSet) not found " +
                    "uniquely in the purchase processor (${verdictSites.size} candidate sites). " +
                    "Layout changed; re-run the recon probe."
            )

        // Overwrite the boxed result with TRUE between the move-result-object
        // and the compareAndSet — every path through the coroutine converges
        // here, so the flow can never be flipped back to false.
        val boxReg = (procInsns[verdictIndex + 1] as OneRegisterInstruction).registerA
        processor.addInstructions(
            verdictIndex + 2,
            "sget-object v$boxReg, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )
    }
}
