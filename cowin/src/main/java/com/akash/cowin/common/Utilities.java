package com.akash.cowin.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import javax.mail.Session;
import org.apache.commons.codec.digest.DigestUtils;
import com.akash.cowin.STATIC;
import com.akash.cowin.http.HttpUtilities;
import com.akash.cowin.redis.RedisCache;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

/**
 * @author Aakash Hirve
 *
 */
public class Utilities {

	/**
	 * Read polling endpoint information from a config file
	 * 
	 * @return
	 */
	public static JsonObject readConfig(String requestId) {
		JsonObject configJson = null;
		try {
			String configData = new String(Files.readAllBytes(Paths.get("cowin.config")), StandardCharsets.UTF_8);
			configJson = new JsonObject(configData);
		} catch (Exception exception) {
			System.out.println(requestId + " | readConfig | Exception: " + exception);
		}
		return configJson;
	}

	/**
	 * Initiate polling the cowin endpoint with 30 minute duration. Set to 30
	 * minutes as cache is updated at 30 minutes for cowin
	 * 
	 * @param requestId
	 * @param vertx
	 * @param request
	 */
	public static void initiatePoller(Vertx vertx, long frequencyMs, Map<String, String> queryParams) {
		System.out.println(STATIC.requestId + " | initiatePoller | Poller initiated with frequency " + frequencyMs + " ms");
		
	
		switch (STATIC.getPollType()) {
		
		case "available-slots-by-district":
			vertx.setPeriodic(frequencyMs, handler -> {
//				System.out.println(STATIC.requestId + " | initiatePoller | Polling cowin...");
				String url = STATIC.getDistrictSearchUrl();
			
				HttpRequest<Buffer> request = Utilities.generateRequest(HttpUtilities.getWebClient(vertx), url, queryParams);
				HttpUtilities.handleResponse(request);

			});
			break;
		default:
			vertx.setPeriodic(frequencyMs, handler -> {
				System.out.println(STATIC.requestId + " | initiatePoller | Polling cowin...");

				HttpRequest<Buffer> request = Utilities.generateRequest(HttpUtilities.getWebClient(vertx), STATIC.getPincodeSearchUrl(), queryParams);
				HttpUtilities.handleResponse(request);

			});
			break;
		}
		
	}

	/**
	 * Form generate request
	 * 
	 * @param webClient
	 * @param url
	 * @param queryParams
	 * @return
	 */
	public static HttpRequest<Buffer> generateRequest(WebClient webClient, String url, Map<String, String> queryParams) {
//		System.out.println(STATIC.requestId + " | generateRequest | Generating request...");
		HttpRequest<Buffer> request = webClient.getAbs(url);
		request.putHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
		request.putHeader("Accept-Language", "hi_IN");
		request.putHeader("Origin", "https://selfregistration.cowin.gov.in");
		request.putHeader("Referer", "https://selfregistration.cowin.gov.in/");
		request.putHeader("Access-Control-Request-Headers", "authorization");
		
		// If JWT token exists add to header
		if(RedisCache.keyExists("jwt_token")) {
			request.putHeader("Authorization", "Bearer "+RedisCache.getKey("jwt_token"));
		}
		
		// Check query parameters
		if (!queryParams.isEmpty() && null != queryParams) {
			queryParams.forEach((key, value) -> {
				request.setQueryParam(key, value);
			});
		}
		return request;
	}

	/**
	 * Generate the message to be sent in email
	 * 
	 * @param requestId
	 * @param response
	 * @return
	 */
	public static void generateEmailMessage(String response) {
		JsonArray availableSessionIds = new JsonArray();
//		int myAgeLimit = STATIC.configJson.getInteger("age_limit");
				
		String emailMessage = "Open COWIN portal now!!! <br><br>"
				+ "<table style=\"width:100%; border: 1px solid black\"> <tr>\r\n"
				+ "<th>Name</th>\r\n"
				+ "<th>Address</th>\r\n"
				+ "<th>Age Limit</th>\r\n"
				+ "<th>Pincode</th>\r\n"
				+ "<th>Date </th>\r\n"
				+ "<th>Available Count</th>\r\n"
				+ "</tr>";
		try {
			JsonArray centersArray = new JsonObject(response).getJsonArray("centers");
			
			for (Object currentCenter : centersArray) {
				JsonObject currentCenterJson = (JsonObject) currentCenter;

				String centerName = currentCenterJson.getString("name");
				int pincode = currentCenterJson.getInteger("pincode");
				String centerAddress = currentCenterJson.getString("address");

				JsonArray currentCenterSessions = currentCenterJson.getJsonArray("sessions");
				for (Object session : currentCenterSessions) {
					JsonObject sessionJson = (JsonObject) session;
					int currentSessionAvailableCapacity = sessionJson.getInteger("available_capacity");
//					int minimumAgeLimit = sessionJson.getInteger("min_age_limit");
					
					// Check if a center has available vaccination in any of the sessions
					if (currentSessionAvailableCapacity > 0) {
						
						String sessionId = sessionJson.getString("session_id");
						availableSessionIds.add(sessionId);

						// Check if email has been sent for particular session, dont send again if already sent
						if (checkSession(sessionId) == 1) {
							System.out.println("\n" + STATIC.requestId + " | generateEmailMessage | Current Center Name: "+ centerName+". Pincode: "+pincode+". Address: "+centerAddress+" | Available capacity: "+ currentSessionAvailableCapacity);
							
							// Schedule appointment - Nope not happening, forget it...
//							scheduleAppointment(requestId, sessionId, centerName, centerAddress);
							
							// Send notification Email
							processSessionsData(sessionJson, centerName, centerAddress, pincode, emailMessage);
							
						}
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Limits notification for each unique session across centers to 1 email 
	 * @param sessionId
	 * @return
	 */
	public static int checkSession(String sessionId) {
		int resendCount = -1;
		
		if (!RedisCache.keyExists(sessionId)) {
			RedisCache.setKey(sessionId, "1");
			resendCount = 1;
		} else {
			resendCount = -1;
		}
		return resendCount;
	}

	/**
	 * Generate SHA256 hash for OTP
	 * @param otp
	 * @return
	 */
	public static String getSHA256Hash(String otp) {
		String sha256hex = DigestUtils.sha256Hex(otp);
		return sha256hex;
	}
	
	/**
	 * Process sessions data and generate email message
	 */
	private static void processSessionsData(JsonObject sessionJson, String centerName, String centerAddress, int pincode, String emailMessage) {
		int availableSessionCapacity = sessionJson.getInteger("available_capacity");
		int minimumAgeLimit = sessionJson.getInteger("min_age_limit");
		String vaccineType = sessionJson.getString("vaccine");
		String date = sessionJson.getString("date");
		
		System.out.println(STATIC.requestId
				+ " | generateEmailMessage | Session Information: Age Limit: " + minimumAgeLimit
				+ ". Vaccine Type: " + vaccineType + ". Session Date: " + date);

//		JsonArray timeSlots = sessionJson.getJsonArray("slots");
		emailMessage += "<tr><td> " + centerName + " </td> <td> " + centerAddress + " </td> <td> "
				+ minimumAgeLimit + " </td> <td>"+pincode+" </td>  <td> " + date
				+ " </td>  <td>" + availableSessionCapacity
				+ "</td></tr>";
				
		Session emailSession = EmailConfigs.getEmailSession();
		System.out.println(STATIC.requestId+ " | get | Sending email notification...");

		// Send Email
		EmailConfigs.getInstance().sendEmail(emailMessage, emailSession);
	}

	/*
	 * Schedules an appointment for vaccination. Nope you can't.
	
	private static void scheduleAppointment(String requestId, String sessionId, String centerName, String centerAddress) {
		System.out.println(requestId + " | scheduleAppointment | Scheduling appointment for "+sessionId+" at center "+centerName+" and address: "+centerAddress);
		
		
		JsonArray beneficiaries = InitiateCowin.getConfig().getJsonArray("beneficiaries");
		String slot = InitiateCowin.getConfig().getString("slot");
		
		
		
		String apiUrl = STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.SCHEDULE_APPOINTMENT;
		System.out.println(requestId + " | scheduleAppointment | Api Url: "+apiUrl);
		
		JsonObject payload = new JsonObject().put("dose", 1).put("session_id", sessionId).put("slot", slot).put("beneficiaries", beneficiaries);
		System.out.println(requestId + " | scheduleAppointment | Appointment booking payload: "+payload);
		Map<String, String> queryParams = new HashMap<>();
		
		// Generate appointment request
		HttpRequest<Buffer> request = generateRequest(HttpUtilities.fetchWebClient(), apiUrl, queryParams);
		
		// Send request
//		HttpUtilities.handleResponse(requestId, request, "schedule-appointment");
	} */
}
