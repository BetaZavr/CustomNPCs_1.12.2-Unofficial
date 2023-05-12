package noppes.npcs.client.gui.animation;

import java.util.ArrayList;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataAnimation;

public class GuiNpcAnimation
extends GuiNPCInterface
implements ISliderListener, ICustomScrollListener {
	
	private DataAnimation animation;
	private AnimationConfig currentAnim;
	private PartConfig currentPart;
	private GuiScreen parent;
	private GuiCustomScroll scroll;
	private String selectedName;
	
	private int[] rots;
	private MarkData mData;
	private int showName;

	public GuiNpcAnimation(GuiScreen parent, EntityCustomNpc npc) {
		super(npc);
		this.parent = parent;
		this.ySize = 240;
		this.xSize = 427;
		this.animation = npc.animation;
		this.closeOnEsc = true;
		
		this.currentAnim = null;
		this.currentPart = null;
		this.setBackground("bgfilled.png");
		this.rots = new int[] { 0, 0, 0, 0 };
		this.mData = new MarkData();
		MarkData md = MarkData.get(this.npc);
		for (Mark m : md.marks) { this.mData.marks.add(m); }
		md.marks.clear();
		this.showName = this.npc.display.getShowName();
		this.npc.display.setShowName(1);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setList(new ArrayList<String>()); // this.curentSelect
		this.scroll.guiLeft = this.guiLeft + 10;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setSize(120, 100);
		this.addScroll(this.scroll);
		if (this.selectedName != null) { this.scroll.setSelected(this.selectedName); }
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 22, this.guiTop, 20, 20, "X"));
		
		this.addButton(new GuiButtonBiDirectional(1, this.guiLeft+4, this.guiTop+14, 120, 20, EnumAnimationType.getNames(), this.currentAnim!=null ? this.currentAnim.getType() : EnumAnimationType.standing.ordinal()));
		this.addSlider(new GuiNpcSlider(this, 10, this.guiLeft+4, this.guiTop+140, 60, 5, 0.5f));
		
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (!(button instanceof GuiNpcButton)) {
			return;
		}
		GuiNpcButton npcButton = (GuiNpcButton) button;
		switch(npcButton.id) {
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void close() {
		MarkData md = MarkData.get(this.npc);
		for (Mark m : this.mData.marks) { md.marks.add(m); }
		this.npc.display.setShowName(this.showName);
		this.mc.displayGuiScreen(this.parent);
		NBTTagCompound nbtAnim = new NBTTagCompound();
		this.animation.writeToNBT(nbtAnim);
		Client.sendData(EnumPacketServer.JobSave, nbtAnim);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		//System.out.println("CNPCs: "+this.npc);
		if (this.npc!=null) {
			int x = this.guiLeft+232, y = this.guiTop+10;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			for (int i=0; i<3; i++) {
				Gui.drawRect(x-1+i*65, y-1, x+56+i*65, y+91, i==0 ? 0xFFF08080 : i==1 ? 0xFF80F080 : 0xFF8080F0);
				Gui.drawRect(x+i*65, y, x+55+i*65, y+90, 0xFF000000);
				this.drawNpc(this.npc, x+15+i*65, y+45, 1.0f, this.rots[2], false);
			}
			x += 54;
			y += 95;
			Gui.drawRect(x-1, y-1, x+78, y+128, 0xFF808080);
			Gui.drawRect(x, y, x+77, y+127, 0xFF000000);
			this.drawNpc(this.npc, x+26, y+79, 1.4111f, this.rots[2], false);
			GlStateManager.popMatrix();
			
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		int percent = (int) (slider.sliderValue * 360.0f);
		slider.setString(percent + "%");
		switch(slider.id) {
			case 1: {
				if (this.currentPart==null) { return; }
				this.currentPart.rotation[0] = (slider.sliderValue - 0.5f) * 2.0f;
				break;
			}
			case 2: {
				if (this.currentPart==null) { return; }
				this.currentPart.rotation[1] = (slider.sliderValue - 0.5f) * 2.0f;
				break;
			}
			case 3: {
				if (this.currentPart==null) { return; }
				this.currentPart.rotation[2] = (slider.sliderValue - 0.5f) * 2.0f;
				break;
			}
		}
		this.npc.updateHitbox();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.selectedName = guiCustomScroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}
}
