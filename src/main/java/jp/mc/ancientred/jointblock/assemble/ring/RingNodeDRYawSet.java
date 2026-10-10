package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRYawSet extends AbstractActivatableRingNode {
   private static final int ENTRY_MODIFYTYPE = 0;
   private static final int ENTRY_WHILETIME = 1;
   private static final int ENTRY_YAWVALUE = 2;
   public ReffableDoubleValueEntry yawValue = new ReffableDoubleValueEntry(0.0, false, 0.0);
   public byte modifyType = 10;
   public float yawStart;

   public AbstractRingNode newInstance() {
      return new RingNodeDRYawSet();
   }

   public byte getNodeTypeID() {
      return 42;
   }

   public int getAuthNeeds() {
      return 4;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(MODIFY_TYPE_CONV, (short)this.modifyType);
         case 1:
            return (double)this.whileTime.value;
         case 2:
            return this.yawValue.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            byte modifyTypeConv = (byte)convertToTypeValue(MODIFY_TYPE_CONV, value);
            if (modifyTypeConv != this.modifyType) {
               this.yawValue.reset();
            }

            this.modifyType = modifyTypeConv;
            break;
         case 1:
            this.whileTime.value = (short)((int)value);
            break;
         case 2:
            this.yawValue.value = value;
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.modifyType);
      this.whileTime.write(out);
      this.yawValue.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      this.yawValue.writeRunningData(out);
      out.writeShort(this.remainTick);
      out.writeFloat(this.yawStart);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.modifyType = inn.readByte(this.modifyType);
      this.whileTime.read(inn);
      this.yawValue.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.yawValue.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
      this.yawStart = inn.readFloat(this.yawStart);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.move, (short)3);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.whileTime.fetchActualValue(program, page) && this.yawValue.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveEntityWork(EntityJointed ej) {
      if (ej.entitySeatPart0 != null && !ej.entitySeatPart0.lockMoveDR) {
         short timeTick = this.whileTime.getActualValueShort();
         float yawValue = (float)this.yawValue.actualValue;
         if (timeTick > 0) {
            int phase = this.calcPhaseUpdateTiming(timeTick);
            if (phase > 0) {
               float interpolate = (float)phase / (float)timeTick;
               if (interpolate > 1.0F) {
                  interpolate = 1.0F;
               }

               float newVal;
               if (this.modifyType == 10) {
                  if (timeTick == 1) {
                     newVal = yawValue;
                  } else {
                     if (phase == 1) {
                        this.yawStart = ej.getYRot();
                     }

                     float mod0 = yawValue - this.yawStart;
                     newVal = this.yawStart + interpolate * mod0;
                  }
               } else if (timeTick == 1) {
                  newVal = yawValue + ej.getYRot();
               } else {
                  if (phase == 1) {
                     this.yawStart = ej.getYRot();
                  }

                  newVal = this.yawStart + interpolate * yawValue;
               }

               ej.entitySeatPart0.hasRINGMoveDR = true;
               ej.entitySeatPart0.yawChange = true;
               ej.entitySeatPart0.yawAdd = false;
               ej.entitySeatPart0.yawValue = newVal % 360.0F;
            }
         }
      }
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.whileTime;
         case 2:
            return this.yawValue;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      return refNum == 0 ? 1 : 2;
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.while.title" : "jbring.yawset.innode.yaw.title";
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 11 : 13;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.runtype.title", (Format)EntryFormats.motModifyTypeValueFormat, 0, lastIdxOf(MODIFY_TYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(1, gs);
      if (this.yawValue.isNodeRefMode) {
         gs.setParamEntryState(2, "jbring.yawset.innode.yaw.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         if (this.modifyType == 20) {
            gs.setParamEntryState(2, (ChoiceFormat)EntryFormats.yawRotSetTitleFormat, (Format)EntryFormats.yawDecimalAngleValueFormat, -360, 360);
         } else {
            gs.setParamEntryState(2, "jbring.yawset.yawabs.title", (Format)EntryFormats.yawDecimalAngleValueFormat, -360, 360);
         }

         gs.setParamButtonToggleAry(2, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(2);
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 80.0F;
   }

   public boolean hasOverlayIcon() {
      return this.modifyType == 20;
   }

   public float getOverlayIconV() {
      return 96.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.yawset.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.yawset.nodedesc");
   }
}
