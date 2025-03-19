package noppes.npcs.client.gui.drop;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DropNbtSet;

public class SubGuiDropValueNbt
extends SubGuiInterface
implements ITextfieldListener, ITextChangeListener {

	public DropNbtSet tag;
	private final String[] tagIds;
	private final String[] tagListIds;
	private GuiTextArea textarea;

	public SubGuiDropValueNbt(DropNbtSet tg) {
		setBackground("companion_empty.png");
		xSize = 172;
		ySize = 167;
		closeOnEsc = true;

		tag = tg;
		String[] ids = new String[11];
		for (int i = 0; i < 10; i++) {
			ids[i] = "tag.type." + i;
		}
		ids[10] = "tag.type.11";
		tagIds = ids;

		String[] lids = new String[5];
		lids[0] = "tag.type.3";
		lids[1] = "tag.type.5";
		lids[2] = "tag.type.6";
		lids[3] = "tag.type.8";
		lids[4] = "tag.type.11";
		tagListIds = lids;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 90: { // type
				tag.setType(Integer.parseInt(button.getVariants()[button.getValue()].replace("tag.type.", "")));
				initGui();
				break;
			}
			case 91: { // done
				close();
				break;
			}
			case 92: { // list type
				tag.setTypeList(Integer.parseInt(button.getVariants()[button.getValue()].replace("tag.type.", "")));
				initGui();
				break;
			}
		}
	}

	private boolean check() {
		if (getTextField(93) == null || textarea == null) {
			return false;
		}
		if (getTextField(93).getText().isEmpty() || textarea.getText().isEmpty()) {
			return false;
		}
		String vs = textarea.getText();
		if (vs.contains("|")) {
			for (String str : vs.split("\\|")) {
				String ch = tag.checkValue(str, tag.getType());
				if (ch == null) {
					return false;
				}
			}
		} else {
			String ch = tag.checkValue(vs, tag.getType());
			if (ch != null) {
				return true;
			}
		}
		return true;
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 100;
		// type value
		int t = tag.getType();
		int tl = tag.getTypeList();
		// name / type
		String name = tag.getPath();
		if (name.contains(".")) {
			while (name.contains(".")) { name = name.substring(name.indexOf(".") + 1); }
		}
		String type = new TextComponentTranslation("tag.type." + t).getFormattedText();
		GuiNpcLabel label = new GuiNpcLabel(anyIDs++, new TextComponentTranslation("drop.tag.type", name, type).getFormattedText(), guiLeft + 4, guiTop + 5);
		label.setHoverText("drop.hover.tag.name");
		addLabel(label);
		// path
		GuiNpcTextField textField = new GuiNpcTextField(93, this, guiLeft + 4, guiTop + 18, 163, 20, tag.getPath());
		textField.setHoverText("drop.hover.tag.path");
		addTextField(textField);
		// value
		addLabel(new GuiNpcLabel(anyIDs++, "type.value", guiLeft + 4, guiTop + 40));
		String[] textArr = tag.getValues();
		StringBuilder text = new StringBuilder();
		if (textArr.length > 0) { text = new StringBuilder(textArr[0]); }
		if (textArr.length > 1) {
			for (int i = 1; i < textArr.length; i++) { text.append("|").append(textArr[i]); }
		}
		(textarea = new GuiTextArea(94, guiLeft + 4, guiTop + 53, 163, 65, text.toString())).setListener(this);
		textarea.active = true;
		if (t == 7 || t == 11) { textarea.setHoverText("drop.hover.tag.value.array", name); }
		else if (t == 9) { textarea.setHoverText("drop.hover.tag.value.list", name); }
		else { textarea.setHoverText("drop.hover.tag.value.normal", name); }
		add(textarea);
		// chance
		addLabel(new GuiNpcLabel(anyIDs, "drop.chance", guiLeft + 56, guiTop + 125));
		textField = new GuiNpcTextField(95, this, guiLeft + 4, guiTop + 120, 50, 20, String.valueOf(tag.getChance()));
		textField.setMinMaxDoubleDefault(0.0001d, 100.0d, tag.getChance());
		textField.setHoverText("drop.hover.tag.chance");
		addTextField(textField);
		// type
		int posId = 0;
		for (int i = 0; i < tagIds.length; i++) {
			if (tagIds[i].equals("tag.type." + tag.getType())) { posId = i; }
		}
		name = ((char) 167) + "2" + name;
		GuiNpcButton button = new GuiButtonBiDirectional(90, guiLeft + 87, guiTop + 120, 80, 20, tagIds, posId);
		button.setHoverText("drop.hover.tag.type", name, getValuesData(t));
		addButton(button);
		// list type
		int posListId = 0;
		for (int i = 0; i < tagListIds.length; i++) {
			if (tagListIds[i].equals("tag.type." + tag.getTypeList())) { posListId = i; }
		}
		button = new GuiButtonBiDirectional(92, guiLeft + 87, guiTop + 142, 80, 20, tagListIds, posListId);
		button.setHoverText("drop.hover.tag.listtype", name, getValuesData(tl));
		button.setVisible(t == 9);
		addButton(button);
		// done
		button = new GuiNpcButton(91, guiLeft + 4, guiTop + 142, 80, 20, "gui.done", check());
		button.setHoverText("hover.back");
		addButton(button);
	}

	private String getValuesData(int t) {
		String gn = ((char) 167) + "2";
		String r = ((char) 167) + "c";
		String gr = ((char) 167) + "7";
		if (t == 1) { return gn + Byte.MIN_VALUE + gr + "<->" + r + Byte.MAX_VALUE; }
		else if (t == 2) { return gn + Short.MIN_VALUE + gr + "<->" + r + Short.MAX_VALUE; }
		else if (t == 3) { return gn + Integer.MIN_VALUE + gr + "<->" + r + Integer.MAX_VALUE; }
		else if (t == 4) { return gn + Long.MIN_VALUE + gr + "<->" + r + Long.MAX_VALUE; }
		else if (t == 5) { return new TextComponentTranslation("type.double", "77").getFormattedText(); }
		else if (t == 6) { return new TextComponentTranslation("type.double", "308").getFormattedText(); }
		else if (t == 7) { return "array [v0, v1, ... vn] v_ = " + gn + Byte.MIN_VALUE + gr + "<->" + r + Byte.MAX_VALUE; }
		else if (t == 8) { return new TextComponentTranslation("type.string").getFormattedText(); }
		else if (t == 9) { return new TextComponentTranslation("type.list").getFormattedText(); }
		else if (t == 11) { return "array [v0, v1, ... vn] v_ = " + gn + Integer.MIN_VALUE + gr + "<->" + r + Integer.MAX_VALUE; }
		return gn + "true" + gr + ", " + gn + "false";
	}

	@Override
	public void textUpdate(String text) {
		tag.setValues(text);
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getID() == 93) { tag.setPath(textfield.getText()); } // path
		else if (textfield.getID() == 95) { tag.setChance(textfield.getDouble()); } // chance
		initGui();
	}
}
