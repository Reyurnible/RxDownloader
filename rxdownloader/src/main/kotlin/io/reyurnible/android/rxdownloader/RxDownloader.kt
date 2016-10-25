package io.reyurnible.android.rxdownloader

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.util.Log
import rx.AsyncEmitter
import rx.Observable
import java.util.*

/**
 * DownloadManagerをRxで処理するためのクラス
 */
class RxDownloader(
        private val context: Context,
        // Downloadするリクエストのリスト
        private val requests: ArrayList<DownloadManager.Request> = ArrayList<DownloadManager.Request>()) {

    companion object {
        const val TAG = "RxDownloader"
    }

    private val manager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    // Requestで行ったダウンロードの一覧を管理しておくためのクラス
    private val queuedRequests: HashMap<Long, DownloadManager.Request> =
            HashMap<Long, DownloadManager.Request>()
    // 最後にUnsubscriveしないと行けないため、ここで所持しておく
    private var receiver: BroadcastReceiver? = null

    fun enqueue(request: DownloadManager.Request): RxDownloader = apply {
        requests.add(request)
    }

    fun execute(): Observable<DownloadStatus> =
            if (requests.isEmpty()) Observable.empty()
            else Observable.fromEmitter({ emitter ->
                receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        intent ?: return
                        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.action)) {
                            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                            if (!queuedRequests.contains(id)) {
                                // このリクエストのセットの中のものでなければ処理をしない
                                return
                            }
                            // チェッキングStatus
                            resolveDownloadStatus(id, emitter)
                            // 未処理のリクエスト一覧からリクエストを削除する
                            queuedRequests.remove(id)
                            // 全てのリクエストが終わっていたらonCompleteを投げる
                            if (queuedRequests.isEmpty()) {
                                emitter.onCompleted()
                            }
                        }
                    }
                }
                context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                // 実際のリクエストを行う
                requests.forEach {
                    val downloadId = manager.enqueue(it)
                    // このObservableの処理待ちのRequestとして追加する
                    queuedRequests.put(downloadId, it)
                    Log.d(TAG, "ID: ${downloadId}, START")
                }

                emitter.setCancellation {
                    // Unsubscribeされたタイミングでリクエストを全て破棄する
                    queuedRequests.forEach {
                        manager.remove(it.key)
                    }
                    // これ以上イベントは来ないのでReceiverを解除する
                    receiver?.let {
                        context.unregisterReceiver(it)
                    }
                }
            }, AsyncEmitter.BackpressureMode.BUFFER)

    private fun resolveDownloadStatus(id: Long, emitter: AsyncEmitter<in DownloadStatus>) {
        val query = DownloadManager.Query().apply {
            setFilterById(id)
        }
        val cursor = manager.query(query)
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
            val requestResult: RequestResult = createRequestResult(id, cursor)
            Log.d(TAG, "RESULT: ${requestResult.toString()}")
            when (status) {
                DownloadManager.STATUS_FAILED -> {
                    val failedReason = when (reason) {
                        DownloadManager.ERROR_CANNOT_RESUME -> "ERROR_CANNOT_RESUME"
                        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "ERROR_DEVICE_NOT_FOUND"
                        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "ERROR_FILE_ALREADY_EXISTS"
                        DownloadManager.ERROR_FILE_ERROR -> "ERROR_FILE_ERROR"
                        DownloadManager.ERROR_HTTP_DATA_ERROR -> "ERROR_HTTP_DATA_ERROR"
                        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "ERROR_INSUFFICIENT_SPACE"
                        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "ERROR_TOO_MANY_REDIRECTS"
                        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "ERROR_UNHANDLED_HTTP_CODE"
                        DownloadManager.ERROR_UNKNOWN -> "ERROR_UNKNOWN"
                        else -> ""
                    }
                    Log.e(TAG, "ID: ${id}, FAILED: ${failedReason}")
                    emitter.onNext(DownloadStatus.Failed(requestResult, failedReason))
                    emitter.onError(DownloadFailedException(failedReason, queuedRequests[id]))
                }
                DownloadManager.STATUS_PAUSED -> {
                    val pausedReason = when (reason) {
                        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "PAUSED_QUEUED_FOR_WIFI"
                        DownloadManager.PAUSED_UNKNOWN -> "PAUSED_UNKNOWN"
                        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "PAUSED_WAITING_FOR_NETWORK"
                        DownloadManager.PAUSED_WAITING_TO_RETRY -> "PAUSED_WAITING_TO_RETRY"
                        else -> ""
                    }
                    Log.d(TAG, "ID: ${id}, PAUSED: ${pausedReason}")
                    emitter.onNext(DownloadStatus.Paused(requestResult, pausedReason))
                }
                DownloadManager.STATUS_PENDING -> {
                    Log.d(TAG, "ID: ${id}, PENDING")
                    emitter.onNext(DownloadStatus.Pending(requestResult))
                }
                DownloadManager.STATUS_RUNNING -> {
                    Log.d(TAG, "ID: ${id}, RUNNING")
                    emitter.onNext(DownloadStatus.Running(requestResult))
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    Log.d(TAG, "ID: ${id}, SUCCESSFUL")
                    emitter.onNext(DownloadStatus.Successful(requestResult))
                }
            }
        }
        cursor.close()
    }

    fun createRequestResult(id: Long, cursor: Cursor): RequestResult =
            RequestResult(
                    id = id,
                    remoteUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)),
                    localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)),
                    mediaType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)),
                    totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                    title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION))
            )

    sealed class DownloadStatus(val result: RequestResult) {
        class Successful(result: RequestResult) : DownloadStatus(result)
        class Running(result: RequestResult) : DownloadStatus(result)
        class Pending(result: RequestResult) : DownloadStatus(result)
        class Paused(result: RequestResult, val reason: String) : DownloadStatus(result)
        class Failed(result: RequestResult, val reason: String) : DownloadStatus(result)
    }

    // 再リクエストできるようにRequestを持たせるようにする
    class DownloadFailedException(message: String, val request: DownloadManager.Request?) : Throwable(message)
}
