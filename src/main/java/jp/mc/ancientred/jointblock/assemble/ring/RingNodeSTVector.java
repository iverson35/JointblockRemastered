package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTVector extends AbstractSyntaxRingNode {
   private static final int ENTRY_VALUE_0 = 0;
   private static final int ENTRY_VALUE_1 = 1;
   private static final int ENTRY_VALUE_2 = 2;
   private static final int ENTRY_VALUE_3 = 3;
   private ReffableDoubleValueEntry value0 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   private ReffableDoubleValueEntry value1 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   private ReffableDoubleValueEntry value2 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   private ReffableDoubleValueEntry value3 = new ReffableDoubleValueEntry(0.0, false, 0.0);
   public final double[] arrayValues = new double[4];

   public AbstractRingNode newInstance() {
      return new RingNodeSTVector();
   }

   public byte getNodeTypeID() {
      return 24;
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
            return this.value3.value;
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
            this.value3.value = value;
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.value0.write(out);
      this.value1.write(out);
      this.value2.write(out);
      this.value3.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      for(int i = 0; i < 4; ++i) {
         out.writeDouble(this.arrayValues[i]);
      }

   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.value0.read(inn);
      this.value1.read(inn);
      this.value2.read(inn);
      this.value3.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      for(int i = 0; i < 4; ++i) {
         this.arrayValues[i] = inn.readDouble(this.arrayValues[i]);
      }

   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.arrayValues[0];
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return true;
   }

   public double getArrayValue(RingProgram program, RingProgramPage page, int index) {
      return index >= 0 && index < this.arrayValues.length ? this.arrayValues[index] : Double.NaN;
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.arrayValues[0] = this.fetchEachVectorValue(program, page, this.value0, 0);
      this.arrayValues[1] = this.fetchEachVectorValue(program, page, this.value1, 1);
      this.arrayValues[2] = this.fetchEachVectorValue(program, page, this.value2, 2);
      this.arrayValues[3] = this.fetchEachVectorValue(program, page, this.value3, 3);
      return program.nextNodeIndex(page);
   }

   public int getRefCount() {
      return 4;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.value0;
         case 1:
            return this.value1;
         case 2:
            return this.value2;
         case 3:
            return this.value3;
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
         case 3:
            return 3;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return "jbring.cmn.innode.v0.title";
         case 1:
            return "jbring.cmn.innode.v1.title";
         case 2:
            return "jbring.cmn.innode.v2.title";
         case 3:
            return "jbring.cmn.innode.v3.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
         default:
            return 0;
         case 1:
            return 1;
         case 2:
            return 2;
         case 3:
            return 3;
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
         case 3:
            return 0.2F;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.value0.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.v0.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(0, (String)"jbring.vector.v0.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(0, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(0);
      if (this.value1.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.v1.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(1, (String)"jbring.vector.v1.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(1, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(1);
      if (this.value2.isNodeRefMode) {
         gs.setParamEntryState(2, (String)"jbring.cmn.innode.v2.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(2, (String)"jbring.vector.v2.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(2, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(2);
      if (this.value3.isNodeRefMode) {
         gs.setParamEntryState(3, (String)"jbring.cmn.innode.v3.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(3, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(3, (String)"jbring.vector.v3.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(3, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(3);
   }

   public boolean allowDecimalInput(int entryNum) {
      return true;
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 144.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.vector.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.vector.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.var");
   }
}
