package noppes.npcs.client.gui.drop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.EnchantSet;

public class SubGuiDropEnchant
extends SubGuiInterface
implements ITextfieldListener {
	
	public EnchantSet enchant;
	public String[] enchIds;
	public int[] levels;

	public SubGuiDropEnchant(EnchantSet ench) {
		this.enchant = ench;
		this.setBackground("companion_empty.png");
		this.xSize = 172;
		this.ySize = 167;
		this.closeOnEsc = true;

		List<Integer> el = new ArrayList<Integer>();
		for (Enchantment en : ForgeRegistries.ENCHANTMENTS.getValuesCollection()) {
			el.add(Enchantment.getEnchantmentID(en));
		}
		Collections.sort(el);
		this.enchIds = new String[el.size()];
		for (int i = 0; i < el.size(); i++) {
			this.enchIds[i] = "" + el.get(i);
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 50) { // select
			this.enchant.setEnchant(Integer.valueOf(button.getVariants()[button.getValue()]));
			this.initGui();
		} else if (button.id == 51) { // done
			this.close();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		String tied = new TextComponentTranslation("drop.tied.random", new Object[0]).getFormattedText();
		if (this.enchant.parent.tiedToLevel) {
			tied = new TextComponentTranslation("drop.tied.level", new Object[0]).getFormattedText();
		}
		if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 19, 76, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.enchant.list", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 41, 46, 24)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.enchant.levels",
					new Object[] { "" + this.enchant.ench.getMaxLevel(), tied }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 71, 46, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.enchant.chance", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 144, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("hover.back", new Object[0]).getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 60;
		// name
		String name = new TextComponentTranslation("drop.enchants", new Object[0]).getFormattedText() + ": "
				+ new TextComponentTranslation(this.enchant.getEnchant(), new Object[0]).getFormattedText();
		this.addLabel(new GuiNpcLabel(anyIDs++, name, this.guiLeft + 4, this.guiTop + 5));
		// select
		int posId = 0;
		for (int i = 0; i < this.enchIds.length; i++) {
			if (this.enchIds[i].equals("" + Enchantment.getEnchantmentID(this.enchant.ench))) {
				posId = i;
			}
		}
		this.addButton(new GuiButtonBiDirectional(50, this.guiLeft + 4, this.guiTop + 17, 80, 20, this.enchIds, posId));
		// levels
		this.levels = new int[] { this.enchant.getMinLevel(), this.enchant.getMaxLevel() };
		this.addLabel(new GuiNpcLabel(anyIDs++, "type.level", this.guiLeft + 56, this.guiTop + 48));
		GuiNpcTextField levelMin = new GuiNpcTextField(52, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 39, 50, 14,
				"" + this.levels[0]);
		levelMin.setNumbersOnly().setMinMaxDefault(0, 100000, this.enchant.getMinLevel());
		this.addTextField(levelMin);
		GuiNpcTextField levelMax = new GuiNpcTextField(53, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 53, 50, 14,
				"" + this.levels[1]);
		levelMax.setNumbersOnly().setMinMaxDefault(0, 100000, this.enchant.getMaxLevel());
		this.addTextField(levelMax);
		// chance
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.chance", this.guiLeft + 56, this.guiTop + 74));
		GuiNpcTextField chanceE = new GuiNpcTextField(54, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 69, 50, 20,
				String.valueOf(this.enchant.getChance()));
		chanceE.setDoubleNumbersOnly().setMinMaxDoubleDefault(0.0001d, 100.0d, this.enchant.getChance());
		this.addTextField(chanceE);
		// done
		this.addButton(new GuiNpcButton(51, this.guiLeft + 4, this.guiTop + 142, 80, 20, "gui.done"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 52) { // level min
			this.levels[0] = textfield.getInteger();
			this.enchant.setLevels(this.levels[0], this.levels[1]);
			this.initGui();
		} else if (textfield.getId() == 53) { // level max
			this.levels[1] = textfield.getInteger();
			this.enchant.setLevels(this.levels[0], this.levels[1]);
			this.initGui();
		} else if (textfield.getId() == 54) { // chance
			this.enchant.setChance(textfield.getDouble());
			this.initGui();
		}
	}
}
