package com.knoldus.logfile_analysis_using_dispatcher

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.MessageDispatcher
import akka.pattern._
import akka.routing.RoundRobinPool
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

object Constants {
  val roundRobinParameter: Int = 5
}

case class LogRecord(file: File, errorCount: Int, warnCount: Int, infoCount: Int)

class LogFileAnalysis extends Actor with ActorLogging {
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
      Future {
        LogRecord(res._1, res._2, res._3, res._4)
      }.pipeTo(sender)
  }
}

class Logs extends Actor with ActorLogging {
  implicit val timeout: Timeout = Timeout(1 seconds)

  override def receive: Receive = {
    case directoryName: String =>
      val dir = new File(directoryName)
      val logFiles = dir.listFiles.toList
      val master = context.actorOf(RoundRobinPool(Constants.roundRobinParameter).props(Props[LogFileAnalysis]).withDispatcher("fixed-thread-pool"), "master")

      @scala.annotation.tailrec
      def getListOfLogRecords(fileIndex: Int, listOfLogRecords: List[Future[LogRecord]]): Future[List[LogRecord]] = {
        if (fileIndex < logFiles.length) {
          val logRecord = master ? logFiles(fileIndex)
          getListOfLogRecords(fileIndex + 1, listOfLogRecords :+ logRecord.mapTo[LogRecord])
        }
        else {
          Future.sequence(listOfLogRecords)
        }
      }

      getListOfLogRecords(0, List[Future[LogRecord]]()).pipeTo(sender())
  }
}

object LogFileAnalysisUsingDispatcher extends App {
  implicit val timeout: Timeout = Timeout(1 seconds)
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res"
  val system = ActorSystem("LogFileAnalysisSystem")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("fixed-thread-pool")
  val logFiles = system.actorOf(Props[Logs])
  val res = logFiles ? directoryName
  val finalRes = res.mapTo[List[Future[LogRecord]]]
}
