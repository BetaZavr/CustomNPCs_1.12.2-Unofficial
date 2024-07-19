package noppes.npcs.constants;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class CustomBlockTypes {

	public enum TreeType implements IStringSerializable {

		NORMAL(MapColor.STONE);

		private final MapColor mapColor;

		TreeType(MapColor mapColor) {
			this.mapColor = mapColor;
		}

		public MapColor getMapColor() {
			return mapColor;
		}

		@Override
		public @Nonnull String getName() {
			return name().toLowerCase();
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

}
