package decok.dfcdvadstf.language.mixin;

import decok.dfcdvadstf.language.LangFixerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Defers the actual language switch until the player presses "Done".
 * <p>
 * Vanilla would hot-swap the language the moment a row got clicked — which is
 * heavy (reloads all resources). With this mixin, clicks only stage a pending
 * choice, and the real work happens once button id 6 ("Done") fires.
 */
@Mixin(GuiLanguage.class)
public abstract class MixinGuiLanguage extends GuiScreen {

    @Shadow private GameSettings field_146451_g;
    @Shadow private LanguageManager field_146454_h;
    @Shadow private GuiOptionButton field_146452_r;
    @Shadow private GuiOptionButton field_146455_i;

    /**
     * Apply the pending language right before the Done button closes the screen.
     * Any other button (unicode font toggle, list entries routed via default)
     * keeps its original behaviour untouched.
     */
    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void langfixer$applyOnDone(GuiButton button, CallbackInfo ci) {
        if (!button.enabled || button.id != 6) {
            return;
        }
        Language pending = LangFixerState.pendingLanguage;
        if (pending == null) {
            return;
        }

        // Mirror what vanilla GuiLanguage$List#elementClicked used to do — just delayed.
        this.field_146454_h.setCurrentLanguage(pending);
        this.field_146451_g.language = pending.getLanguageCode();

        Minecraft mc = Minecraft.getMinecraft();
        mc.refreshResources();

        FontRenderer fr = mc.fontRenderer;
        fr.setUnicodeFlag(this.field_146454_h.isCurrentLocaleUnicode() || this.field_146451_g.forceUnicodeFont);
        fr.setBidiFlag(this.field_146454_h.isCurrentLanguageBidirectional());

        this.field_146452_r.displayString = I18n.format("gui.done", new Object[0]);
        this.field_146455_i.displayString = this.field_146451_g.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
        this.field_146451_g.saveOptions();

        LangFixerState.clear();
    }

    /**
     * Drop any half-chosen language if the player backs out of the screen
     * (ESC, Done without picking anything, etc.) so we don't leak state into
     * the next time this GUI opens.
     */
    @Inject(method = "onGuiClosed", at = @At("HEAD"), remap = false, require = 0)
    private void langfixer$clearOnClose(CallbackInfo ci) {
        LangFixerState.clear();
    }
}
