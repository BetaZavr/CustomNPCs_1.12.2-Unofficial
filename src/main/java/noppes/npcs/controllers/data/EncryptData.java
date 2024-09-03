package noppes.npcs.controllers.data;

import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;

import java.io.File;

public class EncryptData {

	public boolean tab; // only the tab script or all modules
	public boolean isClient; // client scripts
	public File path; // module file
	public String code; // script code
	public String name; // file name
	public ScriptContainer container; // parent container
	public IScriptHandler handler; // parent handler

	public EncryptData(String p, String n, String c, boolean t, ScriptContainer sc, IScriptHandler h) {
		this.path = new File(p);
		if (this.path.getAbsolutePath().contains("\\.\\")) { this.path = new File(this.path.getAbsolutePath().replace("\\.\\", "\\")); }
		this.name = n;
		this.code = c;
		this.tab = t;
		this.isClient = h.isClient();
		this.container = sc;
		this.handler = h;
	}

}
