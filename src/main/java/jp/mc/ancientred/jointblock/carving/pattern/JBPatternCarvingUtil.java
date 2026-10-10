package jp.mc.ancientred.jointblock.carving.pattern;

import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import net.minecraft.world.entity.player.Player;

public class JBPatternCarvingUtil {
   public static JBCarvingPattern[] patternTable = new JBCarvingPattern[8];
   public static final int PATTERN_INDEX_MAXK = 3;
   public static final int PATTERN_INDEX_BOX = 0;
   public static final int PATTERN_INDEX_SLOPE = 1;
   public static final int PATTERN_INDEX_CORNERA = 2;
   public static final int PATTERN_INDEX_CORNERB = 3;
   public static final int MAX_PATTERN_NUM = 3;

   public static void setUpPatterns() {
      patternTable[0] = new PatternBox();
      patternTable[1] = new PatternSlope();
      patternTable[2] = new PatternCorner();
      patternTable[3] = new PatternCornerB();
   }

   public static int getNextPatternDefaultValue(int valuePattern, int toggleDir, int face) {
      int patternIndex = valuePattern >> 14 & 3;
      if (patternIndex == 3) {
         patternIndex = 2;
      }

      patternIndex += toggleDir;
      if (patternIndex > 2) {
         patternIndex = 0;
      }

      if (patternIndex < 0) {
         patternIndex = 2;
      }

      return getDefaultPatternValue(patternIndex, face);
   }

   public static int getDefaultPatternValue(int patternIndex, int face) {
      if (patternIndex == 0) {
         return 0;
      } else {
         JBCarvingPattern pat = patternTable[patternIndex];
         return pat == null ? 0 : patternIndex << 14 | pat.getDefaultPatternValue(face);
      }
   }

   public static JBCarvingPattern getPatternFor(int valuePattern) {
      int patternIndex = valuePattern >> 14 & 3;
      return patternTable[patternIndex];
   }

   public static int getPatternIndexFor(int valuePattern) {
      return valuePattern >> 14 & 3;
   }

   public static String getPatternShortName(int valuePattern) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? "#Err#" : pat.getPatternShortName();
   }

   public static int getPatternSide(int valuePattern) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? 0 : pat.getPatternSide(valuePattern);
   }

   public static int getPatternFace(int valuePattern) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? 0 : pat.getPatternFace(valuePattern);
   }

   public static int getPatternArgs(int valuePattern) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? 0 : pat.getPatternArgs(valuePattern);
   }

   public static int fixFacingForValueCopySrc(int valuePattern, int baseFacing, Player player) {
      int facingTwdPlayer = JBRotationUtil.getPlayerFacingOpposite(player);
      facingTwdPlayer = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(baseFacing, facingTwdPlayer);
      JBCarvingPattern pat = getPatternFor(valuePattern);
      if (pat == null) {
         return valuePattern;
      } else {
         switch (facingTwdPlayer) {
            case 2:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
            case 3:
            default:
               break;
            case 4:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               break;
            case 5:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
         }

         return valuePattern;
      }
   }

   public static int fixFacingForValueCopyDst(int valuePattern, int baseFacing, Player player) {
      int facingTwdPlayer = JBRotationUtil.getPlayerFacingOpposite(player);
      facingTwdPlayer = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(baseFacing, facingTwdPlayer);
      JBCarvingPattern pat = getPatternFor(valuePattern);
      if (pat == null) {
         return valuePattern;
      } else {
         switch (facingTwdPlayer) {
            case 2:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
            case 3:
            default:
               break;
            case 4:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               break;
            case 5:
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
               valuePattern = pat.rotCopyValue3to4(valuePattern);
         }

         return valuePattern;
      }
   }

   public static int turnPatternSide(int valuePattern, int toggleDir) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? valuePattern : pat.turnPatternSide(valuePattern, toggleDir);
   }

   public static int turnPatternFace(int valuePattern, int toggleDir) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? valuePattern : pat.turnPatternFace(valuePattern, toggleDir);
   }

   public static int modifyPatternIndex(int valuePattern, int patternIndex) {
      return (patternIndex & 3) << 14 | valuePattern & -49153;
   }

   public static int modifyPatternArgs(int argsNum, int valuePattern, int toggleDir) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      if (pat == null) {
         return valuePattern;
      } else {
         switch (argsNum) {
            case 0:
               return pat.modArg0(valuePattern, toggleDir);
            case 1:
               return pat.modArg1(valuePattern, toggleDir);
            case 2:
               return pat.modArg2(valuePattern, toggleDir);
            case 3:
               return pat.modArg3(valuePattern, toggleDir);
            default:
               return valuePattern;
         }
      }
   }

   public static int copyValue(int valuePattern, PatternValueCopyType copyType) {
      JBCarvingPattern pat = getPatternFor(valuePattern);
      return pat == null ? valuePattern : pat.copyValue(valuePattern, copyType);
   }
}
