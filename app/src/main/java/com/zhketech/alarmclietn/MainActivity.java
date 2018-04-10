package com.zhketech.alarmclietn;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zhketech.alarmclietn.utils.Logutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

/**
 * 向服务器发送
 */
public class MainActivity extends AppCompatActivity {

    DatagramSocket mDatagramSocket = null;
    InetAddress mInetAddress = null;
    DatagramPacket mDatagramPacket = null;
    String ip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip = getIPAddress(this);
        Logutils.i("ip:" + ip);
    }

    @Override
    protected void onDestroy() {
        mDatagramSocket.close();
        super.onDestroy();
    }

    /**
     * 发送Socket请求
     *
     * @param view
     */
    public void btn_onck(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInetAddress = InetAddress.getByName("192.168.1.16");
                    mDatagramSocket = new DatagramSocket(8084, mInetAddress);
                    byte bb[] = "wpfddddddddddddddddddd".getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(bb, bb.length, mInetAddress, 8084);
                    try {
                        mDatagramSocket.send(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logutils.e("tag", "error:Io" + e.getMessage());
                    }
                    mDatagramSocket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Logutils.e("tag", "error:UnknownHostException" + e.getMessage());

                } catch (SocketException e) {
                    Logutils.e("tag", "error:SocketException" + e.getMessage());
                }
            }
        }).start();

        Logutils.e("tag", "udp数据已发送");
    }

    /**
     * 发送报警报文
     *
     * @param view
     */
    public void btn_onck2(View view) {

        new Thread() {
            @Override
            public void run() {
                Log.d("views", "开始发送报警数据");
                //发送的字节数组
                byte[] requestBys = new byte[1024];
                String fl = "ATIF";//数据头标识
                byte[] zd = fl.getBytes();
                System.arraycopy(zd, 0, requestBys, 0, 4);
                //报警ip
                byte[] id = new byte[32];
                String ip = "192.168.1.16";//要发送报警的ip地址
                byte[] idby = ip.getBytes();
                System.arraycopy(idby, 0, id, 0, idby.length);
                //封装入请求字节数组
                System.arraycopy(id, 0, requestBys, 4, 32);
                //报警类型
                byte[] alertType = new byte[32];
                byte[] alertS = new byte[0];
                try {
                    alertS = "社会大哥打人了".getBytes("gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                System.arraycopy(alertS, 0, alertType, 0, alertS.length);
                Log.d("views", "alertType: " + alertType.length);
                System.arraycopy(alertType, 0, requestBys, 460, 32);
                //保留字段
                byte[] reserved = new byte[32];
                System.arraycopy(reserved, 0, requestBys, 492, 32);
                Log.d("views", "request: " + Arrays.toString(requestBys));
                //建立Socket进行Tcp数据包发送
                Socket socket = null;
                OutputStream os = null;
                try {
                    socket = new Socket("192.168.1.16", 2000);
                    os = socket.getOutputStream();
                    os.write(requestBys);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }.start();

    }

    /**
     * 发送心跳报文
     *
     * @param view
     */
    public void btn_onck3(View view) {

        SendHeart mSendHeart = new SendHeart();
        new Thread(mSendHeart).start();
    }

    class SendHeart extends Thread {
        @Override
        public void run() {


            byte[] requestBytes = new byte[96];
            byte[] flag = "ZDHB".getBytes();// 数据标识头字节
            byte[] idKey = "11111111111111112222".getBytes();//手机唯一 的标识字节
            Logutils.i("length：" + idKey.length);

            byte[] id = new byte[48];
            System.arraycopy(idKey, 0, id, 0, idKey.length);
            System.arraycopy(flag, 0, requestBytes, 0, 4);
            System.arraycopy(id, 0, requestBytes, 4, 48);

            byte[] timeT = "1234567891234567".getBytes();

            System.arraycopy(timeT, 0, requestBytes, 64, 16);

            // 纬度
            double lat = 89.362;
            byte[] latB = getBytes(lat);

            System.arraycopy(latB, 0, requestBytes, 64, 8);
            // 经度
            double lon = 110.123;
            byte[] lonB = getBytes(lon);
            // System.out.println("经度:" + Arrays.toString(lonB));
            // System.out.println(getDouble(lonB));
            System.arraycopy(lonB, 0, requestBytes, 72, 8);

            //设备状态
            //剩余电量
            byte[] power = new byte[1];
            power[0] = 55;
            System.arraycopy(power, 0, requestBytes, 80, 1);
            //内存使用
            byte[] mem = new byte[1];
            mem[0] = 60;
            System.arraycopy(mem, 0, requestBytes, 81, 1);
            //cpu使用
            byte[] cpu = new byte[1];
            cpu[0] = 40;
            System.arraycopy(cpu, 0, requestBytes, 82, 1);
            //信号强度
            byte[] signal = new byte[1];
            signal[0] = 88;
            System.arraycopy(signal, 0, requestBytes, 83, 1);
            //弹箱连接状态
            byte[] bluetooth = new byte[1];
            bluetooth[0] = 0;
            System.arraycopy(bluetooth, 0, requestBytes, 84, 1);
            //保留
            byte[] save = new byte[11];
            System.arraycopy(save, 0, requestBytes, 85, 11);
            //建立UDP请求

            DatagramSocket socketUdp = null;


            try {
                socketUdp = new DatagramSocket(2020);
                DatagramPacket datagramPacket = new DatagramPacket(requestBytes, requestBytes.length, InetAddress.getByName("192.168.1.16"), 2020);
                socketUdp.send(datagramPacket);
                socketUdp.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  申请 开启子弹箱
     *
     */
    public void btn_onck4(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] requestBytes = new byte[102];
                byte[] flag = "ReqB".getBytes();
                System.arraycopy(flag, 0, requestBytes, 0, flag.length);

                Logutils.i("l："+flag.length);
                byte [] ver ="0001".getBytes();
                System.arraycopy(ver, 0, requestBytes, 4, ver.length);
                byte [] uiAction ="0".getBytes();
                Logutils.i("dd:"+uiAction.length);
                System.arraycopy(ver, 0, requestBytes, 8, 1);
                Socket socket = null;
                OutputStream os = null;
                try {
                    socket = new Socket("192.168.1.16", 20000);
                    os = socket.getOutputStream();

                    os.write(requestBytes);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }



            }
        }).start();

    }

    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }


    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


}
