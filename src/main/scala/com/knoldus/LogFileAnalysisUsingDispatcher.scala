package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.MessageDispatcher
import akka.routing.RoundRobinPool

import scala.io.Source

case class LogRecords(file: File, errorCount: Int, warnCount: Int, infoCount: Int)

class LogsAnalytics extends Actor with ActorLogging {
  override def receive: Receive = {
    case file: File =>
      val res = Source.fromFile(file).getLines.toList.foldLeft(file, 0, 0, 0) { (count, elem) =>
        if (elem.contains("[ERROR]")) {
          (count._1, count._2 + 1, count._3, count._4)
        }
        else if (elem.contains("[WARN]")) {
          (count._1, count._2, count._3 + 1, count._4)
        }
        else if (elem.contains("[INFO]")) {
          (count._1, count._2, count._3, count._4 + 1)
        }
        else {
          (count._1, count._2, count._3, count._4)
        }
      }
      log.info(LogRecords(res._1, res._2, res._3, res._4).toString)
  }
}

class Log extends Actor with ActorLogging {
  override def receive: Receive = {
    case directoryName: String =>
      val dir = new File(directoryName)
      val logFiles = dir.listFiles.toList
      for (file <- logFiles) {
        val props = RoundRobinPool(5).props(Props[LogsAnalytics])
        val logsAnalysis = context.actorOf(props.withDispatcher("fixed-thread-pool"))
        logsAnalysis ! file
      }
  }
}

object LogFileAnalysisUsingDispatcher extends App {
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources"
  val system = ActorSystem("LogFileAnalysisSystem")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("fixed-thread-pool")
  val logFiles = system.actorOf(Props[Log])
  logFiles ! directoryName
}
