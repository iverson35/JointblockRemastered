package jp.mc.ancientred.jointblock.common;

import java.util.ArrayList;

public class BlockedArray<E> {
   private final int size;
   private final BlockedArrayBlockSize blockSize;
   private Object[][] valuesBlock;
   private ArrayList<E> list = new ArrayList();

   public BlockedArray(BlockedArrayBlockSize argBlockSize, int argSizeMul16) {
      this.size = argSizeMul16;
      this.blockSize = argBlockSize;
      this.valuesBlock = new Object[this.size >> this.blockSize.bShift][];
   }

   public int size() {
      return this.size;
   }

   @SuppressWarnings("unchecked")
   public E get(int index) {
      if (index >= 0 && index < this.size) {
         int bIndex = index >> this.blockSize.bShift;
         if (this.valuesBlock[bIndex] == null) {
            this.valuesBlock[bIndex] = new Object[this.blockSize.bSize];
         }

         int lIndex = index & this.blockSize.bMask;
         return (E)this.valuesBlock[bIndex][lIndex];
      } else {
         return null;
      }
   }

   public void put(int index, E obj) {
      if (index >= 0 && index < this.size) {
         int bIndex = index >> this.blockSize.bShift;
         if (this.valuesBlock[bIndex] == null) {
            this.valuesBlock[bIndex] = new Object[this.blockSize.bSize];
         }

         int lIndex = index & this.blockSize.bMask;
         this.valuesBlock[bIndex][lIndex] = obj;
         this.list.add(obj);
      }
   }

   public ArrayList<E> getIterable() {
      return this.list;
   }

   public static enum BlockedArrayBlockSize {
      b16(16, 4, 15),
      b32(32, 5, 31);

      public final int bSize;
      public final int bShift;
      public final int bMask;

      private BlockedArrayBlockSize(int argBSize, int argBShift, int argBMask) {
         this.bSize = argBSize;
         this.bShift = argBShift;
         this.bMask = argBMask;
      }
   }
}
