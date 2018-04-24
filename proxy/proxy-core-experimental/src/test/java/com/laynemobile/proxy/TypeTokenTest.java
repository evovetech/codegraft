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

import com.laynemobile.proxy.functions.base.AbstractAction2;
import com.laynemobile.proxy.functions.base.AbstractFunc2;
import com.laynemobile.proxy.internal.ConsoleLogger;
import com.laynemobile.proxy.internal.ProxyLog;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Subscriber;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class TypeTokenTest {
    private static final String TAG = TypeTokenTest.class.getSimpleName();

    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testPrimitiveTypeToken() throws Exception {
        TypeToken<Integer> intType = TypeToken.get(int.class);
        TypeToken<Integer> integerType = TypeToken.get(Integer.class);
        assertNotEquals(intType, integerType);
        assertNotEquals(intType.getRawType(), integerType.getRawType());
    }

    @Test public void testAction2Params() throws Exception {
        AbstractAction2<Integer, Subscriber<? super String>> action = new AbstractAction2<Integer, Subscriber<? super String>>() {
            @Override public void call(Integer integer, Subscriber<? super String> subscriber) {

            }
        };
        TypeToken<Void> expectedReturnType = TypeToken.get(Void.TYPE);
        List<TypeToken<?>> expectedParamTypes = Arrays.asList(
                TypeToken.get(Integer.class),
                new TypeToken<Subscriber<? super String>>() {}
        );

        assertEquals(expectedReturnType, action.returnType());
        assertEquals(expectedParamTypes, action.paramTypes());
    }

    @Test public void testFunc2Params() throws Exception {
        AbstractFunc2<Long, Subscriber<? super String>, String> action = new AbstractFunc2<Long, Subscriber<? super String>, String>() {
            @Override public String call(Long num, Subscriber<? super String> subscriber) {
                return num.toString();
            }
        };
        TypeToken<String> expectedReturnType = TypeToken.get(String.class);
        List<TypeToken<?>> expectedParamTypes = Arrays.asList(
                TypeToken.get(Long.class),
                new TypeToken<Subscriber<? super String>>() {}
        );

        assertEquals(expectedReturnType, action.returnType());
        assertEquals(expectedParamTypes, action.paramTypes());
    }

    @Test public void test_func2ExtShouldFail() throws Exception {
        try {
            new Func2Ext<Long, Subscriber<? super String>, String>() {
                @Override public String call(Long num, Subscriber<? super String> subscriber) {
                    return num.toString();
                }
            };
            fail("should throw");
        } catch (IllegalStateException expected) {
            // passed
        }
    }

    static abstract class Func2Ext<T1, T2, R> extends AbstractFunc2<T1, T2, R> {}
}
