package noppes.npcs.client.gui.advanced;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNPCLinesEdit;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCLinesMenu
extends GuiNPCInterface2
implements IGuiData, ISubGuiListener {
	
	public GuiNPCLinesMenu(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: { this.setSubGui(new SubGuiNPCLinesEdit(0, this.npc, this.npc.advanced.worldLines, "lines.world")); break; }
			case 1: { this.setSubGui(new SubGuiNPCLinesEdit(1, this.npc, this.npc.advanced.attackLines, "lines.attack")); break; }
			case 2: { this.setSubGui(new SubGuiNPCLinesEdit(2, this.npc, this.npc.advanced.interactLines, "lines.interact")); break; }
			case 3: { this.setSubGui(new SubGuiNPCLinesEdit(3, this.npc, this.npc.advanced.killedLines, "lines.killed")); break; }
			case 4: { this.setSubGui(new SubGuiNPCLinesEdit(4, this.npc, this.npc.advanced.killLines, "lines.kill")); break; }
			case 5: { this.setSubGui(new SubGuiNPCLinesEdit(5, this.npc, this.npc.advanced.npcInteractLines, "lines.npcinteract")); break; }
			case 6: { this.npc.advanced.orderedLines = !((GuiNpcButtonYesNo) button).getBoolean(); break; }
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 85, this.guiTop + 20, "lines.world"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 85, this.guiTop + 43, "lines.attack"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 85, this.guiTop + 66, "lines.interact"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 85, this.guiTop + 89, "lines.killed"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 85, this.guiTop + 112, "lines.kill"));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 85, this.guiTop + 135, "lines.npcinteract"));
		this.addLabel(new GuiNpcLabel(16, "lines.random", this.guiLeft + 85, this.guiTop + 163));
		this.addButton(new GuiNpcButtonYesNo(6, this.guiLeft + 175, this.guiTop + 158, !this.npc.advanced.orderedLines));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.remove").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.select").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.npc.advanced.readToNBT(compound);
		this.initGui();
	}
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNPCLinesEdit)) { return; }
		SubGuiNPCLinesEdit sub = (SubGuiNPCLinesEdit) subgui;
		sub.lines.correctLines();
		switch(sub.id) {
			case 0: { this.npc.advanced.worldLines = sub.lines; break; }
			case 1: { this.npc.advanced.attackLines = sub.lines; break; }
			case 2: { this.npc.advanced.interactLines = sub.lines; break; }
			case 3: { this.npc.advanced.killedLines = sub.lines; break; }
			case 4: { this.npc.advanced.killLines = sub.lines; break; }
			case 5: { this.npc.advanced.npcInteractLines = sub.lines; break; }
		}
		this.save();
	}
	
}
