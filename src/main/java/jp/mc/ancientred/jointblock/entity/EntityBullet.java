package jp.mc.ancientred.jointblock.entity;

import java.util.List;
import java.util.Optional;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.TypedLinkable;
import jp.mc.ancientred.jointblock.entity.fx.EntityWhiteSmokeFx;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class EntityBullet extends Entity implements IEntityAdditionalSpawnData, TypedLinkable {
   private static final double INCIDENT_RAD_TO_DETECT_BLOCKHIT = 0.5235987755982988;
   private int entityAge;
   public BulletType bulletType;
   public LivingEntity shootingEntity;
   public LivingEntity conductorEntity;
   public float accuracy;
   public float damage;
   public double lazerLength;
   public double lazerWidth;
   public int extraFieldValue;
   public int extraFlags;
   public double fixedVecX;
   public double fixedVecY;
   public double fixedVecZ;
   public boolean setMobFire;
   public int setMobPotionEffect;
   public int potionAmplifier;
   public int effectDuration;
   public float explosionSize;
   public Entity targetEntity;
   private Vec3 vecWorkA;
   private Vec3 vecMissMot;
   private static final int EXTRA_FLAGS_EXPLODE_AFFECTS_BLOCKS = 1;
   private static final int EXTRA_FLAGS_EXPLODE_AFFECTS_ENTITY = 2;
   private static final int EXTRA_FLAGS_IGNORE_DAMAGE_COOLDOWN = 4;
   private static final int EXTRA_FLAGS_CUT_EXPLOSION_NOCKBACK = 8;
   private static final int EXTRA_FLAGS_DISABLE_VT_EXPLOSION = 16;
   private TypedLinkable next;
   private TypedLinkable prev;
   private long renderCheck;

   public EntityBullet(EntityType<? extends EntityBullet> type, Level level) {
      super(type, level);
      this.bulletType = EntityBullet.BulletType.lazerBeam;
      this.accuracy = 0.2F;
      this.damage = 1.0F;
      this.lazerLength = 16.0;
      this.lazerWidth = 4.0;
      this.extraFieldValue = -855638017;
      this.extraFlags = 0;
      this.setMobFire = false;
      this.setMobPotionEffect = -1;
      this.potionAmplifier = 1;
      this.effectDuration = 5;
      this.vecWorkA = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.vecMissMot = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
      this.renderCheck = -1L;
      this.setExplodeAffectsBlocks(true);
      this.setExplodeAffectsEntity(true);
   }

   public EntityBullet(Level level) {
      this(JBEntityTypes.BULLET, level);
   }

   /** 1.12.2 {@code this.isImmuneToFire = true} moved to the (immutable) 1.20.1 entity type hook. */
   @Override
   public boolean fireImmune() {
      return true;
   }

   public void setFixedVecX(double fixedVecX) {
      this.fixedVecX = fixedVecX;
      this.vecMissMot = new Vec3(this.vecMissMot.x, fixedVecX, this.vecMissMot.z);
   }

   public void setFixedVecY(double fixedVecY) {
      this.fixedVecY = fixedVecY;
      this.vecMissMot = new Vec3(this.vecMissMot.x, fixedVecY, this.vecMissMot.z);
   }

   public void setFixedVecZ(double fixedVecZ) {
      this.fixedVecZ = fixedVecZ;
      this.vecMissMot = new Vec3(this.vecMissMot.x, this.vecMissMot.y, fixedVecZ);
   }

   public boolean isExplodeAffectsBlocks() {
      return (this.extraFlags & 1) != 0;
   }

   public void setExplodeAffectsBlocks(boolean value) {
      if (value) {
         this.extraFlags |= 1;
      } else {
         this.extraFlags &= -2;
      }

   }

   public boolean isExplodeAffectsEntity() {
      return (this.extraFlags & 2) != 0;
   }

   public void setExplodeAffectsEntity(boolean value) {
      if (value) {
         this.extraFlags |= 2;
      } else {
         this.extraFlags &= -3;
      }

   }

   public boolean isIgnoreDamageCooldown() {
      return (this.extraFlags & 4) != 0;
   }

   public void setIgnoreDamageCooldown(boolean value) {
      if (value) {
         this.extraFlags |= 4;
      } else {
         this.extraFlags &= -5;
      }

   }

   public boolean isCutExplosionNockBack() {
      return (this.extraFlags & 8) != 0;
   }

   public void setCutExplosionNockBack(boolean value) {
      if (value) {
         this.extraFlags |= 8;
      } else {
         this.extraFlags &= -9;
      }

   }

   public boolean isDisableVT() {
      return (this.extraFlags & 16) != 0;
   }

   public void setDisableVT(boolean value) {
      if (value) {
         this.extraFlags |= 16;
      } else {
         this.extraFlags &= -17;
      }

   }

   public void setBulletVector(double motionX, double motionY, double motionZ) {
      this.vecMissMot = new Vec3(motionX, motionY, motionZ);
      this.setDeltaMovement(motionX, motionY, motionZ);
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float xz = Mth.sqrt((float)(motionX * motionX + motionZ * motionZ));
         this.yRotO = (float)(Math.atan2(motionX, motionZ) * 180.0 / Math.PI);
         this.setYRot(this.yRotO);
         this.xRotO = (float)(Math.atan2(motionY, (double)xz) * 180.0 / Math.PI);
         this.setXRot(this.xRotO);
      }

   }

   protected void defineSynchedData() {
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         JBModContainer.proxy.addToStickyRenderList(this);
      }

      if (this.conductorEntity != null && this.shootingEntity != null) {
         ++this.entityAge;
         Vec3 motion = this.getDeltaMovement();
         boolean missileMoratorium = false;
         if (this.entityAge <= 300 && (!(Math.abs(motion.x) < 0.01) || !(Math.abs(motion.y) < 0.01) || !(Math.abs(motion.z) < 0.01))) {
            if (this.bulletType == EntityBullet.BulletType.missile && this.entityAge < 15) {
               missileMoratorium = true;
            }
         } else {
            this.discard();
         }

         double distanceMin;
         double expandBB;
         Entity entityHit;
         double distance;
         double posXNew;
         if (!missileMoratorium) {
            Vec3 nowPos = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
            Vec3 movedPos;
            if (this.bulletType == EntityBullet.BulletType.missile) {
               movedPos = JBCommonUtil.createVectorHelper(this.getX() + this.vecMissMot.x, this.getY() + this.vecMissMot.y, this.getZ() + this.vecMissMot.z);
            } else {
               movedPos = JBCommonUtil.createVectorHelper(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            }

            HitResult rayTraceResult = this.level().clip(new ClipContext(nowPos, movedPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            nowPos = JBCommonUtil.createVectorHelper(this.getX(), this.getY(), this.getZ());
            movedPos = JBCommonUtil.createVectorHelper(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            if (rayTraceResult.getType() == HitResult.Type.MISS) {
               rayTraceResult = null;
            } else {
               movedPos = JBCommonUtil.createVectorHelper(rayTraceResult.getLocation().x, rayTraceResult.getLocation().y, rayTraceResult.getLocation().z);
            }

            boolean vt = !this.isDisableVT() && (this.bulletType == EntityBullet.BulletType.rokect || this.bulletType == EntityBullet.BulletType.missile) && this.entityAge > 15;
            entityHit = null;
            distanceMin = 0.0;
            expandBB = vt ? (this.bulletType == EntityBullet.BulletType.missile ? 2.0 : 5.0) : 1.0;
            List<Entity> list = this.level().getEntities(this, this.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(expandBB, expandBB, expandBB), EntitySelector.NO_SPECTATORS);
            int len = list.size();

            for (int i = 0; i < len; ++i) {
               Entity entity = list.get(i);
               if (this.isAllowedTarget(entity)) {
                  if (vt) {
                     if (!this.level().isClientSide) {
                        if (this.bulletType == EntityBullet.BulletType.missile) {
                           this.makeExplosion(entity.getX() - 0.1 + Math.random() * 0.2, entity.getY() + (double)entity.getBbHeight() / 2.0 - 0.1 + Math.random() * 0.2, entity.getZ() - 0.1 + Math.random() * 0.2);
                        } else {
                           posXNew = this.getX() + motion.x;
                           distance = this.getY() + motion.y;
                           double posZNew = this.getZ() + motion.z;
                           this.makeExplosion((entity.getX() + posXNew) / 2.0, (entity.getY() + distance) / 2.0, (entity.getZ() + posZNew) / 2.0);
                        }
                     }

                     this.discard();
                     return;
                  }

                  AABB axisalignedbb1 = entity.getBoundingBox().expandTowards(0.5, 0.5, 0.5);
                  Optional<Vec3> intercept = axisalignedbb1.clip(nowPos, movedPos);
                  if (intercept.isPresent()) {
                     distance = nowPos.distanceTo(intercept.get());
                     if (distance < distanceMin || distanceMin == 0.0) {
                        entityHit = entity;
                        distanceMin = distance;
                     }
                  }
               }
            }

            if (entityHit != null) {
               rayTraceResult = new EntityHitResult(entityHit);
            }

            if (rayTraceResult != null) {
               if (this.bulletType == EntityBullet.BulletType.missile) {
                  if (rayTraceResult instanceof EntityHitResult) {
                     if (this.isDisableVT()) {
                        this.processDirectDamage(rayTraceResult);
                     } else if (!this.level().isClientSide) {
                        this.makeExplosion(rayTraceResult.getLocation().x, rayTraceResult.getLocation().y, rayTraceResult.getLocation().z);
                     }

                     this.discard();
                  } else if (rayTraceResult.getType() == HitResult.Type.BLOCK && !this.level().isClientSide && ((BlockHitResult)rayTraceResult).getDirection() != null) {
                     Vec3i sideHitVec = ((BlockHitResult)rayTraceResult).getDirection().getNormal();
                     this.vecWorkA = new Vec3((double)sideHitVec.getX(), this.vecWorkA.y, this.vecWorkA.z);
                     this.vecWorkA = new Vec3(this.vecWorkA.x, (double)sideHitVec.getY(), this.vecWorkA.z);
                     this.vecWorkA = new Vec3(this.vecWorkA.x, this.vecWorkA.y, (double)sideHitVec.getZ());
                     JBCommonUtil.normalizeVec3Subtle(this.vecWorkA);
                     JBCommonUtil.normalizeVec3Subtle(this.vecMissMot);
                     if (rayTraceResult.getLocation() != null && 1.0 - Math.abs(this.vecMissMot.dot(this.vecWorkA)) < 0.5235987755982988) {
                        this.discard();
                     }
                  }
               } else {
                  if (rayTraceResult instanceof EntityHitResult) {
                     this.processDirectDamage(rayTraceResult);
                  }

                  this.discard();
               }

               if (!this.level().isClientSide && (this.bulletType == EntityBullet.BulletType.rokect || this.bulletType == EntityBullet.BulletType.mortar)) {
                  this.makeExplosion(rayTraceResult.getLocation().x, rayTraceResult.getLocation().y, rayTraceResult.getLocation().z);
               }
            }
         }

         if (this.level().isClientSide && this.bulletType != EntityBullet.BulletType.lazerBeam) {
            this.spawnWhiteSmoke();
         }

         if (this.bulletType == EntityBullet.BulletType.missile && !missileMoratorium) {
            entityHit = null;
            expandBB = 10.0;
            distanceMin = 0.0;
            if (this.targetEntity != null && !this.targetEntity.isRemoved()) {
               entityHit = this.targetEntity;
            } else {
               List<Entity> list = this.level().getEntities(this, this.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(expandBB, expandBB, expandBB), EntitySelector.NO_SPECTATORS);
               int len = list.size();

               for (int i = 0; i < len; ++i) {
                  Entity entity = list.get(i);
                  if (this.isAllowedTarget(entity)) {
                     distance = this.distanceToSqr(entity);
                     if (distance < distanceMin || distanceMin == 0.0) {
                        entityHit = entity;
                        distanceMin = distance;
                     }
                  }
               }
            }

            double motX;
            double motY;
            if (entityHit != null) {
               this.vecWorkA = new Vec3(entityHit.getX() - this.getX(), this.vecWorkA.y, this.vecWorkA.z);
               this.vecWorkA = new Vec3(this.vecWorkA.x, entityHit.getY() + (double)entityHit.getBbHeight() / 2.0 - this.getY(), this.vecWorkA.z);
               this.vecWorkA = new Vec3(this.vecWorkA.x, this.vecWorkA.y, entityHit.getZ() - this.getZ());
               JBCommonUtil.normalizeVec3Subtle(this.vecWorkA);
               JBCommonUtil.normalizeVec3Subtle(this.vecMissMot);
               JBCommonUtil.slerp(this.vecMissMot, this.vecWorkA, (double)this.accuracy, this.vecMissMot);
               motX = this.vecMissMot.x;
               motY = this.vecMissMot.y;
               distance = this.vecMissMot.z;
            } else {
               motX = this.fixedVecX;
               this.vecMissMot = new Vec3(motX, this.vecMissMot.y, this.vecMissMot.z);
               motY = this.fixedVecY;
               this.vecMissMot = new Vec3(this.vecMissMot.x, motY, this.vecMissMot.z);
               distance = this.fixedVecZ;
               this.vecMissMot = new Vec3(this.vecMissMot.x, this.vecMissMot.y, distance);
            }

            this.move(MoverType.SELF, new Vec3(motX, motY, distance));
            double xdiff = this.getX() - this.xo;
            posXNew = this.getY() - this.yo;
            distance = this.getZ() - this.zo;
            float xz = Mth.sqrt((float)(xdiff * xdiff + distance * distance));
            this.setYRot((float)(Math.atan2(xdiff, distance) * 180.0 / Math.PI));
            this.setXRot((float)(Math.atan2(posXNew, (double)xz) * 180.0 / Math.PI));
         } else {
            if (this.bulletType == EntityBullet.BulletType.mortar) {
               this.setDeltaMovement(motion.x * 0.99, motion.y * 0.99 - 0.05, motion.z * 0.99);
               motion = this.getDeltaMovement();
            }

            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
         }

         this.setPos(this.getX(), this.getY(), this.getZ());
         this.checkInsideBlocks();
      } else {
         this.discard();
      }
   }

   private void processDirectDamage(HitResult rayTraceResult) {
      Entity hitEntity = ((EntityHitResult)rayTraceResult).getEntity();
      if (this.isIgnoreDamageCooldown()) {
         hitEntity.invulnerableTime = 0;
      }

      if (hitEntity.hurt(this.createDamageSource(), this.damage) && !this.level().isClientSide) {
         this.addExtraDamageEffects(hitEntity);
      }

   }

   private DamageSource createDamageSource() {
      DamageSource damagesource;
      if (this.conductorEntity != null) {
         if (this.conductorEntity instanceof Player) {
            damagesource = this.damageSources().playerAttack((Player)this.conductorEntity);
         } else {
            damagesource = this.damageSources().mobAttack(this.conductorEntity);
         }
      } else if (this.shootingEntity != null) {
         damagesource = this.damageSources().mobAttack(this.shootingEntity);
      } else {
         damagesource = this.damageSources().mobAttack((LivingEntity)null);
      }

      return damagesource;
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

   private boolean isAllowedTarget(Entity entity) {
      if (entity instanceof Player) {
         Player player = (Player)entity;
         if (player.getAbilities().invulnerable || this.isUnAttackableFromConductor(player)) {
            return false;
         }
      }

      if (this.shootingEntity instanceof IJBEntityState) {
         IJBEntityState entityJointed = (IJBEntityState)this.shootingEntity;
         if (entityJointed.isFriendlyEntity(entity)) {
            return false;
         }
      }

      return entity.isPickable() && entity != this.shootingEntity;
   }

   private boolean isUnAttackableFromConductor(Player player) {
      if (this.conductorEntity != null && this.conductorEntity instanceof Player) {
         return !((Player)this.conductorEntity).canHarmPlayer(player);
      } else {
         return false;
      }
   }

   private void makeExplosion(double x, double y, double z) {
      if (!this.level().isClientSide) {
         if (this.conductorEntity != null) {
            if (this.bulletType == EntityBullet.BulletType.missile) {
               this.doExplosion(this.conductorEntity, x, y, z, this.explosionSize);
            } else if (this.bulletType == EntityBullet.BulletType.mortar) {
               this.doExplosion(this.conductorEntity, x, y, z, this.explosionSize);
            } else {
               this.doExplosion(this.conductorEntity, x, y, z, this.explosionSize);
            }
         }

      }
   }

   public void doExplosion(Entity entity, double posX, double posY, double posZ, float explosionSize) {
      ExplosionEx.newExplosion(entity, posX, posY, posZ, explosionSize, this.isExplodeAffectsBlocks(), this.isExplodeAffectsEntity(), this.isIgnoreDamageCooldown(), this.isCutExplosionNockBack());
   }

   @OnlyIn(Dist.CLIENT)
   public void spawnWhiteSmoke() {
      Level world = this.level();

      for (int i = 0; i < 4; ++i) {
         Minecraft.getInstance().particleEngine.add(new EntityWhiteSmokeFx(world, this.getX() + this.getDeltaMovement().x * (double)i / 4.0, this.getY() + this.getDeltaMovement().y * (double)i / 4.0, this.getZ() + this.getDeltaMovement().z * (double)i / 4.0));
      }

   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.discard();
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   @Override
   public boolean save(CompoundTag tag) {
      return false;
   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
      if (this.shootingEntity == null) {
         buffer.writeInt(-1);
      } else {
         buffer.writeInt(this.shootingEntity.getId());
      }

      if (this.conductorEntity == null) {
         buffer.writeInt(-1);
      } else {
         buffer.writeInt(this.conductorEntity.getId());
      }

      if (this.targetEntity == null) {
         buffer.writeInt(-1);
      } else {
         buffer.writeInt(this.targetEntity.getId());
      }

      buffer.writeInt(this.bulletType.ordinal());
      buffer.writeFloat(this.damage);
      buffer.writeFloat(this.accuracy);
      buffer.writeDouble(this.lazerWidth);
      buffer.writeDouble(this.lazerLength);
      buffer.writeInt(this.extraFieldValue);
      buffer.writeInt(this.extraFlags);
      if (this.bulletType == EntityBullet.BulletType.missile) {
         buffer.writeDouble(this.fixedVecX);
         buffer.writeDouble(this.fixedVecY);
         buffer.writeDouble(this.fixedVecZ);
      }

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

   public void readSpawnData(FriendlyByteBuf additionalData) {
      int shootingEntityId = additionalData.readInt();
      int conductedEntityId = additionalData.readInt();
      int targetEntityId = additionalData.readInt();
      this.bulletType = EntityBullet.BulletType.values()[additionalData.readInt()];
      this.damage = additionalData.readFloat();
      this.accuracy = additionalData.readFloat();
      this.lazerWidth = additionalData.readDouble();
      this.lazerLength = additionalData.readDouble();
      this.extraFieldValue = additionalData.readInt();
      this.extraFlags = additionalData.readInt();
      if (this.bulletType == EntityBullet.BulletType.missile) {
         this.setFixedVecX(additionalData.readDouble());
         this.setFixedVecY(additionalData.readDouble());
         this.setFixedVecZ(additionalData.readDouble());
      }

      Entity conductedEntity = this.level().getEntity(conductedEntityId);
      if (conductedEntity != null && conductedEntity instanceof LivingEntity) {
         this.conductorEntity = (LivingEntity)conductedEntity;
      }

      Entity shotByEntity = this.level().getEntity(shootingEntityId);
      if (shotByEntity != null && shotByEntity instanceof LivingEntity) {
         this.shootingEntity = (LivingEntity)shotByEntity;
      }

      Entity tgtEntity = this.level().getEntity(targetEntityId);
      if (tgtEntity != null) {
         this.targetEntity = tgtEntity;
      }

   }

   public TypedLinkable getNext() {
      return this.next;
   }

   public TypedLinkable getPrev() {
      return this.prev;
   }

   public void setNext(TypedLinkable linkable) {
      this.next = linkable;
   }

   public void setPrev(TypedLinkable linkable) {
      this.prev = linkable;
   }

   public void clearRef() {
      this.next = null;
      this.prev = null;
   }

   public boolean checkRendered(long token) {
      if (this.renderCheck == token) {
         return false;
      } else {
         this.renderCheck = token;
         return true;
      }
   }

   public static enum BulletType {
      lazerBeam,
      rokect,
      missile,
      mortar;
   }
}
