# ELibrary - 电子图书馆管理系统

基于 Spring Boot 4.1.1 + JPA 的电子图书馆管理系统，支持用户注册登录、书籍浏览借阅、在线充值、评论笔记、管理后台等功能。

## 快速开始

### 启动项目

```bash
cd /Users/Admin/work/ELibrary
./mvnw spring-boot:run
```

或使用 IDE 直接运行 `ELibraryApplication.java`。

启动后访问: http://localhost:8080

### API 文档 (Swagger UI)

项目集成了 SpringDoc OpenAPI，启动后可访问:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

API 文档支持中英文双语描述，涵盖所有用户端 (`/api/*`) 和管理后台 (`/bos/*`) 接口。

### H2 控制台

开发环境使用 H2 内存数据库，启动后可访问: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:elibrary`
- 用户名: `sa`
- 密码: (留空)

## 文档索引

| 文档 | 说明 |
|------|------|
| [本地开发指南](LOCAL_DEV_GUIDE.md) | 环境配置、启动方式、API 测试、常见问题等 |
| [API 接口文档](docs/API/API接口文档.md) | 所有 RESTful API 接口详细说明 |
| [测试用例文档](docs/TEST/测试用例文档.md) | 单元测试用例清单和覆盖情况 |
| [设计文档](docs/) | 包含PRD，HLD，LLD等设计类的文档 |


## 技术栈

- **Java**: 21
- **Spring Boot**: 4.1.1
- **数据库**: H2 (开发) / MySQL 8.0 (生产)
- **ORM**: Spring Data JPA + Hibernate
- **配置中心**: Nacos (可选) / 本地配置兜底

## 核心功能

- 用户管理（注册、登录、VIP 管理）
- 书籍管理（上架、下架、搜索）
- 借阅管理（借阅、归还、续借）
- 账户管理（余额充值、消费记录）
- 支付管理（订单创建、支付回调）
- 评论与笔记
- 对账管理
- 管理后台（BOS）

## 项目结构

```
ELibrary/
├── src/main/java/org/hejinggang/elibrary/
│   ├── common/          # 公共组件（枚举、异常、统一响应）
│   ├── config/          # 配置类
│   ├── controller/      # 控制器（web / bos）
│   ├── model/           # 数据模型（DTO / Entity）
│   ├── repository/      # 数据访问层
│   ├── service/         # 业务逻辑层
│   ├── task/            # 定时任务
│   └── util/            # 工具类
├── src/main/resources/
│   └── application.yaml
├── docs/                # 设计文档
├── LOCAL_DEV_GUIDE.md   # 本地开发指南
└── README.md
```

## 参考文档

- [Apache Maven 官方文档](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
- [Spring Web 指南](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)