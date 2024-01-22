package noppes.npcs.client.gui.mainmenu;

import java.util.Arrays;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMeleeProperties;
import noppes.npcs.client.gui.SubGuiNpcProjectiles;
import noppes.npcs.client.gui.SubGuiNpcRangeProperties;
import noppes.npcs.client.gui.SubGuiNpcResistanceProperties;
import noppes.npcs.client.gui.SubGuiNpcRespawn;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataStats;

public class GuiNpcStats
extends GuiNPCInterface2
implements ITextfieldListener, IGuiData {
	
	private DataAI ais;
	private DataDisplay display;
	private DataInventory inventory;
	private DataStats stats;

	public GuiNpcStats(EntityNPCInterface npc) {
		super(npc, 2);
		this.stats = npc.stats;
		this.display = npc.display;
		this.ais = npc.ais;
		this.inventory = npc.inventory;
		Client.sendData(EnumPacketServer.MainmenuStatsGet);
		// New
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
		Client.sendData(EnumPacketServer.MainmenuAIGet);
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: {
				this.setSubGui(new SubGuiNpcRespawn(this.stats));
				break;
			}
			case 2: {
				this.setSubGui(new SubGuiNpcMeleeProperties(this.stats.melee));
				break;
			}
			case 3: {
				this.setSubGui(new SubGuiNpcRangeProperties(this.stats));
				break;
			}
			case 4: {
				this.stats.immuneToFire = (button.getValue() == 1);
				break;
			}
			case 5: {
				this.stats.canDrown = (button.getValue() == 1);
				break;
			}
			case 6: {
				this.stats.burnInSun = (button.getValue() == 1);
				break;
			}
			case 7: {
				this.stats.noFallDamage = (button.getValue() == 1);
				break;
			}
			case 8: {
				this.stats.creatureType = EnumCreatureAttribute.values()[button.getValue()];
				break;
			}
			case 9: {
				this.setSubGui(new SubGuiNpcProjectiles(this.stats.ranged));
				break;
			}
			case 15: {
				this.setSubGui(new SubGuiNpcResistanceProperties(this.stats.resistances));
				break;
			}
			case 17: {
				this.stats.potionImmune = ((GuiNpcButtonYesNo) button).getBoolean();
				break;
			}
			case 22: {
				this.stats.ignoreCobweb = (button.getValue() == 0);
				break;
			}
			case 40: {
				this.save();
				break;
			} // New
			case 41: {
				this.stats.setLevel(1 + button.getValue());
				this.setBaseStats();
				break;
			}
			case 43: {
				this.stats.setRarity(button.getValue());
				this.setBaseStats();
				break;
			}
			case 44: {
				this.stats.calmdown = (button.getValue() == 1);
				break;
			}
		}
	}

	// New
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.max.health").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.aggro").getFormattedText());
		} else if (this.getTextField(14)!=null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.health.regen").getFormattedText());
		} else if (this.getTextField(16)!=null && this.getTextField(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.health.combat").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.respawn").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.melee").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.range").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.fire").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.water").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.sun").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.fall").getFormattedText());
		} else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.type").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.arrow").getFormattedText());
		} else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resists").getFormattedText());
		} else if (this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.potion").getFormattedText());
		} else if (this.getButton(22)!=null && this.getButton(22).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.web").getFormattedText());
		} else if (this.getButton(41)!=null && this.getButton(41).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.level").getFormattedText());
		} else if (this.getButton(43)!=null && this.getButton(43).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.rarity").getFormattedText());
		} else if (this.getButton(44)!=null && this.getButton(44).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.battle").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	public double getHP() {
		int[] corr = CustomNpcs.healthNormal;
		if (this.stats.getRarity() == 1) {
			corr = CustomNpcs.healthElite;
		} else if (this.stats.getRarity() == 2) {
			corr = CustomNpcs.healthBoss;
		}
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.maxLv, 2));
		double b = (double) corr[0] - a;
		double hp = Math.round(a * Math.pow(this.stats.getLevel(), 2) + b);
		if (hp <= 1.0d) {
			hp = 1.0d;
		}
		if (hp > 10000) {
			hp = Math.ceil(hp / 100.0d) * 100.0d;
		} else if (hp > 1000) {
			hp = Math.ceil(hp / 25.0d) * 25.0d;
		} else if (hp > 100) {
			hp = Math.ceil(hp / 10.0d) * 10.0d;
		} else if (hp > 50) {
			hp = Math.ceil(hp / 5.0d) * 5.0d;
		} else {
			hp = Math.ceil(hp);
		}
		if (hp > (double) corr[1]) {
			hp = (double) corr[1];
		}
		return hp;
	}

	public int getMellePower() {
		int[] corr = CustomNpcs.damageNormal;
		if (this.stats.getRarity() == 1) {
			corr = CustomNpcs.damageElite;
		} else if (this.stats.getRarity() == 2) {
			corr = CustomNpcs.damageBoss;
		}
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.maxLv, 2));
		double b = (double) corr[0] - a;
		return (int) Math.round(a * Math.pow(this.stats.getLevel(), 2) + b);
	}

	public int getRangePower() {
		int[] corr = CustomNpcs.damageNormal;
		if (this.stats.getRarity() == 1) {
			corr = CustomNpcs.damageElite;
		} else if (this.stats.getRarity() == 2) {
			corr = CustomNpcs.damageBoss;
		}
		double a = ((double) corr[2] - (double) corr[3]) / (1 - Math.pow(CustomNpcs.maxLv, 2));
		double b = (double) corr[2] - a;
		return (int) Math.round(a * Math.pow(this.stats.getLevel(), 2) + b);
	}

	public int[] getXP() {
		float[] corr = new float[] { (float) CustomNpcs.experience[0], (float) CustomNpcs.experience[1],
				(float) CustomNpcs.experience[2], (float) CustomNpcs.experience[3] };
		int lv = this.stats.getLevel();
		if (this.stats.getRarity() == 1) {
			corr[0] *= 1.75f;
			corr[1] *= 1.75f;
			corr[2] *= 1.75f;
			corr[3] *= 1.75f;
		} else if (this.stats.getRarity() == 2) {
			corr[0] *= 4.75f;
			corr[1] *= 4.75f;
			corr[2] *= 4.75f;
			corr[3] *= 4.75f;
		}
		int subMinLv = CustomNpcs.maxLv / 3;
		int subMaxLv = CustomNpcs.maxLv * 2 / 3;
		float subMinXP = corr[1] / 3.0f;
		float subMaxXP = corr[1] * 2.0f / 3.0f;
		float subMinXPM = corr[3] / 3.0f;
		float subMaxXPM = corr[3] * 2.0f / 3.0f;
		double a = ((subMaxXP - corr[1]) * (1 - subMinLv) - (corr[0] - subMinXP) * (subMaxLv - CustomNpcs.maxLv))
				/ ((subMaxLv - CustomNpcs.maxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.maxLv, 2) - Math.pow(subMaxLv, 2)));
		double b = (corr[0] - subMinXP + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		double c = corr[0] - a - b;
		int min = (int) (Math.pow(lv, 2) * a + lv * b + c);
		a = ((subMaxXPM - corr[3]) * (1 - subMinLv) - (corr[2] - subMinXPM) * (subMaxLv - CustomNpcs.maxLv))
				/ ((subMaxLv - CustomNpcs.maxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.maxLv, 2) - Math.pow(subMaxLv, 2)));
		b = (corr[2] - subMinXPM + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		c = corr[2] - a - b;
		int max = (int) (Math.pow(lv, 2) * a + lv * b + c);
		return new int[] { min, max };
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 10;
		this.addLabel(new GuiNpcLabel(0, "stats.health", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 85, y, 50, 18, "" + (int) this.stats.maxHealth));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, Long.MAX_VALUE, 20);
		this.addLabel(new GuiNpcLabel(1, "stats.aggro", this.guiLeft + 275, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 355, y, 50, 18, this.stats.aggroRange + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, 64, 2);

		this.addLabel(new GuiNpcLabel(34, "stats.creaturetype", this.guiLeft + 140, y + 5));
		this.addButton(new GuiButtonBiDirectional(8, this.guiLeft + 217, y, 56, 20, new String[] { "stats.normal", "stats.undead", "stats.arthropod" }, this.stats.creatureType.ordinal()));
		((GuiButtonBiDirectional) this.getButton(8)).cheakWidth = false;
		y += 22;
		this.addButton(new GuiNpcButton(0, this.guiLeft + 82, y, 56, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(2, "stats.respawn", this.guiLeft + 5, y + 5));
		y += 22;
		this.addButton(new GuiNpcButton(2, this.guiLeft + 82, y, 56, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(5, "stats.meleeproperties", this.guiLeft + 5, y + 5));
		y += 22;
		this.addButton(new GuiNpcButton(3, this.guiLeft + 82, y, 56, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(6, "stats.rangedproperties", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 217, y, 56, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(7, "stats.projectileproperties", this.guiLeft + 140, y + 5));
		y += 34;
		this.addButton(new GuiNpcButton(15, this.guiLeft + 82, y, 56, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(15, "effect.resistance", this.guiLeft + 5, y + 5));
		y += 34;
		this.addButton(new GuiNpcButton(4, this.guiLeft + 82, y, 56, 20, new String[] { "gui.no", "gui.yes" },
				(this.stats.immuneToFire ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(10, "stats.fireimmune", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" },
				(this.stats.canDrown ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(11, "stats.candrown", this.guiLeft + 140, y + 5));
		this.addTextField(new GuiNpcTextField(14, this, this.guiLeft + 355, y, 56, 20, this.stats.healthRegen + "").setNumbersOnly());
		this.addLabel(new GuiNpcLabel(14, "stats.regenhealth", this.guiLeft + 275, y + 5));
		y += 22;
		this.addTextField(new GuiNpcTextField(16, this, this.guiLeft + 355, y, 56, 20, this.stats.combatRegen + "").setNumbersOnly());
		this.addLabel(new GuiNpcLabel(16, "stats.combatregen", this.guiLeft + 275, y + 5));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 82, y, 56, 20, new String[] { "gui.no", "gui.yes" },
				(this.stats.burnInSun ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(12, "stats.burninsun", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" },
				(this.stats.noFallDamage ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(13, "stats.nofalldamage", this.guiLeft + 140, y + 5));
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(17, this.guiLeft + 82, y, 56, 20, this.stats.potionImmune));
		this.addLabel(new GuiNpcLabel(17, "stats.potionImmune", this.guiLeft + 5, y + 5));
		this.addLabel(new GuiNpcLabel(22, "ai.cobwebAffected", this.guiLeft + 140, y + 5));
		this.addButton(new GuiNpcButton(22, this.guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" },
				(this.stats.ignoreCobweb ? 0 : 1)));
		// new
		String[] lvls = new String[CustomNpcs.maxLv]; // level
		for (int g = 0; g < CustomNpcs.maxLv; g++) {
			lvls[g] = "" + (g + 1);
		}
		//this.addButton(new GuiNpcButton(40, this.guiLeft + 217, this.guiTop +32, 56, 20, "selectServer.edit"));
		this.addButton(new GuiButtonBiDirectional(41, this.guiLeft + 217, this.guiTop + 32, 56, 20, lvls,
				this.stats.getLevel() - 1));
		//this.getButton(40).setEnabled(false);
		this.addLabel(new GuiNpcLabel(42, "stats.level", this.guiLeft + 139, this.guiTop + 37));
		this.addButton(new GuiNpcButton(43, this.guiLeft + 217, this.guiTop + 54, 56, 20,
				new String[] { "stats.rarity.normal", "stats.rarity.elite", "stats.rarity.boss" },
				this.stats.getRarity())); // rarity
		this.addLabel(new GuiNpcLabel(44, "stats.rarity", this.guiLeft + 140, this.guiTop + 61));

		this.addLabel(new GuiNpcLabel(45, "stats.calmdown", this.guiLeft + 275, this.guiTop + 40));
		this.addButton(new GuiNpcButton(44, this.guiLeft + 355, this.guiTop + 37, 50, 20,
				new String[] { "gui.no", "gui.yes" }, (this.stats.calmdown ? 1 : 0)));
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuStatsSave, this.stats.writeToNBT(new NBTTagCompound()));
		// New
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, this.display.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.MainmenuAISave, this.ais.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.MainmenuInvSave, this.inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	private void setBaseStats() {
		int lv = this.stats.getLevel();
		int type = this.stats.getRarity();
		// Resistance and model size
		if (!CustomNpcs.recalculateLR) { return; }
		int[] sizeModel = CustomNpcs.modelRaritySize;
		int time = 180;
		float[] resist = new float[] { (float) CustomNpcs.resistanceNormal[0] / 100.0f,
				(float) CustomNpcs.resistanceNormal[1] / 100.0f, (float) CustomNpcs.resistanceNormal[2] / 100.0f,
				(float) CustomNpcs.resistanceNormal[3] / 100.0f };
		if (type == 2) {
			resist = new float[] { (float) CustomNpcs.resistanceBoss[0] / 100.0f,
					(float) CustomNpcs.resistanceBoss[1] / 100.0f, (float) CustomNpcs.resistanceBoss[2] / 100.0f,
					(float) CustomNpcs.resistanceBoss[3] / 100.0f };
		} else if (type == 1) {
			resist = new float[] { (float) CustomNpcs.resistanceElite[0] / 100.0f,
					(float) CustomNpcs.resistanceElite[1] / 100.0f, (float) CustomNpcs.resistanceElite[2] / 100.0f,
					(float) CustomNpcs.resistanceElite[3] / 100.0f };
			if (lv <= 30) {
				time = (int) (300.0d + (Math.round(((double) lv + 5.5d) / 10.0d) - 1.0d) * 60.0d);
			} else {
				time = 480;
			}
		} else {
			if (lv <= 30) {
				time = (int) (90.0d + (Math.round(((double) lv + 5.5d) / 10.0d) - 1.0d) * 30.0d);
			} else {
				time = 180;
			}
		}
		this.display.setSize(sizeModel[type]);
		this.stats.respawnTime = time;
		this.stats.resistances.melee = resist[0];
		this.stats.resistances.arrow = resist[1];
		this.stats.resistances.explosion = resist[2];
		this.stats.resistances.knockback = resist[3];
		// Health
		double hp = getHP();
		this.stats.maxHealth = (int) hp;
		this.npc.setHealth((float) hp);
		this.stats.setHealthRegen((int) (Math.ceil(hp / 50.0d) * 10.0d));
		// Power
		this.stats.melee.setStrength(getMellePower());
		this.stats.ranged.setStrength(getRangePower());
		this.stats.ranged.setAccuracy((int) Math.round((lv + 94.3333d) / 1.4667d));
		// Speed
		double sp = Math.round((0.000081d * Math.pow(lv, 2) - 0.07272d * lv + 22.072639d) * 1.0d);
		this.stats.melee.setDelay((int) sp);
		this.stats.ranged.setDelay((int) sp, (int) sp * 2);
		this.ais.setWalkingSpeed((int) Math.ceil((sp - 7) / 3));
		// Experience
		int[] xp = getXP();
		this.inventory.setExp(xp[0], xp[1]);
		// renaming
		String rarity = "";
		String chr = new String(Character.toChars(0x00A7));
		if (type == 2) {
			rarity += chr + "4Boss ";
		} else if (type == 1) {
			rarity += chr + "6Elite ";
		}
		rarity += chr + "7lv." + chr
			+ (lv <= CustomNpcs.maxLv / 3 ? "2" : (float) lv <= (float) CustomNpcs.maxLv / 1.5f ? "6" : "4") + lv;
		this.stats.setRarityTitle(rarity);
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("CreatureType", 3)) {
			this.stats.readToNBT(compound);
		} // Change
			// New
		else if (compound.hasKey("MarkovGeneratorId", 3)) {
			this.display.readToNBT(compound);
		} else if (compound.hasKey("NpcInv", 9)) {
			this.inventory.readEntityFromNBT(compound);
		} else if (compound.hasKey("MovementType", 3)) {
			this.ais.readToNBT(compound);
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			this.stats.maxHealth = textfield.getInteger();
			this.npc.heal((float) this.stats.maxHealth);
		} else if (textfield.getId() == 1) {
			this.stats.aggroRange = textfield.getInteger();
		} else if (textfield.getId() == 14) {
			this.stats.healthRegen = textfield.getInteger();
		} else if (textfield.getId() == 16) {
			this.stats.combatRegen = textfield.getInteger();
		}
	}
	
}
