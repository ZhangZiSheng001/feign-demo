package cn.zzs.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class MyInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        System.err.println("进入自定义拦截器");
        template.header("token", "123");
    }
}
