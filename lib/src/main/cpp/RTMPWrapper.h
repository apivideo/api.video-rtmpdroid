#pragma once

#include "librtmp/rtmp.h"
#include "Log.h"

class RTMPWrapper {
public:
    static RTMP *getNative(JNIEnv *env, jobject rtmpContext) {
        jclass rtmpContextClazz = env->GetObjectClass(rtmpContext);
        if (!rtmpContextClazz) {
            LOGE("Can't get RTMP context class");
            return nullptr;
        }

        jfieldID rtmpContextFieldId = env->GetFieldID(rtmpContextClazz, "ptr", "J");
        if (!rtmpContextFieldId) {
            LOGE("Can't get ptr field");
            env->DeleteLocalRef(rtmpContextClazz);
            return nullptr;
        }

        RTMP *rtmp = reinterpret_cast<RTMP *>(env->GetLongField(rtmpContext, rtmpContextFieldId));

        env->DeleteLocalRef(rtmpContextClazz);

        return rtmp;
    }
};