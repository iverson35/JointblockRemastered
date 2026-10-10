package jp.mc.ancientred.jointblock.assemble;

import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import net.minecraft.world.phys.Vec3;

public class EjSensor extends MatrixBase {
   public static final int SEARCH_RESULT_SIZE = 4;
   public final JointParts joint;
   public MatrixBase inv = new MatrixBase();
   public Vec3 cmnVec = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   public int[] searchResult;
   public double[] searchResultValue;

   public EjSensor(JointParts joint) {
      this.joint = joint;
   }

   public void ensureAndCleanSearchResult() {
      if (this.searchResult == null) {
         this.searchResult = new int[4];
         this.searchResultValue = new double[4];
      }

      for(int i = 0; i < 4; ++i) {
         this.searchResult[i] = 0;
         this.searchResultValue[i] = Double.MAX_VALUE;
      }

   }

   public boolean hasResultOfIndex(int index) {
      return this.searchResult != null && index < this.searchResult.length && this.searchResultValue[index] != Double.MAX_VALUE;
   }

   public void setPosToCmnVec() {
      this.cmnVec.x = 0.0;
      this.cmnVec.y = 0.0;
      this.cmnVec.z = 0.0;
      this.transform(this.cmnVec);
   }

   public void setWayToCmnVec() {
      this.cmnVec.x = 0.0;
      this.cmnVec.y = 0.0;
      this.cmnVec.z = -1.0;
      this.transformOnlyRotation(this.cmnVec);
   }
}
