package com.knoldus.logfile_analysis

import java.io.File

import scala.io.Source

case class LogRecord(file: File, errorCount: Int, warnCount: Int, infoCount: Int)

case class LogRecordSum(errorSum: Int, warnSum: Int, infoSum: Int)

case class LogRecordAvg(errorAvg: Int, warnAvg: Int, infoAvg: Int)

/**
 * Created to return object of case class LogRecordAvg which contains average of errors per file, average of warnings per file, average of info per file
 */

class LogFileAnalysisForAvg extends LogFileAnalysis {
  def getLogRecordAvg: LogRecordAvg = {
    val listOfLogRecord = getLogRecord
    val logRecordSum = listOfLogRecord.foldLeft(LogRecordSum(0, 0, 0)) { (avg, log) =>
      log match {
        case log: LogRecord => LogRecordSum(avg.errorSum + log.errorCount, avg.warnSum + log.warnCount, avg.infoSum + log.infoCount)
      }
    }

    val logRecordAvg = LogRecordAvg(logRecordSum.errorSum / listOfLogRecord.length, logRecordSum.warnSum / listOfLogRecord.length, logRecordSum.infoSum / listOfLogRecord.length)
    logRecordAvg
  }
}

/**
 * Created to return list of objects of case class LogRecord which contains count of errors per file, count of warnings per file, count of info per file
 */

class LogFileAnalysis extends LogFiles {
  def getLogRecord: List[LogRecord] = {
    val listOfLogFiles = getListOfLogFiles
    val logRecord = listOfLogFiles.map(logFile => Source.fromFile(logFile).getLines.toList.foldLeft(LogRecord(logFile, 0, 0, 0)) { (log, line) =>
      line match {
        case line: String if line.contains("[ERROR]") => LogRecord(log.file, log.errorCount + 1, log.warnCount, log.infoCount)
        case line: String if line.contains("[WARN]") => LogRecord(log.file, log.errorCount, log.warnCount + 1, log.infoCount)
        case line: String if line.contains("[INFO]") => LogRecord(log.file, log.errorCount, log.warnCount, log.infoCount + 1)
        case _ => LogRecord(log.file, log.errorCount, log.warnCount, log.infoCount)
      }
    })
    logRecord
  }
}

/**
 * Created to return list of log files in directory provided
 */

class LogFiles {
  val directoryName = "./src/main/resources/res"

  def getListOfLogFiles: List[File] = {
    val listOfLogFiles = new File(directoryName).listFiles.toList
    listOfLogFiles
  }
}

object LogFiles extends App {
  val logFileAnalysis = new LogFileAnalysis
  val logRecord = logFileAnalysis.getLogRecord
  val logFileAnalysisForAvg = new LogFileAnalysisForAvg
  val logRecordAvg = logFileAnalysisForAvg.getLogRecordAvg
}
