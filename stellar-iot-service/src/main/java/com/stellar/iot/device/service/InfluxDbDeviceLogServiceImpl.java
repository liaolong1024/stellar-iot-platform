package com.stellar.iot.device.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.stellar.iot.device.model.DeviceLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class InfluxDbDeviceLogServiceImpl implements DeviceLogService, ApplicationListener<ApplicationContextEvent> {
    @Autowired
    private InfluxDBClient influxDBClient;

    @Override
    public void saveDeviceLog(DeviceLog deviceLog) {
        Point point = Point
                .measurement("iot_device_log")      // 表（measurement）
                .addTag("deviceId", deviceLog.getDeviceId())
                .addTag("product_id", deviceLog.getProductId())// tag（索引字段）
                .addField("temperature", deviceLog.getTemperature())   // field（值）
                .time(Instant.now(), WritePrecision.NS); //
        influxDBClient.getWriteApiBlocking().writePoint(point);
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        DeviceLog deviceLog = DeviceLog.builder()
                .deviceId("1")
                .temperature(20.0)
                .productId("z01")
                .build();
        saveDeviceLog(deviceLog);


        String flux = "from(bucket:\"stellar_iot/autogen\")"
                + " |> range(start: -10d)"
                + "|> filter(fn: (r) => r._measurement == \"iot_device_log\")"
                ;
        List<FluxTable> query = influxDBClient.getQueryApi().query(flux);
        for (FluxTable fluxTable : query) {
            System.out.println(fluxTable.getGroupKey());
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord record : records) {
                System.out.println(record.getMeasurement());
                System.out.println(record.getField());
                System.out.println(record.getValue());

                // tag
                System.out.println(record.getValueByKey("device_id"));
            }
        }
        System.out.println();
    }
}
