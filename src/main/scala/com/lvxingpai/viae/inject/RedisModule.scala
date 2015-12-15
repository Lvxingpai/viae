package com.lvxingpai.viae.inject

import com.google.inject.{ AbstractModule, Provider }
import com.lvxingpai.configuration.Configuration
import com.redis.{ RedisClient, RedisClientPool }

/**
 * Created by zephyre on 12/11/15.
 */
class RedisModule(config: Configuration) extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RedisClientPool]) toProvider new RedisProvider(config)
  }
}

class RedisProvider(config: Configuration) extends Provider[RedisClientPool] {
  lazy val get: RedisClientPool = {
    (for {
      redisConf <- config getConfig "services.redis"
      hostKey <- redisConf.subKeys.headOption
      conf <- config getConfig s"services.redis.$hostKey"
      host <- conf getString "host"
      port <- conf getInt "port"
      db <- conf getInt "redis.db" orElse Some(1)
    } yield {
      new RedisClientPool(host, port, database = db)
    }).orNull
  }
}
