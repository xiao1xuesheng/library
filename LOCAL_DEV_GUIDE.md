# ELibrary 本地开发环境指南

## 一、当前环境配置

### 1.1 已配置的开发环境（无需外部依赖）

✅ **H2内存数据库** - 替代MySQL
- 访问地址: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:elibrary;DB_CLOSE_DELAY=-1;MODE=MySQL`
- 用户名: `sa`
- 密码: (空)

✅ **本地配置兜底** - 替代Nacos
- 所有配置已写入 `application.yaml`
- Nacos未配置时自动使用本地配置

✅ **Mock分布式锁** - 替代Redis
- `DistributedLockUtil` 已改为内存Map实现
- 开发环境可直接使用

✅ **Mock S3/Kafka** - 暂时不需要
- 书籍上传功能可后续添加
- 消息队列功能可后续添加

### 1.2 技术栈

- **Java**: 21
- **Spring Boot**: 4.1.1 (Spring Framework 7.0.9)
- **数据库**: H2 (开发) / MySQL 8.0 (生产)
- **ORM**: Spring Data JPA + Hibernate
- **配置中心**: Nacos (可选) / 本地配置兜底

## 二、如何启动项目

### 2.1 使用IDE启动（推荐）

1. 打开IDE（IntelliJ IDEA / Eclipse）
2. 导入Maven项目
3. 运行 `ELibraryApplication.java` 的 `main` 方法
4. 访问 http://localhost:8080

### 2.2 使用Maven命令启动

```bash
cd /Users/Admin/work/ELibrary
./mvnw spring-boot:run
```

### 2.3 访问H2控制台

启动后访问: http://localhost:8080/h2-console

连接信息：
- JDBC URL: `jdbc:h2:mem:elibrary`
- 用户名: `sa`
- 密码: (留空)

## 三、已实现的功能

### 3.1 核心功能

✅ 用户管理（注册、登录、查询）
✅ 书籍管理（列表、详情、搜索）
✅ 借阅管理（借阅、归还、续借）
✅ 账户管理（余额查询、充值）
✅ 支付管理（订单创建、支付回调）
✅ 对账管理（对账记录、差异处理）
✅ 操作日志（管理员操作记录）
✅ 分布式锁（定时任务防重复执行）

### 3.2 管理后台（BOS）

✅ 用户管理
✅ 书籍管理（上架、下架）
✅ 订单管理
✅ 对账管理（上传对账文件、执行对账）
✅ 操作日志查询

### 3.3 定时任务

✅ 借阅超期强制归还
✅ VIP到期提醒
✅ 余额不足提醒
✅ 对账任务（需配置）

## 四、API测试

### 4.1 用户注册

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 4.2 用户登录

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 4.3 查询书籍列表

```bash
curl http://localhost:8080/api/books?page=0&size=10
```

### 4.4 借阅书籍

```bash
curl -X POST http://localhost:8080/api/borrow \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1
  }'
```

## 五、数据库表结构

项目启动后会自动创建以下表：

- `t_user` - 用户表
- `t_book` - 书籍表
- `t_borrow_record` - 借阅记录表
- `t_order` - 订单表
- `t_account` - 账户表
- `t_account_balance_log` - 余额变动日志表
- `t_operation_log` - 操作日志表
- `t_reconciliation_report` - 对账报告表
- `t_reconciliation_record` - 对账记录表
- `t_platform_bill_record` - 支付平台账单记录表

## 六、常见问题

### Q1: Lombok不生效怎么办？

**解决方案1**: 使用IDE运行（IDE通常会自动配置Lombok）

**解决方案2**: 安装Lombok插件
- IntelliJ IDEA: Settings → Plugins → 搜索 "Lombok" → 安装
- Eclipse: 下载lombok.jar → 运行安装

**解决方案3**: 手动添加getter/setter（不推荐）

### Q2: 如何切换到MySQL？

修改 `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/elibrary?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

### Q3: 如何启用Nacos？

1. 安装Nacos: https://nacos.io/
2. 修改 `application.yaml`:

```yaml
nacos:
  server-addr: localhost:8848
  namespace: your-namespace
```

### Q4: 编译失败怎么办？

使用IDE直接运行，跳过Maven编译：
- IntelliJ IDEA: 直接运行 `ELibraryApplication`
- 或使用 `./mvnw spring-boot:run`

## 七、下一步开发计划

- [ ] 完善JWT鉴权
- [ ] 添加AWS S3集成（书籍上传）
- [ ] 添加Redis缓存
- [ ] 添加Kafka消息队列
- [ ] 完善阅读历史功能
- [ ] 完善数据看板功能
- [ ] 添加单元测试
- [ ] 添加集成测试

## 八、项目结构

```
ELibrary/
├── src/main/java/org/hejinggang/elibrary/
│   ├── common/          # 公共组件
│   │   ├── enums/       # 枚举类
│   │   ├── exception/   # 异常处理
│   │   └── response/    # 统一响应
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   │   ├── api/         # Web接口
│   │   └── bos/         # 管理后台接口
│   ├── model/           # 数据模型
│   │   ├── dto/         # 数据传输对象
│   │   └── entity/      # 实体类
│   ├── repository/      # 数据访问层
│   ├── service/         # 业务逻辑层
│   ├── task/            # 定时任务
│   └── util/            # 工具类
└── src/main/resources/
    └── application.yaml # 配置文件
```