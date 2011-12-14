package G1;

import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.SoundSensor;
import lejos.nxt.TouchSensor;
import lejos.robotics.navigation.TachoPilot;
import peter.CheckPorts;

import G1.Domino.FatalException;

/**
 * This is a gen-1 maneuver class. It can do lines, curves, and spirals
 * 
 * @author Peter Ehrlich
 * 
 */
public class Robot {
	private int increment = 32; // mm //TODO: make curves nice!
	private int minRadius = 100; // mm //will this ever even be used?
	private int distFromLast = -1;
	private static final int PAUSE = 0; // ms
	private WatchSound ws;
	private Thread wst;
	private final View view = View.getInstance();
	private final TouchSensor raiseLimitSwitch = new TouchSensor(SensorPort.S2);
	private boolean useSound = true;
	public volatile boolean pause = false;
	public final TachoPilot pilot;

	/*
	 * To test: interrupt thread for e-stop. See if motors stop, make sure all sleeps are cleanly quit.
	 */

	public Robot() {
		try {
			CheckPorts.checkAllMotors();
		} catch (InterruptedException e2) {
		}

		SoundSensor s = new SoundSensor(SensorPort.S1);

		// test sensor:
		while (true) { // why does this work but the loop not?!
			if (!CheckPorts.checkSensor(s)) {
				try {
					if (view.confirm("Sound sensor disconnected.  Please fix!", "Rety", "Disable Function")) {
					} else {
						useSound = false;
					}
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
		// while (!CheckPorts.checkSensor(s)); { //TODO: wtf?!
		// try {
		// if (view.confirm("Sound sensor disconnected. Please fix!", "Rety", "Disable Function")) {
		// } else {
		// useSound = false;
		// }
		// } catch (InterruptedException e) {
		// }
		// }

		view.println("Sound sensor ok");

		view.println("Please bump limit switch");
		while (!raiseLimitSwitch.isPressed())
			;
		while (raiseLimitSwitch.isPressed())
			;
		view.println("Limit switch ok");

		// build objects
		pilot = new TachoPilot(43, 120, Motor.B, Motor.C, true); // width was 103
		pilot.setSpeed(300);
		Motor.A.setSpeed(300);

		if (useSound) {
			ws = new WatchSound(s);
			wst = new Thread(ws);
			wst.setDaemon(true);
			wst.start();
		}

		// init robot
		try {
			raise();
		} catch (InterruptedException e) {
		}
	}

	private void raise() throws InterruptedException {
		while (pause == true) {
			Thread.yield();
		}
		Motor.A.backward();
		while (!raiseLimitSwitch.isPressed() && Motor.A.isMoving())
			Thread.yield();
		Motor.A.stop();
		Motor.A.flt();
		Thread.sleep(PAUSE);

	}

	private void lower() throws InterruptedException {
		while (pause == true)
			Thread.yield();
		Motor.A.rotateTo(40);//all the way pushed out
		Motor.A.rotateTo(0);  //TODO: use positioning relative to limit switch instead.
		Thread.sleep(PAUSE);
	}

	/**
	 * places one domino, check that it fell.
	 * 
	 * @throws InterruptedException
	 */
	private void placeDomino() throws InterruptedException {
		boolean redo = false;
		do {
			ws.startSound();
			lower();
			if (!ws.fallDetected()) {
				// shake it:
				Sound.beep();
				Motor.A.rotateTo(-100);
				Thread.sleep(PAUSE);
				Motor.A.rotateTo(0);
				Thread.sleep(PAUSE);
			}

			redo = !ws.stopSound();
			if (redo) {
				if (!view.confirm("Please refill dominos", "Done", "False Negative")) {
					redo = false;
				}
			}
			raise();
		} while (redo == true);
	}

	/**
	 * Moves in mm
	 * 
	 * @param d
	 * @throws InterruptedException
	 */
	private void increment(int d) throws InterruptedException {
		while (pause == true)
			Thread.yield();
		pilot.travel(d);
		Thread.sleep(PAUSE);
		// TODO actions - wait, what does this even mean??
	}

	/**
	 * moves a distance s along and arc of radius r. Positive is curve to the right
	 * 
	 * @param s
	 *            distance to increment (mm)
	 * @param r
	 *            radius of curvature (mm)
	 * @throws InterruptedException
	 */
	private void increment(int s, int r) throws InterruptedException {
		while (pause == true)
			Thread.yield();
		// s = rad*r
		int t = (int) Math.abs(Math.round(Math.toDegrees((float) s / r)));  //neg here would make robot go backwards
		pilot.arc(r, t); // loss of precision?
		Thread.sleep(PAUSE);
	}

	/**
	 * each lay_ assumes that gate is up. takes distance in cm.
	 * 
	 * @param d
	 * @throws InterruptedException
	 */

	public void layLine(int d) throws InterruptedException {
		view.println("LayLine: " + Integer.toString(d) + "cm");
		d *= 10; // convert to mm

		if (distFromLast != -1) { // TODO: this assumes d > increment
			increment(increment - distFromLast);
			d -= (increment - distFromLast);
		}
		placeDomino();

		int count = (int) Math.floor(d / increment);
		for (int i = 0; i < count; i++) {
			increment(increment);
			placeDomino();
		}
		distFromLast = d - increment * count;
		increment(distFromLast); // travel the remainder
		return;
	}

	/**
	 * 
	 * @param theta
	 *            arc (degrees)
	 * @param r
	 *            radius of curvature (cm)
	 * @throws InterruptedException
	 */

	public void layArc(int theta, int r) throws InterruptedException {
		view.println("LayArc: " + Float.toString(theta) + ", " + Float.toString(r));
		if(theta < 0){  //theta always positive, neg r means opposite dir
			theta *=-1;
			r *= -1;
		}
		r *= 10;
		// s = rad r
		int s = (int) Math.abs(Math.round(r * Math.toRadians(theta)));

		int incAdjust = Math.abs(Math.round(11f * ((float)increment / (float)r)));// 1/2 domino width times tan(theta) where theta is the
		// angle traeled over the incrment
		increment += incAdjust;
		view.println("new inc: " + increment);
		// apply formula based upon curvature here (???)

		if (distFromLast != -1) { // TODO: this assumes d > increment
			increment(increment - distFromLast, r);
			s -= (increment - distFromLast);
		}
		placeDomino();
		
		int count = (int) Math.floor(s / increment);
		for (int i = 0; i < count; i++) {
			increment(increment, r);
			placeDomino();
		}
		distFromLast = s - increment * count;
		increment(distFromLast, r); // travel the remainder
		increment -= incAdjust;
		return;
	}

	/**
	 * Lays Arcimidian spiral.
	 */

	public void layASpiral(float rotations) throws InterruptedException {
		view.println("A-Spiral: " + Float.toString(rotations));

		double theta = 0, stepAngle, stepRadius;
		double radius = minRadius;
		double radiusStepPerDeg = minRadius / 360;
		int oldInc = increment;
		int incAdjust, turnRate;
		rotations *= 360;

		do {
			placeDomino();
			incAdjust = (int) Math.round(11 * (increment / radius));// 1/2 domino width times tan(theta)
			// where theta is the angle traeled over the incrment
			increment += incAdjust;
			stepAngle = Math.toDegrees(increment / radius);// s/r
			stepRadius = radiusStepPerDeg * stepAngle;
			radius += stepRadius;
			theta += stepAngle;
			pilot.arc((float) radius, (int) Math.round(stepAngle));
		} while (theta + stepAngle < rotations);
		pilot.arc((float) radius, (int) Math.round(stepAngle));
		distFromLast =
		// turnRate =
		// pilot.steer(turnRate, (int)Math.round(stepAngle)); //steer(how much % slower one than the
		// other)

		increment = oldInc;
	}

	/**
	 * Lays Fermat spiral.
	 */

	public void layFSpiral(float r) {

		view.println("F-Spiral: " + Float.toString(r));
		doSleep(1000);
	}

	public void split() {
		// TODO: figure this shit out
		view.println("split");
		doSleep(1000);
	}

	public void join() {
		// TODO: figure this shit out
		view.println("join");
		doSleep(1000);
	}

	/**
	 * Lays a bezier line
	 */
	public void layBezier(float r) {// scale??

		// TODO everyting
		view.println("bezier: " + Float.toString(r));
		doSleep(1000);
	}

	private static void doSleep(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			LCD.drawString("Asleep error, t=", 1, 1);
			LCD.drawInt(t, 2, 2);
		}
	}

	/**
	 * shutdown procdures. Includes lowering gait to reduce strain on components
	 */
	public void shutDown() {
		view.println("closing robot");
		if (distFromLast != -1) {
			pilot.travel(2 * increment - distFromLast); // make some distance, preserve
			// spacing
		}
		Motor.A.rotateTo(0); // dont call lower because of pause

	}

	/**
	 * Sets the spacing between dominos, in mm.
	 * 
	 * @param i
	 */
	public void setIncrement(int i) {
		increment = i;
	}

	/**
	 * set the minimum radius of curvature that the path finder wil use, in mm.
	 * 
	 * @param m
	 */
	public void setMinRadius(int m) {
		minRadius = m;
	}
}

/*
 * 
 * Knows where it is, where the head is. Responsible for cartesian accuracy. Responsible for deciding whether
 * to move the arm or the Chassis.
 * 
 * private: float increment; //the current and default increment, in meters float minradius: //the minimum
 * curve radius void place() throws Jam, empty; void increment() throws Jam; void increment(r) throws Jam;
 * //increment for arc of radius r void wasSound(); void shutDown(); //lowers the gate, unwinds the pusher,
 * saves vars to ini file
 * 
 * 
 * public: Maneuver(); //Raises the gate layLine(float distance) throws Jam, empty; layArc(float s, float r)
 * throws Jam, empty; laySplit() throws Jam, empty; //lays out two lines the same shape as if they were one.
 * layJoin() throws Jam, empty; Joins...? laySplit(Queue line1, Queue line2, bool firstMaster) throws Jam,
 * empty; //lays out two lines after a split, one 10cm chunk at a time. Works until both say to rejoin, then
 * has 2 plot a line to one. layASpiral(radius) throws Jam, empty;//Archamidian spiral JavaDoc: starting or
 * ending move only layFSpiral(radius) throws Jam, empty;//Fermat's spiral layBezier(Bezier b); //figure this
 * out later
 * 
 */