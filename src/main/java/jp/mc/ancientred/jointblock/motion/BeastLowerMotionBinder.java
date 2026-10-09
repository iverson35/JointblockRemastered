package jp.mc.ancientred.jointblock.motion;

import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.motion.common.CommonSwingRotationHandler;

public class BeastLowerMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int BIND_TYPE_B_FRONT_COXA = 50;
   public static final int BIND_TYPE_B_FRONT_KNEE = 60;
   public static final int BIND_TYPE_B_FRONT_ANKLE = 70;
   public static final int BIND_TYPE_B_REAR_COXA = 80;
   public static final int BIND_TYPE_B_REAR_KNEE = 90;
   public static final int BIND_TYPE_B_REAR_ANKLE = 100;
   private static final CommonSwingRotationHandler beasFrontCoxaMotionHandler = new CommonSwingRotationHandler(10, 0) {
      public int getBindNum() {
         return 50;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.fcoxa.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = -60;
      }
   };
   private static final CommonSwingRotationHandler beastFrontKneeMotionHandler = new CommonSwingRotationHandler(12, 0) {
      public int getBindNum() {
         return 60;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.fknee.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = 120;
      }
   };
   private static final CommonSwingRotationHandler beastFrontAnkleMotionHandler = new CommonSwingRotationHandler(14, 0) {
      public int getBindNum() {
         return 70;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.fankle.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = 70;
      }
   };
   private static final CommonSwingRotationHandler beasRearCoxaMotionHandler = new CommonSwingRotationHandler(10, 1) {
      public int getBindNum() {
         return 80;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.rcoxa.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = -60;
      }
   };
   private static final CommonSwingRotationHandler beastRearKneeMotionHandler = new CommonSwingRotationHandler(12, 1) {
      public int getBindNum() {
         return 90;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.rknee.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = 120;
      }
   };
   private static final CommonSwingRotationHandler beastRearAnkleMotionHandler = new CommonSwingRotationHandler(14, 1) {
      public int getBindNum() {
         return 100;
      }

      public String getMotionHandlerName() {
         return "mhndl.beastlower.rankle.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 100;
         params[2] = 3;
         params[3] = 70;
      }
   };

   public String getBinderName() {
      return "mbinder.beastlower.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 50:
            return beasFrontCoxaMotionHandler;
         case 60:
            return beastFrontKneeMotionHandler;
         case 70:
            return beastFrontAnkleMotionHandler;
         case 80:
            return beasRearCoxaMotionHandler;
         case 90:
            return beastRearKneeMotionHandler;
         case 100:
            return beastRearAnkleMotionHandler;
         default:
            return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return jointType == 8 ? new int[]{50, 60, 70, 80, 90, 100} : null;
   }
}
