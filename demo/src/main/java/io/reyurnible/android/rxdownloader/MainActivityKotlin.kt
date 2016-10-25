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
        val editTextUri: EditText = findViewById(R.id.edittext_url) as EditText
        findViewById(R.id.button_download).setOnClickListener {
            val uri = Uri.parse("https://dl.dropboxusercontent.com/u/31455721/bg_jpg/150501.jpg")
            download(uri)
        }
        findViewById(R.id.button_view).setOnClickListener {
            val intent: Intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }

        /* === Handling multi request === */
        Observable.range(1, 5)
                .map {
                    DownloadManager.Request(
                            Uri.parse("https://dl.dropboxusercontent.com/u/31455721/bg_jpg/15050${it}.jpg")
                    ).apply {
                        setAllowedNetworkTypes(
                                DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
                        )
                        setTitle("Sample Download")
                        setDescription("sample of using download manager")
                        // and so your reuest settings...
                    }
                }
                .toList()
                .flatMap { it.execute(this) }
                .subscribe(object : Subscriber<RxDownloader.DownloadStatus>() {
                    override fun onNext(status: RxDownloader.DownloadStatus?) {
                        // Handling status
                    }

                    override fun onError(e: Throwable?) {
                        // Error action
                    }

                    override fun onCompleted() {
                        // Complete all request
                    }
                })
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
                .subscribe(object : Subscriber<RxDownloader.DownloadStatus>() {
                    override fun onNext(status: RxDownloader.DownloadStatus?) {
                        // Handling status
                    }

                    override fun onError(e: Throwable?) {
                        // Error action
                    }

                    override fun onCompleted() {
                        // Complete all request
                    }
                })
    }


}
