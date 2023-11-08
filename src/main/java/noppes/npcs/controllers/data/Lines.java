package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Lines {
	
	private static Random random = new Random();
	private int lastLine;
	public Map<Integer, Line> lines;

	public Lines() {
		this.lastLine = -1;
		this.lines = Maps.<Integer, Line>newTreeMap();
	}

	public Line getLine(boolean isRandom) {
		if (this.lines.isEmpty()) {
			return null;
		}
		if (isRandom) {
			int i = -1;
			while (i==-1 && i!=this.lastLine) { i = Lines.random.nextInt(this.lines.size()); }
			for (Map.Entry<Integer, Line> e : this.lines.entrySet()) {
				if (--i < 0) {
					this.lastLine = e.getKey();
					return e.getValue().copy();
				}
			}
		}
		++this.lastLine;
		Line line;
		while (true) {
			this.lastLine %= 8;
			line = this.lines.get(this.lastLine);
			if (line != null) {
				break;
			}
			++this.lastLine;
		}
		return line.copy();
	}

	public boolean isEmpty() {
		return this.lines.isEmpty();
	}

	public void remove(int pos) {
		if (!this.lines.containsKey(pos)) { return; }
		this.lines.remove(pos);
		this.correctLines();
	}
	
	public void correctLines() {
		Map<Integer, Line> newLines = Maps.<Integer, Line>newTreeMap();
		int i = 0;
		boolean isChanged = false;
		for (int pos : this.lines.keySet()) {
			if (pos!=i) { isChanged = true; }
			Line line = this.lines.get(pos);
			if (line.getText().isEmpty()) {
				isChanged = true;
				continue;
			}
			newLines.put(i, line);
			i++;
		}
		if (isChanged) { this.lines = newLines; }
	}

	public void readNBT(NBTTagCompound compound) {
		NBTTagList nbttaglist = compound.getTagList("Lines", 10);
		HashMap<Integer, Line> map = new HashMap<Integer, Line>();
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			Line line = new Line();
			line.setText(nbttagcompound.getString("Line"));
			line.setSound(nbttagcompound.getString("Song"));
			map.put(nbttagcompound.getInteger("Slot"), line);
		}
		this.lines = map;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (int slot : this.lines.keySet()) {
			Line line = this.lines.get(slot);
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
