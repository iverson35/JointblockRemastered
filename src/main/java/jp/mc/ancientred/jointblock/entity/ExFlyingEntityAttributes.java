package jp.mc.ancientred.jointblock.entity;

import jp.mc.ancientred.jointblock.assemble.Quaternion;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import net.minecraft.world.phys.Vec3;

public class ExFlyingEntityAttributes {
   public float rotationYawUnfixed;
   public Quaternion coreRotQuats = new Quaternion();
   public Quaternion prevCoreRotQuats = new Quaternion();
   public Quaternion updatedCoreRotQuats = new Quaternion();
   public Quaternion workCoreRotQuats = new Quaternion();
   public Quaternion subWorkCoreRotQuats = new Quaternion();
   public Vec3 facingTwardsVec = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   public int updateInc = 0;

   public ExFlyingEntityAttributes(float initYaw) {
      this.coreRotQuats.setFromAxisAngle(Math.toRadians((double)(-initYaw)), 0.0, 1.0, 0.0);
      this.prevCoreRotQuats.setFrom(this.coreRotQuats);
      this.updatedCoreRotQuats.setFrom(this.coreRotQuats);
      this.rotationYawUnfixed = initYaw;
   }
}
