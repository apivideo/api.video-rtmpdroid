[![badge](https://img.shields.io/twitter/follow/api_video?style=social)](https://twitter.com/intent/follow?screen_name=api_video)
&nbsp; [![badge](https://img.shields.io/github/stars/apivideo/api.video-rtmpdroid?style=social)](https://github.com/apivideo/api.video-rtmpdroid)
&nbsp; [![badge](https://img.shields.io/discourse/topics?server=https%3A%2F%2Fcommunity.api.video)](https://community.api.video)
![](https://github.com/apivideo/.github/blob/main/assets/apivideo_banner.png)
<h1 align="center">api.video Android live stream library</h1>

[api.video](https://api.video) is the video infrastructure for product builders. Lightning fast
video APIs for integrating, scaling, and managing on-demand & low latency live streaming features in
your app.

# Table of contents

- [Table of contents](#table-of-contents)
- [Project description](#project-description)
- [Getting started](#getting-started)
    - [Installation](#installation)
        - [Gradle](#gradle)
    - [Permissions](#permissions)
- [Documentation](#documentation)
- [FAQ](#faq)

# Project description

When it comes to Real-Time Messaging Protocol (RTMP), the most popular implementation is the C
library: [librtmp](http://git.ffmpeg.org/rtmpdump). However, if you are streaming from Android and
want to use librtmp, you need to go through the following painful steps:

- compile librtmp for all Android architectures to generate multiple shared libraries
- write a code that links the librtmp C functions to the Java classes/methods using Java Native
  Interface (JNI)
- write Java classes/methods

rtmpdroid has been built to address this pain: you can directly use librtmp in your Android
application like any other Java/Kotlin library.

rtmpdroid also comes with a minimalist Action Message Format (AMF) encoder that is included in
librtmp.

# Getting started

## Installation

### Gradle

In build.gradle, add the following code:

```groovy
dependencies {
    implementation 'video.api:rtmpdroid:1.2.1'
}
```

## Code sample

### RTMP

```kotlin
val rtmp = Rtmp()
rtmp.connect("rtmp://broadcast.api.video/s/YOUR_STREAM_KEY")
rtmp.connectStream()

while (true) {
    val flvBuffer = getNewFlvFrame() // flvBuffer is a direct ByteBuffer 
    rtmp.write(flvBuffer)
}
```

### AMF

```kotlin
val amfEncoder = AmfEncoder()

amfEncoder.add("myParam", 3.0)
val ecmaArray = EcmaArray()
ecmaArray.add("myOtherParam", "value")
amfEncoder.add(ecmaArray)

val amfBuffer = amfEncoder.encode()
```

## Permissions

```xml

<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

## Packed version

The default version of `rtmpdroid` comes with the following shared libraries:

- librtmp.so
- libssl.so
- libcrypto.so
- librtmpdroid.so

However, your application might already use `libssl` and `libcrypto`. In this case, you can use the
packed version of `rtmpdroid`. It only contains only `librtmpdroid.so` and the other libraries are
contains in this library.
To use the packed version, add a `-packed` suffix to the `rtmpdroid` version in your `build.gradle`:

```groovy
dependencies {
    implementation 'video.api:rtmpdroid:1.2.0-packed'
}
```

# Documentation

* [API documentation](https://apivideo.github.io/api.video-rtmpdroid/)
* [api.video documentation](https://docs.api.video)

# FAQ

If you have any questions, ask us in [https://community.api.video](https://community.api.video) or
use [Issues].


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

[Issues]: <https://github.com/apivideo/api.video-rtmpdroid/issues>
