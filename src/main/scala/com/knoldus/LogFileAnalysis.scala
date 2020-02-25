package com.knoldus

import java.io.File

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

class LogFileAnalysis {
  def totalNoOfErrors(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")))))
  }

  def avgNoOfErrors(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[ERROR]")) / Source.fromFile(file).getLines.toList.length)))
  }

  def totalNoOfWarnings(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")))))
  }

  def avgNoOfWarnings(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[WARN]")) / Source.fromFile(file).getLines.toList.length)))
  }

  def totalNoOfInfo(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]")))))
  }

  def avgNoOfInfo(logFiles: Future[List[File]]): Future[List[(File, Int)]] = {
    logFiles.map(files => files.map(file => (file, Source.fromFile(file).getLines.toList.count(line => line.contains("[INFO]")) / Source.fromFile(file).getLines.toList.length)))
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

  val avgErrorsPerFile = logAnalysis.avgNoOfErrors(logFiles)

  val warningCount = logAnalysis.totalNoOfWarnings(logFiles)

  val avgWarningsPerFile = logAnalysis.avgNoOfWarnings(logFiles)

  val infoCount = logAnalysis.totalNoOfInfo(logFiles)

  val avgInfoPerFile = logAnalysis.avgNoOfInfo(logFiles)
}
