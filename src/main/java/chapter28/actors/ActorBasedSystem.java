package chapter28.actors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Expected command line arguments:
 * <ul>
 * <li>path to input file</li>
 * <li>path to stop words file</li>
 * </ul>
 * 
 * @author imarudina
 *
 */
public class ActorBasedSystem {

	public void run(String[] args) {
		ActiveWFObject wordFreqManager = new WordFrequencyManager();

		ActiveWFObject stopWordManager = new StopWordManager();
		ActiveWFObject.send(stopWordManager, new Object[] { "init", wordFreqManager, args[1] });

		ActiveWFObject storageManager = new DataStorageManager();
		ActiveWFObject.send(storageManager, new Object[] { "init", args[0], stopWordManager });

		ActiveWFObject wfcontroller = new WordFrequencyController();
		ActiveWFObject.send(wfcontroller, new Object[] { "run", storageManager });

		// Wait for the active objects to finish
		try {
			wfcontroller.join();
			wordFreqManager.join();
			stopWordManager.join();
			storageManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ActorBasedSystem actorSystem = new ActorBasedSystem();
		actorSystem.run(args);
	}

}

abstract class ActiveWFObject extends Thread {
	protected boolean stop = false;
	private BlockingQueue<Object[]> queue = new LinkedBlockingQueue<>();

	public ActiveWFObject() {
		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (!stop) {
			try {
				Object[] message = queue.take();
				dispatch(message);

				if (message[0].equals("die")) {
					stop = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param message
	 */
	protected abstract void dispatch(Object[] message);

	public static void send(ActiveWFObject receiver, Object[] message) {
		try {
			receiver.queue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}

class DataStorageManager extends ActiveWFObject {

	private ActiveWFObject stopWordMenager;

	private String data;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ActiveWFObject#dispatch(java.lang.Object)
	 */
	@Override
	protected void dispatch(Object[] message) {
		if (message[0].equals("init")) {
			init((String) message[1], (ActiveWFObject) message[2]);
		} else if (message[0].equals("send_word_freqs")) {
			processWords((ActiveWFObject) message[1]);
		} else {
			send(stopWordMenager, message);
		}
	}

	/**
	 * @param pathToFile
	 * @param stopWordMenager
	 */
	private void init(String pathToFile, ActiveWFObject stopWordMenager) {
		this.stopWordMenager = stopWordMenager;

		try {
			try (Scanner scanner = new Scanner(new File(pathToFile))) {
				data = scanner.useDelimiter("\\Z").next().replaceAll("[^a-zA-Z ]", " ").toLowerCase();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param recipient
	 */
	private void processWords(ActiveWFObject recipient) {
		String[] words = data.split("\\s+");

		for (String w : words) {
			send(stopWordMenager, new Object[] { "filter", w });
		}

		send(stopWordMenager, new Object[] { "top25", recipient });
	}

}

class StopWordManager extends ActiveWFObject {

	private Set<String> stopWords = new HashSet<>();
	private ActiveWFObject wordFreqsManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ActiveWFObject#dispatch(java.lang.Object[])
	 */
	@Override
	protected void dispatch(Object[] message) {
		if (message[0].equals("init")) {
			init((ActiveWFObject) message[1], (String) message[2]);
		} else if (message[0].equals("filter")) {
			filter((String) message[1]);
		} else {
			// forward
			send(wordFreqsManager, message);
		}

	}

	/**
	 * @param fileName
	 * @param object
	 */
	private void init(ActiveWFObject wordFreqsManager, String stopWordsFileName) {
		try (Scanner scanner = new Scanner(new File(stopWordsFileName))) {
			String content = scanner.useDelimiter("\\Z").next().toLowerCase();
			for (String w : content.split(",")) {
				stopWords.add(w);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.wordFreqsManager = wordFreqsManager;
	}

	/**
	 * @param object
	 * @return
	 */
	private void filter(String word) {
		if (!stopWords.contains(word)) {
			send(wordFreqsManager, new Object[] { "word", word });
		}
	}

}

class WordFrequencyManager extends ActiveWFObject {

	private Map<String, Integer> wordFreqs = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see ActiveWFObject#dispatch(java.lang.Object[])
	 */
	@Override
	protected void dispatch(Object[] message) {
		if (message[0].equals("word")) {
			incrementCount((String) message[1]);
		} else if (message[0].equals("top25")) {
			top25((ActiveWFObject) message[1]);
		}
	}

	/**
	 * @param word
	 */
	private void incrementCount(String word) {
		Integer count = wordFreqs.get(word);
		if (count != null) {
			wordFreqs.put(word, count + 1);
		} else {
			wordFreqs.put(word, 1);
		}
	}

	/**
	 * @param object
	 */
	private void top25(ActiveWFObject recipient) {
		List<Entry<String, Integer>> wordFreqsResult = wordFreqs.entrySet().stream().sorted((o1, o2) -> {
			int v1 = o1.getValue();
			int v2 = o2.getValue();
			return (v1 > v2) ? -1 : ((v1 == v2) ? 0 : 1);
		}).limit(25).collect(Collectors.toList());
		send(recipient, new Object[] { "top25", wordFreqsResult });
	}

}

class WordFrequencyController extends ActiveWFObject {

	private ActiveWFObject storageManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ActiveWFObject#dispatch(java.lang.Object[])
	 */
	@Override
	protected void dispatch(Object[] message) {
		if (message[0].equals("run")) {
			run((ActiveWFObject) message[1]);
		} else if (message[0].equals("top25")) {
			display((List<Entry<String, Integer>>) message[1]);
		} else {
			throw new RuntimeException("Message not understood " + message[0]);
		}

	}

	/**
	 * @param storageManager
	 */
	private void run(ActiveWFObject storageManager) {
		this.storageManager = storageManager;
		send(storageManager, new Object[] { "send_word_freqs", this });
	}

	/**
	 * @param object
	 */
	private void display(List<Entry<String, Integer>> wordFreqs) {
		for (Entry<String, Integer> entry : wordFreqs) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}

		send(storageManager, new Object[] { "die" });

		stop = true;
	}

}