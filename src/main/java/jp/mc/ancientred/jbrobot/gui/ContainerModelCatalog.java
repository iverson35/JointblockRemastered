package jp.mc.ancientred.jbrobot.gui;

import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jbrobot.item.external.ItemCatalog;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantPackage;
import jp.mc.ancientred.jbrobot.item.merchant.JBMerchantType;
import jp.mc.ancientred.jbrobot.registry.JBRMenuTypes;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code ContainerModelCatalog}: one merchant result slot plus the player hotbar.
 *
 * <p>1.20.1 substitutions: {@code Container} -> {@link AbstractContainerMenu} with a registered
 * {@code MenuType}; {@code InventoryCraftResult} -> {@link ResultContainer} (the 1.12.2 result
 * inventory, a {@link Container} of size 1); {@code InventoryPlayer} -> {@link Inventory};
 * {@code World} -> {@link Level}; {@code ItemStack#getItemDamage}/{@code func_77970_a} etc. use the
 * 1.20.1 accessors.  The container is player-bound (no block), so the menu factory carries no extra
 * data.
 *
 * <p>1.12.2 {@code func_75134_a} (drop the leftover result on close) is {@link #removed(Player)}.
 */
public class ContainerModelCatalog extends AbstractContainerMenu {
   public static final int TAB_MODEL_ID = 500;
   public static final int TAB_BLUEPRINT_ID = 600;
   public static final int TAB_ADDONS_ID = 700;
   public Player entityplayer;
   private Level world;
   private int posX;
   private int posY;
   private int posZ;
   private final Container invResult = new ResultContainer();

   public ContainerModelCatalog(int containerId, Inventory playerIn) {
      super(JBRMenuTypes.MODEL_CATALOG, containerId);
      this.entityplayer = playerIn.player;
      this.world = playerIn.player.level();
      this.addSlot(new SlotJBMerchantResult(this.invResult, 0, 9, 165));
      int i = 37;

      for (int j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerIn, j, 8 + j * 18, 161 + i));
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return ItemCatalog.doesPlayerHavaCatalog(this.entityplayer);
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      if (!this.entityplayer.level().isClientSide) {
         ItemStack itemstack = this.invResult.removeItemNoUpdate(0);
         if (!itemstack.isEmpty()) {
            player.drop(itemstack, false);
         }
      }
   }

   @Override
   public ItemStack quickMoveStack(Player par1EntityPlayer, int slotNum) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(slotNum);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (slotNum == 0 && !this.moveItemStackTo(itemstack1, 1, 10, true)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }
      }

      return ItemStack.EMPTY;
   }

   /**
    * 1.12.2 {@code receiveButtonAction(EntityPlayer, int, int, int, int)}: the server side of the
    * catalog's "buy" button.  Called from the jbrobot GUI-action packet handler.
    */
   public void receiveButtonAction(ServerPlayer player, int ID, int mtype, int packageId, int entryId) {
      if (player != null && player.level() != null && !player.level().isClientSide) {
         if (ItemCatalog.doesPlayerHavaCatalog(this.entityplayer)) {
            if (ID == 6) {
               if (mtype < 0 || mtype >= JBMerchantType.values().length) {
                  return;
               }

               JBMerchantType type = JBMerchantType.values()[mtype];
               IJBRMerchantPackage merchantPkg = null;
               if (packageId >= 0 && packageId < JBRobotMODContainer.syncedExPackageListServer.size()) {
                  merchantPkg = JBRobotMODContainer.syncedExPackageListServer.get(packageId);
               }

               if (merchantPkg == null) {
                  return;
               }

               IJBRMerchantEntry entry = merchantPkg.getMerchantEntry(type, entryId);
               if (entry == null) {
                  return;
               }

               ItemStack costStack = entry.getMerchantCost();
               ItemStack resultStack = entry.getMerchantResult(player.level());
               if (CompatHelper.isStackEmptyOrNull(costStack) || CompatHelper.isStackEmptyOrNull(resultStack)) {
                  return;
               }

               if (!this.isResultSlotReadyForBuy(resultStack)) {
                  return;
               }

               if (!this.checkPlayerHasMerchantCost(costStack)) {
                  return;
               }

               if (this.takeCostFromPlayer(costStack)) {
                  this.moveItemStackTo(resultStack.copy(), 0, 1, true);
               }
            }
         }
      }
   }

   public boolean isResultSlotReadyForBuy(ItemStack merchantResult) {
      ItemStack stackInResult = this.invResult.getItem(0);
      if (!this.isStackEmpty(stackInResult)) {
         if (!this.isStackSameItemType(merchantResult, stackInResult)) {
            return false;
         }

         int resultSizeMerchant = merchantResult.getCount() + stackInResult.getCount();
         if (resultSizeMerchant > this.invResult.getMaxStackSize() || resultSizeMerchant > merchantResult.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   public boolean checkPlayerHasMerchantCost(ItemStack costStack) {
      if (CompatHelper.isStackEmptyOrNull(costStack)) {
         return false;
      } else {
         ItemStack costStackCpy = costStack.copy();

         for (int i = 1; i < this.slots.size(); ++i) {
            Slot slot = this.slots.get(i);
            ItemStack stackSlot;
            if (slot.hasItem() && !CompatHelper.isStackEmptyOrNull(stackSlot = slot.getItem()) && this.isStackSameItemType(costStackCpy, stackSlot)) {
               costStackCpy.shrink(stackSlot.getCount());
            }
         }

         return costStackCpy.getCount() <= 0;
      }
   }

   public boolean takeCostFromPlayer(ItemStack costStack) {
      if (CompatHelper.isStackEmptyOrNull(costStack)) {
         return false;
      } else {
         ItemStack costStackCpy = costStack.copy();
         int safeCheck = 0;

         do {
            if (safeCheck++ > 30) {
               (new RuntimeException("something went wrong")).printStackTrace();
               return false;
            }

            int leastSlotNum = -1;
            int leastSlotSize = Integer.MAX_VALUE;

            ItemStack stackSlot;
            for (int i = 1; i < this.slots.size(); ++i) {
               Slot slot = this.slots.get(i);
               if (slot.hasItem() && !CompatHelper.isStackEmptyOrNull(stackSlot = slot.getItem()) && this.isStackSameItemType(costStackCpy, stackSlot) && leastSlotSize > stackSlot.getCount()) {
                  leastSlotNum = i;
                  leastSlotSize = stackSlot.getCount();
               }
            }

            if (leastSlotNum == -1) {
               return false;
            }

            Slot slotLeast = this.slots.get(leastSlotNum);
            stackSlot = slotLeast.getItem();
            if (costStackCpy.getCount() <= stackSlot.getCount()) {
               stackSlot.shrink(costStackCpy.getCount());
               if (stackSlot.getCount() <= 0) {
                  slotLeast.set(ItemStack.EMPTY);
                  slotLeast.setChanged();
               } else {
                  slotLeast.set(stackSlot);
                  slotLeast.setChanged();
               }

               costStackCpy.setCount(0);
            } else {
               costStackCpy.shrink(stackSlot.getCount());
               slotLeast.set(ItemStack.EMPTY);
               slotLeast.setChanged();
            }
         } while (costStackCpy.getCount() != 0);

         return true;
      }
   }

   private boolean isStackEmpty(ItemStack itemStack) {
      return CompatHelper.isStackEmptyOrNull(itemStack) || itemStack.isEmpty();
   }

   /**
    * 1.12.2 compared item type, (subtype) damage and NBT tags.  1.20.1 keeps the 1.12 subtype/damage
    * semantics in the item NBT, so {@code ItemStack.isSameItemSameTags} (item + damage + tags)
    * reproduces the whole 1.12.2 condition; the separate {@code getHasSubtypes()} short circuit is
    * therefore not needed.
    */
   private boolean isStackSameItemType(ItemStack costStackCpy, ItemStack stackSlot) {
      return stackSlot.getItem() == costStackCpy.getItem() && ItemStack.isSameItemSameTags(costStackCpy, stackSlot);
   }
}
