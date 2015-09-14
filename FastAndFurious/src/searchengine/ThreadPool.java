package searchengine;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ThreadPool {
	public final static int poolCapacity = 2;
	
	private List<Thread> allThreads;
	
	public ThreadPool(BlockingQueue<String> words, HashMap<String, Double> vals) {
		
		allThreads = new ArrayList<Thread>(poolCapacity);
		for (int i = 0; i < poolCapacity; i++) {
			allThreads.add(new Thread(new Worker(words,vals)));
		}
		start();
	}
	
	public void start() {
		for (Thread thread : allThreads) {
//			thread.setDaemon(true); // a way to terminate but not as good
			thread.start();
		}
	}
	
	public synchronized void stop() {
		// wake up all the threads
		notifyAll();
		// allow time to respond to the wake up call 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) { }
		for (Thread thread : allThreads) {
			// wait for thread to finish request processing
			while (true) {
				if (thread.getState() != Thread.State.RUNNABLE) {
//					thread.stop(); // deprecated - Don't use
					thread.interrupt();
					break;
				}
			}
		}
	}
	
	public void status() {
		for (Thread thread : allThreads) {
			System.out.println(thread.getName() + ": " + thread.getState());
		}
	}
}
