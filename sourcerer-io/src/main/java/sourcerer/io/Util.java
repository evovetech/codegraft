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

import com.squareup.javapoet.TypeName;

import java.util.Locale;

final class Util {
    private Util() { throw new AssertionError("no instances"); }

    static void log(TypeName typeName) {
        log("typeName: %s, class: %s", typeName, typeName.getClass());
    }

    static void log(String msg) {
        System.out.println(msg);
    }

    static void log(String format, Object... args) {
        System.out.printf(Locale.US, format, args);
        System.out.println();
    }
}
