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

#define LOG_TAG "jniFingerutils"
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

#include <stdio.h>
#include <unistd.h>
#include <dlfcn.h>

#define LIB_CACULATE_PATH "libfactorylib.so"

typedef int (*CAC_FUNC)(void);

void *handle;
int ret = -1;
int test_result = -1;
int times = 1;

CAC_FUNC factory_init = NULL;
CAC_FUNC spi_test = NULL;
CAC_FUNC interrupt_test = NULL;
CAC_FUNC deadpixel_test = NULL;
CAC_FUNC finger_detect = NULL;
CAC_FUNC factory_exit = NULL;

static jint Java_NativeFingerprint_factory_init(JNIEnv* env, jobject thiz) {
	ALOGI("factory_init!");
	handle = dlopen(LIB_CACULATE_PATH, RTLD_LAZY);
	if (!handle) {
		ALOGI("fingersor lib dlopen failed! %s, %d IN\n", dlerror(), __LINE__);
		return -1;
	}
	*(void **) (&factory_init) = dlsym(handle, "factory_init");
	if (!factory_init) {
		ALOGI("could not find symbol 'factory_init', %d IN\n", __LINE__);
		return -1;
	} else {
		ret = (*factory_init)();
		if (ret != 0) {
			ALOGI("factory_init fail, ret = %d\n", ret);
			return -1;
		} else {
			ALOGI("factory_init SUCCESS ----->>>>>>, ret = %d\n", ret);
		}
	}
	return ret;
}

static jint Java_NativeFingerprint_factory_exit(JNIEnv* env, jobject thiz) {
	ALOGI("factory_exit");
	*(void **) (&factory_exit) = dlsym(handle, "factory_exit");
	if (!factory_exit) {
		ALOGI("could not find symbol 'factory_exit', %d IN\n", __LINE__);
		return -1;
	} else {
		ret = (*factory_exit)();
		if (ret != 0) {
			ALOGI("factory_exit fail, ret = %d\n", ret);
			return -1;
		}
	}
	return ret;
}

static jint Java_NativeFingerprint_spi_test(JNIEnv* env, jobject thiz) {
	ALOGI("spi_test");
	*(void **) (&spi_test) = dlsym(handle, "spi_test");
	if (!spi_test) {
		ALOGI("could not find symbol 'spi_test', %d IN\n", __LINE__);
		return -1;
	} else {
		ret = (*spi_test)();
		if (ret != 0) {
			ALOGI("spi_test fail, ret = %d\n", ret);
			return -1;
		}
	}
	return ret;
}

static jint Java_NativeFingerprint_deadpixel_test(JNIEnv* env, jobject thiz) {
	ALOGI("deadpixel_test");
	*(void **) (&deadpixel_test) = dlsym(handle, "deadpixel_test");
	if (!deadpixel_test) {
		ALOGI("could not find symbol 'deadpixel_test', %d IN\n", __LINE__);
		return -1;
	} else {
		ret = (*deadpixel_test)();
		if (ret != 0) {
			ALOGI("deadpixel_test fail, ret = %d\n", ret);
			return -1;
		}
	}
	return ret;
}

static jint Java_NativeFingerprint_interrupt_test(JNIEnv* env, jobject thiz) {
	ALOGI("interrupt_test");
	*(void **) (&interrupt_test) = dlsym(handle, "interrupt_test");
	if (!interrupt_test) {
		ALOGI("could not find symbol 'interrupt_test', %d IN\n", __LINE__);
		return -1;
	} else {
		ret = (*interrupt_test)();
		if (ret != 0) {
			ALOGI("interrupt_test fail, ret = %d\n", ret);
			return -1;
		}
	}
	return ret;
}

static jint Java_NativeFingerprint_finger_detect(JNIEnv* env, jobject thiz) {
	ALOGI("finger_detect");
	times = 100;
	*(void **) (&finger_detect) = dlsym(handle, "finger_detect");
	if (!finger_detect) {
		ALOGI("could not find symbol 'finger_detect', %d IN\n", __LINE__);
		return -1;
	} else {
		while (times > 0) {
			ret = (*finger_detect)();
			if (ret == 0) {
				break;
			}
			times--;
			ALOGI("not detect fingerprint, ret = %d, try again...\n", ret);
			usleep(1000 * 100);
		}

		if (ret != 0) {
			ALOGI("finger_detect several times but failed\n");
			return -1;
		}
	}
	return ret;
}

static const char *hardWareClassPathName =
		"com/sprd/validationtools/fingerprint/NativeFingerprint";

static JNINativeMethod getMethods[] = { { "factory_init", "()I",
		(void*) Java_NativeFingerprint_factory_init }, { "factory_exit", "()I",
		(void*) Java_NativeFingerprint_factory_exit }, { "spi_test", "()I",
		(void*) Java_NativeFingerprint_spi_test }, { "deadpixel_test", "()I",
		(void*) Java_NativeFingerprint_deadpixel_test }, { "interrupt_test",
		"()I", (void*) Java_NativeFingerprint_interrupt_test }, {
		"finger_detect", "()I", (void*) Java_NativeFingerprint_finger_detect } };

static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		ALOGE("Native registration unable to find class '%s'", className);
		return JNI_FALSE;
	}
	ALOGE("numMethods'%d'", numMethods);
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
