package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.util.ValueUtil;

public class MarcetSection {

	public static MarcetSection create(NBTTagCompound compound) {
		MarcetSection ms = new MarcetSection(compound.getInteger("ID"));
		ms.name = compound.getString("Name");
		ms.setIcon(compound.getInteger("IconId"));
		NBTTagList list = compound.getTagList("Deals", 10);
        for (NBTBase nbt : list) {
            Deal deal = new Deal();
            deal.readData((NBTTagCompound) nbt);
            ms.deals.add(deal);
        }

        return ms;
	}
	protected final int id;
	protected int iconId;
	public String name = "market.default.section";
	public List<Deal> deals = new ArrayList<>();

	public MarcetSection(int idIn) { id = idIn; }

	public void addDeal(int dealId) {
		if (hadDeal(dealId)) { return; }
		Deal deal = MarcetController.getInstance().getDeal(dealId);
		if (deal == null || !deal.isValid()) { return; }
		Deal marcetDeal = deal.copy();
		marcetDeal.updateNew();
		deals.add(marcetDeal);
	}

	public int getId() { return id; }

	public int getIcon() { return iconId; }

	public void setIcon(int id) { iconId = ValueUtil.correctInt(id, 0, 29); }

	public String getName() { return new TextComponentTranslation(name).getFormattedText(); }

	private boolean hadDeal(int dealId) {
		for (Deal deal : deals) {
			if (deal.getId() == dealId) { return true; }
		}
		return false;
	}

	public void removeAllDeals() { deals.clear(); }

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
		compound.setInteger("IconId", iconId);
		compound.setString("Name", name);

		NBTTagList list = new NBTTagList();
		for (Deal deal : deals) {
			list.appendTag(deal.save());
		}
		compound.setTag("Deals", list);

		return compound;
	}

}
