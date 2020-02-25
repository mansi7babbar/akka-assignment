package com.knoldus

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.Source

case class LogRecord(file: File, errorCount: Int, errorAvg: Int, warnCount: Int, warnAvg: Int, infoCount: Int, infoAvg: Int)

class LogsAnalysis extends Actor with ActorLogging {
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
      log.info(LogRecord(res._1, res._2, res._2 / Source.fromFile(file).getLines.toList.length, res._3, res._3 / Source.fromFile(file).getLines.toList.length, res._4, res._4 / Source.fromFile(file).getLines.toList.length).toString)
      Future {
        LogRecord(res._1, res._2, res._2 / Source.fromFile(file).getLines.toList.length, res._3, res._3 / Source.fromFile(file).getLines.toList.length, res._4, res._4 / Source.fromFile(file).getLines.toList.length)
      }.pipeTo(sender)
  }
}

class Logs extends Actor with ActorLogging {
  implicit val timeout: Timeout = Timeout(1 seconds)

  override def receive: Receive = {
    case directoryName: String =>
      val dir = new File(directoryName)
      val logFiles = dir.listFiles.toList

      @scala.annotation.tailrec
      def getListOfLogRecords(fileIndex: Int, listOfLogRecords: List[Future[LogRecord]]): Future[List[LogRecord]] = {
        if (fileIndex < logFiles.length) {
          val logsAnalysis = context.actorOf(Props[LogsAnalysis])
          val logRecord = logsAnalysis ? logFiles(fileIndex)
          getListOfLogRecords(fileIndex + 1, listOfLogRecords :+ logRecord.mapTo[LogRecord])
        }
        else {
          Future.sequence(listOfLogRecords)
        }
      }

      getListOfLogRecords(0, List[Future[LogRecord]]()).pipeTo(sender())
  }
}

object LogFileAnalysisUsingActorModel extends App {
  implicit val timeout: Timeout = Timeout(1 seconds)
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res"
  val system = ActorSystem("LogFileAnalysisSystem")
  val logFiles = system.actorOf(Props[Logs])
  val res = logFiles ? directoryName
  val finalRes = res.mapTo[List[Future[LogRecord]]]
}
