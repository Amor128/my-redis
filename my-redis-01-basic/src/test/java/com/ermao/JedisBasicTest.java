package com.ermao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author Ermao
 * Date: 2021/9/26 19:29
 */
public class JedisBasicTest {

	@Test
	public void testConnectRedis() {
		Jedis j = new Jedis("193.112.135.242", 6379);
		assertEquals(j.auth("amor19."), "OK");	// 注意 import assertEquals
		System.out.println(j.ping());
		j.close();
	}
}
