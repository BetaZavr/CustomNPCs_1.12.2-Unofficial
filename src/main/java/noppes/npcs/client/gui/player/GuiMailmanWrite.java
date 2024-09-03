package noppes.npcs.client.gui.player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.Util;

@SideOnly(Side.CLIENT)
public class GuiMailmanWrite extends GuiContainerNPCInterface implements ITextfieldListener, ITextChangeListener, IGuiError, IGuiClose, GuiYesNoCallback {

	public static final ResourceLocation icons = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/icons.png");
	private static final ResourceLocation mEnvelope = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/envelope.png");
	private static final ResourceLocation mList = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/list.png");
	private static final ResourceLocation mTable = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/table.png");
	private static final ResourceLocation mSbox = new ResourceLocation(CustomNpcs.MODID, "textures/gui/mail/send_box.png");
	private static final ResourceLocation widgets = new ResourceLocation("textures/gui/widgets.png");

	public static PlayerMail mail = new PlayerMail();
	public static GuiScreen parent;
	private GuiButtonNextPage buttonNextPage;
	private GuiButtonNextPage buttonPreviousPage;
	private NBTTagList bookPages;
	private GuiNpcLabel error;

	private final boolean canEdit;
    private final boolean canSend;
    private boolean hasStacks;
    private boolean hasSend;
	private int bookTotalPages, currPage, updateCount, type;
	private long totalCost;
	private final Map<Integer, Long> cost = Maps.newTreeMap();
	private String username;

	// Animations
	int step, tick, mtick, aType;
	long errTick;
	private final Random rnd = new Random();

	public GuiMailmanWrite(ContainerMail container, boolean canEdit, boolean canSend) {
		super(null, container);
		this.bookTotalPages = 1;
		this.hasSend = false;
		this.mc = Minecraft.getMinecraft();
		this.username = "";
		this.title = "";
		this.canEdit = canEdit;
		this.canSend = canSend;
		this.type = 0;
		this.errTick = 0;
		if (GuiMailmanWrite.mail.message.hasKey("pages")) {
			this.bookPages = GuiMailmanWrite.mail.message.getTagList("pages", 8);
		}
		if (this.bookPages != null) {
			this.bookPages = this.bookPages.copy();
			this.bookTotalPages = this.bookPages.tagCount();
			if (this.bookTotalPages < 1) {
				this.bookTotalPages = 1;
			}
		} else {
			(this.bookPages = new NBTTagList()).appendTag(new NBTTagString(""));
			this.bookTotalPages = 1;
		}
		this.xSize = 306;
		this.ySize = 248;
		this.drawDefaultBackground = false;
		this.closeOnEsc = true;
		ClientTickHandler.checkMails = true;
		this.tick = 30;
		this.mtick = 30;
		this.step = 0;
	}

	private void addNewPage() {
		if (this.bookPages != null && this.bookPages.tagCount() < 50) {
			this.bookPages.appendTag(new NBTTagString(""));
			++this.bookTotalPages;
		}
	}

	private void addString(String str) {
		String text = this.getText();
		String totalText = text + str;
		int textHeight = this.mc.fontRenderer.getWordWrappedHeight(totalText, 152);
		if (textHeight <= 108) {
			this.setText(totalText);
		}
	}

	private void animClose() {
		if (step != 5) {
			return;
		}
		step = 6;
		tick = 5;
		mtick = 5;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (!button.enabled) {
			return;
		}
		switch (button.id) {
		case 0: { // send
			GuiMailmanWrite.mail.message.setTag("pages", this.bookPages);
			if (this.canSend) {
				if (!this.hasSend) {
					this.hasSend = true;
					NoppesUtilPlayer.sendData(EnumPlayerPacket.MailSend, this.username, this.totalCost,
							GuiMailmanWrite.mail.writeNBT());
				}
			} else {
				aType = 0;
				this.animClose();
			}
			break;
		}
		case 1: {
			if (this.currPage < this.bookTotalPages - 1) {
				++this.currPage;
			} else if (this.canEdit) {
				this.addNewPage();
				if (this.currPage < this.bookTotalPages - 1) {
					++this.currPage;
				}
			}
			break;
		}
		case 2: {
			if (this.currPage > 0) {
				--this.currPage;
			}
			break;
		}
		case 3: { // back/exit
			aType = 0;
			this.animClose();
			break;
		}
		case 4: { // delete
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			this.displayGuiScreen(guiyesno);
			break;
		}
		case 6: { // ransom
			if (GuiMailmanWrite.mail.ransom > 0) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.MailRansom, GuiMailmanWrite.mail.timeWhenReceived);
			} else if (GuiMailmanWrite.mail.money > 0) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.MailTakeMoney, GuiMailmanWrite.mail.timeWhenReceived);
			}
			break;
		}
		case 7: { // return letter
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailReturn, GuiMailmanWrite.mail.timeWhenReceived);
			ClientProxy.playerData.mailData.playermail.remove(GuiMailmanWrite.mail);
			aType = 1;
			this.animClose();
			break;
		}
		}
		this.updateButtons();
	}

	@Override
	public void close() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CloseGui);
		ClientTickHandler.checkMails = true;
		this.mc.displayGuiScreen(GuiMailmanWrite.parent);
		if (GuiMailmanWrite.parent != null) {
			GuiMailmanWrite.parent.initGui();
		}
		GuiMailmanWrite.parent = null;
		GuiMailmanWrite.mail = new PlayerMail();
	}

	public void confirmClicked(boolean flag, int id) {
		NoppesUtil.openGUI(this.player, this);
		if (!flag) {
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, GuiMailmanWrite.mail.timeWhenReceived, GuiMailmanWrite.mail.sender);
		ClientProxy.playerData.mailData.playermail.remove(GuiMailmanWrite.mail);
		aType = 2;
		this.animClose();
	}

	private void drawPlace(float u, float v, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(u, v, 0.0f);
		this.mc.renderEngine.bindTexture(mTable);
		this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
		GlStateManager.popMatrix();
		if (step == 5) {
			// envelope
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 142.0f, this.guiTop - 10.0f, 0.0f);
			this.mc.renderEngine.bindTexture(mEnvelope);
			this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
			GlStateManager.popMatrix();
			// list
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 9.0f, this.guiTop + 12.0f, 0.0f);
			this.mc.renderEngine.bindTexture(mList);
			this.drawTexturedModalRect(0, 0, 0, 0, 164, 134);
			GlStateManager.popMatrix();
			// handle
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 0.0f);
			this.mc.renderEngine.bindTexture(mTable);
			this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
			GlStateManager.popMatrix();
			// box
			if (hasStacks || this.canEdit) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 180.0f, this.guiTop + 179.0f, 0.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 0, 74, 54);
				GlStateManager.popMatrix();
			}
			// slots
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 6.0f, this.guiTop + 167.0f, 0.0f);
			this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			for (int j = 0; j < 9; j++) {
				for (int k = 0; k < 4; k++) {
					this.drawTexturedModalRect(18 * j, k * 18 + (k == 3 ? 2 : 0), 0, 0, 18, 18);
				}
			}
			GlStateManager.popMatrix();
			for (int slotId = 0; slotId < this.inventorySlots.inventorySlots.size(); slotId++) {
				Slot slot = this.inventorySlots.getSlot(slotId);
                if (slotId < 4) {
					boolean show = !this.canEdit && !this.canSend ? mail.ransom <= 0 && slot.getHasStack()
							: this.canEdit;
					if (show && slot.xPos == 250000) {
						slot.xPos = 197 + (slotId % 2) * 23;
						slot.yPos = 187 + (slotId / 2) * 23;
					} else if (!show && slot.xPos != 250000) {
						slot.xPos = 250000;
						slot.yPos = 250000;
					}
				} else if (slot.xPos == 250000) { // Inventory
					slot.xPos = 7 + ((slotId - 4) % 9) * 18;
					slot.yPos = 168 + ((slotId - 4) / 9) * 18 + (slotId >= 31 ? 2 : 0);
				}
			}
			int x = this.guiLeft + 170, y = this.guiTop + 48;
			// player name
			GuiNpcLabel l = this.getLabel(0);
			GuiNpcTextField tf;
			GuiNpcButton b;
			if (l != null && l.x == 250000) {
				l.x = x;
				l.y = y;
			}
			if (this.canEdit) {
				tf = this.getTextField(!this.canSend ? 2 : 0);
				if (tf != null && tf.x == 250000) {
					tf.x = x;
					tf.y = (y += 10);
				}
			} else {
				l = this.getLabel(10);
				if (l != null && l.x == 250000) {
					l.x = x + 2;
					l.y = (y += 10);
				}
			}
			// title
			l = this.getLabel(1);
			if (l != null && l.x == 250000) {
				l.x = x;
				l.y = (y += 18);
			}
			if (this.canEdit) {
				tf = this.getTextField(1);
				if (tf != null && tf.x == 250000) {
					tf.x = x;
					tf.y = (y += 10);
				}
			} else {
				l = this.getLabel(11);
				if (l != null && l.x == 250000) {
					l.x = x;
					l.y = (y += 10);
				}
			}
			// ransom
			if (this.getLabel(7) != null) {
				this.getLabel(7).backColor = 0;
			}
			if (this.getLabel(8) != null) {
				this.getLabel(8).backColor = 0;
			}
			if (this.getButton(6) != null) {
				this.getButton(6).layerColor = 0;
				if (!this.canEdit && !this.canSend) {
					if (mail.ransom > 0) {
						this.getButton(6).enabled = this.player.capabilities.isCreativeMode
								|| ClientProxy.playerData.game.getMoney() >= mail.ransom;
					} else {
						this.getButton(6).enabled = mail.money > 0;
					}
				}
			}
			if (!this.canEdit) {
				if (GuiMailmanWrite.mail.ransom > 0 || GuiMailmanWrite.mail.money > 0) {
					l = this.getLabel(7);
					if (l != null && l.x == 250000) {
						l.x = x;
						l.y = y + 18;
					}
					l = this.getLabel(8);
					if (l != null && l.x == 250000) {
						l.x = x + 2;
						l.y = y + 28;
					}
				}
			}
			l = this.error;
			if (l != null && l.x == 250000) {
				l.x = x;
				l.y = this.guiTop + 145;
			}
			// Moneys
			if (this.canEdit) {
				l = this.getLabel(3);
				if (l != null && l.x == 250000) {
					l.x = x;
					l.y = (y += 19) + 4;
				}
				l = this.getLabel(6);
				if (l != null && l.x == 250000) {
					l.x = x + 102;
					l.y = y + 4;
				}
				GuiNpcTextField tf3 = this.getTextField(3);
				GuiNpcTextField tf4 = this.getTextField(4);
				if (tf3 != null) {
					if (tf3.x == 250000) {
						tf3.x = x + 48;
						tf3.y = y;
					}
					int tfV = tf3.isEmpty() ? (int) tf3.def : tf3.isInteger() ? tf3.getInteger() : 0;
					if (tfV > 0) {
						if (tf4 != null) {
							tf4.setVisible(false);
						}
						if (this.getLabel(7) != null) {
							this.getLabel(7).enabled = false;
						}
						if (this.getLabel(8) != null) {
							this.getLabel(8).enabled = false;
						}
					} else {
						if (tf4 != null) {
							tf4.setVisible(true);
						}
						if (this.getLabel(7) != null) {
							this.getLabel(7).enabled = true;
						}
						if (this.getLabel(8) != null) {
							this.getLabel(8).enabled = true;
						}
					}
				}
				l = this.getLabel(7);
				if (l != null && l.x == 250000) {
					l.x = x;
					l.y = (y += 19) + 4;
				}
				l = this.getLabel(8);
				if (l != null && l.x == 250000) {
					l.x = x + 102;
					l.y = y + 4;
				}
				if (tf4 != null) {
					if (tf4.x == 250000) {
						tf4.x = x + 48;
						tf4.y = y;
					}
					int tfV = tf4.isEmpty() ? (int) tf4.def : tf4.isInteger() ? tf4.getInteger() : 0;
					if (tfV > 0) {
						if (tf3 != null) {
							tf3.setVisible(false);
						}
						if (this.getLabel(3) != null) {
							this.getLabel(3).enabled = false;
						}
						if (this.getLabel(6) != null) {
							this.getLabel(6).enabled = false;
						}
					} else {
						if (tf3 != null) {
							tf3.setVisible(true);
						}
						if (this.getLabel(3) != null) {
							this.getLabel(3).enabled = true;
						}
						if (this.getLabel(6) != null) {
							this.getLabel(6).enabled = true;
						}
					}
				}
			}
			x = this.guiLeft + 7;
			y = this.guiTop + 149;
			if (this.canEdit && !this.canSend) { // dialog/quest add to
				b = this.getButton(0);
				if (b != null && b.x == 250000) {
					b.x = x + 52;
					b.y = y;
				}
			} else if (this.canEdit) { // write
				b = this.getButton(0);
				if (b != null && b.x == 250000) {
					b.x = x + 52;
					b.y = y;
				}
			}
			if (!this.canEdit && !this.canSend) { // read -> delete
				if (GuiMailmanWrite.mail.ransom > 0 || GuiMailmanWrite.mail.money > 0) {
					b = this.getButton(6);
					if (b != null && b.x == 250000) {
						b.x = x + 220;
						b.y = y - 42;
					}
				}
				b = this.getButton(4);
				if (b != null && b.x == 250000) {
					b.x = x;
					b.y = y;
				}
				b = this.getButton(7);
				if (b != null && b.x == 250000) {
					b.x = x + 52;
					b.y = y;
				}
			}
			if (!this.canEdit || this.canSend) { // write -> cancel
				b = this.getButton(3);
				if (b != null && b.x == 250000) {
					b.x = x + 104;
					b.y = y;
				}
			}
			if (this.buttonNextPage != null && this.buttonNextPage.x == 250000 && (this.canEdit || mail.ransom == 0)) {
				this.buttonNextPage.x = x + 135;
				this.buttonNextPage.y = y - 16;
			}
			if (this.buttonPreviousPage != null && this.buttonPreviousPage.x == 250000) {
				this.buttonPreviousPage.x = x;
				this.buttonPreviousPage.y = y - 16;
			}
			// Text
			String s = (this.currPage + 1) + "/" + this.bookTotalPages;
			String totalText = "", drawEnd = "";
			if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
				totalText = this.bookPages.getStringTagAt(this.currPage);
			}
			if (this.canEdit) {
				if (this.mc.fontRenderer.getWordWrappedHeight(totalText + TextFormatting.BLACK + "_", 152) > 108) {
					if (this.updateCount / 6 % 2 == 0) {
						drawEnd = TextFormatting.BLACK + "_";
					} else {
						drawEnd = TextFormatting.GRAY + "_";
					}
				} else if (this.mc.fontRenderer.getBidiFlag()) {
					totalText += "_";
				} else if (this.updateCount / 6 % 2 == 0) {
					totalText = totalText + TextFormatting.BLACK + "_";
				} else {
					totalText = totalText + TextFormatting.GRAY + "_";
				}
			} else if (mail.ransom > 0) {
				StringBuilder newText = new StringBuilder();
				char g = ((char) 167);
				boolean rnd = false;
				for (int i = 0; i < totalText.length(); i++) {
					char c = totalText.charAt(i);
					if (this.mc.fontRenderer.getWordWrappedHeight(newText + "" + c, 152) > 9) {
						newText.append(g).append("k");
						rnd = true;
					}
					if (!rnd || c == ((char) 10) || c == ' ') {
						newText.append(c);
					} else {
						newText.append(' ');
					}
				}
				totalText = newText.toString();
			}
			int ls = this.mc.fontRenderer.getStringWidth(s);
			this.mc.fontRenderer.drawString(s, this.guiLeft + 163 - ls, this.guiTop + 18, 0);
			if (!drawEnd.isEmpty()) {
				this.mc.fontRenderer.drawString(drawEnd, this.guiLeft + 159, this.guiTop + 127, 0);
			}
			this.mc.fontRenderer.drawSplitString(totalText, this.guiLeft + 12, this.guiTop + 28, 152, 0);
			if (!this.canEdit && !this.canSend && mail.ransom > 0) {
				this.drawGradientRect(this.guiLeft + 11, this.guiTop + 36, this.guiLeft + 164, this.guiTop + 144,
						0x40000000, 0x40000000);
				if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 11, this.guiTop + 36, 152, 108)) {
					List<String> list = Lists
							.newArrayList(new TextComponentTranslation("mailbox.hover.ransom.sell").getFormattedText());
					this.hoverText = list.toArray(new String[0]);
					this.getLabel(7).backColor = 0x80FF0000;
					this.getLabel(8).backColor = 0x80FF0000;
					this.getButton(6).layerColor = 0xFFF00000;
				}
			}
			// add slots
			for (int slotId = 0; slotId < 4; slotId++) {
				Slot slot = this.inventorySlots.getSlot(slotId);
                boolean show = !this.canEdit && !this.canSend ? slot.getHasStack() : slot.xPos != 250000;
				if (show) {
					int px = this.guiLeft + 193 + 23 * (slotId % 2);
					int py = this.guiTop + 183 + 23 * (slotId / 2);
					GlStateManager.pushMatrix();
					GlStateManager.translate(px, py, 0.0f);
					this.drawGradientRect(3, 3, 21, 21, 0xC0101010, 0xD0101010);
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.mc.getTextureManager().bindTexture(widgets);
					this.drawTexturedModalRect(0, 0, 0, 22, 24, 24);
					GlStateManager.popMatrix();
					if (!this.canEdit && !this.canSend && mail.ransom > 0) {
						GlStateManager.pushMatrix();
						GlStateManager.translate(px + 4, py + 4, 0.0f);
						RenderHelper.enableGUIStandardItemLighting();
						this.mc.getRenderItem().renderItemAndEffectIntoGUI(slot.getStack(), 0, 0);
						GlStateManager.translate(0.0f, 0.0f, 200.0f);
						int count = slot.getStack().getCount();
						this.drawString(this.mc.fontRenderer, "" + count,
								17 - this.mc.fontRenderer.getStringWidth("" + count), 9, 0xFFFFFFFF);
						if (this.isMouseHover(mouseX, mouseY, px, py, 18, 18)) {
							List<String> list = Lists.newArrayList();
							list.add(new TextComponentTranslation("mailbox.hover.ransom.sell").getFormattedText());
							list.addAll(slot.getStack().getTooltip(this.mc.player,
									this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED
											: TooltipFlags.NORMAL));
							this.hoverText = list.toArray(new String[0]);
							this.getLabel(7).backColor = 0x80FF0000;
							this.getLabel(8).backColor = 0x80FF0000;
							this.getButton(6).layerColor = 0xFFF00000;
						}
						this.mc.renderEngine.bindTexture(icons);
						this.drawTexturedModalRect(-2, -2, 0, 32, 20, 20);
						GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
						GlStateManager.popMatrix();
					}
				}
			}

		} else {
			for (int slotId = 0; slotId < this.inventorySlots.inventorySlots.size(); slotId++) {
				Slot slot = this.inventorySlots.getSlot(slotId);
				if (slot.xPos == 250000) {
					continue;
				}
				slot.xPos = 250000;
				slot.yPos = 250000;
			}
			if (this.buttonNextPage != null && this.buttonNextPage.x != 250000) {
				this.buttonNextPage.x = 250000;
				this.buttonNextPage.y = 250000;
			}
			if (this.buttonPreviousPage != null && this.buttonPreviousPage.x != 250000) {
				this.buttonPreviousPage.x = 250000;
				this.buttonPreviousPage.y = 250000;
			}
			for (int i = 0; i < 12; i++) {
				GuiNpcLabel l = this.getLabel(i);
				if (l != null && l.x != 250000) {
					l.x = 250000;
					l.y = 250000;
				}
				GuiNpcButton b = this.getButton(i);
				if (b != null && b.x != 250000) {
					b.x = 250000;
					b.y = 250000;
				}
				GuiNpcTextField tf = this.getTextField(i);
				if (tf != null && tf.x != 250000) {
					tf.x = 250000;
					tf.y = 250000;
				}
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawWorldBackground(0);
		if (this.errTick != 0L && this.errTick < this.mc.world.getTotalWorldTime()) {
			this.errTick = 0L;
			if (this.error != null) {
				this.error.setLabel(null);
			}
		}
		// Info
		if (!this.canEdit && GuiMailmanWrite.mail.ransom > 0) {
			this.mc.renderEngine.bindTexture(icons);
			this.drawTexturedModalRect(this.guiLeft + 33, this.guiTop + 43, 0, 126, 120, 130);
			this.mc.fontRenderer.drawString(
					new TextComponentTranslation("mailbox.hover.ransom.sell").getFormattedText(), this.guiLeft + 36,
					this.guiTop + 100, 0xFFFFFFFF);
		}
		boolean hasMail = false;
		this.cost.clear();
		long c = CustomNpcs.MailCostSendingLetter[0];
		this.cost.put(0, c);
		this.totalCost = c;
		if (this.bookPages != null) {
			c = 0;
			for (int i = 0; i < this.bookPages.tagCount(); ++i) {
				if (this.bookPages.getStringTagAt(i).isEmpty()) {
					continue;
				}
				c += CustomNpcs.MailCostSendingLetter[1];
				hasMail = true;
			}
			this.cost.put(1, c);
			this.totalCost += c;
		}
		c = 0;
		hasStacks = false;
		for (int i = 0; i < 4; i++) {
			Slot slot = this.inventorySlots.getSlot(i);
			if (slot.getStack().isEmpty()) {
				continue;
			}
			c += (int) ((float) CustomNpcs.MailCostSendingLetter[2] * (float) slot.getStack().getCount()
					/ (float) slot.getStack().getMaxStackSize());
			hasStacks = true;
			hasMail = true;
		}
		this.cost.put(3, c);
		this.totalCost += c;
		if (GuiMailmanWrite.mail.money > 0) {
			c = (long) ((float) GuiMailmanWrite.mail.money * (float) CustomNpcs.MailCostSendingLetter[3] / 100.0f);
			this.cost.put(2, c);
			this.totalCost += c;
			this.totalCost += GuiMailmanWrite.mail.money;
			hasMail = true;
		}
		if (GuiMailmanWrite.mail.ransom > 0) {
			c = (long) (int) ((float) GuiMailmanWrite.mail.ransom * (float) CustomNpcs.MailCostSendingLetter[4]
					/ 100.0f);
			this.cost.put(4, c);
			this.totalCost += c;
		}
		if (this.getLabel(5) != null) {
			this.getLabel(5)
					.setLabel(new TextComponentTranslation("mailbox.cost.send",
							"" + (this.totalCost == 0L ? 0
									: Util.instance.getTextReducedNumber(this.totalCost, true, false, false)),
							CustomNpcs.displayCurrencies).getFormattedText());
		}
		if (this.canEdit && this.canSend && this.getButton(0) != null) {
			this.type = 0;
			this.type = !this.player.capabilities.isCreativeMode && this.username.equals(this.player.getName())
					&& !CustomNpcs.MailSendToYourself ? 3 : 0;
			if (this.type == 0) {
				this.type = this.getTextField(0) != null && this.getTextField(0).getText().isEmpty() ? 1 : 0;
			} // player
			if (this.type == 0) {
				this.type = GuiMailmanWrite.mail.title.isEmpty() ? 4 : 0;
			} // title
			if (this.type == 0 && !this.player.capabilities.isCreativeMode) {
				this.type = ClientProxy.playerData.game.getMoney() < this.totalCost ? 2 : 0;
			} // money
			if (this.type == 0 && !hasMail) {
				this.type = 5;
			} // empty
			this.getButton(0).setEnabled(this.type == 0);
		}

		// Animations
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (tick >= 0) {
			if (tick == 0) {
				partialTicks = 0.0f;
			}
			float part = (float) tick + partialTicks;
			float cos = (float) Math.cos(90.0d * part / (double) mtick * Math.PI / 180.0d);
			if (cos < 0.0f) {
				cos = 0.0f;
			} else if (cos > 1.0f) {
				cos = 1.0f;
			}
			switch (step) {
			case 0: { // open
				float u = this.guiLeft + (1.0f - cos) * 174.0f;
				float v = this.guiTop + (1.0f - cos) * 248.0f;
				this.drawPlace(u, v, mouseX, mouseY);
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(53, 168, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 1;
					tick = 21;
					mtick = 20;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.down",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.75f + 0.25f * this.rnd.nextFloat());
					GlStateManager.disableBlend();
				}
				break;
			}
			case 1: { // box and envelope_0 offset
				this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop + cos * 150.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + cos * 183.0f, this.guiTop + cos * 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 2;
					tick = 21;
					mtick = 20;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
					GlStateManager.disableBlend();
				}
				break;
			}
			case 2: { // box added slots and show list
				this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
				// envelope_0
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 5, this.guiTop + 190.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(0, 0, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				float v = this.guiTop + 150.0f - cos * 134.0f;
				int h = 186 - (int) v;
				if (h > 134) {
					h = 134;
				}
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9, v, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, h);
				GlStateManager.popMatrix();
				// handle
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				this.drawTexturedModalRect(25, 27, 0, 0, 18, 18);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 41, 0, 95, 68, 33);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 3;
					tick = 16;
					mtick = 15;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
					GlStateManager.disableBlend();
				}
				break;
			}
			case 3: { // envelope_1 offset
				this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
				// envelope_1
				float u = this.guiLeft + cos * 142.0f;
				float v = this.guiTop + 150.0f - cos * 160.0f;
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9.0f, this.guiTop + 12.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// handle
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				this.drawTexturedModalRect(25, 27, 0, 0, 18, 18);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 41, 0, 95, 68, 33);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 4;
					tick = 36;
					mtick = 35;
					GlStateManager.disableBlend();
				}
				break;
			}
			case 4: { // slots
				this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 142.0f, this.guiTop - 10.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9.0f, this.guiTop + 12.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// handle
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				// slots
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 208.0f, this.guiTop + 196.0f, 2.0f);
				this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				this.drawTexturedModalRect(0, 0, 0, 0, 18, 18);
				double h = 36.0d;
				int x0 = 0, y0 = 0;
				for (int s = 0; s < 36; s++) {
					int t = tick + s - 35;
					int x = s % 9;
					int y = (int) Math.floor((double) s / 9.0d);
					int x1 = x * 18 - 202;
					int y1 = y * 18 + (y == 3 ? 2 : 0) - 29;
					if (t < 0) {
						this.drawTexturedModalRect(x1, y1, 0, 0, 18, 18);
					} else {
						cos = (float) Math.cos(90.0d * ((double) t + partialTicks) / 35.0d * Math.PI / 180.0d);
						if (cos < 0.0f) {
							cos = 0.0f;
						} else if (cos > 1.0f) {
							cos = 1.0f;
						}
						double px = x0 - x1, py = y0 - y1;
						double dist = Math.sqrt(px * px + py * py);
						double r = (Math.pow(dist / 2.0d, 2.0d) + Math.pow(h, 2.0d)) / (2.0d * h);
						double tg = Math.abs(py / px);
						double angle = Math.atan(tg) * 180.0d / Math.PI;
						double cx = (x0 - x1) / 2.0d + x1;
						double cy = (y0 - y1) / 2.0d + y1;
						double rx = cx + Math.sin(angle * Math.PI / 180) * (r - h) * -1;
						double ry = cy + Math.cos(angle * Math.PI / 180) * (r - h);
						px = x0 - rx;
						py = y0 - ry;
						tg = Math.abs(py / px);
						double startAngle = Math.atan(tg) * 180.0d / Math.PI;
						px = x1 - rx;
						py = y1 - ry;
						tg = Math.abs(py / px);
						angle = 180.0d - startAngle - Math.atan(tg) * 180.0d / Math.PI;
						double nowAngle = cos * angle + startAngle;
						x1 = (int) (rx + Math.cos(nowAngle * Math.PI / 180) * r);
						y1 = (int) (ry + Math.sin(nowAngle * Math.PI / 180) * r * -1);
						this.drawTexturedModalRect(x1, y1, 0, 0, 18, 18);
					}
				}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 41, 0, 95, 68, 33);
				GlStateManager.popMatrix();
				if (tick % 4 == 0) {
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.slot",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.7f + 0.4f * this.rnd.nextFloat());
				}
				if (tick == 0) {
					step = 5;
                    mtick = 0;
					GlStateManager.disableBlend();
				}
				break;
			}
			case 6: { // simple close/back
				this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
				if (tick == 0) {
					step = 7;
					tick = 36;
					mtick = 36;
				}
			}
			case 7: { // 0 _ simple close/back
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 142.0f, this.guiTop - 10.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9.0f, this.guiTop + 12.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// handle
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				// slots
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 208.0f, this.guiTop + 196.0f, 2.0f);
				this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				this.drawTexturedModalRect(0, 0, 0, 0, 18, 18);
				int x0 = 0, y0 = 0;
				for (int s = 0; s < 36; s++) {
					int x = s % 9;
					int y = (int) Math.floor((double) s / 9.0d);
					int x1 = x * 18 - 202;
					int y1 = y * 18 + (y == 3 ? 2 : 0) - 29;
					double px = x0 - x1, py = y0 - y1;
					this.drawTexturedModalRect((int) (x1 + px * cos), (int) (y1 + py * cos), 0, 0, 18, 18);
				}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 41, 0, 95, 68, 33);
				GlStateManager.popMatrix();
				if (tick % 6 == 0) {
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.slot",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.9f + 0.3f * this.rnd.nextFloat());
				}
				if (tick == 0) {
					step = aType == 0 ? 8 : aType == 1 ? 10 : 13;
					tick = 31;
					mtick = 30;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
				}
				break;
			}
			case 8: { // 1 _ simple close/back
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				float u = this.guiLeft + 9.0f;
				float v = this.guiTop + 12.0f + cos * 53.0f;
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// handle
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 50.0f, this.guiTop + 8.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, 0, 174, 0, 74, 17);
				GlStateManager.popMatrix();
				u = this.guiLeft + 142.0f - cos * 142.0f;
				v = this.guiTop - 10.0f + cos * 34.0f;
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// box
				u = this.guiLeft + 183.0f - cos * 130.0f;
				v = this.guiTop + 169.0f - cos * 75.0f;
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 9;
					tick = 31;
					mtick = 30;
				}
				break;
			}
			case 9: { // 2 _ simple close/back
				// table
				float u = this.guiLeft + cos * 174.0f;
				float v = this.guiTop + cos * 248.0f;
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 2.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v + 24.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(u + 53.0f, v + 94.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 0;
                    mtick = 0;
					this.close();
				}
				break;
			}
			case 10: { // 1 _ send
				// box
				if (hasStacks) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 0.0f);
					this.mc.renderEngine.bindTexture(mSbox);
					this.drawTexturedModalRect(0, 0, 74, 0, 74, 54);
					GlStateManager.popMatrix();
				} else {
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 0.0f);
					this.mc.renderEngine.bindTexture(mSbox);
					this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
					GlStateManager.popMatrix();
				}
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9.0f, this.guiTop + 12.0f, 0.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 142.0f - cos * 142.0f, this.guiTop - 10.0f + cos * 160.0f,
						1.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 11;
					tick = 16;
					mtick = 15;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
				}
				break;
			}
			case 11: { // 2 _ send
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop + 150.0f, 0.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				float v = this.guiTop + 150.0f - (1.0f - cos) * 134.0f;
				int h = 186 - (int) v;
				if (h > 134) {
					h = 134;
				}
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9, v, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, h);
				GlStateManager.popMatrix();
				if (hasStacks) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.guiLeft + 183.0f, this.guiTop + 169.0f, 0.0f);
					this.mc.renderEngine.bindTexture(mSbox);
					this.drawTexturedModalRect(0, 0, 74, 0, 74, 54);
					GlStateManager.popMatrix();
				}
				if (tick == 0) {
					step = 12;
					tick = 31;
					mtick = 30;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":book.sheet",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
				}
				break;
			}
			case 12: { // 3 _ send
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + cos * 174.0f, this.guiTop + cos * 248.0f, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + cos * 300.0f, this.guiTop + 150.0f - cos * 248.0f, 1.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(5, 40, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				if (hasStacks) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(this.guiLeft + 183.0f + cos * 300.0f, this.guiTop + 169.0f - cos * 248.0f,
							0.0f);
					this.mc.renderEngine.bindTexture(mSbox);
					this.drawTexturedModalRect(0, 0, 74, 0, 74, 54);
					GlStateManager.popMatrix();
				}
				if (tick == 0) {
					step = 0;
                    mtick = 0;
					this.close();
				}
				break;
			}
			case 13: { // 1 _ delete
				// table
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + cos * 174.0f, this.guiTop + cos * 248.0f, 0.0f);
				this.mc.renderEngine.bindTexture(mTable);
				this.drawTexturedModalRect(0, -5, 0, 0, 174, 248);
				GlStateManager.popMatrix();
				// box
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 183.0f - cos * 41.0f, this.guiTop + 169.0f - cos * 74.0f, 1.0f);
				this.mc.renderEngine.bindTexture(mSbox);
				this.drawTexturedModalRect(0, 0, 0, 54, 68, 74);
				GlStateManager.popMatrix();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 147.0f - cos * 54.0f, this.guiTop + 30.0f + cos * 33.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				this.drawTexturedModalRect(0, 0, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 9.0f + cos * 88.0f, this.guiTop + 12.0f + cos * 52.0f, 2.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 14;
					tick = 16;
					mtick = 15;
					MusicController.Instance.forcePlaySound(SoundCategory.PLAYERS, CustomNpcs.MODID + ":mail.delete",
							(float) this.player.posX, (float) this.player.posY, (float) this.player.posZ, 1.0f,
							0.8f + 0.4f * this.rnd.nextFloat());
				}
				break;
			}
			case 14: { // 2 _ delete
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				// envelope
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 93.0f, this.guiTop + 63.0f, 1.0f);
				this.mc.renderEngine.bindTexture(mEnvelope);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f - cos);
				this.drawTexturedModalRect(0, 0, 0, 0, 164, 137);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 97.0f, this.guiTop + 64.0f, 1.0f);
				this.mc.renderEngine.bindTexture(mList);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f - cos);
				this.drawTexturedModalRect(0, 0, 0, 0, 156, 134);
				GlStateManager.popMatrix();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 97.0f + cos * 28.0f, this.guiTop + 64.0f + cos * 19.5f, 2.0f);
				GlStateManager.scale(1.31092f - 0.47059f * cos, 1.09836f - 0.31967 * cos, 1.0f);
				this.mc.renderEngine.bindTexture(mList);
				GlStateManager.color(1.0f, 1.0f, 1.0f, cos);
				this.drawTexturedModalRect(0, 0, 0, 134, 119, 122);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 15;
					tick = 31;
					mtick = 30;
				}
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				break;
			}
			case 15: { // 3 _ delete
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				// list
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 97.0f + 28.0f, this.guiTop + 64.0f + 19.5f, 1.0f);
				GlStateManager.scale(1.31092f - 0.47059f, 1.09836f - 0.31967, 1.0f);
				this.mc.renderEngine.bindTexture(mList);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f - cos);
				this.drawTexturedModalRect(0, 0, 0, 134, 119, 122);
				GlStateManager.popMatrix();

				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 130.0f + cos * 7.5f, this.guiTop + 88.0f + cos * 7.125f, 2.0f);
				GlStateManager.scale(0.85f - 0.15f * cos, 0.85f - 0.15f * cos, 1.0f);
				this.mc.renderEngine.bindTexture(mList);
				GlStateManager.color(1.0f, 1.0f, 1.0f, cos);
				this.drawTexturedModalRect(0, 0, 156, 0, 100, 95);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 16;
					tick = 31;
					mtick = 30;
				}
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				break;
			}
			case 16: { // 4 _ delete
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 137.5f - cos * 300.0f, this.guiTop + 95.125f - cos * 150.0f,
						2.0f);
				GlStateManager.scale(0.6f, 0.6f, 1.0f);
				this.mc.renderEngine.bindTexture(mList);
				this.drawTexturedModalRect(0, 0, 156, 0, 100, 95);
				GlStateManager.popMatrix();
				if (tick == 0) {
					step = 0;
                    mtick = 0;
					this.close();
				}
				break;
			}
			}
			tick--;
		} else {
			this.drawPlace(this.guiLeft, this.guiTop, mouseX, mouseY);
		}
		GlStateManager.popMatrix();
		if (step != 5) {
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		// Player Money
		if (this.mc != null && this.canSend) {
			String text = Util.instance.getTextReducedNumber(ClientProxy.playerData.game.getMoney(), true, true,
					false) + CustomNpcs.displayCurrencies;
			int x = this.guiLeft + 166, y = this.guiTop + 150;
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.translate(x, y, 0.0f);
			this.mc.renderEngine.bindTexture(ClientGuiEventHandler.COIN_NPC);
			float sc = 16.0f / 250.f;
			GlStateManager.scale(sc, sc, sc);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			this.mc.fontRenderer.drawString(text, x + 15, y + 8.0f / 2.0f, 0x404040, false);
			GlStateManager.popMatrix();
		}
		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			if (!this.canSend) {
				this.setHoverText(new TextComponentTranslation("mailbox.hover.done").getFormattedText());
			} // done
			else {
				if (this.type == 0) {
					ITextComponent mes = new TextComponentTranslation("mailbox.hover.send.0", Util.instance.ticksToElapsedTime(CustomNpcs.MailTimeWhenLettersWillBeReceived[1] * 20L, false, true, true), "" + this.totalCost, CustomNpcs.displayCurrencies);
					for (int i : this.cost.keySet()) {
						if (this.cost.get(i) > 0L) {
							String p0 = "" + this.cost.get(i);
							String p1 = "", p2 = "";
							switch (i) {
							case 1:
								p1 = "" + CustomNpcs.MailCostSendingLetter[1];
								break;
							case 2:
								p0 = "" + GuiMailmanWrite.mail.money;
								p1 = "" + this.cost.get(i);
								p2 = "" + CustomNpcs.MailCostSendingLetter[3];
								break;
							case 3:
								p1 = "" + CustomNpcs.MailCostSendingLetter[2];
								break;
							case 4:
								p1 = "" + CustomNpcs.MailCostSendingLetter[4];
								break;
							default:
								break;
							}
							mes.appendSibling(new TextComponentTranslation("mailbox.hover.send.2." + i, p0,
									CustomNpcs.displayCurrencies, p1, CustomNpcs.displayCurrencies, p2));
						}
					}
					this.setHoverText(mes.getFormattedText());
				} else if (this.type == 2) {
					ITextComponent mes = new TextComponentTranslation("mailbox.hover.send.2", "" + this.totalCost,
							CustomNpcs.displayCurrencies);
					for (int i : this.cost.keySet()) {
						if (this.cost.get(i) > 0L) {
							String p0 = "" + this.cost.get(i);
							String p1 = "", p2 = "";
							switch (i) {
							case 1:
								p1 = "" + CustomNpcs.MailCostSendingLetter[1];
								break;
							case 2:
								p0 = "" + GuiMailmanWrite.mail.money;
								p1 = "" + this.cost.get(i);
								p2 = "" + CustomNpcs.MailCostSendingLetter[3];
								break;
							case 3:
								p1 = "" + CustomNpcs.MailCostSendingLetter[2];
								break;
							case 4:
								p1 = "" + CustomNpcs.MailCostSendingLetter[4];
								break;
							default:
								break;
							}
							mes.appendSibling(new TextComponentTranslation("mailbox.hover.send.2." + i, p0,
									CustomNpcs.displayCurrencies, p1, CustomNpcs.displayCurrencies, p2));
						}
					}
					this.setHoverText(mes.getFormattedText());
				} else {
					this.setHoverText(
							new TextComponentTranslation("mailbox.hover.send." + this.type).getFormattedText());
				}
			}
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.X").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) { // take money
			this.setHoverText(new TextComponentTranslation("display.hover.X").getFormattedText());
		} else if ((this.getTextField(0) != null && this.getTextField(0).isMouseOver())
				|| (this.getTextField(2) != null && this.getTextField(2).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation("mailbox.hover.to").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mailbox.hover.title").getFormattedText());
		} else if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mailbox.hover.money").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("mailbox.hover.ransom").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, fontRenderer);
			this.hoverText = null;
		}
	}

	private String getText() {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			return this.bookPages.getStringTagAt(this.currPage);
		}
		return "";
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		int x = this.guiLeft + 170, y = this.guiTop + 48;
		// Text area

		// player name
		this.addLabel(new GuiNpcLabel(0, "mailbox." + (!this.canEdit || !this.canSend ? "sender" : "username"), x, y,
				CustomNpcs.LableColor.getRGB()));
		if (this.canEdit) {
			if (!this.canSend) {
				this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, x, y += 10, 112, 14,
						GuiMailmanWrite.mail.sender));
			} else {
				this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x, y += 10, 112, 14, this.username));
			}
		} else {
			this.addLabel(new GuiNpcLabel(10, "\"" + GuiMailmanWrite.mail.sender + "\"", x + 2, y += 10,
					CustomNpcs.LableColor.getRGB()));
		}
		// title
		this.addLabel(new GuiNpcLabel(1, "mailbox.subject", x, y += 18, CustomNpcs.LableColor.getRGB()));
		if (this.canEdit) {
			this.addTextField(
					new GuiNpcTextField(1, this, this.fontRenderer, x, y += 10, 112, 14, GuiMailmanWrite.mail.title));
		} else {
			this.addLabel(new GuiNpcLabel(11, "\"" + GuiMailmanWrite.mail.title + "\"", x, y += 10,
					CustomNpcs.LableColor.getRGB()));
		}
		// ransom
		if (!this.canEdit) {
			if (GuiMailmanWrite.mail.ransom > 0) {
				this.addLabel(new GuiNpcLabel(7,
						((char) 167) + "4" + ((char) 167) + "l"
								+ new TextComponentTranslation("mailbox.ransom").getFormattedText(),
						x, y + 18, CustomNpcs.LableColor.getRGB()));
				this.addLabel(
						new GuiNpcLabel(8,
								Util.instance.getTextReducedNumber(GuiMailmanWrite.mail.ransom, true, false, false)
										+ " " + CustomNpcs.displayCurrencies,
								x + 2, y + 28, CustomNpcs.LableColor.getRGB()));
			}
			if (GuiMailmanWrite.mail.money > 0) {
				this.addLabel(new GuiNpcLabel(7, "market.currency", x, y + 18, CustomNpcs.LableColor.getRGB()));
				this.addLabel(
						new GuiNpcLabel(8,
								Util.instance.getTextReducedNumber(GuiMailmanWrite.mail.money, true, false, false)
										+ " " + CustomNpcs.displayCurrencies,
								x + 2, y + 28, CustomNpcs.LableColor.getRGB()));
			}
		}
		this.addLabel(this.error = new GuiNpcLabel(2, "", x - 10, this.guiTop + 145, 0xFFFF0000));
		// Moneys
		if (this.canEdit) {
			this.addLabel(new GuiNpcLabel(3, "market.currency", x, (y += 19) + 4, CustomNpcs.LableColor.getRGB()));
			this.addLabel(
					new GuiNpcLabel(6, CustomNpcs.displayCurrencies, x + 102, y + 4, CustomNpcs.LableColor.getRGB()));
			this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, x + 48, y, 50, 16,
					"" + GuiMailmanWrite.mail.money));
			this.getTextField(3).setNumbersOnly();
			this.getTextField(3).setMinMaxDefault(0, (int) (this.player.capabilities.isCreativeMode ? Integer.MAX_VALUE
					: ClientProxy.playerData.game.getMoney()), GuiMailmanWrite.mail.money);

			this.addLabel(new GuiNpcLabel(7, "mailbox.ransom", x, (y += 19) + 4, CustomNpcs.LableColor.getRGB()));
			this.addLabel(
					new GuiNpcLabel(8, CustomNpcs.displayCurrencies, x + 102, y + 4, CustomNpcs.LableColor.getRGB()));
			this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, x + 48, y, 50, 16,
					"" + GuiMailmanWrite.mail.ransom));
			this.getTextField(4).setNumbersOnly();
			this.getTextField(4).setMinMaxDefault(0, Integer.MAX_VALUE, GuiMailmanWrite.mail.ransom);
		}

		x = this.guiLeft + 7;
		y = this.guiTop + 149;
		GuiNpcButton button;
		if (this.canEdit && !this.canSend) { // dialog/quest add to
			button = new GuiNpcButton(0, x + 52, y, 50, 14, "gui.done");
			button.texture = icons;
			button.txrY = 176;
			this.addButton(button);
		} else if (this.canEdit) { // write
			button = new GuiNpcButton(0, x + 52, y, 50, 14, "mailbox.send");
			button.texture = icons;
			button.txrY = 176;
			this.addButton(button);
		}
		if (!this.canEdit && !this.canSend) { // read -> delete
			if (GuiMailmanWrite.mail.ransom > 0) {
				button = new GuiNpcButton(6, x + 220, y - 42, 50, 14, "gui.pay");
				button.texture = icons;
				button.txrY = 176;
				this.addButton(button);
			} else if (GuiMailmanWrite.mail.money > 0) {
				button = new GuiNpcButton(6, x + 220, y - 42, 50, 14, "gui.take");
				button.texture = icons;
				button.txrY = 176;
				this.addButton(button);
			}
			button = new GuiNpcButton(4, x, y, 50, 14, "gui.remove");
			button.texture = icons;
			button.txrY = 176;
			this.addButton(button);
			if (!mail.isReturned()) {
				button = new GuiNpcButton(7, x + 52, y, 50, 14, "mailbox.back");
				button.texture = icons;
				button.txrY = 176;
				this.addButton(button);
			}
		}
		if (!this.canEdit || this.canSend) { // write -> cancel
			button = new GuiNpcButton(3, x + 104, y, 50, 14, !this.canEdit ? "gui.back" : "gui.cancel");
			button.texture = icons;
			button.txrY = 176;
			this.addButton(button);
		}
		this.buttonList.add(this.buttonNextPage = new GuiButtonNextPage(1, x + 135, y - 16, true));
		this.buttonList.add(this.buttonPreviousPage = new GuiButtonNextPage(2, x, y - 16, false));
		this.updateButtons();
	}

	public void keyTyped(char c, int i) {
		if (i == 1) {
			aType = 0;
			this.animClose();
			return;
		}
		if (!GuiNpcTextField.isActive() && this.canEdit) {
			this.keyTypedInBook(c, i);
		} else {
			super.keyTyped(c, i);
		}
	}

	private void keyTypedInBook(char c, int i) {
		if (step != 5 || GuiNpcTextField.isActive()) {
			return;
		}
        if (c == '\u0016') {
            this.addString(GuiScreen.getClipboardString());
        }
        switch (i) {
            case 14: { // back
                String s = this.getText();
                if (!s.isEmpty()) {
                    this.setText(s.substring(0, s.length() - 1));
                }
                return;
            }
            case 28: // enter
            case 156: {
                this.addString("\n");
                return;
            }
            default: {
                if (ChatAllowedCharacters.isAllowedCharacter(c)) {
                    this.addString(Character.toString(c));
                }
            }
        }
    }

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void save() {
	}

	@Override
	public void setClose(int i, NBTTagCompound data) {
		this.player.sendMessage(
				new TextComponentTranslation("mailbox.success", data.getString("username")));
		aType = 1;
		this.animClose();
	}

	@Override
	public void setError(int id, NBTTagCompound data) {
		this.hasSend = false;
		if (this.error == null) {
			return;
		}
		switch (id) {
		case 0:
			this.error.setLabel(new TextComponentTranslation("mailbox.error.username").getFormattedText());
			break;
		case 1:
			this.error.setLabel(new TextComponentTranslation("mailbox.error.subject").getFormattedText());
			break;
		case 2:
			this.error.setLabel(new TextComponentTranslation("mailbox.error.yourself").getFormattedText());
			break;
		case 3:
			this.error.setLabel(new TextComponentTranslation("mailbox.error.nomoney").getFormattedText());
			break;
		}
		this.errTick = this.mc.world.getTotalWorldTime() + 200L;
	}

	private void setText(String str) {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			this.bookPages.set(this.currPage, new NBTTagString(str));
		}
	}

	@Override
	public void textUpdate(String text) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
		case 0:
			this.username = textField.getText();
			break;
		case 1:
			GuiMailmanWrite.mail.title = textField.getText();
			break;
		case 2:
			GuiMailmanWrite.mail.sender = textField.getText();
			break;
		case 3: {
			GuiMailmanWrite.mail.money = textField.getInteger();
			textField.setText("" + GuiMailmanWrite.mail.money);
			textField.def = GuiMailmanWrite.mail.money;
			break;
		}
		case 4: {
			GuiMailmanWrite.mail.ransom = textField.getInteger();
			textField.setText("" + GuiMailmanWrite.mail.ransom);
			textField.def = GuiMailmanWrite.mail.ransom;
			break;
		}
		}
	}

	private void updateButtons() {
		if (!this.canEdit && GuiMailmanWrite.mail.ransom > 0) {
			this.buttonNextPage.setVisible(false);
			this.buttonPreviousPage.setVisible(false);
			return;
		}
		this.buttonNextPage.setVisible(this.currPage < this.bookTotalPages - 1 || this.canEdit);
		this.buttonPreviousPage.setVisible(this.currPage > 0);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		++this.updateCount;
		// New
		if (this.getLabel(4) != null) {
			this.getLabel(4).enabled = false;
		}
		if (this.canEdit) {
			if (this.getLabel(4) != null) {
				this.getLabel(4).enabled = true;
			}
		} else {
			if (!this.canSend && GuiMailmanWrite.mail.money > 0) {
				if (this.getLabel(4) != null) {
					this.getLabel(4).enabled = true;
				}
			}
		}
	}

}
