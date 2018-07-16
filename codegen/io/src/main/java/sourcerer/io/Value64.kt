/*
 * Copyright 2018 evove.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sourcerer.io

import com.google.common.base.Objects
import okio.ByteString
import sourcerer.io.Reader.Companion.parser
import java.io.IOException

class Value64
private constructor(
    private val value: String,
    private val base64: String = ByteString.encodeUtf8(value).base64()
) : Writeable {

    fun value(): String {
        return value
    }

    @Throws(NumberFormatException::class)
    fun intValue(): Int {
        return Integer.parseInt(value)
    }

    @Throws(IOException::class)
    override fun writeTo(writer: Writer) {
        writer.writeString(base64)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Value64) return false
        val value64 = other as Value64?
        return Objects.equal(value, value64!!.value)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(value)
    }

    companion object {
        private val PARSER = parser { reader ->
            val base64 = reader.readString()
            val value = ByteString.decodeBase64(base64)!!
                    .utf8()
            Value64(value, base64)
        }

        fun from(value: Int): Value64 {
            return Value64(Integer.toString(value))
        }

        fun from(value: String): Value64 {
            return Value64(value)
        }

        @Throws(IOException::class)
        fun read(reader: Reader): Value64? {
            return PARSER.parse(reader)
        }

        @Throws(IOException::class)
        fun readList(reader: Reader): List<Value64> {
            return reader.readList(PARSER)
        }
    }
}
