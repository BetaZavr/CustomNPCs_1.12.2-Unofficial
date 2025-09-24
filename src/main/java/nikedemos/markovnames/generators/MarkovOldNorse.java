package nikedemos.markovnames.generators;

import net.minecraft.util.text.TextComponentTranslation;
import nikedemos.markovnames.MarkovDictionary;

public class MarkovOldNorse extends MarkovGenerator {

	public MarkovOldNorse(int seqlen) {
		markov = new MarkovDictionary("old_norse_bothgenders.txt", seqlen);
		name = new TextComponentTranslation("markov.oldNorse").toString();
	}

	@Override
	public String fetch(int gender) { return markov.generateWord(); }

}
