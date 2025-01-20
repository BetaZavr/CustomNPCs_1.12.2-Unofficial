package noppes.npcs.controllers.data;

import java.util.*;

import com.google.common.base.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.CustomNPCsScheduler;

public class Marcet implements IMarcet, Predicate<EntityNPCInterface> {

	public final Map<Integer, MarkupData> markup = new TreeMap<>();
	public final Map<ItemStack, Integer> inventory = new HashMap<>();
	public final Map<Integer, MarcetSection> sections = new TreeMap<>(); // [TabID, Section]
	private int id;
	public boolean isLimited, showXP;
	public long lastTime;
	public List<EntityPlayer> listeners = new ArrayList<>();
	public String name;
	public long nextTime;
	public int updateTime;
	public Lines lines;
	public int limitedType;
	public long money;
	public double coefficient = 5.0d;

	public Marcet(int id) {
		this.id = id;
		this.name = "Market";
		this.updateTime = 0;
		this.markup.put(0, new MarkupData(0, 0.0f, 0.0f, 1000));
		this.markup.put(1, new MarkupData(1, 0.0f, 0.0f, 2200));
		this.markup.put(2, new MarkupData(2, -0.0f, 0.0f, 5000));
		this.sections.put(0, new MarcetSection(0));
		this.lines = new Lines();
		this.isLimited = false;
		this.showXP = false;
		this.limitedType = 0;
		this.money = 0;
		this.updateNew();
	}

	public void addInventoryItems(Map<ItemStack, Integer> items) {
		for (ItemStack stack : items.keySet()) {
			if (NoppesUtilServer.IsItemStackNull(stack)) {
				continue;
			}
			boolean added = false;
			List<ItemStack> del = new ArrayList<>();
			for (ItemStack st : this.inventory.keySet()) {
				if (NoppesUtilServer.IsItemStackNull(st)) {
					del.add(st);
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, st, false, false)) {
					this.inventory.put(st, this.inventory.get(st) + items.get(stack));
					added = true;
					break;
				}
			}
			for (ItemStack st : del) {
				this.inventory.remove(st);
			}
			if (!added) {
				this.inventory.put(stack, items.get(stack));
			}
		}
	}

	public void addListener(EntityPlayer listener, boolean isServer) {
		for (EntityPlayer pl : this.listeners) {
			if (listener == pl || pl.equals(listener)) {
				return;
			}
		}
		this.listeners.add(listener);
		if (isServer && listener instanceof EntityPlayerMP) {
			this.sendTo((EntityPlayerMP) listener);
			this.detectAndSendChanges();
		}
	}

	@Override
	public boolean apply(EntityNPCInterface npc) {
		if (npc == null || !(npc.advanced.roleInterface instanceof RoleTrader)) { return false; }
		return ((RoleTrader) npc.advanced.roleInterface).getMarket() == null ?
				((RoleTrader) npc.advanced.roleInterface).getMarketID() == getId() :
				((RoleTrader) npc.advanced.roleInterface).getMarket().getId() == getId();
	}

	public void closeForAllPlayers() { // server only
		if (this.listeners == null) {
			return;
		}
		for (EntityPlayer player : this.listeners) {
			if (!(player instanceof EntityPlayerMP)) {
				return;
			}
			CustomNPCsScheduler.runTack(player::closeScreen, 250);
		}
	}

	public Marcet copy(int newID) {
		Marcet marcet = new Marcet(newID > -1 ? newID : this.id);
		marcet.readFromNBT(writeToNBT());
		marcet.updateNew();
		return marcet;
	}

	public void detectAndSendChanges() {
		for (EntityPlayer listener : this.listeners) {
			if (listener instanceof EntityPlayerMP) {
				this.sendTo((EntityPlayerMP) listener);
			}
		}
	}

	@Override
	public IDeal[] getAllDeals() {
		List<IDeal> list = new ArrayList<>();
		for (MarcetSection ms : sections.values()) {
            list.addAll(ms.deals);
		}
		return list.toArray(new IDeal[0]);
	}

	public Deal getDeal(int dealID) {
		for (MarcetSection ms : sections.values()) {
			for (Deal deal : ms.deals) {
				if (deal.getId() == dealID) {
					return deal;
				}
			}
		}
		return null;
	}

	@Override
	public IDeal[] getDeals(int section) {
		if (!sections.containsKey(section)) {
			return new IDeal[0];
		}
		return sections.get(section).deals.toArray(new IDeal[0]);
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public int getSection(int dealID) {
		for (MarcetSection ms : sections.values()) {
			for (Deal deal : ms.deals) {
				if (deal.getId() == dealID) {
					return ms.getId();
				}
			}
		}
		return -1;
	}

	public String getSettingName() {
		return "ID:" + id + " " + ((char) 167) + (isEmpty() ? "4" : "a") + getName();
	}

	public String getShowName() {
		return new TextComponentTranslation(this.name).getFormattedText();
	}

	public boolean notHasListener(EntityPlayer player) {
		return !listeners.contains(player);
	}

	public boolean isEmpty() {
		return getAllDeals().length == 0;
	}

	@Override
	public boolean isLimited() {
		return this.isLimited;
	}

	public boolean isValid() {
		if (sections.isEmpty()) {
			return false;
		}
		boolean hasDeals = false;
		for (MarcetSection ms : sections.values()) {
			for (Deal deal : ms.deals) {
				if (deal.isValid()) {
					hasDeals = true;
					continue;
				}
				if (deal.getProduct().getMCItemStack() == null
						|| deal.getProduct().getMCItemStack().getItem() == Items.AIR) {
					return false;
				}
				if (deal.getMoney() == 0 && deal.getCurrency().isEmpty()) {
					return false;
				}
			}
		}
		return hasDeals;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("MarcetID");
		this.name = compound.getString("Name");
		this.isLimited = compound.getBoolean("IsLimited");
		this.showXP = compound.getBoolean("ShowXP");
		this.money = compound.getLong("Money");

		this.markup.clear();
		for (int i = 0; i < compound.getTagList("Markup", 10).tagCount(); i++) {
			MarkupData md = new MarkupData(compound.getTagList("Markup", 10).getCompoundTagAt(i));
			this.markup.put(md.level, md);
		}
		if (this.markup.isEmpty()) {
			this.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000));
			this.markup.put(1, new MarkupData(1, 0.0f, 0.45f, 2200));
			this.markup.put(2, new MarkupData(2, -0.05f, 0.0f, 5000));
		}

		this.inventory.clear();
		for (int i = 0; i < compound.getTagList("Inventory", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getTagList("Inventory", 10).getCompoundTagAt(i);
			this.inventory.put(new ItemStack(nbt), nbt.getInteger("TotalCount"));
		}

		this.sections.clear();
		Map<Integer, MarcetSection> newSec = new TreeMap<>();
		if (!compound.hasKey("Sections", 9) || compound.getTagList("Sections", 10).tagCount() == 0) {
			newSec.put(0, new MarcetSection(0));
		} else {
			for (int i = 0; i < compound.getTagList("Sections", 10).tagCount(); i++) {
				NBTTagCompound nbt = compound.getTagList("Sections", 10).getCompoundTagAt(i);
				MarcetSection ms = MarcetSection.create(nbt);
				newSec.put(ms.getId(), MarcetSection.create(nbt));
			}
			// Sorting
			Map<Integer, MarcetSection> sec = new TreeMap<>();
			int i = 0;
			for (MarcetSection ms : newSec.values()) {
				sec.put(i, ms);
				i++;
			}
			newSec = sec;
		}
		this.sections.putAll(newSec);

		this.limitedType = compound.getInteger("LimitedType");
		this.updateTime = compound.getInteger("UpdateTime");
		this.lastTime = compound.getLong("LastTime");
		this.nextTime = compound.getLong("NextTime");
		if (compound.hasKey("NpcLines", 10)) {
			this.lines.readNBT(compound.getCompoundTag("NpcLines"));
		}
	}

	public void removeInventoryItems(Map<ItemStack, Integer> items) {
		for (ItemStack stack : items.keySet()) {
			if (NoppesUtilServer.IsItemStackNull(stack)) {
				continue;
			}
			List<ItemStack> del = new ArrayList<>();
			for (ItemStack st : this.inventory.keySet()) {
				if (NoppesUtilServer.IsItemStackNull(st)) {
					del.add(st);
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, st, false, false)) {
					this.inventory.put(st, this.inventory.get(st) - items.get(stack));
					if (this.inventory.get(st) <= 0) {
						del.add(st);
					}
					break;
				}
			}
			for (ItemStack st : del) {
				this.inventory.remove(st);
			}
		}
	}

	public void removeListener(EntityPlayer player, boolean isServer) {
		for (EntityPlayer listener : this.listeners) {
			if (listener == player || listener.equals(player)) {
				if (isServer && listener instanceof EntityPlayerMP) {
					Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_CLOSE, this.id);
				}
				this.listeners.remove(listener);
				this.detectAndSendChanges();
				return;
			}
		}
	}

	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.MARCET_DATA, 1, writeToNBT());
		Server.sendData(player, EnumPacketClient.MARCET_DATA, 2);
	}

	@Override
	public void setIsLimited(boolean limited) {
		if (this.isLimited == limited) {
			return;
		}
		this.isLimited = limited;
		if (limited) {
			this.updateNew();
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void update() { // any 1.0 sec -> (MarcetController.update) ServerTickHandler / ServerTickEvent
		if (this.updateTime < 5L) {
			return;
		}
		if (this.lastTime <= System.currentTimeMillis() - 7200000L || this.lastTime + this.updateTime * 60000L < System.currentTimeMillis()) {
			this.updateNew();
		}
	}

	@Override
	public void updateNew() {
		this.inventory.clear();
		this.lastTime = System.currentTimeMillis();
		if (this.lines != null && !this.lines.isEmpty() && CustomNpcs.Server != null) {
			for (WorldServer world : CustomNpcs.Server.worlds) {
				List<EntityNPCInterface> npcs = world.getEntities(EntityNPCInterface.class, this);
				for (EntityNPCInterface npc : npcs) {
					if (!npc.isEntityAlive()) { continue; }
					npc.saySurrounding(this.lines.getLine(true));
				}
			}
		}
		this.money = (long) (Math.random() * 7500.0d);
		for (MarcetSection ms : sections.values()) {
			for (Deal deal : ms.deals) {
				deal.updateNew();
				this.money += (long) ((double) (deal.getMoney()) * (this.coefficient + Math.random() * this.coefficient));
				for (IItemStack iStack : deal.getCurrency().getItems()) {
					ItemStack stack = iStack.getMCItemStack();
					if (NoppesUtilServer.IsItemStackNull(stack)) {
						continue;
					}
					int count = (int) (((double) stack.getCount())
							* (this.coefficient + Math.random() * this.coefficient));
					boolean added = false;
					for (ItemStack st : this.inventory.keySet()) {
						if (NoppesUtilServer.IsItemStackNull(st)) {
							continue;
						}
						if (NoppesUtilPlayer.compareItems(stack, st, false, false)) {
							this.inventory.put(st, this.inventory.get(st) + count);
							added = true;
							break;
						}
					}
					if (!added) {
						this.inventory.put(stack, count);
					}
				}
			}
		}
		this.detectAndSendChanges();
	}

	@SideOnly(Side.CLIENT)
	public void updateTime() { // any 0.5 sec -> (MarcetController.updateTime) ClientTickHandler /
								// ClientTickEvent
		if (this.nextTime < 0L) {
			this.nextTime = 0L;
		} else if (this.nextTime > 0L) {
			this.nextTime -= 500L;
			if (this.nextTime < 0) {
				this.nextTime = 0;
			}
		}
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("MarcetID", this.id);
		compound.setString("Name", this.name);
		compound.setBoolean("IsLimited", this.isLimited);
		compound.setBoolean("ShowXP", this.showXP);
		compound.setLong("Money", this.money);

		NBTTagList markup = new NBTTagList();
		for (int level : this.markup.keySet()) {
			MarkupData mp = this.markup.get(level);
			mp.level = level;
			markup.appendTag(mp.getNBT());
		}
		compound.setTag("Markup", markup);

		NBTTagList items = new NBTTagList();
		for (ItemStack stack : this.inventory.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			stack.writeToNBT(nbt);
			nbt.setInteger("TotalCount", this.inventory.get(stack));
			items.appendTag(nbt);
		}
		compound.setTag("Inventory", items);

		NBTTagList secs = new NBTTagList();
		for (MarcetSection ms : this.sections.values()) {
			secs.appendTag(ms.save());
		}
		compound.setTag("Sections", secs);

		compound.setInteger("LimitedType", this.limitedType);
		compound.setInteger("UpdateTime", this.updateTime);
		compound.setLong("LastTime", this.lastTime);
		compound.setLong("NextTime", this.lastTime + this.updateTime * 60000L - System.currentTimeMillis());
		compound.setTag("NpcLines", this.lines.writeToNBT());
		return compound;
	}

}
