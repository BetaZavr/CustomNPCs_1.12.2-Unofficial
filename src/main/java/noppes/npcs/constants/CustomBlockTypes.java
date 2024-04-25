package noppes.npcs.constants;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class CustomBlockTypes {

	public static enum TreeType implements IStringSerializable {

		NORMAL(MapColor.STONE);

		private final MapColor mapColor;

		TreeType(MapColor mapColor) {
			this.mapColor = mapColor;
		}

		public MapColor getMapColor() {
			return mapColor;
		}

		@Override
		public String getName() {
			return name().toLowerCase();
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public static final PropertyEnum<TreeType> TREE_TYPE = PropertyEnum.create("type", TreeType.class);

}
