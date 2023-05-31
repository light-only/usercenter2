package com.example.usercenter2backend.service;

import com.example.usercenter2backend.model.domain.User;
import org.junit.ComparisonFailure;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;
    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("leoo" + i);
            user.setUserAccount("leoo" +i);
            user.setAvatarUrl("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
            user.setUserPassword("12345678");
            user.setPlanetCode("1" + i);
            user.setGender(0);
            user.setPhone("13949523280");
            user.setUserRole(0);
            user.setUserStatus(0);
            user.setEmail("1480418587@qq.com");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

    /**
     * 并发执行的方式写测试代码
     * 这里的线程可自定义或者用idea默认的，两种方法的区别是，自定义可以跑满线程，
     * 而默认的只能跑CPU核数-1，代码区别：就是在异步执行处加上自定义的线程名
     */
    @Test
    public void doConcurrencyInsertUsers() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //分10组
        int batchSize = 5000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUserName("leoo" + i);
                user.setUserAccount("leoo" +i);
                user.setAvatarUrl("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
                user.setUserPassword("12345678");
                user.setPlanetCode("1" + i);
                user.setGender(0);
                user.setPhone("13949523280");
                user.setUserRole(0);
                user.setUserStatus(0);
                user.setEmail("1480418587@qq.com");
                user.setTags("[]");
                userList.add(user);
                if(j % batchSize == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
                System.out.println("threadName" + Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
