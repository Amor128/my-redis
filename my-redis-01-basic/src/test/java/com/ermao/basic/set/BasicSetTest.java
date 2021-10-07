package com.ermao.basic.set;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/10/4 8:54
 */
public class BasicSetTest {

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
	public void basicOperationOnSet() {
		long effected = j.sadd("numbers", "1", "2", "3");
		assertEquals(3L, effected);

		effected = j.sadd("numbers", "3", "4", "5");
		assertEquals(2L, effected);

		effected = j.srem("numbers", "1", "2");
		assertEquals(2L, effected);

		boolean rel = j.sismember("numbers", "1");
		assertFalse(rel);
		rel = j.sismember("numbers", "3");
		assertTrue(rel);

		Set<String> numbers = j.smembers("numbers");
		assertEquals(3, numbers.size());
		assertTrue(numbers.contains("3"));
		assertTrue(numbers.contains("4"));
		assertTrue(numbers.contains("5"));
	}

	@Test
	public void setDiff() {
		// if you want to store the result into other key, just append "store" behind the command

		long effected = j.sadd("setA", "1", "2", "3");
		assertEquals(3L, effected);

		effected = j.sadd("setB", "3", "4", "5");
		assertEquals(3L, effected);

		effected = j.sadd("setC", "2", "5", "6");
		assertEquals(3L, effected);


		// A - B
		Set<String> diffs = j.sdiff("setA", "setB");
		assertEquals(2, diffs.size());
		assertTrue(diffs.contains("1"));
		assertTrue(diffs.contains("2"));

		// A - B - C
		diffs = j.sdiff("setA", "setB", "setC");
		assertEquals(1, diffs.size());
		assertTrue(diffs.contains("1"));
	}

	@Test
	public void setInter() {
		// if you want to store the result into other key, just append "store" behind the command

		long effected = j.sadd("setA", "1", "2", "3");
		assertEquals(3L, effected);

		effected = j.sadd("setB", "3", "4", "5");
		assertEquals(3L, effected);

		effected = j.sadd("setC", "2", "3", "6");
		assertEquals(3L, effected);

		Set<String> inters = j.sinter("setA", "setB", "setC");
		assertEquals(1, inters.size());
		assertTrue(inters.contains("3"));
	}

	@Test
	public void setScale() {
		long effected = j.sadd("setA", "1", "2", "3");
		assertEquals(3L, effected);

		long card = j.scard("setA");
		assertEquals(3L, card);
	}

	@Test
	public void setUnion() {
		// if you want to store the result into other key, just append "store" behind the command

		long effected = j.sadd("setA", "1", "2", "3");
		assertEquals(3L, effected);

		effected = j.sadd("setB", "3", "4", "5");
		assertEquals(3L, effected);

		effected = j.sadd("setC", "2", "3", "6");
		assertEquals(3L, effected);

		Set<String> union = j.sunion("setA", "setB", "setC");
		assertNotNull(union);
		assertEquals(6, union.size());
		assertTrue(union.contains("6"));
		assertTrue(union.contains("5"));
		assertTrue(union.contains("4"));
		assertTrue(union.contains("3"));
		assertTrue(union.contains("2"));
		assertTrue(union.contains("1"));
	}

	@Test
	public void randomGetFromSet() {
		long effected = j.sadd("setA", "1", "2", "3");
		assertEquals(3L, effected);

		String value = j.srandmember("setA");
		assertTrue(value.equals("1") || value.equals("2") || value.equals("3"));

		List<String> setA = j.srandmember("setA", 2);
		assertEquals(2, setA.size());

		String sPop = j.spop("setA");
		assertNotNull(sPop);

		long size = j.scard("setA");
		assertEquals(2L, size);
	}
}
