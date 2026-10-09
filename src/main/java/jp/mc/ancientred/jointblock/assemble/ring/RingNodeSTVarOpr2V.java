package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTVarOpr2V extends AbstractRingNode implements INodeValueSettable {
   protected static final int ENTRY_OPRTYPE = 0;
   protected static final int ENTRY_REF_INPUTA = 1;
   protected static final int ENTRY_REF_INPUTB = 2;
   protected static final int ENTRY_REF_OUTPUT = 5;
   private static final byte OPR_TYPE_ADD = 0;
   private static final byte OPR_TYPE_SUB = 2;
   private static final byte OPR_TYPE_MUL = 4;
   private static final byte OPR_TYPE_DIV = 6;
   private static final byte OPR_TYPE_MOD = 8;
   private static final byte OPR_TYPE_EQUALS = 10;
   private static final byte OPR_TYPE_EQUALSINT = 12;
   private static final byte OPR_TYPE_LESSER = 14;
   private static final byte OPR_TYPE_GREATER = 16;
   private static final byte OPR_TYPE_LESSEQ = 18;
   private static final byte OPR_TYPE_GREATEQ = 20;
   private static final byte OPR_TYPE_AND = 30;
   private static final byte OPR_TYPE_OR = 32;
   private static final byte OPR_TYPE_XOR = 34;
   private static final byte OPR_TYPE_POW = 36;
   private static final byte OPR_TYPE_MIN = 40;
   private static final byte OPR_TYPE_MAX = 42;
   private static final byte OPR_TYPE_ATAN2 = 44;
   private static final byte OPR_TYPE_VEC3DOT = 50;
   private static final byte OPR_TYPE_VEC3DIST = 52;
   private static final byte OPR_TYPE_VEC3ADD = 54;
   private static final byte OPR_TYPE_VEC3SUB = 56;
   private static final byte OPR_TYPE_VEC3CROSS = 58;
   private static final int[] OPR_TYPE_CONV = new int[]{0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 30, 32, 34, 36, 40, 42, 44, 50, 52, 54, 56, 58};
   private byte oprType = 0;
   public ReffableDoubleValueEntry inputARef = new ReffableDoubleValueEntry(-1.0, true);
   public ReffableDoubleValueEntry inputBRef = new ReffableDoubleValueEntry(-1.0, true);
   public byte outRef = -1;
   private double returnValue = 0.0;
   public double[] arrayValues;

   public RingNodeSTVarOpr2V() {
      this.oprType = 0;
   }

   public RingNodeSTVarOpr2V(byte argOprType) {
      this.oprType = argOprType;
   }

   public AbstractRingNode newInstance() {
      return new RingNodeSTVarOpr2V(this.oprType);
   }

   public byte getNodeTypeID() {
      return 20;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(OPR_TYPE_CONV, (short)this.oprType);
         case 1:
            return this.inputARef.value;
         case 2:
            return this.inputBRef.value;
         case 3:
         case 4:
         default:
            return 0.0;
         case 5:
            return (double)this.outRef;
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
         case 2:
            this.inputBRef.value = value;
         case 3:
         case 4:
         default:
            break;
         case 5:
            this.outRef = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.oprType);
      this.inputARef.write(out);
      this.inputBRef.write(out);
      out.writeByte(this.outRef);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.returnValue);
      this.writeArrayData(out, this.arrayValues);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.oprType = inn.readByte(this.oprType);
      this.inputARef.read(inn);
      this.inputBRef.read(inn);
      this.outRef = inn.readByte(this.outRef);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readDouble(this.returnValue);
      this.arrayValues = this.readArrayData(inn);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      double newValue;
      if (this.inputARef.fetchActualValue(program, page) && this.inputBRef.fetchActualValue(program, page)) {
         double dBZ;
         double dAX;
         double dAY;
         double dAZ;
         double dBX;
         double dBY;
         switch (this.oprType) {
            case 0:
            case 1:
            case 3:
            case 5:
            case 7:
            case 9:
            case 11:
            case 13:
            case 15:
            case 17:
            case 19:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 31:
            case 33:
            case 35:
            case 37:
            case 38:
            case 39:
            case 41:
            case 43:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 51:
            case 53:
            case 55:
            case 57:
            default:
               newValue = this.inputARef.actualValue + this.inputBRef.actualValue;
               break;
            case 2:
               newValue = this.inputARef.actualValue - this.inputBRef.actualValue;
               break;
            case 4:
               newValue = this.inputARef.actualValue * this.inputBRef.actualValue;
               break;
            case 6:
               newValue = this.inputARef.actualValue / this.inputBRef.actualValue;
               break;
            case 8:
               newValue = this.inputARef.actualValue % this.inputBRef.actualValue;
               break;
            case 10:
               if (this.inputARef.actualValue == this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 12:
               if ((int)this.inputARef.actualValue == (int)this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 14:
               if (this.inputARef.actualValue < this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 16:
               if (this.inputARef.actualValue > this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 18:
               if (this.inputARef.actualValue <= this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 20:
               if (this.inputARef.actualValue >= this.inputBRef.actualValue) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 30:
               if (this.inputARef.getActualValueBool() && this.inputBRef.getActualValueBool()) {
                  newValue = 1.0;
                  break;
               }

               newValue = 0.0;
               break;
            case 32:
               if (!this.inputARef.getActualValueBool() && !this.inputBRef.getActualValueBool()) {
                  newValue = 0.0;
                  break;
               }

               newValue = 1.0;
               break;
            case 34:
               if (this.inputARef.getActualValueBool() ^ this.inputBRef.getActualValueBool()) {
                  newValue = 1.0;
               } else {
                  newValue = 0.0;
               }
               break;
            case 36:
               newValue = Math.pow(this.inputARef.actualValue, this.inputBRef.actualValue);
               break;
            case 40:
               newValue = Math.min(this.inputARef.actualValue, this.inputBRef.actualValue);
               break;
            case 42:
               newValue = Math.max(this.inputARef.actualValue, this.inputBRef.actualValue);
               break;
            case 44:
               newValue = Math.atan2(this.inputARef.actualValue, this.inputBRef.actualValue);
               break;
            case 50:
               dAX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dAY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dAZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               dBX = this.fetchEachVectorValue(program, page, this.inputBRef, 0);
               dBY = this.fetchEachVectorValue(program, page, this.inputBRef, 1);
               dBZ = this.fetchEachVectorValue(program, page, this.inputBRef, 2);
               newValue = dAX * dBX + dAY * dBY + dAZ * dBZ;
               break;
            case 52:
               dAX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dAY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dAZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               dBX = this.fetchEachVectorValue(program, page, this.inputBRef, 0);
               dBY = this.fetchEachVectorValue(program, page, this.inputBRef, 1);
               dBZ = this.fetchEachVectorValue(program, page, this.inputBRef, 2);
               dAX -= dBX;
               dAY -= dBY;
               dAZ -= dBZ;
               newValue = Math.sqrt(dAX * dAX + dAY * dAY + dAZ * dAZ);
               break;
            case 54:
               dAX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dAY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dAZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               dBX = this.fetchEachVectorValue(program, page, this.inputBRef, 0);
               dBY = this.fetchEachVectorValue(program, page, this.inputBRef, 1);
               dBZ = this.fetchEachVectorValue(program, page, this.inputBRef, 2);
               this.arrayValues = ensureArrayValues(this.arrayValues);
               this.arrayValues[0] = dAX + dBX;
               this.arrayValues[1] = dAY + dBY;
               this.arrayValues[2] = dAZ + dBZ;
               this.arrayValues[3] = 0.0;
               newValue = this.arrayValues[0];
               break;
            case 56:
               dAX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dAY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dAZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               dBX = this.fetchEachVectorValue(program, page, this.inputBRef, 0);
               dBY = this.fetchEachVectorValue(program, page, this.inputBRef, 1);
               dBZ = this.fetchEachVectorValue(program, page, this.inputBRef, 2);
               this.arrayValues = ensureArrayValues(this.arrayValues);
               this.arrayValues[0] = dAX - dBX;
               this.arrayValues[1] = dAY - dBY;
               this.arrayValues[2] = dAZ - dBZ;
               this.arrayValues[3] = 0.0;
               newValue = this.arrayValues[0];
               break;
            case 58:
               dAX = this.fetchEachVectorValue(program, page, this.inputARef, 0);
               dAY = this.fetchEachVectorValue(program, page, this.inputARef, 1);
               dAZ = this.fetchEachVectorValue(program, page, this.inputARef, 2);
               dBX = this.fetchEachVectorValue(program, page, this.inputBRef, 0);
               dBY = this.fetchEachVectorValue(program, page, this.inputBRef, 1);
               dBZ = this.fetchEachVectorValue(program, page, this.inputBRef, 2);
               this.arrayValues = ensureArrayValues(this.arrayValues);
               this.arrayValues[0] = dAY * dBZ - dAZ * dBY;
               this.arrayValues[1] = dAZ * dBX - dAX * dBZ;
               this.arrayValues[2] = dAX * dBY - dAY * dBX;
               this.arrayValues[3] = 0.0;
               newValue = this.arrayValues[0];
         }

         if (Double.isInfinite(newValue) || Double.isNaN(newValue)) {
            newValue = Double.NaN;
         }
      } else {
         newValue = Double.NaN;
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
      return this.oprType == 54 || this.oprType == 56 || this.oprType == 58;
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
      return 3;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.inputARef;
         case 2:
            return this.inputBRef;
         default:
            return null;
      }
   }

   public boolean isRefLineTowardsThis(int refNum) {
      return refNum != 2;
   }

   public boolean canBeLinedWith(int refNum, AbstractRingNode node) {
      return refNum == 2 ? node instanceof INodeValueSettable : true;
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 1;
         case 1:
            return 2;
         case 2:
            return 5;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return "jbring.cmn.innode.a.title";
         case 1:
            return "jbring.cmn.innode.b.title";
         case 2:
            return "jbring.cmn.outnode.title";
      }
   }

   public int getRefLineColor(int refNum) {
      switch (refNum) {
         case 0:
         case 1:
         default:
            return 16734296;
         case 2:
            return 7895295;
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 6;
         case 1:
            return 7;
         case 2:
            return 9;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.varopr2v.oprtype.title", (Format)EntryFormats.varV2OprTypeValueFormat, 0, lastIdxOf(OPR_TYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      if (this.inputARef.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.a.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.fixed.a.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(1, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(1);
      if (this.inputBRef.isNodeRefMode) {
         gs.setParamEntryState(2, (String)"jbring.cmn.innode.b.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(2, (String)"jbring.cmn.fixed.b.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(2, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(2);
      gs.setParamEntryState(5, (String)"jbring.cmn.outnode.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      gs.setParamButtonToggleAry(5, EntryFormats.defaultButtonStates, 1);
   }

   public float getIconU() {
      switch (this.oprType) {
         case 0:
         case 2:
         case 4:
         case 6:
         case 8:
         case 10:
         case 12:
         case 14:
         case 16:
         case 18:
         case 20:
         case 30:
         case 32:
         case 34:
            return 336.0F;
         case 1:
         case 3:
         case 5:
         case 7:
         case 9:
         case 11:
         case 13:
         case 15:
         case 17:
         case 19:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 31:
         case 33:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         default:
            return 352.0F;
      }
   }

   public float getIconV() {
      switch (this.oprType) {
         case 0:
         case 1:
         case 3:
         case 5:
         case 7:
         case 9:
         case 11:
         case 13:
         case 15:
         case 17:
         case 19:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 31:
         case 33:
         case 35:
         case 37:
         case 38:
         case 39:
         case 41:
         case 43:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 53:
         case 55:
         case 57:
         default:
            return 0.0F;
         case 2:
            return 16.0F;
         case 4:
            return 32.0F;
         case 6:
            return 48.0F;
         case 8:
            return 64.0F;
         case 10:
         case 12:
            return 80.0F;
         case 14:
            return 96.0F;
         case 16:
            return 112.0F;
         case 18:
            return 128.0F;
         case 20:
            return 144.0F;
         case 30:
            return 160.0F;
         case 32:
            return 160.0F;
         case 34:
            return 192.0F;
         case 36:
         case 40:
         case 42:
         case 44:
         case 50:
         case 52:
         case 54:
         case 56:
         case 58:
            return 208.0F;
      }
   }

   public boolean hasOverlayIcon() {
      return true;
   }

   public float getOverlayIconU() {
      return 352.0F;
   }

   public float getOverlayIconV() {
      return 48.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.varopr2v.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.varopr2v.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.var");
   }
}
