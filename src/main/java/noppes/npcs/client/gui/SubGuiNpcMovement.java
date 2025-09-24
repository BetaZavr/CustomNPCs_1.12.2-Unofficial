package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataAI;

import javax.annotation.Nonnull;

public class SubGuiNpcMovement extends SubGuiInterface implements ITextfieldListener {

	protected static final String[] walkingTypes = new String[] { "ai.standing", "ai.wandering", "ai.movingpath" };
	protected static final String[] navigationTypes = new String[] { "movement.ground", "movement.flying", "movement.swimming" };
	protected static final String[] animationTypes = new String[] { "stats.normal", "movement.sitting", "movement.lying", "movement.hug", "movement.sneaking", "movement.dancing", "movement.aiming", "movement.crawling" };
	protected static final String[] rotationTypes = new String[] { "movement.body", "movement.manual", "movement.stalking", "movement.head", "movement.stalking.2" };

	private final DataAI ais;

	public SubGuiNpcMovement(DataAI aisIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		ais = aisIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				ais.setMovingType(button.getValue());
				if (ais.getMovingType() != 0) {
					ais.animationType = 0;
					ais.setStandingType(0);
                    float f = 5.0f;
					ais.bodyOffsetZ = f;
					ais.bodyOffsetY = f;
					ais.bodyOffsetX = f;
				}
				initGui();
				break;
			}
			case 2: ais.movingPause = (button.getValue() == 1); break;
			case 4: ais.setAnimation(button.getValue()); initGui(); break;
			case 5: ais.npcInteracting = (button.getValue() == 1); break;
			case 7: ais.setStandingType(button.getValue()); initGui(); break;
			case 8: ais.movingPattern = button.getValue(); break;
			case 13: ais.stopAndInteract = (button.getValue() == 1); break;
			case 15: ais.movementType = button.getValue(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		int lId = 0;
		addLabel(new GuiNpcLabel(lId++, "movement.type", guiLeft + 4, y + 5));
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 80, y, 100, 20, walkingTypes, ais.getMovingType());
		button.setHoverText("ai.hover.walking.type");
		addButton(button);
		y += 22;
		button = new GuiNpcButton(15, guiLeft + 80, y, 100, 20, navigationTypes, ais.movementType);
		button.setHoverText("ai.hover.walking");
		addButton(button);
		addLabel(new GuiNpcLabel(lId++, "movement.navigation", guiLeft + 4, y + 5));
		GuiNpcTextField textField;
		if (ais.getMovingType() == 1) {
			y += 22;
			textField = new GuiNpcTextField(4, this, guiLeft + 100, y, 40, 20, ais.walkingRange + "");
			textField.setMinMaxDefault(0, 1000, 10);
			textField.setHoverText("ai.hover.walking.range");
			addTextField(textField);
			addLabel(new GuiNpcLabel(lId++, "gui.range", guiLeft + 4, y + 5));
			y += 22;
			button = new GuiNpcButton(5, guiLeft + 100, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (ais.npcInteracting ? 1 : 0));
			button.setHoverText("ai.hover.offset.r");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.wanderinteract", guiLeft + 4, y + 5));
			y += 22;
			button = new GuiNpcButton(2, guiLeft + 80, y, 80, 20, new String[] { "gui.no", "gui.yes" }, (ais.movingPause ? 1 : 0));
			button.setHoverText("ai.hover.offset.r");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.pauses", guiLeft + 4, y + 5));
		}
		else if (ais.getMovingType() == 0) {
			y += 22;
			addLabel(new GuiNpcLabel(lId++, "spawner.posoffset", guiLeft + 4, y + 5));
			// x
			addLabel(new GuiNpcLabel(lId++, "X:", guiLeft + 115, y + 5));
			textField = new GuiNpcTextField(7, this, guiLeft + 99, y, 24, 20, ais.bodyOffsetX + "");
			textField.setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			textField.setHoverText("ai.hover.offset.x");
			addTextField(textField);
			// y
			addLabel(new GuiNpcLabel(lId++, "Y:", guiLeft + 125, y + 5));
			textField = new GuiNpcTextField(8, this, guiLeft + 135, y, 24, 20, ais.bodyOffsetY + "");
			textField.setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			textField.setHoverText("ai.hover.offset.y");
			addTextField(textField);
			// z
			addLabel(new GuiNpcLabel(lId++, "Z:", guiLeft + 161, y + 5));
			textField = new GuiNpcTextField(9, this, guiLeft + 171, y, 24, 20, ais.bodyOffsetZ + "");
			textField.setMinMaxDoubleDefault(0.0d, 10.0d, 5.0d);
			textField.setHoverText("ai.hover.offset.z");
			addTextField(textField);
			y += 22;
			button = new GuiNpcButton(4, guiLeft + 80, y, 100, 20, animationTypes, ais.animationType);
			button.setHoverText("ai.hover.walking.anim");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.animation", guiLeft + 4, y + 5));
			if (ais.animationType != 2) {
				y += 22;
				button = new GuiNpcButton(7, guiLeft + 80, y, 80, 20, rotationTypes, ais.getStandingType());
				button.setHoverText("ai.hover.rotation");
				addButton(button);
				addLabel(new GuiNpcLabel(lId++, "movement.rotation", guiLeft + 4, y + 5));
			} else {
				y += 22;
				textField = new GuiNpcTextField(5, this, guiLeft + 99, y, 40, 20, ais.orientation + "");
				textField.setMinMaxDefault(0, 359, 0);
				textField.setHoverText("ai.hover.interact");
				addTextField(textField);
				addLabel(new GuiNpcLabel(lId++, "movement.rotation", guiLeft + 4, y + 5));
				addLabel(new GuiNpcLabel(lId++, "(0-359)", guiLeft + 142, y + 5));
			}
			if (ais.getStandingType() == 1 || ais.getStandingType() == 3 || ais.getStandingType() == 4) {
				textField = new GuiNpcTextField(5, this, guiLeft + 165, y, 40, 20, ais.orientation + "");
				textField.setMinMaxDefault(0, 359, 0);
				textField.setHoverText("ai.hover.interact");
				addTextField(textField);
				addLabel(new GuiNpcLabel(lId++, "(0-359)", guiLeft + 207, y + 5));
			}
		}
		if (ais.getMovingType() != 0) {
			y += 22;
			button = new GuiNpcButton(4, guiLeft + 80, y, 100, 20, animationTypes, ais.animationType);
			button.setHoverText("ai.hover.walking.anim");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.animation", guiLeft + 4, y + 5));
		}
		if (ais.getMovingType() == 2) {
			y += 22;
			button = new GuiNpcButton(8, guiLeft + 80, y, 80, 20, new String[] { "ai.looping", "ai.backtracking" }, ais.movingPattern);
			button.setHoverText("ai.hover.path.closed");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.name", guiLeft + 4, y + 5));
			y += 22;
			button = new GuiNpcButton(2, guiLeft + 80, y, 80, 20, new String[] { "gui.no", "gui.yes" }, (ais.movingPause ? 1 : 0));
			button.setHoverText("ai.hover.walking.stop");
			addButton(button);
			addLabel(new GuiNpcLabel(lId++, "movement.pauses", guiLeft + 4, y + 5));
		}
		y += 22;
		button = new GuiNpcButton(13, guiLeft + 100, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (ais.stopAndInteract ? 1 : 0));
		button.setHoverText("ai.hover.stop.interact");
		addButton(button);
		addLabel(new GuiNpcLabel(lId++, "movement.stopinteract", guiLeft + 4, y + 5));
		y += 22;
		textField = new GuiNpcTextField(14, this, guiLeft + 80, y, 50, 18, ais.getWalkingSpeed() + "");
		textField.setMinMaxDefault(0, 10, 4);
		textField.setHoverText("ai.hover.walking.speed");
		addTextField(textField);
		addLabel(new GuiNpcLabel(lId++, "stats.movespeed", guiLeft + 5, y + 5));
		y += 22;
		textField = new GuiNpcTextField(15, this, guiLeft + 80, y, 50, 18, ais.stepheight + "");
		textField.setMinMaxDoubleDefault(0.1d, 3.0d, ais.stepheight);
		textField.setHoverText("ai.hover.step.height");
		addTextField(textField);
		addLabel(new GuiNpcLabel(lId, "stats.stepheight", guiLeft + 5, y + 5));
		button = new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 4: ais.walkingRange = textfield.getInteger(); break;
			case 5: ais.orientation = textfield.getInteger(); npc.rotationYaw = textfield.getInteger(); break;
			case 7: ais.bodyOffsetX = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d); initGui(); break;
			case 8: ais.bodyOffsetY = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d); initGui(); break;
			case 9: ais.bodyOffsetZ = (float) (Math.round(textfield.getDouble() * 100.0d) / 100.0d); initGui(); break;
			case 14: ais.setWalkingSpeed(textfield.getInteger()); break;
			case 15: ais.stepheight = (float) textfield.getDouble(); break;
		}
	}

}
