package noppes.npcs.client.gui;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

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
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerBuilderSettings;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.schematics.Blueprint;
import noppes.npcs.schematics.BlueprintUtil;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.BuilderData;

import javax.annotation.Nonnull;

public class GuiBuilderSetting
extends GuiContainerNPCInterface
implements ICustomScrollListener, ITextfieldListener {

	private static final Map<String, SchematicWrapper> baseFiles = new TreeMap<>();
	ContainerBuilderSettings container;
	private final ResourceLocation background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/bgfilled.png");
	private final ResourceLocation inventory = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");

	private final ResourceLocation invRes = new ResourceLocation("textures/gui/container/inventory.png");
	private int maxRange = 10;
	private GuiCustomScroll schematics;
	private final BuilderData builder;
	private final Map<String, SchematicWrapper> files = new TreeMap<>();

	public GuiBuilderSetting(ContainerBuilderSettings cont) {
		super(null, cont);
		closeOnEsc = true;
		xSize = 228;
		ySize = 216;

		container = cont;
		builder = cont.builderData;
		for (String name : SchematicController.included) {
			InputStream stream = MinecraftServer.class.getResourceAsStream("/assets/" + CustomNpcs.MODID + "/schematics/" + name);
			if (stream == null) {
				File file = new File(SchematicController.getDir(), name);
				if (!file.exists()) {
					continue;
				}
				try {
					stream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					continue;
				}
			}
			try {
				NBTTagCompound compound = CompressedStreamTools.readCompressed(stream);
				stream.close();
				if (name.toLowerCase().endsWith(".blueprint")) {
					if (compound.getKeySet().isEmpty() || !compound.hasKey("size_x", 2) || !compound.hasKey("size_y", 2)
							|| !compound.hasKey("size_z", 2)) {
						continue;
					}
					if (!ClientProxy.playerData.game.op
							&& (int) compound.getShort("size_x") * (int) compound.getShort("size_y")
									* (int) compound.getShort("size_z") > CustomNpcs.MaxBuilderBlocks) {
						continue;
					}
					Blueprint bp = BlueprintUtil.readBlueprintFromNBT(compound);
                    if (bp != null) {
						bp.setName(name);
						GuiBuilderSetting.baseFiles.put(name, new SchematicWrapper(bp));
					}
				}
				if (compound.getKeySet().isEmpty() || !compound.hasKey("Width", 2) || !compound.hasKey("Length", 2)
						|| !compound.hasKey("Height", 2)) {
					continue;
				}
				if ((int) compound.getShort("Width") * (int) compound.getShort("Length")
						* (int) compound.getShort("Height") > CustomNpcs.MaxBuilderBlocks) {
					continue;
				}
				Schematic schema = new Schematic(name);
				schema.load(compound);
				GuiBuilderSetting.baseFiles.put(name, new SchematicWrapper(schema));
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}
		files.putAll(GuiBuilderSetting.baseFiles);
		File schematicDir = SchematicController.getDir();
		if (schematicDir.exists()) {
			for (File f : Objects.requireNonNull(schematicDir.listFiles())) {
				if (!f.isFile() || !f.getName().endsWith(".schematic")) {
					continue;
				}
				try {
					NBTTagCompound compound = CompressedStreamTools.readCompressed(Files.newInputStream(f.toPath()));
					if (compound.getKeySet().isEmpty() || !compound.hasKey("Width", 2) || !compound.hasKey("Length", 2)
							|| !compound.hasKey("Height", 2)) {
						continue;
					}
					if (!ClientProxy.playerData.game.op
							&& (int) compound.getShort("Width") * (int) compound.getShort("Length")
									* (int) compound.getShort("Height") > CustomNpcs.MaxBuilderBlocks) {
						continue;
					}
					Schematic schema = new Schematic(f.getName());
					schema.load(compound);
					files.put(f.getName(), new SchematicWrapper(schema));
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: { // Facing
				if (builder == null) {
					return;
				}
				builder.facing = button.getValue();
				break;
			}
			case 2: { // reg[0]
				if (builder == null) {
					return;
				}
				builder.region[0] = button.getValue() + 1;
				break;
			}
			case 3: { // reg[1]
				if (builder == null) {
					return;
				}
				builder.region[1] = button.getValue() + 1;
				break;
			}
			case 4: { // reg[2]
				if (builder == null) {
					return;
				}
				builder.region[2] = button.getValue() + 1;
				break;
			}
			case 5: { // add Air
				if (builder == null) {
					return;
				}
				builder.addAir = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 6: { // replace Air
				if (builder == null) {
					return;
				}
				builder.replaceAir = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 7: { // is Solid
				if (builder == null) {
					return;
				}
				builder.isSolid = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		// Back
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, xSize - 4, ySize - 4);
		drawTexturedModalRect(xSize - 4, 0, 252, 0, 4, ySize - 4);
		drawTexturedModalRect(0, ySize - 4, 0, 252, xSize - 4, 4);
		drawTexturedModalRect(xSize - 4, ySize - 4, 252, 252, 4, 4);
		if (builder == null) {
			GlStateManager.popMatrix();
			return;
		}
		int lineColor = new Color(0xFF808080).getRGB();
		// Slots
		if (builder.getType() < 3) {
			// Region
			Gui.drawRect(140, 92, 200, 130, new Color(0xFF404040).getRGB());
			Gui.drawRect(141, 93, 199, 129, new Color(0xFF606060).getRGB());
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(invRes);
			drawTexturedModalRect(140, 120, 73, 220, 16, 10);
			// Borders
			drawHorizontalLine(4, 170, 132, lineColor);
			drawVerticalLine(170, 131, 212, lineColor);
			if (builder.getType() == 2) {
				drawHorizontalLine(58, 112, 108, lineColor);
				drawVerticalLine(58, 108, 132, lineColor);
			}

			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(7.0f, 135.0f, 0.0f);
			mc.getTextureManager().bindTexture(inventory);
			drawTexturedModalRect(0, 0, 0, 0, 162, 76); // player inventory
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			GlStateManager.translate(0.0f, -119.0f, 0.0f);
			// Slots
			for (int i = 1; i < 10; i++) {
				drawTexturedModalRect((i / 6) * 54, ((i < 6 ? 0 : -5) + i - 1) * 24, 0, 0, 18, 18); // main
			}
			if (builder.getType() == 2) { drawTexturedModalRect(54, 96, 0, 0, 18, 18); }
			drawHorizontalLine(-3, 106, -2, lineColor);
			drawVerticalLine(106, -13, 117, lineColor);
			GlStateManager.popMatrix();

			// Show Region
			float r = 1.0f, g = 0.0f, b = 0.0f;
			if (builder.getType() == 1) {
				r = 0.0f;
				g = 1.0f;
				b = 1.0f;
			} else if (builder.getType() == 2) {
				g = 0.0f;
				b = 1.0f;
			}
			float size = (float) builder.region[2] + (float) (builder.region[0] + builder.region[1]) / 2.0f;
			float scale = size <= 0.0f ? 7.0f : 36.0f / size;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.translate(guiLeft + 170, guiTop + 111, 100.0f);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(30.0f, 1.0f, 0.0f, 1.0f);
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d, -0.5d, -0.5d, 0.5d, 0.5d, 0.5d)), 1.0f, 1.0f, 1.0f, 1.0f);
			if (builder.facing == 0) {
				GlStateManager.translate(0.0f, 0.0f, 1.01f);
			} else if (builder.facing == 2) {
				GlStateManager.translate(0.0f, 0.0f, -1.1f);
			}
			RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(-0.5d * (double) builder.region[0],
					-0.5d * (double) builder.region[1], -0.5d * (double) builder.region[2],
					0.5d * (double) builder.region[0], 0.5d * (double) builder.region[1],
					0.5d * (double) builder.region[2])), r, g, b, 1.0f);
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		} else {
			// Borders
			drawHorizontalLine(118, 223, 36, lineColor);
			drawVerticalLine(117, 3, 212, lineColor);
			GlStateManager.popMatrix();

		}
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
        if (slotId >= 36) {
			int id = slotId - (builder.getType() == 2 ? 36 : 35);
			IGuiNpcTextField textField = getTextField(id);
			if (textField == null) {
				return;
			}
			if (slotIn.getStack().isEmpty()) {
				textField.setFullText("");
			} else {
				if (!builder.chances.containsKey(id)) { builder.chances.put(id, 100); }
				textField.setFullText("" + builder.chances.get(id));
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (builder == null) {
			return;
		}
		maxRange = ClientProxy.playerData.game.op ? 100 : 10;
		int type = builder.getType();
		GuiNpcLabel label;
		GuiNpcButton button;
		GuiNpcTextField textField;
		int y = guiTop + 4;
		if (builder.getID() > -1) {
			addLabel(new GuiNpcLabel(1, "ID:" + builder.getID(), guiLeft + 120, y));
			y += 12;
		}
		if (type > 2) {
			if (schematics == null) { (schematics = new GuiCustomScroll(this, 0)).setSize(110, 197); }
			schematics.setList(new ArrayList<>(files.keySet()));
			schematics.guiLeft = guiLeft + 5;
			schematics.guiTop = guiTop + 14;
			if (!builder.schematicName.isEmpty()) {
				int i = 0;
				for (String key : schematics.getList()) {
					String fName = key;
					if (key.endsWith(".schematic")) {
						fName = key.substring(0, key.lastIndexOf(".schematic"));
					} else if (key.endsWith(".blueprint")) {
						fName = key.substring(0, key.lastIndexOf(".blueprint"));
					}
					if (fName.equals(builder.schematicName)) {
						schematics.setSelect(i);
						break;
					}
					i++;
				}
				if (i == schematics.getList().size()) {
					schematics.setSelect(-1);
				}
			}
			addScroll(schematics);
			addLabel(new GuiNpcLabel(6, new TextComponentTranslation("gui.name").getFormattedText() + ":", guiLeft + 120, guiTop + 40));
			textField = new GuiNpcTextField(10, this, guiLeft + 120, guiTop + 54, 99, 15, builder.schematicName);
			textField.setHoverText("scale.width");
			addTextField(textField);
		}
		if (type < 3) {
			label = new GuiNpcLabel(0, new TextComponentTranslation("gui.help.block").getFormattedText() + " [?]:", guiLeft + 4, guiTop + 4);
			label.setHoverText("builder.hover.blocks." + type);
			addLabel(label);
			label = new GuiNpcLabel(3, new TextComponentTranslation("gui.area").getFormattedText() + " [?]:", guiLeft + 120, y);
			label.setHoverText("builder.hover.type." + type);
			addLabel(label);
			y += 12;
			for (int i = 0; i < 3; i++) { // Region
				textField = new GuiNpcTextField(i + 10, this, guiLeft + 120 + i * 34, y, 30, 15, "" + builder.region[i]);
				textField.setMinMaxDefault(1, maxRange, builder.region[i]);
				if (i == 0) { textField.setHoverText("scale.width"); }
				else if (i == 1) { textField.setHoverText("scale.depth"); }
				else { textField.setHoverText("schematic.height"); }
				addTextField(textField);
			}
			button = new GuiButtonBiDirectional(1, guiLeft + 120, y += 18, 99, 20, new String[] { "builder.fasing.0", "builder.fasing.1", "builder.fasing.2" }, builder.facing);
			button.setHoverText("builder.hover.fasing");
			addButton(button);

			double t = 0, j = 0;
			for (int i = 1; i < 10; i++) {
				if (builder.chances.containsKey(i)) {
					t += (double) builder.chances.get(i);
					j += 1.0d;
				}
			}
			if (builder.addAir) { t += t / j; }
			double[] vs = new double[10];
			for (int i = 1; i < 10; i++) {
				if (getTextField(i) != null && getTextField(i).isMouseOver()) {
					double c = 0.0f;
					if (builder.chances.containsKey(i)) {
						if (type == 0) { c = (double) builder.chances.get(i) / 100.0d; }
						else { c = (double) builder.chances.get(i) / t; }
					}
					vs[i] = Math.round(c * 1000.d) / 10.d;
					return;
				}
			}

			for (int i = 1; i < 10; i++) { // Blocks
				textField = new GuiNpcTextField(i, this, guiLeft + 28 + (i / 6) * 54, guiTop + 17 + ((i < 6 ? 0 : -5) + i - 1) * 24, 28, 15, "" + (builder.chances.containsKey(i) ? builder.chances.get(i) : ""));
				textField.setMinMaxDefault(1, 100, builder.chances.getOrDefault(i, 100));
				textField.setHoverText("builder.hover.chance." + type, "" + vs[i]);
				addTextField(textField);
			}
		}
		else {
			label = new GuiNpcLabel(5, new TextComponentTranslation("gui.file.list").getFormattedText() + " [?]:", guiLeft + 4, guiTop + 4);
			label.setHoverText("builder.hover.list", "" + maxRange);
			addLabel(label);
		}
		if (type < 4) {
			button = new GuiNpcCheckBox(5, guiLeft + 120, y + 22, 99, 15, "tile.air.name", null, builder.addAir);
			button.setHoverText("schematic" + (type == 3 ? ".schem" : "") + ".air");
			addButton(button);
			if (type == 2 || type == 3) {
				button = new GuiNpcCheckBox(6, guiLeft + 172 + (type == 3 ? -52 : 0), guiTop + 145 + (type == 3 ? -60 : 0), 70, 15, "drop.type.all", null, builder.replaceAir);
				button.setHoverText("schematic" + (type == 3 ? ".schem" : "") + ".replace");
				addButton(button);

			}
			if (type == 3) {
				button = new GuiNpcCheckBox(7, guiLeft + 120, guiTop + 100, 70, 15, "gui.solid", null, builder.isSolid);
				button.setHoverText("schematic.schem.solid");
				addButton(button);
			}
		}
		if (type == 2) {
			label = new GuiNpcLabel(4, "_[?]_", guiLeft + 88, guiTop + 116);
			label.setHoverText("builder.hover.main.block");
			addLabel(label);
		}
	}

	@Override
	public void save() {
		if (builder == null || container == null) { return; }
		container.save();
		Client.sendData(EnumPacketServer.BuilderSetting, builder.getNbt());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		// File List
		builder.schematicName = scroll.getSelected();
		if (builder.schematicName.endsWith(".schematic")) {
			builder.schematicName = builder.schematicName.substring(0, builder.schematicName.lastIndexOf(".schematic"));
		} else if (builder.schematicName.endsWith(".blueprint")) {
			builder.schematicName = builder.schematicName.substring(0, builder.schematicName.lastIndexOf(".blueprint"));
		}
		SchematicWrapper schema = SchematicController.Instance.getSchema(builder.schematicName + ".schematic");
		if (schema != null) {
			builder.region[0] = schema.schema.getLength();
			builder.region[1] = schema.schema.getWidth();
			builder.region[2] = schema.schema.getHeight();
		}
		IGuiNpcTextField textField = getTextField(10);
		if (textField != null) {
			textField.setFullText(builder.schematicName);
		}
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (builder == null) {
			return;
		}
		if (textField.getID() < 10) {
			if (textField.getFullText().isEmpty()) {
				return;
			}
			if (builder.inv.getStackInSlot(textField.getID()).isEmpty()) {
				textField.setFullText("");
				return;
			}
			builder.chances.put(textField.getID(), textField.getInteger());
			return;
		}
		if (builder.getType() == 3 || builder.getType() == 4) {
			if (textField.getID() == 10) {
				builder.schematicName = textField.getFullText();
				initGui();
			}
		} else {
			int pos = textField.getID() - 10;
			int value = textField.getInteger();
			if (value > maxRange) {
				value = maxRange;
			}
			if (value <= 0) {
				value = 1;
			}
			builder.region[pos] = value;
		}
	}

}
