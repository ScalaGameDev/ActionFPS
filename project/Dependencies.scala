import sbt._

object Dependencies {
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.4"
  val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.13.5"
  val sourcecode: ModuleID = "com.lihaoyi" %% "sourcecode" % "0.1.4"
  val async: ModuleID = "org.scala-lang.modules" %% "scala-async" % "0.9.7"
  val commonsCsv: ModuleID = "org.apache.commons" % "commons-csv" % "1.5"
  val commonsIO: ModuleID = "commons-io" % "commons-io" % "2.6"
  val kantanCsv: ModuleID = "com.nrinaudo" %% "kantan.csv-generic" % "0.3.1"
  val jsoup: ModuleID = "org.jsoup" % "jsoup" % "1.11.2"
  val hazelcastClient: ModuleID = "com.hazelcast" % "hazelcast-client" % "3.9.2"
  val fluentHc: ModuleID = "org.apache.httpcomponents" % "fluent-hc" % "4.5.4"
  val httpClientCache
    : ModuleID = "org.apache.httpcomponents" % "httpclient-cache" % "4.5.4"
  val playIteratees
    : ModuleID = "com.typesafe.play" %% "play-iteratees" % "2.6.1"
  val playIterateesStreams
    : ModuleID = "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"

  val scalatestPlus
    : ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"
  val alpakkaFile
    : ModuleID = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "0.15.1"
  val seleniumJava
    : ModuleID = "org.seleniumhq.selenium" % "selenium-java" % "3.8.1"
  val seleniumHtmlUnit
    : ModuleID = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"

  val geoipApi: ModuleID = "com.maxmind.geoip" % "geoip-api" % "1.3.1"

  val akkaActor: ModuleID = akka("actor")
  val akkaAgent: ModuleID = akka("agent")
  val akkaslf: ModuleID = akka("slf4j")
  val akkaTestkit: ModuleID = akka("testkit")

  private def akka(stuff: String) =
    "com.typesafe.akka" %% s"akka-$stuff" % "2.5.8"

  val akkaStreamTestkit: ModuleID = akka("stream-testkit")

  val jodaTime: ModuleID = "joda-time" % "joda-time" % "2.9.9"
  val jodaConvert: ModuleID = "org.joda" % "joda-convert" % "1.9.2"
  val fastparse: ModuleID = "com.lihaoyi" %% "fastparse" % "1.0.0"

  val raptureJsonPlay
    : ModuleID = "com.propensive" %% "rapture-json-play" % "2.0.0-M9" exclude ("com.typesafe.play", "play-json")

  val playJson: ModuleID = "com.typesafe.play" %% "play-json" % "2.6.8"

  val jwtPlayJson: ModuleID = "com.pauldijou" %% "jwt-play-json" % "0.14.1"
  val jwtPlay: ModuleID = "com.pauldijou" %% "jwt-play" % "0.14.1"

  val macwireMacros
    : ModuleID = "com.softwaremill.macwire" %% "macros" % "2.3.0"
  val macwireUtil: ModuleID = "com.softwaremill.macwire" %% "util" % "2.3.0"
  val macwireProxy: ModuleID = "com.softwaremill.macwire" %% "proxy" % "2.3.0"

  val jaxbApi: ModuleID = "javax.xml.bind" % "jaxb-api" % "2.3.0"


}
