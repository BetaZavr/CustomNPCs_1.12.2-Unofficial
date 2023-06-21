package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;

public interface IScriptHandler {
	
	void clearConsole();

	Map<Long, String> getConsoleText();

	boolean getEnabled();

	String getLanguage();

	List<ScriptContainer> getScripts();

	boolean isClient();

	String noticeString();

	void runScript(EnumScriptType type, Event event);

	void setEnabled(boolean bo);

	void setLanguage(String language);
	
}
