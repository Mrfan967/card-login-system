# 卡号登录系统

这是一个简单的卡号登录系统，满足以下业务需求：

1. 客户购买卡号
2. 管理员调用生成接口，获取卡号，交给客户，卡号从绑定日开始生效，有效期6个月
3. 客户打开软件登录页，输入卡号，点击【登录】，前端会把MAC地址和卡号发给后台接口进行绑定或登录
4. 绑定规则：1个卡号只能在1台机器上使用，换机器用，需要解绑后才能重新绑定
5. 可以查询卡号有效期及绑定状态

## 技术栈

- 后端：Spring Boot
- 前端：HTML, CSS, JavaScript

## 项目结构

```
card-login-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── cardlogin/
│   │   │               ├── controller/  - REST API控制器
│   │   │               ├── model/       - 数据模型
│   │   │               ├── service/     - 业务逻辑服务
│   │   │               └── CardLoginApplication.java - 主应用类
│   │   └── resources/
│   │       └── static/
│   │           └── index.html  - 前端登录页面
├── pom.xml  - Maven依赖管理
└── README.md
```

## 功能接口

1. 生成卡号：`POST /api/card/generate`
2. 登录：`POST /api/card/login`
3. 解绑：`POST /api/card/unbind`
4. 查询：`GET /api/card/query`

## 运行方法

1. 确保安装了Java 11或更高版本以及Maven
2. 在项目根目录执行：
   ```
   mvn spring-boot:run
   ```
3. 打开浏览器访问：http://localhost:8080

## 注意事项

- 本项目使用内存集合存储卡号信息，系统重启后数据会丢失
- 实际生产环境中应使用数据库持久化存储卡号信息
- 实际使用中应该有更安全的MAC地址获取方式，当前示例使用随机生成的MAC地址模拟 