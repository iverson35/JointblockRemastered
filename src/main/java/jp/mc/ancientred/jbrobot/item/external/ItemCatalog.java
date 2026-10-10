package jp.mc.ancientred.jbrobot.item.external;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The {@code jbrobot_catalog} item: opens the model catalog GUI.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ItemCatalog}. 1.20.1 deviations: the
 * 1.12.2 {@code player.openGui(mod, guiModelCatalogId, world, x, y, z)} call became
 * {@link JBModContainer#proxy}{@code .openGui(int, Player, Level, BlockPos)} (which drives the
 * catalog menu through {@code JBGuiOpener}); the {@code getCurrentEquippedItem} helper,
 * {@code getInventory()} and {@code NBTTagCompound} map to their ported counterparts. The 1.12 GUI
 * id {@code guiModelCatalogId} (5596) is kept as {@link #GUI_MODEL_CATALOG_ID}. A {@code useOn}
 * override was added so that both interactions open the catalog, as requested for the port.
 */
public class ItemCatalog extends Item {
    /** 1.12.2 {@code JBRobotMODContainer.guiModelCatalogId}. */
    public static final int GUI_MODEL_CATALOG_ID = 5596;

    public static final String NBT_TAG_KEY_PACKAGE_PAGE_NUM = "jb:pkgpn";
    public static final String NBT_TAG_KEY_MODEL_ROW_START_IDX = "jb:pidx";

    public ItemCatalog() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
        if (CompatHelper.isStackEmptyOrNull(stack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
        }

        if (!world.isClientSide) {
            JBModContainer.proxy.openGui(GUI_MODEL_CATALOG_ID, player, world, BlockPos.ZERO);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || CompatHelper.isStackEmptyOrNull(context.getItemInHand())) {
            return InteractionResult.PASS;
        }

        if (!context.getLevel().isClientSide) {
            JBModContainer.proxy.openGui(GUI_MODEL_CATALOG_ID, player, context.getLevel(), context.getClickedPos());
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    public static CompoundTag getPlayerCurrentHoldingCatalogNBT(Player player) {
        if (player == null) {
            return null;
        }

        ItemStack itemStack = CompatHelper.getCurrentEquippedItem(player);
        return CompatHelper.isStackEmptyOrNull(itemStack) ? null : itemStack.getTag();
    }

    public static boolean doesPlayerHavaCatalog(Player entityPlayer) {
        if (entityPlayer != null) {
            Inventory invPlayer = entityPlayer.getInventory();

            for (int i = 0; i < 9; ++i) {
                if (isStackCatalog(invPlayer.getItem(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isStackCatalog(ItemStack itemStack) {
        return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemCatalog;
    }
}
