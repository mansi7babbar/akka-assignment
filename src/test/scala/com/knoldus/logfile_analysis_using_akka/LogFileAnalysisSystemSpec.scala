package com.knoldus.logfile_analysis_using_akka

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import scala.concurrent.duration._

class LogFileAnalysisSystemSpec extends TestKit(ActorSystem("LogFileAnalysisSystem")) with ImplicitSender with AnyFlatSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Actor Logs should" should "send response of error count, average errors per file, warning count, average warnings per file, info count, average info per file" in
    within(30 seconds) {
      val message = "./src/main/resources/res"
      val testLogs = system.actorOf(Props[Logs])
      testLogs ! message
      val expectedMessage = List(LogRecord(new File("./src/main/resources/res/stdout-log5"), 0, 1454, 256),
        LogRecord(new File("./src/main/resources/res/stdout-log9"), 87, 1259, 272),
        LogRecord(new File("./src/main/resources/res/stdout-log2"), 0, 1446, 245),
        LogRecord(new File("./src/main/resources/res/stdout-log7"), 0, 1446, 245),
        LogRecord(new File("./src/main/resources/res/stdout-log8"), 0, 1446, 245),
        LogRecord(new File("./src/main/resources/res/stdout-log11"), 89, 1263, 272),
        LogRecord(new File("./src/main/resources/res/stdout-log1"), 91, 1268, 278),
        LogRecord(new File("./src/main/resources/res/stdout-log10"), 91, 1268, 278),
        LogRecord(new File("./src/main/resources/res/stdout-log4"), 0, 1454, 256),
        LogRecord(new File("./src/main/resources/res/stdout-log12"), 91, 1268, 278))
      expectMsg(expectedMessage)
    }
}
