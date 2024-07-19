package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import noppes.npcs.LogWriter;

public class VersionChecker extends Thread {

	@Override
	public void run() {
		EntityPlayer player;
		try { player = Minecraft.getMinecraft().player; }
		catch (Exception e) { return; }
		while (player == null) {
			try {
				player = Minecraft.getMinecraft().player;
				if (player == null) { Thread.sleep(2000L); }
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
		}
		TextComponentTranslation mes = new TextComponentTranslation("cnpcs.version");
		TextComponentTranslation link = new TextComponentTranslation("click.here");
		link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.kodevelopment.nl/minecraft/customnpcs/"));
		player.sendMessage(mes.appendSibling(link));

		mes = new TextComponentTranslation("cnpcs.scripters");
		link = new TextComponentTranslation(((char) 167) + "9" + ((char) 167) + "nDiscord");
		link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/rgczwNV"));
		player.sendMessage(mes.appendSibling(link));
	}
}
