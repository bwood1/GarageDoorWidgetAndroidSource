package com.gutiarist616.garagedoorwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class GarageDoorWidget extends AppWidgetProvider {
	private final String LEFT_BUTTON_ACTION = "ldoor";
	private final String RIGHT_BUTTON_ACTION = "rdoor";
	private String secretKey;
	private String path;
	private String server;
	
	/**
	 * 
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		
		// perform this loop for each App Widget that belongs to this provider
		for (int i = 0; i<N ; i++) {
			int appWidgetId = appWidgetIds[i];
			
			// Create an Intent for each button and set the action
			Intent leftIntent = new Intent(context, this.getClass());
			Intent rightIntent = new Intent(context, this.getClass());
			leftIntent.setAction(LEFT_BUTTON_ACTION);
			rightIntent.setAction(RIGHT_BUTTON_ACTION);
			
			// Make a pending intent for each button
			PendingIntent leftPendingIntent = PendingIntent.getBroadcast(context, 0, leftIntent, 0);
			PendingIntent rightPendingIntent = PendingIntent.getBroadcast(context, 0, rightIntent, 0);
			
			// Get the layout for the App Widget and attach an on-click listener to the buttons
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.garage_door);
			views.setOnClickPendingIntent(R.id.lDoorButton, leftPendingIntent);
			views.setOnClickPendingIntent(R.id.rDoorButton, rightPendingIntent);
			
			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	/**
	 * Attempts to read the properties from the properties file
	 * 
	 * @param context - The context in which this is being called
	 * @return - Either the properties object or null if no properties file is found
	 * @throws IOException - Thrown if the file is not found
	 */
	private Properties loadProperties(Context context) throws IOException {
		File file = context.getFileStreamPath("properties.properties");
		Properties prop = new Properties();
		try {
			FileInputStream localFileInputStream = new FileInputStream(file);
			prop.load(localFileInputStream);
			localFileInputStream.close();
			
			return prop;
		} catch (IOException e) {
			Log.e("", "File " + "properties.properties" + " does not exist");
		}
    	return null;
	}

	/**
	 * This is executed when the widget receives and input from the user
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("on receive called", "");
		
		super.onReceive(context, intent);
		Log.i("", intent.getAction().toString() + " pressed");
		
		Properties prop = null;
		
		//get the properties
		try {
			prop = loadProperties(context);
			this.server = prop.getProperty("server");
			this.path = prop.getProperty("path");
			this.secretKey = prop.getProperty("password");
		} catch (IOException e) {
			Log.e("", "Problem getting properties");
			Toast.makeText(context, "Cannot read properties from file", 
					Toast.LENGTH_SHORT).show();
		}
		
		//figure out which button was clicked
		if (intent.getAction().equals(LEFT_BUTTON_ACTION)) {
			Log.i("", "Left button pressed");
			sendRequest(LEFT_BUTTON_ACTION, context);
		} else if (intent.getAction().equals(RIGHT_BUTTON_ACTION)) {
			Log.i("", "Right button pressed");
			sendRequest(RIGHT_BUTTON_ACTION, context);
		}
	}

	/**
	 * 
	 * @param paramString
	 * @param paramContext
	 */
	@SuppressWarnings("unchecked")
	private void sendRequest(String paramString, Context context) {
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("request", paramString));
		params.add(new BasicNameValuePair("id", secretKey));
		
		Connection conn = new Connection();
		
		AsyncTask<List<NameValuePair>, Void, Boolean>asyncTask = new Connection().execute(params);
		Boolean result = null;
		
		try {
			result = asyncTask.get();
		} catch (InterruptedException e) {
			Log.e("InterruptedException", e.getLocalizedMessage());
		} catch (ExecutionException e) {
			Log.e("ExecutionException", e.getLocalizedMessage());
		}
		
		Log.d("", String.format("The result is %s", result));
		
		if (result) {
			Toast.makeText(context, "Command received", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Command not received", Toast.LENGTH_SHORT).show();
		}
	}

	class Connection extends AsyncTask<List<NameValuePair>, Void, Boolean> {
		Connection(){}

		protected Boolean doInBackground(List<NameValuePair>... list) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params = list[0];
			Log.d("Parameters: ", params.toString());
			
			try {
				HttpClient client = new DefaultHttpClient();
				String url = "http://" + GarageDoorWidget.this.server + GarageDoorWidget.this.path + "/request.php";
				HttpPost post = new HttpPost(url);
				Log.d("",String.format("the url is %s", url));
				
				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
				post.setEntity(ent);
				
				String response = (String) client.execute(post, new BasicResponseHandler());
				Log.d("doInBackground(", String.format("the response is %s", response));
				
				if(response.equals("no")) {
					return false;
				} else {
					return true;
				}
				
			} catch (IOException e) {
				Log.e("IOException", e.getLocalizedMessage());
				return false;
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}			
	}
}

/* Location:           /Users/brandonwood/Desktop/garagedoorwidget/com.gutiarist616.garagedoorwidget-2/dex2jar-0.0.9.15/classes-dex2jar.jar
 * Qualified Name:     com.gutiarist616.garagedoorwidget.GarageDoorWidget
 * JD-Core Version:    0.6.2
 */