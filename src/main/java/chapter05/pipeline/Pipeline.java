package chapter05.pipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Pipeline {

	public static void main(String[] args) {
		print_all(sort(frequencies((remove_stop_words(args[1])).apply(scan(filter_chars_and_normalize(read_file(args[0])))))).subList(0,  25));
	}

	/**
	 * Takes a path to a file and returns the entire contents of the file as a
	 * string
	 * 
	 * @param filename
	 * @return
	 */
	private static String read_file(String filename) {
		try (Scanner scanner = new Scanner(new File(filename))) {
			return scanner.useDelimiter("\\Z").next().toLowerCase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Takes a string and returns a copy with all nonalphanumeric chars replaced by
	 * white space
	 */
	private static String filter_chars_and_normalize(String data) {
		return data.replaceAll("[^a-zA-Z ]+", " ").toLowerCase();
	}

	/**
	 * Takes a string and scans for words, returning a list of words.
	 * 
	 * @return
	 */
	private static String[] scan(String data) {
		return data.split("\\s+");
	}

	/**
	 * Takes a list of words and returns a copy with all stop words removed
	 */
	private static Function<String[], String[]> remove_stop_words(String filename) {
		return (String[] words) -> {
			Set<String> stopWords = new HashSet<>();
			try (Scanner scanner = new Scanner(new File(filename))) {
				String content = scanner.useDelimiter("\\Z").next().toLowerCase();
				for (String w : content.split(",")) {
					stopWords.add(w);
				}

				for (char ch = 'a'; ch <= 'z'; ch++) {
					stopWords.add(String.valueOf(ch));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			Set<Integer> indexes = new HashSet<>();
			for (int i = 0; i < words.length; i++) {
				if (stopWords.contains(words[i])) {
					indexes.add(i);
				}
			}

			String[] words_ = new String[words.length - indexes.size()];
			int j = 0;
			for (int i = 0; i < words.length; i++) {
				if (!indexes.contains(i)) {
					words_[j] = words[i];
					j++;
				}
			}

			return words_;
		};
	}

	/**
	 * Takes a list of words and returns a dictionary associating words with
	 * frequencies of occurrence
	 * 
	 * @param words
	 * @return
	 */
	private static Map<String, Integer> frequencies(String[] words) {
		Map<String, Integer> wordFreqs = new HashMap<>();

		for (String word : words) {
			Integer count = wordFreqs.get(word);
			if (count != null) {
				wordFreqs.put(word, count + 1);
			} else {
				wordFreqs.put(word, 1);
			}
		}

		return wordFreqs;
	}

	/**
	 * Takes a dictionary of words and their frequencies and returns a list of pairs
	 * where the entries are sorted by frequency
	 * 
	 * @param object
	 * @return
	 */
	private static List<Entry<String, Integer>> sort(Map<String, Integer> word_freqs) {
		return word_freqs.entrySet().stream().sorted((o1, o2) -> {
			int v1 = o1.getValue();
			int v2 = o2.getValue();
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).collect(Collectors.toList());
	}

	/**
	 * Takes a list of pairs where the entries are sorted by frequency and print
	 * them recursively.
	 * 
	 * @param sort
	 */
	private static void print_all(List<Entry<String, Integer>> word_freqs) {
		if (word_freqs.size() > 0) {
			System.out.println(word_freqs.get(0).getKey() + " - " + word_freqs.get(0).getValue());
			print_all(word_freqs.subList(1, word_freqs.size()));
		}
	}

}
