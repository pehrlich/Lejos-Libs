package G1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import peter.ButtonMode;
import peter.PowerButton;
import peter.Property;

import lejos.nxt.*;
import lejos.util.TextMenu;

/**
 * Currently only one dialog popup is supported, and it is assumed no more dialog calls will be made while its
 * up. Only lines printed with this class's functions will be restored on dialog close.
 * 
 * @author Peter Ehrlich
 * 
 */
public class View {
	private int debug = 9;
	private Vector currentLines = new Vector(7); // 7 rows of screen
	public final PowerButton pb = new PowerButton(); // remove this?
	private boolean suppressOut = false;
	private String oldMode;

	// example:
	// ButtonMode test1 = new ButtonMode(){
	// public void action() {
	// System.out.println("test1 O.K.");
	// }
	// };
	// pb.addMode("test1", test1);
	// pb.setMode(pb.LEFT, "test1");


	private View() {}
	
	private static class ViewHolder{
		private static final View INSTANCE = new View();
	}
	
	public static View getInstance(){
		return ViewHolder.INSTANCE;
	}

	/**
	 * Makes a selection menu for files of of type fType. Rerturns the file name, and creates a new file if
	 * necessary. Should not be used while a file is open!
	 * @throws IOException 
	 */
	
	//TODO: handle lack of .line files.
	public File pickFile(String fType, boolean newField) throws IOException {
		suppressOut = true;
		File[] f = File.listFiles();
		if (f == null){
			System.out.println("no files found!");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			throw new IOException("no files");
		}
		int s = 0;
		// first count the relevant files:
		for (int i = 0; i < f.length; i++) {
			try {
				if (f[i].getName().lastIndexOf(fType) + fType.length() == f[i].getName().length()) {
					s++;
				}
			} catch (NullPointerException e) {
				break;
			}
		}
		System.out.println(s);


		int flag = 0;
		if (newField == true)
			flag = 1;
		String[] titles = new String[s + flag];// relavent filtered non null files

		int r = 0; // leave extra spot for "new file" field
		// now make the String for the menu
		for (int i = 0; i < f.length; i++) {
			if (f[i].getName().lastIndexOf(fType) + fType.length() == f[i].getName().length()) {
				titles[r] = f[i].getName(); // make extra spot at end
				r++; //TODO why does this work?!
				if (r == titles.length-1)
					break;
			}
		}

		if (newField == true) {
			titles[titles.length - 1] = "New " + fType;
		}

		TextMenu t = new TextMenu(titles, 0, "select " + fType.substring(1));

		// wait for user response
		takeScreen();
		
		int ret = t.select();
		returnScreen();

		if (ret == -1) { // escape
			return null;
		}

		if (ret == titles.length - 1) {
			File newF;
			int i = 1;
			while (true) {
				newF = new File("On the go " + i + fType);
				if (!newF.exists()) {
					try {
						newF.createNewFile();
					} catch (IOException e) {
						printErr("IEO making new file");
					}
					return newF;
				}
				i++;
			}
		}

		Sound.beepSequence();
		return new File(titles[ret]);
	}

	/**
	 * Asks a question and then presents multiple options.
	 * 
	 * @param message
	 *            max length: 32 chars
	 * @param options
	 *            max lenghts: 5 options, 15 chars each.
	 * @return
	 */

	// TODO: nice sounds?
	public int option(String message, String[] options) {
		takeScreen();
		printTop(message);
		int selected = 0;
		reDrawOptions(options, selected);

		while (true) { // TODO: allow menu closure
			while (Button.readButtons() > 0)
				Thread.yield();// wait for release
			try {
				Thread.sleep(20);
			} catch (InterruptedException ie) {
			} // wait to stabilize
			int button = Button.readButtons();
			if (button == 1) {
				Sound.beep();
				returnScreen();
				return selected;
			}
			if (button == 8) {
				Sound.beep();
				returnScreen();
				return -1; // Escape
			}
			if (button == 4) {// scroll forward
				Sound.beep();
				selected++;
				if (selected >= options.length) {
					selected = 0;
				}
				reDrawOptions(options, selected);
			}
			if (button == 2) {// scroll backward
				Sound.beep();
				selected--;
				if (selected < 0) {
					selected = options.length - 1;
				}
				reDrawOptions(options, selected);
			}
		}
	}

	private void reDrawOptions(String[] options, int selected) {
		int diff = LCD.DISPLAY_CHAR_DEPTH - options.length;

		for (int i = options.length - 1; i >= 0; i--) {
			LCD.drawString(options[i], 1, i + diff);
			if (i == selected) {
				LCD.drawString(">", 0, i + diff);
			} else {
				LCD.drawString(" ", 0, i + diff);
			}
		}
	}

	/**
	 * Displays a TextMenu with each property, as well as the option 'done' at the top. If a property is
	 * selected and incremented with the side buttons. Pressing a side button with the enter key will change
	 * the incrment for that variable. Ints ending in 0 or 5have a default incrment 5, else 1. The same goes
	 * for floats, with their smallest digit.
	 * 
	 * Returns 0 if good, -1 if ESC>
	 * 
	 * @param properties
	 * @return
	 * @throws InterruptedException
	 */

	public int configMenu(ArrayList<Property> props) throws InterruptedException {
		String[] titles = new String[props.size() + 1];
		titles[0] = "Done";

		for (int i = 0; i < props.size(); i++){
			titles[i + 1] = makeTitle(props.get(i).getName(), props.get(i).getStrVal());
		}

		int sel;
		TextMenu t;
		while (true) {
			t = new TextMenu(titles);
			sel = t.select();
			if (sel == 0 || sel == -1)
				return sel;
			adjust(props.get(sel-1));
			titles[sel] = makeTitle(props.get(sel-1).getName(), props.get(sel-1).getStrVal());
		}
	}


	/**
	 * Allows the user to adjust a parameter. Supports increments, double increments, and old value reversion.
	 * 
	 * @param p
	 * @return
	 */

	public void adjust(Property p) {
		takeScreen();
		String val = p.getStrVal();
		printCentered("Adjust value:", 1);
		printCentered(p.getName(), 3);
		printCentered(val, 4);
		printCentered("- ok -", 6);
		if (val.length() > 1){ //avoid strlen 1 bug
			printCentered("- Revert ("+ val +") -", 7);
		}else{
			printCentered("- Revert -", 7);
		}
		int b;
		while (true) {
			b = Button.waitForPress();
			if (b == 1) { // enter,esc
				break;
			} else if(b==8){
				p.setVal(val);
				break;
			}
			else if (b == 2) {// left
				p.minus();
				LCD.drawString("                ", 0, 4);
				printCentered(p.getStrVal(), 4);
			} else if (b == 4) {// right
				p.plus();
				LCD.drawString("                ", 0, 4);
				printCentered(p.getStrVal(), 4);
			} else if (b == 5) {
				p.bigPlus();
				LCD.drawString("                ", 0, 4);
				printCentered(p.getStrVal(), 4);
			} else if (b == 3) {
				p.bigMinus();
				LCD.drawString("                ", 0, 4);
				printCentered(p.getStrVal(), 4);
			}
		}
		returnScreen();
		return;
	}

	/**
	 * Displays a confirmation dialog. Enter is OK/true, escape is cancel/false.
	 * 
	 * @throws InterruptedException
	 */
	public boolean confirm(String message) throws InterruptedException {
		return confirm(message, "OK", "Cancel");
	}

	/**
	 * Displays a confirmation dialog with custom ok/cancel labels. Max length for labels: 16 chars.
	 * (Otherwise they will go off the sceen.)
	 * 
	 * @param message
	 * @param ok
	 * @param cancel
	 * @return
	 * @throws InterruptedException
	 */
	public boolean confirm(String message, String ok, String cancel) throws InterruptedException {
		takeScreen();
		printTop(message);
		printCentered("- " + ok + " -", 6);
		printCentered("- " + cancel + " -", 7);
		

		while (true) { // TODO: allow menu closure
			while (Button.readButtons() > 0)
				Thread.yield();// wait for release
			try {
				Thread.sleep(20);
			} catch (InterruptedException ie) {
			} // wait to stabilize
			int button = Button.readButtons();
			if (button == 1) {
				Sound.beep();
				returnScreen();
				return true;
			}
			if (button == 8) {
				Sound.beep();
				returnScreen();
				return false; // Escape
			}
		}
	}

	/**
	 * Displays and alert dialog.Arrow keys scroll error left/right if needed, enter is OK, escape is nothing.
	 * 
	 * @throws InterruptedException
	 */
	public void alert(String message) throws InterruptedException {
		alert(message, "OK");
		Sound.beep();
		Thread.sleep(1000);
	}

	// TODO: shouldn't this void and replace button listeners?
	public void alert(String message, String buttonLabel) throws InterruptedException {
		// message should be top justified, throw max len error, etc, but
		// for now just keep it around 2 lines.
		Sound.beep();
		takeScreen();
		printTop(message);
		printCentered("- " + buttonLabel + " -", 7);
		while (true) { // TODO: allow menu closure
			while (Button.readButtons() > 0)
				Thread.yield();// wait for release
			try {
				Thread.sleep(20);
			} catch (InterruptedException ie) {
			} // wait to stabilize
			int button = Button.readButtons();
			if (button == 1) {
				Sound.beep();
				returnScreen();
				return;
			}
		}
	}


	private void takeScreen() {
		oldMode = pb.getMode(pb.ENTER);
		pb.setMode(pb.ENTER, pb.NONE);
		suppressOut = true;
		LCD.clear();
	}

	/**
	 * clears the screen and prints the old lines to it
	 */
	private void returnScreen() {
		LCD.clear();
		for (int i = currentLines.size(); i >= 0; i--) {
			try {
				System.out.println((String) currentLines.elementAt(i));
			} catch (Exception e) {
			}
		}
		suppressOut = false;
		pb.setMode(pb.ENTER, oldMode);
	}

	/**
	 * Prints a line of text centered on the screen. no wrap. Private because these lines are not added to the
	 * Vector currentlines.
	 * 
	 * @param s
	 * @param line
	 */
	private void printCentered(String s, int line) {
		LCD.drawString(s, LCD.DISPLAY_CHAR_WIDTH / 2 - s.length() / 2, line);
	}

	/**
	 * Right-justifies the text, overflows to the left. no wrap.
	 * 
	 * @param s
	 * @param line
	 */
	private void printRight(String s, int line) {
		LCD.drawString(s, LCD.DISPLAY_CHAR_WIDTH - s.length(), line);
	}

	/**
	 * prints a message to the top of the screen, while still wrapping text.
	 * 
	 * @param s
	 */
	private void printTop(String s) {
		System.out.println(s);
		int lines = LCD.DISPLAY_CHAR_DEPTH - (int) Math.ceil(s.length() / 16);
		for (int i = 1; i < lines; i++) { // just tested, idk why.
			System.out.println(" ");
		}
	}

	/**
	 * Displays a message alerting when an object is loaded
	 * 
	 * @param s
	 *            The name of the loaded object
	 */
	public void printLoad(String s) {
		if (debug > 5) {
			println(s + " loaded");
		}
	}

	public void printErr(String s) {
		if (debug > 3) {
			Sound.beep();
			println(s);
		}
	}

	public void println(int num) {
		println(Integer.toString(num));
	}

	public void println(String s) {
		currentLines.insertElementAt(s, 0);
		try {
			currentLines.removeElementAt(7); // keep only the last 7 lines.
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		if (!suppressOut) {
			System.out.println(s);
		} else {
			Sound.buzz(); // TODO: make this beep recognizable
		}
	}
	
	/**
	 * Puts name on the left, truncates is, and val on the right
	 * 
	 * @param name
	 * @param val
	 */

	public String makeTitle(String name, String val) {
		if (name.length() + val.length() > 14) {
			name = name.substring(0, 14 - val.length()) + " ";// note: single char addition

		} else {
			name = (name + "              ").substring(0, 15 - val.length());
		}
		name += val;
		return name;
	}


	public void setDebug(int debug) {
		this.debug = debug;
	}
}
