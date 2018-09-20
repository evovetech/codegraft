/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
