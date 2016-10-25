package io.reyurnible.android.rxdownloader

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Demo activity in Kotlin
 */
class MainActivityKotlin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById(R.id.button_download).setOnClickListener {
            val uri = Uri.parse("https://dl.dropboxusercontent.com/u/31455721/bg_jpg/150501.jpg")
            download(uri)
        }
        findViewById(R.id.button_view).setOnClickListener {
            val intent: Intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }
    }

    private fun download(uri: Uri) {
        val request = DownloadManager.Request(uri).apply {
            setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
            )
            setTitle("Sample Download")
            setDescription("sample of using download manager")
            // and so your reuest settings...
        }
        request.execute(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ status ->
                    // Handling status
                    when (status) {
                        is RxDownloader.DownloadStatus.Successful -> {
                            Toast.makeText(this@MainActivityKotlin, "Successful Result: ${status.result.title}", Toast.LENGTH_SHORT).show()
                        }
                        is RxDownloader.DownloadStatus.Running -> {
                            Toast.makeText(this@MainActivityKotlin, "Running Result: ${status.result.title}", Toast.LENGTH_SHORT).show()
                        }
                        is RxDownloader.DownloadStatus.Paused -> {
                            Toast.makeText(this@MainActivityKotlin, "Paused Result: ${status.result.title}", Toast.LENGTH_SHORT).show()
                        }
                        is RxDownloader.DownloadStatus.Pending -> {
                            Toast.makeText(this@MainActivityKotlin, "Pending Result: ${status.result.title}", Toast.LENGTH_SHORT).show()
                        }
                        is RxDownloader.DownloadStatus.Failed -> {
                            Toast.makeText(this@MainActivityKotlin, "Failed Result: ${status.result.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, { error ->
                    // Error action
                }, {
                    // Complete all request
                    Toast.makeText(this@MainActivityKotlin, "Complete downloads.", Toast.LENGTH_SHORT).show()
                })
    }


}
