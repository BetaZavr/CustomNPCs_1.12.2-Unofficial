package noppes.npcs.entity.data;

import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.BossInfo;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataDisplay
implements INPCDisplay {
	
	private Availability availability;
	private BossInfo.Color bossColor;
	private String cloakTexture;
	private boolean disableLivingAnimation;
	private String glowTexture;
	private int markovGender;
	private int markovGeneratorId;
	private int modelSize;
	private String name;
	private boolean noHitbox;
	EntityNPCInterface npc;
	public GameProfile playerProfile;
	private byte showBossBar;
	private int showName;
	private int skinColor;
	public byte skinType;
	private String texture;
	private String title;
	private String url;
	private int visible;

	public DataDisplay(EntityNPCInterface npc) {
		this.title = "";
		this.markovGeneratorId = 8;
		this.markovGender = 0;
		this.skinType = 0;
		this.url = "";
		this.texture = CustomNpcs.MODID + ":textures/entity/humanmale/steve.png";
		this.cloakTexture = "";
		this.glowTexture = "";
		this.visible = 0;
		this.availability = new Availability();
		this.modelSize = 5;
		this.showName = 0;
		this.skinColor = 16777215;
		this.disableLivingAnimation = false;
		this.noHitbox = false;
		this.showBossBar = 0;
		this.bossColor = BossInfo.Color.PINK;
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
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		if (modeldata.entityClass == null) {
			return null;
		}
		String name = modeldata.entityClass.getCanonicalName();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = (Class<? extends Entity>) ent.getEntityClass();
			if (c.getCanonicalName().equals(name) && EntityLivingBase.class.isAssignableFrom(c)) {
				return ent.getRegistryName().toString();
			}
		}
		return null;
	}

	@Override
	public float[] getModelScale(int part) {
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
			throw new CustomNPCsException("Unknown part: " + part, new Object[0]);
		}
		return new float[] { model.scaleBase[0], model.scaleBase[1], model.scaleBase[2] };
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
		if (this.visible == 1) { return !this.availability.isAvailable((EntityPlayer) player); }
		return this.availability.isAvailable((EntityPlayer) player);
	}

	@Override
	public boolean isVisibleTo(IPlayer<?> player) {
		return this.isVisibleTo(player);
	}

	public void loadProfile() {
		if (this.playerProfile != null && !StringUtils.isNullOrEmpty(this.playerProfile.getName())
				&& this.npc.getServer() != null
				&& (!this.playerProfile.isComplete() || !this.playerProfile.getProperties().containsKey("textures"))) {
			GameProfile gameprofile = this.npc.getServer().getPlayerProfileCache()
					.getGameProfileForUsername(this.playerProfile.getName());
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

	public void readToNBT(NBTTagCompound nbttagcompound) {
		this.setName(nbttagcompound.getString("Name"));
		this.setMarkovGeneratorId(nbttagcompound.getInteger("MarkovGeneratorId"));
		this.setMarkovGender(nbttagcompound.getInteger("MarkovGender"));
		this.title = nbttagcompound.getString("Title");
		int prevSkinType = this.skinType;
		String prevTexture = this.texture;
		String prevUrl = this.url;
		String prevPlayer = this.getSkinPlayer();
		this.url = nbttagcompound.getString("SkinUrl");
		this.skinType = nbttagcompound.getByte("UsingSkinUrl");
		this.texture = nbttagcompound.getString("Texture");
		this.cloakTexture = nbttagcompound.getString("CloakTexture");
		this.glowTexture = nbttagcompound.getString("GlowTexture");
		this.playerProfile = null;
		if (this.skinType == 1) {
			if (nbttagcompound.hasKey("SkinUsername", 10)) {
				this.playerProfile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkinUsername"));
			} else if (nbttagcompound.hasKey("SkinUsername", 8)
					&& !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkinUsername"))) {
				this.playerProfile = new GameProfile((UUID) null, nbttagcompound.getString("SkinUsername"));
			}
			this.loadProfile();
		}
		this.modelSize = ValueUtil.correctInt(nbttagcompound.getInteger("Size"), 1, 30);
		this.showName = nbttagcompound.getInteger("ShowName");
		if (nbttagcompound.hasKey("SkinColor")) {
			this.skinColor = nbttagcompound.getInteger("SkinColor");
		}
		this.visible = nbttagcompound.getInteger("NpcVisible");
		this.availability.readFromNBT(nbttagcompound.getCompoundTag("VisibleAvailability"));
		VisibilityController.trackNpc(this.npc);
		this.disableLivingAnimation = nbttagcompound.getBoolean("NoLivingAnimation");
		this.noHitbox = nbttagcompound.getBoolean("IsStatue");
		this.setBossbar(nbttagcompound.getByte("BossBar"));
		this.setBossColor(nbttagcompound.getInteger("BossColor"));
		if (prevSkinType != this.skinType || !this.texture.equals(prevTexture) || !this.url.equals(prevUrl)
				|| !this.getSkinPlayer().equals(prevPlayer)) {
			this.npc.textureLocation = null;
		}
		this.npc.textureGlowLocation = null;
		this.npc.textureCloakLocation = null;
		this.npc.updateHitbox();
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
			throw new CustomNPCsException("Invalid Boss Color: " + color, new Object[0]);
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
		ModelData modeldata = ((EntityCustomNpc) this.npc).modelData;
		if (id == null) {
			if (modeldata.entityClass == null) {
				return;
			}
			modeldata.entityClass = null;
			this.npc.updateClient = true;
		} else {
			ResourceLocation resource = new ResourceLocation(id);
			Entity entity = EntityList.createEntityByIDFromName(resource, this.npc.world);
			if (entity == null) {
				throw new CustomNPCsException("Failed to create an entity from given id: " + id, new Object[0]);
			}
			modeldata.setEntityName(entity.getClass().getCanonicalName());
			this.npc.updateClient = true;
		}
	}

	@Override
	public void setModelScale(int part, float x, float y, float z) {
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
			throw new CustomNPCsException("Unknown part: " + part, new Object[0]);
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
	public void setShowName(int type) {
		if (type == this.showName) { return; }
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
			this.playerProfile = new GameProfile((UUID) null, name);
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
		if (this.url.equals(url)) {
			return;
		}
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
		if (type == this.visible) {
			return;
		}
		this.visible = ValueUtil.correctInt(type, 0, 2);
		this.npc.updateClient = true;
	}

	public boolean showName() {
		return !this.npc.isKilled() && (this.showName == 0 || (this.showName == 2 && this.npc.isAttacking()));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("Name", this.name);
		nbttagcompound.setInteger("MarkovGeneratorId", this.markovGeneratorId);
		nbttagcompound.setInteger("MarkovGender", this.markovGender);
		nbttagcompound.setString("Title", this.title);
		nbttagcompound.setString("SkinUrl", this.url);
		nbttagcompound.setString("Texture", this.texture);
		nbttagcompound.setString("CloakTexture", this.cloakTexture);
		nbttagcompound.setString("GlowTexture", this.glowTexture);
		nbttagcompound.setByte("UsingSkinUrl", this.skinType);
		if (this.playerProfile != null) {
			NBTTagCompound nbttagcompound2 = new NBTTagCompound();
			NBTUtil.writeGameProfile(nbttagcompound2, this.playerProfile);
			nbttagcompound.setTag("SkinUsername", nbttagcompound2);
		}
		nbttagcompound.setInteger("Size", this.modelSize);
		nbttagcompound.setInteger("ShowName", this.showName);
		nbttagcompound.setInteger("SkinColor", this.skinColor);
		nbttagcompound.setInteger("NpcVisible", this.visible);
		nbttagcompound.setTag("VisibleAvailability", this.availability.writeToNBT(new NBTTagCompound()));
		nbttagcompound.setBoolean("NoLivingAnimation", this.disableLivingAnimation);
		nbttagcompound.setBoolean("IsStatue", this.noHitbox);
		nbttagcompound.setByte("BossBar", this.showBossBar);
		nbttagcompound.setInteger("BossColor", this.bossColor.ordinal());
		nbttagcompound.setBoolean("EnableInvisibleNpcs", CustomNpcs.EnableInvisibleNpcs);
		return nbttagcompound;
	}

}
