//package com.knoldus
//
//import java.io.File
//
//import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
//import akka.util.Timeout
//import akka.pattern._
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//import scala.io.Source
//
//case class LogRecord(file: File, errorCount: Int, warnCount: Int, infoCount: Int)
//
//case class LogRecordAvg(file: File, errorAvg: Int, warnAvg: Int, infoAvg: Int)
//
//class LogsAnalysis extends Actor with ActorLogging {
//  override def receive: Receive = {
//    case file: File =>
//      val res = Source.fromFile(file).getLines.toList.foldLeft(file, 0, 0, 0) { (count, elem) =>
//        if (elem.contains("[ERROR]")) {
//          (count._1, count._2 + 1, count._3, count._4)
//        }
//        else if (elem.contains("[WARN]")) {
//          (count._1, count._2, count._3 + 1, count._4)
//        }
//        else if (elem.contains("[INFO]")) {
//          (count._1, count._2, count._3, count._4 + 1)
//        }
//        else {
//          (count._1, count._2, count._3, count._4)
//        }
//      }
//      LogRecord(res._1, res._2, res._3, res._4)
//      LogRecordAvg(res._1, res._2 / Source.fromFile(file).getLines.toList.length, res._3 / Source.fromFile(file).getLines.toList.length, res._4 / Source.fromFile(file).getLines.toList.length)
//  }
//}
//
//class Logs extends Actor with ActorLogging {
//  implicit val timeout: Timeout = Timeout(10 seconds)
//
//  override def receive: Receive = {
//    case directoryName: String =>
//      val dir = new File(directoryName)
//      val logFiles = dir.listFiles.toList
//
//      @scala.annotation.tailrec
//      def getListOfLogRecords(fileIndex: Int, listOfLogRecords: List[Future[LogRecord]]): List[Future[LogRecord]] = {
//        if (fileIndex < logFiles.length - 1) {
//          val logsAnalysis = context.actorOf(Props[LogsAnalysis])
//          val logRecord = logsAnalysis ? logFiles(fileIndex)
//          getListOfLogRecords(fileIndex + 1, listOfLogRecords :+ logRecord.mapTo[LogRecord])
//        }
//        else {
//          listOfLogRecords
//        }
//      }
//
//      getListOfLogRecords(0, List[Future[LogRecord]]())
//  }
//}
//
//object Testing extends App {
//  implicit val timeout: Timeout = Timeout(30 seconds)
//  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources"
//  val system = ActorSystem("LogFileAnalysisSystem")
//  val logFiles = system.actorOf(Props[Logs])
//  val res = logFiles ? directoryName
//  val finalRes = res.mapTo[List[LogRecord]]
//  Thread.sleep(40000)
//  println(finalRes)
//}
