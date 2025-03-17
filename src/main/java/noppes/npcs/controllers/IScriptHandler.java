package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraftforge.fml.common.eventhandler.Event;

public interface IScriptHandler {

	void clearConsole();

	void clearConsoleText(Long key);

	TreeMap<Long, String> getConsoleText();

	boolean getEnabled();

	String getLanguage();

	List<ScriptContainer> getScripts();

	boolean isClient();

	String noticeString(String type, Object event);

	void runScript(String type, Event event);

	void setEnabled(boolean bo);

	void setLanguage(String language);

	void setLastInited(long timeMC);

}
