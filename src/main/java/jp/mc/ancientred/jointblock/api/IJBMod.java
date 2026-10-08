package jp.mc.ancientred.jointblock.api;

import java.util.Map;

public interface IJBMod {

    IJBActionFactory getActionFactory();

    IJBBlueprintFactory getBlueprintFactory();

    IJBSoundManager getSoundManager();

    Map<String, IJBAddon.IJBAddonLimitKey> getLimitKeyMap();
}
