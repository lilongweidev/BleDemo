package com.llw.bledemo.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

import com.llw.bledemo.utils.BleConstant;
import com.llw.bledemo.utils.BleHelper;
import com.llw.bledemo.utils.ByteUtils;

import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * @author llw
 * @description BleCallback
 * @date 2021/8/24 20:51
 */
public class BleCallback extends BluetoothGattCallback {

    private static final String TAG = BleCallback.class.getSimpleName();

    /**
     * 连接状态改变回调
     *
     * @param gatt     gatt
     * @param status   gatt连接状态
     * @param newState 新状态
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, Thread.currentThread().getName());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED://连接成功
                    Log.d(TAG, "连接成功");
                    //获取MtuSize
                    gatt.requestMtu(512);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED://断开连接
                    Log.e(TAG, "断开连接");
                    break;
                default:
                    break;
            }
        } else {
            Log.e(TAG, "onConnectionStateChange: " + status);
        }
    }

    /**
     * 物理层改变回调
     *
     * @param gatt   gatt
     * @param txPhy  发送速率  1M 2M
     * @param rxPhy  接收速率  1M 2M
     * @param status 更新操作的状态
     */
    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.d(TAG, "onPhyUpdate: txPhy: "+ txPhy + " rxPhy: "+ rxPhy);
    }

    /**
     * 读取物理层回调
     *
     * @param gatt   gatt
     * @param txPhy  发送速率  1M 2M
     * @param rxPhy  接收速率  1M 2M
     * @param status 更新操作的状态
     */
    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.d(TAG, "onPhyRead: txPhy：" + txPhy + " rxPhy：" + rxPhy);
        if (txPhy == BluetoothDevice.PHY_LE_1M && rxPhy == BluetoothDevice.PHY_LE_1M){
            //获取2M的发送和接收速率
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gatt.setPreferredPhy(BluetoothDevice.PHY_LE_2M, BluetoothDevice.PHY_LE_2M,
                        BluetoothDevice.PHY_OPTION_NO_PREFERRED);
            }
        }
    }

    /**
     * 发现服务回调
     *
     * @param gatt   gatt
     * @param status gatt状态
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered");
        boolean notifyOpen = BleHelper.enableIndicateNotification(gatt);

        if (!notifyOpen) {
            Log.e(TAG, "开启通知属性异常");
            gatt.disconnect();
        }
    }

    /**
     * 特性读取回调
     *
     * @param gatt           gatt
     * @param characteristic 特性
     * @param status         gatt状态
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead: characteristic: " + characteristic.getUuid().toString());
    }

    /**
     * 特性写入回调
     *
     * @param gatt           gatt
     * @param characteristic 特性
     * @param status         gatt状态
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        String command = ByteUtils.bytesToHexString(characteristic.getValue());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite: 写入成功：" + command);
        } else {
            Log.d(TAG, "onCharacteristicWrite: 写入失败：" + command);
        }

        //读取特性
        //Log.d(TAG, "onCharacteristicChanged: 读取特性 " + gatt.readCharacteristic(characteristic));
    }

    /**
     * 特性改变回调
     *
     * @param gatt           gatt
     * @param characteristic 特性
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String content = ByteUtils.bytesToHexString(characteristic.getValue());
        Log.d(TAG, "onCharacteristicChanged: 收到内容：" + content);

        //读取特性
        //Log.d(TAG, "onCharacteristicChanged: 读取特性 " + gatt.readCharacteristic(characteristic));
    }

    /**
     * 描述符获取回调
     *
     * @param gatt       gatt
     * @param descriptor 描述符
     * @param status     gatt状态
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorRead: descriptor: " + descriptor.getUuid().toString());
    }

    /**
     * 描述符写入回调
     *
     * @param gatt       gatt
     * @param descriptor 描述符
     * @param status     gatt状态
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (BleConstant.DESCRIPTOR_UUID.equals(descriptor.getUuid().toString().toLowerCase())) {
            if (status == GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: 通知开启成功");
                //获取phy
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    gatt.readPhy();
                }
                //读取描述符
                gatt.readDescriptor(descriptor);
                //读取RSSI
                gatt.readRemoteRssi();
            } else {
                Log.d(TAG, "onDescriptorWrite: 通知开启失败");
            }
        }
    }

    /**
     * 可靠写入完成回调
     *
     * @param gatt   gatt
     * @param status gatt状态
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onReliableWriteCompleted: 可靠写入");
    }

    /**
     * 读取远程设备的信号强度回调
     *
     * @param gatt   gatt
     * @param rssi   信号强度
     * @param status gatt状态
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.d(TAG, "onReadRemoteRssi: rssi: " + rssi);
    }

    /**
     * Mtu改变回调
     *
     * @param gatt   gatt
     * @param mtu    new MTU size
     * @param status gatt状态
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.d(TAG, "onMtuChanged：mtu： " + mtu);
        //发现服务
        gatt.discoverServices();
    }

}

