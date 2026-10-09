package jp.mc.ancientred.jointblock.item.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

/**
 * 1.20.1 port of the 1.12.2 {@code BakedSubIconItemModel}: a baked item model that merges the
 * quads of a wrapped item model with the (position-rebaked) quads of the mod's small "sub icon"
 * model.
 *
 * <p>Everything the 1.12.2 class delegated is delegated again here:
 * {@link #getQuads}, {@link #useAmbientOcclusion()}, {@link #isGui3d()}, {@link #usesBlockLight()}
 * (1.20.1 replacement for the removed {@code ItemCameraTransforms.TransformType}-era lighting flag,
 * which vanilla now reads off the baked model), {@link #isCustomRenderer()},
 * {@link #getParticleIcon()}, {@link #getTransforms()}, {@link #getOverrides()} and Forge's
 * {@link #getModelData}/{@link #getRenderTypes}. {@code getRenderPasses} is deliberately
 * <em>not</em> delegated: Forge's default returns {@code List.of(this)} so that the sub icon quads
 * survive; delegating would drop them and render the base model alone.
 *
 * <p>Two 1.12.2 details are kept bit-for-bit:
 * <ul>
 * <li>{@link #getQuads} reuses one mutable output list (the 1.12.2 {@code allGeneralQuads} field)
 *     instead of allocating a fresh list on every call.</li>
 * <li>{@link #getOverrides()} returns the empty override list. 1.12.2 returned
 *     {@code ItemOverrideList.NONE}; in 1.20.1 that is {@link ItemOverrides#EMPTY}. The wrapped
 *     model's own overrides are intentionally ignored, exactly as in 1.12.2.</li>
 * </ul>
 */
public class BakedSubIconItemModel implements BakedModel {
   private final BakedModel modelBase;
   private final List<BakedQuad> subBqList;
   private final List<BakedQuad> allGeneralQuads = new ArrayList<>();

   public BakedSubIconItemModel(BakedModel argModelBase, List<BakedQuad> argSubBqList) {
      this.modelBase = argModelBase;
      this.subBqList = argSubBqList;
   }

   /** 1.12.2 {@code func_188616_a}: base quads first, then the sub icon quads (for any side). */
   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
      this.allGeneralQuads.clear();
      this.allGeneralQuads.addAll(this.modelBase.getQuads(state, side, rand));
      this.allGeneralQuads.addAll(this.subBqList);
      return this.allGeneralQuads;
   }

   /** 1.12.2 {@code func_177555_b}. */
   @Override
   public boolean useAmbientOcclusion() {
      return this.modelBase.useAmbientOcclusion();
   }

   /** 1.12.2 {@code func_177556_c}. */
   @Override
   public boolean isGui3d() {
      return this.modelBase.isGui3d();
   }

   /** 1.12.2 had no equivalent flag; the merged model keeps the base model's lighting. */
   @Override
   public boolean usesBlockLight() {
      return this.modelBase.usesBlockLight();
   }

   /** 1.12.2 {@code func_188618_c}: the base model decides whether it needs a custom renderer. */
   @Override
   public boolean isCustomRenderer() {
      return this.modelBase.isCustomRenderer();
   }

   /** 1.12.2 {@code func_177554_e}. */
   @Override
   public TextureAtlasSprite getParticleIcon() {
      return this.modelBase.getParticleIcon();
   }

   /** 1.12.2 {@code func_177552_f}. */
   @Override
   public ItemTransforms getTransforms() {
      return this.modelBase.getTransforms();
   }

   /** 1.12.2 {@code func_188617_f} returned {@code ItemOverrideList.NONE} = {@link ItemOverrides#EMPTY}. */
   @Override
   public ItemOverrides getOverrides() {
      return ItemOverrides.EMPTY;
   }

   @Override
   public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
      return this.modelBase.getModelData(level, pos, state, modelData);
   }

   @Override
   public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData modelData) {
      return this.modelBase.getRenderTypes(state, rand, modelData);
   }

   @Override
   public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
      return this.modelBase.getRenderTypes(stack, fabulous);
   }
}
