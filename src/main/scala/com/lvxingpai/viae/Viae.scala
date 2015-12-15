package com.lvxingpai.viae

import akka.actor.{ActorSystem, Props}
import com.lvxingpai.viae.consumer.{EventDispatcher, PatternedScheduler}
import com.lvxingpai.viae.finagle.ViaeServer
import com.redis.RedisClientPool

/**
 * Created by zephyre on 12/9/15.
 */
object Viae extends App {

  args.headOption getOrElse "" match {
    case "server" => startServer()
    case "worker" => startWorker()
  }

//  def test(): Unit = {
//    import scala.pickling.Defaults._
//    import scala.pickling.json._
//
//    val service = ClientBuilder().hosts("localhost:9000").codec(ThriftClientFramedCodec()).hostConnectionLimit(1000).build()
//    val client = new FinagledClient(service, new Factory())
//    val raw = Message(UUID.randomUUID().toString, System.currentTimeMillis(), CreateOrderEvent(1234)).pickle.value
//    val ret = Await.result(client.postMessage("order:created", raw, Some(Delay.Min1)))
//    println(ret)
//  }

  def startServer(): Unit = {
    (new ViaeServer).start()
  }

  def startWorker(): Unit = {
    val pool = Global.injector.getInstance(classOf[RedisClientPool])

    val system = ActorSystem("system")

    // 每个pattern都启动一个actor来监听
    Global.conf getConfigSeq "schedule.patterns" getOrElse Seq() foreach (pattern => {
      for {
        tag <- pattern getString "tag"
      } yield {
        val queueName = s"schedule:$tag"
        val schedulerActor = system.actorOf(Props(classOf[PatternedScheduler], pool, queueName))
        schedulerActor ! "start"
      }
    })

    // 启动事件监听者的actor
    for {
      entry <- Global.conf getConfigSeq "eventHandlers" getOrElse Seq()
      path <- entry getString "path"
      handler <- entry getString "handler"
    } yield {
      // 创建actor
      val cls = Class forName handler
      system.actorOf(Props(cls), path)
    }

    // 监听这些事件
    val events = (Global.conf getConfigSeq "eventHandlers" getOrElse Seq() map (_ getString "event") filter
      (_.nonEmpty) map (v => s"${v.get}")).distinct
    val eventDispatcher = system.actorOf(Props(classOf[EventDispatcher], pool, events))
    eventDispatcher ! "start"
  }
}

