package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IMarcetHandler;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;

public class MarcetController
implements IMarcetHandler {
	
	private static MarcetController instance;
	private String filePath;
	public final Map<Integer, Marcet> marcets;
	public final Map<Integer, Deal> deals;

	public MarcetController() {
		this.filePath = "";
		MarcetController.instance = this;
		this.marcets = Maps.<Integer, Marcet>newTreeMap();
		this.deals = Maps.<Integer, Deal>newTreeMap();
		this.load();
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
	
	@Override
	public IMarcet addMarcet() {
		Marcet marcet = new Marcet(this.getUnusedMarketId());
		this.marcets.put(marcet.getId(), marcet);
		this.addDeal(marcet.getId());
		return this.marcets.get(marcet.getId());
	}

	public boolean containsMarcetName(String name) {
		for (Marcet m : this.marcets.values()) {
			if (m.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IMarcet getMarcet(String name) {
		for (Marcet m : this.marcets.values()) {
			if (m.name.equals(name)) {
				return m;
			}
		}
		return null;
	}

	@Override
	public IMarcet getMarcet(int marcetId) {
		if (marcetId < 0 || !this.marcets.containsKey(marcetId)) {
			return null;
		}
		return this.marcets.get(marcetId);
	}

	@Override
	public int[] getMarketIDs() {
		int[] arr = new int[this.marcets.size()];
		int i = 0;
		for (Marcet m : this.marcets.values()) {
			arr[i] = m.getId();
			i++;
		}
		return arr;
	}

	@Override
	public int[] getDealIDs() {
		int[] arr = new int[this.deals.size()];
		int i = 0;
		for (Deal m : this.deals.values()) {
			arr[i] = m.getId();
			i++;
		}
		return arr;
	}
	
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		NBTTagList mList = new NBTTagList();
		for (Marcet marcet : this.marcets.values()) {
			if (marcet == null) { continue; }
			NBTTagCompound nbtMarcet = marcet.writeToNBT();
			mList.appendTag(nbtMarcet);
		}
		compound.setTag("Marcets", mList);
		
		NBTTagList dList = new NBTTagList();
		for (Deal deal : this.deals.values()) {
			if (deal == null) { continue; }
			NBTTagCompound nbtDeal = deal.writeToNBT();
			dList.appendTag(nbtDeal);
		}
		compound.setTag("Deals", dList);
		return compound;
	}

	public int getUnusedMarketId() {
		int id = 0;
		while (this.marcets.containsKey(id)) { id++; }
		return id;
	}

	public int getUnusedDealId() {
		int id = 0;
		while (this.deals.containsKey(id)) { id++; }
		return id;
	}

	private void load() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null || saveDir.getAbsolutePath().indexOf(CustomNpcs.MODID)==-1) { return; }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadMarcets");
		}
		this.filePath = saveDir.getAbsolutePath();
		try {
			File file = new File(saveDir, "marcet.dat");
			if (file.exists()) { this.load(file); }
		} catch (Exception e) {
			try {
				File file2 = new File(saveDir, "marcet.dat_old");
				if (file2.exists()) { this.load(file2); }
			} catch (Exception ex) { }
		}

		if (this.marcets.isEmpty() || !this.marcets.containsKey(0)) { this.loadDefaultMarcets(); }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadMarcets");
		}
	}

	private void loadDefaultMarcets() {
		Marcet marcet;
		if (this.marcets.containsKey(0)) {
			marcet = this.marcets.get(0);
			for (int id : marcet.getDealIDs()) { this.removeDeal(id); }
		}
		else { marcet = new Marcet(0); }
		marcet.name = "Default Marcet";
		marcet.updateTime = 5;
		marcet.lastTime = System.currentTimeMillis();
		this.marcets.put(marcet.getId(), marcet);
		Deal d0 = (Deal) this.addDeal(0);
		d0.set(new ItemStack(Items.DIAMOND), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 10, 0), new ItemStack(Items.IRON_INGOT, 45, 0) });
		d0.setType(2);
		d0.setCount(2, 7);
		d0.setMoney(1000);
		d0.setChance(0.1575f);
		Deal d1 = (Deal) this.addDeal(0);
		d1.set(new ItemStack(Items.IRON_INGOT, 4, 0), new ItemStack[] { new ItemStack(Items.GOLD_INGOT) });
		d1.setType(2);
		d1.setMoney(40);
		d1.setChance(0.955f);
		this.saveMarcets();
	}

	private void load(File file) throws IOException {
		this.load(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void load(NBTTagCompound nbtFile) throws IOException {
		this.marcets.clear();
		this.deals.clear();
		if (nbtFile.hasKey("Marcets", 9)) {
			for (int i = 0; i < nbtFile.getTagList("Marcets", 10).tagCount(); ++i) {
				Marcet marcet = this.loadMarcet(nbtFile.getTagList("Marcets", 10).getCompoundTagAt(i));
				if (marcet!=null) { this.marcets.put(marcet.getId(), marcet); }
			}
		}
		if (nbtFile.hasKey("Deals", 9)) {
			for (int i = 0; i < nbtFile.getTagList("Deals", 10).tagCount(); ++i) {
				Deal deal = this.loadDeal(nbtFile.getTagList("Deals", 10).getCompoundTagAt(i));
				if (deal!=null) { this.deals.put(deal.getId(), deal); }
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
		Marcet marcet = new Marcet(id);
		marcet.readFromNBT(nbtMarcet);
		this.marcets.put(marcet.getId(), marcet);
		if (nbtMarcet.hasKey("Deals", 9)) { // pre old load deals
			for (int i = 0; i < nbtMarcet.getTagList("Deals", 10).tagCount(); i++) {
				Deal deal = (Deal) this.addDeal(id);
				NBTTagCompound nbt = nbtMarcet.getTagList("Deals", 10).getCompoundTagAt(i);
				nbt.setInteger("MarcetID", id);
				deal.readFromNBT(nbt);
			}
		}
		return marcet;
	}

	public Deal loadDeal(NBTTagCompound nbtDeal) {
		if (nbtDeal==null || !nbtDeal.hasKey("DealID", 3) || nbtDeal.getInteger("DealID")<0) { return null; }
		int id = nbtDeal.getInteger("DealID");
		int mID = nbtDeal.getInteger("MarcetID");
		if (this.deals.containsKey(id)) {
			this.deals.get(id).readFromNBT(nbtDeal);
			return this.deals.get(id);
		}
		Deal deal = new Deal(id, mID);
		deal.readFromNBT(nbtDeal);
		this.deals.put(deal.getId(), deal);
		return deal;
	}
	
	public int loadOld(NBTTagCompound nbttagcompound) {
		String marketName = nbttagcompound.getString("TraderMarket");
		if (!marketName.isEmpty()) {
			for (Marcet m : this.marcets.values()) {
				if (m.name.equalsIgnoreCase(marketName)) {
					return m.getId();
				}
			}
		}
		IMarcet marcet = this.addMarcet();
		if (!marketName.isEmpty()) { marcet.setName(marketName); }
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
			Deal deal = (Deal) this.addDeal(marcet.getId());
			deal.set(inventorySold.getStackInSlot(i), new ItemStack[] { st0, st1 });
			deal.setIgnoreDamage(ignoreDamage);
			deal.setIgnoreNBT(ignoreNBT);
		}
		return marcet.getId();
	}

	@Override
	public boolean removeMarcet(int marcetID) {
		if (marcetID < 0 || (marcetID!=0 && this.marcets.size() <= 1)) { return false; }
		if (!this.marcets.containsKey(marcetID)) {
			if (marcetID == 0) { this.loadDefaultMarcets(); }
			return false;
		}
		Marcet marcet = this.marcets.get(marcetID);
		marcet.closeForAllPlayers();
		List<Integer> delDeal = Lists.<Integer>newArrayList();
		for (Integer dealId : this.deals.keySet()) {
			if (this.deals.get(dealId).getMarcetID()==marcetID) { delDeal.add(dealId); }
		}
		for (Integer dealId : delDeal) { this.deals.remove(dealId); }
		this.marcets.remove(marcetID);
		if (marcetID == 0) { this.loadDefaultMarcets(); }
		this.saveMarcets();
		return true;
	}
	
	@Override
	public boolean removeDeal(int dealID) {
		boolean isRemove = false;
		if (this.deals.containsKey(dealID)) {
			this.deals.remove(dealID);
			isRemove = true;
		}
		this.saveMarcets();
		return isRemove;
	}

	public void saveMarcets() {
		try {
			// Save
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			if (saveDir == null || saveDir.getAbsolutePath().indexOf(CustomNpcs.MODID)==-1) { return; }
			File file = new File(saveDir, "marcet.dat_new");
			File file2 = new File(saveDir, "marcet.dat_old");
			File file3 = new File(saveDir, "marcet.dat");
			NBTTagCompound nbtFile = this.getNBT();
			CompressedStreamTools.writeCompressed(nbtFile, new FileOutputStream(file));
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
		for (Deal d : this.deals.values()) {
			d.update();
		}
	}

	public void updateTime() {
		for (Marcet m : this.marcets.values()) {
			m.updateTime();
		}
	}
	
	public void sendTo(EntityPlayerMP player, int marcetID) {
		LogWriter.debug("CustomNpcs: Send marked data to \""+player.getName()+"\"");
		if (this.marcets.containsKey(marcetID)) { // market
			this.marcets.get(marcetID).sendTo(player);
			Server.sendDataDelayed(player, EnumPacketClient.MARCET_DATA, 250, 2);
		} else if (marcetID<0) { // all
			if (this.marcets.isEmpty() || !this.marcets.containsKey(0)) { this.loadDefaultMarcets(); }
			Map<Integer, Marcet> mapM = Maps.<Integer, Marcet>newHashMap(this.marcets);
			Map<Integer, Deal> mapD = Maps.<Integer, Deal>newHashMap(this.deals);
			Server.sendData(player, EnumPacketClient.MARCET_DATA, 0);
			for (int id : mapM.keySet()) {
				Marcet market = mapM.get(id);
				if (id < 0 || market.getId() < 0) { continue; }
				if (market.getId() != id) {
					market = market.copy(id);
					mapM.put(id, market);
				}
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 1, market.writeToNBT());
				for (int dealId : market.getDealIDs()) {
					Deal deal = mapD.get(dealId);
					if (deal == null) { continue; }
					Server.sendData(player, EnumPacketClient.MARCET_DATA, 3, deal.writeToNBT());
				}
				Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
			}
			Server.sendDataDelayed(player, EnumPacketClient.MARCET_DATA, 250, 2);
		}
		else { // not
			Server.sendData(player, EnumPacketClient.MARCET_DATA, 4, marcetID);
		}
	}

	public DealMarkup getBuyData(int marcetID, int dealID, int marcetSlot) {
		return this.getBuyData(this.marcets.get(marcetID), this.deals.get(dealID), marcetSlot);
	}
	
	public DealMarkup getBuyData(Marcet marcet, Deal deal, int marcetLevel) {
		DealMarkup dm = new DealMarkup();
		if (deal!=null) { dm.set(deal); }
		if (marcet!=null) {
			dm.set(marcet.markup.containsKey(marcetLevel) ? marcet.markup.get(marcetLevel) : marcetLevel >= marcet.markup.size() ? marcet.markup.get(marcet.markup.size()-1) : marcet.markup.get(0));
		}
		return dm;
	}

	public IDeal addDeal(int marcetID) {
		if (!this.marcets.containsKey(marcetID)) { return null; }
		Deal deal = new Deal(this.getUnusedDealId(), marcetID);
		this.deals.put(deal.getId(), deal);
		this.marcets.get(marcetID).detectAndSendChanges();
		return deal;
	}

	public IDeal getDeal(int dealId) { return this.deals.get(dealId); }
	
}
