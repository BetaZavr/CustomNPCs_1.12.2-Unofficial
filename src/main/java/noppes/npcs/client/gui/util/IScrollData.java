package noppes.npcs.client.gui.util;

import java.util.HashMap;
import java.util.Vector;

public interface IScrollData {

	void setData(Vector<String> dataList, HashMap<String, Integer> dataMap);

	void setSelected(String select);

}
