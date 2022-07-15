package ru.stavegan.sorting.sorter

import ru.stavegan.sorting.reader.{FileReader, _}
import ru.stavegan.sorting.util._
import ru.stavegan.sorting.writer.FileWriter
import zio._
import zio.test._

import java.io.IOException
import java.nio.file.Files
import scala.collection.JavaConverters._

object SorterSpec extends ZIOSpecDefault {

  private val InputResourceFilenames: Chunk[String] =
    Chunk(
      "/input0.txt",
      "/input1.txt",
      "/input2.txt",
      "/input3.txt",
      "/input4.txt"
    )

  // specify your own output directory
  private val OutputDirectory: String = "/home/kapavkin/code/sorting/src/test/resources"
  private val SortedLabel: String = "sorted.txt"

  private val SortedResourceFilename: String = "/output.txt"

  private val layer: URLayer[Scope, Sorter & FileReader & Scope] =
    ZLayer.makeSome[Scope, Sorter & FileReader & Scope](
      FileReader.live,
      FileWriter.live,
      Sorter.live
    )

  private def actualIterable: ZIO[FileReader & Sorter & Scope, IOException, Iterable[BigIntOrString]] =
    for {
      paths <- ZIO.foreach(InputResourceFilenames) { filename =>
        ZIO
          .attempt(getClass.getResource(filename).getPath)
          .flatMap(toPathOption)
          .catchAll(cause => ZIO.fail(new IOException("Could not access the resource \"filename\".", cause)))
      }
      _ <- Sorter.sort(paths.flatten, OutputDirectory)
      path <- toPath(OutputDirectory, SortedLabel)
      stream <- FileReader.inputStream(Chunk.apply(path))
      iterable = stream.iterator.asScala.toSeq
    } yield iterable

  private def expectedIterable: ZIO[FileReader & Scope, IOException, Iterable[BigIntOrString]] =
    for {
      resource <- ZIO
        .attempt(getClass.getResource(SortedResourceFilename).getPath)
        .catchAll(cause => ZIO.fail(new IOException("Could not access the resource \"filename\".", cause)))
      pathOpt <- toPathOption(resource)
      path <- pathOpt match {
        case Some(path) => ZIO.succeed(path)
        case None => ZIO.fail(new IOException("Could not access the resource \"filename\"."))
      }
      stream <- FileReader.inputStream(Chunk.apply(path))
      iterable = stream.iterator.asScala.toSeq
    } yield iterable

  override def spec: Spec[TestEnvironment with Scope, IOException] =
    suite("Sorter")(
      test("sort") {
        for {
          actual <- actualIterable
          expected <- expectedIterable
          sortedPath <- toPath(OutputDirectory, SortedLabel)
          _ <- ZIO.attemptBlockingIO(Files.delete(sortedPath))
        } yield assert(actual)(Assertion.equalTo(expected))
      }
    ).provideSomeLayer(layer)
}
