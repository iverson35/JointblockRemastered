package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import jp.mc.ancientred.jointblock.motion.common.SlavePurgeHelper;
import net.minecraft.util.Mth;

public class RingNodeDRAttracter extends AbstractActivatableRingNode {
   protected static final int ENTRY_MOTIONCH = 0;
   protected static final int ENTRY_FUNCTIONTYPE = 1;
   protected static final int ENTRY_PURGETICK = 2;
   protected static final int ENTRY_DISTERANGE = 3;
   protected static final int ENTRY_DISTETICK = 4;
   protected static final int FUNCTION_TYPE_TOGGLE = 10;
   protected static final int FUNCTION_TYPE_ON = 20;
   protected static final int FUNCTION_TYPE_OFF = 30;
   protected static final int FUNCTION_TYPE_PURGE = 40;
   protected static final int FUNCTION_TYPE_DISABLETE = 50;
   protected static final int FUNCTION_TYPE_DISABLANDPURGE = 60;
   protected static final int[] FUNCTION_TYPE_CONV = new int[]{10, 20, 30, 40, 50, 60};
   public short motionChannel;
   public byte functionType = 10;
   public ReffableShortValueEntry purgeTick = new ReffableShortValueEntry(1, false);
   public ReffableByteValueEntry disTeRange = new ReffableByteValueEntry(1, false);
   public ReffableShortValueEntry disTeTick = new ReffableShortValueEntry(1, false);

   public AbstractRingNode newInstance() {
      return new RingNodeDRAttracter();
   }

   public byte getNodeTypeID() {
      return 62;
   }

   public int getAuthNeeds() {
      return 1;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.motionChannel;
         case 1:
            return (double)convertToTypeIndex(FUNCTION_TYPE_CONV, (short)this.functionType);
         case 2:
            return (double)this.purgeTick.value;
         case 3:
            return (double)this.disTeRange.value;
         case 4:
            return (double)this.disTeTick.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.motionChannel = (short)((int)value);
            break;
         case 1:
            byte modifyTypeConv = (byte)convertToTypeValue(FUNCTION_TYPE_CONV, value);
            if (modifyTypeConv != this.functionType) {
               this.purgeTick.reset();
               this.disTeRange.reset();
               this.disTeTick.reset();
            }

            this.functionType = modifyTypeConv;
            break;
         case 2:
            this.purgeTick.value = (short)((int)value);
            break;
         case 3:
            this.disTeRange.value = (byte)((int)value);
            break;
         case 4:
            this.disTeTick.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeShort(this.motionChannel);
      out.writeByte(0);
      out.writeByte(this.functionType);
      this.purgeTick.write(out);
      this.disTeRange.write(out);
      this.disTeTick.write(out);
      out.writeInt(0);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.purgeTick.writeRunningData(out);
      this.disTeRange.writeRunningData(out);
      this.disTeTick.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.motionChannel = inn.readShort(this.motionChannel);
      byte reservedA = inn.readByte((byte)0);
      this.functionType = inn.readByte(this.functionType);
      this.purgeTick.read(inn);
      this.disTeRange.read(inn);
      this.disTeTick.read(inn);
      int reservedB = inn.readInt(0);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.purgeTick.readRunningData(inn);
      this.disTeRange.readRunningData(inn);
      this.disTeTick.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.motion, this.motionChannel);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return (short)(this.purgeTick.fetchActualValue(program, page) && this.disTeRange.fetchActualValue(program, page) && this.disTeTick.fetchActualValue(program, page) ? 1 : 0);
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      if (jp.jointType == 15) {
         short purgeTimeTick = (short)Mth.clamp(this.purgeTick.actualValue, 1, 200);
         int disableTeRange = Mth.clamp(this.disTeRange.actualValue, 1, 16);
         short disableTeTick = (short)Mth.clamp(this.disTeTick.actualValue, 1, 200);
         int function = this.functionType;
         int slotNum = jp.initValues[0][1];
         switch (function) {
            case 10:
               ej.setMasterSlotEnableStateFlag(slotNum, !ej.getMasterSlotEnableStateFlag(slotNum), false);
               return;
            case 20:
               ej.setMasterSlotEnableStateFlag(slotNum, true, false);
               return;
            case 30:
               ej.setMasterSlotEnableStateFlag(slotNum, false, false);
               return;
            case 40:
               jp.purgeAttatchedSlave(ej, purgeTimeTick);
               return;
            case 50:
               SlavePurgeHelper.disableTeInRange(ej, jp.getTransformMatrix(), disableTeRange, disableTeTick);
               return;
            case 60:
               jp.purgeAttatchedSlave(ej, purgeTimeTick);
               SlavePurgeHelper.disableTeInRange(ej, jp.getTransformMatrix(), disableTeRange, disableTeTick);
               return;
            default:
         }
      }
   }

   public int getRefCount() {
      return 3;
   }

   public boolean isRefEntryDisabled() {
      return this.functionType != 40 && this.functionType != 50 && this.functionType != 60;
   }

   public int getCurrentLayingRefNum() {
      if (this.functionType == 40) {
         return 0;
      } else {
         return this.functionType == 50 ? this.gui.refSetCnt % 2 + 1 : super.getCurrentLayingRefNum();
      }
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 2:
            return this.purgeTick;
         case 3:
            return this.disTeRange;
         case 4:
            return this.disTeTick;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 2;
         case 1:
            return 3;
         case 2:
            return 4;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 1:
            return "jbring.attracter.innode.purgetick.title";
         case 2:
            return "jbring.attracter.innode.disterange.title";
         default:
            return "jbring.attracter.innode.distetick.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 1:
            return 11;
         case 2:
            return 11;
         default:
            return 11;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(1, "keymot.attr.function.title", (Format)EntryFormats.attracterKeyFunctionTypeFormatRing, 0, lastIdxOf(FUNCTION_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      if (this.functionType == 40 || this.functionType == 60) {
         if (this.purgeTick.isNodeRefMode) {
            gs.setParamEntryState(2, "jbring.attracter.innode.purgetick.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(2, "keymot.attr.purgetick.title", "jbcommon.simplenum.valueformat", 1, 200);
            gs.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 0);
         }

         gs.setRefToggleable(2);
      }

      if (this.functionType == 50 || this.functionType == 60) {
         if (this.disTeRange.isNodeRefMode) {
            gs.setParamEntryState(3, "jbring.attracter.innode.disterange.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(3, "keymot.attr.disterange.title", "jbcommon.simplenum.valueformat", 1, 16);
            gs.setParamButtonToggleAry(3, EntryFormats.defaultButtonStates, 0);
         }

         gs.setRefToggleable(3);
         if (this.disTeTick.isNodeRefMode) {
            gs.setParamEntryState(4, "jbring.attracter.innode.distetick.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(4, "keymot.attr.distetick.title", "jbcommon.simplenum.valueformat", 1, 200);
            gs.setParamButtonToggleAry(4, EntryFormats.defaultButtonStates, 0);
         }

         gs.setRefToggleable(4);
      }

   }

   public int getIconSize() {
      return 32;
   }

   public float getIconU() {
      return 224.0F;
   }

   public float getIconV() {
      return 224.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.attracter.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.attracter.nodedesc");
   }
}
