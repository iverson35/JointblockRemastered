package jp.mc.ancientred.jointblock.tileentity;

import jp.mc.ancientred.jointblock.common.CommonProxyClient;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Invisible tile entity anchoring the sticky-entity batch render box ({@code aabb} is
 * recomputed by {@link CommonProxyClient#updateStickeyTeAABB} from the render bounding box).
 *
 * <p>Not ported (Phase 4, TESR-side in 1.20.1):
 * <ul>
 *   <li>{@code shouldRenderInPass(int)} - 1.12 {@code TileEntitySpecialRenderer} multi-pass
 *       hook; 1.20.1 renderers take a single render type.</li>
 *   <li>{@code getMaxRenderDistanceSquared()} / {@code getDistanceSq(double,double,double)} -
 *       the 1.12 "never cull this TESR" overrides; 1.20.1 culling lives in
 *       {@code BlockEntityRenderer#getViewDistance()} / {@code shouldRender(...)}.</li>
 * </ul>
 */
public class TileEntityJBEntityBatcher extends BlockEntity {
   private AABB aabb = new AABB(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

   public TileEntityJBEntityBatcher(BlockPos pos, BlockState state) {
      super(JBBlockEntityTypes.JB_ENTITY_BATCHER, pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public AABB getRenderBoundingBox() {
      CommonProxyClient.updateStickeyTeAABB(this.aabb);
      return this.aabb;
   }
}
