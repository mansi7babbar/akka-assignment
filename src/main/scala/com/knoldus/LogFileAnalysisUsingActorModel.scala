package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.io.Source

class LogAnalysis extends Actor with ActorLogging {

  override def receive: Receive = {
    case LogFiles(files) =>
      log.info(files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")),
        Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")),
        Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]")))).toString)
  }
}

case class LogFiles(logFiles: List[File])

object LogFileAnalysisUsingActorModel extends App {
  implicit val timeout: Timeout = Timeout(5 second)

  def getListOfFiles(directoryName: String): List[File] = {
    val dir = new File(directoryName)
    val files = dir.listFiles.toList
    files
  }

  val logFiles = getListOfFiles("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources")

  val system = ActorSystem("LogFileAnalysisSystem")
  val logAnalysis = system.actorOf(Props[LogAnalysis])
  logAnalysis ! LogFiles(logFiles)

}
