package noppes.npcs.client.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerBuilderSettings;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.schematics.Blueprint;
import noppes.npcs.schematics.BlueprintUtil;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.BuilderData;

public class GuiBuilderSetting
extends GuiContainerNPCInterface
implements ICustomScrollListener, ITextfieldListener {

	ContainerBuilderSettings container;
	private ResourceLocation background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/bgfilled.png");
	private ResourceLocation inventory = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private ResourceLocation invRes = new ResourceLocation("textures/gui/container/inventory.png");

	private int maxRange;
	private GuiCustomScroll schematics;
	private BuilderData builder;
	private Map<String, SchematicWrapper> files;
	private static Map<String, SchematicWrapper> basefiles;
	
	public GuiBuilderSetting(ContainerBuilderSettings cont, int id) {
		super(null, cont);
		this.container = cont;
		this.closeOnEsc = true;
		this.xSize = 228;
		this.ySize = 216;
		this.builder = cont.builderData;
		this.maxRange = 10;
		GuiBuilderSetting.basefiles = Maps.<String, SchematicWrapper>newTreeMap();
		SchematicController sData = SchematicController.Instance;
		for (String name : sData.included) {
			InputStream stream = MinecraftServer.class.getResourceAsStream("/assets/" + CustomNpcs.MODID + "/schematics/" + name);
			if (stream == null) {
				File file = new File(SchematicController.getDir(), name);
				if (!file.exists()) { continue; }
				try { stream = new FileInputStream(file); } catch (FileNotFoundException e) { continue;}
			}
			try {
				NBTTagCompound compound = CompressedStreamTools.readCompressed(stream);
				stream.close();
				if (name.toLowerCase().endsWith(".blueprint")) {
					if (compound.getKeySet().isEmpty() || !compound.hasKey("size_x", 2) || !compound.hasKey("size_y", 2) || !compound.hasKey("size_z", 2)) { continue; }
					if (!ClientProxy.playerData.game.op && (int) compound.getShort("size_x") * (int) compound.getShort("size_y") * (int) compound.getShort("size_z") > CustomNpcs.maxBuilderBlocks) { continue; }
					Blueprint bp = BlueprintUtil.readBlueprintFromNBT(compound);
					bp.setName(name);
					GuiBuilderSetting.basefiles.put(name, new SchematicWrapper(bp));
				}
				if (compound.getKeySet().isEmpty() || !compound.hasKey("Width", 2) || !compound.hasKey("Length", 2) || !compound.hasKey("Height", 2)) { continue; }
				if ((int) compound.getShort("Width") * (int) compound.getShort("Length") * (int) compound.getShort("Height") > CustomNpcs.maxBuilderBlocks) { continue; }
				Schematic schema = new Schematic(name);
				schema.load(compound);
				GuiBuilderSetting.basefiles.put(name, new SchematicWrapper(schema));
			}
			catch (IOException e) { }
		}
		this.files = Maps.<String, SchematicWrapper>newTreeMap();
		this.files.putAll(GuiBuilderSetting.basefiles);
		File schematicDir = SchematicController.getDir();
		if (schematicDir.exists()) {
			for (File f : schematicDir.listFiles()) {
				if (!f.isFile() || !f.getName().endsWith(".schematic")) { continue; }
				try {
					NBTTagCompound compound = CompressedStreamTools.readCompressed(new FileInputStream(f));
					if (compound.getKeySet().isEmpty() || !compound.hasKey("Width", 2) || !compound.hasKey("Length", 2) || !compound.hasKey("Height", 2)) { continue; }
					if (!ClientProxy.playerData.game.op && (int) compound.getShort("Width") * (int) compound.getShort("Length") * (int) compound.getShort("Height") > CustomNpcs.maxBuilderBlocks) { continue; }
					Schematic schema = new Schematic(f.getName());
					schema.load(compound);
					this.files.put(f.getName(), new SchematicWrapper(schema));
				}
				catch (Exception e) { }
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.builder==null) { return; }
		this.maxRange = ClientProxy.playerData.game.op ? 100 : 10;
		int type = this.builder.type;
		GuiNpcTextField textField;
		GuiNpcCheckBox checkBox;
		if (type>2) {
			if (this.schematics == null) { (this.schematics = new GuiCustomScroll(this, 0)).setSize(110, 197); }
			this.schematics.setList(Lists.newArrayList(this.files.keySet()));
			this.schematics.guiLeft = this.guiLeft + 5;
			this.schematics.guiTop = this.guiTop + 14;
			if (!this.builder.schematicaName.isEmpty()) {
				int i = 0;
				for (String key : this.schematics.getList()) {
					String fName = key;
					if (key.endsWith(".schematic")) { fName = key.substring(0, key.lastIndexOf(".schematic")); }
					else if (key.endsWith(".blueprint")) { fName = key.substring(0, key.lastIndexOf(".blueprint")); }
					if (fName.equals(this.builder.schematicaName)) {
						this.schematics.selected = i;
						break;
					}
					i++;
				}
				if (i==this.schematics.getList().size()) { this.schematics.selected = -1; }
			}
			this.addScroll(this.schematics);
			this.addLabel(new GuiNpcLabel(6, new TextComponentTranslation("gui.name").getFormattedText()+":", this.guiLeft + 120, this.guiTop + 40));
			textField = new GuiNpcTextField(10, this, this.guiLeft+120, this.guiTop+54, 99, 15, ""+this.builder.schematicaName);
			this.addTextField(textField);
		}
		this.addButton(new GuiButtonBiDirectional(0, this.guiLeft+120, this.guiTop+14, 99, 20, new String[] { "gui.remove", "gui.set", "gui.replace", "gui.build", "gui.save" }, type));
		this.addButton(new GuiNpcButton(61, this.guiLeft+173, this.guiTop+191, 51, 20, "gui.save"));

		if (type<3) {
			this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("gui.help.block").getFormattedText()+" [?]:", this.guiLeft + 4, this.guiTop + 4));
			this.addLabel(new GuiNpcLabel(3, new TextComponentTranslation("gui.area").getFormattedText()+":", this.guiLeft + 120, this.guiTop + 38));
			
			this.addButton(new GuiButtonBiDirectional(1, this.guiLeft+120, this.guiTop+69, 99, 20, new String[] { "builder.fasing.0", "builder.fasing.1", "builder.fasing.2" }, this.builder.fasing));
			
			for (int i=1; i<10; i++) { // Blocks
				textField = new GuiNpcTextField(i, this, this.guiLeft+28+(i/6)*54, this.guiTop+17+((i<6 ? 0 : -5)+i-1)*24, 28, 15, ""+(this.builder.chances.containsKey(i) ? this.builder.chances.get(i) : ""));
				textField.setNumbersOnly();
				textField.setMinMaxDefault(1, 100, this.builder.chances.containsKey(i) ? this.builder.chances.get(i) : 100);
				this.addTextField(textField);
			}
			for (int i=0; i<3; i++) { // Region
				textField = new GuiNpcTextField(i+10, this, this.guiLeft+120+i*34, this.guiTop+50, 30, 15, ""+this.builder.region[i]);
				textField.setNumbersOnly();
				textField.setMinMaxDefault(1, this.maxRange, this.builder.region[i]);
				this.addTextField(textField);
			}
		}
		else {
			this.addLabel(new GuiNpcLabel(5, new TextComponentTranslation("gui.file.list").getFormattedText()+" [?]:", this.guiLeft + 4, this.guiTop + 4));
		}
		if (type<4) {
			checkBox = new GuiNpcCheckBox(5, this.guiLeft+172+(type==3 ? -52 : 0), this.guiTop+130+(type==3 ? -60 : 0), 70, 15, "tile.air.name");
			checkBox.setSelected(this.builder.addAir);
			this.addButton(checkBox);
			if (type==2 || type==3) {
				checkBox = new GuiNpcCheckBox(6, this.guiLeft+172+(type==3 ? -52 : 0), this.guiTop+145+(type==3 ? -60 : 0), 70, 15, "drop.type.all");
				checkBox.setSelected(this.builder.replaseAir);
				this.addButton(checkBox);
			}
			if (type==3) {
				checkBox = new GuiNpcCheckBox(7, this.guiLeft+120, this.guiTop+100, 70, 15, "gui.solid");
				checkBox.setSelected(this.builder.isSolid);
				this.addButton(checkBox);
			}
		}
		
		this.addLabel(new GuiNpcLabel(1, "ID:"+this.builder.id, this.guiLeft + 120, this.guiTop + 4));
		if (type==2) {
			this.addLabel(new GuiNpcLabel(4, "_[?]_", this.guiLeft + 88, this.guiTop + 116));
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (this.mc.renderEngine!=null) {
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			// Back
			this.mc.renderEngine.bindTexture(this.background);
			this.drawTexturedModalRect(0, 0, 0, 0, this.xSize-4, this.ySize-4);
			this.drawTexturedModalRect(this.xSize-4, 0, 252, 0, 4, this.ySize-4);
			this.drawTexturedModalRect(0, this.ySize-4, 0, 252, this.xSize-4, 4);
			this.drawTexturedModalRect(this.xSize-4, this.ySize-4, 252, 252, 4, 4);
			if (this.builder==null) {
				GlStateManager.popMatrix();
				return;
			}
			// Slots
			if (this.builder.type<3) {
				// Region
				Gui.drawRect(140, 92, 200, 130, 0xFF404040);
				Gui.drawRect(141, 93, 199, 129, 0xFF606060);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.mc.renderEngine.bindTexture(this.invRes);
				this.drawTexturedModalRect(140, 120, 73, 220, 16, 10);
				// Borders
				this.drawHorizontalLine(4, 170, 132, 0xFF808080);
				this.drawVerticalLine(170, 131, 212, 0xFF808080);
				if (this.builder.type==2) {
					this.drawHorizontalLine(58, 112, 108, 0xFF808080);
					this.drawVerticalLine(58, 108, 132, 0xFF808080);
				}
				
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.translate(7.0f, 135.0f, 0.0f);
				this.mc.renderEngine.bindTexture(this.inventory);
				this.drawTexturedModalRect(0, 0, 0, 0, 162, 76); // player inventory
				this.mc.renderEngine.bindTexture(this.slot);
				GlStateManager.translate(0.0f, -119.0f, 0.0f);
				// Slots
				for (int i=1; i<10; i++) {
					this.drawTexturedModalRect((i/6)*54, ((i<6 ? 0 : -5)+i-1)*24, 0, 0, 18, 18); // main
				}
				if (this.builder.type==2) {
					this.drawTexturedModalRect(54, 96, 0, 0, 18, 18);
				}
				this.drawHorizontalLine(-3, 106, -2, 0xFF808080);
				this.drawVerticalLine(106, -13, 117, 0xFF808080);
				GlStateManager.popMatrix();
				
				// Show Region
				float r=1.0f, g=0.0f, b=0.0f;
				if (this.builder.type==1) { r=0.0f; g=1.0f; b=1.0f; }
				else if (this.builder.type==2) { r=1.0f; g=0.0f; b=1.0f; }
				float size = (float) this.builder.region[2] + (float) (this.builder.region[0] + this.builder.region[1]) /2.0f;
				float scale = size <=0.0f ? 7.0f : 36.0f / size;
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(1.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				GlStateManager.translate(this.guiLeft+170, this.guiTop+111, 100.0f);
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
				GlStateManager.rotate(30.0f, 1.0f, 0.0f, 1.0f);
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d, -0.5d, -0.5d, 0.5d, 0.5d, 0.5d)), 1.0f, 1.0f, 1.0f, 1.0f);
				if (this.builder.fasing==0) { GlStateManager.translate(0.0f, 0.0f, 1.01f); }
				else if (this.builder.fasing==2) { GlStateManager.translate(0.0f, 0.0f, -1.1f); }
				RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d*(double)this.builder.region[0],
						-0.5d*(double)this.builder.region[1],
						-0.5d*(double)this.builder.region[2],
						0.5d*(double)this.builder.region[0],
						0.5d*(double)this.builder.region[1],
						0.5d*(double)this.builder.region[2])), r, g, b, 1.0f);
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			else {
				// Borders
				this.drawHorizontalLine(118, 223, 36, 0xFF808080);
				this.drawVerticalLine(117, 3, 212, 0xFF808080);
				GlStateManager.popMatrix();
				
			}
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.builder==null || !CustomNpcs.showDescriptions || this.subgui!=null) { return; }
		// hover text
		int type = this.builder.type;
		int t = 0, j=0;
		for (int i=1; i<10; i++) {
			if (this.getTextField(i)!=null && this.getTextField(i).isInteger()) { t += this.getTextField(i).getInteger(); j++; }
		}
		if (this.builder.addAir) { t += t / (float) j;}
		for (int i=1; i<10; i++) {
			if (this.getTextField(i)!=null && this.getTextField(i).isMouseOver()) {
				float c = 0.0f;
				if (this.getTextField(i).isInteger()) {
					if (type==0) { c = (float) this.getTextField(i).getInteger() / 100.0f; }
					else { c = (float) this.getTextField(i).getInteger() / t; }
				}
				c = (float) (Math.round((double) c * 1000.d)/10.d);
				this.setHoverText(new TextComponentTranslation("builder.hover.chance."+type, ""+c).getFormattedText());
				return;
			}
		}
		if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("scale.width").getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("scale.depth").getFormattedText());
		} else if (this.getTextField(12)!=null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic.height").getFormattedText());
		} else if (this.getLabel(0)!=null && this.getLabel(0).hovered) {
			this.setHoverText("builder.hover.blocks."+type);
		} else if (this.getLabel(4)!=null && this.getLabel(4).hovered) {
			this.setHoverText("builder.hover.main.block");
		}  else if (this.getLabel(5)!=null && this.getLabel(5).hovered) {
			this.setHoverText(new TextComponentTranslation("builder.hover.list", ""+this.maxRange).getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText("builder.hover.type."+type);
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText("builder.hover.fasing");
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic.length").getFormattedText()+"<br>"+new TextComponentTranslation("gui.limitation", "1", "10").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation(type<3 ? "scale.depth" : "schematic.width").getFormattedText()+"<br>"+new TextComponentTranslation("gui.limitation", "1", "10").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic.height").getFormattedText()+"<br>"+new TextComponentTranslation("gui.limitation", "1", "10").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic"+(type==3 ? ".schem" : "")+".air").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic"+(type==3 ? ".schem" : "")+".replace").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("schematic.schem.solid").getFormattedText());
		} else if (this.getButton(61)!=null && this.getButton(61).isMouseOver()) {
			this.setHoverText("hover.save");
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // Type
				if (this.builder==null) { return; }
				this.builder.type = button.getValue();
				Client.sendData(EnumPacketServer.OpenBuilder, this.builder.getNbt());
				GuiNpcTextField.unfocus();
				this.player.closeScreen();
				this.displayGuiScreen(null);
				this.mc.setIngameFocus();
				this.onGuiClosed();
				break;
			}
			case 1: { // Fasing
				if (this.builder==null) { return; }
				this.builder.fasing = button.getValue();
				break;
			}
			case 2: { // reg[0]
				if (this.builder==null) { return; }
				this.builder.region[0] = button.getValue()+1;
				break;
			}
			case 3: { // reg[1]
				if (this.builder==null) { return; }
				this.builder.region[1] = button.getValue()+1;
				break;
			}
			case 4: { // reg[2]
				if (this.builder==null) { return; }
				this.builder.region[2] = button.getValue()+1;
				break;
			}
			case 5: { // add Air
				if (this.builder==null) { return; }
				this.builder.addAir = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 6: { // replase Air
				if (this.builder==null) { return; }
				this.builder.replaseAir = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 7: { // is Solid
				if (this.builder==null) { return; }
				this.builder.isSolid = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 61: { // exit
				this.close();
				break;
			}
		}
	}
			
	@Override
	public void save() {
		if (this.builder==null || this.container==null) { return; }
		this.container.save();
		Client.sendData(EnumPacketServer.BuilderSetting, this.builder.getNbt());
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.builder==null) { return; }
		if (textField.getId()<10) {
			if (textField.getText().isEmpty()) { return; }
			if (this.builder.inv.getStackInSlot(textField.getId()).isEmpty()) {
				textField.setText("");
				return;
			}
			this.builder.chances.put(textField.getId(), textField.getInteger());
			return;
		}
		if (this.builder.type==3 || this.builder.type==4) {
			if (textField.getId()==10) {
				this.builder.schematicaName = textField.getText();
				this.initGui();
			}
		} else {
			int pos = textField.getId()-10;
			int value = textField.getInteger();
			if (value > this.maxRange) { value = this.maxRange; }
			if (value<=0) { value = 1; }
			this.builder.region[pos] = value;
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		// File List
		this.builder.schematicaName = scroll.getSelected();
		if (this.builder.schematicaName.endsWith(".schematic")) {
			this.builder.schematicaName = this.builder.schematicaName.substring(0, this.builder.schematicaName.lastIndexOf(".schematic"));
		}
		else if (this.builder.schematicaName.endsWith(".blueprint")) {
			this.builder.schematicaName = this.builder.schematicaName.substring(0, this.builder.schematicaName.lastIndexOf(".blueprint"));
		}
		SchematicWrapper schema = SchematicController.Instance.getSchema(this.builder.schematicaName+".schematic");
		if (schema!=null) {
			this.builder.region[0] = schema.schema.getLength();
			this.builder.region[1] = schema.schema.getWidth();
			this.builder.region[2] = schema.schema.getHeight();
		}
		GuiNpcTextField textField = this.getTextField(10);
		if (textField!=null) { textField.setText(this.builder.schematicaName); }
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {  }

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		if (slotIn==null) { return; }
		if (slotId>=36) {
			int id = slotId-(this.builder.type==2 ? 36 : 35);
			GuiNpcTextField textField = this.getTextField(id);
			if (textField==null) { return; }
			if (slotIn.getStack().isEmpty()) { textField.setText(""); }
			else {
				if (!this.builder.chances.containsKey(id)) { this.builder.chances.put(id, 100); }
				textField.setText(""+this.builder.chances.get(id)); 
			}
		}
	}
	
}
