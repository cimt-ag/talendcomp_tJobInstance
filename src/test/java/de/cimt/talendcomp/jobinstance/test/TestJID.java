package de.cimt.talendcomp.jobinstance.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.cimt.talendcomp.jobinstance.manage.JID;

public class TestJID {
	
	private long lastId = 0;
	private JID jid = new JID();

	@Test
	public void testCreateJIDInBulk() throws Exception {
		JID jid = new JID();
		long lastId = 0;
		for (int t = 0; t < 1000; t++) {
			for (int i = 0; i < 100000; i++) {
				long id = jid.createJID();
				if (id <= lastId) {
					throw new Exception("Same or lower id found:" + id + " i: " + i + " lastId: " + lastId);
				}
				lastId = id;
			}
			System.out.println("Iteration: " + t + " lastId: " + lastId + " currentMillis: " + jid.getCurrentMillisecond() + " sequenceValue: " + jid.getSequenceValue());
			if (t % 2 == 0) {
				Thread.sleep(500);
			}
		}
	}
	
	@Test
	public void testParallel() throws Exception {
		List<Thread> listProcesses = new ArrayList<Thread>();
		for (int i = 0; i < 100; i++) {
			Thread process = new Thread() {
				@Override
				public void run() {
					try {
						runDummyProcess();
					} catch (Exception e) {
						throw new RuntimeException("Thread: " + Thread.currentThread().getName() + " failed: " + e.getMessage());
					}
				}
			};
			process.setName("Process_" + i);
			process.start();
			listProcesses.add(process);
			Thread.sleep(100);
		}
		boolean running = true;
		while (running) {
			running = false;
			for (Thread t : listProcesses) {
				if (t.isAlive()) {
					running = true;
				}
			}
			Thread.sleep(100);
		}
		assertTrue(true);
	}
	
	@Test
	public void runDummyProcess() throws Exception {
		System.out.println("Thread: " + Thread.currentThread().getName() + " START");
		JID jid = new JID();
		for (int i = 0; i < 10; i++) {
			long id = jid.createJID();
			final long locLastId = lastId;
			if (id == locLastId) {
				throw new Exception("Same or lower id found:" + id + " i: " + i + " lastId: " + locLastId);
			}
			lastId = id;
			Thread.sleep(100);
		}
		System.out.println("Thread: " + Thread.currentThread().getName() + " END with lastId: " + lastId);
	}
	
	    
}
