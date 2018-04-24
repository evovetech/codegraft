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

package com.laynemobile.proxy.internal;

import com.laynemobile.proxy.util.Logger;

public class ConsoleLogger implements Logger {
    @Override public void v(String tag, String msg) {
        System.out.println(tag + ": " + msg);
    }

    @Override public void v(String tag, String msg, Throwable tr) {
        System.out.println(tag + ": " + msg + ", " + ProxyLog.getStackTraceString(tr));
    }

    @Override public void d(String tag, String msg) {
        v(tag, msg);
    }

    @Override public void d(String tag, String msg, Throwable tr) {
        v(tag, msg, tr);
    }

    @Override public void i(String tag, String msg) {
        v(tag, msg);
    }

    @Override public void i(String tag, String msg, Throwable tr) {
        v(tag, msg, tr);
    }

    @Override public void w(String tag, String msg) {
        v(tag, msg);
    }

    @Override public void w(String tag, String msg, Throwable tr) {
        v(tag, msg, tr);
    }

    @Override public void e(String tag, String msg) {
        v(tag, msg);
    }

    @Override public void e(String tag, String msg, Throwable tr) {
        v(tag, msg, tr);
    }
}
