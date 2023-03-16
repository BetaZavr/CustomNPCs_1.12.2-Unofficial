package noppes.npcs.client;

import java.util.TreeSet;

import com.google.common.collect.Sets;

import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;

public class ClientHandler {
	
	static ClientHandler instance;

	TreeSet<Integer> customDimensions;

	public ClientHandler() { this.customDimensions = Sets.<Integer>newTreeSet(); }

	public static ClientHandler getInstance() {
		if (ClientHandler.instance == null) { ClientHandler.instance = new ClientHandler(); }
		return ClientHandler.instance;
	}

	public void cleanUp() {
		for (int id : this.customDimensions) {
			if (DimensionManager.isDimensionRegistered(id)) { DimensionManager.unregisterDimension(id); }
		}
	}

	public void sync(int[] dimensions) {
		this.cleanUp();
		this.customDimensions.clear();
		for (int i = 0; i < dimensions.length; i++) {
			int id = dimensions[i];
			this.customDimensions.add(id);
			if (!DimensionManager.isDimensionRegistered(id)) {
				DimensionManager.registerDimension(id, CustomNpcs.customDimensionType);
			}
		}
	}

	public boolean has(int dimensionId) {
		if (dimensionId<100) { return false; }
		return this.customDimensions.contains(dimensionId);
	}
	
}
