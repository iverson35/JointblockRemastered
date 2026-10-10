package jp.mc.ancientred.jointblock.motion;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.common.CommonSwingRotationHandler;
import jp.mc.ancientred.jointblock.motion.common.CommonWaistRotatorHandler;

public class HumanoidLowerMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int BIND_TYPE_WAIST = 40;
   public static final int BIND_TYPE_COXA = 50;
   public static final int BIND_TYPE_KNEE = 60;
   public static final int BIND_TYPE_ANKLE = 70;
   private static final CommonWaistRotatorHandler waistMotionHandler = new CommonWaistRotatorHandler() {
      public int getBindNum() {
         return 40;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanlower.waist.name";
      }
   };
   private static final CommonSwingRotationHandler coxaMotionHandler = new CommonSwingRotationHandler(4) {
      public int getBindNum() {
         return 50;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanlower.coxa.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 1;
         params[3] = -60;
      }
   };
   private static final CommonSwingRotationHandler kneeMotionHandler = new CommonSwingRotationHandler(6) {
      public int getBindNum() {
         return 60;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanlower.knee.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 1;
         params[3] = 120;
      }
   };
   private static final CommonSwingRotationHandler ankleMotionHandler = new CommonSwingRotationHandler(8) {
      public int getBindNum() {
         return 70;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanlower.ankle.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 2;
         params[3] = 70;
      }
   };

   public String getBinderName() {
      return "mbinder.humanoidlower.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 40:
            return waistMotionHandler;
         case 50:
            return coxaMotionHandler;
         case 60:
            return kneeMotionHandler;
         case 70:
            return ankleMotionHandler;
         default:
            return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return jointType == 8 ? new int[]{40, 50, 60, 70} : null;
   }
}
