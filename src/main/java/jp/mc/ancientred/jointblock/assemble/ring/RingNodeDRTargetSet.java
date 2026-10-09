package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class RingNodeDRTargetSet extends AbstractRingNode {
   protected static final int ENTRY_TARGET_NUM = 0;
   protected static final int ENTRY_TARGET_TYPE = 1;
   protected static final int ENTRY_EID_WPOSX = 2;
   protected static final int ENTRY_WORLDPOSY = 3;
   protected static final int ENTRY_WORLDPOSZ = 4;
   protected static final int TARGETTYPE_ENTITY = 10;
   protected static final int TARGETTYPE_WORLDPOS = 20;
   protected static final int TARGETTYPE_CLEAR = 90;
   protected static final int[] TARGETTYPE_CONV = new int[]{10, 20, 90};
   public ReffableByteValueEntry targetNum = new ReffableByteValueEntry(1, false, 1);
   public byte targetType = 10;
   public ReffableIntValueEntry eIdAndWpX = new ReffableIntValueEntry(0, false);
   public ReffableIntValueEntry worldPosY = new ReffableIntValueEntry(0, false);
   public ReffableIntValueEntry worldPosZ = new ReffableIntValueEntry(0, false);
   public byte returnValue = 0;

   public AbstractRingNode newInstance() {
      return new RingNodeDRTargetSet();
   }

   public byte getNodeTypeID() {
      return 60;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.targetNum.value;
         case 1:
            return (double)convertToTypeIndex(TARGETTYPE_CONV, (short)this.targetType);
         case 2:
            return (double)this.eIdAndWpX.value;
         case 3:
            return (double)this.worldPosY.value;
         case 4:
            return (double)this.worldPosZ.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.targetNum.value = (byte)((int)value);
            break;
         case 1:
            byte convValue = (byte)convertToTypeValue(TARGETTYPE_CONV, value);
            if (convValue != this.targetType) {
               this.eIdAndWpX.reset();
               this.worldPosY.reset();
               this.worldPosZ.reset();
               this.clearRefSetCnt();
            }

            this.targetType = convValue;
            break;
         case 2:
            this.eIdAndWpX.value = (int)value;
            break;
         case 3:
            this.worldPosY.value = (int)value;
            break;
         case 4:
            this.worldPosZ.value = (int)value;
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.targetNum.write(out);
      out.writeByte(this.targetType);
      this.eIdAndWpX.write(out);
      this.worldPosY.write(out);
      this.worldPosZ.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.returnValue);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.targetNum.read(inn);
      this.targetType = inn.readByte(this.targetType);
      this.eIdAndWpX.read(inn);
      this.worldPosY.read(inn);
      this.worldPosZ.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readByte(this.returnValue);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.returnValue = 0;
      if (!this.targetNum.fetchActualValue(program, page)) {
         return program.nextNodeIndex(page);
      } else {
         int targetNum = this.targetNum.getActualValueInt();
         if (targetNum >= 1 && targetNum < ej.targetList.length) {
            if (this.targetType == 10) {
               if (!this.eIdAndWpX.fetchActualValue(program, page)) {
                  return program.nextNodeIndex(page);
               }

               int entityId = this.eIdAndWpX.getActualValueInt();
               Entity entity = ej.level().getEntity(entityId);
               if (entity != null && !entity.isRemoved()) {
                  if (ej.targetList[targetNum] == null || !ej.targetList[targetNum].isTargetEntity || ej.targetList[targetNum].targetEntity != entity) {
                     ej.targetList[targetNum] = new EjTarget(entity);
                     ej.needToSendTargetUpdate = true;
                  }

                  this.returnValue = 1;
               }
            } else if (this.targetType == 20) {
               double wPosXDbl = this.fetchEachVectorValue(program, page, this.eIdAndWpX, 0);
               double wPosYDbl = this.fetchEachVectorValue(program, page, this.worldPosY, 1);
               double wPosZDbl = this.fetchEachVectorValue(program, page, this.worldPosZ, 2);
               if (wPosXDbl != wPosXDbl || wPosYDbl != wPosYDbl || wPosZDbl != wPosZDbl) {
                  return program.nextNodeIndex(page);
               }

               int wPosX = Mth.floor(wPosXDbl);
               int wPosY = Mth.floor(wPosYDbl);
               int wPosZ = Mth.floor(wPosZDbl);
               if (ej.targetList[targetNum] == null || ej.targetList[targetNum].isTargetEntity || ej.targetList[targetNum].worldPosX != wPosX || ej.targetList[targetNum].worldPosY != wPosY || ej.targetList[targetNum].worldPosZ != wPosZ) {
                  ej.targetList[targetNum] = new EjTarget(wPosX, wPosY, wPosZ);
                  ej.needToSendTargetUpdate = true;
               }

               this.returnValue = 1;
            } else {
               ej.targetList[targetNum] = null;
               ej.needToSendTargetUpdate = true;
               this.returnValue = 1;
            }
         }

         return program.nextNodeIndex(page);
      }
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return (double)this.returnValue;
   }

   public int getRefCount() {
      return 4;
   }

   public int getCurrentLayingRefNum() {
      return this.targetType == 10 ? this.gui.refSetCnt % 2 : super.getCurrentLayingRefNum();
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.targetNum;
         case 1:
         default:
            return null;
         case 2:
            return this.eIdAndWpX;
         case 3:
            return this.worldPosY;
         case 4:
            return this.worldPosZ;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
         case 1:
            return 2;
         case 2:
            return 3;
         case 3:
            return 4;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 1:
            if (this.targetType == 10) {
               return "jbring.cmn.innode.entityid.title";
            }

            return "jbring.cmn.innode.wposx.title";
         case 2:
            return "jbring.cmn.innode.wposy.title";
         case 3:
            return "jbring.cmn.innode.wposz.title";
         default:
            return "jbring.cmn.innode.targetnum.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 1:
            if (this.targetType == 10) {
               return 15;
            }

            return 12;
         case 2:
            return 13;
         case 3:
            return 14;
         default:
            return 18;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.targetNum.isNodeRefMode) {
         gs.setParamEntryState(0, "jbring.cmn.innode.targetnum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, "jbring.cmn.targetnum.title", (Format)EntryFormats.targetNumValueFormat, 1, 5);
         gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, "jbring.targettype.title", (Format)EntryFormats.targetTypeValueFormat, 0, lastIdxOf(TARGETTYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      if (this.targetType == 10) {
         if (this.eIdAndWpX.isNodeRefMode) {
            gs.setParamEntryState(2, "jbring.cmn.innode.entityid.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(2, "jbring.cmn.entityid.title", (Format)EntryFormats.simpleDecimalFormat, Integer.MIN_VALUE, Integer.MAX_VALUE);
         }

         gs.setRefToggleable(2);
      } else if (this.targetType == 20) {
         if (this.eIdAndWpX.isNodeRefMode) {
            gs.setParamEntryState(2, "jbring.cmn.innode.wposx.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(2, "jbring.cmn.wposx.title", (Format)EntryFormats.simpleDecimalFormat, -30000000, 30000000);
         }

         gs.setRefToggleable(2);
         if (this.worldPosY.isNodeRefMode) {
            gs.setParamEntryState(3, "jbring.cmn.innode.wposy.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(3, "jbring.cmn.wposy.title", (Format)EntryFormats.simpleDecimalFormat, 0, 255);
         }

         gs.setRefToggleable(3);
         if (this.worldPosZ.isNodeRefMode) {
            gs.setParamEntryState(4, "jbring.cmn.innode.wposz.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(4, "jbring.cmn.wposz.title", (Format)EntryFormats.simpleDecimalFormat, -30000000, 30000000);
         }

         gs.setRefToggleable(4);
      }

   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 80.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.targetset.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.targetset.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.targetset.rvaldesc");
   }
}
