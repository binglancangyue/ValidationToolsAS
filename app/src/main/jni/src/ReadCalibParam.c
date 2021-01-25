#include <stdio.h>
#include "header/CalibParam.h"
#include "header/typedef.h"

BOOL read_calibration_param(int8* calibration_file_name, CalibParam* param)
{
	FILE* pf;
	int32 i,j;
	int32 param_int;
	int32 PrecisionBits = 19;
	pf = fopen(calibration_file_name, "rt");
	if(NULL == pf) {
		printf("Cann't open calibration file: %s\n", calibration_file_name);
		return FALSE;
	}

	fscanf(pf, "%d\n", &param_int);
	param->cx_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->cy_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->fx_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->fy_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k1_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k2_left = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k3_left = (double)param_int/(1<<PrecisionBits);
	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			fscanf(pf, "%d\n", &param_int);
			param->R_left[i][j] = (double)param_int/(1<<PrecisionBits);
		}
	}
	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			fscanf(pf, "%d\n", &param_int);
			param->P_left[i][j] = (double)param_int/(1<<PrecisionBits);
		}
	}

	fscanf(pf, "%d\n", &param_int);
	param->cx_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->cy_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->fx_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->fy_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k1_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k2_right = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->k3_right = (double)param_int/(1<<PrecisionBits);
	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			fscanf(pf, "%d\n", &param_int);
			param->R_right[i][j] = (double)param_int/(1<<PrecisionBits);
		}
	}
	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			fscanf(pf, "%d\n", &param_int);
			param->P_right[i][j] = (double)param_int/(1<<PrecisionBits);
		}
	}

	fscanf(pf, "%d\n", &param_int);
	param->disparity_min = (param_int>>PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->disparity_max = (param_int>>PrecisionBits);

	fscanf(pf, "%d\n", &param_int);
	param->d_52 = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->d_53 = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->d_54 = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->d_55 = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->d_56 = (double)param_int/(1<<PrecisionBits);
	fscanf(pf, "%d\n", &param_int);
	param->d_57 = (double)param_int/(1<<PrecisionBits);

	fclose(pf);
	
	return TRUE;
}

BOOL read_calibration_param_int(int32 *OTP_params_output, CalibParam* param)
{
	int32 i,j,offset=0;
	int32 PrecisionBits = 19;

	param->cx_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->cy_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->fx_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->fy_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k1_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k2_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k3_left = (double)OTP_params_output[offset++]/(1<<PrecisionBits);

	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			param->R_left[i][j] = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
		}
	}

	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			param->P_left[i][j] = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
		}
	}

	param->cx_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->cy_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->fx_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->fy_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k1_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k2_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->k3_right = (double)OTP_params_output[offset++]/(1<<PrecisionBits);

	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			param->R_right[i][j] = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
		}
	}
	for (i = 0; i < 3; i ++)
	{
		for (j = 0; j < 3; j ++)
		{
			param->P_right[i][j] = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
		}
	}

//	param->disparity_min = (OTP_params_output[offset++]>>PrecisionBits);
//	param->disparity_max = (OTP_params_output[offset++]>>PrecisionBits);
//
//	param->d_52 = OTP_params_output[offset++];
//	param->d_53 = OTP_params_output[offset++];
//	param->d_54 = OTP_params_output[offset++];
//	param->d_55 = OTP_params_output[offset++];
//	param->d_56 = OTP_params_output[offset++];
//	param->d_57 = OTP_params_output[offset++];

	param->disparity_min = (OTP_params_output[offset++]>>PrecisionBits);
	param->disparity_max = (OTP_params_output[offset++]>>PrecisionBits);

	param->d_52 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->d_53 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->d_54 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->d_55 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->d_56 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);
	param->d_57 = (double)OTP_params_output[offset++]/(1<<PrecisionBits);

	return TRUE;
}

BOOL read_bin_calibration_param(const char* otp_original, CalibParam* param)
{
	int OTP_Data[57];

	FILE *fid = fopen(otp_original, "rb");
	if(!fid){
		printf("otp.txt load failed!\n");
		return FALSE;
	}

	fread(OTP_Data, sizeof(int), 57, fid);
	fclose(fid);

	read_calibration_param_int(OTP_Data, param);

	return TRUE;
}
