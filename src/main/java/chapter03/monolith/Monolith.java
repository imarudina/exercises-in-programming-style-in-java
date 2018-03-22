/**
 * 
 */
package chapter03.monolith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author imarudina
 *
 */
public class Monolith {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// the global list of [word, frequency] pairs
		List<Object[]> wordFreqs = new ArrayList<>();

		// the list of stop words
		Set<String> stopWords = new HashSet<>();
		try (Scanner scanner = new Scanner(new File(args[1]))) {
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

		// iterate through the file one line at a time
		try (Reader in = new FileReader(args[0]); BufferedReader reader = new BufferedReader(in)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line += "\n";
				int startChar = -1;
				for (int i = 0; i < line.length(); i++) {
					char c = line.charAt(i);
					if (startChar == -1) {
						if (Character.isLetterOrDigit(c))
							// We found the start of a word
							startChar = i;
					} else {
						if (!Character.isLetterOrDigit(c)) {
							// We found the end of a word. Process it
							boolean found = false;
							String word = line.substring(startChar, i).toLowerCase();
							if (!stopWords.contains(word)) {
								int pair_index = 0;

								// Let's see if it already exists
								for (Object[] pair : wordFreqs) {
									if (word.equals(pair[0])) {
										pair[1] = ((Integer) pair[1]) + 1;
										found = true;
										break;
									}
									pair_index += 1;
								}
								if (!found) {
									wordFreqs.add(new Object[] { word, 1 });
								} else if (wordFreqs.size() > 1) {
									// We may need to reorder
									for (int n = pair_index; n >= 0; n--) {
										if ((Integer) wordFreqs.get(pair_index)[1] > (Integer) wordFreqs.get(n)[1]) {
											// swap
											Object[] tmp = wordFreqs.get(n);
											wordFreqs.set(n, wordFreqs.get(pair_index));
											wordFreqs.set(pair_index, tmp);
											pair_index = n;
										}
									}
								}
							}

							// Let's reset
							startChar = -1;
						}
					}
				}
			}

			for (int i = 0; i < 25; i++) {
				Object[] tf = wordFreqs.get(i);
				System.out.println(tf[0] + " - " + tf[1]);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}
