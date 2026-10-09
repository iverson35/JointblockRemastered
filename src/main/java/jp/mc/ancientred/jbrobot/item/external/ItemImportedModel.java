package jp.mc.ancientred.jbrobot.item.external;

import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * The model item of a model pack: one item whose damage value is the pack model id.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ItemImportedModel}. 1.20.1 deviations:
 * {@code setHasSubtypes(true)}/{@code setMaxDamage(0)} collapse into
 * {@code Item.Properties#stacksTo(1)} (subtypes are damage values); {@code getItemDamage()} is
 * {@link ItemStack#getDamageValue()}; the 1.12.2 creative-tab enumeration ({@code func_150895_a}) is
 * not an item hook in 1.20.1, so the tab fills pack metas from
 * {@link ExtraPackage#getModelMetas()}; {@code I18n.format} became
 * {@link CompatHelper#formatLang(String)} and the display name is built from it.
 */
public class ItemImportedModel extends Item implements IJBModelItem, IJBAddon.IJBAddonHolder {
    private final ExtraPackage modelPackage;

    public ItemImportedModel(ExtraPackage argModelPackage) {
        super(new Item.Properties().stacksTo(1));
        this.modelPackage = argModelPackage;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(CompatHelper.formatLang(this.modelPackage.getModelNameFromId(stack.getDamageValue())));
    }

    @Override
    public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
        return this.modelPackage.getActionFromItemDamage(modelMeta);
    }

    @Override
    public IJBAddon getIJBAddon(int modelMeta) {
        return this.modelPackage.getModelAddonFromItemDamage(modelMeta);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        if (this.getIJBActionHandler(itemStack.getDamageValue()) != null) {
            list.add(Component.literal(CompatHelper.formatLang("jbrobot.info.hasaction")));
        }

        if (this.getIJBAddon(itemStack.getDamageValue()) != null) {
            list.add(Component.literal(CompatHelper.formatLang("jbrobot.info.hasaddon")));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
        return this.modelPackage.getModelFromItemDamage(modelMeta);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public double getRenderExpand(int modelMeta) {
        return this.modelPackage.getRenderExpandFromModelMeta(modelMeta);
    }
}
