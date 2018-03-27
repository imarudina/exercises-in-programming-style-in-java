package chapter09.theone;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TheOne {

	private static String stopWordsFileName; // TODO - remove

	public static void main(String[] args) {
		stopWordsFileName = args[1];

		new TFTheOne(args[0])
		.bind(TheOne::read_file)
		.bind(TheOne::filter_chars)
		.bind(TheOne::normalize)
		.bind(TheOne::scan)
		.bind(TheOne::remove_stop_words)
		.bind(TheOne::frequencies)
		.bind(TheOne::sort)
		.bind(TheOne::top25_freqs).printme();
	}

	//
	// The functions
	//
	private static Object read_file(Object filename) {
		try (Scanner scanner = new Scanner(new File((String) filename))) {
			return scanner.useDelimiter("\\Z").next().toLowerCase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static Object filter_chars(Object data) {
		return ((String) data).replaceAll("[^a-zA-Z ]+", " ");
	}

	private static Object normalize(Object data) {
		return ((String) data).toLowerCase();
	}

	private static Object scan(Object str_data) {
		return ((String) str_data).split("\\s+");
	}

	private static Object remove_stop_words(Object word_list) {
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

		String[] words = (String[]) word_list;
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
	}

	private static Object frequencies(Object words_arr) {
		Map<String, Integer> wordFreqs = new HashMap<>();

		String[] words = (String[]) words_arr;
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

	private static Object sort(Object word_freqs_map) {
		return ((Map<String, Integer>) word_freqs_map).entrySet().stream().sorted((o1, o2) -> {
			int v1 = o1.getValue();
			int v2 = o2.getValue();
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).collect(Collectors.toList());
	}

	private static Object top25_freqs(Object word_freqs_list) {
		List<Entry<String, Integer>> word_freqs = (List<Entry<String, Integer>>) word_freqs_list;
		StringBuilder buf = new StringBuilder();
		
		for (int i = 0; i < 25; i++) {
			Entry<String, Integer> tf = word_freqs.get(i);
			buf.append(tf.getKey() + " - " + tf.getValue() + "\n");
		}
		
		return buf.toString();
	}
}

class TFTheOne {
	private Object _value;

	TFTheOne(Object v) {
		this._value = v;
	}

	public TFTheOne bind(Function<Object, Object> func) {
		this._value = func.apply(this._value);
		return this;
	}

	public void printme() {
		System.out.println(this._value);
	}

}