package com.lvxingpai.viae.consumer

import com.lvxingpai.viae.{ Global, Message }
import com.redis.RedisClientPool

/**
 * 对事件进行分发
 *
 * Created by zephyre on 12/10/15.
 */
class EventDispatcher(pool: RedisClientPool, attachedQueues: String*) extends Consumer(pool, attachedQueues: _*) {
  // 构造事件的走向
  private val eventsMap = {
    val map = scala.collection.mutable.Map[String, Seq[String]]()
    Global.conf getConfigSeq "eventHandlers" getOrElse Seq() foreach (conf => {
      for {
        event <- conf getString "event"
        path <- conf getString "path"
      } yield {
        val handlerList = if (map contains event) {
          map(event)
        } else {
          val l = Seq[String]()
          map += event -> l
          l
        }
        map(event) = handlerList :+ path
      }
    })
    map
  }

  /**
   * 处理接收到的消息
   *
   * @param message
   */
  override protected def process(queue: String, message: Message): Boolean = {
    // 获得事件应该发送到哪些actor
    val targetActorPaths = eventsMap getOrElse (queue, Seq())

    message match {
      case Message(id, createTime, body, routingKey, ttl) =>
        targetActorPaths foreach (path => {
          val system = context.system
          val actor = system.actorSelection(system / path)
          actor ! message
        })
    }

    true
  }
}
