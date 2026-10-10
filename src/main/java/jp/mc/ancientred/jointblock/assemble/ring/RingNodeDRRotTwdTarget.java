package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import jp.mc.ancientred.jointblock.motion.MotionHelper;

public class RingNodeDRRotTwdTarget extends AbstractActivatableRingNode {
   protected static final int ENTRY_MOTIONCH = 0;
   protected static final int ENTRY_TARGETNUM = 1;
   protected static final int ENTRY_WHILETIME = 2;
   protected static final int ENTRY_MODETYPE = 3;
   protected static final int ENTRY_LIMITANGLE = 4;
   private static final int MODE_TYPE_ROT_XY = 10;
   private static final int MODE_TYPE_ROT_Y = 20;
   private static final int MODE_TYPE_ROT_X = 30;
   private static final int[] MODE_TYPE_CONV = new int[]{10, 20, 30};
   public short motionChannel;
   public ReffableByteValueEntry targetNum = new ReffableByteValueEntry(1, false, 1);
   public byte modeType = 10;
   public ReffableShortValueEntry limitAngle = new ReffableShortValueEntry(180, false, 180);

   public AbstractRingNode newInstance() {
      return new RingNodeDRRotTwdTarget();
   }

   public byte getNodeTypeID() {
      return 36;
   }

   public int getAuthNeeds() {
      return 1;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.motionChannel;
         case 1:
            return (double)this.targetNum.value;
         case 2:
            return (double)this.whileTime.value;
         case 3:
            return (double)convertToTypeIndex(MODE_TYPE_CONV, (short)this.modeType);
         case 4:
            return (double)this.limitAngle.value;
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
            this.targetNum.value = (byte)((int)value);
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
            break;
         case 3:
            this.modeType = (byte)convertToTypeValue(MODE_TYPE_CONV, value);
            break;
         case 4:
            this.limitAngle.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeShort(this.motionChannel);
      out.writeByte(this.modeType);
      out.writeByte(0);
      this.whileTime.write(out);
      this.targetNum.write(out);
      this.limitAngle.write(out);
      out.writeInt(0);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      this.targetNum.writeRunningData(out);
      this.limitAngle.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.motionChannel = inn.readShort(this.motionChannel);
      this.modeType = inn.readByte(this.modeType);
      byte reservedA = inn.readByte((byte)0);
      this.whileTime.read(inn);
      this.targetNum.read(inn);
      this.limitAngle.read(inn);
      int reservedB = inn.readInt(0);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.targetNum.readRunningData(inn);
      this.limitAngle.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.motion, this.motionChannel);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.whileTime.fetchActualValue(program, page) && this.targetNum.fetchActualValue(program, page) && this.limitAngle.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      short timeTick = this.whileTime.getActualValueShort();
      int targetNum = this.targetNum.getActualValueInt();
      int limitAngleInt = this.limitAngle.getActualValueInt();
      if (timeTick > 0) {
         int phase = this.calcPhaseUpdateTiming(timeTick);
         if (phase > 0) {
            if (limitAngleInt > 180) {
               limitAngleInt = 180;
            } else if (limitAngleInt < 0) {
               limitAngleInt = 0;
            }

            jp.doSpecialRotFlg = true;
            byte limitAngle = (byte)(limitAngleInt - 128);
            ej.updateList.addAsExtraTowardsTargetRotSyncMessage(jp, (byte)(255 & targetNum), this.convertMode(this.modeType), limitAngle);
            if (jp.motionHelper == null) {
               jp.motionHelper = new MotionHelper();
            }

            jp.motionHelper.commonUse = (255 & targetNum) << 16 | (255 & this.convertMode(this.modeType)) << 8 | 255 & limitAngle;
         }
      }
   }

   private byte convertMode(int modeType) {
      switch (modeType) {
         case 10:
         default:
            return 0;
         case 20:
            return 1;
         case 30:
            return 2;
      }
   }

   public int getRefCount() {
      return 3;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.targetNum;
         case 2:
            return this.whileTime;
         case 3:
         default:
            return null;
         case 4:
            return this.limitAngle;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 1;
         case 1:
            return 2;
         case 2:
            return 4;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
            return "jbring.cmn.innode.targetnum.title";
         case 2:
            return "jbring.cmn.innode.angle.title";
         default:
            return "jbring.cmn.innode.while.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
            return 18;
         case 2:
            return 17;
         default:
            return 11;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      if (this.targetNum.isNodeRefMode) {
         gs.setParamEntryState(1, "jbring.cmn.innode.targetnum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(1, "jbring.cmn.targetnum.title", (Format)EntryFormats.targetNumValueFormat, 0, 5);
         gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(1);
      super.setParamEntryWhileTime(2, gs);
      gs.setParamEntryState(3, "jbring.cmn.type.title", (Format)EntryFormats.rotTwdTargetTypeValueFormat, 0, lastIdxOf(MODE_TYPE_CONV));
      gs.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
      if (this.limitAngle.isNodeRefMode) {
         gs.setParamEntryState(4, "jbring.cmn.innode.angle.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(4, "cmnmot.limitrot.title", "jbcommon.angledegreese.format", 0, 180);
         gs.setParamButtonToggleAry(4, EntryFormats.defaultButtonStates, 0);
      }

      gs.setRefToggleable(4);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 64.0F;
   }

   public float getOverlayIconV() {
      return 96.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.rottwdtgt.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.rottwdtgt.nodedesc");
   }
}
