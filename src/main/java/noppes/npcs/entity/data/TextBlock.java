package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.ITextComponent;

public class TextBlock {

	public List<ITextComponent> lines;

	public TextBlock() {
		this.lines = new ArrayList<ITextComponent>();
	}

}
