package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.roles.JobPuppet;

public class GuiNpcPuppet
extends GuiNPCInterface
implements ISliderListener, ICustomScrollListener {
	
	public HashMap<String, JobPuppet.PartConfig> data;
	private boolean isStart;
	private JobPuppet job;
	private GuiScreen parent;
	private GuiCustomScroll scroll;
	private String selectedName;

	public GuiNpcPuppet(GuiScreen parent, EntityCustomNpc npc) {
		super(npc);
		this.isStart = true;
		this.data = new HashMap<String, JobPuppet.PartConfig>();
		this.parent = parent;
		this.ySize = 230;
		this.xSize = 400;
		this.job = (JobPuppet) npc.jobInterface;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if (!(btn instanceof GuiNpcButton)) {
			return;
		}
		GuiNpcButton button = (GuiNpcButton) btn;
		if (btn.id == 29) {
			this.data.get(this.selectedName).disabled = (button.getValue() == 1);
		}
		if (btn.id == 30) {
			this.job.whileStanding = (button.getValue() == 0);
		}
		if (btn.id == 31) {
			this.job.whileMoving = (button.getValue() == 0);
		}
		if (btn.id == 32) {
			this.job.whileAttacking = (button.getValue() == 0);
		}
		if (btn.id == 33) {
			this.job.animate = (button.getValue() == 0);
			this.isStart = true;
			this.initGui();
		}
		if (btn.id == 34) {
			this.job.animationSpeed = button.getValue();
		}
		if (btn.id == 66) {
			this.close();
		}
		if (btn.id == 67) {
			this.isStart = true;
			this.initGui();
		}
		if (btn.id == 68) {
			this.isStart = false;
			this.initGui();
		}
	}

	@Override
	public void close() {
		this.mc.displayGuiScreen(this.parent);
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawNpc(320, 200);
		super.drawScreen(i, j, f);
	}

	private void drawSlider(int y, JobPuppet.PartConfig config) {
		this.addButton(new GuiNpcButton(29, this.guiLeft + 140, y, 80, 20,
				new String[] { "gui.enabled", "gui.disabled" }, (config.disabled ? 1 : 0)));
		y += 22;
		this.addLabel(new GuiNpcLabel(10, "X", this.guiLeft + 100, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 10, this.guiLeft + 120, y, (config.rotationX + 1.0f) / 2.0f));
		y += 22;
		this.addLabel(new GuiNpcLabel(11, "Y", this.guiLeft + 100, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 11, this.guiLeft + 120, y, (config.rotationY + 1.0f) / 2.0f));
		y += 22;
		this.addLabel(new GuiNpcLabel(12, "Z", this.guiLeft + 100, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 12, this.guiLeft + 120, y, (config.rotationZ + 1.0f) / 2.0f));
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop;
		int i = 30;
		int j = this.guiLeft + 110;
		y += 14;
		this.addButton(new GuiNpcButton(i, j, y, 60, 20, new String[] { "gui.yes", "gui.no" },
				(this.job.whileStanding ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(30, "puppet.standing", this.guiLeft + 10, y + 5, 16777215));
		int k = 31;
		int l = this.guiLeft + 110;
		y += 22;
		this.addButton(new GuiNpcButton(k, l, y, 60, 20, new String[] { "gui.yes", "gui.no" },
				(this.job.whileMoving ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(31, "puppet.walking", this.guiLeft + 10, y + 5, 16777215));
		int m = 32;
		int j2 = this.guiLeft + 110;
		y += 22;
		this.addButton(new GuiNpcButton(m, j2, y, 60, 20, new String[] { "gui.yes", "gui.no" },
				(this.job.whileAttacking ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(32, "puppet.attacking", this.guiLeft + 10, y + 5, 16777215));
		int i2 = 33;
		int j3 = this.guiLeft + 110;
		y += 22;
		this.addButton(
				new GuiNpcButton(i2, j3, y, 60, 20, new String[] { "gui.yes", "gui.no" }, (this.job.animate ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(33, "puppet.animation", this.guiLeft + 10, y + 5, 16777215));
		if (this.job.animate) {
			this.addButton(new GuiButtonBiDirectional(34, this.guiLeft + 240, y, 60, 20,
					new String[] { "1", "2", "3", "4", "5", "6", "7", "8" }, this.job.animationSpeed));
			this.addLabel(new GuiNpcLabel(34, "stats.speed", this.guiLeft + 190, y + 5, 16777215));
		}
		y += 34;
		HashMap<String, JobPuppet.PartConfig> data = new HashMap<String, JobPuppet.PartConfig>();
		if (this.isStart) {
			data.put("model.head", this.job.head);
			data.put("model.body", this.job.body);
			data.put("model.larm", this.job.larm);
			data.put("model.rarm", this.job.rarm);
			data.put("model.lleg", this.job.lleg);
			data.put("model.rleg", this.job.rleg);
		} else {
			data.put("model.head", this.job.head2);
			data.put("model.body", this.job.body2);
			data.put("model.larm", this.job.larm2);
			data.put("model.rarm", this.job.rarm2);
			data.put("model.lleg", this.job.lleg2);
			data.put("model.rleg", this.job.rleg2);
		}
		this.data = data;
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setList(new ArrayList<String>(data.keySet()));
		this.scroll.guiLeft = this.guiLeft + 10;
		this.scroll.guiTop = y;
		this.scroll.setSize(80, 100);
		this.addScroll(this.scroll);
		if (this.selectedName != null) {
			this.scroll.setSelected(this.selectedName);
			this.drawSlider(y, data.get(this.selectedName));
		}
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 22, this.guiTop, 20, 20, "X"));
		if (this.job.animate) {
			this.addButton(new GuiNpcButton(67, this.guiLeft + 10, y + 110, 70, 20, "gui.start"));
			this.addButton(new GuiNpcButton(68, this.guiLeft + 90, y + 110, 70, 20, "gui.end"));
			this.getButton(67).enabled = !this.isStart;
			this.getButton(68).enabled = this.isStart;
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		int percent = (int) (slider.sliderValue * 360.0f);
		slider.setString(percent + "%");
		JobPuppet.PartConfig part = this.data.get(this.selectedName);
		if (slider.id == 10) {
			part.rotationX = (slider.sliderValue - 0.5f) * 2.0f;
		}
		if (slider.id == 11) {
			part.rotationY = (slider.sliderValue - 0.5f) * 2.0f;
		}
		if (slider.id == 12) {
			part.rotationZ = (slider.sliderValue - 0.5f) * 2.0f;
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
