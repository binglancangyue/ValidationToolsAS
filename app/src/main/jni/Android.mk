LOCAL_PATH:= $(call my-dir)

#validationtools invoke native methods

include $(CLEAR_VARS)
LOCAL_VENDOR_MODULE = true
LOCAL_MODULE        := libjni_validationtools

LOCAL_NDK_STL_VARIANT := stlport_static

LOCAL_C_INCLUDES += vendor/sprd/modules/libatci
#validationtools invoke native lib
LOCAL_SHARED_LIBRARIES := libcutils libutils liblog
LOCAL_SHARED_LIBRARIES += libatci
LOCAL_LDFLAGS        := -llog

LOCAL_CPPFLAGS += $(JNI_CFLAGS)
LOCAL_MULTILIB := both

LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES     := \
    src/jniutils.cpp \


include $(BUILD_SHARED_LIBRARY)

#fingerprint native
include $(CLEAR_VARS)
LOCAL_VENDOR_MODULE = true
LOCAL_MODULE        := libjni_fingerprint
#validationtools invoke native lib
LOCAL_SHARED_LIBRARIES := libcutils libutils liblog
LOCAL_LDFLAGS        := -llog
LOCAL_CPPFLAGS += $(JNI_CFLAGS)
LOCAL_MULTILIB := both
LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES     := \
    src/jniFingerutils.cpp \

include $(BUILD_SHARED_LIBRARY)

#dualcamera native
include $(CLEAR_VARS)

$(warning "libjni_dualcameraverify ")
LOCAL_VENDOR_MODULE = true
LOCAL_MODULE        := libjni_dualcameraverify
#validationtools invoke native lib
LOCAL_SHARED_LIBRARIES := libcutils libutils liblog
LOCAL_LDFLAGS        := -llog
LOCAL_CPPFLAGS += $(JNI_CFLAGS)
LOCAL_MULTILIB := both
LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES     := \
    src/jniDualcameraVrify.cpp \
    src/ImageFormat_Conversion.c \
    src/ReadCalibParam.c \

include $(BUILD_SHARED_LIBRARY)
