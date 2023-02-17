package noppes.npcs.controllers;

import noppes.npcs.api.block.IBlock;

public interface IScriptBlockHandler
extends IScriptHandler {
	
	IBlock getBlock();
	
}
