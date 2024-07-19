package noppes.npcs.schematics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class BlueprintUtil {
	private static int[] convertBlocksToSaveData(short[][][] multiDimArray, short sizeX, short sizeY, short sizeZ) {
		short[] oneDimArray = new short[sizeX * sizeY * sizeZ];
		int j = 0;
		for (short y = 0; y < sizeY; ++y) {
			for (short z = 0; z < sizeZ; ++z) {
				for (short x = 0; x < sizeX; ++x) {
					oneDimArray[j++] = multiDimArray[y][z][x];
				}
			}
		}
		int[] ints = new int[(int) Math.ceil(oneDimArray.length / 2.0f)];
		int currentInt;
		for (int i = 1; i < oneDimArray.length; i += 2) {
			currentInt = oneDimArray[i - 1];
			currentInt = (currentInt << 16 | oneDimArray[i]);
			ints[(int) (Math.ceil(i / 2.0f) - 1)] = currentInt;
        }
		if (oneDimArray.length % 2 == 1) {
			currentInt = oneDimArray[oneDimArray.length - 1] << 16;
			ints[ints.length - 1] = currentInt;
		}
		return ints;
	}

	public static short[][][] convertSaveDataToBlocks(int[] ints, short sizeX, short sizeY, short sizeZ) {
		short[] oneDimArray = new short[ints.length * 2];
		for (int i = 0; i < ints.length; ++i) {
			oneDimArray[i * 2] = (short) (ints[i] >> 16);
			oneDimArray[i * 2 + 1] = (short) ints[i];
		}
		short[][][] multiDimArray = new short[sizeY][sizeZ][sizeX];
		int j = 0;
		for (short y = 0; y < sizeY; ++y) {
			for (short z = 0; z < sizeZ; ++z) {
				for (short x = 0; x < sizeX; ++x) {
					multiDimArray[y][z][x] = oneDimArray[j++];
				}
			}
		}
		return multiDimArray;
	}

	public static Blueprint createBlueprint(World world, BlockPos pos, short sizeX, short sizeY, short sizeZ) {
		return createBlueprint(world, pos, sizeX, sizeY, sizeZ, null);
	}

	public static Blueprint createBlueprint(World world, BlockPos pos, short sizeX, short sizeY, short sizeZ,
			String name, String... architects) {
		List<IBlockState> palette = new ArrayList<>();
		short[][][] structure = new short[sizeY][sizeZ][sizeX];
		List<NBTTagCompound> tileEntities = new ArrayList<>();
		List<String> requiredMods = new ArrayList<>();
		for (short y = 0; y < sizeY; ++y) {
			for (short z = 0; z < sizeZ; ++z) {
				for (short x = 0; x < sizeX; ++x) {
					IBlockState state = world.getBlockState(pos.add(x, y, z));
					String modName;
					if (!requiredMods.contains(modName = Objects.requireNonNull(state.getBlock().getRegistryName()).getResourceDomain())) {
						requiredMods.add(modName);
					}
					TileEntity te = world.getTileEntity(pos.add(x, y, z));
					if (te != null) {
						NBTTagCompound teTag = te.serializeNBT();
						teTag.setShort("x", x);
						teTag.setShort("y", y);
						teTag.setShort("z", z);
						tileEntities.add(teTag);
					}
					if (!palette.contains(state)) {
						palette.add(state);
					}
					structure[y][z][x] = (short) palette.indexOf(state);
				}
			}
		}
		IBlockState[] states = new IBlockState[palette.size()];
		states = palette.toArray(states);
		NBTTagCompound[] tes = new NBTTagCompound[tileEntities.size()];
		tes = tileEntities.toArray(tes);
		Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (byte) palette.size(), states, structure, tes,
				requiredMods);
		if (name != null) {
			schem.setName(name);
		}
		if (architects != null) {
			schem.setArchitects(architects);
		}
		return schem;
	}

	public static Blueprint readBlueprintFromNBT(NBTTagCompound tag) {
		byte version = tag.getByte("version");
		if (version == 1) {
			short sizeX = tag.getShort("size_x");
			short sizeY = tag.getShort("size_y");
			short sizeZ = tag.getShort("size_z");
			List<String> requiredMods = new ArrayList<>();
			NBTTagList modsList = (NBTTagList) tag.getTag("required_mods");
			short modListSize = (short) modsList.tagCount();
			for (int i = 0; i < modListSize; ++i) {
				requiredMods.add(((NBTTagString) modsList.get(i)).getString());
				if (!Loader.isModLoaded(requiredMods.get(i))) {
					Logger.getGlobal().log(Level.WARNING,
							"Couldn't load Blueprint, the following mod is missing: " + requiredMods.get(i));
					return null;
				}
			}
			NBTTagList paletteTag = (NBTTagList) tag.getTag("palette");
			short paletteSize = (short) paletteTag.tagCount();
			IBlockState[] palette = new IBlockState[paletteSize];
			for (short j = 0; j < palette.length; ++j) {
				palette[j] = NBTUtil.readBlockState(paletteTag.getCompoundTagAt(j));
			}
			short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);
			NBTTagList teTag = (NBTTagList) tag.getTag("tile_entities");
			NBTTagCompound[] tileEntities = new NBTTagCompound[teTag.tagCount()];
			for (short k = 0; k < tileEntities.length; ++k) {
				tileEntities[k] = teTag.getCompoundTagAt(k);
			}
			Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, paletteSize, palette, blocks, tileEntities,
					requiredMods);
			if (tag.hasKey("name")) {
				schem.setName(tag.getString("name"));
			}
			if (tag.hasKey("architects")) {
				NBTTagList architectsTag = (NBTTagList) tag.getTag("architects");
				String[] architects = new String[architectsTag.tagCount()];
				for (int l = 0; l < architectsTag.tagCount(); ++l) {
					architects[l] = architectsTag.getStringTagAt(l);
				}
				schem.setArchitects(architects);
			}
			return schem;
		}
		return null;
	}

	public static NBTTagCompound writeBlueprintToNBT(Blueprint schem) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("version", (byte) 1);
		tag.setShort("size_x", schem.getSizeX());
		tag.setShort("size_y", schem.getSizeY());
		tag.setShort("size_z", schem.getSizeZ());
		IBlockState[] palette = schem.getPalette();
		NBTTagList paletteTag = new NBTTagList();
		for (short i = 0; i < schem.getPaletteSize(); ++i) {
			NBTTagCompound state = new NBTTagCompound();
			NBTUtil.writeBlockState(state, palette[i]);
			paletteTag.appendTag(state);
		}
		tag.setTag("palette", paletteTag);
		int[] blockInt = convertBlocksToSaveData(schem.getStructure(), schem.getSizeX(), schem.getSizeY(),
				schem.getSizeZ());
		tag.setIntArray("blocks", blockInt);
		NBTTagList finishedTes = new NBTTagList();
		NBTTagCompound[] tes = schem.getTileEntities();
        for (NBTTagCompound te : tes) {
            finishedTes.appendTag(te);
        }
		tag.setTag("tile_entities", finishedTes);
		List<String> requiredMods = schem.getRequiredMods();
		NBTTagList modsList = new NBTTagList();
        for (String requiredMod : requiredMods) {
            modsList.appendTag(new NBTTagString(requiredMod));
        }
		tag.setTag("required_mods", modsList);
		String name = schem.getName();
		String[] architects = schem.getArchitects();
		if (name != null) {
			tag.setString("name", name);
		}
		if (architects != null) {
			NBTTagList architectsTag = new NBTTagList();
			for (String architect : architects) {
				architectsTag.appendTag(new NBTTagString(architect));
			}
			tag.setTag("architects", architectsTag);
		}
		return tag;
	}

}
