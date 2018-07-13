/*
 * Copyright 2018 evove.tech
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

package evovetech.blog.medium

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface MediumService {
    @GET("me")
    fun me(
        @Header("Authorization") token: String
    ): Call<User.Response>
}

fun MediumService.me(): Call<User.Response> {
    val auth = "Bearer ${BuildConfig.API_KEY}"
    return me(auth)
}

