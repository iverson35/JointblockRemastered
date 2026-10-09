package jp.mc.ancientred.jointblock.tileentity;

import java.util.Iterator;
import jp.mc.ancientred.jointblock.assemble.AddonBonus;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.assemble.AssembleInfo;
import jp.mc.ancientred.jointblock.assemble.AssembleStack;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectInventory;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.JointInventory;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityJointCore extends TileEntityJoint implements JointInventory.IJointInventoryHolder {
   public static final String NBT_TAG_UNIT_NAME = "unitName";
   public static final int[][] DEFAULT_PALLETE_DATA = new int[][]{{0, -2588877, -5092136, -10053160, -1710797, -8401895, -884827, -1}, {0, -11776948, -6710887, -11763815, -8437838, -13415246, -10073037, -10059981}, {0, -6737101, -15132391, -133562, -7531500, -13827789, -2236963, -7871007}, {0, -10049988, -8825542, -4417438, -7368817, -2407398, -1909853, -12902390}, {0, -9925710, -14003325, -328420, -7796471, -6446684, -7677475, -6900010}, {0, -1, -1, -1, -1, -1, -1, -1}};
   public static int[][] BOFIXED_DEFAULT_PALETTE_DATA = new int[6][];
   public static final int PALETTE_LISTS_SIZE = 6;
   public static final int PALETTE_DATA_LEN = 8;
   public static final int[] EMPTY_IARY;
   public static final int RESULT_ASSEMBLE_INF_CNT = 16;
   public static final int LARGEUNIT_WEIGHT_MAX = 300;
   public int unitId = 0;
   public int[] unitCheckResult = new int[]{-1};
   public float openFront;
   public float openFrontPrev;
   public String unitName = "";
   public int assembleFlags;
   public long iffTagId = 0L;
   public long msbTagId = 0L;
   public byte boardViewFlags;
   public int slaveSlotNum = 1;
   public int[][] paletteDataList = new int[6][];
   public JointInventory innerChest;
   public EffectInventory effectChest;
   public AddonBonus addonBonus;
   public short largeUnitWeightFixed = 100;

   public TileEntityJointCore(BlockPos pos, BlockState state) {
      super(JBBlockEntityTypes.JOINT_CORE, 0, new int[]{0, 0, 0, 0}, pos, state);
      this.boFixedPaletteDataList = new int[6][];
      this.innerChest = new JointInventory(this, "jointGui.unitinventory", 63);
      this.effectChest = new EffectInventory(this, "jointGui.unitinventory");
   }

   public boolean isInventoryCurrentlyUsable(Player player) {
      return BlockJointCore.getTileEntityJointerCore(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) != null;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level.isClientSide) {
         this.openFrontPrev = this.openFront;
         Player entityplayer = this.level.getNearestPlayer((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5, 5.0, false);
         if (entityplayer != null) {
            this.openFront += 0.1F;
         } else {
            this.openFront -= 0.1F;
         }

         if (this.openFront < 0.0F) {
            this.openFront = 0.0F;
         }

         if (this.openFront > 1.0F) {
            this.openFront = 1.0F;
         }
      }

   }

   public void searchParts(boolean isBuildSupportTest, boolean isAddonBonusTest) {
      int corePosX = this.worldPosition.getX();
      int corePosY = this.worldPosition.getY();
      int corePosZ = this.worldPosition.getZ();
      if (!this.level.isClientSide) {
         AssembleStack stack = AssembleStack.tHreadAsembleStack.get().reset();
         AssembleInfo assembleInfo = new AssembleInfo(stack, this.getJointFacing(), corePosX, corePosY, corePosZ);
         this.innerChest.collectAddonInvItems(assembleInfo);
         AssembleInfo.assembleAllParts(this.level, corePosX, corePosY, corePosZ, assembleInfo);
         if (isAddonBonusTest) {
            this.addonBonus = assembleInfo.addonBonus;
            this.addonBonus.createAddonSearchResultAry();
         } else if (isBuildSupportTest) {
            this.unitCheckResult = createResultArray(assembleInfo);
         }

         this.markUpdateThis();
      }
   }

   public EntityJointed assemble(Level worldToSpawn, BlockGetter worldToSearch, Player entityPlayer) {
      int corePosX = this.worldPosition.getX();
      int corePosY = this.worldPosition.getY();
      int corePosZ = this.worldPosition.getZ();
      if (worldToSpawn.isClientSide) {
         return null;
      } else {
         int coreFacing = this.getJointFacing();
         AssembleStack stack = AssembleStack.tHreadAsembleStack.get().reset();
         AssembleInfo assembleInfo = new AssembleInfo(stack, coreFacing, corePosX, corePosY, corePosZ);
         this.innerChest.collectAddonInvItems(assembleInfo);
         AssembleInfo.assembleAllParts(worldToSearch, corePosX, corePosY, corePosZ, assembleInfo);
         if (assembleInfo.partsCount == 0) {
            return null;
         } else {
            if (worldToSearch instanceof Level) {
               Iterator<JointParts> var10 = assembleInfo.keySet().iterator();

               while(var10.hasNext()) {
                  JointParts parts = var10.next();
                  parts.destroyBlockAtCoords((Level)worldToSearch, coreFacing, corePosX, corePosY, corePosZ);
               }
            }

            EntityJointed mob = this.buildEntityJointed(worldToSpawn, assembleInfo);
            float yaw = -JBRotationUtil.roatYawEachSide[assembleInfo.coreFacing];
            mob.moveTo((double)corePosX + 0.5, (double)(corePosY - -assembleInfo.minY), (double)corePosZ + 0.5, yaw, 0.0F);
            if (entityPlayer != null) {
               mob.ownerUUID = entityPlayer.getUUID();
               mob.ownerHash = JBCommonUtil.getUUIDHash(entityPlayer);
               ItemJBController.setEntityUIDtoController(entityPlayer, mob.getUUID(), this.unitName, corePosX, corePosY, corePosZ);
               if ((mob.seatFlags & 1) != 0 && AssembleFlagsUtil.boardImmidiate(mob.assembleFlags)) {
                  mob.boardImmidiatePlayer = entityPlayer;
               }
            }

            return mob;
         }
      }
   }

   protected EntityJointed buildEntityJointed(Level world, AssembleInfo assembleInfo) {
      int seatsFlg = 0;

      for(int i = 1; i <= 4; ++i) {
         if (assembleInfo.seatNumUsed[i]) {
            seatsFlg |= 1 << i - 1;
         }
      }

      EntityJointed mob = new EntityJointed(world);
      mob.unitName = this.unitName;
      mob.coreHeight = (float)(-assembleInfo.minY);
      mob.minYSavedForDisassemble = assembleInfo.minY;
      mob.coreFacing = assembleInfo.coreFacing;
      mob.jointParts = assembleInfo.topLevelParts;
      mob.partsCount = assembleInfo.partsCount;
      mob.mainUnitId = mob.unitId = (byte)this.unitId;
      mob.assembleFlags = this.assembleFlags;
      mob.largeUnitWeightFixed = this.largeUnitWeightFixed;
      mob.boardViewFlags = this.boardViewFlags;
      mob.iffTagId = this.iffTagId;
      mob.msbTagId = this.msbTagId;
      mob.seatFlags = (byte)seatsFlg;
      mob.paletteDataList = this.paletteDataList;
      mob.refLists = assembleInfo.createRefLists();
      mob.addonBonus = assembleInfo.addonBonus;
      mob.maxJBHealth = (short)(JBConfig.baseUnitHealth / 2);
      mob.maxJBEnergy = (short)(JBConfig.baseUnitEnergy / 2);
      mob.setupUnitHealthEnergy(mob.mainUnitId);
      mob.setupAttributeFromUnitAblility();
      mob.setupUnitAbilityFromUnitId(mob.mainUnitId);
      mob.setEFuelPool(this.innerChest.getFuelValue(true));
      if (!AssembleFlagsUtil.isInfiniteFuel(mob.assembleFlags)) {
         mob.setEnergy(0.0F);
      }

      mob.setSlaveMasterFlags(assembleInfo.masterSlotEnableStateFlag);
      mob.setSlaveSlotNum(this.slaveSlotNum);
      if (mob.isSlaveMode()) {
         mob.setCoreSubColor(true);
      }

      mob.innerChest = this.innerChest;
      mob.innerChest.setNewHolder(mob);
      this.effectChest.inventoryItemToEffectSlot(mob.effectSlots);
      return mob;
   }

   public static int[] createResultArray(AssembleInfo assembleInfo) {
      int[] resultIntArray = new int[]{assembleInfo.partsCount, assembleInfo.motionTelePartsCount, assembleInfo.minX, assembleInfo.maxX, assembleInfo.minY, assembleInfo.maxY, assembleInfo.minZ, assembleInfo.maxZ, assembleInfo.coreBlockCount, assembleInfo.seatBlockCount, assembleInfo.normalBlockCount, assembleInfo.rotatorBlockCount, assembleInfo.extenderBlockCount, assembleInfo.scalerBlockCount, assembleInfo.sliderBlockCount, assembleInfo.attracterBlockCount};
      return resultIntArray;
   }

   public void clearUnitTestResult() {
      this.unitCheckResult = EMPTY_IARY;
   }

   @Override
   protected void readOriginalValues(CompoundTag par1nbtTagCompound) {
      super.readOriginalValues(par1nbtTagCompound);
      this.readOriginalValuesExcept(par1nbtTagCompound, false);
   }

   public void readOriginalValuesExcept(CompoundTag par1nbtTagCompound, boolean forBlueprint) {
      this.unitId = par1nbtTagCompound.getInt("unitId");
      if (par1nbtTagCompound.contains("unitName")) {
         this.unitName = par1nbtTagCompound.getString("unitName");
      }

      if (par1nbtTagCompound.contains("assembleFlags", 1)) {
         this.assembleFlags = par1nbtTagCompound.getByte("assembleFlags");
      } else {
         this.assembleFlags = par1nbtTagCompound.getInt("assembleFlags");
      }

      this.boardViewFlags = par1nbtTagCompound.getByte("boardViewFlags");
      this.slaveSlotNum = par1nbtTagCompound.getInt("slaveSlotNum");
      if (par1nbtTagCompound.contains("largeUnitWeightFixed", 2)) {
         this.largeUnitWeightFixed = par1nbtTagCompound.getShort("largeUnitWeightFixed");
      } else if (par1nbtTagCompound.contains("largeUnitWeightFixed", 1)) {
         this.largeUnitWeightFixed = (short)par1nbtTagCompound.getByte("largeUnitWeightFixed");
      } else {
         this.largeUnitWeightFixed = 100;
      }

      if (this.largeUnitWeightFixed <= 0) {
         this.largeUnitWeightFixed = 100;
      }

      this.msbTagId = par1nbtTagCompound.getLong("msbTagId");
      if (!forBlueprint) {
         this.unitCheckResult = par1nbtTagCompound.getIntArray("unittestres");
         this.iffTagId = par1nbtTagCompound.getLong("iffTagId");
         ListTag nbttaglistInv = par1nbtTagCompound.getList("innerChest", 10);
         this.innerChest.readFromNBT(nbttaglistInv);
         nbttaglistInv = par1nbtTagCompound.getList("effectChest", 10);
         this.effectChest.readFromNBT(nbttaglistInv);
         if (par1nbtTagCompound.contains("jbaddonbonus", 10)) {
            this.addonBonus = new AddonBonus();
            this.addonBonus.readFromNBT(par1nbtTagCompound);
         } else {
            this.addonBonus = null;
         }
      }

      JBCarvingDataUtil.getPaletteDataFromNBT(this.paletteDataList, "paletteDataList", par1nbtTagCompound);
      if (this.level != null && this.level.isClientSide) {
         this.checkPalletChange();
      }

   }

   @Override
   protected void writeOriginalValues(CompoundTag par1nbtTagCompound) {
      super.writeOriginalValues(par1nbtTagCompound);
      this.writeOriginalValuesExcept(par1nbtTagCompound, false);
   }

   public void writeOriginalValuesExcept(CompoundTag par1nbtTagCompound, boolean forBlueprint) {
      par1nbtTagCompound.putInt("unitId", this.unitId);
      if (this.unitName != null && this.unitName.length() > 0) {
         par1nbtTagCompound.putString("unitName", this.unitName);
      }

      par1nbtTagCompound.putInt("assembleFlags", this.assembleFlags);
      par1nbtTagCompound.putByte("boardViewFlags", this.boardViewFlags);
      par1nbtTagCompound.putInt("slaveSlotNum", this.slaveSlotNum);
      par1nbtTagCompound.putLong("msbTagId", this.msbTagId);
      par1nbtTagCompound.putShort("largeUnitWeightFixed", this.largeUnitWeightFixed);
      if (!forBlueprint) {
         par1nbtTagCompound.putIntArray("unittestres", this.unitCheckResult);
         par1nbtTagCompound.putLong("iffTagId", this.iffTagId);
         par1nbtTagCompound.put("innerChest", this.innerChest.writeToNBT(new ListTag()));
         par1nbtTagCompound.put("effectChest", this.effectChest.writeToNBT(new ListTag()));
         if (this.addonBonus != null) {
            this.addonBonus.writeToNBT(par1nbtTagCompound);
         }
      }

      JBCarvingDataUtil.setPaletteDataToNBT(this.paletteDataList, "paletteDataList", par1nbtTagCompound);
   }

   @Override
   protected void readAdditionalFromJointParts(EntityJointed entityJointedMayBeNull, JointParts jointParts, int coreFacing) {
      if (entityJointedMayBeNull != null) {
         this.unitId = entityJointedMayBeNull.mainUnitId;
         this.unitName = entityJointedMayBeNull.unitName;
         this.guiPage = 0;
         this.assembleFlags = entityJointedMayBeNull.assembleFlags;
         this.largeUnitWeightFixed = entityJointedMayBeNull.largeUnitWeightFixed;
         this.boardViewFlags = entityJointedMayBeNull.boardViewFlags;
         this.iffTagId = entityJointedMayBeNull.iffTagId;
         this.msbTagId = entityJointedMayBeNull.msbTagId;
         this.slaveSlotNum = entityJointedMayBeNull.getSlaveSlotNum();
         this.paletteDataList = entityJointedMayBeNull.paletteDataList;
         if (this.paletteDataList == null) {
            this.paletteDataList = new int[6][];
         } else {
            for(int i = 0; i < this.paletteDataList.length; ++i) {
               if (this.paletteDataList[i] != null && this.paletteDataList[i].length != 8) {
                  this.paletteDataList[i] = null;
               }
            }
         }

         if (this.level != null && this.level.isClientSide) {
            this.checkPalletChange();
         }

         this.innerChest = entityJointedMayBeNull.innerChest;
         this.innerChest.setNewHolder(this);
         this.effectChest.effectSlotToInventoryItem(entityJointedMayBeNull.effectSlots);
      }
   }

   @Override
   protected void onDisassembledClient() {
      this.openFrontPrev = this.openFront = 1.0F;
   }

   private void checkPalletChange() {
      int[][] boFixedPaletteDataListNew = new int[6][];
      JBCarvingDataUtil.fixPaletteByteOrder(this.paletteDataList, boFixedPaletteDataListNew);
      boolean hasChanged = false;

      for(int i = 0; i < boFixedPaletteDataListNew.length; ++i) {
         if (boFixedPaletteDataListNew[i] != null) {
            if (this.boFixedPaletteDataList[i] == null) {
               hasChanged = true;
               break;
            }

            if (boFixedPaletteDataListNew[i].length != this.boFixedPaletteDataList[i].length) {
               hasChanged = true;
               break;
            }

            for(int j = 0; j < boFixedPaletteDataListNew[i].length; ++j) {
               if (boFixedPaletteDataListNew[i][j] != this.boFixedPaletteDataList[i][j]) {
                  hasChanged = true;
                  break;
               }
            }
         } else if (this.boFixedPaletteDataList[i] != null) {
            hasChanged = true;
            break;
         }
      }

      if (hasChanged) {
         this.boFixedPaletteDataList = boFixedPaletteDataListNew;
      }

   }

   public boolean isForceFirstPersonViewWhenBoard() {
      return (this.boardViewFlags & 128) != 0;
   }

   public boolean isChangeThirdPersonViewDirtanceFar() {
      return (this.boardViewFlags & 64) != 0;
   }

   public int getThirdPersonViewFarLevel() {
      return this.boardViewFlags & 15;
   }

   public boolean isSlaveMode() {
      return AssembleFlagsUtil.isSlaveMode(this.assembleFlags);
   }

   static {
      for(int i = 0; i < DEFAULT_PALLETE_DATA.length; ++i) {
         BOFIXED_DEFAULT_PALETTE_DATA[i] = JBCarvingDataUtil.boFixlPalette(DEFAULT_PALLETE_DATA[i], BOFIXED_DEFAULT_PALETTE_DATA[i]);
      }

      EMPTY_IARY = new int[0];
   }
}
