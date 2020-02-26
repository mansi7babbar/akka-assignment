package com.knoldus.logfile_analysis

import java.io.File

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class LogFileAnalysis {
  def totalNoOfErrors(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")))))
  }

  def totalNoOfWarnings(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")))))
  }

  def totalNoOfInfo(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]")))))
  }
}

object LogFileAnalysis extends App {
  def getListOfFiles(directoryName: String): Future[List[File]] = Future {
    val dir = new File(directoryName)
    val files = dir.listFiles.toList
    files
  }

  val logAnalysis = new LogFileAnalysis

  val logFiles = getListOfFiles("/home/knoldus/IdeaProjects/akka-assignment/src/main/resources")

  val errorCount = logAnalysis.totalNoOfErrors(logFiles)

  val warningCount = logAnalysis.totalNoOfWarnings(logFiles)

  val infoCount = logAnalysis.totalNoOfInfo(logFiles)
}
