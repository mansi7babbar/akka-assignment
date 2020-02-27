package com.knoldus.logfile_analysis_using_akka

import java.io.File

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorKilledException, ActorLogging, ActorRef, ActorSystem, AllForOneStrategy, DeathPactException, Props, SupervisorStrategy}
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
  val maxNrOfRetries: Int = 10
  val withinTimeRange: FiniteDuration = 1 minute
}

case class LogRecord(file: File, errorCount: Int, warnCount: Int, infoCount: Int)

case class LogRecordSum(errorSum: Int, warnSum: Int, infoSum: Int)

case class LogRecordAvg(errorAvg: Int, warnAvg: Int, infoAvg: Int)

class LogFileAnalysisForAvg extends Actor {
  override def receive: Receive = {
    case listOfLogRecord: List[LogRecord] =>
      val logRecordSum = listOfLogRecord.foldLeft(LogRecordSum(0, 0, 0)) { (avg, log) =>
        log match {
          case log: LogRecord => LogRecordSum(avg.errorSum + log.errorCount, avg.warnSum + log.warnCount, avg.infoSum + log.infoCount)
        }
      }

      val logRecordAvg = LogRecordAvg(logRecordSum.errorSum / listOfLogRecord.length, logRecordSum.warnSum / listOfLogRecord.length, logRecordSum.infoSum / listOfLogRecord.length)
      Future.successful(logRecordAvg).pipeTo(sender)
  }
}

class LogFileAnalysis extends Actor {
  override def receive: Receive = {
    case logFile: File =>
      val logRecord = Source.fromFile(logFile).getLines.toList.foldLeft(LogRecord(logFile, 0, 0, 0)) { (log, line) =>
        line match {
          case line: String if line.contains("[ERROR]") => LogRecord(log.file, log.errorCount + 1, log.warnCount, log.infoCount)
          case line: String if line.contains("[WARN]") => LogRecord(log.file, log.errorCount, log.warnCount + 1, log.infoCount)
          case line: String if line.contains("[INFO]") => LogRecord(log.file, log.errorCount, log.warnCount, log.infoCount + 1)
          case _ => LogRecord(log.file, log.errorCount, log.warnCount, log.infoCount)
        }
      }

      Future.successful(logRecord).pipeTo(sender)
  }
}

class Logs extends Actor with ActorLogging {
  implicit val timeout: Timeout = Timeout(5 seconds)

  val mySupervisorStrategy: SupervisorStrategy = {
    AllForOneStrategy(maxNrOfRetries = Constants.maxNrOfRetries, withinTimeRange = Constants.withinTimeRange) {
      case _: ActorKilledException => Restart
      case _: DeathPactException => Restart
      case _: Exception => Escalate
    }
  }

  val master: ActorRef = context.actorOf(RoundRobinPool(Constants.roundRobinParameter, supervisorStrategy = mySupervisorStrategy).props(Props[LogFileAnalysis]).withDispatcher("fixed-thread-pool"), "master")
  val logFileAnalysisForAvg: ActorRef = context.actorOf(Props[LogFileAnalysisForAvg])

  override def receive: Receive = {
    case directoryName: String =>
      val listOfLogFiles = new File(directoryName).listFiles.toList
      val listOfLogRecord = Future.sequence(listOfLogFiles.map(logFile => (master ? logFile).mapTo[LogRecord]))
      val logRecordAvg = listOfLogRecord.map(listOfLogRecord => (logFileAnalysisForAvg ? listOfLogRecord).mapTo[LogRecordAvg]).flatten

      listOfLogRecord.map(listOfLogRecord => listOfLogRecord.map(logRecord =>
        log.info(s"File = ${logRecord.file}, Total errors = ${logRecord.errorCount}, Total warnings = ${logRecord.warnCount}, Total info = ${logRecord.infoCount}")))
      logRecordAvg.map(logRecordAvg =>
        log.info(s"Avg errors per file = ${logRecordAvg.errorAvg}, Avg warnings per file = ${logRecordAvg.warnAvg}, Avg info per file = ${logRecordAvg.infoAvg}"))
  }
}

object LogFileAnalysisSystem extends App {
  implicit val timeout: Timeout = Timeout(5 seconds)
  val system = ActorSystem("LogFileAnalysisSystem")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("fixed-thread-pool")
  val logs = system.actorOf(Props[Logs], "controller")
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res"
  system.scheduler.scheduleWithFixedDelay(5 * 1000 milliseconds, 5 * 60 * 1000 milliseconds, logs, directoryName)
}
