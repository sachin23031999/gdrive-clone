package com.sachin.gdrive.common

/**
 * Application common constants.
 */
object Worker {
    /**
     * File uploader worker name.
     */
    const val FILE_UPLOAD_WORKER_NAME = "periodic_metrics_upload_worker"

    /**
     * File uploader worker repeat interval in case of failure.
     */
    const val WORKER_REPEAT_INTERVAL_IN_HOURS = 1L
}