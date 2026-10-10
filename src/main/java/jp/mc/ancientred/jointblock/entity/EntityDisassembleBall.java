package jp.mc.ancientred.jointblock.entity;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.export.JBEntityJBFactory;
import jp.mc.ancientred.jointblock.registry.JBEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class EntityDisassembleBall extends ThrowableProjectile implements IEntityAdditionalSpawnData {
   /** player id -> entity id of the mount candidate clicked by that player (1.12.2 {@code IntHashMap}). */
   public static ThreadLocal<Int2IntMap> tHreadMountMap = ThreadLocal.withInitial(Int2IntOpenHashMap::new);
   public BallType type;
   public CompoundTag tagSpawnData;

   public EntityDisassembleBall(EntityType<? extends EntityDisassembleBall> type, Level level) {
      super(type, level);
      this.type = EntityDisassembleBall.BallType.disassemble;
   }

   public EntityDisassembleBall(Level level) {
      this(JBEntityTypes.DISASSEMBLE_BALL, level);
   }

   public EntityDisassembleBall(Level level, LivingEntity entityLivingBase) {
      super(JBEntityTypes.DISASSEMBLE_BALL, entityLivingBase, level);
      this.type = EntityDisassembleBall.BallType.disassemble;
   }

   public EntityDisassembleBall(Level level, double posX, double posY, double posZ) {
      super(JBEntityTypes.DISASSEMBLE_BALL, posX, posY, posZ, level);
      this.type = EntityDisassembleBall.BallType.disassemble;
   }

   protected void defineSynchedData() {
   }

   public static final BlockPos searchEmptyZone(Level world, BlockPos center) {
      BlockPos org = center;
      int yTryCnt = 0;

      do {
         BlockPos up = center.above();
         if (world.isEmptyBlock(center) && world.isEmptyBlock(up)) {
            return center;
         }

         center = up;
         ++yTryCnt;
      } while (yTryCnt < 5);

      return org;
   }

   @Override
   protected void onHit(HitResult hit) {
      if (this.type == EntityDisassembleBall.BallType.capsule) {
         if (!this.level().isClientSide && this.tagSpawnData != null) {
            Player player = null;
            if (this.getOwner() instanceof Player) {
               player = (Player)this.getOwner();
            }

            int hitX = Mth.floor(hit.getLocation().x);
            int hitY = Mth.floor(hit.getLocation().y);
            int hitZ = Mth.floor(hit.getLocation().z);
            BlockPos spawnZone = searchEmptyZone(this.level(), new BlockPos(hitX, hitY, hitZ));
            int facing = JBRotationUtil.getFacingOppositeSideFromEntity(player);
            Entity ej = JBEntityJBFactory.creaaEntityJBFromDataTag(this.tagSpawnData, this.level(), player, spawnZone.getX(), spawnZone.getY(), spawnZone.getZ(), facing);
            if (ej != null) {
               this.level().addFreshEntity(ej);
            }
         }

         this.discard();
      } else {
         if (hit instanceof EntityHitResult) {
            Entity hitEntity = ((EntityHitResult)hit).getEntity();
            if (this.type == EntityDisassembleBall.BallType.mount) {
               if (!this.level().isClientSide) {
                  this.processMountBallHit(hit);
               }
            } else {
               LivingEntity thrower = this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null;
               if (hitEntity instanceof EntityJointed) {
                  if (((EntityJointed)hitEntity).isDisassembleCallFromOwner(thrower)) {
                     ((EntityJointed)hitEntity).startDisassemble(thrower);
                  }
               } else if (hitEntity instanceof EntitySeatPart) {
                  if (((EntitySeatPart)hitEntity).isDisassembleCallFromOwner(thrower)) {
                     ((EntitySeatPart)hitEntity).entityJointed.startDisassemble(thrower);
                  }
               } else {
                  hitEntity.hurt(this.damageSources().thrown(this, thrower), 0.0F);
               }
            }
         }

         if (this.type == EntityDisassembleBall.BallType.disassemble) {
            for (int i = 0; i < 8; ++i) {
               this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
         }

         if (!this.level().isClientSide) {
            this.discard();
         }

      }
   }

   private void processMountBallHit(HitResult hit) {
      Entity hitEntity = ((EntityHitResult)hit).getEntity();
      Entity thrower = this.getOwner();
      Player throwerPlayer;
      Int2IntMap mountMap;
      if (hitEntity instanceof EntitySeatPart && thrower instanceof Player) {
         EntitySeatPart seat = (EntitySeatPart)hitEntity;
         if (seat.seatEnabled && !seat.notAsSeat && CompatHelper.getRiddenByEntity(seat) == null) {
            throwerPlayer = (Player)thrower;
            mountMap = tHreadMountMap.get();
            if (mountMap.containsKey(throwerPlayer.getId())) {
               Entity entityToMount = this.level().getEntity(mountMap.get(throwerPlayer.getId()));
               if (entityToMount != null && entityToMount instanceof LivingEntity && entityToMount.level() == seat.level() && !entityToMount.isRemoved() && (double)seat.distanceTo(entityToMount) < 64.0) {
                  entityToMount.startRiding(seat);
               }
            }
         } else if (CompatHelper.getRiddenByEntity(seat) != null && !(CompatHelper.getRiddenByEntity(seat) instanceof Player)) {
            CompatHelper.getRiddenByEntity(seat).stopRiding();
         }
      } else if (hitEntity instanceof LivingEntity && thrower instanceof Player && !(hitEntity instanceof EntityJointed) && !(hitEntity instanceof Player)) {
         Entity ridingEntity = hitEntity.getVehicle();
         if (ridingEntity != null && ridingEntity instanceof EntitySeatPart) {
            hitEntity.stopRiding();
         } else {
            throwerPlayer = (Player)thrower;
            mountMap = tHreadMountMap.get();
            mountMap.put(throwerPlayer.getId(), hitEntity.getId());
            hitEntity.push(0.0, 0.3, 0.0);
         }
      }

   }

   @Override
   public void tick() {
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
      super.baseTick();
      Vec3 vecPos = new Vec3(this.getX(), this.getY(), this.getZ());
      Vec3 vecPosPlus = new Vec3(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
      HitResult rayTraceResult = this.level().clip(new ClipContext(vecPos, vecPosPlus, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
      vecPos = new Vec3(this.getX(), this.getY(), this.getZ());
      vecPosPlus = new Vec3(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
      if (rayTraceResult.getType() != HitResult.Type.MISS) {
         vecPosPlus = new Vec3(rayTraceResult.getLocation().x, rayTraceResult.getLocation().y, rayTraceResult.getLocation().z);
      } else {
         rayTraceResult = null;
      }

      if (!this.level().isClientSide) {
         Entity entity = null;
         Vec3 motion = this.getDeltaMovement();
         AABB ballBB = this.getBoundingBox().expandTowards(motion.x, motion.y, motion.z).inflate(1.0);
         List<Entity> list = this.level().getEntities(this, ballBB, EntitySelector.NO_SPECTATORS);
         if (this.type == EntityDisassembleBall.BallType.mount) {
            this.searchEntityForMtBall(list, vecPos, ballBB);
         }

         double nearestDist = 0.0;
         LivingEntity entityThrower = this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null;

         for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            if (entity1 != entityThrower) {
               boolean canCollideEntity = false;
               if (this.type == EntityDisassembleBall.BallType.mount) {
                  canCollideEntity = !(entity1 instanceof EntityJointed) && (entity1 instanceof EntitySeatPart || entity1 instanceof LivingEntity && entity1.isPickable());
               } else {
                  canCollideEntity = entity1.isPickable();
               }

               if (canCollideEntity) {
                  float f = 0.3F;
                  AABB axisalignedbb = entity1.getBoundingBox().expandTowards((double)f, (double)f, (double)f);
                  Optional<Vec3> intercept = axisalignedbb.clip(vecPos, vecPosPlus);
                  if (intercept.isPresent()) {
                     double dist = vecPos.distanceTo(intercept.get());
                     if (dist < nearestDist || nearestDist == 0.0) {
                        entity = entity1;
                        nearestDist = dist;
                     }
                  }
               }
            }
         }

         if (entity != null) {
            rayTraceResult = new EntityHitResult(entity);
         }
      }

      if (rayTraceResult != null) {
         BlockPos hitPos = rayTraceResult instanceof BlockHitResult ? ((BlockHitResult)rayTraceResult).getBlockPos() : null;
         if (hitPos != null && WorldHelper.getBlock(this.level(), hitPos) == Blocks.NETHER_PORTAL) {
            this.handleInsidePortal(hitPos);
         } else {
            this.onHit(rayTraceResult);
         }
      }

      Vec3 mot = this.getDeltaMovement();
      this.setPos(this.getX() + mot.x, this.getY() + mot.y, this.getZ() + mot.z);
      float f1 = Mth.sqrt((float)(mot.x * mot.x + mot.z * mot.z));
      this.setYRot((float)(Math.atan2(mot.x, mot.z) * 180.0 / Math.PI));

      this.setXRot((float)(Math.atan2(mot.y, (double)f1) * 180.0 / Math.PI));

      while (this.getXRot() - this.xRotO < -180.0F) {
         this.xRotO -= 360.0F;
      }

      while (this.getXRot() - this.xRotO >= 180.0F) {
         this.xRotO += 360.0F;
      }

      while (this.getYRot() - this.yRotO < -180.0F) {
         this.yRotO -= 360.0F;
      }

      while (this.getYRot() - this.yRotO >= 180.0F) {
         this.yRotO += 360.0F;
      }

      this.setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
      this.setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);
      float f2 = 0.99F;
      float f3 = this.getGravity();
      if (this.isInWater()) {
         for (int i = 0; i < 4; ++i) {
            float f4 = 0.25F;
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - mot.x * (double)f4, this.getY() - mot.y * (double)f4, this.getZ() - mot.z * (double)f4, mot.x, mot.y, mot.z);
         }

         f2 = 0.8F;
      }

      this.setDeltaMovement(mot.x * (double)f2, mot.y * (double)f2 - (double)f3, mot.z * (double)f2);
      this.setPos(this.getX(), this.getY(), this.getZ());
   }

   private void searchEntityForMtBall(List<Entity> list, Vec3 vecPos, AABB ballBB) {
      int range = 32;
      AABB searchBB = new AABB(vecPos.x - (double)range, vecPos.y - (double)range, vecPos.z - (double)range, vecPos.x + (double)range, vecPos.y + (double)range, vecPos.z + (double)range);

      for (Entity entity : this.level().getEntities((Entity)null, searchBB, EntitySelector.NO_SPECTATORS)) {
         if (entity instanceof EntityJointed) {
            EntitySeatPart[] esAry = ((EntityJointed)entity).entitySeatPartArray;
            if (esAry == null) {
               continue;
            }

            for (int i1 = 0; i1 < esAry.length; ++i1) {
               EntitySeatPart entitySpFound = esAry[i1];
               if (entitySpFound != null && entitySpFound.getBoundingBox().intersects(ballBB)) {
                  list.add(entitySpFound);
               }
            }
         }
      }

   }

   public void writeSpawnData(FriendlyByteBuf buffer) {
      buffer.writeInt(this.type.ordinal());
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
      int type = additionalData.readInt();
      if (type > 0 && type < EntityDisassembleBall.BallType.values().length) {
         this.type = EntityDisassembleBall.BallType.values()[type];
      }

   }

   public static enum BallType {
      disassemble,
      mount,
      capsule;
   }
}
