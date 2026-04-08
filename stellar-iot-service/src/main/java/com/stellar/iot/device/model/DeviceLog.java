package com.stellar.iot.device.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceLog {
    private String deviceId;

    private Double temperature;

    private String productId;

    public static void main(String[] args) {
        String s = "/a/b/c".split("/")[2];
        System.out.println(s);
    }
}
