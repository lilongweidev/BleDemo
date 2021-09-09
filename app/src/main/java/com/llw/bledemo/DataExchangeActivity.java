package com.llw.bledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.llw.bledemo.callback.BleCallback;
import com.llw.bledemo.utils.BleHelper;

/**
 * 数据交互
 *
 * @author llw
 */
public class DataExchangeActivity extends AppCompatActivity {
    private static final String TAG = DataExchangeActivity.class.getSimpleName();

    /**
     * Gatt
     */
    private BluetoothGatt bluetoothGatt;
    /**
     * 设备是否连接
     */
    private boolean isConnected = false;

    /**
     * Gatt回调
     */
    private BleCallback bleCallback;

    private EditText etCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_exchange);
        //初始化
        bleCallback = new BleCallback();
        //获取上个页面传递过来的设备
        BluetoothDevice device = getIntent().getParcelableExtra("device");
        //连接gatt 设置Gatt回调
        bluetoothGatt = device.connectGatt(this, false, bleCallback);
        etCommand = findViewById(R.id.et_command);
        //发送指令
        findViewById(R.id.btn_send_command).setOnClickListener(v -> {
            String command = etCommand.getText().toString().trim();
            if (command.isEmpty()) {
                showMsg("请输入指令");
                return;
            }
            //发送指令
            BleHelper.sendCommand(bluetoothGatt, command, "010200".equals(command));
        });
    }

    /**
     * 断开设备连接
     */
    private void disconnectDevice() {
        if (isConnected && bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    /**
     * Toast提示
     *
     * @param msg 内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}