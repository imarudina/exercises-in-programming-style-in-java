package chapter02.goforth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class GoForth {

	static Deque<Object> stack = new ArrayDeque<>();
	static Map<String, Object> heap = new HashMap<>();

	public static void main(String args[]) {
		stack.push(args[0]);
		read_file();
		filter_chars();
		scan();
		stack.push(args[1]);
		remove_stop_words();
		frequencies();
		sort();

		stack.push(0);
		
		// Check stack length against 1, because after we process
		// the last word there will be one item left
		while ((Integer) stack.peek() < 25 && stack.size() > 1) {
			heap.put("i", stack.pop());
			Object[] entry = (Object[]) stack.pop();
			System.out.println(entry[0] + " - " + entry[1]);
			stack.push(heap.get("i"));
			stack.push(1);
			stack.push((Integer) stack.pop() + (Integer) stack.pop());
		}

	}

	/**
	 * Takes a path to a file on the stack and places the entire contents of the
	 * file back on the stack.
	 */
	private static void read_file() {
		try (Scanner scanner = new Scanner(new File((String) stack.pop()))) {
			String data = scanner.useDelimiter("\\Z").next().toLowerCase();
			stack.push(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Takes data on the stack and places back a copy with all nonalphanumeric chars
	 * replaced by white space.
	 */
	private static void filter_chars() {
		// This is not in style. RE is too high-level, but using it
		// for doing this fast and short. Push the pattern onto stack
		stack.push("[^a-zA-Z ]");
		// Push the result onto the stack
		String pattern = (String) stack.pop();
		stack.push(((String) stack.pop()).replaceAll(pattern, " ").toLowerCase());
	}

	/**
	 * Takes a string on the stack and scans for words, placing the list of words
	 * back on the stack
	 */
	private static void scan() {
		// Again, split() is too high-level for this style, but using
		// it for doing this fast and short. Left as exercise.
		String content = (String) stack.pop();
		String[] words = content.split("\\s+");
		for (String word : words) {
			stack.push(word);
		}
	}

	/**
	 * Takes a list of words on the stack and removes stop words.
	 */
	private static void remove_stop_words() {
		String filename = (String) stack.pop();

		Set<String> stopWords = new HashSet<>();
		try (Scanner scanner = new Scanner(new File(filename))) {
			String content = scanner.useDelimiter("\\Z").next().toLowerCase();
			for (String w : content.split(",")) {
				stopWords.add(w);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		stack.push(stopWords);

		// add single-letter words
		Set<String> stopChars = new HashSet<>();
		for (char ch = 'a'; ch <= 'z'; ch++) {
			stopChars.add(String.valueOf(ch));
		}
		((Set<String>) stack.peek()).addAll(stopChars);

		heap.put("stop_words", stack.pop());

		// Again, this is too high-level for this style, but using it
		// for doing this fast and short. Left as exercise.
		heap.put("words", new ArrayList<String>());
		while (stack.size() > 0) {
			if (((Set<String>) heap.get("stop_words")).contains(stack.peek())) {
				stack.pop(); // pop it and drop it
			} else {
				((List<String>) heap.get("words")).add((String) stack.pop()); // pop it, store it
			}
		}

		for (String word : ((List<String>) heap.get("words"))) {
			stack.push(word); // Load the words onto the stack
		}

		heap.remove("stop_words");
		heap.remove("words"); // Not needed
	}

	/**
	 * Takes a list of words and returns a dictionary associating words with
	 * frequencies of occurrence.
	 */
	private static void frequencies() {
		heap.put("word_freqs", new HashMap<String, Integer>());
		// A little flavour of the real Forth style here...
		while (stack.size() > 0) {
			// ... but the following line is not in style, because the
			// naive implementation would be too slow
			if (((Map<String, Object>) heap.get("word_freqs")).containsKey(stack.peek())) {
				// Increment the frequency, postfix style: f 1 +
				stack.push(((Map<String, Object>) heap.get("word_freqs")).get(stack.peek())); // push f
				stack.push(1); // push 1
				stack.push((Integer) stack.pop() + (Integer) stack.pop()); // add
			} else {
				stack.push(1); // Push 1 in stack[2]
			}

			// Load the updated freq back onto the heap
			Integer f = (Integer) stack.pop();
			String w = (String) stack.pop();
			((Map<String, Object>) heap.get("word_freqs")).put(w, f);
		}

		// Push the result onto the stack
		for (Entry e : ((Map<String, Object>) heap.get("word_freqs")).entrySet()) {
			Object[] entry = new Object[2];
			entry[0] = e.getKey();
			entry[1] = e.getValue();
			stack.push(entry);
		}

		heap.remove("word_freqs"); // We don't need this variable anymore
	}

	private static void sort() {
		// Not in style, left as exercise
		stack.stream().sorted((o1, o2) -> {
			int v1 = (Integer) ((Object[]) o1)[1];
			int v2 = (Integer) ((Object[]) o2)[1];
			return (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).forEach(stack::push);
	}
}
