package com.akash.cowin.redis;

import com.akash.cowin.STATIC;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Aakash Hirve
 *
 */
public class RedisCache {
	private static volatile JedisPool pool; 
	static int maxActiveConnections = 8;
	static int maxWaitInMillis = 2000;
	static String host = "127.0.0.1";
	static int port = 6379;
	static int REDIS_DB = 1;
	
	private static Jedis jedis;
	
	private static JedisPool initRedisCache() {
		try {
			if (pool == null) {
				JedisPoolConfig jedisConfig = new JedisPoolConfig();
				jedisConfig.setMaxTotal(maxActiveConnections);
				jedisConfig.setMaxWaitMillis(maxWaitInMillis);
				pool = new JedisPool(jedisConfig, host, port);
				
				System.out.println(STATIC.requestId + "initRedisCache | Pool initialised successfully !");
			}
		} catch (Exception ex) {
			System.out.println(STATIC.requestId + "initRedisCache | Exception : " + ex);
		}
		return pool;
	}
	
	public static Jedis getRedisPool() {
		initRedisCache();
		jedis = pool.getResource();
		jedis.getClient().setTimeoutInfinite();
		return jedis;
	}
	
	public static void setKey (String key, String value) {
		jedis.set(key, value);
		jedis.close();
	}
	
	public static String getKey (String key) {
		String value = jedis.get(key);
		jedis.close();
		return value;
	}
	
	public static Boolean keyExists (String key) {
		 Boolean isExists = jedis.exists(key);
		 jedis.close();
		return isExists;
	}
	
	public static void setKey(String key, String value, int expiryTimeSeconds) {
		System.out.println(STATIC.requestId + " | setKey | Saving "+key+" to redis with expiry "+expiryTimeSeconds+ " seconds...");
		jedis.set(key, value);
		jedis.expire(key, expiryTimeSeconds);
		jedis.close();
	}
	
}
