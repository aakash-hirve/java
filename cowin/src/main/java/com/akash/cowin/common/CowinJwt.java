package com.akash.cowin.common;

import com.akash.cowin.STATIC;
import com.akash.cowin.http.HttpUtilities;
import com.akash.cowin.redis.RedisCache;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class CowinJwt {

	/**
	 * Send Mobile OTP request
	 * 
	 * @param config
	 * @return
	 */
	public static void sendMobileOtp() {
		try {
			System.out.println(STATIC.requestId + " | sendMobileOtp | Generating new OTP...");
			
			if (!RedisCache.keyExists("jwt_token")) {

				JsonObject payload = new JsonObject().put("mobile", STATIC.configJson.getString("mobile_number")).put("secret", STATIC.configJson.getString("secret"));
				HttpRequest<Buffer> request = HttpUtilities.fetchWebClient().postAbs(STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.GENERATE_MOBILE_OTP);

				handleOtpRequests(request, payload, true);
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Validate OTP
	 * 
	 * @param requestId
	 */
	public static void validateOtp(String txnId, String otp) {
		try {
			System.out.println(STATIC.requestId + " | validateOtp | Validating OTP. txnId: " + txnId + " OTP: " + otp);

			String sha256Otp = Utilities.getSHA256Hash(otp);
			System.out.println(STATIC.requestId + " | validateOtp | SHA256 hash of OTP generated...");

			JsonObject validatePayload = new JsonObject().put("otp", sha256Otp).put("txnId", txnId);
			HttpRequest<Buffer> request = HttpUtilities.fetchWebClient().postAbs(STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.VALIDATE_MOBILE_OTP);

			handleOtpRequests(request, validatePayload, false);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Handle OTP requests
	 * 
	 * @param requestId
	 * @param request
	 * @param payload
	 * @param isGenerateOtp
	 */
	static void handleOtpRequests(HttpRequest<Buffer> request, JsonObject payload, boolean isGenerateOtp) {
		System.out.println(STATIC.requestId + " | handleOtpRequests | Handling OTP request...");

		request.sendJsonObject(payload, handler -> {

			try {
				int statusCode = handler.result().statusCode();
				String statusMessage = handler.result().statusMessage();

				System.out.println(STATIC.requestId + " | handleOtpRequests | Status Code: " + statusCode + ". Status Message: " + statusMessage);

				if (handler.succeeded()) {
					String response;

					switch (statusCode) {
					case 200:

						if (isGenerateOtp) {

							String txnId = handler.result().bodyAsJsonObject().getString("txnId");
//							new OtpInput(requestId, txnId);  // This is no longer required as OTP automation is done
							RedisCache.setKey("txnId", txnId, 60);

						} else {
							String jwtToken = handler.result().bodyAsJsonObject().getString("token");
							RedisCache.setKey("jwt_token", jwtToken, 900);
							System.out.println(STATIC.requestId + " | handleOtpRequests | JWT Token: " + jwtToken);

						}
						break;

					default:
						response = handler.result().bodyAsString();
						System.out.println(STATIC.requestId + " | handleOtpRequests | Status code: " + statusCode + ". Response: " + response);
						break;
					}
				} else {
					System.out.println(STATIC.requestId + " | handleOtpRequests | Failed: " + statusCode + ". Reason: "
							+ handler.cause());
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		});
	}

//	public static void main(String args[]) {
//		RedisCache.getRedisPool();
//		HttpUtilities.getWebClient(InitiateCowin.getVertx());
//		sendMobileOtp("test",Utilities.readConfig("test"));
//	}
//	
}
