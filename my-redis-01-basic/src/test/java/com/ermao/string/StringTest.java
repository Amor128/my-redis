package com.ermao.string;

import static org.junit.Assert.*;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.Base64;

/**
 * @author Ermao
 * Date: 2021/9/27 10:18
 */
public class StringTest {
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
	public void ping() {
		System.out.println(j.ping());
	}

	@Test
	public void setAndGetTest() {
		String status = j.set("view", "123");
		assertEquals("OK", status);

		String value = j.get("view");
		assertEquals("123", value);

		int newViews = Integer.parseInt(value) + 1;
		status = j.set("view", String.valueOf(newViews));
		assertEquals("OK", status);

		assertEquals("124", j.get("view"));
	}

	@Test
	public void incrAndIncrBy() {
		String status = j.set("view", "123");
		assertEquals("OK", status);

		Long view = j.incr("view");
		assertEquals(124L, view.longValue());

		view = j.incrBy("view", 2L);
		assertEquals(126L, view.longValue());
	}

	@Test
	public void incrPrimaryKey() {
		Long appleCount = j.incr("apples:count");
		assertEquals(1L, appleCount.longValue());

		j.incr("apples:count");
		j.incr("apples:count");
		appleCount = j.incr("apples:count");
		assertEquals(4L, appleCount.longValue());
	}

	@Test
	public void setAndGetObject() {
		// 序列化对象得到字符串，使用 base64 编码
		// may occur that invalid header for outPutStream/InputStream
		Apple oldApple = new Apple();
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		String oldAppleStr = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(oldApple);
			oldAppleStr = Base64.getEncoder().encodeToString(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String status = j.set("apple", oldAppleStr);
		assertEquals("OK", status);
		String newAppleStr = j.get("apple");
		assertEquals(oldAppleStr, newAppleStr);

		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(Base64.getDecoder().decode(newAppleStr));
			ois = new ObjectInputStream(bis);
			Apple newApple  = (Apple) ois.readObject();
			assertEquals(oldApple, newApple);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			oos.close();
			bos.close();
			ois.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
