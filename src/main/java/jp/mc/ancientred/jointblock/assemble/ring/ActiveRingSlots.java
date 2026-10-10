package jp.mc.ancientred.jointblock.assemble.ring;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.common.BlockedArray;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.nbt.CompoundTag;

public class ActiveRingSlots {
   public static final int SEAT_CHANNEL_SPAN = 16;
   public static final int KEY_CHANNEL_BASE = 0;
   public static final int ITEM_CHANNEL_BASE = 8;
   public static final int INP_CHANNEL_CNT = 64;
   public static final int MOV_CHANNEL_CNT = 16;
   public static final int MOT_CHANNEL_CNT = 128;
   public static final int ACT_CHANNEL_CNT = 128;
   public static final int EXT_CHANNEL_CNT = 16;
   public static final int MAX_INP_CHANNEL = 63;
   public static final int MAX_MOT_CHANNEL = 127;
   public static final int MAX_ACT_CHANNEL = 127;
   public static final int MAX_MOV_CHANNEL = 15;
   public static final int MAX_EXT_CHANNEL = 15;
   public static final int MOT_CHANNEL_BASE = 0;
   public static final int ACT_CHANNEL_BASE = 128;
   public static final int INP_CHANNEL_BASE = 256;
   public static final int MOV_CHANNEL_BASE = 320;
   public static final int EXT_CHANNEL_BASE = 336;
   public static final int TOTAL_CHANNEL_SIZE = 672;
   public static final String NBT_TAGNAME_SLOT_LST = "slotlist";
   public static final int MOTIONASK_CAN_BE_ACTIVATED = 0;
   public static final int MOTIONASK_ALREADY = 1;
   public static final int MOTIONASK_NOTEMPTY = 2;
   public static final int MOTIONASK_NOTEXIST = 3;
   public static final short MOVE_DR_CHANNEL_FB = 0;
   public static final short MOVE_DR_CHANNEL_LR = 1;
   public static final short MOVE_DR_CHANNEL_UD = 2;
   public static final short MOVE_DR_CHANNEL_YW = 3;
   public static final short MOVE_DR_CHANNEL_TASK = 4;
   public static final short EXT_DR_CHANNEL_SEARCH = 0;
   private BlockedArray<ActiveSlot> channelSlots;

   public ActiveRingSlots() {
      this.channelSlots = new BlockedArray(BlockedArray.BlockedArrayBlockSize.b16, 672);
   }

   public void updateAllSlots(EntityJointed ej) {
      ArrayList<ActiveSlot> inpIter = this.channelSlots.getIterable();
      int i = 0;

      for(int len = inpIter.size(); i < len; ++i) {
         ActiveSlot slots = (ActiveSlot)inpIter.get(i);
         if (slots != null) {
            slots.updateTick(ej);
         }
      }

   }

   public ActiveSlot getSlotByType(SlotType slotType, short channel) {
      return (ActiveSlot)this.channelSlots.get(this.getActualChannel(slotType, channel));
   }

   private short getActualChannel(SlotType slotType, short channel) {
      if (channel < 0) {
         return -1;
      } else {
         switch (slotType) {
            case input:
               if (channel > 63) {
                  return -1;
               }

               return (short)(256 + channel);
            case move:
               if (channel > 15) {
                  return -1;
               }

               return (short)(320 + channel);
            case action:
               if (channel > 127) {
                  return -1;
               }

               return (short)(128 + channel);
            case search:
               if (channel > 15) {
                  return -1;
               }

               return (short)(336 + channel);
            case motion:
            default:
               return channel > 127 ? -1 : (short)(0 + channel);
         }
      }
   }

   public ActiveSlot getOfCreateChannelSlot(RingProgram program, RingProgramPage page, SlotType slotType, short channel) {
      short actualChannel = this.getActualChannel(slotType, channel);
      if (actualChannel >= 0 && actualChannel < this.channelSlots.size()) {
         ActiveSlot slot = (ActiveSlot)this.channelSlots.get(actualChannel);
         if (slot == null) {
            slot = new ActiveSlot();
            this.channelSlots.put(actualChannel, slot);
            slot.chnnIndex = actualChannel;
         }

         return slot;
      } else {
         return null;
      }
   }

   public void clearChannelSlot(SlotType slotType, short channel) {
      ActiveSlot slot = this.getSlotByType(slotType, channel);
      if (slot != null && slot.activeNode != null) {
         slot.activeNode.onForcedRemovedFromSlot();
         slot.activeNode = null;
      }

   }

   public AbstractActivatableRingNode getActiveNodeFromChannelSlot(SlotType slotType, short channel) {
      ActiveSlot slot = this.getSlotByType(slotType, channel);
      return slot != null ? slot.activeNode : null;
   }

   public void saveToNBT(CompoundTag tag) {
      try {
         int[] dataAry = this.blockedAryToIntAry(this.channelSlots.getIterable());
         if (dataAry != null && dataAry.length > 0) {
            tag.putIntArray("slotlist", dataAry);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public int[] blockedAryToIntAry(ArrayList<ActiveSlot> iterList) {
      int len = iterList.size();
      if (len == 0) {
         return null;
      } else {
         int[] dataAry = new int[len << 1];

         for(int i = 0; i < len; ++i) {
            ActiveSlot slot = (ActiveSlot)iterList.get(i);
            int idxBase = i << 1;
            if (slot != null) {
               int chnnIndex = slot.chnnIndex;
               dataAry[idxBase] = chnnIndex;
               if (slot.activeNode != null) {
                  int nodeIndices = slot.activeNode.nodeIndex << 16 & -65536;
                  nodeIndices |= slot.activeNodePage.pageIndex << 8 & '\uff00';
                  nodeIndices |= slot.activeNodeProgram.slotIndex & 255;
                  dataAry[idxBase + 1] = nodeIndices;
               } else {
                  dataAry[idxBase + 1] = -1;
               }
            } else {
               dataAry[idxBase] = -1;
               dataAry[idxBase + 1] = -1;
            }
         }

         return dataAry;
      }
   }

   public void readFromNBT(CompoundTag tag, EntityJointed ej) {
      try {
         if (ej.ringPrograms == null) {
            return;
         }

         if (tag.contains("slotlist", 11)) {
            int[] dataAry = tag.getIntArray("slotlist");
            this.setIntAryBackToBlockAry(ej, dataAry, this.channelSlots);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   private void setIntAryBackToBlockAry(EntityJointed ej, int[] dataAry, BlockedArray<ActiveSlot> blockAry) {
      int len = dataAry.length;
      if (len != 0 && (len & 1) == 0) {
         len >>= 1;

         for(int i = 0; i < len; ++i) {
            int idxBase = i << 1;
            int chnnIndex = dataAry[idxBase];
            if (chnnIndex != -1) {
               ActiveSlot slot = (ActiveSlot)blockAry.get(chnnIndex);
               if (slot == null) {
                  ActiveSlot newSlot = new ActiveSlot();
                  blockAry.put(chnnIndex, newSlot);
                  newSlot.chnnIndex = chnnIndex;
                  int nodeIndices = dataAry[idxBase + 1];
                  if (nodeIndices != -1) {
                     this.resumeFromIndices(ej, newSlot, chnnIndex, nodeIndices);
                  }
               }
            }
         }

      }
   }

   private void resumeFromIndices(EntityJointed ej, ActiveSlot slot, int chnnIndex, int nodeIndices) {
      short pageIdx = (short)(nodeIndices >> 16 & '\uffff');
      byte nodeIdx = (byte)(nodeIndices >> 8 & 255);
      byte pgmSlotIdx = (byte)(nodeIndices & 255);
      if (pgmSlotIdx >= 0 && pgmSlotIdx < ej.ringPrograms.length) {
         RingProgram program = ej.ringPrograms[pgmSlotIdx];
         if (program != null) {
            RingProgramPage page = program.getPage(pageIdx);
            if (page != null) {
               AbstractRingNode node = page.getNode(nodeIdx);
               if (node != null && node instanceof AbstractActivatableRingNode) {
                  slot.setActiveNode(program, page, (AbstractActivatableRingNode)node);
               }
            }
         }
      }
   }

   public static class ActiveSlot {
      public int chnnIndex;
      public AbstractActivatableRingNode activeNode;
      public RingProgramPage activeNodePage;
      public RingProgram activeNodeProgram;

      public void updateTick(EntityJointed ej) {
         if (this.activeNode != null) {
            if (this.activeNode.remainTick >= 0) {
               --this.activeNode.remainTick;
            }

            if (this.activeNode.remainTick >= 0) {
               this.activeNode.doActiveEntityWork(ej);
            }

            if (this.activeNode.remainTick < 0) {
               this.clearActiveNode();
            }
         }

      }

      public void setActiveNode(RingProgram program, RingProgramPage page, AbstractActivatableRingNode actRingNode) {
         this.activeNode = actRingNode;
         this.activeNodePage = page;
         this.activeNodeProgram = program;
      }

      public void clearActiveNode() {
         this.activeNode = null;
         this.activeNodePage = null;
         this.activeNodeProgram = null;
      }
   }

   public static enum SlotType {
      input,
      move,
      motion,
      action,
      search;
   }
}
