package noppes.npcs.mixin;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions("noppes.npcs.asm")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class NpcsLaunchPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final String[] CLASS_TRANSFORMERS = new String[0];
    private static final List<String> MIXIN_CONFIGS = Collections.singletonList("mixins.npcs.json");

    @Override
    public String[] getASMTransformerClass() {
        return CLASS_TRANSFORMERS;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public List<String> getMixinConfigs() {
        return MIXIN_CONFIGS;
    }

}
