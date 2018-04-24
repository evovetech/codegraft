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

package com.laynemobile.proxy;

import java.lang.reflect.Method;

public interface NamedMethodHandler extends MethodHandler {
    String name();

    class Builder implements com.laynemobile.proxy.Builder<NamedMethodHandler> {
        private String name;
        private MethodHandler methodHandler;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMethodHandler(MethodHandler methodHandler) {
            this.methodHandler = methodHandler;
            return this;
        }

        @Override public NamedMethodHandler build() {
            return new Impl(this);
        }

        private static final class Impl implements NamedMethodHandler {
            private final String name;
            private final MethodHandler methodHandler;

            private Impl(Builder builder) {
                this.name = builder.name;
                this.methodHandler = builder.methodHandler;
            }

            @Override public String name() {
                return name;
            }

            @Override
            public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
                return methodHandler.handle(proxy, method, args, result);
            }
        }
    }
}
