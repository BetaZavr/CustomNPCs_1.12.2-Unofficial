package micdoodle8.mods.galacticraft.api.client.tabs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

public class TabRegistry {

	private static Class<?> clazzJEIConfig = null;
	public static Class<?> clazzNEIConfig = null;
	private static boolean initWithPotion;
	private static final Minecraft mc = FMLClientHandler.instance().getClient();
	private static final ArrayList<AbstractTab> tabList = new ArrayList<>();

	static {
		try {
			TabRegistry.clazzJEIConfig = Class.forName("mezz.jei.config.Config");
		} catch (Exception e) { LogWriter.info("Mezz Config is missed:"); }
		if (TabRegistry.clazzJEIConfig == null) {
			try {
				TabRegistry.clazzNEIConfig = Class.forName("codechicken.nei.NEIClientConfig");
			}
			catch (Exception ee) { LogWriter.info("Code Chicken Config is missed:"); }
		}
	}

	public static void addTabsToList(List<GuiButton> buttonList) {
		for (AbstractTab tab : TabRegistry.tabList) {
			if (tab.shouldAddToList()) {
				buttonList.add(tab);
			}
		}
	}

	public static int getPotionOffset() {
		if (!TabRegistry.mc.player.getActivePotionEffects().isEmpty()) {
			TabRegistry.initWithPotion = true;
			return 60 + getPotionOffsetJEI() + getPotionOffsetNEI();
		}
		TabRegistry.initWithPotion = false;
		return 0;
	}

	public static int getPotionOffsetJEI() {
		if (TabRegistry.clazzJEIConfig != null) {
			try {
				Object enabled = TabRegistry.clazzJEIConfig.getMethod("isOverlayEnabled", new Class[0]).invoke(null);
				if (enabled instanceof Boolean) {
					if (!(boolean) enabled) {
						return 0;
					}
					return -60;
				}
			} catch (Exception e) { LogWriter.error(e); }
		}
		return 0;
	}

	public static int getPotionOffsetNEI() {
		if (TabRegistry.initWithPotion && TabRegistry.clazzNEIConfig != null) {
			try {
				Object hidden = TabRegistry.clazzNEIConfig.getMethod("isHidden", new Class[0]).invoke(null);
				Object enabled = TabRegistry.clazzNEIConfig.getMethod("isEnabled", new Class[0]).invoke(null);
				if (hidden instanceof Boolean && enabled instanceof Boolean) {
					if ((boolean) hidden || !(boolean) enabled) {
						return 0;
					}
					return -60;
				}
			} catch (Exception e) { LogWriter.error(e); }
		}
		return 0;
	}

	public static ArrayList<AbstractTab> getTabList() {
		return TabRegistry.tabList;
	}

	public static void openInventoryGui() {
		TabRegistry.mc.player.connection
				.sendPacket(new CPacketCloseWindow(TabRegistry.mc.player.openContainer.windowId));
		GuiInventory inventory = new GuiInventory(TabRegistry.mc.player);
		TabRegistry.mc.displayGuiScreen(inventory);
	}

	public static void registerTab(AbstractTab tab) {
		for (AbstractTab t : TabRegistry.tabList) {
			if (t.getClass() == tab.getClass()) {
				return;
			}
		}
		TabRegistry.tabList.add(tab);
	}

	public static void updateTabValues(int guiLeft, int guiTop, Class<?> selectedButton) {
		int count = 2;
		for (int i = 0; i < TabRegistry.tabList.size(); ++i) {
			AbstractTab t = TabRegistry.tabList.get(i);
			if (t.shouldAddToList()) {
				t.id = count;
				t.x = guiLeft + (count - 2) * 28;
				t.y = guiTop - 28;
				t.enabled = !t.getClass().equals(selectedButton);
				t.potionOffsetLast = getPotionOffsetNEI();
				++count;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof GuiInventory) {
			int guiLeft = (event.getGui().width - 176) / 2;
			int guiTop = (event.getGui().height - 166) / 2;
			guiLeft += getPotionOffset();
			updateTabValues(guiLeft, guiTop, InventoryTabVanilla.class);
			addTabsToList(event.getButtonList());
		}
	}
}
