package com.example.usercenter2backend.service;

import com.example.usercenter2backend.model.domain.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 用户服务测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Resource UserService userService;

    @Test
    public void addUser() {
        User user = new User();
        user.setUserName("leo");
        user.setUserAccount("leo22");
        user.setAvatarUrl("https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setEmail("1480418586@qq.com");
        user.setUserStatus(0);
        user.setPhone("13949523280");
        user.setPlanetCode("123");
        user.setTags("1");
        user.setProfile("这是一段简介...");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    public void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        //测试其中一个为空
        long result = userService.userRegister(userAccount,userPassword,checkPassword,planetCode );
        Assertions.assertEquals(-1,result);

        //测试账户小于4
        userAccount = "yu";
        result = userService.userRegister(userAccount,userPassword,checkPassword, planetCode);

        //测试密码小于8
        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount,userPassword,checkPassword, planetCode);

        //测试账户是否有特殊字符
        userAccount = "yu pi";
        userPassword = "12345678";
        result =userService.userRegister(userAccount,userPassword,checkPassword, planetCode);

        //测试密码和检验密码是否相同
        checkPassword = "123456789";
        result =userService.userRegister(userAccount,userPassword,checkPassword, planetCode);

        //判断是否已经存在该用户
        userAccount = "leo22";
        checkPassword = "12345678";

        userAccount = "yupi3";
        result =userService.userRegister(userAccount,userPassword,checkPassword, planetCode);

        Assertions.assertTrue(result>0);

    }
}