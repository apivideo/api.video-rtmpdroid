# Changelog

All changes to this project will be documented in this file.

## [1.1.0] - 2023-10-23

- Add API to support enhanced RTMP features
- Upgrade dependencies (Kotlin,...) (incl. openssl to 3.0.9)

## [1.0.5] - 2022-11-07

- Upgrade openssl to 3.0.7
- Fix tcUrl length

## [1.0.4] - 2022-10-06

- Fix a crash when freeing url in `nativeClose`.
  See [#14](https://github.com/apivideo/api.video-flutter-live-stream/issues/14)
  and [#33](https://github.com/apivideo/api.video-reactnative-live-stream/issues/33).

## [1.0.3] - 2022-08-05

- Shutdown socket to interrupt socket long connection

## [1.0.2] - 2022-05-30

- Do not obfuscate `video.api.rtmpdroid` classes

## [1.0.1] - 2022-03-30

- Change `connect(url)` exception when URL is not valid to `IllegalArgumentException`.

## [1.0.0] - 2022-03-22

- Initial version
