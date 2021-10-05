package com.ermao.list;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/10/1 12:04
 */
public class ListTest {

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
	public void pushAndPop() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		String number = j.lpop("numbers");
		assertEquals("3", number);

		number = j.rpop("numbers");
		assertEquals("6", number);
	}

	@Test
	public void subListAndLen() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		len = j.llen("numbers");
		assertEquals(6L, len);

		List<String> numbers = j.lrange("numbers", 1L, 4L);
		assertEquals(4, numbers.size());
		assertEquals("2", numbers.get(0));

		// 顺序永远只能是从左向右
		List<String> numbers1 = j.lrange("numbers", -3L, -1L);
		// 4, 5, 6
		assertEquals("4", numbers1.get(0));

		// 当第二个参数 count > 0 时，删除所有匹配元素，负数和正数则会分别
		// 从左到右或者从右到左删除指定个匹配的元素
		long effected = j.lrem("numbers", 0, "1");
		assertEquals(1L, effected);
	}

	@Test
	public void getAndSetListValueByIndex() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		String number_2 = j.lindex("numbers", 1L);
		assertEquals("2", number_2);

		// 下标越界会返回 null
		String number_7 = j.lindex("numbers", 7L);
		assertNull(number_7);

		String status = j.lset("numbers", 4L, "55");
		assertEquals("OK", status);

		String number_4 = j.lindex("numbers", 4L);
		assertEquals("55", number_4);

		try {
			number_7 = j.lset("numbers", 7L, "77");
			assertNull(number_7);
		} catch (JedisDataException e) {
			System.err.println(e);
		}
	}

	@Test
	public void lTrim() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		String status = j.ltrim("numbers", 1L, 4L);
		// 2, 1, 4, 5
		assertEquals("OK", status);

		List<String> numbers = j.lrange("numbers", 0, -1);
		assertNotNull(numbers);
		assertEquals(4, numbers.size());
		assertEquals("2", numbers.get(0));
		assertEquals("1", numbers.get(1));
		assertEquals("4", numbers.get(2));
		assertEquals("5", numbers.get(3));
	}

	@Test
	public void insertIntoList() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		len = j.linsert("numbers", ListPosition.AFTER, "2", "8");
		assertEquals(7L, len);
		// 3, 2, 8, 1, 4, 5, 6

		len = j.linsert("numbers", ListPosition.BEFORE, "1", "7");
		assertEquals(8L, len);

		String number_3 = j.lindex("numbers", 3);
		assertEquals("7", number_3);
	}

	@Test
	public void listElementTransfer() {
		long len = j.lpush("numbers", "1", "2", "3");
		assertEquals(3L, len);
		// 3, 2, 1

		len = j.rpush("numbers", "4", "5", "6");
		assertEquals(6L, len);
		// 3, 2, 1, 4, 5, 6

		String number = j.rpoplpush("numbers", "newNumbers");
		assertEquals("6", number);

		number = j.rpoplpush("numbers", "newNumbers");
		assertEquals("5", number);

		List<String> newNumbers = j.lrange("newNumbers", 0, -1);
		assertNotNull(newNumbers);
		assertEquals(2, newNumbers.size());
		assertEquals("5", newNumbers.get(0));
		assertEquals("6", newNumbers.get(1));

		// You can also use this command to move the last element to the front of list in the same list
	}
}
