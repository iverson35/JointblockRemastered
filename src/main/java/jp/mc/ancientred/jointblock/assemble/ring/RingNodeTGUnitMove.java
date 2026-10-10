package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.util.Mth;

public class RingNodeTGUnitMove extends AbstractRealTimeStateNode {
   protected static final int ENTRY_DETECT_TYPE = 0;
   private static final int DETECT_TYPE_YAWABS = 5;
   private static final int DETECT_TYPE_MOVING_A = 10;
   private static final int DETECT_TYPE_MOVING_B = 15;
   private static final int DETECT_TYPE_MOVINGUP = 20;
   private static final int DETECT_TYPE_MOVINGDW = 25;
   private static final int DETECT_TYPE_INAIR = 30;
   private static final int DETECT_TYPE_JUMPING = 35;
   private static final int DETECT_TYPE_FLYING = 40;
   private static final int DETECT_TYPE_FLYHOVER = 45;
   private static final int DETECT_TYPE_DASHING = 50;
   private static final int DETECT_TYPE_SPINRATE = 55;
   private static final int DETECT_TYPE_LMBAMOUNT = 60;
   private static final int DETECT_TYPE_GROUNDFAR = 65;
   private static final int DETECT_TYPE_ISZEROG = 70;
   private static final int[] DETECT_TYPE_CONV = new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
   private static final int GROUND_FAR_MAX = 10;
   public byte detectType = 5;

   public AbstractRingNode newInstance() {
      return new RingNodeTGUnitMove();
   }

   public byte getNodeTypeID() {
      return 78;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
         default:
      }
   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      switch (this.detectType) {
         case 5:
            this.returnValue = (double)RingUtils.range180YawValue(ej.getYRot());
            return;
         case 15:
            if (ej.savedPrevRotationYaw != ej.yRotO) {
               this.returnValue = 1.0;
               return;
            }
         case 10:
            double d0 = ej.getX() - ej.xo;
            double d1 = ej.getZ() - ej.zo;
            double d2 = ej.getY() - ej.yo;
            this.returnValue = (double)booleanValue((double)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) > 0.1);
            return;
         case 20:
            this.returnValue = (double)booleanValue(ej.getY() - ej.yo > 0.0);
            return;
         case 25:
            this.returnValue = (double)booleanValue(ej.getY() - ej.yo < 0.0);
            return;
         case 30:
            this.returnValue = (double)booleanValue(!ej.onGround());
            return;
         case 35:
            this.returnValue = (double)booleanValue(ej.isEntityJumping());
            return;
         case 40:
            this.returnValue = (double)booleanValue(ej.isEntityFlying());
            return;
         case 45:
            this.returnValue = (double)booleanValue(ej.isAirBorneHovering());
            return;
         case 50:
            this.returnValue = (double)booleanValue(ej.isEntityDashing());
            return;
         case 55:
            this.returnValue = (double)((float)ej.getSpinRate());
            return;
         case 60:
            float limbSwingAmountF = ej.walkAnimation.speed();
            if (limbSwingAmountF > 1.0F) {
               limbSwingAmountF = 1.0F;
            }

            this.returnValue = (double)ej.walkAnimation.speed();
            return;
         case 65:
            if (ej.level() == null) {
               return;
            } else {
               int nowX = Mth.floor(ej.getX());
               int nowY = Mth.floor(ej.getY());
               int nowZ = Mth.floor(ej.getZ());

               for (int i = 0; i < 11; ++i) {
                  if (!WorldHelper.isAirBlock(ej.level(), nowX, nowY - i, nowZ)) {
                     this.returnValue = (double)((float)ej.getY() - (float)(nowY - i + 1));
                     return;
                  }
               }

               this.returnValue = 10.0;
               return;
            }
         case 70:
            // StarMiner zero-gravity interop was removed in the 1.20.1 port (StarMiner is not shipped; the
            // 1.12.2 check was always false at runtime). Keep the branch shape with a constant false.
            this.returnValue = (double)booleanValue(false);
            return;
         default:
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.unitMoveDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconV() {
      return 144.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgunitmove.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgunitmove.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
