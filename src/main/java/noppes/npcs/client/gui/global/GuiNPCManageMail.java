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

public class GuiNPCManageMail
extends GuiNPCInterface2
implements IGuiData, ITextfieldListener {

	public GuiNPCManageMail(EntityNPCInterface npc) {
		super(npc);
		Client.sendData(EnumPacketServer.PlayerMailsGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) {
			CustomNpcs.MailSendToYourself = ((GuiNpcCheckBox) button).isSelected();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lID = 0, x0 = this.guiLeft + 10, x1 = x0 + 80, y = this.guiTop + 20;
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.days", x0, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, "follower.days", x1 + 65, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x1, y, 60, 20, "" + CustomNpcs.MailTimeWhenLettersWillBeDeleted);
		textField.setMinMaxDefault(0, 60, CustomNpcs.MailTimeWhenLettersWillBeDeleted);
		textField.setHoverText("mail.hover.deleted.time");
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.rec", x0, (y += 24) + 5));
		int[] vd = CustomNpcs.MailTimeWhenLettersWillBeReceived;
		if (vd[0] > vd[1]) {
			int m = vd[0];
			vd[0] = vd[1];
			vd[1] = m;
		}
		this.addLabel(new GuiNpcLabel(lID++, "gui.min", x0, (y += 16) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		textField = new GuiNpcTextField(1, this, x1, y, 60, 20, "" + vd[0]);
		textField.setMinMaxDefault(1, 3600, vd[0]);
		textField.setHoverText("mail.hover.min.send.time");
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "gui.max", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		textField = new GuiNpcTextField(2, this, x1, y, 60, 20, "" + vd[1]);
		textField.setMinMaxDefault(1, 3600, vd[1]);
		textField.setHoverText("mail.hover.max.send.time");
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.costs", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.0", x0, (y += 16) + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x1 + 65, y + 5));
		ITextComponent hoverCost = new TextComponentTranslation("mail.hover.cost");
		textField = new GuiNpcTextField(3, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[0]);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[0]);
		textField.setHoverText(new TextComponentTranslation("mail.hover.cost.0").appendSibling(hoverCost).getFormattedText());
		addTextField(textField);

		int x2 = x1 + 120, x3 = x2 + 80;
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.1", x2, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x3 + 65, y + 5));
		textField = new GuiNpcTextField(4, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[1]);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[1]);
		textField.setHoverText(new TextComponentTranslation("mail.hover.cost.1").appendSibling(hoverCost).getFormattedText());
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.2", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.displayCurrencies, x1 + 65, y + 5));
		textField = new GuiNpcTextField(5, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[2]);
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[2]);
		textField.setHoverText(new TextComponentTranslation("mail.hover.cost.2").appendSibling(hoverCost).getFormattedText());
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.3", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "%", x1 + 65, y + 5));
		textField = new GuiNpcTextField(6, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[3]);
		textField.setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[3]);
		textField.setHoverText(new TextComponentTranslation("mail.hover.cost.3").appendSibling(hoverCost).getFormattedText());
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.4", x2, y + 5));
		this.addLabel(new GuiNpcLabel(lID, "%", x3 + 65, y + 5));
		textField = new GuiNpcTextField(7, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[4]);
		textField.setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[4]);
		textField.setHoverText(new TextComponentTranslation("mail.hover.cost.4").appendSibling(hoverCost).getFormattedText());
		addTextField(textField);

		addButton(new GuiNpcCheckBox(0, x0, y + 24, 200, 14, "mail.send.yourself", null, CustomNpcs.MailSendToYourself));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
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
		this.initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (!textField.isInteger()) {
			this.initGui();
			return;
		}
		switch (textField.getID()) {
			case 0: {
				int v = textField.getInteger();
				if (v == 0) {
					v = 1;
				}
				CustomNpcs.MailTimeWhenLettersWillBeDeleted = v;
				break;
			}
			case 1: {
				if (this.getTextField(2) == null) {
					return;
				}
				GuiNpcTextField textField2 = (GuiNpcTextField) getTextField(2);
				int[] vd = new int[] { textField.getInteger(), textField2.getInteger() };
				if (vd[0] > vd[1]) {
					int m = vd[0];
					vd[0] = vd[1];
					vd[1] = m;
				}
				CustomNpcs.MailTimeWhenLettersWillBeReceived[0] = vd[0];
				CustomNpcs.MailTimeWhenLettersWillBeReceived[1] = vd[1];
				textField.setFullText("" + vd[0]);
				textField2.setText("" + vd[1]);
				break;
			}
			case 2: {
				if (this.getTextField(1) == null) {
					return;
				}
				GuiNpcTextField textField1 = (GuiNpcTextField) getTextField(1);
				int[] vd = new int[] { textField1.getInteger(), textField.getInteger() };
				if (vd[0] > vd[1]) {
					int m = vd[0];
					vd[0] = vd[1];
					vd[1] = m;
				}
				CustomNpcs.MailTimeWhenLettersWillBeReceived[0] = vd[0];
				CustomNpcs.MailTimeWhenLettersWillBeReceived[1] = vd[1];
				textField1.setText("" + vd[0]);
				textField.setFullText("" + vd[1]);
				break;
			}
		}
	}

}
