/*
 * Copyright 2016 Layne Mobile, LLC
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

package sourcerer

import sourcerer.SourceWriter

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

final class Util {
    private Util() { throw new AssertionError("no instances") }

    static void read(InputStream is, Set<SourceWriter> sourceWriters) {
        final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))
        try {
            ZipEntry entry
            while ((entry = zis.nextEntry) != null) {
                for (SourceWriter sourceWriter : sourceWriters) {
                    if (sourceWriter.matches(entry)) {
                        sourceWriter.read(zis)
                        break
                    }
                }
            }
        } finally {
            zis.close()
        }
    }
}
