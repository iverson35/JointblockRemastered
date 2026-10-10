package jp.mc.ancientred.jointblock.item;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class ItemJBController extends Item {
   public static final String TARGET_ENTITY_NBT_TAG_LB = "jb.uuidlb";
   public static final String TARGET_ENTITY_NBT_TAG_HB = "jb.uuidhb";
   public static final String TARGET_ENTITY_NAME_NBT_TAG = "jb.uname";
   public static final String TARGET_ENTITY_ID_NBT_TAG = "jb.eid";
   public static final String TARGETX_NBT_TAG = "jb.tX";
   public static final String TARGETY_NBT_TAG = "jb.tY";
   public static final String TARGETZ_NBT_TAG = "jb.tZ";
   public static final String FOR_CONSTRUCTOR_NBT_TAG = "jb.fc";

   public ItemJBController() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return InteractionResult.PASS;
      } else {
         if (!world.isClientSide) {
            BlockEntity te = WorldHelper.getTileEntity(world, pos);
            if (te instanceof TileEntityJointCore || te instanceof TileEntityBlueprintBuilder) {
               CompoundTag nbttagcompound = stack.getOrCreateTag();
               if (!nbttagcompound.contains("jb.tX")) {
                  nbttagcompound.putInt("jb.tX", pos.getX());
                  nbttagcompound.putInt("jb.tY", pos.getY());
                  nbttagcompound.putInt("jb.tZ", pos.getZ());
               }

               JBItemUtils.playDataRegistrationSound(player);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
      CompoundTag nbttagcompound = stack.getTag();
      if (nbttagcompound != null) {
         if (nbttagcompound.contains("jb.uuidlb")) {
            if (nbttagcompound.contains("jb.uname")) {
               list.add(Component.literal(nbttagcompound.getString("jb.uname")));
            } else {
               list.add(Component.translatable("jointGui.info.nonameunit"));
            }

            if (nbttagcompound.getInt("jb.eid") != 0) {
               list.add(Component.translatable("jbInfo.jbconFoundInterface"));
            }
         } else if (nbttagcompound.contains("jb.tX")) {
            list.add(Component.translatable("jbInfo.jbconInfoON", nbttagcompound.getInt("jb.tX"), nbttagcompound.getInt("jb.tY"), nbttagcompound.getInt("jb.tZ")));
         } else {
            list.add(Component.translatable("jbInfo.jbconInfoOFF"));
         }
      } else {
         list.add(Component.translatable("jbInfo.jbconInfoOFF"));
      }

   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         if (!world.isClientSide) {
            CompoundTag nbttagcompound = stack.getTag();
            if (nbttagcompound != null) {
               if (nbttagcompound.contains("jb.uuidlb")) {
                  long lbit = nbttagcompound.getLong("jb.uuidlb");
                  long hbit = nbttagcompound.getLong("jb.uuidhb");
                  List<EntityJointed> entityList = world.getEntitiesOfClass(EntityJointed.class, getBBCenteringPlayer(player));

                  for(int i = 0; i < entityList.size(); ++i) {
                     EntityJointed entityJointed = (EntityJointed)entityList.get(i);
                     UUID uuid = entityJointed.getUUID();
                     if (uuid.getLeastSignificantBits() == lbit && uuid.getMostSignificantBits() == hbit) {
                        entityJointed.startDisassemble(player);
                     }
                  }
               } else if (nbttagcompound.contains("jb.tX")) {
                  int targetX = nbttagcompound.getInt("jb.tX");
                  int targetY = nbttagcompound.getInt("jb.tY");
                  int targetZ = nbttagcompound.getInt("jb.tZ");
                  BlockEntity te = WorldHelper.getTileEntity(world, targetX, targetY, targetZ);
                  if (te instanceof TileEntityBlueprintBuilder) {
                     if (Math.sqrt(player.distanceToSqr((double)targetX + 0.5, (double)targetY + 0.5, (double)targetZ + 0.5)) <= 32.0) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiBlueprintBuilderId, player, world, new BlockPos(targetX, targetY, targetZ));
                     }
                  } else if (te instanceof TileEntityJointCore && Math.sqrt(player.distanceToSqr((double)targetX + 0.5, (double)targetY + 0.5, (double)targetZ + 0.5)) <= 32.0) {
                     JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, world, new BlockPos(targetX, targetY, targetZ));
                  }
               }
            } else if (player.getVehicle() != null) {
               EntityJointed entityJointed = EntityJointed.getEntityJointedIfPlayerIsRiding(player, 0);
               if (entityJointed != null) {
                  nbttagcompound = new CompoundTag();
                  UUID uniqueID = entityJointed.getUUID();
                  nbttagcompound.putLong("jb.uuidlb", uniqueID.getLeastSignificantBits());
                  nbttagcompound.putLong("jb.uuidhb", uniqueID.getMostSignificantBits());
                  if (entityJointed.unitName != null && entityJointed.unitName.length() > 0) {
                     nbttagcompound.putString("jb.uname", entityJointed.unitName);
                  } else if (nbttagcompound.contains("jb.uname")) {
                     nbttagcompound.remove("jb.uname");
                  }

                  stack.setTag(nbttagcompound);
               }
            }
         }

         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   public static void removeReferringPositionTag(Player entityPlayer, int posX, int posY, int posZ) {
      Iterator var4 = entityPlayer.getInventory().items.iterator();

      while(var4.hasNext()) {
         ItemStack itemStack = (ItemStack)var4.next();
         if (!CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBController) {
            CompoundTag nbttagcompound = itemStack.getTag();
            if (nbttagcompound != null && nbttagcompound.contains("jb.tX") && nbttagcompound.getInt("jb.tX") == posX && nbttagcompound.getInt("jb.tY") == posY && nbttagcompound.getInt("jb.tZ") == posZ) {
               itemStack.setTag((CompoundTag)null);
            }
         }
      }

   }

   public static void setEntityUIDtoController(Player conductor, UUID uniqueID, String unitName, int posX, int posY, int posZ) {
      if (conductor != null && conductor.getInventory() != null) {
         List<ItemStack> mainInv = conductor.getInventory().items;
         if (mainInv != null) {
            ItemStack stackOffHand;
            for(int i = 0; i < mainInv.size(); ++i) {
               stackOffHand = (ItemStack)mainInv.get(i);
               if (isItemStackJBController(stackOffHand)) {
                  setUIDToNBT(uniqueID, unitName, posX, posY, posZ, stackOffHand);
               }
            }

            List<ItemStack> offHandInventory = conductor.getInventory().offhand;
            if (offHandInventory != null && !offHandInventory.isEmpty()) {
               stackOffHand = (ItemStack)offHandInventory.get(0);
               if (isItemStackJBController(stackOffHand)) {
                  setUIDToNBT(uniqueID, unitName, posX, posY, posZ, stackOffHand);
               }

            }
         }
      }
   }

   private static void setUIDToNBT(UUID uniqueID, String unitName, int posX, int posY, int posZ, ItemStack itemStack) {
      CompoundTag nbttagcompound = itemStack.getTag();
      if (nbttagcompound != null && nbttagcompound.contains("jb.tX") && nbttagcompound.getInt("jb.tX") == posX && nbttagcompound.getInt("jb.tY") == posY && nbttagcompound.getInt("jb.tZ") == posZ) {
         nbttagcompound.putLong("jb.uuidlb", uniqueID.getLeastSignificantBits());
         nbttagcompound.putLong("jb.uuidhb", uniqueID.getMostSignificantBits());
         if (unitName != null && unitName.length() > 0) {
            nbttagcompound.putString("jb.uname", unitName);
         } else if (nbttagcompound.contains("jb.uname")) {
            nbttagcompound.remove("jb.uname");
         }
      }

   }

   public static EntityJointed getRelatedEntityJointedServer(ItemStack itemStack, Level world, Player player) {
      CompoundTag nbttagcompound = itemStack.getTag();
      if (nbttagcompound != null && nbttagcompound.contains("jb.uuidlb")) {
         long lbit = nbttagcompound.getLong("jb.uuidlb");
         long hbit = nbttagcompound.getLong("jb.uuidhb");
         List<EntityJointed> entityList = world.getEntitiesOfClass(EntityJointed.class, getBBCenteringPlayer(player));

         for(int i = 0; i < entityList.size(); ++i) {
            EntityJointed entityJointed = (EntityJointed)entityList.get(i);
            UUID uuid = entityJointed.getUUID();
            if (uuid.getLeastSignificantBits() == lbit && uuid.getMostSignificantBits() == hbit) {
               return entityJointed;
            }
         }
      }

      return null;
   }

   private static AABB getBBCenteringPlayer(Player player) {
      return JBCommonUtil.getBoundingBox(player.getX(), player.getY(), player.getZ(), player.getX(), player.getY(), player.getZ()).inflate(32.0, 32.0, 32.0);
   }

   public static void procesConductorsJBController(EntityJointed entityToDisassemble, Player conductor) {
      if (entityToDisassemble != null && conductor != null && conductor.getInventory() != null) {
         List<ItemStack> mainInv = conductor.getInventory().items;
         if (mainInv != null) {
            ItemStack stackOffHand;
            for(int i = 0; i < mainInv.size(); ++i) {
               stackOffHand = (ItemStack)mainInv.get(i);
               if (!CompatHelper.isStackEmptyOrNull(stackOffHand) && isJBControllerRelatedTo(stackOffHand, entityToDisassemble)) {
                  setAfterDisassembleTarget(stackOffHand, entityToDisassemble);
               }
            }

            List<ItemStack> offHandInventory = conductor.getInventory().offhand;
            if (offHandInventory != null && !offHandInventory.isEmpty()) {
               stackOffHand = (ItemStack)offHandInventory.get(0);
               if (!CompatHelper.isStackEmptyOrNull(stackOffHand) && isJBControllerRelatedTo(stackOffHand, entityToDisassemble)) {
                  setAfterDisassembleTarget(stackOffHand, entityToDisassemble);
               }

            }
         }
      }
   }

   public static boolean isJBControllerRelatedTo(ItemStack itemStack, EntityJointed entityJointed) {
      if (!CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBController && entityJointed != null) {
         CompoundTag nbttagcompound = itemStack.getTag();
         if (nbttagcompound == null) {
            return false;
         } else {
            long lbit = nbttagcompound.getLong("jb.uuidlb");
            long hbit = nbttagcompound.getLong("jb.uuidhb");
            UUID uuid = entityJointed.getUUID();
            return uuid.getLeastSignificantBits() == lbit && uuid.getMostSignificantBits() == hbit;
         }
      } else {
         return false;
      }
   }

   public static void setAfterDisassembleTarget(ItemStack itemStack, EntityJointed entityJointed) {
      if (!CompatHelper.isStackEmptyOrNull(itemStack) && entityJointed != null) {
         CompoundTag nbttagcompound = itemStack.getTag();
         if (nbttagcompound != null) {
            nbttagcompound.putInt("jb.tX", Mth.floor(entityJointed.dissassemblePosX));
            nbttagcompound.putInt("jb.tY", Mth.floor(entityJointed.dissassemblePosY));
            nbttagcompound.putInt("jb.tZ", Mth.floor(entityJointed.dissassemblePosZ));
            nbttagcompound.remove("jb.uuidlb");
            nbttagcompound.remove("jb.uuidhb");
         }
      }
   }

   public static boolean isItemStackJBController(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBController;
   }

   @Override
   public void inventoryTick(ItemStack itemStack, Level world, Entity entity, int slotNum, boolean isCurrentItem) {
      isCurrentItem = CompatHelper.isStackPlayerCurrentEquipedMainOrSub(entity, itemStack);
      if (world.isClientSide) {
         this.onUpdateClient(itemStack, world, entity, slotNum, isCurrentItem);
      } else {
         CompoundTag nbttagcompound = itemStack.getTag();
         if (nbttagcompound != null) {
            if (isCurrentItem && entity instanceof ServerPlayer && nbttagcompound.contains("jb.uuidlb")) {
               EntityJointed ej = getRelatedEntityJointedServer(itemStack, world, (Player)entity);
               if (ej != null && !ej.isRemoved() && ej.entitySeatPart0 != null && ej.entitySeatPart0.isAsInteractableJoint) {
                  nbttagcompound.putInt("jb.eid", ej.getId());
                  return;
               }
            }

            if (nbttagcompound.contains("jb.eid")) {
               nbttagcompound.remove("jb.eid");
            }
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public void onUpdateClient(ItemStack itemStack, Level world, Entity entity, int slotNum, boolean isCurrentItem) {
      if (isCurrentItem && entity instanceof LocalPlayer) {
         if (!itemStack.hasTag() || itemStack.getTag().getInt("jb.eid") == 0) {
            return;
         }

         LocalPlayer entityPlayerSP = (LocalPlayer)entity;
         EntityJointed ej = getRelatedEntityJointedClient(itemStack, world, entityPlayerSP);
         if (ej == null || ej.isRemoved()) {
            return;
         }

         EntitySeatPart sp = ej.entitySeatPart0;
         if (sp == null || sp.isRemoved() || CompatHelper.getRiddenByEntity(sp) != null) {
            return;
         }

         sp.updateOperetionStateFromOutside(entityPlayerSP);
      }

   }

   public static EntityJointed getRelatedEntityJointedClient(ItemStack itemStack, Level world, Player player) {
      CompoundTag nbttagcompound = itemStack.getTag();
      if (nbttagcompound != null && nbttagcompound.contains("jb.eid")) {
         int entityId = nbttagcompound.getInt("jb.eid");
         if (entityId != 0) {
            Entity entity = world.getEntity(entityId);
            if (entity != null && entity instanceof EntityJointed) {
               return (EntityJointed)entity;
            }
         }
      }

      return null;
   }
}
