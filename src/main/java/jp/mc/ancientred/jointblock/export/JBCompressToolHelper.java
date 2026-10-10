package jp.mc.ancientred.jointblock.export;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;

/**
 * GZIP + NBT payload helpers, ported from the 1.12.2 {@code JBCompressToolHelper}.
 *
 * <p>1.20.1 API changes only: {@code CompressedStreamTools.func_74800_a/func_152456_a} became
 * {@link NbtIo#write(net.minecraft.nbt.CompoundTag, java.io.DataOutput)} /
 * {@link NbtIo#read(java.io.DataInput, NbtAccounter)} (1.20.1 {@code readCompressed(InputStream)}
 * takes no quota object, so the {@link NbtAccounter} overload is used), {@code NBTSizeTracker}
 * became {@link NbtAccounter} and {@code PacketBuffer} became {@link FriendlyByteBuf}. The wire
 * layout (gz stream, short-length sentinel {@code < 0} == null) is unchanged.
 */
public class JBCompressToolHelper {
   public static byte[] compress(CompoundTag tag) throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
      DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));

      try {
         NbtIo.write(tag, dataoutputstream);
      } finally {
         dataoutputstream.close();
      }

      return bytearrayoutputstream.toByteArray();
   }

   public static CompoundTag readDecompress(byte[] data, NbtAccounter sizeTracker) throws IOException {
      DataInputStream is = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data))));

      CompoundTag compoundTag;
      try {
         compoundTag = NbtIo.read(is, sizeTracker);
      } finally {
         is.close();
      }

      return compoundTag;
   }

   public static CompoundTag readNBTTagCompoundFromBuffer(FriendlyByteBuf buf) throws IOException {
      short short1 = buf.readShort();
      if (short1 < 0) {
         return null;
      } else {
         byte[] abyte = new byte[short1];
         buf.readBytes(abyte);
         DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(abyte))));

         CompoundTag compoundTag;
         try {
            compoundTag = NbtIo.read(datainputstream, new NbtAccounter(2097152L));
         } finally {
            datainputstream.close();
         }

         return compoundTag;
      }
   }
}
