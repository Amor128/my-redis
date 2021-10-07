package com.ermao.basic.hash;

import com.ermao.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/9/30 8:13
 */
public class HashTest {
	private static Jedis j;

	@Before
	public void before() {
		j = new Jedis(Config.HOST, Config.PORT);
		assertEquals(j.auth(Config.PASSWORD), "OK");
		j.flushAll();
	}

	@After
	public void after() {
		j.close();
	}

	@Test
	public void setAndGet() {
		Long effected = j.hset("car", "price", "500");
		assertEquals(1L, effected.longValue());

		effected = j.hset("car", "color", "red");
		assertEquals(1L, effected.longValue());

		String carPrice = j.hget("car", "price");
		assertEquals("500", carPrice);

		String carColor = j.hget("car", "color");
		assertEquals("red", carColor);

		Map<String, String> map = new HashMap<>();
		map.put("color", "green");
		map.put("weight", "1.3");
		effected = j.hset("apple:1", map);
		assertEquals(2L, effected.longValue());

		map.replace("color", "red");
		map.replace("weight", "3.1");
		j.hmset("apple:2", map);
		assertEquals(2L, effected.longValue());

		String apple1Color = j.hget("apple:1", "color");
		assertEquals("green", apple1Color);

		String apple2Weight = j.hget("apple:2", "weight");
		assertEquals("3.1", apple2Weight);

		List<String> apple2Attributes = j.hmget("apple:2", "color", "weight");
		assertEquals("red", apple2Attributes.get(0));
		assertEquals("3.1", apple2Attributes.get(1));

		Map<String, String> apple1All = j.hgetAll("apple:1");
		assertEquals("green", apple1All.get("color"));
		assertEquals("1.3", apple1All.get("weight"));
	}

	@Test
	public void otherHashCommands() {
		long effected = j.hset("apple", "color", "red");
		assertEquals(1L, effected);

		Boolean isExist = j.hexists("apple", "color");
		assertEquals(true, isExist);

		effected = j.hsetnx("apple", "weight", "3");
		assertEquals(1L, effected);

		Long incrWeight = j.hincrBy("apple", "weight", 1L);
		assertEquals(4L, incrWeight.longValue());

		double incrWeightFloat = j.hincrByFloat("apple", "weight", 0.3);
		assert incrWeightFloat - 4.3 < 0.01;

		effected = j.hdel("apple", "color", "weight");
		assertEquals(2L, effected);

		List<String> list = j.hmget("apple", "color", "weight");
		list.forEach(Assert::assertNull);
	}

	@Test
	public void getOnlyKeysOrValue() {
		long effected = j.hset("apple", "color", "red");
		assertEquals(1L, effected);

		effected = j.hset("apple", "weight", "3.14");
		assertEquals(1L, effected);

		long colorLen = j.hstrlen("apple", "color");
		assertEquals(3L, colorLen);

		Set<String> keys = j.hkeys("apple");
		assertTrue(keys.contains("color"));
		assertTrue(keys.contains("weight"));

		List<String> values = j.hvals("apple");
		assertTrue(values.contains("red"));
		assertTrue(values.contains("3.14"));

		long len = j.hlen("apple");
		assertEquals(2L, len);
	}

	
}
