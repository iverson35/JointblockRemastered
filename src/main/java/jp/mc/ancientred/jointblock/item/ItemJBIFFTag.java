package jp.mc.ancientred.jointblock.item;

import java.util.List;
import java.util.Random;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBBase64Util;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ItemJBIFFTag extends Item implements ICommandEditableItem {
   private static final Random rand = new Random();
   public static final String NBT_TAGNAME_IFF_ID = "jb:iffhash";
   public static final String NBT_TAGNAME_POSNUM = "jb:posnum";
   public static final int POSNUM_CENTER = 0;
   public static final int POSNUM_BACK = 1;
   public static final int POSNUM_BACK_BACK = 2;
   public static final int POSNUM_FRONT = 3;
   public static final int POSNUM_FRONT_FRONT = 4;
   public static final int POSNUM_NUM_MAX = 4;
   public static final int TYPE_IFF = 0;
   public static final int TYPE_MSB = 1;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private String[] SUB_NAMES = new String[]{"iff", "msb"};

   public ItemJBIFFTag() {
      super(new Item.Properties());
   }

   /**
    * 1.12 damage-value subtype, now stored as int NBT tag "subtype"
    * (flattening to separate items is a Phase 6 decision).
    */
   public static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt("subtype") : 0;
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      stack.getOrCreateTag().putInt("subtype", subtype);
      return stack;
   }

   @Override
   public void inventoryTick(ItemStack itemStack, Level world, Entity entity, int itemSlotIndex, boolean isCurrentItem) {
      if (entity instanceof Player && isStackMSB(itemStack)) {
         MasterSlot masterSlot = MasterSlot.getFromEntity(entity, 0);
         if (entity.tickCount != masterSlot.updatedTicksExisted) {
            masterSlot.updateConnection();
            masterSlot.updatedTicksExisted = entity.tickCount;
         }
      }

   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int i = getSubtype(itemStack);
      if (i < 0 || i >= this.SUB_NAMES.length) {
         i = 0;
      }

      return super.getDescriptionId() + "." + this.SUB_NAMES[i];
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         CompoundTag tag;
         if (player.isShiftKeyDown()) {
            if (!stack.hasTag()) {
               stack.setTag(new CompoundTag());
            }

            tag = stack.getTag();
            byte patternNum;
            if (tag.contains("jb:posnum")) {
               patternNum = tag.getByte("jb:posnum");
               if (patternNum >= 0 && patternNum <= 4) {
                  ++patternNum;
                  if (patternNum < 0 || patternNum > 4) {
                     patternNum = 0;
                  }
               } else {
                  patternNum = 1;
               }
            } else {
               patternNum = 1;
            }

            tag.putByte("jb:posnum", patternNum);
         } else {
            if (!stack.hasTag()) {
               stack.setTag(new CompoundTag());
            }

            tag = stack.getTag();
            if (!tag.contains("jb:iffhash")) {
               long id;
               do {
                  id = rand.nextLong();
               } while(id == 0L);

               tag.putLong("jb:iffhash", id);
            }
         }

         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   private static int getPosNumFromStackNBT(ItemStack stack) {
      int patternNum = 0;
      if (stack.hasTag()) {
         patternNum = stack.getTag().getByte("jb:posnum");
         if (patternNum < 0 || patternNum > 4) {
            patternNum = 0;
         }
      }

      return patternNum;
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
      CompoundTag nbttagcompound = stack.getTag();
      if (nbttagcompound != null && nbttagcompound.contains("jb:iffhash")) {
         list.add(Component.literal(String.valueOf(JBBase64Util.longToBasd64(nbttagcompound.getLong("jb:iffhash")))));
      }

   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   public static void setIFFTagToCore(ItemStack itemStack, TileEntityJointCore teJoint, Player entityPlayer) {
      if (!teJoint.getLevel().isClientSide) {
         if (entityPlayer.isShiftKeyDown()) {
            if (isStackMSB(itemStack)) {
               teJoint.msbTagId = 0L;
            } else {
               teJoint.iffTagId = 0L;
            }

            teJoint.markUpdateThis();
            JBItemUtils.playDataApplySound(entityPlayer);
         } else if (itemStack.hasTag()) {
            long tagId = itemStack.getTag().getLong("jb:iffhash");
            if (isStackMSB(itemStack)) {
               if (teJoint.msbTagId != tagId) {
                  teJoint.msbTagId = tagId;
                  teJoint.markUpdateThis();
                  JBItemUtils.playDataApplySound(entityPlayer);
               }
            } else if (teJoint.iffTagId != tagId) {
               teJoint.iffTagId = tagId;
               teJoint.markUpdateThis();
               JBItemUtils.playDataApplySound(entityPlayer);
            }
         }
      }

   }

   private static boolean isStackWithSameId(ItemStack itemStack, long iffTagId) {
      if (CompatHelper.isStackEmptyOrNull(itemStack)) {
         return false;
      } else if (!(itemStack.getItem() instanceof ItemJBIFFTag)) {
         return false;
      } else if (!itemStack.hasTag()) {
         return false;
      } else {
         return itemStack.getTag().getLong("jb:iffhash") == iffTagId;
      }
   }

   public static boolean isStackIFF(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && getSubtype(itemStack) == 0;
   }

   public static boolean isStackMSB(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && getSubtype(itemStack) == 1;
   }

   public static boolean isStackIFFWithSameId(ItemStack itemStack, long msbTagId) {
      return isStackWithSameId(itemStack, msbTagId) && getSubtype(itemStack) == 0;
   }

   public static boolean isStackMSBWithSameId(ItemStack itemStack, long msbTagId) {
      return isStackWithSameId(itemStack, msbTagId) && getSubtype(itemStack) == 1;
   }

   public static int getPlayerMSBSlavePositionNum(Player entityPlayer, EntityJointed slaveJointed) {
      List<ItemStack> mainInv = entityPlayer.getInventory().items;
      if (mainInv == null) {
         return 0;
      } else {
         for(int i = 0; i < 9; ++i) {
            ItemStack stack = (ItemStack)mainInv.get(i);
            if (!CompatHelper.isStackEmptyOrNull(stack) && isStackMSBWithSameId(stack, slaveJointed.msbTagId)) {
               return getPosNumFromStackNBT(stack);
            }
         }

         return 0;
      }
   }

   @Override
   public boolean editByCommand(Player player, ItemStack stack, String[] args) {
      if (args != null && args.length > 0) {
         try {
            long id = Long.parseLong(args[0]);
            CompoundTag tag = stack.getTag();
            if (tag == null) {
               stack.setTag(tag = new CompoundTag());
            }

            if (id != 0L) {
               tag.putLong("jb:iffhash", id);
               return true;
            }
         } catch (Exception var7) {
         }

         return false;
      } else {
         return false;
      }
   }
}
