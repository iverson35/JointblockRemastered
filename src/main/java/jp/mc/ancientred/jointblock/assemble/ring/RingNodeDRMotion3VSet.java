package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import jp.mc.ancientred.jointblock.motion.MotionHelper;
import net.minecraft.util.Mth;

public class RingNodeDRMotion3VSet extends AbstractActivatableRingNode {
   protected static final int ENTRY_MOTIONCH = 0;
   protected static final int ENTRY_MODIFYTYPE = 1;
   protected static final int ENTRY_WHILETIME = 2;
   protected static final int ENTRY_MOTIONVAL0 = 3;
   protected static final int ENTRY_MOTIONVAL1 = 4;
   protected static final int ENTRY_MOTIONVAL2 = 5;
   protected static final int ROTATOR_SET_MIN = -359;
   protected static final int ROTATOR_SET_MAX = 359;
   protected static final int SLIDER_SET_MIN = -128;
   protected static final int SLIDER_SET_MAX = 128;
   public int jointType;
   public byte modifyType = 10;
   public short motionChannel;
   public ReffableShortValueEntry motionVal0 = new ReffableShortValueEntry(0, false);
   public ReffableShortValueEntry motionVal1 = new ReffableShortValueEntry(0, false);
   public ReffableShortValueEntry motionVal2 = new ReffableShortValueEntry(0, false);

   public RingNodeDRMotion3VSet() {
   }

   public RingNodeDRMotion3VSet(int argJointType) {
      this.jointType = argJointType;
   }

   public AbstractRingNode newInstance() {
      return new RingNodeDRMotion3VSet(this.jointType);
   }

   public byte getNodeTypeID() {
      return 34;
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
         case 4:
            return (double)this.motionVal1.value;
         case 5:
            return (double)this.motionVal2.value;
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
               this.motionVal1.reset();
               this.motionVal2.reset();
            }

            this.modifyType = modifyTypeConv;
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
            break;
         case 3:
            this.motionVal0.value = (short)((int)value);
            break;
         case 4:
            this.motionVal1.value = (short)((int)value);
            break;
         case 5:
            this.motionVal2.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.jointType);
      out.writeShort(this.motionChannel);
      out.writeByte(0);
      out.writeByte(this.modifyType);
      this.whileTime.write(out);
      this.motionVal0.write(out);
      this.motionVal1.write(out);
      this.motionVal2.write(out);
      out.writeInt(0);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      this.motionVal0.writeRunningData(out);
      this.motionVal1.writeRunningData(out);
      this.motionVal2.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.jointType = inn.readByte((byte)8);
      this.motionChannel = inn.readShort(this.motionChannel);
      byte reservedA = inn.readByte((byte)0);
      this.modifyType = inn.readByte(this.modifyType);
      this.whileTime.read(inn);
      this.motionVal0.read(inn);
      this.motionVal1.read(inn);
      this.motionVal2.read(inn);
      int reservedB = inn.readInt(0);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.motionVal0.readRunningData(inn);
      this.motionVal1.readRunningData(inn);
      this.motionVal2.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.motion, this.motionChannel);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.whileTime.fetchActualValue(program, page) && this.motionVal0.fetchActualValue(program, page) && this.motionVal1.fetchActualValue(program, page) && this.motionVal2.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      if (jp.jointType == this.jointType) {
         short timeTick = this.whileTime.getActualValueShort();
         short mvalue0 = this.motionVal0.actualValue;
         short mvalue1 = this.motionVal1.actualValue;
         short mvalue2 = this.motionVal2.actualValue;
         if (timeTick > 0) {
            int phase = this.calcPhaseUpdateTiming(timeTick);
            if (phase > 0) {
               float interpolate = (float)phase / (float)timeTick;
               if (interpolate > 1.0F) {
                  interpolate = 1.0F;
               }

               int newVal0;
               int newVal1;
               int newVal2;
               float fStart0;
               float fStart1;
               float fStart2;
               float mod0;
               float mod1;
               float mod2;
               if (this.modifyType == 10) {
                  if (timeTick == 1) {
                     newVal0 = mvalue0;
                     newVal1 = mvalue1;
                     newVal2 = mvalue2;
                  } else {
                     if (phase == 1) {
                        if (jp.motionHelper == null) {
                           jp.motionHelper = new MotionHelper();
                        }

                        jp.motionHelper.start0 = (short)jp.getRotationX();
                        jp.motionHelper.start1 = (short)jp.getRotationY();
                        jp.motionHelper.start2 = (short)jp.getRotationZ();
                     }

                     if (jp.motionHelper == null) {
                        return;
                     }

                     fStart0 = (float)jp.motionHelper.start0;
                     fStart1 = (float)jp.motionHelper.start1;
                     fStart2 = (float)jp.motionHelper.start2;
                     mod0 = (float)mvalue0 - fStart0;
                     mod1 = (float)mvalue1 - fStart1;
                     mod2 = (float)mvalue2 - fStart2;
                     newVal0 = Math.round(fStart0 + interpolate * mod0);
                     newVal1 = Math.round(fStart1 + interpolate * mod1);
                     newVal2 = Math.round(fStart2 + interpolate * mod2);
                  }
               } else if (timeTick == 1) {
                  newVal0 = mvalue0 + (short)jp.getRotationX();
                  newVal1 = mvalue1 + (short)jp.getRotationY();
                  newVal2 = mvalue2 + (short)jp.getRotationZ();
               } else {
                  if (phase == 1) {
                     if (jp.motionHelper == null) {
                        jp.motionHelper = new MotionHelper();
                     }

                     jp.motionHelper.start0 = (short)jp.getRotationX();
                     jp.motionHelper.start1 = (short)jp.getRotationY();
                     jp.motionHelper.start2 = (short)jp.getRotationZ();
                  }

                  if (jp.motionHelper == null) {
                     return;
                  }

                  fStart0 = (float)jp.motionHelper.start0;
                  fStart1 = (float)jp.motionHelper.start1;
                  fStart2 = (float)jp.motionHelper.start2;
                  mod0 = (float)mvalue0;
                  mod1 = (float)mvalue1;
                  mod2 = (float)mvalue2;
                  newVal0 = Math.round(fStart0 + interpolate * mod0);
                  newVal1 = Math.round(fStart1 + interpolate * mod1);
                  newVal2 = Math.round(fStart2 + interpolate * mod2);
               }

               if (this.jointType == 8) {
                  newVal0 = RingUtils.moduloRotatorValue(newVal0);
                  newVal1 = RingUtils.moduloRotatorValue(newVal1);
                  newVal2 = RingUtils.moduloRotatorValue(newVal2);
               } else {
                  newVal0 = Mth.clamp(newVal0, -128, 128);
                  newVal1 = Mth.clamp(newVal1, -128, 128);
                  newVal2 = Mth.clamp(newVal2, -128, 128);
               }

               jp.setNewRotationX(newVal0);
               jp.setNewRotationY(newVal1);
               jp.setNewRotationZ(newVal2);
            }
         }
      }
   }

   public int getRefCount() {
      return 4;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 2:
            return this.whileTime;
         case 3:
            return this.motionVal0;
         case 4:
            return this.motionVal1;
         case 5:
            return this.motionVal2;
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
         case 3:
            return 5;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
            return "jbring.cmn.innode.while.title";
         case 1:
            return "jbring.motset.innode.x.title";
         case 2:
            return "jbring.motset.innode.y.title";
         case 3:
            return "jbring.motset.innode.z.title";
         default:
            return null;
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
            return 11;
         case 1:
            return 12;
         case 2:
            return 13;
         case 3:
            return 14;
         default:
            return super.getRefCharIndex(refNum);
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(1, (String)"jbring.cmn.runtype.title", (Format)EntryFormats.motModifyTypeValueFormat, 0, lastIdxOf(MODIFY_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(2, gs);
      if (this.motionVal0.isNodeRefMode) {
         gs.setParamEntryState(3, (String)"jbring.motset.innode.x.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else if (this.jointType == 8) {
         gs.setParamEntryState(3, (ChoiceFormat)EntryFormats.rotateXTitleFormat, "jbcommon.xangledegreese.format", -359, 359);
      } else {
         gs.setParamEntryState(3, (ChoiceFormat)EntryFormats.slideXTitleFormat, "jbcommon.xslideramount.format", -128, 128);
         gs.setParamButtonToggleAry(3, EntryFormats.sliderAmountButtonStates, 0);
      }

      gs.setRefToggleable(3);
      if (this.motionVal1.isNodeRefMode) {
         gs.setParamEntryState(4, (String)"jbring.motset.innode.y.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else if (this.jointType == 8) {
         gs.setParamEntryState(4, (ChoiceFormat)EntryFormats.rotateYTitleFormat, "jbcommon.yangledegreese.format", -359, 359);
      } else {
         gs.setParamEntryState(4, (ChoiceFormat)EntryFormats.slideYTitleFormat, "jbcommon.yslideramount.format", -128, 128);
         gs.setParamButtonToggleAry(4, EntryFormats.sliderAmountButtonStates, 0);
      }

      gs.setRefToggleable(4);
      if (this.motionVal2.isNodeRefMode) {
         gs.setParamEntryState(5, (String)"jbring.motset.innode.z.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else if (this.jointType == 8) {
         gs.setParamEntryState(5, (ChoiceFormat)EntryFormats.rotateZTitleFormat, "jbcommon.zangledegreese.format", -359, 359);
      } else {
         gs.setParamEntryState(5, (ChoiceFormat)EntryFormats.slideZTitleFormat, "jbcommon.zslideramount.format", -128, 128);
         gs.setParamButtonToggleAry(5, EntryFormats.sliderAmountButtonStates, 0);
      }

      gs.setRefToggleable(5);
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
