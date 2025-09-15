# 苍穹外卖项目

## 项目概述

苍穹外卖是一个为餐饮企业（如餐厅、饭店）定制的在线外卖订购系统。该项目旨在为餐饮企业和消费者提供高效、便捷的外卖解决方案，涵盖了从菜品管理、订单处理到配送跟踪的全业务流程。系统采用前后端分离的开发模式，前端使用Vue.js框架构建用户界面，后端基于Spring Boot框架搭建服务层，数据库选用MySQL进行数据存储。

## 技术栈

### 后端技术

- **框架**: Spring Boot 2.7.x（集成Spring MVC, MyBatis）
- **安全框架**: Spring Security, JWT (身份验证与授权)
- **数据库**: MySQL 8.0.x
- **缓存**: Redis 6.2+（用于缓存数据，如用户会话、热销菜品，提升系统响应速度）
- **接口文档**: Swagger / Knife4j
- **其他工具**:
  - Apache HttpClient (处理HTTP请求，与第三方服务通信)
  - Apache POI (操作Excel文件，用于数据导出)
  - 阿里云OSS (对象存储服务，存储图片等文件)
  - WebSocket (实现来单提醒、催单等实时通信)
  - Spring Task (定时任务，如处理超时未支付订单)

### 前端技术

- **管理端**: Vue.js + ElementUI
- **用户端**: 微信小程序
- **状态管理**: Pinia (Vuex的替代方案)
- **HTTP客户端**: Axio
- **构建工具**: Vite

### 部署与运维

- **网关**: Nginx (反向代理、负载均衡、静态资源部署)
- **容器化**: Docker
- **容器编排**: Kubernetes
- **日志系统**: ELK Stack (Elasticsearch, Logstash, Kibana)

## 功能模块

苍穹外卖系统主要分为**管理端**（供餐饮企业内部员工使用）和**用户端**（供消费者使用）两大模块。

### 1. 管理端功能

- **员工管理**: 员工信息的查询、新增、编辑、禁用及权限分配。
- **分类管理**: 对菜品分类或套餐分类进行管理维护（查询、新增、修改、删除）。
- **菜品管理**: 维护各个分类下的菜品信息（查询、新增、修改、删除、启售、停售），包括菜品口味管理。
- **套餐管理**: 维护餐厅中的套餐信息（查询、新增、修改、删除、启售、停售）。
- **订单管理**: 维护用户订单（查询、取消、派送、完成），订单报表下载。
- **数据统计**: 对营业额、用户增长、订单趋势等多维度数据进行统计，生成报表。
- **来单提醒**: 通过WebSocket实现新订单语音播报，提升接单效率。

### 2. 用户端功能 (微信小程序)

- **微信登录**: 用户通过微信授权快速登录。
- **菜品浏览**: 按分类展示菜品及套餐，支持规格查询、收藏与加入购物车。
- **购物车管理**: 实现菜品的添加、数量调整、删除及一键清空。
- **订单支付**: 集成微信支付，支持订单结算、支付状态查询及退款申请。
- **个人中心**: 管理收货地址、查看历史订单、进行账号安全设置。
- **用户催单**: 支付后若商家久未接单，用户可点击催单提醒商家。

### 3. 骑手端功能 (微信小程序)

- **接单与派送**: 骑手可以接收订单、进行派送并进行路线规划。
- **查找订单**: 系统会为订单查找商家一定距离（如10公里）内的在职骑手进行派单。

## 系统架构与设计

### 分层架构设计

- **用户层**:
  - 管理端：基于Vue.js的Web界面。
  - 用户端：微信小程序。
  - 骑手端：微信小程序。
- **网关层**: Nginx作为反向代理服务器，实现请求转发、负载均衡及静态资源部署。
- **应用层**:
  - Controller层：处理前端请求，调用Service层逻辑。
  - Service层：实现核心业务逻辑。
  - Mapper层：操作数据库。
- **数据层**:
  - MySQL：存储核心业务数据。
  - Redis：存储缓存数据（如用户会话、热销菜品）。

### 数据库设计

数据库名为 `sky_take_out`，包含11张核心表：

| 表名            | 说明           | 核心字段                                                     |
| :-------------- | :------------- | :----------------------------------------------------------- |
| `employee`      | 员工表         | id, username, password (MD5加密), name, status (状态: 1启用, 0禁用) |
| `category`      | 分类表         | id, name (分类名称), type (类型: 1菜品分类, 2套餐分类), sort (排序优先级) |
| `dish`          | 菜品表         | id, name, category_id, price, status (状态: 1启售, 0停售), image (图片路径) |
| `dish_flavor`   | 菜品口味表     | id, dish_id, name (口味名称), value (口味值)                 |
| `setmeal`       | 套餐表         | id, name, category_id, price, status, image                  |
| `setmeal_dish`  | 套餐菜品关系表 | id, setmeal_id, dish_id, number (菜品数量)                   |
| `user`          | 用户表         | id, openid (微信用户唯一标识), name, phone, address, create_time |
| `address_book`  | 地址表         | id, user_id, consignee (收货人), phone, detail (详细地址), is_default |
| `shopping_cart` | 购物车表       | id, user_id, dish_id, setmeal_id, number, create_time        |
| `orders`        | 订单表         | id, user_id, employee_id, status (订单状态), amount, order_time, checkout_time |
| `order_detail`  | 订单明细表     | id, order_id, dish_id, setmeal_id, name, number, amount      |

*(此表简化了一些不重要的信息)*

## 环境配置与启动

### 开发环境要求

- JDK 11+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+ (前端运行)
- Maven 3.6+

### 数据库初始化

1. 新建MySQL数据库，命名为 `sky_take_out`。

2. 执行项目提供的SQL脚本初始化表结构及测试数据。

3. 修改后端配置文件 `application-dev.yml` 中的数据库连接信息：

   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://你的主机:端口/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
       username: 你的用户名
       password: 你的密码
   ```

### Redis配置

在 `application-dev.yml` 中配置Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: # 如果设置了密码
    database: 0 # 默认使用0号数据库
```

### 项目启动步骤

**后端启动 (sky-server):**

1. 克隆项目到本地。
2. 进入后端目录: `cd sky-server`。
3. 编译并启动: `mvn spring-boot:run`。
4. 访问接口文档: `http://localhost:8080/doc.html` (Swagger/Knife4j)。

**前端启动 (sky-front):**

黑马已经打包部署到`nginx`服务器上，直接双击`nginx.exe`即可

## 项目特色与亮点

1. **前后端分离架构**: 清晰的分层设计，便于开发和维护。
2. **微信小程序集成**: 用户端和骑手端均基于微信小程序，方便用户使用和传播。
3. **实时通信**: 利用WebSocket实现来单语音提醒和客户催单，提升响应速度。
4. **智能派单**: 系统可根据骑手位置（如10公里内）、订单量等因素为订单智能分派骑手。
5. **数据缓存**: 使用Redis缓存热点数据（如菜品信息、分类数据），显著提升系统性能。
6. **安全可靠**: 使用JWT进行身份认证，MD5加密密码，保障系统安全。
7. **定时任务**: 使用Spring Task处理超时未支付订单等定时业务。

## 注意事项

1. 开发环境中需确保Redis服务已启动，否则可能导致连接错误。
2. 前端页面依赖后端接口，启动时应先确保后端服务正常运行。
3. 生产环境部署时，需关闭Swagger文档接口，并妥善配置数据库密码、Redis密码等敏感信息。
4. 微信支付、短信通知等功能需配置相关的第三方密钥和参数。
5. 项目中的地址解析、配送费计算等功能可能依赖高德地图等第三方API。

## 参与贡献

1. Fork 本仓库。
2. 创建特性分支: `git checkout -b feature/xxx`。
3. 提交修改: `git commit -m '添加xxx功能'`。
4. 推送分支: `git push origin feature/xxx`。
5. 提交 Pull Request。

## 联系方式

- 项目维护者: xwybq(邮箱: [w3290819205@gmail.com](https://mailto:w3290819205@gmail.com))
- 问题反馈: 请在GitHub Issues提交。
