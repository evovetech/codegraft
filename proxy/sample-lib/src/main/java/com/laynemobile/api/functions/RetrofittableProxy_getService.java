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

package com.laynemobile.api.functions;

import com.laynemobile.api.functions.parent.AbstractRetrofittableProxy_getService;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;

import retrofit.RestAdapter;

@Generated
public class RetrofittableProxy_getService<S> extends AbstractRetrofittableProxy_getService<S> {
    public RetrofittableProxy_getService(Func0<S> service) {
        super(service);
    }

    public RetrofittableProxy_getService(final Class<S> serviceType, final Func0<RestAdapter> restAdapter) {
        super(new Func0<S>() {
            @Override public S call() {
                return createService(serviceType, restAdapter.call());
            }
        });
    }

    public RetrofittableProxy_getService(final Class<S> serviceType, final RestAdapter restAdapter) {
        super(new Func0<S>() {
            @Override public S call() {
                return createService(serviceType, restAdapter);
            }
        });
    }

    private static <S> S createService(Class<S> serviceType, RestAdapter restAdapter) {
        return restAdapter.create(serviceType);
    }
}
