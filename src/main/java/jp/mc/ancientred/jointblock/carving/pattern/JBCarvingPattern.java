package jp.mc.ancientred.jointblock.carving.pattern;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.carving.CarvingRayTraceResult;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class JBCarvingPattern {
   protected static final int BBCOLOR_BASE = -16776961;
   protected static final int BBCOLOR_OPTN = -11184811;
   protected static final int BBCOLOR_SUBP = -65536;
   protected static final int BBCOLOR_FACE = -16776961;
   protected static final int BBCOLOR_SIDE = -65536;

   protected static final int convertMutual(int value, int a, int b) {
      if (value == a) {
         return b;
      } else {
         return value == b ? a : value;
      }
   }

   protected static final int convertMutual(int value, int a, int b, int c, int d) {
      if (value == a) {
         return b;
      } else if (value == b) {
         return a;
      } else if (value == c) {
         return d;
      } else {
         return value == d ? c : value;
      }
   }

   public abstract int getPatternSideFace(int var1);

   public abstract int getPatternSide(int var1);

   public abstract int getPatternFace(int var1);

   public abstract int getPatternFaceRaw(int var1);

   public abstract int getPatternArgs(int var1);

   public abstract int clearPatternArgs(int var1);

   public abstract int clearPatternSideFace(int var1);

   public int clearExceptId(int valuePattern) {
      return valuePattern & 114688;
   }

   public int turnPatternSide(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   public int turnPatternFace(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   protected abstract int setNewArgs(int var1, int var2);

   protected abstract int setNewSideFace(int var1, int var2, int var3);

   protected abstract int setNewArgsSideFace(int var1, int var2, int var3, int var4);

   public int getDefaultPatternValue(int faceTo) {
      return 0;
   }

   public int modArg0(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   public int modArg1(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   public int modArg2(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   public int modArg3(int valuePattern, int toggleDir) {
      return valuePattern;
   }

   public int copyValue(int valuePattern, PatternValueCopyType copyType) {
      switch (copyType) {
         case flipX:
            return this.flipCopyValueX(valuePattern);
         case flipY:
            return this.flipCopyValueY(valuePattern);
         case flipZ:
            return this.flipCopyValueZ(valuePattern);
         case flipYofXYZReversed:
            return this.flipCopyValueYofXYZReversed(valuePattern);
         case rot3to4:
            return this.rotCopyValue3to4(valuePattern);
         case rot0to2:
            return this.rotCopyValue0to2(valuePattern);
         case rot0to3:
            return this.rotCopyValue0to3(valuePattern);
         case rot0to4:
            return this.rotCopyValue0to4(valuePattern);
         case rot0to5:
            return this.rotCopyValue0to5(valuePattern);
         default:
            return valuePattern;
      }
   }

   public int flipCopyValueX(int valuePattern) {
      return valuePattern;
   }

   public int flipCopyValueY(int valuePattern) {
      return valuePattern;
   }

   public int flipCopyValueZ(int valuePattern) {
      return valuePattern;
   }

   public int flipCopyValueYofXYZReversed(int valuePattern) {
      return valuePattern;
   }

   public int rotCopyValue3to4(int valuePattern) {
      return valuePattern;
   }

   public int rotCopyValue0to2(int valuePattern) {
      return valuePattern;
   }

   public int rotCopyValue0to3(int valuePattern) {
      return valuePattern;
   }

   public int rotCopyValue0to4(int valuePattern) {
      return valuePattern;
   }

   public int rotCopyValue0to5(int valuePattern) {
      return valuePattern;
   }

   public abstract String getPatternShortName();

   @OnlyIn(Dist.CLIENT)
   public abstract void prebuildPattern(Prebuilder var1, double var2, double var4, double var6, double var8, int var10, int var11);

   @OnlyIn(Dist.CLIENT)
   public abstract void rayTraceSubBounds(CarvingRayTraceResult var1, Vec3 var2, Vec3 var3, int var4, double var5, int var7, AABB var8);

   @OnlyIn(Dist.CLIENT)
   public abstract void setUpRender(FixedDoubleArrayPool var1);

   @OnlyIn(Dist.CLIENT)
   protected void addTris(ArrayList<JBPatternTriangle> coordBaseS0F3, double[] vertA, double[] vertB, double[] vertC) {
      JBPatternTriangle tris = new JBPatternTriangle();
      tris.vertA = new JBPatternCoord(vertA);
      tris.vertB = new JBPatternCoord(vertB);
      tris.vertC = new JBPatternCoord(vertC);
      tris.calculateNormal();
      if (tris.enabled) {
         coordBaseS0F3.add(tris);
      }

   }

   @OnlyIn(Dist.CLIENT)
   protected void addQuads(ArrayList<JBPatternTriangle> coordBaseS0F3, double[] vertA, double[] vertB, double[] vertC, double[] vertD) {
      JBPatternTriangle tris = new JBPatternTriangle();
      tris.vertA = new JBPatternCoord(vertA);
      tris.vertB = new JBPatternCoord(vertB);
      tris.vertC = new JBPatternCoord(vertC);
      tris.calculateNormal();
      if (tris.enabled) {
         coordBaseS0F3.add(tris);
      }

      tris = new JBPatternTriangle();
      tris.vertA = new JBPatternCoord(vertC);
      tris.vertB = new JBPatternCoord(vertD);
      tris.vertC = new JBPatternCoord(vertA);
      tris.calculateNormal();
      if (tris.enabled) {
         coordBaseS0F3.add(tris);
      }

   }
}
