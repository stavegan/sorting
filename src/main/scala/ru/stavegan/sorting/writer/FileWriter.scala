package ru.stavegan.sorting.writer

import ru.stavegan.sorting.writer.impl.FileWriterImpl
import zio._

import java.io.{IOException, OutputStream}
import java.nio.file.Path

object FileWriter {

  trait Service {

    /**
     * Creates output stream from the specified path.
     *
     * @param path
     *        path to produced file.
     *
     * @return the resulting {@code OutputStream}.
     *
     * @throws IOException
     *         if could access the path.
     */
    def outputStream(path: Path)(implicit trace: Trace): ZIO[Scope, IOException, OutputStream]
  }

  def outputStream(path: Path)(implicit trace: Trace): ZIO[FileWriter & Scope, IOException, OutputStream] =
    ZIO.service[FileWriter].flatMap(_.outputStream(path))

  val any: URLayer[FileWriter, FileWriter] =
    ZLayer.service[FileWriter]

  val live: ULayer[FileWriter] =
    ZLayer.succeed(new FileWriterImpl)
}
