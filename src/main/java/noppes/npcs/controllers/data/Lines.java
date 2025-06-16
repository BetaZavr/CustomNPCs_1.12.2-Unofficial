package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Lines {

	private static final Random random = new Random();
	private int lastLine = -1;
	public Map<Integer, Line> lines = new TreeMap<>();

	public Lines copy() {
		Lines newLines = new Lines();
		for (int i : lines.keySet()) {
			newLines.lines.put(i, lines.get(i));
		}
		return newLines;
	}

	public void correctLines() {
		Map<Integer, Line> newLines = new TreeMap<>();
		int i = 0;
		boolean isChanged = false;
		for (int pos : this.lines.keySet()) {
			if (pos != i) {
				isChanged = true;
			}
			Line line = this.lines.get(pos);
			if (line.getText().isEmpty()) {
				isChanged = true;
				continue;
			}
			newLines.put(i, line);
			i++;
		}
		if (isChanged) {
			this.lines = newLines;
		}
	}

	public Line getLine(boolean isRandom) {
		if (lines.isEmpty()) { return null; }
		if (isRandom) {
			int i = lastLine;
			if (lines.size() == 1) { i = 0; }
			else {
				while (i == lastLine) { i = Lines.random.nextInt(lines.size()); }
			}
			if (lines.containsKey(i)) {
				lastLine = i;
				return lines.get(i).copy();
			}
			for (Map.Entry<Integer, Line> e : lines.entrySet()) {
				if (--i < 0) {
					lastLine = e.getKey();
					return e.getValue().copy();
				}
			}
		}
		++lastLine;
		Line line;
		while (true) {
			lastLine %= lines.size();
			line = lines.get(lastLine);
			if (line != null) {
				break;
			}
			++lastLine;
		}
		return line.copy();
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}

	public void readNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = compound.getTagList("Lines", 10);
		HashMap<Integer, Line> map = new HashMap<>();
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			Line line = new Line();
			line.setText(nbttagcompound.getString("Line"));
			line.setSound(nbttagcompound.getString("Song"));
			map.put(nbttagcompound.getInteger("Slot"), line);
		}
		lines = map;
	}

	public void remove(int pos) {
		if (!lines.containsKey(pos)) {
			return;
		}
		lines.remove(pos);
		correctLines();
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (int slot : lines.keySet()) {
			Line line = lines.get(slot);
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("Slot", slot);
			tags.setString("Line", line.getText());
			tags.setString("Song", line.getSound());
			list.appendTag(tags);
		}
		nbt.setTag("Lines", list);
		return nbt;
	}

}
