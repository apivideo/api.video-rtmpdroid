#include <jni.h>
#include <string.h>

#include "librtmp/rtmp.h"
#include "librtmp/log.h"

#include "models/RtmpWrapper.h"
#include "Log.h"
#include "models/RtmpPacket.h"

#define RTMP_CLASS "video/api/rtmpdroid/Rtmp"
#define AMF_ENCODER_CLASS "video/api/rtmpdroid/amf/AmfEncoder"

#define STR2AVAL(av, str)    av.av_val = str; av.av_len = strlen(av.av_val)

JNIEXPORT jlong JNICALL
nativeAlloc(JNIEnv *env, jobject thiz) {
    RTMP *rtmp = RTMP_Alloc();
    if (rtmp != nullptr) {
        RTMP_Init(rtmp);
    }
    rtmp_context *rtmp_context = static_cast<struct rtmp_context *>(malloc(sizeof(rtmp_context)));
    rtmp_context->rtmp = rtmp;
    rtmp_context->url = nullptr;
    return reinterpret_cast<jlong>(rtmp_context);
}

JNIEXPORT jint JNICALL
nativeSetupURL(JNIEnv *env, jobject thiz, jstring jurl) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);

    char *url = const_cast<char *>(env->GetStringUTFChars(jurl, nullptr));
    rtmp_context->url = strdup(url);
    env->ReleaseStringUTFChars(jurl, url);

    int res = RTMP_SetupURL(rtmp_context->rtmp, rtmp_context->url);
    if (res == FALSE) {
        LOGE("Can't parse url'%s'", url);
        return -1;
    }

    return 0;
}

JNIEXPORT jint JNICALL
nativeConnect(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    int res = RTMP_Connect(rtmp_context->rtmp, nullptr);
    if (res == FALSE) {
        LOGE("Can't connect");
        return -1;
    }

    return 0;
}

JNIEXPORT jint JNICALL
nativeConnectStream(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    int res = RTMP_ConnectStream(rtmp_context->rtmp, 0);
    if (res == FALSE) {
        LOGE("Can't connect stream");
        return -1;
    }
    return 0;
}

JNIEXPORT void JNICALL
nativeDeleteStream(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    RTMP_DeleteStream(rtmp_context->rtmp);
}

JNIEXPORT void JNICALL
nativeEnableWrite(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    RTMP_EnableWrite(rtmp_context->rtmp);
}

JNIEXPORT jboolean JNICALL
nativeIsConnected(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    int isConnected = RTMP_IsConnected(rtmp_context->rtmp);
    return isConnected != 0;
}

JNIEXPORT void JNICALL
nativeSetTimeout(JNIEnv *env, jobject thiz, jint timeout) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    rtmp_context->rtmp->Link.timeout = timeout;
}

JNIEXPORT jint JNICALL
nativeGetTimeout(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    return rtmp_context->rtmp->Link.timeout;
}

JNIEXPORT jint JNICALL
nativePause(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    return RTMP_Pause(rtmp_context->rtmp, 1);
}

JNIEXPORT jint JNICALL
nativeResume(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    return RTMP_Pause(rtmp_context->rtmp, 0);
}

JNIEXPORT jint JNICALL
nativeWrite(JNIEnv *env, jobject thiz, jbyteArray data,
            jint offset, jint size) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    char *buf = (char *) env->GetByteArrayElements(data, nullptr);

    int res = RTMP_Write(rtmp_context->rtmp, &buf[offset], size);

    env->ReleaseByteArrayElements(data, (jbyte *) buf, 0);

    return res;
}

JNIEXPORT jint JNICALL
nativeWriteA(JNIEnv *env, jobject thiz, jobject buffer,
             jint offset, jint size) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    char *buf = (char *) env->GetDirectBufferAddress(buffer);

    int res = RTMP_Write(rtmp_context->rtmp, &buf[offset], size);

    return res;
}

JNIEXPORT jint JNICALL
nativeRead(JNIEnv *env, jobject thiz, jbyteArray data, jint offset,
           jint size) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    int dataLength = env->GetArrayLength(data);
    int res = -1;

    if (dataLength >= (offset + size)) {
        char *buf = reinterpret_cast<char *>(env->GetByteArrayElements(data, nullptr));
        res = RTMP_Read(rtmp_context->rtmp, &buf[offset], size);
        env->ReleaseByteArrayElements(data, reinterpret_cast<jbyte *>(buf), 0); // 0 - free buf
    }

    return res;
}

JNIEXPORT jint JNICALL
nativeWritePacket(JNIEnv *env, jobject thiz, jobject rtmpPacket) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    RTMPPacket *rtmp_packet = RtmpPacket::getNative(env, rtmpPacket);

    int res = RTMP_SendPacket(rtmp_context->rtmp, rtmp_packet, FALSE);
    if (res == FALSE) {
        LOGE("Can't write RTMP packet");
        return -1;
    }

    free(rtmp_packet);

    return 0;
}

JNIEXPORT jobject JNICALL
nativeReadPacket(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    RTMPPacket rtmp_packet = { 0 };

    int res = RTMP_ReadPacket(rtmp_context->rtmp, &rtmp_packet);
    if (res == FALSE) {
        LOGE("Can't read RTMP packet");
        return nullptr;
    }

    return RtmpPacket::getJava(env, rtmp_packet);
}

JNIEXPORT void JNICALL
nativeClose(JNIEnv *env, jobject thiz) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);

    if (rtmp_context->rtmp != nullptr) {
        RTMP_Close(rtmp_context->rtmp);
        RTMP_Free(rtmp_context->rtmp);
        rtmp_context->rtmp = nullptr;
    }
    if (rtmp_context->url != nullptr) {
        free(rtmp_context->url);
        rtmp_context->url = nullptr;
    }
}

JNIEXPORT jint JNICALL
nativeServe(JNIEnv *env, jobject thiz, jint fd) {
    rtmp_context *rtmp_context = RtmpWrapper::getNative(env, thiz);
    rtmp_context->rtmp->m_sb.sb_socket = fd;
    int ret = RTMP_Serve(rtmp_context->rtmp);
    if (ret == FALSE) {
        return -1;
    } else {
        return 0;
    }
}


static JNINativeMethod rtmpMethods[] = {
        {"nativeAlloc",         "()J",                        (void *) &nativeAlloc},
        {"nativeEnableWrite",   "()V",                        (void *) &nativeEnableWrite},
        {"nativeIsConnected",   "()Z",                        (void *) &nativeIsConnected},
        {"nativeSetTimeout",    "(I)V",                       (void *) &nativeSetTimeout},
        {"nativeGetTimeout",    "()I",                        (void *) &nativeGetTimeout},
        {"nativeSetupURL",      "(Ljava/lang/String;)I",      (void *) &nativeSetupURL},
        {"nativeConnect",       "()I",                        (void *) &nativeConnect},
        {"nativeConnectStream", "()I",                        (void *) &nativeConnectStream},
        {"nativeDeleteStream",  "()V",                        (void *) &nativeDeleteStream},
        {"nativePause",         "()I",                        (void *) &nativePause},
        {"nativeResume",        "()I",                        (void *) &nativeResume},
        {"nativeWrite",         "([BII)I",                    (void *) &nativeWrite},
        {"nativeWrite",         "(Ljava/nio/ByteBuffer;II)I", (void *) &nativeWriteA},
        {"nativeRead",          "([BII)I",                    (void *) &nativeRead},
        {"nativeWritePacket",   "(L" RTMP_PACKET_CLASS";)I",  (void *) &nativeWritePacket},
        {"nativeReadPacket",    "()L" RTMP_PACKET_CLASS";",   (void *) &nativeReadPacket},
        {"nativeClose",         "()V",                        (void *) &nativeClose},
        {"nativeServe",         "(I)I",                       (void *) &nativeServe}
};

JNIEXPORT jint JNICALL
nativeEncodeBoolean(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                    jboolean parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *newBuf = AMF_EncodeBoolean(&buf[offset], buf + end, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeInt24(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end, jint parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *newBuf = AMF_EncodeInt24(&buf[offset], buf + end, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeInt(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end, jint parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *newBuf = AMF_EncodeInt32(&buf[offset], buf + end, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeNumber(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                   jdouble parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *newBuf = AMF_EncodeNumber(&buf[offset], buf + end, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeString(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                   jstring jparameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *parameter = const_cast<char *>(env->GetStringUTFChars(jparameter, nullptr));
    AVal aVal = {0, 0};
    STR2AVAL(aVal, parameter);
    char *newBuf = AMF_EncodeString(&buf[offset], buf + end, &aVal);
    env->ReleaseStringUTFChars(jparameter, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeNamedBoolean(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                         jstring jname,
                         jboolean parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *name = const_cast<char *>(env->GetStringUTFChars(jname, nullptr));
    AVal av_name = {0, 0};
    STR2AVAL(av_name, name);
    char *newBuf = AMF_EncodeNamedBoolean(&buf[offset], buf + end, &av_name,
                                          parameter);
    env->ReleaseStringUTFChars(jname, name);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeNamedNumber(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                        jstring jname,
                        jdouble parameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *name = const_cast<char *>(env->GetStringUTFChars(jname, nullptr));
    AVal av_name = {0, 0};
    STR2AVAL(av_name, name);
    char *newBuf = AMF_EncodeNamedNumber(&buf[offset], buf + end, &av_name,
                                         parameter);
    env->ReleaseStringUTFChars(jname, name);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

JNIEXPORT jint JNICALL
nativeEncodeNamedString(JNIEnv *env, jclass cls, jobject buffer, jint offset, jint end,
                        jstring jname,
                        jstring jparameter) {
    char *buf = (char *) env->GetDirectBufferAddress(buffer);
    char *parameter = const_cast<char *>(env->GetStringUTFChars(jparameter, nullptr));
    AVal av_param = {0, 0};
    STR2AVAL(av_param, parameter);
    char *name = const_cast<char *>(env->GetStringUTFChars(jname, nullptr));
    AVal av_name = {0, 0};
    STR2AVAL(av_name, name);

    char *newBuf = AMF_EncodeNamedString(&buf[offset], buf + end, &av_name,
                                         &av_param);
    env->ReleaseStringUTFChars(jname, name);
    env->ReleaseStringUTFChars(jparameter, parameter);
    if (nullptr == newBuf) {
        return -1;
    } else {
        return newBuf - buf; // new position
    }
}

static JNINativeMethod amfEncoderMethods[] = {
        {"nativeEncodeBoolean",      "(Ljava/nio/ByteBuffer;IIZ)I",                                    (void *) &nativeEncodeBoolean},
        {"nativeEncodeInt",          "(Ljava/nio/ByteBuffer;III)I",                                    (void *) &nativeEncodeInt},
        {"nativeEncodeInt24",        "(Ljava/nio/ByteBuffer;III)I",                                    (void *) &nativeEncodeInt24},
        {"nativeEncodeNumber",       "(Ljava/nio/ByteBuffer;IID)I",                                    (void *) &nativeEncodeNumber},
        {"nativeEncodeString",       "(Ljava/nio/ByteBuffer;IILjava/lang/String;)I",                   (void *) &nativeEncodeString},
        {"nativeEncodeNamedBoolean", "(Ljava/nio/ByteBuffer;IILjava/lang/String;Z)I",                  (void *) &nativeEncodeNamedBoolean},
        {"nativeEncodeNamedNumber",  "(Ljava/nio/ByteBuffer;IILjava/lang/String;D)I",                  (void *) &nativeEncodeNamedNumber},
        {"nativeEncodeNamedString",  "(Ljava/nio/ByteBuffer;IILjava/lang/String;Ljava/lang/String;)I", (void *) &nativeEncodeNamedString}
};

// Register natives API

static int registerNativeForClassName(JNIEnv *env, const char *className,
                                      JNINativeMethod *methods, int methodsSize) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        LOGE("Unable to find class '%s'", className);
        return JNI_FALSE;
    }
    int res = 0;
    if ((res = env->RegisterNatives(clazz, methods, methodsSize)) < 0) {
        LOGE("RegisterNatives failed for '%s' (reason %d)", className, res);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

void rtmp_log_cb(int level, const char *format, va_list vl) {
    int android_log_level = ANDROID_LOG_UNKNOWN;

    switch (level) {
        case RTMP_LOGCRIT:
            android_log_level = ANDROID_LOG_FATAL;
            break;
        case RTMP_LOGERROR:
            android_log_level = ANDROID_LOG_ERROR;
            break;
        case RTMP_LOGWARNING:
            android_log_level = ANDROID_LOG_WARN;
            break;
        case RTMP_LOGINFO:
            android_log_level = ANDROID_LOG_INFO;
            break;
        case RTMP_LOGDEBUG:
            android_log_level = ANDROID_LOG_DEBUG;
            break;
        case RTMP_LOGDEBUG2:
        case RTMP_LOGALL:
            android_log_level = ANDROID_LOG_VERBOSE;
            break;
        default:
            LOGE("Unknown log level %d", level);
    }

    __android_log_vprint(android_log_level, TAG, format, vl);
}

jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    JNIEnv *env = nullptr;
    jint result;

    if ((result = vm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK) {
        LOGE("GetEnv failed");
        return result;
    }

    if ((registerNativeForClassName(env, RTMP_CLASS, rtmpMethods,
                                    sizeof(rtmpMethods) / sizeof(rtmpMethods[0])) != JNI_TRUE)) {
        LOGE("RegisterNatives for RTMP methods failed");
        return -1;
    }

    if ((registerNativeForClassName(env, AMF_ENCODER_CLASS, amfEncoderMethods,
                                    sizeof(amfEncoderMethods) / sizeof(amfEncoderMethods[0])) !=
         JNI_TRUE)) {
        LOGE("RegisterNatives for AMF encoder methods failed");
        return -1;
    }

    // Register Log
    RTMP_LogSetCallback(rtmp_log_cb);
    //RTMP_LogSetLevel(RTMP_LOGDEBUG);

    return JNI_VERSION_1_6;
}

