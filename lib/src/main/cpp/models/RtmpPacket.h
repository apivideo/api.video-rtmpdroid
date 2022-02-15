#pragma once

#include <malloc.h>

#define RTMP_PACKET_CLASS "video/api/rtmpdroid/RtmpPacket"

class RtmpPacket {
public:
    static RTMPPacket *getNative(JNIEnv *env, jobject rtmpPacket) {
        jclass rtmpPacketClz = env->GetObjectClass(rtmpPacket);
        if (!rtmpPacketClz) {
            LOGE("Can't get RtmpPacket class");
            return nullptr;
        }

        jfieldID channelFieldID = env->GetFieldID(rtmpPacketClz, "channel", "I");
        if (!channelFieldID) {
            LOGE("Can't get channel field");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        jfieldID headerTypeFieldID = env->GetFieldID(rtmpPacketClz, "headerType", "I");
        if (!headerTypeFieldID) {
            LOGE("Can't get header type field");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        jfieldID packetTypeFieldID = env->GetFieldID(rtmpPacketClz, "packetType", "I");
        if (!packetTypeFieldID) {
            LOGE("Can't get rtmp_packet type field");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        jfieldID timestampFieldID = env->GetFieldID(rtmpPacketClz, "timestamp", "I");
        if (!timestampFieldID) {
            LOGE("Can't get timestamp field");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        jfieldID bufferFieldID = env->GetFieldID(rtmpPacketClz, "buffer", "Ljava/nio/ByteBuffer;");
        if (!bufferFieldID) {
            LOGE("Can't get body field");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        RTMPPacket *rtmp_packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
        if (rtmp_packet == nullptr) {
            LOGE("Not enough memory");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        rtmp_packet->m_nChannel = env->GetIntField(rtmpPacket, channelFieldID);
        rtmp_packet->m_headerType = env->GetIntField(rtmpPacket, headerTypeFieldID);
        rtmp_packet->m_packetType = env->GetIntField(rtmpPacket, packetTypeFieldID);
        rtmp_packet->m_nTimeStamp = 0;
        rtmp_packet->m_nInfoField2 = 0;
        rtmp_packet->m_hasAbsTimestamp = 0;
        jobject buffer = env->GetObjectField(rtmpPacket, bufferFieldID);
        rtmp_packet->m_body = (char *) env->GetDirectBufferAddress(buffer);
        rtmp_packet->m_nBodySize = env->GetDirectBufferCapacity(buffer);

        env->DeleteLocalRef(rtmpPacketClz);

        return rtmp_packet;
    }

    static jobject getJava(JNIEnv *env, const RTMPPacket rtmp_packet) {
        jclass rtmpPacketClz = env->FindClass(RTMP_PACKET_CLASS);
        if (!rtmpPacketClz) {
            LOGE("Can't find RtmpPacket class");
            return nullptr;
        }

        jmethodID rtmpPacketConstructor = env->GetMethodID(rtmpPacketClz, "<init>",
                                                           "(IIIILjava/nio/ByteBuffer;)V");
        if (!rtmpPacketConstructor) {
            LOGE("Can't get RtmpPacket constructor");
            env->DeleteLocalRef(rtmpPacketClz);
            return nullptr;
        }

        jobject buffer = env->NewDirectByteBuffer(rtmp_packet.m_body, rtmp_packet.m_nBodySize);
        jobject rtmpPacket = env->NewObject(rtmpPacketClz, rtmpPacketConstructor,
                                            rtmp_packet.m_nChannel,
                                            rtmp_packet.m_headerType, rtmp_packet.m_packetType,
                                            (int32_t) rtmp_packet.m_nTimeStamp, buffer);
        return rtmpPacket;
    }
};