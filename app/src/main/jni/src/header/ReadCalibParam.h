#ifndef _READ_CALIB_PARAM_H_
#define _READ_CALIB_PARAM_H_

#ifdef __cplusplus
extern "C"
{
#endif

#include "CalibParam.h"
#include "typedef.h"


BOOL read_calibration_param(int8* calibration_file_name, CalibParam* param);
BOOL read_calibration_param_int(int *OTP_params_output, CalibParam* param);
BOOL read_bin_calibration_param(const char* otp_original, CalibParam* param);
#ifdef __cplusplus
}
#endif

#endif
