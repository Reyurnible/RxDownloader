package io.reyurnible.android.rxdownloader

import android.app.DownloadManager
import android.content.Context
import rx.Observable
import java.util.*

/**
 * Extention for RxDownloader
 */
fun Array<DownloadManager.Request>.execute(context: Context): Observable<RxDownloader.DownloadStatus> =
        RxDownloader(
                context,
                ArrayList<DownloadManager.Request>().apply { addAll(this@execute) }
        ).execute()

fun Collection<DownloadManager.Request>.execute(context: Context): Observable<RxDownloader.DownloadStatus> =
        RxDownloader(
                context,
                ArrayList<DownloadManager.Request>().apply { addAll(this@execute) }
        ).execute()

fun DownloadManager.Request.execute(context: Context): Observable<RxDownloader.DownloadStatus> =
        RxDownloader(context).enqueue(this).execute()
