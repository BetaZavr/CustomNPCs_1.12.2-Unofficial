package noppes.npcs.client.gui.drop;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DropNbtSet;

public class SubGuiDropValueNbt
extends SubGuiInterface
implements ITextfieldListener, ITextChangeListener {
	
	public DropNbtSet tag;
	private String[] tagIds;
	private String[] tagListIds;
	private GuiTextArea textarea;

	public SubGuiDropValueNbt(DropNbtSet tg) {
		this.tag = tg;
		this.setBackground("companion_empty.png");
		this.xSize = 172;
		this.ySize = 167;
		this.closeOnEsc = true;

		String[] ids = new String[11];
		for (int i = 0; i < 10; i++) {
			ids[i] = "tag.type." + i;
		}
		ids[10] = "tag.type.11";
		this.tagIds = ids;

		String[] lids = new String[5];
		lids[0] = "tag.type.3";
		lids[1] = "tag.type.5";
		lids[2] = "tag.type.6";
		lids[3] = "tag.type.8";
		lids[4] = "tag.type.11";
		this.tagListIds = lids;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		int id = button.id;
		if (id == 90) { // type
			this.tag.setType(Integer.valueOf(button.getVariants()[button.getValue()].replace("tag.type.", "")));
			this.initGui();
		} else if (id == 91) { // done
			this.close();
		}
		if (id == 92) { // list type
			this.tag.setTypeList(Integer.valueOf(button.getVariants()[button.getValue()].replace("tag.type.", "")));
			this.initGui();
		}
	}

	private boolean check() {
		if (this.getTextField(93) == null || this.textarea == null) {
			return false;
		}
		if (this.getTextField(93).getText().length() == 0 || this.textarea.getText().length() == 0) {
			return false;
		}
		String vs = this.textarea.getText();
		if (vs.indexOf("|") != -1) {
			String[] vss = vs.split("|");
			for (String str : vss) {
				String ch = this.tag.cheakValue(str, this.tag.getType());
				if (ch == null) {
					return false;
				}
			}
		} else {
			String ch = this.tag.cheakValue(vs, this.tag.getType());
			if (ch != null) {
				return true;
			}
		}
		return true;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		int t = this.tag.getType();
		int tl = this.tag.getTypeList();
		String gn = new String(Character.toChars(0x00A7)) + "2";
		String r = new String(Character.toChars(0x00A7)) + "c";
		String gr = new String(Character.toChars(0x00A7)) + "7";
		String name = this.tag.getPath();
		if (name.indexOf(".") != -1) {
			List<String> nal = new ArrayList<String>();
			while (name.indexOf(".") != -1) {
				nal.add(name.substring(0, name.indexOf(".")));
				name = name.substring(name.indexOf(".") + 1);
			}
		}
		name = new String(Character.toChars(0x00A7)) + "2" + name;
		if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 7, 159, 10)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.name", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 20, 159, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.path", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 55, 159, 61)) {
			if (t == 7 || t == 11) {
				this.setHoverText(new TextComponentTranslation("drop.hover.tag.value.array", new Object[] { name })
						.getFormattedText());
			} else if (t == 9) {
				this.setHoverText(new TextComponentTranslation("drop.hover.tag.value.list", new Object[] { name })
						.getFormattedText());
			} else {
				this.setHoverText(new TextComponentTranslation("drop.hover.tag.value.normal", new Object[] { name })
						.getFormattedText());
			}
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 122, 46, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.chance", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 89, this.guiTop + 122, 76, 16)) {
			String v = gn + "true" + gr + ", " + gn + "false";
			if (t == 1) {
				v = gn + "-128" + gr + "<->" + r + "127";
			} else if (t == 2) {
				v = gn + "-32 768" + gr + "<->" + r + "32767";
			} else if (t == 3) {
				v = gn + "-2 147 483 648" + gr + "<->" + r + "2 147 483 647";
			} else if (t == 4) {
				v = gn + "-9 223 372 036 854 775 808" + gr + "<->" + r + "9 223 372 036 854 775 807";
			} else if (t == 5) {
				v = new TextComponentTranslation("type.double", new Object[] { "77" }).getFormattedText();
			} else if (t == 6) {
				v = new TextComponentTranslation("type.double", new Object[] { "308" }).getFormattedText();
			} else if (t == 7) {
				v = "array [v0, v1, ... vn] v_ = " + gn + "-128" + gr + "<->" + r + "127";
			} else if (t == 8) {
				v = new TextComponentTranslation("type.string", new Object[] { "308" }).getFormattedText();
			} else if (t == 9) {
				v = new TextComponentTranslation("type.list", new Object[0]).getFormattedText();
			} else if (t == 11) {
				v = "array [v0, v1, ... vn] v_ = " + gn + "-2 147 483 648" + gr + "<->" + r + "2 147 483 647";
			}
			this.setHoverText(
					new TextComponentTranslation("drop.hover.tag.type", new Object[] { name, v }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 89, this.guiTop + 144, 76, 16)) {
			String v = gn + "-2 147 483 648" + gr + "<->" + r + "2 147 483 647";
			if (tl == 5) {
				v = new TextComponentTranslation("type.double", new Object[] { "77" }).getFormattedText();
			} else if (tl == 6) {
				v = new TextComponentTranslation("type.double", new Object[] { "308" }).getFormattedText();
			} else if (tl == 8) {
				v = new TextComponentTranslation("type.string", new Object[] { "308" }).getFormattedText();
			} else if (tl == 11) {
				v = "array [v0, v1, ... vn] v_ = " + gn + "-2 147 483 648" + gr + "<->" + r + "2 147 483 647";
			}
			this.setHoverText(new TextComponentTranslation("drop.hover.tag.listtype", new Object[] { name, v })
					.getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 144, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("hover.back", new Object[0]).getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 100;
		// type value
		int t = this.tag.getType();
		// name / type / path
		String name = this.tag.getPath();
		if (name.indexOf(".") != -1) {
			List<String> nal = new ArrayList<String>();
			while (name.indexOf(".") != -1) {
				nal.add(name.substring(0, name.indexOf(".")));
				name = name.substring(name.indexOf(".") + 1);
			}
		}
		String type = new TextComponentTranslation("tag.type." + t, new Object[0]).getFormattedText();
		this.addLabel(new GuiNpcLabel(anyIDs++,
				new TextComponentTranslation("drop.tag.type", new Object[] { name, type }).getFormattedText(),
				this.guiLeft + 4, this.guiTop + 5));
		GuiNpcTextField path = new GuiNpcTextField(93, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 18, 163, 20,
				this.tag.getPath());
		this.addTextField(path);
		// value
		this.addLabel(new GuiNpcLabel(anyIDs++, "type.value", this.guiLeft + 4, this.guiTop + 40));
		String[] textArr = this.tag.getValues();
		String text = "";
		if (textArr.length > 0) {
			text = textArr[0];
		}
		if (textArr.length > 1) {
			for (int i = 1; i < textArr.length; i++) {
				text += "|" + textArr[i];
			}
		}
		/*
		 * GuiNpcTextField value = new GuiNpcTextField(94, (GuiScreen)this, this.guiLeft
		 * + 4, this.guiTop + 53, 163, 65, text); this.addTextField(value);
		 */
		(this.textarea = new GuiTextArea(94, this.guiLeft + 4, this.guiTop + 53, 163, 65, text)).setListener(this);
		this.textarea.active = true;
		this.add(this.textarea);
		// chance
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.chance", this.guiLeft + 56, this.guiTop + 125));
		GuiNpcTextField chanceE = new GuiNpcTextField(95, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 120, 50, 20,
				String.valueOf(this.tag.getChance()));
		chanceE.setDoubleNumbersOnly().setMinMaxDoubleDefault(0.0001d, 100.0d, this.tag.getChance());
		this.addTextField(chanceE);
		// type
		int posId = 0;
		for (int i = 0; i < this.tagIds.length; i++) {
			if (this.tagIds[i].equals("tag.type." + this.tag.getType())) {
				posId = i;
			}
		}
		this.addButton(
				new GuiButtonBiDirectional(90, this.guiLeft + 87, this.guiTop + 120, 80, 20, this.tagIds, posId));
		// list type
		int posListId = 0;
		for (int i = 0; i < this.tagListIds.length; i++) {
			if (this.tagListIds[i].equals("tag.type." + this.tag.getTypeList())) {
				posListId = i;
			}
		}
		this.addButton(new GuiButtonBiDirectional(92, this.guiLeft + 87, this.guiTop + 142, 80, 20, this.tagListIds,
				posListId));
		this.getButton(92).setVisible(t == 9);
		// done
		this.addButton(new GuiNpcButton(91, this.guiLeft + 4, this.guiTop + 142, 80, 20, "gui.done", check()));
	}

	@Override
	public void textUpdate(String text) {
		this.tag.setValues(text);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 93) { // path
			this.tag.setPath(textfield.getText());
		} else if (textfield.getId() == 95) { // chance
			this.tag.setChance(textfield.getDouble());
		}
		this.initGui();
	}
}
