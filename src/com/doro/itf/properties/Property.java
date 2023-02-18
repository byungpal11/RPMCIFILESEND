package com.doro.itf.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Property {
	
	private volatile static Property instance = null;
	public Property() {

	}

	
	public static Property getInstance() {
		if (instance == null) {
			synchronized (Property.class) {
				if (instance == null) {
					instance = new Property();
				}
			}
		}
		return instance;
	}
	

	public String ReadConfig(String str) throws IOException {
		String msg;
		
		String propFile = "./conf/Rpmciconfig.properties";
	
		Properties props = new Properties();
	
		FileInputStream fis = new FileInputStream(propFile);

		props.load(new java.io.BufferedInputStream(fis));

		msg = props.getProperty(str);

		return msg;

	}

}
