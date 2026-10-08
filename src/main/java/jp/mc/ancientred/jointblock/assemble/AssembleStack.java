package jp.mc.ancientred.jointblock.assemble;

import java.util.Arrays;

public class AssembleStack {
   public static ThreadLocal<AssembleStack> tHreadAsembleStack = new ThreadLocal<AssembleStack>() {
      protected AssembleStack initialValue() {
         return new AssembleStack();
      }
   };
   private int stackSize;
   private int[] stackPosX;
   private int[] stackPosY;
   private int[] stackPosZ;
   private int[] stackDepth;
   private byte[] stackPSide;
   private JointParts[] stackParent;
   private int index;

   private AssembleStack() {
      this.stackSize = 300;
      this.stackPosX = new int[this.stackSize];
      this.stackPosY = new int[this.stackSize];
      this.stackPosZ = new int[this.stackSize];
      this.stackDepth = new int[this.stackSize];
      this.stackPSide = new byte[this.stackSize];
      this.stackParent = new JointParts[this.stackSize];
   }

   public AssembleStack reset() {
      this.index = 0;
      return this;
   }

   public void push(int posX, int posY, int posZ, int side, JointParts parent, int depth) {
      this.stackPosX[this.index] = posX;
      this.stackPosY[this.index] = posY;
      this.stackPosZ[this.index] = posZ;
      this.stackPSide[this.index] = (byte)side;
      this.stackParent[this.index] = parent;
      this.stackDepth[this.index] = depth;
      ++this.index;
      this.ensureStack();
   }

   public boolean pop() {
      if (this.index == 0) {
         return false;
      } else {
         this.stackParent[this.index] = null;
         --this.index;
         return true;
      }
   }

   public int getPosX() {
      return this.stackPosX[this.index];
   }

   public int getPosY() {
      return this.stackPosY[this.index];
   }

   public int getDepth() {
      return this.stackDepth[this.index];
   }

   public int getPosZ() {
      return this.stackPosZ[this.index];
   }

   public int getPSide() {
      return this.stackPSide[this.index];
   }

   public JointParts getParent() {
      return this.stackParent[this.index];
   }

   private void ensureStack() {
      while(this.index >= this.stackSize) {
         this.stackSize *= 2;
         this.stackPosX = Arrays.copyOf(this.stackPosX, this.stackSize);
         this.stackPosY = Arrays.copyOf(this.stackPosY, this.stackSize);
         this.stackPosZ = Arrays.copyOf(this.stackPosZ, this.stackSize);
         this.stackDepth = Arrays.copyOf(this.stackDepth, this.stackSize);
         this.stackPSide = Arrays.copyOf(this.stackPSide, this.stackSize);
         this.stackParent = (JointParts[])Arrays.copyOf(this.stackParent, this.stackSize);
      }

   }
}
