/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.compiler.processing

/**
 * Represents a method, constructor or initializer.
 *
 * @see [javax.lang.model.element.ExecutableElement]
 */
interface XExecutableElement : XHasModifiers, XElement {
    /**
     * The element that declared this executable.
     *
     * @see requireEnclosingTypeElement
     */
    val enclosingElement: XElement
    /**
     * The list of parameters that should be passed into this method.
     *
     * @see [isVarArgs]
     */
    val parameters: List<XExecutableParameterElement>
    /**
     * Returns true if this method receives a vararg parameter.
     */
    fun isVarArgs(): Boolean
}

/**
 * Checks the enclosing element is a TypeElement and returns it, otherwise,
 * throws [IllegalStateException].
 */
fun XExecutableElement.requireEnclosingTypeElement(): XTypeElement {
    return enclosingElement as? XTypeElement
        ?: error("Required enclosing type element for $this but found $enclosingElement")
}
