package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;
import java.util.*;

public class GuiPermissionsEdit extends GuiNPCInterface implements ICustomScrollListener {

    public static final String UserNameRegex = "^[a-zA-Z][a-zA-Z0-9_]{3,15}$";
    protected final Map<String, List<String>> data = new HashMap<>();
    protected final Map<String, String> nodes = new HashMap<>();
    protected GuiCustomScroll permissions;
    protected GuiCustomScroll names;
    protected boolean wait = true;

    public GuiPermissionsEdit() {
        super();
        setBackground("menubg.png");
        xSize = 384;
        ySize = 217;

        Client.sendData(EnumPacketServer.PermissionsGet);
    }

    @Override
    public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
        if (mouseButton != 0) { return; }
        switch (button.id) {
            case 0: {
                setSubGui(new SubGuiEditText(0, player.getName())
                        .setHoverTexts(new TextComponentTranslation("permission.hover.names")));
                break;
            }
            case 1: {
                if (!permissions.hasSelected() || !names.hasSelected() || !data.containsKey(permissions.getSelected())) { return; }
                String node = nodes.get(permissions.getSelected()).substring(11);
                Client.sendData(EnumPacketServer.PermissionsDel, names.getSelected(), node);
                wait = true;
                break;
            }
            case 66: onClosed(); break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (wait) {
            drawWait();
            return;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void init() {
        initGui();
        wait = false;
    }

    @Override
    public void initGui() {
        super.initGui();
        int w = xSize / 2 - 6;
        CustomNpcsPermissions.putToData(data, nodes);
        List<String> list = new ArrayList<>(data.keySet());
        Collections.sort(list);
        LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String name = nodes.get(permissions.getSelected()).substring(11);
            List<String> hovers = new ArrayList<>();
            hovers.add(TextFormatting.GRAY + new TextComponentTranslation("gui.name").getFormattedText() +
                    ": " + TextFormatting.RESET + name);
            hovers.add(new TextComponentTranslation("permission.hover."+name).getFormattedText());
            hts.put(i, hovers);
        }

        if (permissions == null) { permissions = new GuiCustomScroll(this, 0).setSize(w, ySize - 21); }
        permissions.guiLeft = guiLeft + 5;
        permissions.guiTop = guiTop + 16;
        permissions.setUnsortedList(list)
                .setHoverTexts(hts);
        addScroll(permissions);

        int x = guiLeft + xSize / 2 + 1;
        if (names == null) { names = new GuiCustomScroll(this, 1).setSize(w, ySize - 43); }
        names.guiLeft = x;
        names.guiTop = guiTop + 16;
        names.setHoverText("permission.hover.names");
        if (permissions.hasSelected() && data.containsKey(permissions.getSelected())) { names.setList(data.get(permissions.getSelected())); }
        addScroll(names);

        addLabel(new GuiNpcLabel(0, "permission.nodes", permissions.guiLeft + 1, guiTop + 4));
        addLabel(new GuiNpcLabel(1, new TextComponentTranslation("playerdata.players").appendText(":"), x, guiTop + 4));

        int y = guiTop + ySize - 25;
        addButton(new GuiNpcButton(0, names.guiLeft, y, w / 2 - 1, 20, "gui.add")
                .setIsEnable(permissions.hasSelected())
                .setHoverText("permission.hover.add"));
        addButton(new GuiNpcButton(1, names.guiLeft + w / 2 + 1, y, w / 2 - 1, 20, "gui.remove")
                .setIsEnable(names.hasSelected())
                .setHoverText("permission.hover.del"));

        addButton(new GuiNpcButton(66, guiLeft + xSize - 16, guiTop + 4, 12, 12, "X")
                .setHoverText("hover.exit"));
    }

    @Override
    public void subGuiClosed(GuiScreen subgui) {
        if (subgui instanceof SubGuiEditText) {
            SubGuiEditText gui = (SubGuiEditText) subgui;
            if (gui.cancelled || gui.text[0].isEmpty() || !permissions.hasSelected() || !data.containsKey(permissions.getSelected())) { return; }
            String name = gui.text[0];
            if (name.equalsIgnoreCase("all")) { name = "All"; }
            else if (name.equalsIgnoreCase("command block")) { name = "Command Block"; }
            else if (!name.matches(UserNameRegex)) {
                String error = "§c" + Util.instance.translateGoogle(player, "Player name must be at least 4 characters long and must not contain spaces or characters other than _");
                if (error.contains("4")) { error = error.replace("4", "§64§c"); }
                if (error.contains("_")) { error = error.replace("_", "'§f_§c'"); }
                player.sendMessage(new TextComponentString(error));
                return;
            }
            String node = nodes.get(permissions.getSelected()).substring(11);
            Client.sendData(EnumPacketServer.PermissionsAdd, name, node);
            wait = true;
        }
    }

    @Override
    public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
        if (scroll.id == 0) { names.setSelect(-1); }
        initGui();
    }

    @Override
    public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
