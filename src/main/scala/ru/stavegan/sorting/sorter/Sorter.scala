package ru.stavegan.sorting.sorter

import ru.stavegan.sorting.reader.FileReader
import ru.stavegan.sorting.sorter.impl.SorterImpl
import ru.stavegan.sorting.writer.FileWriter
import zio._

import java.io.IOException
import java.nio.file.Path

object Sorter {

  trait Service {

    /**
     * Sorts the input stream and saves it to the directory passed as an argument.
     * The output file has name {directory}/sorted.txt.
     * Uses temporary file to store the intermediate sorting results named {directory}/sorted-{uuid}.txt.
     *
     * @param inputFilesPaths
     *        paths to consumed files.
     *
     * @param directory
     *        path to directory to save sorting result.
     *
     * @throws IOException
     *         if could not print warnings to the console or could not save the resulting file.
     */
    def sort(inputFilesPaths: Chunk[Path], directory: String)(implicit trace: Trace): ZIO[Scope, IOException, Unit]
  }

  def sort(
      inputFilesPaths: Chunk[Path],
      directory: String
    )(implicit trace: Trace): ZIO[Sorter & Scope, IOException, Unit] =
    ZIO.service[Sorter].flatMap(_.sort(inputFilesPaths, directory))

  val any: URLayer[Sorter, Sorter] =
    ZLayer.service[Sorter]

  val live: URLayer[FileReader & FileWriter, Sorter] =
    ZLayer.fromFunction(SorterImpl(_, _))
}
