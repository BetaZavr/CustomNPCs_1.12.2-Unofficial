package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.constants.EnumPacketClient;

public class AnimationController
implements IAnimationHandler {
	
	private static AnimationController instance;
	public TreeMap<Integer, IAnimation> animations;
	public static int version = 0;
	private String filePath;
	
	public AnimationController() {
		this.filePath = "";
		AnimationController.instance = this;
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
		for (int i : this.animations.keySet()) { if (i>=id) { id = i + 1; } }
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
		if (this.animations.size()==0) {
			this.loadDefaultAnimations(-1);
		}
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
		AnimationConfig ac = new AnimationConfig(0);
		ac.readFromNBT(nbtAnimation);
		this.animations.put(nbtAnimation.getInteger("ID"), ac);
		return this.animations.get(nbtAnimation.getInteger("ID"));
	}

	private void loadDefaultAnimations(int version) {
		if (version == AnimationController.version) { return; }
		AnimationConfig anim = (AnimationConfig) this.createNew(AnimationKind.STANDING.get());
		anim.name = "Animation Test";
		// Head
		// frame 0
		AnimationFrameConfig frame = anim.frames.get(0);
		frame.speed = 10;
		// frame 1
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[0].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[0].rotation[0] = 0.625f;
		frame.parts[0].offset[1] = 0.52f;
		frame.parts[0].offset[2] = 0.47f;
		// frame 2
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[0].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[0].rotation[1] = 0.375f;
		frame.parts[0].offset[0] = 0.52f;
		// frame 3
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[0].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[0].rotation[0] = 0.375f;
		frame.parts[0].offset[1] = 0.48f;
		frame.parts[0].offset[2] = 0.47f;
		// frame 4
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[0].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[0].rotation[1] = 0.625f;
		frame.parts[0].offset[0] = 0.48f;
		// frame 5
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[0].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[0].rotation[0] = 0.625f;
		frame.parts[0].offset[1] = 0.52f;
		frame.parts[0].offset[2] = 0.47f;
		
		// Body
		// frame 6
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[3].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[3].offset[2] = 0.425f;
		// frame 7
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[3].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[3].rotation[1] = 0.25f;
		frame.parts[3].offset[0] = 0.575f;
		// frame 8
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[3].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[3].rotation[1] = 0.0f;
		frame.parts[3].offset[2] = 0.575f;
		// frame 9
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[3].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[3].rotation[1] = 0.75f;
		frame.parts[3].offset[0] = 0.425f;
		// frame 10
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[3].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[3].offset[2] = 0.425f;
		
		// Arms
		// frame 11
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		// frame 12
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[1].setScale(1.25f, 1.25f, 1.25f);
		frame.parts[2].setScale(1.25f, 1.25f, 1.25f);
		frame.parts[1].rotation[0] = 0.25f;
		frame.parts[2].rotation[0] = 0.75f;
		frame.parts[1].offset[0] = 0.525f;
		frame.parts[2].offset[0] = 0.475f;
		// frame 13
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[1].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[2].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[1].rotation[0] = 0.0f;
		frame.parts[2].rotation[0] = 0.0f;
		frame.parts[1].offset[0] = 0.55f;
		frame.parts[2].offset[0] = 0.45f;
		// frame 14
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[1].setScale(1.25f, 1.25f, 1.25f);
		frame.parts[2].setScale(1.25f, 1.25f, 1.25f);
		frame.parts[1].rotation[0] = 0.75f;
		frame.parts[2].rotation[0] = 0.25f;
		frame.parts[1].offset[0] = 0.525f;
		frame.parts[2].offset[0] = 0.475f;
		
		// Legs
		// frame 15
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		// frame 16
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[4].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[5].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[4].rotation[0] = 0.25f;
		frame.parts[5].rotation[0] = 0.25f;
		frame.parts[4].offset[0] = 0.525f;
		frame.parts[4].offset[1] = 0.515f;
		frame.parts[5].offset[0] = 0.475f;
		frame.parts[5].offset[1] = 0.515f;
		// frame 17
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[4].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[5].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[4].rotation[0] = 0.25f;
		frame.parts[4].rotation[1] = 0.25f;
		frame.parts[5].rotation[0] = 0.25f;
		frame.parts[5].rotation[1] = 0.75f;
		frame.parts[4].offset[0] = 0.525f;
		frame.parts[4].offset[1] = 0.515f;
		frame.parts[5].offset[0] = 0.475f;
		frame.parts[5].offset[1] = 0.515f;
		// frame 18
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[4].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[5].setScale(1.5f, 1.5f, 1.5f);
		frame.parts[4].rotation[0] = 0.25f;
		frame.parts[4].rotation[1] = 0.0f;
		frame.parts[5].rotation[0] = 0.25f;
		frame.parts[5].rotation[1] = 0.0f;
		frame.parts[4].offset[0] = 0.525f;
		frame.parts[4].offset[1] = 0.515f;
		frame.parts[5].offset[0] = 0.475f;
		frame.parts[5].offset[1] = 0.515f;
		// frame 19
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[4].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[5].setScale(1.3f, 1.3f, 1.3f);
		frame.parts[4].rotation[0] = 0.25f;
		frame.parts[4].rotation[1] = 0.75f;
		frame.parts[5].rotation[0] = 0.25f;
		frame.parts[5].rotation[1] = 0.25f;
		frame.parts[4].offset[0] = 0.525f;
		frame.parts[4].offset[1] = 0.515f;
		frame.parts[5].offset[0] = 0.475f;
		frame.parts[5].offset[1] = 0.515f;
		// frame 20
		frame = (AnimationFrameConfig) anim.addFrame();
		frame.speed = 10;
		frame.parts[4].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[5].setScale(1.15f, 1.15f, 1.15f);
		frame.parts[4].rotation[0] = 0.25f;
		frame.parts[5].rotation[0] = 0.25f;
		frame.parts[4].offset[0] = 0.525f;
		frame.parts[4].offset[1] = 0.515f;
		frame.parts[5].offset[0] = 0.475f;
		frame.parts[5].offset[1] = 0.515f;
	}
	
	public void save() {
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "animations.dat")));
		}
		catch (Exception e) { }
	}
	
	public void sendTo(EntityPlayerMP player) {
		if (CustomNpcs.Server!=null && CustomNpcs.Server.isSinglePlayer()) { return; }
		Server.sendData(player, EnumPacketClient.SYNC_UPDATE, 7, new NBTTagCompound());
		for (IAnimation ac : this.animations.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, 7, ((AnimationConfig) ac).writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		List<IAnimation> list = Lists.<IAnimation>newArrayList();
		for (IAnimation ac : this.animations.values()) {
			if (((AnimationConfig) ac).getType()==animationType) {
				list.add(ac);
			}
		}
		return list.toArray(new IAnimation[list.size()]);
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
	public IAnimation createNew(int animationType) {
		AnimationConfig ac = new AnimationConfig(0);
		int id = this.getUnusedId();
		this.animations.put(id, ac);
		this.save();
		return this.animations.get(id);
	}
	
}
