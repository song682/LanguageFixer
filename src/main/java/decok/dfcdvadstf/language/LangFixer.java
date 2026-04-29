package decok.dfcdvadstf.language;

import java.util.logging.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID, name = Tags.NAME, version = Tags.VERSION, useMetadata = true)
public class LangFixer {
    public static final Logger logger = Logger.getLogger(Tags.MODID);
    @Mod.EventHandler  
    public void preInit(FMLPreInitializationEvent event) {
        logger.info("Pre-initializing " + Tags.NAME);
    }
}
