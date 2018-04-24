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

import static java.util.Locale.US;

public class AbstractFunctionTemplate extends FileTemplate {
    protected final int length;
    protected final String[] type_args;
    protected final String[] wildcard_type_args;
    protected final String[] function_parameters;
    protected final String[] function_args;
    protected final String[] cast_function_args;

    public AbstractFunctionTemplate(String packageName, String fileName, String template, int length) {
        super(packageName, fileName, template);
        String[] type_args = new String[length];
        String[] wildcard_type_args = new String[length];
        String[] function_parameters = new String[length];
        String[] function_args = new String[length];
        String[] cast_function_args = new String[length];
        if (length == 1) {
            type_args[0] = "T";
        } else {
            for (int i = 0; i < length; i++) {
                type_args[i] = "T" + (i + 1);
            }
        }
        for (int i = 0; i < length; i++) {
            String type_arg = type_args[i];
            String function_arg = type_arg.toLowerCase(US);
            wildcard_type_args[i] = "? super " + type_arg;
            function_parameters[i] = type_arg + " " + function_arg;
            function_args[i] = function_arg;
            cast_function_args[i] = "(" + type_arg + ") " + "args[" + i + "]";
        }

        this.length = length;
        this.type_args = type_args;
        this.wildcard_type_args = wildcard_type_args;
        this.function_parameters = function_parameters;
        this.function_args = function_args;
        this.cast_function_args = cast_function_args;
    }

    public AbstractFunctionTemplate fill() {
        this
                .fill("LENGTH", length)
                .fill("TYPE_ARGS", type_args)
                .fill("WILDCARD_TYPE_ARGS", wildcard_type_args)
                .fill("FUNCTION_PARAMETERS", function_parameters)
                .fill("FUNCTION_ARGS", function_args)
                .fill("CAST_FUNCTION_ARGS", cast_function_args)
        ;
        return this;
    }
}
