package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import jp.mc.ancientred.jointblock.motion.MotionHelper;
import net.minecraft.util.Mth;

public class RingNodeDRMotion1VSet extends AbstractActivatableRingNode {
   protected static final int ENTRY_MOTIONCH = 0;
   protected static final int ENTRY_MODIFYTYPE = 1;
   protected static final int ENTRY_WHILETIME = 2;
   protected static final int ENTRY_MOTIONVAL0 = 3;
   protected static final int EXTENDER_SET_MIN = 0;
   protected static final int EXTENDER_SET_MAX = 300;
   protected static final int SCALER_SET_MIN = 0;
   protected static final int SCALER_SET_MAX = 300;
   protected static final int EX_SC_ADD_MIN = -300;
   protected static final int EX_SC_ADD_MAX = 300;
   public int jointType;
   public byte modifyType = 10;
   public short motionChannel;
   public ReffableShortValueEntry motionVal0 = new ReffableShortValueEntry(0, false);

   public RingNodeDRMotion1VSet() {
   }

   public RingNodeDRMotion1VSet(int argJointType) {
      this.jointType = argJointType;
   }

   public AbstractRingNode newInstance() {
      return new RingNodeDRMotion1VSet(this.jointType);
   }

   public byte getNodeTypeID() {
      return 30;
   }

   public int getAuthNeeds() {
      return 1;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.motionChannel;
         case 1:
            return (double)convertToTypeIndex(MODIFY_TYPE_CONV, (short)this.modifyType);
         case 2:
            return (double)this.whileTime.value;
         case 3:
            return (double)this.motionVal0.value;
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
            byte modifyTypeConv = (byte)convertToTypeValue(MODIFY_TYPE_CONV, value);
            if (modifyTypeConv != this.modifyType) {
               this.motionVal0.reset();
            }

            this.modifyType = modifyTypeConv;
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
            break;
         case 3:
            this.motionVal0.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.jointType);
      out.writeShort(this.motionChannel);
      out.writeByte(0);
      out.writeByte(this.modifyType);
      this.whileTime.write(out);
      this.motionVal0.write(out);
      out.writeInt(0);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      this.motionVal0.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.jointType = inn.readByte((byte)10);
      this.motionChannel = inn.readShort(this.motionChannel);
      byte reservedA = inn.readByte((byte)0);
      this.modifyType = inn.readByte(this.modifyType);
      this.whileTime.read(inn);
      this.motionVal0.read(inn);
      int reservedB = inn.readInt(0);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.motionVal0.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.motion, this.motionChannel);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.whileTime.fetchActualValue(program, page) && this.motionVal0.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      if (jp.jointType == this.jointType) {
         short timeTick = this.whileTime.getActualValueShort();
         short mvalue0 = this.motionVal0.actualValue;
         if (timeTick > 0) {
            int phase = this.calcPhaseUpdateTiming(timeTick);
            if (phase > 0) {
               float interpolate = (float)phase / (float)timeTick;
               if (interpolate > 1.0F) {
                  interpolate = 1.0F;
               }

               int newVal0;
               float fStart0;
               float mod0;
               if (this.modifyType == 10) {
                  if (timeTick == 1) {
                     newVal0 = mvalue0;
                  } else {
                     if (phase == 1) {
                        if (jp.motionHelper == null) {
                           jp.motionHelper = new MotionHelper();
                        }

                        jp.motionHelper.start0 = (short)jp.getExtendRate();
                     }

                     if (jp.motionHelper == null) {
                        return;
                     }

                     fStart0 = (float)jp.motionHelper.start0;
                     mod0 = (float)mvalue0 - fStart0;
                     newVal0 = Math.round(fStart0 + interpolate * mod0);
                  }
               } else if (timeTick == 1) {
                  newVal0 = mvalue0 + (short)jp.getExtendRate();
               } else {
                  if (phase == 1) {
                     if (jp.motionHelper == null) {
                        jp.motionHelper = new MotionHelper();
                     }

                     jp.motionHelper.start0 = (short)jp.getExtendRate();
                  }

                  if (jp.motionHelper == null) {
                     return;
                  }

                  fStart0 = (float)jp.motionHelper.start0;
                  mod0 = (float)mvalue0;
                  newVal0 = Math.round(fStart0 + interpolate * mod0);
               }

               if (this.jointType == 10) {
                  newVal0 = Mth.clamp(newVal0, 0, 300);
               } else {
                  newVal0 = Mth.clamp(newVal0, 0, 300);
               }

               jp.setNewExtendRate(newVal0);
            }
         }
      }
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 2:
            return this.whileTime;
         case 3:
            return this.motionVal0;
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
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      if (refNum == 0) {
         return "jbring.cmn.innode.while.title";
      } else if (this.modifyType == 10) {
         return this.jointType == 12 ? "jbring.motset.innode.scaler.title" : "jbring.motset.innode.extend.title";
      } else {
         return "jbring.motset.innode.emove.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 11 : 15;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(1, (String)"jbring.cmn.runtype.title", (Format)EntryFormats.motModifyTypeValueFormat, 0, lastIdxOf(MODIFY_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(2, gs);
      if (this.modifyType == 10) {
         if (this.motionVal0.isNodeRefMode) {
            if (this.jointType == 12) {
               gs.setParamEntryState(3, (String)"jbring.motset.innode.scaler.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
            } else {
               gs.setParamEntryState(3, (String)"jbring.motset.innode.extend.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
            }
         } else if (this.jointType == 12) {
            gs.setParamEntryState(3, (String)"basicmot.scaler.init.title", "jbcommon.percentage.valueformat", 0, 300);
         } else {
            gs.setParamEntryState(3, (String)"basicmot.extend.init.title", "jbcommon.percentage.valueformat", 0, 300);
         }
      } else if (this.motionVal0.isNodeRefMode) {
         gs.setParamEntryState(3, (String)"jbring.motset.innode.emove.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(3, (String)"basicmot.extend.move.title", "jbcommon.percentage.valueformat", -300, 300);
      }

      gs.setRefToggleable(3);
   }

   public int getIconSize() {
      return 32;
   }

   public float getIconU() {
      switch (this.jointType) {
         case 10:
            return 288.0F;
         case 11:
         case 13:
         default:
            return 256.0F;
         case 12:
            return 352.0F;
         case 14:
            return 320.0F;
      }
   }

   public float getIconV() {
      return 224.0F;
   }

   public boolean hasOverlayIcon() {
      return this.modifyType == 20;
   }

   public float getOverlayIconV() {
      return 96.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.motset.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.motset.nodedesc");
   }
}
