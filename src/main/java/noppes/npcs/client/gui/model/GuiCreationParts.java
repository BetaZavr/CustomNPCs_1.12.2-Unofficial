package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationParts
extends GuiCreationScreenInterface
implements ITextfieldListener, ICustomScrollListener, ISubGuiListener  {

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
		public void subGuiClosed(ISubGuiInterface subgui) { }

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
			System.out.println("buttonID: "+btn.id);
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
			}
			if (btn.id < 23) { super.actionPerformed(btn); }
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
				((GuiColorButton) button).color = eyes.eyeColor[1];
				// right
				button = new GuiColorButton(24, x1 + 52, y, eyes.eyeColor[0]);
				button.height = 14;
				addButton(button);
				if (data.type == 2) {
					addButton(new GuiNpcButton(34, x1 + 104, y, 14, 14, "EL"));
					addButton(new GuiNpcButton(35, x1 + 120, y, 14, 14, "ER"));
				}
				
				// pupil color
				y += 16;
				// left
				addLabel(new GuiNpcLabel(25, "eye.color.1", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(25, x1, y, eyes.pupilColor[0]);
				button.height = 14;
				addButton(button);
				// right
				button = new GuiColorButton(26, x1 + 52, y, eyes.pupilColor[1]);
				button.height = 14;
				addButton(button);
				if (data.type == 2) {
					addButton(new GuiNpcButton(36, x1 + 104, y, 14, 14, "PL"));
					addButton(new GuiNpcButton(37, x1 + 120, y, 14, 14, "PR"));
				}

				// center
				y += 16;
				addLabel(new GuiNpcLabel(44, "eye.color.2", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(44, x1, y, eyes.centerColor);
				button.width = 100;
				button.height = 14;
				addButton(button);
				button = new GuiNpcButtonYesNo(43, x1 + 104, y, eyes.activeCenter);
				button.width = 20;
				button.height = 14;
				addButton(button);

				// brow color
				y += 16;
				// left
				addLabel(new GuiNpcLabel(27, "eye.color.3", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(27, x1 + 52, y, eyes.browColor[1]);
				button.height = 14;
				addButton(button);
				// right
				button = new GuiColorButton(28, x1, y, eyes.browColor[0]);
				button.height = 14;
				addButton(button);
				if (data.type == 2) {
					addButton(new GuiNpcButton(38, x1 + 104, y, 14, 14, "BL"));
					addButton(new GuiNpcButton(39, x1 + 120, y, 14, 14, "BR"));
				}

				// brow size
				y += 16;
				addLabel(new GuiNpcLabel(29, "eye.brow", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(29, x1, y, 100, 14, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }, eyes.browThickness));

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
				addButton(new GuiNpcButton(22, x1, y, 100, 14, new String[] { "gui.both", "gui.left", "gui.right" }, data.pattern));

				// closed
				y += 16;
				addLabel(new GuiNpcLabel(31, "eye.closed", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButton(31, x1, y, 100, 14, new String[] { "gui.none", "gui.both", "gui.left", "gui.right" }, eyes.closed);
				addButton(button);
				button = new GuiNpcButtonYesNo(41, x1 + 104, y, eyes.activeRight);
				button.width = 20;
				button.height = 14;
				addButton(button);
				button = new GuiNpcButtonYesNo(42, x1 + 126, y, eyes.activeLeft);
				button.width = 20;
				button.height = 14;
				addButton(button);
				
				// vertical pos
				y += 16;
				addLabel(new GuiNpcLabel(32, "gui.position", x0, y + 3, 0xFFFFFF));
				addButton(new GuiNpcButton(32, x1, y, 100, 14, new String[] { new TextComponentTranslation("gui.down").getFormattedText() + " x2", "gui.down", "gui.normal", "gui.up", new TextComponentTranslation("gui.up").getFormattedText() + " x2" }, eyes.eyePos + 1));

				// glint
				y += 16;
				addLabel(new GuiNpcLabel(33, "eye.glint", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButtonYesNo(33, x1, y, eyes.glint);
				button.width = 100;
				button.height = 14;
				addButton(button);
			}
			return y;
		}
		
		@Override
		public void subGuiClosed(ISubGuiInterface subgui) {
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

	private static int selected = 0;

	private final GuiPart[] parts;

	private GuiCustomScroll scroll;

	public GuiCreationParts(EntityNPCInterface npc) {
		super(npc);
		this.parts = new GuiPart[] {
				new GuiPart(EnumParts.EARS).setTypes(new String[] { "gui.none", "gui.normal", "ears.bunny" }),
				new GuiPartHorns(), new GuiPartHair(),
				new GuiPart(EnumParts.MOHAWK).setTypes(new String[] { "gui.none", "1", "2" }).noPlayerOptions(),
				new GuiPartSnout(), new GuiPartBeard(),
				new GuiPart(EnumParts.FIN).setTypes(new String[] { "gui.none", "fin.shark", "fin.reptile" }),
				new GuiPart(EnumParts.BREASTS).setTypes(new String[] { "gui.none", "1", "2", "3" }).noPlayerOptions(),
				new GuiPartWings(), new GuiPartClaws(),
				new GuiPart(EnumParts.SKIRT).setTypes(new String[] { "gui.none", "gui.normal" }), new GuiPartLegs(),
				new GuiPartTail(), new GuiPartEyes(), new GuiPartParticles() };
		this.active = 2;
		this.closeOnEsc = false;
		Arrays.sort(this.parts, (o1, o2) -> {
			String s1 = new TextComponentTranslation("part." + o1.part.name).getFormattedText();
			String s2 = new TextComponentTranslation("part." + o2.part.name).getFormattedText();
			return s1.compareToIgnoreCase(s2);
		});
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (this.getPart() != null) {
			this.getPart().actionPerformed(btn);
		}
	}
	
	protected GuiPart getPart() { return this.parts[GuiCreationParts.selected]; }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
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
			this.setHoverText(new TextComponentTranslation("display.hover.part.type").getFormattedText());
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
			this.openGui(new GuiCreationExtra(this.npc));
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
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getSelect() >= 0) {
			GuiCreationParts.selected = scroll.getSelect();
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }
	
	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (this.getPart() != null) {
			this.getPart().subGuiClosed(subgui);
		}
		this.initGui();
	}
	
}
