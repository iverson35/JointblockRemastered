package jp.mc.ancientred.jointblock.assemble.ai;

import net.minecraft.util.Mth;

public class JBPathPoint {
   public int posX;
   public int posY;
   public int posZ;
   private int hash;
   int index = -1;
   float totalPathDistFromStart;
   float distToTargetPoint;
   float costDistToTarget;
   JBPathPoint previous;
   public boolean hasSearchedOption;
   int e_entryKey;
   JBPathPoint e_nextEntry;
   int e_slotHash;

   public JBPathPoint(int x, int y, int z, int preCalcedHash) {
      this.posX = x;
      this.posY = y;
      this.posZ = z;
      this.hash = preCalcedHash;
   }

   public JBPathPoint reUse(int x, int y, int z, int preCalcedHash) {
      this.posX = x;
      this.posY = y;
      this.posZ = z;
      this.hash = preCalcedHash;
      this.index = -1;
      this.totalPathDistFromStart = 0.0F;
      this.distToTargetPoint = 0.0F;
      this.costDistToTarget = 0.0F;
      this.previous = null;
      this.hasSearchedOption = false;
      this.e_nextEntry = null;
      return this;
   }

   public JBPathPoint setEntryValues(int slotHash, int key, JBPathPoint nextEntry) {
      this.e_entryKey = key;
      this.e_slotHash = slotHash;
      this.e_nextEntry = nextEntry;
      return this;
   }

   public static int makeHash(int x, int y, int z) {
      return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
   }

   public float distanceTo(JBPathPoint pathPoint) {
      float xDiff = (float)(pathPoint.posX - this.posX);
      float yDiff = (float)(pathPoint.posY - this.posY);
      float zDiff = (float)(pathPoint.posZ - this.posZ);
      return Mth.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
   }

   public float distanceToSquared(JBPathPoint pathPoint) {
      float f = (float)(pathPoint.posX - this.posX);
      float f1 = (float)(pathPoint.posY - this.posY);
      float f2 = (float)(pathPoint.posZ - this.posZ);
      return f * f + f1 * f1 + f2 * f2;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof JBPathPoint)) {
         return false;
      } else {
         JBPathPoint pathpoint = (JBPathPoint)obj;
         return this.hash == pathpoint.hash && this.posX == pathpoint.posX && this.posY == pathpoint.posY && this.posZ == pathpoint.posZ;
      }
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean isAssigned() {
      return this.index >= 0;
   }

   public String toString() {
      return this.posX + ", " + this.posY + ", " + this.posZ;
   }
}
