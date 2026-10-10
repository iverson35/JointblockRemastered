package jp.mc.ancientred.jointblock.entity;

import jp.mc.ancientred.jointblock.assemble.Quaternion;
import jp.mc.ancientred.jointblock.registry.JBClientEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityJBClientCamera extends Entity implements IIgnoreTarget {
   public Entity parentEntity;
   public double updatedPosX;
   public double updatedPosY;
   public double updatedPosZ;
   public float prevCameraRoll;
   public float cameraRoll;
   public Quaternion workQuat = new Quaternion();
   public Quaternion rotCamQuat = new Quaternion();
   public Quaternion prevRotCamQuat = new Quaternion();
   public Quaternion updatedRotCamQuat = new Quaternion();

   public EntityJBClientCamera(EntityType<? extends EntityJBClientCamera> type, Level level) {
      super(type, level);
   }

   public EntityJBClientCamera(Level level) {
      this(JBClientEntityTypes.CLIENT_CAMERA, level);
   }

   public void calculateRotationForEveryRender(float tickPartial) {
      Quaternion q = Quaternion.slerp(tickPartial, this.prevRotCamQuat, this.rotCamQuat, this.workQuat);
      q.normalise();
      double sqw = (double)(q.w * q.w);
      double sqx = (double)(q.x * q.x);
      double sqy = (double)(q.y * q.y);
      double sqz = (double)(q.z * q.z);
      double m00 = sqx - sqy - sqz + sqw;
      double m11 = -sqx + sqy - sqz + sqw;
      double m22 = -sqx - sqy + sqz + sqw;
      double tmp1 = (double)(q.x * q.y);
      double tmp2 = (double)(q.z * q.w);
      double m10 = 2.0 * (tmp1 + tmp2);
      double m01 = 2.0 * (tmp1 - tmp2);
      tmp1 = (double)(q.x * q.z);
      tmp2 = (double)(q.y * q.w);
      double m20 = 2.0 * (tmp1 - tmp2);
      double m02 = 2.0 * (tmp1 + tmp2);
      tmp1 = (double)(q.y * q.z);
      tmp2 = (double)(q.x * q.w);
      double m21 = 2.0 * (tmp1 + tmp2);
      double m12 = 2.0 * (tmp1 - tmp2);
      double updatedYaw = -Math.toDegrees(Math.atan2(m02, m22));
      double updatedPitch = Math.toDegrees(Math.atan2(-m12, Math.sqrt(m02 * m02 + m22 * m22)));
      double updatedRoll = -Math.toDegrees(Math.atan2(-m10, m11));
      this.yRotO = (float)updatedYaw;
      this.setYRot((float)updatedYaw);
      this.xRotO = (float)updatedPitch;
      this.setXRot((float)updatedPitch);
      this.prevCameraRoll = this.cameraRoll = (float)updatedRoll;
   }

   public void tick() {
      this.xOld = this.xo = this.getX();
      this.yOld = this.yo = this.getY();
      this.zOld = this.zo = this.getZ();
      this.prevRotCamQuat.setFrom(this.rotCamQuat);
      if (!this.level().isClientSide) {
         this.discard();
      } else if (this.parentEntity == null) {
         this.discard();
      } else {
         if (this.parentEntity instanceof EntitySeatPart) {
            EntitySeatPart seat = (EntitySeatPart)this.parentEntity;
            if (seat.entityJointed == null || seat.entityJointed.isRemoved()) {
               this.parentEntity = null;
               this.discard();
               return;
            }
         } else if (this.parentEntity.isRemoved()) {
            this.parentEntity = null;
            this.discard();
            return;
         }

         this.setPosRaw(this.updatedPosX, this.updatedPosY, this.updatedPosZ);
         this.rotCamQuat.setFrom(this.updatedRotCamQuat);
      }
   }

   protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
      return 0.0F;
   }

   protected void defineSynchedData() {
   }

   public void baseTick() {
   }

   public boolean isInvulnerableTo(DamageSource source) {
      return true;
   }

   public boolean isAttackable() {
      return false;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean canRiderInteract() {
      return false;
   }

   public boolean isInvisible() {
      return true;
   }

   public boolean isInvisibleTo(Player player) {
      return true;
   }

   public boolean isInWall() {
      return false;
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public void moveTo(double x, double y, double z, float yaw, float pitch) {
   }

   public void absMoveTo(double x, double y, double z, float yaw, float pitch) {
   }

   public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      this.discard();
   }

   protected void addAdditionalSaveData(CompoundTag compound) {
   }
}
