package jp.mc.ancientred.jointblock.carving;

import jp.mc.ancientred.jointblock.carving.pattern.PatternArgsBBSet;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarvingRayTraceResult {
   public AABB aabb = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   public int boxIndex;
   public int hitSide;
   public int commonHitValue;
   public int x;
   public int y;
   public int z;
   public boolean rayTraceSubBounds;
   public PatternArgsBBSet aabbSubBound;

   public int getSubBoundsArg() {
      return this.aabbSubBound == null ? -1 : this.aabbSubBound.argsNum;
   }
}
