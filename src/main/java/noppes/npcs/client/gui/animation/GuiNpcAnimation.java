package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.util.AdditionalMethods;

public class GuiNpcAnimation
extends GuiNPCInterface
implements ISubGuiListener, ISliderListener, ICustomScrollListener, ITextfieldListener {
	
	private DataAnimation animation;
	private Map<String, AnimationConfig> dataAnim;
	private PartConfig currentPart;
	private GuiScreen parent;
	private GuiCustomScroll scroll;
	private String selectedName;
	private AnimationConfig currentAnim;
	
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
		
		this.dataAnim = Maps.<String, AnimationConfig>newHashMap();
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
		this.selectedName = "";
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setList(new ArrayList<String>()); // this.curentSelect
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 36;
		this.scroll.setSize(120, 156);
		this.addScroll(this.scroll);
		if (this.selectedName != null) { this.scroll.setSelected(this.selectedName); }
		this.currentAnim = null;
		if (!this.selectedName.isEmpty() && this.dataAnim.containsKey(this.selectedName)) { this.currentAnim = this.dataAnim.get(this.selectedName); }
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 22, this.guiTop, 20, 20, "X"));
		
		this.addLabel(new GuiNpcLabel(0, "animation.type", this.guiLeft+4, this.guiTop+4));
		this.addButton(new GuiButtonBiDirectional(1, this.guiLeft+4, this.guiTop+14, 120, 20, EnumAnimationType.getNames(), currentAnim!=null ? currentAnim.getType() : EnumAnimationType.standing.ordinal()));
		
		this.addButton(new GuiNpcButton(2, this.guiLeft + 4, this.guiTop + 194, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 64, this.guiTop + 194, 58, 20, "gui.remove"));
		this.getButton(3).enabled = this.currentAnim!=null;
		this.addButton(new GuiNpcButton(4, this.guiLeft + 4, this.guiTop + 216, 58, 20, "gui.load"));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 64, this.guiTop + 216, 58, 20, "gui.save"));
		this.getButton(5).enabled = this.currentAnim!=null;
		
		if (this.currentAnim!=null) {
			int x = this.guiLeft+230, y = this.guiTop+102;
			for (int i=0; i<4; i++) {
				int xn = i>2 ? 65 : i*65, yn = i>2 ? 100 : 0;
				this.addSlider(new GuiNpcSlider(this, 10+i, x+xn, y+yn, 59, 6, (float) this.rots[i] / 360.0f));
			}
			
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		System.out.println("buttonID: "+button.id);
		if (!(button instanceof GuiNpcButton)) {
			return;
		}
		GuiNpcButton npcButton = (GuiNpcButton) button;
		switch(npcButton.id) {
			case 2: {
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
			}
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
		if (this.npc!=null && this.currentAnim!=null) {
			int x = this.guiLeft+232, y = this.guiTop+10;
			int xn = 27, yn = 70;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			for (int i=0; i<3; i++) {
				Gui.drawRect(x-1+i*65, y-1, x+56+i*65, y+91, i==0 ? 0xFFF08080 : i==1 ? 0xFF80F080 : 0xFF8080F0);
				Gui.drawRect(x+i*65, y, x+55+i*65, y+90, 0xFF000000);
				this.drawNpc(this.npc, x+xn+i*65, y+yn, 1.0f, this.rots[i], false);
			}
			x += 65;
			y += 100;
			Gui.drawRect(x-1, y-1, x+56, y+91, 0xFF808080);
			Gui.drawRect(x, y, x+55, y+90, 0xFF000000);
			this.drawNpc(this.npc, x+xn, y+yn, 1.0f, this.rots[3], false);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft+4, this.guiTop+36, 120, 156)) {
			this.setHoverText(new TextComponentTranslation("animation.hover.list").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.type").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.add").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		}  else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.load").getFormattedText());
		}  else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.save").getFormattedText());
		} else if (this.currentAnim!=null) {
			if (this.isMouseHover(mouseX, mouseY, this.guiLeft+232, this.guiTop+10, 55, 90)) {
				this.setHoverText(new TextComponentTranslation("animation.hover.animated").getFormattedText());
			}
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		slider.setString(String.valueOf((int) (slider.sliderValue * 360.0f)));
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
			case 10: {
				this.rots[0] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 11: {
				this.rots[1] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 12: {
				this.rots[2] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 13: {
				this.rots[3] = (int) (slider.sliderValue * 360.0f);
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

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		if (subgui.id == 1) { // New
			
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		
	}
	
}
