package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiColorButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiCreationParts extends GuiCreationScreenInterface implements ITextfieldListener, ICustomScrollListener {

	class GuiPart {
		protected boolean canBeDeleted;
		protected ModelPartData data;
		protected boolean hasPlayerOption;
		protected boolean noPlayerTypes;
		EnumParts part;
		public int paterns;
		protected String[] types;

		public GuiPart(EnumParts part) {
			this.paterns = 0;
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
				GuiCreationParts.this.setSubGui(
						new GuiModelColor(GuiCreationParts.this, this.data.color, color -> this.data.color = color));
			}
		}

		public int initGui() {
			this.data = GuiCreationParts.this.playerdata.getPartData(this.part);
			int y = GuiCreationParts.this.guiTop + 50;
			if (this.data == null || !this.data.playerTexture || !this.noPlayerTypes) {
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(20, "gui.type", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(20, GuiCreationParts.this.guiLeft + 145, y,
						100, 20, this.types, (this.data == null) ? 0 : (this.data.type + 1)));
				y += 25;
			}
			if (this.data != null && this.hasPlayerOption) {
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(21, "gui.playerskin", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				GuiCreationParts.this.addButton(
						new GuiNpcButtonYesNo(21, GuiCreationParts.this.guiLeft + 170, y, this.data.playerTexture));
				y += 25;
			}
			if (this.data != null && !this.data.playerTexture) {
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(23, "gui.color", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				GuiCreationParts.this
						.addButton(new GuiColorButton(23, GuiCreationParts.this.guiLeft + 170, y, this.data.color));
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
	}

	class GuiPartBeard extends GuiPart {
		public GuiPartBeard() {
			super(EnumParts.BEARD);
			this.types = new String[] { "gui.none", "1", "2", "3", "4" };
			this.noPlayerTypes();
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
			GuiCreationParts.this
					.addLabel(new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
			GuiCreationParts.this.addButton(new GuiButtonBiDirectional(22, GuiCreationParts.this.guiLeft + 145, y, 100,
					20, new String[] { "gui.both", "gui.left", "gui.right" }, this.data.pattern));
			return y;
		}
	}

	class GuiPartEyes extends GuiPart {
		private ModelEyeData eyes;

		public GuiPartEyes() {
			super(EnumParts.EYES);
			this.types = new String[] { "gui.none", "1", "2" };
			this.noPlayerOptions();
			this.canBeDeleted = false;
			this.eyes = (ModelEyeData) this.data;
		}

		@Override
		protected void actionPerformed(GuiButton btn) {
			switch (btn.id) {
			case 34:
				this.eyes.glint = ((GuiNpcButtonYesNo) btn).getBoolean();
				break;
			case 35:
				GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.browColor,
						color -> this.eyes.browColor = color));
				break;
			case 36:
				GuiCreationParts.this.setSubGui(new GuiModelColor(GuiCreationParts.this, this.eyes.skinColor,
						color -> this.eyes.skinColor = color));
				break;
			case 37:
				this.eyes.eyePos = ((GuiButtonBiDirectional) btn).getValue() - 1;
				break;
			case 38:
				this.eyes.browThickness = ((GuiButtonBiDirectional) btn).getValue();
				break;
			case 39:
				this.eyes.closed = ((GuiNpcButtonYesNo) btn).getBoolean();
				break;
			}
			super.actionPerformed(btn);
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (this.data != null && this.eyes.isEnabled()) {
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(22, GuiCreationParts.this.guiLeft + 145, y,
						100, 20, new String[] { "gui.both", "gui.left", "gui.right" }, this.data.pattern));
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(22, "gui.draw", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				y += 25;
				GuiCreationParts.this
						.addButton(new GuiButtonBiDirectional(37, GuiCreationParts.this.guiLeft + 145, y, 100, 20,
								new String[] { new TextComponentTranslation("gui.down").getFormattedText() + " x2",
										"gui.down", "gui.normal", "gui.up",
										new TextComponentTranslation("gui.up").getFormattedText() + " x2" },
								this.eyes.eyePos + 1));
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(37, "gui.position", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				y += 25;
				GuiCreationParts.this
						.addButton(new GuiNpcButtonYesNo(34, GuiCreationParts.this.guiLeft + 145, y, this.eyes.glint));
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(34, "eye.glint", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				y += 25;
				GuiCreationParts.this
						.addButton(new GuiColorButton(35, GuiCreationParts.this.guiLeft + 170, y, this.eyes.browColor));
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(35, "eye.brow", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				GuiCreationParts.this.addButton(new GuiButtonBiDirectional(38, GuiCreationParts.this.guiLeft + 225, y,
						50, 20, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }, this.eyes.browThickness));
				y += 25;
				GuiCreationParts.this
						.addButton(new GuiColorButton(36, GuiCreationParts.this.guiLeft + 170, y, this.eyes.skinColor));
				GuiCreationParts.this
						.addLabel(new GuiNpcLabel(36, "eye.lid", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
				y = GuiCreationParts.this.guiTop + 50;
				GuiCreationParts.this
						.addButton(new GuiNpcButtonYesNo(39, GuiCreationParts.this.guiLeft + 300, y, this.eyes.closed));
				GuiCreationParts.this.addLabel(
						new GuiNpcLabel(39, "eye.closed", GuiCreationParts.this.guiLeft + 255, y + 5, 16777215));
			}
			return y;
		}
	}

	class GuiPartHair extends GuiPart {
		public GuiPartHair() {
			super(EnumParts.HAIR);
			this.types = new String[] { "gui.none", "1", "2", "3", "4" };
			this.noPlayerTypes();
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
						new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
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
			int y = super.initGui();
			if (this.data == null) {
				return y;
			}
			return y;
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
						new GuiNpcLabel(22, "gui.pattern", GuiCreationParts.this.guiLeft + 102, y + 5, 16777215));
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
		protected void actionPerformed(GuiButton btn) {
			if (btn.id == 34) {
			}
			super.actionPerformed(btn);
		}

		@Override
		public int initGui() {
			int y = super.initGui();
			if (this.data == null) {
				return y;
			}
			return y;
		}
	}

	private static int selected = 0;

	private GuiPart[] parts;

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
	protected void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if (this.parts[GuiCreationParts.selected] != null) {
			this.parts[GuiCreationParts.selected].actionPerformed(btn);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
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
		} else {
			if (this.getButton(23) != null && this.getButton(23).visible) {
				if (isMouseHover(mouseX, mouseY, this.getButton(23).x, this.getButton(23).y, this.getButton(23).width,
						this.getButton(23).height)) {
					this.setHoverText(new TextComponentTranslation("display.hover.part.color").getFormattedText());
					return;
				}
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
			List<String> list = new ArrayList<String>();
			for (GuiPart part : this.parts) {
				list.add(new TextComponentTranslation("part." + part.part.name).getFormattedText());
			}
			(this.scroll = new GuiCustomScroll(this, 0)).setListNotSorted(list);
		}
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 46;
		this.scroll.setSize(100, this.ySize - 74);
		this.addScroll(this.scroll);
		if (this.parts[GuiCreationParts.selected] != null) {
			this.scroll
					.setSelected(new TextComponentTranslation("part." + this.parts[GuiCreationParts.selected].part.name)
							.getFormattedText());
			this.parts[GuiCreationParts.selected].initGui();
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
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 23) {
		}
	}

}
