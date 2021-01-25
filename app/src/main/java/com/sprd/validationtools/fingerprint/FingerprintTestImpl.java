package com.sprd.validationtools.fingerprint;

import com.sprd.validationtools.fingerprint.microarray.IFactoryTestImpl;
import com.sprd.validationtools.fingerprint.microarray.ISprdFingerDetectListener;

public class FingerprintTestImpl implements IFactoryTestImpl {

	@Override
	public int factory_init() {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			ret = NativeFingerprint.factory_init();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int factory_exit() {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			ret = NativeFingerprint.factory_exit();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int spi_test() {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			ret = NativeFingerprint.spi_test();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int interrupt_test() {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			ret = NativeFingerprint.interrupt_test();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int deadpixel_test() {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			ret = NativeFingerprint.deadpixel_test();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public int finger_detect(ISprdFingerDetectListener listener) {
		// TODO Auto-generated method stub
		int ret = -1;
		try {
			if (listener != null) {
				ret = NativeFingerprint.finger_detect();
				listener.on_finger_detected(ret);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
	}

}
