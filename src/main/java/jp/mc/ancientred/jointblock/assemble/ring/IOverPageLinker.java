package jp.mc.ancientred.jointblock.assemble.ring;

public interface IOverPageLinker {
   boolean hasOPPageLinkEntry();

   short getPageLinkNum();

   void setPageLinkNum(int var1);

   boolean hasOPNodeLinkEntry();

   int getNodeLinkNum();

   void setNodeLinkNum(int var1);
}
