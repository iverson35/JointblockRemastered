package jp.mc.ancientred.jointblock.assemble.ring;

import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public abstract class AbstractActivatableRingNode extends AbstractRingNode {
   protected static final int MODIFY_TYPE_SET = 10;
   protected static final int MODIFY_TYPE_ADD = 20;
   protected static final int[] MODIFY_TYPE_CONV = new int[]{10, 20};
   public ReffableShortValueEntry whileTime = new ReffableShortValueEntry(1, false, 1);
   public short remainTick;

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
   }

   public void doActiveEntityWork(EntityJointed ej) {
   }

   public void onForcedRemovedFromSlot() {
      this.remainTick = 0;
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.remainTick > 0 ? (double)this.remainTick : 0.0;
   }

   public void trySetRingNodeToParts(EntityJointed ej, RingProgram program, RingProgramPage page, ActiveRingSlots.SlotType type, short channel) {
      if (ej.activeRingSlots != null) {
         ActiveRingSlots.ActiveSlot slot = ej.activeRingSlots.getOfCreateChannelSlot(program, page, type, channel);
         if (slot != null) {
            if (slot.activeNode != null && slot.activeNode.remainTick <= 0) {
               slot.clearActiveNode();
            }

            if (slot.activeNode == this) {
               return;
            }

            if (slot.activeNode == null) {
               short takesTime = this.onBeforeAddedToSlot(ej, program, page);
               if (takesTime >= 1) {
                  slot.setActiveNode(program, page, this);
                  this.remainTick = takesTime;
                  return;
               }
            }
         }
      }

      this.remainTick = 0;
   }

   protected int calcPhaseUpdateTiming(short timeTick) {
      return timeTick - (this.remainTick + 1 - 1);
   }

   public abstract short onBeforeAddedToSlot(EntityJointed var1, RingProgram var2, RingProgramPage var3);

   protected void setParamEntryWhileTime(int entryIndex, JBGuiSettingsImpl gs) {
      if (this.whileTime.isNodeRefMode) {
         gs.setParamEntryState(entryIndex, (String)"jbring.cmn.innode.while.title", (String)"jbcommon.simplenum.valueformat", -1, 23);
      } else {
         gs.setParamEntryState(entryIndex, (String)"jbring.cmn.timetick.title", (Format)EntryFormats.takesTimeValueFormat, 1, 32767);
      }

      gs.setRefToggleable(entryIndex);
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.activevalue");
   }
}
