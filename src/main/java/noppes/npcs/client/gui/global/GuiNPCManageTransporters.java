package noppes.npcs.client.gui.global;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTransportSetup;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCManageTransporters
extends GuiContainerNPCInterface2
implements IGuiData, ISubGuiListener, ICustomScrollListener, ITextfieldListener {
	
	public final Map<String, Integer> dataCat, dataLoc;
	private GuiCustomScroll categoryes, locations;
	public String catSel, locSel;
	private boolean wait;
	private ContainerNPCTransportSetup container;
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

	public GuiNPCManageTransporters(EntityNPCInterface npc, ContainerNPCTransportSetup container) {
		super(npc, container);
		this.ySize = 200;
		this.catSel = "";
		this.locSel = "";
		this.dataCat = Maps.<String, Integer>newTreeMap();
		this.dataLoc = Maps.<String, Integer>newTreeMap();
		Client.sendData(EnumPacketServer.TransportCategoriesGet);
		this.setBackground("tradersetup.png");
		this.wait = true;
		this.container = container;
		if (TransportController.getInstance().categories.containsKey(container.catId)) {
			TransportCategory category = TransportController.getInstance().categories.get(container.catId);
			this.catSel = ((char) 167)+"7ID: "+container.catId+" \""+((char) 167)+"r"+(new TextComponentTranslation(category.title).getFormattedText())+((char) 167)+"7\"";
			this.dataCat.put(this.catSel, container.catId);
		}
		if (container.location.id>-1) {
			this.locSel = ((char) 167)+"7ID: "+container.location.id+" \""+((char) 167)+"r"+(new TextComponentTranslation(container.location.name).getFormattedText())+((char) 167)+"7\"";
		}
	}
	
	@Override
	public void initGui() {
		if (this.wait) {
			this.clear();
			super.initGui();
			return;
		}
		super.initGui();
		if (this.categoryes == null) {
			(this.categoryes = new GuiCustomScroll(this, 0)).setSize(100, 96);
		}
		this.categoryes.setListNotSorted(Lists.newArrayList(this.dataCat.keySet()));
		int x = this.guiLeft + 5, y = this.guiTop + 14;
		this.categoryes.guiLeft = x;
		this.categoryes.guiTop = y;
		this.addScroll(this.categoryes);
		if (!this.catSel.isEmpty()) { this.categoryes.setSelected(this.catSel); }
		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 5, y - 10));
		y += this.categoryes.height + 2;
		this.addButton(new GuiNpcButton(0, x, y, 49, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, x + 52, y, 49, 20, "gui.remove"));
		this.getButton(1).setEnabled(!this.catSel.isEmpty());
		
		if (this.locations == null) {
			(this.locations = new GuiCustomScroll(this, 1)).setSize(100, 96);
		}
		this.locations.setListNotSorted(Lists.newArrayList(this.dataLoc.keySet()));
		x += 102;
		y = this.guiTop + 14;
		this.locations.guiLeft = x;
		this.locations.guiTop = y;
		this.addScroll(this.locations);
		if (!this.locSel.isEmpty()) { this.locations.setSelected(this.locSel); }
		this.addLabel(new GuiNpcLabel(1, "gui.location", this.guiLeft + 113, y - 10));
		y += this.locations.height + 2;
		this.addButton(new GuiNpcButton(2, x, y, 100, 20, "transporter.travel"));
		this.getButton(2).setEnabled(!this.locSel.isEmpty());
		
		if (this.catSel.isEmpty()) { return; }
		TransportCategory cat = TransportController.getInstance().categories.get(this.dataCat.get(this.catSel));
		
		if (cat==null) { return; }
		y = this.guiTop + 191;
		this.addLabel(new GuiNpcLabel(2, "parameter.ikeysetting.catname", this.guiLeft + 216, y - 10));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 214, y, 132, 20, cat.title));
		
		if (this.locSel.isEmpty()) { return; }
		x = this.guiLeft + 214;
		y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(3, "market.barter", x + 2, y));
		y += 80;
		this.addLabel(new GuiNpcLabel(4, "market.currency", x + 2, y - 10));
		this.addTextField(new GuiNpcTextField(1, this, x, y, 50, 20, "" + this.container.location.money));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, Integer.MAX_VALUE, (int) this.container.location.money);
		
		y += 34;
		this.addLabel(new GuiNpcLabel(5, "parameter.ikeysetting.name", x + 2, y - 10));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, x, y, 202, 20, this.container.location.name));
		y += 34;
		this.addLabel(new GuiNpcLabel(6, "UUID NPC", x + 2, y - 11));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, x, y, 202, 20, this.container.location.npc==null ? "" : this.container.location.npc.toString()));
		
		x += 60;
		y = this.guiTop + 20;
		this.addLabel(new GuiNpcLabel(7, "parameter.world", x + 2, y - 11));
		this.addLabel(new GuiNpcLabel(8, "ID:", x + 2, y + 6));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, x + 15, y, 42, 20, ""+this.container.location.dimension));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, this.container.location.dimension);
		
		y += 34;
		this.addLabel(new GuiNpcLabel(9, "parameter.position", x + 2, y - 11));
		String idt = "ID:"+this.container.location.id;
		this.addLabel(new GuiNpcLabel(10, idt, this.guiLeft + this.xSize - this.fontRenderer.getStringWidth(idt) - 4, this.guiTop + 8));
		for (int i = 0; i < 3; i++) {
			int v = i == 0 ? this.container.location.pos.getX() : i == 1 ? this.container.location.pos.getY() : this.container.location.pos.getZ();
			this.addTextField(new GuiNpcTextField(5 + i, this, this.fontRenderer, x + 1 + i * 48, y, 44, 20, ""+v));
			this.getTextField(5 + i).setNumbersOnly();
			this.getTextField(5 + i).setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, v);
		}
		y += 34;
		this.addLabel(new GuiNpcLabel(11, "gui.type", x + 2, y - 11));
		this.addButton(new GuiNpcButton(3, x, y, 137, 20, new String[] { "transporter.discovered", "transporter.start", "transporter.interaction" }, this.container.location.type));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		TransportCategory cat = null;
		TransportLocation loc = this.container.location;
		if (!this.catSel.isEmpty()) { 
			cat = TransportController.getInstance().categories.get(this.dataCat.get(this.catSel));
		}
		switch (button.id) {
			case 0: { // add cat
				this.setSubGui(new SubGuiEditText(0, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 1: { // del cat
				if (cat == null) { return; }
				Client.sendData(EnumPacketServer.TransportCategoryRemove, cat.id);
				break;
			}
			case 2: { // tp
				this.transfer(loc);
				break;
			}
			case 3: {
				if (loc == null) { return; }
				loc.type = button.getValue();
				break;
			}
		}
		
	}

	private void transfer(TransportLocation loc) {
		if (loc == null) { return; }
		try { Client.sendData(EnumPacketServer.TeleportTo, loc.dimension, loc.pos.getX(), loc.pos.getY(), loc.pos.getZ()); }
		catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		if (!this.locSel.isEmpty()) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			for (int slotId = 0; slotId < 10; ++slotId) {
				int x = this.guiLeft + this.container.getSlot(slotId).xPos;
				int y = this.guiTop + this.container.getSlot(slotId).yPos;
				this.mc.renderEngine.bindTexture(this.slot);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
			}
		}
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!this.wait && !this.catSel.isEmpty()) {
			this.drawHorizontalLine(this.guiLeft + 212, this.guiLeft + this.xSize - 3, this.guiTop + 178, 0x80000000);
			this.drawVerticalLine(this.guiLeft + 211, this.guiTop + 4, this.guiTop + this.ySize + 12, 0x80000000);
			if (!this.locSel.isEmpty()) {
				this.drawVerticalLine(this.guiLeft + 271, this.guiTop + 4, this.guiTop + 111, 0x80000000);
				this.drawHorizontalLine(this.guiLeft + 212, this.guiLeft + 270, this.guiTop + 110, 0x80000000);
			}
			this.drawVerticalLine(this.guiLeft + 418, this.guiTop + 4, this.guiTop + this.ySize + 12, 0x80000000);
		}
		if (this.subgui != null || this.wait) { return; }
		if (this.subgui !=null || !CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.del", "\""+this.catSel+"\"").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.type").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.cat.name").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.money").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.transport.loc.name").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.entity.uuid").getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.dimension.id").getFormattedText());
		} else if (this.getTextField(5)!=null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posx").getFormattedText());
		} else if (this.getTextField(6)!=null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posy").getFormattedText());
		} else if (this.getTextField(7)!=null && this.getTextField(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posz").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (this.catSel.isEmpty() || this.getTextField(0)==null) { return; }
		TransportCategory cat = TransportController.getInstance().categories.get(this.dataCat.get(this.catSel));
		if (cat==null) { return; }
		cat.title = this.getTextField(0).getText();
		if (this.locSel.isEmpty()  || this.getTextField(1)==null) {
			NBTTagCompound compound = new NBTTagCompound();
			cat.writeNBT(compound);
			Client.sendData(EnumPacketServer.TransportCategorySave, compound);
			return;
		}
		this.container.location.money = this.getTextField(1).getInteger();
		this.container.location.name = this.getTextField(2).getText();
		this.container.location.dimension = this.getTextField(4).getInteger();
		this.container.location.pos = new BlockPos(this.getTextField(5).getInteger(), this.getTextField(6).getInteger(), this.getTextField(7).getInteger());
		try { this.container.location.npc = UUID.fromString(this.getTextField(3).getText()); } catch (Exception e) { }
		this.container.location.type = this.getButton(3).getValue();
		Client.sendData(EnumPacketServer.TransportCategorySave, this.container.saveTransport(cat));
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
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		switch(scroll.id) {
			case 0: {
				if (this.catSel.equals(scroll.getSelected()) || !this.dataCat.containsKey(scroll.getSelected())) { return; }
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, this.container.location.id, this.dataCat.get(scroll.getSelected()), 0);
				this.wait = true;
				this.initGui();
				break;
			}
			case 1: {
				if (this.locSel.equals(scroll.getSelected()) || !this.dataLoc.containsKey(scroll.getSelected()) || !this.dataCat.containsKey(this.catSel)) { return; }
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, this.dataLoc.get(scroll.getSelected()), this.dataCat.get(this.catSel), 0);
				this.wait = true;
				this.initGui();
				break;
			}
		}
		
		this.setGuiData(null);
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (scroll.id==1 && scroll.hasSelected()) {
			this.transfer(this.container.location);
		}
	}
	
	@Override
	public void unFocused(GuiNpcTextField textField) {
		TransportCategory cat = null;
		TransportLocation loc = this.container.location;
		if (!this.catSel.isEmpty()) { 
			cat = TransportController.getInstance().categories.get(this.dataCat.get(this.catSel));
		}
		switch(textField.getId()) {
			case 0: { // cat name
				if (textField.getText().isEmpty() || cat==null) { return; }
				cat.title = textField.getText();
				break;
			}
			case 1: { // money
				if (textField.getText().isEmpty() || loc==null) { return; }
				loc.money = textField.getInteger();
				break;
			}
			case 2: { // loc name
				if (textField.getText().isEmpty() || loc==null) { return; }
				loc.name = textField.getText();
				break;
			}
			case 3: { // npc uuid
				if (textField.getText().isEmpty() || loc==null) { return; }
				try { loc.npc = UUID.fromString(textField.getText()); }
				catch (Exception e) { textField.setText(loc.npc==null ? "" : loc.npc.toString()); }
				break;
			}
			case 4: { // dim ID
				if (textField.getText().isEmpty() || loc==null) { return; }
				int dimId = textField.getInteger();
				if (!TransportController.getInstance().worldIDs.contains(dimId)) {
					textField.setText(""+loc.dimension);
					return;
				}
				loc.dimension = dimId;
				break;
			}
			case 5: { // X
				if (textField.getText().isEmpty() || loc==null) { return; }
				int y = loc.pos.getY();
				int z = loc.pos.getZ();
				loc.pos = new BlockPos(textField.getInteger(), y, z);
				break;
			}
			case 6: { // Y
				if (textField.getText().isEmpty() || loc==null) { return; }
				int x = loc.pos.getX();
				int z = loc.pos.getZ();
				loc.pos = new BlockPos(x, textField.getInteger(), z);
				break;
			}
			case 7: { // Z
				if (textField.getText().isEmpty() || loc==null) { return; }
				int x = loc.pos.getX();
				int y = loc.pos.getY();
				loc.pos = new BlockPos(x, y, textField.getInteger());
				break;
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		int selCatId = -1;
		if (!this.catSel.isEmpty()) {
			selCatId = this.dataCat.get(this.catSel);
		}
		this.dataCat.clear();
		this.dataLoc.clear();
		for (int catId : TransportController.getInstance().categories.keySet()) {
			TransportCategory cat = TransportController.getInstance().categories.get(catId);
			String catKey = ((char) 167)+"7ID: "+catId+" \""+((char) 167)+"r"+(new TextComponentTranslation(cat.title).getFormattedText())+((char) 167)+"7\"";
			this.dataCat.put(catKey, catId);
			if (catId == selCatId) {
				for (int locId : cat.locations.keySet()) {
					String locKey = ((char) 167)+"7ID: "+locId+" \""+((char) 167)+"r"+(new TextComponentTranslation(cat.locations.get(locId).name).getFormattedText())+((char) 167)+"7\"";
					this.dataLoc.put(locKey, locId);
				}
			}
		}
		if (!this.catSel.isEmpty() && !this.dataCat.containsKey(this.catSel)) { this.catSel = ""; }
		if (!this.locSel.isEmpty() && !this.dataLoc.containsKey(this.locSel)) { this.locSel = ""; }
		this.wait = false;
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).text[0].isEmpty()) { return; }
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("CategoryId", -1);
		compound.setString("CategoryTitle", ((SubGuiEditText) subgui).text[0]);
		Client.sendData(EnumPacketServer.TransportCategorySave, compound);
	}
	
}
