package com.knoldus.logfile_analysis_using_scheduler

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

class LogFileAnalysis extends Actor {
  override def receive: Receive = {
    case file: File =>
      val logRecord = Source.fromFile(file).getLines.toList.foldLeft(LogRecord(file, 0, 0, 0)) { (log, line) =>
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

  override def receive: Receive = {
    case directoryName: String =>
      val logFiles = new File(directoryName).listFiles.toList
      val logRecords = Future.sequence(logFiles.map(file => (master ? file).mapTo[LogRecord]))
      logRecords.map(logRecord => log.info(s"$logRecord"))
  }
}

object LogFileAnalysisSystem extends App {
  implicit val timeout: Timeout = Timeout(5 seconds)
  val system = ActorSystem("LogFileAnalysisSystem")
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("fixed-thread-pool")
  val logs = system.actorOf(Props[Logs], "controller")
  val directoryName = "/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res"
  logs ! directoryName
  system.scheduler.schedule(0 milliseconds, 1 * 60 * 1000 milliseconds, logs, directoryName)
}
