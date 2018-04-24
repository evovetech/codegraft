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

public class ProxyActionDefTemplate extends AbstractFunctionDefTemplate {
    private static final String CLASS_NAME_TEMPLATE = "ProxyAction${LENGTH}Def";

    public ProxyActionDefTemplate(String packageName, int length) {
        super(packageName, className(length), TEMPLATE, length);
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
            "import com.laynemobile.proxy.TypeToken;\n" +
            "import com.laynemobile.proxy.functions.transforms.ProxyAction${LENGTH}Transform;\n" +
            "\n" +
            "public class ${CLASS_NAME}<P, ${TYPE_ARGS}> extends ProxyActionDef<P, ProxyAction${LENGTH}Transform<P, ${TYPE_ARGS}>> {\n" +
            "    public ${CLASS_NAME}(String name, ${TYPE_TOKEN_PARAMETERS}) {\n" +
            "        super(name, new TypeToken<?>[]{${FUNCTION_ARGS}});\n" +
            "    }\n" +
            "\n" +
            "    @Override public Action<P, ${TYPE_ARGS}> asFunction(ProxyAction${LENGTH}Transform<P, ${TYPE_ARGS}> transform) {\n" +
            "        return new Action<>(this, transform);\n" +
            "    }\n" +
            "\n" +
            "    public static class Action<P, ${TYPE_ARGS}> extends ProxyAction2<P, ProxyAction${LENGTH}Transform<P, ${TYPE_ARGS}>> {\n" +
            "        protected Action(ProxyAction${LENGTH}Def<P, ${TYPE_ARGS}> actionDef, ProxyAction${LENGTH}Transform<P, ${TYPE_ARGS}> action) {\n" +
            "            super(actionDef, action);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
}
