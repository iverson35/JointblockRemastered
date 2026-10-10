package jp.mc.ancientred.jointblock.carving.mcube;

public class MCubeGathered {
   public int dim;
   public byte[] gatheredDensity;
   public boolean[] gatheredUnDrawFlg;

   public MCubeGathered(int argDim) {
      int dimPlus2 = argDim + 2;
      int size = dimPlus2 * dimPlus2 * dimPlus2;
      this.dim = argDim;
      this.gatheredDensity = new byte[size];
      this.gatheredUnDrawFlg = new boolean[size];
   }
}
