package ru.stavegan.sorting

import ru.stavegan.sorting.util.LineBreakChar

import scala.util.Try

package object reader {

  type FileReader = FileReader.Service

  type BigIntOrString = Either[BigInt, String]

  def toBigIntOrString(line: String): BigIntOrString =
    Try(BigInt.apply(line)).toOption.toLeft(line)

  implicit class RichBigIntOrString(val value: BigIntOrString) extends AnyVal {

    override def toString: String = value match {
      case Left(number) => number.toString
      case Right(line) => line
    }

    def toByteArrayWithLineBreak: Array[Byte] = toString.appended(LineBreakChar).getBytes

    def lessOrEqual(that: Option[BigIntOrString]): Boolean =
      that.forall(value.compareTo(_) <= 0L)

    def compareTo(that: BigIntOrString): Int =
      (value, that) match {
        case (Left(leftNumber), Left(rightNumber)) => leftNumber.compareTo(rightNumber)
        case (Left(_), Right(_)) => -1
        case (Right(leftLine), Right(rightLine)) => leftLine.compareTo(rightLine)
        case (Right(_), Left(_)) => 1
      }
  }
}
