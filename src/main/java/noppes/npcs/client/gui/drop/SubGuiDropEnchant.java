package noppes.npcs.client.gui.drop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.EnchantSet;

import javax.annotation.Nonnull;

public class SubGuiDropEnchant extends SubGuiInterface implements ITextfieldListener {

	protected String[] enchIds;
	protected int[] levels;
	public EnchantSet enchant;

	public SubGuiDropEnchant(EnchantSet ench) {
		super(0);
		setBackground("companion_empty.png");
		closeOnEsc = true;
		xSize = 172;
		ySize = 167;

		enchant = ench;
		List<Integer> el = new ArrayList<>();
		for (Enchantment en : ForgeRegistries.ENCHANTMENTS.getValuesCollection()) { el.add(Enchantment.getEnchantmentID(en)); }
		Collections.sort(el);
		enchIds = new String[el.size()];
		for (int i = 0; i < el.size(); i++) { enchIds[i] = "" + el.get(i); }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 50: enchant.setEnchant(Integer.parseInt(button.getVariants()[button.getValue()])); initGui(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 60;
		// name
		String name = new TextComponentTranslation("drop.enchants").getFormattedText() + ": " + new TextComponentTranslation(enchant.getEnchant()).getFormattedText();
		addLabel(new GuiNpcLabel(anyIDs++, name, guiLeft + 4, guiTop + 5));
		// select
		int posId = 0;
		String idName = "" + Enchantment.getEnchantmentID(enchant.ench);
		for (int i = 0; i < enchIds.length; i++) {
			if (enchIds[i].equals(idName)) { posId = i; }
		}
		addButton(new GuiButtonBiDirectional(50, guiLeft + 4, guiTop + 17, 80, 20, enchIds, posId)
				.setHoverText("drop.hover.enchant.list"));
		// levels
		levels = new int[] { enchant.getMinLevel(), enchant.getMaxLevel() };
		addLabel(new GuiNpcLabel(anyIDs++, "type.level", guiLeft + 56, guiTop + 48));
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (enchant.parent.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		// min
		addTextField(new GuiNpcTextField(52, this, guiLeft + 4, guiTop + 39, 50, 14, "" + levels[0])
				.setMinMaxDefault(0, 100000, enchant.getMinLevel())
				.setHoverText("drop.hover.enchant.levels", "" + enchant.ench.getMaxLevel(), tied));
		// max
		addTextField(new GuiNpcTextField(53, this, guiLeft + 4, guiTop + 53, 50, 14, "" + levels[1])
				.setMinMaxDefault(0, 100000, enchant.getMaxLevel())
				.setHoverText("drop.hover.enchant.levels", "" + enchant.ench.getMaxLevel(), tied));
		// chance
		addLabel(new GuiNpcLabel(anyIDs, "drop.chance", guiLeft + 56, guiTop + 74));
		addTextField(new GuiNpcTextField(54, this, guiLeft + 4, guiTop + 69, 50, 20, String.valueOf(enchant.getChance()))
				.setMinMaxDoubleDefault(0.0001d, 100.0d, enchant.getChance())
				.setHoverText("drop.hover.enchant.chance"));
		// done
		addButton(new GuiNpcButton(66, guiLeft + 4, guiTop + 142, 80, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 52: levels[0] = textfield.getInteger(); enchant.setLevels(levels[0], levels[1]); initGui(); break; // level min
			case 53: levels[1] = textfield.getInteger(); enchant.setLevels(levels[0], levels[1]); initGui(); break; // level max
			case 54: enchant.setChance(textfield.getDouble()); initGui(); break; // chance
		}
	}

}
