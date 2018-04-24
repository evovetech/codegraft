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

import com.google.auto.service.AutoService;
import com.laynemobile.proxy.internal.ConsoleLogger;
import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.model.ProxyTemplate;

import javax.annotation.processing.Processor;

import sourcerer.processor.Env;
import sourcerer.processor.TemplateProcessor;

@AutoService(Processor.class)
public class ProxyGenerator extends TemplateProcessor<ProxyTemplate> {
    @Override protected ProxyTemplate createEnv(Env env) {
        return new ProxyTemplate(env);
    }

    @Override protected void init(ProxyTemplate proxyTemplate) {
        super.init(proxyTemplate);
        ProxyLog.setLogger(new ConsoleLogger());
    }
}
