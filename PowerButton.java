package peter;

import java.util.Enumeration;
import java.util.Hashtable;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.Sound;

/**
 * This gives buttons more power, including the ability to change and remove button listeners. There can be up
 * to four instances of PowerButton per program, as there can only be four buttonlisteners per button per
 * program. Can only handle one wait for press at a time.
 * 
 * @author Peter Ehrlich
 * 
 */
public class PowerButton {
	private String[] mode = new String[9]; // these three lines would be better to add Button
	private boolean[] pressed = new boolean[9]; // where 'pressed' means was pressed
	private boolean[] punched = new boolean[9]; // where 'punched' means was pressed and
	// released
	private Hashtable modes = new Hashtable();
	public static final String NONE = "_none_";
	public Button ENTER = Button.ENTER;
	public Button ESCAPE = Button.ESCAPE;
	public Button LEFT = Button.LEFT;
	public Button RIGHT = Button.RIGHT;

	public PowerButton() {
		modes.put(NONE, new ButtonMode() {
			public void action() {
			}
		});
		mode[RIGHT.getId()] = NONE;
		mode[LEFT.getId()] = NONE;
		mode[ENTER.getId()] = NONE;
		mode[ESCAPE.getId()] = NONE;
		Button.setKeyClickTone(ENTER.getId(), 0);
		Button.setKeyClickTone(RIGHT.getId(), 0);
		Button.setKeyClickTone(LEFT.getId(), 0);
		Button.setKeyClickTone(ESCAPE.getId(), 0);
		resetPress();

		ButtonListener universal = new ButtonListener() {
			public void buttonPressed(Button b) {
				if (!pressed[b.getId()]) {
					pressed[b.getId()] = true;
					if (!mode[b.getId()].equals(NONE)) {
						Sound.beep(); // TODO: make this pretty. Use an array of sounds for each button
						((ButtonMode) modes.get(mode[b.getId()])).action();
					}
				}
			}

			public void buttonReleased(Button b) {
				if (pressed[b.getId()]) {
					pressed[b.getId()] = false;
					punched[b.getId()] = true;
				}
			}
		};

		ENTER.addButtonListener(universal);
		ESCAPE.addButtonListener(universal);
		LEFT.addButtonListener(universal);
		RIGHT.addButtonListener(universal);
	}

	/**
	 * Waits for press and release on button, checking every 100ms
	 * 
	 * @param b
	 * @throws InterruptedException
	 */

	public Button waitForPunch(Button[] b) throws InterruptedException {
		String[] oldmode = new String[b.length];
		for (int i = 0; i < b.length; i++) {
			oldmode[i] = mode[b[i].getId()];
			mode[b[i].getId()] = NONE;
		}
		resetPress();
		while (true) {
			for (int i = 0; i < b.length; i++) {
				if (punched[b[i].getId()]) {
					for (int j = 0; j < b.length; j++) {
						mode[b[j].getId()] = oldmode[j];
					}
					Sound.beep(); // TODO: make this pretty
					return b[i];
				}
				Thread.sleep(100);
			}
		}

	}

	public void waitForPunch(Button b) throws InterruptedException {
		String oldmode = mode[b.getId()];
		mode[b.getId()] = NONE;
		resetPress();
		while (true) {
			if (punched[b.getId()]) {
				Sound.beep(); // TODO: make this pretty
				mode[b.getId()] = oldmode;
				return;
			}
			Thread.sleep(100);
		}

	}

	private void resetPress() {
		pressed[ENTER.getId()] = false;
		pressed[ESCAPE.getId()] = false;
		pressed[LEFT.getId()] = false;
		pressed[RIGHT.getId()] = false;

		punched[ENTER.getId()] = false;
		punched[ESCAPE.getId()] = false;
		punched[LEFT.getId()] = false;
		punched[RIGHT.getId()] = false;

	}

	/**
	 * Sets the mode of a button. Mode must exist. To remove all modes, set the mode to PowerButton.NONE;
	 * 
	 * @param b
	 * @param mode
	 */
	public void setMode(Button b, String modeName) throws NullPointerException {
		if (modes.get(modeName) == null) { // first check is named mode is in the map
			throw new NullPointerException();
		}
		this.mode[b.getId()] = modeName;
	}

	/**
	 * Adds a new mode, of name name.
	 * 
	 * @param bm
	 * @param name
	 *            How this mode will be referred to
	 */
	public void addMode(String name, ButtonMode bm) {
		modes.put(name, bm);
	}

	/**
	 * Gets the names of all the modes, or possible actions, known to this PowerButton instance.
	 * 
	 * @return
	 */
	public Enumeration getModeName() {
		return modes.keys();
	}

	/**
	 * Gets the current action, or "mode" of a button.
	 * 
	 * @param b
	 * @return
	 */
	public String getMode(Button b) {
		return mode[b.getId()];
	}

}