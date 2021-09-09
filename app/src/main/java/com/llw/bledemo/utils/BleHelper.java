package com.llw.bledemo.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import java.util.UUID;

/**
 * @author llw
 * @description BleHelper
 * @date 2021/9/7 20:09
 */
public class BleHelper {

    /**
     * 启用指令通知
     */
    public static boolean enableIndicateNotification(BluetoothGatt gatt) {
        //获取Gatt 服务
        BluetoothGattService service = gatt.getService(UUID.fromString(BleConstant.OTA_SERVICE_UUID));
        if (service == null) {
            return false;
        }
        //获取Gatt 特征（特性）
        BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID.fromString(BleConstant.OTA_CHARACTERISTIC_INDICATE_UUID));
        return setCharacteristicNotification(gatt, gattCharacteristic);
    }

    /**
     * 设置特征通知
     * return true, if the write operation was initiated successfully
     */
    private static boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic gattCharacteristic) {
        //如果特性具备Notification功能，返回true就代表设备设置成功
        boolean isEnableNotification = gatt.setCharacteristicNotification(gattCharacteristic, true);
        if (isEnableNotification) {
            //构建BluetoothGattDescriptor对象
            BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(UUID.fromString(BleConstant.DESCRIPTOR_UUID));
            gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //写入描述符
            return gatt.writeDescriptor(gattDescriptor);
        } else {
            return false;
        }
    }

    /**
     * 发送指令
     * @param gatt gatt
     * @param command 指令
     * @param isResponse 是否响应
     * @return
     */
    public static boolean sendCommand(BluetoothGatt gatt, String command, boolean isResponse) {
        //获取服务
        BluetoothGattService service = gatt.getService(UUID.fromString(BleConstant.OTA_SERVICE_UUID));
        if (service == null) {
            Log.e("TAG", "sendCommand: 服务未找到");
            return false;
        }
        //获取特性
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BleConstant.OTA_CHARACTERISTIC_WRITE_UUID));
        if (characteristic == null) {
            Log.e("TAG", "sendCommand: 特性未找到");
            return false;
        }
        //写入类型  WRITE_TYPE_DEFAULT  默认有响应， WRITE_TYPE_NO_RESPONSE  无响应。
        characteristic.setWriteType(isResponse ?
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT : BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        //将字符串command转Byte后进行写入
        characteristic.setValue(ByteUtils.hexStringToBytes(command));
        boolean result = gatt.writeCharacteristic(characteristic);
        //执行可靠写入
        gatt.executeReliableWrite();
        Log.d("TAG", result ? "写入初始化成功：" + command : "写入初始化失败：" + command);
        return result;
    }
}
