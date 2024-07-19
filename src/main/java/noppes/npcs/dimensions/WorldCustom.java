package noppes.npcs.dimensions;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nonnull;

public class WorldCustom extends WorldServer {

	private final WorldServer delegate;
	private final IBorderListener borderListener;

	public WorldCustom(WorldInfo worldInfo, MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn) {
		super(server, saveHandlerIn, worldInfo, dimensionId, profilerIn);
		this.delegate = delegate;
		this.borderListener = new IBorderListener() {
			public void onCenterChanged(@Nonnull WorldBorder border, double x, double z) {
				WorldCustom.this.getWorldBorder().setCenter(x, z);
			}

			public void onDamageAmountChanged(@Nonnull WorldBorder border, double newAmount) {
				WorldCustom.this.getWorldBorder().setDamageAmount(newAmount);
			}

			public void onDamageBufferChanged(@Nonnull WorldBorder border, double newSize) {
				WorldCustom.this.getWorldBorder().setDamageBuffer(newSize);
			}

			public void onSizeChanged(@Nonnull WorldBorder border, double newSize) {
				WorldCustom.this.getWorldBorder().setTransition(newSize);
			}

			public void onTransitionStarted(@Nonnull WorldBorder border, double oldSize, double newSize, long time) {
				WorldCustom.this.getWorldBorder().setTransition(oldSize, newSize, time);
			}

			public void onWarningDistanceChanged(@Nonnull WorldBorder border, int newDistance) {
				WorldCustom.this.getWorldBorder().setWarningDistance(newDistance);
			}

			public void onWarningTimeChanged(@Nonnull WorldBorder border, int newTime) {
				WorldCustom.this.getWorldBorder().setWarningTime(newTime);
			}

		};
		this.delegate.getWorldBorder().addListener(this.borderListener);
	}

	@Override
	public void flush() {
		super.flush();
		this.delegate.getWorldBorder().removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
	}

	public @Nonnull World init() {
		this.mapStorage = this.delegate.getMapStorage();
		this.worldScoreboard = this.delegate.getScoreboard();
		this.lootTable = this.delegate.getLootTableManager();
		String s = VillageCollection.fileNameForProvider(this.provider);
		VillageCollection villagecollection = (VillageCollection) this.perWorldStorage
				.getOrLoadData(VillageCollection.class, s);
		if (villagecollection == null) {
			this.villageCollection = new VillageCollection(this);
			this.perWorldStorage.setData(s, this.villageCollection);
		} else {
			this.villageCollection = villagecollection;
			this.villageCollection.setWorldsForAll(this);
		}
		return this;
	}

	protected void saveLevel() {
		this.perWorldStorage.saveAllData();
	}

}
