package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EjSensor;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeTGSensor extends AbstractRealTimeStateNode {
   protected static final int ENTRY_SENSORNUM = 0;
   protected static final int ENTRY_DETECT_TYPE = 1;
   private static final int DETECT_TYPE_SPOSX = 10;
   private static final int DETECT_TYPE_SPOSY = 12;
   private static final int DETECT_TYPE_SPOSZ = 14;
   private static final int DETECT_TYPE_SPOSVEC = 16;
   private static final int DETECT_TYPE_SWAYVEC = 18;
   private static final int DETECT_TYPE_SRES0 = 20;
   private static final int DETECT_TYPE_SRES1 = 22;
   private static final int DETECT_TYPE_SRES2 = 24;
   private static final int DETECT_TYPE_SRES3 = 26;
   private static final int DETECT_TYPE_SRESVEC = 28;
   private static final int DETECT_TYPE_PARTSID = 50;
   private static final int DETECT_TYPE_ENABLED = 90;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 50, 90};
   public ReffableByteValueEntry sensorNum = new ReffableByteValueEntry(0, false, 1);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGSensor();
   }

   public byte getNodeTypeID() {
      return 86;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.sensorNum.value;
         case 1:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.sensorNum.value = (byte)((int)value);
            break;
         case 1:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.sensorNum.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.sensorNum.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.sensorNum.fetchActualValue(program, page)) {
         this.returnValue = Double.NaN;
      } else {
         int sensorNum = this.sensorNum.getActualValueShort();
         if (ej.sensorList != null && sensorNum >= 0 && sensorNum <= ej.sensorList.length) {
            EjSensor sensor = ej.sensorList[sensorNum];
            if (sensor != null && sensor.joint != null) {
               switch (this.detectType) {
                  case 10:
                     sensor.setPosToCmnVec();
                     this.returnValue = sensor.cmnVec.x;
                     return;
                  case 12:
                     sensor.setPosToCmnVec();
                     this.returnValue = sensor.cmnVec.y;
                     return;
                  case 14:
                     sensor.setPosToCmnVec();
                     this.returnValue = sensor.cmnVec.z;
                     return;
                  case 16:
                     sensor.setPosToCmnVec();
                     this.arrayValues = ensureArrayValues(this.arrayValues);
                     this.arrayValues[0] = sensor.cmnVec.x;
                     this.arrayValues[1] = sensor.cmnVec.y;
                     this.arrayValues[2] = sensor.cmnVec.z;
                     this.arrayValues[3] = 0.0;
                     this.returnValue = this.arrayValues[0];
                     return;
                  case 18:
                     sensor.setWayToCmnVec();
                     sensor.cmnVec = JBCommonUtil.normalizeVec3(sensor.cmnVec);
                     this.arrayValues = ensureArrayValues(this.arrayValues);
                     this.arrayValues[0] = sensor.cmnVec.x;
                     this.arrayValues[1] = sensor.cmnVec.y;
                     this.arrayValues[2] = sensor.cmnVec.z;
                     this.arrayValues[3] = 0.0;
                     this.returnValue = this.arrayValues[0];
                     return;
                  case 20:
                     this.returnValue = sensor.hasResultOfIndex(0) ? (double)sensor.searchResult[0] : Double.NaN;
                     return;
                  case 22:
                     this.returnValue = sensor.hasResultOfIndex(1) ? (double)sensor.searchResult[1] : Double.NaN;
                     return;
                  case 24:
                     this.returnValue = sensor.hasResultOfIndex(2) ? (double)sensor.searchResult[2] : Double.NaN;
                     return;
                  case 26:
                     this.returnValue = sensor.hasResultOfIndex(3) ? (double)sensor.searchResult[3] : Double.NaN;
                     return;
                  case 28:
                     this.arrayValues = ensureArrayValues(this.arrayValues);
                     this.arrayValues[0] = sensor.hasResultOfIndex(0) ? (double)sensor.searchResult[0] : Double.NaN;
                     this.arrayValues[1] = sensor.hasResultOfIndex(1) ? (double)sensor.searchResult[1] : Double.NaN;
                     this.arrayValues[2] = sensor.hasResultOfIndex(2) ? (double)sensor.searchResult[2] : Double.NaN;
                     this.arrayValues[3] = sensor.hasResultOfIndex(3) ? (double)sensor.searchResult[3] : Double.NaN;
                     this.returnValue = this.arrayValues[0];
                     return;
                  case 50:
                     this.returnValue = (double)sensor.joint.id;
                     return;
                  case 90:
                     this.returnValue = 1.0;
                     return;
                  default:
               }
            } else if (this.detectType == 90) {
               this.returnValue = 0.0;
            } else {
               this.returnValue = Double.NaN;
            }
         } else if (this.detectType == 90) {
            this.returnValue = 0.0;
         } else {
            this.returnValue = Double.NaN;
         }
      }
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return this.detectType == 28 || this.detectType == 16 || this.detectType == 18;
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.sensorNum;
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
      return "jbring.cmn.innode.sensornum.title";
   }

   public int getRefCharIndex(int refNum) {
      return 16;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.sensorNum.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.sensornum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.sensornum.title", (Format)EntryFormats.sensorNumValueFormat, 0, 10);
         gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.sensorDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 112.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgsensor.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgsensor.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
