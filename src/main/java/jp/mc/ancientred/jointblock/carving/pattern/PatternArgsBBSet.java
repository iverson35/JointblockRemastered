package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.common.JBCommonUtil;

public class PatternArgsBBSet {
   public static final int TYPE_ARGS = 0;
   public static final int TYPE_SIDE_FACE = 1;
   public int type;
   public int argsNum;
   public PatternArgsBB[] bbAry;

   public PatternArgsBBSet(int type, int argsNum, PatternArgsBB... ary) {
      this.type = type;
      this.argsNum = argsNum;
      this.bbAry = ary;
   }

   private PatternArgsBBSet() {
   }

   public PatternArgsBBSet copy() {
      PatternArgsBBSet newObj = new PatternArgsBBSet();
      newObj.type = this.type;
      newObj.argsNum = this.argsNum;
      if (this.bbAry != null) {
         newObj.bbAry = new PatternArgsBB[this.bbAry.length];

         for(int i = 0; i < newObj.bbAry.length; ++i) {
            newObj.bbAry[i] = this.bbAry[i].copy();
         }
      }

      return newObj;
   }

   public void offsetAll(int x, int y, int z) {
      for(int i = 0; i < this.bbAry.length; ++i) {
         this.bbAry[i] = (PatternArgsBB)JBCommonUtil.offsetBB(this.bbAry[i], (double)x, (double)y, (double)z);
      }

   }
}
