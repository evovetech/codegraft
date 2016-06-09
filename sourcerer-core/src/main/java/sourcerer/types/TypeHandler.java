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

package sourcerer.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeHandler<T> {
    final Class<? extends T> type;
    final Map<String, List<MethodHandler>> handlers;

    protected TypeHandler(Builder<T> builder) {
        this.type = builder.type;
        this.handlers = Collections.unmodifiableMap(builder.handlers);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeHandler module = (TypeHandler) o;
        return type.equals(module.type);
    }

    @Override public int hashCode() {
        return type.hashCode();
    }

    public static final class Builder<T> {
        private final Class<? extends T> type;
        private final Map<String, List<MethodHandler>> handlers
                = new HashMap<String, List<MethodHandler>>();

        public Builder(Class<? extends T> type) {
            this.type = type;
        }

        public MethodBuilder<T> method(String methodName) {
            return new MethodBuilder<T>(this, methodName);
        }

        public Builder<T> handle(String methodName, MethodHandler handler) {
            return method(methodName)
                    .handle(handler)
                    .add();
        }

        public TypeHandler<T> build() {
            return new TypeHandler<T>(this);
        }

        private Builder<T> add(MethodBuilder<T> builder) {
            List<MethodHandler> handlerList = this.handlers.get(builder.methodName);
            if (handlerList == null) {
                handlerList = new ArrayList<MethodHandler>();
                this.handlers.put(builder.methodName, handlerList);
            }
            handlerList.add(builder.handler);
            return this;
        }
    }

    public static final class MethodBuilder<T> {
        private final Builder<T> builder;
        private final String methodName;
        private MethodHandler handler;

        private MethodBuilder(Builder<T> builder, String methodName) {
            this.builder = builder;
            this.methodName = methodName;
        }

        public MethodBuilder<T> handle(MethodHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder<T> add() {
            if (handler == null) {
                throw new IllegalArgumentException("must set a handler");
            }
            return builder.add(this);
        }
    }
}
