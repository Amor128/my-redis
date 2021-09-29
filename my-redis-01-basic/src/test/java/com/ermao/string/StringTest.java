package com.ermao.string;

import static org.junit.Assert.*;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.Base64;
import java.util.List;

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

	@Test
	public void decrAndDecrBy() {
		Long view = j.decr("view");
		assertEquals(-1L, view.longValue());

		view = j.decrBy("view", 4L);
		assertEquals(-5L, view.longValue());

		String viewStr = j.get("view");
		assertNotNull(viewStr);
	}

	@Test
	public void incrByFloat() {
		Double weight = j.incrByFloat("weight", 45.15D);
		assert weight - 45.15D < 0.001;

		weight = j.incrByFloat("weight", 10.35D);
		assert weight - 55.50D < 0.001;

		weight = j.incrByFloat("weight", -10L);
		assert weight - 45.50D < 0.01;
	}

	@Test
	public void appendANdLen() {
		String status = j.set("key", "hello");
		assertEquals("OK", status);

		Long key = j.append("key", " world!");
		assertEquals(12L, key.longValue());

		String keyStr = j.get("key");
		assertEquals(keyStr, "hello world!");

		Long strLen = j.strlen("key");
		assertEquals(12L, strLen.longValue());
	}

	@Test
	public void mSetAndMGet() {
		String status = j.mset("a", "1", "b", "2", "c", "3");
		assertEquals("OK", status);

		String b = j.get("b");
		assertEquals("2", b);

		List<String> list = j.mget("a", "c");
		assertEquals("1", list.get(0));
		assertEquals("3", list.get(1));
	}

	@Test
	public void bitOperation1() {
		// bar 对应的 ascii 是 98、97、114
		// 二进制是 0110_0010, 0110_0001, 0111_0010
		// 三个字符按照我们认为的顺序并排连接
		String status = j.set("foo", "bar");
		assertEquals("OK", status);

		Boolean foo_0_bit = j.getbit("foo", 0L);
		assertEquals(false, foo_0_bit);

		Boolean foo_8_bit = j.getbit("foo", 6L);
		assertEquals(true, foo_8_bit);

		// 超出范围，默认为 0
		Boolean foo_max_bit = j.getbit("foo", 100L);
		assertEquals(false, foo_max_bit);

		Boolean statusSet = j.setbit("foo", 6L, false);
		assertEquals(true, statusSet);

		statusSet = j.setbit("foo", 7L, true);
		assertEquals(false, statusSet);

		Long fooBitCount = j.bitcount("foo");
		assertEquals(10L, fooBitCount.longValue());

		// aa 中 i bit 的个数
		fooBitCount = j.bitcount("foo", 0L, 1L);
		assertEquals(6L, fooBitCount.longValue());
	}

	@Test
	public void bitOperation2() {
		// redis 内置的 与或非操作
		String status = j.set("foo1", "bar");
		assertEquals("OK", status);

		status = j.set("foo2", "aar");
		assertEquals("OK", status);

		// 结果存储在 destKey 中，返回结果字符串的字节长度
		Long result = j.bitop(BitOP.OR, "foo1", "foo2");
		assertEquals(3L, result.longValue());

		status = j.set("foo", "bar");
		assertEquals("OK", status);

		// 查找字符串第一个 0 bit 的位置
		Long pos = j.bitpos("foo", true);
		assertEquals(1L, pos.longValue());

		// 1 和 2 代表查询的范围是从第 1 到第 2 个字节，如果超出范围则补 0
		pos = j.bitpos("foo", true, new BitPosParams(1L, 2L));
		assertEquals(9L, pos.longValue());
	}

}
