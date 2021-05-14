package com.akash.cowin.http;

import com.akash.cowin.InitiateCowin;
import com.akash.cowin.STATIC;
import com.akash.cowin.common.CowinJwt;
import com.akash.cowin.redis.RedisCache;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class HttpServer {

	private static Router router;
	private static io.vertx.core.http.HttpServer server;

	public void startOtpServer() {
		System.out.println(STATIC.requestId + " | startOtpServer | Starting http server on port 4567...");

		router = Router.router(InitiateCowin.getVertx());
		// Add body handler
		router.route().handler(BodyHandler.create());

		router.route(HttpMethod.POST, "/handleotp").handler(handle -> {
			System.out.println(STATIC.requestId + " | /handleotp | Body: " + handle.getBodyAsString());
			try {

				if (handle.getBodyAsString().contains("OTP") && handle.getBodyAsString().contains("CoWIN")) {
					
					// Extract the OTP - This is received from my android app, will post the link for the android source code for that in GitHub readme, soon...
					String otp = handle.getBodyAsString().substring(37, 43);
					System.out.println(STATIC.requestId + " | /handleotp | OTP received! " + otp);

					// Set OTP in redis for 1 minute
					RedisCache.setKey("otp", otp, 60);

					// Validate OTP
					CowinJwt.validateOtp(RedisCache.getKey("txnId"), otp);
				} else {
					System.out.println(STATIC.requestId + " | /handleotp | SMS not for CoWIN! But here's the content ;) -> "+handle.getBodyAsString());
				}
				
				// End HTTP response
				handle.response().putHeader("Content-Type", "application/json").setChunked(true).setStatusMessage("success").setStatusCode(200).end();

			} catch (Exception exception) {
				System.out.println(STATIC.requestId + " /handleotp | Exception: " + exception.getMessage());
				exception.printStackTrace();
			}
		});

		server = InitiateCowin.getVertx().createHttpServer();

		// Add request handler to HTTP server and listen on port 8080
		server.requestHandler(router).listen(4567);

	}
}
