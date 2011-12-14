This is some library code developed in preparation for brickworld 2010.

The domonobot folder holds an example of usage.  This includes a sweet (but complicated) view class, with menus and dialogs.  Take a look at the completed robot, here: http://www.youtube.com/watch?v=OOAn7MbHnxY



### Armored Behavior:
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


### CheckPorts:

 This enabled the robot to detect if it has not been wired correctly.  It can look pretty cool when testing sensors while booting up!


### PowerButton:

 This gives buttons more power, including the ability to change and remove button listeners. There can be up
 to four instances of PowerButton per program, as there can only be four buttonlisteners per button per
 program. Can only handle one wait for press at a time.


### PowerProperties & Property:

Allows you to save preferences in runtime


### WatchForStalls:

 This watches for motor stalls on all motors passed. A motor is considered stalled if it has a
 speed below the specified limit 200ms in a row, or if it has a decreasing speed below limit. This
 is to cover motors starting in locked states as well as fast response time for stalling motors.
