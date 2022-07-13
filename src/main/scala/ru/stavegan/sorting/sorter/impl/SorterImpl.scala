package ru.stavegan.sorting.sorter.impl

import cats.Monad
import ru.stavegan.sorting.reader.{FileReader, _}
import ru.stavegan.sorting.sorter.Sorter
import ru.stavegan.sorting.util._
import ru.stavegan.sorting.writer.FileWriter
import zio._
import zio.Console.printLine
import zio.interop.catz.core._

import java.io._
import java.nio.file._
import java.util.UUID
import scala.collection.BufferedIterator

final case class SorterImpl(reader: FileReader.Service, writer: FileWriter.Service) extends Sorter.Service {

  import SorterImpl._

  override def sort(
      inputFilesPaths: Chunk[Path],
      directory: String
    )(implicit trace: Trace): ZIO[Scope, IOException, Unit] =
    for {
      sortedFilePath <- toPath(directory, more = s"$SortedLabel.txt")
      sortedTempFilePath <- toPath(directory, more = s"$SortedLabel-${UUID.randomUUID}.txt")
      _ <- write(inputFilesPaths, sortedTempFilePath)
      count <- reader.count(inputFilesPaths)
      savedFilePath <- loop(sortedTempFilePath, sortedFilePath, count)
      _ <- write(savedFilePath, sortedFilePath).when(savedFilePath.equals(sortedTempFilePath))
      _ <- ZIO
        .attempt(Files.delete(sortedTempFilePath))
        .catchAll(_ => printLine(s"Could not delete the file \"$sortedTempFilePath\"."))
    } yield ()

  private def write(
      inputFilesPaths: Chunk[Path],
      outputFilePath: Path
    )(implicit trace: Trace): ZIO[Scope, IOException, Unit] =
    ZIO.scoped {
      for {
        stream <- reader.inputStream(inputFilesPaths)
        outputStream <- writer.outputStream(outputFilePath)
        _ <- ZIO.attemptBlockingIO {
          stream.forEach { value =>
            outputStream.write(value.toByteArrayWithLineBreak)
          }
        }
      } yield ()
    }

  private def write(inputFilePath: Path, outputFilePath: Path)(implicit trace: Trace): ZIO[Scope, IOException, Unit] =
    write(Chunk.apply(inputFilePath), outputFilePath)

  private def loop(
      inputFilePath: Path,
      outputFilePath: Path,
      count: Long,
      step: Long = 1L
    )(implicit trace: Trace): ZIO[Scope, IOException, Path] =
    Monad[ScopedEIO].tailRecM((inputFilePath, outputFilePath, step)) {
      case (inputFilePath, outputFilePath, step) if count > step =>
        ZIO
          .scoped {
            writer
              .outputStream(outputFilePath)
              .flatMap(sortTo(inputFilePath, _, count, step))
          }
          .as {
            Left(
              (
                outputFilePath,
                inputFilePath,
                2L * step
              )
            )
          }
      case _ => ZIO.succeed(Right(inputFilePath))
    }

  private def sortTo(
      inputFilePath: Path,
      outputStream: OutputStream,
      count: Long,
      step: Long
    )(implicit trace: Trace): ZIO[Scope, IOException, Unit] =
    ZIO.loopDiscard(initial = 0L)(_ < count, _ + 2L * step) { index =>
      for {
        iterator <- reader.iterator(inputFilePath)
        (first, second) = iterator.duplicate
        headIndex = index.toInt
        middleIndex = (index + step).toInt
        lastIndex = (index + 2L * step).toInt
        firstSliced = first.slice(headIndex, middleIndex).buffered
        secondSliced = second.slice(middleIndex, lastIndex).buffered
        _ <- sortTo(firstSliced, secondSliced, outputStream)
      } yield ()
    }

  private def sortTo(
      first: BufferedIterator[BigIntOrString],
      second: BufferedIterator[BigIntOrString],
      outputStream: OutputStream
    )(implicit trace: Trace): IO[IOException, Unit] =
    Monad[EIO].tailRecM((first, second)) {
      case (first, second) if first.headOption.exists(_.lessOrEqual(second.headOption)) =>
        for {
          head <- ZIO.attemptBlockingIO(first.head)
          _ <- ZIO.attemptBlockingIO(outputStream.write(head.toByteArrayWithLineBreak))
          _ <- ZIO.succeed(first.nextOption())
        } yield Left((first, second))
      case (first, second)
          if first.headOption.exists(!_.lessOrEqual(second.headOption)) ||
            first.headOption.isEmpty && second.headOption.nonEmpty =>
        ZIO.succeed(Left((second, first)))
      case _ => ZIO.succeed(Right(()))
    }
}

object SorterImpl {

  private val SortedLabel: String = "sorted"

  private type EIO[+A] = ZIO[Any, IOException, A]
  private type ScopedEIO[+A] = ZIO[Scope, IOException, A]
}
