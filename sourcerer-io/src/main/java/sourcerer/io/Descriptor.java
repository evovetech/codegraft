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

import java.util.zip.ZipEntry;

public final class Descriptor {
    private final String dir;
    private final String fileExtension;

    public Descriptor(String dir, String fileExtension) {
        this.dir = dir;
        this.fileExtension = fileExtension;
    }

    public String dir() {
        return dir;
    }

    public String fileExtension() {
        return fileExtension;
    }

    public File parseFile(ZipEntry entry) {
        final String path = entry.getName();
        if (path.startsWith(dir)) {
            String other = path.substring(dir.length(), path.length());
            if (other.length() >= 2 && other.startsWith("/")) {
                return file(other.substring(1, other.length()));
            }
        }
        return null;
    }

    public File file(String fileName) {
        return new File(fileName);
    }

    public final class File {
        private final String fileName;

        private File(String fileName) {
            this.fileName = fileName;
        }

        public String fileName() {
            return fileName;
        }

        public String extFileName() {
            return fileName + fileExtension;
        }

        public String extFilePath() {
            return dir + "/" + extFileName();
        }

        public boolean matches(ZipEntry entry) {
            return extFilePath().equals(entry.getName());
        }
    }
}
