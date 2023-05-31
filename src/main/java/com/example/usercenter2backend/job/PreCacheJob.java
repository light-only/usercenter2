package com.example.usercenter2backend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    //重点用户
    /**
     * 这段代码创建了一个包含一个元素的 List 对象，该元素为 Long 类型的数字 1。注意，这里的 "l" 表示 long 类型，因为 Java 中的整数字面量默认为 int 类型，如果要表示 long 类型的数字，需要在数字后面加上 "l" 或 "L"。另外，Arrays.asList() 方法是将数组转换为 List 的方法，因此在这个例子中，它将一个 Long 类型的数组转换成了一个 List 对象。
     */
    private List<Long> mainUserList = Arrays.asList(1l);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 45 16 * * *")
    public void doCacheRecommendUser(){
        for (Long userId: mainUserList){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
            String redisKey = String.format("leo:user:recommend:%s", userId);
            ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
            //写缓存
            try {
                valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
            }catch (Exception e){
                log.error("redis set key error",e);
            }
        }
    }

}
