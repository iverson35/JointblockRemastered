package jp.mc.ancientred.jointblock.common;

public interface TypedLinkable {
   TypedLinkable getNext();

   TypedLinkable getPrev();

   void setNext(TypedLinkable var1);

   void setPrev(TypedLinkable var1);

   void clearRef();

   boolean checkRendered(long var1);
}
