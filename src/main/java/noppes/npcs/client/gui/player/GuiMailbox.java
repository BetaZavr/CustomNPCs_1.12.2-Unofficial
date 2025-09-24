package noppes.npcs.client.gui.player;

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
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiMailbox extends GuiNPCInterface
		implements IGuiData, ICustomScrollListener, GuiYesNoCallback {

	protected static final ResourceLocation mBox = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_empty.png");
	protected static final ResourceLocation mDoor = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_door.png");
	protected static final ResourceLocation mList = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/box_list.png");
	public static final ResourceLocation icons = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/icons.png");

	protected final Map<String, PlayerMail> scrollData = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected PlayerMail selected;
	// Animations
	protected int closeType;
	protected int step;
	protected int tick;
	protected int millyTick;
	protected final Random rnd = new Random();

	public GuiMailbox() {
		super();
		xSize = 192;
		ySize = 236;

		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailGet);
		ClientTickHandler.checkMails = true;
		// Animations
		tick = 30;
		millyTick = 30;
		step = 0;
		closeType = 0;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		GuiMailmanWrite.parent = this;
		switch (button.getID()) {
			case 0: {
				if (selected == null) { return; }
				GuiMailmanWrite.mail = selected;
				step = 4;
				tick = 15;
				millyTick = 15;
				closeType = 2;
				break;
			}
			case 1: {
				step = 4;
				tick = 15;
				millyTick = 15;
				closeType = 1;
				break;
			}
			case 2: {
				if (selected == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 3: {
				if (ClientProxy.playerData.mailData.playerMails.isEmpty()) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("mailbox.name").getFormattedText() + ":", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
				displayGuiScreen(guiyesno);
				break;
			}
			case 4: {
				if (ClientProxy.playerData.mailData.playerMails.isEmpty()) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("mailbox.name").getFormattedText() + ":", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				displayGuiScreen(guiyesno);
				break;
			}
			case 5: {
				step = 4;
				tick = 15;
				millyTick = 15;
				closeType = 0;
				break;
			}
		}
	}

	public void confirmClicked(boolean flag, int id) {
		NoppesUtil.openGUI(player, this);
		if (!flag) { return; }
		if (id == 0 && selected != null) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, selected.timeWhenReceived, selected.sender);
			selected = null;
		} else if (id == 1) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, 0L);
			selected = null;
		} else if (id == 2) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, -1L);
			selected = null;
		}
		MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.delete",
				(float) player.posX, (float) player.posY, (float) player.posZ, 1.0f,
				0.9f + 0.2f * rnd.nextFloat());
	}

	private void drawMailBox(float u, float v) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(u, v, 0.0f);
		mc.getTextureManager().bindTexture(mBox);
		drawTexturedModalRect(0, 0, 0, 0, 192, 236); // Box
		if (!scrollData.isEmpty()) {
			mc.getTextureManager().bindTexture(mList);
			drawTexturedModalRect(8, 45, 0, 0, 176, 156); // list
		}
		if (step == 3) {
			mc.getTextureManager().bindTexture(mDoor);
			drawTexturedModalRect(-5, 44, 181, 0, 7, 158); // door
		}
		GlStateManager.popMatrix();
		if (scroll != null) {
			scroll.guiLeft = (int) u + 9;
			scroll.guiTop = (int) v + 45;
		}
		if (getLabel(0) != null && getLabel(0).enabled) {
			GuiNpcLabel l = getLabel(0);
			l.x = (int) u + 95 - (l.width / 2);
			l.y = (int) v + 11;
		}
		for (int i = 0; i < 6; i++) {
			if (getButton(i) == null) { return; }
			GuiNpcButton b = getButton(i);
			b.setIsEnable(step == 3);
			switch (i) {
				case 0: {
					b.x = (int) u + 8;
					b.y = (int) v + 202;
					b.setIsEnable(step == 3 && selected != null);
					break;
				} // read
				case 1: {
					b.x = (int) u + 67;
					b.y = (int) v + 202;
					b.setIsEnable(step == 3);
					break;
				} // write
				case 2: {
					b.x = (int) u + 126;
					b.y = (int) v + 202;
					b.setIsEnable(step == 3 && selected != null);
					break;
				} // remove
				case 3: {
					b.x = (int) u + 8;
					b.y = (int) v + 218;
					b.setIsEnable(step == 3 && scroll != null && !scroll.getList().isEmpty());
					break;
				} // remove all
				case 4: {
					b.x = (int) u + 67;
					b.y = (int) v + 218;
					b.setIsEnable(step == 3 && scroll != null && !scroll.getList().isEmpty());
					break;
				} // clear
				case 5: {
					b.x = (int) u + 126;
					b.y = (int) v + 218;
					b.setIsEnable(step == 3);
					break;
				} // exit
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Animations
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (tick >= 0) {
			if (tick == 0) { partialTicks = 0.0f; }
			float part = (float) tick + partialTicks;
			float cos = (float) Math.cos(90.0d * part / (double) millyTick * Math.PI / 180.0d);
			if (cos < 0.0f) { cos = 0.0f; }
			else if (cos > 1.0f) { cos = 1.0f; }
			switch (step) {
				case 0: {
					if (tick == millyTick) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.movement",
								(float) player.posX, (float) player.posY, (float) player.posZ, 1.0f,
								0.75f + 0.25f * rnd.nextFloat());
					}
					drawMailBox(guiLeft, guiTop + (1.0f - cos) * 236.0f);
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTop + (1.0f - cos) * 236.0f, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 1;
						tick = 20;
						millyTick = 20;
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.open.door",
								(float) player.posX, (float) player.posY, (float) player.posZ, 1.0f,
								0.75f + 0.25f * rnd.nextFloat());
						GlStateManager.disableBlend();
					}
					break;
				} // box appears
				case 1: {
					drawMailBox(guiLeft, guiTop);
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - (cos * 193.0f), guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 2;
						tick = 15;
						millyTick = 15;
						GlStateManager.disableBlend();
					}
					break;
				} // opening the door
				case 2: {
					drawMailBox(guiLeft, guiTop);
					float s = 1.0f - cos;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - 7.0f - (186.0f) * s, guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					GlStateManager.scale(s, 1.0f, 1.0f);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					drawTexturedModalRect(183, 44, 178, 0, 3, 158);
					GlStateManager.popMatrix();
					s = cos;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - 7.0f, guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					GlStateManager.scale(s, 1.0f, 1.0f);
					drawTexturedModalRect(0, 44, 181, 0, 7, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 3;
						millyTick = 0;
						GlStateManager.disableBlend();
					}
					break;
				} // turning the door
				case 4: {
					if (tick == millyTick) {
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS,
								CustomNpcs.MODID + ":mail.close.door", (float) player.posX, (float) player.posY,
								(float) player.posZ, 1.0f, 0.75f + 0.25f * rnd.nextFloat());
					}
					drawMailBox(guiLeft, guiTop);
					float s = cos;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - 7.0f - (186.0f) * s, guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					GlStateManager.scale(s, 1.0f, 1.0f);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					drawTexturedModalRect(183, 44, 178, 0, 3, 158);
					GlStateManager.popMatrix();
					s = 1.0f - cos;
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - 7.0f, guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					GlStateManager.scale(s, 1.0f, 1.0f);
					drawTexturedModalRect(0, 44, 181, 0, 7, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 5;
						tick = 20;
						millyTick = 20;
						GlStateManager.disableBlend();
					}
					break;
				} // back turning the door
				case 5: {
					drawMailBox(guiLeft, guiTop);
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft - ((1.0f - cos) * 193.0f), guiTop, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 6;
						tick = 30;
						millyTick = 30;
						MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.movement",
								(float) player.posX, (float) player.posY, (float) player.posZ, 1.0f,
								0.75f + 0.25f * rnd.nextFloat());
						GlStateManager.disableBlend();
					}
					break;
				} // close the door
				case 6: {
					drawMailBox(guiLeft, guiTop + cos * 236.0f);
					GlStateManager.pushMatrix();
					GlStateManager.translate(guiLeft, guiTop + cos * 236.0f, 2.0f);
					mc.getTextureManager().bindTexture(mDoor);
					drawTexturedModalRect(8, 44, 0, 0, 178, 158);
					GlStateManager.popMatrix();
					if (tick == 0) {
						step = 0;
						tick = 30;
						millyTick = 30;
						if (closeType == 1) {
							NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, 0L, "", 1, 1);
						} else if (closeType == 2 && selected != null) {
							if (!selected.beenRead) {
								selected.beenRead = true;
								PlayerMail mail = ClientProxy.playerData.mailData.get(selected);
								if (mail != null) {
									mail.beenRead = true;
									ClientTickHandler.checkMails = true;
								}
								NoppesUtilPlayer.sendData(EnumPlayerPacket.MailRead, selected.timeWhenReceived, selected.sender);
							}
							NoppesUtilPlayer.sendData(EnumPlayerPacket.MailboxOpenMail, selected.timeWhenReceived, selected.sender, 0, 0, 0);
							selected = null;
							scroll.setSelect(-1);
						}
						GlStateManager.disableBlend();
						GlStateManager.popMatrix();
						onClosed();
						return;
					}
					break;
				} // box hidden
			}
			tick--;
		}
		else { drawMailBox(guiLeft, guiTop); }
		GlStateManager.popMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (step != 3 || hasSubGui() || !CustomNpcs.ShowDescriptions) { return; }
		List<String> hover = new ArrayList<>();
		if (scroll != null && scroll.getSelect() > -1) {
			PlayerMail mail = scrollData.get(scroll.getList().get(scroll.getSelect()));
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
			putHoverText(hover);
			drawHoverText(null);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		ClientTickHandler.checkMails = true;
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(165, 154); }
		scroll.guiLeft = guiLeft + 9;
		scroll.guiTop = guiTop + 45;
		String select = scroll.getSelected();
		scrollData.clear();
		List<PlayerMail> listR = new ArrayList<>();
		List<PlayerMail> listN = new ArrayList<>();
		long time = System.currentTimeMillis();
		for (PlayerMail mail : ClientProxy.playerData.mailData.playerMails) {
			if (time - mail.timeWhenReceived < mail.timeWillCome) { continue; }
			if (mail.beenRead) { listR.add(mail); }
			else { listN.add(mail); }
		}
		listR.sort((o1, o2) -> {
            if (o1.timeWhenReceived == o2.timeWhenReceived) { return 0; }
			else { return (o1.timeWhenReceived > o2.timeWhenReceived) ? -1 : 1; }
        });
		List<String> list = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		List<ResourceData> prefixes = new ArrayList<>();
		int i = 1;
		for (PlayerMail mail : listN) {
			String key = ((char) 167) + "8" + i + ": " + ((char) 167) + "r\"" + new TextComponentTranslation(mail.title).getFormattedText() + "\"";
			list.add(key);
			scrollData.put(key, mail);
			ResourceData rd = new ResourceData(icons, mail.getRansom() > 0 ? 96 : mail.returned ? 128 : 0, 0, 32, 32);
			rd.tH = -3.0f;
			prefixes.add(rd);
			colors.add(CustomNpcs.LableColor.getRGB());
			i++;
		}
		for (PlayerMail mail : listR) {
			String key = ((char) 167) + "8" + i + ": " + ((char) 167) + "r\"" + new TextComponentTranslation(mail.title).getFormattedText() + "\"";
			list.add(key);
			scrollData.put(key, mail);
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
		scroll.clear();
		scroll.setUnsortedList(list).setPrefixes(prefixes).setColors(colors);
		scroll.colorBackS = 0x00000000;
		scroll.colorBackE = 0x00000000;
		if (select != null && !select.isEmpty()) { scroll.setSelected(select); }
		addScroll(scroll);
		String title = new TextComponentTranslation("mailbox.name").getFormattedText();
		int x = (xSize - fontRenderer.getStringWidth(title)) / 2;
		addLabel(new GuiNpcLabel(0, title, guiLeft + x, guiTop + 11));
		getLabel(0).setColor(CustomNpcs.MainColor.getRGB());
		x = guiLeft + 8;
		int y = guiTop + 202;
		addButton(new GuiNpcButton(0, x, y, 58, 14, "mailbox.read")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("mailbox.hover.read")
				.setIsEnable(selected != null));
		addButton(new GuiNpcButton(1, x + 59, y, 58, 14, "mailbox.write")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("mailbox.hover.write"));
		addButton(new GuiNpcButton(2, x + 118, y, 58, 14, "gui.remove")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("mailbox.hover.del")
				.setIsEnable(selected != null));
		addButton(new GuiNpcButton(3, x, y += 16, 58, 14, "gui.remove.all")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("mailbox.hover.delall")
				.setIsEnable(!list.isEmpty()));
		addButton(new GuiNpcButton(4, x + 59, y, 58, 14, "gui.clear")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("mailbox.hover.clear")
				.setIsEnable(!list.isEmpty()));
		addButton(new GuiNpcButton(5, x + 118, y, 58, 14, "display.hover.X")
				.setTexture(icons)
				.setUV(0, 96, 0, 0)
				.setHoverText("hover.exit"));
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui == null && step == 3 && (keyCode == Keyboard.KEY_ESCAPE || isInventoryKey(keyCode))) {
			step = 4;
			tick = 15;
			millyTick = 15;
			closeType = 0;
			return true;
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		selected = scrollData.get(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (selected == null) { return; }
		GuiMailmanWrite.parent = this;
		GuiMailmanWrite.mail = selected;
		step = 4;
		tick = 15;
		millyTick = 15;
		closeType = 2;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		ClientProxy.playerData.mailData.loadNBTData(compound);
		selected = null;
		initGui();
	}

}
