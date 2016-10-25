[![Kotlin](https://img.shields.io/badge/kotlin-1.0.4-blue.svg)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin Slack](https://img.shields.io/badge/chat-kotlin%20slack-orange.svg)](http://kotlinslackin.herokuapp.com)

## About
RxDownloader is handling DownloadManager event as observable.

## Using
Simple use.

1. create DonwloadManager.Request
2. RxDownloader.execute()
  - create RxDownloader
  - enqueue request
  - execute

You can use multi download request.

```
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
/* === Step2 Execute(Kotlin extention) === */
reuest.execute()
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
```

explain for japanease.
[Qiita](http://qiita.com/Reyurnible/items/7a706f9323e66d8819d1)

## Requirement

AndroidStudio > 2.1
RxJava > 1.4.1

## Build

To build:

```
$ git clone git@github.com:Reyurnible/RxDownloader.git
$ cd RxDownloader/
$ ./gradlew build
```

## Bugs and Feedback

For bugs, questions and discussions please use the Github Issues.

## LICENSE

Copyright 2016 Shun Hosaka.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
