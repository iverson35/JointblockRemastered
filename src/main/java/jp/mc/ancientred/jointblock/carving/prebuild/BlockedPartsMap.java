package jp.mc.ancientred.jointblock.carving.prebuild;

import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;

public class BlockedPartsMap {
   private static final int DIMLEN = 256;
   private static final int DIMDIV = 8;
   private static final int DIMPERBLOCK = 32;
   private JointPartsRenderable[][] map = new JointPartsRenderable[512][];

   public void put(int x, int y, int z, JointPartsRenderable parts) {
      if (this.validateXYZ(x, y, z)) {
         JointPartsRenderable[] localBlock = this.getLocalBlock(x, y, z, true);
         localBlock[this.getLocalIndex(x, y, z)] = parts;
      }
   }

   public JointPartsRenderable get(int x, int y, int z) {
      if (!this.validateXYZ(x, y, z)) {
         return null;
      } else {
         JointPartsRenderable[] localBlock = this.getLocalBlock(x, y, z, false);
         return localBlock == null ? null : localBlock[this.getLocalIndex(x, y, z)];
      }
   }

   private int getLocalIndex(int x, int y, int z) {
      int xLocal = x + 128 & 31;
      int yLocal = y + 128 & 31;
      int zLocal = z + 128 & 31;
      int localIndex = zLocal * 32 * 32 + yLocal * 32 + xLocal;
      return localIndex;
   }

   private JointPartsRenderable[] getLocalBlock(int x, int y, int z, boolean createIfNull) {
      int xHead = x + 128 >> 5;
      int yHead = y + 128 >> 5;
      int zHead = z + 128 >> 5;
      int regionIndex = zHead * 8 * 8 + yHead * 8 + xHead;
      if (this.map[regionIndex] == null) {
         if (!createIfNull) {
            return null;
         }

         this.map[regionIndex] = new JointPartsRenderable['耀'];
      }

      JointPartsRenderable[] localBlock = this.map[regionIndex];
      return localBlock;
   }

   private boolean validateXYZ(int x, int y, int z) {
      if (x <= 127 && x >= -128) {
         if (y <= 127 && y >= -128) {
            return z <= 127 && z >= -128;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
