package ru.stavegan.sorting

import ru.stavegan.sorting.reader.FileReader
import ru.stavegan.sorting.sorter.Sorter
import ru.stavegan.sorting.util._
import ru.stavegan.sorting.writer.FileWriter
import zio._

import java.io.IOException

object Main extends ZIOApp {

  override type Environment = Sorter

  implicit override def environmentTag: EnvironmentTag[Environment] = EnvironmentTag.tagFromTagMacro

  override def bootstrap: URLayer[ZIOAppArgs & Scope, Environment] =
    ZLayer.make[Environment](
      FileReader.live,
      FileWriter.live,
      Sorter.live
    )

  override def run: ZIO[Environment & ZIOAppArgs & Scope, IOException, ExitCode] =
    for {
      args <- ZIO.service[ZIOAppArgs].map(_.getArgs)
      paths <- ZIO.foreach(args.dropRight(1))(toPathOption)
      directory <- args match {
        case _ :+ directory => ZIO.succeed(directory)
        case _ => ZIO.fail(new IOException("Output directory did not specified."))
      }
      _ <- Sorter.sort(paths.flatten, directory)
      _ <- ZIO.sleep(1.second)
    } yield ExitCode.success
}
