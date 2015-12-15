package com.lvxingpai.viae.finagle

import java.util.UUID

import com.lvxingpai.viae.{ Global, Message}
import com.redis.RedisClientPool
import com.twitter.util.Future

import scala.pickling.Defaults._
import scala.pickling.json._

/**
 * Created by zephyre on 12/12/15.
 */
class ViaeHandler extends Viae.FutureIface {

  // Delay和对应的pattern的关系
  private val delayMap: Map[Delay, String] = {
    // 将延迟的数值和delayTag联系起来: Map(60 -> "1min", 3600 -> "60min") 这样的
    val delayValue2String = Map(Global.conf getConfigSeq "schedule.patterns" getOrElse Seq() map (conf => {
      for {
        tag <- conf getString "tag"
        delayValue <- conf getInt "delay"
      } yield {
        delayValue -> s"schedule:$tag"
      }
    }) filter (_.nonEmpty) map (_.get): _*)

    Map(Delay.list map (delay => {
      (delay, delayValue2String.getOrElse(delay.value, ""))
    }) filter (_._2.nonEmpty): _*)
  }

  override def ping(): Future[String] = Future("pong")

  override def postMessage(queue: String, rawMessage: String, delayOpt: Option[Delay]): Future[String] = Future {
    // Redis连接池
    val pool = Global.injector.getInstance(classOf[RedisClientPool])

    val inner: Message = rawMessage.unpickle[Message]

    val delay = delayOpt getOrElse Delay.Now
    if (delay == Delay.Now) {
      // 不需要延迟, 直接投递到目标队列
      pool withClient (client => {
        client.lpush(queue, inner.pickle.value)
      })
      inner.id
    } else {
      // 需要延迟
      val routingKey = {
        if (delayMap contains delay) delayMap(delay)
        else throw InvalidDelayException(Some(s"Delay $delay is invalid"))
      }
      val message = Message(UUID.randomUUID().toString, System.currentTimeMillis(), inner, Some(queue),
        Some(delay.value * 1000))
      pool withClient (client => {
        client.lpush(routingKey, message.pickle.value)
      })
      message.id
    }
  }
}
