package jp.mc.ancientred.jointblock.common;

public class IntArray {
   private int[] data;
   private int capacity;
   private int pointer;

   public IntArray() {
      this(32);
   }

   public IntArray(int initCap) {
      this.pointer = 0;
      this.capacity = initCap;
      this.data = new int[this.capacity];
   }

   public int size() {
      return this.pointer;
   }

   public void clear() {
      this.pointer = 0;
   }

   public int[] toIntArray(int limitSize) {
      if (limitSize >= 0 && this.pointer > limitSize) {
         this.pointer = limitSize;
      }

      int[] newArray = new int[this.pointer];
      System.arraycopy(this.data, 0, newArray, 0, this.pointer);
      return newArray;
   }

   public int[] toIntArrayPadded(int headPadding, int limitSize) {
      if (limitSize >= 0 && this.pointer > limitSize) {
         this.pointer = limitSize;
      }

      int[] newArray = new int[this.pointer + headPadding];
      System.arraycopy(this.data, 0, newArray, headPadding, this.pointer);
      return newArray;
   }

   public final int get(int index) {
      return this.data[index];
   }

   public final void set(int index, int value) {
      this.data[index] = value;
   }

   public final void add(int value) {
      this.ensureCapacity();
      this.data[this.pointer++] = value;
   }

   protected void ensureCapacity() {
      if (this.pointer >= this.capacity) {
         int[] newArray = new int[this.capacity *= 2];
         System.arraycopy(this.data, 0, newArray, 0, this.pointer);
         this.data = newArray;
      }

   }
}
