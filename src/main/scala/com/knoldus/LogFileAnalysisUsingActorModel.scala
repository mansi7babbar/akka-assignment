package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.io.Source

class LogAnalysis extends Actor with ActorLogging {

  override def receive: Receive = {
    case LogFiles(files) =>
      log.info(LogAnalysis.getErrorsWarningsInfoPerFile(files).toString)
      log.info(LogAnalysis.getAvgErrors(files).toString)
      log.info(LogAnalysis.getAvgWarnings(files).toString)
      log.info(LogAnalysis.getAvgInfo(files).toString)
  }
}

case class LogFiles(logFiles: List[File])

object LogAnalysis {
  def getErrorsWarningsInfoPerFile(logFiles: List[File]): List[(File, Int, Int, Int)] = {
    logFiles.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")),
      Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")),
      Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]"))))
  }

  def getAvgErrors(logFiles: List[File]): List[(File, Int)] = {
    logFiles.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")) / Source.fromFile(file).getLines.toList.length))
  }

  def getAvgWarnings(logFiles: List[File]): List[(File, Int)] = {
    logFiles.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")) / Source.fromFile(file).getLines.toList.length))
  }

  def getAvgInfo(logFiles: List[File]): List[(File, Int)] = {
    logFiles.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]")) / Source.fromFile(file).getLines.toList.length))
  }
}

object ListOfFiles {
  def getListOfFiles(directoryName: String): List[File] = {
    val dir = new File(directoryName)
    val files = dir.listFiles.toList
    files
  }
}

object LogFileAnalysisUsingActorModel extends App {
  implicit val timeout: Timeout = Timeout(20 second)
  val logFiles = ListOfFiles.getListOfFiles("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources")
  val system = ActorSystem("LogFileAnalysisSystem")
  val logAnalysis = system.actorOf(Props[LogAnalysis])
  logAnalysis ! LogFiles(logFiles)
}
