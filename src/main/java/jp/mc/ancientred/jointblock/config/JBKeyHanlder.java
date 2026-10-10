package jp.mc.ancientred.jointblock.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The twelve "joint key bind" mappings driven by the assembled unit (slots 0-7 plus the
 * four arrow-style movement keys), and the double-click/mouse-wheel edge detection the
 * 1.12.2 mod used.
 *
 * <p>1.12.2 registered these with {@code ClientRegistry.registerKeyBinding} from the
 * constructor; 1.20.1 requires {@code RegisterKeyMappingsEvent} on the mod bus, so
 * {@link #registerKeyMappings} does the registration and {@link #init} only builds the
 * mappings. Default codes are the 1.12.2 LWJGL2 codes translated to their GLFW equivalents
 * (Z,X,C,V,B,N,M,comma then Up,Down,Left,Right) - {@code InputConstants} constants are
 * already GLFW, so the LWJGL2 values 44..51/200/208/203/205 become 90/88/67/86/66/78/77/44
 * and 265/264/263/262.
 */
@OnlyIn(Dist.CLIENT)
public class JBKeyHanlder {
   public static JBKeyHanlder instance;
   public byte keyPressedFlag;
   public final KeyMapping[] keys;
   public int mouseWheelState;
   public static String[] descs;
   public static int[] keyValues;
   public static boolean[] keyPressedPrev;
   public static int KEY_INDEX_UP = 8;
   public static int KEY_INDEX_DW = 9;
   public static int KEY_INDEX_LF = 10;
   public static int KEY_INDEX_RI = 11;

   public JBKeyHanlder() {
      descs = new String[]{"key.jointkeybind0.desc", "key.jointkeybind1.desc", "key.jointkeybind2.desc", "key.jointkeybind3.desc", "key.jointkeybind4.desc", "key.jointkeybind5.desc", "key.jointkeybind6.desc", "key.jointkeybind7.desc", "key.jointkeybindUp.desc", "key.jointkeybindDw.desc", "key.jointkeybindLf.desc", "key.jointkeybindRi.desc"};
      keyValues = new int[]{InputConstants.KEY_Z, InputConstants.KEY_X, InputConstants.KEY_C, InputConstants.KEY_V, InputConstants.KEY_B, InputConstants.KEY_N, InputConstants.KEY_M, InputConstants.KEY_COMMA, InputConstants.KEY_UP, InputConstants.KEY_DOWN, InputConstants.KEY_LEFT, InputConstants.KEY_RIGHT};
      this.keys = new KeyMapping[descs.length];

      for (int i = 0; i < descs.length; ++i) {
         this.keys[i] = new KeyMapping(descs[i], keyValues[i], "key.jointblock.category");
      }

      keyPressedPrev = new boolean[descs.length];
   }

   /** 1.12.2 {@code CommonProxyClient.init}: {@code JBKeyHanlder.instance = new JBKeyHanlder();} */
   public static void init() {
      instance = new JBKeyHanlder();
   }

   /** 1.12.2 {@code ClientRegistry.registerKeyBinding} for every mapping, on the mod bus. */
   public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
      for (KeyMapping key : instance.keys) {
         event.register(key);
      }
   }

   public static boolean isKeyPressed(int keyIndex) {
      return instance.keys[keyIndex].isDown();
   }

   public static boolean detectKeyOnDown(int keyIndex) {
      if (keyPressedPrev == null) {
         return false;
      } else {
         boolean nowKey = isKeyPressed(keyIndex);
         boolean res = nowKey && !keyPressedPrev[keyIndex];
         keyPressedPrev[keyIndex] = nowKey;
         return res;
      }
   }
}
