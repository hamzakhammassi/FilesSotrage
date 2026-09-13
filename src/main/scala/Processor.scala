import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Processor {

  def run(config: FilesConfig): Unit = {

    val types = List(".heic", ".MOV")
    val workingDir = new File(config.workingDirectories)

    println("**********************************************************************************************************")
    println("************************************ Source Directory info ***********************************************")
    println("**********************************************************************************************************")

    types.foreach { l =>
      println(s"number of $l files : ${workingDir.list.count(_.endsWith(l))}")
    }

    getListOfFiles(config.workingDirectories).foreach(file =>
        FileUtils.moveFileToDirectory(file._1, new File(s"${config.workingDirectories}/${file._2.getYear}-${file._2.getMonth}/"), true))

    println("**********************************************************************************************************")
    println("************************************ Result Directories info *********************************************")
    println("**********************************************************************************************************")

    workingDir.listFiles.toList.filter(_.isDirectory).foreach { p =>
      println(s"for this folder : $p")
      println(types.map(t => s"number of $t files : ${new File(p.toString).list.count(_.endsWith(t))}").mkString("\n"))
    }

    println("**********************************************************************************************************")
    println("************************************ ALL Directories info ************************************************")
    println("**********************************************************************************************************")

    println(s"The total count of moved files : ${recursiveListFiles(workingDir).toList.length}")
  }

  private def getListOfFiles(dir: String): Map[File, LocalDate] = {
    val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles
        .filter(_.isFile)
        .map(e => e -> Files.readAttributes(e.toPath, classOf[BasicFileAttributes]).creationTime().toMillis).toMap
        .map(e => (e._1, df.format(e._2))) // .map(e=> e -> df.format(e.lastModified))
        .map(e => (e._1, LocalDate.parse(e._2, format)))
    } else {
      Map[File, LocalDate]()
    }
  }

  private def recursiveListFiles(f: File): Array[File] = {
    val (dir, files) = f.listFiles.partition(_.isDirectory)
    files ++ dir.flatMap(recursiveListFiles)
  }

}