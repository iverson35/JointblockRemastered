package jp.mc.ancientred.jointblock.carving.pattern;

public class JBPatternCoord {
   public static final double COORD_DIM = 4.0;
   public double x;
   public double y;
   public double z;

   public JBPatternCoord(double[] xyz) {
      this.set(xyz[0], xyz[1], xyz[2]);
   }

   public JBPatternCoord(double x, double y, double z) {
      this.set(x, y, z);
   }

   private void set(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public JBPatternCoord cloneThis() {
      return new JBPatternCoord(this.x, this.y, this.z);
   }

   public void turn3to4() {
      double tX = 4.0 - this.z;
      double tY = this.y;
      double tZ = this.x;
      this.set(tX, tY, tZ);
   }

   public void turn0to2() {
      double tX = this.x;
      double tY = 4.0 - this.z;
      double tZ = this.y;
      this.set(tX, tY, tZ);
   }

   public void turn0to3() {
      double tX = this.x;
      double tY = this.z;
      double tZ = 4.0 - this.y;
      this.set(tX, tY, tZ);
   }

   public void turn0to4() {
      double tX = this.y;
      double tY = 4.0 - this.x;
      double tZ = this.z;
      this.set(tX, tY, tZ);
   }

   public void turn0to5() {
      double tX = 4.0 - this.y;
      double tY = this.x;
      double tZ = this.z;
      this.set(tX, tY, tZ);
   }
}
