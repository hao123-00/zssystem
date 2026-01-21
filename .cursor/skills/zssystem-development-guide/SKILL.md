---
name: zssystem-development-guide
description: Provides development guidelines and patterns for the Injection Molding Department Management System (注塑部管理系统). Use when developing features, reviewing code, or implementing modules for user management, equipment management, production management, quality management, or any other system modules. Follows Spring Boot + React architecture with MyBatis-Plus and Ant Design.
---

# 注塑部管理系统开发指南

## 系统概述

注塑部管理系统是一套面向注塑生产车间的综合性管理平台，采用 B/S 架构，包含用户管理、权限管理、人员管理、生产管理、设备管理、模具管理、物料管理、品质管理、现场5S管理等模块。

## 技术架构

### 前端技术栈
- **框架**: React 18.x + TypeScript
- **UI组件库**: Ant Design 5.x
- **状态管理**: React Hooks / Context API
- **路由管理**: React Router 6.x
- **HTTP请求**: Axios
- **构建工具**: Vite

### 后端技术栈
- **框架**: Spring Boot 3.x
- **编程语言**: Java 17
- **安全框架**: Spring Security + JWT
- **ORM框架**: MyBatis-Plus
- **API文档**: Swagger / Knife4j
- **工具类库**: Hutool

### 数据库
- **数据库**: MySQL 8.0+
- **连接池**: HikariCP

## 开发规范

### 代码规范
- 前端：遵循 ESLint 配置规范，使用 Prettier 格式化
- 后端：遵循阿里巴巴 Java 开发手册规范

### API设计规范
- RESTful API 设计风格
- 统一响应格式：`{code, message, data}`
- 统一异常处理机制
- 使用HTTP状态码表示请求结果

### 数据库命名规范
- 表名：小写字母+下划线，如 `user_info`、`equipment_check`
- 字段名：小写字母+下划线，如 `user_name`、`check_date`
- 主键：统一使用 `id`，类型为 `bigint`，自增
- 时间字段：统一使用 `datetime` 类型
- 创建时间：`create_time`
- 更新时间：`update_time`
- 删除标志：`deleted`（逻辑删除，0-未删除，1-已删除）

## 项目结构

### 前端项目结构
```
frontend/src/
├── api/              # API接口定义
├── components/       # 公共组件
├── pages/           # 页面组件
├── routes/          # 路由配置
├── store/           # 状态管理
├── utils/           # 工具函数
└── assets/          # 静态资源
```

### 后端项目结构
```
backend/src/main/java/com/zssystem/
├── controller/    # 控制器层
├── service/       # 业务逻辑层
├── mapper/        # 数据访问层
├── entity/        # 实体类
├── dto/           # 数据传输对象
├── vo/            # 视图对象
├── config/        # 配置类
└── common/        # 公共类
```

## 核心开发模式

### 1. 实体类设计
使用 MyBatis-Plus 注解：
```java
@Data
@TableName("table_name")
public class Entity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

### 2. Controller层模式
```java
@RestController
@RequestMapping("/api/module")
public class ModuleController {
    @Autowired
    private ModuleService service;
    
    @GetMapping("/list")
    public Result<PageResult<ModuleVO>> getList(@Validated QueryDTO dto) {
        IPage<ModuleVO> page = service.getList(dto);
        return Result.success(PageResult.of(page));
    }
    
    @PostMapping
    public Result<Void> create(@Validated @RequestBody SaveDTO dto) {
        service.create(dto);
        return Result.success();
    }
}
```

### 3. Service层模式
```java
@Service
public class ModuleServiceImpl implements ModuleService {
    @Autowired
    private ModuleMapper mapper;
    
    @Override
    @Transactional
    public void create(SaveDTO dto) {
        // 1. 参数校验
        // 2. 业务逻辑处理
        // 3. 数据保存
        // 4. 关联数据处理
    }
}
```

### 4. 前端页面模式
```typescript
const ModuleList: React.FC = () => {
  const [form] = Form.useForm();
  const [tableData, setTableData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const fetchList = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const params = {
        ...values,
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
      };
      const response = await getModuleList(params);
      setTableData(response.data.list);
      setPagination({ ...pagination, total: response.data.total });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Form form={form} layout="inline">
        {/* 查询条件 */}
      </Form>
      <Table
        columns={columns}
        dataSource={tableData}
        loading={loading}
        pagination={pagination}
      />
    </div>
  );
};
```

## 功能模块开发要点

### 用户登录
- JWT Token 认证
- BCrypt 密码加密
- Token 有效期 2 小时，支持刷新
- 登录失败统一提示"用户名或密码错误"

### 用户管理
- 用户名唯一性校验
- 密码长度不少于 6 位
- 逻辑删除
- 重置密码默认值：123456

### 权限管理
- RBAC 模型（角色-权限-用户）
- 菜单权限和按钮权限分离
- 权限树形结构
- 动态路由生成

### 设备管理（核心）
- 设备点检表：记录 30 天数据
- 16 个检查项：电路部分（3项）、机架部分（3项）、油路部分（5项）、周边设备（5项）
- 支持按月份、设备编号、检点人查询
- 检查项状态：1-正常，0-异常

### 生产管理
- 生产订单 → 生产计划 → 生产记录
- 产量统计支持多维度查询
- 实时更新订单和计划进度

### 物料管理
- 入库/出库记录
- 库存实时更新
- 先进先出（FIFO）
- 库存预警（低于最低库存）

### 品质管理
- 检验记录关联生产订单
- 自动计算合格率
- 不合格品处理流程
- 质量追溯功能

### 现场5S管理
- 5S检查：整理、整顿、清扫、清洁、素养
- 整改跟踪：待整改 → 整改中 → 待验证 → 已完成
- 评分统计和排名

## 安全设计

### 身份认证
- JWT Token 认证
- Token 过期时间 2 小时
- 密码 BCrypt 加密存储

### 权限控制
- 基于角色的访问控制（RBAC）
- 菜单级权限控制
- 按钮级权限控制
- API接口权限验证

### 数据安全
- SQL注入防护（参数化查询）
- XSS攻击防护
- CSRF防护
- 敏感数据加密存储

## 开发注意事项

1. **统一响应格式**: 所有接口返回 `Result<T>` 格式
2. **异常处理**: 使用全局异常处理器 `GlobalExceptionHandler`
3. **参数校验**: 使用 `@Validated` 和 Bean Validation
4. **逻辑删除**: 使用 `@TableLogic` 注解，查询时自动过滤已删除数据
5. **时间字段**: 使用 `@TableField(fill = FieldFill.INSERT)` 自动填充
6. **事务管理**: Service 层方法使用 `@Transactional`
7. **唯一性校验**: 新增和更新时都要校验唯一字段
8. **分页查询**: 使用 MyBatis-Plus 的 `Page` 对象

## 参考文档

详细的模块开发文档位于项目根目录：
- `01-系统概述方案.md` - 系统整体架构和设计
- `02-用户登录功能开发文档.md` - 登录功能详细设计
- `03-用户管理功能开发文档.md` - 用户管理模块
- `04-权限管理功能开发文档.md` - RBAC权限系统
- `05-人员管理功能开发文档.md` - 员工信息管理
- `06-生产管理功能开发文档.md` - 生产订单和计划
- `07-设备管理功能开发文档.md` - 设备点检表（核心功能）
- `08-模具管理功能开发文档.md` - 模具全生命周期管理
- `09-物料管理功能开发文档.md` - 物料入库出库和库存
- `10-品质管理功能开发文档.md` - 质量检验和追溯
- `11-现场5S管理功能开发文档.md` - 5S检查和整改跟踪

## 开发流程

1. **需求分析**: 参考对应模块的开发文档
2. **数据库设计**: 遵循命名规范，创建表结构
3. **后端开发**: Entity → Mapper → Service → Controller
4. **前端开发**: API定义 → 页面组件 → 路由配置
5. **测试验证**: 功能测试、边界测试、异常测试
6. **代码审查**: 检查是否符合开发规范

## 常见问题

### Q: 如何生成唯一编号？
A: 使用日期时间 + 序号的方式，如：`ORDER20240115001`

### Q: 如何处理关联数据？
A: 先保存主表，获取主键ID，再保存关联表数据

### Q: 如何实现逻辑删除？
A: 使用 MyBatis-Plus 的 `@TableLogic` 注解，查询时自动过滤

### Q: 如何实现分页查询？
A: 使用 MyBatis-Plus 的 `Page` 对象，前端传递 `pageNum` 和 `pageSize`

### Q: 如何实现权限控制？
A: 前端根据用户权限动态生成菜单和路由，后端在 Controller 方法上使用 `@PreAuthorize` 注解
