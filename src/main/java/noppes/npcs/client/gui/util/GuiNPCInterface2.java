package noppes.npcs.client.gui.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiNPCInterface2
extends GuiNPCInterface {
	
	private ResourceLocation background;
	private GuiNpcMenu menu;

	public GuiNPCInterface2(EntityNPCInterface npc) {
		this(npc, -1);
	}

	public GuiNPCInterface2(EntityNPCInterface npc, int activeMenu) {
		super(npc);
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		this.xSize = 420;
		this.ySize = 200;
		this.menu = new GuiNpcMenu(this, activeMenu, npc);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.drawDefaultBackground) {
			this.drawDefaultBackground();
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.background);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 200, 220);
		this.drawTexturedModalRect(this.guiLeft + this.xSize - 230, this.guiTop, 26, 0, 230, 220);
		int x = mouseX;
		int y = mouseY;
		if (this.hasSubGui()) {
			y = (x = 0);
		}
		this.menu.drawElements(this.getFontRenderer(), x, y, this.mc, partialTicks);
		boolean bo = this.drawDefaultBackground;
		this.drawDefaultBackground = false;
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.drawDefaultBackground = bo;
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.menu!=null && this.menu.getTopButtons().length>0) {
			char chr = Character.toChars(0x00A7)[0];
			for (GuiMenuTopButton tab : this.menu.getTopButtons()) {
				if (tab.isMouseOver()) {
					String text = new TextComponentTranslation("display.hover."+tab.lable).getFormattedText();
					String str = "";
					switch(tab.lable) {
						case "menu.display": {
							text += "<br>"+chr+"7"+new TextComponentTranslation("gui.name").getFormattedText()+chr+"7: "+chr+"r"+this.npc.display.getName()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("gui.title").getFormattedText()+chr+"7: <"+chr+"r"+this.npc.display.getTitle()+chr+"7>;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("display.model").getFormattedText()+chr+"7 "+new TextComponentTranslation("display.size").getFormattedText()+chr+"7: "+chr+"r"+this.npc.display.getSize()+chr+"7;";
							break;
						}
						case "menu.stats": {
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.health").getFormattedText()+chr+"7: "+chr+"r"+this.npc.stats.maxHealth+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.aggro").getFormattedText()+chr+"7: "+chr+"r"+this.npc.stats.aggroRange+chr+"7;";
							switch(this.npc.stats.spawnCycle) {
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
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.respawn").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(str).getFormattedText()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.meleeproperties").getFormattedText()+chr+"7 "+new TextComponentTranslation("stats.meleestrength").getFormattedText()+chr+"7: "+chr+"r"+this.npc.stats.melee.getStrength()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.rangedproperties").getFormattedText()+chr+"7 "+new TextComponentTranslation("enchantment.arrowDamage").getFormattedText()+chr+"7: "+chr+"r"+this.npc.stats.ranged.getStrength()+chr+"7;";
							break;
						}
						case "menu.ai": {
							switch(this.npc.ais.onAttack) {
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
							text += "<br>"+chr+"7"+new TextComponentTranslation("ai.enemyresponse").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(str).getFormattedText()+chr+"7;";
							switch(this.npc.ais.getMovingType()) {
								case 0:
									str = "ai.standing";
									break;
								case 1:
									str = "ai.wandering";
									break;
								default:
									str = "ai.movingpath";
							}
							text += "<br>"+chr+"7"+new TextComponentTranslation("movement.type").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(str).getFormattedText()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("stats.movespeed").getFormattedText()+chr+"7: "+chr+"r"+this.npc.ais.getWalkingSpeed()+chr+"7;";
							break;
						}
						case "menu.inventory": {
							text += "<br>"+chr+"7"+new TextComponentTranslation("quest.exp").getFormattedText()+chr+"7: "+chr+"r"+this.npc.inventory.getExpMin()+chr+"7/"+chr+"r"+this.npc.inventory.getExpMax()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("questlog.all.reward").getFormattedText()+chr+"r"+this.npc.inventory.drops.size()+chr+"7;";
							break;
						}
						case "menu.advanced": {
							text += "<br>"+chr+"7"+new TextComponentTranslation("role.name").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(this.npc.advanced.roleInterface.getEnumType().name).getFormattedText()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("job.name").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(this.npc.advanced.jobInterface.getEnumType().name).getFormattedText()+chr+"7;";
							text += "<br>"+chr+"7"+new TextComponentTranslation("menu.factions").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(this.npc.getFaction().name).getFormattedText()+chr+"7;";
							break;
						}
						default: {
							break;
						}
					}
					this.setHoverText(text);
					return;
				}
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.menu.initGui(this.guiLeft, this.guiTop, this.xSize);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (!this.hasSubGui()) {
			this.menu.mouseClicked(mouseX, mouseY, mouseBottom);
		}
		super.mouseClicked(mouseX, mouseY, mouseBottom);
	}

	@Override
	public abstract void save();
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.close();
			if (this instanceof GuiNpcDisplay) {
				this.save();
			} else {
				this.menu.topButtonPressed(new GuiMenuTopButton(1, 0, 0, ""));
			}
			return;
		}
		super.keyTyped(c, i);
	}
	
}
