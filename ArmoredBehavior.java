package peter;

import lejos.subsumption.Behavior;

/*
 * This is a modification to the normal Subsumption behavior. By default, behavior have no "lock"; they are
 * meant to have no logic and just do one thing, like drive forward. If you want to make a behavior that takes
 * time, you need this, so that it will not interrupt itself or be interrupted at a bad time.
 * 
 * Here's how it works: When you make a new Behavior, make it an ArmoredBehavior instead. Put what you want to
 * happen in action2(), and the condition in takeControl2(). This behavior is armored, meaning that any
 * behavior which would normally interrupt it now just sets 'interrupt' equal to true, meaning that you have
 * to manually check the boolean in your own code - so be responsible! There is another variable, 'armored'.
 * If your action2 sets this to false at some point, it can then be interrupted by its own takeControl() or a
 * higher priority one, but if that doesn't happen, it will run until the end of its action2() before
 * finishing and going to a lower level behavior.
 */

public abstract class ArmoredBehavior implements Behavior {
	protected boolean armored = false, interrupted = false;

	public void action() {
		armored = true;
		interrupted = false;
		action2();
		armored = false;
		interrupted = false;
		return;
	}

	/**
	 * remember to check the interrupted variable
	 */
	protected abstract void action2();

	public void suppress() {
		interrupted = true;
	}

	public boolean takeControl() {
		if (armored) {
			return false;
		} else {
			return takeControl2();
		}
	}

	protected abstract boolean takeControl2();

}
