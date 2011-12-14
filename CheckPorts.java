package peter;

import G1.View;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SoundSensor;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.ColorSensor;
import lejos.nxt.addon.TiltSensor;

public class CheckPorts {
	// TODO: check if wrong sensor!
	//TODO: add more sensors
	public static boolean checkMotor(Motor m) {
		long i = System.currentTimeMillis();
		int p = m.getPower();
		m.setPower(300);
		m.rotateTo(5, true);
		while (m.getTachoCount() == 0 && System.currentTimeMillis() - i < 300)
			;
		if (m.getTachoCount() == 0)
			return false;
		m.rotateTo(0);
		m.resetTachoCount();
		m.setPower(p);
		return true;
	}

	/**
	 * returns true if the sensor is plugged in
	 * 
	 * @param s
	 * @return
	 */
	public static boolean checkSensor(SoundSensor s) {
		return (s.readValue() != 0);
	}

	public static boolean checkSensor(UltrasonicSensor us) {
		return (us.getDistance() != 0);
	}

	/**
	 * This does not work! Use manual/user interaction instead
	 * 
	 * @param t
	 * @return
	 */
	public static boolean checkSensor(TouchSensor t) {
		System.out.println(t.isPressed());
		return true;
	}

	public static boolean checkSensor(LightSensor ls) {
		return (ls.readNormalizedValue() != 0);
	}
	
	//TODO: test
	public static boolean checkSensor(TiltSensor t){
		System.out.println(t.getProductID());
		return true;
	}
	
	//TODO: test
	public static boolean checkSensor(ColorSensor cs){
		System.out.println(cs.getSensorType());
		return true;
	}

	/**
	 * Tests motors ports A, B, C for rotation, and alerts the user if they are not plugged in. Only works for
	 * tachomotors! Untested for PF and etc.
	 * 
	 * @throws InterruptedException
	 */
	public static void checkAllMotors() throws InterruptedException {
		View view = View.getInstance();
		while (!CheckPorts.checkMotor(Motor.A)) {
			if (!view.confirm("Motor @ A not plugged in.", "Rety", "Ignore")) {
				break;
			}
		}

		while (!CheckPorts.checkMotor(Motor.B)) {
			if (!view.confirm("Motor @ B not plugged in.", "Rety", "Ignore")) {
				break;
			}
		}

		while (!CheckPorts.checkMotor(Motor.C)) {
			if (!view.confirm("Motor @ C not plugged in.", "Rety", "Ignore")) {
				break;
			}
		}

		view.println("motors checked");
	}

}
