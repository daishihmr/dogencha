package util
import java.io.ByteArrayInputStream
import java.io.InputStream

import com.google.common.io.InputSupplier

object Guavas {

  implicit def byteArrayToInputSupplier(bytes: Array[Byte]) = new InputSupplier[InputStream]() {
    def getInput = new ByteArrayInputStream(bytes)
  }

  implicit def inputStreamToInputSupplier(in: InputStream) = new InputSupplier[InputStream]() {
    def getInput = in
  }

}