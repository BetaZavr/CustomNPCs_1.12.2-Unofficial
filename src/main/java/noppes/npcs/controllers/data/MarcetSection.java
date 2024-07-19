package noppes.npcs.controllers.data;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.controllers.MarcetController;

public class MarcetSection {

	public static MarcetSection create(NBTTagCompound compound) {
		MarcetSection ms = new MarcetSection(compound.getInteger("ID"));
		ms.name = compound.getString("Name");

		NBTTagList list = compound.getTagList("Deals", 10);
        for (NBTBase nbt : list) {
            Deal deal = new Deal();
            deal.readDataNBT((NBTTagCompound) nbt);
            ms.deals.add(deal);
        }

        return ms;
	}
	private final int id;
	public String name = "market.default.section";

	public List<Deal> deals = Lists.newArrayList();

	public MarcetSection(int id) {
		this.id = id;
	}

	public void addDeal(int dealId) {
		if (hadDeal(dealId)) {
			return;
		}
		Deal deal = (Deal) MarcetController.getInstance().getDeal(dealId);
		if (deal == null || !deal.isValid()) {
			return;
		}
		Deal marcetDeal = deal.copy();
		marcetDeal.updateNew();
		deals.add(marcetDeal);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return new TextComponentTranslation(name).getFormattedText();
	}

	private boolean hadDeal(int dealId) {
		for (Deal deal : deals) {
			if (deal.getId() == dealId) {
				return true;
			}
		}
		return false;
	}

	public void removeAllDeals() {
		deals.clear();
	}

	public void removeDeal(int dealId) {
		for (Deal deal : deals) {
			if (deal.getId() == dealId) {
				deals.remove(deal);
				return;
			}
		}
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("ID", id);
		compound.setString("Name", name);

		NBTTagList list = new NBTTagList();
		for (Deal deal : deals) {
			list.appendTag(deal.writeDataToNBT());
		}
		compound.setTag("Deals", list);

		return compound;
	}

}
