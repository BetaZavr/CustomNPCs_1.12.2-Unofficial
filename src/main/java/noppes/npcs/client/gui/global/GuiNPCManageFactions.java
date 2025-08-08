package noppes.npcs.client.gui.global;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcFactionPoints;
import noppes.npcs.client.gui.SubGuiNpcFactionSelect;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class GuiNPCManageFactions
extends GuiNPCInterface2
implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, ISubGuiListener, GuiYesNoCallback {

	public static boolean isName = true;
	private HashMap<String, Integer> base = new HashMap<>();
	private final Map<String, Integer> data = new LinkedHashMap<>();
	private Faction faction;
	private GuiCustomScroll scrollFactions;

	public GuiNPCManageFactions(EntityNPCInterface npc) {
		super(npc);
		faction = new Faction();
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				save();
				String name = new TextComponentTranslation("gui.new").getFormattedText();
				while (data.containsKey(name)) {
					name += "_";
				}
				Faction faction = new Faction(-1, name, 65280, 1000);
				NBTTagCompound compound = new NBTTagCompound();
				faction.save(compound);
				Client.sendData(EnumPacketServer.FactionSave, compound);
				break;
			}
			case 1: {
				if (!data.containsKey(scrollFactions.getSelected())) {
					return;
				}
				Client.sendData(EnumPacketServer.FactionRemove, data.get(scrollFactions.getSelected()));
				scrollFactions.clear();
				faction = new Faction();
				initGui();
				break;
			}
			case 2: {
				setSubGui(new SubGuiNpcFactionPoints(faction));
				break;
			}
			case 3: {
				faction.hideFaction = (button.getValue() == 1);
				break;
			}
			case 4: {
				faction.getsAttacked = (button.getValue() == 1);
				break;
			}
			case 5: {
				setSubGui(new SubGuiNpcFactionOptions(faction.factions));
				break;
			}
			case 6: {
				if (scrollFactions.getSelected() == null) {
					return;
				}
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : base.keySet()) {
					int id = base.get(name);
					if (faction.id == id || faction.frendFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				setSubGui(new SubGuiNpcFactionSelect(6, scrollFactions.getSelected(), faction.attackFactions, corData));
				break;
			}
			case 7: {
				if (scrollFactions.getSelected() == null) {
					return;
				}
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : base.keySet()) {
					int id = base.get(name);
					if (faction.id == id || faction.attackFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				setSubGui(new SubGuiNpcFactionSelect(7, scrollFactions.getSelected(), faction.frendFactions, corData));
				break;
			}
			case 8: { // description
				setSubGui(new SubGuiNpcTextArea(0, faction.description));
				break;
			}
			case 9: { // flag
				setSubGui(new GuiTextureSelection(null, faction.flag.toString(), "png", 4));
				break;
			}
			case 10: {
				setSubGui(new SubGuiColorSelector(faction.color));
				break;
			}
			case 11: { // get
				if (faction == null || faction.id < 0) {
					return;
				}
				ItemStack stack = new ItemStack(Items.BANNER);
				NBTTagCompound nbt = stack.getTagCompound();
				if (nbt == null) {
					stack.setTagCompound(nbt = new NBTTagCompound());
				}
				nbt.setTag("BlockEntityTag", new NBTTagCompound());
				nbt.getCompoundTag("BlockEntityTag").setInteger("FactionID", faction.id);
				Client.sendData(EnumPacketServer.NbtBookCopyStack, stack.writeToNBT(new NBTTagCompound()));
				break;
			}
			case 14: {
				GuiNPCManageFactions.isName = ((GuiNpcCheckBox) button).isSelected();
				if (!base.isEmpty()) { setData(new Vector<>(base.keySet()), new HashMap<>(base)); }
				button.setHoverText("hover.sort", new TextComponentTranslation("global.factions").getFormattedText(), ((GuiNpcCheckBox) button).getText());
				break;
			}
			case 24: { // reset ID
				Client.sendData(EnumPacketServer.FactionMinID, faction.id);
				break;
			}
			default: {
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) {
			return;
		}
		if (id == 0) {
			Client.sendData(EnumPacketServer.FactionMinID, faction.id);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 380.0f, guiTop + 70.0f, 1.0f);
		drawGradientRect(-1, -1, 21, 40, 0xFF404040, 0xFF404040);
		drawGradientRect(0, 0, 20, 39, 0xFFA0A0A0, 0xFFA0A0A0);
		if (faction != null && faction.id > -1) {
			mc.getTextureManager().bindTexture(faction.flag);
            mc.getTextureManager().getTexture(faction.flag);
            GlStateManager.scale(0.5f, 0.305f, 1.0f);
            drawTexturedModalRect(0, 0, 4, 4, 40, 128);
        }
		GlStateManager.popMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (scrollFactions.getSelected() == null && faction.id > -1 && data.containsValue(faction.id)) {
			for (String name : data.keySet()) {
				if (data.get(name) == faction.id) {
					scrollFactions.setSelected(name);
					break;
				}
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 368;
		int y = guiTop + 8;
		// add
		GuiNpcButton button = new GuiNpcButton(0, x, y, 45, 16, "gui.add");
		button.setHoverText("faction.hover.add");
		addButton(button);
		// del
		button = new GuiNpcButton(1, x, y += 18, 45, 16, "gui.remove");
		button.setHoverText("faction.hover.del");
		addButton(button);
		// get flag
		button = new GuiNpcButton(11, x, y + 87, 45, 16, "gui.get");
		button.setIsVisible(faction != null && faction.id > -1);
		button.setHoverText("faction.hover.flag.get");
		addButton(button);
		// factions list
		if (scrollFactions == null) {
			(scrollFactions = new GuiCustomScroll(this, 0)).setSize(143, 208);
		}
		scrollFactions.guiLeft = guiLeft + 220;
		scrollFactions.guiTop = guiTop + 4;
		addScroll(scrollFactions);
		// sort
		button = new GuiNpcCheckBox(14, x, y + 18, 45, 12, "gui.name" ,"ID", GuiNPCManageFactions.isName);
		button.setHoverText("hover.sort", new TextComponentTranslation("global.factions").getFormattedText(), ((GuiNpcCheckBox) button).getText());
		addButton(button);
		if (faction.id == -1) {
			return;
		}
		// name
		GuiNpcTextField textField = new GuiNpcTextField(0, this, guiLeft + 40, guiTop + 4, 136, 16, Util.instance.deleteColor(faction.name));
		textField.setMaxStringLength(50);
		textField.setHoverText("faction.hover.name");
		addTextField(textField);
		// info
		addLabel(new GuiNpcLabel(0, "gui.name", guiLeft + 8, guiTop + 9));
		addLabel(new GuiNpcLabel(10, "ID", guiLeft + 178, guiTop + 4));
		addLabel(new GuiNpcLabel(11, faction.id + "", guiLeft + 178, guiTop + 14));
		// color
		StringBuilder color = new StringBuilder(Integer.toHexString(faction.color));
		while (color.length() < 6) { color.insert(0, "0"); }
		button = new GuiNpcButton(10, guiLeft + 40, guiTop + 26, 60, 16, color.toString());
		button.setHoverText("faction.hover.color");
		button.setTextColor(faction.color);
		addButton(button);
		addLabel(new GuiNpcLabel(1, "gui.color", guiLeft + 8, guiTop + 31));

		// reset ID
		x = guiLeft + 170;
		y = guiTop + 26;
		int x0 = guiLeft + 8;
		button = new GuiNpcButton(24, x, y, 45, 16, "gui.reset");
		button.setHoverText("hover.reset.id");
		addButton(button);
		// points
		addLabel(new GuiNpcLabel(2, "faction.points", x0, (y += 18) + 5));
		button = new GuiNpcButton(2, x, y, 45, 16, "selectServer.edit");
		button.setHoverText("faction.hover.points");
		addButton(button);
		// hidden
		addLabel(new GuiNpcLabel(3, "faction.hidden", x0, (y += 18) + 5));
		button = new GuiNpcButton(3, x, y, 45, 16, new String[] { "gui.no", "gui.yes" }, (faction.hideFaction ? 1 : 0));
		button.setHoverText("faction.hover.hide");
		addButton(button);
		// attacked
		addLabel(new GuiNpcLabel(4, "faction.attacked", x0, (y += 18) + 5));
		button = new GuiNpcButton(4, x, y, 45, 16, new String[] { "gui.no", "gui.yes" }, (faction.getsAttacked ? 1 : 0));
		button.setHoverText("faction.hover.mobs");
		addButton(button);
		// death points
		addLabel(new GuiNpcLabel(5, "faction.ondeath", x0, (y += 18) + 5));
		button = new GuiNpcButton(5, x, y, 45, 16, "faction.points");
		button.setHoverText("faction.hover.dead.points");
		addButton(button);
		// hostiles
		addLabel(new GuiNpcLabel(6, "faction.hostiles", x0, (y += 18) + 5));
		button = new GuiNpcButton(6, x, y, 45, 16, "selectServer.edit");
		button.setHoverText("faction.hover.hostiles");
		addButton(button);
		// friends
		addLabel(new GuiNpcLabel(7, "faction.friends", x0, (y += 18) + 5));
		button = new GuiNpcButton(7, x, y, 45, 16, "selectServer.edit");
		button.setHoverText("faction.hover.addfrends");
		addButton(button);
		// description
		addLabel(new GuiNpcLabel(8, "faction.description", x0, (y += 18) + 5));
		button = new GuiNpcButton(8, x, y, 45, 16, "selectServer.edit");
		button.setHoverText("faction.hover.description");
		addButton(button);
		// flag
		addLabel(new GuiNpcLabel(9, "faction.flag", x0, (y += 18) + 5));
		button = new GuiNpcButton(9, x, y, 45, 16, "selectServer.edit");
		button.setHoverText("faction.hover.flag.txr");
		addButton(button);
		textField = new GuiNpcTextField(1, this, x0, y + 18, 208, 16, faction.flag.toString());
		textField.setHoverText("faction.hover.flag.txr");
		addTextField(textField);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		if (scrollFactions == null || scrollFactions.getSelected() == null || !data.containsKey(scrollFactions.getSelected()) || faction == null || faction.id == -1) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		faction.save(compound);
		Client.sendData(EnumPacketServer.FactionSave, compound);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			if (!data.containsKey(scrollFactions.getSelected())) {
				return;
			}
			save();
			Client.sendData(EnumPacketServer.FactionGet, data.get(scrollFactions.getSelected()));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
		base = new HashMap<>(dataMap);
		String select = scrollFactions == null || scrollFactions.getSelected() == null ? "" : scrollFactions.getSelected();
		data.clear();
		List<Entry<String, Integer>> newList = new ArrayList<>(dataMap.entrySet());
		newList.sort((f_0, f_1) -> {
            if (GuiNPCManageFactions.isName) {
                return f_0.getKey().compareTo(f_1.getKey());
            } else {
                return f_0.getValue().compareTo(f_1.getValue());
            }
        });
		for (Entry<String, Integer> entry : newList) {
			int id = entry.getValue();
			String name = Util.instance.deleteColor(entry.getKey());
			if (name.contains("ID:" + id + " ")) {
				name = name.substring(name.indexOf(" ") + 3);
			}
			String key = ((char) 167) + "7ID:" + id + " " + ((char) 167) + "r"
					+ new TextComponentTranslation(name).getFormattedText();
			data.put(key, id);
			if (select != null && select.equals(key)) {
				select = key;
			}
		}
		if (scrollFactions != null) {
			scrollFactions.setListNotSorted(new ArrayList<>(data.keySet()));
			if (select != null && !select.isEmpty()) { scrollFactions.setSelected(select); }
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}
		if (compound.hasKey("MinimumID", 3)) {
			if (faction.id != compound.getInteger("MinimumID")) {
				Client.sendData(EnumPacketServer.FactionRemove, faction.id);
				faction.id = compound.getInteger("MinimumID");
				compound = new NBTTagCompound();
				faction.save(compound);
				Client.sendData(EnumPacketServer.FactionSave, compound);
				initGui();
			}
		} else {
			(faction = new Faction()).load(compound);
			setSelected("ID:" + faction.id + " " + faction.name);
			initGui();
		}
	}

	@Override
	public void setSelected(String selected) {
		for (String key : scrollFactions.getList()) {
			if (Util.instance.deleteColor(key).equals(selected) && data.containsKey(key)) {
				scrollFactions.setSelected(key);
				return;
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof GuiTextureSelection) {
			faction.flag = ((GuiTextureSelection) subgui).resource;
			initGui();
		}
		if (subgui instanceof SubGuiNpcTextArea) {
			faction.description = ((SubGuiNpcTextArea) subgui).text;
		} else if (subgui instanceof SubGuiColorSelector) {
			faction.color = ((SubGuiColorSelector) subgui).color;
			initGui();
		} else if (subgui instanceof SubGuiNpcFactionSelect) {
			SubGuiNpcFactionSelect gui = (SubGuiNpcFactionSelect) subgui;
			if (subgui.getId() == 6) {
				faction.attackFactions.clear();
				for (int id : gui.selectFactions) {
					faction.frendFactions.remove(id);
					faction.attackFactions.add(id);
				}
			} else if (gui.id == 7) {
				faction.frendFactions.clear();
				for (int id : gui.selectFactions) {
					faction.attackFactions.remove(id);
					faction.frendFactions.add(id);
				}
			}
			save();
			initGui();
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (faction.id == -1) {
			return;
		}
		if (textField.getID() == 0) {
			String name = textField.getFullText();
			if (!name.isEmpty() && !data.containsKey(name)) {
				String old = scrollFactions.getSelected();
				data.remove(faction.name);
				base.remove(faction.name);
				faction.name = name;
				String str = ((char) 167) + "7ID:" + faction.id + " " + ((char) 167) + "r" + new TextComponentTranslation(name).getFormattedText();
				data.put(str, faction.id);
				base.put(name, faction.id);
				scrollFactions.replace(old, str);
			}
			initGui();
		}
		else if (textField.getID() == 1) {
			faction.setFlag(textField.getFullText());
		}
	}

}
