package jp.mc.ancientred.jointblock.item;

import java.util.List;
import jp.mc.ancientred.jointblock.assemble.AssembleInfo;
import jp.mc.ancientred.jointblock.assemble.AssembleStack;
import jp.mc.ancientred.jointblock.assemble.JointDataSerializer;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.entity.EntityDisassembleBall;
import jp.mc.ancientred.jointblock.export.MemorySavedBlueprintDataCollections;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemJBBlueprint extends Item implements ICeramicModItem, ICommandEditableItem {
   public static final int TYPE_PERSISTENT = 0;
   public static final int TYPE_TEMPORARY = 1;
   public static final int TYPE_MERCHANT = 2;
   public static final int TYPE_CAPSULE = 3;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private String[] SUB_NAMES = new String[]{"pers", "temp", "merch", "caps"};
   public static final String WORLD_SAVEDATA_KEY_NAME = "jb_blueprints";
   public static final String NBT_TAGNAME_BLUEPRINTDATA = "jb:blueprint";
   public static final String NBT_TAGNAME_SEALED = "jb:seal";
   public static final String NBT_TAGNAME_OUTDATED = "jb:outdate";
   public static final String NBT_SUBTAGNAME_CORENUM = "cnm";
   public static final String NBT_SUBTAGNAME_SEATNUM = "snm";
   public static final String NBT_SUBTAGNAME_NORMALNUM = "nnm";
   public static final String NBT_SUBTAGNAME_ROTATORNUM = "rnm";
   public static final String NBT_SUBTAGNAME_EXTENDERNUM = "enm";
   public static final String NBT_SUBTAGNAME_SCALERNUM = "scl";
   public static final String NBT_SUBTAGNAME_SLIDERNUM = "sld";
   public static final String NBT_SUBTAGNAME_ATTRACTERNUM = "atr";
   public static final String NBT_SUBTAGNAME_TOTALNUM = "tnm";
   public static final String NBT_SUBTAGNAME_NAME = "name";
   public static final String NBT_SUBTAGNAME_IDTABLE = "idtbl";
   public static final String NBT_SUBTAGNAME_STRAIGHT_TABLE = "sttbl";
   public static final String NBT_SUBTAGNAME_SERIALIZED = "ser";
   public static final String NBT_SUBTAGNAME_COREINF = "crinf";
   public static final String NBT_SUBTAGNAME_WORLD_SAVED_DATA_KEY = "wsav";
   public static final String NBT_SUBTAGNAME_MEMORY_SAVED_DATA_KEY = "msav";
   public static final String NBT_SUBTAGNAME_FORMAT_VER = "fmt";
   public static final String NBT_DATATAGNAME_SERIALIZED = "ser";
   public static final String NBT_DATATAGNAME_COREINF = "crinf";
   public static final int FORMAT_VER = 20;
   public static final String BLUEPRINT_WORLD_SAVE_KEY_PREF = "bp-jb:w:";
   public static final String BLUEPRINT_MEM_SAVE_KEY_PREF = "bp-jb:t:";

   public ItemJBBlueprint() {
      super((new Item.Properties()).stacksTo(1));
   }

   /**
    * 1.12 damage-value subtype, now stored as int NBT tag "subtype"
    * (flattening to separate items is a Phase 6 decision).
    */
   public static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt("subtype") : 0;
   }

   public static void setSubtype(ItemStack stack, int subtype) {
      stack.getOrCreateTag().putInt("subtype", subtype);
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      setSubtype(stack, subtype);
      return stack;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (!isCapsuletBlueprint(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         if (!world.isClientSide) {
            CompoundTag dataTag = getBlueprintDataFromStack(stack);
            if (dataTag != null) {
               EntityDisassembleBall dab = new EntityDisassembleBall(world, player);
               dab.type = EntityDisassembleBall.BallType.capsule;
               dab.tagSpawnData = dataTag;
               dab.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
               world.addFreshEntity(dab);
            }
         }

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   public void onItemUsedToJointBlock(ItemStack itemStack, Player player, TileEntityJoint te) {
      if (!player.level().isClientSide && !isSealed(itemStack) && player.isShiftKeyDown()) {
         this.buildBluePrintFromJoint(itemStack, player, te);
      }

   }

   private void buildBluePrintFromJoint(ItemStack itemStack, Player player, TileEntityJoint te) {
      Level world = te.getLevel();
      BlockPos pos = te.getBlockPos();
      int corePosX = pos.getX();
      int corePosY = pos.getY();
      int corePosZ = pos.getZ();
      boolean isCore = te.getJointType() == 0;
      int baseFacing;
      if (isCore) {
         baseFacing = te.getJointFacing();
      } else {
         int yawDiv = ((Mth.floor((double)(player.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
         baseFacing = 3;
         if (yawDiv == 0) {
            baseFacing = 3;
         }

         if (yawDiv == 1) {
            baseFacing = 4;
         }

         if (yawDiv == 2) {
            baseFacing = 2;
         }

         if (yawDiv == 3) {
            baseFacing = 5;
         }
      }

      AssembleStack stack = ((AssembleStack)AssembleStack.tHreadAsembleStack.get()).reset();
      AssembleInfo assembleInfo = new AssembleInfo(stack, baseFacing, corePosX, corePosY, corePosZ);
      assembleInfo.ignoreCore = !isCore;
      int faceRelSideFromParent = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, te.getConnectSide());
      faceRelSideFromParent = EnumDirectionOld.OPPOSITES[faceRelSideFromParent];
      AssembleInfo.assembleAllParts(world, corePosX, corePosY, corePosZ, assembleInfo, faceRelSideFromParent);
      int[] serialized = JointDataSerializer.serializeAll(assembleInfo.createRefLists(), assembleInfo.partsCount);
      CompoundTag dataTag = new CompoundTag();
      dataTag.putInt("cnm", assembleInfo.coreBlockCount);
      dataTag.putInt("snm", assembleInfo.seatBlockCount);
      dataTag.putInt("nnm", assembleInfo.normalBlockCount);
      dataTag.putInt("rnm", assembleInfo.rotatorBlockCount);
      dataTag.putInt("enm", assembleInfo.extenderBlockCount);
      dataTag.putInt("scl", assembleInfo.scalerBlockCount);
      dataTag.putInt("sld", assembleInfo.sliderBlockCount);
      dataTag.putInt("atr", assembleInfo.attracterBlockCount);
      dataTag.putInt("tnm", assembleInfo.partsCount);
      if (isCore) {
         addCoreAddtionalInfoToTag(dataTag, world, corePosX, corePosY, corePosZ);
         String unitName = getNameFromCoreAddtionalInfo(dataTag);
         if (unitName != null && unitName.length() > 0) {
            dataTag.putString("name", unitName);
         }
      }

      dataTag.putIntArray("ser", serialized);
      if (isTemporaryBlueprint(itemStack)) {
         saveImportedBpDataToMemroyHeapWithNewId(dataTag);
      }

      dataTag.putInt("fmt", 20);
      itemStack.getOrCreateTag().put("jb:blueprint", dataTag);
      JBItemUtils.playDataRegistrationSound(player);
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
      if (isTemporaryBlueprint(stack)) {
         list.add(Component.translatable("jointGui.info.bpdesc.temp"));
      }

      CompoundTag nbttagcompound = stack.getTag();
      if (nbttagcompound != null && nbttagcompound.contains("jb:blueprint")) {
         if (nbttagcompound.getBoolean("jb:seal")) {
            list.add(Component.translatable("jbInfo.hascopydatasealed"));
         } else {
            list.add(Component.translatable("jbInfo.hascopydata"));
         }

         CompoundTag tagData = nbttagcompound.getCompound("jb:blueprint");
         if (tagData != null && tagData.contains("name")) {
            list.add(Component.translatable(tagData.getString("name")));
         }
      }

   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public int getMaxStackSize(ItemStack stack) {
      return isPersistentBlueprint(stack) ? 1 : 64;
   }

   public static boolean isBlueprint(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBBlueprint;
   }

   public static boolean isTemporaryBlueprint(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBBlueprint && getSubtype(itemStack) == 1;
   }

   public static boolean isPersistentBlueprint(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBBlueprint && getSubtype(itemStack) == 0;
   }

   public static boolean isCapsuletBlueprint(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBBlueprint && getSubtype(itemStack) == 3;
   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int i = getSubtype(itemStack);
      if (i < 0 || i >= this.SUB_NAMES.length) {
         i = 0;
      }

      return super.getDescriptionId() + "." + this.SUB_NAMES[i];
   }

   public static boolean isSealed(ItemStack itemStack) {
      CompoundTag tag = itemStack.getTag();
      return tag == null ? false : tag.getBoolean("jb:seal");
   }

   public static void saveImportedBpDataToMemroyHeapWithNewId(CompoundTag dataTag) {
      MemorySavedBlueprintDataCollections bpCollection = MemorySavedBlueprintDataCollections.instance;
      int newMemSeqId;
      synchronized(bpCollection) {
         newMemSeqId = bpCollection.getUniqueSequenceId();
      }

      String saveDataKey = "bp-jb:w:" + newMemSeqId;
      saveImportedBpDataToMemroyHeap(saveDataKey, dataTag);
   }

   public static void saveImportedBpDataToMemroyHeap(String saveDataKey, CompoundTag dataTag) {
      saveBlueprintNBTToMemoryHeap(saveDataKey, stripSerializedDataFromNBT(dataTag));
      dataTag.putString("msav", saveDataKey);
   }

   private static void saveBlueprintNBTToMemoryHeap(String saveDataKey, CompoundTag blueprintDataNBT) {
      MemorySavedBlueprintDataCollections bpCollection = MemorySavedBlueprintDataCollections.instance;
      if (bpCollection.blueprintDataNBTCollection.contains(saveDataKey)) {
         bpCollection.blueprintDataNBTCollection.remove(saveDataKey);
      }

      bpCollection.blueprintDataNBTCollection.put(saveDataKey, blueprintDataNBT);
   }

   public static CompoundTag tryGetBlueprintNBTFromMemroyHeap(CompoundTag dataTag) {
      String saveDataKey = dataTag.getString("msav");
      MemorySavedBlueprintDataCollections bpCollection = MemorySavedBlueprintDataCollections.instance;
      return !bpCollection.blueprintDataNBTCollection.contains(saveDataKey) ? null : bpCollection.blueprintDataNBTCollection.getCompound(saveDataKey);
   }

   private static CompoundTag stripSerializedDataFromNBT(CompoundTag dataTag) {
      int[] serialized = dataTag.getIntArray("ser");
      dataTag.remove("ser");
      CompoundTag coreTag = null;
      if (dataTag.contains("crinf")) {
         coreTag = dataTag.getCompound("crinf");
         dataTag.remove("crinf");
      }

      CompoundTag blueprintDataNBT = new CompoundTag();
      blueprintDataNBT.putIntArray("ser", serialized);
      if (coreTag != null) {
         blueprintDataNBT.put("crinf", coreTag);
      }

      return blueprintDataNBT;
   }

   private static void addCoreAddtionalInfoToTag(CompoundTag tagSaveData, Level world, int corePosX, int corePosY, int corePosZ) {
      TileEntityJointCore teCore = BlockJointCore.getTileEntityJointerCore(world, corePosX, corePosY, corePosZ);
      if (teCore != null) {
         CompoundTag subForCore = new CompoundTag();
         teCore.writeOriginalValuesExcept(subForCore, true);
         if (teCore.innerChest != null && teCore.innerChest.hasRINGInSlots()) {
            teCore.innerChest.writeRINGDatasToExportNBT(subForCore);
         }

         if (teCore.effectChest != null && !teCore.effectChest.isEmpty()) {
            teCore.effectChest.writeEffectSlotDatasToExportNBT(subForCore);
         }

         tagSaveData.put("crinf", subForCore);
      }

   }

   public static String getNameFromCoreAddtionalInfo(CompoundTag tagSaveData) {
      if (tagSaveData == null) {
         return null;
      } else {
         CompoundTag tagCore = tagSaveData.getCompound("crinf");
         return tagCore != null && tagCore.contains("unitName") ? tagCore.getString("unitName") : null;
      }
   }

   public static CompoundTag getBlueprintDataFromStack(ItemStack stack) {
      if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getItem() instanceof ItemJBBlueprint && stack.hasTag()) {
         CompoundTag tag = stack.getTag();
         if (tag != null && tag.contains("jb:blueprint")) {
            return tag.getCompound("jb:blueprint");
         }
      }

      return null;
   }

   public boolean editByCommand(Player player, ItemStack stack, String[] args) {
      if (args != null && args.length > 0) {
         if (args[0].equals("capsule")) {
            setSubtype(stack, 3);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
