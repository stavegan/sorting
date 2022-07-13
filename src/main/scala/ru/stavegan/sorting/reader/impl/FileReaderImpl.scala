package ru.stavegan.sorting.reader.impl

import ru.stavegan.sorting.reader.{FileReader, _}
import zio._
import zio.Console.printLine

import java.io._
import java.nio.file.Path
import java.util.stream.Stream
import scala.collection.JavaConverters._
import scala.util.matching.Regex

final class FileReaderImpl extends FileReader.Service {

  import FileReaderImpl._

  override def inputStream(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Stream[BigIntOrString]] =
    ZIO.fromAutoCloseable {
      ZIO
        .foldLeft(paths)(InputStream.nullInputStream) { (stream, path) =>
          path.toInputStream
            .map(stream ++ _)
            .catchAll { _ =>
              printLine(s"Failed to access file \"$path\". The file will not be proceeded.") &>
                ZIO.succeed(stream)
            }
        }
        .flatMap(_.toBufferedReader.toStream)
    }

  override def count(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Long] =
    ZIO.scoped(inputStream(paths).map(_.count()))

  override def iterator(paths: Chunk[Path])(implicit trace: Trace): ZIO[Scope, IOException, Iterator[BigIntOrString]] =
    ZIO.scoped(inputStream(paths).map(_.iterator().asScala))
}

object FileReaderImpl {

  private[this] val NoWhiteSpaceRegex: Regex = "^\\S+".r

  implicit private class RichPath(val value: Path) extends AnyVal {

    def toInputStream: IO[IOException, InputStream] =
      ZIO
        .attempt {
          new BufferedInputStream(
            new FileInputStream(
              value.toFile
            )
          )
        }
        .catchAll(cause => ZIO.fail(new IOException(s"Could not access file \"$value\".", cause)))
  }

  implicit private class RichInputStream(val value: InputStream) extends AnyVal {

    def ++(that: InputStream): InputStream =
      new SequenceInputStream(value, that)

    def toBufferedReader: BufferedReader =
      new BufferedReader(new InputStreamReader(value))
  }

  implicit private class RichBufferedReader(val value: BufferedReader) extends AnyVal {

    def toStream: IO[IOException, Stream[BigIntOrString]] =
      ZIO.attemptBlockingIO {
        value.lines
          .map[BigIntOrString](toBigIntOrString)
          .filter {
            case Left(_) => true
            case Right(line) => line.nonEmpty && NoWhiteSpaceRegex.matches(line)
          }
      }
  }
}
