package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.client.Client;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.util.AdditionalMethods;

public class AnimationController
implements IAnimationHandler {
	
	private static AnimationController instance;
	public TreeMap<Integer, IAnimation> animations;
	public static int version = 0;
	private String filePath;
	
	public AnimationController() {
		AnimationController.instance = this;
		this.filePath = CustomNpcs.getWorldSaveDirectory().getAbsolutePath();
		this.animations = Maps.<Integer, IAnimation>newTreeMap();
		this.loadAnimations();
	}
	
	public static AnimationController getInstance() {
		if (newInstance()) { AnimationController.instance = new AnimationController(); }
		return AnimationController.instance;
	}

	private static boolean newInstance() {
		if (AnimationController.instance == null) { return true; }
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !AnimationController.instance.filePath.equals(file.getName());
	}
	
	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (int id : this.animations.keySet()) {
			NBTTagCompound nbtAnimation = ((AnimationConfig) this.animations.get(id)).writeToNBT(new NBTTagCompound());
			nbtAnimation.setInteger("ID", id);
			list.appendTag(nbtAnimation);
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Data", list);
		return compound;
	}
	
	public int getUnusedId() {
		int id = 0;
		for (int i : this.animations.keySet()) {
			if (i != id) { break; }
			id = i + 1;
		}
		return id;
	}
	
	private void loadAnimations() {
		File saveDir = CustomNpcs.Dir;
		if (saveDir == null) { return; }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadAnimations");
		}
		this.filePath = saveDir.getName();
		try {
			File file = new File(saveDir, "animations.dat");
			if (file.exists()) {
				this.loadAnimations(file);
			} else {
				this.loadDefaultAnimations(-1);
			}
		} catch (Exception e) { this.loadDefaultAnimations(-1); }
		if (this.animations.size()==0) { this.loadDefaultAnimations(-1); }
		CustomNpcs.debugData.endDebug("Common", null, "loadAnimations");
	}

	private void loadAnimations(File file) throws IOException {
		this.loadAnimations(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadAnimations(NBTTagCompound compound) throws IOException {
		if (this.animations!=null) { this.animations.clear(); }
		else { this.animations = Maps.<Integer, IAnimation>newTreeMap(); }
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadAnimation(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	public IAnimation loadAnimation(NBTTagCompound nbtAnimation) {
		if (nbtAnimation==null || !nbtAnimation.hasKey("ID", 3) || nbtAnimation.getInteger("ID")<0) { return null; }
		if (this.animations.containsKey(nbtAnimation.getInteger("ID"))) {
			((AnimationConfig) this.animations.get(nbtAnimation.getInteger("ID"))).readFromNBT(nbtAnimation);
			return this.animations.get(nbtAnimation.getInteger("ID"));
		}
		AnimationConfig ac = new AnimationConfig();
		ac.readFromNBT(nbtAnimation);
		this.animations.put(nbtAnimation.getInteger("ID"), ac);
		return this.animations.get(nbtAnimation.getInteger("ID"));
	}

	private void loadDefaultAnimations(int version) {
		if (version == AnimationController.version) { return; }
		InputStream inputStream = AdditionalMethods.instance.getModInputStream("default_animations.dat");
		if (inputStream!=null) {
			try { this.loadAnimations(CompressedStreamTools.readCompressed(inputStream)); } catch (Exception e) {}
		}
	}
	
	public void save() {
		try { CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "animations.dat"))); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void sendTo(EntityPlayerMP player) {
		if (CustomNpcs.Server!=null && CustomNpcs.Server.isSinglePlayer()) { return; }
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, new NBTTagCompound());
		for (IAnimation ac : this.animations.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, ((AnimationConfig) ac).writeToNBT(new NBTTagCompound()));
		}
	}
	
	public void sendToServer() {
		NBTTagCompound nbt = new NBTTagCompound();
		Client.sendData(EnumPacketServer.AnimationChange, nbt);
		for (IAnimation ac : this.animations.values()) {
			Client.sendData(EnumPacketServer.AnimationChange, ((AnimationConfig) ac).writeToNBT(new NBTTagCompound()));
		}
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.AnimationChange, nbt);
	}

	public List<AnimationConfig> getAnimations(List<Integer> ids) {
		List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
		if (ids == null || ids.isEmpty()) { return list; }
		for (IAnimation ac : this.animations.values()) {
			for (int id : ids) {
				if (ac.getId() == id) {
					list.add((AnimationConfig) ac);
					break;
				}
			}
		}
		return list;
	}
	
	@Override
	public IAnimation getAnimation(int animationId) {
		if (this.animations.containsKey(animationId)) { return this.animations.get(animationId); }
		return null;
	}

	@Override
	public IAnimation getAnimation(String animationName) {
		for (IAnimation ac : this.animations.values()) {
			if (ac.getName().equalsIgnoreCase(animationName)) {
				return ac;
			}
		}
		return null;
	}

	@Override
	public boolean removeAnimation(int animationId) {
		if (this.animations.containsKey(animationId)) {
			this.animations.remove(animationId);
			this.save();
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAnimation(String animationName) {
		for (int id : this.animations.keySet()) {
			if (this.animations.get(id).getName().equalsIgnoreCase(animationName)) {
				this.animations.remove(id);
				this.save();
				return true;
			}
		}
		return false;
	}

	@Override
	public IAnimation createNew() {
		AnimationConfig ac = new AnimationConfig();
		ac.id = this.getUnusedId();
		this.animations.put(ac.id, ac);
		return ac;
	}

	@Override
	public IAnimation[] getAnimations() {
		return animations.values().toArray(new IAnimation[animations.size()]);
	}
	
}
