package jp.mc.ancientred.jointblock.assemble.ai;

public class ComputedPointMap {
   private static final float GROW_FACTOR = 0.75F;
   private int count;
   private int threshold = 6144;
   private JBPathPoint[] slots = new JBPathPoint[8192];

   public void clearMap() {
      JBPathPoint[] ary = this.slots;
      int i = 0;

      for (int len = ary.length; i < len; ++i) {
         ary[i] = null;
      }

      this.count = 0;
   }

   private static final int computeHash(int key) {
      key ^= key >>> 20 ^ key >>> 12;
      return key ^ key >>> 7 ^ key >>> 4;
   }

   private static final int calcIndex(int hash, int len) {
      return hash & len - 1;
   }

   public JBPathPoint lookup(int key) {
      int hash = computeHash(key);

      for (JBPathPoint entry = this.slots[calcIndex(hash, this.slots.length)]; entry != null; entry = entry.e_nextEntry) {
         if (entry.e_entryKey == key) {
            return entry;
         }
      }

      return null;
   }

   public void addKey(int key, JBPathPoint obj) {
      int hash = computeHash(key);
      int idx = calcIndex(hash, this.slots.length);

      for (JBPathPoint entry = this.slots[idx]; entry != null; entry = entry.e_nextEntry) {
         if (entry.e_entryKey == key) {
            this.slots[idx] = obj;
            return;
         }
      }

      this.slots[idx] = obj.setEntryValues(hash, key, this.slots[idx]);
      if (this.count++ >= this.threshold) {
         this.grow(2 * this.slots.length);
      }
   }

   private void grow(int newLength) {
      JBPathPoint[] newAry = new JBPathPoint[newLength];
      JBPathPoint[] oldAry = this.slots;
      int j = 0;

      for (int len = oldAry.length; j < len; ++j) {
         JBPathPoint entry = oldAry[j];
         JBPathPoint entryNxt;
         if (entry != null) {
            do {
               entryNxt = entry.e_nextEntry;
               int recomputedIdx = calcIndex(entry.e_slotHash, newLength);
               entry.e_nextEntry = newAry[recomputedIdx];
               newAry[recomputedIdx] = entry;
               entry = entryNxt;
            } while (entryNxt != null);
         }
      }

      this.slots = newAry;
      this.threshold = (int)((float)newLength * 0.75F);
   }
}
