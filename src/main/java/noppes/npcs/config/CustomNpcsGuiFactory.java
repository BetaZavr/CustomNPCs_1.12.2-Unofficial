package noppes.npcs.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.config.CustomNpcsConfigGui;

/** Forge uses this class */
public class CustomNpcsGuiFactory implements IModGuiFactory {

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new CustomNpcsConfigGui(parentScreen, CustomNpcs.Config.getChildElements(), CustomNpcs.MODNAME);
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

}
