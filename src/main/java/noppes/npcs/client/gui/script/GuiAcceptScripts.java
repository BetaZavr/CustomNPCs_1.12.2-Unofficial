package noppes.npcs.client.gui.script;

import com.google.common.collect.Lists;
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
import java.io.File;
import java.util.List;

public class GuiAcceptScripts extends GuiNPCInterface implements GuiYesNoCallback {

    private final int type; // 0: server scripts; 1: local scripts; 2: client scripts
    GuiCustomScroll scroll;

    public GuiAcceptScripts(int t) {
        this.setBackground("largebg.png");
        if (t == 0 && CustomNpcs.Server != null) { t = 1; }
        this.type = t;
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) {
        if (button.id == 0) {
            GuiYesNo guiyesno = new GuiYesNo(this,
                    new TextComponentTranslation("system.scripts.accept.0").getFormattedText(),
                    new TextComponentTranslation("system.scripts.accept.1").getFormattedText(), this.type);
            this.displayGuiScreen(guiyesno);
            return;
        }
        this.close();
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        this.displayGuiScreen(this);
        if (!result) { return; }
        String key = ScriptController.Instance.clientScripts.getWorldName();
        ScriptController.Instance.setAgreement(key, true);
        if (id == 0) { // server
            NoppesUtilPlayer.sendData(EnumPlayerPacket.AcceptScripts, key, true);
        }
        this.close();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.guiLeft, this.guiTop + this.ySize - 1, 0.0f);
        GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
        this.mc.renderEngine.bindTexture(this.background);
        this.drawTexturedModalRect(0, 0, 0, 228, this.xSize, 3);
        GlStateManager.popMatrix();
        this.getButton(0).setEnabled(CustomNpcs.EnableScripting);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!CustomNpcs.ShowDescriptions) { return; }
        if ((this.getButton(0) != null && this.getButton(0).isMouseOver()) ||
                (this.getButton(1) != null && this.getButton(1).isMouseOver()))  {
            this.setHoverText("system.scripts.accept.hover.3");
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int lID = 0;
        String l = new TextComponentTranslation("system." + (this.type == 2 ? "client" : "server") + ".scripts.accept").getFormattedText();
        int w = this.fontRenderer.getStringWidth(l);

        if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(180, 158); }
        this.scroll.height = 168 - 10 * (int) Math.floor(w / 160.0d);
        this.scroll.guiLeft = this.guiLeft + 6;
        this.scroll.guiTop = this.guiTop + 17;
        this.scroll.selectable = false;
        StringBuilder l0 = new StringBuilder();
        List<String> list = Lists.newArrayList();
        for (String str : this.getTextList()) {
            if (this.fontRenderer.getStringWidth(l0 + str + " ") < 140) { l0.append(str).append(" "); }
            else {
                list.add(l0.toString());
                l0 = new StringBuilder();
                l0.append(str).append(" ");
            }
        }
        list.add(l0.toString());
        if (this.type != 0) {
            String c = ((char) 167) + (this.type == 2 ? "a" : "b");
            String path = ("" + CustomNpcs.getWorldSaveDirectory("scripts" + (this.type == 2 ? "/client" : ""))).replace("\\", "/");
            if (this.type == 2) {
                path = (new File(CustomNpcs.Dir, "client_default/"+ScriptController.Instance.clientScripts.getLanguage().toLowerCase())).getAbsolutePath().replace("\\", "/");
                path = path.replace("config/../customnpcs", "customnpcs");
                System.out.println("CNPCs: "+path);
            }
            l0 = new StringBuilder(c);
            for (String str : path.split("/")) {
                if (this.fontRenderer.getStringWidth(l0 + str + "/") < 130) {
                    l0.append(str).append("/");
                } else {
                    list.add(l0.toString());
                    l0 = new StringBuilder(c);
                    l0.append(str).append("/");
                }
            }
            list.add(l0 + "...");
        }
        this.scroll.setListNotSorted(list);
        this.addScroll(this.scroll);

        if (w > 160) {
            l0 = new StringBuilder(((char) 167) + "4" + ((char) 167) + "l");
            int y = this.guiTop + 186 - 10 * (int) Math.floor(w / 160.0d);
            for (String str : l.split(" ")) {
                if (this.fontRenderer.getStringWidth(l0 + str + " ") < 160) { l0.append(str).append(" "); }
                else {
                    this.addLabel(new GuiNpcLabel(lID++, l0.toString(), this.guiLeft + 100 - this.fontRenderer.getStringWidth(l0.toString()) / 2, y));
                    y += 10;
                    l0 = new StringBuilder(((char) 167) + "4" + ((char) 167) + "l");
                    l0.append(str).append(" ");
                }
            }
            this.addLabel(new GuiNpcLabel(lID++, l0.toString(), this.guiLeft + 100 - this.fontRenderer.getStringWidth(l0.toString()) / 2, y));
        }
        else {
            this.addLabel(new GuiNpcLabel(lID++, l, this.guiLeft + 12, this.guiTop + 176));
        }

        this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + 198, 50, 20, "gui.yes"));
        this.addButton(new GuiNpcButton(1, this.guiLeft + 136, this.guiTop + 198, 50, 20, "gui.no"));
        this.addLabel(new GuiNpcLabel(lID, new TextComponentTranslation("gui.detailed").getFormattedText() + "(?)", this.guiLeft + 6, this.guiTop + 7));
        this.getLabel(lID).hoverText = Util.instance.getAgreementKeyHover(ScriptController.Instance.clientScripts.getWorldName());
    }

    private String[] getTextList() {
        String t = new TextComponentTranslation("system.scripts.accept.hover.0").getFormattedText();
        if (this.type == 0) { // server
            t += " " + new TextComponentTranslation("system.scripts.accept.hover.2").getFormattedText();
        }
        else {  // client or local
            t += " " + new TextComponentTranslation("system.scripts.accept.hover.1").getFormattedText();
        }
        return t.split(" ");
    }

    @Override
    public void save() { }

}
