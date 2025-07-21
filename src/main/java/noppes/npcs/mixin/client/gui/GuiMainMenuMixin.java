package noppes.npcs.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = GuiMainMenu.class, priority = 499)
public class GuiMainMenuMixin {

    @Shadow
    private ResourceLocation backgroundTexture;

    @Final
    @Shadow
    private static ResourceLocation[] TITLE_PANORAMA_PATHS;

    @Unique
    public int cnpc$variant = new Random().nextInt(CustomNpcs.PanoramaNumbers);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void npcs$onConstructor(CallbackInfo ci) {
        for (int i = 0; i < 6; i++) {
            TITLE_PANORAMA_PATHS[i] = new ResourceLocation(CustomNpcs.MODID, "textures/gui/title/background/"+cnpc$variant+"/panorama_"+i+".png");
        }
    }

    @Inject(method = "initGui", at = @At("TAIL"))
    public void npcs$initGui(CallbackInfo ci) {
        if (CustomNpcs.ShowButtonsInGuiMenu) {
            ((GuiScreen) (Object) this).buttonList.add(new GuiNpcButton(150, 3, 3, 20, 16, cnpc$variant + 1,
                            "MC", "1", "2", "3", "4"));
        }
    }

    @Inject(method = "actionPerformed", at = @At("TAIL"))
    protected void npcs$actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 150) {
            cnpc$variant = ((GuiNpcButton) button).getValue() - 1;
            for(int i = 0; i < 6; ++i) {
                if (cnpc$variant < 0) { TITLE_PANORAMA_PATHS[i] = new ResourceLocation("textures/gui/title/background/panorama_" + i + ".png"); }
                else { TITLE_PANORAMA_PATHS[i] = new ResourceLocation(CustomNpcs.MODID, "textures/gui/title/background/" + cnpc$variant + "/panorama_" + i + ".png"); }
            }
        }
    }

    /**
     * @author BetaZavr
     * @reason remove white mask
     */
    @Overwrite
    private void rotateAndBlurSkybox() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(backgroundTexture);
        GlStateManager.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
    }

}
