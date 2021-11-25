package com.udacity


sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}

sealed class DownloadState {
    object Successful : DownloadState()
    object Failed : DownloadState()
    object Unknown : DownloadState()
}

fun DownloadState.asString() = when(this) {
    DownloadState.Successful -> "Success"
    DownloadState.Failed -> "Fail"
    else -> "Unknown"
}