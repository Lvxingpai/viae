namespace java com.lvxingpai.viae.finagle
#@namespace scala com.lvxingpai.viae.finagle

// 支持哪些delay
enum Delay {
  NOW = 0,
  SEC1 = 1,
  SEC10 = 10,
  MIN1 = 60,
  MIN5 = 300,
  MIN10 = 600,
  MIN20 = 1200,
  MIN30 = 1800,
  HOUR1 = 3600,
  HOUR2 = 7200,
  HOUR4 = 14400,
  HOUR8 = 28800,
  HOUR12 = 43200,
  DAY1 = 86400,
  DAY2 = 172800,
  DAY3 = 259200,
  DAY4 = 345600,
  DAY5 = 432000,
  DAY7 = 604800,
  DAY10 = 864000,
  DAY15 = 1296000,
  DAY30 = 2592000,
}

// 无效的延迟设置
exception InvalidDelayException {
  1:optional string message
}

// 无效的消息
exception InvalidMessageException {
  1:optional string message
}

//case class Message(id: String, createTime: Long, body: Any, routingKey: Option[String] = None, ttl: Option[Long] = None)
service Viae {
  string ping()

  // 投放一条消息
  string postMessage(1:string queue, 2:string message, 3:optional Delay delay)
}

