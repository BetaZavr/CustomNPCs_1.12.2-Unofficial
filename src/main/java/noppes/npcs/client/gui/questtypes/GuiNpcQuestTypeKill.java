package noppes.npcs.client.gui.questtypes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeKill extends SubGuiInterface implements ITextfieldListener, ICustomScrollListener {
	public GuiScreen parent;
	private GuiCustomScroll scroll;
	// private Quest quest; Changed
	// private GuiNpcTextField lastSelected; Changed
	// New
	private QuestObjective task;

	public GuiNpcQuestTypeKill(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		// this.quest = NoppesUtilServer.getEditingQuest((EntityPlayer)this.player); //
		// Changed
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Kill Setup";
		this.setBackground("menubg.png");
		this.xSize = 356;
		this.ySize = 216;
		this.closeOnEsc = true;
		// New
		this.task = task;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 0) {
			this.close();
		}
	}

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null) {
			return;
		}
		if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 70, 180, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 186, this.guiTop + 70, 24, 20)) {
			this.setHoverText(
					new TextComponentTranslation("quest.hover.edit.kill.value", "10000000").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 140, 98, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// int i = 0;
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.player.to").getFormattedText(),
				this.guiLeft + 6, this.guiTop + 50));
		/*
		 * Changed for (String name : this.quest.targets.keySet()) {
		 * this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer,
		 * this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20, name));
		 * this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer,
		 * this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20,
		 * this.quest.targets.get(name) + "")); this.getTextField(i + 3).numbersOnly =
		 * true; this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		 * ++i; } while (i < 3) { this.addTextField(new GuiNpcTextField(i, this,
		 * this.fontRenderer, this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20,
		 * "")); this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer,
		 * this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20, "1"));
		 * this.getTextField(i + 3).numbersOnly = true; this.getTextField(i +
		 * 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1); ++i; }
		 */
		// New
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70, 180, 20,
				this.task.getTargetName()));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 186, this.guiTop + 70, 24, 20,
				this.task.getMaxProgress() + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(1, Integer.MAX_VALUE, 1);

		ArrayList<String> list = new ArrayList<String>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = (Class<? extends Entity>) ent.getEntityClass();
			String name = ent.getName();
			try {
				if (!EntityLivingBase.class.isAssignableFrom(c) || EntityNPCInterface.class.isAssignableFrom(c)
						|| c.getConstructor(World.class) == null || Modifier.isAbstract(c.getModifiers())) {
					continue;
				}
				list.add(name.toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException ex) {
			}
		}
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setList(list);
		this.scroll.setSize(130, 198);
		this.scroll.guiLeft = this.guiLeft + 220;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 140, 98, 20, "gui.back"));
	}

	@Override
	public void save() {
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());

		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != this.task.getEnumType()) {
				continue;
			}
			if (task.getTargetName().equals(this.task.getTargetName())) {
				this.getTextField(0).setText("");
				this.task.setTargetName("");
				this.task.setMaxProgress(1);
				break;
			}
		}

		if (this.task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
			}
		}
	}

	private void saveTargets() {
		// Changed
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.getTextField(0).setText(guiCustomScroll.getSelected());
		this.saveTargets();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		// Changed
		this.saveTargets();
	}

}
