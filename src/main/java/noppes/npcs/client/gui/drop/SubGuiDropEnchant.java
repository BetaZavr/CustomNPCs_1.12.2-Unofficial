package noppes.npcs.client.gui.drop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		setBackground("companion_empty.png");
		xSize = 172;
		ySize = 167;
		closeOnEsc = true;

		enchant = ench;
		List<Integer> el = new ArrayList<>();
		for (Enchantment en : ForgeRegistries.ENCHANTMENTS.getValuesCollection()) {
			el.add(Enchantment.getEnchantmentID(en));
		}
		Collections.sort(el);
		enchIds = new String[el.size()];
		for (int i = 0; i < el.size(); i++) { enchIds[i] = "" + el.get(i); }
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 50) { // select
			enchant.setEnchant(Integer.parseInt(button.getVariants()[button.getValue()]));
			initGui();
		} else if (button.id == 51) { // done
			close();
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
		GuiNpcButton button = new GuiButtonBiDirectional(50, guiLeft + 4, guiTop + 17, 80, 20, enchIds, posId);
		button.setHoverText("drop.hover.enchant.list");
		addButton(button);
		// levels
		levels = new int[] { enchant.getMinLevel(), enchant.getMaxLevel() };
		addLabel(new GuiNpcLabel(anyIDs++, "type.level", guiLeft + 56, guiTop + 48));
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (enchant.parent.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		// min
		GuiNpcTextField textField = new GuiNpcTextField(52, this, guiLeft + 4, guiTop + 39, 50, 14, "" + levels[0]);
		textField.setMinMaxDefault(0, 100000, enchant.getMinLevel());
		textField.setHoverText("drop.hover.enchant.levels", "" + enchant.ench.getMaxLevel(), tied);
		addTextField(textField);
		// max
		textField = new GuiNpcTextField(53, this, guiLeft + 4, guiTop + 53, 50, 14, "" + levels[1]);
		textField.setMinMaxDefault(0, 100000, enchant.getMaxLevel());
		textField.setHoverText("drop.hover.enchant.levels", "" + enchant.ench.getMaxLevel(), tied);
		addTextField(textField);
		// chance
		addLabel(new GuiNpcLabel(anyIDs, "drop.chance", guiLeft + 56, guiTop + 74));
		textField = new GuiNpcTextField(54, this, guiLeft + 4, guiTop + 69, 50, 20, String.valueOf(enchant.getChance()));
		textField.setMinMaxDoubleDefault(0.0001d, 100.0d, enchant.getChance());
		textField.setHoverText("drop.hover.enchant.chance");
		addTextField(textField);
		// done
		button = new GuiNpcButton(51, guiLeft + 4, guiTop + 142, 80, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 52) { // level min
			levels[0] = textfield.getInteger();
			enchant.setLevels(levels[0], levels[1]);
			initGui();
		} else if (textfield.getId() == 53) { // level max
			levels[1] = textfield.getInteger();
			enchant.setLevels(levels[0], levels[1]);
			initGui();
		} else if (textfield.getId() == 54) { // chance
			enchant.setChance(textfield.getDouble());
			initGui();
		}
	}
}
