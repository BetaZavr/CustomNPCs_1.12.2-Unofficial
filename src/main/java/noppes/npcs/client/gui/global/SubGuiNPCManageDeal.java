package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.drop.SubGuiDropEdit;
import noppes.npcs.client.gui.select.SubGuiColorSelector;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DropSet;

import javax.annotation.Nonnull;

public class SubGuiNPCManageDeal extends GuiContainerNPCInterface
		implements ICustomScrollListener, ITextfieldListener {

	public static GuiScreen parent;
	protected static final Random rnd = new Random();
	protected final ContainerNPCTraderSetup menu;
	protected final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
	protected final Deal deal;
	protected final int[][] slotPoses = new int[10][2];
	protected GuiCustomScroll scroll;
	protected ResourceLocation objCase;
	//case
	protected Map<String, String> materialTextures = new HashMap<>();
	protected boolean type;
	protected boolean start;

	public SubGuiNPCManageDeal(EntityNPCInterface npc, ContainerNPCTraderSetup cont) {
		super(npc, cont);
		setBackground("npcdrop.png");
		drawDefaultBackground = false;
		closeOnEsc = true;
		xSize = 380;
		ySize = 217;
		menu = cont;
		title = "";
		for (int slotId = 0; slotId < 10; ++slotId) {
			slotPoses[slotId][0] = menu.getSlot(slotId).xPos;
			slotPoses[slotId][1] = menu.getSlot(slotId).yPos;
		}
		deal = menu.deal;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: deal.setIgnoreDamage(button.getValue() == 1); break;
			case 1: deal.setIgnoreNBT(button.getValue() == 1); break;
			case 2: setSubGui(new SubGuiNpcAvailability(deal.availability,  parent)); initGui(); break;
			case 3: deal.setType(button.getValue()); break;
			case 4: deal.setIsCase(button.getValue() == 1); initGui(); break;
			case 5: {
				if (!deal.isCase()) { return; }
				SubGuiDropEdit.parent = null;
				SubGuiDropEdit.parentContainer = EnumGuiType.SetupTraderDeal;
				SubGuiDropEdit.parentData = new BlockPos(menu.marcet.getId(), deal.getId(), 0);
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("InventoryType", 1);
				compound.setInteger("Marcet", menu.marcet.getId());
				compound.setInteger("Deal", deal.getId());
				compound.setInteger("DropSet", -1);
				NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
				break;
			} // add
			case 6: {
				if (!deal.isCase() || !scroll.hasSelected()) { return; }
				deal.removeCaseItem(scroll.getSelect());
				initGui();
				break;
			} // del
			case 7: {
				if (!deal.isCase() || !scroll.hasSelected()) { return; }
				SubGuiDropEdit.parent = null;
				SubGuiDropEdit.parentContainer = EnumGuiType.SetupTraderDeal;
				SubGuiDropEdit.parentData = new BlockPos(menu.marcet.getId(), deal.getId(), 0);
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("InventoryType", 1);
				compound.setInteger("Marcet", menu.marcet.getId());
				compound.setInteger("Deal", deal.getId());
				compound.setInteger("DropSet", scroll.getSelect());
				NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
				break;
			} // edit
			case 8: {
				setSubGui(new SubGuiColorSelector(deal.getRarityColor(), new SubGuiColorSelector.ColorCallback() {
					@Override
					public void color(int colorIn) {
						deal.setRarityColor(colorIn);
						initGui();
					}
					@Override
					public void preColor(int colorIn) {
						((GuiColorButton) button).setColor(colorIn);
						deal.setRarityColor(colorIn);
					}
				}));
				break;
			} // color
			case 9: if (deal.isCase()) { setSubGui(new SubGuiNpcDealCaseSetting(deal)); } break;
			case 11: if (deal.isCase()) { deal.setShowInCase(((GuiNpcCheckBox) button).isSelected()); } break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void onClosed() {
		GuiNpcTextField.unfocus();
		save();
		IEditNPC screen = null;
		if (parent instanceof IEditNPC) { screen = (IEditNPC) parent; }
		else if (mc.currentScreen instanceof IEditNPC) { screen = (IEditNPC) mc.currentScreen; }
		if (screen != null) {
			screen.subGuiClosed(this);
			screen.setSubGui(null);
			while (screen instanceof SubGuiInterface && ((SubGuiInterface) screen).parent instanceof IEditNPC) { screen = (IEditNPC) ((SubGuiInterface) screen).parent; }
			displayGuiScreen((GuiScreen) screen);
			return;
		}
		if (parent != null) { displayGuiScreen(parent); }
		else {
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// Background
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 182, ySize);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(guiLeft + 182, guiTop, 256 - xSize + 182, 0, xSize - 182, ySize);
		int x0 = guiLeft + 4;
		int x1 = x0 + 59;
		int y0 = guiTop + 3;
		int y1 = y0 + 129;
		int y2 = y0 + 36;
		if (deal.getRarityColor() != 0) {
			int color = 0xA0000000 | deal.getRarityColor();
			drawGradientRect(x0 + 1, y0 + 2, x1, y2, 0x0, color);
			drawGradientRect(x0 + 1, y2, x1, y1, 0x0, color);
		}
		// Slots
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = deal.isCase() ? 1 : 0; slotId < 10; ++slotId) {
			int x = guiLeft + slotPoses[slotId][0];
			int y = guiTop + slotPoses[slotId][1];
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
		int color = new Color(0x80000000).getRGB();
		drawHorizontalLine(guiLeft + 170, guiLeft + xSize - 4, guiTop + 15, color);
		drawHorizontalLine(guiLeft + 4, guiLeft + 170, guiTop + 132, color);
		drawVerticalLine(guiLeft + 170, guiTop + 3, guiTop + ySize - 4, color);
		drawHorizontalLine(x0 + 1, guiLeft + 170, y0 + 1, color);
		drawHorizontalLine(x0 + 1, x1, y2, color);
		drawVerticalLine(x0, y0, guiTop + ySize - 4, color);
		drawVerticalLine(x1, y0, y1, color);
		// case
		if (deal.isCase() && objCase != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 30, guiTop + 21, 50.0f);
			if ((System.currentTimeMillis()) % 10000 < 2000) {
				float i = (float) ((System.currentTimeMillis()) % 2000);
				if (!start) {
					GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
					GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
					GlStateManager.scale(16.0f, -16.0f, 16.0f);
					GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
					if (i >= 1980) { start = true; }
				}
				else {
					if (i <= 20) { type = rnd.nextFloat() < 0.5f; }
					float rot;
					if (type) {
						if (i < 600) { rot = 0.033333f * i; }
						else if (i < 1700) { rot = - 0.027273f * i + 36.363636f; }
						else { rot = 0.033333f * i - 66.666666f; }
						GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
						GlStateManager.rotate(-75.0f + rot, 0.0f, 1.0f, 0.0f);
						GlStateManager.scale(16.0f, -16.0f, 16.0f);
						GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
					}
					else {
						GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
						GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
						GlStateManager.scale(16.0f, -16.0f, 16.0f);
						GlStateManager.callList(ModelBuffer.getDisplayList(objCase, Collections.singletonList("body"), materialTextures));
						if (i < 1500) { rot = 0.016667f * i; }
						else if (i < 1900) { rot = 25.0f; }
						else { rot = -0.25f * i + 500.0f; }
						GlStateManager.pushMatrix();
						GlStateManager.rotate(rot, 0.0f, 0.0f, 1.0f);
						GlStateManager.callList(ModelBuffer.getDisplayList(objCase, Collections.singletonList("top"), materialTextures));
						GlStateManager.popMatrix();
					}
				}
			}
			else {
				GlStateManager.rotate(-15.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
				GlStateManager.scale(16.0f, -16.0f, 16.0f);
				GlStateManager.callList(ModelBuffer.getDisplayList(objCase, null, materialTextures));
			}
			GlStateManager.popMatrix();
			drawHorizontalLine(guiLeft + 170, guiLeft + xSize - 4, guiTop + 143, color);
		}
		else {
			drawHorizontalLine(guiLeft + 170, guiLeft + xSize - 4, guiTop + 160, color);
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + menu.getSlot(1).xPos;
		int y = guiTop + menu.getSlot(1).yPos - 48;
		addLabel(new GuiNpcLabel(lId++, "market.product", x, y)
				.setHoverText("market.hover.product"));
		y = guiTop + menu.getSlot(1).yPos - 11;
		addLabel(new GuiNpcLabel(lId++, "market.barter", x, y)
				.setHoverText("market.hover.item"));
		// Type
		x = guiLeft + 67;
		y = guiTop + 6;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.type").appendText(":"), x, y + 1, 20, 12));
		addButton(new GuiNpcButton(4, x + 22, y, 80, 14, deal.isCase() ? 1 : 0, "enum.entity.item", "gui.case")
				.setHoverText("market.hover.deal.type"));
		addButton(new GuiNpcButton(66, guiLeft + xSize - 17, y - 2, 12, 12, "X")
				.setHoverText("hover.back"));
		ICustomDrop[] caseItems = deal.getCaseItems();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(102, 94); }
		scroll.canSearch(false);
		scroll.visible = deal.isCase();
		scroll.guiLeft = x;
		scroll.guiTop = y + 16;
		addScroll(scroll);
		if (deal.isCase()) {
			y += 111;
			java.util.List<String> list = new ArrayList<>();
			java.util.List<ItemStack> stacks = new ArrayList<>();
			LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
			int i = 0;
			for (ICustomDrop dropSet : caseItems) {
				list.add(((DropSet) dropSet).getKey());
				stacks.add(((DropSet) dropSet).getStackInSlot(0));
				hts.put(i++, ((DropSet) dropSet).getHover(player));
			}
			scroll.setUnsortedList(list).setStacks(stacks).setHoverTexts(hts);
			addButton(new GuiNpcButton(5, x, y, 32, 14, "gui.add")
					.setHoverText("market.hover.case.add"));
			addButton(new GuiNpcButton(6, x + 34, y, 32, 14, "gui.remove")
					.setIsEnable(scroll.hasSelected())
					.setHoverText("market.hover.case.del"));
			addButton(new GuiNpcButton(7, x + 68, y, 34, 14, "selectServer.edit")
					.setIsEnable(scroll.hasSelected())
					.setHoverText("market.hover.case.edit"));
		}
		// Dial settings
		x = guiLeft + 174;
		y = guiTop + 4;
		addLabel(new GuiNpcLabel(lId++, "marcet.deal.settings", x, y, 200, 12)
				.setHoverText("market.hover.deal.section"));
		addLabel(new GuiNpcLabel(lId++, "market.currency", x, (y += 14) + 1, 98, 12));
		addLabel(new GuiNpcLabel(lId++, CustomNpcs.displayCurrencies, x + 141, y + 1, 15, 12));
		addLabel(new GuiNpcLabel(lId++, CustomNpcs.displayDonation, x + 195, y + 1, 15, 12));
		addTextField(new GuiNpcTextField(0, this, x + 100, y, 39, 12, "" + deal.getMoney())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMoney())
				.setHoverText("market.hover.set.currency"));
		addTextField(new GuiNpcTextField(4, this, x + 154, y, 39, 12, "" + deal.getDonat())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getDonat())
				.setHoverText("market.hover.set.donat"));
		addLabel(new GuiNpcLabel(lId++, "drop.chance", x, (y += 16) + 1, 98, 12));
		addLabel(new GuiNpcLabel(lId++, "%", x + 155, y + 1, 10, 12));
		addTextField(new GuiNpcTextField(1, this, x + 100, y, 50, 12, "" + deal.getChance())
				.setMinMaxDefault(0, 100, deal.getChance())
				.setHoverText("market.hover.set.chance"));
		addLabel(new GuiNpcLabel(lId++, "quest.itemamount", x, (y += 16) + 1, 98, 12));
		addTextField(new GuiNpcTextField(2, this, x + 100, y, 40, 12, "" + deal.getMinCount())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMinCount())
				.setHoverText("market.hover.set.amount"));
		addLabel(new GuiNpcLabel(lId++, "<->", x + 143, y + 1, 15, 12));
		addTextField(new GuiNpcTextField(3, this, x + 160, y, 40, 12, "" + deal.getMaxCount())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMaxCount())
				.setHoverText("market.hover.set.amount"));
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreDamage", x, (y += 15) + 1, 98, 12));
		addButton(new GuiNpcButton(0, x + 100, y, 80, 14, deal.getIgnoreDamage() ? 1 : 0,
				"gui.ignoreDamage.0", "gui.ignoreDamage.1")
				.setHoverText("recipe.hover.damage"));
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreNBT", x, (y += 16) + 1, 98, 12));
		addButton(new GuiNpcButton(1, x + 100, y, 80, 14, deal.getIgnoreNBT() ? 1 : 0,
				"gui.ignoreNBT.0", "gui.ignoreNBT.1")
				.setHoverText("recipe.hover.nbt"));
		addLabel(new GuiNpcLabel(lId++, "availability.options", x, (y += 16) + 1, 98, 12));
		addButton(new GuiNpcButton(2, x + 100, y, 80, 14, "selectServer.edit")
				.setHoverText("availability.hover"));
		addLabel(new GuiNpcLabel(lId++, "market.case.color", x, (y += 16) + 1, 98, 12));
		addButton(new GuiColorButton(8, x + 100, y, 80, 14, deal.getRarityColor())
				.setHoverText("market.hover.deal.color"));
		materialTextures.clear();
		addButton(new GuiNpcCheckBox(10, x, y += 16, 200, 12, "market.deal.barter.true", "market.deal.barter.false", false)
				.setIsEnable(false));
		if (deal.isCase()) {
			addButton(new GuiNpcCheckBox(11, x, y += 16, 200, 12, "market.deal.show.case.info.true", "market.deal.show.case.info.false", deal.showInCase())
					.setIsEnable(deal.isCase()));
			addLabel(new GuiNpcLabel(lId, new TextComponentTranslation("gui.case").appendText(":"), x, (y += 17) + 1, 98, 12));
			addButton(new GuiNpcButton(9, x + 100, y, 80, 14, "selectServer.edit")
					.setHoverText("market.hover.deal.case"));
			objCase = deal.getCaseObjModel();
			if (objCase != null) {
				try {
					mc.getResourceManager().getResource(objCase);
                    objCase = Deal.defaultCaseOBJ;
                }
				catch (Exception e) { objCase = null; }
			}
			menu.setSlotPos(0, new int[] { -5000, -5000 });
			materialTextures.put("minecraft:entity/chest/christmas", deal.getCaseTexture().toString());
		}
		else {
			objCase = null;
			addButton(new GuiNpcButton(3, x, y + 16, 200, 14, deal.getType(),
					"market.deal.type.0", "market.deal.type.1", "market.deal.type.2")
					.setHoverText("market.hover.set.type"));
			menu.setSlotPos(0, slotPoses[0]);
		}
	}

	@Override
	public void save() {
		if (MarcetController.getInstance().deals.containsKey(deal.getId()) ||
				(deal.isCase() && deal.getCaseItems().length > 0) ||
				!deal.getProduct().isEmpty()) { Client.sendData(EnumPacketServer.TraderMarketSave, deal.write()); }
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 0: deal.setMoney(textField.getInteger()); break;
			case 1: deal.setChance(textField.getInteger()); break;
			case 2: deal.setCount(textField.getInteger(), deal.getMaxCount()); break;
			case 3: deal.setCount(deal.getMinCount(), textField.getInteger()); break;
			case 4: deal.setDonat(textField.getInteger()); break;
		}
		initGui();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { initGui(); }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (!deal.isCase() || !scroll.hasSelected()) { return; }
		SubGuiDropEdit.parent = null;
		SubGuiDropEdit.parentContainer = EnumGuiType.SetupTraderDeal;
		SubGuiDropEdit.parentData = new BlockPos(menu.marcet.getId(), deal.getId(), 0);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("InventoryType", 1);
		compound.setInteger("Marcet", menu.marcet.getId());
		compound.setInteger("Deal", deal.getId());
		compound.setInteger("DropSet", scroll.getSelect());
		NoppesUtil.requestOpenContainerGUI(EnumGuiType.SetupDrop, compound);
	}

}
