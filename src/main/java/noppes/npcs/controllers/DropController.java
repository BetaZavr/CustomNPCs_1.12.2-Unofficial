package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;

public class DropController {
	
	private static DropController instance;
	private String filePath;
	private Random rnd;
	public final Map<String, Map<Integer, Map<Integer, DropSet>>> groups;

	public DropController() {
		this.filePath = "";
		this.groups = Maps.<String, Map<Integer, Map<Integer, DropSet>>>newTreeMap();
		this.rnd = new Random();
		DropController.instance = this;
		this.load();
	}
	
	public static DropController getInstance() {
		if (newInstance()) { DropController.instance = new DropController(); }
		return DropController.instance;
	}

	private static boolean newInstance() {
		if (DropController.instance == null) { return true; }
		return CustomNpcs.Dir != null && !DropController.instance.filePath.equals(CustomNpcs.Dir.getAbsolutePath());
	}
	
	public void load() {
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.startDebug("Common", null, "loadDrops"); }
		LogWriter.info("Loading Drops");
		this.loadFile();
		if (CustomNpcs.VerboseDebug) { CustomNpcs.debugData.endDebug("Common", null, "loadDrops"); }
	}
	
	private void loadFile() {
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		try {
			File file = new File(CustomNpcs.Dir, "drops.dat");
			if (file.exists()) {
				try {
					NBTTagCompound nbtFile = CompressedStreamTools
							.readCompressed((InputStream) new FileInputStream(file));
					this.loadNBTData(nbtFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				this.groups.clear();
				this.loadDefaultDrops();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				File file2 = new File(CustomNpcs.Dir, "recipes.dat_old");
				if (file2.exists()) {
					try {
						NBTTagCompound nbtFile = CompressedStreamTools
								.readCompressed((InputStream) new FileInputStream(file2));
						this.loadNBTData(nbtFile);
					} catch (IOException err) {
						err.printStackTrace();
					}
				}
			} catch (Exception ee) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadNBTData(NBTTagCompound nbtFile) {
		this.groups.clear();
		if (nbtFile.hasKey("Groups", 9)) {
			for (int i = 0; i < nbtFile.getTagList("Groups", 10).tagCount(); i++) {
				NBTTagCompound nbtG = nbtFile.getTagList("Groups", 10).getCompoundTagAt(i);
				if (!nbtG.hasKey("GroupID", 3)) { continue; }
				String group = nbtG.getString("GroupID");
				if (!this.groups.containsKey(group)) {
					this.groups.put(group, Maps.<Integer, Map<Integer, DropSet>>newTreeMap());
				}
				Set<String> keys = nbtG.getKeySet();
				for (String groupId : keys) {
					if (groupId.indexOf("Drops_")!=0) { continue; }
					int id = -1;
					try { id = Integer.parseInt(groupId.replace("Drops_", "")); }
					catch (NumberFormatException ex) { }
					if (id<0) { continue; }
					for (int j = 0; j < nbtG.getTagList(groupId, 10).tagCount(); j++) {
						NBTTagCompound nbtDS = nbtG.getTagList(groupId, 10).getCompoundTagAt(j);
						DropSet ds = new DropSet(null);
						ds.load(nbtDS);
						if (!this.groups.get(group).containsKey(id)) { this.groups.get(group).put(id, Maps.<Integer, DropSet>newTreeMap()); }
						this.groups.get(group).get(id).put(ds.pos, ds);
					}
				}
				
			}
		}
		if (this.groups.isEmpty()) {this.loadDefaultDrops(); }
	}
	
	private void loadDefaultDrops() {
		this.save();
	}
	
	public void save() {
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "recipes.dat")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public NBTTagCompound getNBT() {
		NBTTagCompound nbtFile = new NBTTagCompound();
		NBTTagList groups = new NBTTagList();
		for (String group : this.groups.keySet()) {
			NBTTagCompound nbtG = new NBTTagCompound();
			nbtG.setString("GroupID", group);
			for (int id : this.groups.get(group).keySet()) {
				NBTTagList list = new NBTTagList();
				for (DropSet ds : this.groups.get(group).get(id).values()) {
					list.appendTag(ds.getNBT());
				}
				nbtG.setTag("Drops_"+id, list);
			}
			groups.appendTag(nbtG);
		}
		nbtFile.setTag("Groups", groups);
		return nbtFile;
	}

	public List<IItemStack> createDrops(String saveDropsName, double ch, boolean isLooted, EntityLivingBase attacking) {
		List<IItemStack> list = Lists.<IItemStack>newArrayList();
		if (saveDropsName==null || saveDropsName.isEmpty() || !this.groups.containsKey(saveDropsName)) { return list;}
		Map<Integer, Map<Integer, DropSet>> drop = this.groups.get(saveDropsName);
		for (int groupId : drop.keySet()) {
			float r = this.rnd.nextFloat();
			List<IItemStack> prelist = Lists.<IItemStack>newArrayList();
			for (DropSet ds : drop.get(groupId).values()) {
				double c = ds.chance * ch / 100.0d;
				if (ds.item == null || ds.item.isEmpty() || isLooted == ds.lootMode || (c<1.0d && c > r)) { continue; }
				boolean needAdd = true;
				if (ds.getQuestID() > 0) {
					if (attacking instanceof EntityPlayer) {
						IPlayer<?> player = (IPlayer<?>) NpcAPI.Instance().getIEntity(attacking);
						for (IQuest q : player.getActiveQuests()) {
							if (q.getId() == ds.getQuestID()) {
								for (IQuestObjective objQ : q.getObjectives(player)) {
									if (!objQ.isCompleted()) {
										needAdd = false;
										break;
									}
								}
								break;
							}
						}
					}
				}
				if (needAdd && !(ds.amount[0]==0 && ds.amount[1]==0)) { prelist.add(ds.createLoot(ch)); }
			}
			if (prelist.isEmpty()) { continue; }
			int p = this.rnd.nextInt(list.size());
			if (p==list.size()) { p = list.size() - 1; }
			if (p<0) { continue; }
			list.add(prelist.get(p));
		}
		return list;
	}
	
}
