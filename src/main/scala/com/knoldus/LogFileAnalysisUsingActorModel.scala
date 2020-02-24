package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.io.Source

class TotalNoOfErrors extends Actor with ActorLogging {
  var errorCounter = 0

  override def receive: Receive = {
    case count: Int => errorCounter += count
  }

  val avgErrorsPerFile: Int = errorCounter / LogFileAnalysisUsingActorModel.logFiles.length
  log.info(s"Average errors per file are $avgErrorsPerFile")
}

class TotalNoOfWarnings extends Actor with ActorLogging {
  var warningCounter = 0

  override def receive: Receive = {
    case count: Int => warningCounter += count
  }

  val avgWarningsPerFile: Int = warningCounter / LogFileAnalysisUsingActorModel.logFiles.length
  log.info(s"Average warnings per file are $avgWarningsPerFile")
}

class TotalNoOfInfo extends Actor with ActorLogging {
  var infoCounter = 0

  override def receive: Receive = {
    case count: Int => infoCounter += count
  }

  val avgInfoPerFile: Int = infoCounter / LogFileAnalysisUsingActorModel.logFiles.length
  log.info(s"Average info per file are $avgInfoPerFile")
}

object LogFileAnalysisUsingActorModel extends App {
  implicit val timeout: Timeout = Timeout(5 second)

  def getListOfFiles(directoryName: String): List[File] = {
    val dir = new File(directoryName)
    val files = dir.listFiles.toList
    files
  }

  val logFiles = getListOfFiles("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources")

  val system = ActorSystem("LogFileAnalysisSystem")
  val totalNoOfErrors = system.actorOf(Props[TotalNoOfErrors])
  val totalNoOfWarnings = system.actorOf(Props[TotalNoOfWarnings])
  val totalNoOfInfo = system.actorOf(Props[TotalNoOfInfo])

  for (file <- logFiles) {
    totalNoOfErrors ! Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]"))
    totalNoOfWarnings ! Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]"))
    totalNoOfInfo ! Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]"))
  }

}
