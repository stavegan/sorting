package ru.stavegan.sorting.writer.impl

import ru.stavegan.sorting.writer.FileWriter
import zio._

import java.io._
import java.nio.file.Path

final class FileWriterImpl extends FileWriter.Service {

  override def outputStream(path: Path)(implicit trace: Trace): ZIO[Scope, IOException, OutputStream] =
    ZIO.fromAutoCloseable {
      ZIO
        .attempt {
          new BufferedOutputStream(
            new FileOutputStream(
              path.toFile
            )
          )
        }
        .catchAll(cause => ZIO.fail(new IOException(s"Could not access directory \"$path\".", cause)))
    }
}
