package jp.mc.ancientred.jointblock.carving.pattern;

import net.minecraft.world.phys.AABB;

public class PatternArgsBB extends AABB {
   public int bbColor;

   public PatternArgsBB(int bbColor, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
      this.bbColor = bbColor;
   }

   public PatternArgsBB copy() {
      return new PatternArgsBB(this.bbColor, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }
}
