package jp.mc.ancientred.jointblock.assemble.ring;

import jp.mc.ancientred.jointblock.common.CompatHelper;

public class RingNodeSTEmpty extends AbstractSyntaxRingNode {
   public AbstractRingNode newInstance() {
      return new RingNodeSTEmpty();
   }

   public byte getNodeTypeID() {
      return 0;
   }

   public float getIconV() {
      return 0.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.empty.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.empty.nodedesc");
   }
}
