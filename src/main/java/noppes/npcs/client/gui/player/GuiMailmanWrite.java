package noppes.npcs.client.gui.player;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.AdditionalMethods;

@SideOnly(Side.CLIENT)
public class GuiMailmanWrite extends GuiContainerNPCInterface
		implements ITextfieldListener, IGuiError, IGuiClose, GuiYesNoCallback {
	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	private static ResourceLocation bookInventory = new ResourceLocation("textures/gui/container/inventory.png");
	private static ResourceLocation bookWidgets = new ResourceLocation("textures/gui/widgets.png");
	public static PlayerMail mail = new PlayerMail();
	public static GuiScreen parent;
	private int bookImageHeight;
	private int bookImageWidth;
	private NBTTagList bookPages;
	private int bookTotalPages;
	private GuiButtonNextPage buttonNextPage;
	private GuiButtonNextPage buttonPreviousPage;
	private boolean canEdit;
	private boolean canSend;
	private int currPage;
	private GuiNpcLabel error;
	private boolean hasSend;
	private Minecraft mc;
	private int updateCount;
	private String username;

	public GuiMailmanWrite(ContainerMail container, boolean canEdit, boolean canSend) {
		super(null, container);
		this.bookImageWidth = 192;
		this.bookImageHeight = 192;
		this.bookTotalPages = 1;
		this.hasSend = false;
		this.mc = Minecraft.getMinecraft();
		this.username = "";
		this.title = "";
		this.canEdit = canEdit;
		this.canSend = canSend;
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
		this.xSize = 360;
		this.ySize = 260;
		this.drawDefaultBackground = false;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.enabled) {
			switch (button.id) {
				case 0: {
					GuiMailmanWrite.mail.message.setTag("pages", this.bookPages);
					if (this.canSend) {
						if (!this.hasSend) {
							this.hasSend = true;
							NoppesUtilPlayer.sendData(EnumPlayerPacket.MailSend, this.username,
									GuiMailmanWrite.mail.writeNBT());
						}
					} else {
						this.close();
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
				case 3: {
					this.close();
					break;
				}
				case 4: {
					GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, "",
							new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
					this.displayGuiScreen(guiyesno);
					break;
				}
				case 5: {
					if (GuiMailmanWrite.mail.money <= 0) {
						return;
					}
					NoppesUtilPlayer.sendData(EnumPlayerPacket.TakeMoney, GuiMailmanWrite.mail.sender,
							GuiMailmanWrite.mail.subject, GuiMailmanWrite.mail.time);
					GuiMailmanWrite.mail.money = 0;
					this.initGui();
					break;
				}
			}
			this.updateButtons();
		}
	}

	private void addNewPage() {
		if (this.bookPages != null && this.bookPages.tagCount() < 50) {
			this.bookPages.appendTag(new NBTTagString(""));
			++this.bookTotalPages;
		}
	}

	@Override
	public void close() {
		this.mc.displayGuiScreen(GuiMailmanWrite.parent);
		GuiMailmanWrite.parent = null;
		GuiMailmanWrite.mail = new PlayerMail();
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, GuiMailmanWrite.mail.time,
					GuiMailmanWrite.mail.sender);
			this.close();
		} else {
			NoppesUtil.openGUI((EntityPlayer) this.player, this);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawWorldBackground(0);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiMailmanWrite.bookGuiTextures);
		this.drawTexturedModalRect(this.guiLeft + 130, this.guiTop + 22, 0, 0, this.bookImageWidth,
				this.bookImageHeight / 3);
		this.drawTexturedModalRect(this.guiLeft + 130, this.guiTop + 22 + this.bookImageHeight / 3, 0,
				this.bookImageHeight / 2, this.bookImageWidth, this.bookImageHeight / 2);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop + 2, 0, 0, this.bookImageWidth, this.bookImageHeight);
		// New
		this.drawTexturedModalRect(this.guiLeft, this.guiTop + 174, 0, 100, 158, 81);
		this.drawTexturedModalRect(this.guiLeft + 158, this.guiTop + 174, 128, 100, 38, 81);
		this.drawTexturedModalRect(this.guiLeft + 166, this.guiTop + 170, 136, 96, 30, 95);
		// ----
		this.mc.getTextureManager().bindTexture(GuiMailmanWrite.bookInventory);
		/*
		 * Changed this.drawTexturedModalRect(this.guiLeft + 20, this.guiTop + 173, 0,
		 * 82, 180, 55); this.drawTexturedModalRect(this.guiLeft + 20, this.guiTop +
		 * 228, 0, 140, 180, 26);
		 */
		// New
		this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 174, 7, 83, 162, 54);
		this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 229, 7, 141, 162, 18);
		// ----
		String s = net.minecraft.client.resources.I18n.format("book.pageIndicator",
				new Object[] { this.currPage + 1, this.bookTotalPages });
		String s2 = "";
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			s2 = this.bookPages.getStringTagAt(this.currPage);
		}
		if (this.canEdit) {
			if (this.mc.fontRenderer.getBidiFlag()) {
				s2 += "_";
			} else if (this.updateCount / 6 % 2 == 0) {
				s2 = s2 + "" + TextFormatting.BLACK + "_";
			} else {
				s2 = s2 + "" + TextFormatting.GRAY + "_";
			}
		}
		int l = this.mc.fontRenderer.getStringWidth(s);
		this.mc.fontRenderer.drawString(s, this.guiLeft - l + this.bookImageWidth - 44, this.guiTop + 18, 0);
		this.mc.fontRenderer.drawSplitString(s2, this.guiLeft + 36, this.guiTop + 18 + 16, 116, 0);
		this.drawGradientRect(this.guiLeft + 175, this.guiTop + 136, this.guiLeft + 269, this.guiTop + 154, -1072689136,
				-804253680);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiMailmanWrite.bookWidgets);
		for (int i = 0; i < 4; ++i) {
			this.drawTexturedModalRect(this.guiLeft + 175 + i * 24, this.guiTop + 134, 0, 22, 24, 24);
		}
		super.drawScreen(par1, par2, par3);
	}

	private String func_74158_i() {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			return this.bookPages.getStringTagAt(this.currPage);
		}
		return "";
	}

	private void func_74159_a(String par1Str) {
		if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
			this.bookPages.set(this.currPage, new NBTTagString(par1Str));
		}
	}

	private void func_74160_b(String par1Str) {
		String s1 = this.func_74158_i();
		String s2 = s1 + par1Str;
		int i = this.mc.fontRenderer.getWordWrappedHeight(s2 + "" + TextFormatting.BLACK + "_", 118);
		if (i <= 118 && s2.length() < 256) {
			this.func_74159_a(s2);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		if (this.canEdit && !this.canSend) {
			this.addLabel(new GuiNpcLabel(0, "mailbox.sender", this.guiLeft + 170, this.guiTop + 32, 0));
		} else {
			this.addLabel(new GuiNpcLabel(0, "mailbox.username", this.guiLeft + 170, this.guiTop + 32, 0));
		}
		if (this.canEdit) {
			if (!this.canSend) {
				this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 170, this.guiTop + 42,
						114, 20, GuiMailmanWrite.mail.sender));
			} else {
				this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 170, this.guiTop + 42,
						114, 20, this.username));
			}
		} else {
			this.addLabel(new GuiNpcLabel(10, GuiMailmanWrite.mail.sender, this.guiLeft + 170, this.guiTop + 42, 0));
		}
		this.addLabel(new GuiNpcLabel(1, "mailbox.subject", this.guiLeft + 170, this.guiTop + 66, 0)); // Changed
		if (this.canEdit) {
			this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 170, this.guiTop + 76, 114,
					20, GuiMailmanWrite.mail.subject)); // Changed
		} else {
			this.addLabel(new GuiNpcLabel(11, GuiMailmanWrite.mail.subject, this.guiLeft + 170, this.guiTop + 76, 0)); // Changed
		}
		this.addLabel(this.error = new GuiNpcLabel(2, "", this.guiLeft + 170, this.guiTop + 114, 16711680));
		if (this.canEdit && !this.canSend) {
			this.addButton(new GuiNpcButton(0, this.guiLeft + 200, this.guiTop + 171, 60, 20, "gui.done"));
		} else if (this.canEdit) {
			this.addButton(new GuiNpcButton(0, this.guiLeft + 200, this.guiTop + 171, 60, 20, "mailbox.send"));
		}
		if (!this.canEdit && !this.canSend) {
			this.addButton(
					new GuiNpcButton(4, this.guiLeft + 200, this.guiTop + 171, 60, 20, "selectWorld.deleteButton"));
		}
		if (!this.canEdit || this.canSend) {
			this.addButton(new GuiNpcButton(3, this.guiLeft + 200, this.guiTop + 194, 60, 20, "gui.cancel"));
		}
		this.buttonList
				.add(this.buttonNextPage = new GuiButtonNextPage(1, this.guiLeft + 120, this.guiTop + 156, true));
		this.buttonList
				.add(this.buttonPreviousPage = new GuiButtonNextPage(2, this.guiLeft + 38, this.guiTop + 156, false));
		this.updateButtons();
		// New
		if (this.canEdit) {
			this.addLabel(new GuiNpcLabel(3, "gui.market.currency", this.guiLeft + 170, this.guiTop + 100, 0));
			this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 170, this.guiTop + 110,
					114, 20, "" + GuiMailmanWrite.mail.money));
			this.getTextField(3).setNumbersOnly();
			this.getTextField(3)
					.setMinMaxDefault(0,
							(int) (this.player.capabilities.isCreativeMode ? Integer.MAX_VALUE
									: CustomNpcs.proxy.getPlayerData(this.player).game.getMoney()),
							GuiMailmanWrite.mail.money);
		} else {
			if (!this.canSend && GuiMailmanWrite.mail.money > 0) {
				this.addLabel(new GuiNpcLabel(3, "gui.market.currency", this.guiLeft + 170, this.guiTop + 100, 0));
				this.addLabel(new GuiNpcLabel(4,
						(GuiMailmanWrite.mail.money > 999999
								? AdditionalMethods.getTextReducedNumber(GuiMailmanWrite.mail.money, true, true, false)
								: "" + GuiMailmanWrite.mail.money) + CustomNpcs.charCurrencies,
						this.guiLeft + 170, this.guiTop + 110, 0));
				this.addButton(new GuiNpcButton(5, this.guiLeft + 225, this.guiTop + 106, 60, 20, "gui.take"));
			} else {

			}
		}
	}

	public void keyTyped(char par1, int par2) {
		if (!GuiNpcTextField.isActive() && this.canEdit) {
			this.keyTypedInBook(par1, par2);
		} else {
			super.keyTyped(par1, par2);
		}
	}

	private void keyTypedInBook(char par1, int par2) {
		switch (par1) {
		case '\u0016': {
			this.func_74160_b(GuiScreen.getClipboardString());
		}
		default: {
			switch (par2) {
			case 14: {
				String s = this.func_74158_i();
				if (s.length() > 0) {
					this.func_74159_a(s.substring(0, s.length() - 1));
				}
				return;
			}
			case 28:
			case 156: {
				this.func_74160_b("\n");
				return;
			}
			default: {
				if (ChatAllowedCharacters.isAllowedCharacter(par1)) {
					this.func_74160_b(Character.toString(par1));
				}
				return;
			}
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
				new TextComponentTranslation("mailbox.succes", new Object[] { data.getString("username") }));
	}

	@Override
	public void setError(int i, NBTTagCompound data) {
		if (i == 0) {
			this.error.setLabel(new TextComponentTranslation("mailbox.errorUsername").getFormattedText());
		}
		if (i == 1) {
			this.error.setLabel(new TextComponentTranslation("mailbox.errorSubject").getFormattedText());
		}
		this.hasSend = false;
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getId() == 0) {
			this.username = textField.getText();
		}
		if (textField.getId() == 1) {
			GuiMailmanWrite.mail.subject = textField.getText();
		}
		if (textField.getId() == 2) {
			GuiMailmanWrite.mail.sender = textField.getText();
		}
		if (textField.getId() == 3) { // New
			GuiMailmanWrite.mail.money = textField.getInteger();
		}
	}

	private void updateButtons() {
		this.buttonNextPage.setVisible(this.currPage < this.bookTotalPages - 1 || this.canEdit);
		this.buttonPreviousPage.setVisible(this.currPage > 0);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		++this.updateCount;
		// New
		if (this.getLabel(3) != null) {
			this.getLabel(3).enabled = false;
		}
		if (this.getLabel(4) != null) {
			this.getLabel(4).enabled = false;
		}
		if (this.getButton(5) != null) {
			this.getButton(5).setVisible(false);
		}
		if (this.canEdit) {
			if (this.getLabel(3) != null) {
				this.getLabel(3).enabled = true;
			}
			if (this.getLabel(4) != null) {
				this.getLabel(4).enabled = true;
			}
			if (this.getButton(5) != null) {
				this.getButton(5).setVisible(false);
			}
		} else {
			if (!this.canSend && GuiMailmanWrite.mail.money > 0) {
				if (this.getLabel(3) != null) {
					this.getLabel(3).enabled = true;
				}
				if (this.getLabel(4) != null) {
					this.getLabel(4).enabled = true;
				}
				if (this.getButton(5) != null) {
					this.getButton(5).setVisible(true);
				}
			}
		}
	}

}
