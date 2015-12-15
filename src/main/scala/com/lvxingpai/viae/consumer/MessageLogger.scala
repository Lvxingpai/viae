package com.lvxingpai.viae.consumer

import akka.actor.Actor
import akka.event.Logging
import com.lvxingpai.viae.Message

/**
 * Created by zephyre on 12/10/15.
 */
class MessageLogger extends Actor {
  private val log = Logging(context.system, this)

  def receive = {
    case m: Message =>
      log.info(s"Message ${m.id} received: ${m.body}")
    case s: String =>
      log.info(s"String message received: $s")
  }
}
