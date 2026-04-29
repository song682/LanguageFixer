package decok.dfcdvadstf.language.mixin;

import decok.dfcdvadstf.language.LangFixerState;
import net.minecraft.client.resources.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Hijacks the list row click so it no longer triggers a resource reload.
 * The click just remembers which language was picked; the outer
 * {@link MixinGuiLanguage} will commit it when "Done" is pressed.
 *
 * <p>Also tweaks {@code isSelected} so the highlight follows the pending
 * choice — otherwise the tick would still sit on the current in-use language
 * and players would have no idea their click did anything.
 */
@Mixin(targets = "net.minecraft.client.gui.GuiLanguage$List")
@SuppressWarnings("rawtypes")
public abstract class MixinGuiLanguageList {

    // Raw types on purpose — vanilla declares these as raw collections.
    @Shadow(aliases = {"field_148176_l"}) private List field_148176_l;
    @Shadow(aliases = {"field_148177_m"}) private Map field_148177_m;

    /**
     * Replace the vanilla behaviour entirely — stash the selection and bail.
     */
    @Inject(method = "elementClicked", at = @At("HEAD"), cancellable = true)
    private void langfixer$stageSelection(int index, boolean doubleClick, int mouseX, int mouseY, CallbackInfo ci) {
        Object key = this.field_148176_l.get(index);
        Language picked = (Language) this.field_148177_m.get(key);
        LangFixerState.pendingLanguage = picked;
        ci.cancel();
    }

    /**
     * Show the highlight on the row the player just clicked, falling back to
     * the current language when nothing is staged.
     */
    @Inject(method = "isSelected", at = @At("HEAD"), cancellable = true)
    private void langfixer$selectPending(int index, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Language pending = LangFixerState.pendingLanguage;
        if (pending == null) {
            return;
        }
        Object code = this.field_148176_l.get(index);
        cir.setReturnValue(pending.getLanguageCode().equals(code));
    }
}
