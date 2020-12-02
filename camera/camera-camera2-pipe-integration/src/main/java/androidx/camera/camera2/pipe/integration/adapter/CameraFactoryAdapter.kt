/*
 * Copyright 2020 The Android Open Source Project
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
package androidx.camera.camera2.pipe.integration.adapter

import android.content.Context
import androidx.camera.camera2.pipe.CameraId
import androidx.camera.camera2.pipe.impl.Debug
import androidx.camera.camera2.pipe.impl.Log.debug
import androidx.camera.camera2.pipe.impl.Timestamps
import androidx.camera.camera2.pipe.impl.Timestamps.measureNow
import androidx.camera.camera2.pipe.impl.Timestamps.formatMs
import androidx.camera.camera2.pipe.integration.config.CameraConfig
import androidx.camera.camera2.pipe.integration.config.CameraAppComponent
import androidx.camera.camera2.pipe.integration.config.CameraAppConfig
import androidx.camera.camera2.pipe.integration.config.DaggerCameraAppComponent
import androidx.camera.core.CameraSelector
import androidx.camera.core.impl.CameraFactory
import androidx.camera.core.impl.CameraInternal
import androidx.camera.core.impl.CameraThreadConfig

/**
 * The [CameraFactoryAdapter] is responsible for creating the root dagger component that is used
 * to share resources across Camera instances.
 */
class CameraFactoryAdapter(
    context: Context,
    threadConfig: CameraThreadConfig,
    availableCamerasSelector: CameraSelector?
) : CameraFactory {
    private val appComponent: CameraAppComponent by lazy {
        Debug.traceStart { "CameraFactoryAdapter#appComponent" }
        val start = Timestamps.now()
        val result = DaggerCameraAppComponent.builder()
            .config(CameraAppConfig(context, threadConfig))
            .build()
        debug { "Created CameraFactoryAdapter in ${start.measureNow().formatMs()}" }
        debug { "availableCamerasSelector: $availableCamerasSelector " }
        Debug.traceStop()
        result
    }

    init {
        debug { "Created CameraFactoryAdapter" }
    }

    override fun getCamera(cameraId: String): CameraInternal =
        appComponent.cameraBuilder()
            .config(CameraConfig(CameraId(cameraId)))
            .build()
            .getCameraInternal()

    override fun getAvailableCameraIds(): Set<String> = appComponent.getAvailableCameraIds()
    override fun getCameraManager(): Any? = appComponent
}