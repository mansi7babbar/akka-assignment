package com.knoldus.logfile_analysis

import java.io.File

import org.scalatest._

class LogFilesSpec extends FlatSpec with BeforeAndAfterAll {

  var logFilesAnalysis: LogFileAnalysis = new LogFileAnalysis
  var logFilesAnalysisForAvg: LogFileAnalysisForAvg = new LogFileAnalysisForAvg

  override def beforeAll(): Unit = {
    logFilesAnalysis = new LogFileAnalysis
    logFilesAnalysisForAvg = new LogFileAnalysisForAvg
  }

  override def afterAll(): Unit = {
    if (logFilesAnalysis != null & logFilesAnalysisForAvg != null) {
      logFilesAnalysis = null
      logFilesAnalysisForAvg = null
    }
  }

  "getLogRecord method" should "return list of objects of case class LogRecord" in {
    val actualResult = logFilesAnalysis.getLogRecord
    val expectedResult = List(LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log5"), 0, 1454, 256), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log9"), 87, 1259, 272), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log2"), 0, 1446, 245), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log7"), 0, 1446, 245), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log8"), 0, 1446, 245), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log11"), 89, 1263, 272), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log1"), 91, 1268, 278), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log10"), 91, 1268, 278), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log4"), 0, 1454, 256), LogRecord(new File("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources/res/stdout-log12"), 91, 1268, 278))
    assert(expectedResult == actualResult)
  }

  "getLogRecordAvg method" should "return object of case class LogRecordAvg" in {
    val actualResult = logFilesAnalysisForAvg.getLogRecordAvg
    val expectedResult = LogRecordAvg(44, 1357, 262)
    assert(expectedResult == actualResult)
  }
}
