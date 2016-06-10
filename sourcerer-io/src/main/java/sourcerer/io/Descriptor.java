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

package sourcerer.io;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public abstract class Descriptor {
    private final String dir;
    private final String ext;

    protected Descriptor(String dir, String ext) {
        this.dir = dir;
        this.ext = ext;
    }

    public final String dir() {
        return dir;
    }

    public final String fileExtension() {
        return ext;
    }

    public abstract File file(String fileName);

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Descriptor)) return false;
        Descriptor that = (Descriptor) o;
        return Objects.equal(dir, that.dir) &&
                Objects.equal(ext, that.ext);
    }

    @Override public int hashCode() {
        return Objects.hashCode(dir, ext);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dir", dir)
                .add("fileExtension", ext)
                .toString();
    }

    public abstract class File {
        private final String fileName;

        protected File(File file) {
            this(file.fileName);
        }

        protected File(String fileName) {
            this.fileName = fileName;
        }

        public final String fileName() {
            return fileName;
        }

        public final String extFileName() {
            return fileName + ext;
        }

        public final String extFilePath() {
            return dir + "/" + extFileName();
        }

        public abstract Descriptor descriptor();

        public abstract Writer newWriter();

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof File)) return false;
            File that = (File) o;
            return Objects.equal(descriptor(), that.descriptor())
                    && Objects.equal(fileName, that.fileName);
        }

        @Override public int hashCode() {
            return Objects.hashCode(fileName);
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fileName", fileName)
                    .toString();
        }
    }
}
