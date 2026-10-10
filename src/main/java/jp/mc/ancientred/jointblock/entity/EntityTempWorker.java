package jp.mc.ancientred.jointblock.entity;

import java.util.List;
import java.util.UUID;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.TypedLinkable;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.fx.EntityWhiteSmokeFx;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Short lived helper entity driven by the joint block actions ({@code ActionDrill},
 * {@code ActionBlade}, {@code ActionFlamer}, ...). It carries the actual working logic
 * for the action (digging, slashing entities, setting fire) and is killed as soon as
 * the action stops being executed.
 *
 * <p>Port notes vs. the 1.12.2 source:
 * <ul>
 *   <li>{@code IEntityAdditionalSpawnData} now takes a {@link FriendlyByteBuf}.</li>
 *   <li>{@code func_70070_b} (brightness override) and {@code shouldRenderInPass} are
 *       render hooks. 1.20.1 packs entity light in
 *       {@code EntityRenderer#getPackedLightCoords}, so there is no entity side hook
 *       left to override; the old override is dropped and {@code shouldRenderInPass}
 *       is kept as a plain query method (render pass consumption is handled by the
 *       renderer in the render phase of the port).</li>
 * </ul>
 */
public class EntityTempWorker extends Entity implements IEntityAdditionalSpawnData, TypedLinkable, IIgnoreTarget {
   public static final String NBT_TAGNAME_TEMPWORKER_RR = "jbwk:rr";
   private static final int FLAME_WORKARG_FLG_BURNBLOCK = 1;
   private static final int FLAME_WORKARG_FLG_SETFIRE = 2;
   private static final int FLAME_WORKARG_FLG_SETPOISON = 4;
   private static final EntityDataAccessor<Integer> DW_ENTITY_LIMIT = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> DW_WORKARG = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> DW_ADDITIONAL_POSX = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DW_ADDITIONAL_POSY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DW_ADDITIONAL_POSZ = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DW_DAMAGE = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DW_USEENERGY = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.FLOAT);
   public static final byte EFFECT_TYPE_SMOKE = 10;
   public LivingEntity baseEntity;
   public LivingEntity conductorEntity;
   public WorkerType workerType;
   public int hittingTick;
   public int hittingBlockId;
   public int workingX;
   public int workingY;
   public int workingZ;
   public boolean renderOn;
   public int extraFieldValue;
   public boolean setBlockFire;
   public boolean setMobFire;
   public int setMobPotionEffect;
   public int potionAmplifier;
   public int effectDuration;
   public UUID uuidRerideBaseEj;
   public byte seatPartNum;
   public byte searchTickRand;
   private static final int BLADE_PATH_HOLD_NUM = 12;
   private static final int FLAME_PATH_HOLD_NUM = 8;
   private static final double HIT_VELOCITY_LIMIT = 3.0;
   public Vec3[] pathVecsTop;
   public Vec3[] pathVecsBot;
   public Vec3 vecWork;
   private TypedLinkable next;
   private TypedLinkable prev;
   private long renderCheck;

   public EntityTempWorker(EntityType<? extends EntityTempWorker> type, Level level) {
      super(type, level);
      this.workerType = WorkerType.effect;
      this.renderOn = false;
      this.extraFieldValue = -2130758861;
      this.setBlockFire = false;
      this.setMobFire = false;
      this.setMobPotionEffect = -1;
      this.potionAmplifier = 1;
      this.effectDuration = 5;
      this.renderCheck = -1L;
      this.noPhysics = true;
      this.noCulling = true;
   }

   @SuppressWarnings("unchecked")
   public EntityTempWorker(Level level) {
      this((EntityType<? extends EntityTempWorker>)JBEntityTypes.TEMP_WORKER, level);
   }

   @Override
   protected void defineSynchedData() {
      this.entityData.define(DW_ENTITY_LIMIT, 0);
      this.entityData.define(DW_WORKARG, (byte)0);
      this.entityData.define(DW_ADDITIONAL_POSX, 0.0F);
      this.entityData.define(DW_ADDITIONAL_POSY, 0.0F);
      this.entityData.define(DW_ADDITIONAL_POSZ, 0.0F);
      this.entityData.define(DW_DAMAGE, 0.0F);
      this.entityData.define(DW_USEENERGY, 0.0F);
   }

   @Override
   public boolean fireImmune() {
      return true;
   }

   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return true;
   }

   @Override
   public boolean isAttackable() {
      return false;
   }

   @Override
   public boolean isPushedByFluid() {
      return false;
   }

   @Override
   public void lerpTo(double posX, double posY, double posZ, float rotationYaw, float rotationPitch, int posRotationIncrements, boolean teleport) {
      this.setPos(posX, posY, posZ);
   }

   public EntityTempWorker setConducter(LivingEntity entityConducted) {
      this.conductorEntity = entityConducted;
      return this;
   }

   public EntityTempWorker setBase(LivingEntity entityLivingBase) {
      this.baseEntity = entityLivingBase;
      return this;
   }

   public EntityTempWorker setWorkerType(WorkerType workerType) {
      this.workerType = workerType;
      return this;
   }

   public void setLifeLimit(int limit) {
      this.entityData.set(DW_ENTITY_LIMIT, limit);
   }

   public int getLifeLimit() {
      return this.entityData.get(DW_ENTITY_LIMIT);
   }

   public void setWorkArg(byte arg) {
      this.entityData.set(DW_WORKARG, arg);
   }

   public byte getWorkArg() {
      return this.entityData.get(DW_WORKARG);
   }

   public void setAdditionalPos(double argX, double argY, double argZ) {
      this.entityData.set(DW_ADDITIONAL_POSX, (float)argX);
      this.entityData.set(DW_ADDITIONAL_POSY, (float)argY);
      this.entityData.set(DW_ADDITIONAL_POSZ, (float)argZ);
   }

   public void setDamage(float arg) {
      this.entityData.set(DW_DAMAGE, arg);
   }

   public float getDamage() {
      return this.entityData.get(DW_DAMAGE);
   }

   public void setUseEnergy(float arg) {
      this.entityData.set(DW_USEENERGY, arg);
   }

   public float getUseEnergy() {
      return this.entityData.get(DW_USEENERGY);
   }

   public Vec3 getAdditionalPos(Vec3 toSet) {
      double x = (double)(float)this.entityData.get(DW_ADDITIONAL_POSX);
      double y = (double)(float)this.entityData.get(DW_ADDITIONAL_POSY);
      double z = (double)(float)this.entityData.get(DW_ADDITIONAL_POSZ);
      toSet.x = x;
      toSet.y = y;
      toSet.z = z;
      return toSet;
   }

   public void setContinueWork(int workArg) {
      this.setLifeLimit(10);
      this.setWorkArg((byte)workArg);
   }

   @Override
   public void tick() {
      int lifeLimit = this.getLifeLimit();
      --lifeLimit;
      this.setLifeLimit(lifeLimit);
      if (this.level().isClientSide && this.renderOn) {
         JBModContainer.proxy.addToStickyRenderList(this);
      }

      if (this.workerType == WorkerType.reride && lifeLimit > 0) {
         super.tick();
         if (!this.level().isClientSide) {
            this.doRerideAttachWork();
         }
      } else if (this.baseEntity != null && this.conductorEntity != null && lifeLimit > 0) {
         super.tick();
         switch (this.workerType) {
            case drill:
               this.doDrillWork();
               break;
            case blade:
               this.doBladeWork();
               break;
            case flamer:
               this.doFlameWork();
            case reride:
               break;
            default:
               this.doEffectWork();
         }
      } else {
         this.discard();
         if (this.level().isClientSide) {
            this.level().destroyBlockProgress(this.getId(), BlockPos.ZERO, -1);
         }
      }

   }

   private void doEffectWork() {
      if (this.level().isClientSide) {
         int effect = this.getWorkArg();
         switch ((byte)effect) {
            case EFFECT_TYPE_SMOKE:
               this.spawnWhiteSmoke();
         }
      }

   }

   private void doDrillWork() {
      int targetX = 0;
      int targetY = 0;
      int targetZ = 0;
      int baseX = Mth.floor(this.getX());
      int baseY = Mth.floor(this.getY());
      int baseZ = Mth.floor(this.getZ());
      double distMin = -1.0;
      Block blockHit = null;
      BlockState bStateHit = null;
      int power = this.getWorkArg();
      int range = power >= 1 ? 1 : 0;

      int blockId;
      for(blockId = baseX - range; blockId <= baseX + range; ++blockId) {
         for(int z = baseZ - range; z <= baseZ + range; ++z) {
            BlockState bState = WorldHelper.getBlockState(this.level(), blockId, baseY, z);
            Block block = bState.getBlock();
            if (block != null && block != Blocks.AIR && bState.isSolid()) {
               double dist = this.distanceToSqr((double)blockId + 0.5, (double)baseY + 0.5, (double)z + 0.5);
               if (distMin == -1.0 || distMin > dist) {
                  distMin = dist;
                  targetX = blockId;
                  targetY = baseY;
                  targetZ = z;
                  blockHit = block;
                  bStateHit = bState;
               }
            }
         }
      }

      if (blockHit == null) {
         this.hittingTick = 0;
      } else {
         blockId = Block.getId(blockHit.defaultBlockState());
         BlockPos hitPos = new BlockPos(targetX, targetY, targetZ);
         float hardness = bStateHit.getDestroySpeed(this.level(), hitPos);
         if (hardness < 0.0F || this.conductorEntity instanceof Player && !this.level().mayInteract((Player)this.conductorEntity, hitPos)) {
            this.hittingTick = 0;
            if (this.level().isClientSide && this.tickCount % 2 == 0) {
               this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.AMBIENT, 1.0F, 2.0F, true);
            }

         } else if (this.hittingTick != 0 && blockId == this.hittingBlockId && this.workingX == targetX && this.workingY == targetY && this.workingZ == targetZ) {
            float energyBase = this.getUseEnergy();
            if (this.baseEntity instanceof IJBEntityState && ((IJBEntityState)this.baseEntity).useEntityEnergy(energyBase + energyBase * (float)power)) {
               ++this.hittingTick;
               float totalHit = (float)(this.hittingTick * (1 + power)) / 10.0F;
               if (this.level().isClientSide && this.tickCount % 2 == 0) {
                  this.level().destroyBlockProgress(this.getId(), hitPos, (int)(totalHit / hardness * 10.0F) - 1);
                  this.playDiggSound(hitPos, bStateHit);
               }

               if (totalHit >= hardness) {
                  if (!this.level().isClientSide) {
                     Block.dropResources(this.level().getBlockState(hitPos), this.level(), hitPos);
                     this.level().destroyBlockProgress(this.getId(), hitPos, 20);
                     WorldHelper.setBlockToAir(this.level(), hitPos);
                     this.level().levelEvent(2001, hitPos, Block.getId(bStateHit));
                  }

                  this.discard();
               }
            }

         } else {
            this.hittingTick = 1;
            this.hittingBlockId = blockId;
            this.workingX = targetX;
            this.workingY = targetY;
            this.workingZ = targetZ;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void playDiggSound(BlockPos posBlock, BlockState iblockstate) {
      Minecraft mc = Minecraft.getInstance();
      SoundType soundtype = iblockstate.getBlock().getSoundType(iblockstate, mc.level, posBlock, mc.player);
      mc.level.playLocalSound(posBlock, soundtype.getHitSound(), SoundSource.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, false);
   }

   @OnlyIn(Dist.CLIENT)
   public void spawnWhiteSmoke() {
      for(int i = 0; i < 4; ++i) {
         Minecraft.getInstance().particleEngine.add(new EntityWhiteSmokeFx(this.level(), this.xo, this.yo, this.zo));
      }

   }

   private void doBladeWork() {
      Vec3 posTop;
      if (this.level().isClientSide) {
         this.renderOn = true;
         if (this.vecWork == null) {
            this.vecWork = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
         }

         posTop = this.getAdditionalPos(this.vecWork);
         int i;
         if (this.pathVecsBot == null) {
            this.pathVecsTop = new Vec3[BLADE_PATH_HOLD_NUM];
            this.pathVecsBot = new Vec3[BLADE_PATH_HOLD_NUM];

            for(i = 0; i < this.pathVecsBot.length; ++i) {
               this.pathVecsTop[i] = JBCommonUtil.createVectorHelper(posTop.x, posTop.y, posTop.z);
               this.pathVecsBot[i] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
            }
         } else {
            for(i = this.pathVecsBot.length - 2; i >= 0; --i) {
               this.pathVecsBot[i + 1].x = this.pathVecsBot[i].x;
               this.pathVecsBot[i + 1].y = this.pathVecsBot[i].y;
               this.pathVecsBot[i + 1].z = this.pathVecsBot[i].z;
               this.pathVecsTop[i + 1].x = this.pathVecsTop[i].x;
               this.pathVecsTop[i + 1].y = this.pathVecsTop[i].y;
               this.pathVecsTop[i + 1].z = this.pathVecsTop[i].z;
            }

            this.pathVecsTop[0].x = posTop.x;
            this.pathVecsTop[0].y = posTop.y;
            this.pathVecsTop[0].z = posTop.z;
            this.pathVecsBot[0].x = this.getX();
            this.pathVecsBot[0].y = this.getY();
            this.pathVecsBot[0].z = this.getZ();
         }

      } else if (this.baseEntity instanceof IJBEntityState) {
         if (this.pathVecsBot == null) {
            this.pathVecsTop = new Vec3[2];
            this.pathVecsBot = new Vec3[2];
            this.vecWork = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
            this.pathVecsTop[0] = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
            this.pathVecsTop[0] = this.getAdditionalPos(this.pathVecsTop[0]);
            this.pathVecsBot[0] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
            this.pathVecsTop[1] = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
            this.pathVecsTop[1] = this.getAdditionalPos(this.pathVecsTop[1]);
            this.pathVecsBot[1] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
         } else {
            this.pathVecsBot[1].x = this.pathVecsBot[0].x;
            this.pathVecsBot[1].y = this.pathVecsBot[0].y;
            this.pathVecsBot[1].z = this.pathVecsBot[0].z;
            this.pathVecsTop[1].x = this.pathVecsTop[0].x;
            this.pathVecsTop[1].y = this.pathVecsTop[0].y;
            this.pathVecsTop[1].z = this.pathVecsTop[0].z;
            this.pathVecsTop[0] = this.getAdditionalPos(this.pathVecsTop[0]);
            this.pathVecsBot[0].x = this.getX();
            this.pathVecsBot[0].y = this.getY();
            this.pathVecsBot[0].z = this.getZ();
         }

         posTop = this.pathVecsTop[0];
         Vec3 posBottom = this.pathVecsBot[0];
         Vec3 prevPosTop = this.pathVecsTop[1];
         Vec3 prevPosBottom = this.pathVecsBot[1];
         this.vecWork.x = (posTop.x + posBottom.x + prevPosTop.x + prevPosBottom.x) / 4.0;
         this.vecWork.y = (posTop.y + posBottom.y + prevPosTop.y + prevPosBottom.y) / 4.0;
         this.vecWork.z = (posTop.z + posBottom.z + prevPosTop.z + prevPosBottom.z) / 4.0;
         double rangeBB = posTop.distanceTo(posBottom);
         AABB bladeBB = this.getBoundingBox();
         bladeBB.minX = this.vecWork.x - rangeBB;
         bladeBB.minY = this.vecWork.y - rangeBB;
         bladeBB.minZ = this.vecWork.z - rangeBB;
         bladeBB.maxX = this.vecWork.x + rangeBB;
         bladeBB.maxY = this.vecWork.y + rangeBB;
         bladeBB.maxZ = this.vecWork.z + rangeBB;
         boolean isVelocityCalculated = false;
         float useEnergy = this.getUseEnergy();
         List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
         int len = list.size();

         for(int i = 0; i < len; ++i) {
            Entity entity = (Entity)list.get(i);
            if (this.isAllowedTarget(entity)) {
               AABB axisalignedbb1 = entity.getBoundingBox().inflate(1.0, 1.0, 1.0);
               Vec3 rayTraceResultHit = axisalignedbb1.clip(posTop, prevPosBottom).orElse(null);
               if (rayTraceResultHit == null) {
                  rayTraceResultHit = axisalignedbb1.clip(prevPosTop, posBottom).orElse(null);
               }

               if (rayTraceResultHit != null) {
                  if (!this.canBaseEntityUseEnergy(useEnergy)) {
                     break;
                  }

                  if (entity.hurt(this.getDamageSourceCondoctor(), this.getDamage())) {
                     ((IJBEntityState)this.baseEntity).useEntityEnergy(useEnergy);
                     this.addExtraDamageEffects(entity);
                     if (!isVelocityCalculated) {
                        this.vecWork.x = posBottom.x - prevPosBottom.x;
                        this.vecWork.y = posBottom.y - prevPosBottom.y;
                        this.vecWork.z = posBottom.z - prevPosBottom.z;
                        double mag;
                        if ((mag = this.vecWork.length()) > HIT_VELOCITY_LIMIT) {
                           Vec3 r = this.vecWork;
                           if (mag < 1.0E-4) {
                              this.vecWork.x = 0.0;
                              this.vecWork.y = 0.0;
                              this.vecWork.z = 0.0;
                           } else {
                              this.vecWork.x = HIT_VELOCITY_LIMIT * r.x / mag;
                              this.vecWork.y = HIT_VELOCITY_LIMIT * r.y / mag;
                              this.vecWork.z = HIT_VELOCITY_LIMIT * r.z / mag;
                           }
                        }

                        isVelocityCalculated = true;
                     }

                     entity.push(this.vecWork.x, this.vecWork.y, this.vecWork.z);
                  }
               }
            }
         }

      }
   }

   private DamageSource getDamageSourceCondoctor() {
      return this.conductorEntity instanceof Player ? this.damageSources().playerAttack((Player)this.conductorEntity) : this.damageSources().mobAttack(this.conductorEntity);
   }

   private void addExtraDamageEffects(Entity entity) {
      if (this.setMobFire) {
         entity.setSecondsOnFire(this.effectDuration);
      }

      MobEffect pt = MobEffect.byId(this.setMobPotionEffect);
      if (pt != null) {
         int duration = pt.isInstantenous() ? 1 : this.effectDuration * 20;
         ((LivingEntity)entity).addEffect(new MobEffectInstance(pt, duration, this.potionAmplifier));
      }

   }

   private void doFlameWork() {
      int lst = FLAME_PATH_HOLD_NUM - 1;
      double rangeBB;
      if (this.pathVecsBot == null) {
         this.vecWork = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
         this.pathVecsTop = new Vec3[FLAME_PATH_HOLD_NUM];
         this.pathVecsTop[lst] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
         this.pathVecsTop[0] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
         this.pathVecsBot = new Vec3[2];
         this.pathVecsBot[0] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
         this.pathVecsBot[1] = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
         this.pathVecsTop[0].x = this.getX();
         this.pathVecsTop[0].y = this.getY();
         this.pathVecsTop[0].z = this.getZ();

         for(int i = 1; i < this.pathVecsTop.length; ++i) {
            if (this.pathVecsTop[i] == null) {
               this.pathVecsTop[i] = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
            }

            this.pathVecsTop[i].x = this.pathVecsTop[0].x;
            this.pathVecsTop[i].y = this.pathVecsTop[0].y;
            this.pathVecsTop[i].z = this.pathVecsTop[0].z;
         }
      } else {
         this.pathVecsBot[1] = this.getAdditionalPos(this.pathVecsBot[1]);
         this.pathVecsBot[0].x = this.getX();
         this.pathVecsBot[0].y = this.getY();
         this.pathVecsBot[0].z = this.getZ();

         for(int i = 0; i < this.pathVecsTop.length; ++i) {
            double lerp = (double)i / (double)lst;
            this.vecWork.x = this.pathVecsBot[0].x + (this.pathVecsBot[1].x - this.pathVecsBot[0].x) * lerp;
            this.vecWork.y = this.pathVecsBot[0].y + (this.pathVecsBot[1].y - this.pathVecsBot[0].y) * lerp;
            this.vecWork.z = this.pathVecsBot[0].z + (this.pathVecsBot[1].z - this.pathVecsBot[0].z) * lerp;
            rangeBB = 1.2 - lerp;
            if (rangeBB > 1.0) {
               rangeBB = 1.0;
            }

            this.pathVecsTop[i].x += (this.vecWork.x - this.pathVecsTop[i].x) * rangeBB;
            this.pathVecsTop[i].y += (this.vecWork.y - this.pathVecsTop[i].y) * rangeBB;
            this.pathVecsTop[i].z += (this.vecWork.z - this.pathVecsTop[i].z) * rangeBB;
         }
      }

      this.renderOn = true;
      if (!this.level().isClientSide) {
         Vec3 posStart = this.pathVecsTop[0];
         Vec3 posEnd = this.pathVecsTop[lst];
         float useEnergy = this.getUseEnergy();
         if (this.setBlockFire) {
            this.pathVecsBot[0].x = posStart.x;
            this.pathVecsBot[0].y = posStart.y;
            this.pathVecsBot[0].z = posStart.z;
            this.pathVecsBot[1].x = posEnd.x;
            this.pathVecsBot[1].y = posEnd.y;
            this.pathVecsBot[1].z = posEnd.z;
            BlockHitResult rayTraceResultBlock = this.level().clip(new ClipContext(this.pathVecsBot[0], this.pathVecsBot[1], ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (rayTraceResultBlock.getType() != HitResult.Type.MISS && this.canBaseEntityUseEnergy(useEnergy)) {
               Block block = WorldHelper.getBlock(this.level(), rayTraceResultBlock.getBlockPos());
               if (block != null && block.isFlammable(this.level().getBlockState(rayTraceResultBlock.getBlockPos()), this.level(), rayTraceResultBlock.getBlockPos(), rayTraceResultBlock.getDirection())) {
                  this.tryCatchFire(this.level(), rayTraceResultBlock.getBlockPos(), 200, rayTraceResultBlock.getDirection());
               }
            }
         }

         this.vecWork.x = (posStart.x + posEnd.x) / 2.0;
         this.vecWork.y = (posStart.y + posEnd.y) / 2.0;
         this.vecWork.z = (posStart.z + posEnd.z) / 2.0;
         rangeBB = posStart.distanceTo(posEnd);
         AABB flameBB = this.getBoundingBox();
         flameBB.minX = this.vecWork.x - rangeBB;
         flameBB.minY = this.vecWork.y - rangeBB;
         flameBB.minZ = this.vecWork.z - rangeBB;
         flameBB.maxX = this.vecWork.x + rangeBB;
         flameBB.maxY = this.vecWork.y + rangeBB;
         flameBB.maxZ = this.vecWork.z + rangeBB;
         List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
         int len = list.size();

         for(int i = 0; i < len; ++i) {
            Entity entity = (Entity)list.get(i);
            if (this.isAllowedTarget(entity)) {
               AABB axisalignedbb1 = entity.getBoundingBox().inflate(1.0, 1.0, 1.0);
               Vec3 rayTraceResult = axisalignedbb1.clip(posStart, posEnd).orElse(null);
               if (rayTraceResult != null) {
                  if (!this.canBaseEntityUseEnergy(useEnergy)) {
                     break;
                  }

                  if (entity.hurt(this.getDamageSourceCondoctor(), this.getDamage())) {
                     ((IJBEntityState)this.baseEntity).useEntityEnergy(useEnergy);
                     this.addExtraDamageEffects(entity);
                  }
               }
            }
         }

      }
   }

   private void tryCatchFire(Level world, BlockPos pos, int fireChance, Direction face) {
      BlockState bState = world.getBlockState(pos);
      Block block = bState.getBlock();
      int flammability = block.getFlammability(bState, world, pos, face);
      if (this.level().random.nextInt(fireChance) < flammability) {
         boolean flag = block == Blocks.TNT;
         this.level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
         ((IJBEntityState)this.baseEntity).useEntityEnergy(this.getUseEnergy());
         if (flag) {
            Blocks.TNT.onCaughtFire(bState.setValue(TntBlock.UNSTABLE, true), world, pos, null, null);
         }
      }

   }

   private boolean canBaseEntityUseEnergy(float toUse) {
      if (this.baseEntity == null) {
         return false;
      } else {
         return ((IJBEntityState)this.baseEntity).getEntityEnergy() >= toUse;
      }
   }

   private boolean isAllowedTarget(Entity entity) {
      if (entity instanceof Player) {
         Player player = (Player)entity;
         if (player.getAbilities().invulnerable || this.isUnAttackableFromConductor(player)) {
            return false;
         }
      }

      if (this.baseEntity instanceof IJBEntityState) {
         IJBEntityState entityJointed = (IJBEntityState)this.baseEntity;
         if (entityJointed.isFriendlyEntity(entity)) {
            return false;
         }
      }

      return entity.isPickable() && entity != this.baseEntity;
   }

   private boolean isUnAttackableFromConductor(Player player) {
      if (this.conductorEntity != null && this.conductorEntity instanceof Player) {
         return !((Player)this.conductorEntity).canHarmPlayer(player);
      } else {
         return false;
      }
   }

   private void doRerideAttachWork() {
      Entity riddenByEntity = this.getVehicle();
      if (riddenByEntity != null && this.uuidRerideBaseEj != null) {
         if (this.tickCount % 10 == this.searchTickRand) {
            List<EntityJointed> list = this.level().getEntitiesOfClass(EntityJointed.class, this.getBoundingBox().inflate(64.0, 64.0, 64.0));
            int len = list.size();

            for(int i = 0; i < len; ++i) {
               EntityJointed entityJointed = (EntityJointed)list.get(i);
               if (entityJointed.getUUID().equals(this.uuidRerideBaseEj) && this.seatPartNum >= 0 && this.seatPartNum < entityJointed.entitySeatPartArray.length) {
                  EntitySeatPart sp = entityJointed.entitySeatPartArray[this.seatPartNum];
                  if (sp != null && CompatHelper.getRiddenByEntity(sp) == null) {
                     riddenByEntity.startRiding(sp);
                     this.setLifeLimit(-1);
                     this.discard();
                  }
               }
            }
         }

      } else {
         this.discard();
      }
   }

   public boolean shouldRenderInPass(int pass) {
      return pass == 1;
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag != null && tag.contains(NBT_TAGNAME_TEMPWORKER_RR)) {
         CompoundTag subTag = tag.getCompound(NBT_TAGNAME_TEMPWORKER_RR);
         if (subTag != null && subTag.contains("UUIDMost", 4) && subTag.contains("UUIDLeast", 4)) {
            this.workerType = WorkerType.reride;
            this.uuidRerideBaseEj = new UUID(subTag.getLong("UUIDMost"), subTag.getLong("UUIDLeast"));
            this.seatPartNum = subTag.getByte("seatAryNum");
            this.searchTickRand = (byte)this.level().random.nextInt(10);
            this.setLifeLimit(500);
            return;
         }
      }

      this.discard();
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   public void writeSpawnData(FriendlyByteBuf buffer) {
      if (this.baseEntity == null) {
         buffer.writeInt(-1);
      } else {
         buffer.writeInt(this.baseEntity.getId());
      }

      if (this.conductorEntity == null) {
         buffer.writeInt(-1);
      } else {
         buffer.writeInt(this.conductorEntity.getId());
      }

      buffer.writeInt(this.workerType.ordinal());
      buffer.writeInt(this.extraFieldValue);
   }


   /**
    * 1.12.2 Forge routed entity spawn packets through {@code NetworkHooks.getEntitySpawningPacket},
    * so {@link IEntityAdditionalSpawnData#writeSpawnData} ran for every tracked entity. 1.20.1
    * {@code ServerEntity} uses {@link Entity#getAddEntityPacket()}, which vanilla implements as a
    * plain {@code ClientboundAddEntityPacket} - the Forge spawn packet has to be returned here, or
    * the client never receives the extra spawn data.
    */
   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public void readSpawnData(FriendlyByteBuf additionalData) {
      Entity entity = this.level().getEntity(additionalData.readInt());
      if (entity instanceof LivingEntity) {
         this.baseEntity = (LivingEntity)entity;
      }

      entity = this.level().getEntity(additionalData.readInt());
      if (entity instanceof LivingEntity) {
         this.conductorEntity = (LivingEntity)entity;
      }

      this.workerType = WorkerType.values()[additionalData.readInt()];
      this.extraFieldValue = additionalData.readInt();
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

   public static enum WorkerType {
      drill,
      effect,
      blade,
      flamer,
      reride;
   }
}
