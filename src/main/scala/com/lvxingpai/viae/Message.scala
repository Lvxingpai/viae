package com.lvxingpai.viae

/**
 * 定义了消息队列中, 一条消息应该是怎样的结构
 *
 * Created by zephyre on 12/10/15.
 */
case class Message(id: String, createTime: Long, body: Any, routingKey: Option[String] = None, ttl: Option[Long] = None)

