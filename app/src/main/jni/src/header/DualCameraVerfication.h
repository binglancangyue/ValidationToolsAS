#ifndef _DUALCAMERAVERIFICATION_H_
#define _DUALCAMERAVERIFICATION_H_

#ifndef __linux__
#define EXPORT_INTERFACE
#else
#define EXPORT_INTERFACE __attribute__((visibility("default")))
#endif

#define IMAGE_RGB_FORMAT  1
#define IMAGE_NV21_FORMAT 2

//#ifdef __cplusplus
extern "C" {
//#endif

typedef struct tagDualCameraVerficationConfig {
	int pattern_size_row;  //number of corners in row
	int pattern_size_col;  //number of corners in column
	double rms_th;         //threshold of rms. used to determine whether verification passes or not

	int width;			//widht of input image
	int height;			//height of input image
	int image_format;	//image format, IMAGE_RGB_FORMAT or IMAGE_NV21_FORMAT
	int width_calibration;			//width_calibration of input image to resize
	int height_calibration;			//height_calibration of input image to resize
	//rio.li, 171010
	double rms; //calculated reprojection rms
} DualCameraVerficationConfig;

EXPORT_INTERFACE int DualCameraVerfication(const unsigned char* image_left, const unsigned char* image_right, CalibParam* param, DualCameraVerficationConfig* dualCameraVerficationConfig);

//rio.li, 171010
EXPORT_INTERFACE double GetDualcameraVerificationRMS(double rmsSrc);
//#ifdef __cplusplus
}
//#endif

#endif
