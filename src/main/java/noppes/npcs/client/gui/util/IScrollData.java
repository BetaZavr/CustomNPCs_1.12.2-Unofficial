package noppes.npcs.client.gui.util;

import java.util.HashMap;
import java.util.Vector;

public interface IScrollData {
	
	void setData(Vector<String> list, HashMap<String, Integer> data);

	void setSelected(String select);

}
