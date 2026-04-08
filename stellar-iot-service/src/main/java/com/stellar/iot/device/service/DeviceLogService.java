package com.stellar.iot.device.service;

import com.stellar.iot.device.model.DeviceLog;

public interface DeviceLogService {
    /**
     * 保存设备日志
     *
     * @param deviceLog 设备日志
     */
    void saveDeviceLog(DeviceLog deviceLog);
}
