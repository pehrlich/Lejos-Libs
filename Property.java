package peter;


/**
 * This is a property interface.
 * 
 * @author Peter Ehrlich
 * 
 */
public interface Property {

	public abstract String getName();

	public abstract String getStrVal();

	public abstract void setVal(String val);

	public abstract void plus();

	public abstract void minus();

	public abstract void bigPlus();

	public abstract void bigMinus();

	// TODO: make string property
	// TODO: add to view instead?

	public class FloatProperty implements Property {
		private float val, inc;
		private String name;
		private Float max = null, min = null;

		/**
		 * Will have a default inc of 0.1
		 * @param name
		 * @param _val
		 */
		public FloatProperty(String _name, float _val){
			name = _name;
			val = _val;
			inc = 0.1f;
		}
		
		/**
		 * Make a new property
		 * 
		 * @param _name
		 *            This will be displayed in the menu as the name you're adjusting, as well as saved to the
		 *            properties file. Advised same as variable name. (Spaces ok.)
		 * @param _val
		 *            This is the default value, and will be used if this property cannot be loaded from the
		 *            file.
		 * @param _inc
		 *            This is the increment when user press up/down on the nxt.
		 */
		public FloatProperty(String _name, float _val, float _inc) {
			val = _val;
			inc = _inc;
			name = _name;
		}

		public String getName() {
			return name;
		}

		//		
		public float getVal() {
			return val;
		}

		public String getStrVal() {
			return Float.toString(val);
		}

		public void setVal(String val) {
			this.val = Float.parseFloat(val);
		}

		public void plus() {
			val += inc;
			if (max != null && val > max) {
				val = max;
			}
		}

		public void minus() {
			val -= inc;
			if (min != null && val < min) {
				val = min;
			}
		}

		public void bigPlus() {
			val += inc * 2;
			if (max != null && val > max) {
				val = max;
			}
		}

		public void bigMinus() {
			val -= inc * 2;
			if (min != null && val < min) {
				val = min;
			}
		}

		public void setMax(float max) {
			this.max = max;
		}

		public void setMin(float min) {
			this.min = min;
		}
	}

	public class IntProperty implements Property {
		private int val, inc;
		private Integer max = null, min = null;
		private String name;
		
		/**
		 * Will have a default increment of 1.
		 * @param _name
		 * @param _val
		 */
		public IntProperty(String _name, int _val) {
			val = _val;
			inc = 1;
			name = _name;
		}

		/**
		 * Make a new property
		 * 
		 * @param _name
		 *            This will be displayed in the menu as the name you're adjusting, as well as saved to the
		 *            properties file. Advised same as variable name. (Spaces ok.)
		 * @param _val
		 *            This is the default value, and will be used if this property cannot be loaded from the
		 *            file.
		 * @param _inc
		 *            This is the increment when user press up/down on the nxt.
		 */
		public IntProperty(String _name, int _val, int _inc) {
			val = _val;
			inc = _inc;
			name = _name;
		}

		public String getName() {
			return name;
		}

		public int getVal() {
			return val;
		}

		public String getStrVal() {
			return Float.toString(val);
		}

		public void setVal(String val) {
			this.val = (int) Math.round(Float.parseFloat(val));
		}

		public void plus() {
			val += inc;
			if (max != null && val > max)
				val = max;
		}

		public void minus() {
			val -= inc;
			if (min != null && val < min)
				val = min;
		}

		public void bigPlus() {
			val += inc * 2;
			if (max != null && val > max)
				val = max;
		}

		public void bigMinus() {
			val -= inc * 2;
			if (min != null && val < min)
				val = min;
		}

		public void setMax(int max) {
			this.max = max;

		}

		public void setMin(int min) {
			this.min = min;

		}
	}

	public class BoolProperty implements Property {
		private boolean value;
		private String name;

		public BoolProperty(String name, boolean value) {
			super();
			this.value = value;
			this.name = name;
		}

		public void bigMinus() {
			value = !value;
		}

		public void bigPlus() {
			value = !value;
		}

		public String getName() {
			return name;
		}

		public String getStrVal() {
			if (value)
				return "true";
			return "false";
		}

		public void minus() {
			value = !value;
		}

		public void plus() {
			value = !value;
		}

		public void setVal(String val) {
			value = Boolean.parseBoolean(val);
		}

		public boolean getVal() {
			return value;
		}
	}

	public class StringProperty implements Property {
		private String[] options;
		private String name;
		private int value;
		private Integer max = null, min = null;
		
		/**
		 * Properties constructed with this can only be viewed, and have no way of being set.
		 * @param name
		 */
		public StringProperty(String name) {
			super();
			this.name = name;
		}

		

		public StringProperty(String name, String[] options, int value) {
			super();
			this.options = options;
			this.name = name;
			this.value = value;
		}

		public void bigMinus() {
			if (min != null && value > min)
				value--;
			if (min != null && value > min)
				value--;
			if (value == -1)
				value = options.length - 1;
			if (value == -2)
				value = options.length - 2; // TODO: unchecked
			if (max != null && value > max)
				value = max;

		}

		public void bigPlus() { //shoulda just called plus twice
			if (max != null && value < max)
				value++;
			if (max != null && value < max)
				value++;
			if (value == options.length)
				value = 0;
			if (value == options.length + 1)
				value = 1;
			if (min != null && value < min)
				value = min;
		}

		public String getName() {
			return name;
		}

		public String getStrVal() {
			return name;
		}

		public void minus() {
			if (min != null && value > min)
				value--;
			if (value == -1)
				value = options.length - 1;
			if (max != null && value > max)
				value = max;

		}

		public void plus() {
			if (max != null && value < max)
				value++;
			if (value == options.length)
				value = 0;
			if (min!=null && value < min)
				value = min;
		}

		public void setMax(int max) {
			this.max = max;
		}

		public void setMin(int min) {
			this.min = min;
		}

		public void setVal(String val) {
			value = Integer.parseInt(val);
		}

	}
}