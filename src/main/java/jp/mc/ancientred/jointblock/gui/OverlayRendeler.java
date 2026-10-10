package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.JointPartsRenderable;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.JBBase64Util;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 OverlayRendeler ported to Forge 1.20.1.
 *
 * <p>Plain data holder for the per-frame indicator list ({@link #addIndicate},
 * {@link #addIndicateColor}, {@link #addIndicatePalette}, {@link #clearAllIndicates}) plus the
 * static HUD entry point {@link #render(GuiGraphics, float, int, int)}. {@code JBClientSetup}
 * ({@code onRegisterGuiOverlays}) wraps {@code render} in an {@code IGuiOverlay} and registers it
 * with {@code RegisterGuiOverlaysEvent} above {@code VanillaGuiOverlay.BOSS_EVENT_PROGRESS}
 * ({@code IGuiOverlay.render(ForgeGui, GuiGraphics, float partialTick, int screenWidth,
 * int screenHeight)} is the 1.20.1 substitute for the 1.12 {@code RenderGameOverlayEvent.Post} at
 * {@code ElementType.BOSSHEALTH}); this class does not register itself.
 *
 * <p>Drawing substitutions:
 * <ul>
 *   <li>{@code drawTexturedModalRect(x,y,u,v,w,h)} (the 1.12 {@code zLevel = -90} textured quad,
 *       sampling whatever texture was bound - {@code modj_jointblock:textures/gui/guijointblock.png}
 *       for the indicator borders and {@code minecraft:textures/gui/icons.png} for the status bars)
 *       becomes {@link GuiGraphics#blit(ResourceLocation, int, int, int, int, int, int)}.</li>
 *   <li>The window size that 1.12 read from {@code event.getResolution()} comes from the overlay's
 *       {@code screenWidth}/{@code screenHeight} (equivalently {@link GuiGraphics#guiWidth()} /
 *       {@link GuiGraphics#guiHeight()}).</li>
 *   <li>The 1.12 {@code GlStateManager.enableBlend/disableAlpha/color(1,1,1,1)/disableBlend} state is
 *       dropped: every {@link GuiGraphics} draw carries its own blend/alpha state and its own
 *       per-vertex colour, so there is no fixed-function state to set or restore.</li>
 *   <li>1.12 palette swatches ({@code Gui.drawRect}) become {@link GuiGraphics#fill}.</li>
 *   <li>The {@code JBConfig.debugGLListState} readout is not reproduced: it printed the
 *       {@code GLCompiledListManager} display-list counters and {@code World.MAX_ENTITY_RADIUS},
 *       both of which no longer exist in the 1.20.1 port (the display-list manager is a deleted
 *       1.12-only file), so there is no state left to display.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class OverlayRendeler {
   private static final ResourceLocation resource = ResourceLocation.tryBuild("jointblock", "textures/gui/guijointblock.png");
   /** The vanilla HUD sheet whose v=74..93 rows the 1.12 status bars sampled (see {@link #render}). */
   private static final ResourceLocation ICONS = ResourceLocation.tryBuild("minecraft", "textures/gui/icons.png");
   private static final String[] PALETTE_CHARS = new String[]{"1", "2", "3", "4", "5", "6", "B"};
   /** 1.12 field: window centre offset applied to every indicator row. */
   public static int indicatorStringCenterPad = 0;
   private static int[] indicateColor = new int[1];
   private static int[] indicatePalette = new int[7];
   /** 1.12 field: extra rows reserved by the palette indicators. */
   public static int additionalIndicatorLines = 0;
   public static ArrayList<String> indicatorTitles = new ArrayList<String>();
   public static ArrayList<Object> indicatorValues = new ArrayList<Object>();

   /**
    * 1.12 {@code renderOverlay(RenderGameOverlayEvent.Post)}; the 1.20.1 equivalent is the
    * {@code IGuiOverlay} registered by {@code JBClientSetup#onRegisterGuiOverlays}. Keeps the
    * dispatch order and the seat-part guard of the original (the target sight overlay dereferences
    * the ridden seat part unconditionally).
    */
   public static void render(GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      Entity ridingEntity = mc.player == null ? null : mc.player.getVehicle();
      if (ridingEntity instanceof EntitySeatPart) {
         EntitySeatPart seatPart = (EntitySeatPart)ridingEntity;
         if (seatPart.entityJointed == null) {
            return;
         }

         Font font = mc.font;
         int top = 12 + JBConfig.unitStatusBarOffsetY;
         if (mc.gui != null && mc.gui.getChat() != null && !mc.gui.getChat().getRecentChat().isEmpty()) {
            top += 30;
         }

         int barWidth = 142;
         int left = 15 + JBConfig.unitStatusBarOffsetX;
         float health = seatPart.entityJointed.getHealth();
         float energy = seatPart.entityJointed.getEnergy();
         float healthMax = seatPart.entityJointed.getMaxHealth();
         float energyMax = seatPart.entityJointed.getMaxEnergy();
         float healthScale = health / healthMax;
         float energyScale = energy / energyMax;
         int healthlen = (int)(healthScale * (float)barWidth);
         int energylen = (int)(energyScale * (float)barWidth);
         // 1.12.2 drew these four quads with drawTexturedModalRect *without* binding a texture,
         // so they sampled whatever the vanilla HUD had bound last - minecraft:textures/gui/
         // icons.png, whose v=74..93 rows hold the two boss-bar sprites; that is where the bar look
         // came from. The port passed guijointblock.png to blit() instead, and that texture is
         // opaque black in exactly this region, so both bars rendered black regardless of the
         // values. icons.png still carries the identical sprites in 1.20.1 (verified pixel for
         // pixel against the 1.12.2 sheet), so blit the same rectangles the 1.12 code sampled.
         // 1.12 guarded these quads with GlStateManager.color(1,1,1,1); the 1.20.1 equivalent is
         // clearing the GuiGraphics tint, since the overlays drawn before this one (the boss bar
         // among them) leave a non-white shader colour behind.
         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         guiGraphics.blit(ICONS, left, top, 10, 84, barWidth, 5);
         guiGraphics.blit(ICONS, left, top + 8, 10, 74, barWidth, 5);
         if (healthlen > 0) {
            guiGraphics.blit(ICONS, left, top, 10, 89, healthlen, 5);
            guiGraphics.blit(ICONS, left, top + 8, 10, 79, energylen, 5);
         }

         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

         int heightFix = 2 - font.lineHeight / 2;
         StringBuilder strBuf = new StringBuilder();
         strBuf.setLength(0);
         strBuf.append((int)health).append('/').append((int)healthMax);
         guiGraphics.drawString(font, strBuf.toString(), left + barWidth + 4, top + heightFix, -1, false);
         strBuf.setLength(0);
         strBuf.append((int)energy).append('/').append((int)energyMax);
         guiGraphics.drawString(font, strBuf.toString(), left + barWidth + 4, top + 8 + heightFix, -1, false);
         strBuf.setLength(0);
         if (AssembleFlagsUtil.isInfiniteFuel(seatPart.entityJointed.assembleFlags)) {
            strBuf.append("EP:INF");
         } else {
            strBuf.append(String.format("EP:%.1f", seatPart.entityJointed.getEFuelPool()));
         }

         guiGraphics.drawString(font, strBuf.toString(), left, top + 16 + heightFix, -1, false);
         String nameS;
         if (seatPart.entityJointed.isAirBorneHovering()) {
            nameS = seatPart.entityJointed.unitAbility.isSubmarine() ? "[S]" : "[H]";
            guiGraphics.drawString(font, nameS, left + barWidth - font.width(nameS), top + 16 + heightFix, -1, false);
         }

         nameS = seatPart.entityJointed.getJointEntityName();
         if (seatPart.entityJointed.iffTagId != 0L) {
            if (seatPart.entityJointed.iffTagString == null) {
               seatPart.entityJointed.iffTagString = "[" + JBBase64Util.longToBasd64(seatPart.entityJointed.iffTagId) + "]";
            }

            guiGraphics.drawString(font, seatPart.entityJointed.iffTagString + nameS, left, top - 10, 16777215);
         } else {
            guiGraphics.drawString(font, nameS, left, top - 10, 16777215);
         }

         OverlayTargetSightRenderer.renderTargetSights(guiGraphics, mc, partialTick);
      }

      OverlayRingDebugRendeler.renderRingDebugWindow(guiGraphics, mc, screenWidth, screenHeight);
      renderInidicateWindow(guiGraphics, mc, screenWidth, screenHeight);
   }

   public static void addIndicateColor(String title, int color) {
      if (JBConfig.indicatorWindowEnabled) {
         indicatorTitles.add(title);
         indicateColor[0] = JBCarvingDataUtil.boFixedToUnFixed(color);
         indicatorValues.add(indicateColor);
      }

   }

   public static void addIndicatePalette(String title, int[] colors) {
      if (JBConfig.indicatorWindowEnabled) {
         indicatorTitles.add(title);

         for(int i = 0; i < indicatePalette.length; ++i) {
            indicatePalette[i] = JBCarvingDataUtil.boFixedToUnFixed(colors[i + 1]);
         }

         indicatorValues.add(indicatePalette);
         ++additionalIndicatorLines;
      }

   }

   public static void addIndicate(String title, String value) {
      if (JBConfig.indicatorWindowEnabled) {
         indicatorTitles.add(title);
         indicatorValues.add(value);
      }

   }

   public static void clearAllIndicates(int stringCenterPad) {
      if (JBConfig.indicatorWindowEnabled) {
         additionalIndicatorLines = 0;
         indicatorTitles.clear();
         indicatorValues.clear();
         indicatorStringCenterPad = stringCenterPad;
      }

   }

   /**
    * 1.12 {@code renderInidicateWindow}: the selected joint indicator is collected first, then the
    * window frame sprites and every title/value row are drawn, and the collected indicators are
    * consumed/cleared once per frame.
    */
   private static void renderInidicateWindow(GuiGraphics guiGraphics, Minecraft mc, int screenWidth, int screenHeight) {
      if (JBConfig.indicatorWindowEnabled) {
         addSelectedJointIndicator();
         int indicatorsCnt = indicatorTitles.size();
         if (indicatorsCnt > 0) {
            if (indicatorsCnt == indicatorValues.size()) {
               int midddleBlockCols = 6;
               int center;
               if (JBConfig.indicatorWindowPosition == 0) {
                  center = 8 + midddleBlockCols / 2 * 16;
               } else if (JBConfig.indicatorWindowPosition == 2) {
                  center = screenWidth - 8 - midddleBlockCols / 2 * 16;
               } else {
                  center = screenWidth / 2;
               }

               int screenBot = screenHeight;
               int rowHeight = 10;
               int bot = screenBot - 55;
               int top = bot - (indicatorsCnt + additionalIndicatorLines) * rowHeight;
               int bordersTop = top - 8;
               int bordersLeft = center - midddleBlockCols / 2 * 16 - 8;
               drawTexturedModalRect(guiGraphics, bordersLeft, bordersTop, 0, 224, 8, 8);

               int pad;
               for(pad = 0; pad < midddleBlockCols; ++pad) {
                  drawTexturedModalRect(guiGraphics, bordersLeft + 8 + pad * 16, bordersTop, 8, 224, 16, 8);
               }

               drawTexturedModalRect(guiGraphics, bordersLeft + 8 + midddleBlockCols * 16, bordersTop, 24, 224, 8, 8);
               bordersTop += 8;
               pad = 0;

               label85:
               while(true) {
                  int i;
                  if (pad >= indicatorsCnt + additionalIndicatorLines) {
                     drawTexturedModalRect(guiGraphics, bordersLeft, bordersTop, 0, 248, 8, 8);

                     for(pad = 0; pad < midddleBlockCols; ++pad) {
                        drawTexturedModalRect(guiGraphics, bordersLeft + 8 + pad * 16, bordersTop, 8, 248, 16, 8);
                     }

                     drawTexturedModalRect(guiGraphics, bordersLeft + 8 + midddleBlockCols * 16, bordersTop, 24, 248, 8, 8);
                     center += indicatorStringCenterPad;
                     pad = 0;
                     i = 0;

                     while(true) {
                        if (i >= indicatorsCnt) {
                           break label85;
                        }

                        String str = (String)indicatorTitles.get(i);
                        int w = mc.font.width(str);
                        guiGraphics.drawString(mc.font, str, center - w, top + (pad + i) * rowHeight, 16777215);
                        Object obj = indicatorValues.get(i);
                        if (obj instanceof String) {
                           str = (String)obj;
                           guiGraphics.drawString(mc.font, str, center, top + (pad + i) * rowHeight, 16777215);
                        } else if (obj instanceof int[]) {
                           int[] colors = (int[])((int[])obj);

                           for(int c = 0; c < colors.length; ++c) {
                              int xShift = c == 6 ? -8 : c % 3 * 8;
                              int x1 = center + xShift;
                              int yShift = Math.min(c / 3, 1) * 8;
                              int y1 = top + (pad + i) * rowHeight + yShift;
                              guiGraphics.fill(x1, y1, x1 + 8, y1 + 8, colors[c]);
                              if (colors.length > 1) {
                                 int wc = mc.font.width(PALETTE_CHARS[c]);
                                 guiGraphics.drawString(mc.font, PALETTE_CHARS[c], x1 + 8 - wc, y1, -1, true);
                              }
                           }

                           if (colors.length > 1) {
                              ++pad;
                           }
                        }

                        ++i;
                     }
                  }

                  drawTexturedModalRect(guiGraphics, bordersLeft, bordersTop, 0, 232, 8, rowHeight);

                  for(i = 0; i < midddleBlockCols; ++i) {
                     drawTexturedModalRect(guiGraphics, bordersLeft + 8 + i * 16, bordersTop, 8, 232, 16, rowHeight);
                  }

                  drawTexturedModalRect(guiGraphics, bordersLeft + 8 + midddleBlockCols * 16, bordersTop, 24, 232, 8, rowHeight);
                  bordersTop += rowHeight;
                  ++pad;
               }
            }

            clearAllIndicates(0);
         }

      }
   }

   private static void addSelectedJointIndicator() {
      if (StaticClientTuningStates.targetSelected.isSetectionEnabled) {
         EntityJointed entityJointed = StaticClientTuningStates.targetSelected.rayTracedHitEntity;
         JointParts joint = StaticClientTuningStates.targetSelected.rayTracedHitJoint;
         if (joint instanceof JointPartsRenderable) {
            clearAllIndicates(-10);
            ((JointPartsRenderable)joint).addOverlayIndicator();
         }
      }

   }

   /**
    * 1.12 {@code drawTexturedModalRect(int,int,int,int,int,int)}: the {@code zLevel = -90} textured
    * quad of {@code guijointblock.png} (256x256), now a {@link GuiGraphics#blit} of the same
    * {@code (u,v,width,height)} region at the same screen position.
    */
   public static void drawTexturedModalRect(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height) {
      guiGraphics.blit(resource, x, y, u, v, width, height);
   }
}
