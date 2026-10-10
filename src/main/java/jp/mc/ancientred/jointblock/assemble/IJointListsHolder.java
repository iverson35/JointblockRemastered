package jp.mc.ancientred.jointblock.assemble;

import jp.mc.ancientred.jointblock.entity.EntityJointed;

public interface IJointListsHolder {
   void setReferenceLists(JointParts[] var1, JointParts[] var2);

   void setMinMaxY(int var1, int var2);

   void createBlocksFromRefLists(BlockArranger var1, int var2, int var3, int var4, int var5);

   void createCoreBlockFromRefLists(BlockArranger var1, int var2, int var3, int var4, int var5);

   public interface BlockArranger {
      void placeBlockAt(JointParts var1, EntityJointed var2, int var3, int var4, int var5, int var6, int var7);
   }
}
