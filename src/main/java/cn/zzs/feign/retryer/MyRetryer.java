package cn.zzs.feign.retryer;

import feign.RetryableException;
import feign.Retryer;


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
