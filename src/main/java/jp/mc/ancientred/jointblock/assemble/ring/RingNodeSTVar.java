package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTVar extends AbstractSyntaxRingNode implements INodeValueSettable {
   private static final int ENTRY_INITVALUE = 0;
   private double initValue = 0.0;
   protected double returnValue = 0.0;

   public AbstractRingNode newInstance() {
      return new RingNodeSTVar();
   }

   public byte getNodeTypeID() {
      return 16;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.initValue;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.initValue = value;
         default:
      }
   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.initValue);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.returnValue);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.initValue = inn.readDouble(this.initValue);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readDouble(this.returnValue);
   }

   public void onProgramInit(RingProgram program, RingProgramPage page) {
      this.returnValue = this.initValue;
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.returnValue;
   }

   public void setReturnValue(RingProgram program, RingProgramPage page, double newValue) {
      this.returnValue = newValue;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.var.initvalue.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
      gs.setParamButtonToggleAry(0, EntryFormats.hasDecimalButtonStates, 1);
   }

   public boolean allowDecimalInput(int entryNum) {
      return entryNum == 0;
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 0.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.var.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.var.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.var");
   }
}
