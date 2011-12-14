package G1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.nxt.SoundSensor;
import lejos.util.Stopwatch;

//TODO!! Currently wait/notify don't use conditions, which means if anyone calls notify, this guy starts up.
public class WatchSound implements Runnable {
	private boolean running = false;
	private SoundSensor s;
	// private File data;
	// private FileOutputStream fout;
	private Stopwatch t0;

	WatchSound(SoundSensor _s) {
		this.s = _s;
		s.setDBA(true); // this seems to reduce background noise
		t0 = new Stopwatch();
		// readyFile();

	}

	public void run() {
		while (true) {
			if (running) {
				monitorSound();
			} else {
				synchronized (this) {
					try {
						wait();  //remember, a wait releases and gets back sync when it starts and finishes
					} catch (InterruptedException e) {
						System.out.println("sound listener interrupted");
					}
				}
			}
		}
	}

	public void startSound() {
		synchronized (this) {
			running = true;
			notify();
		}
	}

	public boolean fallDetected() {
		synchronized (this) {
			return !running;
		}
	}

	/**
	 * stops the recorder and alerts whether the sound sensor heard the domino fall
	 * 
	 * @return
	 */

	public boolean stopSound() {
		synchronized (this) {
			if (running == false)
				return true;
			running = false;
			return false;
		}
	}

	private void monitorSound() {
		// String line;
		while (running == true) {
			try {

				for (int i = 0; i < 10; i++) {
					Thread.sleep(10);
					if (s.readValue() == 93) { // 93 is maximum
						running = false;
						return;
					}
					// line = t0.elapsed() + ", "+ s.readValue()+"\n";
					// writeln(line, fout);
				}
			} catch (InterruptedException e) {
			}
		}
		// try {
		// fout.flush();
		// } catch (IOException e) {
		// }
	}

	// you can ignore these methods

	// private void readyFile(){
	// data = new File("sata5");
	// if (data.exists()) {
	// data.delete();
	// }
	// try {
	// data.createNewFile();
	// } catch (IOException e1) {
	// System.out.println("file make error");
	// }
	// fout = new FileOutputStream(data);
	// }

	// private void writeln(String s, FileOutputStream fos) {
	// byte[] byteText = getBytes(s);
	// try {
	// for (int i = 0; i < byteText.length - 1; i++) {
	// fos.write((int) byteText[i]);
	// }
	// } catch (IOException e) {
	// System.out.println("write error");
	// }
	// }
	//
	// private byte[] getBytes(String inputText) {
	// // Debug Point //???
	// byte[] nameBytes = new byte[inputText.length() + 1];
	//
	// for (int i = 0; i < inputText.length(); i++) {
	// nameBytes[i] = (byte) inputText.charAt(i);
	// }
	// nameBytes[inputText.length()] = 0;
	//
	// return nameBytes;
	// }

}
