package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataAI;

public class SubGuiNpcMovement extends SubGuiInterface implements ITextfieldListener {
	private DataAI ai;

	public SubGuiNpcMovement(DataAI ai) {
		this.ai = ai;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			this.ai.setMovingType(button.getValue());
			if (this.ai.getMovingType() != 0) {
				this.ai.animationType = 0;
				this.ai.setStandingType(0);
				DataAI ai = this.ai;
				DataAI ai2 = this.ai;
				DataAI ai3 = this.ai;
				float bodyOffsetX = 5.0f;
				ai3.bodyOffsetZ = bodyOffsetX;
				ai2.bodyOffsetY = bodyOffsetX;
				ai.bodyOffsetX = bodyOffsetX;
			}
			this.initGui();
			break;
		}
		case 2: {
			this.ai.movingPause = (button.getValue() == 1);
			break;
		}
		case 4: {
			this.ai.setAnimation(button.getValue());
			this.initGui();
			break;
		}
		case 5: {
			this.ai.npcInteracting = (button.getValue() == 1);
			break;
		}
		case 7: {
			this.ai.setStandingType(button.getValue());
			this.initGui();
			break;
		}
		case 8: {
			this.ai.movingPattern = button.getValue();
			break;
		}
		case 13: {
			this.ai.stopAndInteract = (button.getValue() == 1);
			break;
		}
		case 15: {
			this.ai.movementType = button.getValue();
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking.range").getFormattedText());
		} else if (this.getTextField(5) != null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.offset.r").getFormattedText());
		} else if (this.getTextField(7) != null && this.getTextField(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.offset.x").getFormattedText());
		} else if (this.getTextField(8) != null && this.getTextField(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.offset.y").getFormattedText());
		} else if (this.getTextField(9) != null && this.getTextField(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.offset.z").getFormattedText());
		} else if (this.getTextField(14) != null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking.speed").getFormattedText());
		} else if (this.getTextField(15) != null && this.getTextField(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.step.height").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking.type").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking.stop").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking.anim").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.interact").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.path.closed").getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.stop.interact").getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.walking").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 4;
		this.addLabel(new GuiNpcLabel(0, "movement.type", this.guiLeft + 4, y + 5));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 80, y, 100, 20,
				new String[] { "ai.standing", "ai.wandering", "ai.movingpath" }, this.ai.getMovingType()));
		y += 22;
		this.addButton(new GuiNpcButton(15, this.guiLeft + 80, y, 100, 20,
				new String[] { "movement.ground", "movement.flying", "movement.swimming" }, this.ai.movementType));
		this.addLabel(new GuiNpcLabel(15, "movement.navigation", this.guiLeft + 4, y + 5));
		if (this.ai.getMovingType() == 1) {
			y += 22;
			this.addTextField(new GuiNpcTextField(4, this, this.guiLeft + 100, y, 40, 20, this.ai.walkingRange + ""));
			this.getTextField(4).setNumbersOnly();
			this.getTextField(4).setMinMaxDefault(0, 1000, 10);
			this.addLabel(new GuiNpcLabel(4, "gui.range", this.guiLeft + 4, y + 5));
			y += 22;
			this.addButton(new GuiNpcButton(5, this.guiLeft + 100, y, 50, 20, new String[] { "gui.no", "gui.yes" },
					(this.ai.npcInteracting ? 1 : 0)));
			this.addLabel(new GuiNpcLabel(5, "movement.wanderinteract", this.guiLeft + 4, y + 5));
			y += 22;
			this.addButton(new GuiNpcButton(2, this.guiLeft + 80, y, 80, 20, new String[] { "gui.no", "gui.yes" },
					(this.ai.movingPause ? 1 : 0)));
			this.addLabel(new GuiNpcLabel(9, "movement.pauses", this.guiLeft + 4, y + 5));
		} else if (this.ai.getMovingType() == 0) {
			y += 22;
			this.addTextField(new GuiNpcTextField(7, this, this.guiLeft + 99, y, 24, 20, this.ai.bodyOffsetX + ""));
			this.addLabel(new GuiNpcLabel(17, "spawner.posoffset", this.guiLeft + 4, y + 5));
			this.addLabel(new GuiNpcLabel(7, "X:", this.guiLeft + 115, y + 5));
			this.getTextField(7).setDoubleNumbersOnly();
			this.getTextField(7).setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			this.addLabel(new GuiNpcLabel(8, "Y:", this.guiLeft + 125, y + 5));
			this.addTextField(new GuiNpcTextField(8, this, this.guiLeft + 135, y, 24, 20, this.ai.bodyOffsetY + ""));
			this.getTextField(8).setDoubleNumbersOnly();
			this.getTextField(8).setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			this.addLabel(new GuiNpcLabel(9, "Z:", this.guiLeft + 161, y + 5));
			this.addTextField(new GuiNpcTextField(9, this, this.guiLeft + 171, y, 24, 20, this.ai.bodyOffsetZ + ""));
			this.getTextField(9).setDoubleNumbersOnly();
			this.getTextField(9).setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			y += 22;
			this.addButton(new GuiNpcButton(4, this.guiLeft + 80, y, 100, 20,
					new String[] { "stats.normal", "movement.sitting", "movement.lying", "movement.hug",
							"movement.sneaking", "movement.dancing", "movement.aiming", "movement.crawling" },
					this.ai.animationType));
			this.addLabel(new GuiNpcLabel(3, "movement.animation", this.guiLeft + 4, y + 5));
			if (this.ai.animationType != 2) {
				y += 22;
				this.addButton(new GuiNpcButton(7, this.guiLeft + 80, y, 80, 20,
						new String[] { "movement.body", "movement.manual", "movement.stalking", "movement.head" },
						this.ai.getStandingType()));
				this.addLabel(new GuiNpcLabel(1, "movement.rotation", this.guiLeft + 4, y + 5));
			} else {
				y += 22;
				this.addTextField(new GuiNpcTextField(5, this, this.guiLeft + 99, y, 40, 20, this.ai.orientation + ""));
				this.getTextField(5).setNumbersOnly();
				this.getTextField(5).setMinMaxDefault(0, 359, 0);
				this.addLabel(new GuiNpcLabel(6, "movement.rotation", this.guiLeft + 4, y + 5));
				this.addLabel(new GuiNpcLabel(5, "(0-359)", this.guiLeft + 142, y + 5));
			}
			if (this.ai.getStandingType() == 1 || this.ai.getStandingType() == 3) {
				this.addTextField(
						new GuiNpcTextField(5, this, this.guiLeft + 165, y, 40, 20, this.ai.orientation + ""));
				this.getTextField(5).setNumbersOnly();
				this.getTextField(5).setMinMaxDefault(0, 359, 0);
				this.addLabel(new GuiNpcLabel(5, "(0-359)", this.guiLeft + 207, y + 5));
			}
		}
		if (this.ai.getMovingType() != 0) {
			y += 22;
			this.addButton(new GuiNpcButton(4, this.guiLeft + 80, y, 100, 20,
					new String[] { "stats.normal", "movement.sitting", "movement.lying", "movement.hug",
							"movement.sneaking", "movement.dancing", "movement.aiming", "movement.crawling" },
					this.ai.animationType));
			this.addLabel(new GuiNpcLabel(12, "movement.animation", this.guiLeft + 4, y + 5));
		}
		if (this.ai.getMovingType() == 2) {
			y += 22;
			this.addButton(new GuiNpcButton(8, this.guiLeft + 80, y, 80, 20,
					new String[] { "ai.looping", "ai.backtracking" }, this.ai.movingPattern));
			this.addLabel(new GuiNpcLabel(8, "movement.name", this.guiLeft + 4, y + 5));
			y += 22;
			this.addButton(new GuiNpcButton(2, this.guiLeft + 80, y, 80, 20, new String[] { "gui.no", "gui.yes" },
					(this.ai.movingPause ? 1 : 0)));
			this.addLabel(new GuiNpcLabel(9, "movement.pauses", this.guiLeft + 4, y + 5));
		}
		y += 22;
		this.addButton(new GuiNpcButton(13, this.guiLeft + 100, y, 50, 20, new String[] { "gui.no", "gui.yes" },
				(this.ai.stopAndInteract ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(13, "movement.stopinteract", this.guiLeft + 4, y + 5));
		y += 22;
		this.addTextField(new GuiNpcTextField(14, this, this.guiLeft + 80, y, 50, 18, this.ai.getWalkingSpeed() + ""));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 10, 4);
		this.addLabel(new GuiNpcLabel(14, "stats.movespeed", this.guiLeft + 5, y + 5));
		y += 22;
		this.addTextField(new GuiNpcTextField(15, this, this.guiLeft + 80, y, 50, 18, this.ai.stepheight + ""));
		this.getTextField(15).setDoubleNumbersOnly();
		this.getTextField(15).setMinMaxDoubleDefault(0.1d, 3.0d, this.ai.stepheight);
		this.addLabel(new GuiNpcLabel(15, "stats.stepheight", this.guiLeft + 5, y + 5));

		this.addButton(new GuiNpcButton(66, this.guiLeft + 190, this.guiTop + 190, 60, 20, "gui.done"));

	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 7) {
			this.ai.bodyOffsetX = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d);
			this.initGui();
		} else if (textfield.getId() == 8) {
			this.ai.bodyOffsetY = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d);
			this.initGui();
		} else if (textfield.getId() == 9) {
			this.ai.bodyOffsetZ = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d);
			this.initGui();
		} else if (textfield.getId() == 5) {
			this.ai.orientation = textfield.getInteger();
			this.npc.rotationYaw = textfield.getInteger();
		} else if (textfield.getId() == 4) {
			this.ai.walkingRange = textfield.getInteger();
		} else if (textfield.getId() == 14) {
			this.ai.setWalkingSpeed(textfield.getInteger());
		} else if (textfield.getId() == 15) {
			this.ai.stepheight = (float) textfield.getDouble();
		}
	}

}
