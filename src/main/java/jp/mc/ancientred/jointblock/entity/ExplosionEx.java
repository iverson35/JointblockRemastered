package jp.mc.ancientred.jointblock.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class ExplosionEx {
   public static void doExplosionA(ExplosionExtraAttribute extra, Level world, Entity exploder, double posX, double posY, double posZ, float strength, List<BlockPos> affectedBlockPositions, Map<Player, Vec3> affectPlayerMap) {
      Set<BlockPos> set = Sets.newHashSet();
      ExplosionDamageCalculator damageCalculator = exploder != null ? new EntityBasedExplosionDamageCalculator(exploder) : new ExplosionDamageCalculator();

      int k;
      int l;
      for(int j = 0; j < 16; ++j) {
         for(k = 0; k < 16; ++k) {
            for(l = 0; l < 16; ++l) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                  double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  float f = strength * (0.7F + world.random.nextFloat() * 0.6F);
                  double d4 = posX;
                  double d6 = posY;
                  double d8 = posZ;

                  for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                     BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                     BlockState blockstate = world.getBlockState(blockpos);
                     FluidState fluidstate = world.getFluidState(blockpos);
                     Optional<Float> optional = damageCalculator.getBlockExplosionResistance(extra.dummy, world, blockpos, blockstate, fluidstate);
                     if (optional.isPresent()) {
                        f -= (optional.get() + 0.3F) * 0.3F;
                     }

                     if (f > 0.0F && damageCalculator.shouldBlockExplode(extra.dummy, world, blockpos, blockstate, f)) {
                        set.add(blockpos);
                     }

                     d4 += d0 * 0.30000001192092896;
                     d6 += d1 * 0.30000001192092896;
                     d8 += d2 * 0.30000001192092896;
                  }
               }
            }
         }
      }

      affectedBlockPositions.addAll(set);
      if (extra.affectsEntity) {
         float f3 = strength * 2.0F;
         k = Mth.floor(posX - (double)f3 - 1.0);
         l = Mth.floor(posX + (double)f3 + 1.0);
         int i2 = Mth.floor(posY - (double)f3 - 1.0);
         int i1 = Mth.floor(posY + (double)f3 + 1.0);
         int j2 = Mth.floor(posZ - (double)f3 - 1.0);
         int j1 = Mth.floor(posZ + (double)f3 + 1.0);
         List<Entity> list = world.getEntities(exploder, new AABB((double)k, (double)i2, (double)j2, (double)l, (double)i1, (double)j1));
         ForgeEventFactory.onExplosionDetonate(world, extra.dummy, list, (double)f3);
         Vec3 vec3d = new Vec3(posX, posY, posZ);

         for(int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);
            if (!entity.ignoreExplosion()) {
               double d12 = entity.distanceToSqr(posX, posY, posZ) / (double)f3;
               if (d12 <= 1.0) {
                  double d5 = entity.getX() - posX;
                  double d7 = entity.getY() + (double)entity.getEyeHeight() - posY;
                  double d9 = entity.getZ() - posZ;
                  double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                  if (d13 != 0.0) {
                     d5 /= d13;
                     d7 /= d13;
                     d9 /= d13;
                     double d14 = (double)Explosion.getSeenPercent(vec3d, entity);
                     double d10 = (1.0 - d12) * d14;
                     if (extra.ignoreCooldown) {
                        entity.invulnerableTime = 0;
                     }

                     entity.hurt(extra.dummy.getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)));
                     if (!extra.cutsExplosionNockBack) {
                        double d11 = d10;
                        if (entity instanceof LivingEntity) {
                           d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)entity, d10);
                        }

                        entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));
                        if (entity instanceof Player) {
                           Player entityplayer = (Player)entity;
                           if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.getAbilities().flying)) {
                              affectPlayerMap.put(entityplayer, new Vec3(d5 * d10, d7 * d10, d9 * d10));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public static void doExplosionB(ExplosionExtraAttribute extra, Level world, Entity exploder, double posX, double posY, double posZ, float strength, List<BlockPos> affectedBlockPositions, boolean spawnParticles) {
      world.playSound((Player)null, posX, posY, posZ, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);
      if (strength >= 2.0F) {
         world.addParticle(ParticleTypes.EXPLOSION_EMITTER, posX, posY, posZ, 1.0, 0.0, 0.0);
      } else {
         world.addParticle(ParticleTypes.EXPLOSION, posX, posY, posZ, 1.0, 0.0, 0.0);
      }

      for(BlockPos blockpos : affectedBlockPositions) {
         BlockState blockstate = world.getBlockState(blockpos);
         if (spawnParticles) {
            double d0 = (double)((float)blockpos.getX() + world.random.nextFloat());
            double d1 = (double)((float)blockpos.getY() + world.random.nextFloat());
            double d2 = (double)((float)blockpos.getZ() + world.random.nextFloat());
            double d3 = d0 - posX;
            double d4 = d1 - posY;
            double d5 = d2 - posZ;
            double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
            d3 /= d6;
            d4 /= d6;
            d5 /= d6;
            double d7 = 0.5 / (d6 / (double)strength + 0.1);
            d7 *= (double)(world.random.nextFloat() * world.random.nextFloat() + 0.3F);
            d3 *= d7;
            d4 *= d7;
            d5 *= d7;
            world.addParticle(ParticleTypes.POOF, (d0 + posX) / 2.0, (d1 + posY) / 2.0, (d2 + posZ) / 2.0, d3, d4, d5);
            world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
         }

         if (extra.affectsBlocks && !blockstate.isAir()) {
            if (blockstate.canDropFromExplosion(world, blockpos, extra.dummy)) {
               Block.dropResources(blockstate, world, blockpos, null, exploder, ItemStack.EMPTY);
            }

            blockstate.onBlockExploded(world, blockpos, extra.dummy);
         }
      }

   }

   public static Explosion newExplosion(Entity entity, double posX, double posY, double posZ, float strength, boolean affectsBlocks, boolean affectsEntity, boolean ignoreCooldown, boolean cutsExplosionNockBack) {
      ExplosionExtraAttribute extra = new ExplosionExtraAttribute(new Explosion(entity.level(), entity, posX, posY, posZ, strength, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY), affectsBlocks, affectsEntity, ignoreCooldown, cutsExplosionNockBack);
      Map<Player, Vec3> affectPlayerMap = new HashMap<>();
      List<BlockPos> affectedBlockPositions = Lists.newArrayList();
      doExplosionA(extra, entity.level(), entity, posX, posY, posZ, strength, affectedBlockPositions, affectPlayerMap);
      doExplosionB(extra, entity.level(), entity, posX, posY, posZ, strength, affectedBlockPositions, false);

      for(Player entityplayer : entity.level().players()) {
         if (entityplayer instanceof ServerPlayer && entityplayer.distanceToSqr(posX, posY, posZ) < 4096.0) {
            JBPacketSender.sendDummyExplosionToPlayer((ServerPlayer)entityplayer, posX, posY, posZ, strength, affectsBlocks, affectsEntity, affectedBlockPositions, affectPlayerMap.get(entityplayer));
         }
      }

      return extra.dummy;
   }

   public static class ExplosionExtraAttribute {
      Explosion dummy;
      public boolean affectsBlocks;
      public boolean affectsEntity;
      public boolean ignoreCooldown;
      public boolean cutsExplosionNockBack;

      public ExplosionExtraAttribute(Explosion dummy, boolean affectsBlocks, boolean affectsEntity, boolean ignoreCooldown, boolean cutsExplosionNockBack) {
         this.dummy = dummy;
         this.affectsBlocks = affectsBlocks;
         this.affectsEntity = affectsEntity;
         this.ignoreCooldown = ignoreCooldown;
         this.cutsExplosionNockBack = cutsExplosionNockBack;
      }
   }
}
