package noppes.npcs.client.gui.global;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCManageMail extends GuiNPCInterface2 implements IGuiData, ITextfieldListener {

	public GuiNPCManageMail(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuGlobal;

		Client.sendData(EnumPacketServer.PlayerMailsGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && button.getID() == 0) {
			CustomNpcs.MailSendToYourself = ((GuiNpcCheckBox) button).isSelected();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lID = 0, x0 = guiLeft + 10, x1 = x0 + 80, y = guiTop + 20;
		addLabel(new GuiNpcLabel(lID++, "mail.time.days", x0, y + 5));
		addLabel(new GuiNpcLabel(lID++, "follower.days", x1 + 65, y + 5));
		addTextField(new GuiNpcTextField(0, this, x1, y, 60, 20, "" + CustomNpcs.MailTimeWhenLettersWillBeDeleted)
				.setMinMaxDefault(0, 60, CustomNpcs.MailTimeWhenLettersWillBeDeleted)
				.setHoverText("mail.hover.deleted.time"));
		addLabel(new GuiNpcLabel(lID++, "mail.time.rec", x0, (y += 24) + 5));
		int[] vd = CustomNpcs.MailTimeWhenLettersWillBeReceived;
		if (vd[0] > vd[1]) {
			int m = vd[0];
			vd[0] = vd[1];
			vd[1] = m;
		}
		addLabel(new GuiNpcLabel(lID++, "gui.min", x0, (y += 16) + 5));
		addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		addTextField(new GuiNpcTextField(1, this, x1, y, 60, 20, "" + vd[0])
				.setMinMaxDefault(1, 3600, vd[0])
				.setHoverText("mail.hover.min.send.time"));
		addLabel(new GuiNpcLabel(lID++, "gui.max", x0, (y += 24) + 5));
		addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		addTextField(new GuiNpcTextField(2, this, x1, y, 60, 20, "" + vd[1])
				.setMinMaxDefault(1, 3600, vd[1])
				.setHoverText("mail.hover.max.send.time"));
		addLabel(new GuiNpcLabel(lID++, "mail.time.costs", x0, (y += 24) + 5));
		addLabel(new GuiNpcLabel(lID++, "mail.time.cost.0", x0, (y += 16) + 5));
		addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x1 + 65, y + 5));
		ITextComponent hoverCost = new TextComponentTranslation("mail.hover.cost");
		addTextField(new GuiNpcTextField(3, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[0])
				.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[0])
				.setHoverText(new TextComponentTranslation("mail.hover.cost.0").appendSibling(hoverCost).getFormattedText()));
		int x2 = x1 + 120, x3 = x2 + 80;
		addLabel(new GuiNpcLabel(lID++, "mail.time.cost.1", x2, y + 5));
		addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x3 + 65, y + 5));
		addTextField(new GuiNpcTextField(4, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[1])
				.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[1])
				.setHoverText(new TextComponentTranslation("mail.hover.cost.1").appendSibling(hoverCost).getFormattedText()));
		addLabel(new GuiNpcLabel(lID++, "mail.time.cost.2", x0, (y += 24) + 5));
		addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x1 + 65, y + 5));
		addTextField(new GuiNpcTextField(5, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[2])
				.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[2])
				.setHoverText(new TextComponentTranslation("mail.hover.cost.2").appendSibling(hoverCost).getFormattedText()));
		addLabel(new GuiNpcLabel(lID++, "mail.time.cost.3", x0, (y += 24) + 5));
		addLabel(new GuiNpcLabel(lID++, "%", x1 + 65, y + 5));
		addTextField(new GuiNpcTextField(6, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[3])
				.setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[3])
				.setHoverText(new TextComponentTranslation("mail.hover.cost.3").appendSibling(hoverCost).getFormattedText()));
		addLabel(new GuiNpcLabel(lID++, "mail.time.cost.4", x2, y + 5));
		addLabel(new GuiNpcLabel(lID, "%", x3 + 65, y + 5));
		addTextField(new GuiNpcTextField(7, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[4])
				.setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[4])
				.setHoverText(new TextComponentTranslation("mail.hover.cost.4").appendSibling(hoverCost).getFormattedText()));
		addButton(new GuiNpcCheckBox(0, x0, y + 24, 200, 14, "mail.send.yourself", null, CustomNpcs.MailSendToYourself));
	}

	@Override
	public void save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("LettersBeDeleted", CustomNpcs.MailTimeWhenLettersWillBeDeleted);
		compound.setIntArray("LettersBeReceived", CustomNpcs.MailTimeWhenLettersWillBeReceived);
		compound.setBoolean("SendToYourself", CustomNpcs.MailSendToYourself);
		Client.sendData(EnumPacketServer.PlayerMailsSave, compound);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		int[] vs = compound.getIntArray("LettersBeReceived");
        System.arraycopy(vs, 0, CustomNpcs.MailTimeWhenLettersWillBeReceived, 0, vs.length);
		vs = compound.getIntArray("CostSendingLetter");
        System.arraycopy(vs, 0, CustomNpcs.MailCostSendingLetter, 0, vs.length);
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (!textField.isInteger()) { initGui(); return; }
		switch (textField.getID()) {
			case 0: {
				int v = textField.getInteger();
				if (v == 0) { v = 1; }
				CustomNpcs.MailTimeWhenLettersWillBeDeleted = v;
				break;
			}
			case 1: {
				if (getTextField(2) == null) {return; }
				GuiNpcTextField textField2 = getTextField(2);
				int[] vd = new int[] { textField.getInteger(), textField2.getInteger() };
				if (vd[0] > vd[1]) {
					int m = vd[0];
					vd[0] = vd[1];
					vd[1] = m;
				}
				CustomNpcs.MailTimeWhenLettersWillBeReceived[0] = vd[0];
				CustomNpcs.MailTimeWhenLettersWillBeReceived[1] = vd[1];
				textField.setText("" + vd[0]);
				textField2.setText("" + vd[1]);
				break;
			}
			case 2: {
				if (getTextField(1) == null) { return; }
				GuiNpcTextField textField1 = getTextField(1);
				int[] vd = new int[] { textField1.getInteger(), textField.getInteger() };
				if (vd[0] > vd[1]) {
					int m = vd[0];
					vd[0] = vd[1];
					vd[1] = m;
				}
				CustomNpcs.MailTimeWhenLettersWillBeReceived[0] = vd[0];
				CustomNpcs.MailTimeWhenLettersWillBeReceived[1] = vd[1];
				textField1.setText("" + vd[0]);
				textField.setText("" + vd[1]);
				break;
			}
		}
	}

}
