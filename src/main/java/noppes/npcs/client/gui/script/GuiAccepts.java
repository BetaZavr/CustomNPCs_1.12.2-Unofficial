package noppes.npcs.client.gui.script;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class GuiAccepts extends GuiNPCInterface implements GuiYesNoCallback {
    
    GuiCustomScroll scroll;
    private final Map<String, String> data = Maps.newHashMap();

    public GuiAccepts() {
        this.setBackground("largebg.png");
        this.closeOnEsc = true;
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) {
        switch (button.id) {
            case 0: {
                if (!this.scroll.hasSelected() || !this.data.containsKey(this.scroll.getSelected())) { return; }
                String[] keyWorld = this.data.get(this.scroll.getSelected()).split("/");
                final GuiYesNo guiyesno = getGuiYesNo(keyWorld);
                this.displayGuiScreen(guiyesno);
                break;
            }
            case 1: {
                if (!this.scroll.hasSelected() || !this.data.containsKey(this.scroll.getSelected())) { return; }
                String key = this.data.get(this.scroll.getSelected());
                ScriptController.Instance.setAgreement(key, false);
                if (key.split("/").length > 3) {
                    NoppesUtilPlayer.sendData(EnumPlayerPacket.AcceptScripts, key, false);
                    ScriptController.Instance.setAgreement("main_client_scripts", false);
                    NoppesUtilPlayer.sendData(EnumPlayerPacket.AcceptScripts, "main_client_scripts", false);
                }
                this.initGui();
                break;
            }
            case 2: this.close(); break;
        }
    }

    private GuiYesNo getGuiYesNo(String[] keyWorld) {
        String key = keyWorld[0];
        if (keyWorld[0].equals("main_client_scripts")) {
            key = "In main menu";
        }
        else if (keyWorld.length > 3) {
            key += " [" + ((char) 167) + "6" + keyWorld[keyWorld.length - 1] + ((char) 167) + "r]";
        }
        return new GuiYesNo(this,
                new TextComponentTranslation("system.scripts.accept.0").getFormattedText() + ": \"" + key + ((char) 167) + "r\"",
                new TextComponentTranslation("system.scripts.accept.1").getFormattedText(), 0);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        this.displayGuiScreen(this);
        if (!result) { return; }
        if (!this.scroll.hasSelected() || !this.data.containsKey(this.scroll.getSelected())) { return; }
        String key = this.data.get(this.scroll.getSelected());
        ScriptController.Instance.setAgreement(key, true);
        if (key.split("/").length > 3) {
            NoppesUtilPlayer.sendData(EnumPlayerPacket.AcceptScripts, key, true);
            ScriptController.Instance.setAgreement("main_client_scripts", true);
            NoppesUtilPlayer.sendData(EnumPlayerPacket.AcceptScripts, "main_client_scripts", true);
        }
        this.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.guiLeft, this.guiTop + this.ySize - 1, 0.0f);
        GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
        this.mc.renderEngine.bindTexture(this.background);
        this.drawTexturedModalRect(0, 0, 0, 228, this.xSize, 3);
        GlStateManager.popMatrix();
        boolean bo = this.scroll != null && this.scroll.hasSelected() && this.data.containsKey(this.scroll.getSelected());
        if (bo) {
            boolean agreement = ScriptController.Instance.getAgreements().get(this.data.get(this.scroll.getSelected()));
            this.getButton(0).setEnabled(CustomNpcs.EnableScripting && !agreement);
            this.getButton(1).setEnabled(agreement);
        } else {
            this.getButton(0).setEnabled(false);
            this.getButton(1).setEnabled(false);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!CustomNpcs.ShowDescriptions) { return; }
        if (this.getButton(0) != null && this.getButton(0).isMouseOver())  {
            this.setHoverText("system.scripts.hover.sets.yes");
        } else if (this.getButton(1) != null && this.getButton(1).isMouseOver())  {
            this.setHoverText("system.scripts.hover.sets.no");
        } else if (this.getButton(2) != null && this.getButton(2).isMouseOver())  {
            this.setHoverText("hover.exit");
        }
    }
    
    @Override
    public void initGui() {
        super.initGui();
        if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(180, 179); }
        this.scroll.guiLeft = this.guiLeft + 6;
        this.scroll.guiTop = this.guiTop + 17;
        Map<String, Boolean> map = ScriptController.Instance.getAgreements();
        List<String> list = Lists.newArrayList();
        List<String> suffixs = Lists.newArrayList();
        int i = 0;
        this.scroll.hoversTexts = new String[map.size()][];
        for (String worldName : map.keySet()) {
            if (worldName.indexOf("any_maps") == 0) { continue; }
            boolean bo = map.get(worldName);
            String color = ((char) 167) + (worldName.split("/").length < 4 ? "3" : "6");
            suffixs.add(((char) 167) + (bo ? "a" : "c") + bo);
            String part;
            if (worldName.equals("main_client_scripts")) {
                part = color + i + ": In main menu";
                this.scroll.hoversTexts[i] = new String[] { "Running client scripts received from the last connection"};
            } else {
                part = color + i + ": " + ((char) 167) + "r" + worldName.split("/")[0];
                this.scroll.hoversTexts[i] = Util.instance.getAgreementKeyHover(worldName);
            }
            list.add(part);
            this.data.put(part, worldName);
            i++;
        }
        this.scroll.setListNotSorted(list);
        this.scroll.setSuffixs(suffixs);
        this.addScroll(this.scroll);

        this.addLabel(new GuiNpcLabel(0, "system.scripts.list", this.guiLeft + 6, this.guiTop + 7));
        this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + 198, 50, 20, "gui.yes"));
        this.addButton(new GuiNpcButton(1, this.guiLeft + 58, this.guiTop + 198, 50, 20, "gui.no"));
        this.addButton(new GuiNpcButton(2, this.guiLeft + 136, this.guiTop + 198, 50, 20, "gui.done"));
    }
    
    @Override
    public void save() { }
    
}
