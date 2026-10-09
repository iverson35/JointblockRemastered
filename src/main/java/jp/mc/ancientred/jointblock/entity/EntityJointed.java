package jp.mc.ancientred.jointblock.entity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.AddonBonus;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.assemble.DisassembleHelper;
import jp.mc.ancientred.jointblock.assemble.EjSensor;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.IJointListsHolder;
import jp.mc.ancientred.jointblock.assemble.JointDataSerializer;
import jp.mc.ancientred.jointblock.assemble.JointListsHolderImpl;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.assemble.Quaternion;
import jp.mc.ancientred.jointblock.assemble.SimpleUpdatedJointList;
import jp.mc.ancientred.jointblock.assemble.ai.JBPathNavigate;
import jp.mc.ancientred.jointblock.assemble.ring.ActiveRingSlots;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgram;
import jp.mc.ancientred.jointblock.assemble.tuning.TuningCommand;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CommonProxy;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.TypedLinkable;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.masterslave.AbstractSlaveAttatcher;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlaveControl;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.item.ItemJBIFFTag;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import jp.mc.ancientred.jointblock.unit.UnitAbility;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.ReportedException;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.entity.PartEntity;

/**
 * Ported from 1.12.2. Key API migrations:
 * - IEntityMultiPart -> {@link net.minecraftforge.common.extensions.IForgeEntity} multipart
 *   ({@link #isMultipartEntity()} / {@link #getParts()}) with {@link PartEntity} seat parts.
 * - IEntityAdditionalSpawnData retained (Forge 1.20.1 still provides it; FriendlyByteBuf).
 * - StarMiner IZeroGravityWorldProvider checks removed (always false in this port).
 * - {@code shouldRenderInPass} (1.12.2 Forge render-pass hook) has no 1.20.1 counterpart - deferred Phase 4.
 * - The MAX_HEALTH custom attribute-instance wrapper is replaced by the registered MAX_ENERGY/MAX_HEALTH
 *   attributes (LivingEntity#getMaxHealth() is final in 1.20.1 and reads the attribute).
 */
public class EntityJointed extends LivingEntity implements IEntityAdditionalSpawnData, IJBEntityState, IJointListsHolder, JointInventory.IJointInventoryHolder, TypedLinkable {
   public static final Attribute MAX_ENERGY = new RangedAttribute("attribute.jointblock.max_energy", 20.0, 0.0, Double.MAX_VALUE).setSyncable(true);
   public static final int INIT_TICK_WITH_MERGIN = 60;
   public static final int INIT_TICK = 30;
   public static final int DISSASSAMBLE_TICK_WITH_MERGIN = 60;
   public static final int DISSASSAMBLE_TICK = 30;
   protected static final EntityDataAccessor<Byte> DW_FLAGS_NUM = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Float> DW_ENERGY_NUM = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Byte> DW_INIT_LAYER = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Integer> DW_SMSLOT_FLG = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> DW_EPOOL_NUM = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> DW_ECACHE_NUM = SynchedEntityData.defineId(EntityJointed.class, EntityDataSerializers.FLOAT);
   private static final double IMMIDIATE_BOARD_RANGE = 5.5;
   private static final byte JOINT_DATA_FORMAT_VER_1_0 = 10;
   private static final float LARGEWEIGHT_YARDSTICK = 5.0F;
   public boolean hasInitEnd;
   public int initTick = 0;
   public int coreFacing;
   public int partsCount;
   public JointParts jointParts;
   public JointParts[] refLists;
   public JointParts[] motionRefLists;
   public SimpleUpdatedJointList updateList = new SimpleUpdatedJointList();
   public String unitName;
   public int ownerHash;
   public UUID ownerUUID;
   public long iffTagId;
   public long msbTagId;
   public byte unitId;
   public byte mainUnitId;
   public AddonBonus addonBonus;
   public UnitAbility unitAbility = new UnitAbility();
   public short maxJBHealth;
   public short maxJBEnergy;
   public float spinningRate;
   public int launchTick;
   public float prevCoreHight = 1.0F;
   public float coreHeight = 1.0F;
   public float totalHeight = 1.0F;
   public float largeUnitWeight = 1.0F;
   public short largeUnitWeightFixed = 100;
   public float preDASavedCoreHeight;
   public int minYSavedForDisassemble;
   private static final String[] PLAYER_PASSENGER_KEY;
   private static final String[] PLAYER_PASSENGER_MOST_KEY;
   public byte seatFlags;
   public EntitySeatPart[] entitySeatPartArray;
   public EntitySeatPart entitySeatPart0;
   public EntitySeatPart entitySeatPart1;
   public EntitySeatPart entitySeatPart2;
   public EntitySeatPart entitySeatPart3;
   public ArrayList<Entity> passengersList = new ArrayList<Entity>();
   public LinkedList<Byte> seatReMountArray;
   public boolean isReInit;
   public boolean disassemble;
   public int disassembleTick;
   private int disassembleFacing;
   private float disassembleRotationYaw;
   public double dissassemblePosX;
   public double dissassemblePosY;
   public double dissassemblePosZ;
   private Player disassembleConductor;
   public int destructCount;
   public int destructNeed;
   public boolean playDestructSound;
   private int nextStepDistancePrivate = 1;
   private float lastSentRotationYaw;
   private float rotationYawReceived = Float.MAX_VALUE;
   private boolean hasReceivedFirstPosRow = false;
   private int commonInputReserveTick = 0;
   public float stepSoundCycle;
   public float prevLimbSwing;
   public EffectSlot[] effectSlots = new EffectSlot[16];
   public int[][] paletteDataList = new int[6][];
   public int[][] boFixedPaletteDataList = new int[6][];
   public int assembleFlags;
   public Player boardImmidiatePlayer;
   public Object iffTagString;
   public byte boardViewFlags;
   private int needAttracterSyncTick;
   private static final int REQUEST_ATTRACTER_SYNC_SPAN = 10;
   private static final int REQUEST_ATTRACTER_SYNC_TOTAL = 50;
   private static final int ALWAYS_ATTRACTER_SYNC_SPAN = 200;
   public AbstractSlaveAttatcher slaveAttatcher;
   public MasterSlot[] masterSlotHolderArray;
   public int slaveSlotDelay;
   public int[] slaveSlotToggleDelay;
   private static final int SLAVE_SLOT_UPDATE_DELAY = 20;
   private int forceFindMasterSearchTick;
   public boolean reverseSelectedWireframe = false;
   public boolean isClientPlayerOnBoard;
   public boolean isTunbleEntityOnClient;
   public JointInventory innerChest;
   public RingProgram[] ringPrograms;
   private int createdTuningCommandIntickCount;
   private static final int ACCEPTABLE_TUNINGS_IN_TICK = 5;
   private TuningCommand[] tuningCommandList;
   public ExFlyingEntityAttributes exFlyAttr;
   public boolean syncUpdateCoreRotation;
   private boolean fixPositionYEpsilon;
   /** 1.12 Entity.field_70144_Y (entityCollisionReduction) was removed in 1.20; kept mod-owned. */
   public float entityCollisionReduction = 0.0F;
   public float savedPrevRotationYaw;
   public Object prebuilder;
   public ActiveRingSlots activeRingSlots;
   private JBPathNavigate navigator;
   private DisassembleHelper disassembleHelper;
   public static final float INTERFERE_BB_WIDTH = 4.0F;
   public static final double MOVE_COLLIDE_WIDTH = 0.4;
   public boolean isInLava = false;
   public Entity entityCamera;
   public int brightnessTemp;
   public boolean isDelayedUpdate = false;
   private static final int FLGBIT_JUMPING = 1;
   private static final int FLGBIT_FLYING = 2;
   private static final int FLGBIT_DASHING = 4;
   private static final int FLGBIT_AB_HOVER = 8;
   private static final int FLGBIT_CORESUBCOLOR = 16;
   public static final int TOGGLE_MAN_SUB_STILL = 0;
   public static final int TOGGLE_MAN_SUB_MAIN = 1;
   public static final int TOGGLE_MAN_SUB_SUB = 2;
   public static final int FORCE_FPS_VIEW = 128;
   public static final int FORCE_TPSV_FAR = 64;
   public static final int TPSV_FAR_LEVEL = 15;
   public static final int MAX_TARGET_CNT = 6;
   public static final int MAX_SENSOR_CNT = 11;
   public EjSensor[] sensorList = new EjSensor[11];
   public EjTarget[] targetList = new EjTarget[6];
   public boolean needToSendTargetUpdate = false;
   private TypedLinkable next;
   private TypedLinkable prev;
   private long renderCheck = -1L;

   public EntityJointed(EntityType<? extends EntityJointed> type, Level level) {
      super(type, level);
      this.fixPositionYEpsilon = level.isClientSide;
      this.entityCollisionReduction = 0.7F;
      this.entitySeatPartArray = new EntitySeatPart[]{
         this.entitySeatPart0 = new EntitySeatPart(this, 0, 0.5F, 0.5F),
         this.entitySeatPart1 = new EntitySeatPart(this, 1, 0.5F, 0.5F),
         this.entitySeatPart2 = new EntitySeatPart(this, 2, 0.5F, 0.5F),
         this.entitySeatPart3 = new EntitySeatPart(this, 3, 0.5F, 0.5F)};
      this.noCulling = true;
      this.createInventory();
   }

   public EntityJointed(Level level) {
      this(JBEntityTypes.JOINTED, level);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DW_FLAGS_NUM, (byte)0);
      this.getEntityData().define(DW_ENERGY_NUM, 1.0F);
      this.getEntityData().define(DW_INIT_LAYER, (byte)0);
      this.getEntityData().define(DW_SMSLOT_FLG, 0);
      this.getEntityData().define(DW_EPOOL_NUM, 1.0F);
      this.getEntityData().define(DW_ECACHE_NUM, 0.0F);
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return EntityDimensions.scalable(INTERFERE_BB_WIDTH, this.totalHeight);
   }

   @Override
   public void moveTo(double posX, double posY, double posZ, float yaw, float pitch) {
      if (this.fixPositionYEpsilon) {
         posY += 0.015625;
      }
      super.moveTo(posX, posY, posZ, yaw, pitch);
   }

   @Override
   public void remove(Entity.RemovalReason reason) {
      super.remove(reason);
      try {
         for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
            this.entitySeatPartArray[i].discard();
         }
      } catch (Exception var2) {
      }
   }

   @Override
   public boolean startRiding(Entity entityIn, boolean force) {
      return false;
   }

   @Override
   public Entity getVehicle() {
      return this.isMasterConnected() ? this.getMasterIfConnected() : null;
   }

   @Override
   public void stopRiding() {
   }

   @Override
   public void onAddedToWorld() {
      super.onAddedToWorld();
      if (!this.level().isClientSide) {
         // 1.12 spawned the seat parts into the world here. Seat parts are now Forge
         // PartEntity objects tracked by their parent (never registered/spawned), so
         // only their ids matter and no spawn call is required.
         for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
            if ((this.seatFlags & 1 << i) != 0) {
               this.entitySeatPartArray[i].setId(this.getId() + 1 + i);
            }
         }
      } else {
         JBModContainer.proxy.onEntityJBJoinedWorld(this, this.level());
      }
   }

   @Override
   protected boolean canAddPassenger(Entity passenger) {
      return false;
   }

   @Override
   protected void addPassenger(Entity passenger) {
   }

   @Override
   protected void removePassenger(Entity passenger) {
   }

   // 1.12.2 override of Entity#updatePassenger(func_184232_k) was empty (seats position their riders).
   // 1.20.1 Entity#positionRider(Entity) is final; the empty override goes on the 2-arg variant.
   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
   }

   // NOTE: 1.12.2 overrode getPassengers()/func_184188_bt to return an empty list; 1.20.1
   // Entity#getPassengers() is final. addPassenger/removePassenger stay no-ops, so the vanilla
   // passenger list is already always empty for this entity.

   @Override
   public boolean hasPassenger(Entity entityIn) {
      return false;
   }

   @Override
   public boolean isMultipartEntity() {
      return true;
   }

   @Override
   public PartEntity<?>[] getParts() {
      return this.entitySeatPartArray;
   }

   public boolean isPlayerUnitOwner(Player entityPlayer) {
      if (entityPlayer == null) {
         return false;
      }
      int idHashPlayer = JBCommonUtil.getUUIDHash(entityPlayer);
      return idHashPlayer == this.ownerHash;
   }

   public static EntityJointed asEntityJointed(Entity entity) {
      return entity != null && entity instanceof EntityJointed ? (EntityJointed)entity : null;
   }

   public static EntityJointed getRidingEntityJB(Entity entity) {
      if (entity instanceof Player) {
         Entity ridingEntity = entity.getVehicle();
         if (ridingEntity instanceof EntitySeatPart) {
            return ((EntitySeatPart)ridingEntity).entityJointed;
         }
      }
      return null;
   }

   /**
    * 1.12.2 supplied these through a custom getEntityAttribute override. In 1.20.1 the values must
    * come from a registered attribute supplier; wire this through Forge's
    * {@code EntityAttributeCreationEvent} for {@code JBEntityTypes.JOINTED}.
    */
   public static AttributeSupplier.Builder createAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.MAX_HEALTH, 20.0)
         .add(MAX_ENERGY, 20.0);
   }

   public void setupAttributeFromUnitAblility() {
      if (this.maxJBHealth <= 10) {
         this.maxJBHealth = 10;
      }
      if (this.maxJBEnergy <= 10) {
         this.maxJBEnergy = 10;
      }
      AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
      if (maxHealth != null) {
         maxHealth.setBaseValue((double)this.maxJBHealth);
         // 1.20.1 LivingEntity#getMaxHealth() is final and reads this attribute, so health is only
         // clamped/set once the supplier is registered.
         this.setHealth((float)this.maxJBHealth);
      }
      AttributeInstance maxEnergy = this.getAttribute(MAX_ENERGY);
      if (maxEnergy != null) {
         maxEnergy.setBaseValue((double)this.maxJBEnergy);
      }
      this.setEnergy((float)this.maxJBEnergy);
   }

   @OnlyIn(Dist.CLIENT)
   public String getJointEntityName() {
      return this.unitName != null && this.unitName.length() > 0 ? this.unitName : CompatHelper.formatLang("jointGui.info.nonameunit");
   }

   @Override
   protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
      return this.coreHeight;
   }

   public JBPathNavigate getJBNavigator() {
      if (this.navigator == null) {
         this.navigator = new JBPathNavigate(this);
      }
      return this.navigator;
   }

   public float getEnergy() {
      return (Float)this.getEntityData().get(DW_ENERGY_NUM);
   }

   public void setEnergy(float value) {
      this.getEntityData().set(DW_ENERGY_NUM, Mth.clamp(value, 0.0F, this.getMaxEnergy()));
   }

   public float getMaxEnergy() {
      AttributeInstance inst = this.getAttribute(MAX_ENERGY);
      return inst == null ? (float)this.maxJBEnergy : (float)inst.getValue();
   }

   @Override
   public byte getInitLayerMode() {
      return (Byte)this.getEntityData().get(DW_INIT_LAYER);
   }

   protected void setInitLayerMode(byte value) {
      this.getEntityData().set(DW_INIT_LAYER, value);
   }

   @Override
   public int getSlaveSlotNum() {
      return (Integer)this.getEntityData().get(DW_SMSLOT_FLG) & '\uffff';
   }

   public void setSlaveSlotNum(int value) {
      int fix = (Integer)this.getEntityData().get(DW_SMSLOT_FLG);
      fix &= -65536;
      fix |= '\uffff' & value;
      this.getEntityData().set(DW_SMSLOT_FLG, fix);
   }

   public int getSlaveMasterFlags() {
      return (Integer)this.getEntityData().get(DW_SMSLOT_FLG);
   }

   public void setSlaveMasterFlags(int value) {
      this.getEntityData().set(DW_SMSLOT_FLG, value);
   }

   public float getEFuelPool() {
      return (Float)this.getEntityData().get(DW_EPOOL_NUM);
   }

   public void setEFuelPool(float value) {
      this.getEntityData().set(DW_EPOOL_NUM, Math.max(value, 0.0F));
   }

   public float getEFuelPoolCache() {
      return (Float)this.getEntityData().get(DW_ECACHE_NUM);
   }

   public void setEFuelPoolCache(float value) {
      this.getEntityData().set(DW_ECACHE_NUM, Math.max(value, 0.0F));
   }

   private void useEFuelPool(float useValue) {
      this.setEFuelPool(this.getEFuelPool() - useValue);
      this.innerChest.consumespendFuelUntil(this.getEFuelPool());
   }

   @Override
   public boolean isEntityJumping() {
      return ((Byte)this.getEntityData().get(DW_FLAGS_NUM) & FLGBIT_JUMPING) != 0;
   }

   public void setEntityJumping(boolean value) {
      byte flags = (Byte)this.getEntityData().get(DW_FLAGS_NUM);
      // 1.12 mirrored these flags into Entity.isAirBorne; that field no longer exists in 1.20.
      this.getEntityData().set(DW_FLAGS_NUM, (byte)(value ? flags | FLGBIT_JUMPING : flags & ~FLGBIT_JUMPING));
   }

   @Override
   public boolean isEntityFlying() {
      return ((Byte)this.getEntityData().get(DW_FLAGS_NUM) & FLGBIT_FLYING) != 0;
   }

   public void setEntityFlying(boolean value) {
      byte flags = (Byte)this.getEntityData().get(DW_FLAGS_NUM);
      this.getEntityData().set(DW_FLAGS_NUM, (byte)(value ? flags | FLGBIT_FLYING : flags & ~FLGBIT_FLYING));
   }

   @Override
   public boolean isEntityDashing() {
      return ((Byte)this.getEntityData().get(DW_FLAGS_NUM) & FLGBIT_DASHING) != 0;
   }

   public void setEntityDashing(boolean value) {
      byte flags = (Byte)this.getEntityData().get(DW_FLAGS_NUM);
      this.getEntityData().set(DW_FLAGS_NUM, (byte)(value ? flags | FLGBIT_DASHING : flags & ~FLGBIT_DASHING));
   }

   public boolean isAirBorneHovering() {
      return ((Byte)this.getEntityData().get(DW_FLAGS_NUM) & FLGBIT_AB_HOVER) != 0;
   }

   public void setAirBorneHovering(boolean value) {
      byte flags = (Byte)this.getEntityData().get(DW_FLAGS_NUM);
      this.getEntityData().set(DW_FLAGS_NUM, (byte)(value ? flags | FLGBIT_AB_HOVER : flags & ~FLGBIT_AB_HOVER));
   }

   public boolean isUnderMasterControl() {
      return this.slaveAttatcher != null && this.slaveAttatcher.isUnderMasterControl();
   }

   public boolean isCoreSubColor() {
      return ((Byte)this.getEntityData().get(DW_FLAGS_NUM) & FLGBIT_CORESUBCOLOR) != 0;
   }

   public void setCoreSubColor(boolean value) {
      byte flags = (Byte)this.getEntityData().get(DW_FLAGS_NUM);
      this.getEntityData().set(DW_FLAGS_NUM, (byte)(value ? flags | FLGBIT_CORESUBCOLOR : flags & ~FLGBIT_CORESUBCOLOR));
   }

   protected double getJumpPower(float mul) {
      double a = 4.903;
      double b = 0.09;
      double c = -0.055;
      double y = (double)(this.unitAbility.jumpPower * this.coreHeight + 0.5F);
      double xT = Math.sqrt(b * b - 4.0 * a * (c - y));
      double xA = (-b + xT) / (2.0 * a);
      double xB = (-b - xT) / (2.0 * a);
      xA = Math.max(xA, xB);
      return !Double.isNaN(xA) && !Double.isInfinite(xA) && !(xA < 0.0) ? xA : 0.0;
   }

   @Override
   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   @Override
   public Iterable<ItemStack> getArmorSlots() {
      return Collections.emptyList();
   }

   @Override
   public ItemStack getItemBySlot(EquipmentSlot slotIn) {
      return ItemStack.EMPTY;
   }

   @Override
   public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
   }

   public boolean isOnInitPhase() {
      return this.initTick <= INIT_TICK_WITH_MERGIN && !this.hasInitEnd;
   }

   @Override
   public void tick() {
      // The seat parts are PartEntity children and are never in the level, so nothing calls
      // setOldPosAndRot on them and their xOld/yOld/zOld would stay 0 forever - Forge renders the
      // F3+B part boxes (and any other part interpolation) from xOld, which made them sweep out of
      // (0,0,0) every frame. The level does this for real entities in tickNonPassenger; the parent
      // is the ticker here, and it must happen before checkSeatUpdated moves the parts.
      if (this.entitySeatPartArray != null) {
         for (int seatIndex = 0; seatIndex < this.entitySeatPartArray.length; ++seatIndex) {
            EntitySeatPart seatPart = this.entitySeatPartArray[seatIndex];
            if (seatPart != null) {
               seatPart.setOldPosAndRot();
            }
         }
      }

      if (this.getMasterEntityIfConnected() != this && this.slaveAttatcher != null) {
         if (this.isDelayedUpdate) {
            super.tick();
         } else {
            this.slaveAttatcher.savePrevValues(this);
            if (this.level().isClientSide) {
               CommonProxy.delayedListClient.add(this);
            } else {
               CommonProxy.delayedListServer.add(this);
            }
         }
      } else {
         super.tick();
      }
      // 1.12.2 spawned the EntitySeatPart children into the world (World.spawnEntity) so the world
      // ticked them. PartEntity children are not in the level in 1.20.1, so the parent drives them.
      if (this.entitySeatPartArray != null) {
         for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
            EntitySeatPart seatPart = this.entitySeatPartArray[i];
            if (seatPart != null) {
               seatPart.tick();
            }
         }
      }
   }

   @Override
   public void aiStep() {
      if (!this.level().isClientSide && !this.isOnInitPhase() && !this.disassemble) {
         this.savedPrevRotationYaw = this.getYRot();
         this.updateTargetList();
         this.entitySeatPart0.resetMoveDR();
         this.processRingChannels();
      }

      if ((this.tickCount & 31) == 0) {
         this.updateTargetOwner();
      }

      if (this.level().isClientSide && this.exFlyAttr != null) {
         this.exFlyAttr.prevCoreRotQuats.setFrom(this.exFlyAttr.coreRotQuats);
         synchronized (this.exFlyAttr) {
            if (this.exFlyAttr.updateInc > 0) {
               Quaternion.slerp(1.0F / (float)this.exFlyAttr.updateInc, this.exFlyAttr.coreRotQuats, this.exFlyAttr.updatedCoreRotQuats, this.exFlyAttr.workCoreRotQuats);
               this.exFlyAttr.coreRotQuats.setFrom(this.exFlyAttr.workCoreRotQuats);
               --this.exFlyAttr.updateInc;
            }
         }
      }

      super.aiStep();
      boolean isGravityZero = false; // StarMiner zero-gravity interop removed
      if (this.unitAbility.isExFlyable() && this.exFlyAttr == null) {
         this.exFlyAttr = new ExFlyingEntityAttributes(this.getYRot());
         this.syncUpdateCoreRotation = true;
      }

      this.prevCoreHight = this.coreHeight;
      int createdTuningCommand = this.createdTuningCommandIntickCount;
      this.createdTuningCommandIntickCount = 0;
      if (this.isRemoved()) {
         this.ensureRiddenRelease();
      }

      boolean addCoreSyncMessage;
      float newCoreHeight;
      float newTotalHeight;
      float newLargeUnitWeight;
      if (!this.level().isClientSide && this.isEnergyHealTick()) {
         float max = this.getMaxEnergy();
         addCoreSyncMessage = this.isMasterConnected();
         if (!isGravityZero && this.unitAbility.isFloatOnly() && !this.isInWater()) {
            this.useEntityEnergy(max / 20.0F);
         } else if (isGravityZero || !this.unitAbility.isExFlyable() || addCoreSyncMessage || this.onGround()) {
            if (addCoreSyncMessage) {
               max *= JBConfig.slaveModeEnergyHealMult;
            }
            newCoreHeight = max / 20.0F;
            if (AssembleFlagsUtil.isInfiniteFuel(this.assembleFlags)) {
               this.setEnergy(this.getEnergy() + newCoreHeight);
            } else {
               newTotalHeight = this.getMaxEnergy() - this.getEnergy();
               if (newTotalHeight < newCoreHeight) {
                  newCoreHeight = newTotalHeight;
               }
               newLargeUnitWeight = this.getEFuelPool();
               if (newLargeUnitWeight < newCoreHeight) {
                  newCoreHeight = newLargeUnitWeight;
               }
               if (newCoreHeight > 0.0F) {
                  this.useEFuelPool(newCoreHeight);
                  this.setEnergy(this.getEnergy() + newCoreHeight);
               }
            }
         }
      }

      if (!this.level().isClientSide) {
         this.createRingPrograms();
         if (this.getEFuelPoolCache() != this.innerChest.getConsumedCashe()) {
            this.setEFuelPoolCache(this.innerChest.getConsumedCashe());
         }
      }

      if (this.jointParts != null && !this.isRemoved()) {
         if (this.disassemble && this.updateDisassemble()) {
            return;
         }

         this.resetSeatUpdated();
         if (this.hasInitEnd && !this.disassemble) {
            if (!this.level().isClientSide) {
               this.searchForMasterTargetAtServer();
            }
            if (this.slaveAttatcher != null) {
               this.updateSlaveState();
            }
            this.updateMasterState();
         }

         ModelViewMatrix matrix = this.setupOwnMatrix();
         if (!this.level().isClientSide) {
            this.updateList.clear();
         }

         JointParts.updateAllParts(this, matrix);
         if (createdTuningCommand > 0 && this.tuningCommandList != null) {
            for (int i = 0; i < createdTuningCommand; ++i) {
               if (this.tuningCommandList[i] != null && this.tuningCommandList[i].isCommandAccepted && this.tuningCommandList[i].validaeAndCommitTuning(this)) {
                  JBPacketSender.sendAssemblyTuningActionToPlayers(this, this.tuningCommandList[i]);
               }
            }
         }

         if (!this.level().isClientSide && this.initTick >= INIT_TICK_WITH_MERGIN) {
            addCoreSyncMessage = false;
            newCoreHeight = (float)(-matrix.min.y);
            newTotalHeight = (newCoreHeight + (float)matrix.max.y + 0.5F) * 0.98F;
            float threshold;
            if (AssembleFlagsUtil.isManualSetLargeWeight(this.assembleFlags)) {
               newLargeUnitWeight = (float)this.largeUnitWeightFixed / 100.0F;
            } else {
               threshold = newCoreHeight * 0.2F;
               if (threshold < 1.0F) {
                  threshold = 1.0F;
               }
               if (threshold > 100.0F) {
                  threshold = 100.0F;
               }
               newLargeUnitWeight = 1.0F / threshold;
            }

            if (!this.hasInitEnd) {
               this.setCoreHeightAndTotalHeight(newCoreHeight, newTotalHeight, newLargeUnitWeight, this.seatFlags);
               this.hasInitEnd = true;
               addCoreSyncMessage = true;
               if (this.unitAbility.isExFlyable()) {
                  this.setOnGround(true);
               }
            } else if (this.initTick < 600 && this.initTick % 90 == 0 && !this.disassemble) {
               addCoreSyncMessage = true;
            } else if (!this.disassemble && JBConfig.fluctuateUnitHeight) {
               threshold = JBConfig.fluctuateUnitHeightThreshold;
               if (Math.abs(this.coreHeight - newCoreHeight) > threshold || Math.abs(this.totalHeight - newTotalHeight) > threshold) {
                  this.setCoreHeightAndTotalHeight(newCoreHeight, newTotalHeight, newLargeUnitWeight, this.seatFlags);
                  addCoreSyncMessage = true;
               }
            }

            if (this.hasInitEnd && !this.disassemble) {
               this.processRingPrograms();
            }

            if (!this.disassemble && (addCoreSyncMessage || this.tickCount % 200 == 0 || this.needAttracterSyncTick > 0 && this.needAttracterSyncTick-- % 10 == 0)) {
               JBPacketSender.sendCoreSyncToPlayers(this);
            }
         } else if (this.level().isClientSide && this.isOnInitPhase() && !this.disassemble) {
            this.coreHeight = (float)(-matrix.min.y);
         }

         if (this.initTick < 4007) {
            ++this.initTick;
         }

         this.checkSeatUpdated();
         if (!this.level().isClientSide) {
            if (this.syncUpdateCoreRotation) {
               this.updateList.addAsCorelSynchronizeUpdate(this);
               this.syncUpdateCoreRotation = false;
            }
            if (this.needToSendTargetUpdate) {
               this.sendTargetSelectUpdate();
               this.needToSendTargetUpdate = false;
            }
         }

         if (!this.level().isClientSide && (this.updateList.index > 0 || this.getYRot() != this.lastSentRotationYaw)) {
            JBPacketSender.sendJointsUpdateToPlayers(this);
            this.lastSentRotationYaw = this.getYRot();
         }
      }

      if (this.slaveSlotDelay > 0) {
         --this.slaveSlotDelay;
         if (this.slaveSlotToggleDelay != null) {
            for (int i = 0; i < this.slaveSlotToggleDelay.length; ++i) {
               if (this.slaveSlotToggleDelay[i] > 0) {
                  int var10002 = this.slaveSlotToggleDelay[i]--;
               }
            }
         }
      }

      if (this.level().isClientSide) {
         JBModContainer.proxy.addToStickyRenderList(this);
      }
   }

   private void createRingPrograms() {
      if (this.innerChest != null) {
         for (int rp = 0; rp < 4; ++rp) {
            if (this.ringPrograms == null || this.ringPrograms[rp] == null) {
               ItemStack stack = this.innerChest.getItem(50 + rp);
               if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getItem() instanceof ItemRingProgram) {
                  byte[] pgmData = ItemRingProgram.getRingProgramData(stack);
                  if (pgmData != null) {
                     if (this.ringPrograms == null) {
                        this.ringPrograms = new RingProgram[4];
                     }
                     this.ringPrograms[rp] = RingProgram.readFromByteArray(pgmData);
                     this.ringPrograms[rp].slotIndex = (byte)rp;
                     this.ringPrograms[rp].setEntityJointed(this);
                     if (!this.ringPrograms[rp].hadError) {
                        this.ringPrograms[rp].initProgram();
                        if (this.activeRingSlots == null) {
                           this.activeRingSlots = new ActiveRingSlots();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void clearRingPrograms() {
      this.ringPrograms = null;
      this.activeRingSlots = null;
   }

   private void writeProgramsToNBT(CompoundTag subtag) {
      for (int rp = 0; rp < 4; ++rp) {
         if (this.ringPrograms[rp] != null) {
            byte[] pgmData = this.ringPrograms[rp].writeToByteArray(true);
            if (pgmData != null) {
               subtag.putByteArray(JointInventory.NBT_RING_PKEYS[rp], pgmData);
            }
         }
      }
   }

   private void readProgramsFromNBT(CompoundTag subtag) {
      for (int rp = 0; rp < 4; ++rp) {
         if (subtag.contains(JointInventory.NBT_RING_PKEYS[rp], 7)) {
            if (this.ringPrograms == null) {
               this.ringPrograms = new RingProgram[4];
            }
            this.ringPrograms[rp] = RingProgram.readFromByteArray(subtag.getByteArray(JointInventory.NBT_RING_PKEYS[rp]), true);
            this.ringPrograms[rp].slotIndex = (byte)rp;
            this.ringPrograms[rp].setEntityJointed(this);
            if (this.activeRingSlots == null) {
               this.activeRingSlots = new ActiveRingSlots();
            }
         }
      }
   }

   private void processRingChannels() {
      if (this.activeRingSlots != null) {
         this.activeRingSlots.updateAllSlots(this);
      }
      if (this.ringPrograms != null) {
         for (int rp = 0; rp < 4; ++rp) {
            if (this.ringPrograms[rp] != null && this.ringPrograms[rp].isOPCut()) {
               this.entitySeatPart0.cutDefaultOP = true;
            }
         }
      }
   }

   private void processRingPrograms() {
      if (this.ringPrograms != null) {
         for (int rp = 0; rp < 4; ++rp) {
            if (this.ringPrograms[rp] != null && !this.ringPrograms[rp].hadError) {
               try {
                  this.ringPrograms[rp].executeTick(this);
               } catch (Exception var3) {
                  this.ringPrograms[rp].hadError = true;
                  var3.printStackTrace();
               }
            }
         }
      }
   }

   private boolean isEnergyHealTick() {
      int interval = Math.max(this.unitAbility.energyHealSpeed, 1);
      return this.unitId == 0 ? this.tickCount % (interval * 3) == 0 : this.tickCount % interval == 0;
   }

   public void setCoreHeightAndTotalHeight(float newCoreHeight, float totalHeight, float largeWeight, byte seatFlags) {
      this.coreHeight = newCoreHeight;
      this.totalHeight = totalHeight;
      this.largeUnitWeight = largeWeight;
      this.seatFlags = seatFlags;
      this.setSizeWithoutPosChange();
   }

   private void setSizeWithoutPosChange() {
      double savedPosY = this.getY();
      this.noPhysics = true;
      this.refreshDimensions();
      this.noPhysics = false;
      this.setPos(this.getX(), savedPosY, this.getZ());
   }

   private ModelViewMatrix setupOwnMatrix() {
      ModelViewMatrix matrix = ModelViewMatrix.tHreadMvMatrix.get();
      matrix.reset();
      if (this.exFlyAttr != null && this.unitAbility.isExFlyable()) {
         matrix.setRotation(this.exFlyAttr.coreRotQuats);
      } else {
         matrix.rotateYY(-this.getYRot() * 0.017453292F);
      }

      matrix.scale(1.0F, -1.0F, -1.0F);
      if (!this.disassemble && this.isUnderMasterControl()) {
         matrix.translate(0.0F, -1.5F - this.coreHeight, 0.0F);
         matrix.saveBaseRotation();
         this.slaveAttatcher.setAdditionalMatrix(this, matrix);
         matrix.setBasePos(this, false);
      } else {
         matrix.translate(0.0F, -1.5F, 0.0F);
         matrix.saveBaseRotation();
         matrix.setBasePos(this, true);
      }

      if (this.level().isClientSide) {
         if (JBModContainer.proxy.setPlayerRayVec(matrix)) {
            matrix.setupRayVec();
         }
         this.isTunbleEntityOnClient = JBModContainer.proxy.isEntityJointedAllowdTuning(this);
      }

      return matrix;
   }

   @Override
   public void notifyClientAction(IJBJointParameterHolder.IJBJoint joint) {
      this.updateList.addAsExtraClientActionSyncMessage((JointParts)joint);
   }

   @Override
   public void notifyClientMotion(IJBJointParameterHolder.IJBJoint joint) {
      this.updateList.addAsExtraClientMotionSyncMessage((JointParts)joint);
   }

   @Override
   public void notifyClientEffect(IJBJointParameterHolder.IJBJoint joint, byte effectSlot) {
      this.updateList.addAsExtraClientEffectSyncMessage((JointParts)joint, effectSlot);
   }

   public void receiveJointStateUpdate(float limbSwing, float yaw, byte[] updata) {
      if (!this.disassemble) {
         this.walkAnimation.position(limbSwing);
         this.setRotationYawWithPosInc(yaw);
         SimpleUpdatedJointList.updateReceivedAmount(this, updata);
      }
   }

   private void setRotationYawWithPosInc(float yaw) {
      if (this.hasReceivedFirstPosRow) {
         this.rotationYawReceived = yaw;
         this.lerpYRot = (double)this.rotationYawReceived;
         this.lerpSteps = 3;
      }
   }

   @Override
   public void travel(Vec3 travelVector) {
      float moveStrafing = (float)travelVector.x;
      float moveForward = (float)travelVector.z;
      if (!this.noPhysics && !this.isOnInitPhase() && !this.isUnderMasterControl()) {
         if (!this.entitySeatPart0.hasDirectionRider() && !this.isAirBorneHovering()) {
            if (CompatHelper.getRiddenByEntity(this.entitySeatPart0) != null && this.level().isClientSide) {
               this.checkOnGroundClient();
            } else if (this.hasInitEnd) {
               if (this.isFootInOpaque()) {
                  this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
               }
               this.travelOrg(moveStrafing, moveForward);
               if (this.onGround()) {
                  this.spinningRate *= 0.95F;
                  if (this.spinningRate < 0.01F) {
                     this.spinningRate = 0.0F;
                  }
                  this.slowlyResetCoreRotation(10.0F);
               }
            }
         } else {
            boolean isExFlyable = this.unitAbility.isExFlyable();
            if (!isExFlyable) {
               if (this.entitySeatPart0.hasPlayerRider()) {
                  Entity rider = CompatHelper.getRiddenByEntity(this.entitySeatPart0);
                  this.yRotO = rider.getYRot();
                  this.setYRot(rider.getYRot());
                  this.setXRot(rider.getXRot() * 0.5F);
                  this.setRot(this.getYRot(), this.getXRot());
                  this.yHeadRot = this.yBodyRot = this.getYRot();
               } else if (this.entitySeatPart0.yawChange) {
                  if (this.entitySeatPart0.yawAdd) {
                     this.yRotO = this.getYRot() + this.entitySeatPart0.yawValue;
                     this.setYRot(this.getYRot() + this.entitySeatPart0.yawValue);
                     this.setRot(this.getYRot(), this.getXRot());
                     this.yHeadRot = this.yBodyRot = this.getYRot();
                  } else {
                     this.yRotO = this.entitySeatPart0.yawValue;
                     this.setYRot(this.entitySeatPart0.yawValue);
                     this.setRot(this.getYRot(), this.getXRot());
                     this.yHeadRot = this.yBodyRot = this.getYRot();
                  }
               }
               if (this.exFlyAttr != null) {
                  this.fixCoreRotFromCurrentYaw();
               }
            }

            boolean isGravityZero = false; // StarMiner zero-gravity interop removed
            boolean isSpinningUp;
            float addYaw;
            if (!isExFlyable) {
               if (isGravityZero) {
                  moveStrafing = this.entitySeatPart0.getMoveStrafingDR();
                  moveForward = this.entitySeatPart0.getMoveForwardDR();
                  this.setMaxUpStep(0.0F);
                  if (this.entitySeatPart0.isJumpOrUpDR()) {
                     this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.08, 0.0));
                     if (this.getDeltaMovement().y > 0.6) {
                        this.setDeltaMovement(this.getDeltaMovement().x, 0.6, this.getDeltaMovement().z);
                     }
                  } else if (this.entitySeatPart0.isSneakOrDownDR()) {
                     this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.08, 0.0));
                     if (this.getDeltaMovement().y < -0.6) {
                        this.setDeltaMovement(this.getDeltaMovement().x, -0.6, this.getDeltaMovement().z);
                     }
                  }
               } else {
                  if (this.unitAbility.isMovable()) {
                     moveStrafing = this.entitySeatPart0.getMoveStrafingDR();
                     moveForward = this.entitySeatPart0.getMoveForwardDR();
                     if (moveForward <= 0.0F) {
                        moveForward = (float)((double)moveForward * ((double)this.unitAbility.backWardSlowdownRate / 100.0));
                     }
                     this.setMaxUpStep((float)this.unitAbility.stepHight);
                  }

                  if (this.unitAbility.isHoverable()) {
                     this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.5, 1.0));
                     isSpinningUp = this.detectAirBlockBelow(5);
                     if (!isSpinningUp) {
                        double move = Math.sqrt((double)(moveStrafing * moveStrafing + moveForward * moveForward));
                        if (move > 1.0) {
                           move = 1.0;
                        }
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.1 * move, 0.0));
                        this.setEntityFlying(true);
                     }
                  } else if (this.unitAbility.isFlyable()) {
                     this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.85, 1.0));
                     if (this.onGround()) {
                        moveStrafing = 0.0F;
                        moveForward = 0.0F;
                        if (this.spinnUpOnGroundCommon(this.entitySeatPart0.isJumpOrUpDR())) {
                           if ((this.spinningRate += 0.015F) >= 1.0F) {
                              this.spinningRate = 1.0F;
                              this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.2, 0.0));
                              this.setEntityFlying(true);
                           }
                        } else {
                           this.spinningRate *= 0.95F;
                           if (this.spinningRate < 0.01F) {
                              this.spinningRate = 0.0F;
                           }
                        }
                     } else {
                        if (this.entitySeatPart0.isJumpOrUpDR()) {
                           if (this.entitySeatPart0.isJumpOrUpDoubleDR()) {
                              this.toggleAirHovering(false);
                           } else if (!this.isAirBorneHovering()) {
                              this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.15, 0.0));
                              if (this.getDeltaMovement().y > 1.8) {
                                 this.setDeltaMovement(this.getDeltaMovement().x, 1.8, this.getDeltaMovement().z);
                              }
                              if ((this.spinningRate += 0.1F) >= 1.0F) {
                                 this.spinningRate = 1.0F;
                              }
                           }
                        }
                        if (this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
                        }
                        if (this.entitySeatPart0.isJumpOrUpDR() && this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.1, 0.0));
                           if (this.getDeltaMovement().y > 0.8) {
                              this.setDeltaMovement(this.getDeltaMovement().x, 0.8, this.getDeltaMovement().z);
                           }
                        } else if (this.entitySeatPart0.isSneakOrDownDR() && this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.1, 0.0));
                           if (this.getDeltaMovement().y < -0.8) {
                              this.setDeltaMovement(this.getDeltaMovement().x, -0.8, this.getDeltaMovement().z);
                           }
                        }
                     }
                  } else if (this.unitAbility.isJumpable()) {
                     if (this.onGround() && this.entitySeatPart0.isJumpOrUpDR() && !this.isEntityJumping()) {
                        this.setDeltaMovement(this.getDeltaMovement().x, this.getJumpPower(1.0F), this.getDeltaMovement().z);
                        this.setEntityJumping(true);
                     } else if (this.onGround() && this.unitAbility.isMovable() && this.isDashing() && this.canUseDashEnergy()) {
                        this.setDeltaMovement(this.getDeltaMovement().x, Math.log(1.386000051140785), this.getDeltaMovement().z);
                        this.setEntityJumping(true);
                        this.setEntityDashing(true);
                        float f4 = Mth.sin(this.getYRot() * 3.1415927F / 180.0F);
                        addYaw = Mth.cos(this.getYRot() * 3.1415927F / 180.0F);
                        this.setDeltaMovement(this.getDeltaMovement().add((double)(moveStrafing * addYaw - moveForward * f4) * 2.0, 0.0, 0.0));
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.0, (double)(moveForward * addYaw + moveStrafing * f4) * 2.0));
                     }
                  } else if (this.unitAbility.isSubmarine()) {
                     if (this.entitySeatPart0.isJumpOrUpDoubleDR()) {
                        this.toggleAirHovering(true);
                     }
                     if (this.isInWater()) {
                        if (this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
                        }
                        if (this.entitySeatPart0.isJumpOrUpDR() && this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04, 0.0));
                           if (this.getDeltaMovement().y > 0.6) {
                              this.setDeltaMovement(this.getDeltaMovement().x, 0.6, this.getDeltaMovement().z);
                           }
                        } else if (this.entitySeatPart0.isSneakOrDownDR() && this.isAirBorneHovering()) {
                           this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.04, 0.0));
                           if (this.getDeltaMovement().y < -0.6) {
                              this.setDeltaMovement(this.getDeltaMovement().x, -0.6, this.getDeltaMovement().z);
                           }
                        }
                     }
                  }
               }
            } else {
               moveStrafing = 0.0F;
               moveForward = 0.0F;
               this.setMaxUpStep((float)this.unitAbility.stepHight);
               isSpinningUp = this.spinnUpOnGroundCommon(this.entitySeatPart0.isJumpOrUpDR());
               if (this.launchTick > 0) {
                  --this.launchTick;
               }
               if (this.launchTick > 80) {
                  this.launchTick = 60;
               }
               addYaw = 0.0F;
               if (this.entitySeatPart0.isSneakOrDownDR() && this.entitySeatPart0.isRgtDR()) {
                  ++addYaw;
               }
               if (this.entitySeatPart0.isSneakOrDownDR() && this.entitySeatPart0.isLftDR()) {
                  --addYaw;
               }
               float addRoll;
               if (!isGravityZero && this.onGround()) {
                  this.setEntityFlying(false);
                  this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.01, 0.0));
                  if (isSpinningUp) {
                     if ((this.spinningRate += 0.004F) >= 0.5F && addYaw == 0.0F) {
                        this.launchTick += 2;
                     }
                     if (!this.horizontalCollision && this.getMoveSpeedSq() > 0.0144 && this.launchTick > 60) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.5, 0.0));
                        this.resetCoreRotToLiftPitch(-10.0F);
                        this.setEntityFlying(true);
                     }
                  } else if (this.spinningRate > 0.0F) {
                     if (this.horizontalCollision) {
                        this.spinningRate *= 0.95F;
                     } else {
                        this.spinningRate -= (float)Math.exp((double)(-10.0F * this.spinningRate)) * 0.08F;
                        if (this.spinningRate < 0.01F) {
                           this.spinningRate = 0.0F;
                        }
                     }
                  }

                  if (this.spinningRate >= 0.5F) {
                     this.spinningRate = 0.5F;
                  }
                  moveForward = (float)Math.pow((double)(2.0F * this.spinningRate), 2.0) * 0.6F;
                  this.slowlyResetCoreRotation(0.1F);
                  if (this.exFlyAttr != null) {
                     Quaternion base = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)(-this.exFlyAttr.rotationYawUnfixed)), 0.0, 1.0, 0.0);
                     addRoll = Math.abs(base.getAngleBetween(this.exFlyAttr.coreRotQuats, base));
                     if (addRoll > 40.0F) {
                        float damageRate = addRoll / 80.0F;
                        this.hurt(this.damageSources().fall(), this.getMaxHealth() * damageRate);
                     }
                  }
               } else {
                  if (!this.isEntityFlying()) {
                     if (!isGravityZero) {
                        this.useEntityEnergy(this.getMaxEnergy() * 0.33F);
                     }
                     this.setEntityFlying(true);
                  }
                  if (isSpinningUp) {
                     if ((this.spinningRate += 0.025F) >= 1.0F) {
                        this.spinningRate = 1.0F;
                     }
                  } else if (this.spinningRate < 0.5F) {
                     if (this.hasInitEnd && (this.spinningRate = (float)((double)this.spinningRate + Math.pow((double)(this.spinningRate + 0.3F), 2.0) * 0.20000000298023224)) >= 0.5F) {
                        this.spinningRate = 0.5F;
                     }
                  } else {
                     this.spinningRate *= 0.94F;
                  }

                  float addPitch = 0.0F;
                  addRoll = 0.0F;
                  if (!this.entitySeatPart0.isSneakOrDownDR() && this.entitySeatPart0.isRgtDR()) {
                     addRoll += 2.0F;
                  }
                  if (!this.entitySeatPart0.isSneakOrDownDR() && this.entitySeatPart0.isLftDR()) {
                     addRoll -= 2.0F;
                  }
                  if (this.entitySeatPart0.isFwdDR()) {
                     addPitch += 2.0F;
                  }
                  if (this.entitySeatPart0.isBwdDR()) {
                     addPitch -= 2.0F;
                  }
                  this.applyCoreRotRollPitch(addPitch, addRoll);
               }
               this.applyCoreRotYaw(addYaw);
            }

            this.travelOrg(moveStrafing, moveForward);
            if (isGravityZero) {
               this.setEntityFlying(true);
               this.setEntityJumping(true);
            } else if (this.onGround()) {
               this.setEntityFlying(false);
               this.setEntityJumping(false);
               this.setEntityDashing(false);
               if (!this.unitAbility.isSubmarine()) {
                  this.setAirBorneHovering(false);
               }
            }
         }

         if (this.level().isClientSide && this.exFlyAttr != null && !this.unitAbility.isExFlyable()) {
            this.fixCoreRotFromCurrentYaw();
         }
         this.calculateLimbSwing();
      } else {
         this.slowlyResetCoreRotation(0.05F);
      }
   }

   protected void fixCoreRotFromCurrentYaw() {
      this.exFlyAttr.rotationYawUnfixed = this.getYRot();
      this.exFlyAttr.coreRotQuats.setFromAxisAngle(Math.toRadians((double)(-this.exFlyAttr.rotationYawUnfixed)), 0.0, 1.0, 0.0);
   }

   private void toggleAirHovering(boolean ignoreBelowBlockState) {
      if (!this.isAirBorneHovering()) {
         if (ignoreBelowBlockState) {
            this.setAirBorneHovering(true);
         } else {
            boolean hasAirBelow = this.detectAirBlockBelow(2);
            if (hasAirBelow) {
               this.setAirBorneHovering(true);
            }
         }
      } else {
         this.setAirBorneHovering(false);
      }
   }

   public void calculateLimbSwing() {
      double xMoved = this.getX() - this.xOld;
      double zMoved = this.getZ() - this.zOld;
      float movedDistance = (float)(Math.sqrt(xMoved * xMoved + zMoved * zMoved) * 4.0);
      if (movedDistance > 1.0F) {
         movedDistance = 1.0F;
      }
      float oldSpeed = this.walkAnimation.speed();
      this.walkAnimation.speed(oldSpeed + (movedDistance - oldSpeed) * 0.4F);
      if (!this.level().isClientSide) {
         this.walkAnimation.position(this.walkAnimation.position() + this.walkAnimation.speed() * this.largeUnitWeight);
      }
   }

   private boolean isFootInOpaque() {
      int footX = Mth.floor(this.getX());
      int footY = Mth.floor(this.getY() + 1.0E-4);
      int footZ = Mth.floor(this.getZ());
      return WorldHelper.getBlockState(this.level(), footX, footY, footZ).isSolidRender(this.level(), new BlockPos(footX, footY, footZ));
   }

   private double getMoveSpeedSq() {
      return this.getDeltaMovement().x * this.getDeltaMovement().x + this.getDeltaMovement().y * this.getDeltaMovement().y + this.getDeltaMovement().z * this.getDeltaMovement().z;
   }

   private boolean spinnUpOnGroundCommon(boolean isSpinnUpKeyPressed) {
      if (isSpinnUpKeyPressed) {
         this.commonInputReserveTick = 5;
         return true;
      } else if (--this.commonInputReserveTick <= 0) {
         this.commonInputReserveTick = 0;
         return false;
      } else {
         return true;
      }
   }

   private boolean canUseDashEnergy() {
      float dashEnergy = this.getMaxEnergy() / 15.0F;
      if (dashEnergy < 0.5F) {
         dashEnergy = 0.5F;
      }
      return this.useEntityEnergy(dashEnergy);
   }

   private boolean isDashing() {
      EntitySeatPart opSeat = this.entitySeatPart0;
      if (opSeat != null) {
         if (opSeat.hasRINGMoveDR && (opSeat.moveForwardDR >= 2 || opSeat.moveStrafingDR >= 2)) {
            return true;
         }
         if (CompatHelper.getRiddenByEntity(opSeat) != null) {
            return opSeat.isRiddenbyDoublePressedFwdKey || opSeat.isRiddenbyDoublePressedBwdKey || opSeat.isRiddenbyDoublePressedLftKey || opSeat.isRiddenbyDoublePressedRgtKey;
         }
      }
      return false;
   }

   public boolean detectAirBlockBelow(int numOfAirBlock) {
      int nowX = Mth.floor(this.getX());
      int nowY = Mth.floor(this.getY());
      int nowZ = Mth.floor(this.getZ());
      boolean isAllAirBelow = true;
      for (int i = 0; i < numOfAirBlock; ++i) {
         if (!WorldHelper.isAirBlock(this.level(), nowX, nowY - i, nowZ)) {
            isAllAirBelow = false;
         }
      }
      return isAllAirBelow;
   }

   private void checkOnGroundClient() {
      this.setOnGround(false);
      double dummyMotionY = -1.0;
      AABB box = this.getBoundingBox();
      AABB below = box.inflate(0.0, -1.0, 0.0);
      for (VoxelShape shape : this.level().getBlockCollisions(this, below)) {
         dummyMotionY = shape.collide(Direction.Axis.Y, box, dummyMotionY);
      }
      for (VoxelShape entityShape : this.level().getEntityCollisions(this, below)) {
         for (AABB entityBB : entityShape.toAabbs()) {
            dummyMotionY = Shapes.create(entityBB).collide(Direction.Axis.Y, box, dummyMotionY);
         }
      }
      if (dummyMotionY != -1.0) {
         this.setOnGround(true);
      }
   }

   private void addVelocityFloating() {
      double d0 = 0.0;
      double height = (double)this.coreHeight * 0.8;
      for (int i = 0; i < 5; ++i) {
         double d1;
         double d3;
         if (height < 1.0) {
            d1 = this.getBoundingBox().minY + 0.7 * (double)(i + 0) / 5.0;
            d3 = this.getBoundingBox().minY + 0.7 * (double)(i + 1) / 5.0;
         } else {
            d1 = this.getBoundingBox().minY + height * (double)(i + 0) / 5.0;
            d3 = this.getBoundingBox().minY + height * (double)(i + 1) / 5.0;
         }
         AABB axisalignedbb = JBCommonUtil.getBoundingBox(this.getBoundingBox().minX, d1, this.getBoundingBox().minZ, this.getBoundingBox().maxX, d3, this.getBoundingBox().maxZ);
         if (this.level().containsAnyLiquid(axisalignedbb)) {
            d0 += 0.2;
         }
      }
      if (d0 < 1.0) {
         if (height < 1.0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01599999910593033 * d0 * 1.0, 0.0));
         } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.03999999910593033 * (d0 * 2.0 - 1.0), 0.0));
         }
      } else {
         if (this.getDeltaMovement().y < 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0 / (2.0), 1.0));
         }
         if (height < 1.0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.029000000216066836, 0.0));
         } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04700000021606684, 0.0));
         }
      }
   }

   public void travelOrg(float moveStrafe, float moveForward) {
      boolean isGravityZero = false; // StarMiner zero-gravity interop removed
      if (this.exFlyAttr != null && this.unitAbility.isExFlyable() && this.isEntityFlying()) {
         isGravityZero = true;
      }

      double savedPosY;
      if (this.isInWater()) {
         savedPosY = this.getY();
         this.modMoveRelative(moveStrafe, moveForward, 0.02F, 0.0F);
         if (!isGravityZero && this.unitAbility.isFloatble()) {
            if (!this.unitAbility.isSubmarine() || !this.isAirBorneHovering()) {
               this.addVelocityFloating();
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (moveStrafe == 0.0F && moveForward == 0.0F) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.91, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.949999988079071, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.91));
            } else {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.9900000095367432, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.949999988079071, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.9900000095367432));
            }

            if (this.unitAbility.isSubmarine()) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.985, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.985));
            }
         } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.800000011920929, 1.0, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.800000011920929, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.800000011920929));
         }

         if (!isGravityZero) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.02, 0.0));
            if (this.horizontalCollision && this.isFree(this.getDeltaMovement().x, this.getDeltaMovement().y + 0.6000000238418579 - this.getY() + savedPosY, this.getDeltaMovement().z)) {
               this.setDeltaMovement(this.getDeltaMovement().x, 0.30000001192092896, this.getDeltaMovement().z);
            }
         }
      } else if (this.checkLavaMovement()) {
         savedPosY = this.getY();
         this.modMoveRelative(moveStrafe, moveForward, 0.02F, 0.0F);
         if (!isGravityZero && this.unitAbility.isFloatble()) {
            if (!this.unitAbility.isSubmarine() || !this.isAirBorneHovering()) {
               this.addVelocityFloating();
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            if (moveStrafe == 0.0F && moveForward == 0.0F) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.91, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.949999988079071, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.91));
            } else {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.9900000095367432, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.949999988079071, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.9900000095367432));
            }

            if (this.unitAbility.isSubmarine()) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.985, 1.0, 1.0));
               this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.985));
            }
         } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.5, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.5));
         }

         if (!isGravityZero) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.02, 0.0));
            if (this.horizontalCollision && this.isFree(this.getDeltaMovement().x, this.getDeltaMovement().y + 0.6000000238418579 - this.getY() + savedPosY, this.getDeltaMovement().z)) {
               this.setDeltaMovement(this.getDeltaMovement().x, 0.30000001192092896, this.getDeltaMovement().z);
            }
         }
      } else {
         float slipperNess = 0.91F;
         if (this.onGround()) {
            BlockState underState = this.level().getBlockState(BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1.0, this.getZ()));
            slipperNess = underState.getBlock().getFriction() * 0.91F;
         }

         float f3 = 0.16277136F / (slipperNess * slipperNess * slipperNess);
         float moveSpeed;
         if (this.onGround()) {
            moveSpeed = 0.1F * f3;
         } else {
            moveSpeed = this.getFlyingSpeed();
         }

         this.modMoveRelative(moveStrafe, moveForward, moveSpeed, 0.0F);
         slipperNess = 0.91F;
         if (this.onGround()) {
            BlockState underState = this.level().getBlockState(BlockPos.containing(this.getX(), this.getBoundingBox().minY - 1.0, this.getZ()));
            slipperNess = underState.getBlock().getFriction() * 0.91F;
         }

         if (!isGravityZero && this.unitAbility.isFloatOnly() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.9, 1.0, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.9));
         }

         this.move(MoverType.SELF, this.getDeltaMovement());
         BlockPos posWork = BlockPos.containing(this.getX(), 0.0, this.getZ());
         if (!isGravityZero) {
            if (this.level().isClientSide && !this.level().hasChunkAt(posWork)) {
               if (this.getY() > 0.0) {
                  this.setDeltaMovement(this.getDeltaMovement().x, -0.1, this.getDeltaMovement().z);
               } else {
                  this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
               }
            } else if (!this.isNoGravity()) {
               this.setDeltaMovement(this.getDeltaMovement().subtract(0.0, 0.08, 0.0));
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.9800000190734863, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply((double)slipperNess, 1.0, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, (double)slipperNess));
         } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.91, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.91, 1.0, 1.0));
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 1.0, 0.91));
         }
      }
   }

   private List<VoxelShape> collectCollisions(AABB aabb) {
      List<VoxelShape> list = new ArrayList<VoxelShape>();
      for (VoxelShape entityShape : this.level().getEntityCollisions(this, aabb)) {
         for (AABB entityBB : entityShape.toAabbs()) {
            list.add(Shapes.create(entityBB));
         }
      }
      for (VoxelShape shape : this.level().getBlockCollisions(this, aabb)) {
         list.add(shape);
      }
      return list;
   }

   @Override
   public void move(MoverType type, Vec3 argMove) {
      if (!this.noPhysics && !this.isUnderMasterControl()) {
         this.level().getProfiler().push("move");
         double d3 = this.getX();
         double d4 = this.getY();
         double d5 = this.getZ();
         double savedX1 = argMove.x;
         double savedY1 = argMove.y;
         double savedZ1 = argMove.z;
         AABB axisalignedbb = JBCommonUtil.copyBB(this.getBoundingBox());
         this.shrinkBoundWidth();
         List<VoxelShape> list = this.collectCollisions(this.getBoundingBox().inflate(savedX1, savedY1, savedZ1));
         double argMoveX = savedX1;
         double argMoveY = savedY1;
         double argMoveZ = savedZ1;

         for (int i = 0; i < list.size(); ++i) {
            argMoveY = list.get(i).collide(Direction.Axis.Y, this.getBoundingBox(), argMoveY);
         }
         JBCommonUtil.offsetBB(this.getBoundingBox(), 0.0, argMoveY, 0.0);
         boolean isCollidedVerticallly = this.onGround() || savedY1 != argMoveY && savedY1 < 0.0;
         if (this.unitAbility.isFloatble()) {
            isCollidedVerticallly |= this.isInWater();
         }

         for (int i = 0; i < list.size(); ++i) {
            argMoveX = list.get(i).collide(Direction.Axis.X, this.getBoundingBox(), argMoveX);
         }
         JBCommonUtil.offsetBB(this.getBoundingBox(), argMoveX, 0.0, 0.0);
         for (int i = 0; i < list.size(); ++i) {
            argMoveZ = list.get(i).collide(Direction.Axis.Z, this.getBoundingBox(), argMoveZ);
         }
         JBCommonUtil.offsetBB(this.getBoundingBox(), 0.0, 0.0, argMoveZ);
         if (this.maxUpStep() > 0.0F && isCollidedVerticallly && (savedX1 != argMoveX || savedZ1 != argMoveZ)) {
            double savedX2 = argMoveX;
            double savedY2 = argMoveY;
            double savedZ2 = argMoveZ;
            argMoveX = savedX1;
            argMoveY = (double)this.maxUpStep();
            argMoveZ = savedZ1;
            AABB axisalignedbb1 = JBCommonUtil.copyBB(this.getBoundingBox());
            this.setBoundingBox(axisalignedbb);
            this.shrinkBoundWidth();
            list = this.collectCollisions(this.getBoundingBox().inflate(savedX1, argMoveY, savedZ1));

            for (int i = 0; i < list.size(); ++i) {
               argMoveY = list.get(i).collide(Direction.Axis.Y, this.getBoundingBox(), argMoveY);
            }
            JBCommonUtil.offsetBB(this.getBoundingBox(), 0.0, argMoveY, 0.0);

            for (int i = 0; i < list.size(); ++i) {
               argMoveX = list.get(i).collide(Direction.Axis.X, this.getBoundingBox(), argMoveX);
            }
            JBCommonUtil.offsetBB(this.getBoundingBox(), argMoveX, 0.0, 0.0);

            for (int i = 0; i < list.size(); ++i) {
               argMoveZ = list.get(i).collide(Direction.Axis.Z, this.getBoundingBox(), argMoveZ);
            }
            JBCommonUtil.offsetBB(this.getBoundingBox(), 0.0, 0.0, argMoveZ);
            argMoveY = (double)(-this.maxUpStep());

            for (int i = 0; i < list.size(); ++i) {
               argMoveY = list.get(i).collide(Direction.Axis.Y, this.getBoundingBox(), argMoveY);
            }
            JBCommonUtil.offsetBB(this.getBoundingBox(), 0.0, argMoveY, 0.0);
            if (savedX2 * savedX2 + savedZ2 * savedZ2 >= argMoveX * argMoveX + argMoveZ * argMoveZ) {
               argMoveX = savedX2;
               argMoveY = savedY2;
               argMoveZ = savedZ2;
               this.setBoundingBox(axisalignedbb1);
            }
         }

         this.level().getProfiler().pop();
         this.level().getProfiler().push("rest");
         this.reapplyPosition();
         this.resetBoundWidth();
         this.horizontalCollision = savedX1 != argMoveX || savedZ1 != argMoveZ;
         this.verticalCollision = savedY1 != argMoveY;
         this.setOnGround(savedY1 != argMoveY && savedY1 < 0.0);
         int fallX = Mth.floor(this.getX());
         int fallY = Mth.floor(this.getY() - 0.20000000298023224);
         int fallZ = Mth.floor(this.getZ());
         BlockPos blockpos = new BlockPos(fallX, fallY, fallZ);
         BlockState bState = this.level().getBlockState(blockpos);
         Block block = bState.getBlock();
         if (bState.isAir()) {
            BlockPos blockpos1 = blockpos.below();
            BlockState iblockstate1 = this.level().getBlockState(blockpos1);
            Block block1 = iblockstate1.getBlock();
            if (block1 instanceof FenceBlock || block1 instanceof WallBlock || block1 instanceof FenceGateBlock) {
               bState = iblockstate1;
               blockpos = blockpos1;
            }
         }

         this.checkFallDamage(argMoveY, this.onGround(), bState, blockpos);
         if (savedX1 != argMoveX) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, this.getDeltaMovement().z);
         }

         if (savedZ1 != argMoveZ) {
            this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, 0.0);
         }

         if (savedY1 != argMoveY) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.0, this.getDeltaMovement().z);
            block.updateEntityAfterFallOn(this.level(), this);
         }

         double savedX2 = this.getX() - d3;
         double savedY2 = this.getY() - d4;
         double savedZ2 = this.getZ() - d5;
         if (block != null && this.onGround()) {
            block.stepOn(this.level(), blockpos, bState, this);
         }

         this.walkDist = (float)((double)this.walkDist + (double)Math.sqrt(savedX2 * savedX2 + savedZ2 * savedZ2) * 0.6);
         this.moveDist = (float)((double)this.moveDist + (double)Math.sqrt(savedX2 * savedX2 + savedY2 * savedY2 + savedZ2 * savedZ2) * 0.6);
         if (this.moveDist > (float)this.nextStepDistancePrivate && !bState.isAir()) {
            this.nextStepDistancePrivate = (int)this.moveDist + 1;
            if (!this.unitAbility.isFloatble() && this.isInWater()) {
               float f = (float)Math.sqrt((double)(this.getDeltaMovement().x * this.getDeltaMovement().x * 0.20000000298023224F + this.getDeltaMovement().y * this.getDeltaMovement().y + this.getDeltaMovement().z * this.getDeltaMovement().z * 0.20000000298023224F)) * 0.35F;
               if (f > 1.0F) {
                  f = 1.0F;
               }

               this.playSound(this.getSwimSound(), f, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            }
         }

         if (this.onGround() && (this.stepSoundCycle = (float)((double)this.stepSoundCycle + (double)((this.walkAnimation.position() - this.prevLimbSwing) * 0.32F) * 180.0 / Math.PI)) > 180.0F) {
            if (!this.unitAbility.isExFlyable()) {
               this.playStepSound(blockpos, bState);
            }

            this.stepSoundCycle = 0.0F;
         }

         this.prevLimbSwing = this.walkAnimation.position();

         try {
            this.checkInsideBlocks();
         } catch (Throwable var40) {
            CrashReport crashreport = CrashReport.forThrowable(var40, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
         }

         this.level().getProfiler().pop();
      } else {
         JBCommonUtil.offsetBB(this.getBoundingBox(), argMove.x, argMove.y, argMove.z);
         this.reapplyPosition();
      }
   }

   private void resetBoundWidth() {
      float wh = this.getBbWidth() / 2.0F;
      AABB bb = this.getBoundingBox();
      this.setBoundingBox(new AABB(this.getX() - (double)wh, bb.minY, this.getZ() - (double)wh, this.getX() + (double)wh, bb.maxY, this.getZ() + (double)wh));
   }

   protected void shrinkBoundWidth() {
      double wh = 0.2;
      AABB bb = this.getBoundingBox();
      double cX = (bb.minX + bb.maxX) / 2.0;
      double cZ = (bb.minZ + bb.maxZ) / 2.0;
      this.setBoundingBox(new AABB(cX - wh, bb.minY, cZ - wh, cX + wh, bb.maxY, cZ + wh));
   }

   private boolean checkLavaMovement() {
      double shrink = 1.9000000000000001;
      // 1.12 used World.isMaterialInBB(bb.grow(-1.9,-0.4,-1.9), Material.LAVA); the shrunk box is
      // empty for this entity's size, so the flag stays false exactly as in the original.
      this.isInLava = this.level().containsAnyLiquid(this.getBoundingBox().inflate(-shrink, -0.4000000059604645, -shrink));
      return this.isInLava;
   }

   @Override
   public boolean isInLava() {
      return this.isInLava;
   }

   public void modMoveRelative(float moveStrafing, float moveForward, float speed, float friction) {
      if (this.exFlyAttr != null && this.unitAbility.isExFlyable() && this.isEntityFlying()) {
         double flySpeed = (double)(this.spinningRate * 0.06F);
         this.setDeltaMovement(this.getDeltaMovement().add(this.exFlyAttr.facingTwardsVec.x * flySpeed, 0.0, 0.0));
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, this.exFlyAttr.facingTwardsVec.y * flySpeed, 0.0));
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.0, this.exFlyAttr.facingTwardsVec.z * flySpeed));
      } else {
         float moveAmount = moveStrafing * moveStrafing + moveForward * moveForward;
         if (moveAmount >= 1.0E-4F) {
            moveAmount = (float)Math.sqrt((double)moveAmount);
            if (moveAmount < 1.0F) {
               moveAmount = 1.0F;
            }

            moveAmount = speed / moveAmount;
            moveStrafing *= moveAmount * ((float)this.unitAbility.moveStrafingSpeedRate / 100.0F);
            moveForward *= moveAmount * ((float)this.unitAbility.moveForwardSpeedRate / 100.0F);
            float f4 = Mth.sin(this.getYRot() * 3.1415927F / 180.0F);
            float f5 = Mth.cos(this.getYRot() * 3.1415927F / 180.0F);
            this.setDeltaMovement(this.getDeltaMovement().add((double)(moveStrafing * f5 - moveForward * f4), 0.0, 0.0));
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.0, (double)(moveForward * f5 + moveStrafing * f4)));
         }
      }
   }

   public boolean starReInit(byte initLayerMode) {
      return this.starReInit(initLayerMode, 0);
   }

   public boolean starReInit(byte initLayerMode, int mainSubToggleFlg) {
      if (!this.level().isClientSide && this.hasInitEnd && !this.disassemble) {
         if (!this.isSlaveMode() && this.slaveSlotDelay <= 0) {
            if (this.getInitLayerMode() == initLayerMode) {
               return false;
            } else {
               this.setInitLayerMode(initLayerMode);
               this.toggleMainSubType(mainSubToggleFlg);
               this.initTick = 0;
               this.hasInitEnd = false;
               JBPacketSender.sendJointsReInitToPlayers(this, initLayerMode, (byte)mainSubToggleFlg);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void receiveReInitOnClient(byte initLayerMode, int mainSubToggleFlg) {
      this.setInitLayerMode(initLayerMode);
      this.toggleMainSubType(mainSubToggleFlg);
      this.initTick = 0;
      this.hasInitEnd = false;
      this.isReInit = true;
   }

   protected void toggleMainSubType(int mainSubToggleFlg) {
      if (mainSubToggleFlg != 0) {
         if (mainSubToggleFlg == 2 && this.addonBonus.subTypeUnitId != 0) {
            this.setupUnitAbilityFromUnitId(this.addonBonus.subTypeUnitId);
            this.unitId = (byte)this.addonBonus.subTypeUnitId;
         } else {
            this.setupUnitAbilityFromUnitId(this.mainUnitId);
            this.unitId = this.mainUnitId;
         }
      }
   }

   public boolean changeInitValueMode(int toMode) {
      return this.changeInitValueMode(toMode, 0);
   }

   public boolean changeInitValueMode(int toMode, int mainSubToggleFlg) {
      if (this.isSlaveMode()) {
         return false;
      } else if (toMode >= 0 && toMode <= 127) {
         return this.getInitLayerMode() == toMode ? true : this.starReInit((byte)toMode, mainSubToggleFlg);
      } else {
         return false;
      }
   }

   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return this.disassemble;
   }

   public void startDisassemble(LivingEntity conductor) {
      if (!this.level().isClientSide) {
         if (JBConfig.disableMasterConnectedDisassemble) {
            if (!this.isMasterConnected() && this.startDisassemble(false) && conductor instanceof Player) {
               this.disassembleConductor = (Player)conductor;
            }
         } else if (this.startDisassemble(false) && conductor instanceof Player) {
            this.disassembleConductor = (Player)conductor;
         }
      }
   }

   public boolean startDisassemble(boolean force) {
      if (!force && JBConfig.disableDisassemble) {
         return false;
      } else if (!this.level().isClientSide && this.hasInitEnd && !this.disassemble) {
         this.preDASavedCoreHeight = this.coreHeight;
         this.disassemble = true;
         this.disassembleTick = 0;
         this.noPhysics = true;
         int rotationRounded = (Mth.floor((double)(this.getYRot() * 4.0F / 360.0F) + 0.5) & 3) % 4;
         if (rotationRounded == 0) {
            this.disassembleFacing = 3;
         }
         if (rotationRounded == 1) {
            this.disassembleFacing = 4;
         }
         if (rotationRounded == 2) {
            this.disassembleFacing = 2;
         }
         if (rotationRounded == 3) {
            this.disassembleFacing = 5;
         }

         for (this.disassembleRotationYaw = (float)(rotationRounded * 90); this.getYRot() - this.disassembleRotationYaw < -180.0F; this.disassembleRotationYaw -= 360.0F) {
         }
         while (this.getYRot() - this.disassembleRotationYaw >= 180.0F) {
            this.disassembleRotationYaw += 360.0F;
         }

         this.dissassemblePosX = (double)Mth.floor(this.getX()) + 0.5;
         this.dissassemblePosY = (double)Mth.floor(this.getY() + (double)(-this.minYSavedForDisassemble)) + 0.0;
         this.dissassemblePosZ = (double)Mth.floor(this.getZ()) + 0.5;
         JBPacketSender.sendJointsDisassembleToPlayers(this, this.disassembleFacing);
         return true;
      } else {
         return false;
      }
   }

   public void receiveJointDisassemble(int disassembleFacing, double posX, double posY, double posZ) {
      this.disassembleHelper = new DisassembleHelper();
      this.disassemble = true;
      this.noPhysics = true;
      this.disassembleTick = 0;
      this.disassembleFacing = disassembleFacing;
      int rotationRounded = 0;
      if (this.disassembleFacing == 3) {
         rotationRounded = 0;
      }
      if (this.disassembleFacing == 4) {
         rotationRounded = 1;
      }
      if (this.disassembleFacing == 2) {
         rotationRounded = 2;
      }
      if (this.disassembleFacing == 5) {
         rotationRounded = 3;
      }

      for (this.disassembleRotationYaw = (float)(rotationRounded * 90); this.getYRot() - this.disassembleRotationYaw < -180.0F; this.disassembleRotationYaw -= 360.0F) {
      }
      while (this.getYRot() - this.disassembleRotationYaw >= 180.0F) {
         this.disassembleRotationYaw += 360.0F;
      }

      this.dissassemblePosX = posX;
      this.dissassemblePosY = posY;
      this.dissassemblePosZ = posZ;
      this.disassembleHelper.cHelpSavedX = this.getX();
      this.disassembleHelper.cHelpSavedY = this.getY();
      this.disassembleHelper.cHelpSavedZ = this.getZ();
      this.disassembleHelper.cHelpSavedYaw = this.getYRot();
   }

   private boolean updateDisassemble() {
      float interpolate = Math.min((float)this.disassembleTick / 30.0F, 1.0F);
      if (this.disassembleHelper != null) {
         DisassembleHelper var10000 = this.disassembleHelper;
         var10000.cHelpSavedX += (this.dissassemblePosX - this.disassembleHelper.cHelpSavedX) * (double)interpolate;
         var10000 = this.disassembleHelper;
         var10000.cHelpSavedY += (this.dissassemblePosY - this.disassembleHelper.cHelpSavedY) * (double)interpolate;
         var10000 = this.disassembleHelper;
         var10000.cHelpSavedZ += (this.dissassemblePosZ - this.disassembleHelper.cHelpSavedZ) * (double)interpolate;
         this.setPos(this.disassembleHelper.cHelpSavedX, this.disassembleHelper.cHelpSavedY, this.disassembleHelper.cHelpSavedZ);
      } else {
         this.setPos(this.getX() + (this.dissassemblePosX - this.getX()) * (double)interpolate, this.getY() + (this.dissassemblePosY - this.getY()) * (double)interpolate, this.getZ() + (this.dissassemblePosZ - this.getZ()) * (double)interpolate);
      }

      this.coreHeight += (0.0F - this.coreHeight) * interpolate;
      if (this.exFlyAttr != null && this.unitAbility.isExFlyable()) {
         Quaternion disassembleRot = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)(-this.disassembleRotationYaw)), 0.0, 1.0, 0.0);
         Quaternion.slerp(interpolate, this.exFlyAttr.coreRotQuats, disassembleRot, this.exFlyAttr.coreRotQuats);
      } else if (this.disassembleHelper != null) {
         DisassembleHelper var10001 = this.disassembleHelper;
         var10001.cHelpSavedYaw += (this.disassembleRotationYaw - this.disassembleHelper.cHelpSavedYaw) * interpolate;
         this.setYRot(this.disassembleHelper.cHelpSavedYaw);
      } else {
         this.setYRot(this.getYRot() + (this.disassembleRotationYaw - this.getYRot()) * interpolate);
      }

      if (this.disassembleTick++ > 60 && this.jointParts != null) {
         this.createBlocksFromRefLists(new JointListsHolderImpl.DefaultBlockArranger(this.level()), this.disassembleFacing, Mth.floor(this.dissassemblePosX), Mth.floor(this.dissassemblePosY), Mth.floor(this.dissassemblePosZ));
         this.discard();
         this.jointParts = null;
         this.ensureRiddenRelease();
         if (this.disassembleConductor != null) {
            ItemJBController.procesConductorsJBController(this, this.disassembleConductor);
            this.disassembleConductor = null;
         }
         return true;
      } else {
         return false;
      }
   }

   public boolean isDisassembleCallFromOwner(LivingEntity caller) {
      if (caller == null) {
         return false;
      } else {
         int throwerHash = JBCommonUtil.getUUIDHash(caller);
         return throwerHash == this.ownerHash;
      }
   }

   public EntitySeatPart getSeatEntity(int seatNum) {
      EntityJointed getFrom;
      if (this.isSlaveOpIndependent()) {
         getFrom = this;
      } else {
         getFrom = this.getMasterIfConnected();
      }
      return seatNum >= 0 && seatNum < getFrom.entitySeatPartArray.length ? getFrom.entitySeatPartArray[seatNum] : null;
   }

   private void checkSeatUpdated() {
      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         EntitySeatPart entitySeat = this.entitySeatPartArray[i];
         entitySeat.seatEnabled = false;
         if (CompatHelper.getRiddenByEntity(entitySeat) == null) {
            entitySeat.canDetach = false;
         }

         if (this.level().isClientSide) {
            if (entitySeat.cameraModeSeatNum == -2) {
               entitySeat.autoDetectCameraNum();
            }
         } else {
            if (!entitySeat.keyReceived) {
               ++entitySeat.usedPrevValueCount;
            }
            entitySeat.keyReceived = false;
            if (entitySeat.keyConductor != null && entitySeat.keyConductor.isRemoved()) {
               entitySeat.keyConductor = null;
            }

            entitySeat.storedIsRiddenbyPressingExKeyList[8] = entitySeat.isRiddenbyPressingFwdKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[9] = entitySeat.isRiddenbyPressingBwdKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[10] = entitySeat.isRiddenbyPressingLftKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[11] = entitySeat.isRiddenbyPressingRgtKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[12] = entitySeat.isRiddenbyPressingJumpKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[13] = entitySeat.isRiddenbyPressingSneakKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[13] = entitySeat.isRiddenbyPressingSneakKey;
            entitySeat.storedIsRiddenbyPressingExKeyList[14] = entitySeat.isRiddenbyUsingWheelUp;
            entitySeat.storedIsRiddenbyPressingExKeyList[15] = entitySeat.isRiddenbyUsingWheelDown;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[8] = entitySeat.isRiddenbyDoublePressedFwdKey;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[9] = entitySeat.isRiddenbyDoublePressedBwdKey;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[10] = entitySeat.isRiddenbyDoublePressedLftKey;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[11] = entitySeat.isRiddenbyDoublePressedRgtKey;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[12] = entitySeat.isRiddenbyDoublePressedJumpKey;
            entitySeat.storedIsRiddenbyDoublePressedExKeyList[13] = entitySeat.isRiddenbyDoublePressedSneakKey;
            entitySeat.isRiddenbyPressingFwdKey = false;
            entitySeat.isRiddenbyPressingBwdKey = false;
            entitySeat.isRiddenbyPressingLftKey = false;
            entitySeat.isRiddenbyPressingRgtKey = false;
            entitySeat.isRiddenbyPressingJumpKey = false;
            entitySeat.isRiddenbyPressingSneakKey = false;
            entitySeat.isRiddenbyDoublePressedFwdKey = false;
            entitySeat.isRiddenbyDoublePressedBwdKey = false;
            entitySeat.isRiddenbyDoublePressedLftKey = false;
            entitySeat.isRiddenbyDoublePressedRgtKey = false;
            entitySeat.isRiddenbyDoublePressedJumpKey = false;
            entitySeat.isRiddenbyDoublePressedSneakKey = false;

            int j;
            for (j = 0; j < entitySeat.isRiddenbyPressingExKeyList.length; ++j) {
               entitySeat.storedIsRiddenbyPressingExKeyList[j] = entitySeat.isRiddenbyPressingExKeyList[j];
               entitySeat.isRiddenbyPressingExKeyList[j] = false;
            }
            for (j = 0; j < entitySeat.isRiddenbyDoublePressedExKeyList.length; ++j) {
               entitySeat.storedIsRiddenbyDoublePressedExKeyList[j] = entitySeat.isRiddenbyDoublePressedExKeyList[j];
               entitySeat.isRiddenbyDoublePressedExKeyList[j] = false;
            }
            for (j = 0; j < entitySeat.isRiddenbyUsedItemList.length; ++j) {
               entitySeat.storedIsRiddenbyUsedItemList[j] = entitySeat.isRiddenbyUsedItemList[j];
               entitySeat.isRiddenbyUsedItemList[j] = false;
            }

            if (entitySeat.mountLivingDuration > 0) {
               --entitySeat.mountLivingDuration;
            }

            entitySeat.ringKeyDirections = 0;
            entitySeat.ringDblKeyDirections = 0;
            entitySeat.ringItemDirections = 0;
            if (entitySeat.playerReRidingSearchTick > 0) {
               --entitySeat.playerReRidingSearchTick;
               if (entitySeat.reRidePlayerUUID != null) {
                  Player player = this.level().getPlayerByUUID(entitySeat.reRidePlayerUUID);
                  if (player != null && player.level() == this.level()) {
                     if ((double)player.distanceTo(entitySeat) < 24.0) {
                        player.startRiding(entitySeat);
                        if (player instanceof ServerPlayer) {
                           ((ServerPlayer)player).connection.send(new ClientboundSetPassengersPacket(this));
                        }
                     }
                     entitySeat.reRidePlayerUUID = null;
                  }
               }
            }
         }

         if ((this.seatFlags & 1 << i) != 0) {
            entitySeat.seatEnabled = true;
         } else {
            entitySeat.seatEnabled = false;
         }

         if (!entitySeat.isPositionUpdated) {
            entitySeat.updatedJointPositionX = this.getX();
            entitySeat.updatedJointPositionY = this.getY();
            entitySeat.updatedJointPositionZ = this.getZ();
         }

         entitySeat.setPos(entitySeat.updatedJointPositionX, entitySeat.updatedJointPositionY, entitySeat.updatedJointPositionZ);
         entitySeat.setYRot(0.0F);
         entitySeat.setXRot(0.0F);
         Entity passenger = CompatHelper.getRiddenByEntity(entitySeat);
         if (passenger != null && !this.level().isClientSide) {
            if (!entitySeat.canDetach) {
               passenger.setShiftKeyDown(false);
            }

            // 1.12 called Entity#updateRidden (func_70098_U); 1.20 has no counterpart - riders are
            // positioned by EntitySeatPart#positionRider, only the motion reset is kept.
            passenger.setDeltaMovement(Vec3.ZERO);
         }

         if (!this.level().isClientSide) {
            if (i == 0 && this.boardImmidiatePlayer != null && entitySeat.seatEnabled && !entitySeat.notAsSeat) {
               if ((double)entitySeat.distanceTo(this.boardImmidiatePlayer) <= 5.5) {
                  this.boardImmidiatePlayer.startRiding(entitySeat);
               }
               this.boardImmidiatePlayer = null;
            }

            AbstractUnitBase unitBase = AbstractUnitBase.getUnitBaseById(this.unitId);
            if (unitBase != null) {
               unitBase.updatePlayerEffect(this, CompatHelper.getRiddenByEntity(entitySeat));
            }
         } else {
            entitySeat.updatePlayerOperationState();
         }

         entitySeat.trackRiddenChanged();
      }
   }

   private void resetSeatUpdated() {
      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         this.entitySeatPartArray[i].isPositionUpdated = false;
      }
   }

   public static EntityJointed getEntityJointedIfPlayerIsRiding(Player player, int seatNum) {
      Entity ridingEntity = player.getVehicle();
      if (ridingEntity instanceof EntitySeatPart) {
         EntitySeatPart seat = (EntitySeatPart)ridingEntity;
         if (seat.seatNum == seatNum) {
            return seat.entityJointed;
         }
      }
      return null;
   }

   private void ensureRiddenRelease() {
      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         EntitySeatPart seatPart = this.entitySeatPartArray[i];
         if (CompatHelper.getRiddenByEntity(seatPart) != null) {
            if (CompatHelper.getRiddenByEntity(seatPart) instanceof Player && CompatHelper.getRiddenByEntity(seatPart).getVehicle() instanceof EntitySeatPart) {
               this.detachPlayerMPToDisassembledSeatBlock(seatPart);
            } else {
               CompatHelper.getRiddenByEntity(seatPart).stopRiding();
            }
         }
      }
   }

   private void detachPlayerMPToDisassembledSeatBlock(EntitySeatPart seatPart) {
      Player entityPlayer = (Player)CompatHelper.getRiddenByEntity(seatPart);
      if (!this.level().isClientSide) {
         entityPlayer.moveTo(seatPart.updatedJointPositionX, seatPart.updatedJointPositionY + 0.2, seatPart.updatedJointPositionZ);
      }
      if (entityPlayer.isPassenger()) {
         entityPlayer.stopRiding();
      }
      if (entityPlayer instanceof ServerPlayer) {
         ServerPlayer serverPlayer = (ServerPlayer)entityPlayer;
         serverPlayer.connection.send(new ClientboundSetPassengersPacket(this));
         serverPlayer.connection.teleport(entityPlayer.getX(), entityPlayer.getY(), entityPlayer.getZ(), entityPlayer.getYRot(), entityPlayer.getXRot());
      }
   }

   @Override
   public float getPickRadius() {
      return Float.NaN;
   }

   @Override
   public boolean hurt(DamageSource damegeSource, float damage) {
      Entity entityDs = damegeSource.getEntity();
      if (entityDs != null) {
         for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
            EntitySeatPart seatPart = this.entitySeatPartArray[i];
            if (seatPart != null && CompatHelper.getRiddenByEntity(seatPart) != null && CompatHelper.getRiddenByEntity(seatPart).equals(entityDs)) {
               return false;
            }
         }
         if (entityDs.equals(this.getMasterEntityIfConnected())) {
            return false;
         }
      }
      return super.hurt(damegeSource, damage);
   }

   @Override
   public void knockback(double strength, double x, double z) {
      if (!AssembleFlagsUtil.noMove(this.assembleFlags)) {
         super.knockback(strength, x, z);
      }
   }

   @Override
   public boolean isPushable() {
      return !AssembleFlagsUtil.noMove(this.assembleFlags) && !this.isUnderMasterControl();
   }

   @Override
   public boolean isInWall() {
      if (this.isMasterEntityConnected()) {
         return false;
      } else {
         for (int i = 0; i < 8; ++i) {
            float xp = ((float)(i >> 0 & 1) - 0.5F) * 0.4F * 0.5F;
            float yp = ((float)(i >> 1 & 1) - 0.5F) * 0.1F;
            float zp = ((float)(i >> 2 & 1) - 0.5F) * 0.4F * 0.5F;
            int xx = Mth.floor(this.getX() + (double)xp);
            int yy = Mth.floor(this.getY() + (double)this.getEyeHeight() + 0.5 + (double)yp);
            int zz = Mth.floor(this.getZ() + (double)zp);
            if (WorldHelper.getBlockState(this.level(), xx, yy, zz).canOcclude()) {
               return true;
            }
         }
         return false;
      }
   }

   @Override
   public boolean canBreatheUnderwater() {
      return true;
   }

   /**
    * 1.12.2 override of EntityLivingBase#damageEntity(func_70665_d) only called super and is
    * therefore omitted: 1.20.1's equivalent is LivingEntity#actuallyHurt.
    */

   @Override
   public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
      if (fallDistance > 1.0F) {
         this.playSound(SoundEvents.GENERIC_BIG_FALL, 0.4F, 1.0F);
      }
      // Original fall() never called super -> no fall damage is applied.
      return false;
   }

   @Override
   public boolean onClimbable() {
      return false;
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      try {
         this.ensureRiddenRelease();
         this.discard();
         int len = this.refLists.length;
         for (int i = 0; i < len; ++i) {
            if (this.refLists[i] != null) {
               this.refLists[i].breakChane();
            }
         }
         this.dropItemsInChest(this.innerChest);
      } catch (Exception var4) {
      }
   }

   @Override
   protected void pushEntities() {
      if (this.isPushable()) {
         List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.20000000298023224, 0.0, 0.20000000298023224));
         if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
               Entity entity = list.get(i);
               if (!this.isRidingThisEntity(entity) && entity.isPushable()) {
                  entity.push(this);
               }
            }
         }
      }
   }

   @Override
   public void push(Entity entity) {
      if (!this.isRidingThisEntity(entity) && this.hasInitEnd) {
         double d0 = entity.getX() - this.getX();
         double d1 = entity.getZ() - this.getZ();
         double d2 = Mth.absMax(d0, d1);
         if (d2 >= 0.009999999776482582) {
            d2 = (double)Mth.sqrt((float)d2);
            d0 /= d2;
            d1 /= d2;
            double d3 = 1.0 / d2;
            if (d3 > 1.0) {
               d3 = 1.0;
            }
            d0 *= d3;
            d1 *= d3;
            d0 *= 0.05000000074505806;
            d1 *= 0.05000000074505806;
            d0 *= (double)(1.0F - this.entityCollisionReduction);
            d1 *= (double)(1.0F - this.entityCollisionReduction);
            this.push(-d0, 0.0, -d1);
            entity.push(d0, 0.0, d1);
         }
      }
   }

   public boolean isRidingOrBeingRiddenBy(Entity entityIn) {
      return this.hasPassenger(entityIn) || entityIn.hasPassenger(this) || this.isRelatedToThisEntity(entityIn);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void lerpTo(double posX, double posY, double posZ, float rotationYaw, float rotationPitch, int posRotationIncrements, boolean teleport) {
      if (!this.disassemble) {
         this.lerpX = posX;
         this.lerpY = posY;
         this.lerpZ = posZ;
         if (this.rotationYawReceived != Float.MAX_VALUE) {
            this.lerpYRot = (double)this.rotationYawReceived;
         } else {
            this.lerpYRot = (double)rotationYaw;
         }
         this.lerpXRot = (double)rotationPitch;
         this.lerpSteps = posRotationIncrements;
      }
      this.hasReceivedFirstPosRow = true;
   }

   /**
    * 1.12.2 override of Entity#getBrightnessForRender(func_70070_b) is omitted - deferred Phase 4:
    * 1.20.1 has no Entity-level brightness hook (light is resolved by EntityRenderer).
    */

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.IRON_GOLEM_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.IRON_GOLEM_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos pos, BlockState state) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
   }

   @Override
   public void playSound(SoundEvent soundIn, float volume, float pitch) {
      if (!this.level().isClientSide) {
         if (!this.isSilent()) {
            EffectSlot soundSlot = null;
            int soundSlotNum = -1;
            if (soundIn == SoundEvents.IRON_GOLEM_STEP) {
               soundSlot = this.effectSlots[EffectSlot.SOUND_STEP_SLOT_NUM];
               soundSlotNum = EffectSlot.SOUND_STEP_SLOT_NUM;
            } else if (soundIn == SoundEvents.GENERIC_BIG_FALL) {
               soundSlot = this.effectSlots[EffectSlot.SOUND_FALL_SLOT_NUM];
               soundSlotNum = EffectSlot.SOUND_FALL_SLOT_NUM;
            } else if (soundIn == SoundEvents.IRON_GOLEM_HURT) {
               soundSlot = this.effectSlots[EffectSlot.SOUND_HURT_SLOT_NUM];
               soundSlotNum = EffectSlot.SOUND_HURT_SLOT_NUM;
            } else if (soundIn == SoundEvents.IRON_GOLEM_DEATH) {
               soundSlot = this.effectSlots[EffectSlot.SOUND_DEATH_SLOT_NUM];
               soundSlotNum = EffectSlot.SOUND_DEATH_SLOT_NUM;
            } else if (soundIn == SoundEvents.GENERIC_SWIM) {
               soundSlot = this.effectSlots[EffectSlot.SOUND_SWIM_SLOT_NUM];
               soundSlotNum = EffectSlot.SOUND_SWIM_SLOT_NUM;
            }

            if (soundSlot != null && !soundSlot.isParticle() && soundSlot.isAsDefault()) {
               JBPacketSender.sendEntityJBSoundToPlayers(this, soundSlotNum);
               return;
            }

            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundIn, this.getSoundSource(), volume, pitch);
         }
      }
   }

   @Override
   public byte getUnitId() {
      return this.unitId;
   }

   @Override
   public byte getUnitFacing() {
      return (byte)this.coreFacing;
   }

   @Override
   public float getEntityEnergy() {
      return this.getEnergy();
   }

   @Override
   public boolean useEntityEnergy(float energy) {
      if (energy > this.getEnergy()) {
         return false;
      } else {
         this.setEnergy(this.getEnergy() - energy);
         return true;
      }
   }

   @Override
   public float getEntityMaxEnergy() {
      return this.getMaxEnergy();
   }

   @Override
   public float getEntityCoreHeight() {
      return this.coreHeight;
   }

   @Override
   public double getSpinRate() {
      return (double)this.spinningRate;
   }

   public Player getRidingPlayer(int seatNum) {
      if (seatNum >= 0 && seatNum < this.entitySeatPartArray.length) {
         Entity entity = CompatHelper.getRiddenByEntity(this.entitySeatPartArray[seatNum]);
         if (entity instanceof Player) {
            return (Player)entity;
         }
      }
      return null;
   }

   public boolean isRidingPlayerGenericKeyPressed(int seatNum, int keyNum, boolean askDoublePressed) {
      EntityJointed getFrom;
      if (this.isSlaveOpIndependent()) {
         getFrom = this;
      } else {
         getFrom = this.getMasterIfConnected();
      }
      if (seatNum >= 0 && seatNum < getFrom.entitySeatPartArray.length && keyNum >= 0 && keyNum < 16) {
         EntitySeatPart entitySeat = getFrom.entitySeatPartArray[seatNum];
         return askDoublePressed ? entitySeat.isRiddenbyDoublePressedExKey(keyNum) : entitySeat.isRiddenbyPressingExKey(keyNum);
      } else {
         return false;
      }
   }

   public boolean isRidingThisEntity(Entity entity) {
      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         EntitySeatPart seatPart = this.entitySeatPartArray[i];
         if (CompatHelper.getRiddenByEntity(seatPart) == entity) {
            return true;
         }
      }
      return false;
   }

   public boolean isRelatedToThisEntity(Entity entity) {
      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         EntitySeatPart seatPart = this.entitySeatPartArray[i];
         if (seatPart == entity || CompatHelper.getRiddenByEntity(seatPart) == entity) {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean isFriendlyEntity(Entity entity) {
      if (entity == null) {
         return false;
      } else if (this.isRelatedToThisEntity(entity)) {
         return true;
      } else {
         if (this.iffTagId != 0L || this.msbTagId != 0L) {
            Entity ridingEntityV = entity.getVehicle();
            if (ridingEntityV instanceof EntitySeatPart) {
               entity = ridingEntityV;
            }
            if (entity instanceof EntitySeatPart && ((EntitySeatPart)entity).entityJointed != null) {
               entity = ((EntitySeatPart)entity).entityJointed;
            }
            if (entity instanceof EntityJointed) {
               EntityJointed ej = (EntityJointed)entity;
               if (ej.iffTagId == this.iffTagId || ej.msbTagId == this.msbTagId) {
                  return true;
               }
               Player operator = ej.getRidingPlayer(0);
               if (operator != null) {
                  entity = operator;
               }
            }
            if (entity instanceof Player && this.doesPlayerHavaSameIffOrMsb((Player)entity)) {
               return true;
            }
         }
         return false;
      }
   }

   public boolean doesPlayerHavaSameIffOrMsb(Player entityPlayer) {
      NonNullList<ItemStack> inv = entityPlayer.getInventory().items;
      int len = 9;
      for (int i = 0; i < len; ++i) {
         if (ItemJBIFFTag.isStackMSBWithSameId(inv.get(i), this.msbTagId)) {
            return true;
         }
         if (ItemJBIFFTag.isStackIFFWithSameId(inv.get(i), this.iffTagId)) {
            return true;
         }
      }
      return false;
   }

   public Entity getCameraForNum(int seatNum0to3, boolean ignoreEnabled) {
      return seatNum0to3 < 0 || seatNum0to3 >= this.entitySeatPartArray.length || !ignoreEnabled && !this.entitySeatPartArray[seatNum0to3].seatEnabled ? null : this.entitySeatPartArray[seatNum0to3].entityCamera;
   }

   public void setFlyMoveDirectionFromCore(ModelViewMatrix matrix) {
      if (this.exFlyAttr != null) {
         this.exFlyAttr.facingTwardsVec = new Vec3((double)(-matrix.m02), (double)(-matrix.m12), (double)(-matrix.m22)).normalize();
      }
   }

   private void applyCoreRotRollPitch(float addPitch, float addRoll) {
      if (this.exFlyAttr != null) {
         Quaternion qRotRoll;
         if (addPitch != 0.0F) {
            qRotRoll = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)addPitch), 1.0, 0.0, 0.0);
            Quaternion.mul(this.exFlyAttr.coreRotQuats, qRotRoll, this.exFlyAttr.coreRotQuats);
            this.syncUpdateCoreRotation = true;
         }
         if (addRoll != 0.0F) {
            qRotRoll = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)addRoll), 0.0, 0.0, 1.0);
            Quaternion.mul(this.exFlyAttr.coreRotQuats, qRotRoll, this.exFlyAttr.coreRotQuats);
            this.syncUpdateCoreRotation = true;
         }
         this.fixEntityYawIfNeed();
      }
   }

   private void applyCoreRotYaw(float addYaw) {
      if (this.exFlyAttr != null) {
         if (addYaw != 0.0F) {
            Quaternion qRotYaw = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)(-addYaw)), 0.0, 1.0, 0.0);
            Quaternion.mul(this.exFlyAttr.coreRotQuats, qRotYaw, this.exFlyAttr.coreRotQuats);
            this.syncUpdateCoreRotation = true;
         }
         this.fixEntityYawIfNeed();
      }
   }

   private void resetCoreRotToLiftPitch(float angle) {
      if (this.exFlyAttr != null) {
         this.exFlyAttr.coreRotQuats.setFromAxisAngle(Math.toRadians((double)(-this.exFlyAttr.rotationYawUnfixed)), 0.0, 1.0, 0.0);
         Quaternion qRotPitch = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)angle), 1.0, 0.0, 0.0);
         Quaternion.mul(this.exFlyAttr.coreRotQuats, qRotPitch, this.exFlyAttr.coreRotQuats);
         this.syncUpdateCoreRotation = true;
         this.fixEntityYawIfNeed();
      }
   }

   private void fixEntityYawIfNeed() {
      if (this.syncUpdateCoreRotation) {
         this.exFlyAttr.rotationYawUnfixed = this.exFlyAttr.coreRotQuats.getRotationYAxis();
         this.setYRot(this.exFlyAttr.rotationYawUnfixed);
      }
   }

   private void slowlyResetCoreRotation(float descRate) {
      if (this.exFlyAttr != null) {
         Quaternion reset = this.exFlyAttr.workCoreRotQuats.setFromAxisAngle(Math.toRadians((double)(-this.exFlyAttr.rotationYawUnfixed)), 0.0, 1.0, 0.0);
         if (!reset.equals(this.exFlyAttr.coreRotQuats)) {
            Quaternion.slerp(descRate, this.exFlyAttr.coreRotQuats, reset, this.exFlyAttr.coreRotQuats);
            this.syncUpdateCoreRotation = true;
         }
         this.fixEntityYawIfNeed();
      }
   }

   public void receiveTuningAction(Player player, short jointRefId, byte additionalBarNum, byte additionalBarOffset, byte additionalBarSide) {
      if (this.createdTuningCommandIntickCount < ACCEPTABLE_TUNINGS_IN_TICK) {
         if (player != null && this.hasInitEnd && !this.disassemble) {
            if (this.tuningCommandList == null) {
               this.tuningCommandList = new TuningCommand[ACCEPTABLE_TUNINGS_IN_TICK];
            }

            TuningCommand acctepted;
            if ((acctepted = this.tuningCommandList[this.createdTuningCommandIntickCount]) == null) {
               this.tuningCommandList[this.createdTuningCommandIntickCount] = acctepted = new TuningCommand();
            }

            if (acctepted.createCommandFromTuningInf(this, jointRefId, additionalBarNum, additionalBarOffset, additionalBarSide)) {
               ++this.createdTuningCommandIntickCount;
            }
         }
      }
   }

   public JointParts getJointPartsFromRefId(int jointRefId) {
      return this.refLists != null && jointRefId >= 0 && jointRefId < this.refLists.length ? this.refLists[jointRefId] : null;
   }

   public void receiveTuningActionAtClient(short refId, byte initLayer, byte initParamIndex, short newValue) {
      JointParts elem = this.refLists[refId];
      if (elem != null && !elem.isDestructed) {
         elem.updateAndForceInitValue(initLayer, initParamIndex, newValue, 126);
      }
   }

   public boolean isForceFirstPersonViewWhenBoard() {
      return (this.boardViewFlags & FORCE_FPS_VIEW) != 0;
   }

   public boolean isChangeThirdPersonViewDirtanceFar() {
      return (this.boardViewFlags & FORCE_TPSV_FAR) != 0;
   }

   public int getThirdPersonViewFarLevel() {
      return this.boardViewFlags & TPSV_FAR_LEVEL;
   }

   @Override
   public boolean isSlaveMode() {
      return AssembleFlagsUtil.isSlaveMode(this.assembleFlags);
   }

   public boolean isSlaveMotIndependent() {
      return AssembleFlagsUtil.isSlaveMotIndependent(this.assembleFlags);
   }

   public boolean isSlaveOpIndependent() {
      return AssembleFlagsUtil.isSlaveOpIndependent(this.assembleFlags);
   }

   public static boolean isItMotIndependant(LivingEntity entityLivingBase) {
      if (entityLivingBase instanceof EntityJointed) {
         EntityJointed ej = (EntityJointed)entityLivingBase;
         if (ej.isMasterEntityConnected() && !ej.isSlaveMotIndependent()) {
            return false;
         }
      }
      return true;
   }

   public boolean isMasterConnected() {
      return this.getMasterIfConnected() != this;
   }

   public boolean isMasterEntityConnected() {
      return this.getMasterEntityIfConnected() != this;
   }

   public boolean isMasterTeConnected() {
      return this.getMasterTeIfConnected() != null;
   }

   public boolean hasSameMBSHash(Entity entity) {
      if (entity instanceof Player) {
         Player entityPlayer = (Player)entity;
         if (entityPlayer.getInventory() != null) {
            int i = 0;
            for (int len = entityPlayer.getInventory().getContainerSize(); i < 9 && i < len; ++i) {
               ItemStack itemStack = entityPlayer.getInventory().getItem(i);
               if (ItemJBIFFTag.isStackMSBWithSameId(itemStack, this.msbTagId)) {
                  return true;
               }
            }
         }
      } else if (entity instanceof EntityJointed) {
         EntityJointed entityJointed = (EntityJointed)entity;
         return entityJointed.msbTagId == this.msbTagId;
      }
      return false;
   }

   private void searchForMasterTargetAtServer() {
      if (this.isSlaveMode()) {
         if (this.slaveAttatcher == null && (this.forceFindMasterSearchTick > 0 || this.tickCount % 20 == 0)) {
            MasterSlaveControl.searchMasterNearBy(this);
            if (this.slaveAttatcher != null) {
               this.requestCoreUpdateForawhile();
            }
         }
      } else if (this.slaveAttatcher != null) {
         this.slaveAttatcher = null;
         this.requestCoreUpdateForawhile();
      }

      if (this.forceFindMasterSearchTick > 0) {
         --this.forceFindMasterSearchTick;
      }
   }

   public void requestCoreUpdateForawhile() {
      this.needAttracterSyncTick = REQUEST_ATTRACTER_SYNC_TOTAL;
   }

   private void updateSlaveState() {
      this.slaveAttatcher.isEnabled = false;
      if (this.level().isClientSide) {
         if (!this.slaveAttatcher.hasMasterReferenceClient(this)) {
            this.slaveAttatcher.searchAndSetupMasterReference(this, true);
         }
         if (this.slaveAttatcher.hasMasterReferenceClient(this)) {
            this.slaveAttatcher.isEnabled = true;
            this.slaveAttatcher.setPositionFromMaster(this);
         }
         this.slaveAttatcher.keepConnectionAlive();
         ++this.slaveAttatcher.tickCounter;
      } else {
         boolean disposeConnectionWithMaster = false;
         if (!this.slaveAttatcher.hasMasterReferenceServer(this) && this.slaveAttatcher.tickCounter++ % 10 == 0) {
            this.slaveAttatcher.searchAndSetupMasterReference(this, false);
         }
         if (this.slaveAttatcher.hasMasterReferenceServer(this)) {
            this.slaveAttatcher.cannotFindMasterTick = 0;
            if (this.slaveAttatcher.isConenctionValid(this)) {
               this.slaveAttatcher.isEnabled = true;
               this.slaveAttatcher.setPositionFromMaster(this);
               this.slaveAttatcher.keepConnectionAlive();
            } else {
               disposeConnectionWithMaster = true;
            }
         } else if (this.slaveAttatcher.cannotFindMasterTick++ > 200) {
            disposeConnectionWithMaster = true;
         }

         if (disposeConnectionWithMaster) {
            this.slaveAttatcher.breakConnection(this);
            this.slaveAttatcher = null;
            this.forceFindMasterSearchTick = 10;
            this.requestCoreUpdateForawhile();
         }
      }
   }

   public EntityJointed getMasterIfConnected() {
      AbstractSlaveAttatcher slaveAt = this.slaveAttatcher;
      if (slaveAt != null && slaveAt.isEnabled && slaveAt instanceof AbstractSlaveAttatcher.JointSlaveAttatcher) {
         EntityJointed master = ((AbstractSlaveAttatcher.JointSlaveAttatcher)slaveAt).entityMaster;
         if (master != null) {
            return master;
         }
      }
      return this;
   }

   public Entity getMasterEntityCheckSlaveOpIndependent() {
      return (Entity)(this.isSlaveOpIndependent() ? this : this.getMasterEntityIfConnected());
   }

   public Entity getMasterEntityIfConnected() {
      AbstractSlaveAttatcher slaveAt = this.slaveAttatcher;
      if (slaveAt != null && slaveAt.isEnabled) {
         if (slaveAt instanceof AbstractSlaveAttatcher.JointSlaveAttatcher) {
            EntityJointed masterEntityJointed = ((AbstractSlaveAttatcher.JointSlaveAttatcher)slaveAt).entityMaster;
            if (masterEntityJointed != null) {
               return masterEntityJointed;
            }
         }
         if (slaveAt instanceof AbstractSlaveAttatcher.PlayerSlaveAttatcher) {
            Player masterPlayer = ((AbstractSlaveAttatcher.PlayerSlaveAttatcher)slaveAt).entityMaster;
            if (masterPlayer != null) {
               return masterPlayer;
            }
         }
      }
      return this;
   }

   public BlockEntity getMasterTeIfConnected() {
      AbstractSlaveAttatcher slaveAt = this.slaveAttatcher;
      if (slaveAt != null && slaveAt.isEnabled && slaveAt instanceof AbstractSlaveAttatcher.TileEntitySlaveAttatcher) {
         TileEntityJointAttracter masterTe = ((AbstractSlaveAttatcher.TileEntitySlaveAttatcher)slaveAt).teAttracter;
         if (masterTe != null) {
            return masterTe;
         }
      }
      return null;
   }

   public void purgeSlaveAttatcher(short purgeTick) {
      AbstractSlaveAttatcher.setTemporaryAtattcher(this, purgeTick);
      this.requestCoreUpdateForawhile();
   }

   public void updateMasterState() {
      if (this.masterSlotHolderArray != null) {
         for (int i = 0; i < this.masterSlotHolderArray.length; ++i) {
            MasterSlot masterSlot = this.masterSlotHolderArray[i];
            if (masterSlot != null) {
               masterSlot.updateConnection();
            }
         }
      }
   }

   public boolean getMasterSlotEnableStateFlag(int slotNum) {
      return (this.getSlaveMasterFlags() & 65536 << slotNum) != 0;
   }

   public void setMasterSlotEnableStateFlag(int slotNum, boolean setOn, boolean checkDelay) {
      if (!checkDelay || !this.isInMasterSlotUpdateDelay(slotNum)) {
         this.setMasterSlotUpdateDelay(slotNum);
         int mask = 65536 << slotNum;
         int flags = this.getSlaveMasterFlags();
         if (setOn) {
            flags |= mask;
         } else {
            flags &= ~mask;
         }
         this.setSlaveMasterFlags(flags);
      }
   }

   public boolean setSlaveMode(boolean setOn, int index) {
      if (!this.level().isClientSide && this.hasInitEnd && !this.disassemble) {
         if (this.slaveSlotDelay > 0) {
            return false;
         } else {
            this.slaveSlotDelay = SLAVE_SLOT_UPDATE_DELAY;
            if (setOn) {
               if (AssembleFlagsUtil.isSlaveMode(this.assembleFlags) && this.getSlaveSlotNum() != index) {
                  this.slaveAttatcher = null;
               }
               this.assembleFlags = AssembleFlagsUtil.setSlaveMode(this.assembleFlags, true);
               this.setSlaveSlotNum(index);
               this.setCoreSubColor(true);
            } else {
               this.assembleFlags = AssembleFlagsUtil.setSlaveMode(this.assembleFlags, false);
               this.setSlaveSlotNum(0);
               this.setCoreSubColor(false);
               this.slaveAttatcher = null;
            }
            this.requestCoreUpdateForawhile();
            return true;
         }
      } else {
         return false;
      }
   }

   public void setMasterSlotUpdateDelay(int slotNum) {
      if (slotNum > 0 && slotNum < MasterSlaveControl.SLAVE_SLOT_NUM) {
         if (this.slaveSlotToggleDelay == null) {
            this.slaveSlotToggleDelay = new int[MasterSlaveControl.SLAVE_SLOT_NUM];
         }
         this.slaveSlotDelay = SLAVE_SLOT_UPDATE_DELAY;
         this.slaveSlotToggleDelay[slotNum] = SLAVE_SLOT_UPDATE_DELAY;
      }
   }

   public boolean isInMasterSlotUpdateDelay(int slotNum) {
      if (slotNum > 0 && slotNum < MasterSlaveControl.SLAVE_SLOT_NUM) {
         if (this.slaveSlotToggleDelay == null) {
            this.slaveSlotToggleDelay = new int[MasterSlaveControl.SLAVE_SLOT_NUM];
         }
         return this.slaveSlotToggleDelay[slotNum] > 0;
      } else {
         return true;
      }
   }

   public boolean changeSlaveSlotNum(int toMode) {
      if (toMode < 0) {
         return !this.isSlaveMode() ? true : this.setSlaveMode(false, 0);
      } else if (toMode > MasterSlaveControl.MAX_SLAVE_SLOT_INDEX) {
         return false;
      } else {
         return this.isSlaveMode() && this.getSlaveSlotNum() == toMode ? true : this.setSlaveMode(true, toMode);
      }
   }

   /**
    * 1.12.2 World accessor func_82194_d is dropped: 1.20.1 has Entity#level(), and the only caller
    * (EntitySeatPart's constructor) now uses the parent's level directly.
    * func_70965_a(attackEntityFromPart) likewise has no 1.20.1 counterpart; EntitySeatPart#hurt
    * forwards damage to this entity.
    */

   public void setReferenceLists(JointParts[] referenceAry, JointParts[] motionReferenceAry) {
      this.refLists = referenceAry;
      this.motionRefLists = motionReferenceAry;
   }

   public void setMinMaxY(int minY, int maxY) {
      this.minYSavedForDisassemble = minY;
   }

   @Override
   public void createBlocksFromRefLists(IJointListsHolder.BlockArranger arranger, int facing, int posX, int posY, int posZ) {
      int len = this.refLists.length;
      for (int i = 0; i < len; ++i) {
         if (this.refLists[i] != null) {
            this.refLists[i].turnIntoBlock(arranger, this, facing, posX, posY, posZ);
         }
      }
   }

   public void createCoreBlockFromRefLists(IJointListsHolder.BlockArranger arranger, int facing, int posX, int posY, int posZ) {
      if (this.refLists.length != 0) {
         this.refLists[0].turnIntoBlock(arranger, (EntityJointed)null, facing, posX, posY, posZ);
      }
   }

   private void createInventory() {
      this.innerChest = new JointInventory(this, "jointGui.unitinventory", JointInventory.UNIT_INVENTORY_NUM);
   }

   @Override
   public JointInventory getInnerInventory() {
      return this.innerChest;
   }

   public boolean isInventoryCurrentlyUsable(Player player) {
      return !this.isRemoved() && !this.disassemble && this.hasInitEnd;
   }

   private void dropItemsInChest(SimpleContainer inventory) {
      if (!this.disassemble) {
         if (inventory != null && !this.level().isClientSide) {
            for (int i = 0; i < inventory.getContainerSize(); ++i) {
               ItemStack itemstack = inventory.getItem(i);
               if (!CompatHelper.isStackEmptyOrNull(itemstack)) {
                  this.spawnAtLocation(itemstack, 0.0F);
               }
            }
         }
      }
   }

   public boolean hasTarget() {
      for (int i = 0; i < this.targetList.length; ++i) {
         if (this.targetList[i] != null) {
            return true;
         }
      }
      return false;
   }

   public void clearTargetList() {
      for (int i = 0; i < this.targetList.length; ++i) {
         this.targetList[i] = null;
      }
   }

   private void sendTargetSelectUpdate() {
      int sendCnt = 0;
      for (int i = 1; i < this.targetList.length; ++i) {
         if (this.targetList[i] != null) {
            ++sendCnt;
         }
      }
      JBPacketSender.sendTargetSelectPacketToClient(this, sendCnt, this.targetList);
   }

   public void updateTargetOwner() {
      if (this.ownerUUID != null && (this.targetList[0] == null || !this.targetList[0].isTargetOwnerPlayer(this.ownerUUID))) {
         Player ownerPlayer = this.level().getPlayerByUUID(this.ownerUUID);
         if (ownerPlayer != null) {
            this.targetList[0] = new EjTarget(ownerPlayer);
         }
      }
   }

   public void updateTargetList() {
      for (int i = this.targetList.length - 1; i >= 1; --i) {
         if (this.targetList[i] != null && this.targetList[i].isTargetEntity) {
            this.targetList[i].usedCountInTick = 0;
            if (this.targetList[i].targetEntity == null || this.targetList[i].targetEntity.isRemoved()) {
               this.targetList[i] = null;
               this.needToSendTargetUpdate = true;
            }
         }
      }
   }

   public Entity getNearestTargetEntityUnused(double posX, double posY, double posZ) {
      EjTarget nearest = null;
      int usedCountMin = Integer.MAX_VALUE;
      double distMinSq = Double.MAX_VALUE;
      for (int i = 1; i < this.targetList.length; ++i) {
         EjTarget target = this.targetList[i];
         if (target != null && target.isTargetEntity && target.targetEntity != null && target.targetEntity instanceof LivingEntity) {
            Entity entity = target.targetEntity;
            if (target.usedCountInTick <= usedCountMin) {
               double diffX = entity.getX() - posX;
               double diffY = entity.getY() - posY;
               double diffZ = entity.getZ() - posZ;
               // Kept verbatim from the 1.12.2 source (including the diffX*diffY term).
               double distSq = diffX * diffY + diffX * diffY + diffZ * diffZ;
               if (target.usedCountInTick == usedCountMin) {
                  if (distMinSq > distSq) {
                     nearest = target;
                     usedCountMin = target.usedCountInTick;
                     distMinSq = distSq;
                  }
               } else {
                  nearest = target;
                  usedCountMin = target.usedCountInTick;
                  distMinSq = distSq;
               }
            }
         }
      }

      if (nearest != null) {
         ++nearest.usedCountInTick;
         return nearest.targetEntity;
      } else {
         return null;
      }
   }

   public void setMatrixToSensor(JointParts jp, ModelViewMatrix matrix, int sensorNum) {
      if (this.sensorList != null && sensorNum >= 0 && sensorNum < this.sensorList.length) {
         EjSensor sensor = this.sensorList[sensorNum];
         if (sensor == null) {
            sensor = this.sensorList[sensorNum] = new EjSensor(jp);
         }
         if (sensor.joint != jp) {
            return;
         }
         matrix.tempPushMatrix();
         matrix.translate(0.0F, 1.0F, 0.0F);
         matrix.copyTo(sensor);
         sensor.shift((float)matrix.baseEntityPosX, (float)matrix.baseEntityPosY, (float)matrix.baseEntityPosZ);
         matrix.tempPopMatrix();
      }
   }

   public void setupUnitHealthEnergy(byte mainUnitId2) {
      AbstractUnitBase unitBase = AbstractUnitBase.getUnitBaseById(this.unitId);
      if (unitBase != null) {
         unitBase.setUnitHealthStatesAtServer(this);
      }
      this.addonBonus.applyAddonBonusHealthAndEnergy(this);
   }

   public void setupUnitAbilityFromUnitId(int unitId) {
      AbstractUnitBase unitBase = AbstractUnitBase.getUnitBaseById(unitId);
      this.unitAbility.clearMovementStates();
      if (unitBase != null) {
         unitBase.setUnitMovementAbility(this.unitAbility);
         unitBase.setUnitMovementStates(this.unitAbility);
      }
      this.addonBonus.applyAddonBonusMovement(this.unitAbility);
      if (this.exFlyAttr != null) {
         this.syncUpdateCoreRotation = true;
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      this.hasInitEnd = tag.getBoolean("hasInitEnd");
      this.coreHeight = tag.getFloat("coreHeight");
      this.totalHeight = tag.getFloat("totalHeight");
      this.largeUnitWeight = tag.getFloat("largeUnitWeight");
      if (tag.contains("largeUnitWeightFixed", 1)) {
         this.largeUnitWeightFixed = (short)tag.getByte("largeUnitWeightFixed");
      } else {
         this.largeUnitWeightFixed = tag.getShort("largeUnitWeightFixed");
      }
      this.coreFacing = tag.getInt("coreFacing");
      this.seatFlags = tag.getByte("seatFlags");
      this.destructCount = tag.getInt("destructCount");
      this.ownerHash = tag.getInt("ownerHash");
      if (tag.contains("assembleFlags", 1)) {
         this.assembleFlags = tag.getByte("assembleFlags");
      } else {
         this.assembleFlags = tag.getInt("assembleFlags");
      }
      this.boardViewFlags = tag.getByte("boardViewFlags");
      this.iffTagId = tag.getLong("iffTagId");
      this.msbTagId = tag.getLong("msbTagId");
      boolean noMove = tag.getBoolean("noMove");
      if (noMove) {
         this.assembleFlags |= 2;
      }
      this.setInitLayerMode(tag.getByte("initMode"));
      this.setSlaveMasterFlags(tag.getInt("slaveMasterFlags"));
      if (this.isSlaveMode()) {
         this.setCoreSubColor(true);
      }

      if (tag.contains("innerChest")) {
         this.innerChest.readFromNBT(tag.getList("innerChest", 10));
         this.innerChest.setConsumedCashe(tag.getFloat("consumedCashe"));
      }

      if (tag.contains("effectSlots")) {
         EffectSlot.readEffectSlotsDataFromNBT(this.effectSlots, tag.getList("effectSlots", 10));
      }

      if (tag.contains("ringPrograms", 10)) {
         CompoundTag subtag = tag.getCompound("ringPrograms");
         this.readProgramsFromNBT(subtag);
         if (tag.contains("activeRingSlots", 10) && this.activeRingSlots != null) {
            subtag = tag.getCompound("activeRingSlots");
            this.activeRingSlots.readFromNBT(subtag, this);
         }
      }

      if (tag.contains("OwnerUUIDMost", 4)) {
         this.ownerUUID = new UUID(tag.getLong("OwnerUUIDMost"), tag.getLong("OwnerUUIDLeast"));
      }

      this.unitId = tag.getByte("unitId");
      if (!tag.contains("mainUnitId", 1)) {
         this.mainUnitId = this.unitId;
      } else {
         this.mainUnitId = tag.getByte("mainUnitId");
      }

      this.addonBonus = new AddonBonus();
      this.addonBonus.readFromNBT(tag);
      this.setupUnitAbilityFromUnitId(this.unitId);
      if (tag.contains("ja.maxJBHealth", 2)) {
         this.maxJBHealth = tag.getShort("ja.maxJBHealth");
         this.maxJBEnergy = tag.getShort("ja.maxJBEnergy");
      } else {
         this.maxJBHealth = tag.getShort("maxJBHealth");
         this.maxJBEnergy = tag.getShort("maxJBEnergy");
      }

      this.setupAttributeFromUnitAblility();
      this.setHealth(tag.getFloat("health"));
      this.setEnergy(tag.getFloat("energy"));
      this.setEFuelPool(tag.getFloat("epool"));
      JBCarvingDataUtil.getPaletteDataFromNBT(this.paletteDataList, "paletteDataList", tag);
      byte formatVer = tag.getByte("formatVer");
      this.slaveAttatcher = AbstractSlaveAttatcher.createFromNBT(tag);

      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         if (tag.contains(PLAYER_PASSENGER_MOST_KEY[i], 4)) {
            UUID uuidPlayer = tag.getUUID(PLAYER_PASSENGER_KEY[i]);
            if (uuidPlayer != null) {
               this.entitySeatPartArray[i].reRidePlayerUUID = uuidPlayer;
               this.entitySeatPartArray[i].playerReRidingSearchTick = 240;
            }
         }
      }

      if (tag.contains("Passengers", 9)) {
         ListTag taglist = tag.getList("Passengers", 10);
         for (int i = 0; i < taglist.size(); ++i) {
            CompoundTag tagPassenger = taglist.getCompound(i);
            if (tagPassenger != null) {
               byte num = tagPassenger.getByte("jb:_:psnum");
               if (this.seatReMountArray == null) {
                  this.seatReMountArray = new LinkedList<Byte>();
               }
               this.seatReMountArray.addLast(num);
            }
         }
      }

      try {
         this.partsCount = tag.getInt("partsCount");
         if (formatVer != JOINT_DATA_FORMAT_VER_1_0) {
            this.discard();
            return;
         }
         this.jointParts = JointDataSerializer.rebuildAll(tag.getIntArray("jointParts"), this.partsCount, this);
         if (this.jointParts == null) {
            this.discard();
         }
         this.setSizeWithoutPosChange();
         if (tag.contains("unitName")) {
            this.unitName = tag.getString("unitName");
         } else {
            this.unitName = CompatHelper.formatLang("jointGui.info.nonameunit");
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         this.discard();
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      tag.putBoolean("hasInitEnd", this.hasInitEnd);
      tag.putFloat("coreHeight", this.disassemble ? this.preDASavedCoreHeight : this.coreHeight);
      tag.putFloat("totalHeight", this.totalHeight);
      tag.putFloat("largeUnitWeight", this.largeUnitWeight);
      tag.putShort("largeUnitWeightFixed", this.largeUnitWeightFixed);
      tag.putInt("coreFacing", this.coreFacing);
      tag.putByte("seatFlags", this.seatFlags);
      tag.putInt("destructCount", this.destructCount);
      tag.putInt("ownerHash", this.ownerHash);
      tag.putInt("assembleFlags", this.assembleFlags);
      tag.putByte("boardViewFlags", this.boardViewFlags);
      tag.putLong("iffTagId", this.iffTagId);
      tag.putLong("msbTagId", this.msbTagId);
      tag.putByte("initMode", this.getInitLayerMode());
      tag.putInt("slaveMasterFlags", this.getSlaveMasterFlags());
      if (this.innerChest != null) {
         tag.put("innerChest", this.innerChest.writeToNBT(new ListTag()));
         tag.putFloat("consumedCashe", this.innerChest.getConsumedCashe());
      }

      tag.put("effectSlots", EffectSlot.writeEffectSlotsDataToNBT(this.effectSlots, new ListTag()));
      if (this.ringPrograms != null) {
         CompoundTag subtag = new CompoundTag();
         this.writeProgramsToNBT(subtag);
         tag.put("ringPrograms", subtag);
         if (this.activeRingSlots != null) {
            subtag = new CompoundTag();
            this.activeRingSlots.saveToNBT(subtag);
            tag.put("activeRingSlots", subtag);
         }
      }

      if (this.ownerUUID != null) {
         tag.putLong("OwnerUUIDMost", this.ownerUUID.getMostSignificantBits());
         tag.putLong("OwnerUUIDLeast", this.ownerUUID.getLeastSignificantBits());
      }

      tag.putByte("unitId", this.unitId);
      tag.putByte("mainUnitId", this.mainUnitId);
      this.addonBonus.writeToNBT(tag);
      tag.putShort("maxJBHealth", this.maxJBHealth);
      tag.putShort("maxJBEnergy", this.maxJBEnergy);
      tag.putFloat("health", this.getHealth());
      tag.putFloat("energy", this.getEnergy());
      tag.putFloat("epool", this.getEFuelPool());
      JBCarvingDataUtil.setPaletteDataToNBT(this.paletteDataList, "paletteDataList", tag);
      tag.putByte("formatVer", JOINT_DATA_FORMAT_VER_1_0);
      AbstractSlaveAttatcher.saveToNBT(this.slaveAttatcher, tag);
      ListTag psListTag = null;

      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         EntitySeatPart sp = this.entitySeatPartArray[i];
         Entity riddenBy = CompatHelper.getRiddenByEntity(sp);
         if (riddenBy instanceof Player) {
            tag.putUUID(PLAYER_PASSENGER_KEY[i], ((Player)riddenBy).getUUID());
         } else if (riddenBy != null) {
            CompoundTag riddenByNBT = new CompoundTag();
            double savedPosX = riddenBy.getX();
            double savedPosZ = riddenBy.getZ();
            riddenBy.setPos(this.getX(), riddenBy.getY(), this.getZ());
            if (riddenBy.save(riddenByNBT)) {
               if (psListTag == null) {
                  psListTag = new ListTag();
               }
               riddenByNBT.putByte("jb:_:psnum", (byte)i);
               psListTag.add(riddenByNBT);
            }
            riddenBy.setPos(savedPosX, riddenBy.getY(), savedPosZ);
         }
      }

      if (psListTag != null && !psListTag.isEmpty()) {
         tag.put("Passengers", psListTag);
      }

      try {
         tag.putInt("partsCount", this.partsCount);
         tag.putIntArray("jointParts", JointDataSerializer.serializeAll(this.refLists, this.partsCount));
         if (this.unitName != null && this.unitName.length() > 0) {
            tag.putString("unitName", this.unitName);
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }
   }

   @Override
   public void writeSpawnData(FriendlyByteBuf buffer) {
      int[] data;
      try {
         data = JointDataSerializer.serializeAll(this.refLists, this.partsCount);
      } catch (Exception var5) {
         var5.printStackTrace();
         data = new int[]{0};
      }

      buffer.writeBoolean(this.hasInitEnd);
      buffer.writeFloat(this.coreHeight);
      buffer.writeFloat(this.totalHeight);
      buffer.writeFloat(this.largeUnitWeight);
      buffer.writeInt(this.coreFacing);
      buffer.writeByte(this.seatFlags);
      buffer.writeInt(this.destructCount);
      buffer.writeInt(this.ownerHash);
      buffer.writeInt(this.assembleFlags);
      buffer.writeByte(this.boardViewFlags);
      buffer.writeLong(this.iffTagId);
      buffer.writeLong(this.msbTagId);
      buffer.writeByte(this.getInitLayerMode());
      buffer.writeInt(this.getSlaveMasterFlags());
      buffer.writeByte(this.unitId);
      buffer.writeByte(this.mainUnitId);
      this.addonBonus.writeSpawnData(buffer);
      buffer.writeShort(this.maxJBHealth);
      buffer.writeShort(this.maxJBEnergy);
      buffer.writeFloat(this.getHealth());
      buffer.writeFloat(this.getEnergy());
      buffer.writeFloat(this.getEFuelPool());

      int len;
      for (len = 0; len < this.entitySeatPartArray.length; ++len) {
         buffer.writeInt(this.entitySeatPartArray[len].getId());
      }

      JBCarvingDataUtil.writePaletteDataToBBuf(this.paletteDataList, buffer);
      if (this.slaveAttatcher != null && this.slaveAttatcher.hasMasterReferenceServer(this)) {
         buffer.writeByte(this.slaveAttatcher.typeId);
         this.slaveAttatcher.writeSpawnData(buffer);
      } else {
         buffer.writeByte(0);
      }

      EffectSlot.writeEffectSlotsDataToBBuf(this.effectSlots, buffer);

      try {
         buffer.writeInt(this.partsCount);
         buffer.writeInt(data.length);
         len = data.length;
         for (int i = 0; i < len; ++i) {
            buffer.writeInt(data[i]);
         }
         byte[] strBytes = JBCommonUtil.createByteAryFromNameString(this.unitName);
         if (strBytes.length == 0) {
            buffer.writeByte(0);
         } else {
            buffer.writeByte(strBytes.length);
            buffer.writeBytes(strBytes);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }


   /**
    * 1.12.2 Forge routed entity spawn packets through {@code NetworkHooks.getEntitySpawningPacket},
    * so {@link IEntityAdditionalSpawnData#writeSpawnData} ran for every tracked entity. 1.20.1
    * {@code ServerEntity} uses {@link Entity#getAddEntityPacket()}, which vanilla implements as a
    * plain {@code ClientboundAddEntityPacket} - the Forge spawn packet has to be returned here, or
    * the client never receives the joint data / palette / seat ids.
    */
   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public void readSpawnData(FriendlyByteBuf additionalData) {
      this.hasInitEnd = additionalData.readBoolean();
      this.coreHeight = additionalData.readFloat();
      this.totalHeight = additionalData.readFloat();
      this.largeUnitWeight = additionalData.readFloat();
      this.coreFacing = additionalData.readInt();
      this.seatFlags = additionalData.readByte();
      this.destructCount = additionalData.readInt();
      this.ownerHash = additionalData.readInt();
      this.assembleFlags = additionalData.readInt();
      this.boardViewFlags = additionalData.readByte();
      this.iffTagId = additionalData.readLong();
      this.msbTagId = additionalData.readLong();
      this.setInitLayerMode(additionalData.readByte());
      this.setSlaveMasterFlags(additionalData.readInt());
      this.unitId = additionalData.readByte();
      this.mainUnitId = additionalData.readByte();
      this.addonBonus = new AddonBonus();
      this.addonBonus.readSpawnData(additionalData);
      this.setupUnitAbilityFromUnitId(this.unitId);
      this.maxJBHealth = additionalData.readShort();
      this.maxJBEnergy = additionalData.readShort();
      this.setupAttributeFromUnitAblility();
      this.setHealth(additionalData.readFloat());
      this.setEnergy(additionalData.readFloat());
      this.setEFuelPool(additionalData.readFloat());

      for (int i = 0; i < this.entitySeatPartArray.length; ++i) {
         this.entitySeatPartArray[i].setId(additionalData.readInt());
      }

      JBCarvingDataUtil.readPaletteDataFromBBuf(this.paletteDataList, additionalData);
      JBCarvingDataUtil.fixPaletteByteOrder(this.paletteDataList, this.boFixedPaletteDataList);
      int attatcherTypeId = additionalData.readByte();
      if (attatcherTypeId != 0) {
         if (this.slaveAttatcher == null || attatcherTypeId != this.slaveAttatcher.typeId) {
            this.slaveAttatcher = AbstractSlaveAttatcher.createFromTypeId(attatcherTypeId);
         }
         if (this.slaveAttatcher != null) {
            this.slaveAttatcher.readSpawnData(additionalData);
         }
      } else {
         this.slaveAttatcher = null;
      }

      EffectSlot.readEffectSlotsDataFromBBuf(this.effectSlots, additionalData);

      try {
         this.partsCount = additionalData.readInt();
         int[] data = new int[additionalData.readInt()];
         int len = data.length;
         for (int i = 0; i < len; ++i) {
            data[i] = additionalData.readInt();
         }
         this.jointParts = JointDataSerializer.rebuildAll(data, this.partsCount, this);
         this.setSizeWithoutPosChange();
         byte strLen = additionalData.readByte();
         if (strLen > 0) {
            byte[] strBytes = new byte[strLen];
            additionalData.readBytes(strBytes, 0, strLen);
            this.unitName = new String(strBytes, StandardCharsets.UTF_8);
         } else {
            this.unitName = CompatHelper.formatLang("jointGui.info.nonameunit");
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      this.fixPositionYEpsilon = false;
   }

   public static void disassembleEntityJointedInRange(CommandSourceStack commandSender, int value, boolean forceDisassembleSlave) {
      if (commandSender.getEntity() instanceof ServerPlayer) {
         ServerPlayer entityPlayerMP = (ServerPlayer)commandSender.getEntity();
         AABB bb = JBCommonUtil.getBoundingBox(entityPlayerMP.getX(), entityPlayerMP.getY(), entityPlayerMP.getZ(), entityPlayerMP.getX(), entityPlayerMP.getY(), entityPlayerMP.getZ());
         List<EntityJointed> list = entityPlayerMP.level().getEntitiesOfClass(EntityJointed.class, bb.inflate((double)value, (double)value, (double)value));
         if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); ++i) {
               Entity entity = list.get(i);
               if (entity instanceof EntityJointed) {
                  EntityJointed entityJointed = (EntityJointed)entity;
                  if (!forceDisassembleSlave && JBConfig.disableMasterConnectedDisassemble) {
                     if (!entityJointed.isMasterConnected() && entityJointed.startDisassemble(false) && entityPlayerMP instanceof Player) {
                        entityJointed.disassembleConductor = entityPlayerMP;
                     }
                  } else if (entityJointed.startDisassemble(false) && entityPlayerMP instanceof Player) {
                     entityJointed.disassembleConductor = entityPlayerMP;
                  }
               }
            }
         }
      }
   }

   @Override
   public TypedLinkable getNext() {
      return this.next;
   }

   @Override
   public TypedLinkable getPrev() {
      return this.prev;
   }

   @Override
   public void setNext(TypedLinkable linkable) {
      this.next = linkable;
   }

   @Override
   public void setPrev(TypedLinkable linkable) {
      this.prev = linkable;
   }

   @Override
   public void clearRef() {
      this.next = null;
      this.prev = null;
   }

   @Override
   public boolean checkRendered(long token) {
      if (this.renderCheck == token) {
         return false;
      } else {
         this.renderCheck = token;
         return true;
      }
   }

   static {
      PLAYER_PASSENGER_KEY = new String[]{"jbplpassenger_0", "jbplpassenger_1", "jbplpassenger_2", "jbplpassenger_3"};
      PLAYER_PASSENGER_MOST_KEY = new String[]{PLAYER_PASSENGER_KEY[0] + "Most", PLAYER_PASSENGER_KEY[1] + "Most", PLAYER_PASSENGER_KEY[2] + "Most", PLAYER_PASSENGER_KEY[3] + "Most"};
   }
}
