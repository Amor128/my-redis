package com.ermao.zset;

import com.ermao.Config;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;

import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Ermao
 * Date: 2021/10/5 7:52
 */
public class BasicZSetTest {

	private static Jedis j;

	@Before
	public void before() {
		j = new Jedis(Config.HOST, Config.PORT);
		assertEquals(j.auth(Config.PASSWORD), "OK");
		j.flushAll();
	}

	@After
	public void after() {
		j.flushAll();
		j.close();
	}

	@Test
	public void cURD() {
		long effected = j.zadd("fruit", 1, "apple");
		assertEquals(1L, effected);

		HashMap<String, Double> map = new HashMap<>();
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		effected = j.zadd("fruit", map);
		assertEquals(3L, effected);

		// 根据值删除元素
		effected = j.zrem("fruit", "banana", "watermelon");
		assertEquals(2L, effected);

		// 修改元素分数
		effected  = j.zadd("fruit", 2, "apple");
		assertEquals(0L, effected);	// 注意这里改变了元素的 score，但是返回值确是 0

		// 根据元素值查看分数
		Double appleScore = j.zscore("fruit", "apple");
		assertTrue(appleScore - 2D < 0.01D);
	}

	@Test
	public void getElementsByRankIndex() {
		HashMap<String, Double> map = new HashMap<>();
		map.put("apple", 1D);
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		long effected = j.zadd("fruit", map);
		assertEquals(4L, effected);

		// 根据分数的排名从低到高选取指定范围的元素
		Set<String> fruit_0_3 = j.zrange("fruit", 0, 2);
		assertNotNull(fruit_0_3);
		assertEquals(3, fruit_0_3.size());
		assertTrue(fruit_0_3.contains("apple"));
		assertTrue(fruit_0_3.contains("banana"));
		assertTrue(fruit_0_3.contains("orange"));

		// 根据分数的排名从高到低选取指定范围的元素
		fruit_0_3 = j.zrevrange("fruit", 0, 2);
		assertNotNull(fruit_0_3);
		assertEquals(3, fruit_0_3.size());
		assertTrue(fruit_0_3.contains("banana"));
		assertTrue(fruit_0_3.contains("orange"));
		assertTrue(fruit_0_3.contains("watermelon"));
	}

	@Test
	public void getElementsByScore() {
		HashMap<String, Double> map = new HashMap<>();
		map.put("apple", 1D);
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		long effected = j.zadd("fruit", map);
		assertEquals(4L, effected);

		// 根据分数的排名选取指定范围的元素，包含端点
		Set<String> fruit_0_3 = j.zrangeByScore("fruit", "1", "3");
		assertNotNull(fruit_0_3);
		assertEquals(3, fruit_0_3.size());
		assertTrue(fruit_0_3.contains("apple"));
		assertTrue(fruit_0_3.contains("banana"));
		assertTrue(fruit_0_3.contains("orange"));

		// 不包含端点
		Set<String> fruit_2 = j.zrangeByScore("fruit", "(1", "(3");
		assertNotNull(fruit_2);
		assertEquals(1, fruit_2.size());
		assertTrue(fruit_2.contains("banana"));

		// 无穷 inf
		Set<String> fruit_2_inf = j.zrangeByScore("fruit", "(1", "+inf");
		assertNotNull(fruit_2_inf);
		assertEquals(3, fruit_2_inf.size());
		assertTrue(fruit_2_inf.contains("banana"));
		assertTrue(fruit_2_inf.contains("orange"));
		assertTrue(fruit_2_inf.contains("watermelon"));

		// 带着 score 一起返回
		Set<Tuple> fruit_2_inf_tuple = j.zrangeByScoreWithScores("fruit", "(1", "+inf");
		assertTrue(fruit_2_inf_tuple.contains(new Tuple("banana", 2D)));
		assertTrue(fruit_2_inf_tuple.contains(new Tuple("orange", 3D)));
		assertTrue(fruit_2_inf_tuple.contains(new Tuple("watermelon", 4D)));
	}

	@Test
	public void incrScore() {
		HashMap<String, Double> map = new HashMap<>();
		map.put("apple", 1D);
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		long effected = j.zadd("fruit", map);
		assertEquals(4L, effected);

		Double appleScore = j.zincrby("fruit", 1.1, "apple");	// 返回新的 score
		assertTrue(appleScore - 2.1 < 0.01);

		appleScore = j.zincrby("fruit", -1.0, "apple");	// 返回新的 score
		assertTrue(appleScore - 1.1 < 0.01);
	}

	@Test
	public void getCardOfZSet() {
		HashMap<String, Double> map = new HashMap<>();
		map.put("apple", 1D);
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		long effected = j.zadd("fruit", map);
		assertEquals(4L, effected);

		long card = j.zcard("fruit");
		assertEquals(4L, card);

		// 按照排名范围删除元素
		effected = j.zremrangeByRank("fruit", 3L, 3L);
		assertEquals(1L, effected);

		// 根据 score 范围统计元素个数
		long count = j.zcount("fruit", "(1", "4");
		assertEquals(2L, count);

		// 按照 score 范围删除元素
		effected = j.zremrangeByScore("fruit", "1", "1");
		assertEquals(1L, effected);

		// 返回元素排名，从小到大，最小是 0
		long rank_banana = j.zrank("fruit", "banana");
		assertEquals(0L, rank_banana);

		// 返回元素排名，从大到小，最小是 0
		rank_banana = j.zrevrank("fruit", "banana");
		assertEquals(1L, rank_banana);
	}

	@Test
	public void zSetInter() {
		HashMap<String, Double> map = new HashMap<>();
		map.put("apple", 1D);
		map.put("banana", 2D);
		map.put("orange", 3D);
		map.put("watermelon", 4D);
		long effected = j.zadd("fruit_1", map);
		assertEquals(4L, effected);

		map.remove("apple");
		map.remove("orange");
		effected = j.zadd("fruit_2", map);
		assertEquals(2L, effected);

		// fruit_1: "apple", 1D | "banana", 2D | "orange", 3D | "watermelon", 4D
		// fruit_2: "banana", 2D | "watermelon", 4D

		// 两个有序集合求交集，并且交集后的 score 为两者之和，还可以设置成两者间的最小值、最大值
		ZParams zParams = new ZParams();
		zParams.aggregate(ZParams.Aggregate.SUM);
		long cardOfNewZSet = j.zinterstore("fruit_3", zParams, "fruit_1", "fruit_2");
		assertEquals(2L, cardOfNewZSet);

		double score_banana = j.zscore("fruit_3", "banana");
		assertTrue(score_banana - 4D < 0.01);
	}
}
