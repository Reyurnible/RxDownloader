package io.reyurnible.android.rxdownloader

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* === Step1 Create request object === */
        // Uri for download target uri
        val uri = Uri.parse("https://dl.dropboxusercontent.com/u/31455721/bg_jpg/150501.jpg")
        val request = DownloadManager.Request(uri).apply {
            setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI
            )
            setTitle("Sample Download")
            setDescription("sample of using download manager")
            // and so your reuest settings...
        }
        /* === Step2 Create rxdownload === */
        val rxDownloader = RxDownloader(this)
        /* === Step3 Enqueue request === */
        rxDownloader.enqueue(request)
        /* === Step4 Execute and subscribe === */
        rxDownloader.execute()
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

        /* === Step2~4 Kotlin extention   === */
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
}
