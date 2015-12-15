package com.lvxingpai.viae

import com.google.inject.name.Names
import com.google.inject.{ Guice, Key }
import com.lvxingpai.configuration.Configuration
import com.lvxingpai.etcd.EtcdStoreModule
import com.lvxingpai.viae.inject.RedisModule

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zephyre on 12/11/15.
 */
object Global {
  val (conf, injector) = {
    val basicInjector = Guice.createInjector(new EtcdStoreModule(Configuration.load()))
    val conf = basicInjector.getInstance(Key.get(classOf[Configuration], Names.named("etcd")))
    val injector = Guice.createInjector(new EtcdStoreModule(conf), new RedisModule(conf))
    (conf, injector)
  }
}
