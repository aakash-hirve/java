package com.akash.cowin;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class STATIC {
	
	public static final class API {
		public static final class ROUTE {
			public static final String SLOTS_BY_PIN = "appointment/sessions/public/calendarByPin";
			public static final String SLOTS_BY_DISTRICT = "appointment/sessions/public/calendarByDistrict";
		}
		
		public static final class ENDPOINT {
			public static final String PRODUCTION = "https://cdn-api.co-vin.in/api/v2/";
		}
		
		public static final class HEADER {
			public static final String AUTHORIZATION = "";
			public static final String ACCEPT = "";
		}
		
		public static final class QUERYPARAM {
			public static final String PINCODE = "pincode";
			public static final String DATE = "date";
			public static final String DISTRICT_ID = "district_id";
		}
	}
}
