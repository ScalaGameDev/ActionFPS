package com.actionfps.reference

import java.io.Reader
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.util.Try

/**
  * Created by William on 05/12/2015.
  */

case class ServerRecord(region: String, hostname: String, port: Int, kind: String, password: Option[String]) {
  def address = s"""$hostname:$port"""

  def url: String = {
    val pwdBit = password.filter(_.nonEmpty).map { password => s"?password=$password" }
    s"${protocol}://${hostname}:${port}${pwdBit.getOrElse("")}"
  }

  def joinUrl: String = {
    s"/servers/?join=${hostname}:${port}"
  }

  def name: String = s"${hostname} ${port}"

  def protocol: String = if (kind == "ActionFPS") "actionfps" else "assaultcube"

  def connectAddress: String = s"""$protocol://$hostname:$port""" + password.map(pw => s"/?password=$pw").getOrElse("")
}

object ServerRecord {

  import kantan.csv.ops._
  import kantan.csv.generic._

  def parseRecords(input: Reader): List[ServerRecord] = {
    input
      .asCsvReader[ServerRecord](sep = ',', header = false)
      .toList
      .flatMap(_.toList)
      .filter(_.hostname.nonEmpty)
      .filter(_.kind.nonEmpty)
  }
}
