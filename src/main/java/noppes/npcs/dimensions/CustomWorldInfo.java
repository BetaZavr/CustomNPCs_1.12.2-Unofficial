package noppes.npcs.dimensions;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IWorldInfo;

import javax.annotation.Nonnull;

public class CustomWorldInfo extends WorldInfo implements IWorldInfo {

	WorldInfo superInfo;
	public int id = 100;
	public String versionName;
	public int versionId;
	public boolean versionSnapshot;
	public long randomSeed;
	public WorldType terrainType = WorldType.DEFAULT;
	public String generatorOptions = "";
	public int spawnX;
	public int spawnY;
	public int spawnZ;
	public long totalTime;
	public long worldTime;
	public long lastTimePlayed;
	public long sizeOnDisk;
	public NBTTagCompound playerTag;
	public int dimension;
	public String levelName;
	public int saveVersion;
	public int cleanWeatherTime;
	public boolean raining;
	public int rainTime;
	public boolean thundering;
	public int thunderTime;
	public GameType gameType;
	public boolean mapFeaturesEnabled;
	public boolean hardcore;
	public boolean allowCommands;
	public boolean initialized;
	public EnumDifficulty difficulty;
	public boolean difficultyLocked;
	public double borderCenterX;
	public double borderCenterZ;
	public double borderSize = 6.0E7D;
	public long borderSizeLerpTime;
	public double borderSizeLerpTarget;
	public double borderSafeZone = 5.0D;
	public double borderDamagePerBlock = 0.2D;
	public int borderWarningDistance = 5;
	public int borderWarningTime = 15;
	public final Map<Integer, NBTTagCompound> dimensionData = Maps.newHashMap();
	public GameRules gameRules = new GameRules();

	public CustomWorldInfo(NBTTagCompound nbt) {
		super(nbt);
		this.superInfo = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
	}

	public CustomWorldInfo(WorldSettings settings, String name) {
		super(settings, name);
	}

	@Override
	public boolean areCommandsAllowed() {
		return superInfo.areCommandsAllowed();
	}

	@Override
	public @Nonnull EnumDifficulty getDifficulty() {
		return superInfo.getDifficulty();
	}

	@Override
	public @Nonnull GameRules getGameRulesInstance() {
		return superInfo.getGameRulesInstance();
	}

	@Override
	public @Nonnull GameType getGameType() {
		return superInfo.getGameType();
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public long getLastTimePlayed() {
		return superInfo.getLastTimePlayed();
	}

	@Override
	public INbt getNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.read());
	}

	@Override
	public @Nonnull NBTTagCompound getPlayerNBTTagCompound() {
		return superInfo.getPlayerNBTTagCompound();
	}

	@Override
	public boolean isDifficultyLocked() {
		return superInfo.isDifficultyLocked();
	}

	@Override
	public boolean isHardcoreModeEnabled() {
		return superInfo.isHardcoreModeEnabled();
	}

	public void load(NBTTagCompound nbt) {
		if (nbt.hasKey("Version", 10)) {
			NBTTagCompound nbttagcompound = nbt.getCompoundTag("Version");
			this.versionName = nbttagcompound.getString("Name");
			this.versionId = nbttagcompound.getInteger("Id");
			this.versionSnapshot = nbttagcompound.getBoolean("Snapshot");
		}

		this.randomSeed = nbt.getLong("RandomSeed");

		if (nbt.hasKey("generatorName", 8)) {
			String s1 = nbt.getString("generatorName");
			this.terrainType = WorldType.parseWorldType(s1);
            if (this.terrainType.isVersioned()) {
                int i = 0;
                if (nbt.hasKey("generatorVersion", 99)) {
                    i = nbt.getInteger("generatorVersion");
                }
                this.terrainType = this.terrainType.getWorldTypeForGeneratorVersion(i);
            }
			if (nbt.hasKey("generatorOptions", 8)) {
				this.generatorOptions = nbt.getString("generatorOptions");
			}
		}

		this.gameType = GameType.getByID(nbt.getInteger("GameType"));
		if (nbt.hasKey("MapFeatures", 99)) {
			this.mapFeaturesEnabled = nbt.getBoolean("MapFeatures");
		} else {
			this.mapFeaturesEnabled = true;
		}
		this.spawnX = nbt.getInteger("SpawnX");
		this.spawnY = nbt.getInteger("SpawnY");
		this.spawnZ = nbt.getInteger("SpawnZ");
		this.totalTime = nbt.getLong("Time");
		if (nbt.hasKey("DayTime", 99)) {
			this.worldTime = nbt.getLong("DayTime");
		} else {
			this.worldTime = this.totalTime;
		}
		this.lastTimePlayed = nbt.getLong("LastPlayed");
		this.sizeOnDisk = nbt.getLong("SizeOnDisk");
		this.levelName = nbt.getString("LevelName");
		this.saveVersion = nbt.getInteger("version");
		this.cleanWeatherTime = nbt.getInteger("clearWeatherTime");
		this.rainTime = nbt.getInteger("rainTime");
		this.raining = nbt.getBoolean("raining");
		this.thunderTime = nbt.getInteger("thunderTime");
		this.thundering = nbt.getBoolean("thundering");
		this.hardcore = nbt.getBoolean("hardcore");
		if (nbt.hasKey("initialized", 99)) {
			this.initialized = nbt.getBoolean("initialized");
		} else {
			this.initialized = true;
		}

		if (nbt.hasKey("allowCommands", 99)) {
			this.allowCommands = nbt.getBoolean("allowCommands");
		} else {
			this.allowCommands = this.gameType == GameType.CREATIVE;
		}

		if (nbt.hasKey("Player", 10)) {
			this.playerTag = nbt.getCompoundTag("Player");
			this.dimension = this.playerTag.getInteger("Dimension");
		}

		if (nbt.hasKey("GameRules", 10)) {
			this.gameRules.readFromNBT(nbt.getCompoundTag("GameRules"));
		}

		if (nbt.hasKey("Difficulty", 99)) {
			this.difficulty = EnumDifficulty.getDifficultyEnum(nbt.getByte("Difficulty"));
		}

		if (nbt.hasKey("DifficultyLocked", 1)) {
			this.difficultyLocked = nbt.getBoolean("DifficultyLocked");
		}

		if (nbt.hasKey("BorderCenterX", 99)) {
			this.borderCenterX = nbt.getDouble("BorderCenterX");
		}

		if (nbt.hasKey("BorderCenterZ", 99)) {
			this.borderCenterZ = nbt.getDouble("BorderCenterZ");
		}

		if (nbt.hasKey("BorderSize", 99)) {
			this.borderSize = nbt.getDouble("BorderSize");
		}

		if (nbt.hasKey("BorderSizeLerpTime", 99)) {
			this.borderSizeLerpTime = nbt.getLong("BorderSizeLerpTime");
		}

		if (nbt.hasKey("BorderSizeLerpTarget", 99)) {
			this.borderSizeLerpTarget = nbt.getDouble("BorderSizeLerpTarget");
		}

		if (nbt.hasKey("BorderSafeZone", 99)) {
			this.borderSafeZone = nbt.getDouble("BorderSafeZone");
		}

		if (nbt.hasKey("BorderDamagePerBlock", 99)) {
			this.borderDamagePerBlock = nbt.getDouble("BorderDamagePerBlock");
		}

		if (nbt.hasKey("BorderWarningBlocks", 99)) {
			this.borderWarningDistance = nbt.getInteger("BorderWarningBlocks");
		}

		if (nbt.hasKey("BorderWarningTime", 99)) {
			this.borderWarningTime = nbt.getInteger("BorderWarningTime");
		}

		if (nbt.hasKey("DimensionData", 10)) {
			NBTTagCompound compound = nbt.getCompoundTag("DimensionData");
			for (String s : compound.getKeySet()) {
				this.dimensionData.put(Integer.parseInt(s), compound.getCompoundTag(s));
			}
		}
	}

	public NBTTagCompound read() {
		NBTTagCompound nbt = this.cloneNBTCompound(null);
		nbt.getCompoundTag("Version").setInteger("Id", this.id);
		nbt.getCompoundTag("Version").setBoolean("Snapshot", true);
		return nbt;
	}

	@Override
	public void setNbt(INbt inbt) {
		this.load(inbt.getMCNBT());
	}
}
