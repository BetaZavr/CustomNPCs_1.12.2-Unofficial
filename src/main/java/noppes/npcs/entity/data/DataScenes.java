package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.ValueUtil;

public class DataScenes {
	
	public class SceneContainer {
		public int btn;
		public boolean enabled;
		private List<SceneEvent> events;
		public String lines;
		public String name;
		private SceneState state;
		public int ticks;

		public SceneContainer() {
			this.btn = 0;
			this.name = "";
			this.lines = "";
			this.enabled = false;
			this.ticks = -1;
			this.state = null;
			this.events = new ArrayList<SceneEvent>();
		}

		private void handle(SceneEvent event) throws Exception {
			if (event.type == SceneType.MOVE) {
				String[] param = event.param.split(" ");
				while (param.length > 1) {
					boolean move = false;
					if (param[0].startsWith("to")) {
						move = true;
					} else if (!param[0].startsWith("tp")) {
						break;
					}
					BlockPos pos = null;
					if (param[0].startsWith("@")) {
						EntityLivingBase entitylivingbase = CommandBase.getEntity(DataScenes.this.npc.getServer(),
								DataScenes.this.npc, param[0], EntityLivingBase.class);
						if (entitylivingbase != null) {
							pos = entitylivingbase.getPosition();
						}
						param = Arrays.copyOfRange(param, 2, param.length);
					} else {
						if (param.length < 4) {
							return;
						}
						pos = CommandBase.parseBlockPos(DataScenes.this.npc, param, 1, false);
						param = Arrays.copyOfRange(param, 4, param.length);
					}
					if (pos == null) {
						continue;
					}
					DataScenes.this.npc.ais.setStartPos(pos);
					DataScenes.this.npc.getNavigator().clearPath();
					DataScenes.this.npc.resetBackPos(); // New
					if (move) {
						Path pathentity = DataScenes.this.npc.getNavigator().getPathToPos(pos);
						DataScenes.this.npc.getNavigator().setPath(pathentity, 1.0);
						DataScenes.this.npc.resetBackPos(); // New
					} else {
						if (DataScenes.this.npc.isInRange(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 2.0)) {
							continue;
						}
						DataScenes.this.npc.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					}
				}
			} else if (event.type == SceneType.SAY) {
				DataScenes.this.npc.saySurrounding(new Line(event.param));
			} else if (event.type == SceneType.ROTATE) {
				DataScenes.this.npc.lookAi.resetTask();
				if (event.param.startsWith("@")) {
					EntityLivingBase entitylivingbase2 = CommandBase.getEntity(DataScenes.this.npc.getServer(),
							DataScenes.this.npc, event.param, EntityLivingBase.class);
					DataScenes.this.npc.lookAi
							.rotate(DataScenes.this.npc.world.getClosestPlayerToEntity(entitylivingbase2, 30.0));
				} else {
					DataScenes.this.npc.lookAi.rotate(Integer.parseInt(event.param));
				}
			} else if (event.type == SceneType.EQUIP) {
				String[] args = event.param.split(" ");
				if (args.length < 2) {
					return;
				}
				IItemStack itemstack = null;
				if (!args[1].equalsIgnoreCase("none")) {
					Item item = CommandBase.getItemByText(DataScenes.this.npc, args[1]);
					int i = (args.length >= 3) ? CommandBase.parseInt(args[2], 1, 64) : 1;
					int j = (args.length >= 4) ? CommandBase.parseInt(args[3]) : 0;
					itemstack = NpcAPI.Instance().getIItemStack(new ItemStack(item, i, j));
				}
				if (args[0].equalsIgnoreCase("main")) {
					DataScenes.this.npc.inventory.weapons.put(0, itemstack);
				} else if (args[0].equalsIgnoreCase("off")) {
					DataScenes.this.npc.inventory.weapons.put(2, itemstack);
				} else if (args[0].equalsIgnoreCase("proj")) {
					DataScenes.this.npc.inventory.weapons.put(1, itemstack);
				} else if (args[0].equalsIgnoreCase("head")) {
					DataScenes.this.npc.inventory.armor.put(0, itemstack);
				} else if (args[0].equalsIgnoreCase("body")) {
					DataScenes.this.npc.inventory.armor.put(1, itemstack);
				} else if (args[0].equalsIgnoreCase("legs")) {
					DataScenes.this.npc.inventory.armor.put(2, itemstack);
				} else if (args[0].equalsIgnoreCase("boots")) {
					DataScenes.this.npc.inventory.armor.put(3, itemstack);
				}
			} else if (event.type == SceneType.ATTACK) {
				if (event.param.equals("none")) {
					DataScenes.this.npc.setAttackTarget(null);
				} else {
					EntityLivingBase entity = CommandBase.getEntity(DataScenes.this.npc.getServer(),
							DataScenes.this.npc, event.param, EntityLivingBase.class);
					if (entity != null) {
						DataScenes.this.npc.setAttackTarget(entity);
					}
				}
			} else if (event.type == SceneType.THROW) {
				String[] args = event.param.split(" ");
				EntityLivingBase entity2 = CommandBase.getEntity(DataScenes.this.npc.getServer(), DataScenes.this.npc,
						args[0], EntityLivingBase.class);
				if (entity2 == null) {
					return;
				}
				float damage = Float.parseFloat(args[1]);
				if (damage <= 0.0f) {
					damage = 0.01f;
				}
				ItemStack stack = ItemStackWrapper.MCItem(DataScenes.this.npc.inventory.getProjectile());
				if (args.length > 2) {
					Item item2 = CommandBase.getItemByText(DataScenes.this.npc, args[2]);
					stack = new ItemStack(item2, 1, 0);
				}
				EntityProjectile projectile = DataScenes.this.npc.shoot(entity2, 100, stack, false);
				projectile.damage = damage;
			} else if (event.type == SceneType.ANIMATE) {
				DataScenes.this.npc.animateAi.temp = 0;
				if (event.param.equalsIgnoreCase("sleep")) {
					DataScenes.this.npc.animateAi.temp = 2;
				} else if (event.param.equalsIgnoreCase("sneak")) {
					DataScenes.this.npc.ais.animationType = 4;
				} else if (event.param.equalsIgnoreCase("normal")) {
					DataScenes.this.npc.ais.animationType = 0;
				} else if (event.param.equalsIgnoreCase("sit")) {
					DataScenes.this.npc.animateAi.temp = 1;
				} else if (event.param.equalsIgnoreCase("crawl")) {
					DataScenes.this.npc.ais.animationType = 7;
				} else if (event.param.equalsIgnoreCase("bow")) {
					DataScenes.this.npc.animateAi.temp = 11;
				} else if (event.param.equalsIgnoreCase("yes")) {
					DataScenes.this.npc.animateAi.temp = 13;
				} else if (event.param.equalsIgnoreCase("no")) {
					DataScenes.this.npc.animateAi.temp = 12;
				}
			} else if (event.type == SceneType.COMMAND) {
				NoppesUtilServer.runCommand(DataScenes.this.npc, DataScenes.this.npc.getName(), event.param, null);
			} else if (event.type == SceneType.STATS) {
				int k = event.param.indexOf(" ");
				if (k <= 0) {
					return;
				}
				String type = event.param.substring(0, k).toLowerCase();
				String value = event.param.substring(k).trim();
				try {
					if (type.equals("walking_speed")) {
						DataScenes.this.npc.ais.setWalkingSpeed(ValueUtil.correctInt(Integer.parseInt(value), 0, 10));
					} else if (type.equals("size")) {
						DataScenes.this.npc.display.setSize(ValueUtil.correctInt(Integer.parseInt(value), 1, 30));
					} else {
						NoppesUtilServer.NotifyOPs("Unknown scene stat: " + type, new Object[0]);
					}
				} catch (NumberFormatException e) {
					NoppesUtilServer.NotifyOPs("Unknown scene stat " + type + " value: " + value, new Object[0]);
				}
			} else if (event.type == SceneType.FACTION) {
				DataScenes.this.npc.setFaction(Integer.parseInt(event.param));
			} else if (event.type == SceneType.FOLLOW) {
				if (event.param.equalsIgnoreCase("none")) {
					DataScenes.this.owner = null;
					DataScenes.this.ownerScene = null;
				} else {
					EntityLivingBase entity = CommandBase.getEntity(DataScenes.this.npc.getServer(),
							DataScenes.this.npc, event.param, EntityLivingBase.class);
					if (entity == null) {
						return;
					}
					DataScenes.this.owner = entity;
					DataScenes.this.ownerScene = this.name;
				}
			}
		}

		public void readFromNBT(NBTTagCompound compound) {
			this.enabled = compound.getBoolean("Enabled");
			this.name = compound.getString("Name");
			this.lines = compound.getString("Lines");
			this.btn = compound.getInteger("Button");
			this.ticks = compound.getInteger("Ticks");
			ArrayList<SceneEvent> events = new ArrayList<SceneEvent>();
			for (String line : this.lines.split("\r\n|\r|\n")) {
				SceneEvent event = SceneEvent.parse(line);
				System.out.println("event: \""+line+"\" = "+event);
				if (event != null) {
					events.add(event);
				}
			}
			Collections.sort(events);
			this.events = events;
		}

		public void update() {
			if (!this.enabled || this.events.isEmpty() || this.state == null) {
				return;
			}
			for (SceneEvent event : this.events) {
				if (event.ticks > this.state.ticks) { break; }
				if (event.ticks != this.state.ticks) { continue; }
				try { this.handle(event); }
				catch (Exception ex) { }
			}
			this.ticks = this.state.ticks;
		}

		public boolean validState() {
			if (!this.enabled) {
				return false;
			}
			if (this.state != null) {
				if (DataScenes.StartedScenes.containsValue(this.state)) {
					return !this.state.paused;
				}
				this.state = null;
			}
			this.state = DataScenes.StartedScenes.get(this.name.toLowerCase());
			if (this.state == null) {
				this.state = DataScenes.StartedScenes.get(this.btn + "btn");
			}
			return this.state != null && !this.state.paused;
		}

		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setBoolean("Enabled", this.enabled);
			compound.setString("Name", this.name);
			compound.setString("Lines", this.lines);
			compound.setInteger("Button", this.btn);
			compound.setInteger("Ticks", this.ticks);
			return compound;
		}
	}

	public static class SceneEvent implements Comparable<SceneEvent> {
		public static SceneEvent parse(String str) {
			SceneEvent event = new SceneEvent();
			int i = str.indexOf(" ");
			if (i <= 0) {
				return null;
			}
			try {
				event.ticks = Integer.parseInt(str.substring(0, i));
				str = str.substring(i + 1);
			} catch (NumberFormatException ex) {
				return null;
			}
			i = str.indexOf(" ");
			if (i <= 0) {
				return null;
			}
			String name = str.substring(0, i);
			for (SceneType type : SceneType.values()) {
				if (name.equalsIgnoreCase(type.name())) {
					event.type = type;
				}
			}
			if (event.type == null) {
				return null;
			}
			event.param = str.substring(i + 1);
			return event;
		}

		public String param;
		public int ticks;

		public SceneType type;

		public SceneEvent() {
			this.ticks = 0;
			this.param = "";
		}

		@Override
		public int compareTo(SceneEvent o) {
			return this.ticks - o.ticks;
		}

		@Override
		public String toString() {
			return this.ticks + " " + this.type.name() + " " + this.param;
		}
	}

	public static class SceneState {
		public boolean paused;
		public int ticks;

		public SceneState() {
			this.paused = false;
			this.ticks = -1;
		}
	}

	public enum SceneType {
		ANIMATE, ATTACK, COMMAND, EQUIP, FACTION, FOLLOW, MOVE, ROTATE, SAY, STATS, THROW;
	}

	public static List<SceneContainer> ScenesToRun = new ArrayList<SceneContainer>();
	public static Map<String, SceneState> StartedScenes = new HashMap<String, SceneState>();

	public static void Pause(ICommandSender sender, String id) {
		if (id == null) {
			for (SceneState state : DataScenes.StartedScenes.values()) {
				state.paused = true;
			}
			NoppesUtilServer.NotifyOPs("Paused all scenes", new Object[0]);
		} else {
			SceneState state2 = DataScenes.StartedScenes.get(id.toLowerCase());
			state2.paused = true;
			NoppesUtilServer.NotifyOPs("Paused scene %s at %s", id, state2.ticks);
		}
	}

	public static void Reset(ICommandSender sender, String id) {
		if (id == null) {
			if (DataScenes.StartedScenes.isEmpty()) {
				return;
			}
			DataScenes.StartedScenes = new HashMap<String, SceneState>();
			NoppesUtilServer.NotifyOPs("Reset all scene", new Object[0]);
		} else if (DataScenes.StartedScenes.remove(id.toLowerCase()) == null) {
			sender.sendMessage(new TextComponentTranslation("Unknown scene %s ", new Object[] { id }));
		} else {
			NoppesUtilServer.NotifyOPs("Reset scene %s", id);
		}
	}

	public static void Start(ICommandSender sender, String id) {
		SceneState state = DataScenes.StartedScenes.get(id.toLowerCase());
		if (state == null) {
			NoppesUtilServer.NotifyOPs("Started scene %s", id);
			DataScenes.StartedScenes.put(id.toLowerCase(), new SceneState());
		} else if (state.paused) {
			state.paused = false;
			NoppesUtilServer.NotifyOPs("Started scene %s from %s", id, state.ticks);
		}
	}

	public static void Toggle(ICommandSender sender, String id) {
		SceneState state = DataScenes.StartedScenes.get(id.toLowerCase());
		if (state == null || state.paused) {
			Start(sender, id);
		} else {
			state.paused = true;
			NoppesUtilServer.NotifyOPs("Paused scene %s at %s", id, state.ticks);
		}
	}

	private EntityNPCInterface npc;

	private EntityLivingBase owner;

	private String ownerScene;

	public List<SceneContainer> scenes;

	public DataScenes(EntityNPCInterface npc) {
		this.scenes = new ArrayList<SceneContainer>();
		this.owner = null;
		this.ownerScene = null;
		this.npc = npc;
	}

	public void addScene(String name) {
		if (name.isEmpty()) {
			return;
		}
		SceneContainer scene = new SceneContainer();
		scene.name = name;
		this.scenes.add(scene);
	}

	public EntityLivingBase getOwner() {
		return this.owner;
	}

	public void readFromNBT(NBTTagCompound compound) {
		NBTTagList list = compound.getTagList("Scenes", 10);
		List<SceneContainer> scenes = new ArrayList<SceneContainer>();
		for (int i = 0; i < list.tagCount(); ++i) {
			SceneContainer scene = new SceneContainer();
			scene.readFromNBT(list.getCompoundTagAt(i));
			scenes.add(scene);
		}
		this.scenes = scenes;
	}

	public void update() {
		for (SceneContainer scene : this.scenes) {
			if (scene.validState()) {
				DataScenes.ScenesToRun.add(scene);
			}
		}
		if (this.owner != null && !DataScenes.StartedScenes.containsKey(this.ownerScene.toLowerCase())) {
			this.owner = null;
			this.ownerScene = null;
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (SceneContainer scene : this.scenes) {
			list.appendTag(scene.writeToNBT(new NBTTagCompound()));
		}
		compound.setTag("Scenes", list);
		return compound;
	}
}
