package chapter08.kickfforward;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class KickForward {

	private static String stopWordsFileName;

	public static void main(String[] args) {
		stopWordsFileName = args[1];
		read_file(args[0], KickForward::filter_chars);
	}

	static void read_file(String path_to_file, BiConsumer<String, BiConsumer> func) {
		try (Scanner scanner = new Scanner(new File(path_to_file))) {
			String data = scanner.useDelimiter("\\Z").next().toLowerCase();
			func.accept(data, (BiConsumer<String, BiConsumer>) KickForward::normalize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	static void filter_chars(String str_data, BiConsumer<String, BiConsumer> func) {
		func.accept(str_data.replaceAll("[^a-zA-Z ]+", " "), (BiConsumer<String, BiConsumer>) KickForward::scan);
	}

	static void normalize(String str_data, BiConsumer<String, BiConsumer> func) {
		func.accept(str_data.toLowerCase(), (BiConsumer<String[], BiConsumer>) KickForward::remove_stop_words);
	}

	static void scan(String str_data, BiConsumer<String[], BiConsumer> func) {
		func.accept(str_data.split("\\s+"), (BiConsumer<String[], BiConsumer>) KickForward::frequencies);
	}

	static void remove_stop_words(String[] words, BiConsumer<String[], BiConsumer> func) {
		Set<String> stopWords = new HashSet<>();
		try (Scanner scanner = new Scanner(new File(stopWordsFileName))) {
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

		func.accept(words_, (BiConsumer<Map<String, Integer>, BiConsumer>) KickForward::sort);
	}

	static void frequencies(String[] words, BiConsumer<Map<String, Integer>, BiConsumer> func) {
		Map<String, Integer> wordFreqs = new HashMap<>();

		for (String word : words) {
			Integer count = wordFreqs.get(word);
			if (count != null) {
				wordFreqs.put(word, count + 1);
			} else {
				wordFreqs.put(word, 1);
			}
		}

		func.accept(wordFreqs, (BiConsumer<List<Entry<String, Integer>>, Consumer>) KickForward::print_text);
	}

	static void sort(Map<String, Integer> wordFreqs, BiConsumer<List<Entry<String, Integer>>, Consumer> func) {
		List<Entry<String, Integer>> sortedWordFreqs = wordFreqs.entrySet().stream().sorted((o1, o2) -> {
			int v1 = o1.getValue();
			int v2 = o2.getValue();
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).collect(Collectors.toList());
		func.accept(sortedWordFreqs, (Consumer<Void>) KickForward::no_op);
	}

	static void print_text(List<Entry<String, Integer>> sortedWordFreqs, Consumer<Void> func) {
		sortedWordFreqs.stream().limit(25).forEach((e) -> System.out.println(e.getKey() + " - " + e.getValue()));
		func.accept(null);
	}

	static void no_op(Void v) {
	}
}
