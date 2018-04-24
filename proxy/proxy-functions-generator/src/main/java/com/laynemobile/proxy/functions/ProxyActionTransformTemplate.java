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

public class ProxyActionTransformTemplate extends AbstractFunctionTemplate {
    private static final String CLASS_NAME_TEMPLATE = "ProxyAction${LENGTH}Transform";

    private final int func_length;

    public ProxyActionTransformTemplate(String packageName, int length) {
        super(packageName, className(length), TEMPLATE, length);
        this.func_length = length + 1;
    }

    @Override public ProxyActionTransformTemplate fill() {
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
            "/*\n" +
            " * Copyright 2016 Layne Mobile, LLC\n" +
            " *\n" +
            " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " * you may not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " *     http://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing, software\n" +
            " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " * See the License for the specific language governing permissions and\n" +
            " * limitations under the License.\n" +
            " */\n" +
            "\n" +
            "package ${PACKAGE_NAME};\n" +
            "\n" +
            "import com.laynemobile.proxy.functions.Action0;\n" +
            "import com.laynemobile.proxy.functions.Action${LENGTH};\n" +
            "import com.laynemobile.proxy.functions.Action${FUNC_LENGTH};\n" +
            "import com.laynemobile.proxy.functions.Actions;\n" +
            "\n" +
            "public class ${CLASS_NAME}<P, ${TYPE_ARGS}>\n" +
            "        extends ProxyActionTransform<P, Action${FUNC_LENGTH}<? super P, ${WILDCARD_TYPE_ARGS}>>\n" +
            "        implements Action${FUNC_LENGTH}<P, ${TYPE_ARGS}> {\n" +
            "    public ProxyAction${LENGTH}Transform() {\n" +
            "        super(Actions.empty());\n" +
            "    }\n" +
            "\n" +
            "    public ProxyAction${LENGTH}Transform(final Action0 action) {\n" +
            "        super(new Action${FUNC_LENGTH}<P, ${TYPE_ARGS}>() {\n" +
            "            @Override public void call(P p, ${FUNCTION_PARAMETERS}) {\n" +
            "                action.call();\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    public ProxyAction${LENGTH}Transform(final Action${LENGTH}<${WILDCARD_TYPE_ARGS}> action) {\n" +
            "        super(new Action${FUNC_LENGTH}<P, ${TYPE_ARGS}>() {\n" +
            "            @Override public void call(P p, ${FUNCTION_PARAMETERS}) {\n" +
            "                action.call(${FUNCTION_ARGS});\n" +
            "            }\n" +
            "        });\n" +
            "    }\n" +
            "\n" +
            "    public ProxyAction${LENGTH}Transform(Action${FUNC_LENGTH}<? super P, ${WILDCARD_TYPE_ARGS}> action) {\n" +
            "        super(action);\n" +
            "    }\n" +
            "\n" +
            "    public ProxyAction${LENGTH}Transform(ProxyAction${LENGTH}Transform<? super P, ${WILDCARD_TYPE_ARGS}> action) {\n" +
            "        super(action.function);\n" +
            "    }\n" +
            "\n" +
            "    @SuppressWarnings(\"unchecked\")\n" +
            "    @Override protected final void invoke(P proxy, Object... args) {\n" +
            "        if (args.length != ${LENGTH}) {\n" +
            "            throw new RuntimeException(\"Action${LENGTH} expecting ${LENGTH} arguments.\");\n" +
            "        }\n" +
            "        function.call(proxy, ${CAST_FUNCTION_ARGS});\n" +
            "    }\n" +
            "\n" +
            "    @Override public final void call(P proxy, ${FUNCTION_PARAMETERS}) {\n" +
            "        function.call(proxy, ${FUNCTION_ARGS});\n" +
            "    }\n" +
            "}\n";
}
