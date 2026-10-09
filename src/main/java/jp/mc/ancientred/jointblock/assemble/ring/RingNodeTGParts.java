package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.core.Direction;

public class RingNodeTGParts extends AbstractRealTimeStateNode {
   protected static final int ENTRY_PARTSNUM = 0;
   protected static final int ENTRY_DETECT_TYPE = 1;
   private static final int DETECT_TYPE_JTYPE = 10;
   private static final int DETECT_TYPE_DEPTH = 12;
   private static final int DETECT_TYPE_VALUE_X = 30;
   private static final int DETECT_TYPE_VALUE_Y = 32;
   private static final int DETECT_TYPE_VALUE_Z = 34;
   private static final int DETECT_TYPE_VALUE_E = 36;
   private static final int DETECT_TYPE_VALUE_VEC = 38;
   private static final int DETECT_TYPE_INITV_A = 50;
   private static final int DETECT_TYPE_INITV_B = 52;
   private static final int DETECT_TYPE_INITV_C = 54;
   private static final int DETECT_TYPE_INITV_D = 56;
   private static final int DETECT_TYPE_INITV_VEC = 58;
   private static final int DETECT_TYPE_PARENT = 70;
   private static final int DETECT_TYPE_CHILD_D = 80;
   private static final int DETECT_TYPE_CHILD_U = 81;
   private static final int DETECT_TYPE_CHILD_B = 82;
   private static final int DETECT_TYPE_CHILD_F = 83;
   private static final int DETECT_TYPE_CHILD_R = 84;
   private static final int DETECT_TYPE_CHILD_L = 85;
   private static final int DETECT_TYPE_CHILD_A = 86;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 12, 30, 32, 34, 36, 38, 50, 52, 54, 56, 58, 70, 80, 81, 82, 83, 84, 85, 86};
   public ReffableShortValueEntry partsNum = new ReffableShortValueEntry(0, false, 1);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGParts();
   }

   public byte getNodeTypeID() {
      return 94;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.partsNum.value;
         case 1:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.partsNum.value = (short)((int)value);
            break;
         case 1:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.partsNum.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.partsNum.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.partsNum.fetchActualValue(program, page)) {
         this.returnValue = Double.NaN;
      } else {
         int partsNum = this.partsNum.getActualValueShort();
         if (ej.refLists != null && partsNum >= 0 && partsNum < ej.refLists.length) {
            JointParts parts = ej.refLists[partsNum];
            if (parts == null) {
               this.returnValue = Double.NaN;
            } else {
               switch (this.detectType) {
                  case 10:
                     this.returnValue = (double)parts.jointType;
                     return;
                  case 11:
                  case 13:
                  case 14:
                  case 15:
                  case 16:
                  case 17:
                  case 18:
                  case 19:
                  case 20:
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
                  case 51:
                  case 53:
                  case 55:
                  case 57:
                  case 59:
                  case 60:
                  case 61:
                  case 62:
                  case 63:
                  case 64:
                  case 65:
                  case 66:
                  case 67:
                  case 68:
                  case 69:
                  case 71:
                  case 72:
                  case 73:
                  case 74:
                  case 75:
                  case 76:
                  case 77:
                  case 78:
                  case 79:
                  default:
                     return;
                  case 12:
                     this.returnValue = (double)parts.depth;
                     return;
                  case 30:
                     this.returnValue = (double)parts.rotateAngleX;
                     return;
                  case 32:
                     this.returnValue = (double)parts.rotateAngleY;
                     return;
                  case 34:
                     this.returnValue = (double)parts.rotateAngleZ;
                     return;
                  case 36:
                     this.returnValue = (double)parts.extendRate;
                     return;
                  case 38:
                     this.arrayValues = ensureArrayValues(this.arrayValues);
                     this.arrayValues[0] = (double)parts.rotateAngleX;
                     this.arrayValues[1] = (double)parts.rotateAngleY;
                     this.arrayValues[2] = (double)parts.rotateAngleZ;
                     this.arrayValues[3] = (double)parts.extendRate;
                     this.returnValue = this.arrayValues[0];
                     return;
                  case 50:
                     this.returnValue = (double)parts.getInitValue(0);
                     return;
                  case 52:
                     this.returnValue = (double)parts.getInitValue(1);
                     return;
                  case 54:
                     this.returnValue = (double)parts.getInitValue(2);
                     return;
                  case 56:
                     this.returnValue = (double)parts.getInitValue(3);
                     return;
                  case 58:
                     this.arrayValues = ensureArrayValues(this.arrayValues);
                     this.arrayValues[0] = (double)parts.getInitValue(0);
                     this.arrayValues[1] = (double)parts.getInitValue(1);
                     this.arrayValues[2] = (double)parts.getInitValue(2);
                     this.arrayValues[3] = (double)parts.getInitValue(3);
                     this.returnValue = this.arrayValues[0];
                     return;
                  case 70:
                     if (parts.parent != null && parts.parent != ej.jointParts) {
                        this.returnValue = (double)parts.parent.id;
                        return;
                     }

                     this.returnValue = Double.NaN;
                     return;
                  case 80:
                     this.setRetValFromChild(parts, Direction.DOWN.get3DDataValue());
                     return;
                  case 81:
                     this.setRetValFromChild(parts, Direction.UP.get3DDataValue());
                     return;
                  case 82:
                     this.setRetValFromChild(parts, Direction.NORTH.get3DDataValue());
                     return;
                  case 83:
                     this.setRetValFromChild(parts, Direction.SOUTH.get3DDataValue());
                     return;
                  case 84:
                     this.setRetValFromChild(parts, Direction.WEST.get3DDataValue());
                     return;
                  case 85:
                     this.setRetValFromChild(parts, Direction.EAST.get3DDataValue());
                     return;
                  case 86:
                     if (parts.child != null) {
                        for(int i = 0; i < parts.child.length; ++i) {
                           if (parts.child[i] != null) {
                              this.returnValue = (double)parts.child[i].id;
                              return;
                           }
                        }
                     }

                     this.returnValue = Double.NaN;
               }
            }
         } else {
            this.returnValue = Double.NaN;
         }
      }
   }

   private void setRetValFromChild(JointParts parts, int index) {
      if (parts.child != null && index >= 0 && index < parts.child.length && parts.child[index] != null) {
         this.returnValue = (double)parts.child[index].id;
      } else {
         this.returnValue = Double.NaN;
      }
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return this.detectType == 38 || this.detectType == 58;
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.partsNum;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.partsid.title" : null;
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 10 : super.getRefCharIndex(refNum);
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.partsNum.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.partsid.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.partsid.title", (Format)EntryFormats.simpleDecimalFormat, 0, 32767);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.partsDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 176.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgparts.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgparts.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
