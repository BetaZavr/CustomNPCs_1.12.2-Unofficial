package noppes.npcs.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.*;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelOBJPlayerArmor;
import noppes.npcs.client.renderer.data.CustomOBJState;
import noppes.npcs.client.renderer.data.ParameterizedModel;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;

public class ModelBuffer {

	protected static final Map<ResourceLocation, TextureAtlasSprite> ATLASES = new HashMap<>(); // list of parameterized
	protected static final List<ParameterizedModel> MODELS = new ArrayList<>(); // list of parameterized
	// rendered models
	public static List<ResourceLocation> NOT_FOUND = new ArrayList<>();	// list of missing models
	// so as not to freeze
	// the client
	private static ModelOBJPlayerArmor objModel;

	/**
	 * Actually trying to get the list ID:
	 *
	 * @param objModel
	 *            - resource for the location of the OBJ model
	 * @param visibleMeshes
	 *            - list of names of meshes/grids that need to be displayed from the
	 *            model
	 * @param replacesMaterialTextures
	 *            - texture replacement map. Key is a resource for a texture from a
	 *            material, Value is a new resource texture
	 * @return ID of the drawing sheet
	 */
	public static int getDisplayList(ResourceLocation objModel, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		if (ModelBuffer.NOT_FOUND.contains(objModel)) { return -1; }
		ParameterizedModel tempModel = new ParameterizedModel(-1, objModel, visibleMeshes, replacesMaterialTextures);
		for (ParameterizedModel pm : ModelBuffer.MODELS) {
			if (pm.equals(tempModel)) {
				tempModel = pm;
				break;
			}
		}
		ParameterizedModel model = tempModel;
		Minecraft mc = Minecraft.getMinecraft();
		if (model.listId < 0) {
			try {
				ImmutableMap<String, String> customData = ImmutableMap.copyOf(Collections.singletonMap("flip-v", "true")); // revers V coordinates in texture
				model.iModel = (OBJModel) OBJLoader.INSTANCE.loadModel(model.file).process(customData);
				GL11.glPushMatrix();
				GL11.glNewList(model.listId = GL11.glGenLists(1), GL11.GL_COMPILE);
				Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> {
					if (location.toString().equals("minecraft:missingno") || location.toString().equals("minecraft:builtin/white")) {
						return mc.getTextureMapBlocks().getAtlasSprite(location.toString());
					}
					ResourceLocation loc = location;
					if (replacesMaterialTextures != null &&
							replacesMaterialTextures.containsKey(location.toString()) &&
							!replacesMaterialTextures.get(location.toString()).equals(location.toString())) {
						loc = new ResourceLocation(replacesMaterialTextures.get(location.toString()));
						LogWriter.debug("Replace texture: " + location + " -> " + loc);
					}
					TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(loc.toString());
					if (sprite == mc.getTextureMapBlocks().getMissingSprite()) {
						try {
							if (ATLASES.containsKey(loc)) { sprite = ATLASES.get(loc); }
							else { sprite = createDynamicTextureAtlas(loc, model); }
							return mc.getTextureMapBlocks().getAtlasSprite(loc.toString());
						} catch (Exception e) {
							LogWriter.error("Failed to load texture " + location, e);
						}
					}
					return sprite;
				};
				if (model.visibleMeshes == null || model.visibleMeshes.isEmpty()) { model.visibleMeshes = new ArrayList<>(model.iModel.getMatLib().getGroups().keySet()); }
				@SuppressWarnings("deprecation")
				IBakedModel bakedModel = model.iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(model.visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				for (BakedQuad bakedquad : bakedModel.getQuads(null, null, 0)) { buffer.addVertexData(bakedquad.getVertexData()); }
				tessellator.draw();
				GL11.glEndList();
				GL11.glPopMatrix();
				ModelBuffer.MODELS.add(model);
			} catch (Exception e) {
				ModelBuffer.NOT_FOUND.add(objModel);
				LogWriter.error("Error create OBJ \"" + objModel + "\" render list", e);
			}
		}
		mc.getTextureManager().bindTexture(model.atlas);
		return model.listId;
	}

	private static TextureAtlasSprite createDynamicTextureAtlas(ResourceLocation location, ParameterizedModel model) {
		Minecraft mc = Minecraft.getMinecraft();
		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getMissingSprite();
		try {
			ResourceLocation textureLocation = location;
			if (!location.getResourcePath().toLowerCase().endsWith(".png")) {
				textureLocation = new ResourceLocation(location.getResourceDomain(), "textures/" + location.getResourcePath() + ".png");
			}
			IResource resource = mc.getResourceManager().getResource(textureLocation);
			mc.getTextureManager().bindTexture(textureLocation);
			sprite = mc.getTextureMapBlocks().registerSprite(location);
			BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
			if (bufferedImage.getWidth() > 0 && bufferedImage.getHeight() > 0) {
				int[][] pixels = new int[1][];
				pixels[0] = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
				bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels[0], 0, bufferedImage.getWidth());
				sprite.setIconWidth(bufferedImage.getWidth());
				sprite.setIconHeight(bufferedImage.getHeight());
				sprite.setFramesTextureData(Lists.<int[][]>newArrayList(pixels));
				model.atlas = textureLocation;
			}
			else { LogWriter.error("Invalid dimensions for texture: " + location); }
		}
		catch (Exception ignored) { }
		ATLASES.put(location, sprite);
		return sprite;
	}

	public static ResourceLocation getMainOBJTexture(ResourceLocation objModel) {
		try {
			ResourceLocation location = new ResourceLocation(objModel.getResourceDomain(), objModel.getResourcePath().replace(".obj", ".mtl"));
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
			String mat_lib = IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8);
			if (mat_lib.contains("map_Kd")) {
				int endIndex = mat_lib.indexOf("" + ((char) 10), mat_lib.indexOf("map_Kd"));
				if (endIndex == -1) {
					endIndex = mat_lib.length();
				}
				String txtr = mat_lib.substring(mat_lib.indexOf(" ", mat_lib.indexOf("map_Kd")) + 1, endIndex);
				String domain = "", path;
				if (!txtr.contains(":")) {
					path = txtr;
				} else {
					domain = txtr.substring(0, txtr.indexOf(":"));
					path = txtr.substring(txtr.indexOf(":") + 1);
				}
				return new ResourceLocation(domain, path);
			}
		} catch (IOException e) { LogWriter.error(e); }
		return null;
	}

	public static ModelBiped getOBJModelBiped(CustomArmor armor, EntityLivingBase entity, ModelBiped defModel) {
		if (!(entity instanceof EntityPlayer) && !(entity instanceof EntityNPCInterface)) {
			return null;
		}
		if (entity instanceof EntityNPCInterface) { return defModel; }
		if (ModelBuffer.objModel == null) {
			ModelBuffer.objModel = new ModelOBJPlayerArmor(armor);
		}
		return ModelBuffer.objModel;
	}

	public static IBakedModel getIBakedModel(CustomArmor armor) {
		if (armor.objModel == null) { return null; }
		List<String> visibleMeshes = new ArrayList<>();
		if (armor.getEquipmentSlot() == EntityEquipmentSlot.HEAD) {
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.HEAD));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.MOHAWK));
		} else if (armor.getEquipmentSlot() == EntityEquipmentSlot.CHEST) {
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.BODY));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.ARM_LEFT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.ARM_RIGHT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.WRIST_LEFT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.WRIST_RIGHT));
		} else if (armor.getEquipmentSlot() == EntityEquipmentSlot.LEGS) {
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.BELT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.LEG_LEFT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.LEG_RIGHT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.FOOT_LEFT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.FOOT_RIGHT));
		} else if (armor.getEquipmentSlot() == EntityEquipmentSlot.FEET) {
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.FEET_LEFT));
			visibleMeshes.addAll(armor.getMeshNames(EnumParts.FEET_RIGHT));
		} else { return null; }
		try {
			OBJModel iModel = (OBJModel) OBJLoader.INSTANCE.loadModel(armor.objModel);
			Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> {
				if (location.toString().equals("minecraft:missingno") || location.toString().equals("minecraft:builtin/white")) {
					return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
				}
				TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
				if (sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
					LogWriter.debug("Not load or found texture sprite: " + location + " to " + objModel);
				}
				return sprite;
			};
			return iModel.bake(new CustomOBJState(ImmutableList.copyOf(visibleMeshes), true, armor), DefaultVertexFormats.ITEM, spriteFunction);
		} catch (Exception e) { LogWriter.error(e); }
		return null;
	}

}
