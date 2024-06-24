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
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.client.Client;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.util.AdditionalMethods;

public class AnimationController implements IAnimationHandler {

	private static AnimationController instance;
	public static int version = 0;
	public final TreeMap<Integer, AnimationConfig> animations = Maps.<Integer, AnimationConfig>newTreeMap();
	public final TreeMap<Integer, EmotionConfig> emotions = Maps.<Integer, EmotionConfig>newTreeMap();
	private String filePath;
	
	public static AnimationController getInstance() {
		if (newInstance()) {
			AnimationController.instance = new AnimationController();
		}
		return AnimationController.instance;
	}
	
	private static boolean newInstance() {
		if (AnimationController.instance == null) {
			return true;
		}
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !AnimationController.instance.filePath.equals(file.getName());
	}

	public AnimationController() {
		AnimationController.instance = this;
		this.filePath = CustomNpcs.getWorldSaveDirectory().getAbsolutePath();
		this.loadAnimations();
	}

	@Override
	public IAnimation createNewAnim() {
		AnimationConfig ac = new AnimationConfig();
		ac.id = this.getUnusedAnimId();
		this.animations.put(ac.id, ac);
		return ac;
	}

	@Override
	public IEmotion createNewEmtn() {
		EmotionConfig ec = new EmotionConfig();
		ec.id = this.getUnusedEmtnId();
		this.emotions.put(ec.id, ec);
		return ec;
	}

	@Override
	public IAnimation getAnimation(int animationId) {
		if (this.animations.containsKey(animationId)) {
			return this.animations.get(animationId);
		}
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
	public IEmotion getEmotion(int emotionId) {
		if (this.emotions.containsKey(emotionId)) {
			return this.emotions.get(emotionId);
		}
		return null;
	}

	@Override
	public IEmotion getEmotion(String emotionName) {
		for (IEmotion ec : this.emotions.values()) {
			if (ec.getName().equalsIgnoreCase(emotionName)) {
				return ec;
			}
		}
		return null;
	}
	
	@Override
	public IAnimation[] getAnimations() {
		return animations.values().toArray(new IAnimation[animations.size()]);
	}

	public List<AnimationConfig> getAnimations(List<Integer> ids) {
		List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
		if (ids == null || ids.isEmpty()) { return list; }
		for (AnimationConfig ac : this.animations.values()) {
			for (int id : ids) {
				if (ac.getId() == id) {
					list.add(ac);
					break;
				}
			}
		}
		return list;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		NBTTagList listA = new NBTTagList();
		for (int id : this.animations.keySet()) {
			NBTTagCompound nbtAnimation = this.animations.get(id).writeToNBT(new NBTTagCompound());
			nbtAnimation.setInteger("ID", id);
			listA.appendTag(nbtAnimation);
		}
		compound.setTag("Animations", listA);
		
		NBTTagList listE = new NBTTagList();
		for (int id : this.emotions.keySet()) {
			NBTTagCompound nbtEmotion = this.emotions.get(id).writeToNBT(new NBTTagCompound());
			nbtEmotion.setInteger("ID", id);
			listE.appendTag(nbtEmotion);
		}
		compound.setTag("Emotions", listE);

		return compound;
	}

	public int getUnusedEmtnId() {
		int id = 0;
		for (int i : this.emotions.keySet()) {
			if (i != id) {
				break;
			}
			id = i + 1;
		}
		return id;
	}
	
	public int getUnusedAnimId() {
		int id = 0;
		for (int i : this.animations.keySet()) {
			if (i != id) {
				break;
			}
			id = i + 1;
		}
		return id;
	}
	
	public IAnimation loadAnimation(NBTTagCompound nbtAnimation) {
		if (nbtAnimation == null || !nbtAnimation.hasKey("ID", 3) || nbtAnimation.getInteger("ID") < 0) {
			return null;
		}
		if (this.animations.containsKey(nbtAnimation.getInteger("ID"))) {
			this.animations.get(nbtAnimation.getInteger("ID")).readFromNBT(nbtAnimation);
			return this.animations.get(nbtAnimation.getInteger("ID"));
		}
		AnimationConfig ac = new AnimationConfig();
		ac.readFromNBT(nbtAnimation);
		this.animations.put(nbtAnimation.getInteger("ID"), ac);
		return this.animations.get(nbtAnimation.getInteger("ID"));
	}
	
	public IEmotion loadEmotion(NBTTagCompound nbtEmotion) {
		if (nbtEmotion == null || !nbtEmotion.hasKey("ID", 3) || nbtEmotion.getInteger("ID") < 0) {
			return null;
		}
		if (this.emotions.containsKey(nbtEmotion.getInteger("ID"))) {
			this.emotions.get(nbtEmotion.getInteger("ID")).readFromNBT(nbtEmotion);
			return this.emotions.get(nbtEmotion.getInteger("ID"));
		}
		EmotionConfig ec = new EmotionConfig();
		ec.readFromNBT(nbtEmotion);
		this.emotions.put(nbtEmotion.getInteger("ID"), ec);
		return this.emotions.get(nbtEmotion.getInteger("ID"));
	}

	private void loadAnimations() {
		File saveDir = CustomNpcs.Dir;
		if (saveDir == null) {
			return;
		}
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
		} catch (Exception e) {
			this.loadDefaultAnimations(-1);
		}
		if (this.animations.size() == 0) {
			this.loadDefaultAnimations(-1);
		}
		CustomNpcs.debugData.endDebug("Common", null, "loadAnimations");
	}

	private void loadAnimations(File file) throws IOException {
		this.loadAnimations(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadAnimations(NBTTagCompound compound) throws IOException {
		this.animations.clear();
		NBTTagList listA = compound.getTagList("Animations", 10);
		if (compound.hasKey("Animations", 9)) { listA = compound.getTagList("Animations", 10); }
		else if (compound.hasKey("Data", 9)) { listA = compound.getTagList("Data", 10); }
		if (listA != null) {
			for (int i = 0; i < listA.tagCount(); ++i) {
				this.loadAnimation(listA.getCompoundTagAt(i));
			}
		}
		NBTTagList listE = compound.getTagList("Emotions", 10);
		if (listE != null) {
			for (int i = 0; i < listE.tagCount(); ++i) {
				this.loadEmotion(listE.getCompoundTagAt(i));
			}
		}
	}

	private void loadDefaultAnimations(int version) {
		if (version == AnimationController.version) {
			return;
		}
		InputStream inputStream = AdditionalMethods.instance.getModInputStream("default_animations.dat");
		if (inputStream != null) {
			try {
				this.loadAnimations(CompressedStreamTools.readCompressed(inputStream));
			} catch (Exception e) {
			}
		}
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
	public boolean removeEmotion(int emotionId) {
		if (this.emotions.containsKey(emotionId)) {
			this.emotions.remove(emotionId);
			this.save();
			return true;
		}
		return false;
	}

	@Override
	public boolean removeEmotion(String emotionName) {
		for (int id : this.emotions.keySet()) {
			if (this.emotions.get(id).getName().equalsIgnoreCase(emotionName)) {
				this.emotions.remove(id);
				this.save();
				return true;
			}
		}
		return false;
	}

	public void save() {
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "animations.dat")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTo(EntityPlayerMP player) {
		if (CustomNpcs.Server != null && CustomNpcs.Server.isSinglePlayer()) {
			return;
		}
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, new NBTTagCompound());
		for (AnimationConfig ac : this.animations.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, ac.writeToNBT(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.EmotionData, new NBTTagCompound());
		for (EmotionConfig ec : this.emotions.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.EmotionData, ec.writeToNBT(new NBTTagCompound()));
		}
	}

	public void sendToServer() {
		NBTTagCompound nbt = new NBTTagCompound();
		Client.sendData(EnumPacketServer.AnimationChange, nbt);
		List<AnimationConfig> listA = Lists.newArrayList(animations.values());
		for (AnimationConfig ac : listA) {
			Client.sendData(EnumPacketServer.AnimationChange, ac.writeToNBT(new NBTTagCompound()));
		}
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.AnimationChange, nbt);
	}
	
	public IEmotion[] getEmotions() {
		return this.emotions.values().toArray(new IEmotion[this.emotions.size()]);
	}

}
