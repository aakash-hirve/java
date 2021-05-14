package com.akash.cowin;

import java.util.UUID;

import com.akash.cowin.common.CowinJwt;
import com.akash.cowin.common.Utilities;
import com.akash.cowin.redis.RedisCache;

import io.vertx.core.json.JsonObject;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class STATIC {
	
	public static final JsonObject configJson = Utilities.readConfig("");
	public static final String requestId = UUID.randomUUID().toString();
	
	public static String getDistrictSearchUrl() {
		return STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.SLOTS_BY_DISTRICT;
	}
	
	public static String getPincodeSearchUrl() {
		return STATIC.API.ENDPOINT.PRODUCTION + STATIC.API.ROUTE.SLOTS_BY_PIN;
	}
	
	public static String getPollType() {
		return configJson.getString("polling_object_type");
	}
	
	public static void mobileOtpPoller() {
		InitiateCowin.getVertx().setPeriodic(60000, handleJwtGeneration -> {
			if (!RedisCache.keyExists("jwt_token")) {
				System.out.println(STATIC.requestId + " | mobileOtpPoller (10 sec poller) | JWT expired creating a new one...");
				CowinJwt.sendMobileOtp();
			}
		});
	}
	
	
	public static final class API {
		public static final class ROUTE {
			public static final String SLOTS_BY_PIN = "appointment/sessions/public/calendarByPin";
			public static final String SLOTS_BY_DISTRICT = "appointment/sessions/calendarByDistrict";
			public static final String GENERATE_MOBILE_OTP = "auth/generateMobileOTP";
			public static final String VALIDATE_MOBILE_OTP = "auth/validateMobileOtp";
			public static final String GET_BENEFICIARIES = "appointment/beneficiaries";
			public static final String SCHEDULE_APPOINTMENT = "/v2/appointment/schedule";
		}
		
		public static final class ENDPOINT {
			public static final String PRODUCTION = "https://cdn-api.co-vin.in/api/v2/";
		}
		
		public static final class HEADER {
			public static final String AUTHORIZATION = "Authorization";
			public static final String ACCEPT = "Accept";
		}
		
		public static final class QUERYPARAM {
			public static final String PINCODE = "pincode";
			public static final String DATE = "date";
			public static final String DISTRICT_ID = "district_id";
		}
		
		
	}
}
