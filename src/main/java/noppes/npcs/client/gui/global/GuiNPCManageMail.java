package noppes.npcs.client.gui.global;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageMail
extends GuiNPCInterface2
implements IGuiData, ITextfieldListener {
	
	public GuiNPCManageMail(EntityNPCInterface npc) {
		super(npc);
		Client.sendData(EnumPacketServer.PlayerMailsGet, new Object[0]);
	}

	@Override
	public void initGui() {
		super.initGui();
		int lID = 0, x0 = this.guiLeft + 10, x1 = x0 + 80, y = this.guiTop + 20;
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.days", x0, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, "follower.days", x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, x1, y, 60, 20, "" + CustomNpcs.MailTimeWhenLettersWillBeDeleted));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, 60, CustomNpcs.MailTimeWhenLettersWillBeDeleted);
		
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.rec", x0, (y += 24) + 5));
		int[] vd = CustomNpcs.MailTimeWhenLettersWillBeReceived;
		if (vd[0] > vd[1]) {
			int m = new Integer(vd[0]);
			vd[0] = vd[1];
			vd[1] = m;
		}
		this.addLabel(new GuiNpcLabel(lID++, "gui.min", x0, (y += 16) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, x1, y, 60, 20, "" + vd[0]));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, 3600, vd[0]);

		this.addLabel(new GuiNpcLabel(lID++, "gui.max", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "gui.sec", x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(2, this, x1, y, 60, 20, "" + vd[1]));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(1, 3600, vd[1]);
		
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.costs", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.0", x0, (y += 16) + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.CharCurrencies, x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[0]));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[0]);
		int x2 = x1 + 120, x3 = x2 + 80;
		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.1", x2, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.CharCurrencies, x3 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(4, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[1]));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[1]);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.2", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, CustomNpcs.CharCurrencies, x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(5, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[2]));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, Integer.MAX_VALUE, CustomNpcs.MailCostSendingLetter[2]);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.3", x0, (y += 24) + 5));
		this.addLabel(new GuiNpcLabel(lID++, "%", x1 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(6, this, x1, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[3]));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[3]);

		this.addLabel(new GuiNpcLabel(lID++, "mail.time.cost.4", x2, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, "%", x3 + 65, y + 5));
		this.addTextField(new GuiNpcTextField(7, this, x3, y, 60, 20, "" + CustomNpcs.MailCostSendingLetter[4]));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, 100, CustomNpcs.MailCostSendingLetter[4]);
		
		this.addButton(new GuiNpcCheckBox(0, x0, (y += 24), 200, 14, "mail.send.yourself", CustomNpcs.MailSendToYourself));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.deleted.time").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.min.send.time").getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.max.send.time").getFormattedText());
		} else if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.cost.0").appendSibling(new TextComponentTranslation("mail.hover.cost")).getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.cost.1").appendSibling(new TextComponentTranslation("mail.hover.cost")).getFormattedText());
		} else if (this.getTextField(5) != null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.cost.2").appendSibling(new TextComponentTranslation("mail.hover.cost")).getFormattedText());
		} else if (this.getTextField(6) != null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.cost.3").appendSibling(new TextComponentTranslation("mail.hover.cost")).getFormattedText());
		} else if (this.getTextField(7) != null && this.getTextField(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mail.hover.cost.4").appendSibling(new TextComponentTranslation("mail.hover.cost")).getFormattedText());
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			CustomNpcs.MailSendToYourself = ((GuiNpcCheckBox) button).isSelected();
		}
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
		for (int i = 0; i < vs.length; i++) {
			CustomNpcs.MailTimeWhenLettersWillBeReceived[i] = vs[i];
		}
		vs = compound.getIntArray("CostSendingLetter");
		for (int i = 0; i < vs.length; i++) {
			CustomNpcs.MailCostSendingLetter[i] = vs[i];
		}
		this.initGui();
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (!textField.isInteger()) {
			this.initGui();
			return;
		}
		switch(textField.getId()) {
			case 0: {
				int v = textField.getInteger();
				if (v == 0) { v = 1; }
				CustomNpcs.MailTimeWhenLettersWillBeDeleted = v;
				break;
			}
			case 1: {
				if (this.getTextField(2) == null) { return; }
				GuiNpcTextField textField2 = this.getTextField(2);
				int[] vd = new int[] { textField.getInteger(), textField2.getInteger() };
				if (vd[0] > vd[1]) {
					int m = new Integer(vd[0]);
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
				if (this.getTextField(1) == null) { return; }
				GuiNpcTextField textField1 = this.getTextField(1);
				int[] vd = new int[] { textField1.getInteger(), textField.getInteger() };
				if (vd[0] > vd[1]) {
					int m = new Integer(vd[0]);
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
