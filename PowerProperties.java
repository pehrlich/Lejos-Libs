package peter;

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

import G1.View;
import peter.Property.*;

public class PowerProperties {
	private ArrayList<Property> props;
	private View view;

	public PowerProperties(View view) {
		this(view, new ArrayList<Property>());
	}
	
	public PowerProperties(View view, ArrayList<Property> props) {
		this.view = view;
		this.props = props;
	}
	
	/**
	 * TODO: add methds for things besides floats...
	 * @param name
	 * @param val
	 * @param inc
	 */

	public void addPropery(String name, float val, float inc) {
		props.add(new FloatProperty(name, val, inc));
	}
	
	/**
	 * returns the property's value if it exists, null if not found.
	 * @param name
	 * @return
	 */
	public Property getProperty(String name){
		for(Property p : props){
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}
	
	public ArrayList<Property> getProps(){
		return props;
	}
	
	
	
	/**
	 * This loads properties from a properties file if they and it exist, allows the user to customize each
	 * property, optionally saves the property, and returns the updated properties list.
	 * 
	 * @param props 
	 * @param fName the properties ini that you waqnt to use
	 * @param save whether to save.
	 * @return
	 */
	//TODO: This really doesn't belong in view, but would otherwise need to be passed a view.
	public void configAll(String fName, boolean save) {
		if (props.size()> 0) {
			Properties p = new Properties();
			try {
				p.load(new FileInputStream(new File(fName))); // this will close stream when done?
			} catch (FileNotFoundException e) {
				view.printErr(fName + "not found"); // uses static class View
			} catch (IOException e) {
				view.printErr(fName +  "IO exception");
			}
			

			for (Property prop : props){
				if (p.getProperty(prop.getName()) != null) { //TODO: checl for number format exception!
					prop.setVal(p.getProperty(prop.getName()));
				}
			}

			try { // TODO catch
				view.configMenu(props);
			} catch (InterruptedException e) {
			}

			
			for (Property prop : props){
				view.println(view.makeTitle(prop.getName() + ": ", prop.getStrVal()));
				p.setProperty(prop.getName(), prop.getStrVal());
			}

			if (save) {
				File config = new File(fName);
				if (config.exists()) {
					config.delete();
				}
				try {
					config.createNewFile();
				} catch (IOException e1) {
					view.printErr("can't make properties file");
				}
				FileOutputStream fout = new FileOutputStream(config);
				try {
					p.store(fout, "This is an NXT config file");
					fout.close();
				} catch (IOException e) {
					view.printErr("error writing indy.ini: " + e.getMessage());
				}
			}
		}
	}
	
	

}
