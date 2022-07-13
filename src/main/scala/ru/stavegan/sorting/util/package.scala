package ru.stavegan.sorting

import zio._
import zio.Console.printLine

import java.io.IOException
import java.nio.file.{Path, Paths}

package object util {

  val LineBreakChar: Char = '\n'

  def toPath(directory: String, more: String*): IO[IOException, Path] =
    ZIO
      .attempt(Paths.get(directory, more: _*))
      .catchAll(cause => ZIO.fail(new IOException(s"Could not access directory \"$directory\".", cause)))

  def toPathOption(filename: String): IO[IOException, Option[Path]] =
    ZIO
      .attempt(Paths.get(filename))
      .map(Some(_))
      .catchAll { _ =>
        val logEffect = printLine(s"Failed to access file \"$filename\". The file will not be proceeded.")
        logEffect &> ZIO.none
      }
}
