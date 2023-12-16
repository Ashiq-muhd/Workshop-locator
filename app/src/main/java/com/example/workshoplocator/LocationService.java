package com.example.workshoplocator;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LocationService extends Service implements SensorListener {
	//	public static String namespace="urn:demo";
//	public static String url="http://192.168.1.6/WebServiceSOAP/AAA.php?wsdl";
//	public static String method="loc";
//	public static String soapaction="namespace+method";
	Handler hnd;
	int NOTIFICATION_ID = 234;
	NotificationManager notificationManager;
	private SensorManager sensorMgr;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 5400;
	public static String orglat = "";
	SharedPreferences sh2, sh3;
	private LocationManager locationManager;
	private Handler handler = new Handler();
	SharedPreferences sh;
	public static double lati;
	public static double longi;

	public static String place = "", address;
	public static Location curLocation;


	final String valuespeed = "";
	final String valuesteering = "";
	String finalvaluespeed = "";
	String finalvaluesteering = "";

	long startTime = 0;

	private static final String TAG = "AusteerLogging";

	String locString = "";
	String altitudeString = "";
	String speedString = "";
	String steerStringX;
	String steerStringY;
	String steerStringZ;


	List<Double> speedlist = new ArrayList();

	Double lastLat;
	Double lastLng;
	Double lastAlt;
	Long lastTime;
	float speed = 0;
	float speedvalue = 0;
	String outputString = "";
	public static Double total_dis = 0.0;


	static int count = 0, tmpcount = 0;
	String cid = "";
	public static String scid = "", pc = "";
	String ip, ur, uid, sid, urs, urlsp;
	String[] bpid, bp, pid, pcon;
	ArrayList<String> sms = new ArrayList<String>();

	TelephonyManager manager;
	String imeino;


	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			finalvaluespeed = speedString;
//            speedins();

			outputString = String.format("%d:%02d:%02d", minutes, seconds, millis % 3000) + "," + locString + "," + speedString + "," + steerStringY + "," + altitudeString + "\n";
			timerHandler.postDelayed(this, 5000);
		}
	};


	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (curLocation == null) {
				curLocation = location;

			} else if (curLocation.getLatitude() == location.getLatitude() && curLocation.getLongitude() == location.getLongitude()) {
				return;
			} else {
			}


			curLocation = location;

			final DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.CEILING);

			final DecimalFormat df2 = new DecimalFormat("#.#");
			df2.setRoundingMode(RoundingMode.CEILING);
			Double newLat = location.getLatitude();
			Double newLng = location.getLongitude();
			Double newAlt = location.getAltitude();
			Long newTime = location.getTime();

			altitudeString = Double.toString(newAlt);

			Float accuracy = location.getAccuracy();

			// Altitude too inaccurate so just use the same altitude for calculating speed,
			// you're not moving that fast unless you fall off a cliff
			locString = df.format(newLat) + ", " + df.format(newLng);
			Log.v(TAG, "LOCATION CHANGE, lat=" + df.format(newLat) + ", lon=" + df.format(newLng) + "(Accuracy: " + accuracy + ")");
			// on first run set location and start timer
			if (lastLat == null) {
				startTime = System.currentTimeMillis();
				timerHandler.postDelayed(timerRunnable, 0);
				speedString = "0";
				lastLat = newLat;
				lastLng = newLng;
				lastAlt = newAlt;
				lastTime = newTime;
			} else {
				Double dist = distance(lastLat, newLat, lastLng, newLng, newAlt, newAlt);
				total_dis += dist;
				Log.v(TAG, "DISTANCE = " + dist);
				Log.d("distance", "==== " + dist);
				float timediff = (newTime - lastTime) / 1000;

				//Toast.makeText(getApplicationContext(), "speed n time" + dist + " - " + timediff, Toast.LENGTH_SHORT).show();
				//  Toast.makeText(getApplicationContext(), "speed list" + speedlist.size(), Toast.LENGTH_SHORT).show();


				if (timediff == 0) {
					// do nothing because distance is less than accuracy
					// or measurement too quick
					Log.v(TAG, "SPEED UNCHANGED");
					Log.d("speed uncganged", "==== " + finalvaluespeed);
				} else {
					Double speed = Math.abs(dist) / timediff;
					// Average speed from last five position results
					speedlist.add(speed);
					if (speedlist.size() > 5) {
						speedlist.remove(0);
						Double averagespeed = averageSpeed(speedlist);
						if (averagespeed < 0.5) {
							averagespeed = 0.0;
						}
						speedString = df2.format(Math.abs(averagespeed));
						Log.v(TAG, "SPEEDS = " + speedlist.toString());
						Log.v(TAG, "ALTITUDE = " + altitudeString);
					}
					// only update speeds if dist/time is changed
					lastLat = newLat;
					lastLng = newLng;
					lastAlt = newAlt;
					lastTime = newTime;
				}
			}


		}

		private Double averageSpeed(List<Double> speeds) {
			// TODO Auto-generated method stub

			Double total = 0.0;
			for (int i = 0; i < speeds.size(); i++) {
				total = total + (double) speeds.get(i);
			}
			return (total / speeds.size());


		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	@Override
	public void onCreate() {


//        hnd = new Handler();
//        hnd.post(rn);
		notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		super.onCreate();
		try {
			if (Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
		} catch (Exception e) {
		}

		sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		sh2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		sh3 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

//		manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
				Settings.Secure.ANDROID_ID);
		imeino = android_id;

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		String andid_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
				Settings.Secure.ANDROID_ID);
		imeino = andid_id;
//		imeino = manager.getDeviceId().toString();
		if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		curLocation = getBestLocation();
		if (curLocation == null) {
			//Toast.makeText(this, "GPS problem..........", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onStart(Intent i, int startId) {

		//Toast.makeText(this, "Start Services", Toast.LENGTH_SHORT).show();
		handler.post(GpsFinder);

		// start motion detection
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);

		if (!accelSupported) {
			// on accelerometer on this device
			sensorMgr.unregisterListener((SensorListener) this,
					SensorManager.SENSOR_ACCELEROMETER);
		}


	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy() {
		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (provider.contains("gps")) { //if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
		handler.removeCallbacks(GpsFinder);
		handler = null;
		//Toast.makeText(this, "Service Stopped..!!", Toast.LENGTH_SHORT).show();
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

	public Runnable GpsFinder = new Runnable() {

		@SuppressWarnings("deprecation")
		public void run() {

			try {
//	    		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
//	    	    intent.putExtra("enabled", true);
//	    	    sendBroadcast(intent);

				String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				if (!provider.contains("gps")) { //if gps is disabled
					final Intent poke = new Intent();
					poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
					poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
					poke.setData(Uri.parse("3"));
					sendBroadcast(poke);
				}
			} catch (Exception e) {
				//Toast.makeText(getApplicationContext(), "Error in gps on : " + e.toString(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

			Location tempLoc = getBestLocation();
			if (tempLoc != null) {
				curLocation = tempLoc;
				//Toast.makeText(getApplicationContext(), "lat" + Double.toString(curLocation.getLatitude()) + Double.toString(curLocation.getLongitude()), Toast.LENGTH_SHORT).show();
				loc();
				updateloc();
				//String loc="";

//                Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
//                try {
//                    List<Address> addresses = geoCoder.getFromLocation(curLocation.getLatitude(), curLocation.getLongitude(), 1);
//                    if (addresses.size() > 0) {
//                        for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++)
//                            address += addresses.get(0).getAddressLine(index) + " ";
//                        //Log.d("get loc...", address);
////
//                       place = addresses.get(0).getSubLocality().toString();
////		            	 Toast.makeText(getApplicationContext(), address, Toast.LENGTH_SHORT).show();
//                } else {
//                       if (!place.equalsIgnoreCase("SK Temple Road")) {
//                          lati = 11.25;
//                           longi = 75.78;
//                       } else {
//                           lati = 11.26;
//                            longi = 75.78;
//                       }
//                       Toast.makeText(getApplicationContext(), "Not Loc" + place, Toast.LENGTH_SHORT).show();
//                    }
//                } catch (IOException e) {
//               }
			} else {
				if (!place.equalsIgnoreCase("SK Temple Road")) {
					lati = 11.25;
					longi = 75.78;
				} else {
					lati = 11.26;
					longi = 75.78;
				}
			}
			handler.postDelayed(GpsFinder, 50000);// register again to start after 20 seconds...
		}

		public void loc() {
			lati = curLocation.getLatitude();
			longi = curLocation.getLongitude();
			sh2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			ip = sh2.getString("ip", "");
			String id = sh2.getString("lid", "");
//             Toast.makeText(getApplicationContext(),"lati"+lati,Toast.LENGTH_SHORT).show();





//           checklat();
			try {
				if (Build.VERSION.SDK_INT > 9) {
					StrictMode.ThreadPolicy th = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(th);
				}
			} catch (Exception e) {

			}
//            sh2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            sh3 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//            uid = sh2.getString("id", "");
//
//            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
//            if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
		}

	};

    private void updateloc() {
        RequestQueue queue = Volley.newRequestQueue(LocationService.this);
        String url = "http://" + sh.getString("ip", "") + ":5000/update_location";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the response string.
                Log.d("+++++++++++++++++", response);
                try {
                    JSONObject json = new JSONObject(response);
                    String res = json.getString("task");
					if(res.equalsIgnoreCase("success")){
						Toast.makeText(LocationService.this, "updated", Toast.LENGTH_SHORT).show();
					}
					else
					{
						Toast.makeText(LocationService.this, "not updated", Toast.LENGTH_SHORT).show();
					}

//                    String lat = json.getString("lati");
//                    String longt = json.getString("longi");
//                    String valu = sh.getString("reslt", "");
//                    if (!valu.equals("Red")) {
//                        if (res.equals("Red")) {
//                            getnotify();
//                        }
//                    }
//                    SharedPreferences.Editor
//                            ed = sh.edit();
//                    ed.putString("reslt", res);
//                    ed.commit();
//
//
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "err" + e, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
				params.put("lid", sh.getString("lid", ""));
                params.put("lati", ""+LocationService.lati);
                params.put("longi", ""+LocationService.longi);
//                params.put("imei", ""+imeino);


                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private Location getBestLocation() {
		Location gpslocation = null;
		Location networkLocation = null;

		if (locationManager == null) {
			locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		}
		try {
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					//  return TODO;
				}
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);// here you can set the 2nd argument time interval also that after how much time it will get the gps location
				gpslocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					// return TODO;
				}
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
				networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		} catch (IllegalArgumentException e) {
			Log.e("error", e.toString());
		}
		if (gpslocation == null && networkLocation == null)
			return null;

		if (gpslocation != null && networkLocation != null) {
			if (gpslocation.getTime() < networkLocation.getTime()) {
				gpslocation = null;
				return networkLocation;
			} else {
				networkLocation = null;
				return gpslocation;
			}
		}
		if (gpslocation == null) {
			return networkLocation;
		}
		if (networkLocation == null) {
			return gpslocation;
		}
		return null;
	}

	@Override
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(int arg0, float[] arg1) {

		if (arg0 == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = arg1[SensorManager.DATA_X];
				y = arg1[SensorManager.DATA_Y];
				z = arg1[SensorManager.DATA_Z];

				if (Round(x, 4) > 10.0000) {
					Log.d("sensor", "X Right axis: " + x);
					//     Toast.makeText(this, "Right shake detected", Toast.LENGTH_SHORT).show();
				} else if (Round(y, 4) > 10.0000) {
					Log.d("sensor", "X Right axis: " + x);
					//    Toast.makeText(this, "Top shake detected", Toast.LENGTH_SHORT).show();
				} else if (Round(y, 4) > -10.0000) {
					Log.d("sensor", "X Right axis: " + x);
					//     Toast.makeText(this, "Bottom shake detected", Toast.LENGTH_SHORT).show();
				} else if (Round(x, 4) < -10.0000) {
					Log.d("sensor", "X Left axis: " + x);
					//    Toast.makeText(this, "Left shake detected", Toast.LENGTH_SHORT).show();
				}

				speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

				// Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
				if (speed > SHAKE_THRESHOLD) {
					Log.d("sensor", "Shake detected w/ speed: " + speed);

//					TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
					String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
							Settings.Secure.ANDROID_ID);
					imeino = android_id;
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
						return;
					}

//					Thread th = new Thread(new Runnable() {
//						@Override
//						public void run() {
							RequestQueue queue = Volley.newRequestQueue(LocationService.this);
							final String url ="http://"+sh.getString("ip", "") + ":5000/LOCATION";
							StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									// Display the response string.
									Log.d("+++++++++++++++++", response);
									try {
										JSONObject json = new JSONObject(response);
										String res = json.getString("task");
										if(res.equalsIgnoreCase("success")) {
											String phn = json.getString("cno");
											String hid = json.getString("hid");
//											SharedPreferences.Editor ed=sh.edit();
//											ed.putString("hid","");
//											ed.commit();
											String reslt[] = phn.split("#");
											for (int ik = 0; ik < reslt.length; ik++) {
												SmsManager sms = SmsManager.getDefault();
												sms.sendTextMessage(reslt[ik], null, "help Me" + " " + "http://maps.google.com/maps?q=" +
														LocationService.lati + "," + LocationService.longi, null, null);
											}
//											try {
//												Thread.sleep(10000);
//											} catch (InterruptedException e) {
//												e.printStackTrace();
//											}
//											String filepath = Environment.getExternalStorageDirectory() + "/abc.jpg";
//											for(int i=0;i<=5;i++){
//												WEbtest(filepath,hid);
//											}


										}
									} catch (JSONException e) {
										e.printStackTrace();
									}


								}

							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {


//                                    Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
								}
							}) {
								@Override
								protected Map<String, String> getParams() {
									Map<String, String> params = new HashMap<>();


									params.put("latitude", ""+LocationService.lati);
									params.put("longitude", ""+LocationService.longi);
									params.put("lid",sh.getString("lid",""));



									return params;
								}
							};
							queue.add(stringRequest);

//						}
//					});
//					th.start();

					last_x = x;
					last_y = y;
					last_z = z;
				}
			}
		}

	}




	public void onLocationChanged(){



	}

	public static float Round(float x2, int i) {
		// TODO Auto-generated method stub
		float p = (float) Math.pow(10, i);
		x2 = x2 * p;
		float tmp = Math.round(x2);
		return (float) tmp / p;
	}


	public Double distance(double lat1, double lat2, double lon1,
						   double lon2, double el1, double el2) {

		final int R = 6371; // Radius of the earth

		Double latDistance = Math.toRadians(lat2 - lat1);
		Double lonDistance = Math.toRadians(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}


//    public void speedins() {
//
//
//        try {
//            if (Build.VERSION.SDK_INT > 9) {
//                StrictMode.ThreadPolicy th = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                StrictMode.setThreadPolicy(th);
//            }
//        } catch (Exception e) {
//
//        }
//
//
//        if (curLocation != null)
//            if (curLocation.hasSpeed()) {
//                speedvalue = curLocation.getSpeed();
//                speedvalue *= 3.6;
//                //Toast.makeText(getApplicationContext(), "speed:"+speedvalue, Toast.LENGTH_SHORT).show();
//
//            }
//
//
////	Toast.makeText(getApplicationContext(), "db value==="+finalvaluesteering, Toast.LENGTH_SHORTz).show();
//        uid = sh2.getString("id", "");
//
//        Thread tt = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                RequestQueue queue = Volley.newRequestQueue(LocationService.this);
//                final String url ="http://"+sh.getString("ip", "") + ":5000/vehinfo";
//                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the response string.
//                        Log.d("+++++++++++++++++", response);
//                        try {
//                            JSONObject json = new JSONObject(response);
//                            String res = json.getString("task");
//
//                            if (res.equalsIgnoreCase("success")) {
//
//
//
//
//                            } else {
//
//
//
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//
//                        Toast.makeText(getApplicationContext(), "Error" + error, Toast.LENGTH_LONG).show();
//                    }
//                }) {
//                    @Override
//                    protected Map<String, String> getParams() {
//                        Map<String, String> params = new HashMap<>();
//                        params.put("vid", sh.getString("lid", ""));
//                        params.put("speed", ""+speed);
//
//
//
//                        return params;
//                    }
//                };
//                queue.add(stringRequest);
//                String s = null;
//            }
//        });
//        tt.start();
//    }

	public void WEbtest(String fpath, String hid) {
//        File as = new File(f.getAbsolutePath());
//
//        int ln=(int) as.length();
//        try
//        {
//            InputStream inputStream = new FileInputStream(as);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            byte[] b = new byte[ln];
//            int bytesRead =0;
//
//            while ((bytesRead = inputStream.read(b)) != -1)
//            {
//                bos.write(b, 0, bytesRead);
//            }
//            inputStream.close();
//            byteArray = bos.toByteArray();
//        }
//        catch (IOException e)
//        {
//            Toast.makeText(this,"Stringsss :"+e.getMessage().toString(), Toast.LENGTH_LONG).show();
//        }
//
//
//        String dd = as.toString();





		String fileName = fpath;


		try
		{
			if(Build.VERSION.SDK_INT > 9)
			{
				StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
		}
		catch(Exception e)
		{

		}



//		String upLoadServerUri = "http://" + sh.getString("ip", "") + ":5000/shareuservisual";

//		FileUpload fp = new FileUpload(fileName);
//		Map<String, String> mp = new HashMap<String,String>();
//		mp.put("hid",hid);







//		String  res=fp.multipartRequest(upLoadServerUri, mp, fileName, "files", "application/octet-stream");
//                    Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
//		if(res.equalsIgnoreCase("ok"))
//		{
////                Toast.makeText(getApplicationContext(), "uploaded", Toast.LENGTH_SHORT).show();
//
////                ttobj.speak("bad path ahead..", TextToSpeech.QUEUE_FLUSH, null);
//
//		}
////            else if (res.equalsIgnoreCase("ok"))
//		else
//		{
////                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
////                ttobj.speak("good road ahead..", TextToSpeech.QUEUE_FLUSH, null);
//		}




//		return res;
	}
	private  void getnotify() {

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		String CHANNEL_ID = "my_channel_01";
		CharSequence name = "my_channel";
		String Description = "This is my channel";
		int importance = NotificationManager.IMPORTANCE_HIGH;
		NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
		mChannel.setDescription(Description);
		mChannel.enableLights(true);
		mChannel.setLightColor(Color.RED);
		mChannel.enableVibration(true);
		mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//       mChannel.setVibrationPattern(new long[]{0, 800, 200, 1200, 300, 2000, 400, 4000});
		mChannel.setShowBadge(false);
//		NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.createNotificationChannel(mChannel);
	}
	NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_channel_01")
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle("Notification")
			.setContentText("Dangerous Spot");
//		Intent resultIntent = new Intent(getApplicationContext(),User_home .class);
//		Intent resultIntent1= new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?q="+lat+","+longt));
//	  			startActivity(resultIntent1);

//	  			String uri="http://maps.google.com/maps?q=11.2553782,75.7852937";
//	  			Intent resultIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//	  			startActivity(resultIntent);
//

//    Intent resultIntent = new Intent(getApplicationContext(), Details.class);
//    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
//    stackBuilder.addParentStack(MainActivity.class);
//    stackBuilder.addNextIntent(resultIntent);
//    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//    builder.setContentIntent(resultPendingIntent);
	notificationManager.notify(NOTIFICATION_ID, builder.build());
}

//	Toast.makeText(getApplicationContext(), "hhhh", Toast.LENGTH_LONG).show();
//	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
//			.setSmallIcon(android.R.drawable.ic_dialog_email)
//			.setContentTitle("Alert")
//			.setContentText("Dangerous spot")
//			.setAutoCancel(true);
//	// Creates an explicit intent for an Activity in your app
////	  			Intent resultIntent = new Intent(getApplicationContext(),User_home .class);
//	Intent resultIntent= new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?q="+lat+","+longt));
////	  			startActivity(i);
//
////	  			String uri="http://maps.google.com/maps?q=11.2553782,75.7852937";
////	  			Intent resultIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
////	  			startActivity(resultIntent);
//
//	// The stack builder object will contain an artificial back stack for the
//	// started Activity.
//	// This ensures that navigating backward from the Activity leads out of
//	// your application to the Home screen.
//	TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
//	// Adds the back stack for the Intent (but not the Intent itself)
//	stackBuilder.addParentStack(User_home.class);
//	// Adds the Intent that starts the Activity to the top of the stack
//	stackBuilder.addNextIntent(resultIntent);
//	//	startActivity(resultIntent);
//	PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
//	mBuilder.setContentIntent(resultPendingIntent);
//	//mBuilder.setVibrate();
//	Notification note=mBuilder.build();
//	note.defaults |= Notification.DEFAULT_VIBRATE;
//	note.defaults |= Notification.DEFAULT_SOUND;
//
//	NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//	// mId allows you to update the notification later on.
//	mNotificationManager.notify(100,note);// mBuilder.build());
//}

}


