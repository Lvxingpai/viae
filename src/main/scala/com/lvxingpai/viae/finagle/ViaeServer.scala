package com.lvxingpai.viae.finagle

import java.net.InetSocketAddress

import com.lvxingpai.viae.Global
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol.Factory

/**
 * Created by zephyre on 12/12/15.
 */
class ViaeServer {
  private val service = new Viae$FinagleService(new ViaeHandler, new Factory())
  private val serverBuilder = {
    val conf = Global.conf

    val port = conf getInt "port" getOrElse 9000
    val maxConcur = conf getInt "maxConcur" getOrElse 1000
    val name = conf getString "serverName" getOrElse "viae"
    ServerBuilder()
      .bindTo(new InetSocketAddress(port))
      .codec(ThriftServerFramedCodec())
      .name(name)
      .maxConcurrentRequests(maxConcur)
  }

  def start(): Unit = {
    serverBuilder.build(service)
  }
}
