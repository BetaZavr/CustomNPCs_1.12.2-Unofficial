package noppes.npcs.client.gui.util;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiContainerNPCInterface2
extends GuiContainerNPCInterface {

	protected ResourceLocation background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
	protected ResourceLocation defaultBackground = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
	private GuiNpcMenu menu;
	public int menuYOffset;

	public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont) {
		this(npc, cont, -1);
	}

	public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont, int activeMenu) {
		super(npc, cont);
		this.menuYOffset = 0;
		this.xSize = 420;
		this.menu = new GuiNpcMenu((GuiScreen) this, activeMenu, npc);
		this.title = "";
	}

	public void delete() {
		this.npc.delete();
		this.displayGuiScreen(null);
		this.mc.setIngameFocus();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.background);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 256, 256);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.defaultBackground);
		this.drawTexturedModalRect(this.guiLeft + this.xSize - 200, this.guiTop, 26, 0, 200, 220);
		this.menu.drawElements(this.fontRenderer, i, j, this.mc, f);
		super.drawGuiContainerBackgroundLayer(f, i, j);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
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
							switch(this.npc.advanced.role) {
								case 0:
									str = "role.none";
									break;
								case 1:
									str = "role.trader";
									break;
								case 2:
									str = "role.mercenary";
									break;
								case 3:
									str = "role.bank";
									break;
								case 4:
									str = "role.transporter";
									break;
								case 5:
									str = "role.mailman";
									break;
								case 6:
									str = NoppesStringUtils.translate("role.companion", "(WIP)");
									break;
								default:
									str = "dialog.dialog";
							}
							text += "<br>"+chr+"7"+new TextComponentTranslation("role.name").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(str).getFormattedText()+chr+"7;";
							switch(this.npc.advanced.job) {
								case 0:
									str = "job.none";
									break;
								case 1:
									str = "job.bard";
									break;
								case 2:
									str = "job.healer";
									break;
								case 3:
									str = "job.guard";
									break;
								case 4:
									str = "job.itemgiver";
									break;
								case 5:
									str = "role.follower";
									break;
								case 6:
									str = "job.spawner";
									break;
								case 7:
									str = "job.conversation";
									break;
								case 8:
									str = "job.chunkloader";
									break;
								case 9:
									str = "job.puppet";
									break;
								case 10:
									str = "job.builder";
									break;
								default:
									str = "job.farmer";
							}
							text += "<br>"+chr+"7"+new TextComponentTranslation("job.name").getFormattedText()+chr+"7: "+chr+"r"+new TextComponentTranslation(str).getFormattedText()+chr+"7;";
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
	public ResourceLocation getResource(String texture) {
		return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.menu.initGui(this.guiLeft, this.guiTop + this.menuYOffset, this.xSize);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
		if (!this.hasSubGui()) {
			this.menu.mouseClicked(i, j, k);
		}
	}

	public void setBackground(String texture) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
	}
}
