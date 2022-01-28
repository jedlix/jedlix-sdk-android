/*
 * Copyright 2022 Jedlix B.V. The Netherlands
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jedlix.sdk.networking

/**
 * Http method
 */
sealed interface Method {
    /**
     * Get method
     */
    object Get : Method

    /**
     * Delete method
     */
    object Delete : Method

    /**
     * Http method containing a body
     * @param Body the body object of the method
     */
    sealed interface MethodWithBody<Body : Any> : Method {
        val body: Body
    }

    /**
     * Post method
     */
    interface Post<Body : Any> : MethodWithBody<Body>

    /**
     * Put method
     */
    interface Put<Body : Any> : MethodWithBody<Body>

    /**
     * Patch method
     */
    interface Patch<Body : Any> : MethodWithBody<Body>

    object EmptyPost : Method
}
