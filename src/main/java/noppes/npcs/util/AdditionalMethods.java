package noppes.npcs.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IContainerCustomChest;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IDimension;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.IScoreboard;
import noppes.npcs.api.IScoreboardObjective;
import noppes.npcs.api.IScoreboardScore;
import noppes.npcs.api.IScoreboardTeam;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.block.IBlockFluidContainer;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.IBlockScriptedDoor;
import noppes.npcs.api.entity.IAnimal;
import noppes.npcs.api.entity.IArrow;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IMonster;
import noppes.npcs.api.entity.IPixelmon;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IThrowable;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.gui.ITextField;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.handler.data.IDataElement;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.handler.data.IScriptData;
import noppes.npcs.api.item.IItemArmor;
import noppes.npcs.api.item.IItemBlock;
import noppes.npcs.api.item.IItemBook;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.AnimalWrapper;
import noppes.npcs.api.wrapper.ArrowWrapper;
import noppes.npcs.api.wrapper.BlockFluidContainerWrapper;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.api.wrapper.BlockScriptedWrapper;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.ContainerCustomChestWrapper;
import noppes.npcs.api.wrapper.ContainerWrapper;
import noppes.npcs.api.wrapper.DamageSourceWrapper;
import noppes.npcs.api.wrapper.DataObject;
import noppes.npcs.api.wrapper.DimensionWrapper;
import noppes.npcs.api.wrapper.EntityItemWrapper;
import noppes.npcs.api.wrapper.EntityLivingBaseWrapper;
import noppes.npcs.api.wrapper.EntityLivingWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;
import noppes.npcs.api.wrapper.ItemArmorWrapper;
import noppes.npcs.api.wrapper.ItemBlockWrapper;
import noppes.npcs.api.wrapper.ItemBookWrapper;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.MonsterWrapper;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.api.wrapper.PixelmonWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.ProjectileWrapper;
import noppes.npcs.api.wrapper.RayTraceWrapper;
import noppes.npcs.api.wrapper.ScoreboardObjectiveWrapper;
import noppes.npcs.api.wrapper.ScoreboardScoreWrapper;
import noppes.npcs.api.wrapper.ScoreboardTeamWrapper;
import noppes.npcs.api.wrapper.ScoreboardWrapper;
import noppes.npcs.api.wrapper.ThrowableWrapper;
import noppes.npcs.api.wrapper.VillagerWrapper;
import noppes.npcs.api.wrapper.WorldWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.api.wrapper.data.DataElement;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiItemSlotWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.recipebook.GuiNpcButtonRecipeTab;
import noppes.npcs.client.gui.recipebook.GuiNpcRecipeBook;
import noppes.npcs.containers.SlotNpcCrafting;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;

public class AdditionalMethods {

	private static Map<Class<?>, Class<?>> map = Maps.newHashMap();

	static {
		AdditionalMethods.map.put(IAnimal.class, AnimalWrapper.class);
		AdditionalMethods.map.put(IArrow.class, ArrowWrapper.class);
		AdditionalMethods.map.put(IBlockFluidContainer.class, BlockFluidContainerWrapper.class);
		AdditionalMethods.map.put(IPos.class, BlockPosWrapper.class);
		AdditionalMethods.map.put(IBlockScriptedDoor.class, BlockScriptedDoorWrapper.class);
		AdditionalMethods.map.put(IBlockScripted.class, BlockScriptedWrapper.class);
		AdditionalMethods.map.put(IBlock.class, BlockWrapper.class);
		AdditionalMethods.map.put(IContainerCustomChest.class, ContainerCustomChestWrapper.class);
		AdditionalMethods.map.put(IContainer.class, ContainerWrapper.class);
		AdditionalMethods.map.put(IDamageSource.class, DamageSourceWrapper.class);
		AdditionalMethods.map.put(IDataObject.class, DataObject.class);
		AdditionalMethods.map.put(IDimension.class, DimensionWrapper.class);
		AdditionalMethods.map.put(IEntityItem.class, EntityItemWrapper.class);
		AdditionalMethods.map.put(IEntityLivingBase.class, EntityLivingBaseWrapper.class);
		AdditionalMethods.map.put(IEntityLiving.class, EntityLivingWrapper.class);
		AdditionalMethods.map.put(IEntity.class, EntityWrapper.class);
		AdditionalMethods.map.put(IItemArmor.class, ItemArmorWrapper.class);
		AdditionalMethods.map.put(IItemBlock.class, ItemBlockWrapper.class);
		AdditionalMethods.map.put(IItemBook.class, ItemBookWrapper.class);
		AdditionalMethods.map.put(IItemScripted.class, ItemScriptedWrapper.class);
		AdditionalMethods.map.put(IScriptHandler.class, ItemScriptedWrapper.class);
		AdditionalMethods.map.put(IItemStack.class, ItemStackWrapper.class);
		AdditionalMethods.map.put(IMonster.class, MonsterWrapper.class);
		AdditionalMethods.map.put(INbt.class, NBTWrapper.class);
		AdditionalMethods.map.put(ICustomNpc.class, NPCWrapper.class);
		AdditionalMethods.map.put(IPixelmon.class, PixelmonWrapper.class);
		AdditionalMethods.map.put(IPlayer.class, PlayerWrapper.class);
		AdditionalMethods.map.put(IProjectile.class, ProjectileWrapper.class);
		AdditionalMethods.map.put(IRayTrace.class, RayTraceWrapper.class);
		AdditionalMethods.map.put(IScoreboardObjective.class, ScoreboardObjectiveWrapper.class);
		AdditionalMethods.map.put(IScoreboardScore.class, ScoreboardScoreWrapper.class);
		AdditionalMethods.map.put(IScoreboardTeam.class, ScoreboardTeamWrapper.class);
		AdditionalMethods.map.put(IScoreboard.class, ScoreboardWrapper.class);
		AdditionalMethods.map.put(IThrowable.class, ThrowableWrapper.class);
		AdditionalMethods.map.put(IVillager.class, VillagerWrapper.class);
		AdditionalMethods.map.put(IWorld.class, WorldWrapper.class);
		AdditionalMethods.map.put(NpcAPI.class, WrapperNpcAPI.class);
		AdditionalMethods.map.put(IButton.class, CustomGuiButtonWrapper.class);
		AdditionalMethods.map.put(ICustomGuiComponent.class, CustomGuiComponentWrapper.class);
		AdditionalMethods.map.put(IItemSlot.class, CustomGuiItemSlotWrapper.class);
		AdditionalMethods.map.put(ILabel.class, CustomGuiLabelWrapper.class);
		AdditionalMethods.map.put(IScroll.class, CustomGuiScrollWrapper.class);
		AdditionalMethods.map.put(ITextField.class, CustomGuiTextFieldWrapper.class);
		AdditionalMethods.map.put(ITexturedRect.class, CustomGuiTexturedRectWrapper.class);
		AdditionalMethods.map.put(ICustomGui.class, CustomGuiWrapper.class);
		AdditionalMethods.map.put(IDataElement.class, DataElement.class);
	}

	/** Stripping a string of color */
	public static String deleteColor(String str) {
		if (str == null) { return null; }
		String chr = new String(Character.toChars(0x00A7));
		while (str.indexOf(chr) != (-1)) {
			str = str.substring(0, str.indexOf(chr)) + str.substring(str.indexOf(chr) + 2);
		}
		while (str.indexOf("�") != (-1)) {
			str = str.substring(0, str.indexOf("�")) + str.substring(str.indexOf("�") + 2);
		}
		while (str.indexOf("&") != (-1)) {
			str = str.substring(0, str.indexOf("&")) + str.substring(str.indexOf("&") + 2);
		}
		return str;
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

	public static Map<String, Map<String, String[]>> getObjectVarAndMetods(String[] path,
			Map<String, Class<?>> inFunc) {
		Object obj = null;
		for (int i = 0; i < path.length; i++) {
			String key = path[i];
			if (key.isEmpty()) {
				break;
			}
			if (i == 0 && inFunc != null) {
				obj = inFunc.get(key);
				continue;
			}
			if (obj == null) {
				break;
			}
			Class<?> clazz = AdditionalMethods.getScriptClass(obj);
			obj = null;
			if (key.indexOf('(') != -1) {
				key = key.substring(0, key.indexOf('('));
				if (key.equals("getMCEntity")) {
					if (clazz == PlayerWrapper.class) {
						obj = EntityPlayerMP.class;
						continue;
					} else if (clazz == EntityLivingBaseWrapper.class) {
						obj = EntityLivingBase.class;
						continue;
					} else if (clazz == EntityLivingWrapper.class) {
						obj = EntityLiving.class;
						continue;
					} else if (clazz == EntityWrapper.class) {
						obj = Entity.class;
						continue;
					}
				}
				for (Method m : clazz.getMethods()) {
					if (m.getName().equals(key)) {
						obj = m.getReturnType();
						continue;
					}
				}
				if (!CustomNpcs.scriptHelperObfuscations) {
					continue;
				}

				Map<String, Method> methodsMap = Maps.newHashMap();
				for (Method m : clazz.getMethods()) {
					methodsMap.put(m.getName(), m);
				}

				for (int j = 0; j < 4; j++) {
					Map<String, String> m;
					if (j == 0) {
						m = ObfuscationPart0.map;
					} else if (j == 1) {
						m = ObfuscationPart1.map;
					} else if (j == 2) {
						m = ObfuscationPart2.map;
					} else {
						m = ObfuscationPart3.map;
					}
					if (m.containsValue(key)) {
						for (String s : m.keySet()) {
							if (m.get(s).equals(key)) {
								if (methodsMap.containsKey(s)) {
									obj = methodsMap.get(s).getReturnType();
								}
								break;
							}
						}
					}
					if (obj != null) {
						break;
					}
				}
			} else {
				for (Class<?> c : clazz.getClasses()) {
					if (c.getSimpleName().equals(key)) {
						obj = c;
						continue;
					}
				}
				for (Field f : clazz.getFields()) {
					if (f.getName().equals(key)) {
						obj = f.getType();
						continue;
					}
				}
				if (!CustomNpcs.scriptHelperObfuscations) {
					continue;
				}

				Map<String, Field> fieldMap = Maps.newHashMap();
				Map<String, Class<?>> classMap = Maps.newHashMap();
				for (Field f : clazz.getFields()) {
					fieldMap.put(f.getName(), f);
				}
				for (Class<?> c : clazz.getClasses()) {
					classMap.put(c.getSimpleName(), c);
				}

				for (int j = 0; j < 11; j++) {
					Map<String, String> m;
					if (j == 0) {
						m = ObfuscationPart4.map;
					} else if (j == 1) {
						m = ObfuscationPart5.map;
					} else if (j == 2) {
						m = ObfuscationPart6.map;
					} else if (j == 3) {
						m = ObfuscationPart7.map;
					} else if (j == 4) {
						m = ObfuscationPart8.map;
					} else if (j == 5) {
						m = ObfuscationPart9.map;
					} else if (j == 6) {
						m = ObfuscationPart10.map;
					} else if (j == 7) {
						m = ObfuscationPart11.map;
					} else if (j == 8) {
						m = ObfuscationPart12.map;
					} else if (j == 9) {
						m = ObfuscationPart13.map;
					} else {
						m = ObfuscationPart14.map;
					}
					if (m.containsValue(key)) {
						for (String s : m.keySet()) {
							if (m.get(s).equals(key)) {
								if (classMap.containsKey(s)) {
									obj = classMap.get(s);
								}
								if (fieldMap.containsKey(s)) {
									obj = fieldMap.get(s).getType();
								}
								break;
							}
						}
					}
					if (obj != null) {
						break;
					}
				}
			}
		}

		Map<String, Map<String, String[]>> map = Maps.newHashMap();
		if (obj == null) {
			return map;
		}

		char chr = Character.toChars(0x00A7)[0];
		Class<?> clazz = AdditionalMethods.getScriptClass(obj);
		if (!(obj instanceof Event) && clazz.getClasses().length > 0) {
			if (!map.containsKey("classes")) {
				map.put("classes", Maps.newHashMap());
			}
			for (Class<?> c : clazz.getClasses()) {
				String key = chr + "e" + c.getSimpleName();
				if (map.get("classes").containsKey(key)) {
					continue;
				}
				map.get("classes").put(key, new String[] { chr + "eclass " + chr + "f" + c.getName() });
			}
		}
		if (clazz.getFields().length > 0) {
			if (!map.containsKey("fields")) {
				map.put("fields", Maps.newHashMap());
			}
			for (Field f : clazz.getFields()) {
				String key = chr + "6" + f.getName();
				if (map.get("fields").containsKey(key)) {
					continue;
				}
				map.get("fields").put(key,
						new String[] { f.getType() == null ? chr + "4null" : chr + "f" + f.getType().getName() });
			}
		}
		if (clazz.getMethods().length > 0) {
			if (!map.containsKey("methods")) {
				map.put("methods", Maps.newHashMap());
			}
			for (Method m : clazz.getMethods()) {
				String key = chr + "a" + m.getName();
				String ret = m.getReturnType().getName().equals("void") ? ""
						: chr + "cReturn: " + chr + "f" + m.getReturnType().getName();
				String[] ht = ret.isEmpty() ? null : new String[] { ret };
				key += "(";
				if (m.getParameters().length > 0) {
					Parameter[] pars = m.getParameters();
					ht = new String[pars.length + (!ret.isEmpty() ? 1 : 0)];
					int i;
					for (i = 0; i < pars.length; i++) {
						String n = pars[i].getType().getSimpleName().toLowerCase();
						if (n.equalsIgnoreCase("integer")) {
							n = "int";
						}
						key += n + ",";
						ht[i] = chr + "2" + n + chr + "r - " + pars[i].getType().getName();
					}
					key = key.substring(0, key.length() - 1);
					if (!ret.isEmpty()) {
						ht[ht.length - 1] = ret;
					}
				}
				key += ")";
				if (map.get("methods").containsKey(key)) {
					continue;
				}
				map.get("methods").put(key, ht);
			}
		}
		return map;
	}

	private static Class<?> getScriptClass(Object obj) {
		Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
		if (AdditionalMethods.map.containsKey(clazz)) {
			clazz = AdditionalMethods.map.get(clazz);
		}
		return clazz;
	}

	public static Object[] getScriptData(String language, ScriptContainer container,
			Map<String, Class<?>> baseFuncNames) {
		if (container == null) {
			return new Object[] { new ArrayList<IScriptData>(), "Container: null!" };
		}
		container.setEngine(language);
		// container.engine == jdk.nashorn.api.scripting.NashornScriptEngine
		String error = "";
		try {
			container.engine.eval(container.getFullCode());
		} catch (ScriptException e) {
			error = "" + e;
		}
		// Bindings = jdk.nashorn.api.scripting.ScriptObjectMirror
		Bindings scriptObjectMirror = container.engine.getBindings(ScriptContext.ENGINE_SCOPE);
		List<IScriptData> list = Lists.newArrayList();
		List<ScriptData> vars = Lists.newArrayList();
		for (Map.Entry<String, Object> scopeEntry : scriptObjectMirror.entrySet()) {
			ScriptData sd = new ScriptData(scopeEntry.getKey(), scopeEntry.getValue(), language);
			sd.isConstant = container.varIsConstant(scopeEntry.getKey());
			if (sd.isConstant) {
				vars.add(sd);
			}
			list.add(sd);
		}
		for (IScriptData isd : list) {
			if (isd.getType() == 12) {
				((ScriptData) isd).setVarToFunction(vars, baseFuncNames);
			}
		}
		return new Object[] { list, error };
	}

	/**
	 * 1234567890.9 to 1,2G
	 * 
	 * @param value
	 *			- any number
	 * @param color
	 *			- need set color
	 * @return string
	 */
	public static String getTextReducedNumber(double value, boolean isInteger, boolean color, boolean notPfx) {
		if (value == 0) {
			return "0";
		}
		String chr = new String(Character.toChars(0x00A7));
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
		String num = isInteger ? ("" + (int) corr) : ("" + corr).replace(".", ",");
		return sufc + num + type + end;
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

	public static String match(String text, int pos, char[] starts, char[] ends) {
		int s = 0, e = text.length();
		for (int i = pos - 1; i >= 0; i--) {
			if (starts != null) {
				boolean has = false;
				for (char c : starts) {
					if (text.charAt(i) == c) {
						has = true;
						break;
					}
				}
				if (has) {
					e = i;
					break;
				}
				continue;
			}
			char ch = text.charAt(i);
			if (Character.toChars(0x000A)[0]==ch || !Character.isAlphabetic(ch) && !Character.isDigit(ch)) {
				if ( ch== '.' || ch == '(' || ch == Character.toChars(0x000A)[0]) {
					s = i + 1;
				} else {
					s = i;
				}
				break;
			}
		}
		for (int i = pos; i < text.length(); i++) {
			if (ends != null) {
				boolean has = false;
				for (char c : ends) {
					if (text.charAt(i) == c) {
						has = true;
						break;
					}
				}
				if (has) {
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
	}

	/** Correct deletion of folders */
	public static boolean remove(File directory) {
		if (!directory.isDirectory()) {
			return directory.delete();
		}
		File[] list = directory.listFiles();
		if (list != null) {
			for (File tempFile : list) {
				if (tempFile.isDirectory()) {
					remove(tempFile);
				}
				tempFile.delete();
			}
		}
		return directory.delete();
	}

	public static boolean removeItem(EntityPlayerMP player, ItemStack stack, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || stack.isEmpty()) {
			return false;
		}
		int count = stack.getCount();
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
	
	public static boolean canAddItemAfterRemoveItems(EntityPlayer player, ItemStack addStack, NonNullList<ItemStack> removeItems, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || addStack.isEmpty()) { return false; }
		if (player.capabilities.isCreativeMode) { return true; }
		NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (NoppesUtilServer.IsItemStackNull(player.inventory.getStackInSlot(i))) { continue; }
			inv.set(i, player.inventory.getStackInSlot(i).copy());
		}
		if (removeItems!=null && !removeItems.isEmpty()) {
			for (ItemStack stack : removeItems) {
				if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
				int count = stack.getCount();
				for (int i = 0; i < inv.size(); ++i) {
					ItemStack is = inv.get(i);
					if (NoppesUtilServer.IsItemStackNull(is)) { continue; }
					if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
						if (count < is.getCount()) {
							is.splitStack(count);
							inv.set(i, is);
							count = 0;
						}
						else {
							count -= is.getCount();
							inv.set(i, ItemStack.EMPTY);
						}
						if (count<=0) { break; }
					}
				}
			}
		}
		for (int i = 0; i < inv.size(); ++i) {
			if (inv.get(i).isEmpty() || NoppesUtilPlayer.compareItems(addStack, inv.get(i), ignoreDamage, ignoreNBT)) { return true; }
		}
		return false;
	}
	
	public static boolean canRemoveItems(EntityPlayer player, NonNullList<ItemStack> items, boolean ignoreDamage, boolean ignoreNBT) {
		if (player == null || items.isEmpty()) {
			return false;
		}
		NonNullList<ItemStack> inv = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
			if (NoppesUtilServer.IsItemStackNull(player.inventory.getStackInSlot(i))) { continue; }
			inv.set(i, player.inventory.getStackInSlot(i).copy());
		}
		boolean canRemove = true;
		for (ItemStack stack : items) {
			if (NoppesUtilServer.IsItemStackNull(stack)) { continue; }
			int count = stack.getCount();
			for (int i = 0; i < inv.size(); ++i) {
				ItemStack is = inv.get(i);
				if (NoppesUtilServer.IsItemStackNull(is)) { continue; }
				if (NoppesUtilPlayer.compareItems(stack, is, ignoreDamage, ignoreNBT)) {
					if (count < is.getCount()) {
						is.splitStack(count);
						inv.set(i, is);
						count = 0;
					}
					else {
						count -= is.getCount();
						inv.set(i, ItemStack.EMPTY);
					}
					if (count<=0) { break; }
				}
			}
			if (count>0) {
				canRemove = false;
				break;
			}
		}
		return canRemove;
	}

	@SideOnly(Side.CLIENT)
	public static void resetRecipes(EntityPlayer player, GuiContainer gui) {
		CustomNpcs.proxy.updateRecipes(null, false, false, "AdditionalMethods.resetRecipes()");

		Container conteiner = null;
		SlotCrafting slotIn = null;
		List<RecipeList> lists = null;

		if (gui instanceof GuiCrafting) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomItems.tab, true));
			ObfuscationHelper.setValue(GuiCrafting.class, (GuiCrafting) gui, recipeBookGui, GuiRecipeBook.class);

			conteiner = (ContainerWorkbench) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerWorkbench) conteiner).craftMatrix,
					((ContainerWorkbench) conteiner).craftResult, 0, 124, 35);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab);
		} else if (gui instanceof GuiInventory) {
			GuiNpcRecipeBook recipeBookGui = new GuiNpcRecipeBook(true);
			recipeBookGui.getRecipeTabs().add(new GuiNpcButtonRecipeTab(0, CustomItems.tab, true));
			ObfuscationHelper.setValue(GuiInventory.class, (GuiInventory) gui, recipeBookGui, 3);

			conteiner = (ContainerPlayer) gui.inventorySlots;
			slotIn = new SlotNpcCrafting(player, ((ContainerPlayer) conteiner).craftMatrix,
					((ContainerPlayer) conteiner).craftResult, 0, 154, 28);
			slotIn.slotNumber = 0;
			lists = RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab);
		}
		if (gui instanceof GuiNpcCarpentryBench) {
			lists = ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab);
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

	/**
	 * @param ticks
	 *			- time
	 * @param isMilliSeconds
	 *			= true - milliseconds (1 sec = 1000 ms)
	 * @param isMilliSeconds
	 *			= false - minecraft time (1 sec = 20 tick)
	 * @param colored
	 *			- added color
	 * @param upped
	 *			- only the maximum period (years or months or days, etc.)
	 * @return
	 */
	public static String ticksToElapsedTime(long ticks, boolean isMilliSeconds, boolean colored, boolean upped) {
		String time = isMilliSeconds ? "0.000" : "--/--";
		String chr = new String(Character.toChars(0x00A7));
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

	public static void updatePlayerInventory(EntityPlayerMP player) {
		PlayerQuestData playerdata = PlayerData.get(player).questData;
		for (QuestData data : playerdata.activeQuests.values()) { // Changed
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
				if (obj.getType() != 0) {
					continue;
				}
				playerdata.checkQuestCompletion(player, data);
			}
		}
	}

	public static boolean equalsDeleteColor(String str0, String str1, boolean ignoreCase) {
		str0 = AdditionalMethods.deleteColor(str0);
		str1 = AdditionalMethods.deleteColor(str1);
		return ignoreCase ? str0.equalsIgnoreCase(str1) : str0.equals(str1);
	}

	public static String getDeleteColor(Map<?, ?> map, String str0, boolean isKey, boolean ignoreCase) {
		for (Object obj : (isKey ? map.keySet() : map.values())) {
			if (AdditionalMethods.equalsDeleteColor(obj.toString(), str0, ignoreCase)) { return obj.toString(); }
		}
		return "";
	}

	public static boolean containsDeleteColor(Set<String> set, String text, boolean ignoreCase) {
		if (set==null || text==null) { return false; }
		for (String str : set) {
			if (AdditionalMethods.equalsDeleteColor(str, text, ignoreCase)) { return true; }
		}
		return false;
	}

	public static void addStackToList(List<ItemStack> list, ItemStack stack) {
		int count = stack.getCount();
		for (ItemStack st : list) {
			if (AdditionalMethods.canMergeStacks(st, stack)) {
				if (st.getMaxStackSize() >= st.getCount()+count) {
					st.setCount(st.getCount()+count);
					count = 0;
					break;
				} else {
					count -= (st.getMaxStackSize()-st.getCount());
					st.setCount(st.getMaxStackSize());
				}
			}
		}
		if (count>0) {
			ItemStack st = stack.copy();
			st.setCount(count);
			list.add(st);
		}
	}

	public static boolean canMergeStacks(ItemStack stack1, ItemStack stack2) {
		return !stack1.isEmpty() &&
				stack1.getItem() == stack2.getItem() &&
				(!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) &&
				ItemStack.areItemStackTagsEqual(stack1, stack2) &&
				stack1.isStackable() &&
				stack1.getCount() < stack1.getMaxStackSize();
	}
}
