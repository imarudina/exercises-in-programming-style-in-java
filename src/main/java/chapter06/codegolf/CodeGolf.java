package chapter06.codegolf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CodeGolf {

	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(new File(args[0])); Scanner scannerStopWords = new Scanner(new File(args[1]))) {
			String data = scanner.useDelimiter("\\Z").next().replaceAll("[^a-zA-Z ]+", " ").toLowerCase();

			Set<String> stopWords = new HashSet<>(Arrays.asList(scannerStopWords.useDelimiter("\\Z").next().toLowerCase().split(",")));
			
			Map<String, Long> wordFreqs = Arrays.asList(data.split("\\s+")).stream().parallel().filter(w -> w.length() > 1 && !stopWords.contains(w))
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			
			wordFreqs.entrySet().stream().sorted((e1, e2) ->  e2.getValue().compareTo(e1.getValue())).limit(25)
				.forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
