package noppes.npcs.controllers.data;

import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;

public class EncryptData {

	public boolean tab, isClient;
	public String path, code, name;
	public ScriptContainer container;
	public IScriptHandler handler;
	
	public EncryptData(String p, String n, String c, boolean t, ScriptContainer sc, IScriptHandler h) {
		this.path = p;
		this.name = n;
		this.code = c;
		this.tab = t;
		this.isClient = h.isClient();
		this.container = sc;
		this.handler = h;
	}

}
