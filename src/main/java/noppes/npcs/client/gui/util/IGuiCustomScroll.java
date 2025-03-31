package noppes.npcs.client.gui.util;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface IGuiCustomScroll extends IComponentGui {

    void canSearch(boolean setSearch);

    void clear();

    int getColor(int pos);

    List<String> getList();

    String getSelected();

    HashSet<String> getSelectedList();

    boolean hasSelected();

    void resetRoll();

    void scrollTo(String name);

    void setColors(List<Integer> newColors);

    void setList(List<String> newList);

    void setListNotSorted(List<String> newList);

    void setPrefixes(List<IResourceData> newPrefixes);

    boolean hasSelected(String name);

    void setSelected(String name);

    void setSelectedList(HashSet<String> newSelectedList);

    void setSize(int w, int h);

    void setStacks(List<ItemStack> newStacks);

    void setSuffixes(List<String> newSuffixes);

    IGuiCustomScroll setUnSelectable();

    void setHoverTexts(LinkedHashMap<Integer, List<String>> map);

    @Nonnull
    Map<Integer, List<String>> getHoversTexts();

    @Nonnull List<String> getHoversText();

    void setParent(ICustomScrollListener gui);

    int getSelect();

    void setSelect(int slotIndex);

}
