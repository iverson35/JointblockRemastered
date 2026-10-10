package jp.mc.ancientred.jointblock.assemble.tuning;

public class TuningBarInf {
   public int barSideNum;
   public double minX;
   public double maxX;
   public double minY;
   public double maxY;
   public double minZ;
   public double maxZ;
   public int repeatCount;
   public double[] offset;
   private static final double BAR_W = 0.3;
   private static final double TIN_W = 0.025;
   private static final double TIC_W = 0.3;
   private static final double BAR_H = 0.75;
   private static final double NONE = 0.0;
   private static TuningBarInf[] TUNING_BAR_INFS_Rt = new TuningBarInf[]{new TuningBarInf(0, -0.3, 0.3, -0.25, 0.5, -0.025, 0.025, 2, new double[]{0.0, -0.75, 0.0}), new TuningBarInf(1, -0.3, 0.3, 1.5, 2.25, -0.025, 0.025, 2, new double[]{0.0, 0.75, 0.0}), new TuningBarInf(2, -0.025, 0.025, 0.7, 1.3, 0.5, 1.25, 2, new double[]{0.0, 0.0, 0.75}), new TuningBarInf(3, -0.025, 0.025, 0.7, 1.3, -1.25, -0.5, 2, new double[]{0.0, 0.0, -0.75}), new TuningBarInf(4, -1.25, -0.5, 0.975, 1.025, -0.3, 0.3, 2, new double[]{-0.75, 0.0, 0.0}), new TuningBarInf(5, 0.5, 1.25, 0.975, 1.025, -0.3, 0.3, 2, new double[]{0.75, 0.0, 0.0})};
   private static TuningBarInf[] TUNING_BAR_INFS_SL = new TuningBarInf[]{new TuningBarInf(0, -0.3, 0.3, -0.25, 0.5, -0.025, 0.025, 2, new double[]{0.0, -0.75, 0.0}), new TuningBarInf(1, -0.3, 0.3, 1.5, 2.25, -0.025, 0.025, 2, new double[]{0.0, 0.75, 0.0}), new TuningBarInf(2, -0.025, 0.025, 0.7, 1.3, 0.5, 1.25, 2, new double[]{0.0, 0.0, 0.75}), new TuningBarInf(3, -0.025, 0.025, 0.7, 1.3, -1.25, -0.5, 2, new double[]{0.0, 0.0, -0.75}), new TuningBarInf(4, -1.25, -0.5, 0.7, 1.3, -0.025, 0.025, 2, new double[]{-0.75, 0.0, 0.0}), new TuningBarInf(5, 0.5, 1.25, 0.7, 1.3, -0.025, 0.025, 2, new double[]{0.75, 0.0, 0.0})};
   private static TuningBarInf[] TUNING_BAR_INFS_ExSc = new TuningBarInf[]{new TuningBarInf(0, -0.3, 0.3, -0.25, 0.5, -0.025, 0.025, 2, new double[]{0.0, -0.75, 0.0}), new TuningBarInf(1, -0.3, 0.3, 1.5, 2.25, -0.025, 0.025, 2, new double[]{0.0, 0.75, 0.0})};

   public TuningBarInf(int barSideNum, double minX, double maxX, double minY, double maxY, double minZ, double maxZ, int repeatCount, double[] offset) {
      this.barSideNum = barSideNum;
      this.minX = minX;
      this.maxX = maxX;
      this.minY = minY;
      this.maxY = maxY;
      this.minZ = minZ;
      this.maxZ = maxZ;
      this.repeatCount = repeatCount;
      this.offset = offset;
   }

   public static TuningBarInf[] getTuningBarInfsByJointType(int jointType) {
      switch (jointType) {
         case 8:
            return TUNING_BAR_INFS_Rt;
         case 9:
         case 11:
         case 13:
         default:
            return null;
         case 10:
         case 12:
            return TUNING_BAR_INFS_ExSc;
         case 14:
            return TUNING_BAR_INFS_SL;
      }
   }
}
