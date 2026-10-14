#!/bin/bash

# ============================================
# ELibrary 演示自动化脚本
# 用途：一键完成完整的演示流程
# 依赖：curl, jq
# ============================================

BASE_URL="http://localhost:8080"

echo "========================================="
echo "  ELibrary 演示脚本"
echo "========================================="
echo ""

# 检查服务是否启动
echo "【检查】检查服务是否启动..."
if curl -s -o /dev/null -w "%{http_code}" $BASE_URL/api/books | grep -q "200"; then
  echo "✓ 服务正常运行"
else
  echo "✗ 服务未启动，请先运行: ./mvnw spring-boot:run"
  exit 1
fi
echo ""

# 1. 登录
echo "========================================="
echo "【步骤 1】用户登录"
echo "========================================="
echo "用户名: demo_user"
echo "密码: 123456"
echo ""

LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "demo_user", "password": "123456"}')

echo "响应:"
echo "$LOGIN_RESPONSE" | jq .

# 提取 Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.data.userId')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "✗ 登录失败，请检查用户名和密码"
  exit 1
fi

echo ""
echo "✓ 登录成功，用户ID: $USER_ID"
echo ""

# 2. 浏览书籍列表
echo "========================================="
echo "【步骤 2】浏览书籍列表"
echo "========================================="
echo ""

echo "--- 2.1 查看所有书籍 ---"
curl -s -X GET "$BASE_URL/api/books?page=1&size=20" | jq .
echo ""

echo "--- 2.2 搜索关键词 'Java' ---"
curl -s -X GET "$BASE_URL/api/books?keyword=Java" | jq .
echo ""

echo "--- 2.3 按分类筛选 '编程语言' ---"
curl -s -X GET "$BASE_URL/api/books?category=编程语言" | jq .
echo ""

# 3. 查看书籍详情
echo "========================================="
echo "【步骤 3】查询书籍详细资料"
echo "========================================="
echo ""

echo "--- 3.1 查看《Java核心技术 卷I》详情 ---"
curl -s -X GET $BASE_URL/api/books/1 | jq .
echo ""

echo "--- 3.2 查看《设计模式》详情 ---"
curl -s -X GET $BASE_URL/api/books/3 | jq .
echo ""

# 4. 借阅书籍
echo "========================================="
echo "【步骤 4】借阅书籍"
echo "========================================="
echo ""

echo "--- 4.1 借阅《Spring实战 第6版》(ID: 2) ---"
BORROW_RESPONSE=$(curl -s -X POST $BASE_URL/api/borrow \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"bookId": 2, "requestId": "demo-borrow-001"}')

echo "响应:"
echo "$BORROW_RESPONSE" | jq .

BORROW_ID=$(echo $BORROW_RESPONSE | jq -r '.data.id')
echo ""
echo "✓ 借阅成功，借阅记录ID: $BORROW_ID"
echo ""

echo "--- 4.2 借阅《数据结构与算法分析》(ID: 5) ---"
BORROW_RESPONSE2=$(curl -s -X POST $BASE_URL/api/borrow \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"bookId": 5, "requestId": "demo-borrow-002"}')

echo "响应:"
echo "$BORROW_RESPONSE2" | jq .

BORROW_ID2=$(echo $BORROW_RESPONSE2 | jq -r '.data.id')
echo ""
echo "✓ 借阅成功，借阅记录ID: $BORROW_ID2"
echo ""

# 5. 查看我的借阅记录
echo "========================================="
echo "【步骤 5】查看目前已借阅的书籍"
echo "========================================="
echo ""

echo "--- 5.1 查看所有活跃借阅 ---"
curl -s -X GET "$BASE_URL/api/my/borrows" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 6. 归还书籍
echo "========================================="
echo "【步骤 6】归还书籍"
echo "========================================="
echo ""

echo "--- 6.1 归还《Java核心技术 卷I》(借阅记录ID: 1) ---"
curl -s -X POST $BASE_URL/api/borrow/1/return \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "--- 6.2 归还后再次查看借阅记录 ---"
curl -s -X GET "$BASE_URL/api/my/borrows" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 演示完成
echo "========================================="
echo "  ✓ 演示完成！"
echo "========================================="
echo ""
echo "演示流程总结："
echo "  1. ✓ 用户登录"
echo "  2. ✓ 浏览书籍列表（支持搜索、筛选）"
echo "  3. ✓ 查询书籍详细资料"
echo "  4. ✓ 借阅书籍"
echo "  5. ✓ 查看已借阅的书籍"
echo "  6. ✓ 归还书籍"
echo ""