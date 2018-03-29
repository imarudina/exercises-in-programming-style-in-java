package chapter10.things;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author imarudina
 *
 */
public class Things {

	public static void main(String[] args) {
		WordFrequencyController wfc = new WordFrequencyController(args[0], args[1]);
		wfc.run();
	}

}

class WordFrequencyController {
	private DataStorageManager storage_manager;
	private StopWordManager stop_word_manager;
	private WordFrequencyManager word_freq_manager;
	
	public WordFrequencyController(String filename, String stopWordsFilename) {
		this.storage_manager = new DataStorageManager(filename);
		this.stop_word_manager = new StopWordManager(stopWordsFilename);
		this.word_freq_manager = new WordFrequencyManager();
	}

	/**
	 * 
	 */
	public void run() {
		for (String w : storage_manager.words()) {
            if (!stop_word_manager.is_stop_word(w)) {
                word_freq_manager.increment_count(w);
            }
		}

        List<Entry<String, Integer>> word_freqs = word_freq_manager.sorted();

		for (int i = 0; i < 25; i++) {
			Entry<String, Integer> tf = word_freqs.get(i);
			System.out.println(tf.getKey() + " - " + tf.getValue());
        }

	}

}

/**
 * Models the contents of the file
 */
class DataStorageManager {
	
	private String data;

	/**
	 * @param filename
	 */
	public DataStorageManager(String filename) {
		try (Scanner scanner = new Scanner(new File(filename))) {
			data = scanner.useDelimiter("\\Z").next().replaceAll("[^a-zA-Z ]+", " ").toLowerCase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the list words in storage
	 */
	public String[] words() {
		return data.split("\\s+");
	}

}

/**
 * Models the stop word filter
 */
class StopWordManager {
	
	private Set<String> stopWords;
	
	/**
	 * @param stopWordsFilename
	 */
	public StopWordManager(String filename) {
		stopWords = new HashSet<>();
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
	}

	/**
	 * @param w
	 * @return
	 */
	public boolean is_stop_word(String w) {
		return stopWords.contains(w);
	}
	
}

/**
 * Keeps the word frequency data
 */
class WordFrequencyManager {
	
	private Map<String, Integer> wordFreqs = new HashMap<>();

	/**
	 * @param wprd
	 */
	public void increment_count(String word) {

			Integer count = wordFreqs.get(word);
			if (count != null) {
				wordFreqs.put(word, count + 1);
			} else {
				wordFreqs.put(word, 1);
			}
	}
	
	/**
	 * @return
	 */
	public List<Entry<String, Integer>> sorted() {
		return wordFreqs.entrySet().stream().sorted((o1, o2) -> {
			int v1 = o1.getValue();
			int v2 = o2.getValue();
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).collect(Collectors.toList());
	}
	
}