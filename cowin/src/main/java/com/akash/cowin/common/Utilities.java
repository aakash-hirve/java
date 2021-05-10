package com.akash.cowin.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.mail.Session;

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
//			System.out.println(requestId + " | readConfig | Config: " + configData);
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
	public static void initiatePoller(String requestId, Vertx vertx, HttpRequest<Buffer> request, long frequencyMs) {
		System.out.println(requestId + " | initiatePoller | Poller initiated with frequency " + frequencyMs + " ms");
		vertx.setPeriodic(frequencyMs, handler -> {
			System.out.println(requestId + " | initiatePoller | Polling cowin...");
			HttpUtilities.handleResponse(requestId, request);

		});
	}

	/**
	 * Form generate request
	 * 
	 * @param webClient
	 * @param url
	 * @param queryParams
	 * @return
	 */
	public static HttpRequest<Buffer> generateRequest(WebClient webClient, String url,
			Map<String, String> queryParams) {
		System.out.println("generateRequest | Generating request...");
		HttpRequest<Buffer> request = webClient.getAbs(url);
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
	public static void generateEmailMessage(String requestId, String response, JsonObject inputJson) {
		JsonArray availableSessionIds = new JsonArray();
		int centersCount = 1;

		String emailMessage = "Hello! <br><br>"
				+ "<table style=\"width:100%; border: 1px solid black\"> <tr>\r\n"
				+ "<th>Name</th>\r\n" + "<th>Address</th>\r\n" + "<th>Age Limit</th>\r\n" + "<th>Vaccine Type</th>\r\n"
				+ "<th> Date </th>\r\n" + "<th>Time Slots</th>\r\n" + "<th>Available Count</th>\r\n" + "</tr>";
		try {
			JsonArray centersArray = new JsonObject(response).getJsonArray("centers");

			for (Object currentCenter : centersArray) {
				++centersCount;
				JsonObject currentCenterJson = (JsonObject) currentCenter;

				String centerName = currentCenterJson.getString("name");

				String centerAddress = currentCenterJson.getString("address");

				JsonArray currentCenterSessions = currentCenterJson.getJsonArray("sessions");
				for (Object session : currentCenterSessions) {
					JsonObject sessionJson = (JsonObject) session;
					int currentSessionAvailableCapacity = sessionJson.getInteger("available_capacity");

					// Check if a center has available vaccination in any of the sessions
					if (currentSessionAvailableCapacity > 0) {

						String sessionId = sessionJson.getString("session_id");
						availableSessionIds.add(sessionId);

						// Check if email has been sent for particular session, dont send again if already sent
						if (checkSession(sessionId) > 0) {

							System.out.println("\n\n" + requestId + " | generateEmailMessage | Current Center Name: "+ centerName);
							System.out.println(requestId + " | generateEmailMessage | Available capacity: "+ currentSessionAvailableCapacity);

							int availableSessionCapacity = sessionJson.getInteger("available_capacity");
							int minimumAgeLimit = sessionJson.getInteger("min_age_limit");
							String vaccineType = sessionJson.getString("vaccine");
							String date = sessionJson.getString("date");
							System.out.println(requestId
									+ " | generateEmailMessage | Session Information: Age Limit: " + minimumAgeLimit
									+ "Vaccine Type: " + vaccineType + "Session Date: " + date);

							JsonArray timeSlots = sessionJson.getJsonArray("slots");
							emailMessage += "<tr><td> " + centerName + " </td> <td> " + centerAddress + " </td> <td> "
									+ minimumAgeLimit + " </td> <td> " + vaccineType + " </td>  <td> " + date
									+ " </td> <td> " + timeSlots + " </td> <td>" + availableSessionCapacity
									+ "</td></tr>";
							
							System.out.println("Center actual count: "+centersArray.size()+ " =? Center current count: "+centersCount);
							
							Session emailSession = EmailConfigs.getEmailSession(inputJson);
							System.out.println(requestId+ " | get | Sending email notification...");

							// Send Email
							EmailConfigs.getInstance().sendEmail(requestId, emailMessage, emailSession, inputJson);
							
						}
					}
				}
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Checks how many notifications have been sent already, sends 3 emails for any
	 * available session
	 * 
	 * @param sessionId
	 * @return
	 */
	public static int checkSession(String sessionId) {
		int resendCount = -1;
		RedisCache.getRedisPool();
		if (!RedisCache.keyExists(sessionId)) {
			RedisCache.setKey(sessionId, "1");
			resendCount = 1;
		} else if (Integer.parseInt(RedisCache.getKey(sessionId)) >= 0) {
			String sentCount = RedisCache.getKey(sessionId);
			resendCount = Integer.parseInt(sentCount) - 1;
			RedisCache.setKey(sessionId, String.valueOf(resendCount));
		}
		return resendCount;
	}

}
