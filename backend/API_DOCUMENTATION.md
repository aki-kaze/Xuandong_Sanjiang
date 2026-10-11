# 寻侗三江旅游平台后端服务

## 项目简介
基于 Spring Boot + MyBatis 的旅游平台后端服务，提供景点、非遗、美食、酒店、路线、门票、订单、用户、社区等功能的 RESTful API。

## 技术栈
- Spring Boot 2.7.18
- MyBatis 2.3.2
- MySQL 8.0
- PageHelper 分页插件
- Lombok

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS xundong_sanjiang DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 导入数据库表结构
```bash
mysql -u root -p xundong_sanjiang < database/schema.sql
```

### 3. 导入测试数据（可选）
```bash
mysql -u root -p xundong_sanjiang < database/insert_data.sql
```

### 4. 配置数据库连接
编辑 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/xundong_sanjiang?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

## 项目结构
```
src/main/java/org/example/sanjiang/
├── common/              # 公共类
│   └── Result.java      # 统一响应结果
├── config/              # 配置类
│   └── CorsConfig.java  # 跨域配置
├── controller/          # 控制器层
├── entity/              # 实体类
├── mapper/              # Mapper 接口
├── service/             # 服务接口
│   └── impl/            # 服务实现
└── SanjiangApplication.java  # 启动类

src/main/resources/
├── mapper/              # MyBatis XML 映射文件
└── application.properties  # 配置文件
```

## 功能模块

### 1. 景点管理 (Attraction)
- GET /api/attractions - 分页查询景点列表
- GET /api/attractions/{id} - 查询景点详情
- POST /api/attractions - 添加景点
- PUT /api/attractions - 更新景点
- DELETE /api/attractions/{id} - 删除景点

### 2. 非遗管理 (Heritage)
- GET /api/heritage - 分页查询非遗列表
- GET /api/heritage/{id} - 查询非遗详情
- POST /api/heritage - 添加非遗
- PUT /api/heritage - 更新非遗
- DELETE /api/heritage/{id} - 删除非遗

### 3. 美食管理 (Food)
- GET /api/foods - 分页查询美食列表
- GET /api/foods/{id} - 查询美食详情
- POST /api/foods - 添加美食
- PUT /api/foods - 更新美食
- DELETE /api/foods/{id} - 删除美食

### 4. 酒店管理 (Hotel)
- GET /api/hotels - 分页查询酒店列表
- GET /api/hotels/{id} - 查询酒店详情
- POST /api/hotels - 添加酒店
- PUT /api/hotels - 更新酒店
- DELETE /api/hotels/{id} - 删除酒店

### 5. 路线管理 (Route)
- GET /api/routes - 分页查询路线列表
- GET /api/routes/{id} - 查询路线详情
- POST /api/routes - 添加路线
- PUT /api/routes - 更新路线
- DELETE /api/routes/{id} - 删除路线

### 6. 门票管理 (Ticket)
- GET /api/tickets - 分页查询门票列表
- GET /api/tickets/{id} - 查询门票详情
- POST /api/tickets - 添加门票
- PUT /api/tickets - 更新门票
- DELETE /api/tickets/{id} - 删除门票

### 7. 订单管理 (Order)
- GET /api/orders - 分页查询订单列表
- GET /api/orders/{id} - 查询订单详情
- POST /api/orders - 创建订单
- POST /api/orders/pay/{orderNo} - 支付订单
- POST /api/orders/cancel/{orderNo} - 取消订单
- POST /api/orders/use/{orderNo} - 使用订单
- DELETE /api/orders/{id} - 删除订单

### 8. 用户管理 (User)
- GET /api/users - 分页查询用户列表
- GET /api/users/{id} - 查询用户详情
- POST /api/users/login - 用户登录
- GET /api/users/info - 获取用户信息
- PUT /api/users - 更新用户
- DELETE /api/users/{id} - 删除用户
- GET /api/users/count - 统计用户数量

### 9. 社区帖子 (Post)
- GET /api/posts - 分页查询帖子列表
- GET /api/posts/{id} - 查询帖子详情
- POST /api/posts - 发布帖子
- PUT /api/posts - 更新帖子
- DELETE /api/posts/{id} - 删除帖子
- POST /api/posts/approve/{id} - 审核通过
- POST /api/posts/reject/{id} - 审核拒绝

### 10. 活动管理 (Activity)
- GET /api/activities - 分页查询活动列表
- GET /api/activities/{id} - 查询活动详情
- POST /api/activities - 添加活动
- PUT /api/activities - 更新活动
- DELETE /api/activities/{id} - 删除活动
- POST /api/activities/signup/{id} - 报名活动

### 11. 动态管理 (Moment)
- GET /api/moments - 分页查询动态列表
- GET /api/moments/status/{status} - 按状态查询动态
- GET /api/moments/{id} - 查询动态详情
- POST /api/moments - 发布动态
- PUT /api/moments - 更新动态
- DELETE /api/moments/{id} - 删除动态
- POST /api/moments/like/{id} - 点赞动态
- POST /api/moments/comment/{id} - 评论动态
- POST /api/moments/approve/{id} - 审核通过
- POST /api/moments/reject/{id} - 审核拒绝
- GET /api/moments/count/pending - 统计待审核数量

### 12. 行程管理 (Journey)
- GET /api/journeys - 分页查询行程列表
- GET /api/journeys/user/{userId} - 查询用户行程
- GET /api/journeys/{id} - 查询行程详情
- POST /api/journeys - 添加行程
- PUT /api/journeys - 更新行程
- DELETE /api/journeys/{id} - 删除行程

### 13. 管理员 (Admin)
- POST /api/admin/login - 管理员登录
- GET /api/admin/info - 获取管理员信息

### 14. 数据统计 (Dashboard)
- GET /api/dashboard/stats - 获取统计数据

## 启动项目

### 1. 使用 Maven 启动
```bash
mvn spring-boot:run
```

### 2. 使用 IDE 启动
运行 `SanjiangApplication.java` 的 main 方法

### 3. 打包部署
```bash
mvn clean package
java -jar target/sanjiang-backend-1.0.0.jar
```

## 访问地址
- 本地访问：http://localhost:8081
- API 文档：所有接口返回统一格式的 JSON 数据

## 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 分页参数
- page: 页码，默认 1
- size: 每页数量，默认 10

## 注意事项
1. 确保 MySQL 服务已启动
2. 确保数据库连接配置正确
3. 首次运行需要先创建数据库并导入表结构
4. 默认端口为 8081，可在 application.properties 中修改

