package jp.mc.ancientred.jointblock.common;

public enum EnumDirectionOld {
   DOWN(0, -1, 0),
   UP(0, 1, 0),
   NORTH(0, 0, -1),
   SOUTH(0, 0, 1),
   WEST(-1, 0, 0),
   EAST(1, 0, 0);

   public final int offsetX;
   public final int offsetY;
   public final int offsetZ;
   public static final EnumDirectionOld[] VALID_DIRECTIONS = new EnumDirectionOld[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
   public static final int[] OPPOSITES = new int[]{1, 0, 3, 2, 5, 4, 6};

   private EnumDirectionOld(int x, int y, int z) {
      this.offsetX = x;
      this.offsetY = y;
      this.offsetZ = z;
   }
}
