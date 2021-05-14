package com.akash.cowin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.akash.cowin.common.CowinJwt;
import com.akash.cowin.common.Utilities;
import com.akash.cowin.http.HttpServer;
import com.akash.cowin.http.HttpUtilities;
import com.akash.cowin.redis.RedisCache;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class InitiateCowin {
	
	private static final Vertx vertx;
	
	static {
		//Instantiating the File class
	     File file = new File(System.getProperty("user.dir") + "\\cowin.log");
	     
	     //Instantiating the PrintStream class
	     PrintStream stream = null;
		try {
			stream = new PrintStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     System.out.println("Check logs in this file -> "+file.getAbsolutePath());
	     System.setOut(stream);
	      
		VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(20000);
		// Initialize Vertx
		vertx = Vertx.vertx(vertxOptions);
		// Initialize Redis
		RedisCache.getRedisPool();
		// Initialize webclient
		HttpUtilities.getWebClient(vertx);
		
		
	}

	/**
	 * Getter for vertx
	 * 
	 * @return
	 */
	public static Vertx getVertx() {
		return vertx;
	}

	/**
	 * main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			// Initialize HTTP server which handles OTPs
			HttpServer server = new HttpServer();
			server.startOtpServer();

			System.out.println(STATIC.requestId + " | main | Generating mobile OTP for JWT token generation...");

			// Generate JWT at the beginning
			CowinJwt.sendMobileOtp();
			STATIC.mobileOtpPoller();
			
			System.out.println(STATIC.requestId + " | main | Config: " + STATIC.configJson);

			String pollingObjectType = STATIC.configJson.getString("polling_object_type");
			long frequencyMs = STATIC.configJson.getLong("poll_frequency_ms");

			// Setting polling frequency to 5 seconds by default if its configured to less than 1 minute/60000ms
//			frequencyMs = (frequencyMs < 900) ? 5000 : frequencyMs;

			String url;
			String date;
			Map<String, String> queryParams;

			switch (pollingObjectType) {

			case "available-slots-by-pin":

				// Read relevant configs from cowin.config
				date = STATIC.configJson.getString("date");
				String pincode = STATIC.configJson.getString("pincode");

				// Build URL
				url = STATIC.getPincodeSearchUrl();
				System.out.println(STATIC.requestId + " | main | URL: " + url);

				// Query params map
				queryParams = new HashMap<>();
				queryParams.put(STATIC.API.QUERYPARAM.DATE, date);
				queryParams.put(STATIC.API.QUERYPARAM.PINCODE, pincode);
				Utilities.initiatePoller(vertx, frequencyMs, queryParams);
				break;

			case "available-slots-by-district":

				// Query params map
				String districtId = STATIC.configJson.getString("district_id");
				date = STATIC.configJson.getString("date");
				queryParams = new HashMap<>();
				queryParams.put(STATIC.API.QUERYPARAM.DATE, date);
				queryParams.put(STATIC.API.QUERYPARAM.DISTRICT_ID, districtId);
				Utilities.initiatePoller(vertx, frequencyMs, queryParams);
				break;

			default:
				break;
			}
			System.out.println(STATIC.requestId + " | main | Execution ends here...");

		} catch (Exception exception) {
			System.out.println(STATIC.requestId + " | main | Exception: " + exception);

		} finally {

		}
	}

}
