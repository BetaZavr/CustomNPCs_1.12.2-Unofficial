package noppes.npcs.entity.data;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import noppes.npcs.LogWriter;
import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.BossInfo;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataDisplay implements INPCDisplay {

	EntityNPCInterface npc;
	public GameProfile playerProfile;
	public byte skinType = (byte) 0;

	private final Availability availability = new Availability();
	private BossInfo.Color bossColor = BossInfo.Color.PINK;
	private boolean disableLivingAnimation = false;
	private boolean noHitbox = false;
	private boolean isNormalModel = false;
	private byte showBossBar = 0;
	private int markovGender = 0;
	private int markovGeneratorId;
	private int modelSize = 5;
	private int showName = 0;
	private int skinColor = 0xFFFFFF;
	private int visible = 0;
	private String cloakTexture = "";
	private String title = "";
	private String url = "";
	private String glowTexture = "";
	private String name;
	private String texture = CustomNpcs.MODID + ":textures/entity/humanmale/steve.png";
	public float shadowSize = 1.0f;
	public float width = 0.0f;
	public float height = 0.0f;

	public DataDisplay(EntityNPCInterface npc) {
		this.npc = npc;
		this.markovGeneratorId = new Random().nextInt(CustomNpcs.MARKOV_GENERATOR.length - 1);
		this.name = this.getRandomName();
	}

	public Availability getAvailability() {
		return this.availability;
	}

	@Override
	public int getBossbar() {
		return this.showBossBar;
	}

	@Override
	public int getBossColor() {
		return this.bossColor.ordinal();
	}

	@Override
	public String getCapeTexture() {
		return this.cloakTexture;
	}

	@Override
	public boolean getHasHitbox() {
		return !this.noHitbox;
	}

	@Override
	public boolean isNormalModel() {
		return !this.isNormalModel;
	}
	
	@Override
	public boolean getHasLivingAnimation() {
		return !this.disableLivingAnimation;
	}

	public int getMarkovGender() {
		return this.markovGender;
	}

	public int getMarkovGeneratorId() {
		return this.markovGeneratorId;
	}

	@Override
	public String getModel() {
		if (!(this.npc instanceof EntityCustomNpc)) {
			return null;
		}
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		if (modeldata.entityClass == null) {
			return null;
		}
		String name = modeldata.entityClass.getCanonicalName();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = ent.getEntityClass();
			if (c.getCanonicalName().equals(name) && EntityLivingBase.class.isAssignableFrom(c)) {
				return Objects.requireNonNull(ent.getRegistryName()).toString();
			}
		}
		return null;
	}

	@Override
	public float[] getModelScale(int part) {
		if (!(this.npc instanceof EntityCustomNpc)) {
			return new float[] { 1.0f, 1.0f, 1.0f };
		}
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		ModelPartConfig model = null;
		if (part == 0) {
			model = modeldata.getPartConfig(EnumParts.HEAD);
		} else if (part == 1) {
			model = modeldata.getPartConfig(EnumParts.BODY);
		} else if (part == 2) {
			model = modeldata.getPartConfig(EnumParts.ARM_LEFT);
		} else if (part == 3) {
			model = modeldata.getPartConfig(EnumParts.ARM_RIGHT);
		} else if (part == 4) {
			model = modeldata.getPartConfig(EnumParts.LEG_LEFT);
		} else if (part == 5) {
			model = modeldata.getPartConfig(EnumParts.LEG_RIGHT);
		}
		if (model == null) {
			throw new CustomNPCsException("Unknown part: " + part);
		}
		return new float[] { model.scale[0], model.scale[1], model.scale[2] };
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOverlayTexture() {
		return this.glowTexture;
	}

	public String getRandomName() {
		return CustomNpcs.MARKOV_GENERATOR[this.markovGeneratorId].fetch(this.markovGender);
	}

	@Override
	public int getShadowType() {
		if (this.shadowSize < 0.5f) {
			return 0;
		}
		if (this.shadowSize < 1.0f) {
			return 1;
		}
		if (this.shadowSize < 1.5f) {
			return 2;
		}
		return 3;
	}

	@Override
	public int getShowName() {
		return this.showName;
	}

	@Override
	public int getSize() {
		return this.modelSize;
	}

	@Override
	public String getSkinPlayer() {
		return (this.playerProfile == null) ? "" : this.playerProfile.getName();
	}

	@Override
	public String getSkinTexture() {
		return this.texture;
	}

	@Override
	public String getSkinUrl() {
		return this.url;
	}

	@Override
	public int getTint() {
		return this.skinColor;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public int getVisible() {
		return this.visible;
	}

	public boolean hasVisibleOptions() {
		return CustomNpcs.EnableInvisibleNpcs && this.availability.hasOptions();
	}

	public boolean isVisibleTo(EntityPlayerMP player) {
		if (this.visible == 1) {
			return !this.availability.isAvailable(player);
		}
		return true;
	}

	@Override
	public boolean isVisibleTo(IPlayer<?> player) {
		return this.npc.isInvisibleToPlayer(player.getMCEntity());
	}

	public void loadProfile() {
		if (playerProfile != null && !StringUtils.isNullOrEmpty(this.playerProfile.getName()) && this.npc.getServer() != null && (!this.playerProfile.isComplete() || !this.playerProfile.getProperties().containsKey("textures"))) {
			GameProfile gameprofile = this.npc.getServer().getPlayerProfileCache().getGameProfileForUsername(this.playerProfile.getName());
			if (gameprofile != null) {
				Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), null);
				if (property == null) {
					gameprofile = this.npc.getServer().getMinecraftSessionService().fillProfileProperties(gameprofile,
							true);
				}
				this.playerProfile = gameprofile;
			}
		}
	}

	public void readToNBT(NBTTagCompound displayNbt) {
		this.setName(displayNbt.getString("Name"));
		this.setMarkovGeneratorId(displayNbt.getInteger("MarkovGeneratorId"));
		this.setMarkovGender(displayNbt.getInteger("MarkovGender"));
		this.title = displayNbt.getString("Title");
		int prevSkinType = this.skinType;
		String prevTexture = this.texture;
		String prevUrl = this.url;
		String prevPlayer = this.getSkinPlayer();
		this.url = displayNbt.getString("SkinUrl");
		this.skinType = displayNbt.getByte("UsingSkinUrl");
		this.texture = displayNbt.getString("Texture");
		this.cloakTexture = displayNbt.getString("CloakTexture");
		this.glowTexture = displayNbt.getString("GlowTexture");
		this.playerProfile = null;
		if (!this.url.isEmpty() && !this.url.startsWith("http")) {
			try {
				final String json = new String(Base64.decodeBase64(this.url), StandardCharsets.UTF_8);
				Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
				MinecraftTexturesPayload mtp = gson.fromJson(json, MinecraftTexturesPayload.class);
				MinecraftProfileTexture mpt = mtp.getTextures().get(MinecraftProfileTexture.Type.SKIN);
				if (!mpt.getUrl().isEmpty()) { this.url = mpt.getUrl(); }
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (this.skinType == 1) {
			if (displayNbt.hasKey("SkinUsername", 10)) {
				this.playerProfile = NBTUtil.readGameProfileFromNBT(displayNbt.getCompoundTag("SkinUsername"));
			} else if (displayNbt.hasKey("SkinUsername", 8) && !StringUtils.isNullOrEmpty(displayNbt.getString("SkinUsername"))) {
				this.playerProfile = new GameProfile(null, displayNbt.getString("SkinUsername"));
			}
			this.loadProfile();
		}
		this.modelSize = ValueUtil.correctInt(displayNbt.getInteger("Size"), 1, 30);
		this.showName = displayNbt.getInteger("ShowName");
		if (displayNbt.hasKey("SkinColor")) {
			this.skinColor = displayNbt.getInteger("SkinColor");
		}
		this.visible = displayNbt.getInteger("NpcVisible");
		this.availability.readFromNBT(displayNbt.getCompoundTag("VisibleAvailability"));
		this.disableLivingAnimation = displayNbt.getBoolean("NoLivingAnimation");
		this.noHitbox = displayNbt.getBoolean("IsStatue");
		this.isNormalModel = displayNbt.getBoolean("HasJoints");
		
		this.setBossbar(displayNbt.getByte("BossBar"));
		this.setBossColor(displayNbt.getInteger("BossColor"));
		if (prevSkinType != this.skinType || !this.texture.equals(prevTexture) || !this.url.equals(prevUrl)
				|| !this.getSkinPlayer().equals(prevPlayer)) {
			this.npc.textureLocation = null;
		}
		this.npc.textureGlowLocation = null;
		this.npc.textureCloakLocation = null;
		this.npc.updateHitbox();
		if (displayNbt.hasKey("ShadowSize", 5)) {
			this.shadowSize = ValueUtil.correctFloat(displayNbt.getFloat("ShadowSize"), 0, 1.5f);
		} else {
			this.shadowSize = 1.0f;
		}
		if (displayNbt.hasKey("HitBoxWidth", 5)) { this.width = ValueUtil.correctFloat(displayNbt.getFloat("HitBoxWidth"), 0.0f, 5.0f); }
		if (displayNbt.hasKey("HitBoxHeight", 5)) { this.height = ValueUtil.correctFloat(displayNbt.getFloat("HitBoxHeight"), 0.0f, 10.0f); }
        if (this.getHasHitbox() && this.width != 0.0f && this.height != 0.0f) {
        	this.npc.width = this.width;
        	this.npc.height = this.height;
        	this.npc.setPosition(this.npc.posX, this.npc.posY, this.npc.posZ);
		}
		CustomNpcs.visibilityController.trackNpc(this.npc);
	}

	@Override
	public void setBossbar(int type) {
		if (type == this.showBossBar) {
			return;
		}
		this.showBossBar = (byte) ValueUtil.correctInt(type, 0, 2);
		this.npc.bossInfo.setVisible(this.showBossBar == 1);
		this.npc.updateClient = true;
	}

	@Override
	public void setBossColor(int color) {
		if (color < 0 || color >= BossInfo.Color.values().length) {
			throw new CustomNPCsException("Invalid Boss Color: " + color);
		}
		this.bossColor = BossInfo.Color.values()[color];
		this.npc.bossInfo.setColor(this.bossColor);
	}

	@Override
	public void setCapeTexture(String texture) {
		if (this.cloakTexture.equals(texture)) {
			return;
		}
		this.cloakTexture = texture.toLowerCase();
		this.npc.textureCloakLocation = null;
		this.npc.updateClient = true;
	}

	@Override
	public void setHasHitbox(boolean bo) {
		if (this.noHitbox != bo) {
			return;
		}
		this.noHitbox = !bo;
		this.npc.updateClient = true;
	}

	@Override
	public void setNormalModel(boolean bo) {
		if (this.isNormalModel == bo) {
			return;
		}
		this.isNormalModel = bo;
		this.npc.updateClient = true;
	}

	@Override
	public void setHasLivingAnimation(boolean enabled) {
		this.disableLivingAnimation = !enabled;
		this.npc.updateClient = true;
	}

	public void setMarkovGender(int gender) {
		if (this.markovGender == gender) {
			return;
		}
		this.markovGender = ValueUtil.correctInt(gender, 0, 2);
	}

	public void setMarkovGeneratorId(int id) {
		if (this.markovGeneratorId == id) {
			return;
		}
		this.markovGeneratorId = ValueUtil.correctInt(id, 0, CustomNpcs.MARKOV_GENERATOR.length - 1);
	}

	@Override
	public void setModel(String id) {
		if (!(this.npc instanceof EntityCustomNpc)) {
			return;
		}
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		if (id == null) {
			if (modeldata.entityClass == null) {
				return;
			}
			modeldata.entityClass = null;
        } else {
			ResourceLocation resource = new ResourceLocation(id);
			Entity entity = EntityList.createEntityByIDFromName(resource, this.npc.world);
			if (entity == null) {
				throw new CustomNPCsException("Failed to create an entity from given id: " + id);
			}
			modeldata.setEntityName(entity.getClass().getCanonicalName());
        }
        this.npc.updateClient = true;
    }

	@Override
	public void setModelScale(int part, float x, float y, float z) {
		if (!(this.npc instanceof EntityCustomNpc)) {
			return;
		}
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		ModelPartConfig model = null;
		if (part == 0) {
			model = modeldata.getPartConfig(EnumParts.HEAD);
		} else if (part == 1) {
			model = modeldata.getPartConfig(EnumParts.BODY);
		} else if (part == 2) {
			model = modeldata.getPartConfig(EnumParts.ARM_LEFT);
		} else if (part == 3) {
			model = modeldata.getPartConfig(EnumParts.ARM_RIGHT);
		} else if (part == 4) {
			model = modeldata.getPartConfig(EnumParts.LEG_LEFT);
		} else if (part == 5) {
			model = modeldata.getPartConfig(EnumParts.LEG_RIGHT);
		}
		if (model == null) {
			throw new CustomNPCsException("Unknown part: " + part);
		}
		model.setScale(x, y, z);
		this.npc.updateClient = true;
	}

	@Override
	public void setName(String name) {
		if (this.name.equals(name)) {
			return;
		}
		this.name = name;
		this.npc.bossInfo.setName(this.npc.getDisplayName());
		this.npc.updateClient = true;
	}

	@Override
	public void setOverlayTexture(String texture) {
		if (this.glowTexture.equals(texture)) {
			return;
		}
		this.glowTexture = texture;
		this.npc.textureGlowLocation = null;
		this.npc.updateClient = true;
	}

	@Override
	public void setShadowType(int type) {
		if (type < 0) {
			type *= -1;
		}
		switch (type % 4) {
		case 0:
			this.shadowSize = 0.0f;
			break;
		case 1:
			this.shadowSize = 0.5f;
			break;
		case 2:
			this.shadowSize = 1.0f;
			break;
		default:
			this.shadowSize = 1.5f;
			break;
		}
	}

	@Override
	public void setShowName(int type) {
		if (type == this.showName) {
			return;
		}
		this.showName = ValueUtil.correctInt(type, 0, 2);
		this.npc.updateClient = true;
	}

	@Override
	public void setSize(int size) {
		if (this.modelSize == size) {
			return;
		}
		this.modelSize = ValueUtil.correctInt(size, 1, 30);
		this.npc.updateClient = true;
	}

	@Override
	public void setSkinPlayer(String name) {
		if (name == null || name.isEmpty()) {
			this.playerProfile = null;
			this.skinType = 0;
		} else {
			this.playerProfile = new GameProfile(null, name);
			this.skinType = 1;
		}
		this.npc.updateClient = true;
	}

	@Override
	public void setSkinTexture(String texture) {
		if (texture == null || this.texture.equals(texture)) {
			return;
		}
		this.texture = texture.toLowerCase();
		this.npc.textureLocation = null;
		this.skinType = 0;
		this.npc.updateClient = true;
	}

	@Override
	public void setSkinUrl(String url) {
		if (this.url.equals(url)) { return; }
		this.url = url;
		if (url.isEmpty()) {
			this.skinType = 0;
		} else {
			this.skinType = 2;
		}
		this.npc.updateClient = true;
	}

	@Override
	public void setTint(int color) {
		if (color == this.skinColor) {
			return;
		}
		this.skinColor = color;
		this.npc.updateClient = true;
	}

	@Override
	public void setTitle(String title) {
		if (this.title.equals(title)) {
			return;
		}
		this.title = title;
		this.npc.updateClient = true;
	}

	@Override
	public void setVisible(int type) {
		if (type == this.visible) { return; }
		this.visible = ValueUtil.correctInt(type, 0, 2);
		this.npc.updateClient = true;
	}

	public boolean showName() {
		return !this.npc.isKilled() && (this.showName == 0 || (this.showName == 2 && this.npc.isAttacking()));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound displayNbt) {
		displayNbt.setString("Name", this.name);
		displayNbt.setInteger("MarkovGeneratorId", this.markovGeneratorId);
		displayNbt.setInteger("MarkovGender", this.markovGender);
		displayNbt.setString("Title", this.title);
		displayNbt.setString("SkinUrl", this.url);
		displayNbt.setString("Texture", this.texture);
		displayNbt.setString("CloakTexture", this.cloakTexture);
		displayNbt.setString("GlowTexture", this.glowTexture);
		displayNbt.setByte("UsingSkinUrl", this.skinType);
		if (this.playerProfile != null) {
			NBTTagCompound displayNbt2 = new NBTTagCompound();
			NBTUtil.writeGameProfile(displayNbt2, this.playerProfile);
			displayNbt.setTag("SkinUsername", displayNbt2);
		}
		displayNbt.setInteger("Size", this.modelSize);
		displayNbt.setInteger("ShowName", this.showName);
		displayNbt.setInteger("SkinColor", this.skinColor);
		displayNbt.setInteger("NpcVisible", this.visible);
		displayNbt.setTag("VisibleAvailability", this.availability.writeToNBT(new NBTTagCompound()));
		displayNbt.setBoolean("NoLivingAnimation", this.disableLivingAnimation);
		displayNbt.setBoolean("IsStatue", this.noHitbox);
		displayNbt.setBoolean("HasJoints", this.isNormalModel);
		displayNbt.setByte("BossBar", this.showBossBar);
		displayNbt.setInteger("BossColor", this.bossColor.ordinal());
		displayNbt.setBoolean("EnableInvisibleNpcs", CustomNpcs.EnableInvisibleNpcs);
		displayNbt.setFloat("ShadowSize", this.shadowSize);
		displayNbt.setFloat("HitBoxWidth", this.width);
		displayNbt.setFloat("HitBoxHeight", this.height);
		
		
		return displayNbt;
	}

}
