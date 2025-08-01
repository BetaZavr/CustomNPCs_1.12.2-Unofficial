package noppes.npcs.entity.data;

import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

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
			this.events = new ArrayList<>();
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
					BlockPos pos;
					if (param[0].startsWith("@")) {
						EntityLivingBase entitylivingbase = CommandBase.getEntity(Objects.requireNonNull(npc.getServer()), npc, param[0], EntityLivingBase.class);
                        pos = entitylivingbase.getPosition();
                        param = Arrays.copyOfRange(param, 2, param.length);
					} else {
						if (param.length < 4) {
							return;
						}
						pos = CommandBase.parseBlockPos(npc, param, 1, false);
						param = Arrays.copyOfRange(param, 4, param.length);
					}
					npc.ais.setStartPos(pos);
					npc.getNavigator().clearPath();
					if (move) {
						Path pathEntity = npc.getNavigator().getPathToPos(pos);
						npc.getNavigator().setPath(pathEntity, 1.0);
					} else {
						if (npc.isInRange(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 2.0)) {
							continue;
						}
						npc.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					}
				}
			} else if (event.type == SceneType.SAY) {
				npc.saySurrounding(new Line(event.param));
			} else if (event.type == SceneType.ROTATE) {
				npc.lookAi.resetTask();
				if (event.param.startsWith("@")) {
					EntityLivingBase entity = CommandBase.getEntity(Objects.requireNonNull(npc.getServer()), npc, event.param, EntityLivingBase.class);
					npc.lookAi.rotate(npc.world.getClosestPlayerToEntity(entity, 30.0));
				} else {
					npc.lookAi.rotate(Integer.parseInt(event.param));
				}
			} else if (event.type == SceneType.EQUIP) {
				String[] args = event.param.split(" ");
				if (args.length < 2) {
					return;
				}
				IItemStack itemstack = null;
				if (!args[1].equalsIgnoreCase("none")) {
					Item item = CommandBase.getItemByText(npc, args[1]);
					int i = (args.length >= 3) ? CommandBase.parseInt(args[2], 1, 64) : 1;
					int j = (args.length >= 4) ? CommandBase.parseInt(args[3]) : 0;
					itemstack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(item, i, j));
				}
				if (args[0].equalsIgnoreCase("main")) {
					npc.inventory.weapons.put(0, itemstack);
				} else if (args[0].equalsIgnoreCase("off")) {
					npc.inventory.weapons.put(2, itemstack);
				} else if (args[0].equalsIgnoreCase("proj")) {
					npc.inventory.weapons.put(1, itemstack);
				} else if (args[0].equalsIgnoreCase("head")) {
					npc.inventory.armor.put(0, itemstack);
				} else if (args[0].equalsIgnoreCase("body")) {
					npc.inventory.armor.put(1, itemstack);
				} else if (args[0].equalsIgnoreCase("legs")) {
					npc.inventory.armor.put(2, itemstack);
				} else if (args[0].equalsIgnoreCase("boots")) {
					npc.inventory.armor.put(3, itemstack);
				}
			} else if (event.type == SceneType.ATTACK) {
				if (event.param.equals("none")) {
					npc.setAttackTarget(null);
				} else {
					EntityLivingBase entity = CommandBase.getEntity(Objects.requireNonNull(npc.getServer()), npc, event.param, EntityLivingBase.class);
					npc.setAttackTarget(entity);
                }
			} else if (event.type == SceneType.THROW) {
				String[] args = event.param.split(" ");
				EntityLivingBase entity2 = CommandBase.getEntity(Objects.requireNonNull(npc.getServer()), npc, args[0], EntityLivingBase.class);
                float damage = Float.parseFloat(args[1]);
				if (damage <= 0.0f) {
					damage = 0.01f;
				}
				ItemStack stack = ItemStackWrapper.MCItem(npc.inventory.getProjectile());
				if (args.length > 2) {
					Item item2 = CommandBase.getItemByText(npc, args[2]);
					stack = new ItemStack(item2, 1, 0);
				}
				EntityProjectile projectile = npc.shoot(entity2, 100, stack, false);
				projectile.damage = damage;
			} else if (event.type == SceneType.ANIMATE) {
				npc.animateAi.tempAnimation = 0;
				if (event.param.equalsIgnoreCase("sleep")) {
					npc.animateAi.tempAnimation = 2;
				} else if (event.param.equalsIgnoreCase("sneak")) {
					npc.ais.animationType = 4;
				} else if (event.param.equalsIgnoreCase("normal")) {
					npc.ais.animationType = 0;
				} else if (event.param.equalsIgnoreCase("sit")) {
					npc.animateAi.tempAnimation = 1;
				} else if (event.param.equalsIgnoreCase("crawl")) {
					npc.ais.animationType = 7;
				} else if (event.param.equalsIgnoreCase("bow")) {
					npc.animateAi.tempAnimation = 11;
				} else if (event.param.equalsIgnoreCase("yes")) {
					npc.animateAi.tempAnimation = 13;
				} else if (event.param.equalsIgnoreCase("no")) {
					npc.animateAi.tempAnimation = 12;
				}
			} else if (event.type == SceneType.COMMAND) {
				NoppesUtilServer.runCommand(npc, npc.getName(), event.param, null);
			} else if (event.type == SceneType.STATS) {
				int k = event.param.indexOf(" ");
				if (k <= 0) {
					return;
				}
				String type = event.param.substring(0, k).toLowerCase();
				String value = event.param.substring(k).trim();
				try {
					if (type.equals("walking_speed")) {
						npc.ais.setWalkingSpeed(ValueUtil.correctInt(Integer.parseInt(value), 0, 10));
					} else if (type.equals("size")) {
						npc.display.setSize(ValueUtil.correctInt(Integer.parseInt(value), 1, 30));
					} else {
						ITextComponent message = new TextComponentString("Unknown scene stat: " + type);
						message.getStyle().setColor(TextFormatting.GRAY);
						NoppesUtilServer.NotifyOPs(message, false);
					}
				} catch (NumberFormatException e) {
					ITextComponent message = new TextComponentString("Unknown scene stat " + type + " value: " + value);
					message.getStyle().setColor(TextFormatting.GRAY);
					NoppesUtilServer.NotifyOPs(message, false);
				}
			} else if (event.type == SceneType.FACTION) {
				npc.setFaction(Integer.parseInt(event.param));
			} else if (event.type == SceneType.FOLLOW) {
				if (event.param.equalsIgnoreCase("none")) {
					DataScenes.this.owner = null;
					DataScenes.this.ownerScene = null;
				} else {
                    DataScenes.this.owner = CommandBase.getEntity(Objects.requireNonNull(npc.getServer()), npc, event.param, EntityLivingBase.class);
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
			ArrayList<SceneEvent> events = new ArrayList<>();
			for (String line : this.lines.split("\r\n|\r|\n")) {
				SceneEvent event = SceneEvent.parse(line);
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
				if (event.ticks > this.state.ticks) {
					break;
				}
				if (event.ticks != this.state.ticks) {
					continue;
				}
				try {
					this.handle(event);
				} catch (Exception e) { LogWriter.error(e); }
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
		public int compareTo(@Nonnull SceneEvent event) {
			return this.ticks - event.ticks;
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
		ANIMATE, ATTACK, COMMAND, EQUIP, FACTION, FOLLOW, MOVE, ROTATE, SAY, STATS, THROW
	}

	public static List<SceneContainer> ScenesToRun = new ArrayList<>();
	public static Map<String, SceneState> StartedScenes = new HashMap<>();

	public static void Pause(String id) {
		if (id == null) {
			for (SceneState state : DataScenes.StartedScenes.values()) {
				state.paused = true;
			}
			ITextComponent message = new TextComponentString("Paused all scenes");
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		} else {
			SceneState state2 = DataScenes.StartedScenes.get(id.toLowerCase());
			state2.paused = true;

			ITextComponent message = new TextComponentString("Paused scene " + id + " at " + state2.ticks);
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		}
	}

	public static void Reset(ICommandSender sender, String id) {
		if (id == null) {
			if (DataScenes.StartedScenes.isEmpty()) {
				return;
			}
			DataScenes.StartedScenes = new HashMap<>();
			ITextComponent message = new TextComponentString("Reset all scene");
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		} else if (DataScenes.StartedScenes.remove(id.toLowerCase()) == null) {
			sender.sendMessage(new TextComponentTranslation("Unknown scene %s ", id));
		} else {
			ITextComponent message = new TextComponentString("Reset scene " + id);
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		}
	}

	public static void Start(String id) {
		SceneState state = DataScenes.StartedScenes.get(id.toLowerCase());
		if (state == null) {
			ITextComponent message = new TextComponentString("Started scene " + id);
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
			DataScenes.StartedScenes.put(id.toLowerCase(), new SceneState());
		} else if (state.paused) {
			state.paused = false;
			ITextComponent message = new TextComponentString("Started scene " + id + " from " + state.ticks);
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		}
	}

	public static void Toggle(String id) {
		SceneState state = DataScenes.StartedScenes.get(id.toLowerCase());
		if (state == null || state.paused) {
			Start(id);
		} else {
			state.paused = true;
			ITextComponent message = new TextComponentString("Paused scene " + id + " from " + state.ticks);
			message.getStyle().setColor(TextFormatting.GRAY);
			NoppesUtilServer.NotifyOPs(message, false);
		}
	}

	private final EntityNPCInterface npc;

	private EntityLivingBase owner;

	private String ownerScene;

	public List<SceneContainer> scenes;

	public DataScenes(EntityNPCInterface npc) {
		this.scenes = new ArrayList<>();
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
		List<SceneContainer> scenes = new ArrayList<>();
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
