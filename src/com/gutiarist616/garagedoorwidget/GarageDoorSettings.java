package com.gutiarist616.garagedoorwidget;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GarageDoorSettings extends Activity {
	
	/**
	 * This is called when the activity is opened
	 */
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.garage_door_settings);
		final EditText ipField = (EditText)findViewById(R.id.setIpField);
		final EditText passField = (EditText)findViewById(R.id.setPasswordField);
		// Check to see if properties exists. If not get defaults and create and save file

		Properties prop = null;
		try {
			prop = loadProperties(); 
		} catch (IOException e ) {
			e.printStackTrace();
		}

		if (prop != null) {
			populateFields(prop, ipField, passField);

			((Button)findViewById(R.id.submitChanges)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setOnClickListener(ipField, passField);
				}
			});
		} else {
			Log.e("Filesystem error", "There was a problem getting the properties from the file");
		}
	}
	
	/**
	 * This method is called when the activity is pushed to the background
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	/**
	 * This method is called when the activity returns to the foreground
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/**
	 * 
	 * @param properties
	 * @throws IOException
	 */
	private void handlePropertyChange(Properties prop) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(getFileStreamPath("properties.properties"));
		prop.store(outputStream, null);
		outputStream.close();
		Toast.makeText(getApplicationContext(), "Properties updated", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private Properties loadProperties() throws IOException {
		Properties prop = new Properties();
		File file = getFileStreamPath("properties.properties");
		if (file.exists()) {
			Log.i("", "File exists, load it up");
			try {
				FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
				prop.load(inputStream);
				inputStream.close();
				return prop;
			} catch (IOException e) {
				Log.e("", "Couldnt read properties file");
				return null;
			}
		} else {
			Log.i("", "File doesn't exist. Get from assets");
			return initProperties(prop, file);
		}
	}

	/**
	 * Initializes the properties file. This should only be called the first time the app is launched
	 * 
	 * @param prop - A Properties object to initialize
	 * @param file - The location on the file system where the properties file resides.
	 * @return - Either a Properties object or null if an exception is thrown
	 * @throws IOException
	 */
	private Properties initProperties(Properties prop, File file) throws IOException {
		try {
			InputStream inputStream = getAssets().open("app.properties");
			prop.load(inputStream);
			inputStream.close();
			Log.d("", "Got the file from assets now write to properties.properties");
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				prop.store(fileOutputStream, null);
				fileOutputStream.close();
				return prop;
			} catch (IOException e) {
				Log.e("", "Couldn't write to new properties file");
				return null;
			}
		} catch (IOException e) {
			Log.e("", "Couldnt open app.properties");
			return null;
		}
	}

	/**
	 * 
	 * @param prop
	 * @param ipField
	 * @param passField
	 */
	private void populateFields(Properties prop, EditText ipField, EditText passField) {
		ipField.setText(prop.getProperty("server"));
		passField.setText(prop.getProperty("password"));
	}

	/**
	 * 
	 * @param ipField
	 * @param passField
	 */
	private void setOnClickListener(EditText ipField, EditText passField) {
		((InputMethodManager)GarageDoorSettings.this.getSystemService("input_method")).hideSoftInputFromWindow(passField.getWindowToken(), 0);
		Properties myProp = null;
		try {
			myProp = GarageDoorSettings.this.loadProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String server = ipField.getText().toString();
		String pass = passField.getText().toString();
		myProp.setProperty("server", server);
		myProp.setProperty("password", pass);

		try {
			GarageDoorSettings.this.handlePropertyChange(myProp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/* Location:           /Users/brandonwood/Desktop/garagedoorwidget/com.gutiarist616.garagedoorwidget-2/dex2jar-0.0.9.15/classes-dex2jar.jar
 * Qualified Name:     com.gutiarist616.garagedoorwidget.GarageDoorSettings
 * JD-Core Version:    0.6.2
 */