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

package sourcerer.dev

import java.util.regex.Pattern

/**
 * A regular expression to match a small list of specific packages deemed to be unhelpful to
 * display in fully qualified types in error messages.
 *
 *
 * Note: This should never be applied to messages themselves.
 */
private val COMMON_PACKAGE_PATTERN = Pattern.compile(
    "(?:^|[^.a-z_])" // What we want to match on but not capture.

    + "((?:" // Start a group with a non-capturing or part

    + "java[.]lang"
    + "|java[.]util"
    + "|javax[.]inject"
    + "|dagger"
    + "|com[.]google[.]common[.]base"
    + "|com[.]google[.]common[.]collect"
    + ")[.])" // Always end with a literal .

    + "[A-Z]"
) // What we want to match on but not capture.

/**
 * A method to strip out common packages and a few rare type prefixes from types' string
 * representation before being used in error messages.
 *
 *
 * This type assumes a String value that is a valid fully qualified (and possibly
 * parameterized) type, and should NOT be used with arbitrary text, especially prose error
 * messages.
 *
 *
 * TODO(cgruber): Tighten these to take type representations (mirrors and elements) to avoid
 * accidental mis-use by running errors through this method.
 */
fun stripCommonTypePrefixes(type: String): String {
    // Do regex magic to remove common packages we care to shorten.
    val matcher = COMMON_PACKAGE_PATTERN.matcher(type)
    val result = StringBuilder()
    var index = 0
    while (matcher.find()) {
        result.append(type.subSequence(index, matcher.start(1)))
        index = matcher.end(1) // Skip the matched pattern content.
    }
    result.append(type.subSequence(index, type.length))
    return result.toString()
}
