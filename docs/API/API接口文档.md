# ELibrary API 接口文档

> 电子图书馆管理系统 RESTful API 文档
> 
> Base URL: `http://localhost:8080`
> 
> 版本: v1.0

## 目录

- [认证说明](#认证说明)
- [统一响应格式](#统一响应格式)
- [用户端 API](#用户端-api)
  - [认证模块](#1-认证模块-apiauth)
  - [书籍模块](#2-书籍模块-apibooks)
  - [借阅模块](#3-借阅模块-api)
  - [支付模块](#4-支付模块-apipayment)
  - [备注模块](#5-备注模块-api)
  - [阅读模块](#6-阅读模块-api)
  - [评论模块](#7-评论模块-api)
- [管理后台 API](#管理后台-api)
  - [管理员认证](#8-管理员认证-bosauth)
  - [书籍管理](#9-书籍管理-bosbooks)
  - [用户管理](#10-用户管理-bosusers)
  - [评论管理](#11-评论管理-bosreviews)
  - [对账管理](#12-对账管理-bosreconciliation)
  - [仪表盘](#13-仪表盘-bosdashboard)

---

## 认证说明

### JWT Token 认证

除公开接口（书籍列表、书籍详情、用户注册/登录）外，所有接口需要在请求头中携带 JWT Token：

```
Authorization: Bearer <token>
```

### Token 获取

通过 `/api/auth/login` 或 `/api/auth/register` 接口获取 Token。

---

## 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 状态码，0 表示成功，非 0 表示错误 |
| message | string | 提示信息 |
| data | object | 业务数据（可选） |

---

## 用户端 API

### 1. 认证模块 `/api/auth`

#### 1.1 用户注册

```
POST /api/auth/register
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| email | string | 是 | 邮箱 |
| password | string | 是 | 密码 |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "username": "testuser",
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

#### 1.2 用户登录

```
POST /api/auth/login
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "username": "testuser",
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

#### 1.3 获取当前用户信息

```
GET /api/auth/profile
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "userType": "NORMAL",
    "userStatus": "NORMAL",
    "balance": 100.00,
    "vipExpireAt": null
  }
}
```

---

### 2. 书籍模块 `/api/books`

#### 2.1 分页浏览书籍

```
GET /api/books?keyword=&author=&category=&sortBy=createdAt&sortOrder=desc&page=1&size=20
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 否 | - | 书名关键词 |
| author | string | 否 | - | 作者 |
| category | string | 否 | - | 分类 |
| sortBy | string | 否 | createdAt | 排序字段 |
| sortOrder | string | 否 | desc | 排序方向（asc/desc） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 20 | 每页数量 |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "items": [
      {
        "id": 1,
        "title": "Java Core Technology",
        "author": "Cay S. Horstmann",
        "isbn": "978-7-111-11111-1",
        "category": "Tech",
        "coverUrl": "https://example.com/cover.jpg",
        "rating": 4.5,
        "reviewCount": 10,
        "borrowCount": 5
      }
    ]
  }
}
```

#### 2.2 获取书籍详情

```
GET /api/books/{id}
Authorization: Bearer <token>  (可选)
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 书籍ID |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Java Core Technology",
    "author": "Cay S. Horstmann",
    "description": "Java核心技术详解",
    "isbn": "978-7-111-11111-1",
    "category": "Tech",
    "pageCount": 500,
    "coverUrl": "https://example.com/cover.jpg",
    "rating": 4.5,
    "reviewCount": 10,
    "borrowCount": 5,
    "publishedDate": "2023-01-01",
    "userPermission": "NONE"
  }
}
```

---

### 3. 借阅模块 `/api`

#### 3.1 借阅书籍

```
POST /api/borrow
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookId | long | 是 | 书籍ID |
| requestId | string | 是 | 幂等请求ID |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "bookId": 1,
    "bookTitle": "Java Core Technology",
    "status": "ACTIVE",
    "borrowDate": "2024-01-01T10:00:00",
    "dueDate": "2024-01-08T10:00:00",
    "totalCost": 0.00
  }
}
```

#### 3.2 归还书籍

```
POST /api/borrow/{id}/return
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 借阅记录ID |

#### 3.3 续借

```
POST /api/borrow/{id}/renew?requestId=xxx
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | 是 | 借阅记录ID |

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| requestId | string | 否 | 幂等请求ID |

#### 3.4 查看我的借阅记录

```
GET /api/my/borrows?status=
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 借阅状态（ACTIVE/RETURNED/OVERDUE） |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "Java Core Technology",
      "status": "ACTIVE",
      "borrowDate": "2024-01-01T10:00:00",
      "dueDate": "2024-01-08T10:00:00"
    }
  ]
}
```

---

### 4. 支付模块 `/api/payment`

#### 4.1 创建支付订单

```
POST /api/payment/order
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productType | string | 是 | 产品类型（BALANCE/VIP_MONTH/VIP_QUARTER/VIP_YEAR） |
| productId | long | 否 | 产品ID |
| amount | decimal | 是 | 金额 |

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderNo": "ORD123456",
    "amount": 100.00,
    "expireTime": "2024-01-01T10:30:00"
  }
}
```

#### 4.2 支付回调处理

```
POST /api/payment/callback
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 是 | 订单号 |
| payChannel | string | 是 | 支付渠道（ALIPAY/WECHAT） |
| payOrderNo | string | 是 | 支付平台订单号 |

---

### 5. 备注模块 `/api`

#### 5.1 创建备注

```
POST /api/books/{bookId}/notes
Authorization: Bearer <token>
Content-Type: application/json
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookId | long | 是 | 书籍ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 备注内容 |
| page | int | 是 | 页码 |

#### 5.2 查看书籍备注

```
GET /api/books/{bookId}/notes?page=
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码筛选 |

#### 5.3 查看我的所有备注

```
GET /api/notes
Authorization: Bearer <token>
```

#### 5.4 编辑备注

```
PUT /api/notes/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 新备注内容 |

#### 5.5 删除备注

```
DELETE /api/notes/{id}
Authorization: Bearer <token>
```

---

### 6. 阅读模块 `/api`

#### 6.1 保存阅读进度

```
POST /api/reading/progress?bookId=1&page=50&chapter=Chapter%203&scrollPosition=200&readDuration=120&deviceType=web
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookId | long | 是 | 书籍ID |
| page | int | 是 | 当前页码 |
| chapter | string | 否 | 当前章节 |
| scrollPosition | int | 否 | 滚动位置 |
| readDuration | int | 否 | 阅读时长（秒） |
| deviceType | string | 否 | 设备类型 |

#### 6.2 获取阅读进度

```
GET /api/reading/progress/{bookId}
Authorization: Bearer <token>
```

#### 6.3 获取阅读历史

```
GET /api/reading/history?page=1&size=20
Authorization: Bearer <token>
```

---

### 7. 评论模块 `/api`

#### 7.1 发表评论

```
POST /api/books/{bookId}/reviews
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 评论内容 |
| rating | int | 是 | 评分（1-5） |
| pageReference | string | 否 | 页码引用 |
| requestId | string | 是 | 幂等请求ID |

#### 7.2 查看书籍评论

```
GET /api/books/{bookId}/reviews?page=1&size=20
```

#### 7.3 编辑评论

```
PUT /api/reviews/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 新评论内容 |

#### 7.4 删除评论

```
DELETE /api/reviews/{id}
Authorization: Bearer <token>
```

---

## 管理后台 API

### 8. 管理员认证 `/bos/auth`

#### 8.1 管理员登录

```
POST /bos/auth/login
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 管理员用户名 |
| password | string | 是 | 管理员密码 |

#### 8.2 创建管理员

```
POST /bos/auth/admin
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 新管理员用户名 |
| password | string | 是 | 新管理员密码 |
| role | string | 是 | 角色（ADMIN/OPERATOR） |

#### 8.3 管理员列表

```
GET /bos/auth/admins
Authorization: Bearer <token>
```

#### 8.4 修改管理员角色

```
PUT /bos/auth/admin/{adminId}/role
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role | string | 是 | 新角色 |

#### 8.5 禁用管理员

```
PUT /bos/auth/admin/{adminId}/disable
Authorization: Bearer <token>
```

---

### 9. 书籍管理 `/bos/books`

#### 9.1 创建书籍

```
POST /bos/books
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 书名 |
| author | string | 是 | 作者 |
| isbn | string | 是 | ISBN |
| category | string | 是 | 分类 |
| description | string | 否 | 描述 |
| pageCount | int | 是 | 页数 |
| publishedDate | string | 否 | 出版日期（yyyy-MM-dd） |
| coverUrl | string | 否 | 封面URL |
| contentUrl | string | 否 | 内容URL |
| previewUrl | string | 否 | 预览URL |

#### 9.2 更新书籍

```
PUT /bos/books/{id}
Authorization: Bearer <token>
```

#### 9.3 更新书籍状态

```
PUT /bos/books/{id}/status?status=ON_SHELF
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 是 | 状态（ON_SHELF/OFF_SHELF） |

#### 9.4 书籍列表（管理后台）

```
GET /bos/books?status=&category=&page=1&size=20
Authorization: Bearer <token>
```

#### 9.5 配置书籍访问权限

```
POST /bos/books/{bookId}/access-config
Authorization: Bearer <token>
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accessType | string | 是 | 访问类型（PUBLIC_FREE/VIP_ONLY/PAID） |
| dailyPrice | decimal | 否 | 每日价格 |
| vipDiscount | decimal | 否 | VIP折扣 |
| maxRenewTimes | int | 否 | 最大续借次数 |
| renewDaysFree | int | 否 | 免费用户续借天数 |
| renewDaysVip | int | 否 | VIP用户续借天数 |

---

### 10. 用户管理 `/bos/users`

#### 10.1 用户列表

```
GET /bos/users?userType=&userStatus=&page=1&size=20
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userType | string | 否 | 用户类型（NORMAL/VIP） |
| userStatus | string | 否 | 用户状态（NORMAL/DISABLED/MUTED） |
| page | int | 否 | 页码 |
| size | int | 否 | 每页数量 |

#### 10.2 修改用户类型

```
PUT /bos/users/{id}/type?userType=VIP
Authorization: Bearer <token>
```

#### 10.3 修改用户状态

```
PUT /bos/users/{id}/status?userStatus=DISABLED
Authorization: Bearer <token>
```

#### 10.4 查看用户借阅记录

```
GET /bos/users/{id}/borrows
Authorization: Bearer <token>
```

---

### 11. 评论管理 `/bos/reviews`

#### 11.1 评论列表

```
GET /bos/reviews?bookId=&page=1&size=20
Authorization: Bearer <token>
```

#### 11.2 管理员删除评论

```
DELETE /bos/reviews/{id}?reason=xxx
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 删除原因 |

---

### 12. 对账管理 `/bos/reconciliation`

#### 12.1 上传支付记录

```
POST /bos/reconciliation/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | CSV文件 |
| channel | string | 是 | 支付渠道（ALIPAY/WECHAT） |
| billDate | string | 是 | 账单日期（yyyy-MM-dd） |

#### 12.2 对账

```
POST /bos/reconciliation/execute?billDate=2024-01-01
Authorization: Bearer <token>
```

---

### 13. 仪表盘 `/bos/dashboard`

#### 13.1 获取仪表盘数据

```
GET /bos/dashboard
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalUsers": 1000,
    "totalBooks": 500,
    "activeBorrows": 200,
    "totalRevenue": 50000.00,
    "todayRevenue": 1000.00
  }
}
```

---

## 错误码列表

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 1001 | 用户名已存在 |
| 1002 | 用户不存在 |
| 1003 | 密码错误 |
| 1004 | 用户已禁用 |
| 1005 | 用户已禁言 |
| 2001 | 书籍已下架 |
| 2002 | 书籍不存在 |
| 3001 | 已借阅 |
| 3002 | 借阅记录不存在 |
| 3003 | 已归还 |
| 3004 | 借阅上限已超 |
| 3005 | 续借上限已超 |
| 3006 | 余额不足 |
| 3007 | 无借阅权限 |
| 3008 | 无阅读权限 |
| 4001 | 订单不存在 |
| 4002 | 订单已过期 |
| 5001 | 评论不存在 |
| 5002 | 非您的评论 |
| 6001 | 备注不存在 |
| 6002 | 非您的备注 |
| 7001 | 管理员不存在 |
| 7002 | 管理员已禁用 |

---

## 在线 API 文档

启动应用后可访问：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs