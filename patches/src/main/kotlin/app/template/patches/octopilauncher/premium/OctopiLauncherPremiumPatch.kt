package app.template.patches.octopilauncher.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OCTOPILAUNCHER_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.getReference
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Verified against 1.92 bytecode (recon 2026-10-08):
// - Fingerprint: unchanged. "pro_snack" + public final (Object)Object still
//   uniquely resolves the billing coroutine — now Lhq2;->v (was ln2.v).
// - isPro ViewModel: Lsv5; (was yq5) — resolved dynamically, never pinned.
// - isPro LiveData: field name "s" UNCHANGED; type Lvu4; (was zo4) — the
//   only token that moved between 1.88 and 1.92.
// - isPro getter: e()Z UNCHANGED — reads Lsv5;->s:Lvu4;, unboxes Boolean.
// - No Pairip, no signature check, no SSL pinning, no root detection.

@Suppress("unused")
val octopiLauncherPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Octopi Launcher Pro by returning true from the isPro LiveData getter, bypassing all pro feature gates without modifying billing or database logic.",
    default = true,
) {
    compatibleWith(OCTOPILAUNCHER_COMPATIBILITY)

    execute {
        val vmType = ProSnackCoroutineFingerprint.method.instructions
            .firstNotNullOfOrNull { instruction ->
                if (instruction.opcode != Opcode.IGET_OBJECT) return@firstNotNullOfOrNull null
                instruction.getReference<FieldReference>()
                    ?.takeIf { ref -> ref.name == "s" && ref.type == "Lvu4;" }
                    ?.definingClass
            }
            ?: throw PatchException(
                "Octopi Launcher 1.92: could not resolve the isPro ViewModel — " +
                    "no IGET_OBJECT of name \"s\" type \"Lvu4;\" in the anchor method. " +
                    "Tokens moved again; re-run the recon probe."
            )

        val vmClass = mutableClassDefBy(vmType)
        val isProMethod = vmClass.methods.singleOrNull { method ->
            method.name == "e" &&
                method.returnType == "Z" &&
                method.parameterTypes.isEmpty()
        }
            ?: throw PatchException(
                "Octopi Launcher 1.92: could not uniquely find e()Z on $vmType — " +
                    "getter renamed or duplicated; re-run the recon probe."
            )

        vmClass.findMutableMethodOf(isProMethod).returnEarly(true)
    }
}
