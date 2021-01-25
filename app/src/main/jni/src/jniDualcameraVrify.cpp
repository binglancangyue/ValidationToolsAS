/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "jniDualcameraVrify"
#include "utils/Log.h"

#include <stdint.h>
#include <jni.h>

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <malloc.h>
#include <dlfcn.h>
#include "header/CalibParam.h"
#include "header/DualCameraVerfication.h"
#include "header/ImageFormat_Conversion.h"
#include "header/ReadCalibParam.h"
#include "header/typedef.h"

#define ROOT_MAGIC 0x524F4F54 //"ROOT"
#define ROOT_OFFSET 512
#define MAX_COMMAND_BYTES               (8 * 1024)

void * filehandle = NULL;

typedef int (*getResult)(const char*, const char*, CalibParam*,
        DualCameraVerficationConfig*);
getResult getResultMethod;
/*SPRD bug 759782 : Display RMS value*/
typedef double (*getResultRMS)(double rmsSrc);
getResultRMS getResultRMSMethod;
double rms = 0.0;
/*@}*/
#define LIB_CACULATE_PATH "libCameraVerfication.so"

static jint JNIDualCameraVerfication(JNIEnv* env, jobject thiz,
        jstring image_left, jstring image_right, jstring otpfile) {
    const char *filename_left = env->GetStringUTFChars(image_left, 0);
    const char *filename_right = env->GetStringUTFChars(image_right, 0);
    const char *filename_otp = env->GetStringUTFChars(otpfile, 0);
    int result = -1;
    char *error;
    ALOGD("JNICameraVerifcation JNIDualCameraVerfication");
    filehandle = dlopen(LIB_CACULATE_PATH, RTLD_NOW);
    if (!filehandle) {
        ALOGD("JNICameraVerifcation stderr:%s dlerror:%s\n", stderr, dlerror());
        return result;
    }
	ALOGD("JNICameraVerifcation JNIDualCameraVerfication2");
	getResultMethod = (getResult) dlsym(filehandle, "DualCameraVerfication");
	if (!getResultMethod) {
		ALOGD("JNICameraVerifcation dlsym stderr:%s dlerror:%s\n", stderr,
				dlerror());
		return result;
	}
	ALOGD("JNICameraVerifcation DualCameraVerfication getResultMethod done");

	int width = 1600;
	int height = 1200;
	char *YUVImage_left, *YUVImage_right;
	YUVImage_left = new char[height * width * 3 / 2];
	YUVImage_right = new char[height * width * 3 / 2];
	const char* left = filename_left;
	const char* right = filename_right;

	ReadYUV420File(left, width, height, YUVImage_left);
	ReadYUV420File(right, width, height, YUVImage_right);

	CalibParam calibParam;
	DualCameraVerficationConfig dualCameraVerficationConfig = { 11, 7, 1.5,
			width, height, IMAGE_NV21_FORMAT, 800, 600, 0.0 };
	dualCameraVerficationConfig.pattern_size_row = 11; //8;
	dualCameraVerficationConfig.pattern_size_col = 7; //5;
	dualCameraVerficationConfig.rms_th = 1.5;
	dualCameraVerficationConfig.width = width;
	dualCameraVerficationConfig.height = height;
	dualCameraVerficationConfig.image_format = IMAGE_NV21_FORMAT;
	dualCameraVerficationConfig.width_calibration = 800;
	dualCameraVerficationConfig.height_calibration = 600;
	dualCameraVerficationConfig.rms = 0.0;

	read_bin_calibration_param((char*) (filename_otp), &calibParam);
	ALOGD("JNICameraVerifcation DualCameraVerfication JNIDualCameraVerfication 4");

	/*if (getResultMethod) {
	 result = getResultMethod(YUVImage_left, YUVImage_right, &calibParam,
	 &dualCameraVerficationConfig);
	 }*/
	result = getResultMethod(YUVImage_left, YUVImage_right, &calibParam,
			&dualCameraVerficationConfig);
	ALOGD("JNICameraVerifcation DualCameraVerfication result: %d", result);
	//
	getResultRMSMethod = (getResultRMS) dlsym(filehandle,
			"GetDualcameraVerificationRMS");
	if (!getResultRMSMethod) {
		ALOGD("JNICameraVerifcation dlsym stderr:%s dlerror:%s\n", stderr,
				dlerror());
		return result;
	}
	/*SPRD bug 759782 : Display RMS value*/
	rms = getResultRMSMethod(dualCameraVerficationConfig.rms);
	ALOGD("JNICameraVerifcation DualCameraVerfication rms: %f", rms);
	/*@}*/
	//dlclose(filehandle);
	//filehandle = NULL;
	delete[] YUVImage_left;
	delete[] YUVImage_right;
    ALOGD("JNICameraVerifcation JNIDualCameraVerfication result:%d\n", result);
    return result;
}

/*SPRD bug 759782 : Display RMS value*/
static jdouble JNIGetCameraVerficationRMS(JNIEnv* env, jobject thiz) {
    return rms;
}
/*@}*/

static const char *hardWareClassPathName =
        "com/sprd/validationtools/camera/NativeCameraCalibration";

static JNINativeMethod getMethods[] = { { "native_dualCameraVerfication",
		"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I",
		(void*) JNIDualCameraVerfication }, { "native_getCameraVerficationRMS",
		"()D", (void*) JNIGetCameraVerficationRMS } };

static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    //use JNI1.6
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        ALOGE("Error: GetEnv failed in JNI_OnLoad");
        return -1;
    }
    if (!registerNativeMethods(env, hardWareClassPathName, getMethods,
            sizeof(getMethods) / sizeof(getMethods[0]))) {
        ALOGE("Error: could not register native methods for HardwareFragment");
        return -1;
    }
    return JNI_VERSION_1_6;
}
