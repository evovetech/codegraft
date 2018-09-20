/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    fun user(
        @Path("userId") userId: String = "me"
    ): MediumCall<User>

    @GET("users/{username}/publications")
    fun publications(
        @Path("username") userId: String
    ): MediumCall<List<Publication>>
}

inline
fun <T : Any> Response<T>.onSuccess(success: (body: T?) -> Unit): Unit =
    fold(success = success, failure = {})

inline
fun Response<*>.onFailure(failure: (error: ResponseBody?) -> Unit): Unit =
    fold(success = {}, failure = failure)

inline
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
