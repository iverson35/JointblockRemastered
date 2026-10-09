package jp.mc.ancientred.jointblock.carving;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeMeshBuilder;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingUtil;
import jp.mc.ancientred.jointblock.carving.pattern.PatternArgsBBSet;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.gui.OverlayRendeler;
import jp.mc.ancientred.jointblock.item.ItemPaintGun;
import jp.mc.ancientred.jointblock.item.ItemPatternCutter;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

public class JBCarvingClientUtil {
   private static final int DATA_BITLEN = 4;
   private static long lastSentTimeMillis;
   private static final long EDIT_SEND_INTERVAL = 100L;
   /** Visible-face mask of {@link #renderBoxesCarvedModelGreedy}: grown on demand, reused per call. */
   private static boolean[] greedyMask = new boolean[0];
   /** Palette slot per {@link #greedyMask} cell, so only equal-slot faces merge. */
   private static byte[] greedySlot = new byte[0];

   public static void editCarveDataOnClient(Player player, TileEntityJoint te, boolean leftClick) {
      long now = System.currentTimeMillis();
      if (now - lastSentTimeMillis > 100L) {
         Level level = player.level();
         Vec3 vecPos = player.getEyePosition(1.0F);
         Vec3 vecLook = player.getViewVector(1.0F);
         double reach = 6.0;
         Vec3 velLookRay = vecPos.add(vecLook.x * reach, vecLook.y * reach, vecLook.z * reach);
         BlockPos tePos = te.getBlockPos();
         int teX = tePos.getX();
         int teY = tePos.getY();
         int teZ = tePos.getZ();
         int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
         switch (carvingType) {
            case 2:
            case 4:
            default:
               CarvingRayTraceResult res = collisionRayTrace(te.getJointFacing(), te.carvingData, level, teX, teY, teZ, vecPos, velLookRay, detectSubBoundHitType(player));
               if (res != null) {
                  JBPacketSender.sendCarvingActionToServer(teX, teY, teZ, res.boxIndex, res.getSubBoundsArg(), res.hitSide, leftClick);
                  lastSentTimeMillis = now;
               }
         }
      }

   }

   private static int detectSubBoundHitType(Player player) {
      int hitType = -1;
      ItemStack currentStack;
      if (player != null && (currentStack = CompatHelper.getCurrentEquippedItem(player)) != null) {
         if (ItemPatternCutter.isPatternArgsCutter(currentStack)) {
            hitType = 0;
         } else if (ItemPatternCutter.isPatternSideFaceCutter(currentStack)) {
            hitType = 1;
         }

         return hitType;
      } else {
         return hitType;
      }
   }

   private static ItemStack getPlayerHoldingPaintGunStack(Player player) {
      ItemStack currentStack;
      if (player != null && (currentStack = CompatHelper.getCurrentEquippedItem(player)) != null) {
         return currentStack.getItem() instanceof ItemPaintGun ? currentStack : null;
      } else {
         return null;
      }
   }

   private static CarvingRayTraceResult collisionRayTrace(int facing, int[] carvingData, Level level, int posX, int posY, int posZ, Vec3 vecPos, Vec3 velLookRay, int hitType) {
      int type = JBCarvingDataUtil.getCarvingType(carvingData);
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return null;
      } else {
         int divCountPow = divideCount * divideCount;
         AABB aabbWork = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
         double unitd = 1.0 / (double)divideCount;
         vecPos = vecPos.add((double)(-posX), (double)(-posY), (double)(-posZ));
         velLookRay = velLookRay.add((double)(-posX), (double)(-posY), (double)(-posZ));
         double rotateCenterX = 0.5;
         double rotateCenterY = 0.5;
         double rotateCenterZ = 0.5;
         switch (facing) {
            case 2:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
            case 3:
            default:
               break;
            case 4:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               break;
            case 5:
               JBRotationUtil.rotateVec(vecPos, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
               JBRotationUtil.rotateVec(velLookRay, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
         }

         CarvingRayTraceResult result = null;
         double disanceMin = Double.MAX_VALUE;
         int hitValue = 0;

         int y;
         for(y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;
            aabbWork.minY = (double)y * unitd;
            aabbWork.maxY = aabbWork.minY + unitd;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;
               aabbWork.minZ = (double)z * unitd;
               aabbWork.maxZ = aabbWork.minZ + unitd;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndexCnt = x + yPzCalc;
                  aabbWork.minX = (double)x * unitd;
                  aabbWork.maxX = aabbWork.minX + unitd;
                  boolean doRayTraceSubBounds = false;
                  int mod;
                  int dat;
                  switch (type) {
                     case 2:
                        mod = (boxIndexCnt & 3) << 3;
                        dat = carvingData[boxIndexCnt >> 2] >> mod & 240;
                        if (dat == 0) {
                           continue;
                        }

                        hitValue = carvingData[boxIndexCnt >> 2] >> mod & 15;
                        doRayTraceSubBounds = true;
                        break;
                     case 4:
                        mod = (boxIndexCnt & 3) << 3;
                        dat = carvingData[boxIndexCnt >> 2] >> mod & 32;
                        if (dat != 0) {
                           continue;
                        }

                        hitValue = carvingData[boxIndexCnt >> 2] >> mod & 31;
                        break;
                     default:
                        mod = (boxIndexCnt & 7) << 2;
                        dat = carvingData[boxIndexCnt >> 3] >>> mod & 15;
                        if (dat == 0) {
                           continue;
                        }
                  }

                  Vec3 vec32 = getIntermediateWithXValue(vecPos, velLookRay, aabbWork.minX);
                  Vec3 vec33 = getIntermediateWithXValue(vecPos, velLookRay, aabbWork.maxX);
                  Vec3 vec34 = getIntermediateWithYValue(vecPos, velLookRay, aabbWork.minY);
                  Vec3 vec35 = getIntermediateWithYValue(vecPos, velLookRay, aabbWork.maxY);
                  Vec3 vec36 = getIntermediateWithZValue(vecPos, velLookRay, aabbWork.minZ);
                  Vec3 vec37 = getIntermediateWithZValue(vecPos, velLookRay, aabbWork.maxZ);
                  if (!isVecInsideYZBounds(aabbWork, vec32)) {
                     vec32 = null;
                  }

                  if (!isVecInsideYZBounds(aabbWork, vec33)) {
                     vec33 = null;
                  }

                  if (!isVecInsideXZBounds(aabbWork, vec34)) {
                     vec34 = null;
                  }

                  if (!isVecInsideXZBounds(aabbWork, vec35)) {
                     vec35 = null;
                  }

                  if (!isVecInsideXYBounds(aabbWork, vec36)) {
                     vec36 = null;
                  }

                  if (!isVecInsideXYBounds(aabbWork, vec37)) {
                     vec37 = null;
                  }

                  Vec3 vec38 = null;
                  if (vec32 != null && (vec38 == null || vecPos.distanceToSqr(vec32) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec32;
                  }

                  if (vec33 != null && (vec38 == null || vecPos.distanceToSqr(vec33) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec33;
                  }

                  if (vec34 != null && (vec38 == null || vecPos.distanceToSqr(vec34) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec34;
                  }

                  if (vec35 != null && (vec38 == null || vecPos.distanceToSqr(vec35) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec35;
                  }

                  if (vec36 != null && (vec38 == null || vecPos.distanceToSqr(vec36) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec36;
                  }

                  if (vec37 != null && (vec38 == null || vecPos.distanceToSqr(vec37) < vecPos.distanceToSqr(vec38))) {
                     vec38 = vec37;
                  }

                  if (vec38 != null) {
                     byte b0 = -1;
                     if (vec38 == vec32) {
                        b0 = 4;
                     }

                     if (vec38 == vec33) {
                        b0 = 5;
                     }

                     if (vec38 == vec34) {
                        b0 = 0;
                     }

                     if (vec38 == vec35) {
                        b0 = 1;
                     }

                     if (vec38 == vec36) {
                        b0 = 2;
                     }

                     if (vec38 == vec37) {
                        b0 = 3;
                     }

                     double distance = vec38.distanceTo(vecPos);
                     if (distance < disanceMin) {
                        disanceMin = distance;
                        if (result == null) {
                           result = new CarvingRayTraceResult();
                        }

                        JBCommonUtil.copyBBTo(aabbWork, result.aabb);
                        result.boxIndex = boxIndexCnt;
                        result.hitSide = b0;
                        result.commonHitValue = hitValue;
                        result.x = x;
                        result.y = y;
                        result.z = z;
                        result.rayTraceSubBounds = doRayTraceSubBounds;
                     }
                  }
               }
            }
         }

         if (result != null && result.rayTraceSubBounds) {
            JBPatternCarvingClientUtil.rayTracePatternSubBounds(carvingData, result, vecPos, velLookRay, hitType, aabbWork);
         }

         if (result != null) {
            if (result.aabb != null) {
               switch (facing) {
                  case 2:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
                  case 3:
                  default:
                     break;
                  case 4:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
                     break;
                  case 5:
                     JBRotationUtil.rotateAABBAt(result.aabb, rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
               }
            }

            if (result.aabbSubBound != null && result.aabbSubBound.bbAry != null) {
               for(y = 0; y < result.aabbSubBound.bbAry.length; ++y) {
                  switch (facing) {
                     case 2:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to2);
                     case 3:
                     default:
                        break;
                     case 4:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to4);
                        break;
                     case 5:
                        JBRotationUtil.rotateAABBAt(result.aabbSubBound.bbAry[y], rotateCenterX, rotateCenterY, rotateCenterZ, JBRotationUtil.matirxRoat3to5);
                  }
               }
            }
         }

         return result;
      }
   }

   public static boolean isVecInsideYZBounds(AABB aabb, Vec3 p_149654_1_) {
      return p_149654_1_ == null ? false : p_149654_1_.y >= aabb.minY && p_149654_1_.y <= aabb.maxY && p_149654_1_.z >= aabb.minZ && p_149654_1_.z <= aabb.maxZ;
   }

   public static boolean isVecInsideXZBounds(AABB aabb, Vec3 p_149687_1_) {
      return p_149687_1_ == null ? false : p_149687_1_.x >= aabb.minX && p_149687_1_.x <= aabb.maxX && p_149687_1_.z >= aabb.minZ && p_149687_1_.z <= aabb.maxZ;
   }

   public static boolean isVecInsideXYBounds(AABB aabb, Vec3 p_149661_1_) {
      return p_149661_1_ == null ? false : p_149661_1_.x >= aabb.minX && p_149661_1_.x <= aabb.maxX && p_149661_1_.y >= aabb.minY && p_149661_1_.y <= aabb.maxY;
   }

   /**
    * 1.12.2 {@code Vec3#getIntermediateWithXValue} - the helper {@code AxisAlignedBB#calculateIntercept}
    * used, dropped in 1.20.1. {@code start} is the 1.12.2 receiver and {@code end} its argument;
    * {@code null} when the ray is parallel to the plane or the crossing lies outside the segment.
    */
   public static Vec3 getIntermediateWithXValue(Vec3 start, Vec3 end, double x) {
      double dX = end.x - start.x;
      double dY = end.y - start.y;
      double dZ = end.z - start.z;
      if (dX * dX < 1.0E-7) {
         return null;
      } else {
         double s = (x - start.x) / dX;
         return s >= 0.0 && s <= 1.0 ? new Vec3(start.x + dX * s, start.y + dY * s, start.z + dZ * s) : null;
      }
   }

   /** 1.12.2 {@code Vec3#getIntermediateWithYValue}; see {@link #getIntermediateWithXValue}. */
   public static Vec3 getIntermediateWithYValue(Vec3 start, Vec3 end, double y) {
      double dX = end.x - start.x;
      double dY = end.y - start.y;
      double dZ = end.z - start.z;
      if (dY * dY < 1.0E-7) {
         return null;
      } else {
         double s = (y - start.y) / dY;
         return s >= 0.0 && s <= 1.0 ? new Vec3(start.x + dX * s, start.y + dY * s, start.z + dZ * s) : null;
      }
   }

   /** 1.12.2 {@code Vec3#getIntermediateWithZValue}; see {@link #getIntermediateWithXValue}. */
   public static Vec3 getIntermediateWithZValue(Vec3 start, Vec3 end, double z) {
      double dX = end.x - start.x;
      double dY = end.y - start.y;
      double dZ = end.z - start.z;
      if (dZ * dZ < 1.0E-7) {
         return null;
      } else {
         double s = (z - start.z) / dZ;
         return s >= 0.0 && s <= 1.0 ? new Vec3(start.x + dX * s, start.y + dY * s, start.z + dZ * s) : null;
      }
   }

   /**
    * 1.12.2 {@code drawJBCarvedBoundingBox}, hang of the {@code DrawBlockHighlightEvent} path.
    *
    * <p>The event type became {@link RenderHighlightEvent.Block} (the 1.20.1 block-highlight
    * event); its {@code PoseStack}/{@code MultiBufferSource} replace the implicit GL state, and the
    * 1.12.2 {@code event.getPlayer()} is read from the client. Because the highlight pose is
    * camera-relative in 1.20.1, the camera position ({@code Camera.getPosition()}) replaces the
    * 1.12.2 "player last-tick position interpolated by partialTick" that the box offsets used;
    * see {@link #drawBoxesBB}.
    */
   public static void drawJBCarvedBoundingBox(RenderHighlightEvent.Block event, TileEntityJoint te) {
      Player player = Minecraft.getInstance().player;
      if (player == null) {
         return;
      }

      Level level = player.level();
      float partialTick = event.getPartialTick();
      Vec3 vecPos = player.getEyePosition(partialTick);
      Vec3 vecLook = player.getViewVector(partialTick);
      double reach = 6.0;
      Vec3 velLookRay = vecPos.add(vecLook.x * reach, vecLook.y * reach, vecLook.z * reach);
      int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
      BlockPos tePos = te.getBlockPos();
      int teX = tePos.getX();
      int teY = tePos.getY();
      int teZ = tePos.getZ();
      CarvingRayTraceResult res = collisionRayTrace(te.getJointFacing(), te.carvingData, level, teX, teY, teZ, vecPos, velLookRay, detectSubBoundHitType(player));
      if (res != null) {
         res.aabb = JBCommonUtil.offsetBB(res.aabb, (double)teX, (double)teY, (double)teZ);
         if (res.aabbSubBound != null) {
            res.aabbSubBound.offsetAll(teX, teY, teZ);
         }

         boolean enablePGunIndicate = true;
         OverlayRendeler.clearAllIndicates(5);
         if (carvingType == 4) {
            drawBoxesBB(event, player, partialTick, res.aabb, res.aabbSubBound, true);
            OverlayRendeler.addIndicate("Type:", "M-Cube");
            OverlayRendeler.addIndicate("Density:", String.valueOf(res.commonHitValue));
            enablePGunIndicate = false;
         } else if (carvingType == 2) {
            drawBoxesBB(event, player, partialTick, res.aabb, res.aabbSubBound, false);
            OverlayRendeler.addIndicate("Type:", "Pattern");
            int valuePattern = JBCarvingDataUtil.getPatternValueFromTable(te.carvingData, res.commonHitValue);
            OverlayRendeler.addIndicate("Pattern:", String.valueOf(JBCarvingDataUtil.getPatternShortName(valuePattern)));
            if (JBConfig.enableDebugIndicates) {
               OverlayRendeler.addIndicate("PtnSide:", String.valueOf(JBPatternCarvingUtil.getPatternSide(valuePattern)));
               OverlayRendeler.addIndicate("PtnFace:", String.valueOf(JBPatternCarvingUtil.getPatternFace(valuePattern)));
               OverlayRendeler.addIndicate("PtnArgs:", String.valueOf(JBPatternCarvingUtil.getPatternArgs(valuePattern)));
               OverlayRendeler.addIndicate("PtblIdx:", String.valueOf(res.commonHitValue));
               OverlayRendeler.addIndicate("PtblUsd:", String.valueOf(JBCarvingDataUtil.getUsedPatternTableNum(te.carvingData)));
            }
         } else {
            drawBoxesBB(event, player, partialTick, res.aabb, res.aabbSubBound, false);
            OverlayRendeler.addIndicate("Type:", "Box");
         }

         if (enablePGunIndicate) {
            ItemStack pGunStack = getPlayerHoldingPaintGunStack(player);
            if (!CompatHelper.isStackEmptyOrNull(pGunStack)) {
               int paletteColorIndex = ItemPaintGun.getColorNumFromStack(pGunStack);
               int paletteIndex = JBCarvingDataUtil.getPaletteIndex(te.carvingData);
               int[] paletteData = null;
               if (te.boFixedPaletteDataList != null) {
                  paletteData = te.boFixedPaletteDataList[paletteIndex];
               }

               if (paletteData == null) {
                  paletteData = TileEntityJointCore.BOFIXED_DEFAULT_PALETTE_DATA[paletteIndex];
               }

               if (paletteData != null) {
                  OverlayRendeler.addIndicateColor("PGunCol:", paletteData[paletteColorIndex]);
                  OverlayRendeler.addIndicatePalette("Palette:", paletteData);
               }
            }
         }

         if (JBConfig.enableDebugIndicates) {
            OverlayRendeler.addIndicate("X:", String.valueOf(res.x));
            OverlayRendeler.addIndicate("Y:", String.valueOf(res.y));
            OverlayRendeler.addIndicate("Z:", String.valueOf(res.z));
            OverlayRendeler.addIndicate("BlockFace:", String.valueOf(te.getJointFacing()));
         }
      }

   }

   /**
    * Draws the carved hit box and, optionally, the "additional" cross lines and the pattern/m-cube
    * sub bounds.
    *
    * <p>1.12.2 ran this with hand-managed blend/line-width/no-depth state and a white-texture bind,
    * then called {@code RenderGlobal.drawSelectionBoundingBox} (a {@code GL_LINES} outline). In
    * 1.20.1 the state comes from {@link RenderType#lines()} and the outline from
    * {@link LevelRenderer#renderLineBox}; the {@code disableDepth()} used for the sub bounds has no
    * per-draw switch any more, so those outlines now depth-test like the main box.
    */
   private static void drawBoxesBB(RenderHighlightEvent.Block event, Player player, float partialTick, AABB drawBB, PatternArgsBBSet drawSubBBAry, boolean addLine) {
      PoseStack pose = event.getPoseStack();
      VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
      float f1 = 0.002F;
      Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      AABB expanded = JBCommonUtil.offsetBB(drawBB.inflate((double)f1, (double)f1, (double)f1), -camPos.x, -camPos.y, -camPos.z);
      LevelRenderer.renderLineBox(pose, consumer, expanded, 0.0F, 0.0F, 0.0F, 0.4F);
      if (addLine) {
         drawAdditionalBoundingBoxLines(pose, consumer, expanded);
      }

      if (drawSubBBAry != null && drawSubBBAry.bbAry != null) {
         for(int i = 0; i < drawSubBBAry.bbAry.length; ++i) {
            AABB subExpanded = JBCommonUtil.offsetBB(drawSubBBAry.bbAry[i].inflate((double)f1, (double)f1, (double)f1), -camPos.x, -camPos.y, -camPos.z);
            drawOutlinedBoundingBox(pose, consumer, subExpanded, drawSubBBAry.bbAry[i].bbColor, 255);
         }
      }

   }

   /** 1.12.2 {@code drawOutlinedBoundingBox} ({@code RenderGlobal.drawSelectionBoundingBox}). */
   public static void drawOutlinedBoundingBox(PoseStack pose, VertexConsumer consumer, AABB aabb, int color, int intAlpha) {
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      float alpha = (float)(intAlpha & 255) / 255.0F;
      LevelRenderer.renderLineBox(pose, consumer, aabb, red, green, blue, alpha);
   }

   /** 1.12.2 {@code drawAdditionalBoundingBoxLines}: the three 4x "cross" lines through the box. */
   public static void drawAdditionalBoundingBoxLines(PoseStack pose, VertexConsumer consumer, AABB aabb) {
      Matrix4f matrix = pose.last().pose();
      double lenX = aabb.maxX - aabb.minX;
      double lenY = aabb.maxY - aabb.minY;
      double lenZ = aabb.maxZ - aabb.minZ;
      double cX = (aabb.maxX + aabb.minX) / 2.0;
      double cY = (aabb.maxY + aabb.minY) / 2.0;
      double cZ = (aabb.maxZ + aabb.minZ) / 2.0;
      double exMinX = cX - lenX * 3.0;
      double exMaxX = cX + lenX * 3.0;
      double exMinY = cY - lenY * 3.0;
      double exMaxY = cY + lenY * 3.0;
      double exMinZ = cZ - lenZ * 3.0;
      double exMaxZ = cZ + lenZ * 3.0;
      line(consumer, matrix, exMinX, cY, cZ, exMaxX, cY, cZ, 1.0F, 0.0F, 0.0F);
      line(consumer, matrix, cX, exMinY, cZ, cX, exMaxY, cZ, 0.0F, 1.0F, 0.0F);
      line(consumer, matrix, cX, cY, exMinZ, cX, cY, exMaxZ, 0.0F, 0.0F, 1.0F);
   }

   /** One white {@link RenderType#lines()} segment; the normal selects the screen-space line axis. */
   private static void line(VertexConsumer consumer, Matrix4f matrix, double x0, double y0, double z0, double x1, double y1, double z1, float nx, float ny, float nz) {
      consumer.vertex(matrix, (float)x0, (float)y0, (float)z0).color(1.0F, 1.0F, 1.0F, 1.0F).normal(nx, ny, nz).endVertex();
      consumer.vertex(matrix, (float)x1, (float)y1, (float)z1).color(1.0F, 1.0F, 1.0F, 1.0F).normal(nx, ny, nz).endVertex();
   }

   /**
    * 1.12.2 standard item lighting, evaluated on the CPU.
    *
    * <p>{@code RenderHelper.enableStandardItemLighting} (on for the whole entity pass in 1.12.2 -
    * the mod's own code disables it around the unlit helpers) lit the carved quads through
    * {@code POSITION_COLOR_NORMAL} vertices, so every face got the light-model ambient 0.4 plus the
    * two 0.6 diffuse lights aimed at (0.2, 1, -0.7) and (-0.2, 1, 0.7). GL clamped the sum to 1;
    * that clamp is the "top bright, bottom dark" shading the 1.12.2 machines had.
    */
   public static float faceLight(float nx, float ny, float nz) {
      float len = (float)Math.sqrt((double)(nx * nx + ny * ny + nz * nz));
      if (len > 1.0E-6F) {
         nx /= len;
         ny /= len;
         nz /= len;
      }

      float d0 = nx * LIGHT0_X + ny * LIGHT0_Y + nz * LIGHT0_Z;
      float d1 = nx * LIGHT1_X + ny * LIGHT1_Y + nz * LIGHT1_Z;
      float light = LIGHT_MODEL_AMBIENT;
      if (d0 > 0.0F) {
         light += LIGHT_DIFFUSE * d0;
      }

      if (d1 > 0.0F) {
         light += LIGHT_DIFFUSE * d1;
      }

      return light > 1.0F ? 1.0F : light;
   }

   private static final float LIGHT_MODEL_AMBIENT = 0.4F;
   private static final float LIGHT_DIFFUSE = 0.6F;
   private static final float LIGHT0_X = 0.1616904F;
   private static final float LIGHT0_Y = 0.8084521F;
   private static final float LIGHT0_Z = -0.5659164F;
   private static final float LIGHT1_X = -0.1616904F;
   private static final float LIGHT1_Y = 0.8084521F;
   private static final float LIGHT1_Z = 0.5659164F;
   /** The six axis-aligned face factors of {@link #faceLight}. */
   private static final float SHADE_DOWN = faceLight(0.0F, -1.0F, 0.0F);
   private static final float SHADE_UP = faceLight(0.0F, 1.0F, 0.0F);
   private static final float SHADE_Z = faceLight(0.0F, 0.0F, 1.0F);
   private static final float SHADE_X = faceLight(1.0F, 0.0F, 0.0F);

   public static int shade(int color, float light) {
      int cb = (int)((float)(color >> 16 & 255) * light);
      int cg = (int)((float)(color >> 8 & 255) * light);
      int cr = (int)((float)(color & 255) * light);
      return cr & 255 | (cg & 255) << 8 | (cb & 255) << 16;
   }

   /**
    * 1.12.2 {@code renderBoxesCarvedModel}, written into a {@link VertexConsumer} instead of a
    * {@code Tessellator} ({@code GL_QUADS}, {@code POSITION_COLOR_NORMAL}).
    *
    * <p>Two 1.20.1-only changes, neither of which alters what is visible: the face colour carries
    * the 1.12.2 light-model shading (see {@link #faceLight}), and a face is skipped when the voxel
    * behind it is carved too - 1.12.2 could afford the interior faces because each part was
    * compiled into a GL display list once, while this path re-emits the geometry every frame.
    */
   public static final void renderBoxesCarvedModel(VertexConsumer bb, Matrix4f matrix, int[] carvingData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         double unitd = 1.0 / (double)divideCount;
         int boxIndexCnt = -1;
         int plane = divideCount * divideCount;
         // [palette index][face] - the six face colours of the palette, shaded once per part.
         int[][] shadedPalette = shadedPalette(palletData);

         for(int y = 0; y < divideCount; ++y) {
            double minY = (double)y * unitd;
            double maxY = minY + unitd;

            for(int z = 0; z < divideCount; ++z) {
               double minZ = (double)z * unitd;
               double maxZ = minZ + unitd;

               for(int x = 0; x < divideCount; ++x) {
                  ++boxIndexCnt;
                  double minX = (double)x * unitd;
                  double maxX = minX + unitd;
                  int mod = (boxIndexCnt & 7) << 2;
                  int dat = carvingData[boxIndexCnt >> 3] >>> mod & 15;
                  if (dat != 0 && (dat & 8) == 0) {
                     int[] shaded = shadedPalette[dat & 15];
                     int cDown = shaded[0];
                     int cUp = shaded[1];
                     int cN = shaded[2];
                     int cS = shaded[3];
                     int cW = shaded[4];
                     int cE = shaded[5];
                     if (y == 0 || !isCarvedCell(carvingData, boxIndexCnt - plane)) {
                        quad(bb, matrix, cDown, 0.0F, -1.0F, 0.0F, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ);
                     }

                     if (y == divideCount - 1 || !isCarvedCell(carvingData, boxIndexCnt + plane)) {
                        quad(bb, matrix, cUp, 0.0F, 1.0F, 0.0F, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ);
                     }

                     if (z == 0 || !isCarvedCell(carvingData, boxIndexCnt - divideCount)) {
                        quad(bb, matrix, cN, 0.0F, 0.0F, -1.0F, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
                     }

                     if (z == divideCount - 1 || !isCarvedCell(carvingData, boxIndexCnt + divideCount)) {
                        quad(bb, matrix, cS, 0.0F, 0.0F, 1.0F, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ);
                     }

                     if (x == 0 || !isCarvedCell(carvingData, boxIndexCnt - 1)) {
                        quad(bb, matrix, cW, -1.0F, 0.0F, 0.0F, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ);
                     }

                     if (x == divideCount - 1 || !isCarvedCell(carvingData, boxIndexCnt + 1)) {
                        quad(bb, matrix, cE, 1.0F, 0.0F, 0.0F, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ);
                     }
                  }
               }
            }
         }

      }
   }

   /**
    * Greedy-meshed form of {@link #renderBoxesCarvedModel}, reproducing the same 1.12.2
    * {@code GL_QUADS} geometry ({@code POSITION_COLOR_NORMAL}) with merged rectangles: coplanar
    * visible unit faces that share a direction and a palette slot are merged into maximal
    * axis-aligned rectangles, so a flat {@code divide}x{@code divide} face becomes a single quad
    * instead of {@code divide*divide} of them. The emitted surface, per-face colour, normal and
    * vertex winding are identical to {@link #renderBoxesCarvedModel}.
    *
    * <p>The six directions are meshed independently. For a direction the visible mask is a 2D plane
    * of unit faces (one layer per coordinate along the face axis); the layer is deleted greedily
    * into equal-slot rectangles. Only the lazily grown static scratch arrays are reused, so a call
    * allocates nothing.
    */
   public static final void renderBoxesCarvedModelGreedy(VertexConsumer bb, Matrix4f matrix, int[] carvingData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return;
      }

      int plane = divideCount * divideCount;
      double unitd = 1.0 / (double)divideCount;
      int maskSize = divideCount * divideCount;
      if (greedyMask.length < maskSize) {
         greedyMask = new boolean[maskSize];
         greedySlot = new byte[maskSize];
      }

      int[][] shaded = shadedPalette(palletData);

      for(int d = 0; d < 6; ++d) {
         // Face axis (0=x, 1=y, 2=z), its sign, and the two mask axes as increasing axes.
         int ax = d < 2 ? 1 : (d < 4 ? 2 : 0);
         int s = (d & 1) == 0 ? -1 : 1;
         int u = ax == 0 ? 1 : 0;
         int v = ax == 2 ? 1 : 2;
         // Index weights for cellIndex = (y * divide + z) * divide + x.
         int coefAx = ax == 0 ? 1 : (ax == 1 ? plane : divideCount);
         int coefU = u == 0 ? 1 : (u == 1 ? plane : divideCount);
         int coefV = v == 0 ? 1 : (v == 1 ? plane : divideCount);

         for(int p = 0; p < divideCount; ++p) {
            boolean border = s < 0 ? p == 0 : p == divideCount - 1;
            int pBase = p * coefAx;

            // Visible-face mask: carved cell whose neighbour behind the face is not carved.
            for(int a = 0; a < divideCount; ++a) {
               int rowBase = a * divideCount;
               int aBase = pBase + a * coefU;
               for(int b = 0; b < divideCount; ++b) {
                  int mi = rowBase + b;
                  greedyMask[mi] = false;
                  int cellIndex = aBase + b * coefV;
                  int dat = carvingData[cellIndex >> 3] >>> ((cellIndex & 7) << 2) & 15;
                  if (dat != 0 && (dat & 8) == 0 && (border || !isCarvedCell(carvingData, cellIndex + s * coefAx))) {
                     greedyMask[mi] = true;
                     greedySlot[mi] = (byte)(dat & 15);
                  }
               }
            }

            // Merge equal-slot set cells into maximal rectangles: widen along v, then along u.
            for(int a = 0; a < divideCount; ++a) {
               int rowBase = a * divideCount;
               for(int b = 0; b < divideCount; ++b) {
                  int mi = rowBase + b;
                  if (!greedyMask[mi]) {
                     continue;
                  }

                  int slot = greedySlot[mi];
                  int b1 = b;
                  while(b1 + 1 < divideCount && greedyMask[rowBase + b1 + 1] && greedySlot[rowBase + b1 + 1] == slot) {
                     ++b1;
                  }

                  int a1 = a;
                  grow:
                  while(a1 + 1 < divideCount) {
                     int nextBase = (a1 + 1) * divideCount;
                     for(int bx = b; bx <= b1; ++bx) {
                        if (!greedyMask[nextBase + bx] || greedySlot[nextBase + bx] != slot) {
                           break grow;
                        }
                     }
                     ++a1;
                  }

                  double minU = (double)a * unitd;
                  double maxU = (double)(a1 + 1) * unitd;
                  double minV = (double)b * unitd;
                  double maxV = (double)(b1 + 1) * unitd;
                  double planeC = (s < 0 ? (double)p : (double)(p + 1)) * unitd;
                  int color = shaded[slot][d];

                  if (d == 0) {
                     quad(bb, matrix, color, 0.0F, -1.0F, 0.0F, minU, planeC, maxV, minU, planeC, minV, maxU, planeC, minV, maxU, planeC, maxV);
                  } else if (d == 1) {
                     quad(bb, matrix, color, 0.0F, 1.0F, 0.0F, maxU, planeC, maxV, maxU, planeC, minV, minU, planeC, minV, minU, planeC, maxV);
                  } else if (d == 2) {
                     quad(bb, matrix, color, 0.0F, 0.0F, -1.0F, minU, maxV, planeC, maxU, maxV, planeC, maxU, minV, planeC, minU, minV, planeC);
                  } else if (d == 3) {
                     quad(bb, matrix, color, 0.0F, 0.0F, 1.0F, minU, maxV, planeC, minU, minV, planeC, maxU, minV, planeC, maxU, maxV, planeC);
                  } else if (d == 4) {
                     quad(bb, matrix, color, -1.0F, 0.0F, 0.0F, planeC, maxU, maxV, planeC, maxU, minV, planeC, minU, minV, planeC, minU, maxV);
                  } else {
                     quad(bb, matrix, color, 1.0F, 0.0F, 0.0F, planeC, minU, maxV, planeC, minU, minV, planeC, maxU, minV, planeC, maxU, maxV);
                  }

                  for(int aa = a; aa <= a1; ++aa) {
                     int clearBase = aa * divideCount;
                     for(int by = b; by <= b1; ++by) {
                        greedyMask[clearBase + by] = false;
                     }
                  }
               }
            }
         }
      }
   }

   /** Face order: down, up, north, south, west, east. */
   private static int[][] shadedPalette(int[] palletData) {
      int[][] shaded = new int[16][];
      int len = Math.min(16, palletData.length);
      for (int i = 0; i < len; ++i) {
         int color = palletData[i];
         shaded[i] = new int[]{
            shade(color, SHADE_DOWN), shade(color, SHADE_UP), shade(color, SHADE_Z),
            shade(color, SHADE_Z), shade(color, SHADE_X), shade(color, SHADE_X)};
      }

      for (int i = len; i < 16; ++i) {
         shaded[i] = shaded[0];
      }

      return shaded;
   }

   /** True when the 4-bit cell at {@code index} is a carved, drawn voxel. */
   private static boolean isCarvedCell(int[] carvingData, int index) {
      int word = index >> 3;
      if (word < 0 || word >= carvingData.length) {
         return false;
      }

      int dat = carvingData[word] >>> ((index & 7) << 2) & 15;
      return dat != 0 && (dat & 8) == 0;
   }

   /**
    * 1.12.2 {@code renderBoxesCarvedModelWithoutNormal}: used by the OBJ/MTL path, where the normals
    * come from elsewhere and a {@code POSITION_COLOR} batch was enough.
    */
   public static final void renderBoxesCarvedModelWithoutNormal(VertexConsumer bb, Matrix4f matrix, int[] carvingData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         double unitd = 1.0 / (double)divideCount;
         int boxIndexCnt = -1;

         for(int y = 0; y < divideCount; ++y) {
            double minY = (double)y * unitd;
            double maxY = minY + unitd;

            for(int z = 0; z < divideCount; ++z) {
               double minZ = (double)z * unitd;
               double maxZ = minZ + unitd;

               for(int x = 0; x < divideCount; ++x) {
                  ++boxIndexCnt;
                  double minX = (double)x * unitd;
                  double maxX = minX + unitd;
                  int mod = (boxIndexCnt & 7) << 2;
                  int dat = carvingData[boxIndexCnt >> 3] >>> mod & 15;
                  if (dat != 0 && (dat & 8) == 0) {
                     int color = palletData[dat];
                     int cb = color >> 16 & 255;
                     int cg = color >> 8 & 255;
                     int cr = color & 255;
                     int ca = 255;
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ);
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ);
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ);
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ);
                     quadWithoutNormal(bb, matrix, cr, cg, cb, ca, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ);
                  }
               }
            }
         }

      }
   }

   /**
    * 1.12.2 {@code renderMCubesCarvedModel}, written into a {@link VertexConsumer}. The mesh comes
    * from the shared {@link MCubeMeshBuilder#instance} scratch buffers.
    */
   public static final void renderMCubesCarvedModel(VertexConsumer bb, Matrix4f matrix, int[] carvingData, MCubeGathered mCubeGatheredData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         MCubeMeshBuilder builder = MCubeMeshBuilder.instance;
         builder.buildMesh(divideCount, mCubeGatheredData);
         if (builder.addedvertexcount != 0) {
            float scale = 1.0F / (float)divideCount;
            float baseX = 0.0F - scale / 2.0F;
            float baseY = 0.0F - scale / 2.0F;
            float baseZ = 0.0F - scale / 2.0F;
            int len = builder.addedindexcount;
            int[] idxAry = builder.indices;
            int color = palletData[7];

            for(int i = 0; i < len; i += 3) {
               MCubeMeshBuilder.MeshVertex vertA = builder.vertices[idxAry[i + 0]];
               MCubeMeshBuilder.MeshVertex vertB = builder.vertices[idxAry[i + 1]];
               MCubeMeshBuilder.MeshVertex vertC = builder.vertices[idxAry[i + 2]];
               float[] norms = builder.normals[i];
               int shaded = shade(color, faceLight(norms[0], norms[1], norms[2]));
               int cb = shaded >> 16 & 255;
               int cg = shaded >> 8 & 255;
               int cr = shaded & 255;
               bb.vertex(matrix, baseX + vertA.vertexX * scale, baseY + vertA.vertexY * scale, baseZ + vertA.vertexZ * scale).color(cr, cg, cb, 255).normal(norms[0], norms[1], norms[2]).endVertex();
               bb.vertex(matrix, baseX + vertB.vertexX * scale, baseY + vertB.vertexY * scale, baseZ + vertB.vertexZ * scale).color(cr, cg, cb, 255).normal(norms[0], norms[1], norms[2]).endVertex();
               bb.vertex(matrix, baseX + vertC.vertexX * scale, baseY + vertC.vertexY * scale, baseZ + vertC.vertexZ * scale).color(cr, cg, cb, 255).normal(norms[0], norms[1], norms[2]).endVertex();
            }

         }
      }
   }

   /** 1.12.2 {@code renderMCubesCarvedModelWithoutNormal}: same mesh, colour only. */
   public static final void renderMCubesCarvedModelWithoutNormal(VertexConsumer bb, Matrix4f matrix, int[] carvingData, MCubeGathered mCubeGatheredData, int[] palletData) {
      int divideCount = JBCarvingDataUtil.getCarvingDivide(carvingData);
      if (divideCount >= 1) {
         MCubeMeshBuilder builder = MCubeMeshBuilder.instance;
         builder.buildMesh(divideCount, mCubeGatheredData);
         if (builder.addedvertexcount != 0) {
            float scale = 1.0F / (float)divideCount;
            float baseX = 0.0F - scale / 2.0F;
            float baseY = 0.0F - scale / 2.0F;
            float baseZ = 0.0F - scale / 2.0F;
            int len = builder.addedindexcount;
            int[] idxAry = builder.indices;
            int color = palletData[7];
            int cb = color >> 16 & 255;
            int cg = color >> 8 & 255;
            int cr = color & 255;
            int ca = 255;

            for(int i = 0; i < len; i += 3) {
               MCubeMeshBuilder.MeshVertex vertA = builder.vertices[idxAry[i + 0]];
               MCubeMeshBuilder.MeshVertex vertB = builder.vertices[idxAry[i + 1]];
               MCubeMeshBuilder.MeshVertex vertC = builder.vertices[idxAry[i + 2]];
               bb.vertex(matrix, baseX + vertA.vertexX * scale, baseY + vertA.vertexY * scale, baseZ + vertA.vertexZ * scale).color(cr, cg, cb, ca).endVertex();
               bb.vertex(matrix, baseX + vertB.vertexX * scale, baseY + vertB.vertexY * scale, baseZ + vertB.vertexZ * scale).color(cr, cg, cb, ca).endVertex();
               bb.vertex(matrix, baseX + vertC.vertexX * scale, baseY + vertC.vertexY * scale, baseZ + vertC.vertexZ * scale).color(cr, cg, cb, ca).endVertex();
            }

         }
      }
   }

   /** Two triangles (v0,v1,v2) + (v2,v3,v0) with a shared normal - the 1.12.2 quad layout. */
   private static void quad(VertexConsumer bb, Matrix4f matrix, int color, float nx, float ny, float nz,
                            double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
      int cb = color >> 16 & 255;
      int cg = color >> 8 & 255;
      int cr = color & 255;
      bb.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, (float)x1, (float)y1, (float)z1).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, (float)x3, (float)y3, (float)z3).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
      bb.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, 255).normal(nx, ny, nz).endVertex();
   }

   /** Two triangles with colour only, matching the 1.12.2 {@code POSITION_COLOR} batches. */
   private static void quadWithoutNormal(VertexConsumer bb, Matrix4f matrix, int cr, int cg, int cb, int ca,
                            double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
      bb.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, ca).endVertex();
      bb.vertex(matrix, (float)x1, (float)y1, (float)z1).color(cr, cg, cb, ca).endVertex();
      bb.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, ca).endVertex();
      bb.vertex(matrix, (float)x2, (float)y2, (float)z2).color(cr, cg, cb, ca).endVertex();
      bb.vertex(matrix, (float)x3, (float)y3, (float)z3).color(cr, cg, cb, ca).endVertex();
      bb.vertex(matrix, (float)x0, (float)y0, (float)z0).color(cr, cg, cb, ca).endVertex();
   }

   static {
      lastSentTimeMillis = 0L;
   }
}
