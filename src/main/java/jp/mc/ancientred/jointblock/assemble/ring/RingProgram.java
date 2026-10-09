package jp.mc.ancientred.jointblock.assemble.ring;

import java.util.ArrayList;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJointed;

public class RingProgram {
   public static final RingProgram ERROR_PGM = new RingProgram(true);
   public static final byte DATA_SEGMENTS_PROG_INF = 10;
   public static final byte DATA_SEGMENTS_PROG_RUN = 20;
   public static final byte DATA_SEGMENTS_PAGE_INF = 30;
   public static final byte DATA_SEGMENTS_PAGE_RUN = 40;
   public static final byte DATA_SEGMENTS_PAGE_NAME = 50;
   public static final byte DATA_SEGMENTS_NODE_INF = 60;
   public static final byte DATA_SEGMENTS_NODE_RUN = 70;
   public static final int RP_VERSION_ONE = 1;
   public static final int MAX_RINGPAGE_COUNT = 32;
   public static final int MAX_RINGPAGE_INDEX = 31;
   public static final int NO_PAGE_INDEX = -1;
   public static final int MAX_VIEW_ENTRY_NUM = 6;
   public static final int EXECUTE_TYPE_CYCLE = 0;
   public static final int EXECUTE_TYPE_SHOT = 1;
   public static final int DEBUG_ACTIVATED_TICK = 10;
   private static final int MAX_NODE_EXECUTION_ON_TICK = 48;
   private static final int RESERVED = -560161812;
   public static final int AUT_FLG_MOT = 1;
   public static final int AUT_FLG_ACT = 2;
   public static final int AUT_FLG_MOV = 4;
   public static final int AUT_FLG_INP = 8;
   public static final int AUT_FLG_OPC = 65536;
   public static final int AUT_FLG_OVP = 131072;
   private static final int AUTH_INIT = 15;
   private int authFlags = 15;
   private byte executeType = 0;
   public short currentPageIndex = 0;
   public byte currentNodeIndex = 0;
   public LimitedCallStack callStack = new LimitedCallStack();
   public boolean hadError;
   public ErrorType hadErrorType;
   public short lastPageIndex;
   public byte lastNodeIndex;
   public short lastLastPageIndex;
   public byte lastLastNodeIndex;
   public byte slotIndex;
   public long tickGeneration;
   private ArrayList<AbstractRingNode> workerList;
   public ArrayList<RingProgramPage> pageList;
   public String errorTypeStr;
   private EntityJointed entityJointed;

   private RingProgram(ArrayList<RingProgramPage> argList) {
      this.hadErrorType = RingProgram.ErrorType.unKnownError;
      this.lastPageIndex = -1;
      this.lastNodeIndex = -1;
      this.lastLastPageIndex = -1;
      this.lastLastNodeIndex = -1;
      this.tickGeneration = 0L;
      this.pageList = argList;
   }

   private RingProgram(boolean argError) {
      this.hadErrorType = RingProgram.ErrorType.unKnownError;
      this.lastPageIndex = -1;
      this.lastNodeIndex = -1;
      this.lastLastPageIndex = -1;
      this.lastLastNodeIndex = -1;
      this.tickGeneration = 0L;
      this.hadError = argError;
      this.pageList = new ArrayList(1);
   }

   public void setExecuteType(int value) {
      this.executeType = (byte)value;
   }

   public int getExecuteType() {
      return this.executeType;
   }

   public boolean isExecuteTypeCycle() {
      return this.executeType == 0;
   }

   public boolean isExecuteTypeShot() {
      return this.executeType == 1;
   }

   public EntityJointed getEntityJointed() {
      return this.entityJointed;
   }

   public void setEntityJointed(EntityJointed entityJointed) {
      this.entityJointed = entityJointed;
   }

   public boolean isAutMotOK() {
      return (this.authFlags & 1) != 0;
   }

   public boolean isAutActOK() {
      return (this.authFlags & 2) != 0;
   }

   public boolean isAutMovOK() {
      return (this.authFlags & 4) != 0;
   }

   public boolean isAutInpOK() {
      return (this.authFlags & 8) != 0;
   }

   public boolean isOPCut() {
      return (this.authFlags & 65536) != 0;
   }

   public void setAutMotOK(boolean value) {
      this.authFlags = this.authFlags & -2 | (value ? 1 : 0);
   }

   public void setAutActOK(boolean value) {
      this.authFlags = this.authFlags & -3 | (value ? 2 : 0);
   }

   public void setAutMovOK(boolean value) {
      this.authFlags = this.authFlags & -5 | (value ? 4 : 0);
   }

   public void setAutInpOK(boolean value) {
      this.authFlags = this.authFlags & -9 | (value ? 8 : 0);
   }

   public void setOPCut(boolean value) {
      this.authFlags = this.authFlags & -65537 | (value ? 65536 : 0);
   }

   public void setAllowOverpower(boolean value) {
      this.authFlags = this.authFlags & -131073 | (value ? 131072 : 0);
   }

   public boolean isAuthorizedNodeType(AbstractRingNode ringNode) {
      int needs = ringNode.getAuthNeeds();
      return (this.authFlags & needs) == needs;
   }

   public void initProgram() {
      this.currentPageIndex = 0;
      this.hadError = false;
      this.lastPageIndex = -1;
      this.lastNodeIndex = -1;
      this.lastLastPageIndex = -1;
      this.lastLastNodeIndex = -1;
      this.setAllowOverpower(JBConfig.enableOverpowerNode);
      int i = 0;

      for(int len = this.pageList.size(); i < len; ++i) {
         RingProgramPage page = (RingProgramPage)this.pageList.get(i);
         if (page != null) {
            page.initProgramPage(this);
         }
      }

   }

   public ArrayList<AbstractRingNode> getWorkList(boolean clear) {
      if (this.workerList == null) {
         this.workerList = new ArrayList();
      }

      if (clear) {
         this.workerList.clear();
      }

      return this.workerList;
   }

   public void executeTick(EntityJointed ej) {
      if (this.pageList.size() == 0) {
         this.setError(RingProgram.ErrorType.buildError);
      } else {
         ++this.tickGeneration;

         try {
            if (this.isExecuteTypeShot()) {
               this.currentPageIndex = 0;
               this.currentNodeIndex = 0;
               RingProgramPage page0 = this.getPage(this.currentPageIndex);
               if (page0 == null) {
                  this.setError(RingProgram.ErrorType.buildError);
                  return;
               }

               this.callStack.initForShotMode();
            }

            int runCount = 0;
            RingProgramPage cPage = null;
            short cPageIndex = -1;

            while(!this.hadError) {
               if (cPageIndex != this.currentPageIndex) {
                  if ((cPage = this.getPage(this.currentPageIndex)) == null) {
                     this.setError(RingProgram.ErrorType.invalidPageIndexError);
                     return;
                  }

                  cPageIndex = this.currentPageIndex;
               }

               AbstractRingNode cNode = cPage.getNode(this.currentNodeIndex);
               if (cNode == null) {
                  this.setError(RingProgram.ErrorType.invalidNodeIndexError);
                  return;
               }

               byte cNodeIndex = this.currentNodeIndex;
               this.lastLastPageIndex = this.lastPageIndex;
               this.lastLastNodeIndex = this.lastNodeIndex;
               this.lastPageIndex = cPageIndex;
               this.lastNodeIndex = cNodeIndex;
               if (cNode.tickGeneration == this.tickGeneration) {
                  return;
               }

               cNode.tickGeneration = this.tickGeneration;
               if (cNode.gui != null) {
                  cNode.gui.dbgActivated = 10;
               }

               byte nextIndex = cNode.ringRun(ej, this, cPage);
               if (!this.hadError) {
                  this.currentNodeIndex = nextIndex;
                  if (cNode.isPageTransition(this, cPage)) {
                     short transPageIndex = cNode.getTransitionPageIndex(this, cPage);
                     if (transPageIndex < 0 || transPageIndex >= this.pageList.size()) {
                        this.setError(RingProgram.ErrorType.invalidPageIndexError);
                        return;
                     }

                     RingProgramPage nextPage = (RingProgramPage)this.pageList.get(transPageIndex);
                     if (!cNode.processTransition(ej, this, cPage, nextPage, cNodeIndex, transPageIndex)) {
                        return;
                     }
                  }
               }

               ++runCount;
               if (runCount > 48) {
                  return;
               }
            }
         } catch (Exception var11) {
            JointBlockRemastered.LOGGER.info("[JointBlock]R.I.N.G. ERROR:");
            JointBlockRemastered.LOGGER.info("", var11);
            this.setError(RingProgram.ErrorType.unKnownError);
         }

      }
   }

   public RingProgramPage getPage(short pageIndex) {
      if (pageIndex >= 0 && pageIndex < this.pageList.size()) {
         RingProgramPage page = (RingProgramPage)this.pageList.get(pageIndex);
         return page;
      } else {
         return null;
      }
   }

   public int nextPageIndex() {
      return this.currentPageIndex >= this.pageList.size() - 1 ? 0 : this.currentPageIndex + 1;
   }

   protected RingProgram setError(ErrorType type) {
      this.hadError = true;
      this.hadErrorType = type;
      return this;
   }

   public String getRingZeroName() {
      if (this.pageList != null && this.pageList.size() > 0) {
         RingProgramPage page = (RingProgramPage)this.pageList.get(0);
         if (page != null) {
            return page.getPageName();
         }
      }

      return "";
   }

   public RingProgram getSlefSlotPgm(byte parentSlotNum) {
      return this.entityJointed != null ? this.entityJointed.ringPrograms[parentSlotNum] : null;
   }

   public RingProgram getMasterSlotPgm(byte parentSlotNum) {
      if (this.entityJointed != null) {
         EntityJointed ejMaster = this.entityJointed.getMasterIfConnected();
         if (ejMaster != null && ejMaster != this.entityJointed && ejMaster.ringPrograms != null && parentSlotNum >= 0 && parentSlotNum < ejMaster.ringPrograms.length) {
            return ejMaster.ringPrograms[parentSlotNum];
         }
      }

      return null;
   }

   public byte nextNodeIndex(RingProgramPage cPage) {
      return cPage.incrementNodeIndex(this.currentNodeIndex);
   }

   public byte prevNodeIndex(RingProgramPage cPage) {
      return cPage.decrementNodeIndex(this.currentNodeIndex);
   }

   public byte[] writeToByteArray() {
      return this.writeToByteArray(false);
   }

   public byte[] writeToByteArray(boolean saveRunData) {
      try {
         DataBufferableOutput dos = new DataBufferableOutput();
         dos.writeByte(this.pageList.size());
         dos.writeByte(1);
         dos.writeByte(this.executeType);
         dos.writeInt(this.authFlags);
         dos.writeShort(this.currentPageIndex);
         dos.writeByte(this.currentNodeIndex);
         dos.writeBoolean(this.hadError);
         dos.writeByte(this.hadErrorType.ordinal());
         dos.writeShort(this.lastPageIndex);
         dos.writeByte(this.lastNodeIndex);
         dos.writeShort(this.lastLastPageIndex);
         dos.writeByte(this.lastLastNodeIndex);
         dos.writeInt(-560161812);
         dos.commitSegment((byte)10);
         int i = 0;

         for(int len = this.pageList.size(); i < len; ++i) {
            ((RingProgramPage)this.pageList.get(i)).write(dos, saveRunData);
         }

         if (saveRunData) {
            this.callStack.writeStack(dos);
            dos.commitSegment((byte)20);
         }

         byte[] data = dos.getByteArray();
         return data;
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public static RingProgram readFromByteArray(byte[] data) {
      return readFromByteArray(data, false);
   }

   public static RingProgram readFromByteArray(byte[] data, boolean readRunData) {
      try {
         DataBufferableInput dis = new DataBufferableInput(data);
         if (!dis.bufferNextSegment((byte)10)) {
            return ERROR_PGM.setError(RingProgram.ErrorType.buildError);
         }

         int pageCount = dis.readByte((byte)0);
         if (pageCount <= 0 || pageCount > 32) {
            return ERROR_PGM.setError(RingProgram.ErrorType.buildError);
         }

         int version = dis.readByte((byte)1);
         byte executeType = dis.readByte((byte)0);
         int authFlags = dis.readInt(15);
         short currentPageIndex = dis.readShort((short)0);
         byte currentNodeIndex = dis.readByte((byte)0);
         boolean hadError = dis.readBoolean(false);
         byte hadErrorType = dis.readByte((byte)RingProgram.ErrorType.buildError.ordinal());
         short lastPageIndex = dis.readShort((short)-1);
         byte lastNodeIndex = dis.readByte((byte)-1);
         short lastLastPageIndex = dis.readShort((short)-1);
         byte lastLastNodeIndex = dis.readByte((byte)-1);
         dis.readInt(0);
         ArrayList<RingProgramPage> pageList = null;
         short index = 0;

         for(int i = 0; i < pageCount && dis.bufferNextSegment((byte)30); ++i) {
            RingProgramPage page = RingProgramPage.createFrom(dis, readRunData);
            if (page != null) {
               if (pageList == null) {
                  pageList = new ArrayList();
               }

               pageList.add(page);
               page.pageIndex = index++;
            }
         }

         if (pageList != null) {
            RingProgram ret = new RingProgram(pageList);
            ret.executeType = executeType;
            ret.authFlags = authFlags;
            ret.currentPageIndex = currentPageIndex;
            ret.currentNodeIndex = currentNodeIndex;
            ret.hadError = hadError;
            ret.hadErrorType = RingProgram.ErrorType.get(hadErrorType);
            ret.lastPageIndex = lastPageIndex;
            ret.lastNodeIndex = lastNodeIndex;
            ret.lastLastPageIndex = lastLastPageIndex;
            ret.lastLastNodeIndex = lastLastNodeIndex;
            if (readRunData && dis.bufferNextSegment((byte)20)) {
               ret.callStack.readStack(dis);
            }

            return ret;
         }
      } catch (Exception var19) {
         var19.printStackTrace();
      }

      return ERROR_PGM.setError(RingProgram.ErrorType.buildError);
   }

   public static RingProgram createNewRingProgram(boolean withGui) {
      ArrayList<RingProgramPage> pageList = new ArrayList();
      pageList.add(RingProgramPage.createNewRingProgramPage(withGui));
      return new RingProgram(pageList);
   }

   public static enum ErrorType {
      buildError("jbring.err.build"),
      fetchValueError("jbring.err.fetchvalue"),
      invalidPageIndexError("jbring.err.invalidpageidx"),
      invalidNodeIndexError("jbring.err.invalidnodeidx"),
      unKnownError("jbring.err.unknown");

      private String errorMessageKey;

      private ErrorType(String argMesKey) {
         this.errorMessageKey = argMesKey;
      }

      public String getErrorMessageKey() {
         return this.errorMessageKey;
      }

      public static ErrorType get(byte hadErrorTypeOrdinal) {
         ErrorType[] values = values();
         return hadErrorTypeOrdinal >= 0 && hadErrorTypeOrdinal < values.length ? values[hadErrorTypeOrdinal] : unKnownError;
      }
   }
}
