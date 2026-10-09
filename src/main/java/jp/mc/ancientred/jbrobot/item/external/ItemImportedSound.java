package jp.mc.ancientred.jbrobot.item.external;

import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/**
 * The sound item of a model pack: one item whose damage value is the pack sound item id.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ItemImportedSound}. 1.20.1 deviations:
 * {@code setMaxStackSize(1)}/{@code setHasSubtypes(true)}/{@code setMaxDamage(0)} collapse into
 * {@code Item.Properties#stacksTo(1)}; {@code getItemDamage()} is
 * {@link ItemStack#getDamageValue()}; the creative-tab enumeration is gone (see
 * {@link ItemImportedModel}); {@code I18n.format} became {@link CompatHelper#formatLang(String)}.
 */
public class ItemImportedSound extends Item implements IJBSound.IJBSoundHolder {
    private final ExtraPackage modelPackage;

    public ItemImportedSound(ExtraPackage argModelPackage) {
        super(new Item.Properties().stacksTo(1));
        this.modelPackage = argModelPackage;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(CompatHelper.formatLang(this.modelPackage.getSoundItemNameFromId(stack.getDamageValue())));
    }

    @Override
    public IJBSound getIJBSound(int modelMeta) {
        return this.modelPackage.getSoundFromSoundItemDamage(modelMeta);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }
}
