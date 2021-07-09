package cn.zzs.feign;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.zzs.feign.entity.Contributor;
import cn.zzs.feign.entity.Issue;
import cn.zzs.feign.entity.Repository;
import cn.zzs.feign.error.decoder.GitHubClientError;
import feign.ExceptionPropagationPolicy;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.Request;
import feign.RequestLine;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

/**
 * 利用github的接口来测试feign
 * @author zzs
 * @date 2021年7月6日 下午2:53:56
 */
public class GitHubExample {

    public interface GitHub {

        // @RequestLine(value = "GET /users/{username}/repos?sort=full_name", decodeSlash = false)// 测试转义"/"、"+"
        @RequestLine("GET /users/{username:[a-zA-Z]*}/repos?sort=full_name")// 测试正则校验
        // @RequestLine("GET /users/{username}/repos?sort=full_name")
        // @Headers("Accept: application/json")// 测试添加header
        List<Repository> repos(@Param("username") String owner);

        
        @RequestLine("GET /repos/{owner}/{repo}/contributors") 
        List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);

        @RequestLine("POST /repos/{owner}/{repo}/issues")
        void createIssue(Issue issue, @Param("owner") String owner, @Param("repo") String repo);

        /** Lists all contributors for all repos owned by a user. */
        default List<String> contributors(String owner) {
            return repos(owner).stream()
                    .flatMap(repo -> contributors(owner, repo.getName()).stream())
                    .map(c -> c.getLogin())
                    .distinct()
                    .collect(Collectors.toList())
                    ;
        }

        static GitHub connect() {
            final Decoder decoder = new JacksonDecoder();
            final Encoder encoder = new JacksonEncoder();
            return Feign.builder()
                    .encoder(encoder)
                    .decoder(decoder)
                    //.errorDecoder(new GitHubErrorDecoder(decoder))
                    .errorDecoder(new ErrorDecoder.Default())
                    //.retryer(new MyRetryer())
                    .retryer(new Retryer.Default())
                    //.exceptionPropagationPolicy(ExceptionPropagationPolicy.UNWRAP)
                    //.logger(new Logger.ErrorLogger())
                    .logger(new Slf4jLogger())
                    .logLevel(Logger.Level.BASIC)
                    .requestInterceptor(template -> {
                            template.header(
                            // 用你自己的token
                            "Authorization", 
                            "token " + System.getProperty("github_token", "123"));
                    })
                    .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
                    .target(GitHub.class, "https://api.github.com");
        }
    }

    public static void main(String... args) {
        final GitHub github = GitHub.connect();

        System.out.println("Let's fetch and print a list of the contributors to this org.");
        final List<String> contributors = github.contributors("OpenFeign");
        for(final String contributor : contributors) {
            System.out.println(contributor);
        }

        System.out.println("Now, let's cause an error.");
        try {
            github.contributors("openfeign", "some-unknown-project");
        } catch(final GitHubClientError e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Now, try to create an issue - which will also cause an error.");
        try {
            final Issue issue = new Issue();
            issue.setTitle("The title");
            issue.setBody("Some Text");
            github.createIssue(issue, "OpenFeign", "SomeRepo");
        } catch(final GitHubClientError e) {
            System.out.println(e.getMessage());
        }
    }


}

/*interface GitHub {
    
    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    // @RequestLine(value = "GET /repos/{owner}/{repo}/contributors", decodeSlash = false)// 测试转义"/"、"+"
    // @RequestLine("GET /repos/{owner:[a-zA-Z]*}/{repo}/contributors")// 测试正则校验
    List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);
}

public static class Contributor {

    String login;

    int contributions;
}

public class MyApp {

    public static void main(String... args) {
        GitHub github = Feign.builder()
                .decoder(new GsonDecoder()) // 配置解码器
                .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true)) // 配置超时等
                .target(GitHub.class, "https://api.github.com");

        // Fetch and print a list of the contributors to this library.
        github.contributors("OpenFeign", "feign").stream()
            .map(contributor -> contributor.login + " (" + contributor.contributions + ")")
            .forEach(System.out::println);
    }
}*/
