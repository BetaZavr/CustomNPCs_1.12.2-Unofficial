package noppes.npcs.controllers.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.role.IRoleTrader;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumPacketClient;

public class Marcet implements IRoleTrader {

	public final Map<Integer, Deal> data;
	public int id;
	public long lastTime;
	public List<EntityPlayer> listeners = Lists.<EntityPlayer>newArrayList();
	public String name;
	public long nextTime;
	public int updateTime;

	public Marcet() {
		this.id = -1;
		this.name = "Market";
		this.updateTime = 0;
		this.data = Maps.<Integer, Deal>newTreeMap();
		this.data.put(0, new Deal());
	}

	public Deal addDeal() {
		Deal deal = new Deal();
		while (this.data.containsKey(deal.id)) {
			deal.id++;
		}
		this.data.put(deal.id, deal);
		return this.data.get(deal.id);
	}

	public void addListener(EntityPlayer listener, boolean isServer) {
		for (EntityPlayer pl : this.listeners) {
			if (listener == pl || pl.equals(listener)) {
				return;
			}
		}
		this.listeners.add(listener);
		if (isServer && listener instanceof EntityPlayerMP) {
			NBTTagCompound compound = new NBTTagCompound();
			this.writeEntityToNBT(compound);
			Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_UPDATE, compound);
			this.detectAndSendChanges();
		}
	}

	public void detectAndSendChanges() {
		NBTTagCompound compound = new NBTTagCompound();
		this.writeEntityToNBT(compound);
		for (EntityPlayer listener : this.listeners) {
			if (listener instanceof EntityPlayerMP) {
				Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_UPDATE, compound);
			}
		}
	}

	@Override
	public IItemStack getCurrency(int position, int slot) {
		if (slot < 0 || slot >= 9 || this.data.containsKey(position)) {
			return NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
		}
		return NpcAPI.Instance().getIItemStack(this.data.get(position).inventoryCurrency.getStackInSlot(slot));
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IItemStack getProduct(int position) {
		if (this.data.containsKey(position)) {
			return NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
		}
		return NpcAPI.Instance().getIItemStack(this.data.get(position).inventorySold.getStackInSlot(0));
	}

	public String getSettingName() {
		String str = new String(Character.toChars(0x00A7));
		return "ID:" + this.id + " " + str + (this.isEmpty() ? "4" : this.hasEmpty() ? "c" : "a")
				+ new TextComponentTranslation(this.name).getFormattedText();
	}

	public String getShowName() {
		return new TextComponentTranslation(this.name).getFormattedText();
	}

	@Override
	public int getType() {
		return 1;
	}

	public boolean hasCurrency(ItemStack stack) {
		for (Deal d : this.data.values()) {
			for (ItemStack item : d.inventoryCurrency.items) {
				if (!item.isEmpty() && NoppesUtilPlayer.compareItems(item, stack, d.ignoreDamage, d.ignoreNBT)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasEmpty() {
		boolean notProducts = false;
		if (this.data.isEmpty()) {
			return true;
		}
		for (Deal d : this.data.values()) {
			if (d.inventorySold.getStackInSlot(0).isEmpty() || (d.inventoryCurrency.isEmpty() && d.money <= 0)) {
				notProducts = true;
				break;
			}
		}
		return notProducts;
	}

	public boolean hasListener(EntityPlayer player) {
		for (EntityPlayer listener : this.listeners) {
			if (listener.equals(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
		boolean notProducts = true;
		if (this.data.isEmpty()) {
			return notProducts;
		}
		for (Deal d : this.data.values()) {
			if (!d.inventorySold.getStackInSlot(0).isEmpty()) {
				notProducts = false;
				break;
			}
		}
		return notProducts;
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("MarcetID");
		this.name = compound.getString("Name");
		this.data.clear();
		for (int i = 0; i < compound.getTagList("Deals", 10).tagCount(); i++) {
			Deal d = new Deal();
			d.read(compound.getTagList("Deals", 10).getCompoundTagAt(i));
			d.id = i;
			this.data.put(i, d);
		}
		this.updateTime = compound.getInteger("UpdateTime");
		this.lastTime = compound.getLong("LastTime");
		this.nextTime = compound.getLong("NextTime");
	}

	@Override
	public void remove(int position) {
		this.data.remove(position);
		this.detectAndSendChanges();
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

	@Override
	public void set(int position, IItemStack product, IItemStack[] currencys) {
		ItemStack[] c = new ItemStack[currencys.length];
		for (int i = 0; i < currencys.length; i++) {
			c[i] = currencys[i].getMCItemStack();
		}
		this.set(position, product.getMCItemStack(), c);
		this.detectAndSendChanges();
	}

	public Deal set(int position, ItemStack product, ItemStack[] currencys) {
		Deal d;
		if (this.data.containsKey(position)) {
			d = this.data.get(position);
		} else {
			d = new Deal();
		}
		d.set(product, currencys);
		this.data.put(position, d);
		this.detectAndSendChanges();
		return this.data.get(position);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void update() { // any 0.5 sec -> ServerTickHandler / WorldTickEvent
		if (this.updateTime < 5L) {
			return;
		}
		if (this.lastTime <= 0L || this.lastTime + this.updateTime * 60000L < System.currentTimeMillis()) {
			this.lastTime = System.currentTimeMillis();
			for (Deal d : this.data.values()) {
				d.updateNew();
			}
			this.detectAndSendChanges();
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateTime() { // any 0.5 sec -> ClientTickHandler / ClientTickEvent
		if (this.nextTime < 0L) {
			this.nextTime = 0L;
		} else if (this.nextTime > 0L) {
			this.nextTime -= 500L;
		}
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("MarcetID", this.id);
		compound.setString("Name", this.name);
		NBTTagList deals = new NBTTagList();
		for (Deal d : this.data.values()) {
			deals.appendTag(d.getNBT());
		}
		compound.setTag("Deals", deals);
		compound.setInteger("UpdateTime", this.updateTime);
		compound.setLong("LastTime", this.lastTime);
		compound.setLong("NextTime", this.lastTime + this.updateTime * 60000L - System.currentTimeMillis());
	}

}
