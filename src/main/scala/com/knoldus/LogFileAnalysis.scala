package com.knoldus

import java.io.File

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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

  def avgCountPerFile(countPerFile: Future[List[(File, Int)]]): Future[Int] = {
    countPerFile.map(files => files.foldLeft(0) { (avg, count) => avg + count._2 } / files.length)
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

  val avgErrorsPerFile = logAnalysis.avgCountPerFile(errorCount)

  val warningCount = logAnalysis.totalNoOfWarnings(logFiles)

  val avgWarningsPerFile = logAnalysis.avgCountPerFile(warningCount)

  val infoCount = logAnalysis.totalNoOfInfo(logFiles)

  val avgInfoPerFile = logAnalysis.avgCountPerFile(infoCount)

  Thread.sleep(5000)
  println(logFiles)
  println(errorCount)
  println(avgErrorsPerFile)
  println(warningCount)
  println(avgWarningsPerFile)
  println(infoCount)
  println(avgInfoPerFile)

}
