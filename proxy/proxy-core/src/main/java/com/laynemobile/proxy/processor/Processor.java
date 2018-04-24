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

package com.laynemobile.proxy.processor;

public interface Processor<T, R> {

    R call(T t);

    // Marker interface
    interface Extension<T, R> {}

    interface Parent<T, R> extends Extension<T, R>, ErrorHandlerProcessor<T, R> {}

    interface Checker<T, R> extends Extension<T, R> {
        void check(T t) throws Exception;
    }

    interface Modifier<T, R> extends Extension<T, R> {
        R modify(T t, R r);
    }

    interface Interceptor<T, R> extends Extension<T, R> {
        R intercept(Chain<T, R> chain);

        interface Chain<T, R> {
            T value();

            R proceed(T t);
        }
    }
}
