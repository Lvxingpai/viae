package com.lvxingpai.viae.consumer

import akka.actor.Actor
import akka.event.Logging
import com.lvxingpai.viae.Message
import com.redis.RedisClientPool

import scala.pickling.Defaults._
import scala.pickling.PicklingException
import scala.pickling.json._

/**
 * Created by zephyre on 12/10/15.
 */
abstract class Consumer(protected val pool: RedisClientPool, private val attachedQueues: String*) extends Actor {
  protected val logger = Logging(context.system, this)

  /**
   * 处理接收到的消息
   *
   */
  protected def process(queue: String, message: Message): Boolean

  def receive = {
    case "start" =>
      logger info s"Consumer started on ${attachedQueues mkString ":"}"
      self ! "fetch"
    case "fetch" =>
      pool withClient (client => {
        for {
          (queue, raw) <- client brpop (120, attachedQueues.headOption getOrElse "",
            (if (attachedQueues.nonEmpty) attachedQueues.tail else Seq()): _*)
        } yield {
          try {
            val message = JSONPickle(raw).unpickle[Message]
            val result = process(queue, message)
            if (result)
              self ! "fetch"
          } catch {
            case e: PicklingException =>
              logger.error(e, s"Unknown message: $raw")
              self ! "fetch"
            case e =>
              logger.error(e, "Unknown error")
              self ! "fetch"
          }
        }
      })
  }
}
