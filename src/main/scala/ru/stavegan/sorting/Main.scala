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
      args <- ZIO.service[ZIOAppArgs]
      filenames = args.getArgs.dropRight(1)
      paths <- ZIO.foreach(filenames)(toPathOption)
      directory <- ZIO.service[ZIOAppArgs].flatMap {
        case args if args.getArgs.lastOption.nonEmpty => ZIO.succeed(args.getArgs.last)
        case _ => ZIO.fail(new IOException("Output directory did not specified."))
      }
      _ <- Sorter.sort(paths.flatten, directory)
      _ <- ZIO.sleep(1.second)
    } yield ExitCode.success
}
