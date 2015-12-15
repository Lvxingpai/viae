package com.lvxingpai.viae.event

import akka.actor.Actor
import akka.event.Logging
import com.lvxingpai.viae.Message

/**
 * 和订单相关, 发送短信的handler
 *
 * Created by zephyre on 12/11/15.
 */
class OrderSmsHandler extends Actor {
  private val logger = Logging(context.system, this)

  def receive = {
    case Message(id, createTime, CreateOrderEvent(orderId), _, _) =>
      logger.info(s"OrderSmsHandler: orderId=$orderId, msgId=$id")
    case message @ _ =>
      logger.info(s"OrderSmsHandler: unknown message: $message")
  }
}
