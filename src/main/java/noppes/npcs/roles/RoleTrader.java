package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleTrader
extends RoleInterface {
	
	public int marcet;

	public RoleTrader(EntityNPCInterface npc) {
		super(npc);
		this.marcet = -1;
		this.type = EnumNpcRole.TRADER;
	}

	@Deprecated
	public IItemStack getCurrency1(int slot) {
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			return m.getCurrency(slot, 0);
		}
		return NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
	}

	@Deprecated
	public IItemStack getCurrency2(int slot) {
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			return m.getCurrency(slot / 2, 1);
		}
		return NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
	}

	@Deprecated
	public String getMarket() {
		Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
		return m == null ? "" : m.name;
	}

	@Deprecated
	public IItemStack getSold(int slot) {
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			return m.getProduct(slot);
		}
		return NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
	}

	public boolean hasCurrency(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			return m.hasCurrency(stack);
		}
		return false;
	}

	@Override
	public void interact(EntityPlayer player) {
		this.npc.say(player, this.npc.advanced.getInteractLine());
		Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
		if (m == null || m.hasEmpty()) {
			return;
		}
		if (player instanceof EntityPlayerMP) {
			m.addListener(player, true);
		}
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTrader, this.npc);
	}

	@Deprecated
	public void remove(int slot) {
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			m.remove(slot);
		} else {
			if (slot >= 18 || slot < 0) {
				throw new CustomNPCsException("Invalid slot: " + slot, new Object[0]);
			}
		}
	}

	@Deprecated
	public void set(int slot, IItemStack currency, IItemStack currency2, IItemStack sold) {
		if (sold == null) {
			throw new CustomNPCsException("Sold item was null", new Object[0]);
		}
		if (slot >= 18 || slot < 0) {
			throw new CustomNPCsException("Invalid slot: " + slot, new Object[0]);
		}
		if (currency == null) {
			currency = currency2;
			currency2 = null;
		}
		if (MarcetController.getInstance().marcets.containsKey(this.marcet)) {
			Marcet m = MarcetController.getInstance().marcets.get(this.marcet);
			int size = (currency != null ? 1 : 0) + (currency2 != null ? 1 : 0);
			IItemStack[] c = new IItemStack[size];
			if (size > 0) {
				int i = 0;
				if (currency != null) {
					c[i] = currency;
					i++;
				}
				if (currency2 != null) {
					c[i] = currency2;
				}
			}
			m.set(slot, sold, c);
		}
	}

	@Deprecated
	public void setMarket(String name) {
		Marcet m = MarcetController.getInstance().get(name);
		if (m != null) {
			this.marcet = m.id;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("MarketID", 3)) { this.marcet = compound.getInteger("MarketID"); }
		else { this.marcet = MarcetController.getInstance().loadOld(compound); } // Old
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("MarketID", this.marcet);
		return compound;
	}

}
