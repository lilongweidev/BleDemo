package com.llw.bledemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.llw.bledemo.adapter.BleDeviceAdapter;
import com.llw.bledemo.bean.BleDevice;
import com.llw.easyutil.EasySP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * 请求打开蓝牙
     */
    private static final int REQUEST_ENABLE_BLUETOOTH = 100;
    /**
     * 权限请求码
     */
    public static final int REQUEST_PERMISSION_CODE = 9527;

    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * nordic扫描回调
     */
    private ScanCallback scanCallback;

    /**
     * 设备列表
     */
    private List<BleDevice> mList = new ArrayList<>();

    /**
     * 列表适配器
     */
    private BleDeviceAdapter deviceAdapter;

    /**
     * 加载进度条
     */
    private ContentLoadingProgressBar loadingProgressBar;

    /**
     * 等待连接
     */
    private LinearLayout layConnectingLoading;

    /**
     * Gatt
     */
    private BluetoothGatt bluetoothGatt;

    /**
     * 设备是否连接
     */
    private boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        initView();
        //检查Android版本
        checkAndroidVersion();
    }

    /**
     * 初始化
     */
    private void initView() {
        RecyclerView rvDevice = findViewById(R.id.rv_device);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        layConnectingLoading = findViewById(R.id.lay_connecting_loading);
        findViewById(R.id.btn_start_scan).setOnClickListener(v -> startScanDevice());
        findViewById(R.id.btn_stop_scan).setOnClickListener(v -> stopScanDevice());
        //扫描结果回调
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                //添加到设备列表
                addDeviceList(new BleDevice(result.getDevice(), result.getRssi(), result.getDevice().getName()));
            }

            @Override
            public void onScanFailed(int errorCode) {
                throw new RuntimeException("Scan error");
            }
        };
        //列表配置
        deviceAdapter = new BleDeviceAdapter(R.layout.item_device_rv, mList);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        //item点击事件
        deviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            //连接设备
            connectDevice(mList.get(position));
        });
        //启用动画
        deviceAdapter.setAnimationEnable(true);
        //设置动画方式
        deviceAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInRight);
        rvDevice.setAdapter(deviceAdapter);
    }

    /**
     * 连接设备
     *
     * @param bleDevice 蓝牙设备
     */
    private void connectDevice(BleDevice bleDevice) {
        //显示连接等待布局
        //layConnectingLoading.setVisibility(View.VISIBLE);
        //停止扫描
        stopScanDevice();

        //跳转页面
        Intent intent = new Intent(this,DataExchangeActivity.class);
        intent.putExtra("device",bleDevice.getDevice());
        startActivity(intent);

        //连接Gatt
        //connectGatt(bleDevice);
    }

    private void connectGatt(BleDevice bleDevice) {
        //获取远程设备
        BluetoothDevice device = bleDevice.getDevice();
        //连接gatt
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED://连接成功
                        isConnected = true;
                        Log.d(TAG, "连接成功");
                        runOnUiThread(() -> {
                            layConnectingLoading.setVisibility(View.GONE);
                            showMsg("连接成功");
                            //放入缓存
                            EasySP.putString("address",device.getAddress());
                        });
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED://断开连接
                        isConnected = false;
                        Log.d(TAG, "断开连接");
                        runOnUiThread(() -> {
                            layConnectingLoading.setVisibility(View.GONE);
                            showMsg("断开连接");
                        });
                        break;
                    default:
                        break;
                }
            }
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
     * 添加到设备列表
     *
     * @param bleDevice 蓝牙设备
     */
    private void addDeviceList(BleDevice bleDevice) {
        if (!mList.contains(bleDevice)) {
            bleDevice.setRealName(bleDevice.getRealName() == null ? "UNKNOWN" : bleDevice.getRealName());
            mList.add(bleDevice);
        } else {
            //更新设备信号强度值
            for (BleDevice device : mList) {
                device.setRssi(bleDevice.getRssi());
            }
        }
        //刷新列表适配器
        deviceAdapter.notifyDataSetChanged();
    }

    /**
     * 开始扫描设备
     */
    public void startScanDevice() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        mList.clear();
        deviceAdapter.notifyDataSetChanged();
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(scanCallback);
    }

    /**
     * 停止扫描设备
     */
    public void stopScanDevice() {
        loadingProgressBar.setVisibility(View.INVISIBLE);
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
    }

    /**
     * 检查Android版本
     */
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android 6.0及以上动态请求权限
            requestPermission();
        } else {
            //检查蓝牙是否打开
            openBluetooth();
        }
    }

    /**
     * 请求权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_CODE)
    private void requestPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //权限通过之后检查有没有打开蓝牙
            openBluetooth();
        } else {
            // 没有权限
            EasyPermissions.requestPermissions(this, "App需要定位权限", REQUEST_PERMISSION_CODE, perms);
        }
    }

    /**
     * 权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 是否打开蓝牙
     */
    public void openBluetooth() {
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {//是否支持蓝牙
            if (bluetoothAdapter.isEnabled()) {//打开
                showMsg("蓝牙已打开");
            } else {//未打开
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
            }
        } else {
            showMsg("你的设备不支持蓝牙");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
                if (bluetoothAdapter.isEnabled()) {
                    //蓝牙已打开
                    showMsg("蓝牙已打开");
                } else {
                    showMsg("请打开蓝牙");
                }
            }
        }
    }
}