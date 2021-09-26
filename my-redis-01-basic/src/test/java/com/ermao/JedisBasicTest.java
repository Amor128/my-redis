package com.ermao;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Jedis;

/**
 * @author Ermao
 * Date: 2021/9/26 19:29
 */
public class JedisBasicTest {

	@Test
	public void builderFactoryTest() {
		Double build = BuilderFactory.DOUBLE.build("1.0".getBytes());
		assertEquals(Double.valueOf(1.0), build);
		build = BuilderFactory.DOUBLE.build("inf".getBytes());
		assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), build);
		build = BuilderFactory.DOUBLE.build("+inf".getBytes());
		assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), build);
		build = BuilderFactory.DOUBLE.build("-inf".getBytes());
		assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), build);
	}


	@Test
	public void testConnectRedis() {
		Jedis j = new Jedis("193.112.135.242", 6379);
		assertEquals(j.auth("wal14."), "OK");
		System.out.println(j.ping());
		j.close();
	}

	@Test
	public void testACL() {
	}
}
