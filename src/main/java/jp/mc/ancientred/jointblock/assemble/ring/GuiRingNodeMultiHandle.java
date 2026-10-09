package jp.mc.ancientred.jointblock.assemble.ring;

import java.util.ArrayList;

public class GuiRingNodeMultiHandle extends AbstractRingNode {
   public ArrayList<AbstractRingNode> multiNodes = new ArrayList();
   public ArrayList<AbstractRingNode> originalListInst;
   public ArrayList<AbstractRingNode> originalListCopy;
   public int originalFrom = -1;
   public int originalTo = -1;

   public AbstractRingNode newInstance() {
      return null;
   }

   public byte getNodeTypeID() {
      return -1;
   }

   public float getIconV() {
      return 16.0F;
   }

   public AbstractRingNode cloneNode(NodeCloneHelper helper) {
      boolean withGui = this.gui != null;
      GuiRingNodeMultiHandle newNode = new GuiRingNodeMultiHandle();
      int mIdx = 0;

      int len;
      AbstractRingNode refFrom;
      for(len = this.multiNodes.size(); mIdx < len; ++mIdx) {
         refFrom = (AbstractRingNode)this.multiNodes.get(mIdx);
         AbstractRingNode clonedEach = refFrom.cloneNode(helper);
         if (withGui) {
            clonedEach.withGui();
         }

         newNode.multiNodes.add(clonedEach);
      }

      mIdx = 0;

      for(len = this.multiNodes.size(); mIdx < len; ++mIdx) {
         refFrom = (AbstractRingNode)this.multiNodes.get(mIdx);
         if (refFrom.gui != null && refFrom.hasRefEntry()) {
            for(int ref = 0; ref < refFrom.getRefCount(); ++ref) {
               if (!refFrom.isNotInRefMode(ref) && refFrom.gui.refNodes[ref] != null) {
                  int indexOrg = this.multiNodes.indexOf(refFrom.gui.refNodes[ref]);
                  if (indexOrg != -1) {
                     AbstractRingNode clonedEach = (AbstractRingNode)newNode.multiNodes.get(mIdx);
                     clonedEach.gui.refNodes[ref] = (AbstractRingNode)newNode.multiNodes.get(indexOrg);
                  }
               }
            }
         }
      }

      return newNode;
   }

   public void clearOriginalListPos() {
      this.originalListCopy = null;
      this.originalListInst = null;
      this.originalFrom = -1;
      this.originalTo = -1;
   }
}
