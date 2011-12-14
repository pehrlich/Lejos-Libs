package G1;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EmptyQueueException;
import java.util.Queue;

import javax.microedition.lcdui.Command;

import lejos.nxt.Sound;

import G1.Domino.FatalException;

/**
 * This is a slimmed version of line Queue. It only needs to read commands and then ask the Maneuver to do
 * things.
 * 
 * @author Peter Ehrlich
 * 
 */
public class LineQueue {
	private static final String line_extension = ".line";
	private Command cmd1;
	private Queue todo = new Queue();
	private View view;
	private Robot robot;

	public LineQueue(Robot robot) {
		view = View.getInstance();
		this.robot = robot;
	}

	public void start() {
		if (todo.empty()) {
			view.printErr("queue empty!"); // TODO: needs testing
		}
		while (!todo.empty()) {
			try { // TODO: handle exceptions
				((Segment) todo.pop()).execute(robot);
			} catch (EmptyQueueException e) {
				view.printErr("QUEUE empty!"); // TODO: needs testing
			} catch (InterruptedException e) {
				view.printErr("queue interrupt!"); // TODO: needs testing
			}
		}
		view.println("queue done!");
		Sound.twoBeeps();
		robot.shutDown();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}

	@SuppressWarnings("deprecation")
	public void loadFile(File f) throws FatalException {
		if (!f.exists()) {
			throw new FatalException(f.getName() + " not found");
		}

		try {
			FileInputStream fin = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fin);
			String line = dis.readLine();

			while (line != null && line.length() > 1) {
				view.println(line);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				todo.addElement(new Segment(line)); // tested, ok!
				// later: add to map, check pathing. Find time to complete, dominos needed,
				// whatever.
				// TODO: splits
				line = dis.readLine();
			}
		} catch (FileNotFoundException e) {
			Sound.beep();
			throw new FatalException(f.getName() + " not found");
		} catch (IOException e) {
			throw new FatalException("error reading " + f.getName());
		}
		// view.println(todo.size() + " segments loaded"); //TODO: infinite loop bug

		view.println("segments loaded"); // TODO: count items
	}

	private class Segment {
		private int command;
		private String commandString;
		private int int1 = -1, int2 = -1;

		public Segment(String s) throws FatalException {
			commandString = s;
			String cmd = s.substring(0, s.indexOf(' '));
			if (cmd.equals("line")) {
				int1 = parseAnInt(s);
				command = 0;
			} else if (cmd.equals("arc")) {
				int[] args = parseTwoInts(s);
				int2 = args[1];
				int1 = args[0]; // ...swap needed....apparently
				command = 1;
			} else if (cmd.equals("Aspiral")) {
				int1 = parseAnInt(s);
				command = 2;
			} else if (cmd.equals("Fspiral")) {
				command = 3;
			} else if (cmd.equals("split")) {
				command = 4;
			} else if (cmd.equals("join")) {
				command = 5;
			} else if (cmd.equals("bezier")) {
				command = 6;
			} else {
				throw new FatalException("Q parse error '" + cmd + "'");
			}
		}

		private int parseAnInt(String s) throws FatalException {
			int arg = Integer.parseInt(s.substring(s.indexOf(' ') + 1));
			if (arg == -1) {
				throw new FatalException("Q int parse error '" + arg + "'"); // this will loop if arglength
																				// = 1
			}
			return arg;
		}

		private int[] parseTwoInts(String s) throws FatalException {
			int[] args = new int[2];
			args[0] = Integer.parseInt(s.substring(s.indexOf(' ') + 1, s.indexOf(',')));
			args[1] = Integer.parseInt(s.substring(s.indexOf(',') + 1));
			if (args[0] == -1) {
				throw new FatalException("Q int parse error '" + args[0] + "'"); // this will loop if
																					// arglength = 1
			}
			if (args[1] == -1) {
				throw new FatalException("Q int parse error '" + args[1] + "'"); // this will loop if
																					// arglength = 1
			}
			return args;
		}

		public void execute(Robot m) throws InterruptedException {
			switch (command) {
			case 0:
				m.layLine(int1);
				break;
			case 1:
				m.layArc(int1, int2);
				break;
			case 2:
				m.layASpiral(int1);
				break;
			case 3:
				m.layFSpiral(int1);
				break;
			case 4:
				m.split();
				break;
			case 5:
				m.join();
				break;
			case 6:
				m.layBezier(int1);
				break;
			}
		}

		public String getCommand() {
			return commandString;
		}
	}
}
