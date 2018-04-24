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

package com.laynemobile.proxy.util;

public interface Logger {
    Logger NONE = new Logger() {
        @Override
        public void v(String tag, String msg) {}

        @Override
        public void v(String tag, String msg, Throwable tr) {}

        @Override
        public void d(String tag, String msg) {}

        @Override
        public void d(String tag, String msg, Throwable tr) {}

        @Override
        public void i(String tag, String msg) {}

        @Override
        public void i(String tag, String msg, Throwable tr) {}

        @Override
        public void w(String tag, String msg) {}

        @Override
        public void w(String tag, String msg, Throwable tr) {}

        @Override
        public void e(String tag, String msg) {}

        @Override
        public void e(String tag, String msg, Throwable tr) {}
    };

    void v(final String tag, final String msg);

    void v(final String tag, final String msg, final Throwable tr);

    void d(final String tag, final String msg);

    void d(final String tag, final String msg, final Throwable tr);

    void i(final String tag, final String msg);

    void i(final String tag, final String msg, final Throwable tr);

    void w(final String tag, final String msg);

    void w(final String tag, final String msg, final Throwable tr);

    void e(final String tag, final String msg);

    void e(final String tag, final String msg, final Throwable tr);
}
