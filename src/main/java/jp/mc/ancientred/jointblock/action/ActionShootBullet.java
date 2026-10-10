package jp.mc.ancientred.jointblock.action;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ActionShootBullet extends AbstractSoundAction {
   private static final int EXTRA_VALUE_NUM_COOLTIME = 0;
   protected EntityBullet.BulletType bulletType;
   protected float damage;
   protected double speed;
   protected int shootSide;
   protected float useEnergy;
   protected int coolTimeTick;
   protected double lazerLength;
   protected double lazerWidth;
   protected int lazerColor;
   protected float accuracy;
   protected boolean dummyExplode;
   protected boolean explodeAffectsBlocks;
   protected boolean explodeAffectsEntity;
   public float explosionSize;
   public boolean ignoreDamageCooldown;
   public boolean cutExplosionNockBack;
   public boolean disableVT;
   public boolean setMobFire;
   public int setMobPotionEffect;
   public int potionAmplifier;
   public int effectDuration;

   public ActionShootBullet() {
      this.bulletType = EntityBullet.BulletType.lazerBeam;
      this.damage = 6.0F;
      this.speed = 3.5;
      this.shootSide = 1;
      this.useEnergy = 1.0F;
      this.coolTimeTick = 3;
      this.lazerLength = 12.0;
      this.lazerWidth = 4.0;
      this.lazerColor = -855638017;
      this.accuracy = 0.2F;
      this.dummyExplode = false;
      this.explodeAffectsBlocks = true;
      this.explodeAffectsEntity = true;
      this.explosionSize = 1.5F;
      this.ignoreDamageCooldown = false;
      this.cutExplosionNockBack = false;
      this.disableVT = false;
      this.setMobFire = false;
      this.setMobPotionEffect = -1;
      this.potionAmplifier = 1;
      this.effectDuration = 5;
   }

   public String getActionName() {
      switch (this.bulletType) {
         case missile:
            return "jbaction.bullet.missile.name";
         case rokect:
            return "jbaction.bullet.rocket.name";
         case mortar:
            return "jbaction.bullet.mortar.name";
         default:
            return "jbaction.bullet.lazer.name";
      }
   }

   public void action(Level world, LivingEntity entityLivingBase, LivingEntity entityConducted, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer matrixUtil) {
      if (!world.isClientSide) {
         if (entityConducted != null) {
            if (!joint.hasInitExtraActionVars()) {
               joint.initExtraActionVars(0, -1, 0, Integer.MAX_VALUE);
            }

            int extraValue = joint.getExtraActionVars(0);
            if (extraValue <= 0) {
               if (((IJBEntityState)entityLivingBase).useEntityEnergy(this.useEnergy)) {
                  Vec3 posCenter = JBCommonUtil.createVectorHelper(0.0, 1.0, 0.0);
                  posCenter = matrixUtil.transformAbsolute(posCenter);
                  int xDirection = EnumDirectionOld.VALID_DIRECTIONS[this.shootSide].offsetX;
                  int yDirection = EnumDirectionOld.VALID_DIRECTIONS[this.shootSide].offsetY;
                  int zDirection = EnumDirectionOld.VALID_DIRECTIONS[this.shootSide].offsetZ;
                  Vec3 dirOut = JBCommonUtil.createVectorHelper((double)xDirection, (double)(-yDirection), (double)(-zDirection));
                  dirOut = matrixUtil.transformOnlyRotation(dirOut);
                  dirOut = JBCommonUtil.normalizeVec3Subtle(dirOut);
                  double vectorX = dirOut.x;
                  double vectorY = dirOut.y;
                  double vectorZ = dirOut.z;
                  EntityBullet bullet = new EntityBullet(world);
                  bullet.shootingEntity = entityLivingBase;
                  bullet.conductorEntity = entityConducted;
                  bullet.moveTo(posCenter.x + vectorX, posCenter.y + vectorY, posCenter.z + vectorZ, 0.0F, 0.0F);
                  bullet.setBulletVector(vectorX * this.speed, vectorY * this.speed, vectorZ * this.speed);
                  bullet.bulletType = this.bulletType;
                  bullet.damage = this.damage;
                  bullet.accuracy = this.accuracy;
                  bullet.lazerLength = this.lazerLength;
                  bullet.lazerWidth = this.lazerWidth;
                  bullet.extraFieldValue = this.lazerColor;
                  if (this.dummyExplode) {
                     bullet.setExplodeAffectsBlocks(false);
                     bullet.setExplodeAffectsEntity(false);
                  } else {
                     bullet.setExplodeAffectsBlocks(this.explodeAffectsBlocks);
                     bullet.setExplodeAffectsEntity(this.explodeAffectsEntity);
                  }

                  bullet.setIgnoreDamageCooldown(this.ignoreDamageCooldown);
                  bullet.setCutExplosionNockBack(this.cutExplosionNockBack);
                  bullet.setDisableVT(this.disableVT);
                  bullet.explosionSize = this.explosionSize;
                  if (this.bulletType == EntityBullet.BulletType.missile) {
                     Vec3 vec = entityConducted.getLookAngle();
                     bullet.setFixedVecX(vec.x * this.speed * 2.0);
                     bullet.setFixedVecY(vec.y * this.speed * 2.0);
                     bullet.setFixedVecZ(vec.z * this.speed * 2.0);
                     bullet.targetEntity = ((IJBEntityState)entityLivingBase).getNearestTargetEntityUnused(bullet.getX(), bullet.getY(), bullet.getZ());
                  }

                  bullet.setMobFire = this.setMobFire;
                  bullet.setMobPotionEffect = this.setMobPotionEffect;
                  bullet.potionAmplifier = this.potionAmplifier;
                  bullet.effectDuration = this.effectDuration;
                  world.addFreshEntity(bullet);
                  joint.setExtraActionVars(0, this.coolTimeTick);
                  ((IJBEntityState)entityLivingBase).notifyClientAction(joint);
               }
            }
         }
      } else {
         Vec3 soundPos = matrixUtil.transformAbsolute(JBCommonUtil.createVectorHelper(0.0, 1.0, 0.0));
         if (this.sound == null && this.soundNameLoc == null) {
            JBModContainer.proxy.playDefaultBulletSound(this.bulletType, soundPos.x, soundPos.y, soundPos.z);
         } else {
            ((JointParts)joint).updateEffectPosition(soundPos);
            JBModContainer.proxy.playPartsSoundEvent((JointParts)joint, (EntityJointed)entityLivingBase, this);
         }

      }
   }
}
