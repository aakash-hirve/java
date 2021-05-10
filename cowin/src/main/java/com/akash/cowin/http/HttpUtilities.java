package com.akash.cowin.http;

import com.akash.cowin.common.Utilities;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class HttpUtilities {

	private static WebClient webClient = null;

	/**
	 * Getter for vertx webclient
	 * 
	 * @param vertx
	 * @return
	 */
	public static WebClient getWebClient(Vertx vertx) {
		if (webClient == null) {
			initializeWebClient(vertx);
		}
		return webClient;
	}

	/**
	 * Initialize vertx webclient
	 * 
	 * @param vertx
	 */
	private static void initializeWebClient(Vertx vertx) {
		WebClientOptions options = new WebClientOptions().setConnectTimeout(60000);
		webClient = WebClient.create(vertx, options);
	}

	public static void closeWebClient() {
		if (webClient != null) {
			webClient.close();
		}
	}

	/**
	 * HTTP response handler
	 * 
	 * @param requestId
	 * @param request
	 */
	public static void handleResponse(String requestId, HttpRequest<Buffer> request) {
		request.send(handler -> {
			try {
				int statusCode = handler.result().statusCode();
				String statusMessage = handler.result().statusMessage();
				if (handler.succeeded()) {
					System.out.println(requestId + " | get | Status code: " + statusCode);
					System.out.println(requestId + " | get | Status message: " + statusMessage);

					JsonObject inputJson = Utilities.readConfig(requestId);
					String pollingObjectType = inputJson.getString("polling_object_type");

					switch (statusCode) {

					case 200:
						JsonObject response;
						
						switch (pollingObjectType) {
						case "available-slots-by-pin":
							response = handler.result().bodyAsJsonObject();
							// Call to generate email message
							Utilities.generateEmailMessage(requestId, response.encode(), inputJson);
							break;
							
						case "available-slots-by-district":
							response = handler.result().bodyAsJsonObject();
							// Call to generate email message
							Utilities.generateEmailMessage(requestId, response.encode(), inputJson);							
					
							break;

						default:
							response = handler.result().bodyAsJsonObject();
							System.out.println(requestId + " | get | Status code: " + statusCode + ". Response: " + response);
							break;
						}
						break;

					default:
						System.out.println(requestId + " | get | Response: " + handler.result().bodyAsString());
						break;
					}

				} else {
					System.out.println(requestId + " | get | Failed with status code: " + statusCode + ". Cause: "+ handler.cause());
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});
	}
}
