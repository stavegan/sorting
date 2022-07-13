package ru.stavegan.sorting.reader

import ru.stavegan.sorting.reader.impl.FileReaderImpl
import zio._

import java.io.IOException
import java.nio.file.Path
import java.util.stream.Stream

object FileReader {

  trait Service {

    /**
     * Creates input stream from the specified paths.
     *
     * @param paths
     *        paths to consumed files.
     *
     * @return the resulting {@code Stream}.
     *
     * @throws IOException
     *         if could not print warnings to the console.
     */
    def inputStream(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Stream[BigIntOrString]]

    def inputStream(path: Path)(implicit trace: Trace): ZIO[Scope, IOException, Stream[BigIntOrString]] =
      inputStream(Chunk.apply(path))

    /**
     * Calculates the count of lines from the specified paths.
     *
     * @param paths
     *        paths to consumed files.
     *
     * @return count of lines.
     *
     * @throws IOException
     *         if could not print warnings to the console.
     */
    def count(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Long]

    def count(path: Path)(implicit trace: Trace): ZIO[Scope, IOException, Long] =
      count(Chunk.apply(path))

    /**
     * Creates iterator from the specified paths.
     *
     * @param paths
     *        paths to consumed files.
     *
     * @return the resulting {@code Iterator}.
     *
     * @throws IOException
     *         if could not print warnings to the console.
     */
    def iterator(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Iterator[BigIntOrString]]

    def iterator(path: Path)(implicit trace: Trace): ZIO[Scope, IOException, Iterator[BigIntOrString]] =
      iterator(Chunk.apply(path))
  }

  def inputStream(
      paths: Chunk[Path]
    )(implicit trace: Trace): ZIO[FileReader & Scope, IOException, Stream[BigIntOrString]] =
    ZIO.service[FileReader].flatMap(_.inputStream(paths))

  def count(paths: Chunk[Path])(implicit trace: Trace): ZIO[FileReader & Scope, IOException, Long] =
    ZIO.service[FileReader].flatMap(_.count(paths))

  def iterator(
      paths: Chunk[Path]
    )(implicit trace: Trace): ZIO[FileReader & Scope, IOException, Iterator[BigIntOrString]] =
    ZIO.service[FileReader].flatMap(_.iterator(paths))

  val any: URLayer[FileReader, FileReader] =
    ZLayer.service[FileReader]

  val live: ULayer[FileReader] =
    ZLayer.succeed(new FileReaderImpl)
}
