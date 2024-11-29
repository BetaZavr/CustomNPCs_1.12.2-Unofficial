package noppes.npcs.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.entity.data.AttributeSet;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.entity.data.EnchantSet;

public class DropController {

	private static DropController instance;
	public static DropController getInstance() {
		if (newInstance()) {
			DropController.instance = new DropController();
		}
		return DropController.instance;
	}
	private static boolean newInstance() {
		if (DropController.instance == null) {
			return true;
		}
		return CustomNpcs.Dir != null && !DropController.instance.filePath.equals(CustomNpcs.Dir.getAbsolutePath());
	}

	private String filePath;

	public final Map<String, DropsTemplate> templates = new TreeMap<>();

	public DropController() {
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		DropController.instance = this;
		this.load();
	}

	public List<IItemStack> createDrops(String saveDropsName, double ch, boolean isLooted, EntityLivingBase attacking) {
		List<IItemStack> list = new ArrayList<>();
		if (saveDropsName == null || saveDropsName.isEmpty() || !this.templates.containsKey(saveDropsName)) {
			return list;
		}
		DropsTemplate template = this.templates.get(saveDropsName);
		if (template == null) {
			return list;
		}
		return template.createDrops(ch, isLooted, attacking);
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtFile = new NBTTagCompound();
		NBTTagList templates = new NBTTagList();
		for (String template : this.templates.keySet()) {
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", template);
			nbtTemplate.setTag("Groups", this.templates.get(template).getNBT());
			templates.appendTag(nbtTemplate);
		}
		nbtFile.setTag("Templates", templates);
		return nbtFile;
	}

	public void load() {
		CustomNpcs.debugData.startDebug("Common", null, "loadDrops");
		LogWriter.info("Loading Drops");
		this.loadFile();
		CustomNpcs.debugData.endDebug("Common", null, "loadDrops");
	}

	private void loadDefaultDrops() {
		NpcAPI api = NpcAPI.Instance();
		if (api == null) { return; }
		DropsTemplate temp = new DropsTemplate();
		temp.groups.put(0, new TreeMap<>());
		DropSet ds0 = new DropSet(null);
		ds0.amount[0] = 5;
		ds0.amount[1] = 8;
		ds0.chance = 72.5d;
		ds0.item = api.getIItemStack(new ItemStack(Items.COAL));
		ds0.pos = 0;
		temp.groups.get(0).put(0, ds0);
		DropSet ds1 = new DropSet(null);
		ds1.amount[0] = 2;
		ds1.amount[1] = 5;
		ds1.chance = 8.0d;
		ds1.item = api.getIItemStack(new ItemStack(Items.IRON_INGOT));
		ds1.pos = 1;
		temp.groups.get(0).put(1, ds1);
		DropSet ds2 = new DropSet(null);
		ds2.amount[0] = 1;
		ds2.amount[1] = 3;
		ds2.chance = 4.3333d;
		ds2.item = api.getIItemStack(new ItemStack(Items.GOLD_INGOT));
		ds2.pos = 2;
		temp.groups.get(0).put(2, ds2);
		DropSet ds3 = new DropSet(null);
		ds3.amount[0] = 1;
		ds3.amount[1] = 2;
		ds3.chance = 0.575d;
		ds3.item = api.getIItemStack(new ItemStack(Items.DIAMOND));
		ds3.pos = 3;
		temp.groups.get(0).put(3, ds3);

		temp.groups.put(1, new TreeMap<>());
		DropSet df0 = new DropSet(null);
		df0.amount[0] = 1;
		df0.amount[1] = 1;
		df0.chance = 2.5d;
		df0.item = api.getIItemStack(new ItemStack(Items.IRON_AXE));
		df0.pos = 0;
		EnchantSet ench0 = (EnchantSet) df0.addEnchant(Enchantment.getEnchantmentByLocation("unbreaking"));
		ench0.setChance(50.0d);
		ench0.setLevels(1, 5);
		AttributeSet attr = (AttributeSet) df0.addAttribute(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
		attr.setChance(25.0d);
		attr.setValues(1.0d, 3.0d);
		attr.setSlot(0);
		df0.addDropNbtSet(8, 12.5d, "display.Name", new String[] { "Sword", "Axe" });
		temp.groups.get(1).put(0, df0);
		this.templates.put("default", temp);
		this.save();
	}

	private void loadFile() {
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		try {
			File file = new File(CustomNpcs.Dir, "drops.dat");
			if (file.exists()) {
				try {
					NBTTagCompound nbtFile = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
					this.loadNBTData(nbtFile);
				} catch (IOException e) { LogWriter.error("Error:", e); }
			} else {
				this.templates.clear();
				this.loadDefaultDrops();
			}
		} catch (Exception e) {
			LogWriter.error("Error:", e);
			try {
				File file2 = new File(CustomNpcs.Dir, "recipes.dat_old");
				if (file2.exists()) {
					try {
						NBTTagCompound nbtFile = CompressedStreamTools.readCompressed(Files.newInputStream(file2.toPath()));
						this.loadNBTData(nbtFile);
					} catch (IOException err) {
						LogWriter.error("Error:", err);
					}
				}
			} catch (Exception ee) {
				LogWriter.error("Error:", ee);
			}
		}
	}

	public void loadNBTData(NBTTagCompound nbtFile) {
		this.templates.clear();
		if (nbtFile.hasKey("Templates", 9)) {
			for (int i = 0; i < nbtFile.getTagList("Templates", 10).tagCount(); i++) {
				NBTTagCompound nbtTemplate = nbtFile.getTagList("Templates", 10).getCompoundTagAt(i);
				if (!nbtTemplate.hasKey("Name", 8)) {
					continue;
				}
				this.templates.put(nbtTemplate.getString("Name"),
						new DropsTemplate(nbtTemplate.getCompoundTag("Groups")));
			}
		}
		if (this.templates.isEmpty()) {
			this.loadDefaultDrops();
		}
	}

	public void save() {
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(new File(CustomNpcs.Dir, "drops.dat").toPath()));
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void sendTo(EntityPlayerMP player) {
		if (templates.isEmpty()) { loadDefaultDrops(); }
		Map<String, DropsTemplate> tempMap = new TreeMap<>(templates);
		Server.sendData(player, EnumPacketClient.DROP_GROUP_DATA, new NBTTagCompound());
		for (String template : tempMap.keySet()) {
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", template);
			nbtTemplate.setTag("Groups", tempMap.get(template).getNBT());
			Server.sendData(player, EnumPacketClient.DROP_GROUP_DATA, nbtTemplate);
		}
		Server.sendData(player, EnumPacketClient.GUI_UPDATE);
	}
	
	public void sendToServer(String dropTemplate) {
		if (templates.containsKey(dropTemplate)) {
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", dropTemplate);
			nbtTemplate.setTag("Groups", this.templates.get(dropTemplate).getNBT());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
			return;
		}
		Client.sendDirectData(EnumPacketServer.DropTemplateSave, 0);
        Map<String, DropsTemplate> tempMap = new TreeMap<>(templates);
		for (String template : tempMap.keySet()) {
			NBTTagCompound nbtTemplate = new NBTTagCompound();
			nbtTemplate.setString("Name", template);
			nbtTemplate.setTag("Groups", tempMap.get(template).getNBT());
			Client.sendDirectData(EnumPacketServer.DropTemplateSave, 1, nbtTemplate);
		}
	}

}
