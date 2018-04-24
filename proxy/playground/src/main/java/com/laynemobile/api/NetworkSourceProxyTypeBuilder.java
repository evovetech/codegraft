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

package com.laynemobile.api;

import com.laynemobile.api.functions.NetworkSourceTransform_networkChecker;
import com.laynemobile.api.functions.NetworkSource_networkChecker;
import com.laynemobile.proxy.AbstractProxyTypeBuilder;
import com.laynemobile.proxy.ProxyType;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;

@Generated
public class NetworkSourceProxyTypeBuilder<T, P extends Params> extends AbstractProxyTypeBuilder<NetworkSource<T, P>> {
    private NetworkSource_networkChecker networkCheckerDef = new NetworkSource_networkChecker();
    private NetworkSource_networkChecker.Function networkChecker;

    public NetworkSourceProxyTypeBuilder<T, P> setNetworkChecker(NetworkSource_networkChecker.Function networkChecker) {
        this.networkChecker = networkChecker;
        return this;
    }

    public NetworkSourceProxyTypeBuilder<T, P> setNetworkChecker(NetworkSourceTransform_networkChecker networkChecker) {
        return setNetworkChecker(this.networkCheckerDef.asFunction(networkChecker));
    }

    public NetworkSourceProxyTypeBuilder<T, P> setNetworkChecker(Func0<? extends NetworkChecker> networkChecker) {
        return setNetworkChecker(new NetworkSourceTransform_networkChecker(networkChecker));
    }

    public NetworkSourceProxyTypeBuilder<T, P> setNetworkChecker(NetworkChecker networkChecker) {
        return setNetworkChecker(new NetworkSourceTransform_networkChecker(networkChecker));
    }

    public NetworkSourceProxyTypeBuilder<T, P> setNetworkChecker() {
        return setNetworkChecker(new NetworkSourceTransform_networkChecker());
    }

    @Override public ProxyType<NetworkSource<T, P>> buildProxyType() {
        return new NetworkSourceDef<T, P>().newProxyBuilder()
                .addFunction(networkChecker)
                .build();
    }
}
