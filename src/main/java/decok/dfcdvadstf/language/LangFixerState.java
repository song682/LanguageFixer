package decok.dfcdvadstf.language;

import net.minecraft.client.resources.Language;

/**
 * Holds the language the user just clicked in the list, waiting for the "Done"
 * button to actually apply it. Kept static on purpose so the outer-class mixin
 * and the inner {@code GuiLanguage$List} mixin can share it without fighting
 * with the synthetic {@code this$0} reference.
 */
public final class LangFixerState {

    /** Language picked in the list but not applied yet. {@code null} means "no change". */
    public static Language pendingLanguage;

    private LangFixerState() {
    }

    public static void clear() {
        pendingLanguage = null;
    }
}
