package cn.zzs.feign;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.zzs.feign.entity.Contributor;
import cn.zzs.feign.interceptor.MyInterceptor;
import cn.zzs.feign.retryer.MyRetryer;
import feign.Feign;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;

// 定义HTTP API
interface GitHub {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    // @RequestLine(value = "GET /repos/{owner}/{repo}/contributors", decodeSlash = false)// 测试转义"/"、"+"
    // @RequestLine("GET /repos/{owner:[a-zA-Z]*}/{repo}/contributors")// 测试正则校验
    // @Headers("Accept: application/json") // 测试添加header
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);
    
    // @GetMapping("/repos/{owner}/{repo}/contributors")
    // List<Contributor> contributors(@RequestParam("owner") String owner, @RequestParam("repo") String repo);
}

public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new JacksonDecoder()) // 自定义解码器
                .client(new OkHttpClient())// 自定义client
                //.contract(new SpringContract())// 自定义contract
                .requestInterceptor(new MyInterceptor())
                .retryer(new MyRetryer())
                //.retryer(Retryer.NEVER_RETRY) // 不重试
                .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true)) // 配置超时参数等
                .target(GitHub.class, "https://api.github.com");

        // 像调用方法一样访问HTTP API
        github.contributors("OpenFeign", "feign").stream()
            .map(contributor -> contributor.getLogin() + " (" + contributor.getContributions() + ")")
            .forEach(System.out::println);
    }
}
