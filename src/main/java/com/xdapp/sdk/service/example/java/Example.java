package com.xdapp.sdk.service.example.java;

import com.xdapp.sdk.service.Service;
import com.xdapp.sdk.service.model.ServiceInfo;

/**
 * @author alusa on 2019/9/27.
 */
public class Example {
    public static void main(String[] args) {
        // 其中 demo 为项目名(AppName)，gm 为服务名(ServiceName)，123456 为密钥
        ServiceInfo serviceInfo = new ServiceInfo("demo", "gm", "123456");
        // 连接到本地
        Service service = Service.connectToLocalDev(serviceInfo, "127.0.0.1", 8061);
        // 连接到生产环境（国内）
//        Service service = Service.connectToProd(serviceInfo);
        // 连接到生产环境（海外）
//        Service service = Service.connectToGlobal(serviceInfo);
        // 连接到外网测试服务器
//        Service service = Service.connectToDev(serviceInfo);

        // addWeb方法 会自动对 alias 增加 serviceName 前缀
        service.addWeb("hello", GmService.class);
        // 等于
//        service.add("hello", GmService.class, "gm_hello");
        service.start();
    }
}
