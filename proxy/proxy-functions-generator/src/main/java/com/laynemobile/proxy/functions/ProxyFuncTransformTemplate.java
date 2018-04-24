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

package com.laynemobile.proxy.functions;

public class ProxyFuncTransformTemplate extends AbstractFunctionTemplate {
    private static final String CLASS_NAME_TEMPLATE = "ProxyFunc${LENGTH}Transform";

    private final int func_length;

    public ProxyFuncTransformTemplate(String packageName, int length) {
        super(packageName, className(length), TEMPLATE, length);
        this.func_length = length + 1;
    }

    @Override public ProxyFuncTransformTemplate fill() {
        super.fill()
                .fill("FUNC_LENGTH", func_length);
        return this;
    }

    private static String className(int length) {
        return new CodeTemplate(CLASS_NAME_TEMPLATE)
                .fill("LENGTH", length)
                .output();
    }

    private static final String TEMPLATE = "" +
            "\n" +
            "package ${PACKAGE_NAME};\n" +
            "\n" +
            "import com.laynemobile.proxy.functions.Func0;\n" +
            "import com.laynemobile.proxy.functions.Func${LENGTH};\n" +
            "import com.laynemobile.proxy.functions.Func${FUNC_LENGTH};\n" +
            "\n" +
            "public class ${CLASS_NAME}<P, ${TYPE_ARGS}, R>\n" +
            "        extends ProxyFunctionTransform<P, Func${FUNC_LENGTH}<? super P, ${WILDCARD_TYPE_ARGS}, ? extends R>, R>\n" +
            "        implements Func${FUNC_LENGTH}<P, ${TYPE_ARGS}, R> {\n" +
            "\n" +
            "    public ${CLASS_NAME}(Func${FUNC_LENGTH}<? super P, ${WILDCARD_TYPE_ARGS}, ? extends R> function) {\n" +
            "        super(function);\n" +
            "    }\n" +
            "\n" +
            "    public ${CLASS_NAME}(${CLASS_NAME}<? super P, ${WILDCARD_TYPE_ARGS}, ? extends R> function) {\n" +
            "        super(function.function);\n" +
            "    }\n" +
            "\n" +
            "    public ${CLASS_NAME}(final Func${LENGTH}<${WILDCARD_TYPE_ARGS}, ? extends R> function) {\n" +
            "        super(new Func${FUNC_LENGTH}<P, ${TYPE_ARGS}, R>() {\n" +
            "            @Override public R call(P p, ${FUNCTION_PARAMETERS}) {\n" +
            "                return function.call(${FUNCTION_ARGS});\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    public ${CLASS_NAME}(final Func0<? extends R> function) {\n" +
            "        super(new Func${FUNC_LENGTH}<P, ${TYPE_ARGS}, R>() {\n" +
            "            @Override public R call(P p, ${FUNCTION_PARAMETERS}) {\n" +
            "                return function.call();\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    public ${CLASS_NAME}(final R value) {\n" +
            "        super(new Func${FUNC_LENGTH}<P, ${TYPE_ARGS}, R>() {\n" +
            "            @Override public R call(P p, ${FUNCTION_PARAMETERS}) {\n" +
            "                return value;\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    @Override public final R call(P proxy, ${FUNCTION_PARAMETERS}) {\n" +
            "        return function.call(proxy, ${FUNCTION_ARGS});\n" +
            "    }\n" +
            "\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    @Override public final R call(P proxy, Object... args) {\n" +
            "        if (args.length != ${LENGTH}) {\n" +
            "            throw new RuntimeException(\"Func${LENGTH} expecting ${LENGTH} arguments.\");\n" +
            "        }\n" +
            "        return function.call(proxy, ${CAST_FUNCTION_ARGS});\n" +
            "    }\n" +
            "}";
}
