package jp.mc.ancientred.jointblock.motion;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.common.InsectsLegRotationHandler;

public class InsectsMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int BIND_TYPE_B_FRONT_TROCHANTER = 50;
   public static final int BIND_TYPE_B_FRONT_FEMUR = 60;
   public static final int BIND_TYPE_B_REAR_TROCHANTER = 80;
   public static final int BIND_TYPE_B_REAR_FEMUR = 90;
   private static final InsectsLegRotationHandler insectsFrontTrochanterMotionHandler = new InsectsLegRotationHandler(4, 0) {
      public int getBindNum() {
         return 50;
      }

      public String getMotionHandlerName() {
         return "mhndl.insects.ftrochanter.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
         params[4] = 110;
      }
   };
   private static final InsectsLegRotationHandler insectsFrontFemurMotionHandler = new InsectsLegRotationHandler(6, 0) {
      public int getBindNum() {
         return 60;
      }

      public String getMotionHandlerName() {
         return "mhndl.insects.ffemur.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
         params[4] = -90;
      }
   };
   private static final InsectsLegRotationHandler insectsRearTrochanterMotionHandler = new InsectsLegRotationHandler(4, 1) {
      public int getBindNum() {
         return 80;
      }

      public String getMotionHandlerName() {
         return "mhndl.insects.rtrochanter.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
         params[4] = 110;
      }
   };
   private static final InsectsLegRotationHandler insectsRearFemurMotionHandler = new InsectsLegRotationHandler(6, 1) {
      public int getBindNum() {
         return 90;
      }

      public String getMotionHandlerName() {
         return "mhndl.insects.rfemur.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
         params[4] = -90;
      }
   };

   public String getBinderName() {
      return "mbinder.insects.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 50:
            return insectsFrontTrochanterMotionHandler;
         case 60:
            return insectsFrontFemurMotionHandler;
         case 80:
            return insectsRearTrochanterMotionHandler;
         case 90:
            return insectsRearFemurMotionHandler;
         default:
            return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return jointType == 8 ? new int[]{50, 60, 80, 90} : null;
   }
}
