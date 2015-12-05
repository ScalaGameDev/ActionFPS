package services

import javax.inject._

import akka.agent.Agent
import lib.RecordsReader

import scala.concurrent.ExecutionContext

/**
  * Created by William on 05/12/2015.
  */
@Singleton
class RecordsService @Inject()(recordsReader: RecordsReader)
                              (implicit executionContext: ExecutionContext) {

  val clansAgt = Agent(recordsReader.clans)
  val usersAgt = Agent(recordsReader.users)
  def users = usersAgt.get()
  def clans = clansAgt.get()

  def updateSync(): Unit = {
    clansAgt.alter(recordsReader.clans)
    usersAgt.alter(recordsReader.users)
  }

}