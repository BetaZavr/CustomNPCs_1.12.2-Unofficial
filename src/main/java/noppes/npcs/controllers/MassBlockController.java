package noppes.npcs.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;

public class MassBlockController {
	public interface IMassBlock {
		EntityNPCInterface getNpc();

		int getRange();

		void processed(List<BlockData> p0);
	}

	public static MassBlockController Instance;

	public static void Queue(IMassBlock imb) {
		MassBlockController.Instance.queue.add(imb);
	}

	public static void Update() {
		if (MassBlockController.Instance.queue.isEmpty()) {
			return;
		}
		IMassBlock imb = MassBlockController.Instance.queue.remove();
		World world = imb.getNpc().world;
		BlockPos pos = imb.getNpc().getPosition();
		int range = imb.getRange();
		List<BlockData> list = new ArrayList<BlockData>();
		for (int x = -range; x < range; ++x) {
			for (int z = -range; z < range; ++z) {
				if (world.isBlockLoaded(new BlockPos(x + pos.getX(), 64, z + pos.getZ()))) {
					for (int y = 0; y < range; ++y) {
						BlockPos blockPos = pos.add(x, y - range / 2, z);
						list.add(new BlockData(blockPos, world.getBlockState(blockPos), null));
					}
				}
			}
		}
		imb.processed(list);
	}

	public Queue<IMassBlock> queue;

	public MassBlockController() {
		MassBlockController.Instance = this;
		MassBlockController.Instance.queue = new LinkedList<IMassBlock>();
	}
}
