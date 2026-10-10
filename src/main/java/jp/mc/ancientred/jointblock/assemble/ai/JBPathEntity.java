package jp.mc.ancientred.jointblock.assemble.ai;

import jp.mc.ancientred.jointblock.assemble.EjTarget;

public class JBPathEntity {
   protected final JBPathPoint[] points;
   protected int currentPathIndex;

   public JBPathEntity(JBPathPoint[] ppAry, boolean ignoreFirst) {
      this.points = ppAry;
      if (ignoreFirst && ppAry.length != 0) {
         this.currentPathIndex = 1;
      }
   }

   public boolean isFinished() {
      return this.currentPathIndex >= this.points.length;
   }

   public JBPathPoint getPathPointFromIndex(int currentPathIndex) {
      return this.points[currentPathIndex];
   }

   public JBPathPoint getCurrentPathPoint() {
      return this.currentPathIndex >= 0 && this.currentPathIndex < this.points.length ? this.points[this.currentPathIndex] : null;
   }

   public int getCurrentPathLength() {
      return this.points.length;
   }

   public int getCurrentPathIndex() {
      return this.currentPathIndex;
   }

   public boolean isDestinationInTargetRange(EjTarget target, double range) {
      if (!target.isTargetEntity) {
         return this.isDestinationInRange(target.worldPosX, target.worldPosY, target.worldPosZ, range);
      } else {
         return target.targetEntity != null
            ? this.isDestinationInRange(target.targetEntity.getX(), target.targetEntity.getY(), target.targetEntity.getZ(), range)
            : false;
      }
   }

   public boolean isDestinationInRange(int posX, int posY, int posZ, double range) {
      JBPathPoint finalPP = this.points[this.points.length - 1];
      double xDiff = (double)(finalPP.posX - posX);
      double yDiff = (double)(finalPP.posY - posY);
      double zDiff = (double)(finalPP.posZ - posZ);
      double dist = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
      return dist < range * range * range;
   }

   public boolean isDestinationInRange(double posX, double posY, double posZ, double range) {
      JBPathPoint finalPP = this.points[this.points.length - 1];
      double xDiff = (double)finalPP.posX + 0.5 - posX;
      double yDiff = (double)finalPP.posY + 0.5 - posY;
      double zDiff = (double)finalPP.posZ + 0.5 - posZ;
      double dist = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
      return dist < range * range * range;
   }
}
