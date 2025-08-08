package noppes.npcs.client.gui.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiNPCInterface2
extends GuiNPCInterface {

	private final ResourceLocation background;
	private GuiNpcMenu menu;

	public GuiNPCInterface2(EntityNPCInterface npc) { this(npc, -1); }

	public GuiNPCInterface2(EntityNPCInterface npc, int activeMenu) {
		super(npc);
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		xSize = 420;
		ySize = 200;
		closeOnEsc = true;
		if (npc != null) { menu = new GuiNpcMenu(this, activeMenu, npc); }
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (drawDefaultBackground) { drawDefaultBackground(); }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(background);
		if (menu == null && this instanceof GuiNPCGlobalMainMenu) {
			drawTexturedModalRect(guiLeft + 70, guiTop, 0, 0, 142, 220);
			drawTexturedModalRect(guiLeft + xSize - 208, guiTop, 113, 0, 143, 220);
		}
		else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, 200, 220);
			drawTexturedModalRect(guiLeft + xSize - 230, guiTop, 26, 0, 230, 220);
		}
		int x = mouseX;
		int y = mouseY;
		if (hasSubGui()) {
			y = (x = 0);
		}
		if (menu != null) { menu.drawElements(x, y, mc, partialTicks); }
		boolean bo = drawDefaultBackground;
		drawDefaultBackground = false;
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawDefaultBackground = bo;
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (menu != null  && menu.getTopButtons().length > 0) {
			char chr = ((char) 167);
			for (GuiMenuTopButton tab : menu.getTopButtons()) {
				if (tab.isMouseOver()) {
					String text = new TextComponentTranslation("display.hover." + tab.label).getFormattedText();
					String str;
					switch (tab.label) {
						case "menu.display": {
							text += "<br>" + chr + "7" + new TextComponentTranslation("gui.name").getFormattedText() + chr
									+ "7: " + chr + "r" + npc.display.getName() + chr + "7;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("gui.title").getFormattedText() + chr
									+ "7: <" + chr + "r" + npc.display.getTitle() + chr + "7>;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("display.model").getFormattedText()
									+ chr + "7 " + new TextComponentTranslation("display.size").getFormattedText() + chr
									+ "7: " + chr + "r" + npc.display.getSize() + chr + "7;";
							break;
						}
						case "menu.stats": {
							text += "<br>" + chr + "7" + new TextComponentTranslation("stats.health").getFormattedText()
									+ chr + "7: " + chr + "r" + npc.stats.maxHealth + chr + "7;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("stats.aggro").getFormattedText()
									+ chr + "7: " + chr + "r" + npc.stats.aggroRange + chr + "7;";
							switch (npc.stats.spawnCycle) {
							case 0:
								str = "gui.yes";
								break;
							case 1:
								str = "gui.day";
								break;
							case 2:
								str = "gui.night";
								break;
							case 4:
								str = "stats.naturally";
								break;
							default:
								str = "gui.no";
							}
							text += "<br>" + chr + "7" + new TextComponentTranslation("stats.respawn").getFormattedText()
									+ chr + "7: " + chr + "r" + new TextComponentTranslation(str).getFormattedText() + chr
									+ "7;";
							text += "<br>" + chr + "7"
									+ new TextComponentTranslation("stats.meleeproperties").getFormattedText() + chr + "7 "
									+ new TextComponentTranslation("stats.meleestrength").getFormattedText() + chr + "7: "
									+ chr + "r" + npc.stats.melee.getStrength() + chr + "7;";
							text += "<br>" + chr + "7"
									+ new TextComponentTranslation("stats.rangedproperties").getFormattedText() + chr + "7 "
									+ new TextComponentTranslation("enchantment.arrowDamage").getFormattedText() + chr
									+ "7: " + chr + "r" + npc.stats.ranged.getStrength() + chr + "7;";
							break;
						}
						case "menu.ai": {
							switch (npc.ais.onAttack) {
								case 0:
									str = "gui.retaliate";
									break;
								case 1:
									str = "gui.panic";
									break;
								case 2:
									str = "gui.retreat";
									break;
								default:
									str = "gui.nothing";
								}
								text += "<br>" + chr + "7" + new TextComponentTranslation("ai.enemyresponse").getFormattedText()
										+ chr + "7: " + chr + "r" + new TextComponentTranslation(str).getFormattedText() + chr
										+ "7;";
							switch (npc.ais.getMovingType()) {
							case 0:
								str = "ai.standing";
								break;
							case 1:
								str = "ai.wandering";
								break;
							default:
								str = "ai.movingpath";
							}
							text += "<br>" + chr + "7" + new TextComponentTranslation("movement.type").getFormattedText()
									+ chr + "7: " + chr + "r" + new TextComponentTranslation(str).getFormattedText() + chr
									+ "7;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("stats.movespeed").getFormattedText()
									+ chr + "7: " + chr + "r" + npc.ais.getWalkingSpeed() + chr + "7;";
							break;
						}
						case "menu.inventory": {
							text += "<br>" + chr + "7" + new TextComponentTranslation("quest.exp").getFormattedText() + chr
									+ "7: " + chr + "r" + npc.inventory.getExpMin() + chr + "7/" + chr + "r"
									+ npc.inventory.getExpMax() + chr + "7;";
							text += "<br>" + chr + "7"
									+ new TextComponentTranslation("questlog.all.reward").getFormattedText() + chr + "r"
									+ npc.inventory.drops.size() + chr + "7;";
							break;
						}
						case "menu.advanced": {
							text += "<br>" + chr + "7" + new TextComponentTranslation("role.name").getFormattedText() + chr
									+ "7: " + chr + "r"
									+ new TextComponentTranslation(npc.advanced.roleInterface.getEnumType().name).getFormattedText()
									+ chr + "7;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("job.name").getFormattedText() + chr
									+ "7: " + chr + "r"
									+ new TextComponentTranslation(npc.advanced.jobInterface.getEnumType().name).getFormattedText()
									+ chr + "7;";
							text += "<br>" + chr + "7" + new TextComponentTranslation("menu.factions").getFormattedText()
									+ chr + "7: " + chr + "r"
									+ new TextComponentTranslation(npc.getFaction().name).getFormattedText() + chr
									+ "7;";
							break;
						}
						default: {
							break;
						}
					}
					setHoverText(text);
					return;
				}
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (!hasSubGui() && menu != null) { menu.initGui(guiLeft, guiTop, xSize); }
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (!hasSubGui() && menu != null) { menu.mouseClicked(mouseX, mouseY, mouseBottom); }
		super.mouseClicked(mouseX, mouseY, mouseBottom);
	}

	@Override
	public abstract void save();

	// New from Unofficial (BetaZavr)
	@Override
	public void close() {
		if (menu != null && menu.activeMenu != 1 && ClientProxy.playerData.editingNpc != null) {
			menu.save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuDisplay);
			return;
		}
		super.close();
	}
}
