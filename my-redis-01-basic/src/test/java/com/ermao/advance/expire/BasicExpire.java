package com.ermao.advance.expire;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/10/14 15:05
 */
public class BasicExpire {
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
	public void setExpire() {
		String status = j.set("session:123", "uid321");
		assertEquals("OK", status);
		long expire = j.expire("session:123", 3L);
		assertEquals(1L, expire);
		String s = j.get("session:123");
		assertEquals("uid321", s);
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s = j.get("session:123");
		assertNull(s);

		status = j.setex("session:321", 3L, "uid123");
		assertEquals("OK", status);
		s = j.get("session:321");
		assertEquals("uid123", s);
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s = j.get("session:321");
		assertNull(s);
	}

	@Test
	public void queryExpire() {
		String status = j.set("session:123", "uid321");
		assertEquals("OK", status);
		status = j.set("session:321", "uid123");
		assertEquals("OK", status);
		long expire = j.expire("session:123", 10L);

		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long ttl = j.ttl("session:123");
		assertTrue(ttl < 8L && ttl > 4L);
		ttl = j.ttl("session:321");
		assertEquals(-1L, ttl);
	}
}
