#Docker编写：
    # FROM :依赖的基础镜像
    # WORKDIR :工作目录
    # COPY 从本机复制文件
    # RUN 执行命令
    # CMD/ ENTRYPOINT (附加额外参数） 指定运行容器时默认的命令

FROM maven:3.5-jdk-8-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

#编译，并跳过测试
RUN mvn package -DskipTest

CMD ["java","-jar","/app/target/usercenter2Backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]