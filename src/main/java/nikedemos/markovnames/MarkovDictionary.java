package nikedemos.markovnames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class MarkovDictionary {

	public static final Random rnd = new Random();
	protected final HashMap2D<String, String, Integer> occurrences = new HashMap2D<>();
	protected int sequenceLen;

	public MarkovDictionary(String dictionary) { this(dictionary, 3); }

	public MarkovDictionary(String dictionary, int seqlen) {
		sequenceLen = 3;
		try { applyDictionary(dictionary, seqlen); } catch (Exception e) { LogWriter.error(e); }
	}

	public void applyDictionary(String dictionaryFile, int seqLen) throws IOException {
		StringBuilder input = new StringBuilder();
		ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID + ":markovnames/" + dictionaryFile);
		BufferedReader readIn = new BufferedReader(new InputStreamReader(getResource(resource), StandardCharsets.UTF_8));
		for (String line = readIn.readLine(); line != null; line = readIn.readLine()) { input.append(line).append(" "); }
		readIn.close();
		if (input.length() == 0) { throw new RuntimeException("Resource was empty: + " + resource); }
		if (sequenceLen != seqLen) {
			sequenceLen = seqLen;
			occurrences.clear();
		}
		String input_str = '[' + input.toString().toLowerCase().replaceAll("[\\t\\n\\r\\s]+", "][") + ']';
		for (int maxCursorPos = input_str.length() - 1 - sequenceLen, i = 0; i <= maxCursorPos; ++i) {
			String seqCurr = input_str.substring(i, i + sequenceLen);
			String seqNext = input_str.substring(i + sequenceLen, i + sequenceLen + 1);
			incrementSafe(seqCurr, seqNext);
            incrementSafe("_" + seqCurr + "_", "_TOTAL_");
		}
	}

	public String generateWord() {
		int allEntries = 0;
		for (Map.Entry<String, Map<String, Integer>> pair : occurrences.mMap.entrySet()) {
			String k = pair.getKey();
			if (k.startsWith("_[") && k.endsWith("_")) { allEntries += occurrences.get(k, "_TOTAL_"); }
		}
		if (allEntries == 0) { return "Noppes"; }
		int randomNumber = rnd.nextInt(allEntries);
		Iterator<Map.Entry<String, Map<String, Integer>>> it = occurrences.mMap.entrySet().iterator();
		StringBuilder sequence = new StringBuilder();
		while (it.hasNext()) {
			Map.Entry<String, Map<String, Integer>> pair2 = it.next();
			String j = pair2.getKey();
			if (j.startsWith("_[") && j.endsWith("_")) {
				int topLevelEntries = occurrences.get(j, "_TOTAL_");
				if (randomNumber < topLevelEntries) {
					sequence.append(j, 1, sequenceLen + 1);
					break;
				}
				randomNumber -= topLevelEntries;
			}
		}
		StringBuilder word = new StringBuilder();
		word.append(sequence);
		while (sequence.charAt(sequence.length() - 1) != ']') {
			int subSize = 0;
			for (Map.Entry<String, Integer> entry : occurrences.mMap.get(sequence.toString()).entrySet()) { subSize += entry.getValue(); }
			randomNumber = rnd.nextInt(subSize);
			Iterator<Map.Entry<String, Integer>> m = occurrences.mMap.get(sequence.toString()).entrySet().iterator();
			String chosen = "";
			while (m.hasNext()) {
				Map.Entry<String, Integer> entry2 = m.next();
				int occu = this.occurrences.get(sequence.toString(), entry2.getKey());
				if (randomNumber < occu) {
					chosen = entry2.getKey();
					break;
				}
				randomNumber -= occu;
			}
			word.append(chosen);
			sequence.delete(0, 1);
			sequence.append(chosen);
		}
		return getPost(word.substring(1, word.length() - 1));
	}

	public String getCapitalized(String str) {
		if (str == null || str.isEmpty()) { return str; }
		char[] chars = str.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	public String getPost(String str) { return getCapitalized(str); }

	private InputStream getResource(ResourceLocation resourceLocation) {
		ModContainer container = Loader.instance().activeModContainer();
		if (container == null) { throw new RuntimeException("Failed to find current mod while looking for resource " + resourceLocation); }
		String resourcePath = String.format("/%s/%s/%s", "assets", resourceLocation.getResourceDomain(), resourceLocation.getResourcePath());
		InputStream resourceAsStream = null;
		try { resourceAsStream = container.getMod().getClass().getResourceAsStream(resourcePath); }
		catch (Exception e) { LogWriter.error(e); }
		if (resourceAsStream != null) { return resourceAsStream; }
		throw new RuntimeException("Could not find resource " + resourceLocation);
	}

	public void incrementSafe(String str1, String str2) {
		if (occurrences.containsKeys(str1, str2)) { occurrences.put(str1, str2, occurrences.get(str1, str2) + 1); }
		else { occurrences.put(str1, str2, 1); }
	}

}
