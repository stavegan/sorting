package ru.stavegan.sorting.reader

import ru.stavegan.sorting.util.toPathOption
import zio._
import zio.test.{Assertion, _}

import java.io.IOException
import scala.collection.JavaConverters._

object FileReaderSpec extends ZIOSpecDefault {

  private val EmptyResourceFilename: String = "/input0.txt"
  private val FilledResourceFilename: String = "/input1.txt"

  private val layer: URLayer[Scope, FileReader & Scope] =
    FileReader.live ++ ZLayer.service[Scope]

  private val expectedIterable: Iterable[BigIntOrString] =
    Seq(
      Left(380),
      Right("two-hundred-fifty"),
      Left(140),
      Left(394),
      Left(237),
      Right("three-hundred-fourteen"),
      Left(196),
      Left(202),
      Right("three-hundred-six"),
      Right("fourteen"),
      Right("three-hundred-eighty-seven"),
      Left(67),
      Left(301),
      Right("sixty-four"),
      Right("twenty-one"),
      Right("one-hundred-ninety-eight"),
      Left(361),
      Right("seventy-six"),
      Left(311),
      Right("three-hundred-four"),
      Right("two-hundred-sixty-five"),
      Right("two-hundred-forty-three"),
      Right("two-hundred-sixty-one"),
      Right("sixty-six"),
      Left(388),
      Left(342),
      Left(23),
      Left(182),
      Right("thirteen"),
      Right("nineteen"),
      Left(116),
      Right("three-hundred-seventy-one"),
      Left(99),
      Right("sixty-one"),
      Left(48),
      Left(51),
      Left(157),
      Left(146),
      Left(4),
      Right("eighty-one"),
      Left(62),
      Left(192),
      Right("fifty-nine"),
      Right("one-hundred-forty-seven"),
      Right("eighty-nine"),
      Right("two-hundred-thirty-eight"),
      Left(195),
      Right("forty-three"),
      Left(236),
      Left(389)
    )

  private def actualIterable(filename: String): ZIO[FileReader & Scope, IOException, Iterable[BigIntOrString]] =
    for {
      resource <- ZIO
        .attempt(getClass.getResource(filename).getPath)
        .catchAll(cause => ZIO.fail(new IOException("Could not access the resource \"filename\".", cause)))
      pathOpt <- toPathOption(resource)
      path <- pathOpt match {
        case Some(path) => ZIO.succeed(path)
        case None => ZIO.fail(new IOException("Could not access the resource \"filename\"."))
      }
      reader <- ZIO.service[FileReader.Service]
      stream <- reader.inputStream(path)
      iterable = stream.iterator.asScala.toSeq
    } yield iterable

  override def spec: Spec[TestEnvironment with Scope, IOException] =
    suite("FileReader")(
      test("inputStream (empty)") {
        assertZIO(actualIterable(EmptyResourceFilename))(Assertion.isEmpty)
      },
      test("inputStream (filled)") {
        assertZIO(actualIterable(FilledResourceFilename))(Assertion.equalTo(expectedIterable))
      }
    ).provideSomeLayer(layer)
}
