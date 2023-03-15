package noppes.npcs.api.wrapper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.item.IItemBook;

public class ItemBookWrapper
extends ItemStackWrapper
implements IItemBook {
	
	protected ItemBookWrapper(ItemStack item) {
		super(item);
	}

	@Override
	public String getAuthor() {
		return this.getTag().getString("author");
	}

	private NBTTagCompound getTag() {
		NBTTagCompound comp = this.item.getTagCompound();
		if (comp == null) {
			this.item.setTagCompound(comp = new NBTTagCompound());
		}
		return comp;
	}

	@Override
	public String[] getText() {
		List<String> list = new ArrayList<String>();
		NBTTagList pages = this.getTag().getTagList("pages", 8);
		for (int i = 0; i < pages.tagCount(); ++i) {
			list.add(pages.getStringTagAt(i));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String getTitle() {
		return this.getTag().getString("title");
	}

	@Override
	public int getType() {
		return 1;
	}

	@Override
	public boolean isBook() {
		return true;
	}

	@Override
	public void setAuthor(String author) {
		this.getTag().setString("author", author);
	}

	@Override
	public void setText(String[] pages) {
		NBTTagList list = new NBTTagList();
		if (pages != null && pages.length > 0) {
			for (String page : pages) {
				list.appendTag(new NBTTagString(page));
			}
		}
		this.getTag().setTag("pages", list);
	}

	@Override
	public void setTitle(String title) {
		this.getTag().setString("title", title);
	}
}
