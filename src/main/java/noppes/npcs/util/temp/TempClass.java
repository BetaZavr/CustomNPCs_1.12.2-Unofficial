package noppes.npcs.util.temp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.role.IJobFarmer;
import noppes.npcs.api.entity.data.role.IRoleTrader;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.CustomParticleEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PackageReceived;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.CustomPotionEvent;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.api.gui.ITexturedButton;
import noppes.npcs.api.handler.capability.INbtHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.blocks.tiles.TileScripted.TextPlane;
import noppes.npcs.capability.NbtStorage;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig.PartConfig;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.roles.JobFarmer;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.AdditionalMethods;

public class TempClass {

	public static Map<Class<?>, String> apis = Maps.<Class<?>, String>newHashMap();
	public static Map<Class<?>, String> evs = Maps.<Class<?>, String>newHashMap();
	public static Map<String, List<Class<?>>> imps = Maps.<String, List<Class<?>>>newTreeMap();
	public static Map<Class<?>, List<Class<?>>> fulls = Maps.<Class<?>, List<Class<?>>>newHashMap();
	public static List<File> javaFiles = Lists.<File>newArrayList();
	public static Map<Class<?>, TempDataClass> tempMap;
	public static Map<Class<?>, EnumScriptType> scType;
	private static Map<String, String> bl/*, ru*/;
	
	static {
		TempClass.scType = Maps.<Class<?>, EnumScriptType>newHashMap();
		TempClass.scType.put(BlockEvent.BreakEvent.class, EnumScriptType.BROKEN);
		TempClass.scType.put(BlockEvent.ClickedEvent.class, EnumScriptType.CLICKED);
		TempClass.scType.put(BlockEvent.CollidedEvent.class, EnumScriptType.COLLIDE);
		TempClass.scType.put(BlockEvent.DoorToggleEvent.class, EnumScriptType.DOOR_TOGGLE);
		TempClass.scType.put(BlockEvent.EntityFallenUponEvent.class, EnumScriptType.FALLEN_UPON);
		TempClass.scType.put(BlockEvent.ExplodedEvent.class, EnumScriptType.EXPLODED);
		TempClass.scType.put(BlockEvent.HarvestedEvent.class, EnumScriptType.HARVESTED);
		TempClass.scType.put(BlockEvent.InitEvent.class, EnumScriptType.INIT);
		TempClass.scType.put(BlockEvent.InteractEvent.class, EnumScriptType.INTERACT);
		TempClass.scType.put(BlockEvent.NeighborChangedEvent.class, EnumScriptType.NEIGHBOR_CHANGED);
		TempClass.scType.put(BlockEvent.RainFillEvent.class, EnumScriptType.RAIN_FILLED);
		TempClass.scType.put(BlockEvent.RedstoneEvent.class, EnumScriptType.REDSTONE);
		TempClass.scType.put(BlockEvent.TimerEvent.class, EnumScriptType.TIMER);
		TempClass.scType.put(BlockEvent.UpdateEvent.class, EnumScriptType.TICK);
		TempClass.scType.put(CustomContainerEvent.CloseEvent.class, EnumScriptType.CUSTOM_CHEST_CLOSED);
		TempClass.scType.put(CustomContainerEvent.SlotClickedEvent.class, EnumScriptType.CUSTOM_CHEST_CLICKED);
		TempClass.scType.put(CustomGuiEvent.ButtonEvent.class, EnumScriptType.CUSTOM_GUI_BUTTON);
		TempClass.scType.put(CustomGuiEvent.CloseEvent.class, EnumScriptType.CUSTOM_GUI_CLOSED);
		TempClass.scType.put(CustomGuiEvent.ScrollEvent.class, EnumScriptType.CUSTOM_GUI_SCROLL);
		TempClass.scType.put(CustomGuiEvent.SlotClickEvent.class, EnumScriptType.CUSTOM_GUI_SLOT_CLICKED);
		TempClass.scType.put(CustomGuiEvent.SlotEvent.class, EnumScriptType.CUSTOM_GUI_SLOT);
		TempClass.scType.put(CustomGuiEvent.KeyPressedEvent.class, EnumScriptType.KEY_UP);
		TempClass.scType.put(CustomParticleEvent.CreateEvent.class, EnumScriptType.ATTACK);
		TempClass.scType.put(CustomParticleEvent.RenderEvent.class, EnumScriptType.ATTACK);
		TempClass.scType.put(CustomParticleEvent.UpdateEvent.class, EnumScriptType.ATTACK);
		TempClass.scType.put(AffectEntity.class, EnumScriptType.POTION_AFFECT);
		TempClass.scType.put(EndEffect.class, EnumScriptType.POTION_END);
		TempClass.scType.put(IsReadyEvent.class, EnumScriptType.POTION_IS_READY);
		TempClass.scType.put(PerformEffect.class, EnumScriptType.POTION_PERFORM);
		TempClass.scType.put(DialogEvent.CloseEvent.class, EnumScriptType.DIALOG_CLOSE);
		TempClass.scType.put(DialogEvent.OpenEvent.class, EnumScriptType.DIALOG);
		TempClass.scType.put(DialogEvent.OptionEvent.class, EnumScriptType.DIALOG_OPTION);
		TempClass.scType.put(ForgeEvent.InitEvent.class, EnumScriptType.INIT);
		TempClass.scType.put(ForgeEvent.SoundTickEvent.class, EnumScriptType.SOUND_TICK_EVENT);
		TempClass.scType.put(ItemEvent.AttackEvent.class, EnumScriptType.ATTACK);
		TempClass.scType.put(ItemEvent.InitEvent.class, EnumScriptType.INIT);
		TempClass.scType.put(ItemEvent.InteractEvent.class, EnumScriptType.INTERACT);
		TempClass.scType.put(ItemEvent.PickedUpEvent.class, EnumScriptType.PICKEDUP);
		TempClass.scType.put(ItemEvent.SpawnEvent.class, EnumScriptType.SPAWN);
		TempClass.scType.put(ItemEvent.TossedEvent.class, EnumScriptType.TOSSED);
		TempClass.scType.put(ItemEvent.UpdateEvent.class, EnumScriptType.TICK);
		TempClass.scType.put(NpcEvent.CollideEvent.class, EnumScriptType.COLLIDE);
		TempClass.scType.put(NpcEvent.CustomNpcTeleport.class, EnumScriptType.CUSTOM_TELEPORT);
		TempClass.scType.put(NpcEvent.DamagedEvent.class, EnumScriptType.DAMAGED);
		TempClass.scType.put(NpcEvent.DiedEvent.class, EnumScriptType.DIED);
		TempClass.scType.put(NpcEvent.InitEvent.class, EnumScriptType.INIT);
		TempClass.scType.put(NpcEvent.InteractEvent.class, EnumScriptType.INTERACT);
		TempClass.scType.put(NpcEvent.KilledEntityEvent.class, EnumScriptType.KILL);
		TempClass.scType.put(NpcEvent.MeleeAttackEvent.class, EnumScriptType.ATTACK_MELEE);
		TempClass.scType.put(NpcEvent.RangedLaunchedEvent.class, EnumScriptType.RANGED_LAUNCHED);
		TempClass.scType.put(NpcEvent.StopAnimation.class, EnumScriptType.STOP_ANIMATION);
		TempClass.scType.put(NpcEvent.TargetEvent.class, EnumScriptType.TARGET);
		TempClass.scType.put(NpcEvent.TargetLostEvent.class, EnumScriptType.TARGET_LOST);
		TempClass.scType.put(NpcEvent.TimerEvent.class, EnumScriptType.TIMER);
		TempClass.scType.put(NpcEvent.UpdateEvent.class, EnumScriptType.TICK);
		TempClass.scType.put(PackageReceived.class, EnumScriptType.PACKEGE_RECEIVED);
		TempClass.scType.put(PlayerEvent.AttackEvent.class, EnumScriptType.ATTACK);
		TempClass.scType.put(PlayerEvent.BreakEvent.class, EnumScriptType.BROKEN);
		TempClass.scType.put(PlayerEvent.ChatEvent.class, EnumScriptType.CHAT);
		TempClass.scType.put(PlayerEvent.ContainerClosed.class, EnumScriptType.CONTAINER_CLOSED);
		TempClass.scType.put(PlayerEvent.ContainerOpen.class, EnumScriptType.CONTAINER_OPEN);
		TempClass.scType.put(PlayerEvent.CustomTeleport.class, EnumScriptType.CUSTOM_TELEPORT);
		TempClass.scType.put(PlayerEvent.DamagedEntityEvent.class, EnumScriptType.DAMAGED_ENTITY);
		TempClass.scType.put(PlayerEvent.DamagedEvent.class, EnumScriptType.DAMAGED);
		TempClass.scType.put(PlayerEvent.DiedEvent.class, EnumScriptType.DIED);
		TempClass.scType.put(PlayerEvent.FactionUpdateEvent.class, EnumScriptType.TICK);
		TempClass.scType.put(PlayerEvent.InitEvent.class, EnumScriptType.INIT);
		TempClass.scType.put(PlayerEvent.InteractEvent.class, EnumScriptType.INTERACT);
		TempClass.scType.put(PlayerEvent.ItemCrafted.class, EnumScriptType.ITEM_CRAFTED);
		TempClass.scType.put(PlayerEvent.ItemFished.class, EnumScriptType.ITEM_FISHED);
		TempClass.scType.put(PlayerEvent.KeyActive.class, EnumScriptType.KEY_ACTIVE);
		TempClass.scType.put(PlayerEvent.KeyPressedEvent.class, EnumScriptType.KEY_DOWN); // here
		TempClass.scType.put(PlayerEvent.KilledEntityEvent.class, EnumScriptType.KILL);
		TempClass.scType.put(PlayerEvent.LevelUpEvent.class, EnumScriptType.LEVEL_UP);
		TempClass.scType.put(PlayerEvent.LoginEvent.class, EnumScriptType.LOGIN);
		TempClass.scType.put(PlayerEvent.LogoutEvent.class, EnumScriptType.LOGOUT);
		TempClass.scType.put(PlayerEvent.OpenGUI.class, EnumScriptType.GUI_OPEN);
		TempClass.scType.put(PlayerEvent.PickUpEvent.class, EnumScriptType.PICKUP);
		TempClass.scType.put(PlayerEvent.PlaceEvent.class, EnumScriptType.PLASED);
		TempClass.scType.put(PlayerEvent.PlayerPackage.class, EnumScriptType.PACKEGE_FROM);
		TempClass.scType.put(PlayerEvent.PlayerSound.class, EnumScriptType.SOUND_PLAY); // here
		TempClass.scType.put(PlayerEvent.RangedLaunchedEvent.class, EnumScriptType.RANGED_LAUNCHED);
		TempClass.scType.put(PlayerEvent.TimerEvent.class, EnumScriptType.TIMER);
		TempClass.scType.put(PlayerEvent.TossEvent.class, EnumScriptType.TOSS);
		TempClass.scType.put(PlayerEvent.UpdateEvent.class, EnumScriptType.TICK);
		TempClass.scType.put(ProjectileEvent.ImpactEvent.class, EnumScriptType.PROJECTILE_IMPACT);
		TempClass.scType.put(ProjectileEvent.UpdateEvent.class, EnumScriptType.PROJECTILE_TICK);
		TempClass.scType.put(QuestEvent.QuestCanceledEvent.class, EnumScriptType.QUEST_CANCELED);
		TempClass.scType.put(QuestEvent.QuestCompletedEvent.class, EnumScriptType.QUEST_COMPLETED);
		TempClass.scType.put(QuestEvent.QuestStartEvent.class, EnumScriptType.QUEST_START);
		TempClass.scType.put(QuestEvent.QuestTurnedInEvent.class, EnumScriptType.QUEST_TURNIN);
		TempClass.scType.put(RoleEvent.BankUnlockedEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.BankUpgradedEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.FollowerFinishedEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.FollowerHireEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.MailmanEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.TradeFailedEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.TraderEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.TransporterUnlockedEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(RoleEvent.TransporterUseEvent.class, EnumScriptType.ROLE);
		TempClass.scType.put(WorldEvent.ScriptCommandEvent.class, EnumScriptType.SCRIPT_COMMAND);
		TempClass.scType.put(WorldEvent.ScriptTriggerEvent.class, EnumScriptType.SCRIPT_TRIGGER);
	}
	
	public static void deobfucation() {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() && i<=10) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		if (!dir.exists()) { return; }
		final File dirSave = new File(dir, "deobfucation");
		if (!dirSave.exists()) { dirSave.mkdirs(); return; }
		System.out.println("Start deobfucation in work place: \""+dirSave.getAbsolutePath()+"\"");
		if (TempClass.javaFiles.isEmpty()) {
			TempClass.collectFiles(dirSave, ".java");
		}
		TreeMap<String, String> map = AdditionalMethods.instance.obfuscations;
		long g = 0;
		int iv = 0;
		for (File f : TempClass.javaFiles) {
			String fileText = null;
			try { fileText = Files.toString(f, Charset.forName("UTF-8")); } catch (IOException e) { }
			if (fileText==null) { continue; }
			for (String key : map.keySet()) {
				while(fileText.indexOf(key)!=-1) {
					fileText = fileText.replace(key, map.get(key));
					g++;
				}
			}
			if (fileText.indexOf("package ")!=-1) { fileText = fileText.substring(fileText.indexOf("package ")); }
			try {
				Files.write(fileText.getBytes(), f);
				System.out.println("Save Deobfucation: "+f.getAbsolutePath());
				iv ++;
			} catch (Exception e) { }
		}
		System.out.println("Total deobfucation Files: "+iv+"; total correct keys: "+g);
	}

	public static void createAPIs(boolean acceptAllVariablesAndMethods) {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() && i<=10) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		if (!dir.exists()) { return; }
		System.out.println("Start created "+(acceptAllVariablesAndMethods ? "full " : "")+"APIs TypeScript in work place: \""+dir.getAbsolutePath()+"\"");
		//if (TempClass.apis.isEmpty()) {
			TempClass.imps.clear();
			TempClass.evs.clear();
			TempClass.fulls.clear();
			TempClass.collectInterfases(dir, acceptAllVariablesAndMethods);
			if (acceptAllVariablesAndMethods && !TempClass.fulls.isEmpty()) {
				Map<Class<?>, List<Class<?>>> newFulls = Maps.<Class<?>, List<Class<?>>>newHashMap();
				for (Class<?> z : TempClass.fulls.keySet()) {
					for (Class<?> inf : z.getInterfaces()) {
						if (TempClass.apis.containsKey(inf)) {
							if (!newFulls.containsKey(inf)) { newFulls.put(inf, Lists.<Class<?>>newArrayList()); }
							newFulls.get(inf).add(z);
						}
					}
				}
				newFulls.put(IAvailability.class, Lists.<Class<?>>newArrayList(Availability.class));
				newFulls.put(IQuest.class, Lists.<Class<?>>newArrayList(Quest.class));
				newFulls.put(IAnimation.class, Lists.<Class<?>>newArrayList(AnimationConfig.class));
				newFulls.put(IAnimationFrame.class, Lists.<Class<?>>newArrayList(AnimationFrameConfig.class));
				newFulls.put(IAnimationPart.class, Lists.<Class<?>>newArrayList(PartConfig.class));
				newFulls.put(ITextPlane.class, Lists.<Class<?>>newArrayList(TextPlane.class));
				newFulls.put(IItemScripted.class, Lists.<Class<?>>newArrayList(ItemScriptedWrapper.class));
				newFulls.put(IItemStack.class, Lists.<Class<?>>newArrayList(ItemStackWrapper.class));
				newFulls.put(IBorder.class, Lists.<Class<?>>newArrayList(Zone3D.class));
				newFulls.put(IDialog.class, Lists.<Class<?>>newArrayList(Dialog.class));
				newFulls.put(IJobFarmer.class, Lists.<Class<?>>newArrayList(JobFarmer.class));
				newFulls.put(IRoleTrader.class, Lists.<Class<?>>newArrayList(RoleTrader.class));
				newFulls.put(ICustomDrop.class, Lists.<Class<?>>newArrayList(DropSet.class));
				newFulls.put(IMark.class, Lists.<Class<?>>newArrayList(Mark.class));
				newFulls.put(INPCInventory.class, Lists.<Class<?>>newArrayList(DataInventory.class));
				newFulls.put(IPlayerMail.class, Lists.<Class<?>>newArrayList(PlayerMail.class));
				newFulls.put(ITexturedButton.class, Lists.<Class<?>>newArrayList(CustomGuiButtonWrapper.class));
				newFulls.put(INbtHandler.class, Lists.<Class<?>>newArrayList(NbtStorage.class));
				
				if (!newFulls.containsKey(ICustomNpc.class)) {newFulls.put(ICustomNpc.class, Lists.<Class<?>>newArrayList());}
				newFulls.get(ICustomNpc.class).add(EntityNPCInterface.class);
				newFulls.get(ICustomNpc.class).add(EntityCustomNpc.class);
				
				TempClass.fulls = newFulls;
			}
		//}
		if (TempClass.apis.isEmpty()) { return; }
		char ent = ((char) 10);
		
		File lang = new File(dir, "keys.lang");
		TempClass.bl = Maps.<String, String>newTreeMap();
		try {
			String[] langs = Files.toString(lang, Charset.forName("Windows-1251")).split(""+((char) 10));
			for (String line : langs) {
				if (line.indexOf("=")==-1) { continue; }
				String key = line.substring(0, line.indexOf("="));
				String value = line.indexOf("=")+1<=line.length() ? line.substring(line.indexOf("=")+1) : "";
				if (value.isEmpty()) { continue; }
				TempClass.bl.put(key, value);
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		
		Calendar cldr = Calendar.getInstance();
		String day = ""+cldr.get(Calendar.DAY_OF_MONTH);
		String month = ""+(cldr.get(Calendar.MONTH)+1);
		if (day.length()==1) { day = "0" + day; }
		if (month.length()==1) { month = "0" + month; }
		
		String text, main = "<big>'''"+TempClass.getLoc("api")+day+"."+month+"."+cldr.get(Calendar.YEAR)+".'''</big><br>" + ent +
				TempClass.getLoc("api.i") + ent +
				"== "+TempClass.getLoc("rs")+" ==" + ent +
				"{| class=\"wikitable\" style=\"text-align:left" + ent +
				TempClass.getLoc("pv") + ent;
		
		TempClass.tempMap = Maps.<Class<?>, TempDataClass>newLinkedHashMap();
		for (String key : TempClass.imps.keySet()) {
			String path = key;
			while(path.indexOf("/")!=-1) { path = path.replace("/", "."); }
			String k = path;
			if (k.lastIndexOf(".")!=-1) {
				k = k.substring(k.lastIndexOf(".")+1);
			}
			main += "|-" + ent +
					"| [[Custom NPCs/Unoficial_API_1.12.2/"+k+"|noppes.npcs."+path+"]]" + ent +
					"| ''"+TempClass.getLoc("path."+path)+"''" + ent;
			
			text = "'''"+TempClass.getLoc("r")+" noppes.npcs."+path+"'''<br>" + ent +
					"{| class=\"wikitable\" style=\"text-align:left" + ent +
					TempClass.getLoc("pv") + ent;
			for (Class<?> c : TempClass.imps.get(key)) {
				k = c.getSimpleName();
				text += "|-" + ent +
						"| [[Custom NPCs/Unoficial_API_1.12.2/"+key+"/"+k+"|"+k+"]]" + ent +
						"| ''"+TempClass.getLoc("interfase."+path+"."+k)+"''" + ent;
				Class<?> api = c;
				if (acceptAllVariablesAndMethods && TempClass.fulls.containsKey(c) && TempClass.fulls.get(c).size()==1) {
					api = TempClass.fulls.get(c).get(0);
				}
				if (api.isEnum()) {
					TempClass.tempMap.put(api, new TempDataClass(key, dir, api, TempClass.apis.get(api)));
				} else if (api==CustomNPCsEvent.class) {
					TempClass.tempMap.put(api, new TempDataClass(key, dir, api, api, TempClass.apis.get(api)));
				} else if (api.getSuperclass()==CustomNPCsEvent.class || api.getSuperclass()==NpcEvent.class) {
					if (!TempClass.tempMap.containsKey(api)) { TempClass.tempMap.put(api, new TempDataClass(key, dir, api, api, TempClass.apis.get(api))); }
					for (Class<?> evC : api.getDeclaredClasses()) {
						if (!Modifier.isPublic(evC.getModifiers())) { continue; }
						TempClass.tempMap.put(evC, new TempDataClass(key, dir, api, evC, TempClass.apis.get(api)));
					}
				} else if (api.getSuperclass()==CustomPotionEvent.class) {
					if (!TempClass.tempMap.containsKey(CustomPotionEvent.class)) { TempClass.tempMap.put(CustomPotionEvent.class, new TempDataClass(key, dir, CustomPotionEvent.class, CustomPotionEvent.class, TempClass.apis.get(CustomPotionEvent.class))); }
					TempClass.tempMap.put(c, new TempDataClass(key, dir, CustomPotionEvent.class, c, TempClass.apis.get(c)));
				} else {
					TempClass.tempMap.put(c, new TempDataClass(key, dir, api, c, TempClass.apis.get(api), TempClass.apis.get(c)));
				}
			}
			TempClass.saveFile(new File(dir, key+"/main.txt"), text);
		}
		main += "|}";
		TempClass.saveFile(new File(dir, "main.txt"), main);
		
		// Events
		Map<Class<?>, Map<String, TempDataClass>> events = Maps.<Class<?>, Map<String, TempDataClass>>newHashMap();
		for (Class<?> c : TempClass.tempMap.keySet()) {
			TempDataClass tdc = TempClass.tempMap.get(c);
			if (!tdc.isEvent) { continue; }
			if (!events.containsKey(tdc.ext)) { events.put(tdc.ext, Maps.<String, TempDataClass>newTreeMap()); }
			events.get(tdc.ext).put(tdc.real.getSimpleName(), tdc);
		}
		for (Class<?> base : events.keySet()) {
			String clName = base.getName().replace(base.getSimpleName(), "").toLowerCase();
			while (clName.indexOf(".")!=-1) { clName = clName.replace(".", "/"); }
			clName = base.getName().replace(base.getSimpleName(), "")+"[https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/"+clName+base.getSimpleName()+".java "+base.getSimpleName()+"]";
			main = "<big>'''"+TempClass.getLoc("c")+clName+"'''</big><br>" + ent +
					TempClass.getLoc("all.sub.v") + ent;
			String subCls = "";
			text = "{| class=\"wikitable\" style=\"text-align:left" + ent +
					TempClass.getLoc("pe") + ent;
			for (String event : events.get(base).keySet()) {
				EnumScriptType f = TempClass.scType.get(events.get(base).get(event).real);
				if (f==null) { continue; }
				if (!subCls.isEmpty()) { subCls += ", "; }
				subCls += "[[Custom NPCs/Unoficial_API_1.12.2/api/event/"+base.getSimpleName()+"/"+event+"|"+event+"]]";
				String func = f.function;
				String sfx = "";
				if (func == EnumScriptType.KEY_DOWN.function) { sfx = ".down"; }
				if (func == EnumScriptType.SOUND_PLAY.function) { sfx = ".play"; }
				text += "|-" + ent +
						"| [[Custom NPCs/Unoficial_API_1.12.2/api/event/"+base.getSimpleName()+"/"+event+"|"+base.getSimpleName()+"."+event+"]]" + ent +
						"| {{"+TempClass.getLoc("color")+"|DarkGreen|'''" + func + "'''}}" + ent +
						"| ''" + TempClass.getLoc("interfase.api.event."+base.getSimpleName()+"."+event+sfx) + "''" + ent;
				if (func == EnumScriptType.KEY_DOWN.function || func == EnumScriptType.SOUND_PLAY.function) {
					subCls += "[[Custom NPCs/Unoficial_API_1.12.2/api/event/"+base.getSimpleName()+"/"+event+"|"+event+"]]";
					if (func == EnumScriptType.KEY_DOWN.function) { func = EnumScriptType.KEY_UP.function; sfx = ".up"; }
					if (func == EnumScriptType.SOUND_PLAY.function) { func = EnumScriptType.SOUND_STOP.function; sfx = ".stop"; }
					text += "|-" + ent +
							"| [[Custom NPCs/Unoficial_API_1.12.2/api/event/"+base.getSimpleName()+"/"+event+"|"+base.getSimpleName()+"."+event+"]]" + ent +
							"| {{"+TempClass.getLoc("color")+"|DarkGreen|'''" + func + "'''}}" + ent +
							"| ''" + TempClass.getLoc("interfase.api.event."+base.getSimpleName()+"."+event+sfx) + "''" + ent;
				}
			}
			main += subCls + "<br>" + ent + 
					"== "+TempClass.getLoc("rv")+" ==" + ent +
					text + "|}" + ent;
			
			TempDataClass tcd = TempClass.tempMap.get(base);
			if (tcd!=null) {
				main += tcd.getEventCode();
			} else {
				System.out.println("not found TempDataClass: "+base.getName());
			}
			TempClass.saveFile(new File(dir, "api/event/classes_"+base.getSimpleName()+".txt"), main);
		}
		
		// API
		for (Class<?> c : TempClass.tempMap.keySet()) {
			TempDataClass tdc = TempClass.tempMap.get(c);
			TempClass.saveFile(new File(dir, tdc.path+"/"+(tdc.isEvent ? tdc.ext.getSimpleName()+"_" : "")+c.getSimpleName()+".txt"), tdc.getPageCode());
		}
		
		try {
			text = "";
			String pre = "";
			for (String key : TempClass.bl.keySet()) {
				String p = key;
				if (key.indexOf(".")!=-1) { p = key.substring(0, key.indexOf(".")); }
				if (pre.isEmpty() || !pre.equals(p)) {
					if (!pre.isEmpty()) { text += ent; }
					pre = p;
				}
				text += key + "=" + TempClass.bl.get(key) + ent;
			}
			Files.write(text.getBytes(), lang);
			System.out.println("Save Keys data: "+lang);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		try {
			text = "";
			List<String> list = Lists.newArrayList(CustomNpcs.forgeEventNames.values());
			Collections.sort(list);
			for (String key : list) {
				if (!text.isEmpty()) { text += ", "; }
				text += key;
			}
			Files.write(text.getBytes(), new File(dir, "forge_names.lang"));
			System.out.println("Save Forge names: "+new File(dir, "forge_names.lang"));
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static String getLoc(String loc) {
		String value = TempClass.bl.get(loc);
		if (value!=null && !value.isEmpty()) { return value; }
		/*if (loc.lastIndexOf(".")!=-1 && loc.lastIndexOf(".", loc.lastIndexOf(".")-1)!=-1) {
			String key = loc.substring(loc.lastIndexOf(".", loc.lastIndexOf(".")-1)+1).toLowerCase();
			
			for (String locRu : TempClass.ru.keySet()) {
				if (locRu.toLowerCase().endsWith(key)) {
					TempClass.bl.put(loc, TempClass.ru.get(locRu));
					return TempClass.ru.get(locRu);
				}
			}
		}*/
		TempClass.bl.put(loc, "");
		return loc;
	}

	private static void saveFile(File file, String text) {
		try {
			File dir = file.getParentFile();
			if (!dir.exists()) { dir.mkdirs(); }
			if (!file.exists()) { file.createNewFile(); }
			Files.write(text.getBytes(), file);
		} catch (Exception e) { }
	}

	private static void collectFiles(File dir, String sfx) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.collectFiles(f, sfx); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(sfx)) { TempClass.javaFiles.add(f); }
		}
	}
	
	public static void collectInterfases(File dir, boolean acceptAllVariablesAndMethods) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.collectInterfases(f, acceptAllVariablesAndMethods); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(".java")) {
				try { 
					String classPath = f.getAbsolutePath();
					classPath = classPath.substring(classPath.indexOf("java")+5, classPath.length()-5);
					while(classPath.indexOf("/")!=-1) { classPath = classPath.replace("/", "."); }
					while(classPath.indexOf("\\")!=-1) { classPath = classPath.replace("\\", "."); }
					Class<?> c = null;
					try { c = Class.forName(classPath); } catch (ClassNotFoundException e) { continue; }
					if (c.getName().indexOf(".client")!=-1) { continue; }
					if (acceptAllVariablesAndMethods && !c.isInterface()) {
						try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						TempClass.fulls.put(c, null);
					}
					if (c.getSuperclass()==CustomNPCsEvent.class || c.getSuperclass()==NpcEvent.class || c.getSuperclass()==CustomPotionEvent.class || c.getSuperclass()==Event.class) {
						try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						try { TempClass.evs.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						String inf = "api/event";
						if (!TempClass.imps.containsKey(inf)) { TempClass.imps.put(inf, Lists.<Class<?>>newArrayList()); }
						TempClass.imps.get(inf).add(c);
						continue;
					}
					if (c.isEnum() && c.getName().startsWith("noppes.npcs.api.constants")) {
						try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						String inf = "api/constants";
						if (!TempClass.imps.containsKey(inf)) { TempClass.imps.put(inf, Lists.<Class<?>>newArrayList()); }
						TempClass.imps.get(inf).add(c);
						continue;
					}
					if (c.isInterface() && c.getName().startsWith("noppes.npcs.api") || c == NpcAPI.class) {
						if (c==INbtHandler.class) { continue; }
						try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { continue; }
						String inf = c.getName().replace("noppes.npcs.", "").replace("."+c.getSimpleName(), "");
						if (inf.equals(c.getSimpleName())) { inf = ""; }
						else { while(inf.indexOf(".")!=-1) { inf = inf.replace(".", "/"); } }
						if (!TempClass.imps.containsKey(inf)) { TempClass.imps.put(inf, Lists.<Class<?>>newArrayList()); }
						TempClass.imps.get(inf).add(c);
					}
				}
				catch (Exception e) { System.out.println("Error File: \""+f.getName()+"\" - "+e); }
			}
		}
	}

	public static void cheakLang() {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() && i<=10) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		if (!dir.exists()) { return; }
		List<String> lang = Lists.<String>newArrayList();
		System.out.println("Start cheak Lang in work place: \""+dir.getAbsolutePath()+"\"");
		File en_us = new File(dir, "main/resources/assets/customnpcs/lang/en_us.lang");
		Map<String, String> enMap = Maps.<String, String>newTreeMap();
		String enPfx = "";
		try {
			String[] langs = Files.toString(en_us, Charset.forName("UTF-8")).split(""+((char) 10));
			i = -1;
			for (String line : langs) {
				i++;
				if (line.startsWith("#")) {
					if (!enPfx.isEmpty()) { enPfx += ""+((char) 10); }
					enPfx += line;
					continue;
				}
				if (line.indexOf("=")==-1) { continue; }
				String key = line.substring(0, line.indexOf("="));
				enMap.put(key, line.indexOf("=")+1<=line.length() ? line.substring(line.indexOf("=")+1) : "");
				if (!lang.contains(key)) { lang.add(key); }
				else { System.out.println("found double: \""+key+"\" in line "+i); }
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		File ru_ru = new File(dir, "main/resources/assets/customnpcs/lang/ru_ru.lang");
		Map<String, String> ruMap = Maps.<String, String>newTreeMap();
		String ruPfx = "";
		try {
			String[] langs = Files.toString(ru_ru, Charset.forName("UTF-8")).split(""+((char) 10));
			i = -1;
			for (String line : langs) {
				i++;
				if (line.startsWith("#")) {
					if (!ruPfx.isEmpty()) { ruPfx += ""+((char) 10); }
					ruPfx += line;
					continue;
				}
				if (line.indexOf("=")==-1) { continue; }
				String key = line.substring(0, line.indexOf("="));
				ruMap.put(key, line.indexOf("=")+1<=line.length() ? line.substring(line.indexOf("=")+1) : "");
				if (!lang.contains(key)) { lang.add(key); }
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		TempClass.javaFiles = Lists.<File>newArrayList();
		TempClass.collectFiles(new File(dir, "main/java"), ".java");
		List<String> keys = Lists.<String>newArrayList();
		for (File f : TempClass.javaFiles) {
			try {
				String text = Files.toString(f, Charset.forName("UTF-8"));
				for (String k : lang) {
					if (keys.contains(k)) { continue; }
					String key = "" + k;
					while(key.indexOf(".true")!=-1) { key = key.replace(".true", ""); }
					while(key.indexOf(".false")!=-1) { key = key.replace(".false", ""); }
					String tempKey = key;
					while(tempKey.lastIndexOf(".")!=-1) {
						try {
							Integer.parseInt(tempKey.substring(tempKey.lastIndexOf(".")+1));
							key = tempKey.substring(0, tempKey.lastIndexOf("."));
						}
						catch (Exception e) {}
						tempKey = tempKey.substring(0, tempKey.lastIndexOf("."));
					}
					if (text.indexOf(key)!=-1) {
						keys.add(k);
						continue;
					}
				}
			}
			catch (IOException e) { }
		}
		String ent = "" + ((char) 10);
		try {
			String enText = "" + enPfx + ent + ent;
			String ruText = "" + ruPfx + ent + ent;
			String pre = "";
			int e = 0, r = 0;
			for (String key : enMap.keySet()) {
				if (!keys.contains(key) &&
						!key.startsWith("display.") &&
						!key.startsWith("markov.") &&
						!key.startsWith("menu.") &&
						!key.startsWith("message.") &&
						!key.startsWith("model.") &&
						!key.startsWith("movement.") &&
						!key.startsWith("part.") &&
						!key.startsWith("puppet.") &&
						!key.startsWith("quest.") &&
						!key.startsWith("region.") &&
						!key.startsWith("role.") &&
						!key.startsWith("remote.") &&
						!key.startsWith("schematic.") &&
						!key.startsWith("script.") &&
						!key.startsWith("spawner.") &&
						!key.startsWith("stats.") &&
						!key.startsWith("script.") &&
						!key.startsWith("tag.type.") &&
						!key.startsWith("texture.hover.") &&
						!key.startsWith("task.") &&
						!key.startsWith("tile.") &&
						!key.startsWith("trader.") &&
						!key.startsWith("transporter.") &&
						!key.startsWith("type.") &&
						!key.startsWith("wand.") &&
						!key.startsWith("aitactics.") &&
						!key.startsWith("animation.hover.") &&
						!key.startsWith("availability.") &&
						!key.startsWith("builder.") &&
						!key.startsWith("command.") &&
						!key.startsWith("copy.") &&
						!key.startsWith("dialog.") &&
						!key.startsWith("enchantment.") &&
						!key.startsWith("entity.") &&
						!key.startsWith("enum.") &&
						!key.startsWith("global.") &&
						!key.startsWith("gui.") &&
						!key.startsWith("inv.") &&
						!key.startsWith("item.") &&
						!key.startsWith("itemGroup.") &&
						!key.startsWith("job.") &&
						!key.startsWith("mailbox.") &&
						!key.startsWith("mark.") &&
						!key.startsWith("questlog.")) {
					if (!key.startsWith("method.") && !key.startsWith("parameter.") && !key.startsWith("event.") && !key.startsWith("interfase.") && !key.startsWith("constant.")) {
						System.out.println("Lang not found: \""+key+"\"");
					}
					continue;
				}
				String p = key;
				if (key.indexOf(".")!=-1) { p = key.substring(0, key.indexOf(".")); }
				if (pre.isEmpty() || !pre.equals(p)) {
					if (!pre.isEmpty()) {
						enText += ent;
						if (ruMap.containsKey(key)) { ruText += ent; }
					}
					pre = p;
				}
				enText += key + "=" + enMap.get(key) + ent;
				e++;
				if (ruMap.containsKey(key)) { ruText += key + "=" + ruMap.get(key) + ent; r++; }
			}
			Files.write(enText.getBytes(), new File(dir, "en_us.lang"));
			Files.write(ruText.getBytes(), new File(dir, "ru_ru.lang"));
			System.out.println("Save langs en: "+e+"; ru: "+r);
		}
		catch (Exception e) { e.printStackTrace(); }
		
	}
	
}
