package peter;

import java.util.ArrayList;
import java.util.Iterator;

import lejos.nxt.Motor;
import lejos.nxt.Sound;

public class WatchForStalls implements Runnable {
	private boolean[] wasStalled;
	private int[] oldSpeed;
	private int index, newSpeed;
	ArrayList<Motor> motors;
	private int limit = 110;
	public volatile Boolean stalled = false;

	/**
	 * This watches for motor stalls on all motors passed. A motor is considered stalled if it has a
	 * speed below the specified limit 200ms in a row, or if it has a decreasing speed below limit. This
	 * is to cover motors starting in locked states as well as fast response time for stalling motors.
	 * :-( boo stalls.
	 * 
	 * @param _motors
	 */
	public WatchForStalls(ArrayList<Motor> _motors) {
		motors = _motors;
		wasStalled = new boolean[motors.size()]; // all false
		oldSpeed = new int[motors.size()]; // all 0
		System.out.println("WFS constructor");
	}

	public void run() {
		System.out.println("Watching stalls");

		while (!Thread.interrupted()) {
			index = 0;
			for (Motor m : motors) {
				newSpeed = m.getActualSpeed();
				if (m.isRotating()) {
					if (newSpeed < limit) {
						if (oldSpeed[index] > newSpeed || wasStalled[index]) {
							if (oldSpeed[index] > newSpeed){
								Sound.beep();
							}
							stalled = true;
							m.flt();
							wasStalled[index]=false;

						}
						wasStalled[index] = true;
					}
				}else{
					oldSpeed[index]=0;
				}
				oldSpeed[index] = newSpeed;
				index++;
				// TODO: this will miss a chunk abut once every 20-50 (actually more, this probably takes
				// at least 10ms)
				try {
					Thread.sleep(95);
				} catch (InterruptedException e) {
				}

			}

		}

	}

	public void setLimit(int _limit) {
		limit = _limit;
	}

}
