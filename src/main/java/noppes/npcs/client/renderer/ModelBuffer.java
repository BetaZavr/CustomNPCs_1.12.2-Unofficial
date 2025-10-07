package noppes.npcs.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.*;
import noppes.npcs.CustomNpcs;
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

	// rendered models
	protected static final List<ParameterizedModel> MODELS = new ArrayList<>(); // list of parameterized
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
	@SuppressWarnings("all")
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
				// revers V coordinates in texture
				ImmutableMap<String, String> customData = ImmutableMap.copyOf(Collections.singletonMap("flip-v", "true"));
				// load model
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
						// custom or not load texture to blocks atlas
						try {
							ResourceLocation textureLocation = location;
							if (!location.getResourcePath().toLowerCase().endsWith(".png")) { textureLocation = new ResourceLocation(location.getResourceDomain(), "textures/" + location.getResourcePath() + ".png"); }
							IResource resource = mc.getResourceManager().getResource(textureLocation);
							String key = textureLocation.getResourcePath();
							if (key.contains("textures/")) { key = key.replace("textures/", ""); }
							ResourceLocation atlasKey = new ResourceLocation(CustomNpcs.MODID, "textures/atlas/" + key);
							if (sprite == mc.getTextureMapBlocks().getMissingSprite()) {
								LogWriter.debug("Need create custom atlas \""+atlasKey+"\"");
							}
							sprite = mc.getTextureMapBlocks().registerSprite(atlasKey);
							BufferedImage image = TextureUtil.readBufferedImage(resource.getInputStream());
							int[] pixels = new int[image.getWidth() * image.getHeight()];
							image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
							DynamicTexture dynamicTexture = (DynamicTexture) mc.getTextureManager().getTexture(atlasKey);
							boolean needLoad = dynamicTexture == null;
							if (needLoad) {
								dynamicTexture = new DynamicTexture(image);
								dynamicTexture.updateDynamicTexture();
							}
							sprite.setIconWidth(image.getWidth());
							sprite.setIconHeight(image.getHeight());
							int[][] mipmapPixels = new int[1][];
							mipmapPixels[0] = dynamicTexture.getTextureData();
							sprite.setFramesTextureData(Lists.<int[][]>newArrayList(mipmapPixels));
							sprite.initSprite(image.getWidth(), image.getHeight(), 0, 0, false);
							if (needLoad) {
								mc.getTextureManager().loadTexture(atlasKey, dynamicTexture);
								LogWriter.debug("Create custom atlas \""+atlasKey+"\": from texture: \"" + textureLocation+"\"");
							}
							model.atlas = atlasKey;
						}
						catch (Exception e) { LogWriter.error(e); }
					}
					return sprite;
				};
				if (model.visibleMeshes == null || model.visibleMeshes.isEmpty()) {
					model.visibleMeshes = new ArrayList<>(model.iModel.getMatLib().getGroups().keySet());
					model.visibleMeshes.remove("OBJModel.Default.Element.Name");
				}
				// baked
				@SuppressWarnings("deprecation")
				IBakedModel bakedModel = model.iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(model.visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				// draw
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				for (BakedQuad bakedquad : bakedModel.getQuads(null, null, 0)) { buffer.addVertexData(bakedquad.getVertexData()); }
				tessellator.draw();
				GL11.glEndList();
				GL11.glPopMatrix();
				// save
				ModelBuffer.MODELS.add(model);
			} catch (Exception e) {
				ModelBuffer.NOT_FOUND.add(objModel);
				LogWriter.error("Error create OBJ \"" + objModel + "\" render list", e);
			}
		}
		mc.getTextureManager().bindTexture(model.atlas);
		return model.listId;
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
