
package com.sprd.validationtools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.SystemProperties;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

/*Parse the phasecheck as the little endian*/
public class PhaseCheckParse {
    private static String TAG = "PhaseCheckParse";
    private static int MAX_SN_LEN = 24;

    private static int TYPE_STATION_UNKNOWN = -1;
    private static int TYPE_STATION_SP05 = 0;
    private static int TYPE_STATION_SP09 = 1;
    private static int TYPE_STATION_SP15 = 2;
    private static int TYPE_STATION_MAX = 3;

    class StationParam {
        private int sizeofInt = 4;
        public int magic_number;
        public int max_sn_len;
        public int max_station_num;
        public int max_station_name_len;
        public int max_last_description_len;
        public int sn1_start_index;
        public int sn2_start_index;
        public int station_start_index;
        public int testflag_start_index;
        public int result_start_index;

        public StationParam(int magic, int sn_len, int station_num, int station_name_len, int last_description_len) {
            magic_number = magic;
            max_sn_len = sn_len;
            max_station_num = station_num;
            max_station_name_len = station_name_len;
            max_last_description_len = last_description_len;
            sn1_start_index = sizeofInt;
            sn2_start_index = sn1_start_index+max_sn_len;
            station_start_index = sizeofInt/*(magic)*/ + sn_len*2/*(sn)*/ + sizeofInt/*stationNum*/;
            testflag_start_index = station_start_index + max_station_num*max_station_name_len/*station name*/ +
                                    14/*Reserved && SignFlag*/ + max_last_description_len/*last description*/;
            result_start_index = testflag_start_index+2/*test flag*/;
        }
    }

    private StationParam[] stParam =
    {
        //SP05
        new StationParam(0x53503035, 24, 15, 10, 32),
        //SP09
        new StationParam(0x53503039, 24, 15, 10, 32),
        //SP15
        new StationParam(0x53503135, 64, 20, 15, 32)
    };

    private static int TYPE_GET_SN1 = 0;
    private static int TYPE_GET_SN2 = 1;
    private static int TYPE_WRITE_STATION_TESTED = 2;
    private static int TYPE_WRITE_STATION_PASS = 3;
    private static int TYPE_WRITE_STATION_FAIL = 4;
    private static int TYPE_GET_PHASECHECK = 5;
    private static int TYPE_WRITE_CHARGE_SWITCH = 6;

    private static int TYPE_WRITE_OFFSET = 20;
    private static int TYPE_READ_OFFSET = 21;
    private int stationType = TYPE_STATION_SP09;
    private static String PHASE_CHECKE_FILE = "miscdata";
    private static int BUF_SIZE = 4096;

    private byte[] stream = new byte[300];
    private AdaptBinder binder;

    public PhaseCheckParse() {
        if (!checkPhaseCheck()) {
            stream = null;
        }

        try {
            binder = new AdaptBinder();

            if(binder != null)
                Log.e(TAG, "Get The service connect!");
            else
                Log.e(TAG, "connect Error!!");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private boolean hasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches())
            flag = true;
        return flag;
    }

    /*SPRD: Add for bug556367, Equipment serial number are unintelligible string {@ */
    private boolean isInvalid(String content) {
        boolean flag = true;
        Pattern p = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher m = p.matcher(content);
        if (m.matches())
            flag = false;
        return flag;
    }
    /* {@ */

    private String StationTested(char testSign, char item) {
        if(testSign=='0' && item=='0') return "PASS";
        if(testSign=='0' && item=='1') return "FAIL";
        return "UnTested";
    }

    private boolean checkPhaseCheck() {
        Log.d(TAG, "checkPhaseCheck: " + stream[0] + stream[1] + stream[2] + stream[3]);
        if (stream[0] == '5' && stream[1] == '0' && stream[2] == 'P' && stream[3] == 'S') {
            stationType = TYPE_STATION_SP05;
        }else if (stream[0] == '9' && stream[1] == '0' && stream[2] == 'P' && stream[3] == 'S') {
            stationType = TYPE_STATION_SP09;
        }else if(stream[0] == '5' && stream[1] == '1' && stream[2] == 'P' && stream[3] == 'S') {
            stationType = TYPE_STATION_SP15;
        }else {
            return false;
        }

        Log.d(TAG, "stationType = "+stationType);
        return true;
    }

    private int getStationTest(String station_name) {
        Log.d(TAG, "getStationTest: "+station_name);
        if (stationType < TYPE_STATION_SP05 || stationType > TYPE_STATION_MAX ) {
            Log.d(TAG, "getStationTest return err:  stationType = "+stationType);
            return -1;
        }

        int ret = -1;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(TYPE_GET_PHASECHECK, data, reply, 0);
            Log.e(TAG, "transact SUCESS!!");
            int testSign = reply.readInt();
            int item = reply.readInt();
            String stationName = reply.readString();
            String []str = stationName.split(Pattern.quote("|"));
            String strTestSign = Integer.toBinaryString(testSign);
            String strItem = Integer.toBinaryString(item);
            char[] charSign = strTestSign.toCharArray();
            char[] charItem = strItem.toCharArray();

            Log.e(TAG, "strTestSign = " + strTestSign + " strItem = " + strItem);
            for(int i=0; i<str.length; i++) {
                Log.e(TAG, "str =  "+str[i]);
                if (station_name.equalsIgnoreCase(str[i])) {
                    ret = i;
                }
            }

            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "huasong Exception " + ex.getMessage());
            return -1;
        }

        Log.d(TAG, "getStationTest return "+ret);
        return ret;
    }

    public String getSn() {
        String result = null;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(0, data, reply, 0);
            Log.e(TAG, "transact end");
            String sn1 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn1)) {
                    break;
                }
                binder.transact(TYPE_GET_SN1, data, reply, 0);
                sn1 = reply.readString();
            }
            binder.transact(TYPE_GET_SN2, data, reply, 0);
            String sn2 = reply.readString();
            for(int i = 0; i < 5; i++) {
                if(hasDigit(sn2)) {
                    break;
                }
                binder.transact(1, data, reply, 0);
                sn2 = reply.readString();
            }
            if(!sn1.isEmpty() && sn1.length() > 24 && !sn2.isEmpty() && sn2.length() > 0) {
                /*SPRD bug 838344:Read sn issue.*/
                if(sn1.length() > sn2.length() && sn1.contains(sn2)){
                    sn1 = sn1.substring(0, sn1.length() - sn2.length());
                    Log.e(TAG, "sn1 contains sn2 ,SN1 = " +  sn1 + "\n SN2=" + sn2);
                }
                /*@}*/
            }
            result = "SN1:" + sn1 + "\n" + "SN2:" + sn2;
            Log.e(TAG, "SN1 = " +  sn1 + " SN2=" + sn2);
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "Exception :" + ex);
            ex.printStackTrace();
            result = "get SN fail:" + ex.getMessage();
        }
        return result;
    }

    public boolean writeStationTested(int station) {
        try{
            Parcel data = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_TESTED, data, null, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationTested(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationTested(stat);
        }
    }

    public boolean writeStationPass(int station) {
        try{
            Parcel data = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_PASS, data, null, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationPass(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationPass(stat);
        }
    }

    public boolean writeChargeSwitch(int value) {
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            data.writeInt(value);
            binder.transact(TYPE_WRITE_CHARGE_SWITCH, data, reply, 0);
            Log.e(TAG, "writeChargeSwitch data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " , ex);
            return false;
        }
    }

    public boolean writeStationFail(int station) {
        try{
            Parcel data = new Parcel();
            data.writeInt(station);
            binder.transact(TYPE_WRITE_STATION_FAIL, data, null, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }

    public boolean writeStationFail(String station) {
        int stat = getStationTest(station);
        Log.e(TAG, "stat = " + stat);
        if (stat == -1) {
            return false;
        }else {
            return writeStationFail(stat);
        }
    }

    /*public byte readOffsetValue(int offset) {
        byte value = (byte)0xFF;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            data.writeInt(offset);
            binder.transact(TYPE_READ_OFFSET, data, reply, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            value = reply.readByteArray(value);
            Log.e(TAG, "value = "+value);
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
        }

        return value;
    }

    public boolean writeOffsetValue(int offset, byte value) {
        try{
            Parcel data = new Parcel();
            data.writeInt(offset);
            //data.writeByte(value);
            data.writeByteArray(b, offset, len)
            binder.transact(TYPE_WRITE_OFFSET, data, null, 0);
            Log.e(TAG, "data = " + data.readString() + " SUCESS!!");
            data.recycle();
            return true;
        }catch (Exception ex) {
            Log.e(TAG, "Exception " + ex.getMessage());
            return false;
        }
    }*/

    public String getPhaseCheck() {
        String result = null;
        try{
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            binder.transact(TYPE_GET_PHASECHECK, data, reply, 0);
            Log.e(TAG, "transact SUCESS!!");
            int testSign = reply.readInt();
            int item = reply.readInt();
            String stationName = reply.readString();
            String []str = stationName.split(Pattern.quote("|"));
            String strTestSign = Integer.toBinaryString(testSign);
            String strItem = Integer.toBinaryString(item);
            char[] charSign = strTestSign.toCharArray();
            char[] charItem = strItem.toCharArray();
            StringBuffer sb = new StringBuffer();
            Log.e(TAG, "strTestSign = " + strTestSign + " strItem = " + strItem);
            for(int i=0; i<str.length; i++) {
                sb.append(str[i]+":"+StationTested(charSign[charSign.length-i-1], charItem[charItem.length-i-1])+"\n");
            }
            result = sb.toString();
            data.recycle();
            reply.recycle();
        }catch (Exception ex) {
            Log.e(TAG, "huasong Exception " + ex.getMessage());
            result = "get phasecheck fail:" + ex.getMessage();
        }
        return result;
    }

    public String getSn1() {
        if (stream == null && !(stationType >= TYPE_STATION_SP05 && stationType < TYPE_STATION_MAX )) {
            return "Invalid Sn1!";
        }
        if (!isAscii(stream[stParam[stationType].sn1_start_index])) {
            Log.d(TAG, "Invalid Sn1!");
            return "Invalid Sn1!";
        }

        String sn1 = new String(stream, stParam[stationType].sn1_start_index, stParam[stationType].max_sn_len);
        Log.d(TAG, sn1);
        return sn1;
    }

    public String getSn2() {
        if (stream == null && !(stationType >= TYPE_STATION_SP05 && stationType < TYPE_STATION_MAX )) {
            return "Invalid Sn2!";
        }
        if (!isAscii(stream[stParam[stationType].sn2_start_index])) {
            Log.d(TAG, "Invalid Sn2!");
            return "Invalid Sn2!";
        }
        String sn2 = new String(stream, stParam[stationType].sn2_start_index, stParam[stationType].max_sn_len);
        Log.d(TAG, sn2);
        return sn2;
    }

    private boolean isAscii(byte b) {
        if (b >= 0 && b <= 127) {
            return true;
        }
        return false;
    }

    public String getTestsAndResult() {
        if (stream == null) {
            return "Invalid Phase check!";
        }
        Log.d(TAG, "getTestsAndResult stationType = " + stationType);
        if(stationType < 0){
            return "Invalid Phase check!";
        }
        if (!isAscii(stream[stParam[stationType].station_start_index])) {
            Log.d(TAG, "Invalid Phase check!");
            return "Invalid Phase check!";
        }
        String testResult = null;
        String allResult = "";

        int flag = 1;
        for (int i = 0; i < stParam[stationType].max_station_num; i++) {
            if (0 == stream[stParam[stationType].station_start_index + i * stParam[stationType].max_station_name_len]) {
                Log.d(TAG, "break " + i);
                break;
            }
            testResult = new String(stream, stParam[stationType].station_start_index + i * stParam[stationType].max_station_name_len,
                    stParam[stationType].max_station_name_len);
            if (!isStationTest(i)) {
                testResult += " Not test";
            } else if (isStationPass(i)) {
                testResult += " Pass";
            } else {
                testResult += " Failed";
            }
            flag = flag << 1;
            Log.d(TAG, i + " " + testResult);
            allResult += testResult + "\n";
        }
        return allResult;
    }

    private boolean isStationTest(int station) {
        Log.d(TAG, "isStationTest1 stationType = " + stationType);
        if(stationType < 0){
            return false;
        }
        byte flag = 1;
        if (station < 8) {
            return (0 == ((flag << station) & stream[stParam[stationType].testflag_start_index]));
        } else if (station >= 8 && station < 16) {
            return (0 == ((flag << (station - 8)) & stream[stParam[stationType].testflag_start_index + 1]));
        }
        return false;
    }

    public boolean isStationTest(String stat) {
        byte flag = 1;
        int station = getStationTest(stat);
        Log.e(TAG, "station = " + station);
        if (station == -1) {
          return false;
        }
        Log.d(TAG, "isStationTest2 stationType = " + stationType);
        if(stationType < 0){
            return false;
        }
        if (station < 8) {
            return (0 == ((flag << station) & stream[stParam[stationType].testflag_start_index]));
        } else if (station >= 8 && station < 16) {
            return (0 == ((flag << (station - 8)) & stream[stParam[stationType].testflag_start_index + 1]));
        }
        return false;
    }

    private boolean isStationPass(int station) {
        byte flag = 1;
        Log.d(TAG, "isStationPass1 stationType = " + stationType);
        if(stationType < 0){
            return false;
        }
        if (station < 8) {
            return (0 == ((flag << station) & stream[stParam[stationType].result_start_index]));
        } else if (station >= 8 && station < 16) {
            return (0 == ((flag << (station - 8)) & stream[stParam[stationType].result_start_index + 1]));
        }
        return false;
    }

    public boolean isStationPass(String stat) {
        byte flag = 1;
        int station = getStationTest(stat);
        Log.e(TAG, "station = " + station);
        if (station == -1) {
          return false;
        }
        Log.d(TAG, "isStationPass2 stationType = " + stationType);
        if(stationType < 0){
            return false;
        }

        if (station < 8) {
            return (0 == ((flag << station) & stream[stParam[stationType].result_start_index]));
        } else if (station >= 8 && station < 16) {
            return (0 == ((flag << (station - 8)) & stream[stParam[stationType].result_start_index + 1]));
        }
        return false;
    }

    /* SPRD: 435125 The serial number shows invalid in ValidationTools @{*/
    public static String getSerialNumber(){
        return android.os.Build.SERIAL;
    }
    /* @}*/

    public boolean writeLedlightSwitch(int code, int value) {
        try {
            Parcel data = new Parcel();
            Parcel reply = new Parcel();
            Log.e(TAG, "writeLedlightSwitch light code = " + code+",value="+value);
            logLedLight(code);
            data.writeInt(value);
            binder.transact(code, data, reply, 0);
            Log.e(TAG, "writeLedlightSwitch light data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Exception ", ex);
            return false;
        }
    }
    private void logLedLight(int code){
        switch (code) {
        case 7:
            Log.d(TAG, "Blue light!");
            break;
        case 8:
            Log.d(TAG, "Blue light!");
            break;
        case 9:
            Log.d(TAG, "Blue light!");
            break;

        default:
            Log.d(TAG, "Unknow light!");
            break;
        }
    }

    class AdaptParcel {
        int code;
        int dataSize;
        int replySize;
        byte[] data;
    }

    private static String SOCKET_NAME = "phasecheck_srv";
    class AdaptBinder {
        private LocalSocket socket = new LocalSocket();
        private LocalSocketAddress socketAddr = new LocalSocketAddress(SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT);
        private OutputStream mOutputStream;
        private InputStream mInputStream;
        private AdaptParcel mAdpt;

        public AdaptBinder() {
            mAdpt = new AdaptParcel();
            mAdpt.data = new byte[BUF_SIZE];
            mAdpt.code = 0;
            mAdpt.dataSize = 0;
            mAdpt.replySize = 0;
        }

        private void int2byte(byte[] dst, int offset, int value) {
            dst[offset+3] = (byte)(value >> 24 & 0xff);
            dst[offset+2] = (byte)(value >> 16 & 0xff);
            dst[offset+1] = (byte)(value >> 8 & 0xff);
            dst[offset] = (byte)(value & 0xff);
        }

        public int byte2Int(byte[] bytes, int off) {
            int b0 = bytes[off] & 0xFF;
            int b1 = bytes[off + 1] & 0xFF;
            int b2 = bytes[off + 2] & 0xFF;
            int b3 = bytes[off + 3] & 0xFF;
            return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
        }

        public synchronized void sendCmdAndRecResult(AdaptParcel adpt) {
            Log.d(TAG, "send cmd: ");
            //LogArray(adpt.data, 19);
            byte[] buf = new byte[BUF_SIZE];
            int2byte(buf, 0, adpt.code);
            int2byte(buf, 4, adpt.dataSize);
            int2byte(buf, 8, adpt.replySize);

            //LogArray(adpt.data, 19);
            System.arraycopy(adpt.data, 0, buf, 12, adpt.dataSize+adpt.replySize);
            Log.d(TAG, "code = "+adpt.code);
            Log.d(TAG, "dataSize = "+adpt.dataSize);
            Log.d(TAG, "replySize = "+adpt.replySize);
            //LogArray(buf, 19);

            try {
                socket = new LocalSocket();
                if (!socket.isConnected()) {
                    Log.d(TAG, "isConnected...");
                    socket.connect(socketAddr);
                }

                Log.d(TAG, "mSocketClient connect is " + socket.isConnected());
                mOutputStream = socket.getOutputStream();
                if (mOutputStream != null) {
                    Log.d(TAG, "write...");
                    mOutputStream.write(buf);
                    mOutputStream.flush();
                    Log.d(TAG, "write succ...");
                }
                mInputStream = socket.getInputStream();
                Log.d(TAG, "read ....");
                int count = mInputStream.read(buf, 0, BUF_SIZE);
                Log.d(TAG, "count = " + count + "");
                //LogArray(buf, 19);

                adpt.code = byte2Int(buf, 0);
                adpt.dataSize = byte2Int(buf, 4);
                adpt.replySize = byte2Int(buf, 8);

                Log.d(TAG, "code = "+adpt.code);
                Log.d(TAG, "dataSize = "+adpt.dataSize);
                Log.d(TAG, "replySize = "+adpt.replySize);

                System.arraycopy(buf, 12, adpt.data, 0, adpt.dataSize+adpt.replySize);

                //LogArray(adpt.data, 19);

            } catch (IOException e) {
                Log.e(TAG, "Failed get output stream: " + e.toString());
                return ;
            } finally {
                try {
                    buf = null;
                    if (mOutputStream != null) {
                        mOutputStream.close();
                    }
                    if (mInputStream != null) {
                        mInputStream.close();
                    }
                    if (socket != null) {
                        if (socket.isConnected()) {
                            socket.close();
                            socket = null;
                        } else {
                            socket = null;
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "catch exception is " + e);
                    return ;
                }
            }
        }

        private void LogArray(byte[] b, int j){
            Log.e(TAG, "array length = "+b.length);
            for(int i = 0; i < b.length; i++){
                if (i > j) break;
                Log.e(TAG, "("+i+") = "+b[i]);
            }
        }

        private void convertParcel(AdaptParcel adpt, int code, Parcel data, Parcel reply) {
            data.setDataPosition(0);
            reply.setDataPosition(0);

            code = adpt.code;
            data.writeByteArrayInternal(adpt.data, 0, adpt.dataSize);
            reply.writeByteArrayInternal(adpt.data, adpt.dataSize, adpt.replySize);

            Log.e(TAG, "convertParcel: dataSize = "+data.dataSize()+", replySize = "+ reply.dataSize());
            //Log.e(TAG, "data = "+adpt.data);
            //LogArray(adpt.data, 19);

            data.setDataPosition(0);
            reply.setDataPosition(0);
        }

        /*private void convertAdaptParcel(AdaptParcel adpt, int code, Parcel data, Parcel reply) {
            adpt.code = code;

            data.setDataPosition(0);
            reply.setDataPosition(0);

            data.LogArray();
            byte[] bData = new byte[data.dataSize()];
            data.readByteArray(bData);
            for(int i = 0; i < data.dataSize(); i++){
                adpt.data[i] = bData[i];
            }

            byte[] bReply = new byte[reply.dataSize()];
            reply.readByteArray(bReply);
            for(int i = 0; i < reply.dataSize(); i++){
                adpt.data[i+data.dataSize()] = bReply[i];
            }

            Log.e(TAG, "convertAdaptParcel: dataSize = "+data.dataSize()+", replySize = "+ reply.dataSize());
            LogArray(adpt.data, 19);

            data.setDataPosition(0);
            reply.setDataPosition(0);
        }*/

        private void convertAdaptParcel(int code, Parcel data, Parcel reply) {
            if(mAdpt == null){
                Log.e(TAG, "convertAdaptParcel2: mAdpt == null!");
            }
            mAdpt.code = code;

            data.setDataPosition(0);
            reply.setDataPosition(0);

            data.LogArray();
            byte[] bData = new byte[data.dataSize()];
            data.readByteArray(bData);
            for(int i = 0; i < data.dataSize(); i++){
                mAdpt.data[i] = bData[i];
            }

            byte[] bReply = new byte[reply.dataSize()];
            reply.readByteArray(bReply);
            for(int i = 0; i < reply.dataSize(); i++){
                mAdpt.data[i+data.dataSize()] = bReply[i];
            }
            mAdpt.dataSize = data.dataSize();
            mAdpt.replySize = reply.dataSize();
            Log.e(TAG, "convertAdaptParcel2: dataSize = "+data.dataSize()+", replySize = "+ reply.dataSize());
            //LogArray(mAdpt.data, 19);

            data.setDataPosition(0);
            reply.setDataPosition(0);
        }

        public void transact(int code, Parcel data, Parcel reply, int flags) throws Exception {
            Log.e(TAG, "transact start....");

            InputStream is = null;
            OutputStream out = null;

//            convertAdaptParcel(mAdpt, code, data, reply);
            convertAdaptParcel(code, data, reply);
            sendCmdAndRecResult(mAdpt);
            convertParcel(mAdpt, code, data, reply);

            Log.e(TAG, "transact end....");
        }

        private void connectToSocket() {
            try {
                if (!socket.isConnected()) {
                    socket.connect(socketAddr);
                }
                Log.d(TAG, "socket connect is " + socket.isConnected());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        private void disconnectToSocket() {
            try {
                if (socket.isConnected()) {
                    socket.close();
                }
                Log.d(TAG, "socket close " + socket.isConnected());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    class Parcel {
        private int mDataSize;
        private int mPos;
        private byte[] mData;

//        public static Parcel obtain() {
//            return new Parcel();
//        }

        private Parcel() {
            mData = new byte[BUF_SIZE];
            mPos = 0;
            mDataSize = 0;
        }

        public void writeByteArray(byte[] b, int offset, int len) {
            if (len == 0) return;
            writeInt(len);
            writeByteArrayInternal(b, offset, len);
        }

        public void writeByteArrayInternal(byte[] b, int offset, int len) {
            if (len == 0) return;
            System.arraycopy(b, offset, mData, mPos, len);
            mPos += len;
            mDataSize += len;
        }

        public void readByteArray(byte[] val) {
            System.arraycopy(mData, mPos, val, 0, val.length);
            mPos += val.length;
        }

        public int dataSize() {
            return mDataSize;
        }

        public void writeInt(int i) {
            Log.d(TAG, "ningbiao writeInt i="+i);
            mData[mPos+3] = (byte)(i >> 24 & 0xff);
            mData[mPos+2] = (byte)(i >> 16 & 0xff);
            mData[mPos+1] = (byte)(i >> 8 & 0xff);
            mData[mPos] = (byte)(i & 0xff);
            mPos += 4;
            mDataSize += 4;
        }

        public int readInt() {
            int b0 = mData[mPos] & 0xFF;
            int b1 = mData[mPos + 1] & 0xFF;
            int b2 = mData[mPos + 2] & 0xFF;
            int b3 = mData[mPos + 3] & 0xFF;
            mPos += 4;
            return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
        }

        public void setDataPosition(int i) {
            mPos = i;
        }

        public String readString() throws Exception{
            int nNum = readInt();
            byte[] b = new byte[nNum];
            Log.d(TAG, "readString num = "+nNum);
            readByteArray(b);

            return new String(b, 0, nNum, "utf-8");
        }

        public void recycle() {
            reset();
        }

        public void reset() {
            mPos = 0;
            mDataSize = 0;
        }

        public void LogArray(){
            Log.e(TAG, "array length = "+mData.length);
            for(int i = 0; i < mData.length; i++){
                if (i > 19) break;
                Log.e(TAG, "Parcel LogArray : ("+i+") = "+mData[i]);
            }
        }
    }
}
