package noppes.npcs.client;

import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class CustomNpcResourceListener implements IResourceManagerReloadListener {

	public static int DefaultTextColor = CustomNpcs.LableColor.getRGB();

	private void createTextureCache(IResourceManager resourceManager) {
		this.enlargeTexture("planks_oak", resourceManager);
		this.enlargeTexture("planks_big_oak", resourceManager);
		this.enlargeTexture("planks_birch", resourceManager);
		this.enlargeTexture("planks_jungle", resourceManager);
		this.enlargeTexture("planks_spruce", resourceManager);
		this.enlargeTexture("planks_acacia", resourceManager);
		this.enlargeTexture("iron_block", resourceManager);
		this.enlargeTexture("diamond_block", resourceManager);
		this.enlargeTexture("stone", resourceManager);
		this.enlargeTexture("gold_block", resourceManager);
		this.enlargeTexture("wool_colored_white", resourceManager);
	}

	private void enlargeTexture(String texture, IResourceManager resourceManager) {
        ResourceLocation location = new ResourceLocation("minecraft", "textures/cache/" + texture + ".png");
		IResource resource = null;
		try { resource = resourceManager.getResource(location); } catch (Exception e) { LogWriter.debug("Not found texture: \""+location+"\""); }
		if (!(resource instanceof TextureCache)) {
			new TextureCache(location).loadTexture(resourceManager);
		} else {
			((TextureCache) resource).setImage(new ResourceLocation("minecraft", "textures/blocks/" + texture + ".png"));
		}
	}

	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
		if (resourceManager instanceof SimpleReloadableResourceManager) {
			this.createTextureCache(resourceManager);
			SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) resourceManager;
			FolderResourcePack pack = new FolderResourcePack(CustomNpcs.Dir);
			simplemanager.reloadResourcePack(pack);
			CustomNpcResourceListener.DefaultTextColor = CustomNpcs.LableColor.getRGB();
		}
	}

}
