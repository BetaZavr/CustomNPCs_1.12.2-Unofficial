package noppes.npcs.client.gui.model;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.LayerModel;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.containers.SlotAvailability;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiCreationParts
extends GuiCreationScreenInterface
implements ITextfieldListener, ICustomScrollListener, ISubGuiListener, ISliderListener  {

	public class GuiPart implements ISubGuiListener {

		protected boolean canBeDeleted;
		protected ModelPartData data;
		protected boolean hasPlayerOption;
		protected boolean noPlayerTypes;
		EnumParts part;
		public int patterns;
		protected String[] types;

		public GuiPart(EnumParts part) {
			this.patterns = 0;
			this.types = new String[] { "gui.none" };
			this.hasPlayerOption = true;
			this.noPlayerTypes = false;
			this.canBeDeleted = true;
			this.part = part;
			this.data = playerdata.getPartData(part);
		}

		protected void actionPerformed(GuiButton btn) {
			if (btn.id == 20) {
				int i = ((GuiNpcButton) btn).getValue();
				if (i == 0 && this.canBeDeleted) {
					playerdata.removePart(this.part);
				} else {
					this.data = playerdata.getOrCreatePart(this.part);
					this.data.pattern = 0;
					this.data.setType(i - 1);
				}
				initGui();
			}
			if (btn.id == 22) {
				this.data.pattern = (byte) ((GuiNpcButton) btn).getValue();
			}
			if (btn.id == 21) {
				this.data.playerTexture = ((GuiNpcButtonYesNo) btn).getBoolean();
				initGui();
			}
			if (btn.id == 23) {
				setSubGui(new GuiModelColor(GuiCreationParts.this, this.data.color, color -> this.data.color = color));
			}
		}

		public int initGui() {
			this.data = playerdata.getPartData(this.part);
			int x0 = guiLeft + 123;
			int x1 = guiLeft + 175;
			int y = guiTop + 50;
			if (this.data == null || !this.data.playerTexture || !this.noPlayerTypes) {
				addLabel(new GuiNpcLabel(20, "gui.type", x0, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(20, x1, y, 100, 20, this.types, (this.data == null) ? 0 : (this.data.type + 1)));
				y += 25;
			}
			if (this.data != null && this.hasPlayerOption) {
				addLabel(new GuiNpcLabel(21, "gui.playerskin", x0, y + 5, 0xFFFFFF));
				addButton(new GuiNpcButtonYesNo(21, x1, y, this.data.playerTexture));
				y += 25;
			}
			if (this.data != null && !this.data.playerTexture) {
				addLabel(new GuiNpcLabel(23, "gui.color", x0, y + 5, 0xFFFFFF));
				addButton(new GuiColorButton(23, x1, y, data.color));
				y += 25;
			}
			return y;
		}

		public GuiPart noPlayerOptions() {
			hasPlayerOption = false;
			return this;
		}

		public GuiPart noPlayerTypes() {
			noPlayerTypes = true;
			return this;
		}

		public GuiPart setTypes(String[] typesIn) {
			types = typesIn;
			return this;
		}

		@Override
		public void subGuiClosed(SubGuiInterface subgui) { }

	}

	class GuiPartBeard extends GuiPart {
		public GuiPartBeard() {
			super(EnumParts.BEARD);
			noPlayerTypes().types = new String[] { "gui.none", "1", "2", "3", "4" };
		}
	}

	class GuiPartClaws extends GuiPart {
		public GuiPartClaws() {
			super(EnumParts.CLAWS);
			types = new String[] { "gui.none", "gui.show" };
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (data == null) {
				return y;
			}
			addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
			addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "gui.both", "gui.left", "gui.right" }, data.pattern));
			return y;
		}
	}

	class GuiPartEyes extends GuiPart {
		
		private final ModelEyeData eyes;

		public GuiPartEyes() {
			super(EnumParts.EYES);
			types = new String[] { "gui.none", "gui.small", "gui.normal", "gui.select" };
			noPlayerOptions();
			canBeDeleted = false;
			eyes = (ModelEyeData) data;
		}

		@Override
		protected void actionPerformed(GuiButton btn) {
			switch (btn.id) {
				case 23:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.eyeColor[1], color -> eyes.eyeColor[1] = color));
					break;
				case 24:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.eyeColor[0], color -> eyes.eyeColor[0] = color));
					break;
				case 25:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.pupilColor[0], color -> eyes.pupilColor[0] = color));
					break;
				case 26:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.pupilColor[1], color -> eyes.pupilColor[1] = color));
					break;
				case 27:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.browColor[1], color -> eyes.browColor[1] = color));
					break;
				case 28:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.browColor[0], color -> eyes.browColor[0] = color));
					break;
				case 29:
					eyes.browThickness = ((GuiNpcButton) btn).getValue();
					break;
				case 30:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.skinColor, color -> eyes.skinColor = color));
					break;
				case 31:
					eyes.closed = ((GuiNpcButton) btn).getValue();
					break;
				case 32:
					eyes.eyePos = ((GuiNpcButton) btn).getValue() - 1;
					break;
				case 33:
					eyes.glint = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 34:
					setSubGui(new GuiTextureSelection(0, null, eyes.eyeRight.toString(), "png", 5));
					break;
				case 35:
					setSubGui(new GuiTextureSelection(1, null, eyes.eyeLeft.toString(), "png", 5));
					break;
				case 36:
					setSubGui(new GuiTextureSelection(2, null, eyes.pupilRight.toString(), "png", 5));
					break;
				case 37:
					setSubGui(new GuiTextureSelection(3, null, eyes.pupilLeft.toString(), "png", 5));
					break;
				case 38:
					setSubGui(new GuiTextureSelection(4, null, eyes.browRight.toString(), "png", 5));
					break;
				case 39:
					setSubGui(new GuiTextureSelection(5, null, eyes.browLeft.toString(), "png", 5));
					break;
				case 40:
					eyes.reset();
					initGui();
					break;
				case 41:
					eyes.activeRight = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 42:
					eyes.activeLeft = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 43:
					eyes.activeCenter = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 44:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.centerColor, color -> eyes.centerColor = color));
					break;
				case 45:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.eyeColor[0], color -> {
						eyes.eyeColor[0] = color;
						eyes.eyeColor[1] = color;
					}));
					break;
				case 46:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.pupilColor[0], color -> {
						eyes.pupilColor[0] = color;
						eyes.pupilColor[1] = color;
					}));
					break;
				case 47:
					setSubGui(new GuiModelColor(GuiCreationParts.this, eyes.browColor[0], color -> {
						eyes.browColor[0] = color;
						eyes.browColor[1] = color;
					}));
					break;
				default: super.actionPerformed(btn); break;
			}
		}

		@Override
		public int initGui() {
			int y = super.initGui(); // button IDs: 20 ... 23 
			if (data != null && eyes.isEnabled()) {
				int x0 = guiLeft + 123;
				int x1 = guiLeft + 175;
				y = guiTop + 50;
				GuiNpcLabel label = (GuiNpcLabel) getLabel(20);
				GuiNpcButton button = (GuiNpcButton) getButton(20);
				label.y = y + 3;
				button.x = x1;
				button.y = y;
				button.height = 14;
				if (eyes.type != -1) {
					addButton(new GuiNpcButton(40, x1 + 104, y, 31, 14, "RND"));
				}
				
				// eye color
				y += 16;
				// left
				label = (GuiNpcLabel) getLabel(23);
				button = (GuiNpcButton) getButton(23);
				label.setLabel("eye.color.0");
				label.y = y + 3;
				button.y = y;
				button.x = x1;
				button.height = 14;
				button.width = 40;
				((GuiColorButton) button).color = eyes.eyeColor[1];
				addButton(new GuiNpcButton(45, x1 + 42, y, 18, 14, "-"));
				// right
				addButton(new GuiColorButton(24, x1 + 62, y, 40, 14, eyes.eyeColor[0]));
				if (data.type == 2) {
					addButton(new GuiNpcButton(34, x1 + 104, y, 14, 14, "EL"));
					addButton(new GuiNpcButton(35, x1 + 120, y, 14, 14, "ER"));
				}
				
				// pupil color
				y += 16;
				// left
				addLabel(new GuiNpcLabel(25, "eye.color.1", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(25, x1, y, 40, 14, eyes.pupilColor[0]);
				addButton(button);
				addButton(new GuiNpcButton(46, x1 + 42, y, 18, 14, "-"));
				// right
				addButton(new GuiColorButton(26, x1 + 62, y, 40, 14, eyes.pupilColor[1]));
				if (data.type == 2) {
					addButton(new GuiNpcButton(36, x1 + 104, y, 14, 14, "PL"));
					addButton(new GuiNpcButton(37, x1 + 120, y, 14, 14, "PR"));
				}

				// center
				y += 16;
				addLabel(new GuiNpcLabel(44, "eye.color.2", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(44, x1, y, 102, 14, eyes.centerColor);
				addButton(button);
				button = new GuiNpcButtonYesNo(43, x1 + 104, y, 20, 14, eyes.activeCenter);
				addButton(button);

				// brow color
				y += 16;
				// left
				addLabel(new GuiNpcLabel(27, "eye.color.3", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(27, x1 + 62, y, 40, 14, eyes.browColor[1]);
				addButton(button);
				addButton(new GuiNpcButton(47, x1 + 42, y, 18, 14, "-"));
				// right
				addButton(new GuiColorButton(28, x1, y, 40, 14, eyes.browColor[0]));
				if (data.type == 2) {
					addButton(new GuiNpcButton(38, x1 + 104, y, 14, 14, "BL"));
					addButton(new GuiNpcButton(39, x1 + 120, y, 14, 14, "BR"));
				}

				// brow size
				y += 16;
				addLabel(new GuiNpcLabel(29, "eye.brow", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(29, x1, y, 102, 14, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }, eyes.browThickness));

				// skin color
				y += 16;
				addLabel(new GuiNpcLabel(30, "eye.lid", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(30, x1, y, eyes.skinColor);
				button.width = 100;
				button.height = 14;
				addButton(button);
				
				// both eyes
				y += 16;
				addLabel(new GuiNpcLabel(22, "gui.draw", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(22, x1, y, 102, 14, new String[] { "gui.both", "gui.left", "gui.right" }, data.pattern));

				// closed
				y += 16;
				addLabel(new GuiNpcLabel(31, "eye.closed", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButton(31, x1, y, 102, 14, new String[] { "gui.none", "gui.both", "gui.left", "gui.right" }, eyes.closed);
				addButton(button);
				button = new GuiNpcButtonYesNo(41, x1 + 104, y, 20, 14, eyes.activeRight);
				addButton(button);
				button = new GuiNpcButtonYesNo(42, x1 + 126, y, 20, 14, eyes.activeLeft);
				addButton(button);
				
				// vertical pos
				y += 16;
				addLabel(new GuiNpcLabel(32, "gui.position", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(32, x1, y, 102, 14, new String[] { new TextComponentTranslation("gui.down").getFormattedText() + " x2", "gui.down", "gui.normal", "gui.up", new TextComponentTranslation("gui.up").getFormattedText() + " x2" }, eyes.eyePos + 1));

				// glint
				y += 16;
				addLabel(new GuiNpcLabel(33, "eye.glint", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButtonYesNo(33, x1, y, 102, 14, eyes.glint);
				addButton(button);
			}
			return y;
		}
		
		@Override
		public void subGuiClosed(SubGuiInterface subgui) {
			if (subgui instanceof GuiTextureSelection) {
				GuiTextureSelection tGui = (GuiTextureSelection) subgui;
				if (subgui.getId() == 0) { this.eyes.eyeRight = tGui.resource; }
				else if (subgui.getId() == 1) { this.eyes.eyeLeft = tGui.resource; }
				else if (subgui.getId() == 2) { this.eyes.pupilRight = tGui.resource; }
				else if (subgui.getId() == 3) { this.eyes.pupilLeft = tGui.resource; }
				else if (subgui.getId() == 4) { this.eyes.browRight = tGui.resource; }
				else if (subgui.getId() == 5) { this.eyes.browLeft = tGui.resource; }
			}
		}
		
	}

	class GuiPartHair extends GuiPart {
		public GuiPartHair() {
			super(EnumParts.HAIR);
			this.noPlayerTypes().types = new String[] { "gui.none", "1", "2", "3", "4" };
		}
	}

	class GuiPartHorns extends GuiPart {
		public GuiPartHorns() {
			super(EnumParts.HORNS);
			this.types = new String[] { "gui.none", "horns.bull", "horns.antlers", "horns.antenna" };
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (this.data != null && this.data.type == 2) {
				addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "1", "2" }, this.data.pattern));
			}
			return y;
		}
	}

	class GuiPartLegs extends GuiPart {
		public GuiPartLegs() {
			super(EnumParts.LEGS);
			this.types = new String[] { "gui.none", "gui.normal", "legs.naga", "legs.spider", "legs.horse", "legs.mermaid", "legs.digitigrade" };
			this.canBeDeleted = false;
		}

		@Override
		protected void actionPerformed(GuiButton btn) {
			if (btn.id == 20) {
				int i = ((GuiNpcButton) btn).getValue();
				this.data.playerTexture = (i <= 1);
			}
			super.actionPerformed(btn);
		}

		@Override
		public int initGui() {
			this.hasPlayerOption = (this.data.type == 1 || this.data.type == 5);
			return super.initGui();
		}
	}

	class GuiPartParticles extends GuiPart {
		public GuiPartParticles() {
			super(EnumParts.PARTICLES);
			this.types = new String[] { "gui.none", "1", "2" };
		}

		@Override
		public int initGui() {
            return super.initGui();
		}
	}

	class GuiPartSnout extends GuiPart {
		public GuiPartSnout() {
			super(EnumParts.SNOUT);
			this.types = new String[] { "gui.none", "snout.small", "snout.medium", "snout.large", "snout.bunny",
					"snout.beak" };
		}
	}

	class GuiPartTail extends GuiPart {
		public GuiPartTail() {
			super(EnumParts.TAIL);
			this.types = new String[] { "gui.none", "part.tail", "tail.dragon", "tail.horse", "tail.squirrel",
					"tail.fin", "tail.rodent", "tail.bird", "tail.fox" };
		}

		@Override
		public int initGui() {
			this.data = playerdata.getPartData(this.part);
			this.hasPlayerOption = (this.data != null
					&& (this.data.type == 0 || this.data.type == 1 || this.data.type == 6 || this.data.type == 7));
			int y = super.initGui();
			if (this.data != null && this.data.type == 0) {
				addLabel(new GuiNpcLabel(22, "gui.pattern", guiLeft + 123, y + 5, 0xFFFFFF));
				addButton(new GuiButtonBiDirectional(22, guiLeft + 175, y, 100, 20, new String[] { "1", "2" }, this.data.pattern));
			}
			return y;
		}
	}

	class GuiPartWings extends GuiPart {
		public GuiPartWings() {
			super(EnumParts.WINGS);
			this.setTypes(new String[] { "gui.none", "1", "2", "3", "4" });
		}

		@Override
		public int initGui() {
			return super.initGui();
		}
	}

	class GuiPartLayers extends GuiPart {

		public boolean isCustomLayers = false;
		private final GuiCreationParts parent;
		public GuiCustomScroll scrollIn;
		public int selectPos = 0;
		public HashSet<String> selectedList = new HashSet<>();
		public Map<Integer, EnumParts> partNames = new LinkedHashMap<>();

        public GuiPartLayers(GuiCreationParts parentIn) {
			super(EnumParts.CUSTOM_LAYERS);
			parent = parentIn;
			partNames.put(0, EnumParts.HEAD);
			partNames.put(1, EnumParts.BODY);
			partNames.put(2, EnumParts.ARM_RIGHT);
			partNames.put(3, EnumParts.ARM_LEFT);
			partNames.put(4, EnumParts.LEG_RIGHT);
			partNames.put(5, EnumParts.LEG_LEFT);
			partNames.put(6, EnumParts.WRIST_RIGHT);
			partNames.put(7, EnumParts.WRIST_LEFT);
			partNames.put(8, EnumParts.FOOT_RIGHT);
			partNames.put(9, EnumParts.FOOT_LEFT);
		}

		@Override
		protected void actionPerformed(GuiButton btn) {
			switch (btn.id) {
				case 20: {
					isCustomLayers = ((GuiNpcCheckBox) btn).isSelected();
					parent.initGui();
					break;
				} // change isCustomLayers
				case 21: {
					SlotAvailability slot = ((ContainerLayer) inventorySlots).slot;
					if (slot == null) { return; }
					selectPos = playerdata.addNewLayer((NpcMiscInventory) ((ContainerLayer) inventorySlots).slot.inventory);
					parent.initGui();
					break;
				} // add new item layer
				case 22: {
					SlotAvailability slot = ((ContainerLayer) inventorySlots).slot;
					if (slot == null) { return; }
					selectPos = playerdata.removeLayer(selectPos);
					parent.initGui();
					break;
				} // remove item layer
				case 23: {
					if (toolType == 1) { return; }
					GuiNpcTextField.unfocus();
					toolType = 1;
					parent.initGui();
					break;
				} // select tool pos
				case 24: {
					if (toolType == 0) { return; }
					GuiNpcTextField.unfocus();
					toolType = 0;
					parent.initGui();
					break;
				} // select tool rot
				case 25: {
					if (toolType == 2) { return; }
					GuiNpcTextField.unfocus();
					toolType = 2;
					parent.initGui();
					break;
				} // select tool scale
				case 26: {
					resetAxis(0);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){
						((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
					}
					break;
				} // reset X
				case 27: {
					resetAxis(1);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){
						((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
					}
					break;
				} // reset Y
				case 28: {
					resetAxis(2);
					if (showEntity instanceof EntityCustomNpc && playerdata != null){
						((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
					}
					break;
				} // reset Z
				case 29: {
					SlotAvailability slot = ((ContainerLayer) inventorySlots).slot;
					if (slot == null || !partNames.containsKey(((GuiNpcButton) btn).getValue())) { return; }
					playerdata.moveLayerTo(selectPos, partNames.get(((GuiNpcButton) btn).getValue()) );
					if (showEntity instanceof EntityCustomNpc && playerdata != null){
						((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
					}
					break;
				} // reset part
				default: super.actionPerformed(btn); break;
			}
		}

		@Override
		public int initGui() {
			GuiNpcTextField.unfocus();
			parent.components.removeIf(component -> component.getID() != 66 && component.getID() != 500 && component.getID() > 19 || (component instanceof GuiCustomScroll && component.getID() == 1));
			parent.buttonList.removeIf(button -> button.id != 500 && button.id > 19);
			for (int id : new ArrayList<>(parent.buttons.keySet())) {
				if (id != 66 && id != 500 && id > 19 || parent.buttons.get(id).getID() == 1) { parent.buttons.remove(id); }
			}
			for (int id : new ArrayList<>(parent.labels.keySet())) {
				if (id > 19 || parent.labels.get(id).getID() == 1) { parent.labels.remove(id); }
			}
			for (int id : new ArrayList<>(parent.scrolls.keySet())) {
				if (id > 1 || parent.scrolls.get(id).getID() == 1) { parent.scrolls.remove(id); }
			}
			int x0 = guiLeft + 123;
			int y = guiTop;
			addButton(new GuiNpcCheckBox(20, x0, y, 100, 20, "part.layers.true", "part.layers.false", isCustomLayers));
			((GuiNpcCheckBox) getButton(20)).setColor(CustomNpcs.MainColor.getRGB(), false);
			y += 25;
			GuiNpcButton button;
            if (isCustomLayers) {
				LayerModel lm = playerdata.getLayerModel(selectPos);
				SlotAvailability slot = ((ContainerLayer) inventorySlots).slot;
				GuiNpcSlider slider;
				GuiNpcTextField textField;
				scrollIn = new GuiCustomScroll(parent, 1);
				scrollIn.setSize(100, 102);
				slot.setInventory(playerdata.getLayerInventory());
				slot.setSlotIndex(selectPos, false);
				selectPos = slot.getSlotIndex();
				scrollIn.setListNotSorted(playerdata.getLayerKeys());
				scrollIn.setSelect(selectPos);
				Client.sendData(EnumPacketServer.AvailabilitySlot, selectPos);
				addLabel(new GuiNpcLabel(20, "part.layers.info.0", x0, y));
				getLabel(20).setColor(CustomNpcs.MainColor.getRGB());
				int y1 = y + 116;
				addButton(button = new GuiNpcButton(21, x0, y1, 49, 20, "gui.add"));
				button.setEnabled(slot.inventory.getSizeInventory() == scrollIn.getList().size());
				addButton(button = new GuiNpcButton(22, x0 + 51, y1, 49, 20, "gui.remove"));
				button.setEnabled(scrollIn.hasSelected());
				int x1 = x0 + 104;

				String objModel = lm.getOBJ() == null ? "" : lm.getOBJ().toString();
				y1 = y - 21;
				addLabel(new GuiNpcLabel(19, "OBJ Model path:", x1 + 1, y1));
				getLabel(19).setColor(CustomNpcs.MainColor.getRGB());
				addTextField(new GuiNpcTextField(0, parent, x1, y1 += 12, 140, 16, objModel));
				addLabel(new GuiNpcLabel(22, "Tool type:", x1 + 1, (y1 += 19) + 2));
				getLabel(22).setColor(CustomNpcs.MainColor.getRGB());

				// tool pos
				button = new GuiNpcButton(23,x1 + 50, y1, 14, 14, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.isAnim = true;
				button.txrW = 24;
				button.txrH = 24;
				button.layerColor = toolType == 1 ?
						new Color(0xFFFF4040).getRGB() :
						new Color(0xFFFFFFFF).getRGB();
				button.setHoverText("animation.hover.tool.0");
				addButton(button);
				// tool rot
				button = new GuiNpcButton(24, x1 + 66, y1, 14, 14, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.isAnim = true;
				button.txrX = 24;
				button.txrW = 24;
				button.txrH = 24;
				button.layerColor = toolType == 0 ?
						new Color(0xFF40FF40).getRGB() :
						new Color(0xFFFFFFFF).getRGB();
				button.setHoverText("animation.hover.tool.1");
				addButton(button);
				// tool scale
				button = new GuiNpcButton(25, x1 + 82, y1, 14, 14, "");
				button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
				button.hasDefBack = false;
				button.isAnim = true;
				button.txrX = 48;
				button.txrW = 24;
				button.txrH = 24;
				button.layerColor = toolType == 2 ?
						new Color(0xFF4040FF).getRGB() :
						new Color(0xFFFFFFFF).getRGB();
				button.setHoverText("animation.hover.tool.2");
				addButton(button);

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
							v = Math.round(lm.rotation[i] * 1000.0f) / 1000.0f;
							s = 0.002778f * lm.rotation[i] + 0.5f;
							min = -180.0f;
							max = 180.0f;
							break;
						}
						case 1: {
							v = Math.round(lm.offset[i] * 2000.0f) / 1000.0f;
							s = 0.2f * lm.offset[i] + 0.5f;
							min = -10.0f;
							max = 10.0f;
							break;
						}
						default: {
							v = Math.round(lm.scale[i] * 1000.0f) / 1000.0f;
							s = 0.2f *lm.scale[i];
							min = 0.0f;
							max = 5.0f;
							break;
						}
					}
					slider = new GuiNpcSlider(parent, id, x1 + 9, y1 + i * f, 78, 8, s);
					String hover = "animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale");
					slider.setHoverText(hover, i == 0 ? "X" : i == 1 ? "Y" : "Z");
					addSlider(slider);
					textField = new GuiNpcTextField(id, parent, x1 + 89, y1 + i * f, 42, 8, "" + v);
					textField.setMinMaxDoubleDefault(min, max, v);
					textField.setHoverText(hover, i == 0 ? "X" : i == 1 ? "Y" : "Z");
					addTextField(textField);
					button = new GuiNpcButton(id, x1 + 133, y1 + i * f, 8, 8, "X");
					button.texture = GuiNPCInterface.ANIMATION_BUTTONS;
					button.hasDefBack = false;
					button.isAnim = true;
					button.txrY = 96;
					button.dropShadow = false;
					button.setTextColor(0xFFDC0000);
					button.setHoverText("animation.hover.reset." + toolType, i == 0 ? "X" : i == 1 ? "Y" : "Z");
					addButton(button);
				}  // 26 ... 28
				y1 += f * 3 + 2;
				int pos = 0;
				String[] names = new String[partNames.size()];
				for (int i : partNames.keySet()) {
					if (partNames.get(i) == lm.part) { pos = i; }
					names[i] = "part." + partNames.get(i).name;
				}
				addButton(new GuiButtonBiDirectional(29, x1, y1, 78, 14, names, pos));
			}
			else {
				scrollIn = new GuiCustomScroll(parent, 1, true);
				scrollIn.setSize(120, 175);
				scrollIn.canSearch(false);
				addLabel(new GuiNpcLabel(20, "part.layers.info.1", x0, y));
				getLabel(20).setColor(CustomNpcs.MainColor.getRGB());
				Render<Entity> render = mc.getRenderManager().getEntityRenderObject(npc);
				if (render instanceof RenderCustomNpc) {
					scrollIn.setList(((RenderCustomNpc<?>) render).getLayerRendererNames());
				}
				scrollIn.setSelectedList(selectedList);
				selectedList = scrollIn.getSelectedList();
			}
			y += 12;
			scrollIn.guiLeft = x0;
			scrollIn.guiTop = y;
			addScroll(scrollIn);
			if (showEntity instanceof EntityCustomNpc && playerdata != null){
				((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
			}
			return y;
		}

	}

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
				new GuiPart(EnumParts.EARS).setTypes(new String[] { "gui.none", "gui.normal", "ears.bunny" }),
				new GuiPartHorns(),
				new GuiPartHair(),
				new GuiPart(EnumParts.MOHAWK).setTypes(new String[] { "gui.none", "1", "2" }).noPlayerOptions(),
				new GuiPartSnout(),
				new GuiPartBeard(),
				new GuiPart(EnumParts.FIN).setTypes(new String[] { "gui.none", "fin.shark", "fin.reptile" }),
				new GuiPart(EnumParts.BREASTS).setTypes(new String[] { "gui.none", "1", "2", "3" }).noPlayerOptions(),
				new GuiPartWings(),
				new GuiPartClaws(),
				new GuiPart(EnumParts.SKIRT).setTypes(new String[] { "gui.none", "gui.normal" }), new GuiPartLegs(),
				new GuiPartTail(),
				new GuiPartEyes(),
				new GuiPartParticles(),
				new GuiPartLayers(this) };
		this.active = 2;
		this.closeOnEsc = false;
		Arrays.sort(parts, (o1, o2) -> {
			String s1 = new TextComponentTranslation("part." + o1.part.name).getFormattedText();
			String s2 = new TextComponentTranslation("part." + o2.part.name).getFormattedText();
			return s1.compareToIgnoreCase(s2);
		});
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (getPart() != null) {
			getPart().actionPerformed(btn);
		}
	}
	
	protected GuiPart getPart() { return this.parts[GuiCreationParts.selected]; }

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GuiPart part = getPart();
		if (part instanceof GuiPartLayers && ((GuiPartLayers) part).isCustomLayers) {
			GlStateManager.pushMatrix();
			GlStateManager.translate( guiLeft + 121.0f, guiTop + 163.0f, 0.0f);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			int x;
			int y;
			for (Slot slot : inventorySlots.inventorySlots) {
				if (slot instanceof SlotAvailability) {
					x = 164;
					y = 0;
				}
				else {
					x = (slot.getSlotIndex() % 9) * 18;
					y = (slot.getSlotIndex() / 9) * 18;
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
		if (!isCheck && part instanceof GuiPartLayers && inventorySlots instanceof ContainerLayer && getButton(21) != null) {
			CustomNPCsScheduler.runTack(() -> {
				isCheck = true;
				List<String> scrollList = ((GuiPartLayers) part).scrollIn.getList();
				Slot slot = ((ContainerLayer) inventorySlots).slot;
				NpcMiscInventory inv = (NpcMiscInventory) slot.inventory;
				boolean bo = true;
				boolean isInit = false;
				for (int i = 0; i < scrollList.size(); ++i) {
					ItemStack stack = i == slot.getSlotIndex() ? slot.getStack() : inv.getStackInSlot(i);
					if (i >= scrollList.size() || !((i + 1) + ": " + stack.getDisplayName()).equals(scrollList.get(i))) {
						playerdata.getLayerModel(i).setStack(stack);
						isInit = true;
					}
					if (stack.isEmpty()) { bo = false; }
				}
				getButton(21).setEnabled(bo);
				if (isInit) { initGui(); }
				isCheck = false;
			});
		}
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(1) != null && this.getButton(1).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.parts").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.size").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		} else if (this.getButton(20) != null && this.getButton(20).isHovered()) {
			if (!(getPart() instanceof GuiPartLayers)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.type").getFormattedText());
			}
		} else if (this.getButton(21) != null && this.getButton(21).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.skin").getFormattedText());
		} else if (this.getButton(22) != null && this.getButton(22).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.pattern").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isHovered()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getPart() instanceof GuiPartEyes) {
			if (this.getButton(23) != null && this.getButton(23).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(23).getLeft(), this.getButton(23).getTop(), this.getButton(23).getWidth(), this.getButton(23).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.color.r").getFormattedText());
			} else if (this.getButton(24) != null && this.getButton(24).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(24).getLeft(), this.getButton(24).getTop(), this.getButton(24).getWidth(), this.getButton(24).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.color.l").getFormattedText());
			} else if (this.getButton(25) != null && this.getButton(25).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(25).getLeft(), this.getButton(25).getTop(), this.getButton(25).getWidth(), this.getButton(25).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.color.r").getFormattedText());
			} else if (this.getButton(26) != null && this.getButton(26).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(26).getLeft(), this.getButton(26).getTop(), this.getButton(26).getWidth(), this.getButton(26).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.color.l").getFormattedText());
			} else if (this.getButton(27) != null && this.getButton(27).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(27).getLeft(), this.getButton(27).getTop(), this.getButton(27).getWidth(), this.getButton(27).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.color.r").getFormattedText());
			} else if (this.getButton(28) != null && this.getButton(28).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(28).getLeft(), this.getButton(28).getTop(), this.getButton(28).getWidth(), this.getButton(28).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.color.l").getFormattedText());
			} else if (this.getButton(29) != null && this.getButton(29).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.size").getFormattedText());
			} else if (this.getButton(30) != null && this.getButton(30).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(30).getLeft(), this.getButton(30).getTop(), this.getButton(30).getWidth(), this.getButton(30).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.skin.color").getFormattedText());
			} else if (this.getButton(31) != null && this.getButton(31).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.closed").getFormattedText());
			} else if (this.getButton(32) != null && this.getButton(32).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pos").getFormattedText());
			} else if (this.getButton(33) != null && this.getButton(33).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.glint").getFormattedText());
			} else if (this.getButton(34) != null && this.getButton(34).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.txr.r").getFormattedText());
			} else if (this.getButton(35) != null && this.getButton(35).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.txr.l").getFormattedText());
			} else if (this.getButton(36) != null && this.getButton(36).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.txr.r").getFormattedText());
			} else if (this.getButton(37) != null && this.getButton(37).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.txr.l").getFormattedText());
			} else if (this.getButton(38) != null && this.getButton(38).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.txr.r").getFormattedText());
			} else if (this.getButton(39) != null && this.getButton(39).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.txr.l").getFormattedText());
			} else if (this.getButton(40) != null && this.getButton(40).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.rnd").getFormattedText());
			} else if ((this.getButton(41) != null && this.getButton(41).isHovered()) || (this.getButton(42) != null && this.getButton(42).isHovered())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.active").getFormattedText());
			} else if (this.getButton(43) != null && this.getButton(43).isHovered()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.center.active").getFormattedText());
			} else if (this.getButton(45) != null && this.getButton(45).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(45).getLeft(), this.getButton(45).getTop(), this.getButton(45).getWidth(), this.getButton(45).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.color").getFormattedText());
			} else if (this.getButton(46) != null && this.getButton(46).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(46).getLeft(), this.getButton(46).getTop(), this.getButton(46).getWidth(), this.getButton(46).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.color").getFormattedText());
			} else if (this.getButton(47) != null && this.getButton(47).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(47).getLeft(), this.getButton(47).getTop(), this.getButton(47).getWidth(), this.getButton(47).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.color").getFormattedText());
			}
		} else {
			if (this.getButton(23) != null && this.getButton(23).isVisible() && isMouseHover(mouseX, mouseY, this.getButton(23).getLeft(), this.getButton(23).getTop(), this.getButton(23).getWidth(), this.getButton(23).getHeight())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.color").getFormattedText());
				return;
			}
			for (GuiButton b : this.buttonList) {
				if (b != null && b.isMouseOver()) {
					if (b.id == 500) {
						this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").getFormattedText());
					}
				}
			}
		}
		drawHoverText(null);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.entity != null) {
			this.openGui(new GuiCreationExtra(npc, (ContainerLayer) inventorySlots));
			return;
		}
		if (this.scroll == null) {
			List<String> list = new ArrayList<>();
			for (GuiPart part : this.parts) {
				list.add(new TextComponentTranslation("part." + part.part.name).getFormattedText());
			}
			(this.scroll = new GuiCustomScroll(this, 0)).setListNotSorted(list);
		}
		this.scroll.guiLeft = guiLeft;
		this.scroll.guiTop = guiTop + 46;
		this.scroll.setSize(121, ySize - 74);
		this.addScroll(this.scroll);
		if (this.getPart() != null) {
			this.scroll.setSelected(new TextComponentTranslation("part." + this.getPart().part.name).getFormattedText());
			this.getPart().initGui();
		}
		if (inventorySlots instanceof ContainerLayer) {
			boolean bo = getPart() instanceof GuiPartLayers && ((GuiPartLayers) getPart()).isCustomLayers;
			int i;
			int x;
			int y;
			for (Slot slot : inventorySlots.inventorySlots) {
				if (slot instanceof SlotAvailability) {
					x = 164;
					y = 0;
				}
				else {
					i = slot.getSlotIndex();
					x = (i % 9) * 18;
					y = (i / 9) * 18;
					if (i < 9) { y += 54;} else if (i != 36) { y -= 18;}
				}
				slot.xPos = bo ? 122 + x : -5000;
				slot.yPos = bo ? 164 + y : -5000;
			}
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0 && scroll.getSelect() >= 0) {
			GuiCreationParts.selected = scroll.getSelect();
			initGui();
		}
		if (scroll.getID() == 1 && getPart() instanceof GuiPartLayers) {
			GuiPartLayers part = (GuiPartLayers) getPart();
			if (part.isCustomLayers) {
				((GuiPartLayers) getPart()).selectPos = scroll.getSelect();
			} else {
				((GuiPartLayers) getPart()).selectedList = scroll.getSelectedList();
				npc.display.setDisableLayers(scroll.getSelectedList().toArray(new String[0]));
			}
			part.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (this.getPart() != null) {
			this.getPart().subGuiClosed(subgui);
		}
		this.initGui();
	}

	@Override
	public void keyTyped(char c, int i) {
		if (subgui == null && getPart() instanceof GuiPartLayers) {
			// tool pos - Alt + Q
			if (isPressAndKey(16) && toolType != 1) {
				toolType = 1;
				playButtonClick();
				initGui();
			}
			// tool rot - Alt + W
			if (isPressAndKey(17) && toolType != 0) {
				toolType = 0;
				playButtonClick();
				initGui();
			}
			// tool rot - Alt + E
			if (isPressAndKey(18) && toolType != 2) {
				toolType = 2;
				playButtonClick();
				initGui();
			}
		}
		super.keyTyped(c, i);
	}

	private void playButtonClick() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private boolean isPressAndKey(int id) {
		if (waitKey > 0 && waitKeyID == id) { return false; }
		boolean isPress = isAltKeyDown() && Keyboard.isKeyDown(id);
		if (isPress) {
			waitKey = 30;
			waitKeyID = id;
		}
		return isPress;
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		if (getPart() instanceof GuiPartLayers) {
			switch (slider.getID()) {
				case 26: {
					sliderMoved(0, slider.getSliderValue());
					break;
				} // X
				case 27: {
					sliderMoved(1, slider.getSliderValue());
					break;
				} // Y
				case 28: {
					sliderMoved(2, slider.getSliderValue());
					break;
				} // Z
				default: {
					super.mouseDragged(slider);
					break;
				}
			}
			if (showEntity instanceof EntityCustomNpc && playerdata != null){
				((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
			}
			return;
		}
		super.mouseDragged(slider);
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(IGuiNpcSlider slider) {
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (getPart() instanceof GuiPartLayers) {
			switch (textField.getID()) {
				case 26: {
					textFieldChanged(0, (float) textField.getDouble());
					break;
				} // X
				case 27: {
					textFieldChanged(1, (float) textField.getDouble());
					break;
				} // Y
				case 28: {
					textFieldChanged(2, (float) textField.getDouble());
					break;
				} // Z
			}
			if (showEntity instanceof EntityCustomNpc && playerdata != null){
				((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
			}
			return;
		}
		super.unFocused(textField);
	}

	private void sliderMoved(int id, float sliderValue) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		switch (toolType) {
			case 0: {
				lm.rotation[id] = 360.0f * sliderValue - 180.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("" + (Math.round(lm.rotation[id] * 1000.0f) / 1000.0f)); }
				break;
			} // rotation
			case 1: {
				lm.offset[id] = 5.0f * sliderValue - 2.5f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("" + (Math.round(lm.offset[id] * 2000.0f) / 1000.0f)); }
				break;
			} // offset
			default: {
				lm.scale[id] = 5.0f * sliderValue;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("" + (Math.round(lm.scale[id] * 1000.0f) / 1000.0f)); }
				break;
			} // scale
		}
	}

	private void textFieldChanged(int id, float textValue) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		switch (toolType) {
			case 0: {
				lm.rotation[id] = textValue;
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.002778f * lm.rotation[id] + 0.5f); }
				break;
			} // rotation
			case 1: {
				lm.offset[id] = textValue / 2.0f;
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.offset[id] + 0.5f); }
				break;
			} // offset
			default: {
				lm.scale[id] = textValue;
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.scale[id]); }
				break;
			} // scale
		}
	}

	private void resetAxis(int id) {
		LayerModel lm = playerdata.getLayerModel(((GuiPartLayers) getPart()).selectPos);
		switch (toolType) {
			case 0: {
				lm.rotation[id] = 0.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("0.0"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.002778f * lm.rotation[id] + 0.5f); }
				break;
			} // rotation
			case 1: {
				lm.offset[id] = 0.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("0.0"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.offset[id] + 0.5f); }
				break;
			} // offset
			default: {
				lm.scale[id] = 1.0f;
				if (getTextField(26 + id) != null) { getTextField(26 + id).setFullText("1.0"); }
				if (getSlider(26 + id) != null) { getSlider(26 + id).setSliderValue(0.2f * lm.scale[id]); }
				break;
			} // scale
		}
	}

}
