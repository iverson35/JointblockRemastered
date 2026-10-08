package jp.mc.ancientred.jointblock.carving.pattern;

public class FixedDoubleArrayPool {
   private static final int EACH_ARY_LENGTH = 3;
   private static final int FIXED_ARY_LENGTH = 30;
   private double[][] pool;
   private int index = 0;

   public void initOrReset() {
      if (this.pool == null) {
         this.pool = new double[30][];

         for(int i = 0; i < this.pool.length; ++i) {
            this.pool[i] = new double[3];
         }
      }

      this.index = 0;
   }

   public double[] get(double x, double y, double z) {
      double[] ary = this.pool[this.index++];
      ary[0] = x;
      ary[1] = y;
      ary[2] = z;
      return ary;
   }
}
