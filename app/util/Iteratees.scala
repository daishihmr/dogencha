package util
import java.io.Reader
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Promise

object Iteratees {
}

object Enumerators {

  def fromReader(input: Reader, chunkSize: Int = 1024) = {
    Enumerator.fromCallback(() => {
      val buffer = new Array[Char](chunkSize)
      val chunk = input.read(buffer) match {
        case -1 => None
        case read =>
          val in = new Array[Char](read)
          System.arraycopy(buffer, 0, in, 0, read)
          Some(new String(in))
      }
      Promise.pure(chunk)
    }, input.close)
  }

}

class ReaderPlus(reader: Reader) {
  def +(other: Reader): Reader = {

    null
  }
}