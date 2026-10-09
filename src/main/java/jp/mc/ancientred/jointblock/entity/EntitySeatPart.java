package jp.mc.ancientred.jointblock.entity;

import com.google.common.collect.ImmutableList;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.config.JBKeyHanlder;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.item.ItemDisassembleBall;
import jp.mc.ancientred.jointblock.item.ItemJBAssemblyTuner;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;

/**
 * A seat of an {@link EntityJointed}. Part entities travel with their parent and are never registered/spawned
 * into the level, so all chunk bookkeeping of the original implementation is gone.
 */
public class EntitySeatPart extends PartEntity<EntityJointed> implements IIgnoreTarget {
   private static final int MOBMOUNT_DURATION = 60;
   public static final byte CAMERA_NUM_UNINITTIAL = -3;
   public static final byte CAMERA_NUM_REQESTAUTO = -2;
   public static final byte CAMERA_NUM_UNUSE = -1;
   public static final int USED_PREV_CNT_THRESHOLD = 3;
   public static final int KEY_DETECT_CNT = 8;
   public static final int ITEM_DETECT_CNT = 8;
   public static final int MOVE_KEY_DETECT_CNT = 8;
   public static final int KEY_DETECT_FR_NUM = 8;
   public static final int KEY_DETECT_BK_NUM = 9;
   public static final int KEY_DETECT_LF_NUM = 10;
   public static final int KEY_DETECT_RI_NUM = 11;
   public static final int KEY_DETECT_UP_NUM = 12;
   public static final int KEY_DETECT_DW_NUM = 13;
   public static final int KEY_DETECT_WHEELUP = 14;
   public static final int KEY_DETECT_WHEELDW = 15;
   private static final int MAX_SEAT = 4;
   private static final int DBL_DETECT_TICK = 9;
   public static int[] flagBits = new int[]{16, 32, 64, 128, 65536, 131072, 262144, 524288};
   public static int[] flagBitsDblKey = new int[]{4096, 8192, 16384, 32768, 1048576, 2097152, 4194304, 8388608};
   public boolean keyReceived = false;
   public boolean inputAcceptedFromOutside = false;
   public int usedPrevValueCount = 0;
   public final EntityJointed entityJointed;
   public boolean seatEnabled;
   public final int seatNum;
   public boolean isPositionUpdated;
   public boolean isOperationSeat;
   public double updatedJointPositionX;
   public double updatedJointPositionY;
   public double updatedJointPositionZ;
   public Entity prevRiddenByEntity = null;
   public boolean isRiddenbyPressingJumpKey;
   public boolean isRiddenbyDoublePressedJumpKey;
   public byte isRiddenbyDoublePressedJumpKeyTick;
   public boolean isRiddenbyPressingSneakKey;
   public boolean isRiddenbyDoublePressedSneakKey;
   public byte isRiddenbyDoublePressedSneakKeyTick;
   public boolean isRiddenbyUsingWheelUp;
   public boolean isRiddenbyUsingWheelDown;
   public boolean[] isRiddenbyPressingExKeyList = new boolean[8];
   public boolean[] storedIsRiddenbyPressingExKeyList = new boolean[16];
   public boolean[] isRiddenbyDoublePressedExKeyList = new boolean[8];
   public boolean[] storedIsRiddenbyDoublePressedExKeyList = new boolean[16];
   public byte[] isRiddenbyDoublePressedExKeyTick = new byte[8];
   public boolean isRiddenbyPressingFwdKey;
   public boolean isRiddenbyPressingBwdKey;
   public boolean isRiddenbyPressingLftKey;
   public boolean isRiddenbyPressingRgtKey;
   public boolean isRiddenbyDoublePressedFwdKey;
   public boolean isRiddenbyDoublePressedBwdKey;
   public boolean isRiddenbyDoublePressedLftKey;
   public boolean isRiddenbyDoublePressedRgtKey;
   public byte isRiddenbyDoublePressedFwdKeyTick;
   public byte isRiddenbyDoublePressedBwdKeyTick;
   public byte isRiddenbyDoublePressedLftKeyTick;
   public byte isRiddenbyDoublePressedRgtKeyTick;
   private static final int USE_ITEM_ACT_NUMS = 2;
   private static final int USE_ITEM_COLOR_TYPE_NUMS = 4;
   public boolean[] isRiddenbyUsedItemList = new boolean[8];
   public boolean[] storedIsRiddenbyUsedItemList = new boolean[8];
   public boolean canDetach;
   public boolean isAsCameraJoint;
   public boolean isAsInteractableJoint;
   public boolean notAsSeat;
   public WeakReference<JointParts> theJointPart;
   public Player keyConductor;
   public Entity entityCamera;
   public byte cameraModeSeatNum = -3;
   public boolean hidePlayer = false;
   public short mountLivingDuration;
   public short playerReRidingSearchTick;
   public UUID reRidePlayerUUID;
   public boolean updateRidersYaw = false;
   public float prevRidersYaw = Float.NaN;
   public float ridersYaw;
   public int ringKeyDirections;
   public int ringDblKeyDirections;
   public int ringItemDirections;
   private float moveForward = 0.0F;
   private float moveStrafe = 0.0F;
   private static final int OP_FLAGBIT_WAY_KEY_FWD = 1;
   private static final int OP_FLAGBIT_WAY_KEY_BWD = 2;
   private static final int OP_FLAGBIT_WAY_KEY_LFT = 4;
   private static final int OP_FLAGBIT_WAY_KEY_RGT = 8;
   private static final int OP_FLAGBIT_WAY_KEY_UPJ = 16;
   private static final int OP_FLAGBIT_WAY_KEY_DWS = 32;
   private static final int OP_FLAGBIT_WAY_DBLKEY_FWD = 256;
   private static final int OP_FLAGBIT_WAY_DBLKEY_BWD = 512;
   private static final int OP_FLAGBIT_WAY_DBLKEY_LFT = 1024;
   private static final int OP_FLAGBIT_WAY_DBLKEY_RGT = 2048;
   private static final int OP_FLAGBIT_WAY_DBLKEY_UPJ = 4096;
   private static final int OP_FLAGBIT_WAY_DBLKEY_DWS = 8192;
   private static final int OP_FLAGBIT_MOUSE_WHEEL_UP = 16384;
   private static final int OP_FLAGBIT_MOUSE_WHEEL_DW = 32768;
   public boolean cutDefaultOP;
   public boolean hasRINGMoveDR;
   public byte moveStrafingDR;
   public byte moveForwardDR;
   public byte moveUpDownDR;
   public float moveStrafingAmount;
   public float moveForwardAmount;
   public boolean moveUp;
   public boolean yawChange;
   public boolean yawAdd;
   public float yawValue;
   public boolean lockMoveDR;
   public static final byte DRKEY_NOTDOWN = 0;
   public static final byte DRKEY_SINGLE = 1;
   public static final byte DRKEY_DOUBLE = 2;
   private final EntityDimensions dimensions;

   /**
    * Fixed 1x1 dimensions, matching the original {@code func_70105_a(1.0F, 1.0F)} (the width/height arguments
    * were already ignored there). 1.20.1 has no {@code Entity.setSize(float, float)}; the dimensions are
    * supplied through {@link #getDimensions(Pose)} and applied with {@code refreshDimensions()}.
    */
   public EntitySeatPart(EntityJointed parent, int seatNum, float width, float height) {
      super(parent);
      this.dimensions = EntityDimensions.scalable(1.0F, 1.0F);
      this.refreshDimensions();
      this.entityJointed = parent;
      this.seatNum = seatNum;
   }

   @Override
   public EntityDimensions getDimensions(Pose pose) {
      return this.dimensions;
   }

   @Override
   public Entity getVehicle() {
      return this.entityJointed;
   }

   /**
    * The seat part is logically "riding" its jointed parent, but that link is implicit (both are created
    * together and live side by side), so it must never be dissolved.
    */
   @Override
   public void stopRiding() {
   }

   @Override
   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().size() < 1;
   }

   @Override
   public void tick() {
      if (this.level().isClientSide) {
         Entity entityRidenBy = CompatHelper.getRiddenByEntity(this);
         if (this.prevRiddenByEntity != entityRidenBy) {
            this.prevRiddenByEntity = entityRidenBy;
            if (entityRidenBy != null && entityRidenBy instanceof Player && this.entityJointed != null) {
               JBModContainer.proxy.onPlayerAttachedToSeat((Player)entityRidenBy, this.entityJointed);
            }
         }

         if (this.entityCamera != null) {
            this.entityCamera.tick();
         }
      }

      if (this.entityJointed == null || this.entityJointed.isRemoved() || !this.isSeatOfEntityJointed()) {
         this.discard();
         this.ejectPassengers();
      }
   }

   private boolean isSeatOfEntityJointed() {
      if (this.seatNum >= 0 && this.seatNum < this.entityJointed.entitySeatPartArray.length) {
         return this == this.entityJointed.entitySeatPartArray[this.seatNum];
      } else {
         return false;
      }
   }

   public void trackRiddenChanged() {
      if (!this.level().isClientSide) {
         Entity entityRidenBy = CompatHelper.getRiddenByEntity(this);
         if (this.prevRiddenByEntity != entityRidenBy) {
            this.prevRiddenByEntity = entityRidenBy;
            if (this.level() instanceof ServerLevel serverLevel) {
               serverLevel.getChunkSource().broadcastAndSend(this.entityJointed, new ClientboundSetPassengersPacket(this));
            }
         }
      }
   }

   /**
    * 1.20.1 replacement for {@code getRecursivePassengersByType(Class)}: the seat part must not contribute
    * anything to the recursive passenger search of its parent.
    */
   @Override
   public Iterable<Entity> getIndirectPassengers() {
      return ImmutableList.of();
   }

   public void attachAndCreateCamera() {
      if (this.entityCamera == null && this.isAsCameraJoint) {
         JBModContainer.proxy.attatchEntityCamera(this);
      }
   }

   @Override
   protected void defineSynchedData() {
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   public boolean isPickable() {
      return this.seatEnabled;
   }

   /** Forge re-added this hook on {@code IForgeEntity}; a rider must be able to interact with its own seat. */
   @Override
   public boolean canRiderInteract() {
      return true;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean hurt(DamageSource damageSource, float damage) {
      return this.entityJointed == null ? false : this.entityJointed.hurt(damageSource, damage);
   }

   private byte getSeatAryNum() {
      if (this.entityJointed != null && this.entityJointed.entitySeatPartArray != null) {
         for (int i = 0; i < this.entityJointed.entitySeatPartArray.length; ++i) {
            if (this.entityJointed.entitySeatPartArray[i] == this) {
               return (byte)i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   @Override
   public boolean is(Entity entity) {
      return this == entity || this.entityJointed == entity;
   }

   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
      if (passenger != null && passenger.getVehicle() == this) {
         moveFunction.accept(passenger, this.getX(), this.getY() + passenger.getMyRidingOffset(), this.getZ());
         if (this.updateRidersYaw) {
            if (Float.isNaN(this.prevRidersYaw) || Float.isInfinite(this.prevRidersYaw)) {
               this.prevRidersYaw = this.ridersYaw;
            }

            while (this.ridersYaw - this.prevRidersYaw < -180.0F) {
               this.prevRidersYaw -= 360.0F;
            }

            while (this.ridersYaw - this.prevRidersYaw >= 180.0F) {
               this.prevRidersYaw += 360.0F;
            }

            float yawChanged = this.ridersYaw - this.prevRidersYaw;
            this.prevRidersYaw = this.ridersYaw;
            passenger.setYRot(passenger.getYRot() + yawChanged);
            if (passenger instanceof LivingEntity) {
               LivingEntity riddenByELB = (LivingEntity)passenger;
               riddenByELB.yHeadRot += yawChanged;
               riddenByELB.yBodyRot += yawChanged;
               JBRotationUtil.fixEntityLivingPrevYaw(riddenByELB);
            }

            JBRotationUtil.fixEntityPrevYaw(passenger);
         }

         if (this.playerReRidingSearchTick > 0 && this.playerReRidingSearchTick % 20 == 0
            || this.entityJointed.tickCount % 60 == 0 && !this.level().isClientSide && this.level() instanceof ServerLevel) {
            if (passenger instanceof ServerPlayer serverPlayer) {
               serverPlayer.connection.send(new ClientboundSetPassengersPacket(this));
            }

            if (this.level() instanceof ServerLevel serverLevel) {
               serverLevel.getChunkSource().broadcastAndSend(passenger, new ClientboundSetPassengersPacket(this));
            }
         }
      }

   }

   @Override
   public InteractionResult interact(Player player, InteractionHand hand) {
      if (this.seatEnabled && this.entityJointed != null && !this.entityJointed.isRemoved() && this.entityJointed.hasInitEnd) {
         ItemStack itemStack = CompatHelper.getCurrentEquippedItem(player);
         if (CompatHelper.isStackEmptyOrNull(itemStack)
            || !(itemStack.getItem() instanceof ItemDisassembleBall) && !(itemStack.getItem() instanceof ItemJBAssemblyTuner)) {
            if (CompatHelper.getRiddenByEntity(this) != null && CompatHelper.getRiddenByEntity(this) instanceof Player
               && CompatHelper.getRiddenByEntity(this) != player) {
               return InteractionResult.SUCCESS;
            } else {
               if (!this.level().isClientSide) {
                  if (this.isAsInteractableJoint) {
                     JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromEntityId, player, this.entityJointed);
                  } else if (!this.isAsCameraJoint) {
                     player.startRiding(this);
                  }
               }

               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void setId(int id) {
      super.setId(id);
   }

   public boolean isDisassembleCallFromOwner(LivingEntity caller) {
      return this.entityJointed != null && !this.entityJointed.isRemoved() ? this.entityJointed.isDisassembleCallFromOwner(caller) : false;
   }

   @Override
   public boolean isInWall() {
      return false;
   }

   public void updatePlayerOperationState() {
      if (CompatHelper.getRiddenByEntity(this) instanceof LocalPlayer) {
         LocalPlayer sp = (LocalPlayer)CompatHelper.getRiddenByEntity(this);
         JBPacketSender.sendRidingPlayerInputToServer(this.writeFlags(sp), this.writeFlagsMove(sp));
      } else {
         this.isRiddenbyPressingJumpKey = false;
         this.isRiddenbyPressingSneakKey = false;
      }

   }

   public void receivedPlayerOP(int flagsA, int flagsB) {
      this.readFlags(flagsA);
      this.readFlagsMove(flagsB);
      if (this.isRiddenbyDoublePressedSneakKey) {
         this.canDetach = true;
      }

      this.keyReceived = true;
      this.usedPrevValueCount = 0;
   }

   public void updateOperetionStateFromOutside(Player player) {
      if (CompatHelper.getRiddenByEntity(this) == null && player instanceof LocalPlayer) {
         LocalPlayer sp = (LocalPlayer)player;
         JBPacketSender.sendRidingPlayerInputToServer(this.writeFlags(sp), this.writeFlagsMove(sp));
      }

   }

   private int writeFlags(LocalPlayer sp) {
      int flags = 0;

      for (int i = 0; i < 8; ++i) {
         boolean hasPressedKey = JBKeyHanlder.instance.keys[i].isDown();
         if (hasPressedKey) {
            this.isRiddenbyDoublePressedExKeyList[i] = false;
            if (!this.isRiddenbyPressingExKeyList[i]) {
               if (this.isRiddenbyDoublePressedExKeyTick[i] > 0) {
                  this.isRiddenbyDoublePressedExKeyList[i] = true;
                  this.isRiddenbyDoublePressedExKeyTick[i] = 0;
               } else {
                  this.isRiddenbyDoublePressedExKeyTick[i] = 9;
               }
            }

            this.isRiddenbyPressingExKeyList[i] = true;
         } else {
            this.isRiddenbyPressingExKeyList[i] = false;
            this.isRiddenbyDoublePressedExKeyList[i] = false;
         }

         if (--this.isRiddenbyDoublePressedExKeyTick[i] < 0) {
            this.isRiddenbyDoublePressedExKeyTick[i] = 0;
         }

         flags |= this.isRiddenbyPressingExKeyList[i] ? flagBits[i] : 0;
         flags |= this.isRiddenbyDoublePressedExKeyList[i] ? flagBitsDblKey[i] : 0;
      }

      return flags;
   }

   private int writeFlagsMove(LocalPlayer sp) {
      if (sp.input.forwardImpulse > 0.0F) {
         this.isRiddenbyDoublePressedFwdKey = false;
         if (!this.isRiddenbyPressingFwdKey) {
            if (this.isRiddenbyDoublePressedFwdKeyTick > 0) {
               this.isRiddenbyDoublePressedFwdKey = true;
               this.isRiddenbyDoublePressedFwdKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedFwdKeyTick = 9;
            }
         }

         this.isRiddenbyPressingFwdKey = true;
      } else {
         this.isRiddenbyPressingFwdKey = false;
         this.isRiddenbyDoublePressedFwdKey = false;
      }

      if (--this.isRiddenbyDoublePressedFwdKeyTick < 0) {
         this.isRiddenbyDoublePressedFwdKeyTick = 0;
      }

      if (sp.input.forwardImpulse < 0.0F) {
         this.isRiddenbyDoublePressedBwdKey = false;
         if (!this.isRiddenbyPressingBwdKey) {
            if (this.isRiddenbyDoublePressedBwdKeyTick > 0) {
               this.isRiddenbyDoublePressedBwdKey = true;
               this.isRiddenbyDoublePressedBwdKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedBwdKeyTick = 9;
            }
         }

         this.isRiddenbyPressingBwdKey = true;
      } else {
         this.isRiddenbyPressingBwdKey = false;
         this.isRiddenbyDoublePressedBwdKey = false;
      }

      if (--this.isRiddenbyDoublePressedBwdKeyTick < 0) {
         this.isRiddenbyDoublePressedBwdKeyTick = 0;
      }

      if (sp.input.leftImpulse > 0.0F) {
         this.isRiddenbyDoublePressedLftKey = false;
         if (!this.isRiddenbyPressingLftKey) {
            if (this.isRiddenbyDoublePressedLftKeyTick > 0) {
               this.isRiddenbyDoublePressedLftKey = true;
               this.isRiddenbyDoublePressedLftKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedLftKeyTick = 9;
            }
         }

         this.isRiddenbyPressingLftKey = true;
      } else {
         this.isRiddenbyPressingLftKey = false;
         this.isRiddenbyDoublePressedLftKey = false;
      }

      if (--this.isRiddenbyDoublePressedLftKeyTick < 0) {
         this.isRiddenbyDoublePressedLftKeyTick = 0;
      }

      if (sp.input.leftImpulse < 0.0F) {
         this.isRiddenbyDoublePressedRgtKey = false;
         if (!this.isRiddenbyPressingRgtKey) {
            if (this.isRiddenbyDoublePressedRgtKeyTick > 0) {
               this.isRiddenbyDoublePressedRgtKey = true;
               this.isRiddenbyDoublePressedRgtKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedRgtKeyTick = 9;
            }
         }

         this.isRiddenbyPressingRgtKey = true;
      } else {
         this.isRiddenbyPressingRgtKey = false;
         this.isRiddenbyDoublePressedRgtKey = false;
      }

      if (--this.isRiddenbyDoublePressedRgtKeyTick < 0) {
         this.isRiddenbyDoublePressedRgtKeyTick = 0;
      }

      if (sp.input.jumping) {
         this.isRiddenbyDoublePressedJumpKey = false;
         if (!this.isRiddenbyPressingJumpKey) {
            if (this.isRiddenbyDoublePressedJumpKeyTick > 0) {
               this.isRiddenbyDoublePressedJumpKey = true;
               this.isRiddenbyDoublePressedJumpKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedJumpKeyTick = 9;
            }
         }

         this.isRiddenbyPressingJumpKey = true;
      } else {
         this.isRiddenbyPressingJumpKey = false;
         this.isRiddenbyDoublePressedJumpKey = false;
      }

      if (--this.isRiddenbyDoublePressedJumpKeyTick < 0) {
         this.isRiddenbyDoublePressedJumpKeyTick = 0;
      }

      if (sp.input.shiftKeyDown) {
         this.isRiddenbyDoublePressedSneakKey = false;
         if (!this.isRiddenbyPressingSneakKey) {
            if (this.isRiddenbyDoublePressedSneakKeyTick > 0) {
               this.isRiddenbyDoublePressedSneakKey = true;
               this.isRiddenbyDoublePressedSneakKeyTick = 0;
            } else {
               this.isRiddenbyDoublePressedSneakKeyTick = 9;
            }
         }

         this.isRiddenbyPressingSneakKey = true;
      } else {
         this.isRiddenbyPressingSneakKey = false;
         this.isRiddenbyDoublePressedSneakKey = false;
      }

      if (--this.isRiddenbyDoublePressedSneakKeyTick < 0) {
         this.isRiddenbyDoublePressedSneakKeyTick = 0;
      }

      int flgs = 0;
      if (this.isRiddenbyDoublePressedFwdKey) {
         flgs |= 256;
      }

      if (this.isRiddenbyDoublePressedBwdKey) {
         flgs |= 512;
      }

      if (this.isRiddenbyDoublePressedLftKey) {
         flgs |= 1024;
      }

      if (this.isRiddenbyDoublePressedRgtKey) {
         flgs |= 2048;
      }

      if (this.isRiddenbyDoublePressedJumpKey) {
         flgs |= 4096;
      }

      if (this.isRiddenbyDoublePressedSneakKey) {
         flgs |= 8192;
      }

      if (this.isRiddenbyPressingFwdKey) {
         flgs |= 1;
      }

      if (this.isRiddenbyPressingBwdKey) {
         flgs |= 2;
      }

      if (this.isRiddenbyPressingLftKey) {
         flgs |= 4;
      }

      if (this.isRiddenbyPressingRgtKey) {
         flgs |= 8;
      }

      if (this.isRiddenbyPressingJumpKey) {
         flgs |= 16;
      }

      if (this.isRiddenbyPressingSneakKey) {
         flgs |= 32;
      }

      this.isRiddenbyUsingWheelUp = false;
      this.isRiddenbyUsingWheelDown = false;
      if (JBKeyHanlder.instance.mouseWheelState > 0) {
         this.isRiddenbyUsingWheelUp = true;
         flgs |= 16384;
      } else if (JBKeyHanlder.instance.mouseWheelState < 0) {
         this.isRiddenbyUsingWheelDown = true;
         flgs |= 32768;
      }

      return flgs;
   }

   private void readFlags(int flags) {
      for (int i = 0; i < 8; ++i) {
         this.isRiddenbyPressingExKeyList[i] = (flags & flagBits[i]) != 0;
         this.isRiddenbyDoublePressedExKeyList[i] = (flags & flagBitsDblKey[i]) != 0;
      }

   }

   private void readFlagsMove(int flags) {
      this.isRiddenbyPressingFwdKey = (flags & 1) != 0;
      this.isRiddenbyPressingBwdKey = (flags & 2) != 0;
      this.isRiddenbyPressingLftKey = (flags & 4) != 0;
      this.isRiddenbyPressingRgtKey = (flags & 8) != 0;
      this.isRiddenbyPressingJumpKey = (flags & 16) != 0;
      this.isRiddenbyPressingSneakKey = (flags & 32) != 0;
      this.isRiddenbyDoublePressedFwdKey = (flags & 256) != 0;
      this.isRiddenbyDoublePressedBwdKey = (flags & 512) != 0;
      this.isRiddenbyDoublePressedLftKey = (flags & 1024) != 0;
      this.isRiddenbyDoublePressedRgtKey = (flags & 2048) != 0;
      this.isRiddenbyDoublePressedJumpKey = (flags & 4096) != 0;
      this.isRiddenbyDoublePressedSneakKey = (flags & 8192) != 0;
      this.isRiddenbyUsingWheelUp = (flags & 16384) != 0;
      this.isRiddenbyUsingWheelDown = (flags & 32768) != 0;
      this.moveForward = 0.0F;
      if (this.isRiddenbyPressingFwdKey) {
         ++this.moveForward;
      }

      if (this.isRiddenbyPressingBwdKey) {
         --this.moveForward;
      }

      this.moveStrafe = 0.0F;
      if (this.isRiddenbyPressingLftKey) {
         ++this.moveStrafe;
      }

      if (this.isRiddenbyPressingRgtKey) {
         --this.moveStrafe;
      }

   }

   public boolean isRiddenbyPressingExKey(int numKey) {
      if (numKey < 0) {
         return false;
      } else if ((this.ringKeyDirections & 1 << numKey) != 0) {
         return true;
      } else if (numKey < 8) {
         return !this.keyReceived && this.usedPrevValueCount < 3 ? this.storedIsRiddenbyPressingExKeyList[numKey] : this.isRiddenbyPressingExKeyList[numKey];
      } else if (!this.keyReceived && this.usedPrevValueCount < 3) {
         return numKey < this.storedIsRiddenbyPressingExKeyList.length ? this.storedIsRiddenbyPressingExKeyList[numKey] : false;
      } else {
         switch (numKey) {
            case 8:
               return this.isRiddenbyPressingFwdKey;
            case 9:
               return this.isRiddenbyPressingBwdKey;
            case 10:
               return this.isRiddenbyPressingLftKey;
            case 11:
               return this.isRiddenbyPressingRgtKey;
            case 12:
               return this.isRiddenbyPressingJumpKey;
            case 13:
               return this.isRiddenbyPressingSneakKey;
            case 14:
               return this.isRiddenbyUsingWheelUp;
            case 15:
               return this.isRiddenbyUsingWheelDown;
            default:
               return false;
         }
      }
   }

   public boolean isRiddenbyDoublePressedExKey(int numKey) {
      if (numKey < 0) {
         return false;
      } else if ((this.ringDblKeyDirections & 1 << numKey) != 0) {
         return true;
      } else if (numKey < 8) {
         return !this.keyReceived && this.usedPrevValueCount < 3
            ? this.storedIsRiddenbyDoublePressedExKeyList[numKey]
            : this.isRiddenbyDoublePressedExKeyList[numKey];
      } else if (!this.keyReceived && this.usedPrevValueCount < 3) {
         return numKey < this.storedIsRiddenbyDoublePressedExKeyList.length ? this.storedIsRiddenbyDoublePressedExKeyList[numKey] : false;
      } else {
         switch (numKey) {
            case 8:
               return this.isRiddenbyDoublePressedFwdKey;
            case 9:
               return this.isRiddenbyDoublePressedBwdKey;
            case 10:
               return this.isRiddenbyDoublePressedLftKey;
            case 11:
               return this.isRiddenbyDoublePressedRgtKey;
            case 12:
               return this.isRiddenbyDoublePressedJumpKey;
            case 13:
               return this.isRiddenbyDoublePressedSneakKey;
            default:
               return false;
         }
      }
   }

   public boolean isRiddenbyUsedItem(int numItem) {
      if ((this.ringItemDirections & 1 << numItem) != 0) {
         return true;
      } else {
         return numItem >= 0 && numItem < this.isRiddenbyUsedItemList.length ? this.isRiddenbyUsedItemList[numItem] : false;
      }
   }

   public boolean canKeyItemUsed() {
      return CompatHelper.getRiddenByEntity(this) != null || this.isAsInteractableJoint || this.hasRingKeyDirection();
   }

   private boolean hasRingKeyDirection() {
      return this.ringKeyDirections != 0 || this.ringDblKeyDirections != 0 || this.ringItemDirections != 0;
   }

   public Player getActionDispatcher() {
      Player conductor = this.getConductor();
      if (conductor == null && this.entityJointed != null && this.entityJointed.entitySeatPartArray != null) {
         for (int i = 0; i < this.entityJointed.entitySeatPartArray.length; ++i) {
            conductor = this.entityJointed.entitySeatPartArray[i].getConductor();
            if (conductor != null) {
               break;
            }
         }
      }

      return conductor;
   }

   private Player getConductor() {
      if (CompatHelper.getRiddenByEntity(this) != null && CompatHelper.getRiddenByEntity(this) instanceof Player) {
         return (Player)CompatHelper.getRiddenByEntity(this);
      } else {
         return this.keyConductor != null ? this.keyConductor : null;
      }
   }

   public Entity getCameraForNum(int seatNum0to3, boolean ignoreEnabled) {
      return this.entityJointed == null ? null : this.entityJointed.getCameraForNum(seatNum0to3, ignoreEnabled);
   }

   public void setCamModeNum(int camNum0to3orNeg) {
      if (camNum0to3orNeg >= -1 && camNum0to3orNeg <= 3) {
         this.cameraModeSeatNum = (byte)camNum0to3orNeg;
      }

   }

   public boolean autoDetectCameraNum() {
      for (int seatNum = 0; seatNum < 4; ++seatNum) {
         Entity cam = this.getCameraForNum(seatNum, true);
         if (cam != null) {
            this.cameraModeSeatNum = (byte)seatNum;
            return true;
         }
      }

      this.cameraModeSeatNum = -1;
      return false;
   }

   public void mountEntityWork(EntityJointed entityJointed, double range, IJBVectorTransformer matrixUtil) {
      if (this.mountLivingDuration <= 0) {
         if (CompatHelper.getRiddenByEntity(this) == null) {
            this.searchAndMount(entityJointed, range, matrixUtil);
         } else {
            Entity targetRiddenBy = CompatHelper.getRiddenByEntity(this);
            if (!(targetRiddenBy instanceof Player)) {
               targetRiddenBy.stopRiding();
            }

            this.mountLivingDuration = 60;
         }

      }
   }

   public void searchAndMount(EntityJointed entityJointed, double range, IJBVectorTransformer matrixUtil) {
      ModelViewMatrix matrix = (ModelViewMatrix)matrixUtil;
      matrix.cmnVec.x = 0.0;
      matrix.cmnVec.y = 1.0;
      matrix.cmnVec.z = 0.0;
      matrix.transformAbsolute(matrix.cmnVec);
      int minX = Mth.floor(matrix.cmnVec.x - range);
      int maxX = Mth.ceil(matrix.cmnVec.x + range);
      int minY = Mth.floor(matrix.cmnVec.y - range);
      int maxY = Mth.ceil(matrix.cmnVec.y + range);
      int minZ = Mth.floor(matrix.cmnVec.z - range);
      int maxZ = Mth.ceil(matrix.cmnVec.z + range);
      List<LivingEntity> list = entityJointed
         .level()
         .getEntitiesOfClass(LivingEntity.class, JBCommonUtil.getBoundingBox((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ));
      double d0 = 0.0;
      LivingEntity nearest = null;

      for (int j = 0; j < list.size(); ++j) {
         LivingEntity elv = list.get(j);
         if (!elv.isRemoved() && !elv.isPassenger() && !elv.isVehicle() && !(elv instanceof Player) && !(elv instanceof EntityJointed)) {
            double d1 = elv.distanceToSqr(matrix.cmnVec.x, matrix.cmnVec.y, matrix.cmnVec.z);
            if (d1 < d0 || d0 == 0.0) {
               nearest = elv;
               d0 = d1;
            }
         }
      }

      if (nearest != null) {
         nearest.startRiding(this);
         this.mountLivingDuration = 60;
      }

   }

   public void resetMoveDR() {
      this.cutDefaultOP = false;
      this.hasRINGMoveDR = false;
      this.moveStrafingDR = 0;
      this.moveForwardDR = 0;
      this.moveUpDownDR = 0;
      this.moveStrafingAmount = 0.0F;
      this.moveForwardAmount = 0.0F;
      this.yawChange = false;
      this.yawAdd = false;
      this.yawValue = 0.0F;
      this.lockMoveDR = false;
   }

   public boolean hasDirectionRider() {
      return this.hasPlayerRider() || this.hasRINGMoveDR;
   }

   public boolean hasPlayerRider() {
      return !this.cutDefaultOP && CompatHelper.getRiddenByEntity(this) != null && CompatHelper.getRiddenByEntity(this) instanceof ServerPlayer;
   }

   public float getMoveStrafingDR() {
      float move = 0.0F;
      if (this.hasPlayerRider()) {
         move = this.moveStrafe;
      }

      if (move == 0.0F && this.moveStrafingDR >= 1) {
         move = this.moveStrafingAmount;
      }

      return move;
   }

   public float getMoveForwardDR() {
      float move = 0.0F;
      if (this.hasPlayerRider()) {
         move = this.moveForward;
      }

      if (move == 0.0F && this.moveForwardDR >= 1) {
         move = this.moveForwardAmount;
      }

      return move;
   }

   public boolean isFwdDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(8)) {
         return true;
      } else {
         return this.moveForwardDR >= 1 && this.moveForwardAmount > 0.0F;
      }
   }

   public boolean isBwdDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(9)) {
         return true;
      } else {
         return this.moveForwardDR >= 1 && this.moveForwardAmount < 0.0F;
      }
   }

   public boolean isLftDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(10)) {
         return true;
      } else {
         return this.moveStrafingDR >= 1 && this.moveStrafingAmount > 0.0F;
      }
   }

   public boolean isRgtDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(11)) {
         return true;
      } else {
         return this.moveStrafingDR >= 1 && this.moveStrafingAmount < 0.0F;
      }
   }

   public boolean isJumpOrUpDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(12)) {
         return true;
      } else {
         return this.moveUpDownDR >= 1 && this.moveUp;
      }
   }

   public boolean isSneakOrDownDR() {
      if (this.hasPlayerRider() && this.isRiddenbyPressingExKey(13)) {
         return true;
      } else {
         return this.moveUpDownDR >= 1 && !this.moveUp;
      }
   }

   public boolean isFwdDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(8)) {
         return true;
      } else {
         return this.moveForwardDR >= 2 && this.moveForwardAmount > 0.0F;
      }
   }

   public boolean isBwdDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(9)) {
         return true;
      } else {
         return this.moveForwardDR >= 2 && this.moveForwardAmount < 0.0F;
      }
   }

   public boolean isLftDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(10)) {
         return true;
      } else {
         return this.moveStrafingDR >= 2 && this.moveStrafingAmount > 0.0F;
      }
   }

   public boolean isRgtDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(11)) {
         return true;
      } else {
         return this.moveStrafingDR >= 2 && this.moveStrafingAmount < 0.0F;
      }
   }

   public boolean isJumpOrUpDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(12)) {
         return true;
      } else {
         return this.moveUpDownDR >= 2 && this.moveUp;
      }
   }

   public boolean isSneakOrDownDoubleDR() {
      if (this.hasPlayerRider() && this.isRiddenbyDoublePressedExKey(13)) {
         return true;
      } else {
         return this.moveUpDownDR >= 2 && !this.moveUp;
      }
   }
}
