import sbtassembly.MergeStrategy

name := "xdapp-sdk-java"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.hprose" % "hprose-java" % "2.0.38",
  "io.netty" % "netty-all" % "5.0.0.Alpha2",
  "org.scodec" % "scodec-core_2.12" % "1.11.4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.3.0-alpha4"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x                                   => MergeStrategy.first
}