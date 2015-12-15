package com.lvxingpai.viae.consumer

import akka.event.Logging
import com.lvxingpai.viae.Message
import com.redis.RedisClientPool

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.pickling.Defaults._
import scala.pickling.json._

/**
 * Created by zephyre on 12/10/15.
 */
class PatternedScheduler(pool: RedisClientPool, attachedQueue: String) extends Consumer(pool, attachedQueue) {

  private val log = Logging(context.system, this)

  /**
   * 处理接收到的消息
   *
   */
  override protected def process(queue: String, message: Message): Boolean = {
    log.info(s"Delayed message peeked")

    message match {
      case Message(id, createTime, subMessage: Message, Some(routingKey), Some(ttl)) =>
        // 是否需要等待
        val diff = createTime + ttl - System.currentTimeMillis
        if (diff > 0) {
          // 放回原处
          log.info(s"Push message $id back, wake up in ${diff / 1000} seconds")
          pool withClient (client => {
            client.rpush(attachedQueue, message.pickle.value)
          })
          // 建立计划任务
          context.system.scheduler.scheduleOnce(diff.milliseconds, self, "fetch")
        } else {
          log.info(s"Message $id triggered: $subMessage")
          // 触发消息
          pool withClient (client => {
            val triggered = Message(subMessage.id, System.currentTimeMillis(), subMessage.body, subMessage.routingKey,
              subMessage.ttl)
            client.lpush(routingKey, triggered.pickle.value)
          })
          context.system.scheduler.scheduleOnce(0.milliseconds, self, "fetch")
        }
      case _ =>
        log.warning("Unknown message")
        context.system.scheduler.scheduleOnce(0.milliseconds, self, "fetch")
    }
    // actor在执行process之后, 不再需要fetch
    false
  }
}
