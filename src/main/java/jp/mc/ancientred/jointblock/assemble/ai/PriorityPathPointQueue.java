package jp.mc.ancientred.jointblock.assemble.ai;

public class PriorityPathPointQueue {
   private JBPathPoint[] pathPoints = new JBPathPoint[1024];
   private int count;

   public JBPathPoint addPoint(JBPathPoint pp) {
      if (pp.index >= 0) {
         throw new IllegalStateException("OW KNOWS!");
      } else {
         if (this.count == this.pathPoints.length) {
            JBPathPoint[] apathpoint = new JBPathPoint[this.count << 1];
            System.arraycopy(this.pathPoints, 0, apathpoint, 0, this.count);
            this.pathPoints = apathpoint;
         }

         this.pathPoints[this.count] = pp;
         pp.index = this.count;
         this.sortBack(this.count++);
         return pp;
      }
   }

   public void clearPath() {
      this.count = 0;
   }

   public JBPathPoint dequeue() {
      JBPathPoint pathpoint = this.pathPoints[0];
      this.pathPoints[0] = this.pathPoints[--this.count];
      this.pathPoints[this.count] = null;
      if (this.count > 0) {
         this.sortForward(0);
      }

      pathpoint.index = -1;
      return pathpoint;
   }

   public void changeDistance(JBPathPoint pp, float cost) {
      float f1 = pp.costDistToTarget;
      pp.costDistToTarget = cost;
      if (cost < f1) {
         this.sortBack(pp.index);
      } else {
         this.sortForward(pp.index);
      }
   }

   private void sortBack(int index) {
      JBPathPoint pathpoint = this.pathPoints[index];

      int j;
      for (float cost = pathpoint.costDistToTarget; index > 0; index = j) {
         j = index - 1 >> 1;
         JBPathPoint pathpointBinMid = this.pathPoints[j];
         if (cost >= pathpointBinMid.costDistToTarget) {
            break;
         }

         this.pathPoints[index] = pathpointBinMid;
         pathpointBinMid.index = index;
      }

      this.pathPoints[index] = pathpoint;
      pathpoint.index = index;
   }

   private void sortForward(int index) {
      JBPathPoint pathpoint = this.pathPoints[index];
      float cost = pathpoint.costDistToTarget;

      while (true) {
         int j = 1 + (index << 1);
         int k = j + 1;
         if (j >= this.count) {
            break;
         }

         JBPathPoint pathpoint1 = this.pathPoints[j];
         float f1 = pathpoint1.costDistToTarget;
         JBPathPoint pathpoint2;
         float f2;
         if (k >= this.count) {
            pathpoint2 = null;
            f2 = Float.POSITIVE_INFINITY;
         } else {
            pathpoint2 = this.pathPoints[k];
            f2 = pathpoint2.costDistToTarget;
         }

         if (f1 < f2) {
            if (f1 >= cost) {
               break;
            }

            this.pathPoints[index] = pathpoint1;
            pathpoint1.index = index;
            index = j;
         } else {
            if (f2 >= cost) {
               break;
            }

            this.pathPoints[index] = pathpoint2;
            pathpoint2.index = index;
            index = k;
         }
      }

      this.pathPoints[index] = pathpoint;
      pathpoint.index = index;
   }

   public boolean isPathEmpty() {
      return this.count == 0;
   }
}
