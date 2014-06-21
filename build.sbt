import AssemblyKeys._

assemblySettings

jarName in assembly := "instacq.jar"

name := "Instacq"

version := "0.0.3"

scalaVersion := "2.10.3"

mainClass in assembly := Some("edu.nyu.dlts.instag.Crawl")

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.3",
  "com.typesafe" % "config" % "1.2.1",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.fasterxml" % "jackson-xml-databind" % "0.6.2",
  "com.typesafe.slick" %% "slick" % "2.0.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scalatest" % "scalatest_2.10" % "2.2.0" % "test"
)

