package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.schematics.Blueprint;
import noppes.npcs.schematics.BlueprintUtil;
import noppes.npcs.schematics.ISchematic;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.SchematicBlockData;

public class SchematicController {
	
	public static SchematicController Instance = new SchematicController();
	private SchematicWrapper building;
	private int buildingPercentage;
	private ICommandSender buildStarter;
	public List<String> included;
	public Map<String, SchematicWrapper> map;

	public SchematicController() {
		this.building = null;
		this.buildStarter = null;
		this.buildingPercentage = 0;
		this.included = Arrays.asList("archery_range.schematic", "bakery.schematic", "barn.schematic",
				"building_site.schematic", "chapel.schematic", "church.schematic", "gate.schematic",
				"glassworks.schematic", "guard_Tower.schematic", "guild_house.schematic", "house.schematic",
				"house_small.schematic", "inn.schematic", "library.schematic", "lighthouse.schematic", "mill.schematic",
				"observatory.schematic", "ship.schematic", "shop.schematic", "stall.schematic", "stall2.schematic",
				"stall3.schematic", "tier_house1.schematic", "tier_house2.schematic", "tier_house3.schematic",
				"tower.schematic", "wall.schematic", "wall_corner.schematic");
		this.map = Maps.<String, SchematicWrapper>newHashMap();
	}

	public void build(SchematicWrapper schem, ICommandSender sender) {
		if (this.building != null && this.building.isBuilding) {
			this.info(sender);
			return;
		}
		this.buildingPercentage = 0;
		this.building = schem;
		this.building.isBuilding = true;
		this.buildStarter = sender;
	}

	public static File getDir() {
		File schematicDir = new File(CustomNpcs.getWorldSaveDirectory(), "schematics");
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		while (saveDir.getParentFile()!=null) {
			saveDir = saveDir.getParentFile();
			if ((new File(saveDir, "config")).exists()) {
				schematicDir = new File(saveDir, "schematics");
				break;
			}
		}
		if (!schematicDir.exists()) { schematicDir.mkdir(); }
		return schematicDir;
	}

	public void info(ICommandSender sender) {
		if (this.building == null) {
			this.sendMessage(sender, "Nothing is being build");
		} else {
			this.sendMessage(sender, "Already building: " + this.building.schema.getName() + " - "
					+ this.building.getPercentage() + "%");
			if (this.buildStarter != null) {
				this.sendMessage(sender, "Build started by: " + this.buildStarter.getName());
			}
		}
	}

	public List<String> list() {
		List<String> list = new ArrayList<String>();
		list.addAll(this.included);
		for (File file : SchematicController.getDir().listFiles()) {
			String name = file.getName();
			if (name.toLowerCase().endsWith(".schematic") || name.toLowerCase().endsWith(".blueprint")) {
				list.add(name);
			}
		}
		Collections.sort(list);
		return list;
	}

	public SchematicWrapper load(String name) {
		InputStream stream = null;
		if (this.included.contains(name)) {
			stream = MinecraftServer.class.getResourceAsStream("/assets/" + CustomNpcs.MODID + "/schematics/" + name);
		}
		if (stream == null) {
			File file = new File(SchematicController.getDir(), name);
			if (!file.exists()) {
				for (File f : SchematicController.getDir().listFiles()) {
					if (f.getName().equalsIgnoreCase(name)) {
						file = f;
						break;
					}
				}
			}
			if (!file.exists()) { return null; }
			try { stream = new FileInputStream(file); }
			catch (FileNotFoundException e2) { return null; }
		}
		SchematicWrapper schemaWr = null;
		try {
			NBTTagCompound compound = CompressedStreamTools.readCompressed(stream);
			stream.close();
			if (name.toLowerCase().endsWith(".blueprint")) {
				Blueprint bp = BlueprintUtil.readBlueprintFromNBT(compound);
				bp.setName(name);
				schemaWr = new SchematicWrapper(bp);
			}
			Schematic schema = new Schematic(name);
			schema.load(compound);
			schemaWr = new SchematicWrapper(schema);
		} catch (IOException e) {
			LogWriter.except(e);
		}
		if (schemaWr!=null) { this.map.put(name.toLowerCase(), schemaWr); }
		return schemaWr;
	}

	public void save(ICommandSender sender, String name, int type, BlockPos pos, short height, short width, short length) {
		name = name.replace(" ", "_");
		if (this.included.contains(name)) {
			return;
		}
		World world = sender.getEntityWorld();
		File file = null;
		ISchematic schema = null;
		if (type == 0) {
			file = new File(SchematicController.getDir(), name + ".schematic");
			schema = Schematic.Create(world, name, pos, height, width, length);
		} else if (type == 1) {
			file = new File(SchematicController.getDir(), name + ".blueprint");
			schema = BlueprintUtil.createBlueprint(world, pos, width, length, height);
		}
		NoppesUtilServer.NotifyOPs("Schematic " + name + " succesfully created", new Object[0]);
		try {
			CompressedStreamTools.writeCompressed(schema.getNBT(), (OutputStream) new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(ICommandSender sender, String message) {
		if (sender == null) {
			return;
		}
		sender.sendMessage(new TextComponentString(message));
	}

	public void stop(ICommandSender sender) {
		if (this.building == null || !this.building.isBuilding) {
			this.sendMessage(sender, "Not building");
		} else {
			this.sendMessage(sender, "Stopped building: " + this.building.schema.getName());
			this.building = null;
		}
	}

	public void updateBuilding() {
		if (this.building == null) { return; }
		this.building.build();
		if (this.buildStarter != null && this.building.getPercentage() - this.buildingPercentage >= 10) {
			this.sendMessage(this.buildStarter, "Building at " + this.building.getPercentage() + "%");
			this.buildingPercentage = this.building.getPercentage();
		}
		if (!this.building.isBuilding) {
			if (this.buildStarter != null) {
				this.sendMessage(this.buildStarter, "Building finished");
			}
			this.building = null;
		}
	}

	public SchematicWrapper getSchema(String name) {
		if (!this.map.containsKey(name.toLowerCase())) { this.load(name.toLowerCase()); }
		return this.map.get(name.toLowerCase());
	}

	@SuppressWarnings("deprecation")
	public static void buildBlocks(EntityPlayerMP player, BlockPos pos, int rotaion, Schematic schema) { // Schematica Build
//System.out.println("name: "+schema.getName()+"; pos: "+pos+"; rotaion: "+rotaion+"; schema: "+schema);
		if (pos==null || schema==null) { return; }
		SchematicBlockData[][][] blocks = new SchematicBlockData[schema.height][schema.width][schema.length];
		SchematicBlockData[][][] tempB = new SchematicBlockData[schema.height][schema.width][schema.length];
		NBTTagList entitys = schema.entityList;
		int cx, cy, cz;
		for (int i=0, t=0; i<schema.blockIdsArray.length; i++) {
			cy = (int) Math.floor(i/(schema.length*schema.width));
			cz = (int) Math.floor((i/schema.width)%schema.length);
			cx = (int) Math.floor(i%schema.width);
			Block b = Block.getBlockById((int) schema.blockIdsArray[i]);
			IBlockState state = b.getStateFromMeta((int) schema.blockMetadataArray[i]);
			blocks[cy][cz][cx] = new SchematicBlockData(null, state, new BlockPos(cx+schema.offset[0],cy+schema.offset[1],cz+schema.offset[2]));
			blocks[cy][cz][cx].nbtTile = null;
			if (b instanceof ITileEntityProvider) {
				blocks[cy][cz][cx].nbtTile = schema.entityList.getCompoundTagAt(t);
				t++;
			}
			tempB[cy][cz][cx] = new SchematicBlockData(blocks[cy][cz][cx]);
		}
		if (rotaion!=0) {
			
		}
		NBTTagCompound[] tempE = new NBTTagCompound[entitys.tagCount()];
		for (int i=0; i<schema.entityList.tagCount(); i++) { tempE[i] = schema.entityList.getCompoundTagAt(i); }
		
		
	}

	public static byte rotate(IBlockState state, int rot) {
		Block block = state.getBlock();
		String name = block.getRegistryName().toString();
		int meta = block.getMetaFromState(state)%4;
		int amn = (int) Math.floor(block.getMetaFromState(state)/4.0d);
		if (block instanceof BlockStairs) {
			while (rot!=0) {
				if (meta==0) { meta = 2 + amn * 4; }
				else if (meta==1) { meta = 3 + amn * 4; }
				else if (meta==2) { meta = 1 + amn * 4; }
				else if (meta==3) { meta = amn * 4; }
				rot--;
			}
		}
		else if (block instanceof BlockBanner || block instanceof BlockSign) {
			meta = block.getMetaFromState(state) % 16;
			while (rot!=0) {
				meta = meta % 16;
				if (meta<=12) { meta = 4+meta; } else { meta = 4+meta-16; }
				rot--;
			}
		}
		else if (block instanceof BlockLog) {
			while (rot!=0) {
				if (amn==1) { meta = 8+meta; }
				else if (amn==2) { meta = 4+meta; }
				rot--;
			}
		}
		else if (block instanceof BlockRail) {
			while (rot!=0) {
				if (meta==0) { meta=1; }
				else if (meta==1) { meta=0; }
				else if (name.indexOf('_')!=(-1)) {
					if ( meta == 8) {meta = 9; }
					else if ( meta == 9) {meta = 8; }
				}
				else {
					if (meta<9) { meta = 1+meta; } else {meta = 6; }
				}
				rot--;
			}
		}
		while (rot!=0) {
			rot--;
		}
		
		return (byte) meta;
	}

}
