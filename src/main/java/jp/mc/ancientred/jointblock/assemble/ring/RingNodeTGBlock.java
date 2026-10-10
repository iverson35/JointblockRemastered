package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class RingNodeTGBlock extends AbstractRealTimeStateNode {
   private static final int ENTRY_VALUE_0 = 0;
   private static final int ENTRY_VALUE_1 = 1;
   private static final int ENTRY_VALUE_2 = 2;
   protected static final int ENTRY_DETECT_TYPE = 3;
   private static final int DETECT_TYPE_ISAIR = 10;
   private static final int DETECT_TYPE_BLOCKID = 12;
   private static final int DETECT_TYPE_BLOCKMETA = 14;
   private static final int DETECT_TYPE_ISNORMALBLOCK = 20;
   private static final int DETECT_TYPE_ISOPAQUE = 22;
   private static final int DETECT_TYPE_BLOCKSMOVEMENT = 24;
   private static final int DETECT_TYPE_ISSOLID = 26;
   private static final int DETECT_TYPE_ISLIQUID = 28;
   private static final int DETECT_TYPE_HARDNESS = 30;
   private static final int DETECT_TYPE_OPACITY = 32;
   private static final int DETECT_TYPE_LIGHTVALUE = 34;
   private static final int DETECT_TYPE_CANPROVIDEPOWER = 40;
   private static final int DETECT_TYPE_POWERINPUT = 42;
   private static final int DETECT_TYPE_INDIRECTINPUT = 44;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 12, 14, 20, 22, 24, 26, 28, 30, 32, 34, 40, 42, 44};
   private ReffableDoubleValueEntry value0 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   private ReffableDoubleValueEntry value1 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   private ReffableDoubleValueEntry value2 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   public byte detectType = 10;
   private BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

   public AbstractRingNode newInstance() {
      return new RingNodeTGBlock();
   }

   public byte getNodeTypeID() {
      return 92;
   }

   public int getAuthNeeds() {
      return 131072;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.value0.value;
         case 1:
            return this.value1.value;
         case 2:
            return this.value2.value;
         case 3:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.value0.value = value;
            break;
         case 1:
            this.value1.value = value;
            break;
         case 2:
            this.value2.value = value;
            break;
         case 3:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.value0.write(out);
      this.value1.write(out);
      this.value2.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.value0.read(inn);
      this.value1.read(inn);
      this.value2.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      double targetX = this.fetchEachVectorValue(program, page, this.value0, 0);
      double targetY = this.fetchEachVectorValue(program, page, this.value1, 1);
      double targetZ = this.fetchEachVectorValue(program, page, this.value2, 2);
      if (targetX == targetX && targetY == targetY && targetZ == targetZ) {
         int tXInt = Mth.floor(targetX);
         int tYInt = Mth.floor(targetY);
         int tZInt = Mth.floor(targetZ);
         BlockState bState = WorldHelper.getBlockState(ej.level(), tXInt, tYInt, tZInt);
         if (bState == null) {
            this.returnValue = Double.NaN;
         } else {
            Block block = bState.getBlock();
            if (block == null) {
               this.returnValue = Double.NaN;
            } else {
               switch (this.detectType) {
                  case 10:
                     this.returnValue = (double)booleanValue(block == Blocks.AIR);
                     return;
                  case 11:
                  case 13:
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
                  case 36:
                  case 37:
                  case 38:
                  case 39:
                  case 41:
                  case 43:
                  default:
                     return;
                  case 12:
                     this.returnValue = (double)Block.getId(bState);
                     return;
                  case 14:
                     this.returnValue = (double)WorldHelper.getBlockMetadata(ej.level(), tXInt, tYInt, tZInt);
                     return;
                  case 20:
                     this.returnValue = (double)booleanValue(bState.isRedstoneConductor(ej.level(), this.mPos.set(tXInt, tYInt, tZInt)));
                     return;
                  case 22:
                     this.returnValue = (double)booleanValue(bState.canOcclude());
                     return;
                  case 24:
                     this.returnValue = (double)booleanValue(!bState.isPathfindable(ej.level(), this.mPos.set(tXInt, tYInt, tZInt), PathComputationType.LAND));
                     return;
                  case 26:
                     this.returnValue = (double)booleanValue(bState.blocksMotion());
                     return;
                  case 28:
                     this.returnValue = (double)booleanValue(bState.liquid());
                     return;
                  case 30:
                     this.returnValue = (double)bState.getDestroySpeed(ej.level(), this.mPos.set(tXInt, tYInt, tZInt));
                     return;
                  case 32:
                     this.returnValue = (double)bState.getLightBlock(ej.level(), this.mPos.set(tXInt, tYInt, tZInt));
                     return;
                  case 34:
                     this.returnValue = (double)bState.getLightEmission();
                     return;
                  case 40:
                     this.returnValue = (double)booleanValue(bState.isSignalSource());
                     return;
                  case 42:
                     this.returnValue = (double)ej.level().getBestNeighborSignal(this.mPos.set(tXInt, tYInt, tZInt));
                     return;
                  case 44:
                     this.returnValue = (double)booleanValue(ej.level().hasNeighborSignal(this.mPos.set(tXInt, tYInt, tZInt)));
               }
            }
         }
      } else {
         this.returnValue = Double.NaN;
      }
   }

   public int getRefCount() {
      return 3;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.value0;
         case 1:
            return this.value1;
         case 2:
            return this.value2;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 0;
         case 1:
            return 1;
         case 2:
            return 2;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return "jbring.cmn.innode.x.title";
         case 1:
            return "jbring.cmn.innode.y.title";
         case 2:
            return "jbring.cmn.innode.z.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 12;
         case 1:
            return 13;
         case 2:
            return 14;
      }
   }

   public float getRefCharPos(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 0.35F;
         case 1:
            return 0.3F;
         case 2:
            return 0.25F;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.value0.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.wposx.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.wposx.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(0, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(0);
      if (this.value1.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.wposy.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.wposy.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(1, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(1);
      if (this.value2.isNodeRefMode) {
         gs.setParamEntryState(2, (String)"jbring.cmn.innode.wposz.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(2, (String)"jbring.cmn.wposz.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(2, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(2);
      gs.setParamEntryState(3, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.blockDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
   }

   public boolean allowDecimalInput(int entryNum) {
      return true;
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 160.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgblock.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgblock.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
