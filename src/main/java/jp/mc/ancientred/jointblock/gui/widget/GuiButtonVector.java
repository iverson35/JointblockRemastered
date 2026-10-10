package jp.mc.ancientred.jointblock.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * 1.12.2 {@code GuiButtonVector}: a button whose clickable shape is a set of triangles.
 * <p>1.20.1 substitutions: the 1.12 body opened a {@code GL_TRIANGLES} immediate buffer with
 * {@code GlStateManager.disableBlend()} / {@code disableTexture2D()} and pushed the vertices with
 * {@code GlStateManager.color} picking the fill colour from {@code func_146114_a}. 1.20.1 has no
 * immediate-mode GUI buffer, so each triangle is emitted as a degenerate quad (last vertex repeated)
 * into {@link #TRIANGLE_TYPE} — a position/colour GUI render type whose transparency shard calls
 * {@link RenderSystem#disableBlend()} (the 1.12 state) and whose shader never samples a texture (the
 * 1.12 {@code disableTexture2D}) — with the same per-vertex colour and alpha 0.5, then flushed so it
 * stays below the label.  The hit test keeps the 1.12 angle-of-the-triangle maths, but 1.20.1
 * {@link net.minecraft.world.phys.Vec3} components are final, so the three scratch vectors became
 * plain local doubles (no allocation, identical arithmetic).
 */
@OnlyIn(Dist.CLIENT)
public class GuiButtonVector extends GuiButtonJB {
   /** 1.12.2's blend-disabled triangle batch: position/colour GUI quads drawn without blending. */
   private static final RenderType TRIANGLE_TYPE = RenderType.create(
      "jointblock_gui_triangles",
      DefaultVertexFormat.POSITION_COLOR,
      VertexFormat.Mode.QUADS,
      1536,
      false,
      false,
      RenderType.CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
         .setTransparencyState(new TransparencyStateShard("jointblock_no_transparency", RenderSystem::disableBlend, () -> {}))
         .setDepthTestState(new DepthTestStateShard("jointblock_lequal_depth_test", 515))
         .createCompositeState(false)
   );
   private float[][][] tris;
   private int bgNcolor;
   private int bgScolor;
   private int bgDcolor;

   public GuiButtonVector(int id, int xPos, int yPos, int width, int height, int ncolor, int scolor, int dcolor, float[][][] tris, Component str, Consumer<IJBGuiWidget> onPress) {
      super(id, xPos, yPos, width, height, str, onPress);
      this.tris = tris;
      this.bgNcolor = ncolor;
      this.bgScolor = scolor;
      this.bgDcolor = dcolor;
   }

   public GuiButtonVector(int id, int xPos, int yPos, int width, int height, int ncolor, int scolor, int dcolor, float[][][] tris, String str, Consumer<IJBGuiWidget> onPress) {
      this(id, xPos, yPos, width, height, ncolor, scolor, dcolor, tris, str == null ? Component.empty() : Component.literal(str), onPress);
   }

   public GuiButtonVector(int id, int xPos, int yPos, int width, int height, int ncolor, int scolor, int dcolor, float[][][] tris, Component str) {
      this(id, xPos, yPos, width, height, ncolor, scolor, dcolor, tris, str, (Consumer<IJBGuiWidget>)null);
   }

   public GuiButtonVector(int id, int xPos, int yPos, int width, int height, int ncolor, int scolor, int dcolor, float[][][] tris, String str) {
      this(id, xPos, yPos, width, height, ncolor, scolor, dcolor, tris, str, (Consumer<IJBGuiWidget>)null);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return super.mouseClicked(mouseX, mouseY, button) && this.isInTris((int)mouseX, (int)mouseY);
   }

   @Override
   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
         return;
      }

      Font font = Minecraft.getInstance().font;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.isHovered = this.isInTris(mouseX, mouseY);
      int k = this.getHoverState();
      float r;
      float g;
      float b;
      switch (k) {
         case 0:
            r = (float)(this.bgDcolor >> 16 & 255) / 255.0F;
            g = (float)(this.bgDcolor >> 8 & 255) / 255.0F;
            b = (float)(this.bgDcolor & 255) / 255.0F;
            break;
         case 2:
            r = (float)(this.bgScolor >> 16 & 255) / 255.0F;
            g = (float)(this.bgScolor >> 8 & 255) / 255.0F;
            b = (float)(this.bgScolor & 255) / 255.0F;
            break;
         case 1:
         default:
            r = (float)(this.bgNcolor >> 16 & 255) / 255.0F;
            g = (float)(this.bgNcolor >> 8 & 255) / 255.0F;
            b = (float)(this.bgNcolor & 255) / 255.0F;
      }

      Matrix4f pose = guiGraphics.pose().last().pose();
      VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(TRIANGLE_TYPE);

      for (int i = 0; i < this.tris.length; ++i) {
         float x0 = (float)this.getX() + this.tris[i][0][0];
         float y0 = (float)this.getY() + this.tris[i][0][1];
         float x1 = (float)this.getX() + this.tris[i][1][0];
         float y1 = (float)this.getY() + this.tris[i][1][1];
         float x2 = (float)this.getX() + this.tris[i][2][0];
         float y2 = (float)this.getY() + this.tris[i][2][1];
         buffer.vertex(pose, x0, y0, 0.0F).color(r, g, b, 0.5F).endVertex();
         buffer.vertex(pose, x1, y1, 0.0F).color(r, g, b, 0.5F).endVertex();
         buffer.vertex(pose, x2, y2, 0.0F).color(r, g, b, 0.5F).endVertex();
         buffer.vertex(pose, x2, y2, 0.0F).color(r, g, b, 0.5F).endVertex();
      }

      guiGraphics.flush();
      int l = 14737632;
      if (this.getMessage() != null) {
         if (this.packedFGColour != 0) {
            l = this.packedFGColour;
         } else if (!this.active) {
            l = 10526880;
         } else if (this.isHovered) {
            l = 16777120;
         }

         guiGraphics.drawCenteredString(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, l);
      }
   }

   /** 1.12.2 {@code GuiButton.func_146114_a}: 0 = disabled, 1 = normal, 2 = highlighted. */
   private int getHoverState() {
      if (!this.active) {
         return 0;
      }

      return this.isHovered ? 2 : 1;
   }

   private boolean isInTris(int mouseX, int mouseY) {
      for (int i = 0; i < this.tris.length; ++i) {
         double ax = (double)(mouseX - this.getX()) - (double)this.tris[i][0][0];
         double ay = (double)(mouseY - this.getY()) - (double)this.tris[i][0][1];
         double bx = (double)(mouseX - this.getX()) - (double)this.tris[i][1][0];
         double by = (double)(mouseY - this.getY()) - (double)this.tris[i][1][1];
         double cx = (double)(mouseX - this.getX()) - (double)this.tris[i][2][0];
         double cy = (double)(mouseY - this.getY()) - (double)this.tris[i][2][1];
         double am = (double)Mth.sqrt((float)(ax * ax + ay * ay));
         double bm = (double)Mth.sqrt((float)(bx * bx + by * by));
         double cm = (double)Mth.sqrt((float)(cx * cx + cy * cy));
         if (am < 1.0E-4) {
            ax = 0.0;
            ay = 0.0;
         } else {
            ax /= am;
            ay /= am;
         }

         if (bm < 1.0E-4) {
            bx = 0.0;
            by = 0.0;
         } else {
            bx /= bm;
            by /= bm;
         }

         if (cm < 1.0E-4) {
            cx = 0.0;
            cy = 0.0;
         } else {
            cx /= cm;
            cy /= cm;
         }

         double angles = 0.0;
         angles += Math.acos(Mth.clamp(ax * bx + ay * by, -1.0, 1.0));
         angles += Math.acos(Mth.clamp(bx * cx + by * cy, -1.0, 1.0));
         angles += Math.acos(Mth.clamp(cx * ax + cy * ay, -1.0, 1.0));
         if (Math.abs(angles - 6.283185307179586) < 0.01) {
            return true;
         }
      }

      return false;
   }
}
