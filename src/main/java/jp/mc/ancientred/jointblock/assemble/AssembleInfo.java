package jp.mc.ancientred.jointblock.assemble;

import java.util.HashMap;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AssembleInfo extends HashMap<JointParts, JointParts> {
   public JointParts core;
   public final int corePosX;
   public final int corePosY;
   public final int corePosZ;
   public final int coreFacing;
   public int partsCount = 0;
   public int motionTelePartsCount = 0;
   public int coreBlockCount = 0;
   public int seatBlockCount = 0;
   public int normalBlockCount = 0;
   public int rotatorBlockCount = 0;
   public int extenderBlockCount = 0;
   public int scalerBlockCount = 0;
   public int sliderBlockCount = 0;
   public int attracterBlockCount = 0;
   public int maxX = 0;
   public int maxY = 0;
   public int maxZ = 0;
   public int minX = 0;
   public int minY = 0;
   public int minZ = 0;
   public int masterSlotEnableStateFlag;
   public boolean[] seatNumUsed = new boolean[6];
   public JointParts topLevelParts;
   public int[] intPosWork = new int[3];
   public boolean ignoreAddon;
   public AddonBonus addonBonus = new AddonBonus();
   public boolean ignoreCore = false;
   public boolean baseCollectDone = false;
   public AssembleStack stack;
   public JointParts linkFirst = null;
   public JointParts linkLast = null;

   public AssembleInfo(AssembleStack stack, int coreFacing, int posX, int posY, int posZ) {
      this.stack = stack;
      this.coreFacing = coreFacing;
      this.corePosX = posX;
      this.corePosY = posY;
      this.corePosZ = posZ;
      this.topLevelParts = new JointParts(0, 0, 0);
   }

   public void add(JointParts elem, int jointType, int motionItemId, int motionItemMeta, int motionBindNum) {
      if (jointType == 0) {
         this.core = elem;
      }

      if (elem.xCoord > this.maxX) {
         this.maxX = elem.xCoord;
      }

      if (elem.yCoord > this.maxY) {
         this.maxY = elem.yCoord;
      }

      if (elem.zCoord > this.maxZ) {
         this.maxZ = elem.zCoord;
      }

      if (elem.xCoord < this.minX) {
         this.minX = elem.xCoord;
      }

      if (elem.yCoord < this.minY) {
         this.minY = elem.yCoord;
      }

      if (elem.zCoord < this.minZ) {
         this.minZ = elem.zCoord;
      }

      if (jointType == 0) {
         ++this.coreBlockCount;
      }

      if (jointType == 2) {
         ++this.seatBlockCount;
      }

      if (jointType == 4) {
         ++this.normalBlockCount;
      }

      if (jointType == 8) {
         ++this.rotatorBlockCount;
      }

      if (jointType == 10) {
         ++this.extenderBlockCount;
      }

      if (jointType == 12) {
         ++this.scalerBlockCount;
      }

      if (jointType == 14) {
         ++this.sliderBlockCount;
      }

      if (jointType == 15) {
         ++this.attracterBlockCount;
      }

      if (JBCommonUtil.jointNeedsMotionId(jointType, motionItemId, motionItemMeta, motionBindNum)) {
         ++this.motionTelePartsCount;
      }

      ++this.partsCount;
      if (this.linkFirst == null) {
         this.linkFirst = elem;
         this.linkLast = elem;
      } else {
         elem.linkPrev = this.linkLast;
         this.linkLast = elem;
      }

      super.put(elem, elem);
   }

   public static void collectAllParts(Level world, int corePosX, int corePosY, int corePosZ, AssembleInfo assembleInfo) {
      assembleInfo.stack.push(corePosX, corePosY, corePosZ, 0, assembleInfo.topLevelParts, 0);

      while(assembleInfo.stack.pop()) {
         collectParts(world, assembleInfo.stack.getPosX(), assembleInfo.stack.getPosY(), assembleInfo.stack.getPosZ(), assembleInfo.stack.getPSide(), assembleInfo.stack.getParent(), assembleInfo, assembleInfo.stack.getDepth());
      }

   }

   public static void collectParts(Level world, int posX, int posY, int posZ, int faceRelSideFromParent, JointParts parentParts, AssembleInfo assembleInfo, int depth) {
      if (faceRelSideFromParent >= 0 && faceRelSideFromParent <= 5) {
         if (Math.abs(posX - assembleInfo.corePosX) <= JBConfig.maxAssembleXRange) {
            if (Math.abs(posY - assembleInfo.corePosY) <= JBConfig.maxAssembleYRange) {
               if (Math.abs(posZ - assembleInfo.corePosZ) <= JBConfig.maxAssembleZRange) {
                  if (assembleInfo.partsCount < JBConfig.maxAssembleCount) {
                     TileEntityJoint teJoint = BlockJointBase.getTileEntityJointerBase(world, posX, posY, posZ);
                     if (teJoint != null) {
                        int jointType = teJoint.getJointType();
                        int thisFacingSide;
                        if (assembleInfo.baseCollectDone) {
                           if (jointType != 0) {
                              thisFacingSide = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, teJoint.getConnectSide());
                              if (EnumDirectionOld.OPPOSITES[thisFacingSide] != faceRelSideFromParent) {
                                 return;
                              }
                           } else if (assembleInfo.core != null || assembleInfo.ignoreCore) {
                              return;
                           }
                        }

                        assembleInfo.baseCollectDone = true;
                        thisFacingSide = teJoint.getJointFacing();
                        JointPartsForCollect thisPart = new JointPartsForCollect(posX - assembleInfo.corePosX, posY - assembleInfo.corePosY, posZ - assembleInfo.corePosZ, posX, posY, posZ);
                        thisPart.te = teJoint;
                        thisPart.parent = parentParts;
                        if (!assembleInfo.containsKey(thisPart)) {
                           assembleInfo.add(thisPart, teJoint.getJointType(), 0, 0, 0);
                           if (jointType == 0) {
                              parentParts.child[0] = thisPart;
                           } else {
                              parentParts.child[faceRelSideFromParent] = thisPart;
                           }

                           parentParts.hasChild = true;
                           if (jointType != 15) {
                                                            for(int side = 5; side >= 0; --side) {
                                 int rotatedSide = JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(assembleInfo.coreFacing, side);
                                 if (jointType != 10 || rotatedSide == teJoint.getConnectableExtenderSide()) {
                                    int childX = posX + JBRotationUtil.side_matrix[rotatedSide][0];
                                    int childY = posY + JBRotationUtil.side_matrix[rotatedSide][1];
                                    int childZ = posZ + JBRotationUtil.side_matrix[rotatedSide][2];
                                    Block block = WorldHelper.getBlock(world, childX, childY, childZ);
                                    if (block instanceof BlockJointBase) {
                                       BlockEntity teChild = WorldHelper.getTileEntity(world, childX, childY, childZ);
                                       if (teChild instanceof TileEntityJoint) {
                                          assembleInfo.stack.push(childX, childY, childZ, side, thisPart, depth + 1);
                                       }
                                    }
                                 }
                              }

                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void assembleAllParts(BlockGetter world, int corePosX, int corePosY, int corePosZ, AssembleInfo assembleInfo) {
      assembleAllParts(world, corePosX, corePosY, corePosZ, assembleInfo, 0);
   }

   public static void assembleAllParts(BlockGetter world, int corePosX, int corePosY, int corePosZ, AssembleInfo assembleInfo, int faceRelSideFromParent) {
      assembleInfo.stack.push(corePosX, corePosY, corePosZ, faceRelSideFromParent, assembleInfo.topLevelParts, 0);

      while(assembleInfo.stack.pop()) {
         assembleParts(world, assembleInfo.stack.getPosX(), assembleInfo.stack.getPosY(), assembleInfo.stack.getPosZ(), assembleInfo.stack.getPSide(), assembleInfo.stack.getParent(), assembleInfo, assembleInfo.stack.getDepth());
      }

   }

   public JointParts[] createRefLists() {
      JointParts[] refLists = new JointParts[this.partsCount];
      int index = this.partsCount;

      for(JointParts ref = this.linkLast; ref != null && index > 0; ref = ref.linkPrev) {
         --index;
         refLists[index] = ref;
      }

      return refLists;
   }

   public static void assembleParts(BlockGetter world, int posX, int posY, int posZ, int faceRelSideFromParent, JointParts parentParts, AssembleInfo assembleInfo, int depth) {
      if (faceRelSideFromParent >= 0 && faceRelSideFromParent <= 5) {
         if (Math.abs(posX - assembleInfo.corePosX) <= JBConfig.maxAssembleXRange) {
            if (Math.abs(posY - assembleInfo.corePosY) <= JBConfig.maxAssembleYRange) {
               if (Math.abs(posZ - assembleInfo.corePosZ) <= JBConfig.maxAssembleZRange) {
                  if (assembleInfo.partsCount < JBConfig.maxAssembleCount) {
                     TileEntityJoint teJoint = BlockJointBase.getTileEntityJointerBase(world, posX, posY, posZ);
                     if (teJoint != null) {
                        int jointType = teJoint.getJointType();
                        short smotionItemId = (short)teJoint.getMotionBinderItemId();
                        byte bmotionMeta = (byte)teJoint.getMotionItemDamage();
                        short sbindNum = (short)teJoint.getBindNum();
                        short smodelItemId;
                        byte bmodelMeta;
                        if (teJoint.isValidRenderModel) {
                           smodelItemId = (short)teJoint.getModelItemID();
                           bmodelMeta = (byte)teJoint.getModelItemDamage();
                        } else {
                           smodelItemId = 0;
                           bmodelMeta = 0;
                        }

                        boolean jointNeedsTeleId = JBCommonUtil.jointNeedsMotionId(jointType, smotionItemId, bmotionMeta, sbindNum);
                        if (!jointNeedsTeleId || assembleInfo.motionTelePartsCount < 255 && assembleInfo.motionTelePartsCount < JBConfig.maxAssembleMotionCount) {
                           if (jointType != 0) {
                              int thisFacingSide = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, teJoint.getConnectSide());
                              if (EnumDirectionOld.OPPOSITES[thisFacingSide] != faceRelSideFromParent) {
                                 return;
                              }
                           } else if (assembleInfo.core != null || assembleInfo.ignoreCore) {
                              return;
                           }

                           IJBAddon addon = null;
                           if (!assembleInfo.ignoreAddon) {
                              addon = assembleInfo.addonBonus.checkAndGetAddon(smodelItemId, bmodelMeta);
                              if (assembleInfo.addonBonus.checkLimitResultExceedLimit) {
                                 return;
                              }
                           }

                           int facingSide = teJoint.getJointFacing();
                           int[] ret = JBRotationUtil.rotateFacingToZFront(assembleInfo.coreFacing, assembleInfo.intPosWork, posX - assembleInfo.corePosX, posY - assembleInfo.corePosY, posZ - assembleInfo.corePosZ);
                           JointParts thisPart = new JointParts(ret[0], ret[1], ret[2]);
                           thisPart.parent = parentParts;
                           if (!assembleInfo.containsKey(thisPart)) {
                              assembleInfo.add(thisPart, teJoint.getJointType(), smotionItemId, bmotionMeta, sbindNum);
                              if (jointType == 0) {
                                 parentParts.child[0] = thisPart;
                              } else {
                                 parentParts.child[faceRelSideFromParent] = thisPart;
                              }

                              parentParts.hasChild = true;
                              if (addon != null) {
                                 assembleInfo.addonBonus.applyAddon(addon, smodelItemId, bmodelMeta);
                              }

                              thisPart.jointType = (byte)jointType;
                              thisPart.depth = depth;
                              thisPart.modelItemId = smodelItemId;
                              thisPart.modelMeta = bmodelMeta;
                              thisPart.motionItemId = smotionItemId;
                              thisPart.motionMeta = bmotionMeta;
                              thisPart.bindNum = sbindNum;
                              thisPart.guiPage = (byte)teJoint.guiPage;
                              thisPart.connectSide = (byte)EnumDirectionOld.OPPOSITES[faceRelSideFromParent];
                              thisPart.renderSide = (byte)JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, teJoint.renderSide);
                              thisPart.extraJointStateFlags = teJoint.extraJointStateFlags & -65536;
                              thisPart.carvingData = teJoint.carvingData;
                              if (JBCommonUtil.doSideRotateOnRender(thisPart.jointType) && thisPart.carvingData == null) {
                                 if (thisPart.modelItemId == 0) {
                                    thisPart.facing = (byte)JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, facingSide & 7, thisPart.connectSide);
                                 } else {
                                    thisPart.facing = (byte)JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, facingSide & 7, thisPart.renderSide);
                                 }
                              } else {
                                 thisPart.facing = (byte)JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(assembleInfo.coreFacing, facingSide & 7);
                              }

                              if (teJoint.memoStr != null && !JBCommonUtil.isStringEmpty(teJoint.memoStr)) {
                                 try {
                                    thisPart.memoStrHolder = new MemoStringHolder(teJoint.memoStr);
                                 } catch (Exception var29) {
                                    thisPart.memoStrHolder = null;
                                 }
                              }

                              int i;
                              for(i = 0; i < 6; ++i) {
                                 thisPart.params[i] = (short)teJoint.params[i];
                              }

                              if (thisPart.initValues.length != teJoint.initValues.length) {
                                 thisPart.initValues = new short[teJoint.initValues.length][];
                              }

                              int childX;
                              for(i = 0; i < thisPart.initValues.length; ++i) {
                                 if (thisPart.initValues[i] == null) {
                                    thisPart.initValues[i] = new short[4];
                                 }

                                 for(childX = 0; childX < thisPart.initValues[i].length; ++childX) {
                                    thisPart.initValues[i][childX] = (short)teJoint.initValues[i][childX];
                                 }
                              }

                              short rotatedSide;
                              if (thisPart.jointType == 2) {
                                 if (1 <= thisPart.initValues[0][0] && thisPart.initValues[0][0] <= 4 && !assembleInfo.seatNumUsed[thisPart.initValues[0][0]]) {
                                    assembleInfo.seatNumUsed[thisPart.initValues[0][0]] = true;
                                 } else {
                                    thisPart.initValues[0][0] = 0;
                                 }
                              } else if (thisPart.jointType == 15 && thisPart.initValues[0][0] == 1) {
                                 rotatedSide = thisPart.initValues[0][1];
                                 assembleInfo.masterSlotEnableStateFlag |= 65536 << rotatedSide;
                              }

                              thisPart.renderOffsetX = (float)(thisPart.xCoord - parentParts.xCoord);
                              thisPart.renderOffsetY = (float)(thisPart.yCoord - parentParts.yCoord);
                              thisPart.renderOffsetZ = (float)(thisPart.zCoord - parentParts.zCoord);
                              JointParts.ensureMotionHanlder(thisPart);
                              if (jointType != 15) {
                                                                  for(int side = 5; side >= 0; --side) {
                                    rotatedSide = JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(assembleInfo.coreFacing, side);
                                    if (jointType != 10 || rotatedSide == teJoint.getConnectableExtenderSide()) {
                                       childX = posX + JBRotationUtil.side_matrix[rotatedSide][0];
                                       int childY = posY + JBRotationUtil.side_matrix[rotatedSide][1];
                                       int childZ = posZ + JBRotationUtil.side_matrix[rotatedSide][2];
                                       Block block = world.getBlockState(new BlockPos(childX, childY, childZ)).getBlock();
                                       if (block instanceof BlockJointBase) {
                                          BlockEntity teChild = WorldHelper.getTileEntity(world, childX, childY, childZ);
                                          if (teChild instanceof TileEntityJoint) {
                                             assembleInfo.stack.push(childX, childY, childZ, side, thisPart, depth + 1);
                                          }
                                       }
                                    }
                                 }

                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
