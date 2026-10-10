package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.util.ArrayList;

public class RingProgramPage {
   public static final int MAX_NODE_COUNT = 24;
   public static final int MAX_NODE_INDEX = 23;
   public static final int NO_RING_INDEX = -1;
   public static final String EMPTY_PAGENAME = "";
   public static final int PAGENAME_MAX = 15;
   private static final int RESERVED = -2034328391;
   public String pageName;
   public short pageIndex;
   public ArrayList<AbstractRingNode> nodeList;

   private RingProgramPage(ArrayList<AbstractRingNode> argList) {
      this.nodeList = argList;
   }

   public RingProgramPage(ArrayList<AbstractRingNode> arglist, String argPageNameStr) {
      this(arglist);
      this.pageName = argPageNameStr;
   }

   public AbstractRingNode getNode(byte nodeIndex) {
      int listLen;
      if ((listLen = this.nodeList.size()) == 0) {
         return null;
      } else {
         return nodeIndex >= 0 && nodeIndex < listLen ? (AbstractRingNode)this.nodeList.get(nodeIndex) : null;
      }
   }

   public byte incrementNodeIndex(byte nodeIndex) {
      return nodeIndex >= this.nodeList.size() - 1 ? 0 : (byte)(nodeIndex + 1);
   }

   public byte decrementNodeIndex(byte nodeIndex) {
      return nodeIndex <= 0 ? (byte)(this.nodeList.size() - 1) : (byte)(nodeIndex - 1);
   }

   public void write(DataBufferableOutput out, boolean saveRunData) throws IOException {
      out.writeByte(this.nodeList.size());
      out.commitSegment((byte)30);
      out.writeCommitUTFSegment(this.getPageName(), (byte)50);
      if (saveRunData) {
         out.writeInt(-2034328391);
         out.commitSegment((byte)40);
      }

      int i = 0;

      for(int len = this.nodeList.size(); i < len; ++i) {
         AbstractRingNode node = (AbstractRingNode)this.nodeList.get(i);
         node.write(out);
         out.commitSegment((byte)60);
         if (saveRunData) {
            node.writeRunningData(out);
            out.commitSegment((byte)70);
         }
      }

   }

   public static RingProgramPage createFrom(DataBufferableInput inn, boolean readRunData) throws IOException {
      int nodeCount = inn.readByte((byte)0);
      String pageNameStr = inn.readUTFSegmentSafe((byte)50);
      ArrayList<AbstractRingNode> list = new ArrayList(24);
      RingProgramPage newPage = new RingProgramPage(list, pageNameStr);
      if (readRunData && inn.bufferNextSegment((byte)40)) {
         inn.readInt(0);
      }

      byte index = 0;

      for(int i = 0; i < nodeCount && inn.bufferNextSegment((byte)60); ++i) {
         AbstractRingNode ringNode = AbstractRingNode.read(inn);
         if (readRunData && inn.bufferNextSegment((byte)70)) {
            ringNode.readRunningData(inn);
         }

         if (ringNode != null) {
            list.add(ringNode);
            ringNode.nodeIndex = index++;
         }

         if (index > 23) {
            break;
         }
      }

      return newPage;
   }

   public void initProgramPage(RingProgram program) {
      int i = 0;

      for(int len = this.nodeList.size(); i < len; ++i) {
         AbstractRingNode ringNode = (AbstractRingNode)this.nodeList.get(i);
         if (ringNode != null) {
            if (!program.isAuthorizedNodeType(ringNode)) {
               AbstractRingNode toEmptyNode = new RingNodeSTEmpty();
               toEmptyNode.nodeIndex = ringNode.nodeIndex;
               this.nodeList.set(i, toEmptyNode);
            } else {
               ringNode.onProgramInit(program, this);
            }
         }
      }

   }

   public static RingProgramPage createNewRingProgramPage(boolean withGui) {
      ArrayList<AbstractRingNode> list = new ArrayList();
      list.add((new RingNodeSTEmpty()).withGui());
      list.add((new RingNodeSTEmpty()).withGui());
      list.add((new RingNodeSTEmpty()).withGui());
      return new RingProgramPage(list);
   }

   public void ensureGUI() {
      int i = 0;

      for(int len = this.nodeList.size(); i < len; ++i) {
         AbstractRingNode node = (AbstractRingNode)this.nodeList.get(i);
         if (node != null) {
            node.withGui();
         }
      }

   }

   public void setPageName(String strName) {
      if (strName != null && (strName = strName.trim()).length() != 0) {
         if (strName.length() > 15) {
            strName = strName.substring(0, 15).trim();
         }

         this.pageName = strName;
      } else {
         this.pageName = null;
      }

   }

   public String getPageName() {
      return this.pageName == null ? "" : this.pageName;
   }
}
