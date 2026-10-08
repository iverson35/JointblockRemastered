package jp.mc.ancientred.jointblock;

import com.mojang.logging.LogUtils;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * JointBlock Remastered - Forge 1.20.1 port of JointBlock 0.8.4 (MC 1.12.2, by ARUBE).
 *
 * <p>Migration scaffold: registries, entities, GUI and network are ported module by
 * module from {@code referance/decompiled/Create-JointBlock}. See MIGRATION_PLAN.md.
 */
@Mod(JointBlockRemastered.MOD_ID)
public class JointBlockRemastered {

    public static final String MOD_ID = "jointblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JointBlockRemastered() {
        JBConfig.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigLoaded);
    }

    private void onConfigLoaded(ModConfigEvent event) {
        ModConfig cfg = event.getConfig();
        if (cfg.getModId().equals(MOD_ID)) {
            JBConfig.bake();
        }
    }
}
