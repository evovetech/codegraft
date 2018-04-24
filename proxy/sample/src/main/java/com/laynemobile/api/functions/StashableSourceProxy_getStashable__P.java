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

package com.laynemobile.api.functions;

import com.laynemobile.api.Stashable;
import com.laynemobile.api.StashableParams;
import com.laynemobile.api.functions.parent.AbstractStashableSourceProxy_getStashable__P;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func1;

@Generated
public class StashableSourceProxy_getStashable__P<T, P extends StashableParams<?>> extends AbstractStashableSourceProxy_getStashable__P<T, P> {
    public StashableSourceProxy_getStashable__P(Func1<P, Stashable<T>> getStashable) {
        super(getStashable);
    }
}
