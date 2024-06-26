package noppes.npcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandBase.CoordinateArg;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.IMetods;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.wrapper.data.DataElement;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.recipebook.GuiNpcButtonRecipeTab;
import noppes.npcs.client.gui.recipebook.GuiNpcRecipeBook;
import noppes.npcs.client.model.part.ModelDataShared;
import noppes.npcs.containers.SlotNpcCrafting;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;

public class AdditionalMethods implements IMetods {

	public final static AdditionalMethods instance = new AdditionalMethods();
	
	public static boolean canAddItemAfterRemoveItems(NonNullList<ItemStack> inventory, ItemStack addStack,
			Map<ItemStack, Integer> items, boolean ignoreDamage, boolean ignoreNBT) {
		if (inventory == null || addStack.isEmpty()) {
			return false;
		}
		NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(inventory.size(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.size(); ++i) {
			if (NoppesUtilServer.IsItemStackNull(inventory.get(i))) {
				continue;
			}
			inv.set(i, inventory.get(i).copy());
		}
		if (items != null && !items.isEmpty()) {
			for (ItemStack stack : items.keySet()) {
				if (NoppesUtilServer.IsItemStackNull(stack)) {
					continue;
				}
				int count = items.get(stack);
				for (int i = 0; i < inv.size(); ++i) {
					ItemStack is = inv.get(i);
					if (NoppesUtilServer.IsItemStackNull(is)) {
						continue;
					}
					if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
						if (count < is.getCount()) {
							is.splitStack(count);
							inv.set(i, is);
							count = 0;
						} else {
							count -= is.getCount();
							inv.set(i, ItemStack.EMPTY);
						}
						if (count <= 0) {
							break;
						}
					}
				}
			}
		}
		for (int i = 0; i < inv.size(); ++i) {
			if (inv.get(i).isEmpty() || NoppesUtilPlayer.compareItems(addStack, inv.get(i), ignoreDamage, ignoreNBT)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
		return !stack1.isEmpty() && stack1.getItem() == stack2.getItem()
				&& (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack1, stack2) && stack1.isStackable()
				&& stack1.getCount() < stack1.getMaxStackSize();
	}

	public static boolean canRemoveItems(Map<ItemStack, Integer> inventory, Map<ItemStack, Integer> items,
			boolean ignoreDamage, boolean ignoreNBT) {
		if (inventory == null || items == null || items.isEmpty()) {
			return false;
		}
		for (ItemStack stack : items.keySet()) {
			int count = items.get(stack);
			if (NoppesUtilServer.IsItemStackNull(stack)) {
				continue;
			}
			for (ItemStack is : inventory.keySet()) {
				if (NoppesUtilServer.IsItemStackNull(is)) {
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
					count -= is.getCount();
				}
			}
			if (count > 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean canRemoveItems(Map<ItemStack, Integer> inventory, NonNullList<ItemStack> items,
			boolean ignoreDamage, boolean ignoreNBT) {
		if (inventory == null || items.isEmpty()) {
			return false;
		}
		Map<ItemStack, Integer> inv = Maps.<ItemStack, Integer>newLinkedHashMap();
		if (items == null || !items.isEmpty()) {
			Map<ItemStack, Integer> map = Maps.<ItemStack, Integer>newHashMap();
			for (int slot = 0; slot < items.size(); slot++) {
				ItemStack stack = items.get(slot);
				if (NoppesUtilServer.IsItemStackNull(stack)) {
					continue;
				}
				boolean has = false;
				for (ItemStack s : map.keySet()) {
					if (NoppesUtilPlayer.compareItems(stack, s, ignoreDamage, ignoreNBT)) {
						has = true;
						map.put(s, map.get(s) + stack.getCount());
						break;
					}
				}
				if (!has) {
					map.put(stack, stack.getCount());
				}
			}
			List<Entry<ItemStack, Integer>> list = Lists.newArrayList(map.entrySet());
			Collections.sort(list, new Comparator<Entry<ItemStack, Integer>>() {
				public int compare(Entry<ItemStack, Integer> st_0, Entry<ItemStack, Integer> st_1) {
					return ((Integer) st_1.getValue()).compareTo((Integer) st_0.getValue());
				}
			});
			for (Entry<ItemStack, Integer> entry : list) {
				inv.put(entry.getKey(), entry.getValue());
			}
		}
		return AdditionalMethods.canRemoveItems(inventory, inv, ignoreDamage, ignoreNBT);
	}

	public static boolean canRemoveItems(NonNullList<ItemStack> inventory, ItemStack stack, boolean ignoreDamage,
			boolean ignoreNBT) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Map<ItemStack, Integer> items = Maps.<ItemStack, Integer>newHashMap();
		items.put(stack, stack.getCount());
		return AdditionalMethods.canRemoveItems(inventory, items, ignoreDamage, ignoreNBT);
	}

	public static boolean canRemoveItems(NonNullList<ItemStack> inventory, Map<ItemStack, Integer> items,
			boolean ignoreDamage, boolean ignoreNBT) {
		if (inventory == null) {
			return false;
		}
		if (items == null || items.isEmpty()) {
			return true;
		}
		Map<ItemStack, Integer> inv = Maps.<ItemStack, Integer>newHashMap();
		for (int i = 0; i < inventory.size(); ++i) {
			ItemStack stack = inventory.get(i);
			if (NoppesUtilServer.IsItemStackNull(stack) || stack.isEmpty()) {
				continue;
			}
			boolean found = false;
			for (ItemStack st : inv.keySet()) {
				if (NoppesUtilServer.IsItemStackNull(st) || st.isEmpty()) {
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, st, false, false)) {
					inv.put(st, inv.get(st) + stack.getCount());
					found = true;
					break;
				}
			}
			if (!found) {
				inv.put(stack, stack.getCount());
			}
		}
		return AdditionalMethods.canRemoveItems(inv, items, ignoreDamage, ignoreNBT);
	}

	public static boolean containsDeleteColor(Set<String> set, String text, boolean ignoreCase) {
		if (set == null || text == null) {
			return false;
		}
		for (String str : set) {
			if (AdditionalMethods.equalsDeleteColor(str, text, ignoreCase)) {
				return true;
			}
		}
		return false;
	}

	public static EntityNPCInterface copyToGUI(EntityNPCInterface npcParent, World world, boolean copyRotation) {
		NBTTagCompound npcNbt = new NBTTagCompound();
		if (npcParent == null) {
			npcParent = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
		}
		npcParent.writeEntityToNBT(npcNbt);
		npcParent.writeToNBTOptional(npcNbt);
		Entity entity = EntityList.createEntityFromNBT(npcNbt, world);
		if (!(entity instanceof EntityNPCInterface)) {
			entity = EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), world);
			if (!(entity instanceof EntityNPCInterface)) {
				return npcParent;
			}
			entity.readFromNBT(npcNbt);
		}
		EntityNPCInterface npc = (EntityNPCInterface) entity;
		MarkData.get(npc).marks.clear();
		npc.display.setShowName(1);
		npc.setHealth(npc.getMaxHealth());
		npc.deathTime = 0;
		npc.rotationYaw = 0;
		npc.prevRotationYaw = 0;
		npc.rotationYawHead = 0;
		npc.rotationPitch = 0;
		npc.prevRotationPitch = 0;
		npc.ais.orientation = 0;
		if (copyRotation) {
			npc.rotationYaw = npcParent.rotationYawHead;
			npc.prevRotationYaw = npcParent.rotationYawHead;
			npc.rotationYawHead = npcParent.rotationYawHead;
			npc.prevRotationYawHead = npcParent.rotationYawHead;
			
			npc.rotationPitch = npcParent.rotationPitch;
			npc.prevRotationPitch = npcParent.rotationPitch;
			npc.ais.orientation = npcParent.ais.orientation;
		}
		npc.ais.setStandingType(1);
		npc.ticksExisted = 100;
		if (npc instanceof EntityCustomNpc && npcParent instanceof EntityCustomNpc
				&& ((EntityCustomNpc) npc).modelData instanceof ModelDataShared
				&& ((EntityCustomNpc) npcParent).modelData instanceof ModelDataShared) {
			((ModelDataShared) ((EntityCustomNpc) npc).modelData).entity = ((ModelDataShared) ((EntityCustomNpc) npcParent).modelData).entity;
		}
		return npc;
	}

	public static String deleteSpase(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		while (str.indexOf(" ") != -1) {
			str = str.replace(" ", "");
		}
		while (str.indexOf("" + ((char) 9)) != -1) {
			str = str.replace("" + ((char) 9), "");
		}
		return str;
	}

	public static double distanceTo(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.distanceTo(pos0.getX(), pos0.getY(), pos0.getZ(), pos1.getX(), pos1.getY(),
				pos1.getZ());
	}

	public static boolean equalsDeleteColor(String str0, String str1, boolean ignoreCase) {
		str0 = AdditionalMethods.instance.deleteColor(str0);
		str1 = AdditionalMethods.instance.deleteColor(str1);
		return ignoreCase ? str0.equalsIgnoreCase(str1) : str0.equals(str1);
	}

	public static RayTraceRotate getAngles3D(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.getAngles3D(pos0.getX() + 0.5d, pos0.getY() + 0.5d, pos0.getZ() + 0.5d,
				pos1.getX() + 0.5d, pos1.getY() + 0.5d, pos1.getZ() + 0.5d);
	}

	public static RayTraceRotate getAngles3D(Entity entity, Entity target) {
		return AdditionalMethods.instance.getAngles3D(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ,
				target.posX, target.posY + target.getEyeHeight(), target.posZ);
	}

	public static List<IDataElement> getClassData(Object obj, boolean onlyPublic, boolean addConstructor) {
		List<IDataElement> list = Lists.newArrayList();
		Class<?> cz = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();
		/** Constructors */
		if (addConstructor) {
			Constructor<?>[] cns = onlyPublic ? cz.getConstructors() : cz.getDeclaredConstructors();
			for (Constructor<?> c : cns) {
				list.add(new DataElement(c, obj));
			}
		}
		Map<String, Class<?>> classes = Maps.newHashMap();
		Map<String, Field> fields = Maps.newHashMap();
		Map<String, Method> methods = Maps.newHashMap();

		for (Class<?> cl : onlyPublic ? cz.getClasses() : cz.getDeclaredClasses()) {
			if (!classes.containsKey(cl.getSimpleName())) {
				classes.put(cl.getSimpleName(), cl);
			}
		}
		/** Data */
		List<Class<?>> czs = Lists.newArrayList();
		czs.add(cz);
		while (cz.getSuperclass() != Object.class && !czs.contains(cz.getSuperclass())) {
			czs.add(cz.getSuperclass());
			cz = cz.getSuperclass();
		}
		for (Class<?> c : czs) {
			for (Field f : onlyPublic ? c.getFields() : c.getDeclaredFields()) {
				if (!fields.containsKey(f.getName())) {
					fields.put(f.getName(), f);
				}
			}
			for (Method m : onlyPublic ? c.getMethods() : c.getDeclaredMethods()) {
				if (!methods.containsKey(m.getName())) {
					methods.put(m.getName(), m);
				}
			}
		}
		/** Fields */
		if (fields.size() > 0) {
			List<String> sortNames = Lists.newArrayList(fields.keySet());
			Collections.sort(sortNames);
			List<String> names = Lists.newArrayList();
			for (String name : sortNames) {
				boolean next = false;
				if (names.contains(name)) {
					continue;
				}
				Field f = fields.get(name);
				for (IDataElement td : list) {
					if (td.getObject().equals(f)) {
						next = true;
						break;
					}
				}
				if (next) {
					continue;
				}
				list.add(new DataElement(f, obj));
				names.add(f.getName());
			}
		}
		/** Methods */
		if (methods.size() > 0) {
			List<String> sortNames = Lists.newArrayList(methods.keySet());
			Collections.sort(sortNames);
			List<String> names = Lists.newArrayList();
			for (String name : sortNames) {
				boolean next = false;
				if (names.contains(name)) {
					continue;
				}
				Method m = methods.get(name);
				for (IDataElement td : list) {
					if (td.getObject().equals(m)) {
						next = true;
						break;
					}
				}
				if (next) {
					continue;
				}
				list.add(new DataElement(m, obj));
				names.add(m.getName());
			}
		}
		/** Classes */
		if (classes.size() > 0) {
			List<String> sortNames = Lists.newArrayList(classes.keySet());
			Collections.sort(sortNames);
			for (String name : sortNames) {
				list.add(new DataElement(classes.get(name), obj));
			}
		}
		return list;
	}

	public static String getDeleteColor(Map<?, ?> map, String str0, boolean isKey, boolean ignoreCase) {
		for (Object obj : (isKey ? map.keySet() : map.values())) {
			if (AdditionalMethods.equalsDeleteColor(obj.toString(), str0, ignoreCase)) {
				return obj.toString();
			}
		}
		return "";
	}

	public static Entity getEntityByUUID(UUID uuid, World startWorld) {
		Entity e = AdditionalMethods.getEntityInWorld(uuid, startWorld);
		if (e == null) {
			MinecraftServer server = CustomNpcs.Server != null ? CustomNpcs.Server
					: startWorld != null && startWorld.getMinecraftServer() != null ? startWorld.getMinecraftServer()
							: CustomNpcs.proxy.getPlayer() != null && CustomNpcs.proxy.getPlayer().world != null
									&& CustomNpcs.proxy.getPlayer().world.getMinecraftServer() != null
											? CustomNpcs.proxy.getPlayer().world.getMinecraftServer()
											: null;
			if (server != null) {
				for (WorldServer world : server.worlds) {
					if (world.equals(startWorld)) {
						continue;
					}
					e = AdditionalMethods.getEntityInWorld(uuid, world);
					if (e != null) {
						return e;
					}
				}
			}
		}
		return e;
	}

	public static Entity getEntityInWorld(UUID uuid, World world) {
		for (Entity entity : world.loadedEntityList) {
			if (entity.getUniqueID().equals(uuid)) {
				return entity;
			}
		}
		List<Entity> unloadedEntityList = ObfuscationHelper.getValue(World.class, world, 4);
		for (Entity entity : unloadedEntityList) {
			if (entity.getUniqueID().equals(uuid)) {
				return entity;
			}
		}
		return null;
	}

	public static List<File> getFiles(File dir, String index) {
		List<File> list = Lists.newArrayList();
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return list;
		}
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				list.addAll(AdditionalMethods.getFiles(f, index));
				continue;
			}
			if (!f.isFile() || !f.getName().toLowerCase().endsWith(index.toLowerCase())) {
				continue;
			}
			list.add(f);
		}
		return list;
	}

	public static List<File> getFiles(Object dir, String index) {
		List<File> list = Lists.newArrayList();
		if (!(dir instanceof File)) {
			return list;
		}
		return AdditionalMethods.getFiles((File) dir, index);
	}

	public static Map<ItemStack, Boolean> getInventoryItemCount(EntityPlayer player, IInventory inventory) {
		Map<ItemStack, Integer> counts = Maps.<ItemStack, Integer>newHashMap();
		Map<ItemStack, ItemStack> base = Maps.<ItemStack, ItemStack>newHashMap();
		List<ItemStack> list = Lists.<ItemStack>newArrayList();
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (NoppesUtilServer.IsItemStackNull(stack)) {
				continue;
			}
			boolean has = false;
			if (stack.getMaxStackSize() > 1) {
				for (ItemStack s : list) {
					if (NoppesUtilPlayer.compareItems(stack, s, false, false)) {
						if (s.getCount() != s.getMaxStackSize()) {
							if (stack.getCount() + s.getCount() > stack.getMaxStackSize()) {
								ItemStack c = stack.copy();
								c.setCount((stack.getCount() + s.getCount()) % s.getMaxStackSize());
								s.setCount(s.getMaxStackSize());
								list.add(c);
							} else {
								s.setCount(stack.getCount() + s.getCount());
							}
							has = true;
							break;
						}
					}
				}
			}
			if (!has) {
				list.add(stack.copy());
			}
		}
		Collections.sort(list, new Comparator<ItemStack>() {
			public int compare(ItemStack st_0, ItemStack st_1) {
				return ((Integer) st_1.getCount()).compareTo((Integer) st_0.getCount());
			}
		});
		for (ItemStack stack : list) {
			for (ItemStack s : counts.keySet()) {
				if (NoppesUtilPlayer.compareItems(stack, s, false, false)) {
					counts.put(s, counts.get(s) + stack.getCount());
					base.put(stack, s);
					stack = s;
					break;
				}
			}
			if (!counts.containsKey(stack)) {
				counts.put(stack, stack.getCount());
				base.put(stack, stack);
			}
		}
		Map<ItemStack, Boolean> map = Maps.<ItemStack, Boolean>newHashMap();
		for (ItemStack stack : counts.keySet()) {
			int count = 0;
			for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
				ItemStack s = player.inventory.mainInventory.get(i);
				if (NoppesUtilServer.IsItemStackNull(s)) {
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, s, false, false)) {
					count += s.getCount();
				}
			}
			boolean has = count >= counts.get(stack);
			for (ItemStack inInvStack : base.keySet()) {
				if (base.get(inInvStack) == stack) {
					map.put(inInvStack, has);
				}
			}
		}
		Map<ItemStack, Boolean> total = Maps.<ItemStack, Boolean>newLinkedHashMap();
		for (ItemStack stack : list) {
			total.put(stack, map.get(stack));
		}
		return total;
	}

	public static String getLastColor(String color, String str) {
		char c = (char) 167;
		if (str.lastIndexOf(c) != -1) {
			if (str.lastIndexOf(c) + 1 < str.length()) {
				int start = str.lastIndexOf(c);
				int end = start + 2;
				while (start - 2 >= 0 && str.charAt(start - 2) == c) {
					start -= 2;
				}
				color = str.substring(start, end);
			} else {
				color = AdditionalMethods.getLastColor(color, str.substring(0, str.length() - 1));
			}
		}
		return color;
	}

	public static RayTraceVec getPosition(BlockPos pos, double yaw, double pitch, double radius) {
		return AdditionalMethods.instance.getPosition(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, yaw,
				pitch, radius);
	}

	/**
	 * 1234567890.9 to 1,2G
	 * 
	 * @param value
	 *            - any number
	 * @param color
	 *            - need set color
	 * @return string
	 */
	public static String getTextReducedNumber(double value, boolean isInteger, boolean color, boolean notPfx) {
		if (value == 0) {
			return "0";
		}
		String chr = "" + ((char) 167);
		String chrPR = new String(Character.toChars(0x2248));
		chrPR = "";
		String type = "";
		String sufc = "";
		if (value == 0L) {
			return String.valueOf(value).replace(".", ",");
		}
		double corr = value;
		int exp = 0;
		boolean negatively = false;

		if (value <= 0) {
			negatively = true;
			value *= -1.0d;
		}
		if (value < Math.pow(10, 3)) { /* xxxx,x hecto */
			corr = Math.round(value * 10.0d) / 10.0d;
		} else if (value < Math.pow(10, 6)) { /* xxx,xxK kilo */
			corr = Math.round(value / 100.0d) / 10.0d;
			if (color) {
				type = chr + "e";
			}
			type += "K";
			if (corr * Math.pow(10, 3) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 9)) { /* xxx,xxM mega */
			corr = Math.round(value / Math.pow(10, 3)) / 10.0d;
			if (color) {
				type = chr + "a";
			}
			type += "M";
			if (corr * Math.pow(10, 6) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 12)) { /* xxx,xxG giga */
			corr = Math.round(value / Math.pow(10, 6)) / 10.0d;
			if (color) {
				type = chr + "2";
			}
			type += "G";
			if (corr * Math.pow(10, 9) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 15)) { /* xxx,xxT tera */
			corr = Math.round(value / Math.pow(10, 9)) / 10.0d;
			if (color) {
				type = chr + "b";
			}
			type += "T";
			if (corr * Math.pow(10, 12) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 18)) { /* xxx,xxP peta */
			corr = Math.round(value / Math.pow(10, 12)) / 10.0d;
			if (color) {
				type = chr + "3";
			}
			type += "P";
			if (corr * Math.pow(10, 15) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 21)) { /* xxx,xxE hexa */
			corr = Math.round(value / Math.pow(10, 15)) / 10.0d;
			if (color) {
				type = chr + "9";
			}
			type += "E";
			if (corr * Math.pow(10, 18) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 24)) { /* xxx,xxZ zetta */
			corr = Math.round(value / Math.pow(10, 18)) / 10.0d;
			if (color) {
				type = chr + "d";
			}
			type += "Z";
			if (corr * Math.pow(10, 21) != value) {
				sufc = chrPR;
			}
		} else if (value < Math.pow(10, 27)) { /* xxx,xxY yotta */
			corr = Math.round(value / Math.pow(10, 21)) / 10.0d;
			if (color) {
				type = chr + "5";
			}
			type += "Y";
			if (corr * Math.pow(10, 24) != value) {
				sufc = chrPR;
			}
		} else { /* x,xxxe+exp */
			if (String.valueOf(value).indexOf("e+") >= 0 || String.valueOf(value).indexOf("E+") >= 0) {
				String index = "e+";
				if (String.valueOf(value).indexOf("E+") >= 0) {
					index = "E+";
				}
				exp = Integer.valueOf(String.valueOf(value).substring(String.valueOf(value).indexOf(index) + 2));
				corr = Math
						.round(Integer.valueOf(String.valueOf(value).substring(0, String.valueOf(value).indexOf(index)))
								* 1000.0d)
						/ 1000.0d;
			} else {
				exp = String.valueOf(corr).length();
				corr = value;
			}
			if (color) {
				type = chr + "4";
			}
			type = "E+" + exp;
		}
		if (negatively) { /* ������������� ��� ���� */
			if (color) {
				sufc = chr + "c";
			}
			if (corr != 0.0d) {
				sufc += "-";
			}
		}
		String end = "";
		if (color) {
			end = chr + "r";
		}
		if (notPfx) {
			sufc = "";
		}
		String num = isInteger ? ("" + (long) corr) : ("" + corr).replace(".", ",");
		return sufc + num + type + end;
	}

	public static RayTraceVec getVector3D(BlockPos pos0, BlockPos pos1) {
		return AdditionalMethods.instance.getVector3D(pos0.getX() + 0.5d, pos0.getY() + 0.5d, pos0.getZ() + 0.5d,
				pos1.getX() + 0.5d, pos1.getY() + 0.5d, pos1.getZ() + 0.5d);
	}

	public static int inventoryItemCount(EntityPlayer player, ItemStack stack, Availability availability,
			boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || (availability != null && !availability.isAvailable(player)) || stack.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			ItemStack is = player.inventory.mainInventory.get(i);
			if (NoppesUtilServer.IsItemStackNull(is)) {
				continue;
			}
			if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
				count += is.getCount();
			}
		}
		return count;
	}

	public static String match(String text, int pos, String startCharts, String endCharts) {
		try {
			int s = 0, e = text.length();
			for (int i = pos - 1; i >= 0 && i < text.length(); i--) {
				char c = text.charAt(i);
				if (startCharts != null) {
					if (startCharts.indexOf("" + c) != -1) {
						s = i + 1;
						break;
					}
					continue;
				}
				if (((char) 9) == c || ((char) 10) == c || !Character.isAlphabetic(c) && !Character.isDigit(c)) {
					if (c == '.' || c == '(' || ((char) 10) == c) {
						s = i + 1;
					} else {
						s = i + 1;
					}
					break;
				}
			}
			for (int i = pos; i >= 0 && i < text.length(); i++) {
				char c = text.charAt(i);
				if (endCharts != null) {
					if (startCharts.indexOf("" + c) != -1) {
						e = i;
						break;
					}
					continue;
				}
				if (!Character.isAlphabetic(text.charAt(i)) && !Character.isDigit(text.charAt(i))) {
					e = i;
					break;
				}
			}
			String key = text.substring(s, e);
			while (key.indexOf(" ") != -1) {
				key = key.replace(" ", "");
			}
			return key;
		} catch (Exception e) {
		}
		return "";
	}

	@SuppressWarnings("deprecation")
	public static boolean npcCanSeeTarget(EntityLivingBase entity, EntityLivingBase target, boolean toShoot,
			boolean directLOS) {
		if (entity == null || target == null) {
			return false;
		}
		IAttributeInstance follow_range = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
		double aggroRange = (follow_range == null ? 16.0d : follow_range.getAttributeValue());
		if (entity.isPlayerSleeping()) {
			aggroRange /= 4.0d;
		}
		if (aggroRange < 1.0d) {
			aggroRange = 1.0d;
		}
		RayTraceRotate rtr = AdditionalMethods.instance.getAngles3D(entity.posX, entity.posY + entity.getEyeHeight(),
				entity.posZ, target.posX, target.posY + target.getEyeHeight(), target.posZ);
		List<Entity> seenEntities = null, unseenEntities = null;
		if (entity instanceof EntityLiving) {
			EntitySenses senses = ((EntityLiving) entity).getEntitySenses();
			seenEntities = ObfuscationHelper.getValue(EntitySenses.class, senses, 1);
			unseenEntities = ObfuscationHelper.getValue(EntitySenses.class, senses, 2);
		}
		if (rtr.distance > aggroRange) {
			if (seenEntities != null && seenEntities.contains(target)) {
				seenEntities.remove(target);
			}
			if (unseenEntities != null && !unseenEntities.contains(target)) {
				unseenEntities.add(target);
			}
			return false;
		}
		RayTraceResults rtrs = AdditionalMethods.instance.rayTraceBlocksAndEntitys(entity, rtr.yaw, rtr.pitch,
				rtr.distance);
		if (rtrs != null) {
			if (toShoot && rtrs.entitys.length > 0) {
				double d = AdditionalMethods.instance.distanceTo(entity, target);
				for (IEntity<?> ei : rtrs.entitys) {
					if (d > AdditionalMethods.instance.distanceTo(entity, ei.getMCEntity())) {
						if (seenEntities != null && seenEntities.contains(target)) {
							seenEntities.remove(target);
						}
						if (unseenEntities != null && !unseenEntities.contains(target)) {
							unseenEntities.add(target);
						}
						return false;
					}
				}
			}
			boolean shoot = toShoot && (!(entity instanceof EntityNPCInterface)
					|| ((EntityNPCInterface) entity).stats.ranged.getFireType() != 2);
			for (IBlock bi : rtrs.blocks) {
				if (shoot && !bi.getMCBlock().isPassable(entity.world, bi.getPos().getMCBlockPos())) {
					if (seenEntities != null && seenEntities.contains(target)) {
						seenEntities.remove(target);
					}
					if (unseenEntities != null && !unseenEntities.contains(target)) {
						unseenEntities.add(target);
					}
					return false;
				} else if (bi.getMCBlock().isOpaqueCube(entity.world.getBlockState(bi.getPos().getMCBlockPos()))) {
					if (seenEntities != null && seenEntities.contains(target)) {
						seenEntities.remove(target);
					}
					if (unseenEntities != null && !unseenEntities.contains(target)) {
						unseenEntities.add(target);
					}
					return false;
				}
			}
		}
		if (directLOS && !toShoot
				&& (!(entity instanceof EntityNPCInterface) || ((EntityNPCInterface) entity).ais.directLOS)) {
			double yaw = (entity.rotationYawHead - rtr.yaw) % 360.0d;
			double pitch = (entity.rotationPitch - rtr.pitch) % 360.0d;
			if (yaw < 0.0d) {
				yaw += 360.0d;
			}
			if (!(yaw <= 60.0d || yaw >= 300.0d) || !(pitch <= 60.0d || pitch >= -60.0d)) {
				if (seenEntities != null && seenEntities.contains(target)) {
					seenEntities.remove(target);
				}
				if (unseenEntities != null && !unseenEntities.contains(target)) {
					unseenEntities.add(target);
				}
				return false;
			}
		}
		int invis = 1 + (!target.isPotionActive(MobEffects.INVISIBILITY) ? -1
				: target.getActivePotionEffect(MobEffects.INVISIBILITY).getAmplifier());
		double chance = invis == 0 ? 1.0d
				: -0.00026d * Math.pow((double) invis, 3.0d) + 0.00489d * Math.pow((double) invis, 2.0d)
						- 0.03166 * (double) invis + 0.08d;
		if (chance > 1.0d) {
			chance = 1.0d;
		}
		if (chance < 0.002d) {
			chance = 0.002d;
		}
		if (chance != 1.0d) {
			chance *= -1.0d * (rtr.distance / aggroRange) + 1.0d;
		} // distance
		if (chance != 1.0d) {
			chance *= 0.3d;
		} // is sneaks

		if (chance > 1.0d) {
			chance = 1.0d;
		}
		if (chance < 0.0005d) {
			chance = 0.0005d;
		}
		boolean canSee = chance > Math.random();
		if (canSee) {
			if (seenEntities != null && !seenEntities.contains(target)) {
				seenEntities.add(target);
			}
			if (unseenEntities != null && unseenEntities.contains(target)) {
				unseenEntities.remove(target);
			}
		} else {
			if (seenEntities != null && seenEntities.contains(target)) {
				seenEntities.remove(target);
			}
			if (unseenEntities != null && !unseenEntities.contains(target)) {
				unseenEntities.add(target);
			}
		}
		return canSee;
	}

	/** Correct deletion of folders */
	public static boolean removeFile(File directory) {
		if (!directory.isDirectory()) {
			return directory.delete();
		}
		File[] list = directory.listFiles();
		if (list != null) {
			for (File tempFile : list) {
				AdditionalMethods.removeFile(tempFile);
			}
		}
		return directory.delete();
	}

	public static boolean removeItem(EntityPlayerMP player, ItemStack stack, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || stack == null || stack.isEmpty()) {
			return false;
		}
		return AdditionalMethods.removeItem(player, stack, stack.getCount(), ignoreDamage, ignoreNBT);
	}

	public static boolean removeItem(EntityPlayerMP player, ItemStack stack, int count, boolean ignoreDamage,
			boolean ignoreNBT) {
		if (player == null || stack == null || stack.isEmpty()) {
			return false;
		}
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (NoppesUtilServer.IsItemStackNull(is)) {
				continue;
			}
			if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
				if (count < is.getCount()) {
					is.splitStack(count);
					AdditionalMethods.updatePlayerInventory(player);
					return true;
				}
				count -= is.getCount();
				player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
		return count <= 0;
	}

	@SideOnly(Side.CLIENT)
	public static void resetRecipes(EntityPlayer player, GuiContainer gui) {
		CustomNpcs.proxy.updateRecipes(null, false, false, "this.resetRecipes()");

		Container conteiner = null;
		SlotCrafting slotIn = null;
		List<RecipeList> lists = null;

		if (gui instanceof GuiCrafting) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomRegisters.tab, true));
			ObfuscationHelper.setValue(GuiCrafting.class, (GuiCrafting) gui, recipeBookGui, GuiRecipeBook.class);

			conteiner = (ContainerWorkbench) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerWorkbench) conteiner).craftMatrix,
					((ContainerWorkbench) conteiner).craftResult, 0, 124, 35);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomRegisters.tab);
		} else if (gui instanceof GuiInventory) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomRegisters.tab, true));
			ObfuscationHelper.setValue(GuiInventory.class, (GuiInventory) gui, recipeBookGui, 3);

			conteiner = (ContainerPlayer) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerPlayer) conteiner).craftMatrix,
					((ContainerPlayer) conteiner).craftResult, 0, 154, 28);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomRegisters.tab);
		}
		if (gui instanceof GuiNpcCarpentryBench) {
			lists = ClientProxy.MOD_RECIPES_BY_TAB.get(CustomRegisters.tab);
		}
		if (slotIn != null && conteiner != null) {
			conteiner.inventorySlots.remove(slotIn.slotNumber);
			conteiner.inventoryItemStacks.remove(slotIn.slotNumber);
			conteiner.inventorySlots.add(slotIn.slotNumber, slotIn);
			conteiner.inventoryItemStacks.add(slotIn.slotNumber, ItemStack.EMPTY);
		}
		RecipeBook book = ((EntityPlayerSP) player).getRecipeBook();
		if (book != null && lists != null) {
			for (RecipeList list : lists) {
				list.updateKnownRecipes(book);
			}
		}
	}

	public static GameProfile setSkinsToProfile(GameProfile profile, String skinUrl, String capeUrl, String elytraUrl) {
		if ((skinUrl == null || skinUrl.isEmpty()) && (capeUrl == null || capeUrl.isEmpty())
				&& (elytraUrl == null || elytraUrl.isEmpty())) {
			return profile;
		}
		Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
		Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
		if (property != null) {
			try {
				// String json = new String(Base64.decodeBase64(property.getValue()),
				// Charset.forName("UTF-8"));
				// MinecraftTexturesPayload parent = gson.fromJson(json,
				// MinecraftTexturesPayload.class);
			} catch (final JsonParseException e) {
			}
			profile.getProperties().remove("textures", property);
		}
		MinecraftTexturesPayload textures = new MinecraftTexturesPayload();
		ObfuscationHelper.setValue(MinecraftTexturesPayload.class, textures, Long.valueOf(System.currentTimeMillis()),
				long.class);
		ObfuscationHelper.setValue(MinecraftTexturesPayload.class, textures, profile.getId(), UUID.class);
		ObfuscationHelper.setValue(MinecraftTexturesPayload.class, textures, profile.getName(), String.class);
		ObfuscationHelper.setValue(MinecraftTexturesPayload.class, textures, true, boolean.class);
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> mapTs = Maps.<MinecraftProfileTexture.Type, MinecraftProfileTexture>newHashMap();
		ObfuscationHelper.setValue(MinecraftTexturesPayload.class, textures, mapTs, Map.class);

		if (skinUrl != null && !skinUrl.isEmpty()) {
			mapTs.put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(skinUrl, null));
		}
		if (capeUrl != null && !capeUrl.isEmpty()) {
			mapTs.put(MinecraftProfileTexture.Type.CAPE, new MinecraftProfileTexture(capeUrl, null));
		}
		if (elytraUrl != null && !elytraUrl.isEmpty()) {
			mapTs.put(MinecraftProfileTexture.Type.ELYTRA, new MinecraftProfileTexture(elytraUrl, null));
		}

		if (!mapTs.isEmpty()) {
			try {
				profile.getProperties().put("textures",
						new Property("textures", Base64.encodeBase64String(gson.toJson(textures).getBytes())));
			} catch (final JsonParseException e) {
				e.printStackTrace();
			}
		}
		return profile;
	}

	/* Vanila Teleport in world */
	public static Entity teleportEntity(Entity entityIn, CommandBase.CoordinateArg argX, CommandBase.CoordinateArg argY,
			CommandBase.CoordinateArg argZ, CommandBase.CoordinateArg argYaw, CommandBase.CoordinateArg argPitch) {
		if (entityIn instanceof EntityPlayerMP) {
			Set<SPacketPlayerPosLook.EnumFlags> set = EnumSet.<SPacketPlayerPosLook.EnumFlags>noneOf(
					SPacketPlayerPosLook.EnumFlags.class);
			if (argX.isRelative()) {
				set.add(SPacketPlayerPosLook.EnumFlags.X);
			}
			if (argY.isRelative()) {
				set.add(SPacketPlayerPosLook.EnumFlags.Y);
			}
			if (argZ.isRelative()) {
				set.add(SPacketPlayerPosLook.EnumFlags.Z);
			}
			if (argPitch.isRelative()) {
				set.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
			}
			if (argYaw.isRelative()) {
				set.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
			}
			float f = (float) argYaw.getAmount();
			if (!argYaw.isRelative()) {
				f = MathHelper.wrapDegrees(f);
			}
			float f1 = (float) argPitch.getAmount();
			if (!argPitch.isRelative()) {
				f1 = MathHelper.wrapDegrees(f1);
			}
			entityIn.dismountRidingEntity();
			((EntityPlayerMP) entityIn).connection.setPlayerLocation(argX.getAmount(), argY.getAmount(),
					argZ.getAmount(), f, f1, set);
			entityIn.setRotationYawHead(f);
		} else {
			float f2 = (float) MathHelper.wrapDegrees(argYaw.getResult());
			float f3 = (float) MathHelper.wrapDegrees(argPitch.getResult());
			f3 = MathHelper.clamp(f3, -90.0F, 90.0F);
			entityIn.setLocationAndAngles(argX.getResult(), argY.getResult(), argZ.getResult(), f2, f3);
			entityIn.setRotationYawHead(f2);
		}
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isElytraFlying()) {
			entityIn.motionY = 0.0D;
			entityIn.onGround = true;
		}
		return entityIn;
	}

	public static Entity teleportEntity(Entity entityIn, double x, double y, double z) {
		return teleportEntity(entityIn, x, y, z, 0.0f, 0.0f);
	}

	public static Entity teleportEntity(Entity entityIn, double x, double y, double z, float yaw, float pitch) {
		try {
			CommandBase.CoordinateArg argX = CommandBase.parseCoordinate(entityIn.posX, "" + x, true);
			CommandBase.CoordinateArg argY = CommandBase.parseCoordinate(entityIn.posY, "" + y, -4096, 4096, false);
			CommandBase.CoordinateArg argZ = CommandBase.parseCoordinate(entityIn.posZ, "" + z, true);
			CommandBase.CoordinateArg argYaw = CommandBase.parseCoordinate((double) entityIn.rotationYaw, "" + yaw,
					false);
			CommandBase.CoordinateArg argPitch = CommandBase.parseCoordinate((double) entityIn.rotationPitch,
					"" + pitch, false);
			return teleportEntity(entityIn, argX, argY, argZ, argYaw, argPitch);
		} catch (NumberInvalidException e) {
		}
		return null;
	}

	public static Entity teleportEntity(MinecraftServer server, Entity entity, int dimension, BlockPos pos)
			throws CommandException {
		return AdditionalMethods.teleportEntity(server, entity, dimension, (double) pos.getX() + 0.5d,
				(double) pos.getY(), (double) pos.getZ() + 0.5d);
	}

	public static Entity teleportEntity(MinecraftServer server, Entity entity, int dimension, double x, double y,
			double z) throws CommandException {
		if (entity == null) {
			return null;
		}
		int homeDim = entity.world.provider.getDimension();
		if (entity instanceof EntityNPCInterface) {
			homeDim = ((EntityNPCInterface) entity).homeDimensionId;
		}
		if (entity.world.provider.getDimension() != dimension) {
			entity = travelAndCopyEntity(server, entity, dimension);
			if (entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) entity).homeDimensionId = homeDim;
			}
		}
		if (entity == null) {
			return null;
		}
		CoordinateArg xn = CommandBase.parseCoordinate(entity.posX, "" + x, true);
		CoordinateArg yn = CommandBase.parseCoordinate(entity.posY, "" + y, -4096, 4096, false);
		CoordinateArg zn = CommandBase.parseCoordinate(entity.posZ, "" + z, true);
		CoordinateArg w = CommandBase.parseCoordinate((double) entity.rotationYaw, "~", false);
		CoordinateArg p = CommandBase.parseCoordinate((double) entity.rotationPitch, "~", false);
		teleportEntity(entity, xn, yn, zn, w, p);
		return entity;
	}

	/**
	 * @param ticks
	 *            - time
	 * @param isMilliSeconds
	 *            = true - milliseconds (1 sec = 1000 ms)
	 * @param isMilliSeconds
	 *            = false - minecraft time (1 sec = 20 tick)
	 * @param colored
	 *            - added color
	 * @param upped
	 *            - only the maximum period (years or months or days, etc.)
	 * @return
	 */
	public static String ticksToElapsedTime(long ticks, boolean isMilliSeconds, boolean colored, boolean upped) {
		String time = isMilliSeconds ? "0.000" : "--/--";
		String chr = "" + ((char) 167);
		if (ticks < 0) {
			return (colored ? chr + "8" : "") + time;
		}
		long timeSeconds = (isMilliSeconds ? ticks : ticks * 50L) / 1000L;
		int ms = (int) ((isMilliSeconds ? ticks : ticks * 50L) % 1000L);
		int sec = (int) (timeSeconds % 60L);
		int min = (int) (timeSeconds % 3600L) / 60;
		int hour = (int) (timeSeconds % 86400L) / 3600;
		int day = (int) (timeSeconds % 2592000L) / 86400;
		int month = (int) (timeSeconds % 31449600L) / 2620800;
		int year = (int) (timeSeconds / 31449600L);
		String mins, secs;
		if (min < 10) {
			mins = "0" + min;
		} else {
			mins = "" + min;
		}
		if (sec < 10) {
			secs = "0" + sec;
		} else {
			secs = "" + sec;
		}
		time = "";
		if (year > 0) {
			if (colored) {
				time += chr + "r" + year + chr + "6y ";
			} else {
				time += year + "y ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (month > 0) {
			if (colored) {
				time += chr + "r" + month + chr + "1m ";
			} else {
				time += month + "m ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (day > 0) {
			if (colored) {
				time += chr + "r" + day + chr + "2d ";
			} else {
				time += day + "d ";
			}
		}
		if (upped && time.length() > 0) {
			return time;
		}
		if (hour > 0 || year > 0 || month > 0 || day > 0) {
			if (colored) {
				time += chr + "r" + hour + ":";
			} else {
				time += hour + ":";
			}
		}
		time += (colored ? chr + "r" : "") + mins + ":" + secs;
		if (isMilliSeconds) {
			time += (colored ? chr + "8" : "") + "." + ms;
		}
		return time;
	}

	public static Entity travelAndCopyEntity(MinecraftServer server, Entity entity, int dimension)
			throws CommandException {
		if (server == null) {
			throw new CommandException("Server cannot have value Null");
		}
		World world = server.getWorld(dimension);
		if (world == null) {
			throw new CommandException("Couldn't find dimension " + dimension);
		}
		if (entity instanceof EntityPlayerMP) {
			server.getPlayerList().transferPlayerToDimension((EntityPlayerMP) entity, dimension,
					new CustomNpcsTeleporter((WorldServer) server.getEntityWorld()));
			return entity;
		} else {
			return travelEntity(server, entity, dimension);
		}
	}

	/* [Teleport] Copy and Place Entity to Spawn next Dimensions */
	public static Entity travelEntity(MinecraftServer server, Entity entity, int dimensionId) {
		if (entity.world.isRemote || entity.isDead) {
			return null;
		}
		net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity, dimensionId);
		entity.world.profiler.startSection("changeDimension");
		int dimensionStart = entity.dimension;
		WorldServer worldserverStart = server.getWorld(dimensionStart);
		WorldServer worldserverEnd = server.getWorld(dimensionId);
		entity.dimension = dimensionId;
		Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(entity.getClass()), worldserverEnd);
		if (newEntity != null) {
			try {
				AdditionalMethods.instance.copyDataFromOld.invoke(newEntity, entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			entity.world.removeEntity(entity);
			newEntity.forceSpawn = true;
			worldserverEnd.spawnEntity(newEntity);
		}
		try {
			worldserverEnd.updateEntityWithOptionalForce(newEntity, true);
			entity.isDead = true;
			entity.world.profiler.endSection();
			worldserverStart.resetUpdateEntityTick();
			worldserverEnd.resetUpdateEntityTick();
			entity.world.profiler.endSection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newEntity;
	}

	public static void updatePlayerInventory(EntityPlayerMP player) {
		PlayerQuestData playerdata = PlayerData.get(player).questData;
		for (QuestData data : playerdata.activeQuests.values()) {
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
				if (obj.getType() != 0) {
					continue;
				}
				playerdata.checkQuestCompletion(player, data);
			}
		}
	}

	private Method copyDataFromOld;

	public AdditionalMethods() {
		try {
			this.copyDataFromOld = Entity.class.getDeclaredMethod(
					(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") ? "copyDataFromOld"
							: "func_180432_n",
					Entity.class);
			this.copyDataFromOld.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Stripping a string of color */
	@Override
	public String deleteColor(String str) {
		if (str == null) {
			return null;
		}
		if (str.isEmpty()) {
			return str;
		}
		for (int i = 0; i < 3; i++) {
			String chr = "" + ((char) 167);
			if (i == 1) {
				chr = "&";
			} else if (i == 2) {
				chr = "" + ((char) 65535);
			}
			try {
				while (str.indexOf(chr) != (-1)) {
					int p = str.indexOf(chr);
					str = (p > 0 ? str.substring(0, p) : "") + (p + 2 == str.length() ? "" : str.substring(p + 2));
				}
			} catch (Exception e) {
			}
		}
		return str;
	}

	@Override
	public double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1) {
		double d0 = x0 - x1;
		double d1 = y0 - y1;
		double d2 = z0 - z1;
		return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public double distanceTo(Entity entity, Entity target) {
		return this.distanceTo(entity.posX, entity.posY, entity.posZ, target.posX, target.posY, target.posZ);
	}

	@Override
	public double distanceTo(IEntity<?> entity, IEntity<?> target) {
		return this.distanceTo(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ,
				target.getMCEntity().posX, target.getMCEntity().posY, target.getMCEntity().posZ);
	}

	@Override
	public RayTraceRotate getAngles3D(double dx, double dy, double dz, double mx, double my, double mz) {
		RayTraceRotate rtr = new RayTraceRotate();
		rtr.calculate(dx, dy, dz, mx, my, mz);
		return rtr;
	}

	@Override
	public RayTraceRotate getAngles3D(IEntity<?> entity, IEntity<?> target) {
		return AdditionalMethods.getAngles3D(entity.getMCEntity(), target.getMCEntity());
	}

	@Override
	public String getJSONStringFromObject(Object obj) {
		String str = "";
		if (obj.getClass().isArray()) {
			str = "[";
			for (Object value : (Object[]) obj) {
				String s = this.getJSONStringFromObject(value);
				if (!str.isEmpty()) {
					str += ", ";
				}
				str += s;
			}
			str += "]";
		} else if (obj instanceof Number) {
			str = obj.toString();
		} else if (obj instanceof String) {
			str = "'" + obj.toString() + "'";
		} else if (obj instanceof Bindings) {
			ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
			if (engine != null) {
				engine.put("temp", obj);
				try {
					str = (String) engine.eval("JSON.stringify(temp)");
				} catch (ScriptException e) {
				}
			}
		}
		return str;
	}

	public InputStream getModInputStream(String fileName) {
		if (fileName == null || fileName.isEmpty() || fileName.lastIndexOf(".") == -1) {
			return null;
		}
		InputStream inputStream = null;
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists() && mod.getSource().getName().equals(CustomNpcs.MODID)
					|| mod.getSource().getName().endsWith("bin") || mod.getSource().getName().endsWith("main")) {
				if (!mod.getSource().isDirectory()
						&& (mod.getSource().getName().endsWith(".jar") || mod.getSource().getName().endsWith(".zip"))) {
					try {
						ZipFile zip = new ZipFile(mod.getSource());
						Enumeration<? extends ZipEntry> entries = zip.entries();
						while (entries.hasMoreElements()) {
							ZipEntry zipentry = (ZipEntry) entries.nextElement();
							if (zipentry.isDirectory() || !zipentry.getName().endsWith(fileName)) {
								continue;
							}
							inputStream = zip.getInputStream(zipentry);
							break;
						}
						zip.close();
					} catch (Exception e) {
					}
				} else {
					List<File> list = AdditionalMethods.getFiles(mod.getSource(),
							fileName.substring(fileName.lastIndexOf(".")));
					for (File file : list) {
						if (!file.isFile() || !file.getName().equals(fileName)) {
							continue;
						}
						try {
							inputStream = new FileInputStream(file);
						} catch (Exception e) {
						}
						break;
					}
				}
			}
		}
		return inputStream;
	}

	@Override
	public RayTraceVec getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius) {
		RayTraceVec rtv = new RayTraceVec();
		rtv.calculatePos(cx, cy, cz, yaw, pitch, radius);
		return rtv;
	}

	@Override
	public RayTraceVec getPosition(IEntity<?> entity, double yaw, double pitch, double radius) {
		return this.getPosition(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ, yaw,
				pitch, radius);
	}

	@Override
	public RayTraceVec getVector3D(double dx, double dy, double dz, double mx, double my, double mz) {
		RayTraceVec rtv = new RayTraceVec();
		rtv.calculateVec(dx, dy, dz, mx, my, mz);
		return rtv;
	}

	@Override
	public RayTraceVec getVector3D(IEntity<?> entity, IEntity<?> target) {
		return this.getVector3D(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ,
				target.getMCEntity().posX, target.getMCEntity().posY, target.getMCEntity().posZ);
	}

	@Override
	public RayTraceVec getVector3D(IEntity<?> entity, IPos pos) {
		return this.getVector3D(entity.getMCEntity().posX, entity.getMCEntity().posY, entity.getMCEntity().posZ,
				pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
	}

	public RayTraceResults rayTraceBlocksAndEntitys(Entity entity, double yaw, double pitch, double distance) {
		if (entity == null || entity.world == null || distance <= 0.0d) {
			return null;
		}
		RayTraceResults rtrs = new RayTraceResults();

		Vec3d vecStart = entity.getPositionEyes(1.0f);
		double rad = Math.PI / 180.0d;
		double f = Math.cos(-yaw * rad - Math.PI);
		double f1 = Math.sin(-yaw * rad - Math.PI);
		double f2 = -Math.cos(-pitch * rad);
		double f3 = Math.sin(-pitch * rad);
		Vec3d vecLook = new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
		Vec3d vecEnd = vecStart.addVector(vecLook.x * distance, vecLook.y * distance, vecLook.z * distance);
		rtrs.add(entity, distance, vecStart, vecEnd);

		int x0 = MathHelper.floor(vecStart.x);
		int y0 = MathHelper.floor(vecStart.y);
		int z0 = MathHelper.floor(vecStart.z);
		int x1 = MathHelper.floor(vecEnd.x);
		int y1 = MathHelper.floor(vecEnd.y);
		int z1 = MathHelper.floor(vecEnd.z);

		BlockPos pos = new BlockPos(x0, y0, z0);
		IBlockState state = entity.world.getBlockState(pos);
		rtrs.add(entity.world, pos, state);

		int k1 = 200;
		while (k1-- >= 0) {
			if (x0 == x1 && y0 == y1 && z0 == z1) {
				return rtrs;
			}

			boolean butEqualX = true;
			boolean butEqualY = true;
			boolean butEqualZ = true;
			double d0 = 999.0D;
			double d1 = 999.0D;
			double d2 = 999.0D;

			if (x1 > x0) {
				d0 = (double) x0 + 1.0D;
			} else if (x1 < x0) {
				d0 = (double) x0 + 0.0D;
			} else {
				butEqualX = false;
			}

			if (y1 > y0) {
				d1 = (double) y0 + 1.0D;
			} else if (y1 < y0) {
				d1 = (double) y0 + 0.0D;
			} else {
				butEqualY = false;
			}

			if (z1 > z0) {
				d2 = (double) z0 + 1.0D;
			} else if (z1 < z0) {
				d2 = (double) z0 + 0.0D;
			} else {
				butEqualZ = false;
			}

			double d3 = 999.0D;
			double d4 = 999.0D;
			double d5 = 999.0D;
			double d6 = vecEnd.x - vecStart.x;
			double d7 = vecEnd.y - vecStart.y;
			double d8 = vecEnd.z - vecStart.z;

			if (butEqualX) {
				d3 = (d0 - vecStart.x) / d6;
			}
			if (butEqualY) {
				d4 = (d1 - vecStart.y) / d7;
			}
			if (butEqualZ) {
				d5 = (d2 - vecStart.z) / d8;
			}

			if (d3 == -0.0D) {
				d3 = -1.0E-4D;
			}
			if (d4 == -0.0D) {
				d4 = -1.0E-4D;
			}
			if (d5 == -0.0D) {
				d5 = -1.0E-4D;
			}

			EnumFacing enumfacing;
			if (d3 < d4 && d3 < d5) {
				enumfacing = x1 > x0 ? EnumFacing.WEST : EnumFacing.EAST;
				vecStart = new Vec3d(d0, vecStart.y + d7 * d3, vecStart.z + d8 * d3);
			} else if (d4 < d5) {
				enumfacing = y1 > y0 ? EnumFacing.DOWN : EnumFacing.UP;
				vecStart = new Vec3d(vecStart.x + d6 * d4, d1, vecStart.z + d8 * d4);
			} else {
				enumfacing = z1 > z0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
				vecStart = new Vec3d(vecStart.x + d6 * d5, vecStart.y + d7 * d5, d2);
			}

			x0 = MathHelper.floor(vecStart.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
			y0 = MathHelper.floor(vecStart.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
			z0 = MathHelper.floor(vecStart.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
			pos = new BlockPos(x0, y0, z0);
			state = entity.world.getBlockState(pos);
			rtrs.add(entity.world, pos, state);
		}
		return rtrs;
	}

	@Override
	public RayTraceResults rayTraceBlocksAndEntitys(IEntity<?> entity, double yaw, double pitch, double distance) {
		if (entity == null) {
			return null;
		}
		return rayTraceBlocksAndEntitys(entity.getMCEntity(), yaw, pitch, distance);
	}

	@Override
	public Object readObjectFromNbt(NBTBase tag) {
		if (tag instanceof NBTTagByte) {
			return ((NBTTagByte) tag).getByte();
		} else if (tag instanceof NBTTagShort) {
			return ((NBTTagShort) tag).getShort();
		} else if (tag instanceof NBTTagInt) {
			return ((NBTTagInt) tag).getInt();
		} else if (tag instanceof NBTTagLong) {
			return ((NBTTagLong) tag).getLong();
		} else if (tag instanceof NBTTagFloat) {
			return ((NBTTagFloat) tag).getFloat();
		} else if (tag instanceof NBTTagDouble) {
			return ((NBTTagDouble) tag).getDouble();
		} else if (tag instanceof NBTTagString) {
			return ((NBTTagString) tag).getString();
		} else if (tag instanceof NBTTagByteArray) {
			return ((NBTTagByteArray) tag).getByteArray();
		} else if (tag instanceof NBTTagIntArray) {
			return ((NBTTagIntArray) tag).getIntArray();
		} else if (tag instanceof NBTTagLongArray) {
			return ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) tag, 0);
		} else if (tag instanceof NBTTagCompound && ((NBTTagCompound) tag).hasKey("IsArray", 1)) {
			boolean isArray = ((NBTTagCompound) tag).getBoolean("IsArray");
			ScriptEngine engine = ScriptController.Instance.getEngineByName("ECMAScript");
			if (engine == null) {
				return null;
			}
			try {
				String str = "JSON.parse('" + (isArray ? "[" : "{");
				Set<String> sets = ((NBTTagCompound) tag).getKeySet();
				Map<String, Object> map = Maps.<String, Object>newTreeMap();
				for (String k : sets) {
					if (k.equals("IsArray")) {
						continue;
					}
					Object v = this.readObjectFromNbt(((NBTTagCompound) tag).getTag(k));
					if (v != null) {
						map.put(k, v);
					}
				}
				for (String k : map.keySet()) {
					String s = this.getJSONStringFromObject(map.get(k));
					if (isArray) {
						str += s + ", ";
					} else {
						str += "\"" + k + "\":" + s + ", ";
					}
				}
				if (!map.isEmpty()) {
					str = str.substring(0, str.length() - 2);
				}
				str += (isArray ? "]" : "}") + "')";
				return engine.eval(str);
			} catch (Exception e) {
			}
		} else if (tag instanceof NBTTagList) {
			Object[] arr = new Object[((NBTTagList) tag).tagCount()];
			int i = 0;
			for (NBTBase listTag : (NBTTagList) tag) {
				arr[i] = this.readObjectFromNbt(listTag);
				i++;
			}
			return arr;
		}
		return null;
	}

	@Override
	public IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos) {
		Entity e = null;
		try {
			if (pos != null) {
				e = AdditionalMethods.teleportEntity(CustomNpcs.Server, entity.getMCEntity(), dimension,
						pos.getMCBlockPos());
			} else {
				e = AdditionalMethods.travelAndCopyEntity(CustomNpcs.Server, entity.getMCEntity(), dimension);
			}
		} catch (CommandException error) {
		}
		if (e != null) {
			return NpcAPI.Instance().getIEntity(e);
		}
		return entity;
	}

	@Override
	public NBTBase writeObjectToNbt(Object value) {
		if (value.getClass().isArray()) {
			Object[] vs = (Object[]) value;
			if (vs.length == 0) {
				return new NBTTagList();
			}
			if (vs[0] instanceof Byte) {
				List<Byte> l = Lists.<Byte>newArrayList();
				for (Object v : vs) {
					if (v instanceof Byte) {
						l.add((Byte) v);
					}
				}
				byte[] arr = new byte[l.size()];
				int i = 0;
				for (byte d : l) {
					arr[i] = d;
					i++;
				}
				return new NBTTagByteArray(arr);
			} else if (vs[0] instanceof Integer) {
				List<Integer> l = Lists.<Integer>newArrayList();
				for (Object v : vs) {
					if (v instanceof Integer) {
						l.add((Integer) v);
					}
				}
				int[] arr = new int[l.size()];
				int i = 0;
				for (int d : l) {
					arr[i] = d;
					i++;
				}
				return new NBTTagIntArray(arr);
			} else if (vs[0] instanceof Long) {
				List<Long> l = Lists.<Long>newArrayList();
				for (Object v : vs) {
					if (v instanceof Long) {
						l.add((Long) v);
					}
				}
				long[] arr = new long[l.size()];
				int i = 0;
				for (long d : l) {
					arr[i] = d;
					i++;
				}
				return new NBTTagLongArray(arr);
			} else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
				NBTTagList list = new NBTTagList();
				for (Object v : vs) {
					double d;
					if (v instanceof Short) {
						d = (double) (Short) v;
					} else if (v instanceof Float) {
						d = (double) (Float) v;
					} else if (v instanceof Double) {
						d = (Double) v;
					} else {
						continue;
					}
					list.appendTag(new NBTTagDouble(d));
				}
				return list;
			}
		} else if (value instanceof Byte) {
			return new NBTTagByte((Byte) value);
		} else if (value instanceof Short) {
			return new NBTTagShort((Short) value);
		} else if (value instanceof Integer) {
			return new NBTTagInt((Integer) value);
		} else if (value instanceof Long) {
			return new NBTTagLong((Long) value);
		} else if (value instanceof Float) {
			return new NBTTagFloat((Float) value);
		} else if (value instanceof Double) {
			return new NBTTagDouble((Double) value);
		} else if (value instanceof String) {
			return new NBTTagString((String) value);
		} else if (value instanceof Bindings) {
			String clazz = value.toString();
			if (!clazz.equals("[object Array]") && !clazz.equals("[object Object]")) {
				return null;
			}
			boolean isArray = clazz.equals("[object Array]");
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("IsArray", isArray);
			for (Map.Entry<String, Object> scopeEntry : ((Bindings) value).entrySet()) {
				Object v = scopeEntry.getValue();
				if (v.getClass().isArray()) {
					Object[] vs = (Object[]) v;
					if (vs.length == 0) {
						nbt.setTag(scopeEntry.getKey(), new NBTTagList());
						continue;
					}
					if (vs[0] instanceof Byte) {
						List<Byte> l = Lists.<Byte>newArrayList();
						for (Object va : vs) {
							if (va instanceof Byte) {
								l.add((Byte) va);
							}
						}
						byte[] arr = new byte[l.size()];
						int i = 0;
						for (byte d : l) {
							arr[i] = d;
							i++;
						}
						nbt.setByteArray(scopeEntry.getKey(), arr);
					} else if (vs[0] instanceof Integer) {
						List<Integer> l = Lists.<Integer>newArrayList();
						for (Object va : vs) {
							if (va instanceof Integer) {
								l.add((Integer) va);
							}
						}
						int[] arr = new int[l.size()];
						int i = 0;
						for (int d : l) {
							arr[i] = d;
							i++;
						}
						nbt.setIntArray(scopeEntry.getKey(), arr);
					} else if (vs[0] instanceof Long) {
						List<Long> l = Lists.<Long>newArrayList();
						for (Object va : vs) {
							if (va instanceof Long) {
								l.add((Long) va);
							}
						}
						long[] arr = new long[l.size()];
						int i = 0;
						for (long d : l) {
							arr[i] = d;
							i++;
						}
						nbt.setTag(scopeEntry.getKey(), new NBTTagLongArray(arr));
					} else if (vs[0] instanceof Short || vs[0] instanceof Float || vs[0] instanceof Double) {
						NBTTagList list = new NBTTagList();
						for (Object va : vs) {
							double d;
							if (va instanceof Short) {
								d = (double) (Short) va;
							} else if (va instanceof Float) {
								d = (double) (Float) va;
							} else if (va instanceof Double) {
								d = (Double) va;
							} else {
								continue;
							}
							list.appendTag(new NBTTagDouble(d));
						}
						nbt.setTag(scopeEntry.getKey(), list);
					}
				} else if (v instanceof Byte) {
					nbt.setByte(scopeEntry.getKey(), (Byte) v);
				} else if (v instanceof Short) {
					nbt.setShort(scopeEntry.getKey(), (Short) v);
				} else if (v instanceof Integer) {
					nbt.setInteger(scopeEntry.getKey(), (Integer) v);
				} else if (v instanceof Long) {
					nbt.setLong(scopeEntry.getKey(), (Long) v);
				} else if (v instanceof Float) {
					nbt.setFloat(scopeEntry.getKey(), (Float) v);
				} else if (v instanceof Double) {
					nbt.setDouble(scopeEntry.getKey(), (Double) v);
				} else if (v instanceof String) {
					nbt.setString(scopeEntry.getKey(), (String) v);
				} else {
					NBTBase n = this.writeObjectToNbt(v);
					if (n != null) {
						nbt.setTag(scopeEntry.getKey(), n);
					}
				}
			}
			return nbt;
		}
		return null;
	}

	public void sort(NonNullList<ItemStack> items) {
		Map<String, List<ItemStack>> mapArmor = Maps.<String, List<ItemStack>>newTreeMap();
		Map<String, List<ItemStack>> mapPotion = Maps.<String, List<ItemStack>>newTreeMap();
		Map<Integer, List<ItemStack>> mapSimple = Maps.<Integer, List<ItemStack>>newTreeMap();
		Map<String, List<ItemStack>> mapAny = Maps.<String, List<ItemStack>>newTreeMap();
		// Collect
		for (ItemStack stack : items) {
			if (stack.getItem() instanceof CustomArmor) {
				String key = ((CustomArmor) stack.getItem()).getCustomName();
				if (!mapArmor.containsKey(key)) { mapArmor.put(key, Lists.<ItemStack>newArrayList()); }
				mapArmor.get(key).add(stack);
			}
			else if (stack.getItem() instanceof ItemPotion) {
				String key = stack.getItem().getClass().getSimpleName();
				if (!mapPotion.containsKey(key)) { mapPotion.put(key, Lists.<ItemStack>newArrayList()); }
				mapPotion.get(key).add(stack);
			}
			else if (stack.getItem() instanceof ICustomElement) {
				int key = ((ICustomElement) stack.getItem()).getType();
				if (!mapSimple.containsKey(key)) { mapSimple.put(key, Lists.<ItemStack>newArrayList()); }
				mapSimple.get(key).add(stack);
			}
			else {
				String key = stack.getItem().getClass().getSimpleName();
				if (!mapAny.containsKey(key)) { mapAny.put(key, Lists.<ItemStack>newArrayList()); }
				mapAny.get(key).add(stack);
			}
		}
		items.clear();
		// sort
		for (List<ItemStack> list: mapArmor.values()) {
			Collections.sort(list, new Comparator<ItemStack>() {
				public int compare(ItemStack st_0, ItemStack st_1) {
					CustomArmor a_0 = (CustomArmor) st_0.getItem();
					CustomArmor a_1 = (CustomArmor) st_1.getItem();
					return Integer.compare(a_0.getEquipmentSlot().ordinal(), a_1.getEquipmentSlot().ordinal());
				}
			});
			items.addAll(list);
		}
		for (List<ItemStack> list: mapPotion.values()) {
			Collections.sort(list, new Comparator<ItemStack>() {
				public int compare(ItemStack st_0, ItemStack st_1) {
					return st_1.getDisplayName().compareTo(st_0.getDisplayName());
				}
			});
			items.addAll(list);
		}
		for (List<ItemStack> list: mapSimple.values()) {
			Collections.sort(list, new Comparator<ItemStack>() {
				public int compare(ItemStack st_0, ItemStack st_1) {
					return st_1.getDisplayName().compareTo(st_0.getDisplayName());
				}
			});
			items.addAll(list);
		}
		for (List<ItemStack> list: mapAny.values()) {
			Collections.sort(list, new Comparator<ItemStack>() {
				public int compare(ItemStack st_0, ItemStack st_1) {
					return st_1.getDisplayName().compareTo(st_0.getDisplayName());
				}
			});
			items.addAll(list);
		}
	}
	
}