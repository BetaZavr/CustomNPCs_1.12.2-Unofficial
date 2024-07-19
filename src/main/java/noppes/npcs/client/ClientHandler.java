package noppes.npcs.client;

import java.util.TreeSet;

import com.google.common.collect.Sets;

import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;

public class ClientHandler {

	static ClientHandler instance;

	public static ClientHandler getInstance() {
		if (ClientHandler.instance == null) {
			ClientHandler.instance = new ClientHandler();
		}
		return ClientHandler.instance;
	}

	TreeSet<Integer> customDimensions;

	public ClientHandler() {
		this.customDimensions = Sets.newTreeSet();
	}

	public void cleanUp() {
		for (int id : this.customDimensions) {
			if (DimensionManager.isDimensionRegistered(id)) {
				DimensionManager.unregisterDimension(id);
			}
		}
	}

	public boolean has(int dimensionId) {
		if (dimensionId < 100) {
			return false;
		}
		return this.customDimensions.contains(dimensionId);
	}

	public void sync(int[] dimensions) {
		this.cleanUp();
		this.customDimensions.clear();
        for (int id : dimensions) {
            this.customDimensions.add(id);
            if (!DimensionManager.isDimensionRegistered(id)) {
                DimensionManager.registerDimension(id, CustomNpcs.customDimensionType);
            }
        }
	}

}
