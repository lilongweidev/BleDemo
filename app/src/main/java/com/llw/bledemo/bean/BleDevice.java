package com.llw.bledemo.bean;

import android.bluetooth.BluetoothDevice;

/**
 * @author llw
 * @description BleDevice
 * @date 2021/7/21 19:20
 */
public class BleDevice {
    private BluetoothDevice device;
    private int rssi;
    private String realName;//真实名称

    /**
     * 构造Device
     * @param device 蓝牙设备
     * @param rssi 信号强度
     * @param realName 真实名称
     */
    public BleDevice(BluetoothDevice device, int rssi, String realName) {
        this.device = device;
        this.rssi = rssi;
        this.realName = realName;
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    public int getRssi(){
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getRealName(){
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof BleDevice){
            final BleDevice that =(BleDevice) object;
            return device.getAddress().equals(that.device.getAddress());
        }
        return super.equals(object);
    }
}
