package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;

public class MarcetController {
	
	private static MarcetController instance;
	private String filePath;
	public final Map<Integer, Marcet> marcets;

	public MarcetController() {
		this.filePath = "";
		MarcetController.instance = this;
		this.marcets = Maps.<Integer, Marcet>newTreeMap();
		this.loadMarcets();
	}

	public static MarcetController getInstance() {
		if (newInstance()) {
			MarcetController.instance = new MarcetController();
		}
		return MarcetController.instance;
	}

	private static boolean newInstance() {
		if (MarcetController.instance == null) {
			return true;
		}
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !MarcetController.instance.filePath.equals(file.getAbsolutePath());
	}

	public Marcet addMarcet() {
		Marcet marcet = new Marcet();
		marcet.id = this.getUnusedId();
		this.marcets.put(marcet.id, marcet);
		return this.marcets.get(marcet.id);
	}

	public boolean contains(String name) {
		for (Marcet m : this.marcets.values()) {
			if (m.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsRecipeName(int id, String name) {
		for (Marcet m : this.marcets.values()) {
			if (m.id != id && m.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public Marcet get(String name) {
		for (Marcet m : this.marcets.values()) {
			if (m.name.equals(name)) {
				return m;
			}
		}
		return null;
	}

	public Marcet getMarcet(int marcetId) {
		if (marcetId < 0 || !this.marcets.containsKey(marcetId)) {
			return null;
		}
		return this.marcets.get(marcetId);
	}

	public int[] getMarkets(EntityNPCInterface npc) {
		int[] arr = new int[this.marcets.size()];
		if (npc.world.isRemote) {
			return new int[0];
		}
		int i = 0;
		for (Marcet m : this.marcets.values()) {
			arr[i] = m.id;
			i++;
		}
		return arr;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Marcet marcet : this.marcets.values()) {
			NBTTagCompound nbtfactions = new NBTTagCompound();
			marcet.writeToNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getUnusedId() {
		int id = 0;
		while (this.marcets.containsKey(id)) { id++; }
		return id;
	}

	private void loadMarcets() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadMarcets");
		}
		this.filePath = saveDir.getAbsolutePath();
		try {
			File file = new File(saveDir, "marcet.dat");
			if (file.exists()) {
				this.loadMarcets(file);
			}
		} catch (Exception e) {
			try {
				File file2 = new File(saveDir, "marcet.dat_old");
				if (file2.exists()) {
					this.loadMarcets(file2);
				}
			} catch (Exception ex) {
			}
		}

		if (this.marcets.isEmpty()) { this.loadDefaultMarcets(); }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadMarcets");
		}
	}

	private void loadDefaultMarcets() {
		Marcet marcet = this.addMarcet();
		marcet.name = "Default Marcet";
		marcet.updateTime = 120;
		marcet.lastTime = System.currentTimeMillis();
		Deal d0 = marcet.set(0, new ItemStack(Items.DIAMOND), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 32, 0), new ItemStack(Items.IRON_INGOT, 48, 0) });
		d0.type = 2;
		d0.count = new int[] { 2, 7 };
		d0.money = 1000;
		d0.chance = 0.1575f;
		Deal d1 = marcet.set(1, new ItemStack(Items.IRON_INGOT, 4, 0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT) });
		d1.type = 2;
		d1.money = 40;
		d1.chance = 0.955f;
		this.marcets.put(marcet.id, marcet);
		this.saveMarcets();
	}

	private void loadMarcets(File file) throws IOException {
		this.loadMarcets(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadMarcets(NBTTagCompound nbtFile) throws IOException {
		this.marcets.clear();
		NBTTagList list = nbtFile.getTagList("Data", 10);
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				Marcet marcet = this.loadMarcet(list.getCompoundTagAt(i));
				if (marcet!=null) { this.marcets.put(marcet.id, marcet); }
			}
		}
	}
	
	public Marcet loadMarcet(NBTTagCompound nbtMarcet) {
		if (nbtMarcet==null || !nbtMarcet.hasKey("MarcetID", 3) || nbtMarcet.getInteger("MarcetID")<0) { return null; }
		int id = nbtMarcet.getInteger("MarcetID");
		if (this.marcets.containsKey(id)) {
			this.marcets.get(id).readFromNBT(nbtMarcet);
			return this.marcets.get(id);
		}
		Marcet marcet = new Marcet();
		marcet.readFromNBT(nbtMarcet);
		this.marcets.put(marcet.id, marcet);
		return this.marcets.get(marcet.id);
	}

	public int loadOld(NBTTagCompound nbttagcompound) {
		String marketName = nbttagcompound.getString("TraderMarket");
		Marcet marcet = null;
		if (!marketName.isEmpty()) {
			for (Marcet m : this.marcets.values()) {
				if (m.name.equalsIgnoreCase(marketName)) {
					marcet = m;
					break;
				}
			}
		}
		if (marcet == null) {
			marcet = new Marcet();
			if (!marketName.isEmpty()) {
				marcet.name = marketName;
			}
			marcet.id = this.getUnusedId();

			boolean ignoreDamage = nbttagcompound.getBoolean("TraderIgnoreDamage");
			boolean ignoreNBT = nbttagcompound.getBoolean("TraderIgnoreNBT");
			NpcMiscInventory inventoryCurrency = new NpcMiscInventory(36);
			NpcMiscInventory inventorySold = new NpcMiscInventory(18);
			inventoryCurrency.setFromNBT(nbttagcompound.getCompoundTag("TraderCurrency"));
			inventorySold.setFromNBT(nbttagcompound.getCompoundTag("TraderSold"));

			for (int i = 0; i < 18; i++) {
				if (inventorySold.getStackInSlot(i).isEmpty()) {
					continue;
				}
				ItemStack st0 = inventoryCurrency.getStackInSlot(i);
				ItemStack st1 = inventoryCurrency.getStackInSlot(i + 18);
				if (st0.isEmpty() && st1.isEmpty()) {
					continue;
				}
				Deal deal = marcet.addDeal();
				deal.set(inventorySold.getStackInSlot(i), new ItemStack[] { st0, st1 });
				deal.ignoreDamage = ignoreDamage;
				deal.ignoreNBT = ignoreNBT;
			}
			this.marcets.put(marcet.id, marcet);
		}
		return marcet.id;
	}

	public void removeMarcet(int marcetID) {
		if (marcetID < 0 || this.marcets.size() <= 1) {
			return;
		}
		this.marcets.remove(marcetID);
		this.saveMarcets();
	}

	public void saveMarcet(Marcet marcet) {
		if (marcet.id < 0) {
			marcet.id = this.getUnusedId();
		}
		while (this.containsRecipeName(marcet.id, marcet.name)) {
			marcet.name += "_";
		}
		this.marcets.put(marcet.id, marcet);
		this.saveMarcets();
	}

	public void saveMarcets() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "marcet.dat_new");
			File file2 = new File(saveDir, "marcet.dat_old");
			File file3 = new File(saveDir, "marcet.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), new FileOutputStream(file));
			if (file2.exists()) { file2.delete(); }
			file3.renameTo(file2);
			if (file3.exists()) { file3.delete(); }
			file.renameTo(file3);
			if (file.exists()) { file.delete(); }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update() {
		for (Marcet m : this.marcets.values()) {
			m.update();
		}
	}

	public void updateTime() {
		for (Marcet m : this.marcets.values()) {
			m.updateTime();
		}
	}
	
	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.MARCET_DATA, 5, false);
		for (int id : this.marcets.keySet()) {
			if (id < 0 || this.marcets.get(id).id < 0) { continue; }
			if (this.marcets.get(id).id != id) { this.marcets.get(id).id = id; }
			NBTTagCompound nbtMarcet = new NBTTagCompound();
			this.marcets.get(id).writeToNBT(nbtMarcet);
			Server.sendDataDelayed(player, EnumPacketClient.MARCET_DATA, 10, true, nbtMarcet);
		}
	}
	
}
