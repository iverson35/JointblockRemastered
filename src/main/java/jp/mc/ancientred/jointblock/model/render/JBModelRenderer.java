package jp.mc.ancientred.jointblock.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * 1.20.1 replacement for the 1.12.2 {@code net.minecraft.client.model.ModelRenderer}.
 *
 * <p>The 1.12.2 joint models are plain {@code ModelBase} box models built at construction time and
 * driven per-frame from {@code TileEntityJoint}/{@code EntityJointed} state (rotation points,
 * rotation angles, visibility, per-part offsets). {@link ModelPart} cannot be constructed that way
 * - it is fed from a baked {@code LayerDefinition} - and it dropped the {@code offsetX/Y/Z},
 * {@code isHidden}, {@code childModels} and variable {@code textureSize} surface these models use.
 *
 * <p>So this keeps the 1.12 API shape verbatim (same method and field names, same 1/16 unit
 * convention) but stores the boxes as vanilla {@link CubeDefinition}s and renders through vanilla
 * {@link ModelPart}, which means the box UV unwrap, mirroring and grow inflation are vanilla's own
 * code rather than a reimplementation.
 *
 * <p>Unit convention: all coordinates are model units (1/16 block), exactly like 1.12.2. The pose
 * passed to {@link #render} must be in block units - rotation points are divided by 16 here and
 * {@link ModelPart} divides the box vertices by 16, so the two match; {@code offsetX/Y/Z} are applied
 * raw, as 1.12.2 {@code ModelRenderer} did.
 */
@OnlyIn(Dist.CLIENT)
public class JBModelRenderer {

   /** Model units -> blocks. 1.12.2 {@code ModelBase.render} was always given this as its scale. */
   public static final float UNIT = 0.0625F;

   private final int textureOffsetX;
   private final int textureOffsetY;
   private int textureWidth = 64;
   private int textureHeight = 32;

   /** Boxes recorded before they can be baked (mirror/textureSize are assigned after addBox in the 1.12 sources). */
   private final List<float[]> boxes = new ArrayList<>();
   private final List<JBModelRenderer> childModels = new ArrayList<>();

   private List<ModelPart.Cube> cubes;
   private ModelPart part;

   public float rotationPointX;
   public float rotationPointY;
   public float rotationPointZ;
   public float rotateAngleX;
   public float rotateAngleY;
   public float rotateAngleZ;
   public float offsetX;
   public float offsetY;
   public float offsetZ;
   public boolean mirror;
   public boolean showModel = true;
   public boolean isHidden;
   public String boxName;

   public JBModelRenderer(int texOffX, int texOffY) {
      this.textureOffsetX = texOffX;
      this.textureOffsetY = texOffY;
   }

   /** 1.12.2 {@code addBox(x, y, z, w, h, d)}. */
   public void addBox(float x, float y, float z, int width, int height, int depth) {
      this.addBox(x, y, z, width, height, depth, 0.0F);
   }

   /** 1.12.2 {@code addBox(x, y, z, w, h, d, delta)} - delta inflates the box on every axis. */
   public void addBox(float x, float y, float z, int width, int height, int depth, float delta) {
      this.boxes.add(new float[]{x, y, z, (float) width, (float) height, (float) depth, delta});
      this.invalidate();
   }

   /** 1.12.2 {@code setTextureSize(width, height)} - also carries the renderer's own texture size. */
   public void setTextureSize(int width, int height) {
      this.textureWidth = width;
      this.textureHeight = height;
      this.invalidate();
   }

   /** 1.12.2 {@code setRotationPoint(x, y, z)}. */
   public void setRotationPoint(float x, float y, float z) {
      this.rotationPointX = x;
      this.rotationPointY = y;
      this.rotationPointZ = z;
   }

   public void addChild(JBModelRenderer child) {
      this.childModels.add(child);
   }

   private void invalidate() {
      this.cubes = null;
      this.part = null;
   }

   private ModelPart part() {
      if (this.part == null) {
         CubeListBuilder builder = CubeListBuilder.create()
            .texOffs(this.textureOffsetX, this.textureOffsetY)
            .mirror(this.mirror);
         for (float[] box : this.boxes) {
            float delta = box[6];
            builder.addBox(box[0], box[1], box[2], box[3], box[4], box[5],
               delta == 0.0F ? CubeDeformation.NONE : new CubeDeformation(delta, delta, delta));
         }

         List<CubeDefinition> definitions = builder.getCubes();
         List<ModelPart.Cube> baked = new ArrayList<>(definitions.size());
         for (CubeDefinition definition : definitions) {
            baked.add(definition.bake(this.textureWidth, this.textureHeight));
         }

         this.cubes = Collections.unmodifiableList(baked);
         this.part = new ModelPart(this.cubes, Collections.emptyMap());
      }
      return this.part;
   }

   public void render(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay) {
      this.render(pose, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   /**
    * 1.12.2 {@code render(scale)}: the pose is the caller's block-unit transform, the scale half of
    * the original contract is expressed by the 1/16 translations below plus ModelPart's own 1/16.
    */
   public void render(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay,
                      float red, float green, float blue, float alpha) {
      if (this.isHidden || !this.showModel) {
         return;
      }

      pose.pushPose();
      // 1.12.2 ModelRenderer.render applies offsetX/Y/Z raw (block units), unlike the rotation point.
      pose.translate(this.offsetX, this.offsetY, this.offsetZ);
      pose.translate(this.rotationPointX * UNIT, this.rotationPointY * UNIT, this.rotationPointZ * UNIT);
      if (this.rotateAngleZ != 0.0F) {
         pose.mulPose(zRotation(this.rotateAngleZ));
      }
      if (this.rotateAngleY != 0.0F) {
         pose.mulPose(yRotation(this.rotateAngleY));
      }
      if (this.rotateAngleX != 0.0F) {
         pose.mulPose(xRotation(this.rotateAngleX));
      }

      this.part().render(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);

      // 1.12.2 renders childModels inside the parent's rotate/translate, before the reverse transform.
      for (JBModelRenderer child : this.childModels) {
         child.render(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);
      }

      pose.popPose();
   }

   private static Quaternionf xRotation(float radians) {
      return new Quaternionf().rotationX(radians);
   }

   private static Quaternionf yRotation(float radians) {
      return new Quaternionf().rotationY(radians);
   }

   private static Quaternionf zRotation(float radians) {
      return new Quaternionf().rotationZ(radians);
   }
}
