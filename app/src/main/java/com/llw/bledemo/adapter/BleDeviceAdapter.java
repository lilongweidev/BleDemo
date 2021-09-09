package com.llw.bledemo.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.llw.bledemo.R;
import com.llw.bledemo.bean.BleDevice;

import java.util.List;

/**
 * Ble设备适配器
 * @author llw
 * @description BleDeviceAdapter
 * @date 2021/7/21 19:34
 */
public class BleDeviceAdapter extends BaseQuickAdapter<BleDevice, BaseViewHolder> {

    public BleDeviceAdapter(int layoutResId, List<BleDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, BleDevice bleDevice) {
        holder.setText(R.id.tv_device_name, bleDevice.getRealName())
                .setText(R.id.tv_mac_address, bleDevice.getDevice().getAddress())
                .setText(R.id.tv_rssi, bleDevice.getRssi() + " dBm");
    }
}
