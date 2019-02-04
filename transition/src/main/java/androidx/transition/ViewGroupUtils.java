/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.transition;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.os.BuildCompat;

/**
 * Compatibility utilities for platform features of {@link ViewGroup}.
 */
class ViewGroupUtils {

    /**
     * False when linking of the hidden suppressLayout method has previously failed.
     */
    private static boolean sTryHiddenSuppressLayout = true;

    /**
     * Backward-compatible {@link ViewGroup#getOverlay()}.
     */
    static ViewGroupOverlayImpl getOverlay(@NonNull ViewGroup group) {
        if (Build.VERSION.SDK_INT >= 18) {
            return new ViewGroupOverlayApi18(group);
        }
        return ViewGroupOverlayApi14.createFrom(group);
    }

    /**
     * Provides access to the hidden ViewGroup#suppressLayout method.
     */
    @SuppressLint("NewApi") // TODO: Remove this suppression once Q SDK is released.
    static void suppressLayout(@NonNull ViewGroup group, boolean suppress) {
        if (BuildCompat.isAtLeastQ()) {
            group.suppressLayout(suppress);
        } else if (Build.VERSION.SDK_INT >= 18) {
            hiddenSuppressLayout(group, suppress);
        } else {
            ViewGroupUtilsApi14.suppressLayout(group, suppress);
        }
    }

    @RequiresApi(18)
    @SuppressLint("NewApi") // Lint doesn't know about the hidden method.
    private static void hiddenSuppressLayout(@NonNull ViewGroup group, boolean suppress) {
        if (sTryHiddenSuppressLayout) {
            // Since this was an @hide method made public, we can link directly against it with
            // a try/catch for its absence instead of doing the same through reflection.
            try {
                group.suppressLayout(suppress);
            } catch (NoSuchMethodError e) {
                sTryHiddenSuppressLayout = false;
            }
        }
    }

    private ViewGroupUtils() {
    }
}
