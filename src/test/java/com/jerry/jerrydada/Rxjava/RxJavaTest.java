package com.jerry.jerrydada.Rxjava;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RxJavaTest {

    @Test
    public void test() throws InterruptedException {
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS)
                .map(i -> i + 1)
                .subscribeOn(Schedulers.io());//在io线程中执行

        // 订阅Flowable流，并打印出接收到的数组
        flowable
                .observeOn(Schedulers.io())
                .doOnNext(i -> System.out.println("Received: " + i))
                .subscribe();

        //主线程睡眠
        Thread.sleep(10000L);
    }
}
