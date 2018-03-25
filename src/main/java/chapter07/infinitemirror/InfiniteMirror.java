package chapter07.infinitemirror;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class InfiniteMirror {

	public static final int RECURSION_LIMIT = 1000;
	
	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(new File(args[0])); Scanner scannerStopWords = new Scanner(new File(args[1]))) {
			Set<String> stopWords = new HashSet<>(Arrays.asList(scannerStopWords.useDelimiter("\\Z").next().toLowerCase().split(",")));

			List<String> words = Arrays.asList(scanner.useDelimiter("\\Z").next().replaceAll("[^a-zA-Z ]+", " ").toLowerCase().split("\\s+"));

			Map<String, Integer> word_freqs = new HashMap<>();

			// Theoretically, we would just call count(words, word_freqs)
			// Try doing that and see what happens.
			for (int i = 0; i < words.size(); i += RECURSION_LIMIT) {
				count(words.subList(i, Math.min(i + RECURSION_LIMIT, words.size())), stopWords, word_freqs);
			}

			List<Entry<String, Integer>> word_freq_list = word_freqs.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).collect(Collectors.toList());
			
			wf_print(word_freq_list.subList(0, 25));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static void count(List<String> words, Set<String> stopWords, Map<String, Integer> word_freqs) {
		// What to do with an empty list
		if (words.isEmpty()) {
			return;
		} else {
			// The inductive case, what to do with a list of words
			// Process the head word
			String word = words.get(0);
			if (word.length() > 1 && !stopWords.contains(word)) {
				Integer count = word_freqs.get(word);
				if (count != null) {
					word_freqs.put(word, count + 1);
				} else {
					word_freqs.put(word, 1);
				}
			}

			// Process the tail
			count(words.subList(1, words.size()), stopWords, word_freqs);
		}
	}

	private static void wf_print(List<Entry<String, Integer>> wordFreqList) {
		if (wordFreqList.isEmpty()) {
			return;
		} else {
			Entry<String, Integer> wordfreq = wordFreqList.get(0);
			System.out.println(wordfreq.getKey() + " - " + wordfreq.getValue());
			wf_print(wordFreqList.subList(1, wordFreqList.size()));
		}
	}

}
