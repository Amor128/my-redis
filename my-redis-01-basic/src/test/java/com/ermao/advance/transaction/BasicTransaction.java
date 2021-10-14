package com.ermao.advance.transaction;

import com.ermao.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/10/8 9:03
 */
public class BasicTransaction {
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
	public void submitTransaction() {
		// 提交事务申请，返回一个事务对象
		Transaction transaction = j.multi();
		assertNotNull(transaction);

		Response<Long> response = transaction.sadd("user:1:following", "2");
		assertNotNull(response);
		response = transaction.sadd("user:1:following", "1");
		assertNotNull(response);

		// 执行事务，返回事务的执行结果，1 代表成功，0 代表失败
		List<Object> commands = transaction.exec();
		assertNotNull(commands);
		commands.forEach(Assert::assertNotNull);
	}

	@Test
	public void discardTransaction() {
		// 提交事务申请，返回一个事务对象
		Transaction transaction = j.multi();
		assertNotNull(transaction);

		Response<Long> response = transaction.sadd("user:1:following", "2");
		assertNotNull(response);
		response = transaction.sadd("user:1:following", "1");
		assertNotNull(response);

		// 放弃事务，返回 OK
		String status = transaction.discard();
		assertEquals("OK", status);
	}

	@Test
	public void transactionErrorHandleDirect() {
		// 入队时产生语法错误，整个事务不被执行
		Transaction transaction = j.multi();
		assertNotNull(transaction);

		Response<String> response = transaction.set("key", "value");
		assertNotNull(response);
		response = transaction.set("key", null);
		assertNotNull(response);

		List<Object> commands = transaction.exec();
		assertNotNull(commands);
		commands.forEach(System.out::println);
	}

	@Test
	public void transactionErrorHandleCovert() {
		// 入队时产生语法错误，整个事务不被执行
		Transaction transaction = j.multi();
		assertNotNull(transaction);

		Response<String> response1 = transaction.set("key", "value");
		assertNotNull(response1);
		assertEquals("Response string", response1.toString());

		Response<Long> response2 = transaction.sadd("key", "new_value");
		assertNotNull(response2);
		assertEquals("Response long", response2.toString());


		Response<String> response3 = transaction.set("key", "new_new_value");
		assertNotNull(response3);
		assertEquals("Response string", response3.toString());

		List<Object> commands = transaction.exec();
		assertNotNull(commands);
		commands.forEach(System.out::println);

		String value = j.get("key");
		assertEquals("new_new_value", value);
	}

	@Test
	public void basicWatch() {
		String status = j.set("key", "1");
		assertEquals("OK", status);

		status = j.watch("key");
		assertEquals("OK", status);

		status = j.set("key", "2");
		assertEquals("OK", status);

		Transaction transaction = j.multi();
		assertNotNull(transaction);

		Response<String> response = transaction.set("key", "3");
		assertNotNull(response);
		assertEquals("Response string", response.toString());

		List<Object> commands = transaction.exec();
		assertNull(commands);	// commands 返回 null，因为没有事务命令被执行

		String value = j.get("key");
		assertEquals("2", value);
	}

	@Test
	public void optimisticLock() {
		String status = j.set("key", "1");
		assertEquals("OK", status);

		status = j.watch("key");
		assertEquals("OK", status);

		String key = j.get("key");
		assertEquals("1", key);

		key = "2";
		Transaction transaction = j.multi();
		Response<String> res = transaction.set("key", key);
		assertNotNull(res);
		assertEquals("Response string", res.toString());

		List<Object> commands = transaction.exec();
		assertNotNull(commands);
		commands.forEach(Assert::assertNotNull);

		String value = j.get("key");
		assertEquals("2", value);
	}
}
