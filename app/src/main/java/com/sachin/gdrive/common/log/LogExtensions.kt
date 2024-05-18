package com.sachin.gdrive.common.log

import android.util.Log

/**
 * Use for debug logging.
 */
inline fun logD(tag: String = Logger.getTag(), desc: () -> String) {
    if (Logger.DEBUG) {
        Log.d(tag, desc())
    }
}

/**
 * Use for error logging.
 */
inline fun logE(tag: String = Logger.getTag(), desc: () -> String) {
    Log.e(tag, desc())
}

/**
 * Use for info logging.
 */
inline fun logI(tag: String = Logger.getTag(), desc: () -> String) {
    Log.i(tag, desc())
}
