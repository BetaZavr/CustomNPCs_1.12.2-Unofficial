package noppes.npcs.client.gui.global;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Deal;

import javax.annotation.Nonnull;

public class SubGuiNpcDealCaseSetting extends SubGuiInterface implements ITextfieldListener {

    protected final Deal deal;

    public SubGuiNpcDealCaseSetting(Deal dealIn) {
        super(0);
        setBackground("smallbg.png");
        title = new TextComponentTranslation("gui.case").appendText(":").getFormattedText();
        closeOnEsc = true;
        xSize = 176;
        ySize = 222;

        deal = dealIn;
    }

    @Override
    public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
        if (mouseButton != 0) { return; }
        switch (button.id) {
            case 0: deal.setShowInCase(((GuiNpcCheckBox) button).isSelected()); break;
            case 66: onClosed(); break;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int lId = 0;
        int x = guiLeft + 4;
        int y = guiTop + 19;
        ICustomDrop[] caseItems = deal.getCaseItems();
        // name
        addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.name").appendText(":"), x, y, 166, 12));
        addTextField(new GuiNpcTextField(0, this, x, y += 12, 166, 12, deal.getCaseName())
                .setHoverText("market.hover.case.name", TextFormatting.RESET + new TextComponentTranslation(deal.getCaseName()).getFormattedText()));
        addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.obj").appendText(":"), x, y += 14, 166, 12));
        // obj model
        addTextField(new GuiNpcTextField(1, this, x, y += 12, 166, 12, deal.getCaseObjModel())
                .setHoverText("market.hover.case.obj"));
        // texture
        addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.texture").appendText(":"), x, y += 14, 166, 12));
        ResourceLocation texture = deal.getCaseTexture();
        addTextField(new GuiNpcTextField(2, this, x, y += 12, 166, 12, texture)
                .setHoverText("market.hover.case.texture"));
        // sound
        addLabel(new GuiNpcLabel(lId++, "market.case.sound", x, y += 14, 166, 12));
        addTextField(new GuiNpcTextField(3, this, x, y += 12, 166, 12, deal.getCaseSound())
                .setHoverText("market.hover.case.sound"));
        // command
        addLabel(new GuiNpcLabel(lId++, "advMode.command", x, y += 14, 166, 12));
        addTextField(new GuiNpcTextField(4, this, x, y += 12, 166, 12, deal.getCaseCommand())
                .setHoverText("dialog.option.hover.command"));
        // count
        addLabel(new GuiNpcLabel(lId, "market.case.count", x, y += 14, 166, 12));
        addTextField(new GuiNpcTextField(5, this, x, y += 12, 83, 12, "" + deal.getCaseCount())
                .setMinMaxDefault(1, caseItems.length - 1, deal.getCaseCount())
                .setHoverText("market.hover.case.count"));
        // show items in hover
        addButton(new GuiNpcCheckBox(0, x, y + 16, 166, 12, "market.case.show.true", "market.case.show.false", deal.showInCase()));
        // exit
        addButton(new GuiNpcButton(66, x, guiTop + ySize - 20, 80, 16, "gui.back")
                .setHoverText("hover.back"));
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        switch (textField.getID()) {
            case 0: deal.setCaseName(textField.getText()); break;
            case 1: deal.setCaseObjModel(textField.getText().isEmpty() ? null : new ResourceLocation(textField.getText())); break;
            case 2: deal.setCaseTexture(textField.getText().isEmpty() ? null : new ResourceLocation(textField.getText())); break;
            case 3: deal.setCaseSound(textField.getText().isEmpty() ? null : new ResourceLocation(textField.getText())); break;
            case 4: deal.setCaseCommand(textField.getText()); break;
            case 5: deal.setCaseCount(textField.getInteger()); break;
        }
        initGui();
    }

}
