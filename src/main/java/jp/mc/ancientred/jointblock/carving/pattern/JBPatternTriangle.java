package jp.mc.ancientred.jointblock.carving.pattern;

import net.minecraft.util.Mth;

public class JBPatternTriangle {
   public boolean enabled = true;
   public float normalX;
   public float normalY;
   public float normalZ;
   public JBPatternCoord vertA;
   public JBPatternCoord vertB;
   public JBPatternCoord vertC;

   public void calculateNormal() {
      double v1X = this.vertC.x - this.vertB.x;
      double v1Y = this.vertC.y - this.vertB.y;
      double v1Z = this.vertC.z - this.vertB.z;
      double v2X = this.vertA.x - this.vertB.x;
      double v2Y = this.vertA.y - this.vertB.y;
      double v2Z = this.vertA.z - this.vertB.z;
      double xt = v1Y * v2Z - v1Z * v2Y;
      double yt = v1Z * v2X - v1X * v2Z;
      double zt = v1X * v2Y - v1Y * v2X;
      double mgn = (double)Mth.sqrt((float)(xt * xt + yt * yt + zt * zt));
      if (mgn == 0.0) {
         this.enabled = false;
      } else {
         this.enabled = true;
         this.normalX = (float)(xt / mgn);
         this.normalY = (float)(yt / mgn);
         this.normalZ = (float)(zt / mgn);
      }

   }

   public JBPatternTriangle cloneTurn3to4This() {
      JBPatternTriangle tris = new JBPatternTriangle();
      tris.vertA = this.vertA.cloneThis();
      tris.vertB = this.vertB.cloneThis();
      tris.vertC = this.vertC.cloneThis();
      tris.turn3to4();
      return tris;
   }

   public void turn3to4() {
      this.vertA.turn3to4();
      this.vertB.turn3to4();
      this.vertC.turn3to4();
      this.calculateNormal();
   }

   public void turn0to2() {
      this.vertA.turn0to2();
      this.vertB.turn0to2();
      this.vertC.turn0to2();
      this.calculateNormal();
   }

   public void turn0to3() {
      this.vertA.turn0to3();
      this.vertB.turn0to3();
      this.vertC.turn0to3();
      this.calculateNormal();
   }

   public void turn0to4() {
      this.vertA.turn0to4();
      this.vertB.turn0to4();
      this.vertC.turn0to4();
      this.calculateNormal();
   }

   public void turn0to5() {
      this.vertA.turn0to5();
      this.vertB.turn0to5();
      this.vertC.turn0to5();
      this.calculateNormal();
   }
}
