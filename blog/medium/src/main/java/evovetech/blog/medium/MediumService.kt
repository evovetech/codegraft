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

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

typealias MediumCall<T> = Call<MediumResponse<T>>

interface MediumService {
    @GET("{userId}")
    fun user(@Path("userId") userId: String = "me"): MediumCall<User>

    @GET("users/{username}/publications")
    fun publications(
        @Path("username") userId: String
    ): MediumCall<List<Publication>>
}

fun <T : Any> Response<T>.onSuccess(success: (body: T?) -> Unit): Unit =
    fold(success = success, failure = {})

fun Response<*>.onFailure(failure: (error: ResponseBody?) -> Unit): Unit =
    fold(success = {}, failure = failure)

fun <T : Any, R> Response<T>.fold(
    success: (body: T?) -> R,
    failure: (error: ResponseBody?) -> R
): R {
    return if (isSuccessful) {
        success(body())
    } else {
        failure(errorBody())
    }
}

fun User.publications(service: MediumService): MediumCall<List<Publication>> {
    return service.publications(id)
}
