package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.io.Source

case class LogRecord(file: File, errorCount: Int, warnCount: Int, infoCount: Int)

class LogsAnalysis extends Actor with ActorLogging {
  override def receive: Receive = {
    case file: File =>
      val res = Source.fromFile(file).getLines.toList.foldLeft(file, 0, 0, 0) { (count, elem) =>
        elem match {
          case line: String if line.contains("[ERROR]") => (count._1, count._2 + 1, count._3, count._4)
          case line: String if line.contains("[WARN]") => (count._1, count._2, count._3 + 1, count._4)
          case line: String if line.contains("[INFO]") => (count._1, count._2, count._3, count._4 + 1)
          case _ => (count._1, count._2, count._3, count._4)
        }
      }
      log.info(LogRecord(res._1, res._2, res._3, res._4).toString)
  }
}

class Logs extends Actor with ActorLogging {
  override def receive: Receive = {
    case directoryName: String =>
      val dir = new File(directoryName)
      val logFiles = dir.listFiles.toList
      for (file <- logFiles) {
        val logsAnalysis = context.actorOf(Props[LogsAnalysis])
        logsAnalysis ! file
      }
  }
}

object LogFileAnalysisUsingActorModel extends App {
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources"
  val system = ActorSystem("LogFileAnalysisSystem")
  val logFiles = system.actorOf(Props[Logs])
  logFiles ! directoryName
}
