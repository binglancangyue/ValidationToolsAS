package com.sprd.validationtools.utils;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.OutputStream;
import java.io.InputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import android.util.Log;
import android.net.LocalSocketAddress.Namespace;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.os.SystemProperties;

public class SocketUtils {

    private static final String TAG = "SocketUtils";
    public static final String OK = "OK";
    public static final String FAIL = "FAIL";

    private static String mSocketName = null;
    private static LocalSocket mSocketClient = null;
    private static OutputStream mOutputStream;
    private static InputStream mInputStream;
    private static LocalSocketAddress mSocketAddress;

    public static synchronized String sendCmdAndRecResult(String socketName, Namespace namespace,
            String strcmd) {
        Log.d(TAG, "send cmd: " + strcmd);
        byte[] buf = new byte[255];
        String result = null;
        Log.d(TAG, "set cmd: " + strcmd);
        try {
            mSocketClient = new LocalSocket();
            mSocketName = socketName;
            mSocketAddress = new LocalSocketAddress(mSocketName, namespace);
            if (!mSocketClient.isConnected()) {
                Log.d(TAG, "isConnected...");
                mSocketClient.connect(mSocketAddress);
            }
            // mSocketClient.connect(mSocketAddress);
            Log.d(TAG, "mSocketClient connect is " + mSocketClient.isConnected());
            mOutputStream = mSocketClient.getOutputStream();
            if (mOutputStream != null) {
                final StringBuilder cmdBuilder = new StringBuilder(strcmd).append('\0');
                final String cmd = cmdBuilder.toString();
                mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
                mOutputStream.flush();
            }
            mInputStream = mSocketClient.getInputStream();
            int count = mInputStream.read(buf, 0, 255);
            result = "";
            result = new String(buf, "utf-8");
            Log.d(TAG, "count = " + count + ", result is " + result);
        } catch (IOException e) {
            Log.e(TAG, "Failed get output stream: " + e.toString());
            return null;
        } finally {
            try {
                buf = null;
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mSocketClient != null) {
                    if (mSocketClient.isConnected()) {
                        mSocketClient.close();
                        mSocketClient = null;
                    } else {
                        mSocketClient = null;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "catch exception is " + e);
                return null;
            }
        }
        return result;
    }

    public static synchronized String sendCmdAndRecResult(String socketName, Namespace namespace,
            String strcmd, int time) {
        while (time-- != 0) {
            String tmp = sendCmdAndRecResult(socketName, namespace, strcmd);
            if (tmp != null)
                return tmp;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            Log.d(TAG, "try again" + time);
        }
        return null;
    }

    public static String sendCmdNoCloseSocket(String socketName, Namespace namespace, String strcmd) {
        byte[] buf = new byte[255];
        String result = null;
        try {
            if (mSocketClient == null) {
                mSocketClient = new LocalSocket();
                mSocketName = socketName;
                mSocketAddress = new LocalSocketAddress(mSocketName, namespace);
                mSocketClient.connect(mSocketAddress);
            }
            Log.d(TAG, "mSocketClient connect is " + mSocketClient.isConnected());
            mOutputStream = mSocketClient.getOutputStream();
            if (mOutputStream != null) {
                final StringBuilder cmdBuilder = new StringBuilder(strcmd).append('\0');
                final String cmd = cmdBuilder.toString();
                mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
                mOutputStream.flush();
            }
            mInputStream = mSocketClient.getInputStream();
            int count = mInputStream.read(buf, 0, 255);
            result = new String(buf, "utf-8");
            Log.d(TAG, "count = " + count + ", result is " + result);
        } catch (IOException e) {
            Log.e(TAG, "Failed get output stream: " + e);
        }
        return result;
    }

    public static void closeSocket() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mSocketClient != null && mSocketClient.isConnected()) {
                mSocketClient.close();
                mSocketClient = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "catch exception is " + e);
        }
    }

    public static synchronized String readSocketResult(String socketName, Namespace namespace) {
        byte[] buf = new byte[255];
        String result = null;
        Log.d(TAG, "read socket name: " + socketName);
        try {
            mSocketClient = new LocalSocket();
            mSocketName = socketName;
            mSocketAddress = new LocalSocketAddress(mSocketName, namespace);
            if (!mSocketClient.isConnected()) {
                Log.d(TAG, "isConnected...");
                mSocketClient.connect(mSocketAddress);
            }
            Log.d(TAG, "mSocketClient connect is " + mSocketClient.isConnected());
            mInputStream = mSocketClient.getInputStream();
            int count = mInputStream.read(buf, 0, 255);
            result = "";
            result = new String(buf, "utf-8");
            Log.d(TAG, "count = " + count + ", result is " + result);
        } catch (IOException e) {
            Log.e(TAG, "Failed get output stream: " + e.toString());
            return null;
        } finally {
            try {
                buf = null;
                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mSocketClient != null) {
                    if (mSocketClient.isConnected()) {
                        mSocketClient.close();
                        mSocketClient = null;
                    } else {
                        mSocketClient = null;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "catch exception is " + e);
                return null;
            }
        }
        return result;
    }

    public static String sendSocketCmd(String cmd) {
        // connect socket
        SystemProperties.set("persist.sys.cmdservice.enable", "enable");
        String status = SystemProperties.get("persist.sys.cmdservice.enable", "");
        Log.d(TAG, "cmd_service pro is " + status);
        // sleep 100ms to make sure cmd_service start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = SocketUtils.sendCmdAndRecResult("cmd_skt",
                LocalSocketAddress.Namespace.ABSTRACT, new StringBuilder(cmd).append('\n')
                        .toString());
        Log.d(TAG, "result is " + result);
        if (result != null) {
            String[] str = result.split("\n");
            if ("Result".equals(str[0].trim()) || result.contains("File exists")) {
                Log.d(TAG, "success");
                SystemProperties.set("persist.sys.cmdservice.enable", "disable");
                return result;
            }
        }
        SystemProperties.set("persist.sys.cmdservice.enable", "disable");
        return result;
    }

    public static String SendSlogModemAt(String cmd) {
        Log.d(TAG, "SendSlogModemAt " + cmd);
        String strTmp = sendCmdAndRecResult("slogmodem", LocalSocketAddress.Namespace.ABSTRACT, cmd);
        return strTmp;
    }

    public static synchronized String sendYlogCmdAndRecResult(String strcmd, int time) {
        return sendCmdAndRecResult("ylog_cli", LocalSocketAddress.Namespace.ABSTRACT, strcmd, time);
    }
}
