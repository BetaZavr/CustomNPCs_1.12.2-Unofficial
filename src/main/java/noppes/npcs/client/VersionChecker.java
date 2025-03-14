package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class VersionChecker extends Thread {

	@Override
	public void run() {
		TextComponentTranslation messageVersion = new TextComponentTranslation("cnpcs.version");
		TextComponentTranslation linkVersion = new TextComponentTranslation("click.here");
		linkVersion.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.kodevelopment.nl/minecraft/customnpcs/"));

		TextComponentTranslation messageScripters = new TextComponentTranslation("cnpcs.scripters");
		TextComponentTranslation linkScripters = new TextComponentTranslation(((char) 167) + "9" + ((char) 167) + "nDiscord");
		linkScripters.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/rgczwNV"));

		while(true) {
			if (Minecraft.getMinecraft().player != null) {
				Minecraft.getMinecraft().player.sendMessage(messageVersion.appendSibling(linkVersion));
				Minecraft.getMinecraft().player.sendMessage(messageScripters.appendSibling(linkScripters));
				break;
			}
		}
	}
}
