# 什么是Feign

Feign 是由 Netflix 团队开发的一款基于 Java 实现的 HTTP client，借鉴了 Retrofi、 JAXRS-2.0、WebSocket 等类库。通过 Feign，我们可以像调用方法一样非常简单地访问 HTTP API。这篇博客将介绍如何使用原生的 Feign，注意，是原生的，不是经过 Spring 层层封装的 Feign。

补充一下，在 maven 仓库中搜索 feign，我们会看到两种 Feign： OpenFeign Feign 和 Netflix Feign。它们有什么区别呢？简单地说，OpenFeign Feign 的前身就是 Netflix Feign，因为 Netflix Feign 从 2016 年开始就不维护了，所以建议还是使用 OpenFeign Feign。

![zzs_feign_001.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092457876-232554073.png)

# 为什么使用Feign

## 为什么要使用HTTP client

首先，因为 Feign 本身是一款 HTTP client，所以，这里先回答：为什么使用 HTTP client？

假设不用 HTTP client，我们访问 HTTP API 的过程大致如下。是不是相当复杂呢？直接操作 socket 已经非常麻烦了，我们还必须在熟知 HTTP 协议的前提下自行完成报文的组装和解析，代码的复杂程度可想而知。

![zzs_feign_003.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092516146-1713527948.png)

那么，这个过程是不是可以更简单一些呢？

我们可以发现，在上面的图中，红框的部分是相对通用的，是不是可以把这些逻辑封装起来？基于这样的思考，于是就有了 HTTP client（根据类库的不同，封装的层次会有差异）。

所以，为什么要使用 HTTP client 呢？简单地说，就是为了让我们更方便地访问 HTTP API。

## 为什么要使用Feign

HTTP client 的类库还有很多，例如 Retrofi、JDK 自带的 HttpURLConnection、Apache HttpClient、OkHttp、Spring 的 RestTemplate，等等。我很少推荐说要使用哪种具体的类库，如果真的要推荐 Feign 的话，主要是由于它优秀的扩展性（不是一般的优秀，后面的使用例子就可以看到）。

# 如何使用Feign

关于如何使用 Feign，官方给出了非常详细的[文档](https://github.com/OpenFeign/feign)，在我看过的第三方类库中，算是比较少见的。

本文用到的例子也是参考了官方文档。

## 项目环境说明

os：win 10

jdk：1.8.0_231

maven：3.6.3

IDE：Spring Tool Suite 4.6.1.RELEASE

## 引入依赖

这里引入 gson，是因为入门例子需要有一个 json 解码器。

```xml
    <properties>
        <feign.version>11.2</feign.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-core</artifactId>
            <version>${feign.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.openfeign</groupId>
            <artifactId>feign-gson</artifactId>
            <version>${feign.version}</version>
        </dependency>
    </dependencies>
```

## 入门例子

入门例子中使用 Feign 来访问 github 的接口获取 Feign 这个仓库的所有贡献者。

通过下面的代码可以发现，**Feign 本质上是使用了动态代理来生成访问 HTTP API 的代码**，定义 HTTP API 的过程有点像在定义 advice。

```java
// 定义HTTP API
interface GitHub {
    
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    // @RequestLine(value = "GET /repos/{owner}/{repo}/contributors", decodeSlash = false)// 测试转义"/"、"+"
    // @RequestLine("GET /repos/{owner:[a-zA-Z]*}/{repo}/contributors")// 测试正则校验
    // @Headers("Accept: application/json") // 测试添加header
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);
}

public static class Contributor {
    String login;
    int contributions;
}

public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder()) // 返回内容为json格式，所以需要用到json解码器
                // .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true)) // 配置超时参数等
                .target(GitHub.class, "https://api.github.com");

        // 像调用方法一样访问HTTP API
        github.contributors("OpenFeign", "feign").stream()
            .map(contributor -> contributor.login + " (" + contributor.contributions + ")")
            .forEach(System.out::println);
    }
}
```

## 个性化配置

除了简单方便之外，Feign 还有一个很大的亮点，就是**有相当优秀的扩展性，几乎什么都可以自定义**。下面是官方给的一张图，基本涵盖了 Feign 可以扩展的内容。每个扩展支持都有一个对应的适配包，例如，更换解码器为 jackson 时，需要引入`io.github.openfeign:feign-jackson`的适配包。

![zzs_feign_004.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092536804-910086868.png)

### 更换为Spring的注解

在入门例子中，我们使用 Feign 自带的注解来定义 HTTP API。但是，对于习惯了 Spring 注解的许多人来说，无疑需要增加学习成本。我们自然会问，Feign 能不能支持 Spring 注解呢？答案是肯定的。Feign 不但能支持 Spring 注解，还可以支持 JAX-RS、SOAP 等等。

![zzs_feign_005.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092606211-52728283.png)

下面就是使用 Sping 注解定义 HTTP API 的例子。注意，**pom 文件中要引入 `io.github.openfeign:feign-spring4` 的依赖**。

```java
// 定义HTTP API
interface GitHub {
    
    @GetMapping("/repos/{owner}/{repo}/contributors")
    List<Contributor> contributors(@RequestParam("owner") String owner, @RequestParam("repo") String repo);
}


public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder())
                .contract(new SpringContract())// 自定义contract
                .target(GitHub.class, "https://api.github.com");
    }
}
```

### 自定义解码器和编码器

在入门例子中，我们使用 gson 来解析 json。那么，如果我想把它换成 jackson 行不行？Feign 照样提供了支持。

![zzs_feign_006.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092620973-423554498.png)

注意，**pom 文件中要引入 `io.github.openfeign:feign-jackson` 的依赖**。

```java
public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new JacksonDecoder()) // 自定义解码器
                .encoder(new JacksonEncoder()) // 自定义编码器
                .target(GitHub.class, "https://api.github.com");
    }
}
```

### 自定义内置的HTTP client

接下来的这个自定义就更厉害了。Feign 本身作为一款 HTTP client，竟然还可以支持其他 HTTP client。

![zzs_feign_007.png](https://img2020.cnblogs.com/blog/1731892/202107/1731892-20210709092639188-503096522.png)

这里用 OkHttp 作例子。注意，**pom 文件中要引入 `io.github.openfeign:feign-okhttp` 的依赖**。

```java
public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder())
                .client(new OkHttpClient())// 自定义client
                .target(GitHub.class, "https://api.github.com");
    }
}
```

### 自定义拦截器

我们访问外部接口时，有时需要带上一些特定的 header，例如，应用标识、token，我们可以通过两种方式实现：一是使用注解定义 HTTP API，二是使用拦截器（更常用）。下面的例子中，使用拦截器给请求添加 token 请求头。

```java
public class MyInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("token", LoginUtils.getCurrentToken());
    }
}
public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder())
                .requestInterceptor(new MyInterceptor())
                .target(GitHub.class, "https://api.github.com");
    }
}
```

### 自定义重试器

默认情况下，Feign 访问 HTTP API 时，如果抛出`IOException`，它会认为是短暂的网络异常而发起重试，这时，Feign 会使用默认的重试器`feign.Retryer.Default`（最多重试 5 次），如果不想启用重试，则可以选择另一个重试器`feign.Retryer.NEVER_RETRY`。当然，我们也可以自定义。

奇怪的是，Feign 通过重试器的` continueOrPropagate(RetryableException e)`方法是否抛出`RetryableException`来判断是否执行重试，为什么不使用 true 或 false 来判断呢？

注意，重试器是用来判断是否执行重试，自身不包含重试的逻辑。

```java
public class MyRetryer implements Retryer {
    
    int attempt = 0;

    @Override
    public void continueOrPropagate(RetryableException e) {
        // 如果把RetryableException抛出，则不会继续重试
        // 否则继续重试
        if(attempt++ >= 3) {// 重试三次
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return this;
    }
}
public class MyApp {

    public static void main(String... args) {
        // 获取用来访问HTTP API的代理类
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder())
                .retryer(new MyRetryer())
                //.retryer(Retryer.NEVER_RETRY) // 不重试
                .exceptionPropagationPolicy(ExceptionPropagationPolicy.UNWRAP)
                .target(GitHub.class, "https://api.github.com");
    }
}
```

# 结语

以上，基本讲完 Feign 的使用方法，其实 Feign 还有其他可以扩展的东西，例如，断路器、监控等等。感兴趣的话，可以自行分析。

最后，感谢阅读。


# 参考资料

[Feign github](https://github.com/OpenFeign/feign)

> 相关源码请移步：https://github.com/ZhangZiSheng001/feign-demo

> 本文为原创文章，转载请附上原文出处链接：https://www.cnblogs.com/ZhangZiSheng001/p/14989165.html