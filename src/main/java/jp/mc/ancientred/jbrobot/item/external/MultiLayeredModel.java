package jp.mc.ancientred.jbrobot.item.external;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.model.obj.AbstractJBObjModel;
import jp.mc.ancientred.jointblock.model.obj.WavefrontBuildedObj;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import jp.mc.ancientred.jointblock.modelpack.ExJBModelInfo;
import jp.mc.ancientred.jointblock.modelpack.ExJBModelLayer;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import jp.mc.ancientred.jointblock.modelpack.FileLoader;
import jp.mc.ancientred.jointblock.modelpack.JBRModelUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The {@link IJBModelItem.IJBModel} of a pack model: draws the model's wavefront layers, bucketed by
 * render pass.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.MultiLayeredModel}. The 1.12.2 render loop
 * translated by {@code scale * 24} (the call sites always passed {@code scale = 0.0625F}), flipped
 * Y/Z, then bound either the shared white texture (for layers carrying a {@code .mtl}) or the
 * layer's own texture and called the cached GL display list of a
 * {@link WavefrontBuildedObj}. 1.20.1 has no display lists, so the mesh is re-emitted every frame
 * through {@link JBRenderTypes#jointModel(ResourceLocation)} (textures travel with the render type),
 * which is also why the layer {@code .obj} is parsed lazily out of the pack's
 * {@link ExtraPackage#fileLoader} on first draw.
 *
 * <p>Two recorded 1.12.2 -> 1.20.1 substitutions:
 * <ul>
 *   <li>{@code LoadedTextures.whiteResTexId} no longer exists (1.20.1 textures come from the render
 *       type); the port uses {@code jointblock:textures/entity/white.png}, the same all-white texel
 *       the 1.12.2 runtime texture provided, so the per-face {@code .mtl} colour still shows through.</li>
 *   <li>the {@code JBConfig.hasShaderModInstalled} branch that selected
 *       {@code renderModelWithMtlWithoutNormal} is gone (the port does not detect shader mods), so
 *       material layers always draw with the normal-bearing variant.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class MultiLayeredModel extends AbstractJBObjModel {
    private static final int RENDER_TYPE_ONLY_0 = 0;
    private static final int RENDER_TYPE_ONLY_1 = 1;
    private static final int RENDER_TYPE_EVERY = 2;

    /** The 1.12.2 call sites always rendered pack models with {@code scale = 0.0625F}. */
    private static final float MODEL_RENDER_SCALE = 0.0625F;

    /** Stands in for the 1.12.2 white runtime texture bound for {@code .mtl} layers. */
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("jointblock:textures/entity/white.png");

    private int renderType = RENDER_TYPE_ONLY_0;
    private ExJBModelInfo modelInf;
    private ExJBModelLayer[][] modelLayersByRenderPass = new ExJBModelLayer[2][];

    public MultiLayeredModel(ExJBModelInfo modelInf) {
        this.modelInf = modelInf;
        ExJBModelLayer[] layers = modelInf.modelLayers != null ? modelInf.modelLayers : new ExJBModelLayer[0];
        int pass0Count = 0;
        int pass1Count = 0;

        int i;
        for (i = 0; i < layers.length; ++i) {
            if (layers[i].pass == 0) {
                ++pass0Count;
            }

            if (layers[i].pass == 1) {
                ++pass1Count;
            }
        }

        this.modelLayersByRenderPass[0] = new ExJBModelLayer[pass0Count];
        this.modelLayersByRenderPass[1] = new ExJBModelLayer[pass1Count];
        pass0Count = 0;
        pass1Count = 0;

        for (i = 0; i < layers.length; ++i) {
            if (layers[i].pass == 0) {
                this.modelLayersByRenderPass[0][pass0Count++] = layers[i];
            }

            if (layers[i].pass == 1) {
                this.modelLayersByRenderPass[1][pass1Count++] = layers[i];
            }
        }

        if (pass0Count != 0 && pass1Count != 0) {
            this.renderType = RENDER_TYPE_EVERY;
        } else if (pass1Count != 0) {
            this.renderType = RENDER_TYPE_ONLY_1;
        }
    }

    @Override
    public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
        if (renderPass < 0 || renderPass >= this.modelLayersByRenderPass.length) {
            return;
        }

        pose.translate(0.0F, MODEL_RENDER_SCALE * 24.0F, 0.0F);
        pose.scale(1.0F, -1.0F, -1.0F);

        ExJBModelLayer[] modelLayerForPass = this.modelLayersByRenderPass[renderPass];
        for (ExJBModelLayer modelLayer : modelLayerForPass) {
            if (!this.ensureLayerLoaded(modelLayer)) {
                continue;
            }

            if (modelLayer.wavefrontObj.isWithMaterial) {
                VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(WHITE_TEXTURE));
                this.renderModelWithMtl(modelLayer.wavefrontObj, pose, consumer, packedLight, packedOverlay);
            } else {
                VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.jointModel(modelLayer.textureLocation));
                this.renderModel(modelLayer.wavefrontObj, pose, consumer, packedLight, packedOverlay);
            }
        }
    }

    /**
     * Parses the layer's {@code .obj} (and its {@code .mtl}, when present) out of the pack zip the
     * first time the layer is drawn. {@link ExJBModelLayer#textureLocation} doubles as the
     * "parse attempted" marker, so a broken layer is not re-parsed every frame.
     */
    private boolean ensureLayerLoaded(ExJBModelLayer modelLayer) {
        if (modelLayer.textureLocation == null) {
            modelLayer.textureLocation = this.modelInf.parent.packResource(modelLayer.textureFileName);
            this.parseLayer(modelLayer);
        }

        return modelLayer.wavefrontObj != null;
    }

    private void parseLayer(ExJBModelLayer modelLayer) {
        ExtraPackage pkg = this.modelInf.parent;
        if (pkg == null || pkg.fileLoader == null) {
            this.modelLoadError = true;
            return;
        }

        try {
            FileLoader.FileEntry entryModel = pkg.getEntryIgnoreCase(modelLayer.modelFileName);
            if (JBRModelUtils.isInvalidFileLoaderEntiry(entryModel)) {
                JBRModelUtils.LogFailedReadingModelFile(modelLayer.modelFileName);
                this.modelLoadError = true;
                return;
            }

            FileLoader.FileEntry entryMtl = JBRModelUtils.getMtlFileFromModelEntry(pkg.fileLoader, entryModel);
            try (InputStream is = pkg.fileLoader.getInputStream(entryModel);
                 InputStream isMtl = entryMtl != null ? pkg.fileLoader.getInputStream(entryMtl) : null) {
                modelLayer.wavefrontObj = new WavefrontBuildedObj(entryModel.getName(), is, isMtl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.modelLoadError = true;
        }
    }

    @Override
    public void setRenderState(PoseStack pose, IJBJointParameterHolder joint, int modelMeta, float tickPartial, LivingEntity entityMayBeNull, int renderPass) {
        // 1.12.2 MultiLayeredModel#setRenderState was an empty no-op.
    }

    @Override
    public boolean shouldRenderInPass(int modelMeta, int renderPass) {
        switch (this.renderType) {
            case RENDER_TYPE_ONLY_0:
                return renderPass == 0;
            case RENDER_TYPE_ONLY_1:
                return renderPass == 1;
            default:
                return renderPass <= 1;
        }
    }
}
