package com.akash.cowin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.akash.cowin.common.Utilities;
import com.akash.cowin.http.HttpUtilities;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class InitiateCowin {
	private static final Vertx vertx;
	private static final String requestId = UUID.randomUUID().toString();
	private static final JsonObject inputJson = Utilities.readConfig(requestId);
	
	
	static {
		VertxOptions vertxOptions = new VertxOptions().setBlockedThreadCheckInterval(8000);
		vertx = Vertx.vertx(vertxOptions);
	}

	public static void main(String[] args) {

		try {
			System.out.println(requestId + " | main | Configuring notifications...");
			
			System.out.println(requestId + " | main | Config: "+inputJson);
			
			String pollingObjectType = inputJson.getString("polling_object_type");
			System.out.println(requestId + " | main | Polling object type: "+pollingObjectType);
			
			long frequencyMs = inputJson.getLong("poll_frequency_ms");
			System.out.println(requestId + " | main | Polling frequency in ms (milliseconds): "+frequencyMs);
			
			// Setting polling frequency to 15 minutes by default if its configured to less than 1 minute/60000ms
			frequencyMs = (frequencyMs < 10000)? 900000 : frequencyMs;
			
			String url;
			String date;
			Map<String, String> queryParams;
			HttpRequest<Buffer> request;
			
			switch (pollingObjectType) {

			case "available-slots-by-pin":
				
				// Read relevant configs from cowin.config
				date = inputJson.getString("date");
				String pincode = inputJson.getString("pincode");
				
				// Build URL
				url = STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.SLOTS_BY_PIN;
				System.out.println(requestId + " | main | URL: " + url);
				
				// Query params map
				queryParams = new HashMap<>();
				queryParams.put(STATIC.API.QUERYPARAM.DATE, date);
				queryParams.put(STATIC.API.QUERYPARAM.PINCODE, pincode);
				System.out.println(requestId + " | main | Query params map: "+queryParams);

				request = Utilities.generateRequest(HttpUtilities.getWebClient(vertx), url, queryParams);
				Utilities.initiatePoller(requestId, vertx, request, frequencyMs);
				break;
				
			case "available-slots-by-district":
				String districtId = inputJson.getString("district_id");
				date = inputJson.getString("date");
				
				url = STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.SLOTS_BY_DISTRICT;
				System.out.println(requestId + " | main | URL: " + url);
				
				// Query params map
				queryParams = new HashMap<>();
				queryParams.put(STATIC.API.QUERYPARAM.DATE, date);
				queryParams.put(STATIC.API.QUERYPARAM.DISTRICT_ID, districtId);
				System.out.println(requestId + " | main | Query params map: "+queryParams);
				request = Utilities.generateRequest(HttpUtilities.getWebClient(vertx), url, queryParams);
				Utilities.initiatePoller(requestId, vertx, request, frequencyMs);
				break;

			default:
				break;
			}
			System.out.println(requestId + " | main | Execution ends here...");
			
		} catch (Exception exception) {
			System.out.println(requestId + " | main | Exception: " + exception);

		} finally {
			
		}
	}

}
