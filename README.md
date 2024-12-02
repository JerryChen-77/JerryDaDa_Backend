# AI 答题应用平台



### 项目介绍
本项目是一个基于 Spring Boot 的答题应用平台
提供用户注册、登录、答题等功能。
用户可以注册账号，登录后可以参与答题，答题完成后可以查看自己的得分和排名。
项目还提供了文件上传和下载功能，用户可以上传自己的头像和应用图标，并在答题结束后下载结果图片。

### 功能介绍
- 用户注册和登录
- 答题功能
- 文件上传和下载
- 用户答题和展示
- 管理员审核功能

### 技术栈
- Spring Boot
- MyBatis/PLUS
- MySQL
- Redis
- ChatGLM

## 如何启动项目

### MySQL 数据库
修改 `application.yml` 的数据库配置为你自己的：

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/my_db
    username: root
    password: 123456
```

### Redis配置
修改 `application.yml` 的数据库配置为你自己的：
如果没有密码请注释掉，否则会报错
```yml
redis:
database: 1
host: localhost
port: 6379
timeout: 5000
#    password: 123456
```

### ChatGLM 智能谱AI接口调用
修改 `application.yml` 的API配置为你自己的：
```yml
ai:
apiKey: xxx
```

### COS对象存储
修改 `application.yml` 的COS配置为你自己的：
```yml
cos:
  client:
    accessKey: xxx
    secretKey: xxx
    region: xxx
    bucket: xxx
```

## 界面展示:

暂未开放完全，敬请期待