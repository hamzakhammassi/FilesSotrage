import org.apache.commons.io.FileUtils

import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import scala.sys.process._

class ProcessorGen {

  def run(config: FilesConfig): Unit = {

    val types = List(
      ".heic",
      ".HEIC",
      ".mov",
      ".MOV",
      ".jpg",
      ".jpeg",
      ".JPG",
      ".JPEG",
      ".dng",
      ".DNG"
    )

    val workingDir = new File(config.workingDirectories)

    println("****************************************************************")
    println("*********************** SOURCE INFO *****************************")
    println("****************************************************************")

    types.foreach { l =>
      println(
        s"number of $l files : ${workingDir.list.count(_.endsWith(l))}"
      )
    }

    val files =
      getListOfFiles(config.workingDirectories)

    println(s"Detected ${files.size} files")

    files.foreach { file =>

      val folderName =
        s"${file._2.getYear}-${"%02d".format(file._2.getMonthValue)}"

      val targetDir =
        new File(
          s"${config.workingDirectories}/$folderName"
        )

      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }

      println(
        s"Moving ${file._1.getName} -> $folderName"
      )

      FileUtils.moveFileToDirectory(
        file._1,
        targetDir,
        true
      )
    }

    println("****************************************************************")
    println("************************ RESULT ********************************")
    println("****************************************************************")

    workingDir.listFiles.toList
      .filter(_.isDirectory)
      .foreach { p =>

        println(s"folder : $p")

        println(
          types
            .map(t =>
              s"$t : ${new File(p.toString).list.count(_.endsWith(t))}"
            )
            .mkString("\n")
        )
      }
  }

  /**
   * Liste des fichiers avec la vraie colonne Windows "Date"
   */
  private def getListOfFiles(
                              dir: String
                            ): Map[File, LocalDate] = {

    val d = new File(dir)

    if (d.exists && d.isDirectory) {

      d.listFiles
        .filter(_.isFile)
        .flatMap { file =>

          extractWindowsExplorerDate(file) match {

            case Some(date) =>
              Some(file -> date)

            case None =>
              println(
                s"Impossible de lire date : ${file.getName}"
              )
              None
          }
        }
        .toMap

    } else {

      Map.empty[File, LocalDate]
    }
  }

  /**
   * Lit EXACTEMENT la colonne "Date" de Windows Explorer
   */
  private def extractWindowsExplorerDate(
                                          file: File
                                        ): Option[LocalDate] = {

    try {

      val fullPath =
        file.getAbsolutePath.replace("'", "''")

      val psCommand =
        s"""
           |$$item = Get-Item '$fullPath'
           |$$date = $$item.ExtendedProperty('System.ItemDate')
           |Write-Output $$date
           |""".stripMargin

      val output =
        Seq(
          "powershell",
          "-NoProfile",
          "-Command",
          psCommand
        ).!!.trim

      if (output.nonEmpty) {

        // Exemple :
        // Sunday, October 27, 2024 3:14:22 PM

        val zonedDate =
          java.time.ZonedDateTime.parse(
            output,
            java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
          )

        Some(zonedDate.toLocalDate)

      } else {

        None
      }

    } catch {

      case e: Exception =>

        println(
          s"Impossible de lire date : ${file.getName}"
        )

        println(e.getMessage)

        None
    }
  }
}