package nikedemos.markovnames.generators;

import java.util.Random;

import net.minecraft.util.text.TextComponentTranslation;
import nikedemos.markovnames.MarkovDictionary;

public class MarkovOldNorse extends MarkovGenerator {
	public MarkovDictionary markov2;

	public MarkovOldNorse() {
		this(4, new Random());
	}

	public MarkovOldNorse(int seqlen) {
		this(seqlen, new Random());
	}

	public MarkovOldNorse(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("old_norse_bothgenders.txt", seqlen, rng);
		this.name = new TextComponentTranslation("markov.oldNorse", new Object[0]).toString();
	}

	@Override
	public String fetch(int gender) {
		return this.markov.generateWord();
	}
}
