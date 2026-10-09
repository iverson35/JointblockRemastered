package jp.mc.ancientred.jointblock.motion;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.common.CommonNeckRotatorHandler;
import jp.mc.ancientred.jointblock.motion.common.CommonSwingRotationHandler;

public class HumanoidUpperMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int BIND_TYPE_NECK = 10;
   public static final int BIND_TYPE_SHOULDER = 20;
   public static final int BIND_TYPE_ELBOW = 30;
   private static final CommonNeckRotatorHandler neckMotionHandler = new CommonNeckRotatorHandler() {
      public int getBindNum() {
         return 10;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanupper.neck.name";
      }
   };
   private static final CommonSwingRotationHandler shoulderMotionHandler = new CommonSwingRotationHandler(0) {
      public int getBindNum() {
         return 20;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanupper.shoulder.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
      }
   };
   private static final CommonSwingRotationHandler elbowMotionHandler = new CommonSwingRotationHandler(2) {
      public int getBindNum() {
         return 30;
      }

      public String getMotionHandlerName() {
         return "mhndl.humanupper.elbow.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 0;
         params[3] = 0;
      }
   };

   public String getBinderName() {
      return "mbinder.humanoidupper.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 10:
            return neckMotionHandler;
         case 20:
            return shoulderMotionHandler;
         case 30:
            return elbowMotionHandler;
         default:
            return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return jointType == 8 ? new int[]{10, 20, 30} : null;
   }
}
