package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiColorButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationParts
extends GuiCreationScreenInterface
implements ITextfieldListener, ICustomScrollListener, ISubGuiListener  {

	class GuiPart implements ISubGuiListener {

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
			this.data = GuiCreationParts.this.playerdata.getPartData(part);
		}

		protected void actionPerformed(GuiButton btn) {
			if (btn.id == 20) {
				int i = ((GuiNpcButton) btn).getValue();
				if (i == 0 && this.canBeDeleted) {
					GuiCreationParts.this.playerdata.removePart(this.part);
				} else {
					this.data = GuiCreationParts.this.playerdata.getOrCreatePart(this.part);
					this.data.pattern = 0;
					this.data.setType(i - 1);
				}
				GuiCreationParts.this.initGui();
			}
			if (btn.id == 22) {
				this.data.pattern = (byte) ((GuiNpcButton) btn).getValue();
			}
			if (btn.id == 21) {
				this.data.playerTexture = ((GuiNpcButtonYesNo) btn).getBoolean();
				GuiCreationParts.this.initGui();
			}
			if (btn.id == 23) {
				GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.data.color, color -> this.data.color = color));
			}
		}

		public int initGui() {
			this.data = GuiCreationParts.this.playerdata.getPartData(this.part);
			int y = GuiCreationParts.this.guiTop + 50;
			if (this.data == null || !this.data.playerTexture || !this.noPlayerTypes) {
				GuiCreationParts.this.addLabel(new GuiNpcLabel(20, "gui.type", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(20, GuiCreationParts.this.guiLeft + 145, y, 100, 20, this.types, (this.data == null) ? 0 : (this.data.type + 1)));
				y += 25;
			}
			if (this.data != null && this.hasPlayerOption) {
				GuiCreationParts.this.addLabel(new GuiNpcLabel(21, "gui.playerskin", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiNpcButtonYesNo(21, GuiCreationParts.this.guiLeft + 170, y, this.data.playerTexture));
				y += 25;
			}
			if (this.data != null && !this.data.playerTexture) {
				GuiCreationParts.this.addLabel(new GuiNpcLabel(23, "gui.color", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiColorButton(23, GuiCreationParts.this.guiLeft + 170, y, this.data.color));
				y += 25;
			}
			return y;
		}

		public GuiPart noPlayerOptions() {
			this.hasPlayerOption = false;
			return this;
		}

		public GuiPart noPlayerTypes() {
			this.noPlayerTypes = true;
			return this;
		}

		public GuiPart setTypes(String[] types) {
			this.types = types;
			return this;
		}

		@Override
		public void subGuiClosed(SubGuiInterface subgui) { }

	}

	class GuiPartBeard extends GuiPart {
		public GuiPartBeard() {
			super(EnumParts.BEARD);
			this.noPlayerTypes().types = new String[] { "gui.none", "1", "2", "3", "4" };
		}
	}

	class GuiPartClaws extends GuiPart {
		public GuiPartClaws() {
			super(EnumParts.CLAWS);
			this.types = new String[] { "gui.none", "gui.show" };
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (this.data == null) {
				return y;
			}
			GuiCreationParts.this.addLabel(new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
			GuiCreationParts.this.addButton(new GuiButtonBiDirectional(22, GuiCreationParts.this.guiLeft + 145, y, 100, 20, new String[] { "gui.both", "gui.left", "gui.right" }, this.data.pattern));
			return y;
		}
	}

	class GuiPartEyes extends GuiPart {
		
		private final ModelEyeData eyes;

		public GuiPartEyes() {
			super(EnumParts.EYES);
			this.types = new String[] { "gui.none", "gui.small", "gui.normal", "gui.select" };
			this.noPlayerOptions();
			this.canBeDeleted = false;
			this.eyes = (ModelEyeData) this.data;
		}

		@Override
		protected void actionPerformed(GuiButton btn) {
			switch (btn.id) {
				case 23:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.eyeColor[1], color -> this.eyes.eyeColor[1] = color));
					break;
				case 24:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.eyeColor[0], color -> this.eyes.eyeColor[0] = color));
					break;
				case 25:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.pupilColor[0], color -> this.eyes.pupilColor[0] = color));
					break;
				case 26:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.pupilColor[1], color -> this.eyes.pupilColor[1] = color));
					break;
				case 27:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.browColor[1], color -> this.eyes.browColor[1] = color));
					break;
				case 28:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.browColor[0], color -> this.eyes.browColor[0] = color));
					break;
				case 29:
					this.eyes.browThickness = ((GuiNpcButton) btn).getValue();
					break;
				case 30:
					GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.skinColor, color -> this.eyes.skinColor = color));
					break;
				case 31:
					this.eyes.closed = ((GuiNpcButton) btn).getValue();
					break;
				case 32:
					this.eyes.eyePos = ((GuiNpcButton) btn).getValue() - 1;
					break;
				case 33:
					this.eyes.glint = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 34:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(0, null, this.eyes.eyeRight.toString(), "png", 5));
					break;
				case 35:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(1, null, this.eyes.eyeLeft.toString(), "png", 5));
					break;
				case 36:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(2, null, this.eyes.pupilRight.toString(), "png", 5));
					break;
				case 37:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(3, null, this.eyes.pupilLeft.toString(), "png", 5));
					break;
				case 38:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(4, null, this.eyes.browRight.toString(), "png", 5));
					break;
				case 39:
					GuiCreationParts.this.setSubGui(new GuiTextureSelection(5, null, this.eyes.browLeft.toString(), "png", 5));
					break;
				case 40:
					this.eyes.reset();
					this.initGui();
					break;
				case 41:
					this.eyes.activeRight = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
				case 42:
					this.eyes.activeLeft = ((GuiNpcButtonYesNo) btn).getBoolean();
					break;
			}
			if (btn.id < 23) { super.actionPerformed(btn); }
		}

		@Override
		public int initGui() {
			int y = super.initGui(); // button IDs: 20 ... 23 
			if (this.data != null && this.eyes.isEnabled()) {
				int x0 = GuiCreationParts.this.guiLeft + 102;
				int x1 = GuiCreationParts.this.guiLeft + 155;
				y = GuiCreationParts.this.guiTop + 50;
				GuiNpcLabel lable = GuiCreationParts.this.getLabel(20);
				GuiNpcButton button = GuiCreationParts.this.getButton(20);
				lable.y = y + 3;
				button.x = x1;
				button.y = y;
				button.height = 14;
				if (this.eyes.type != -1) {
					GuiCreationParts.this.addButton(new GuiNpcButton(40, x1 + 104, y, 31, 14, "RND"));
				}
				
				// eye color
				y += 16;
				// left
				lable = GuiCreationParts.this.getLabel(23);
				button = GuiCreationParts.this.getButton(23);
				lable.setLabel("eye.color.0");
				lable.y = y + 3;
				button.y = y;
				button.x = x1;
				button.height = 14;
				((GuiColorButton) button).color = this.eyes.eyeColor[1];
				// right
				button = new GuiColorButton(24, x1 + 52, y, this.eyes.eyeColor[0]);
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				if (this.data.type == 2) {
					GuiCreationParts.this.addButton(new GuiNpcButton(34, x1 + 104, y, 14, 14, "EL"));
					GuiCreationParts.this.addButton(new GuiNpcButton(35, x1 + 120, y, 14, 14, "ER"));
				}
				
				// pupil color
				y += 16;
				// left
				GuiCreationParts.this.addLabel(new GuiNpcLabel(25, "eye.color.1", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(25, x1, y, this.eyes.pupilColor[0]);
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				// right
				button = new GuiColorButton(26, x1 + 52, y, this.eyes.pupilColor[1]);
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				if (this.data.type == 2) {
					GuiCreationParts.this.addButton(new GuiNpcButton(36, x1 + 104, y, 14, 14, "PL"));
					GuiCreationParts.this.addButton(new GuiNpcButton(37, x1 + 120, y, 14, 14, "PR"));
				}
				
				// brow color
				y += 16;
				// left
				GuiCreationParts.this.addLabel(new GuiNpcLabel(27, "eye.color.2", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(27, x1 + 52, y, this.eyes.browColor[1]);
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				// right
				button = new GuiColorButton(28, x1, y, this.eyes.browColor[0]);
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				if (this.data.type == 2) {
					GuiCreationParts.this.addButton(new GuiNpcButton(38, x1 + 104, y, 14, 14, "BL"));
					GuiCreationParts.this.addButton(new GuiNpcButton(39, x1 + 120, y, 14, 14, "BR"));
				}
				
				// brow size
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(29, "eye.brow", x0, y + 3, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiNpcButton(29, x1, y, 100, 14, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }, this.eyes.browThickness));

				// skin color
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(30, "eye.lid", x0, y + 3, 0xFFFFFF));
				button = new GuiColorButton(30, x1, y, this.eyes.skinColor);
				button.width = 100;
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				
				// both eyes
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(22, "gui.draw", x0, y + 3, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiNpcButton(22, x1, y, 100, 14, new String[] { "gui.both", "gui.left", "gui.right" }, this.data.pattern));

				// closed
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(31, "eye.closed", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButton(31, x1, y, 100, 14, new String[] { "gui.none", "gui.both", "gui.left", "gui.right" }, this.eyes.closed);
				GuiCreationParts.this.addButton(button);
				button = new GuiNpcButtonYesNo(41, x1 + 104, y, this.eyes.activeRight);
				button.width = 20;
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				button = new GuiNpcButtonYesNo(42, x1 + 126, y, this.eyes.activeLeft);
				button.width = 20;
				button.height = 14;
				GuiCreationParts.this.addButton(button);
				
				// vertical pos
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(32, "gui.position", x0, y + 3, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiNpcButton(32, x1, y, 100, 14, new String[] { new TextComponentTranslation("gui.down").getFormattedText() + " x2", "gui.down", "gui.normal", "gui.up", new TextComponentTranslation("gui.up").getFormattedText() + " x2" }, this.eyes.eyePos + 1));

				// glint
				y += 16;
				GuiCreationParts.this.addLabel(new GuiNpcLabel(33, "eye.glint", x0, y + 3, 0xFFFFFF));
				button = new GuiNpcButtonYesNo(33, x1, y, this.eyes.glint);
				button.width = 100;
				button.height = 14;
				GuiCreationParts.this.addButton(button);
			}
			return y;
		}
		
		@Override
		public void subGuiClosed(SubGuiInterface subgui) {
			if (subgui instanceof GuiTextureSelection) {
				GuiTextureSelection tGui = (GuiTextureSelection) subgui;
				if (subgui.id == 0) { this.eyes.eyeRight = tGui.resource; }
				else if (subgui.id == 1) { this.eyes.eyeLeft = tGui.resource; }
				else if (subgui.id == 2) { this.eyes.pupilRight = tGui.resource; }
				else if (subgui.id == 3) { this.eyes.pupilLeft = tGui.resource; }
				else if (subgui.id == 4) { this.eyes.browRight = tGui.resource; }
				else if (subgui.id == 5) { this.eyes.browLeft = tGui.resource; }
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
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(22, GuiCreationParts.this.guiLeft + 145, y,
						100, 20, new String[] { "1", "2" }, this.data.pattern));
			}
			return y;
		}
	}

	class GuiPartLegs extends GuiPart {
		public GuiPartLegs() {
			super(EnumParts.LEGS);
			this.types = new String[] { "gui.none", "gui.normal", "legs.naga", "legs.spider", "legs.horse",
					"legs.mermaid", "legs.digitigrade" };
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
			this.data = GuiCreationParts.this.playerdata.getPartData(this.part);
			this.hasPlayerOption = (this.data != null
					&& (this.data.type == 0 || this.data.type == 1 || this.data.type == 6 || this.data.type == 7));
			int y = super.initGui();
			if (this.data != null && this.data.type == 0) {
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 0xFFFFFF));
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(22, GuiCreationParts.this.guiLeft + 145, y,
						100, 20, new String[] { "1", "2" }, this.data.pattern));
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
	
	public GuiPart getPart() { return this.parts[GuiCreationParts.selected]; }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.parts").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.size").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		} else if (this.getButton(20) != null && this.getButton(20).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.type").getFormattedText());
		} else if (this.getButton(21) != null && this.getButton(21).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.skin").getFormattedText());
		} else if (this.getButton(22) != null && this.getButton(22).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.pattern").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getPart() instanceof GuiPartEyes) {
			if (this.getButton(23) != null && this.getButton(23).visible && isMouseHover(mouseX, mouseY, this.getButton(23).x, this.getButton(23).y, this.getButton(23).width, this.getButton(23).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.color.r").getFormattedText());
			} else if (this.getButton(24) != null && this.getButton(24).visible && isMouseHover(mouseX, mouseY, this.getButton(24).x, this.getButton(24).y, this.getButton(24).width, this.getButton(24).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.color.l").getFormattedText());
			} else if (this.getButton(25) != null && this.getButton(25).visible && isMouseHover(mouseX, mouseY, this.getButton(25).x, this.getButton(25).y, this.getButton(25).width, this.getButton(25).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.color.r").getFormattedText());
			} else if (this.getButton(26) != null && this.getButton(26).visible && isMouseHover(mouseX, mouseY, this.getButton(26).x, this.getButton(26).y, this.getButton(26).width, this.getButton(26).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.color.l").getFormattedText());
			} else if (this.getButton(27) != null && this.getButton(27).visible && isMouseHover(mouseX, mouseY, this.getButton(27).x, this.getButton(27).y, this.getButton(27).width, this.getButton(27).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.color.r").getFormattedText());
			} else if (this.getButton(28) != null && this.getButton(28).visible && isMouseHover(mouseX, mouseY, this.getButton(28).x, this.getButton(28).y, this.getButton(28).width, this.getButton(28).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.color.l").getFormattedText());
			} else if (this.getButton(29) != null && this.getButton(29).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.size").getFormattedText());
			} else if (this.getButton(30) != null && this.getButton(30).visible && isMouseHover(mouseX, mouseY, this.getButton(30).x, this.getButton(30).y, this.getButton(30).width, this.getButton(30).height)) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.skin.color").getFormattedText());
			} else if (this.getButton(31) != null && this.getButton(31).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.closed").getFormattedText());
			} else if (this.getButton(32) != null && this.getButton(32).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pos").getFormattedText());
			} else if (this.getButton(33) != null && this.getButton(33).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.glint").getFormattedText());
			} else if (this.getButton(34) != null && this.getButton(34).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.txr.r").getFormattedText());
			} else if (this.getButton(35) != null && this.getButton(35).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.txr.l").getFormattedText());
			} else if (this.getButton(36) != null && this.getButton(36).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.txr.r").getFormattedText());
			} else if (this.getButton(37) != null && this.getButton(37).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.pupil.txr.l").getFormattedText());
			} else if (this.getButton(38) != null && this.getButton(38).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.txr.r").getFormattedText());
			} else if (this.getButton(39) != null && this.getButton(39).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.brow.txr.l").getFormattedText());
			} else if (this.getButton(40) != null && this.getButton(40).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.rnd").getFormattedText());
			} else if ((this.getButton(41) != null && this.getButton(41).isMouseOver()) || (this.getButton(42) != null && this.getButton(42).isMouseOver())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.eye.active").getFormattedText());
			}
		} else {
			if (this.getButton(23) != null && this.getButton(23).visible && isMouseHover(mouseX, mouseY, this.getButton(23).x, this.getButton(23).y, this.getButton(23).width, this.getButton(23).height)) {
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
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
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
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 46;
		this.scroll.setSize(100, this.ySize - 74);
		this.addScroll(this.scroll);
		if (this.getPart() != null) {
			this.scroll.setSelected(new TextComponentTranslation("part." + this.getPart().part.name).getFormattedText());
			this.getPart().initGui();
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.selected >= 0) {
			GuiCreationParts.selected = scroll.selected;
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (this.getPart() != null) {
			this.getPart().subGuiClosed(subgui);
		}
		this.initGui();
	}
	
}
