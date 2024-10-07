package noppes.npcs.mixin.client.audio;

import net.minecraft.client.audio.ISound;

import java.util.Map;

public interface ISoundManagerMixin {

    Map<String, ISound> npcs$getPlayingSounds();

}
