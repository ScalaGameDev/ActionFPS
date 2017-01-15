package com.actionfps

import java.time.ZonedDateTime

import scala.util.Try
import fastparse.all._

/**
  * Created by William on 09/12/2015.
  *
  * Notify clients of an '!inter' message on a server by a registered user.
  */
package object inter {

  case class InterCall(time: ZonedDateTime, server: String, ip: String, nickname: String)

  case class InterMessage(ip: String, nickname: String) {
    def toCall(time: ZonedDateTime, server: String) = InterCall(
      time = time,
      server = server,
      ip = ip,
      nickname = nickname
    )
  }


  object InterMessage {

    private val ip = {
      val part = CharIn('1' to '9') ~ CharIn('0' to '9').rep
      part ~ "." ~ part ~ "." ~ part ~ "." ~ part
    }

    private val nickname = CharsWhile(_ != ' ')

    private val matcher = "[" ~ ip.! ~ "]" ~ " " ~ nickname.! ~ " says: '!inter'"
    private val mex = matcher.map(Function.tupled(InterMessage.apply))

    def unapply(input: String): Option[InterMessage] = {
      val m = mex.parse(input)
      PartialFunction.condOpt(m) {
        case Parsed.Success(r, _)=> r
      }
    }
  }

}
