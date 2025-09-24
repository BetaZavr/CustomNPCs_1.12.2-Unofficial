package noppes.npcs.client.gui.model;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.LayerModel;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiCreationParts extends GuiCreationScreenInterface
		implements ITextfieldListener, ICustomScrollListener, ISliderListener  {

	public class GuiPart {

		protected String[] types;
		protected ModelPartData data;
		protected EnumParts part;
		protected boolean canBeDeleted;
		protected boolean hasPlayerOption;
		protected boolean noPlayerTypes;
		protected final GuiCreationParts parent;
		public int patterns;

		public GuiPart(EnumParts partIn, GuiCreationParts parentIn) {
			parent = parentIn;
			patterns = 0;
			types = new String[] { "gui.none" };
			hasPlayerOption = true;
			noPlayerTypes = false;
			canBeDeleted = true;
			part = partIn;
			data = playerdata.getPartData(partIn);
		}

		public boolean buttonEvent(@Nonnull GuiNpcButton button) {
			switch (button.getID()) {
				case 20: {
					int i = button.getValue();
					if (i == 0 && canBeDeleted) { playerdata.removePart(part); }
					else {
						data = playerdata.getOrCreatePart(part);
						data.pattern = 0;
						data.setType(i - 1);
					}
					parent.initGui();
					return true;
				}
				case 21: {
					if (data != null && button instanceof GuiNpcButtonYesNo) { data.playerTexture = ((GuiNpcButtonYesNo) button).getBoolean(); }
					parent.initGui();
					return true;
				}
				case 22: {
					data.pattern = (byte) button.getValue();
					parent.initGui();
					return true;
				}
				case 23: {
					setSubGui(new SubGuiModelColor(GuiCreationParts.this, data.color, color -> data.color = color));
					parent.initGui();
					return true;
				}
			}
			return false;
		}

		public int initGui() {
			data = playerdata.getPartData(part);
			int x0 = guiLeft + 123;
			int x1 = guiLeft + 175;
			int y = guiTop + 50;
			if (data != null || !noPlayerTypes) {
				addLabel(new GuiNpcLabel(20, "gui.type", x0, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(20, x1, y, 100, 20, types, (data == null) ? 0 : (data.type + 1))
						.setHoverText(new TextComponentTranslation("display.hover.part.type").getFormattedText()));
				y += 25;
			}
			if (data != null && hasPlayerOption) {
				addLabel(new GuiNpcLabel(21, "gui.playerskin", x0, y + 5, 0xFFFFFF));
				addButton(new GuiNpcButtonYesNo(21, x1, y, data.playerTexture)
						.setHoverText("display.hover.part.skin"));
				y += 25;
			}
			if (data != null && !data.playerTexture) {
				addLabel(new GuiNpcLabel(23, "gui.color", x0, y + 5, 0xFFFFFF));
				addButton(new GuiColorButton(23, x1, y, data.color)
						.setHoverText("display.hover.part.color"));
				y += 25;
			}
			return y;
		}

		public GuiPart noPlayerOptions() { hasPlayerOption = false; return this; }

		public GuiPart noPlayerTypes() { noPlayerTypes = true; return this; }

		public GuiPart setTypes(String[] typesIn) { types = typesIn; return this; }

		public void subGuiClosed(SubGuiInterface subgui) { }

	}

	class GuiPartBeard extends GuiPart {

		public GuiPartBeard(GuiCreationParts parentIn) {
			super(EnumParts.BEARD, parentIn);
			noPlayerTypes().types = new String[] { "gui.none", "1", "2", "3", "4" };
		}

	}

	class GuiPartClaws extends GuiPart {

		public GuiPartClaws(GuiCreationParts parentIn) {
			super(EnumParts.CLAWS, parentIn);
			types = new String[] { "gui.none", "gui.show" };
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (data == null) { return y; }
			addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
			addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "gui.both", "gui.left", "gui.right" }, data.pattern)
					.setHoverText("display.hover.part.pattern"));
			return y;
		}

	}

	class GuiPartEyes extends GuiPart {
		
		private final ModelEyeData eyes;

		public GuiPartEyes(GuiCreationParts parentIn) {
			super(EnumParts.EYES, parentIn);
			types = new String[] { "gui.none", "gui.small", "gui.normal", "gui.select" };
			noPlayerOptions();
			canBeDeleted = false;
			eyes = (ModelEyeData) data;
		}

		@Override
		public boolean buttonEvent(@Nonnull GuiNpcButton button) {
			switch (button.getID()) {
				case 23: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.eyeColor[1], color -> eyes.eyeColor[1] = color)); return true;
				case 24: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.eyeColor[0], color -> eyes.eyeColor[0] = color)); return true;
				case 25: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.pupilColor[0], color -> eyes.pupilColor[0] = color)); return true;
				case 26: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.pupilColor[1], color -> eyes.pupilColor[1] = color)); return true;
				case 27: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.browColor[1], color -> eyes.browColor[1] = color)); return true;
				case 28: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.browColor[0], color -> eyes.browColor[0] = color)); return true;
				case 29: eyes.browThickness = button.getValue(); return true;
				case 30: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.skinColor, color -> eyes.skinColor = color)); return true;
				case 31: eyes.closed = button.getValue(); return true;
				case 32: eyes.eyePos = button.getValue() - 1; return true;
				case 33: eyes.glint = ((GuiNpcButtonYesNo) button).getBoolean(); return true;
				case 34: setSubGui(new SubGuiTextureSelection(0, null, eyes.eyeRight.toString(), "png", 5)); return true;
				case 35: setSubGui(new SubGuiTextureSelection(1, null, eyes.eyeLeft.toString(), "png", 5)); return true;
				case 36: setSubGui(new SubGuiTextureSelection(2, null, eyes.pupilRight.toString(), "png", 5)); return true;
				case 37: setSubGui(new SubGuiTextureSelection(3, null, eyes.pupilLeft.toString(), "png", 5)); return true;
				case 38: setSubGui(new SubGuiTextureSelection(4, null, eyes.browRight.toString(), "png", 5)); return true;
				case 39: setSubGui(new SubGuiTextureSelection(5, null, eyes.browLeft.toString(), "png", 5)); return true;
				case 40: eyes.reset(); parent.initGui(); return true;
				case 41: eyes.activeRight = ((GuiNpcButtonYesNo) button).getBoolean(); return true;
				case 42: eyes.activeLeft = ((GuiNpcButtonYesNo) button).getBoolean(); return true;
				case 43: eyes.activeCenter = ((GuiNpcButtonYesNo) button).getBoolean(); return true;
				case 44: setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.centerColor, color -> eyes.centerColor = color)); return true;
				case 45:
					setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.eyeColor[0], color -> {
						eyes.eyeColor[0] = color;
						eyes.eyeColor[1] = color;
					}));
					return true;
				case 46:
					setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.pupilColor[0], color -> {
						eyes.pupilColor[0] = color;
						eyes.pupilColor[1] = color;
					}));
					return true;
				case 47:
					setSubGui(new SubGuiModelColor(GuiCreationParts.this, eyes.browColor[0], color -> {
						eyes.browColor[0] = color;
						eyes.browColor[1] = color;
					}));
					return true;
				default: return super.buttonEvent(button);
			}
		}

		@Override
		public int initGui() {
			int y = super.initGui(); // button IDs: 20 ... 23 
			if (data != null && eyes.isEnabled()) {
				int x0 = guiLeft + 123;
				int x1 = guiLeft + 175;
				y = guiTop + 50;
				getLabel(20).y = y + 3;
				getButton(20).setXY(x1, y).height = 14;
				if (eyes.type != -1) {
					addButton(new GuiNpcButton(40, x1 + 104, y, 31, 14, "RND").setHoverText("display.hover.part.rnd"));
				}
				// eye color
				y += 16;
				// left
				getLabel(23).setLabel("eye.color.0").y = y + 3;
				getButton(23).setXY(x1, y).setWH(40, 14);
				((GuiColorButton) getButton(23).setHoverText("display.hover.part.eye.color.r")).color = eyes.eyeColor[1];
				addButton(new GuiNpcButton(45, x1 + 42, y, 18, 14, "-")
						.setHoverText("display.hover.part.eye.color"));
				// right
				addButton(new GuiColorButton(24, x1 + 62, y, 40, 14, eyes.eyeColor[0])
						.setHoverText("display.hover.part.eye.color.l"));
				if (data.type == 2) {
					addButton(new GuiNpcButton(34, x1 + 104, y, 14, 14, "EL")
							.setHoverText("display.hover.part.eye.txr.r"));
					addButton(new GuiNpcButton(35, x1 + 120, y, 14, 14, "ER")
							.setHoverText("display.hover.part.eye.txr.l"));
				}
				// pupil color
				// left
				addLabel(new GuiNpcLabel(25, "eye.color.1", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiColorButton(25, x1, y, 40, 14, eyes.pupilColor[0])
						.setHoverText("display.hover.part.pupil.color.r"));
				addButton(new GuiNpcButton(46, x1 + 42, y, 18, 14, "-")
						.setHoverText("display.hover.part.pupil.color"));
				// right
				addButton(new GuiColorButton(26, x1 + 62, y, 40, 14, eyes.pupilColor[1])
						.setHoverText("display.hover.part.pupil.color.l"));
				if (data.type == 2) {
					addButton(new GuiNpcButton(36, x1 + 104, y, 14, 14, "PL")
							.setHoverText("display.hover.part.pupil.txr.r"));
					addButton(new GuiNpcButton(37, x1 + 120, y, 14, 14, "PR")
							.setHoverText("display.hover.part.pupil.txr.l"));
				}
				// center
				addLabel(new GuiNpcLabel(44, "eye.color.2", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiColorButton(44, x1, y, 102, 14, eyes.centerColor));
				addButton(new GuiNpcButtonYesNo(43, x1 + 104, y, 20, 14, eyes.activeCenter)
						.setHoverText("display.hover.part.center.active"));
				// brow color
				// left
				addLabel(new GuiNpcLabel(27, "eye.color.3", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiColorButton(27, x1 + 62, y, 40, 14, eyes.browColor[1])
						.setHoverText("display.hover.part.brow.color.r"));
				addButton(new GuiNpcButton(47, x1 + 42, y, 18, 14, "-")
						.setHoverText("display.hover.part.brow.color"));
				// right
				addButton(new GuiColorButton(28, x1, y, 40, 14, eyes.browColor[0])
						.setHoverText("display.hover.part.brow.color.l"));
				if (data.type == 2) {
					addButton(new GuiNpcButton(38, x1 + 104, y, 14, 14, "BL")
							.setHoverText("display.hover.part.brow.txr.r"));
					addButton(new GuiNpcButton(39, x1 + 120, y, 14, 14, "BR")
							.setHoverText("display.hover.part.brow.txr.l"));
				}
				// brow size
				addLabel(new GuiNpcLabel(29, "eye.brow", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(29, x1, y, 102, 14, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }, eyes.browThickness)
						.setHoverText("display.hover.part.brow.size"));
				// skin color
				addLabel(new GuiNpcLabel(30, "eye.lid", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiColorButton(30, x1, y, eyes.skinColor)
						.setWH(100, 14)
						.setHoverText("display.hover.part.skin.color"));
				// both eyes
				addLabel(new GuiNpcLabel(22, "gui.draw", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(22, x1, y, 102, 14, new String[] { "gui.both", "gui.left", "gui.right" }, data.pattern)
						.setHoverText("display.hover.part.pattern"));
				// closed
				addLabel(new GuiNpcLabel(31, "eye.closed", x0, (y += 16) + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(31, x1, y, 102, 14, new String[] { "gui.none", "gui.both", "gui.left", "gui.right" }, eyes.closed)
						.setHoverText("display.hover.part.closed"));
				addButton(new GuiNpcButtonYesNo(41, x1 + 104, y, 20, 14, eyes.activeRight)
						.setHoverText("display.hover.part.eye.active"));
				addButton(new GuiNpcButtonYesNo(42, x1 + 126, y, 20, 14, eyes.activeLeft)
						.setHoverText("display.hover.part.eye.active"));
				// vertical pos
				y += 16;
				addLabel(new GuiNpcLabel(32, "gui.position", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(32, x1, y, 102, 14, new String[] {
						new TextComponentTranslation("gui.down").getFormattedText() + " x2",
						"gui.down", "gui.normal", "gui.up",
						new TextComponentTranslation("gui.up").getFormattedText() + " x2" }, eyes.eyePos + 1)
						.setHoverText("display.hover.part.pos"));
				// glint
				y += 16;
				addLabel(new GuiNpcLabel(33, "eye.glint", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButtonYesNo(33, x1, y, 102, 14, eyes.glint)
						.setHoverText("display.hover.part.glint"));
			}
			return y;
		}
		
		@Override
		public void subGuiClosed(SubGuiInterface subgui) {
			if (subgui instanceof SubGuiTextureSelection) {
				SubGuiTextureSelection tGui = (SubGuiTextureSelection) subgui;
				if (subgui.getId() == 0) { eyes.eyeRight = tGui.resource; }
				else if (subgui.getId() == 1) { eyes.eyeLeft = tGui.resource; }
				else if (subgui.getId() == 2) { eyes.pupilRight = tGui.resource; }
				else if (subgui.getId() == 3) { eyes.pupilLeft = tGui.resource; }
				else if (subgui.getId() == 4) { eyes.browRight = tGui.resource; }
				else if (subgui.getId() == 5) { eyes.browLeft = tGui.resource; }
			}
		}
		
	}

	class GuiPartHair extends GuiPart {

		public GuiPartHair(GuiCreationParts parentIn) {
			super(EnumParts.HAIR, parentIn);
			noPlayerTypes().types = new String[] { "gui.none", "1", "2", "3", "4" };
		}

	}

	class GuiPartHorns extends GuiPart {

		public GuiPartHorns(GuiCreationParts parentIn) {
			super(EnumParts.HORNS, parentIn);
			types = new String[] { "gui.none", "horns.bull", "horns.antlers", "horns.antenna" };
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (data != null && data.type == 2) {
				addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "1", "2" }, data.pattern)
						.setHoverText("display.hover.part.pattern"));
			}
			return y;
		}

	}

	class GuiPartLegs extends GuiPart {

		public GuiPartLegs(GuiCreationParts parentIn) {
			super(EnumParts.LEGS, parentIn);
			types = new String[] { "gui.none", "gui.normal", "legs.naga", "legs.spider", "legs.horse", "legs.mermaid", "legs.digitigrade" };
			canBeDeleted = false;
		}

		@Override
		public boolean buttonEvent(@Nonnull GuiNpcButton button) {
			if (button.getID() == 20) {
				int i = button.getValue();
				data.playerTexture = (i <= 1);
				return true;
			}
			super.buttonEvent(button);
			return false;
		}

		@Override
		public int initGui() {
			hasPlayerOption = (data.type == 1 || data.type == 5);
			return super.initGui();
		}

	}

	class GuiPartParticles extends GuiPart {

		public GuiPartParticles(GuiCreationParts parentIn) {
			super(EnumParts.PARTICLES, parentIn);
			types = new String[] { "gui.none", "1", "2" };
		}

		@Override
		public int initGui() { return super.initGui(); }

	}

	class GuiPartSnout extends GuiPart {
		public GuiPartSnout(GuiCreationParts parentIn) {
			super(EnumParts.SNOUT, parentIn);
			types = new String[] { "gui.none", "snout.small", "snout.medium", "snout.large", "snout.bunny", "snout.beak" };
		}
	}

	class GuiPartTail extends GuiPart {
		public GuiPartTail(GuiCreationParts parentIn) {
			super(EnumParts.TAIL, parentIn);
			types = new String[] { "gui.none", "part.tail", "tail.dragon", "tail.horse", "tail.squirrel", "tail.fin", "tail.rodent", "tail.bird", "tail.fox" };
		}

		@Override
		public int initGui() {
			data = playerdata.getPartData(part);
			hasPlayerOption = (data != null && (data.type == 0 || data.type == 1 || data.type == 6 || data.type == 7));
			int y = super.initGui();
			if (data != null && data.type == 0) {
				addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "1", "2" }, data.pattern)
						.setHoverText("display.hover.part.pattern"));
			}
			return y;
		}
	}

	class GuiPartWings extends GuiPart {

		public GuiPartWings(GuiCreationParts parentIn) {
			super(EnumParts.WINGS, parentIn);
			setTypes(new String[] { "gui.none", "1", "2", "3", "4" });
		}

		@Override
		public int initGui() { return super.initGui(); }

	}

	class GuiPartLayers extends GuiPart {

		private final ContainerLayer cont;

		public GuiCustomScroll scrollIn;
		public int selectPos = 0;
		public Map<Integer, EnumParts> partNames = new LinkedHashMap<>();

        public GuiPartLayers(GuiCreationParts parentIn) {
			super(EnumParts.CUSTOM_LAYERS, parentIn);
			cont = (ContainerLayer) inventorySlots;
			partNames.put(0, EnumParts.HEAD);
			partNames.put(1, EnumParts.BODY);
			partNames.put(2, EnumParts.ARM_RIGHT);
			partNames.put(3, EnumParts.ARM_LEFT);
			partNames.put(4, EnumParts.LEG_RIGHT);
			partNames.put(5, EnumParts.LEG_LEFT);
			partNames.put(6, EnumParts.BELT);
			partNames.put(7, EnumParts.WRIST_RIGHT);
			partNames.put(8, EnumParts.WRIST_LEFT);
			partNames.put(9, EnumParts.FOOT_RIGHT);
			partNames.put(10, EnumParts.FOOT_LEFT);
			LayerModel lm = playerdata.getLayerModel(selectPos);
			if (lm != null) { Client.sendData(EnumPacketServer.ChangeItemInSlot, "ContainerLayer", 0, lm.getStack().writeToNBT(new NBTTagCompound())); }
		}

		@Override
		public boolean buttonEvent(@Nonnull GuiNpcButton button) {
			switch (button.getID()) {
				case 21: {
					if (cont == null) { return true; }
					selectPos = playerdata.addNewLayer();
					cont.getSlot(0).putStack(ItemStack.EMPTY);
					Client.sendData(EnumPacketServer.ChangeItemInSlot, "ContainerLayer", 0, ItemStack.EMPTY.writeToNBT(new NBTTagCompound()));
					return true;
				} // add new item layer
				case 22: {
					selectPos = playerdata.removeLayer(selectPos);
					NBTTagCompound compound = new NBTTagCompound();
					LayerModel lm = playerdata.getLayerModel(selectPos);
					if (lm == null) {
						ItemStack.EMPTY.writeToNBT(compound);
						cont.getSlot(0).putStack(ItemStack.EMPTY);
					}
					else {
						lm.getStack().writeToNBT(compound);
						cont.getSlot(0).putStack(lm.getStack());
					}
					Client.sendData(EnumPacketServer.ChangeItemInSlot, "ContainerLayer", 0, compound);
					parent.initGui();
					return true;
				} // remove item layer
				case 23: {
					if (toolType == 1) { return true; }
					GuiNpcTextField.unfocus();
					toolType = 1;
					parent.initGui();
					return true;
				} // select tool pos
				case 24: {
					if (toolType == 0) { return true; }
					GuiNpcTextField.unfocus();
					toolType = 0;
					parent.initGui();
					return true;
				} // select tool rot
				case 25: {
					if (toolType == 2) { return true; }
					GuiNpcTextField.unfocus();
					toolType = 2;
					parent.initGui();
					return true;
				} // select tool scale
				case 26: {
					resetAxis(0);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){ ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
					return true;
				} // reset X
				case 27: {
					resetAxis(1);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){ ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
					return true;
				} // reset Y
				case 28: {
					resetAxis(2);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){ ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
					return true;
				} // reset Z
				case 29: {
					LayerModel lm = playerdata.getLayerModel(selectPos);
					if (lm == null) { return true; }
					lm.part = partNames.get(button.getValue());
					if (showEntity instanceof EntityCustomNpc && playerdata != null) { ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
					return true;
				} // reset part
				default: return super.buttonEvent(button);
			}
		}

		@Override
		public int initGui() {
			GuiNpcTextField.unfocus();
			super.initGui();
			for (int i = 20; i < 24; i++) {
				if (getLabel(i) != null) { getLabel(i).setIsVisible(false); }
				if (getButton(i) != null) { getButton(i).setIsVisible(false); }
			}
			int x0 = guiLeft + 123;
			int y = guiTop;
			scrollIn = new GuiCustomScroll(parent, 1);
			scrollIn.setSize(100, 127);
			NBTTagCompound compound = new NBTTagCompound();
			LayerModel lm = playerdata.getLayerModel(selectPos);
			if (lm == null) {
				ItemStack stack = cont.getSlot(0).getStack();
				if (!stack.isEmpty()) {
					selectPos = playerdata.addNewLayer();
					lm = playerdata.getLayerModel(selectPos);
					lm.setStack(stack);
					stack.writeToNBT(compound);
				} else {
					selectPos = -1;
					ItemStack.EMPTY.writeToNBT(compound);
				}
			}
			else { lm.getStack().writeToNBT(compound); }
			Client.sendData(EnumPacketServer.ChangeItemInSlot, "ContainerLayer", 0, compound);
			scrollIn.setUnsortedList(playerdata.getLayerKeys());
			scrollIn.setSelect(selectPos);
			addLabel(new GuiNpcLabel(20, "part.layers.info.0", x0, y)
					.setColor(CustomNpcs.MainColor.getRGB()));
			int y1 = y + 141;
			addButton(new GuiNpcButton(21, x0, y1, 49, 20, "gui.add")
					.setIsEnable(playerdata.isNoEmptyLayer()));
			addButton(new GuiNpcButton(22, x0 + 51, y1, 49, 20, "gui.remove")
					.setIsEnable(selectPos != -1));
			if (lm != null) {
				int x1 = x0 + 104;
				String objModel = lm.getOBJ() == null ? "" : lm.getOBJ().toString();
				y1 = y;
				addLabel(new GuiNpcLabel(19, "OBJ Model path:", x1 + 1, y1));
				getLabel(19).setColor(CustomNpcs.MainColor.getRGB());
				addTextField(new GuiNpcTextField(25, parent, x1, y1 += 12, 150, 16, objModel)
						.setHoverText("display.hover.layer.obj"));
				addLabel(new GuiNpcLabel(22, "Tool type:", x1 + 1, (y1 += 19) + 2)
						.setColor(CustomNpcs.MainColor.getRGB()));
				if (!lm.getStack().isEmpty() || lm.getOBJ() != null) {
					// tool pos
					addButton(new GuiNpcButton(23,x1 + 50, y1, 14, 14, "")
							.setTexture(GuiNPCInterface.ANIMATION_BUTTONS)
							.setHasDefaultBack(false)
							.setIsAnim(true)
							.setUV(0, 0, 24, 24)
							.setLayerColor(toolType == 1 ?  new Color(0xFFFF4040).getRGB() :  new Color(0xFFFFFFFF).getRGB())
							.setHoverText("animation.hover.tool.0"));
					// tool rot
					addButton(new GuiNpcButton(24, x1 + 66, y1, 14, 14, "")
							.setTexture(GuiNPCInterface.ANIMATION_BUTTONS)
							.setHasDefaultBack(false)
							.setIsAnim(true)
							.setUV(24, 0, 24, 24)
							.setLayerColor(toolType == 0 ?  new Color(0xFF40FF40).getRGB() :  new Color(0xFFFFFFFF).getRGB())
							.setHoverText("animation.hover.tool.1"));
					// tool scale
					addButton(new GuiNpcButton(25, x1 + 82, y1, 14, 14, "")
							.setTexture(GuiNPCInterface.ANIMATION_BUTTONS)
							.setHasDefaultBack(false)
							.setIsAnim(true)
							.setUV(48, 24, 24, 24)
							.setLayerColor(toolType == 2 ?  new Color(0xFF4040FF).getRGB() :  new Color(0xFFFFFFFF).getRGB())
							.setHoverText("animation.hover.tool.2"));
					int f = 11;
					int id;
					y1 += 16;
					for (int i = 0; i < 3; i++) { // 26 ... 28
						id = i + 26;
						addLabel(new GuiNpcLabel(id, i == 0 ? "X:" : i == 1 ? "Y:" : "Z:", x1 + 1, y1 + i * f));
						getLabel(id).setColor(CustomNpcs.MainColor.getRGB());
						float v;
						float s;
						float max;
						float min;
						switch (toolType) {
							case 0: {
								v = lm.rotation[i];
								s = 0.002778f * lm.rotation[i] + 0.5f;
								min = -180.0f;
								max = 180.0f;
								break;
							}
							case 1: {
								v = lm.offset[i];
								s = 0.2f * lm.offset[i] + 0.5f;
								min = -10.0f;
								max = 10.0f;
								break;
							}
							default: {
								v = lm.scale[i];
								s = 0.2f * lm.scale[i];
								min = 0.0f;
								max = 5.0f;
								break;
							}
						}
						String hover = "animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale");
						addSlider(new GuiNpcSlider(parent, id, x1 + 9, y1 + i * f, 78, 8, s)
								.setHoverText(hover, i == 0 ? "X" : i == 1 ? "Y" : "Z"));
						addTextField(new GuiNpcTextField(id, parent, x1 + 89, y1 + i * f, 42, 8, df.format(v))
								.setMinMaxDoubleDefault(min, max, v)
								.setHoverText(hover, i == 0 ? "X" : i == 1 ? "Y" : "Z"));
						addButton(new GuiNpcButton(id, x1 + 133, y1 + i * f, 8, 8, "X")
								.setTexture(GuiNPCInterface.ANIMATION_BUTTONS)
								.setHasDefaultBack(false)
								.setIsAnim(true)
								.setUV(0, 96, 0, 0)
								.setTextColor(0xFFDC0000)
								.setDropShadow(false)
								.setHoverText("animation.hover.reset." + toolType, i == 0 ? "X" : i == 1 ? "Y" : "Z"));
					}  // 26 ... 28
					y1 += f * 3 + 2;
				}
				int pos = 0;
				String[] names = new String[partNames.size()];
				for (int i : partNames.keySet()) {
					if (partNames.get(i) == lm.part) { pos = i; }
					names[i] = "part." + partNames.get(i).name;
				}
				addButton(new GuiButtonBiDirectional(29, x1, y1, 78, 14, names, pos)
						.setHoverText("display.hover.layer.type"));
			}
			y += 12;
			scrollIn.guiLeft = x0;
			scrollIn.guiTop = y;
			addScroll(scrollIn);
			if (showEntity instanceof EntityCustomNpc && playerdata != null){ ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
			return y;
		}

	}

	private static final DecimalFormat df = new DecimalFormat("#.####");
	private static int selected = 0;
	private final GuiPart[] parts;
	private GuiCustomScroll scroll;
	private boolean isCheck = false;
	private int waitKey;
	private int waitKeyID;
	private int toolType; // 0 - rotation, 1 - offset, 2 - scale

	public GuiCreationParts(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		parts = new GuiPart[] {
				new GuiPart(EnumParts.EARS, this).setTypes(new String[] { "gui.none", "gui.normal", "ears.bunny" }),
				new GuiPartHorns(this),
				new GuiPartHair(this),
				new GuiPart(EnumParts.MOHAWK, this).setTypes(new String[] { "gui.none", "1", "2" }).noPlayerOptions(),
				new GuiPartSnout(this),
				new GuiPartBeard(this),
				new GuiPart(EnumParts.FIN, this).setTypes(new String[] { "gui.none", "fin.shark", "fin.reptile" }),
				new GuiPart(EnumParts.BREASTS, this).setTypes(new String[] { "gui.none", "1", "2", "3" }).noPlayerOptions(),
				new GuiPartWings(this),
				new GuiPartClaws(this),
				new GuiPart(EnumParts.SKIRT, this).setTypes(new String[] { "gui.none", "gui.normal" }), new GuiPartLegs(this),
				new GuiPartTail(this),
				new GuiPartEyes(this),
				new GuiPartParticles(this),
				new GuiPartLayers(this) };
		active = 2;
		closeOnEsc = false;
		Arrays.sort(parts, (o1, o2) -> {
			String s1 = new TextComponentTranslation("part." + o1.part.name).getFormattedText();
			String s2 = new TextComponentTranslation("part." + o2.part.name).getFormattedText();
			return s1.compareToIgnoreCase(s2);
		});
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 0 && getPart() != null && getPart().buttonEvent(button)) { return; }
		super.buttonEvent(button, mouseButton);
	}
	
	protected GuiPart getPart() { return parts[GuiCreationParts.selected]; }

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GuiPart part = getPart();
		if (part instanceof GuiPartLayers) {
			GlStateManager.pushMatrix();
			GlStateManager.translate( guiLeft + 121.0f, guiTop + 163.0f, 0.0f);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			int x;
			int y;
			for (Slot slot : ((GuiPartLayers) part).cont.inventorySlots) {
				if (slot.slotNumber == 0) {
					x = 164;
					y = 0;
				}
				else {
					x = ((slot.slotNumber - 1) % 9) * 18;
					y = ((slot.slotNumber - 1) / 9) * 18;
				}
				drawTexturedModalRect(x, y, 0, 0, 18, 18);
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (waitKey != 0) { waitKey--; }
		GuiPart part = getPart();
		if (!isCheck && part instanceof GuiPartLayers && getButton(21) != null) {
			CustomNPCsScheduler.runTack(() -> {
				isCheck = true;
				getButton(21).setIsEnable(playerdata.isNoEmptyLayer()); // add
				LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
				GuiCustomScroll scrollIn = ((GuiPartLayers) part).scrollIn;
				ItemStack stack = ((GuiPartLayers) part).cont.getSlot(0).getStack();
				if (lm == null) {
					if (scrollIn.hasSelected() || !stack.isEmpty()) { initGui(); }
				}
				else if (lm.getStack() != stack) {
					lm.setStack(((GuiPartLayers) part).cont.getSlot(0).getStack());
					initGui();
				}
				isCheck = false;
			});
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (entity != null) { openGui(new GuiCreationExtra(npc, (ContainerLayer) inventorySlots)); return; }
		if (scroll == null) {
			List<String> list = new ArrayList<>();
			for (GuiPart part : parts) { list.add(new TextComponentTranslation("part." + part.part.name).getFormattedText()); }
			scroll = new GuiCustomScroll(this, 0).setUnsortedList(list);
		}
		scroll.guiLeft = guiLeft;
		scroll.guiTop = guiTop + 46;
		scroll.setSize(121, ySize - 74);
		addScroll(scroll);
		if (getPart() != null) {
			scroll.setSelected(new TextComponentTranslation("part." + getPart().part.name).getFormattedText());
			getPart().initGui();
		}
		if (inventorySlots instanceof ContainerLayer) {
			boolean bo = getPart() instanceof GuiPartLayers;
			int x;
			int y;
			for (Slot slot : inventorySlots.inventorySlots) {
				if (slot.slotNumber == 0) {
					x = 164;
					y = 0;
				}
				else {
					x = ((slot.slotNumber - 1) % 9) * 18;
					y = ((slot.slotNumber - 1) / 9) * 18;
					if ((slot.slotNumber - 1) < 9) { y += 54;} else if ((slot.slotNumber - 1) != 36) { y -= 18;}
				}
				slot.xPos = bo ? 122 + x : -5000;
				slot.yPos = bo ? 164 + y : -5000;
			}
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0 && scroll.getSelect() >= 0) {
			GuiCreationParts.selected = scroll.getSelect();
			initGui();
		}
		if (scroll.getID() == 1 && getPart() instanceof GuiPartLayers) {
			GuiPartLayers part = (GuiPartLayers) getPart();
			part.selectPos = scroll.getSelect();
			LayerModel lm = playerdata.getLayerModel(scroll.getSelect());
			if (lm == null) { part.cont.getSlot(0).putStack(ItemStack.EMPTY); }
			else { part.cont.getSlot(0).putStack(lm.getStack()); }
			part.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (getPart() != null) { getPart().subGuiClosed(subgui); }
		initGui();
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui == null && getPart() instanceof GuiPartLayers) {
			// tool pos - Alt + Q
			if (isPressAndKey(Keyboard.KEY_Q) && toolType != 1) {
				toolType = 1;
				playButtonClick();
				initGui();
				return true;
			}
			// tool rot - Alt + W
			if (isPressAndKey(Keyboard.KEY_W) && toolType != 0) {
				toolType = 0;
				playButtonClick();
				initGui();
				return true;
			}
			// tool rot - Alt + E
			if (isPressAndKey(Keyboard.KEY_E) && toolType != 2) {
				toolType = 2;
				playButtonClick();
				initGui();
				return true;
			}
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

	private void playButtonClick() { mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F)); }

	private boolean isPressAndKey(int keyCode) {
		if (waitKey > 0 && waitKeyID == keyCode) { return false; }
		boolean isPress = isAltKeyDown() && Keyboard.isKeyDown(keyCode);
		if (isPress) { waitKey = 30; waitKeyID = keyCode; }
		return isPress;
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (getPart() instanceof GuiPartLayers) {
			switch (slider.getID()) {
				case 26: sliderMoved(0, slider.sliderValue); break; // X
				case 27: sliderMoved(1, slider.sliderValue); break; // Y
				case 28: sliderMoved(2, slider.sliderValue); break; // Z
				default: super.mouseDragged(slider); break;
			}
			if (showEntity instanceof EntityCustomNpc && playerdata != null){ ((EntityCustomNpc) showEntity).modelData.load(playerdata.save()); }
			return;
		}
		super.mouseDragged(slider);
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (getPart() instanceof GuiPartLayers) {
			switch (textField.getID()) {
				case 25: {
					LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
					if (lm == null) { return; }
					lm.setOBJ(textField.getText());
					if (lm.getOBJ() != null) { textField.setText(lm.getOBJ().toString()); }
					getPart().initGui();
					break;
				} // objModel
				case 26: textField.setText(textFieldChanged(0, (float) textField.getDouble())); break; // X
				case 27: textField.setText(textFieldChanged(1, (float) textField.getDouble())); break; // Y
				case 28: textField.setText(textFieldChanged(2, (float) textField.getDouble())); break; // Z
			}
			if (showEntity instanceof EntityCustomNpc && playerdata != null){
				((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
			}
		}
	}

	private void sliderMoved(int id, float sliderValue) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		if (lm == null) { return; }
		switch (toolType) {
			case 0: {
				lm.rotation[id] = 360.0f * sliderValue - 180.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText(df.format(lm.rotation[id])); }
				break;
			} // rotation
			case 1: {
				lm.offset[id] = 5.0f * sliderValue - 2.5f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText(df.format(lm.offset[id])); }
				break;
			} // offset
			default: {
				lm.scale[id] = 5.0f * sliderValue;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText(df.format(lm.scale[id])); }
				break;
			} // scale
		}
	}

	private String textFieldChanged(int id, float textValue) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		String text = "" + textValue;
		if (lm == null) { return text; }
		switch (toolType) {
			case 0: {
				while (textValue < -180.0f) { textValue += 180.0f; }
				while (textValue > 180.0f) { textValue -= 180.0f; }
				lm.rotation[id] = textValue;
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.002778f * lm.rotation[id] + 0.5f); }
				text = df.format(lm.rotation[id]);
				break;
			} // rotation
			case 1: {
				lm.offset[id] = ValueUtil.correctFloat(textValue, -10.0f, 10.0f);
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.offset[id] + 0.5f); }
				text = df.format(lm.offset[id]);
				break;
			} // offset
			default: {
				lm.scale[id] = ValueUtil.correctFloat(textValue, 0.0f, 5.0f);
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.scale[id]); }
				text = df.format(lm.scale[id]);
				break;
			} // scale
		}
		return text;
	}

	private void resetAxis(int id) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		if (lm == null) { return; }
		switch (toolType) {
			case 0: {
				lm.rotation[id] = 0.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText("0"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.002778f * lm.rotation[id] + 0.5f); }
				break;
			} // rotation
			case 1: {
				lm.offset[id] = 0.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText("0"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.offset[id] + 0.5f); }
				break;
			} // offset
			default: {
				lm.scale[id] = 1.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setText("1"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.scale[id]); }
				break;
			} // scale
		}
	}

}
