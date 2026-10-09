package jp.mc.ancientred.jointblock.assemble;

import java.util.Comparator;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.Direction;

public class JointPartsForCollect extends JointParts {
   public int xCoordInt;
   public int yCoordInt;
   public int zCoordInt;
   public int absPosX;
   public int absPosY;
   public int absPosZ;
   public TileEntityJoint te;
   public TileEntityJoint dstTe;
   public boolean ignoreDestroy;

   public JointPartsForCollect(int relX, int relY, int relZ, int posX, int posY, int posZ) {
      super(0, 0, 0);
      this.xCoordInt = relX;
      this.yCoordInt = relY;
      this.zCoordInt = relZ;
      this.absPosX = posX;
      this.absPosY = posY;
      this.absPosZ = posZ;
   }

   public int hashCode() {
      int result = 17;
      result = 37 * result + this.xCoordInt;
      result = 37 * result + this.yCoordInt;
      result = 37 * result + this.zCoordInt;
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         JointPartsForCollect other = (JointPartsForCollect)obj;
         return this.xCoordInt == other.xCoordInt && this.yCoordInt == other.yCoordInt && this.zCoordInt == other.zCoordInt;
      }
   }

   public static Comparator<JointPartsForCollect> getComparatorByDir(Direction dir) {
      switch (dir) {
         case WEST:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = Integer.compare(o1.absPosX, o2.absPosX);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosY, o2.absPosY);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosZ, o2.absPosZ);
                  }

                  return res;
               }
            };
         case EAST:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = -Integer.compare(o1.absPosX, o2.absPosX);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosY, o2.absPosY);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosZ, o2.absPosZ);
                  }

                  return res;
               }
            };
         case NORTH:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = Integer.compare(o1.absPosZ, o2.absPosZ);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosX, o2.absPosX);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosY, o2.absPosY);
                  }

                  return res;
               }
            };
         case SOUTH:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = -Integer.compare(o1.absPosZ, o2.absPosZ);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosX, o2.absPosX);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosY, o2.absPosY);
                  }

                  return res;
               }
            };
         case DOWN:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = Integer.compare(o1.absPosY, o2.absPosY);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosZ, o2.absPosZ);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosX, o2.absPosX);
                  }

                  return res;
               }
            };
         case UP:
         default:
            return new Comparator<JointPartsForCollect>() {
               public int compare(JointPartsForCollect o1, JointPartsForCollect o2) {
                  int res = -Integer.compare(o1.absPosY, o2.absPosY);
                  if (res == 0) {
                     res = Integer.compare(o1.absPosZ, o2.absPosZ);
                  }

                  if (res == 0) {
                     res = Integer.compare(o1.absPosX, o2.absPosX);
                  }

                  return res;
               }
            };
      }
   }
}
