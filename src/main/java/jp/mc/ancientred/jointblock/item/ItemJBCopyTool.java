package jp.mc.ancientred.jointblock.item;

import java.util.List;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

public class ItemJBCopyTool extends Item implements ICeramicModItem {
   public static final String NBT_TAGNAME_CARVINGDATA = "jb:cdata";
   public static final String NBT_TAGNAME_PALETTEDATA = "jb:pdata";
   public static final int TYPE_PALETTE = 0;
   public static final int TYPE_COPYMOLD = 1;
   public static final int TYPE_FLIPMOLD = 2;
   public static final int TYPE_PARAMCPY = 3;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private String[] SUB_NAMES = new String[]{"palette", "copymold", "flipmold", "paramcpy"};
   public static final String NBT_TAGNAME_PARAMCPY = "jb:prmcpy";
   public static final String SUBTAG_JOINTTYPE = "jtyp";
   public static final String SUBTAG_CPYMOTION = "docmtn";
   public static final String SUBTAG_MOTIONID = "motid";
   public static final String SUBTAG_BINDNUMS = "motbnd";
   public static final String SUBTAG_ITEMMETA = "imeta";
   public static final String SUBTAG_MOTPARAMS = "pmary";
   public static final String SUBTAG_CPYINIT = "docint";
   public static final String SUBTAG_INITVALUES = "ivary";
   public static final String SUBTAG_CPYEXTRAS = "docext";
   public static final String SUBTAG_EXTRAFLAGS = "extf";
   public static final int CPY_FLAGS_CPYMOTION = 1;
   public static final int CPY_FLAGS_CPYINITVAL = 2;
   public static final int CPY_FLAGS_CPYEXTRAS = 4;

   public ItemJBCopyTool() {
      super((new Item.Properties()).stacksTo(8));
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

   public void onItemUsedToJointBlock(ItemStack stack, Player player, TileEntityJoint te) {
      if (isPaletteTool(stack)) {
         if (te instanceof TileEntityJointCore) {
            if (player.isShiftKeyDown()) {
               if (!player.level().isClientSide) {
                  this.copyPaletteDataFromTile(stack, (TileEntityJointCore)te, player);
               }
            } else {
               JBModContainer.proxy.openGui(JBGuiIds.guiJointCorePaleteId, player, player.level(), te.getBlockPos());
            }
         } else if (!player.level().isClientSide) {
            if (player.isShiftKeyDown()) {
               JBCarvingDataUtil.setPaletteIndexPrev(te.carvingData);
            } else {
               JBCarvingDataUtil.setPaletteIndexNext(te.carvingData);
            }

            te.markUpdateForCarvingDataMod(player);
            te.isCarvingCompiled = false;
         }
      }

      if (isParamCpyTool(stack)) {
         if (player.isShiftKeyDown()) {
            if (!(te instanceof TileEntityJointCore) && !player.level().isClientSide) {
               te.copyValuesToParamCpy(player, stack);
            }
         } else {
            JBModContainer.proxy.openGui(JBGuiIds.guiJointParamCpyId, player, player.level(), te.getBlockPos());
         }
      }

      if (!player.level().isClientSide && te.isValidCarveable()) {
         if (isCopyMoldTool(stack)) {
            if (player.isShiftKeyDown()) {
               this.copyCarvingDataFromTile(stack, te, player);
            } else {
               this.copyCarvingDataToTile(stack, te, player);
            }
         } else if (isFlipMoldTool(stack)) {
            if (player.isShiftKeyDown()) {
               te.carvingData = JBCarvingDataUtil.flipCarveDataY(te.carvingData);
               te.markUpdateForCarvingDataMod(player);
               te.isCarvingCompiled = false;
            } else {
               int facing = te.getJointFacing();
               int yawDiv = ((Mth.floor((double)(player.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
               int playerOpposite = 3;
               if (yawDiv == 0) {
                  playerOpposite = 3;
               }

               if (yawDiv == 1) {
                  playerOpposite = 4;
               }

               if (yawDiv == 2) {
                  playerOpposite = 2;
               }

               if (yawDiv == 3) {
                  playerOpposite = 5;
               }

               boolean flipX = false;
               switch (facing) {
                  case 2:
                  case 3:
                     flipX = playerOpposite == 2 || playerOpposite == 3;
                     break;
                  case 4:
                  case 5:
                     flipX = playerOpposite == 4 || playerOpposite == 5;
               }

               if (flipX) {
                  te.carvingData = JBCarvingDataUtil.flipCarveDataX(te.carvingData);
               } else {
                  te.carvingData = JBCarvingDataUtil.flipCarveDataZ(te.carvingData);
               }

               te.markUpdateForCarvingDataMod(player);
               te.isCarvingCompiled = false;
            }
         }
      }

   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   private void copyCarvingDataFromTile(ItemStack stack, TileEntityJoint te, Player player) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putIntArray("jb:cdata", te.carvingData);
      JBItemUtils.playDataRegistrationSound(player);
   }

   private void copyCarvingDataToTile(ItemStack stack, TileEntityJoint te, Player player) {
      if (JBConfig.enableCarving) {
         if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            te.carvingData = tag.getIntArray("jb:cdata");
            te.isCarvingCompiled = false;
            if (!te.isValidCarveable()) {
               te.carvingData = null;
            }

            te.markUpdateForCarvingDataMod(player);
            JBItemUtils.playDataApplySound(player);
         }
      }
   }

   public static boolean doesStackHavePaletteData(ItemStack stack) {
      if (!stack.hasTag()) {
         return false;
      } else {
         CompoundTag tag = stack.getTag();
         return JBCarvingDataUtil.doesNBTHavePaletteData("jb:pdata", tag);
      }
   }

   public static int[][] getPaletteData(ItemStack stack) {
      if (!stack.hasTag()) {
         return (int[][])null;
      } else {
         CompoundTag tag = stack.getTag();
         int[][] paletteData = new int[6][];
         JBCarvingDataUtil.getPaletteDataFromNBT(paletteData, "jb:pdata", tag);
         return paletteData;
      }
   }

   public void copyPaletteDataFromTile(ItemStack stack, TileEntityJointCore teCore, Player player) {
      CompoundTag tag = stack.getOrCreateTag();
      JBCarvingDataUtil.setPaletteDataToNBT(teCore.paletteDataList, "jb:pdata", tag);
      JBItemUtils.playDataRegistrationSound(player);
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
      CompoundTag nbttagcompound = stack.getTag();
      if (nbttagcompound != null) {
         if (nbttagcompound.contains("jb:cdata")) {
            list.add(Component.translatable("jbInfo.hascopydata"));
         } else if (nbttagcompound.contains("jb:pdata")) {
            list.add(Component.translatable("jbInfo.hascopydata"));
         } else if (nbttagcompound.contains("jb:prmcpy")) {
            list.add(Component.translatable("jbInfo.hascopydata"));
            CompoundTag subTag = getCopyValueTag(stack);
            if (subTag != null) {
               list.add(Component.literal("T:").append(Component.translatable(TileEntityJoint.getJointTypeName(subTag.getInt("jtyp")))));
            }
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
   public int getEnchantmentValue() {
      return 0;
   }

   public static boolean isPaletteTool(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBCopyTool && getSubtype(itemStack) == 0;
   }

   public static boolean isCopyMoldTool(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBCopyTool && getSubtype(itemStack) == 1;
   }

   public static boolean isFlipMoldTool(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBCopyTool && getSubtype(itemStack) == 2;
   }

   public static boolean isParamCpyTool(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBCopyTool && getSubtype(itemStack) == 3;
   }

   public static CompoundTag getCopyValueTag(ItemStack itemStack) {
      CompoundTag tag = itemStack.getTag();
      return tag != null && tag.contains("jb:prmcpy", 10) ? tag.getCompound("jb:prmcpy") : null;
   }

   public static boolean checkDataAndJointType(ItemStack itemStack, TileEntityJoint te) {
      CompoundTag tag = getCopyValueTag(itemStack);
      if (tag != null) {
         return te.jointType == tag.getInt("jtyp");
      } else {
         return false;
      }
   }
}
