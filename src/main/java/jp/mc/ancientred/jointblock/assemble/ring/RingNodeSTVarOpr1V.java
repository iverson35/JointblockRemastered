package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTVarOpr1V extends AbstractRingNode implements INodeValueSettable {
   protected static final int ENTRY_OPRTYPE = 0;
   protected static final int ENTRY_REF_INPUT = 1;
   protected static final int ENTRY_REF_OUTPUT = 5;
   private static final byte OPR_TYPE_SET = 0;
   private static final byte OPR_TYPE_TRUNC = 10;
   private static final byte OPR_TYPE_ROUND = 12;
   private static final byte OPR_TYPE_FLOOR = 14;
   private static final byte OPR_TYPE_CEIL = 16;
   private static final byte OPR_TYPE_NOT = 20;
   private static final byte OPR_TYPE_ABS = 22;
   private static final byte OPR_TYPE_SQRT = 24;
   private static final byte OPR_TYPE_SIN = 26;
   private static final byte OPR_TYPE_COS = 28;
   private static final byte OPR_TYPE_TAN = 30;
   private static final byte OPR_TYPE_ASIN = 32;
   private static final byte OPR_TYPE_ACOS = 34;
   private static final byte OPR_TYPE_ATAN = 36;
   private static final byte OPR_TYPE_TORAD = 38;
   private static final byte OPR_TYPE_TODEG = 40;
   private static final byte OPR_TYPE_EXP = 42;
   private static final byte OPR_TYPE_VECX = 50;
   private static final byte OPR_TYPE_VECY = 52;
   private static final byte OPR_TYPE_VECZ = 54;
   private static final byte OPR_TYPE_VECW = 56;
   private static final byte OPR_TYPE_VEC3LEN = 60;
   private static final byte OPR_TYPE_VEC3NORM = 62;
   private static final byte OPR_TYPE_E = 90;
   private static final byte OPR_TYPE_PI = 92;
   private static final byte OPR_TYPE_ERR = 94;
   private static final byte OPR_TYPE_RAND = 96;
   private static final int[] OPR_TYPE_CONV = new int[]{0, 10, 12, 14, 16, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 50, 52, 54, 56, 60, 62, 90, 92, 94, 96};
   private byte oprType = 0;
   public ReffableDoubleValueEntry inputARef = new ReffableDoubleValueEntry(-1.0, true);
   public byte outRef = -1;
   private double returnValue = 0.0;
   public double[] arrayValues;

   public AbstractRingNode newInstance() {
      return new RingNodeSTVarOpr1V();
   }

   public byte getNodeTypeID() {
      return 18;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(OPR_TYPE_CONV, (short)this.oprType);
         case 1:
            return this.inputARef.value;
         case 5:
            return (double)this.outRef;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.oprType = (byte)convertToTypeValue(OPR_TYPE_CONV, value);
            break;
         case 1:
            this.inputARef.value = value;
            break;
         case 5:
            this.outRef = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.oprType);
      this.inputARef.write(out);
      out.writeByte(this.outRef);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.returnValue);
      this.writeArrayData(out, this.arrayValues);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.oprType = inn.readByte(this.oprType);
      this.inputARef.read(inn);
      this.outRef = inn.readByte(this.outRef);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readDouble(this.returnValue);
      this.arrayValues = this.readArrayData(inn);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      double newValue;
      if (!this.inputARef.fetchActualValue(program, page)) {
         newValue = Double.NaN;
      } else {
         double dX;
         double dY;
         double dZ;
         switch (this.oprType) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            case 13:
            case 15:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
            case 25:
            case 27:
            case 29:
            case 31:
            case 33:
            case 35:
            case 37:
            case 39:
            case 41:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 53:
            case 55:
            case 57:
            case 58:
            case 59:
            case 61:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 91:
            case 93:
            case 95:
            default:
               newValue = this.inputARef.actualValue;
               break;
            case 10:
               newValue = (double)((long)this.inputARef.actualValue);
               break;
            case 12:
               newValue = (double)Math.round(this.inputARef.actualValue);
               break;
            case 14:
               newValue = Math.floor(this.inputARef.actualValue);
               break;
            case 16:
               newValue = Math.ceil(this.inputARef.actualValue);
               break;
            case 20:
               if (!this.inputARef.getActualValueBool()) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 22:
               newValue = Math.abs(this.inputARef.actualValue);
               break;
            case 24:
               newValue = Math.sqrt(this.inputARef.actualValue);
               break;
            case 26:
               newValue = Math.sin(this.inputARef.actualValue);
               break;
            case 28:
               newValue = Math.cos(this.inputARef.actualValue);
               break;
            case 30:
               newValue = Math.tan(this.inputARef.actualValue);
               break;
            case 32:
               newValue = Math.asin(this.inputARef.actualValue);
               break;
            case 34:
               newValue = Math.acos(this.inputARef.actualValue);
               break;
            case 36:
               newValue = Math.atan(this.inputARef.actualValue);
               break;
            case 38:
               newValue = Math.toRadians(this.inputARef.actualValue);
               break;
            case 40:
               newValue = Math.toDegrees(this.inputARef.actualValue);
               break;
            case 42:
               newValue = Math.exp(this.inputARef.actualValue);
               break;
            case 50:
               newValue = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               break;
            case 52:
               newValue = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               break;
            case 54:
               newValue = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               break;
            case 56:
               newValue = this.fetchEachVectorValue(program, page, this.inputARef, 3);
               break;
            case 60:
               dX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               newValue = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
               break;
            case 62:
               dX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               double mgn = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
               if (Math.abs(mgn) < 1.0E-4) {
                  dZ = 0.0;
                  dY = 0.0;
                  dX = 0.0;
               } else {
                  dX /= mgn;
                  dY /= mgn;
                  dZ /= mgn;
               }

               this.arrayValues = ensureArrayValues(this.arrayValues);
               newValue = dX;
               this.arrayValues[0] = dX;
               this.arrayValues[1] = dY;
               this.arrayValues[2] = dZ;
               this.arrayValues[3] = 0.0;
               break;
            case 90:
               newValue = Math.E;
               break;
            case 92:
               newValue = Math.PI;
               break;
            case 94:
               newValue = Double.NaN;
               break;
            case 96:
               newValue = Math.random();
         }

         if (Double.isInfinite(newValue) || Double.isNaN(newValue)) {
            newValue = Double.NaN;
         }
      }

      this.returnValue = newValue;
      AbstractRingNode outNode = page.getNode(this.outRef);
      if (outNode instanceof INodeValueSettable) {
         INodeValueSettable toSetVarNode = (INodeValueSettable)outNode;
         toSetVarNode.setReturnValue(program, page, this.returnValue);
      }

      return program.nextNodeIndex(page);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.returnValue;
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return this.oprType == 62;
   }

   public double getArrayValue(RingProgram program, RingProgramPage page, int index) {
      if (this.hasVectorOutput(program, page) && this.arrayValues != null) {
         return index >= 0 && index < this.arrayValues.length ? this.arrayValues[index] : Double.NaN;
      } else {
         return super.getArrayValue(program, page, index);
      }
   }

   public void setReturnValue(RingProgram program, RingProgramPage page, double newValue) {
      this.returnValue = newValue;
   }

   public int getRefCount() {
      return 2;
   }

   public int getRefEntryNum(int refNum) {
      return refNum == 0 ? 1 : 5;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      return entryNum == 1 ? this.inputARef : null;
   }

   public boolean isRefLineTowardsThis(int refNum) {
      return refNum != 1;
   }

   public boolean canBeLinedWith(int refNum, AbstractRingNode node) {
      return refNum == 1 ? node instanceof INodeValueSettable : true;
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.title" : "jbring.cmn.outnode.title";
   }

   public int getRefLineColor(int refNum) {
      return refNum == 0 ? 16734296 : 7895295;
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 8 : 9;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.varopr2v.oprtype.title", (Format)EntryFormats.varV1OprTypeValueFormat, 0, lastIdxOf(OPR_TYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      if (this.inputARef.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.fixed.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(1, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(1);
      gs.setParamEntryState(5, (String)"jbring.cmn.outnode.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      gs.setParamButtonToggleAry(5, EntryFormats.defaultButtonStates, 1);
   }

   public float getIconU() {
      switch (this.oprType) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 21:
         case 23:
         case 25:
         case 27:
         case 29:
         case 31:
         case 33:
         case 35:
         case 37:
         case 39:
         case 41:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 53:
         case 55:
         case 57:
         case 58:
         case 59:
         case 61:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 91:
         case 93:
         case 95:
         default:
            return 352.0F;
         case 20:
            return 336.0F;
         case 22:
         case 24:
         case 26:
         case 28:
         case 30:
         case 32:
         case 34:
         case 36:
         case 38:
         case 40:
         case 42:
         case 50:
         case 52:
         case 54:
         case 56:
         case 60:
         case 62:
         case 90:
         case 92:
         case 94:
         case 96:
            return 352.0F;
      }
   }

   public float getIconV() {
      switch (this.oprType) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 21:
         case 23:
         case 25:
         case 27:
         case 29:
         case 31:
         case 33:
         case 35:
         case 37:
         case 39:
         case 41:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 53:
         case 55:
         case 57:
         case 58:
         case 59:
         case 61:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 91:
         case 93:
         case 95:
         default:
            return 16.0F;
         case 20:
            return 208.0F;
         case 22:
         case 24:
         case 26:
         case 28:
         case 30:
         case 32:
         case 34:
         case 36:
         case 38:
         case 40:
         case 42:
         case 50:
         case 52:
         case 54:
         case 56:
         case 60:
         case 62:
         case 90:
         case 92:
         case 94:
         case 96:
            return 208.0F;
      }
   }

   public boolean hasOverlayIcon() {
      switch (this.oprType) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 21:
         case 23:
         case 25:
         case 27:
         case 29:
         case 31:
         case 33:
         case 35:
         case 37:
         case 39:
         case 41:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 53:
         case 55:
         case 57:
         case 58:
         case 59:
         case 61:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 91:
         case 93:
         case 95:
         default:
            return false;
         case 20:
         case 22:
         case 24:
         case 26:
         case 28:
         case 30:
         case 32:
         case 34:
         case 36:
         case 38:
         case 40:
         case 42:
         case 50:
         case 52:
         case 54:
         case 56:
         case 60:
         case 62:
         case 90:
         case 92:
         case 94:
         case 96:
            return true;
      }
   }

   public float getOverlayIconU() {
      return 352.0F;
   }

   public float getOverlayIconV() {
      return 32.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.varopr1v.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.varopr1v.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.var");
   }
}
