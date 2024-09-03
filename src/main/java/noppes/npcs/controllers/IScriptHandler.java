package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;

public interface IScriptHandler {

	void clearConsole();

	Map<Long, String> getConsoleText();

	boolean getEnabled();

	String getLanguage();

	List<ScriptContainer> getScripts();

	boolean isClient();

	String noticeString();

	void runScript(String type, Event event);

	void setEnabled(boolean bo);

	void setLanguage(String language);

	void setLastInited(long timeMC);

}
