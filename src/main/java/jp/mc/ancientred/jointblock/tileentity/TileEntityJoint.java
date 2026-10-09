package jp.mc.ancientred.jointblock.tileentity;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.assemble.AssembleInfo;
import jp.mc.ancientred.jointblock.assemble.AssembleStack;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.JointPartsForCollect;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.block.ModelBlockRegister;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import jp.mc.ancientred.jointblock.item.ItemJBCopyTool;
import jp.mc.ancientred.jointblock.item.ItemJBMirrorTool;
import jp.mc.ancientred.jointblock.item.JBItemUtils;
import jp.mc.ancientred.jointblock.motion.BasicMotionBinder;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityJoint extends BlockEntity implements Container, IJBJointParameterHolder {
   private static final int CONNECT_CHECK_DEPTH = 1024;
   public static final int INV_SLOT_MODEL_ITEM = 0;
   public static final int INV_SLOT_MOTION_ITEM = 1;
   public static final String ITEM_STACK_TAG_MOTION_PARAMS = "jb:mparams";
   public static final int INIT_VALS_NUM = 4;
   public static final int FLIP_BY_WEST_EAST = 0;
   public static final int FLIP_BY_SOUTH_NORTH = 2;
   public static final int FLIP_BY_UP_DOWN = 4;
   public static final int MIRROR_COPY_LEFT_TO_RIGHT = 10;
   public static final int MIRROR_COPY_RIGHT_TO_LEFT = 20;
   public static final int MIRROR_COPY_DOWN_TO_UP = 30;
   public static final int MIRROR_COPY_UP_TO_DOWN = 40;
   public static final int MIRROR_COPY_DELETE_CONF = 50;
   public static final int MIRROR_COPY_DELETE_EXEC = 60;
   public static final int MIRROR_COPY_SEARCH_FAIL_SAFE = 512;
   public static final int COMMON_TAB_BASE_ID = 7221;
   public static final int COMMON_TAB_EXTRA_ID = 7222;
   public static final int COMMON_TAB_COREINV_ID = 7223;
   public static final int COMMON_TAB_TOPARENT_ID = 7224;
   public static final int COMMON_TAB_TOFORWARDID = 7225;
   public static final int COMMON_TAB_COREFUELS_ID = 7226;
   public static final int COMMON_TAB_COREADDONS_ID = 7227;
   public static final int COMMON_TAB_CORESOUNDS_ID = 7228;
   public static final int MEMO_STR_MAX = 5;
   private int motionBinderItemIdCache = 0;
   private IJBMotionBinderItem motionBinderItemCache = null;
   public boolean isValidRenderModel = false;
   public boolean failedOnRender = false;
   public BlockEntity tileEntityToRender;
   public boolean checkdTileEntity;
   public boolean breakByAssemble;
   public int jointType;
   public int facingSide;
   public int connectSide;
   public int renderSide;
   public int bindNum;
   public int[] params = new int[6];
   public int initValuesLayer;
   public int[][] initValues = new int[][]{null};
   public int extraJointStateFlags;
   public String memoStr;
   public boolean hasJBAtConnectSide = false;
   public boolean isConnectedToCore = false;
   public int connectedDepth = 1024;
   public int[][] boFixedPaletteDataList;
   public int[][] prevBoFixedPaletteDataList;
   public int electricAnimation;
   private int prevMotionItemId = 0;
   private int prevBindNum = 0;
   private int prevMotionItemDamage = 0;
   private Player mirrorPendingPlayer;
   private boolean isDisassembled;
   private NonNullList<ItemStack> itemStacks;
   public boolean hasMultipleBind;
   public int guiPage;
   public int[] carvingData;
   public boolean isCarvingCompiled;
   public int carvingCompiledListNum;
   public MCubeGathered mCubeGatheredData;

   public TileEntityJoint(BlockPos pos, BlockState state) {
      this(JBBlockEntityTypes.JOINT, pos, state);
   }

   public TileEntityJoint(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
      this.guiPage = 0;
      this.isCarvingCompiled = false;
      this.carvingCompiledListNum = 0;
   }

   public TileEntityJoint(int jointType, int[] initValues, BlockPos pos, BlockState state) {
      this(JBBlockEntityTypes.JOINT, jointType, initValues, pos, state);
   }

   protected TileEntityJoint(BlockEntityType<?> type, int jointType, int[] initValues, BlockPos pos, BlockState state) {
      this(type, pos, state);
      this.jointType = jointType;
      this.initValues[0] = initValues;
   }

   public int getJointFacing() {
      return this.facingSide & 7;
   }

   public void setJointFacing(int facing) {
      this.facingSide = facing & 7;
   }

   public int getConnectableExtenderSide() {
      return EnumDirectionOld.OPPOSITES[this.connectSide];
   }

   public boolean shouldRenderInPass(int pass) {
      return pass == 0 || pass == 1;
   }

   @Override
   public void setRemoved() {
      super.setRemoved();
      this.freeGLCompiledList();
   }

   @Override
   public void onChunkUnloaded() {
      super.onChunkUnloaded();
      this.freeGLCompiledList();
   }

   private void freeGLCompiledList() {
      if (this.carvingCompiledListNum != 0) {
         // Phase 4 (rendering): GL compiled list release (JBModContainer.proxy.freeCarvingCompiledListNum) deferred.
         this.isCarvingCompiled = false;
         this.carvingCompiledListNum = 0;
      }

   }

   public IJBModelItem getModelItemFromStack() {
      return getModelItemFromStack(this.itemStacks);
   }

   public int getModelItemID() {
      IJBModelItem modelItem = getModelItemFromStack(this.itemStacks);
      if (modelItem != null && modelItem instanceof Item) {
         return BuiltInRegistries.ITEM.getId((Item)modelItem);
      } else {
         Block block = getBlockFromStack(this.itemStacks);
         return block != null ? -BuiltInRegistries.BLOCK.getId(block) : 0;
      }
   }

   private static final IJBModelItem getModelItemFromStack(NonNullList<ItemStack> stacks) {
      return stacks.size() >= 1 && !stacks.get(0).isEmpty() && stacks.get(0).getItem() instanceof IJBModelItem ? (IJBModelItem)stacks.get(0).getItem() : null;
   }

   public int getModelItemDamage() {
      return !this.itemStacks.get(0).isEmpty() ? this.itemStacks.get(0).getDamageValue() : 0;
   }

   public Block getBlockFromStack() {
      return getBlockFromStack(this.itemStacks);
   }

   private static final Block getBlockFromStack(NonNullList<ItemStack> stacks) {
      return stacks.size() >= 1 && !stacks.get(0).isEmpty() ? BlockJointBase.convertItemToBlock(stacks.get(0).getItem()) : null;
   }

   public BlockState getBlockStateFromStack() {
      return getBlockStateFromStack(this.itemStacks);
   }

   private static final BlockState getBlockStateFromStack(NonNullList<ItemStack> stacks) {
      if (stacks.size() >= 1 && !stacks.get(0).isEmpty()) {
         ItemStack stack = stacks.get(0);
         return BlockJointBase.convertItemMetaToBlockState(stack.getItem(), stack.getDamageValue());
      } else {
         return null;
      }
   }

   private void validateModelItem(boolean updateAfter) {
      IJBModelItem model = this.getModelItemFromStack();
      if (model != null) {
         this.isValidRenderModel = true;
      } else {
         Block block = this.getBlockFromStack();
         if (block != null && ModelBlockRegister.isAllowedModelBlock(block)) {
            this.isValidRenderModel = true;
         }
      }

      if (updateAfter) {
         this.markUpdateThis();
      }

   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public AABB getRenderBoundingBox() {
      double xCoord = (double)this.getBlockPos().getX();
      double yCoord = (double)this.getBlockPos().getY();
      double zCoord = (double)this.getBlockPos().getZ();
      AABB bb = JBCommonUtil.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1.0, yCoord + 1.0, zCoord + 1.0);
      IJBModelItem modelItem = getModelItemFromStack(this.itemStacks);
      if (modelItem != null && modelItem instanceof IJBModelItem) {
         double expand = modelItem.getRenderExpand(this.getModelItemDamage());
         if (expand < 0.0) {
            expand = 0.0;
         }

         return bb.inflate(expand);
      } else {
         return bb;
      }
   }

   public void setExtraFlagsTransparentAlways(boolean value) {
      if (value) {
         this.extraJointStateFlags |= Integer.MIN_VALUE;
      } else {
         this.extraJointStateFlags &= Integer.MAX_VALUE;
      }

   }

   public boolean isExtraFlagsTransparentAlways() {
      return (this.extraJointStateFlags & Integer.MIN_VALUE) != 0;
   }

   public void setExtraFlagsTransparentOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 1073741824;
      } else {
         this.extraJointStateFlags &= -1073741825;
      }

   }

   public boolean isExtraFlagsTransparentOnBoard() {
      return (this.extraJointStateFlags & 1073741824) != 0;
   }

   public void setExtraFlagsWireframeOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 536870912;
      } else {
         this.extraJointStateFlags &= -536870913;
      }

   }

   public boolean isExtraFlagsWireframeOnBoard() {
      return (this.extraJointStateFlags & 536870912) != 0;
   }

   public void setExtraFlagsVisibleOnlyOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 33554432;
      } else {
         this.extraJointStateFlags &= -33554433;
      }

   }

   public boolean isExtraFlagsVisibleOnlyOnBoard() {
      return (this.extraJointStateFlags & 33554432) != 0;
   }

   public void setExtraFlagsTypeSpecCommonA(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 268435456;
      } else {
         this.extraJointStateFlags &= -268435457;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonA() {
      return (this.extraJointStateFlags & 268435456) != 0;
   }

   public void setExtraFlagsTypeSpecCommonB(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 134217728;
      } else {
         this.extraJointStateFlags &= -134217729;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonB() {
      return (this.extraJointStateFlags & 134217728) != 0;
   }

   public void setExtraFlagsTypeSpecCommonC(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 67108864;
      } else {
         this.extraJointStateFlags &= -67108865;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonC() {
      return (this.extraJointStateFlags & 67108864) != 0;
   }

   private static final IJBMotionBinderItem getMotionItemFromStack(NonNullList<ItemStack> stacks) {
      return stacks.size() >= 2 && !stacks.get(1).isEmpty() && stacks.get(1).getItem() instanceof IJBMotionBinderItem ? (IJBMotionBinderItem)stacks.get(1).getItem() : null;
   }

   public IJBMotionBinderItem.IJBMotionBinder getMotionBinder() {
      IJBMotionBinderItem actItem = getMotionItemFromStack(this.itemStacks);
      return actItem == null ? null : actItem.getMotionBinder();
   }

   public String getMotionBinderName() {
      IJBMotionBinderItem.IJBMotionBinder motHndl = this.getMotionBinder();
      return motHndl == null ? "jointGui.info.setmotionhandler" : motHndl.getBinderName();
   }

   public int getMotionBinderItemId() {
      IJBMotionBinderItem motItem = getMotionItemFromStack(this.itemStacks);
      if (motItem == this.motionBinderItemCache) {
         return this.motionBinderItemIdCache;
      } else if (motItem != null && motItem instanceof Item) {
         this.motionBinderItemCache = motItem;
         this.motionBinderItemIdCache = BuiltInRegistries.ITEM.getId((Item)motItem);
         return this.motionBinderItemIdCache;
      } else {
         this.motionBinderItemCache = null;
         return this.motionBinderItemIdCache = 0;
      }
   }

   public int getMotionItemDamage() {
      return !this.itemStacks.get(1).isEmpty() && this.itemStacks.get(1).getItem().canBeDepleted() ? this.itemStacks.get(1).getDamageValue() : 0;
   }

   public void tick() {
      if (this.level.isClientSide) {
         ++this.electricAnimation;
         if (this.jointType != 0) {
            TileEntityJoint connectedTe = this.getConnectedSideTileEntityJointed();
            if (connectedTe != null) {
               this.hasJBAtConnectSide = true;
               this.boFixedPaletteDataList = connectedTe.boFixedPaletteDataList;
               if (this.boFixedPaletteDataList != this.prevBoFixedPaletteDataList) {
                  this.isCarvingCompiled = false;
                  this.prevBoFixedPaletteDataList = this.boFixedPaletteDataList;
               }

               this.connectedDepth = connectedTe.connectedDepth + 1;
               if (this.connectedDepth < 1024) {
                  this.isConnectedToCore = true;
               } else {
                  this.isConnectedToCore = false;
               }
            } else {
               this.hasJBAtConnectSide = false;
               this.boFixedPaletteDataList = (int[][])null;
               if (this.boFixedPaletteDataList != this.prevBoFixedPaletteDataList) {
                  this.isCarvingCompiled = false;
                  this.prevBoFixedPaletteDataList = this.boFixedPaletteDataList;
               }

               this.connectedDepth = 1024;
               this.isConnectedToCore = false;
            }
         } else {
            this.connectedDepth = 0;
            this.isConnectedToCore = true;
         }
      }

   }

   public void checkMotionItemChange() {
      int nowMotionItemId = this.getMotionBinderItemId();
      int nowItemDamage = this.getMotionItemDamage();
      if (nowMotionItemId != this.prevMotionItemId || this.bindNum != this.prevBindNum || nowItemDamage != this.prevMotionItemDamage) {
         if (nowMotionItemId != this.prevMotionItemId || nowItemDamage != this.prevMotionItemDamage) {
            this.bindNum = 0;
         }

         IJBMotionBinderItem.IJBMotionHandler mHndl = this.getBindOrFixBindStatus();
         mHndl.setParamsInitialValue(this.jointType, this.params);
         nowMotionItemId = this.getMotionBinderItemId();
         if (this.prevMotionItemId == 0 && nowMotionItemId != 0) {
            IJBMotionBinderItem.IJBMotionBinder mBnd = this.getMotionBinder();
            if (mBnd != null && !JBCommonUtil.isArrayEmptyOrNull(mBnd.getBindNumberList(this.jointType, this.getMotionItemDamage()))) {
               this.guiPage = 1;
            }
         }

         this.prevMotionItemId = nowMotionItemId;
         this.prevBindNum = this.bindNum;
         this.prevMotionItemDamage = this.getMotionItemDamage();
         this.markUpdateThis();
      }

   }

   public TileEntityJoint getConnectedSideTileEntityJointed() {
      int connectX = this.worldPosition.getX() + JBRotationUtil.side_matrix[this.connectSide][0];
      int connectY = this.worldPosition.getY() + JBRotationUtil.side_matrix[this.connectSide][1];
      int connectZ = this.worldPosition.getZ() + JBRotationUtil.side_matrix[this.connectSide][2];
      return BlockJointBase.getTileEntityJointerBase(this.level, connectX, connectY, connectZ);
   }

   public Block getConnectedSideBlock() {
      int connectX = this.worldPosition.getX() + JBRotationUtil.side_matrix[this.connectSide][0];
      int connectY = this.worldPosition.getY() + JBRotationUtil.side_matrix[this.connectSide][1];
      int connectZ = this.worldPosition.getZ() + JBRotationUtil.side_matrix[this.connectSide][2];
      return WorldHelper.getBlock(this.level, connectX, connectY, connectZ);
   }

   public void makeEverySideBlockConnectThis() {
      if (this.level != null) {
         for(int side = 0; side < 6; ++side) {
            TileEntityJoint te = JBRotationUtil.getSideNeighbourTe(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), side);
            if (te != null) {
               te.setConnectSide(EnumDirectionOld.OPPOSITES[side]);
               te.markUpdateThis();
            }
         }

      }
   }

   public void turnToNewConnectSide() {
      int sideOld = this.connectSide;
      int sideNew = JBRotationUtil.rotateSide(sideOld);
      this.connectSide = sideNew;
      this.renderSide = sideNew;
      this.markUpdateThis();
   }

   @Override
   public int getJointType() {
      return this.jointType;
   }

   @Override
   public int getParam(int paramNum) {
      return this.params[paramNum];
   }

   @Override
   public void setParam(int paramNum, int value) {
      this.params[paramNum] = value;
   }

   @Override
   public int getInitValue(int initValueNum) {
      return this.initValues[0][initValueNum];
   }

   @Override
   public int getBindNum() {
      return this.bindNum;
   }

   @Override
   public int getConnectSide() {
      return this.connectSide;
   }

   @Override
   public int getRenderSide() {
      return this.renderSide;
   }

   public void setConnectSide(int connectSide) {
      this.connectSide = connectSide;
      this.renderSide = connectSide;
   }

   @Override
   public int[] getCarvinData() {
      return this.carvingData;
   }

   public void readFromJointParts(EntityJointed entityJointedMayBeNull, JointParts jointParts, int facingSide, int coreFacing) {
      this.jointType = jointParts.jointType;
      this.guiPage = jointParts.guiPage;
      this.facingSide = facingSide;
      this.connectSide = JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(coreFacing, jointParts.connectSide);
      this.renderSide = JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(coreFacing, jointParts.renderSide);

      int i;
      for(i = 0; i < this.params.length; ++i) {
         if (i <= jointParts.params.length) {
            this.params[i] = jointParts.params[i];
         }
      }

      if (this.initValues.length != jointParts.initValues.length) {
         this.initValues = new int[jointParts.initValues.length][];
      }

      for(i = 0; i < this.initValues.length; ++i) {
         if (this.initValues[i] == null) {
            this.initValues[i] = new int[4];
         }

         for(int j = 0; j < this.initValues[i].length; ++j) {
            this.initValues[i][j] = jointParts.initValues[i][j];
         }
      }

      this.carvingData = jointParts.carvingData;
      this.isCarvingCompiled = false;
      Item modelItem;
      if (jointParts.modelItemId < 0) {
         Block modelBlock = BuiltInRegistries.BLOCK.byId(-jointParts.modelItemId);
         modelItem = this.convertReedItems(modelBlock);
         if (modelItem == null) {
            modelItem = Item.byBlock(modelBlock);
         }
      } else {
         modelItem = Item.byId(jointParts.modelItemId);
      }

      if (modelItem != null) {
         ItemStack modelStack = new ItemStack(modelItem);
         modelStack.setDamageValue(jointParts.modelMeta);
         this.itemStacks.set(0, modelStack);
         this.isValidRenderModel = true;
      }

      if (jointParts.motionItemId > 0) {
         modelItem = Item.byId(jointParts.motionItemId);
         if (modelItem != null) {
            ItemStack motionStack = new ItemStack(modelItem);
            motionStack.setDamageValue(jointParts.motionMeta);
            this.itemStacks.set(1, motionStack);
            this.prevMotionItemId = BuiltInRegistries.ITEM.getId(modelItem);
         }
      }

      this.bindNum = jointParts.bindNum;
      this.prevBindNum = jointParts.bindNum;
      this.prevMotionItemDamage = jointParts.motionMeta;
      this.extraJointStateFlags = jointParts.extraJointStateFlags & -65536;
      if (jointParts.memoStrHolder != null) {
         this.memoStr = jointParts.memoStrHolder.memoStr;
      }

      this.readAdditionalFromJointParts(entityJointedMayBeNull, jointParts, coreFacing);
      this.isDisassembled = true;
   }

   private Item convertReedItems(Block modelBlock) {
      Item modelItem = null;
      if (modelBlock == Blocks.TRIPWIRE) {
         modelItem = Items.STRING;
      } else if (modelBlock == Blocks.SUGAR_CANE) {
         modelItem = Items.SUGAR_CANE;
      } else if (modelBlock == Blocks.CAKE) {
         modelItem = Items.CAKE;
      } else if (modelBlock == Blocks.REPEATER) {
         modelItem = Items.REPEATER;
      } else if (modelBlock == Blocks.BREWING_STAND) {
         modelItem = Items.BREWING_STAND;
      } else if (modelBlock == Blocks.CAULDRON) {
         modelItem = Items.CAULDRON;
      } else if (modelBlock == Blocks.FLOWER_POT) {
         modelItem = Items.FLOWER_POT;
      } else if (modelBlock == Blocks.COMPARATOR) {
         modelItem = Items.COMPARATOR;
      }

      return modelItem;
   }

   protected void readAdditionalFromJointParts(EntityJointed entityJointed, JointParts jointParts, int coreFacing) {
   }

   protected void onDisassembledClient() {
   }

   @Override
   public void load(CompoundTag par1NBTTagCompound) {
      super.load(par1NBTTagCompound);
      this.readOriginalValues(par1NBTTagCompound);
      ListTag nbttaglist = par1NBTTagCompound.getList("Items", 10);

      for(int i = 0; i < nbttaglist.size(); ++i) {
         CompoundTag nbttagcompound1 = nbttaglist.getCompound(i);
         int j = nbttagcompound1.getByte("Slot") & 255;
         if (j >= 0 && j < this.itemStacks.size()) {
            this.itemStacks.set(j, ItemStack.of(nbttagcompound1));
         }
      }

      this.prevMotionItemId = this.getMotionBinderItemId();
      this.prevBindNum = this.bindNum;
      this.prevMotionItemDamage = this.getMotionItemDamage();
   }

   protected void readOriginalValues(CompoundTag par1NBTTagCompound) {
      this.jointType = par1NBTTagCompound.getInt("jointType");
      this.guiPage = par1NBTTagCompound.getInt("guiPage");
      this.bindNum = par1NBTTagCompound.getInt("bindNum");
      this.facingSide = par1NBTTagCompound.getInt("facingSide");
      this.connectSide = par1NBTTagCompound.getInt("connectSide");
      this.renderSide = par1NBTTagCompound.getInt("renderSide");
      this.extraJointStateFlags = par1NBTTagCompound.getInt("extraJointStateFlags");
      if (par1NBTTagCompound.contains("isValidRenderModel")) {
         this.isValidRenderModel = par1NBTTagCompound.getBoolean("isValidRenderModel");
      } else {
         this.isValidRenderModel = true;
      }

      if (par1NBTTagCompound.contains("memoStr")) {
         this.memoStr = par1NBTTagCompound.getString("memoStr");
      } else {
         this.memoStr = null;
      }

      this.params = fixArrayLength(par1NBTTagCompound.getIntArray("params"), 6);
      this.initValuesLayer = par1NBTTagCompound.getInt("initvalsLayer");
      if (par1NBTTagCompound.contains("initvalsExtras")) {
         ListTag nbttaglist = par1NBTTagCompound.getList("initvalsExtras", 10);
         this.initValues = new int[nbttaglist.size() + 1][];
         this.initValues[0] = fixArrayLength(par1NBTTagCompound.getIntArray("initvals"), 4);

         for(int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag nbttagcompound1 = nbttaglist.getCompound(i);
            this.initValues[i + 1] = fixArrayLength(nbttagcompound1.getIntArray("vals"), 4);
         }
      } else {
         if (this.initValues.length > 1) {
            this.initValues = new int[1][];
         }

         this.initValues[0] = fixArrayLength(par1NBTTagCompound.getIntArray("initvals"), 4);
      }

      if (this.initValuesLayer < 0 || this.initValuesLayer > this.initValues.length - 1) {
         this.initValuesLayer = this.initValues.length - 1;
      }

      if (par1NBTTagCompound.contains("carvingData")) {
         this.carvingData = par1NBTTagCompound.getIntArray("carvingData");
         if (!this.isValidCarveable()) {
            this.carvingData = null;
         }
      } else {
         this.carvingData = null;
      }

      this.isCarvingCompiled = false;
      this.mCubeGatheredData = null;
      this.isDisassembled = par1NBTTagCompound.getBoolean("isDisassembled");
   }

   @Override
   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   private static final int[] fixArrayLength(int[] ary, int fixLength) {
      if (ary.length != fixLength) {
         ary = Arrays.copyOf(ary, fixLength);
      }

      return ary;
   }

   @Override
   protected void saveAdditional(CompoundTag par1NBTTagCompound) {
      super.saveAdditional(par1NBTTagCompound);
      this.writeOriginalValues(par1NBTTagCompound);
      ListTag nbttaglist = new ListTag();
      int i = 0;

      for(int len = this.itemStacks.size(); i < len; ++i) {
         if (!this.itemStacks.get(i).isEmpty()) {
            CompoundTag nbttagcompound1 = new CompoundTag();
            nbttagcompound1.putByte("Slot", (byte)i);
            this.itemStacks.get(i).save(nbttagcompound1);
            nbttaglist.add(nbttagcompound1);
         }
      }

      par1NBTTagCompound.put("Items", nbttaglist);
   }

   protected void writeOriginalValues(CompoundTag par1NBTTagCompound) {
      par1NBTTagCompound.putInt("jointType", this.jointType);
      par1NBTTagCompound.putInt("guiPage", this.guiPage);
      par1NBTTagCompound.putInt("bindNum", this.bindNum);
      par1NBTTagCompound.putInt("facingSide", this.facingSide);
      par1NBTTagCompound.putInt("connectSide", this.connectSide);
      par1NBTTagCompound.putInt("renderSide", this.renderSide);
      par1NBTTagCompound.putInt("extraJointStateFlags", this.extraJointStateFlags);
      par1NBTTagCompound.putBoolean("isValidRenderModel", this.isValidRenderModel);
      if (!JBCommonUtil.isStringEmpty(this.memoStr)) {
         par1NBTTagCompound.putString("memoStr", this.memoStr);
      }

      par1NBTTagCompound.putIntArray("params", this.params);
      par1NBTTagCompound.putInt("initvalsLayer", this.initValuesLayer);
      par1NBTTagCompound.putIntArray("initvals", this.initValues[0]);
      if (this.initValues.length > 1) {
         ListTag nbttaglist = new ListTag();

         for(int i = 1; i < this.initValues.length; ++i) {
            CompoundTag nbttagcompound1 = new CompoundTag();
            nbttagcompound1.putIntArray("vals", this.initValues[i]);
            nbttaglist.add(nbttagcompound1);
         }

         par1NBTTagCompound.put("initvalsExtras", nbttaglist);
      }

      if (this.carvingData != null && JBConfig.enableCarving) {
         par1NBTTagCompound.putIntArray("carvingData", this.carvingData);
      }

      par1NBTTagCompound.putBoolean("isDisassembled", this.isDisassembled);
   }

   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
      CompoundTag tag = pkt.getTag();
      if (tag != null) {
         this.load(tag);
      }
      if (this.isDisassembled) {
         this.onDisassembledClient();
         this.isDisassembled = false;
      }

   }

   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      CompoundTag tag = this.getUpdateTag();
      this.isDisassembled = false;
      return ClientboundBlockEntityDataPacket.create(this, (be) -> tag);
   }

   @Override
   public int getContainerSize() {
      return this.itemStacks.size();
   }

   @Override
   public ItemStack getItem(int slot) {
      return this.itemStacks.get(slot);
   }

   @Override
   public ItemStack removeItem(int slot, int num) {
      return ContainerHelper.removeItem(this.itemStacks, slot, num);
   }

   @Override
   public void setItem(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.itemStacks.size()) {
         ItemStack oldStack = this.itemStacks.get(slot);
         this.itemStacks.set(slot, stack);
         if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
         }

         if (slot == 0 && !ItemStack.matches(oldStack, this.itemStacks.get(slot))) {
            this.isValidRenderModel = false;
            this.tileEntityToRender = null;
            this.checkdTileEntity = false;
            if (!this.level.isClientSide) {
               this.validateModelItem(true);
            }
         }

         if (!this.level.isClientSide) {
            this.checkMotionItemChange();
         }

         this.setChanged();
      }
   }

   public String getName() {
      return "";
   }

   public boolean hasCustomName() {
      return true;
   }

   @Override
   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public boolean stillValid(Player par1EntityPlayer) {
      return WorldHelper.getTileEntity(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) != this ? false : par1EntityPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) < 100.0;
   }

   @Override
   public void startOpen(Player entityPlayer) {
   }

   @Override
   public void stopOpen(Player entityPlayer) {
   }

   @Override
   public boolean canPlaceItem(int slot, ItemStack itemStack) {
      return true;
   }

   @Override
   public ItemStack removeItemNoUpdate(int slot) {
      if (slot >= 0 && slot < this.itemStacks.size()) {
         ItemStack oldStack = this.itemStacks.get(slot);
         this.setItem(slot, ItemStack.EMPTY);
         return oldStack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Override
   public boolean isEmpty() {
      int i = 0;

      for(int len = this.itemStacks.size(); i < len; ++i) {
         if (!this.itemStacks.get(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   @Override
   public void clearContent() {
   }

   public void markUpdateThisWithMirrorProcess(Player player) {
      this.markUpdateThis();
      if (player != null && this.level != null) {
         setMirrorBlock(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.getJointFacing(), player, false);
      }

   }

   public void markUpdateThis() {
      WorldHelper.markBlockForUpdate(this.level, this.worldPosition);
      this.setChanged();
      this.notiyfyCarvingNeighbour();
   }

   public void notiyfyCarvingNeighbour() {
      if (JBCarvingDataUtil.isCarvingMCube(this.carvingData)) {
         int posX = this.worldPosition.getX();
         int posY = this.worldPosition.getY();
         int posZ = this.worldPosition.getZ();

         for(int y = -1; y <= 1; ++y) {
            for(int x = -1; x <= 1; ++x) {
               for(int z = -1; z <= 1; ++z) {
                  if (x != 0 || y != 0 || z != 0) {
                     int tx = posX + x;
                     int ty = posY + y;
                     int tz = posZ + z;
                     TileEntityJoint teJoint = BlockJointBase.getTileEntityJointerBase(this.level, tx, ty, tz);
                     if (teJoint != null && JBCarvingDataUtil.isCarvingMCube(teJoint.carvingData)) {
                        WorldHelper.markBlockForUpdate(this.level, this.worldPosition);
                        teJoint.setChanged();
                     }
                  }
               }
            }
         }
      }

   }

   public String getTypeName() {
      return this.getBlockState().getBlock().getName().getString();
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindOrFixBindStatus() {
      this.hasMultipleBind = false;
      IJBMotionBinderItem.IJBMotionBinder bindHndl = this.getMotionBinder();
      if (bindHndl == null) {
         bindHndl = BasicMotionBinder.instance;
      }

      IJBMotionBinderItem.IJBMotionHandler motHndl = null;
      int[] bindNumList;
      if (this.isJointTypeBindable((IJBMotionBinderItem.IJBMotionBinder)bindHndl)) {
         if (this.isCurrendBindIncludedInBindableList((IJBMotionBinderItem.IJBMotionBinder)bindHndl)) {
            motHndl = ((IJBMotionBinderItem.IJBMotionBinder)bindHndl).getBindMotionHandler(this.bindNum, this.getMotionItemDamage());
         } else {
            bindNumList = ((IJBMotionBinderItem.IJBMotionBinder)bindHndl).getBindNumberList(this.jointType, this.getMotionItemDamage());
            this.bindNum = bindNumList[0];
            motHndl = ((IJBMotionBinderItem.IJBMotionBinder)bindHndl).getBindMotionHandler(this.bindNum, this.getMotionItemDamage());
         }
      }

      if (motHndl == null) {
         motHndl = this.resetMotionBindStatus();
      } else {
         bindNumList = ((IJBMotionBinderItem.IJBMotionBinder)bindHndl).getBindNumberList(this.getJointType(), this.getMotionItemDamage());
         this.hasMultipleBind = bindNumList != null && bindNumList.length > 1;
      }

      return motHndl;
   }

   public IJBMotionBinderItem.IJBMotionHandler validateBindNumOrReset(IJBMotionBinderItem.IJBMotionBinder bindHndl, boolean chekPrev) {
      int[] bindNumList = bindHndl.getBindNumberList(this.getJointType(), this.getMotionItemDamage());
      if (bindNumList != null && bindNumList.length >= 1) {
         int bindNumShift;
         if (chekPrev) {
            bindNumShift = this.getPrevBindNumFromList(bindNumList, bindHndl);
         } else {
            bindNumShift = this.getNextBindNumFromList(bindNumList, bindHndl);
         }

         if (bindNumShift == -1) {
            return this.resetMotionBindStatus();
         } else {
            this.bindNum = bindNumList[bindNumShift];
            return bindHndl.getBindMotionHandler(this.bindNum, this.getMotionItemDamage());
         }
      } else {
         return this.resetMotionBindStatus();
      }
   }

   private IJBMotionBinderItem.IJBMotionHandler resetMotionBindStatus() {
      this.bindNum = 0;
      return BasicMotionBinder.instance.getBindMotionHandler(this.bindNum, this.getMotionItemDamage());
   }

   private final boolean isJointTypeBindable(IJBMotionBinderItem.IJBMotionBinder bind) {
      int[] bindNumList = bind.getBindNumberList(this.jointType, this.getMotionItemDamage());
      return bindNumList != null && bindNumList.length > 0;
   }

   private final boolean isCurrendBindIncludedInBindableList(IJBMotionBinderItem.IJBMotionBinder bind) {
      int[] bindNumList = bind.getBindNumberList(this.jointType, this.getMotionItemDamage());
      if (bindNumList != null && bindNumList.length != 0) {
         for(int i = 0; i < bindNumList.length; ++i) {
            if (bindNumList[i] == this.bindNum) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private final int getNextBindNumFromList(int[] bindNumList, IJBMotionBinderItem.IJBMotionBinder bind) {
      if (bindNumList == null) {
         return -1;
      } else {
         for(int i = 0; i < bindNumList.length; ++i) {
            if (bindNumList[i] == this.bindNum) {
               ++i;
               return i < bindNumList.length ? i : 0;
            }
         }

         return -1;
      }
   }

   private final int getPrevBindNumFromList(int[] bindNumList, IJBMotionBinderItem.IJBMotionBinder bind) {
      if (bindNumList == null) {
         return -1;
      } else {
         for(int i = 0; i < bindNumList.length; ++i) {
            if (bindNumList[i] == this.bindNum) {
               --i;
               return i >= 0 ? i : bindNumList.length - 1;
            }
         }

         return -1;
      }
   }

   public boolean setJBItemStack(ItemStack itemStack) {
      if (CompatHelper.isStackEmptyOrNull(itemStack)) {
         return false;
      } else {
         Item itemToSet = itemStack.getItem();
         if (itemToSet instanceof IJBModelItem && this.itemStacks.get(0).isEmpty()) {
            this.setItem(0, itemStack.copy().split(1));
            return true;
         } else if (itemToSet instanceof IJBMotionBinderItem && this.itemStacks.get(1).isEmpty() && this.jointType != 0) {
            this.setItem(1, itemStack.copy().split(1));
            return true;
         } else {
            return false;
         }
      }
   }

   public void setInitValuesLayerPrev(Player player) {
      if (JBCommonUtil.jointTypeHasMotion(this.jointType)) {
         if (this.initValuesLayer > 0) {
            --this.initValuesLayer;
         }

      }
   }

   public void setInitValuesLayerNext(Player player) {
      if (JBCommonUtil.jointTypeHasMotion(this.jointType)) {
         if (this.initValuesLayer < JBConfig.maxInitLayers - 1) {
            ++this.initValuesLayer;
         } else {
            player.displayClientMessage(Component.translatable("jbInfo.exceedILLimit", JBConfig.maxInitLayers), true);
         }

         this.ensureInitValuesArySize();
      }
   }

   private void ensureInitValuesArySize() {
      if (this.initValuesLayer >= this.initValues.length) {
         int[][] newArr = new int[this.initValuesLayer + 1][];

         for(int i = 0; i < newArr.length; ++i) {
            if (i < this.initValues.length && this.initValues[i] != null) {
               newArr[i] = Arrays.copyOf(this.initValues[i], 4);
            } else {
               int prevIndex = i - 1;
               if (prevIndex >= 0 && prevIndex < this.initValues.length && this.initValues[prevIndex] != null) {
                  newArr[i] = Arrays.copyOf(this.initValues[prevIndex], 4);
               } else {
                  newArr[i] = Arrays.copyOf(this.initValues[0], 4);
               }
            }
         }

         this.initValues = newArr;
      }

   }

   public boolean isValidCarveable() {
      return this.carvingData != null && JBCarvingDataUtil.validateCarvingData(this.carvingData);
   }

   public void appendCarvingDivide(int type, Player player) {
      if (JBConfig.enableCarving) {
         if (this.isValidCarveable()) {
            int carvingType = JBCarvingDataUtil.getCarvingType(this.carvingData);
            if (carvingType == type) {
               int carvingDiv = JBCarvingDataUtil.getCarvingDivide(this.carvingData);
               this.isCarvingCompiled = false;
               switch (carvingType) {
                  case 2:
                     ++carvingDiv;
                     if (carvingDiv <= JBConfig.maxCarvingDivide && carvingDiv <= 5) {
                        this.carvingData = JBCarvingDataUtil.createCarvingData(type, carvingDiv);
                     } else {
                        this.carvingData = null;
                     }
                     break;
                  case 4:
                     ++carvingDiv;
                     if (carvingDiv <= JBConfig.maxCarvingDivide && carvingDiv <= 5) {
                        this.carvingData = JBCarvingDataUtil.createCarvingData(type, carvingDiv);
                     } else {
                        this.carvingData = null;
                     }
                     break;
                  default:
                     ++carvingDiv;
                     if (carvingDiv > JBConfig.maxCarvingDivide) {
                        this.carvingData = null;
                     } else {
                        this.carvingData = JBCarvingDataUtil.createCarvingData(type, carvingDiv);
                     }
               }

               this.markUpdateForCarvingDataMod(player);
               return;
            }
         }

         if (player.isShiftKeyDown()) {
            this.carvingData = JBCarvingDataUtil.createCarvingData(type, 1);
         } else {
            this.carvingData = JBCarvingDataUtil.createCarvingData(type, 2);
         }

         this.markUpdateForCarvingDataMod(player);
      }
   }

   public void markUpdateForCarvingDataMod(Player player) {
      this.mCubeGatheredData = null;
      this.markUpdateThisWithMirrorProcess(player);
   }

   public static void setMirrorBlock(Level world, int posX, int posY, int posZ, int facingSide, Player player, boolean isBreakBlock) {
      TileEntityJoint teSource = BlockJointBase.getTileEntityJointerBase(world, posX, posY, posZ);
      if (teSource != null && teSource.jointType != 0 && teSource.jointType != 2) {
         if (player != null && player.getInventory() != null && player.getInventory().items != null) {
            boolean foundMirrorItem = false;
            boolean isMirrorLR = false;

            for(int i = 0; i < 9; ++i) {
               if (foundMirrorItem = ItemJBMirrorTool.isMirrorItemLeftRight(player.getInventory().items.get(i))) {
                  isMirrorLR = true;
                  break;
               }

               if (foundMirrorItem = ItemJBMirrorTool.isMirrorItemUpDown(player.getInventory().items.get(i))) {
                  isMirrorLR = false;
                  break;
               }
            }

            if (foundMirrorItem) {
               TileEntityJoint te = teSource.getConnectedSideTileEntityJointed();

               for(int searchedCnt = 0; te != null; te = te.getConnectedSideTileEntityJointed()) {
                  if (searchedCnt != 0 && te == teSource) {
                     return;
                  }

                  ++searchedCnt;
                  if (searchedCnt > 512) {
                     break;
                  }

                  if (te instanceof TileEntityJointCore) {
                     TileEntityJointCore teCore = (TileEntityJointCore)te;
                     int coreFacing = teCore.getJointFacing();
                     doFlipCopy(world, posX, posY, posZ, facingSide, teSource, isMirrorLR, teCore, coreFacing, isBreakBlock);
                     break;
                  }
               }

            }
         }
      }
   }

   private static void doFlipCopy(Level world, int posX, int posY, int posZ, int facingSide, TileEntityJoint teSource, boolean isMirrorLR, TileEntityJointCore teCore, int coreFacing, boolean isBreakBlock) {
      int mirrorX = 0;
      int mirrorY = 0;
      int mirrorZ = 0;
      int flipBy = -1;
      BlockPos corePos = teCore.getBlockPos();
      if (isMirrorLR && (coreFacing == 3 || coreFacing == 2) && posX != corePos.getX()) {
         mirrorX = corePos.getX() - posX + corePos.getX();
         mirrorY = posY;
         mirrorZ = posZ;
         flipBy = 0;
      } else if (isMirrorLR && (coreFacing == 4 || coreFacing == 5) && posZ != corePos.getZ()) {
         mirrorX = posX;
         mirrorY = posY;
         mirrorZ = corePos.getZ() - posZ + corePos.getZ();
         flipBy = 2;
      } else if (!isMirrorLR && posY != corePos.getY()) {
         mirrorX = posX;
         mirrorY = corePos.getY() - posY + corePos.getY();
         mirrorZ = posZ;
         flipBy = 4;
      }

      if (flipBy != -1) {
         TileEntityJoint teSet = BlockJointBase.getTileEntityJointerBase(world, mirrorX, mirrorY, mirrorZ);
         if (isBreakBlock) {
            if (teSet != null) {
               teSet.breakByAssemble = true;
               WorldHelper.setBlockToAir(world, mirrorX, mirrorY, mirrorZ);
            }
         } else {
            if ((teSet == null || teSet.jointType != teSource.jointType) && WorldHelper.setBlock(world, mirrorX, mirrorY, mirrorZ, teSource.getBlockState().getBlock(), 3)) {
               teSet = BlockJointBase.getTileEntityJointerBase(world, mirrorX, mirrorY, mirrorZ);
               teSet.setJointFacing(facingSide);
            }

            if (teSet != null) {
               teSet.flipCopyData(isMirrorLR, flipBy, teSource, mirrorX, mirrorY, mirrorZ, facingSide);
            }
         }
      }

   }

   public void copyDataFrom(TileEntityJoint teSource) {
      CompoundTag tagSrc = teSource.saveWithFullMetadata();
      this.load(tagSrc.copy());
      if (!this.level.isClientSide) {
         this.checkMotionItemChange();
      }

      this.markUpdateThis();
   }

   public void flipCopyData(boolean isMirrorLR, int flipBy, TileEntityJoint teSource, int posX, int posY, int posZ, int facingSide) {
      CompoundTag tagSrc = teSource.saveWithFullMetadata();
      this.load(tagSrc.copy());
      if (flipBy == 0) {
         if (this.connectSide == 4) {
            this.connectSide = 5;
         } else if (this.connectSide == 5) {
            this.connectSide = 4;
         }

         if (this.renderSide == 4) {
            this.renderSide = 5;
         } else if (this.renderSide == 5) {
            this.renderSide = 4;
         }

         this.setJointFacing(JBRotationUtil.convertFacingOnFlip(JBRotationUtil.faceConvertOnflipWE, this.renderSide, facingSide));
         if (this.isValidCarveable()) {
            this.carvingData = JBCarvingDataUtil.flipCarveDataX(this.carvingData);
            this.isCarvingCompiled = false;
         }
      } else if (flipBy == 2) {
         if (this.connectSide == 3) {
            this.connectSide = 2;
         } else if (this.connectSide == 2) {
            this.connectSide = 3;
         }

         if (this.renderSide == 2) {
            this.renderSide = 3;
         } else if (this.renderSide == 3) {
            this.renderSide = 2;
         }

         this.setJointFacing(JBRotationUtil.convertFacingOnFlip(JBRotationUtil.faceConvertOnflipSN, this.renderSide, facingSide));
         if (this.isValidCarveable()) {
            this.carvingData = JBCarvingDataUtil.flipCarveDataX(this.carvingData);
            this.isCarvingCompiled = false;
         }
      } else {
         if (this.connectSide == 0) {
            this.connectSide = 1;
         } else if (this.connectSide == 1) {
            this.connectSide = 0;
         }

         if (this.renderSide == 0) {
            this.renderSide = 1;
         } else if (this.renderSide == 1) {
            this.renderSide = 0;
         }

         this.setJointFacing(JBRotationUtil.convertFacingOnFlip(JBRotationUtil.faceConvertOnflipUD, this.renderSide, facingSide));
         if (this.isValidCarveable()) {
            if (facingSide != 4 && facingSide != 5) {
               if (this.renderSide != 4 && this.renderSide != 5) {
                  this.carvingData = JBCarvingDataUtil.flipCarveDataYofXYReversed(this.carvingData);
               } else {
                  this.carvingData = JBCarvingDataUtil.flipCarveDataY(this.carvingData);
               }
            } else if (this.renderSide != 4 && this.renderSide != 5) {
               this.carvingData = JBCarvingDataUtil.flipCarveDataY(this.carvingData);
            } else {
               this.carvingData = JBCarvingDataUtil.flipCarveDataYofXYReversed(this.carvingData);
            }

            this.isCarvingCompiled = false;
         }
      }

      int flipFlg = isMirrorLR ? 2 : 1;
      EntryFormats.flipDefault(this, flipFlg);
      IJBMotionBinderItem.IJBMotionHandler motHndl = this.getBindOrFixBindStatus();
      if (motHndl != null) {
         motHndl.onJointFlip(this, flipFlg);
      }

      IJBModelItem modelItem = this.getModelItemFromStack();
      if (modelItem != null && modelItem instanceof IJBModelItem.IJBModelItemFlippable) {
         ItemStack prevStack = this.getItem(0);
         if (!CompatHelper.isStackEmptyOrNull(prevStack)) {
            ItemStack newStack = ((IJBModelItem.IJBModelItemFlippable)modelItem).getFlippedStack(this, prevStack, flipFlg);
            this.setJBItemStack(newStack);
         }
      }

      if (!this.level.isClientSide) {
         this.checkMotionItemChange();
      }

      this.markUpdateThis();
   }

   public boolean conductHammerWorks(Direction dir) {
      Level world = this.level;
      if (world != null && !world.isClientSide) {
         try {
            BlockPos teBasePos = this.getBlockPos();
            int basePosX = teBasePos.getX();
            int basePosY = teBasePos.getY();
            int basePosZ = teBasePos.getZ();
            int power = 1;
            int shiftPosX = dir.getOpposite().getNormal().getX() * power;
            int shiftPosY = dir.getOpposite().getNormal().getY() * power;
            int shiftPosZ = dir.getOpposite().getNormal().getZ() * power;
            AssembleStack stack = AssembleStack.tHreadAsembleStack.get().reset();
            AssembleInfo assembleInfo = new AssembleInfo(stack, this.getJointFacing(), basePosX, basePosY, basePosZ);
            assembleInfo.topLevelParts = new JointPartsForCollect(0, 0, 0, 0, 0, 0);
            assembleInfo.ignoreCore = this.jointType != 0;
            assembleInfo.baseCollectDone = false;
            AssembleInfo.collectAllParts(world, basePosX, basePosY, basePosZ, assembleInfo);
            TreeMap<JointPartsForCollect, JointPartsForCollect> sortMap = new TreeMap(JointPartsForCollect.getComparatorByDir(dir));
            JointPartsForCollect existCheckRel = new JointPartsForCollect(0, 0, 0, 0, 0, 0);
            if (assembleInfo.partsCount > 0) {
               JointParts[] refLists = assembleInfo.createRefLists();
               int i = 0;

               int dstZ;
               for(int len = refLists.length; i < len; ++i) {
                  JointPartsForCollect each = (JointPartsForCollect)refLists[i];
                  existCheckRel.xCoordInt = each.xCoordInt + shiftPosX;
                  existCheckRel.yCoordInt = each.yCoordInt + shiftPosY;
                  existCheckRel.zCoordInt = each.zCoordInt + shiftPosZ;
                  JointPartsForCollect exsit = (JointPartsForCollect)assembleInfo.get(existCheckRel);
                  if (exsit != null) {
                     exsit.ignoreDestroy = true;
                     each.dstTe = exsit.te;
                  } else {
                     int dstX = each.absPosX + shiftPosX;
                     int dstY = each.absPosY + shiftPosY;
                     dstZ = each.absPosZ + shiftPosZ;
                     BlockState state = WorldHelper.getBlockState(world, dstX, dstY, dstZ);
                     if (!state.canBeReplaced()) {
                        return false;
                     }
                  }

                  sortMap.put(each, each);
               }

               Iterator var25 = sortMap.keySet().iterator();

               while(var25.hasNext()) {
                  JointPartsForCollect each = (JointPartsForCollect)var25.next();
                  int dstX = each.absPosX + shiftPosX;
                  int dstY = each.absPosY + shiftPosY;
                  dstZ = each.absPosZ + shiftPosZ;
                  BlockPos dstPos = new BlockPos(dstX, dstY, dstZ);
                  TileEntityJoint teSource = each.te;
                  TileEntityJoint teSet = each.dstTe;
                  if (teSet == null || teSet.jointType != teSource.jointType) {
                     if (teSet != null) {
                        teSet.breakByAssemble = true;
                     }

                     if (WorldHelper.setBlock(world, dstPos, teSource.getBlockState().getBlock(), 3)) {
                        teSet = BlockJointBase.getTileEntityJointerBase(world, dstPos);
                     }
                  }

                  if (teSet != null) {
                     teSet.copyDataFrom(teSource);
                     if (!world.isClientSide) {
                        teSet.checkMotionItemChange();
                     }
                  }

                  if (!each.ignoreDestroy) {
                     each.te.breakByAssemble = true;
                     WorldHelper.setBlockToAir(world, each.absPosX, each.absPosY, each.absPosZ);
                  }
               }
            }

            return true;
         } catch (Exception var24) {
            var24.printStackTrace();
            return false;
         }
      } else {
         return false;
      }
   }

   public void conductMirrorCopyAllChildes(int mirrorCopyType) {
      TileEntityJointCore teCore = null;
      TileEntityJoint te;
      if (this instanceof TileEntityJointCore) {
         teCore = (TileEntityJointCore)this;
      } else {
         te = this.getConnectedSideTileEntityJointed();

         for(int searchedCnt = 0; te != null; te = te.getConnectedSideTileEntityJointed()) {
            if (searchedCnt != 0 && te == this) {
               return;
            }

            ++searchedCnt;
            if (searchedCnt > 512) {
               break;
            }

            if (te instanceof TileEntityJointCore) {
               teCore = (TileEntityJointCore)te;
               break;
            }
         }
      }

      if (teCore != null) {
         Level world = this.level;
         if (world != null) {
            BlockPos teCorePos = teCore.getBlockPos();
            int corePosX = teCorePos.getX();
            int corePosY = teCorePos.getY();
            int corePosZ = teCorePos.getZ();
            int coreFacing = teCore.getJointFacing();
            AssembleStack stack = AssembleStack.tHreadAsembleStack.get().reset();
            AssembleInfo assembleInfo = new AssembleInfo(stack, coreFacing, corePosX, corePosY, corePosZ);
            assembleInfo.ignoreAddon = true;
            AssembleInfo.assembleAllParts(world, corePosX, corePosY, corePosZ, assembleInfo);
            if (assembleInfo.partsCount > 0) {
               JointParts[] refLists = assembleInfo.createRefLists();
               boolean isMirrorLR = mirrorCopyType == 10 || mirrorCopyType == 20;
               boolean mirrorStarted = false;
               int i = 0;

               for(int len = refLists.length; i < len; ++i) {
                  JointParts each = refLists[i];
                  if (each != null) {
                     if (this == (te = each.getTileEntityFromCoord(world, coreFacing, corePosX, corePosY, corePosZ))) {
                        mirrorStarted = true;
                        each.paramModifyDirFlg = true;
                     }

                     if (mirrorStarted && te != null && each.paramModifyDirFlg) {
                        BlockPos tePos = te.getBlockPos();
                        boolean markChildAndSkip = false;
                        int c;
                        switch (mirrorCopyType) {
                           case 10:
                              if (each.xCoord < 0) {
                                 continue;
                              }

                              if (each.xCoord == 0) {
                                 markChildAndSkip = true;
                              }
                              break;
                           case 20:
                              if (each.xCoord > 0) {
                                 continue;
                              }

                              if (each.xCoord == 0) {
                                 markChildAndSkip = true;
                              }
                              break;
                           case 30:
                              if (each.yCoord > 0) {
                                 continue;
                              }

                              if (each.yCoord == 0) {
                                 markChildAndSkip = true;
                              }
                              break;
                           case 40:
                              if (each.yCoord < 0) {
                                 continue;
                              }

                              if (each.yCoord == 0) {
                                 markChildAndSkip = true;
                              }
                              break;
                           case 60:
                              for(c = 0; c < 6; ++c) {
                                 if (each.child[c] != null) {
                                    each.child[c].paramModifyDirFlg = true;
                                 }
                              }

                              te.breakByAssemble = true;
                              WorldHelper.setBlockToAir(world, tePos);
                              continue;
                        }

                        if (!markChildAndSkip && each.jointType == 0) {
                           markChildAndSkip = true;
                        }

                        for(c = 0; c < 6; ++c) {
                           if (each.child[c] != null) {
                              each.child[c].paramModifyDirFlg = true;
                           }
                        }

                        if (!markChildAndSkip && each.jointType != 0 && each.jointType != 2) {
                           doFlipCopy(world, tePos.getX(), tePos.getY(), tePos.getZ(), te.getJointFacing(), te, isMirrorLR, teCore, coreFacing, false);
                        }
                     }
                  }
               }
            }

         }
      }
   }

   public void copyValuesToParamCpy(Player player, ItemStack stack) {
      CompoundTag tag = stack.getTag();
      if (tag == null) {
         stack.setTag(tag = new CompoundTag());
      }

      CompoundTag subTag = new CompoundTag();
      int[] iAry = new int[this.initValues.length * 4];
      int i = 0;

      for(int ti = 0; i < this.initValues.length; ++i) {
         for(int j = 0; j < this.initValues[i].length; ++j) {
            iAry[ti++] = this.initValues[i][j];
         }
      }

      subTag.putInt("jtyp", this.jointType);
      subTag.putIntArray("ivary", iAry);
      subTag.putInt("motid", this.getMotionBinderItemId());
      subTag.putInt("motbnd", this.bindNum);
      subTag.putInt("imeta", this.getMotionItemDamage());
      subTag.putIntArray("pmary", this.params);
      subTag.putBoolean("docint", true);
      subTag.putBoolean("docmtn", true);
      subTag.putInt("extf", this.extraJointStateFlags);
      subTag.putBoolean("docext", true);
      tag.put("jb:prmcpy", subTag);
      JBItemUtils.playDataRegistrationSound(player);
   }

   private boolean checkCpyDstType(int cpySrcType) {
      return this.jointType != 0 && this.jointType == cpySrcType;
   }

   public boolean copyValueFromParamCpy(Player player, ItemStack itemStack, int flags) {
      CompoundTag tag = ItemJBCopyTool.getCopyValueTag(itemStack);
      if (tag == null) {
         return false;
      } else {
         boolean updated = false;
         JBGuiSettingsImpl gs = null;
         tag.putBoolean("docint", false);
         tag.putBoolean("docmtn", false);
         tag.putBoolean("docext", false);
         if (flags == 0) {
            return false;
         } else {
            int srcBindId;
            if ((flags & 2) != 0) {
               tag.putBoolean("docint", true);
               if (this.checkCpyDstType(tag.getInt("jtyp"))) {
                  if (gs == null) {
                     gs = new JBGuiSettingsImpl();
                  }

                  EntryFormats.setDefaultJointTypeButtonState(gs, true, this.jointType);
                  int[] iAry = tag.getIntArray("ivary");
                  if (iAry != null && iAry.length != 0 && iAry.length % 4 == 0) {
                     srcBindId = iAry.length / 4;
                     int[][] newInitVals = new int[srcBindId][];

                     for(int i = 0; i < iAry.length; ++i) {
                        int layerIdx = i / 4;
                        int valueIdx = i % 4;
                        if (newInitVals[layerIdx] == null) {
                           newInitVals[layerIdx] = new int[4];
                        }

                        if (gs.entrySettings[valueIdx].enabled) {
                           newInitVals[layerIdx][valueIdx] = JBGuiSettingsImpl.trySetValue((long)iAry[i], gs.entrySettings[valueIdx].min, gs.entrySettings[valueIdx].max);
                        }
                     }

                     this.initValues = newInitVals;
                     updated = true;
                  }
               }
            }

            int srcMotnId;
            if ((flags & 1) != 0) {
               tag.putBoolean("docmtn", true);
               if (this.checkCpyDstType(tag.getInt("jtyp"))) {
                  if (gs == null) {
                     gs = new JBGuiSettingsImpl();
                  } else {
                     gs.resetAll();
                  }

                  srcMotnId = tag.getInt("motid");
                  srcBindId = tag.getInt("motbnd");
                  int srcMetaId = tag.getInt("imeta");
                  int[] iAry = tag.getIntArray("pmary");
                  if (iAry != null && iAry.length == 6) {
                     IJBMotionBinderItem.IJBMotionBinder bindHndl = null;
                     Item item = null;
                     if (srcMotnId == 0) {
                        bindHndl = BasicMotionBinder.instance;
                     } else {
                        item = Item.byId(srcMotnId);
                        if (item != null && item instanceof IJBMotionBinderItem) {
                           bindHndl = ((IJBMotionBinderItem)item).getMotionBinder();
                        }
                     }

                     IJBMotionBinderItem.IJBMotionHandler motHndl = null;
                     if (bindHndl != null) {
                        motHndl = ((IJBMotionBinderItem.IJBMotionBinder)bindHndl).getBindMotionHandler(srcBindId, srcMetaId);
                     }

                     if (motHndl != null) {
                        motHndl.setGuiEntryState(this.jointType, gs);
                        int[] newParams = new int[6];
                        motHndl.setParamsInitialValue(this.jointType, newParams);

                        for(int i = 0; i < 6; ++i) {
                           if (gs.entrySettings[i].enabled) {
                              newParams[i] = JBGuiSettingsImpl.trySetValue((long)iAry[i], gs.entrySettings[i].min, gs.entrySettings[i].max);
                           }
                        }

                        if (item != null) {
                           ItemStack motionStack = new ItemStack(item);
                           motionStack.setDamageValue(srcMetaId);
                           this.itemStacks.set(1, motionStack);
                        } else {
                           this.itemStacks.set(1, ItemStack.EMPTY);
                        }

                        this.bindNum = srcBindId;
                        this.params = newParams;
                        this.prevMotionItemId = this.getMotionBinderItemId();
                        this.prevBindNum = this.bindNum;
                        this.prevMotionItemDamage = this.getMotionItemDamage();
                        updated = true;
                     }
                  }
               }
            }

            if ((flags & 4) != 0) {
               tag.putBoolean("docext", true);
               srcMotnId = tag.getInt("extf") & -65536;
               srcBindId = -503316480;
               this.extraJointStateFlags &= ~srcBindId;
               srcMotnId &= srcBindId;
               this.extraJointStateFlags |= srcMotnId;
               updated = true;
            }

            if (updated) {
               if (!this.level.isClientSide) {
                  this.markUpdateThisWithMirrorProcess(player);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   public static String getJointTypeName(int jointType) {
      switch (jointType) {
         case 0:
            return "tile.joint_core.name";
         case 1:
         case 3:
         case 5:
         case 6:
         case 7:
         case 9:
         case 11:
         case 13:
         default:
            return "";
         case 2:
            return "tile.joint_seat.name";
         case 4:
            return "tile.joint_normal.name";
         case 8:
            return "tile.joint_rotator.name";
         case 10:
            return "tile.joint_extender.name";
         case 12:
            return "tile.joint_scaler.name";
         case 14:
            return "tile.joint_slider.name";
         case 15:
            return "tile.joint_attracter.name";
      }
   }
}
