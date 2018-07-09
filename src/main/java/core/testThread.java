package core;

import dataCenter.TestingFunctionReader;
import javafx.util.Pair;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class testThread implements Runnable {
	private String threadName;

	testThread( String name) {
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		try {
			for(int i = 4; i > 0; i--) {
				System.out.println("Thread: " + threadName + ", " + i);
				// Let the thread sleep for a while.
				Thread.sleep(50);
			}
		} catch (InterruptedException e) {
			System.out.println("Thread " +  threadName + " interrupted.");
		}
		System.out.println("Thread " +  threadName + " exiting.");
	}

	public static void main(String args[]) {
		ExecutorService executor = Executors.newFixedThreadPool(3);

		for (int i = 0; i < 10; ++i) {
			testThread R1 = new testThread("Thread-" + Integer.toString(i));
			executor.execute(R1);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			System.out.println("Waiting error");
		}
		System.out.println("Finished all threads for testing");

	}
}