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

package sourcerer.inject.descriptor

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.TypeMirror

data
class Key(
    private val wrappedType: Equivalence.Wrapper<TypeMirror>,
    private val wrappedQualifier: Equivalence.Wrapper<AnnotationMirror>? = null
) {
    constructor(
        type: TypeMirror,
        qualifier: AnnotationMirror? = null
    ) : this(
        wrappedType = MoreTypes.equivalence().wrap(type),
        wrappedQualifier = qualifier?.run(AnnotationMirrors.equivalence()::wrap)
    )

    val type: TypeMirror
        get() = wrappedType.get()!!

    val qualifier: AnnotationMirror?
        get() = wrappedQualifier?.get()
}
