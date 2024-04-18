package noppes.npcs.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import noppes.npcs.client.gui.config.CustomNpcsConfigGui;

public class CustomNpcsGuiFactory
implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) { }

	@Override
	public boolean hasConfigGui() { return true; }

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) { return new CustomNpcsConfigGui(parentScreen); }

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }

}
