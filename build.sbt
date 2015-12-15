name := """viae"""

organization := "com.lvxingpai"

version := "1.0"

scalaVersion := "2.11.4"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.lvxingpai" %% "etcd-store-guice" % "0.1.1-SNAPSHOT",
  "net.debasishg" %% "redisclient" % "3.0",
  "org.scala-lang.modules" %% "scala-pickling" % "0.10.2-SNAPSHOT",
  "com.twitter" %% "finagle-thriftmux" % "6.30.0",
  "com.twitter" %% "scrooge-core" % "4.2.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

resolvers += Resolver.sonatypeRepo("snapshots")

com.twitter.scrooge.ScroogeSBT.newSettings

enablePlugins(JavaAppPackaging)

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

