package chapter04.cookbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author imarudina
 *
 */
public class Cookbook {

	// the global list of [word, frequency] pairs
	static List<Object[]> word_freqs = new ArrayList<>();

	static String data;

	static String[] words;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		read_file(args[0]);
		filter_chars_and_normalize();
		scan();
		remove_stop_words(args[1]);
		frequencies();
		sort();

		for (int i = 0; i < 25; i++) {
			Object[] tf = word_freqs.get(i);
			System.out.println(tf[0] + " - " + tf[1]);
		}
	}

	/**
	 * Takes a path to a file and assigns the entire contents of the file to the
	 * global variable data
	 * 
	 * @param filename
	 */
	private static void read_file(String filename) {
		try (Scanner scanner = new Scanner(new File(filename))) {
			data = scanner.useDelimiter("\\Z").next().toLowerCase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Replaces all nonalphanumeric chars in data with white space
	 */
	private static void filter_chars_and_normalize() {
		char[] data_ = data.toCharArray();
		for (int i = 0; i < data_.length; i++) {
			if (!Character.isLetterOrDigit(data_[i])) {
				data_[i] = ' ';
			} else {
				data_[i] = Character.toLowerCase(data_[i]);
			}
		}
		data = new String(data_);
	}

	/**
	 * Scans data for words, filling the global variable words
	 */
	private static void scan() {
		words = data.split("\\s+");
	}

	/**
	 * 
	 */
	private static void remove_stop_words(String filename) {
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

		words = words_;
	}

	/**
	 * Creates a list of pairs associating words with frequencies
	 */
	private static void frequencies() {
		for (String w : words) {
			int index = -1, i = 0;
			for (Object[] entry : word_freqs) {
				if (w.equals(entry[0])) {
					index = i;
					break;
				}
				i++;
			}
			if (index != -1) {
				word_freqs.set(index, new Object[] { w, ((Integer) word_freqs.get(index)[1]) + 1 });
			} else {
				word_freqs.add(new Object[] { w, 1 });
			}
		}

	}

	/**
	 * Sorts word_freqs by frequency
	 */
	private static void sort() {
		word_freqs.sort((o1, o2) -> {
			int v1 = (Integer) o1[1];
			int v2 = (Integer) o2[1];
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		});
	}

}
