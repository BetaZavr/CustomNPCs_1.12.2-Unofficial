package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;
import noppes.npcs.util.AdditionalMethods;

public class GuiMailbox extends GuiNPCInterface implements IGuiData, ICustomScrollListener, GuiYesNoCallback {
	private PlayerMailData data;
	private GuiCustomScroll scroll;
	private PlayerMail selected;
	private long time;

	public GuiMailbox() {
		this.xSize = 256;
		this.time = System.currentTimeMillis();
		this.setBackground("menubg.png");
		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailGet, new Object[0]);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (this.scroll.selected < 0) {
			return;
		}
		if (button.id == 0) {
			GuiMailmanWrite.parent = this;
			GuiMailmanWrite.mail = this.selected;
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, this.selected.time, this.selected.sender);
			this.selected = null;
			this.scroll.selected = -1;
		}
		if (button.id == 1) {
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, "",
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag && this.selected != null) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, this.selected.time, this.selected.sender);
			this.selected = null;
		}
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.getLabel(2) != null && this.getLabel(2).enabled) {
			this.getLabel(2).setLabel(new TextComponentTranslation("mailbox.timesend",
					new Object[] { this.getTimePast() }).getFormattedText());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private String getTimePast() { // Changed
		if (this.selected == null) {
			return "";
		}
		return AdditionalMethods.ticksToElapsedTime((this.selected.timePast + System.currentTimeMillis() - this.time) / 50, false, true, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 186);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 4;
		this.addScroll(this.scroll);
		String title = new TextComponentTranslation("mailbox.name").getFormattedText();
		int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop - 8));
		if (this.selected != null) {
			this.addLabel(new GuiNpcLabel(3, new TextComponentTranslation("mailbox.sender").getFormattedText() + ":",
					this.guiLeft + 170, this.guiTop + 6));
			this.addLabel(new GuiNpcLabel(1, this.selected.sender, this.guiLeft + 174, this.guiTop + 18));
			this.addLabel(new GuiNpcLabel(2, "", this.guiLeft + 174, this.guiTop + 30));
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 192, 82, 20, "mailbox.read"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 88, this.guiTop + 192, 82, 20, "selectWorld.deleteButton"));
		this.getButton(1).setEnabled(this.selected != null);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.selected = this.data.playermail.get(guiCustomScroll.selected);
		this.initGui();
		if (this.selected != null && !this.selected.beenRead) {
			this.selected.beenRead = true;
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailRead, this.selected.time, this.selected.sender);
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		GuiMailmanWrite.parent = this;
		GuiMailmanWrite.mail = this.selected;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, this.selected.time, this.selected.sender);
		this.selected = null;
		this.scroll.selected = -1;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		PlayerMailData data = new PlayerMailData();
		data.loadNBTData(compound);
		List<String> list = new ArrayList<String>();
		Collections.sort(data.playermail, (o1, o2) -> {
			if (o1.time == o2.time) {
				return 0;
			} else {
				return (o1.time > o2.time) ? -1 : 1;
			}
		});
		for (PlayerMail mail : data.playermail) {
			list.add(mail.subject);
		}
		this.data = data;
		this.scroll.clear();
		this.selected = null;
		this.scroll.setListNotSorted(list);
	}
}
