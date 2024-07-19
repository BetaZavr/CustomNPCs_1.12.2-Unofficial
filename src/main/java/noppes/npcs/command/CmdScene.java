package noppes.npcs.command;

import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.entity.data.DataScenes;

import javax.annotation.Nonnull;

public class CmdScene extends CommandNoppesBase {
	@Override
	public String getDescription() {
		return "Scene operations";
	}

	@Nonnull
    public String getName() {
		return "scene";
	}

	@SubCommand(desc = "Pause scene", usage = "[name]", permission = 2)
	public void pause(String[] args) {
		DataScenes.Pause((args.length == 0) ? null : args[0]);
	}

	@SubCommand(desc = "Reset scene", usage = "[name]", permission = 2)
	public void reset(ICommandSender sender, String[] args) {
		DataScenes.Reset(sender, (args.length == 0) ? null : args[0]);
	}

	@SubCommand(desc = "Start scene", usage = "<name>", permission = 2)
	public void start(ICommandSender sender, String[] args) {
		DataScenes.Start(args[0]);
	}

	@SubCommand(desc = "Get/Set scene time", usage = "[time] [name]", permission = 2)
	public void time(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			this.sendMessage(sender, "Active scenes:");
			for (Map.Entry<String, DataScenes.SceneState> entry : DataScenes.StartedScenes.entrySet()) {
				this.sendMessage(sender, "Scene %s time is %s", entry.getKey(), entry.getValue().ticks);
			}
		} else if (args.length == 1) {
			int ticks = Integer.parseInt(args[0]);
			for (DataScenes.SceneState state : DataScenes.StartedScenes.values()) {
				state.ticks = ticks;
			}
			this.sendMessage(sender, "All Scene times are set to " + ticks);
		} else {
			DataScenes.SceneState state2 = DataScenes.StartedScenes.get(args[1].toLowerCase());
			if (state2 == null) {
				throw new CommandException("Unknown scene name %s", args[1]);
			}
			state2.ticks = Integer.parseInt(args[0]);
			this.sendMessage(sender, "Scene %s set to %s", args[1], state2.ticks);
		}
	}
}
