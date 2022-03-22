#pragma once

#include "../Log.h"
#include "RtmpContext.h"

using namespace std;

class RtmpWrapper {
public:
    static rtmp_context *getNative(JNIEnv *env, jobject wrapperContext) {
        jclass wrapperContextClazz = env->GetObjectClass(wrapperContext);
        if (!wrapperContextClazz) {
            LOGE("Can't get RTMP wrapper class");
            return nullptr;
        }

        jfieldID wrapperContextFieldId = env->GetFieldID(wrapperContextClazz, "ptr", "J");
        if (!wrapperContextFieldId) {
            LOGE("Can't get ptr field");
            env->DeleteLocalRef(wrapperContextClazz);
            return nullptr;
        }

        rtmp_context *rtmp_context = reinterpret_cast<struct rtmp_context *>(env->GetLongField(wrapperContext, wrapperContextFieldId));

        env->DeleteLocalRef(wrapperContextClazz);

        return rtmp_context;
    }
};