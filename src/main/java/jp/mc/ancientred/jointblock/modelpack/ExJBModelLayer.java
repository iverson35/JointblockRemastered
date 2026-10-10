package jp.mc.ancientred.jointblock.modelpack;

import jp.mc.ancientred.jointblock.model.obj.WavefrontBuildedObj;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One render layer of a pack model: a wavefront {@code .obj} plus the texture it draws with.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExJBModelLayer}. The 1.12.2 client fields
 * cached a GL display list and a raw GL texture id; 1.20.1 re-emits the vertices every frame, so the
 * layer only keeps the parsed model and the {@link ResourceLocation} the texture is registered
 * under.
 */
public class ExJBModelLayer {
    public int pass;
    public String modelFileName;
    public String textureFileName;

    @OnlyIn(Dist.CLIENT)
    public WavefrontBuildedObj wavefrontObj;
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation textureLocation;

    public ExJBModelLayer(int argPass, String argModelFileName, String argTextureFileName) {
        this.pass = argPass;
        this.modelFileName = argModelFileName;
        this.textureFileName = argTextureFileName;
    }
}
