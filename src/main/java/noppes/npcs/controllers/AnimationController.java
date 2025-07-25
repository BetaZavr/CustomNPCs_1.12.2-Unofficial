package noppes.npcs.controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.util.Util;

public class AnimationController implements IAnimationHandler {

	private static AnimationController instance;
	public final TreeMap<Integer, AnimationConfig> animations = new TreeMap<>();
	public final TreeMap<Integer, EmotionConfig> emotions = new TreeMap<>();

	public static AnimationController getInstance() {
		if (AnimationController.instance == null) {
			AnimationController.instance = new AnimationController();
		}
		return AnimationController.instance;
	}

	@Override
	public IAnimation createNewAnim() {
		AnimationConfig ac = new AnimationConfig();
		ac.id = getUnusedAnimId();
		animations.put(ac.id, ac);
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
		return animations.values().toArray(new IAnimation[0]);
	}

	public List<AnimationConfig> getAnimations(List<Integer> ids) {
		List<AnimationConfig> list = new ArrayList<>();
		if (ids == null || ids.isEmpty()) { return list; }
		for (AnimationConfig ac : animations.values()) {
			for (int id : ids) {
				if (ac.getId() == id) {
					list.add(ac);
					break;
				}
			}
		}
		return list;
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
		for (int i : animations.keySet()) {
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
		AnimationConfig ac;
		if (animations.containsKey(nbtAnimation.getInteger("ID"))) {
			ac = animations.get(nbtAnimation.getInteger("ID"));
			ac.load(nbtAnimation);
			return ac;
		}
		ac = new AnimationConfig();
		ac.load(nbtAnimation);
		this.animations.put(ac.id, ac);
		return this.animations.get(nbtAnimation.getInteger("ID"));
	}

	public IEmotion loadEmotion(NBTTagCompound nbtEmotion) {
		if (nbtEmotion == null || !nbtEmotion.hasKey("ID", 3) || nbtEmotion.getInteger("ID") < 0) {
			return null;
		}
		if (this.emotions.containsKey(nbtEmotion.getInteger("ID"))) {
			this.emotions.get(nbtEmotion.getInteger("ID")).read(nbtEmotion);
			return this.emotions.get(nbtEmotion.getInteger("ID"));
		}
		EmotionConfig ec = new EmotionConfig();
		ec.read(nbtEmotion);
		this.emotions.put(nbtEmotion.getInteger("ID"), ec);
		return this.emotions.get(nbtEmotion.getInteger("ID"));
	}

	@SuppressWarnings("all")
	public void loadAnimations() {
		CustomNpcs.debugData.start(null);
		LogWriter.info("Start load animations");

		boolean needSave = false;
		File animDir;
		animations.clear();
		File emtnDir;
		emotions.clear();
		if (CustomNpcs.Dir != null) {
			File oldFile = new File(CustomNpcs.Dir, "animations.dat");
			if (oldFile.exists()) {
				try { this.loadOldAnimations(CompressedStreamTools.readCompressed(Files.newInputStream(oldFile.toPath()))); } catch (Exception e) { LogWriter.error("Error:", e); }
				Util.instance.removeFile(oldFile);
			}
			animDir = new File (CustomNpcs.Dir,  "animations");
			if (animDir.exists()) { try { loadAnimations(animDir); } catch (Exception e) { LogWriter.error("Error:", e); } }
			emtnDir = new File (CustomNpcs.Dir,  "emotions");
			if (emtnDir.exists()) { try { loadEmotions(emtnDir); } catch (Exception e) { LogWriter.error("Error:", e); } }
		}
		animDir = new File (CustomNpcs.getWorldSaveDirectory(),  "animations");
		if (animDir.exists()) {
			try { loadAnimations(animDir); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		else { needSave = true; }

		emtnDir = new File (CustomNpcs.getWorldSaveDirectory(),  "emotions");
		if (emtnDir.exists()) { try { loadEmotions(emtnDir); } catch (Exception e) { LogWriter.error("Error:", e); } }
		else { needSave = true; }
		loadDefaultAnimations();
		if (needSave) { save(); }
		LogWriter.info("End load animations");
		CustomNpcs.debugData.end(null);
	}

	private void loadOldAnimations(NBTTagCompound compound) {
		NBTTagList listA = compound.getTagList("Animations", 10);
		if (compound.hasKey("Animations", 9)) { listA = compound.getTagList("Animations", 10); }
		else if (compound.hasKey("Data", 9)) { listA = compound.getTagList("Data", 10); }
        for (int i = 0; i < listA.tagCount(); ++i) {
            AnimationConfig anim = (AnimationConfig) this.loadAnimation(listA.getCompoundTagAt(i));
            if (anim.id < 43) { anim.immutable = true; }
        }
        this.emotions.clear();
		NBTTagList listE = compound.getTagList("Emotions", 10);
        for (int i = 0; i < listE.tagCount(); ++i) {
            EmotionConfig emtn = (EmotionConfig) this.loadEmotion(listE.getCompoundTagAt(i));
            emtn.immutable = true;
        }
    }

	private void loadAnimations(File file) {
		for (File f : Objects.requireNonNull(file.listFiles())) {
			try {
				NBTTagCompound nbt = CompressedStreamTools.readCompressed(Files.newInputStream(f.toPath()));
				int id = -1;
				try { id = Integer.parseInt(f.getName().toLowerCase().replace(".dat", "")); } catch (Exception e) { LogWriter.error("Error:", e); }
				if (id != -1 && this.animations.containsKey(id)) { nbt.setInteger("ID", this.getUnusedAnimId()); }
				else { nbt.setInteger("ID", id); }
				loadAnimation(nbt);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	private void loadEmotions(File file) {
		for (File f : Objects.requireNonNull(file.listFiles())) {
			try {
				try {
					NBTTagCompound nbt = CompressedStreamTools.readCompressed(Files.newInputStream(f.toPath()));
					int id = -1;
					try { id = Integer.parseInt(f.getName().toLowerCase().replace(".dat", "")); } catch (Exception e) { LogWriter.error("Error:", e); }
					if (id != -1 && this.animations.containsKey(id)) { nbt.setInteger("ID", this.getUnusedAnimId()); }
					else { nbt.setInteger("ID", id); }
					this.loadEmotion(nbt);
				} catch (Exception e) { LogWriter.error("Error:", e); }
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	private void loadDefaultAnimations() {
		InputStream inputStream = Util.instance.getModInputStream("a_def.dat");
		if (inputStream == null) { return; }
		NBTTagCompound compound = new NBTTagCompound();
		try { compound = CompressedStreamTools.readCompressed(inputStream); } catch (Exception e) { LogWriter.error("Error:", e); }
		NBTTagList listA = compound.getTagList("Animations", 10);
		if (listA.tagCount() != 0) {
			for (int i = 0; i < listA.tagCount(); ++i) {
				NBTTagCompound nbt = listA.getCompoundTagAt(i);
				int id = nbt.getInteger("ID");
				if (animations.containsKey(id)) {
					if (!animations.get(id).immutable || !animations.get(id).name.equals(nbt.getString("Name"))) {
						boolean found = false;
						for (AnimationConfig anim : animations.values()) {
							if (anim.name.equals(nbt.getString("Name"))) {
								id = anim.id;
								found = true;
								break;
							}
						}
						if (!found) { id = this.getUnusedAnimId(); }
						nbt.setInteger("ID", id);
					}
				}
				IAnimation iAnim = loadAnimation(nbt);
				if (iAnim != null) {
					((AnimationConfig) iAnim).immutable = true;
				}
			}
		}
		NBTTagList listE = compound.getTagList("Emotions", 10);
		if (listE.tagCount() != 0) {
			for (int i = 0; i < listE.tagCount(); ++i) {
				NBTTagCompound nbt = listE.getCompoundTagAt(i);
				int id = nbt.getInteger("ID");
				if (this.emotions.containsKey(id)) {
					if (!this.emotions.get(id).immutable || !this.emotions.get(id).name.equals(nbt.getString("Name"))) {
						boolean found = false;
						for (EmotionConfig emtn : this.emotions.values()) {
							if (emtn.name.equals(nbt.getString("Name"))) {
								id = emtn.id;
								found = true;
								break;
							}
						}
						if (!found) { id = this.getUnusedEmtnId(); }
						nbt.setInteger("ID", id);
					}
				}
				this.loadEmotion(nbt);
			}
		}
	}

	@Override
	public boolean removeAnimation(int animationId) {
		if (animations.containsKey(animationId)) {
			animations.remove(animationId);
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

	@SuppressWarnings("all")
	public void save() {
		CustomNpcs.debugData.start(null);
		File animDir = CustomNpcs.getWorldSaveDirectory("animations");
		if (animDir == null) {
			CustomNpcs.debugData.end(null);
			return;
		}
		if (!animDir.exists()) { animDir.mkdirs(); }
		for (int id : animations.keySet()) {
			if (animations.get(id).immutable) { continue; }
			try { CompressedStreamTools.writeCompressed(animations.get(id).save(), Files.newOutputStream(new File(animDir, id + ".dat").toPath())); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		File emtnDir = CustomNpcs.getWorldSaveDirectory("emotions");
		if (emtnDir == null) {
			CustomNpcs.debugData.end(null);
			return;
		}
		if (!emtnDir.exists()) { emtnDir.mkdirs(); }
		for (int id : emotions.keySet()) {
			try { CompressedStreamTools.writeCompressed(this.emotions.get(id).save(), Files.newOutputStream(new File(emtnDir, id + ".dat").toPath())); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		CustomNpcs.debugData.end(null);
	}

	public void sendTo(EntityPlayerMP player) {
		if (CustomNpcs.Server != null && CustomNpcs.Server.isSinglePlayer()) {
			return;
		}
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, new NBTTagCompound());
		for (AnimationConfig ac : this.animations.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, ac.save());
		}
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.EmotionData, new NBTTagCompound());
		for (EmotionConfig ec : this.emotions.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.EmotionData, ec.save());
		}
	}

	public IEmotion[] getEmotions() {
		return this.emotions.values().toArray(new IEmotion[0]);
	}

}
