package noppes.npcs.client.gui.player;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.util.ResourceData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.Util;

public class GuiMailbox
extends GuiNPCInterface
implements IGuiData, ICustomScrollListener, GuiYesNoCallback {

	public static final ResourceLocation icons = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/icons.png");
	private static final ResourceLocation mBox = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_empty.png");
	private static final ResourceLocation mDoor = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_door.png");
	private static final ResourceLocation mList = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_list.png");
	private final Map<String, PlayerMail> scrollData = new HashMap<>();
	private GuiCustomScroll scroll;
	private PlayerMail selected;

	// Animations
	private int closeType;
	private int step;
	private int tick;
	private int millyTick;
	private final Random rnd = new Random();

	public GuiMailbox() {
		this.xSize = 192;
		this.ySize = 236;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailGet);

		ClientTickHandler.checkMails = true;
		// Animations
		tick = 30;
		millyTick = 30;
		step = 0;
		closeType = 0;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		GuiMailmanWrite.parent = this;
		switch (button.getID()) {
			case 0: {
				if (this.selected == null) {
					return;
				}
				GuiMailmanWrite.mail = this.selected;
				step = 4;
				tick = 15;
				millyTick = 15;
				this.closeType = 2;
				break;
			}
			case 1: {
				step = 4;
				tick = 15;
				millyTick = 15;
				this.closeType = 1;
				break;
			}
			case 2: {
				if (this.selected == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, this.scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 3: {
				if (ClientProxy.playerData.mailData.playerMails.isEmpty()) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("mailbox.name").getFormattedText() + ":", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
				displayGuiScreen(guiyesno);
				break;
			}
			case 4: {
				if (ClientProxy.playerData.mailData.playerMails.isEmpty()) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("mailbox.name").getFormattedText() + ":", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 5: {
				step = 4;
				tick = 15;
				millyTick = 15;
				this.closeType = 0;
				break;
			}
		}
	}

	public void confirmClicked(boolean flag, int id) {
		NoppesUtil.openGUI(this.player, this);
		if (!flag) {
			return;
		}
		if (id == 0 && this.selected != null) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, this.selected.timeWhenReceived,
					this.selected.sender);
			this.selected = null;
		} else if (id == 1) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, 0L);
			this.selected = null;
		} else if (id == 2) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, -1L);
			this.selected = null;
		}
		MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.delete",
				(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
				0.9f + 0.2f * this.rnd.nextFloat());
	}

	private void drawMailBox(float u, float v) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(u, v, 0.0f);
		this.mc.getTextureManager().bindTexture(mBox);
		this.drawTexturedModalRect(0, 0, 0, 0, 192, 236); // Box
		if (!this.scrollData.isEmpty()) {
			this.mc.getTextureManager().bindTexture(mList);
			this.drawTexturedModalRect(8, 45, 0, 0, 176, 156); // list
		}
		if (step == 3) {
			this.mc.getTextureManager().bindTexture(mDoor);
			this.drawTexturedModalRect(-5, 44, 181, 0, 7, 158); // door
		}
		GlStateManager.popMatrix();

		if (scroll != null) {
			scroll.guiLeft = (int) u + 9;
			scroll.guiTop = (int) v + 45;
		}
		if (this.getLabel(0) != null && this.getLabel(0).isEnabled()) {
			GuiNpcLabel l = (GuiNpcLabel) getLabel(0);
			l.x = (int) u + 95 - (l.width / 2);
			l.y = (int) v + 11;
		}
		for (int i = 0; i < 6; i++) {
			if (this.getButton(i) == null) {
				return;
			}
			GuiNpcButton b = (GuiNpcButton) getButton(i);
			b.setEnabled(step == 3);
			switch (i) {
			case 0: { // read
				b.x = (int) u + 8;
				b.y = (int) v + 202;
				b.setEnabled(step == 3 && this.selected != null);
				break;
			}
			case 1: { // write
				b.x = (int) u + 67;
				b.y = (int) v + 202;
				b.setEnabled(step == 3);
				break;
			}
			case 2: { // remove
				b.x = (int) u + 126;
				b.y = (int) v + 202;
				b.setEnabled(step == 3 && this.selected != null);
				break;
			}
			case 3: { // remove all
				b.x = (int) u + 8;
				b.y = (int) v + 218;
				b.setEnabled(step == 3 && scroll != null && !scroll.getList().isEmpty());
				break;
			}
			case 4: { // clear
				b.x = (int) u + 67;
				b.y = (int) v + 218;
				b.setEnabled(step == 3 && scroll != null && !scroll.getList().isEmpty());
				break;
			}
			case 5: { // exit
				b.x = (int) u + 126;
				b.y = (int) v + 218;
				b.setEnabled(step == 3);
				break;
			}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Animations
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (tick >= 0) {
			if (tick == 0) {
				partialTicks = 0.0f;
			}
			float part = (float) tick + partialTicks;
			float cos = (float) Math.cos(90.0d * part / (double) millyTick * Math.PI / 180.0d);
			if (cos < 0.0f) {
				cos = 0.0f;
			} else if (cos > 1.0f) {
				cos = 1.0f;
			}
			switch (step) {
			case 0: { // box appears
				if (tick == millyTick) {
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.movement",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.75f + 0.25f * this.rnd.nextFloat());
				}
				this.drawMailBox(this.guiLeft, this.guiTop + (1.0f - cos) * 236.0f);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop + (1.0f - cos) * 236.0f, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 1;
					tick = 20;
					millyTick = 20;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.open.door",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.75f + 0.25f * this.rnd.nextFloat());
					GlStateManager.disableBlend();
				}
				break;
			}
			case 1: { // opening the door
				this.drawMailBox(this.guiLeft, this.guiTop);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - (cos * 193.0f), this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 2;
					tick = 15;
					millyTick = 15;
					GlStateManager.disableBlend();
				}
				break;
			}
			case 2: { // turning the door
				this.drawMailBox(this.guiLeft, this.guiTop);
				float s = 1.0f - cos;
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - 7.0f - (186.0f) * s, this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				GlStateManager.scale(s, 1.0f, 1.0f);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				this.drawTexturedModalRect(183, 44, 178, 0, 3, 158);
				GlStateManager.popMatrix();

				s = cos;
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - 7.0f, this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				GlStateManager.scale(s, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 44, 181, 0, 7, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 3;
					millyTick = 0;
					GlStateManager.disableBlend();
				}
				break;
			}
			case 4: { // back turning the door
				if (tick == millyTick) {
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS,
							CustomNpcs.MODID + ":mail.close.door", (float) this.player.posX, (float) this.player.posY,
							(float) this.player.posZ, 1.0f, 0.75f + 0.25f * this.rnd.nextFloat());
				}
				this.drawMailBox(this.guiLeft, this.guiTop);
				float s = cos;
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - 7.0f - (186.0f) * s, this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				GlStateManager.scale(s, 1.0f, 1.0f);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				this.drawTexturedModalRect(183, 44, 178, 0, 3, 158);
				GlStateManager.popMatrix();

				s = 1.0f - cos;
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - 7.0f, this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				GlStateManager.scale(s, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 44, 181, 0, 7, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 5;
					tick = 20;
					millyTick = 20;
					GlStateManager.disableBlend();
				}
				break;
			}
			case 5: { // close the door
				this.drawMailBox(this.guiLeft, this.guiTop);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft - ((1.0f - cos) * 193.0f), this.guiTop, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 6;
					tick = 30;
					millyTick = 30;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.movement",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.75f + 0.25f * this.rnd.nextFloat());
					GlStateManager.disableBlend();
				}
				break;
			}
			case 6: { // box hidden
				this.drawMailBox(this.guiLeft, this.guiTop + cos * 236.0f);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop + cos * 236.0f, 2.0f);
				this.mc.getTextureManager().bindTexture(mDoor);
				this.drawTexturedModalRect(8, 44, 0, 0, 178, 158);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 0;
					tick = 30;
					millyTick = 30;
					if (this.closeType == 1) {
						NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, 0L, "", 1, 1);
					} else if (this.closeType == 2 && this.selected != null) {
						if (!this.selected.beenRead) {
							this.selected.beenRead = true;
							PlayerMail mail = ClientProxy.playerData.mailData.get(this.selected);
							if (mail != null) {
								mail.beenRead = true;
								ClientTickHandler.checkMails = true;
							}
							NoppesUtilPlayer.sendData(EnumPlayerPacket.MailRead, this.selected.timeWhenReceived,
									this.selected.sender);
						}
						NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, this.selected.timeWhenReceived,
								this.selected.sender, 0, 0, 0);
						this.selected = null;
						this.scroll.setSelect(-1);
					}
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
					this.close();
					return;
				}
				break;
			}
			}
			tick--;
		} else {
			this.drawMailBox(this.guiLeft, this.guiTop);
		}
		GlStateManager.popMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (step != 3 || this.hasSubGui() || !CustomNpcs.ShowDescriptions) {
			return;
		}
		List<String> hover = new ArrayList<>();
		if (this.scroll != null && this.scroll.hover > -1) {
			PlayerMail mail = this.scrollData.get(this.scroll.getList().get(this.scroll.hover));
			hover.add(((char) 167) + "7" + new TextComponentTranslation("mailbox.sender").getFormattedText()
					+ ((char) 167) + "7 \"" + ((char) 167) + "r" + mail.sender + ((char) 167) + "7\"");
			long timeWhenReceived = System.currentTimeMillis() - mail.timeWhenReceived - mail.timeWillCome;
			if (CustomNpcs.MailTimeWhenLettersWillBeDeleted > 0) {
				long timeToRemove = CustomNpcs.MailTimeWhenLettersWillBeDeleted * 86400000L - timeWhenReceived;
				if (timeToRemove < 0L) {
                    NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, mail.timeWhenReceived, mail.sender);
                    return;
				}
				hover.add(((char) 167) + "7"
						+ new TextComponentTranslation("mailbox.when.removed",
						Util.instance.ticksToElapsedTime(timeToRemove / 50, false, true, false))
										.getFormattedText());
			}
			if (mail.beenRead) {
				hover.add(((char) 167) + "a" + new TextComponentTranslation("mailbox.when.read").getFormattedText());
			} else {
				hover.add(((char) 167) + "7"
						+ new TextComponentTranslation("mailbox.when.received",
						Util.instance.ticksToElapsedTime(timeWhenReceived / 50, false, true, false))
										.getFormattedText());
			}
		}
		if (!hover.isEmpty()) {
			setHoverText(hover);
			drawHoverText(null);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		ClientTickHandler.checkMails = true;
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 154);
		}
		this.scroll.guiLeft = this.guiLeft + 9;
		this.scroll.guiTop = this.guiTop + 45;
		String select = this.scroll.getSelected();
		this.scrollData.clear();
		List<PlayerMail> listR = new ArrayList<>();
		List<PlayerMail> listN = new ArrayList<>();
		long time = System.currentTimeMillis();
		for (PlayerMail mail : ClientProxy.playerData.mailData.playerMails) {
			if (time - mail.timeWhenReceived < mail.timeWillCome) {
				continue;
			}
			if (mail.beenRead) {
				listR.add(mail);
			} else {
				listN.add(mail);
			}
		}
		listR.sort((o1, o2) -> {
            if (o1.timeWhenReceived == o2.timeWhenReceived) {
                return 0;
            } else {
                return (o1.timeWhenReceived > o2.timeWhenReceived) ? -1 : 1;
            }
        });
		List<String> list = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		List<IResourceData> prefixes = new ArrayList<>();
		int i = 1;
		for (PlayerMail mail : listN) {
			String key = ((char) 167) + "8" + i + ": " + ((char) 167) + "r\""
					+ new TextComponentTranslation(mail.title).getFormattedText() + "\"";
			list.add(key);
			this.scrollData.put(key, mail);
			ResourceData rd = new ResourceData(icons, mail.getRansom() > 0 ? 96 : mail.returned ? 128 : 0, 0, 32, 32);
			rd.tH = -3.0f;
			prefixes.add(rd);
			colors.add(CustomNpcs.LableColor.getRGB());
			i++;
		}
		for (PlayerMail mail : listR) {
			String key = ((char) 167) + "8" + i + ": " + ((char) 167) + "r\""
					+ new TextComponentTranslation(mail.title).getFormattedText() + "\"";
			list.add(key);
			this.scrollData.put(key, mail);
			boolean isEmpty = true;
			for (ItemStack stack : mail.items) {
				if (!stack.isEmpty()) {
					isEmpty = false;
					break;
				}
			}
			ResourceData rd = new ResourceData(icons, mail.getRansom() > 0 ? 96 : isEmpty ? 64 : 32, 0, 32, 32);
			rd.tH = -3.0f;
			prefixes.add(rd);
			colors.add(CustomNpcs.LableColor.getRGB());
			i++;
		}
		this.scroll.clear();
		this.scroll.setListNotSorted(list);
		this.scroll.setPrefixes(prefixes);
		this.scroll.setColors(colors);
		this.scroll.colorBack = new Color(0x00000000).getRGB();
		if (select != null && !select.isEmpty()) {
			this.scroll.setSelected(select);
		}
		this.addScroll(this.scroll);
		String title = new TextComponentTranslation("mailbox.name").getFormattedText();
		int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop + 11));
		this.getLabel(0).setColor(CustomNpcs.MainColor.getRGB());
		x = this.guiLeft + 8;
		int y = this.guiTop + 202;

		GuiNpcButton button = new GuiNpcButton(0, x, y, 58, 14, "mailbox.read");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("mailbox.hover.read");
		button.setEnabled(selected != null);
		addButton(button);

		button = new GuiNpcButton(1, x + 59, y, 58, 14, "mailbox.write");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("mailbox.hover.write");
		addButton(button);

		button = new GuiNpcButton(2, x + 118, y, 58, 14, "gui.remove");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("mailbox.hover.del");
		button.setEnabled(selected != null);
		addButton(button);

		button = new GuiNpcButton(3, x, y += 16, 58, 14, "gui.remove.all");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("mailbox.hover.delall");
		button.setEnabled(!list.isEmpty());
		addButton(button);

		button = new GuiNpcButton(4, x + 59, y, 58, 14, "gui.clear");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("mailbox.hover.clear");
		button.setEnabled(!list.isEmpty());
		addButton(button);

		button = new GuiNpcButton(5, x + 118, y, 58, 14, "display.hover.X");
		button.texture = icons;
		button.txrY = 96;
		button.setHoverText("hover.exit");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (step == 3 && i == 1 || this.isInventoryKey(i)) {
			step = 4;
			tick = 15;
			millyTick = 15;
			this.closeType = 0;
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		this.selected = this.scrollData.get(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (this.selected == null) {
			return;
		}
		GuiMailmanWrite.parent = this;
		GuiMailmanWrite.mail = this.selected;
		step = 4;
		tick = 15;
		millyTick = 15;
		this.closeType = 2;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		ClientProxy.playerData.mailData.loadNBTData(compound);
		this.selected = null;
		this.initGui();
	}

}
