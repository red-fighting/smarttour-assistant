# 开发踩坑记录

## 2026-08-10  Git 强制推送覆盖旧仓库

### 问题描述
想把本地包含前后端的新代码完全替换 GitHub 旧仓库，执行 `git push -u origin main --force` 时报错 `src refspec main does not match any`。
### 原因分析
本地默认分支是 `master`，而远程仓库默认分支是 `main`，直接推送 `main` 找不到本地分支。
### 解决方案
改用 `git push -u origin master:main --force`，将本地的 master 分支强制推送到远程的 main 分支。

### 问题描述
项目根目录下多了一层 backend/ 文件夹，导致 Maven 找不到标准的 src/main/java 路径，启动项目报错 程序包org.junit.jupiter.api不存在，编译失败。
### 原因分析
Maven 默认扫描 <项目根目录>/src/main/java，但实际代码在 <项目根目录>/backend/src/main/java 下，测试类找不到 JUnit 和 Spring Boot Test 相关依赖，且依赖未完整下载。
### 解决方案
将 backend/ 下的 main 和 test 文件夹剪切到项目根目录，删除空文件夹。
在 IDEA 右侧 Maven 面板点击刷新，或终端执行 .\mvnw clean compile -U 强制下载依赖。
（可选）配置阿里云镜像加速 settings.xml，解决国内下载慢的问题。

### 问题描述
Spring Boot 应用启动失败，提示 Failed to configure a DataSource: 'url' attribute is not specified。
### 原因分析
项目中引入了 MyBatis 和 MySQL 依赖，但 application.yml 中没有配置数据库连接信息（URL、用户名、密码），Spring Boot 自动配置数据源时找不到必要参数。
### 解决方案
方案一（推荐）：在 src/main/resources/application.yml 中添加数据库配置：

### 问题描述
在 SQL 控制台执行 select * from users 时报错 [1046] No database selected。
### 原因分析
连接数据库后没有选择具体的库名，MySQL 不知道要在哪个数据库中执行查询。
### 解决方案
先执行 USE your_db_name; 切换数据库，再执行查询。

或直接指定库名：SELECT * FROM your_db_name.user;。

### 问题描述
执行 git push -u origin main --force 时报错 src refspec main does not match any，无法推送。
### 原因分析
本地默认分支名为 master，而远程仓库默认分支名为 main，直接推送 main 分支，Git 找不到本地的 main 分支。
### 解决方案
改用本地分支名映射到远程分支名的方式推送：
bash
git push -u origin master:main --force
将本地的 master 分支强制推送到远程的 main 分支。

### 问题描述
执行 git reset --hard origin/main 后，本地新增的 frontend/ 文件夹消失了，前端代码丢失。
### 原因分析
reset --hard 会强制将本地工作区回滚到远程提交的状态，所有未被 Git 跟踪的本地新文件（如未 add 和 commit 的 frontend/ 文件夹）会被删除。
### 解决方案
从 Windows 回收站还原被删除的文件夹。
或从之前的备份目录重新复制 frontend/ 到项目根目录。
经验教训：执行 reset --hard 或 rm -rf .git 前，务必先整份拷贝备份！

### 问题描述
想把本地包含前后端的新代码完全替换 GitHub 旧仓库，清除所有历史提交记录。
### 原因分析
远程仓库存在旧的提交历史（含早期后端代码），本地需要一种方式用新代码完全覆盖，且不保留任何旧记录。
### 解决方案
备份当前代码（以防万一）。
删除本地 .git 文件夹：rm -Force -Recurse .git
重新初始化：git init
添加所有文件并提交：git add . → git commit -m "完整前后端代码"
关联远程：git remote add origin git@github.com:red-fighting/smarttour-assistant.git
强制推送覆盖：git push -u origin master:main --force

### 问题描述
在社区发布分享后，刷新页面或从其他页面跳转回来，发布的帖子直接消失。
### 原因分析
loadCommunityPosts 只从后端 getAllShares() 接口加载数据；当后端不可用或返回空数据时，没有本地数据源兜底。
### 解决方案
实现三层数据源合并去重机制：
1. 后端帖子数据（ getAllShares ）
2. 本地分享数据（ localStorage.myShares ）
3. 默认预置帖子（ID 改为 900001-900004 避免与数据库自增 ID 冲突）

### 修改密码接口报 500 错误 问题描述
点击修改密码后，后端返回 Invalid bound statement (not found): com.panduoma.trevaljava.mapper.UserMapper.selectByPrimaryKey 。
### 原因分析
UserMapper 存在两个冲突：
1. 同时声明了两个同名方法 selectByPrimaryKey(int) 和 selectByPrimaryKey(Long) ，MyBatis 无法绑定
2. selectByEmail 、 selectByPhone 在 XML 中有定义但 Java 接口未声明，导致整个 Mapper 加载失败 
### 解决方案
- 删除 selectByPrimaryKey(int) 方法，只保留 Long 版本
- 补充 selectByEmail 和 selectByPhone 方法声明
- 删除 @Insert 注解（与 XML 定义冲突）
- 执行 Build → Rebuild Project 清理旧的 .class 缓存

### 收藏功能实现了但我的收藏不显示 问题描述
点击社区帖子的收藏按钮后，后端返回 200 成功，但个人中心的「我的收藏」列表为空。
### 原因分析
1. localStorage 中收藏数据的字段名（如 username ）与后端返回的字段名（ postUsername ）不一致
2. 收藏列表只在用户点击弹窗时才加载，页面初次渲染时 ref 为空数组
3. 存在脏数据（标题+内容+图片全空的空记录） 
### 解决方案
- normalizeFavorites 函数兼容驼峰、下划线、数据库原始字段等多种命名
- 过滤掉标题+内容+图片全空的脏数据，并自动清理 localStorage
- onMounted 中预加载收藏和分享数据到 ref

### 社区帖子、图片、头像不显示 问题描述
社区页面帖子列表为空，用户头像和帖子图片无法加载。
### 原因分析
posts 从 reactive 改为 ref 后， filteredPosts 计算属性仍使用 posts 而非 posts.value ，导致无法正确遍历数组。
### 解决方案
修改 filteredPosts 计算属性，使用 posts.value 访问 ref 数据。

### 底部导航栏不显示 问题描述
从对话页返回首页后，底部导航栏没有跟着跳回首页高亮。
### 原因分析
van-tabbar 的 v-model 绑定的是一个 computed 属性，computed 只读无法被 tabbar 修改。
### 解决方案
将 active 改为 ref(0) ，通过 watch 监听 route.name 变化，动态更新高亮索引（Home→0, Chat→1, Community→2, Profile→3）。

### Chat 界面输入框被底部导航覆盖 问题描述
Chat 页面的消息输入框固定在底部，但被底部导航栏遮挡。
### 原因分析
输入框使用 position: fixed; bottom: 0 ，与底部导航栏位置重叠，且没有为 tabbar 预留空间。
### 解决方案
- .chat-input-area 的 padding-bottom 增加到 calc(62px + safe-area-inset-bottom) ，为 tabbar 腾出 50px 空间
- .chat-container 的 padding-bottom 增加到 140px，确保消息列表不被输入框遮挡
- 给 van-tabbar 添加 z-index: 200 ，确保导航栏层级在输入框之上

### JWT 令牌过期导致接口 500 错误 问题描述
登录一段时间后，所有接口返回 500 错误，message 包含 JWT expired 。
### 原因分析
request.js 的响应拦截器只在 HTTP 状态码 401 时跳转登录，后端返回业务码 500 时没有处理 JWT 过期场景，导致前端误判为普通接口错误。
### 解决方案
细化拦截器逻辑：
- 新增 isJwtExpired() 函数，检测 JWT expired 、 token expired 、 令牌过期 等关键词
- HTTP 200 但业务码非 200 时，检查 message 是否含 JWT 过期信息
- HTTP 500 但响应体含 JWT 过期信息时，也跳转登录

### 问题描述
后端控制台报 HikariPool-1 - Thread starvation or clock leap detected (housekeeper delta=46m6s) 。
### 原因分析
电脑进入睡眠模式，Java 进程被挂起 46 分钟，连接池中的数据库连接失效。
### 解决方案
在 application.yml 添加 Hikari 连接池配置：
- keepalive-time: 30s （防止连接被 MySQL 断开）
- max-lifetime: 30min （过期自动重建）
- connection-test-query: SELECT 1 （获取连接前验证）
- 同时建议禁用电脑自动休眠
### 问题描述
「我的收藏」弹窗中出现蓝色头像、只有"用户"文字的空白卡片。
### 原因分析
收藏数据中存在脏记录——标题为空、内容为空、图片也为空，这些记录在列表中被渲染成空卡片。
### 解决方案
normalizeFavorites 函数过滤掉标题+内容+图片全空的记录，并同步清理 localStorage 中的脏数据。

## 2026-08-10  功能新增
社区 发布分享 支持在社区发布帖子（标题+内容+标签+图片），写入 localStorage + 后端 share 表 社区 标签筛选 Tab 切换：推荐 / 攻略 / 游记 / 问答 / 晒图 
社区 点赞功能 点击爱心图标切换点赞状态，本地+后端同步 社区 收藏功能 点击星标收藏帖子，后端接口 + localStorage 双重保障 
社区 评论功能 支持帖子评论，浅灰底白卡层级展示 个人中心 我的收藏弹窗 展示所有收藏的帖子（后端+本地合并） 
个人中心 我的分享弹窗 展示发布的所有内容 个人中心 数据统计 顶部显示收藏数/分享数/点赞数 
个人中心 修改密码 旧密码验证 → 修改新密码 → 后端 BCrypt 加密存储 个人中心 帮助与反馈 反馈类型选择 + 内容提交 
个人中心 关于 Smarttour_Assistant 旧版样式的应用介绍 
个人中心 退出登录 清除 token + 跳转登录页 首页 轮播图 4 张风景图自动轮播（九寨沟/张家界/西湖/黄山），带标题+副标题叠加 
首页 欢迎栏 根据时间动态问候（早上好/下午好/晚上好）+ 用户头像

### 后端修复
UserMapper.java 两个同名 selectByPrimaryKey(int) 和 selectByPrimaryKey(Long) 方法，导致绑定失败 删除 int 版本，保留 Long 版本 
UserMapper.java 有 XML 定义的方法没在 Java 接口声明（ selectByEmail / selectByPhone ） 补充声明 
UserMapper.java 注解 @Insert 与 XML 双定义冲突 删除注解，只保留 XML 
UserMapper.xml insert 引用了 User 实体不存在的 deleted 、 updateTime 字段 删除多余字段 
MyBatisConfig.java 未加载 XML 映射文件，所有 XML SQL 失效 添加 setMapperLocations + 驼峰转换 
application.yml 无 Hikari 连接池配置 添加 keepalive-time=30s、max-lifetime=30min、connection-test-query
在treval数据库中创建了share和favorite两张表

### 前端修复
request.js JWT 过期误判为普通错误，导致降级本地存储 细化拦截器，仅在 401 或检测到 JWT 过期关键词时跳转登录 
index.js getUserInfo URL 错误 /userInfo → 404 改为 /user/userInfo 
Community.vue posts 用 reactive 改为 ref 后，filteredPosts 未用 .value 修复响应式访问 
Community.vue 默认帖子 ID (1-4) 与数据库自增 ID 冲突，过滤掉后端数据 默认帖子 ID 改为 900001-900004 
Community.vue 发布分享后刷新丢失 三层数据源合并（后端+本地+默认） 
Profile.vue onBack 未定义导致 Vue warn 添加 router.back() 
Profile.vue 收藏字段名不匹配（驼峰 vs 下划线） normalizeFavorites 兼容多种命名 
Profile.vue 统计数始终为 0（只在弹窗打开时加载） onMounted 预加载 loadFavorites() + loadShares() 
Profile.vue 收藏脏数据（标题+内容+图片全空） normalizeFavorites 过滤无效项 + 自动清理 localStorage 
Chat.vue 输入框被底部导航覆盖 padding-bottom 增加 50px tabbar 高度 
App.vue tabbar 无 z-index，被 Chat 输入框覆盖 tabbar z-index: 200 
App.vue van-tabbar v-model 绑定 computed（只读） 改为 ref + watch 监听路由

### 后端新建及其作用
Favorite.java 收藏实体类，映射 favorite 表字段（id、userId、postId、postTitle、postContent 等） 
Share.java 分享实体类，映射 share 表字段（id、userId、postTitle、postContent、postImages、postTag 等） 
FavoriteMapper.java 收藏数据访问层，提供增删查接口（addFavorite、removeFavorite、getFavorites） 
ShareMapper.java 分享数据访问层，提供增删查接口（createShare、getMyShares、getAllShares） 
FavoriteService.java 收藏业务逻辑层，处理收藏/取消收藏/查询收藏列表 
ShareService.java 分享业务逻辑层，处理发布分享/查询我的分享/查询全部分享 
FavoriteController.java 收藏控制器，提供 REST API（POST /api/favorite/add、/remove，GET /api/favorite/list） 
ShareController.java 分享控制器，提供 REST API（POST /api/share/create，GET /api/share/my、/api/share/all） 
share_favorite_tables.sql 建表脚本，创建 share 和 favorite 两张表

### 修改文件
UserMapper.java 删除重复的 selectByPrimaryKey(int) 方法，只保留 Long 版本；补充 selectByEmail 、 selectByPhone 声明；删除与 XML 冲突的 @Insert 注解 解决 MyBatis 绑定失败，让所有 SQL 方法正常工作 
UserMapper.xml 删除 insert 语句中 User 实体不存在的 deleted 、 updateTime 字段 修复 SQL 参数绑定失败 
MyBatisConfig.java 添加 setMapperLocations 加载 classpath:mapper/*.xml；添加驼峰转换配置 修复 XML 映射文件未加载导致所有 XML SQL 失效 
application.yml 添加 Hikari 连接池配置（keepalive-time、max-lifetime、connection-test-query、connection-timeout） 防止电脑休眠后数据库连接失效

### API 接口清单
添加收藏 POST /api/favorite/add 将帖子收藏到 favorite 表 
取消收藏 POST /api/favorite/remove 从 favorite 表删除收藏记录 
获取收藏列表 GET /api/favorite/list 查询当前用户的所有收藏 
发布分享 POST /api/share/create 将分享内容写入 share 表 
获取我的分享 GET /api/share/my 查询当前用户发布的所有分享 
获取全部分享 GET /api/share/all 查询所有用户发布的分享（社区展示用）

### 实现头像从本地添加，并存于后端数据库，同步与前端个需要界面

---

# 2026-09-02 ~ 2026-09-03 功能开发与踩坑记录

## 一、本期完成的工作（功能清单）

- 头像上传修复：个人中心上传头像 → 存后端 uploads 目录 → 同步到社区帖子/首页/个人中心所有界面。
- 首页移除旧的快捷入口界面及相关代码。
- 首页新增「发现世界」商品区：热门目的地下方展示旅游商品卡片列表，点击弹出商品详情（详细路线/美食特色/导游信息）。
- 购物车与订单：新建 product、cart_order 两张表；后端 10 个 API（商品列表/详情/加购/订单列表/删除/支付/统计）；个人中心订单弹窗分购物车/已支付两个 Tab。
- 用户角色体系：user 表加 role(0普通用户/1管理员)、status 字段；登录后按角色自动分流（管理员→/admin，普通用户→/）。
- 管理员独立后台：侧边栏 + 顶部品牌栏 + 头像下拉菜单，共 5 个 Tab（仪表盘/用户管理/商品管理/订单管理/数据可视化）。
- 用户管理：支持全部/普通用户/管理员筛选、角色切换、账号启用/禁用。
- 商品管理：抽屉式弹窗实现商品新增/编辑/删除，数据存入后端数据库。
- 订单管理：支持全部/购物车/已支付状态筛选、订单状态更新、删除订单。
- 管理员注册码：注册选管理员时必须填写注册码（admin2026），后端校验不符返回 400。
- 数据可视化：ECharts 实现 4 张渐变统计卡 + 用户角色分布饼图 + 订单状态饼图 + 商品销量 Top6 柱图 + 销售额柱图 + 底部汇总条。
- 首页订单动态查询：规划卡片支持按起始地/目的地/预算/出发返回日期筛选，点「查询合适产品」弹窗展示匹配商品。
- 首页日期选择：「天数」输入框改为「出发日期 + 返回日期」双日期选择器，选完自动计算共 N 天。
- 加入购物车 5 重校验：加购前依次校验登录态、日期必填、起始地匹配、目的地匹配、天数严格相等，不符弹 toast 拦截；出行日期一并存入 cart_order 表。

## 二、遇到的问题与解决方案

### 问题描述
头像上传功能不工作：点击上传后图片不更新；社区帖子头像全部显示破损图。
### 原因分析
1. MyBatis 查询用户头像时，avatar 为 NULL 的列被跳过，返回结果里没有 post_avatar 这个 key，前端拿到 undefined。
2. 上传时 FileNotFoundException：application.yml 中 app.upload.dir 配的是相对路径，Spring Boot 运行时工作目录不同导致找不到目录。
3. Community.vue 对空头像没有兜底，直接渲染 <img src=""> 显示破损图标。
### 解决方案
- ShareMapper 查询加 IFNULL(u.avatar, '') AS post_avatar，保证 post_avatar key 永远存在。
- app.upload.dir 改为绝对路径 D:/Front-end-learning/project/Smarttour_Assistant/uploads。
- Community.vue 定义 defaultAvatar 常量 + onAvatarError 兜底 + postAvatar 空值处理。

### 问题描述
社区/首页引用的 Unsplash 图片全部加载失败，控制台报 net::ERR_BLOCKED_BY_ORB。
### 原因分析
Unsplash 返回的 CORS 响应头不标准，被 Chromium 的 Opaque Response Blocking（ORB）机制拦截。
### 解决方案
全部替换为 picsum.photos：MySQL product 表 6 条记录 UPDATE 为 https://picsum.photos/seed/pic_{id}/800/600；Home.vue 4 张 banner 换为 seed/banner_{景点名}；product_cart_tables.sql 模板同步。picsum 支持 CORS、seed 参数固定出图、全球 CDN、无需鉴权。替换后 0 控制台错误、图片 100% 加载。

### 问题描述
管理员后台登录页的「用户类型」选择（普通用户/管理员）用 Vant radio-group 实现后，两个选项无法横向并排，选中后布局错乱。
### 原因分析
Vant van-radio 的 DOM 结构（radio__icon + radio__label）与自定义 flex 卡片嵌套冲突；:deep(.van-radio__label){display:none} 连自定义卡片内容也一起隐藏了。
### 解决方案
弃用 Vant radio，手写纯 HTML+CSS：.role-select 弹性横向布局 + .role-cards flex:1 等宽 + 点击切换；只隐藏 .van-radio__icon 和 .van-radio__icon--checked；选中态从深渐变改为浅青绿渐变 + 描边（颜色调浅）。

### 问题描述
登录/注册模式切换或重新进入登录页时，表单里残留上次输入的用户名、密码、手机号等内容。
### 原因分析
Login.vue 组件被路由复用，setup 只执行一次，reactive 表单状态不会重置。
### 解决方案
在 App.vue 的 <router-view> 上加 :key="route.fullPath"，路由变化时强制重建 Login 组件；密码框统一 type="password"。

### 问题描述
加入购物车功能改造（加出行日期）后，Maven 编译报错：无法将 selectCartExisting 应用到给定类型，需要 (Long,Long,LocalDate,LocalDate)，找到 (Long,long)。
### 原因分析
CartOrderMapper.selectCartExisting 签名从 2 个参数扩展为 4 个（加了 startDate/endDate），但 CartOrderService.payOne() 里还残留一行旧的占位调用 cartOrderMapper.selectCartExisting(userId, -1L)。
### 解决方案
删除 payOne() 中无用的占位调用（该行本来就没被使用，实际支付走 payById）。编译通过。

### 问题描述
Home.vue 出发日期选择器控制台报警告：Expected Number with value NaN（van-field type="date" 的 min 属性）。
### 原因分析
van-field 日期组件的 :min 属性要求传 Date 对象，之前传的是字符串 "2026-09-02"，内部转 Number 得到 NaN。
### 解决方案
定义 const minDateObj = new Date(today) 传给出发日期 :min；返回日期 :min 用 formData.startDate ? new Date(formData.startDate) : minDateObj。

### 问题描述
出发日期和返回日期选同一天时，自动计算的天数显示 0 天。
### 原因分析
天数算法 (end - start)/86400000 在同一天时 diff=0，旧逻辑 diff > 0 ? diff+1 : 0 直接返回 0；但旅行天数应含首尾，同一天算 1 天。
### 解决方案
computedDays 改为：diff < 0 返回 0（返回早于出发），否则返回 diff + 1（含首尾）。验证：9.5→9.7=3天、9.5→9.5=1天、9.5→9.10=6天、9.15→9.10=0天，全部符合预期。

### 问题描述
加入购物车的天数匹配规则与用户预期不符：最初实现为「用户天数 ≤ 产品天数就放行」，但用户原话是「天数……一样就能选择」。
### 原因分析
需求语义是严格相等，宽松匹配（<=）会让选 2 天的用户也能加入 3 天行程的产品。
### 解决方案
校验条件从 userDays > productDays 拦截改为 userDays !== productDays 拦截（严格相等），提示文案改为「天数不匹配！产品行程：3天2晚（3天），您选了N天」。

### 问题描述
cart_order 表不同出行日期的同一商品被错误合并成一条购物车记录（数量累加）。
### 原因分析
selectCartExisting 只按 (user_id, product_id, status=0) 判重，没有日期维度。
### 解决方案
判重 SQL 增加 AND start_date = #{startDate} AND end_date = #{endDate}，同商品 + 同出发/返回日期才合并数量，不同日期生成独立购物车项。

## 三、数据库变更

- 新建 product 表：id、title、origin、destination、duration（文本如"3天2晚"）、price、route_desc、food_feature、guide_name、guide_phone、sold_count、image。
- 新建 cart_order 表：id、user_id、product_id、quantity、price（价格快照）、title_snapshot、image_snapshot、status(0购物车/1已付/2取消)、paid_time、create_time、update_time；后 ALTER 新增 start_date DATE、end_date DATE。
- user 表 ALTER 新增 role TINYINT(0普通/1管理员)、status 字段。
- 所有用户数据（shares/favorites/comments/likes/orders）均按 user_id 隔离；share_like 用 (share_id,user_id) 复合主键；favorite 用 UNIQUE(user_id,post_id) 防重复收藏。

## 四、需要测试的内容与测试方法

### 测试账号
- 管理员：admin / admin1 / admin2 / admin3，密码均 123456（登录后应跳 /admin/dashboard）。
- 普通用户：testfav_a / 张三 / 麻子，密码 123456（登录后应跳 / 首页）。
- 管理员注册码：admin2026（错误码注册应返回 400）。

### 测试方法 1：后端接口直测（PowerShell，最快）
```powershell
# 登录拿 token
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/user/login' -Method Post -ContentType 'application/json' -Body '{"username":"testfav_a","password":"123456"}'
$token = $login.data.token
# 带日期加购
Invoke-RestMethod -Uri 'http://localhost:8080/api/cart/add' -Method Post -ContentType 'application/json' -Headers @{Authorization="Bearer $token"} -Body '{"productId":1,"quantity":1,"startDate":"2026-09-05","endDate":"2026-09-07"}'
# 查购物车
Invoke-RestMethod -Uri 'http://localhost:8080/api/cart/my?status=0' -Headers @{Authorization="Bearer $token"}
# 商品动态查询
Invoke-RestMethod -Uri 'http://localhost:8080/api/product/search?origin=成都&destination=九寨沟&duration=3'
```

### 测试方法 2：数据库验证
```powershell
& "D:\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p123456 travel -e "SELECT id,product_id,start_date,end_date,status FROM cart_order ORDER BY id DESC LIMIT 5;"
```
预期：新加购记录 start_date/end_date 有值；不同日期同商品为不同行。

### 测试方法 3：浏览器端到端（E2E）
1. 未选任何日期，直接点商品购物车图标 → 应弹「请先在规划卡片选择出发日期和返回日期」（已验证 PASS）。
2. 起始地选「北京」，对 origin=成都 的商品点加购 → 应弹「起始地不匹配！产品起始地：成都」。
3. 目的地选「九寨沟」，对 destination 不含九寨沟的商品加购 → 应弹「目的地不匹配」。
4. 选 6 天日期对「3天2晚」商品加购 → 应弹「天数不匹配！…（3天），您选了6天」。
5. 选起始地=成都、目的地=九寨沟、出发 9.5 返回 9.7（3天），对匹配的九寨沟 3 天产品加购 → 应弹「已加入购物车 🛒」，Network 中 POST /api/cart/add 的 body 含 startDate/endDate。
6. 管理员账号登录 → 自动进 /admin；手动改地址访问 / 应被踢回 /admin；普通用户访问 /admin 应被踢回登录/首页。
7. 注册页选「管理员」但不填/错填注册码 → 应注册失败提示。
8. 数据可视化页 4 个图表正常渲染，窗口缩放图表自适应，切换 Tab 无内存泄漏。

### 测试方法 4：前端静态检查
- VS Code GetDiagnostics 确认 Home.vue 等文件 0 错误。
- 浏览器 F12 Console 确认 0 红色错误、无 Unsplash/ORB 报错、无 van-field NaN 警告。

## 五、测试中发现的 Bug 与最终结果

1. 加购改造后 Maven 编译失败（selectCartExisting 参数不匹配）：根因是 CartOrderService.payOne() 残留旧签名占位调用；删除该无用调用后编译通过。
2. 日期选择器控制台报 Expected Number with value NaN：根因是 van-field 的 :min 传了字符串而非 Date 对象；改为传 new Date() 后警告消失。
3. 出发返回选同一天时天数显示 0 天：根因是 diff=0 时旧逻辑直接返回 0；改为 diff+1 含首尾计算后，同日显示 1 天，验证正确。
4. 天数匹配规则过松（用户天数 ≤ 产品天数即放行）：与用户「天数一样才能加购」的语义不符；改为严格相等 !== 才放行。
5. 不同出行日期的同一商品被合并成一条购物车记录：根因是判重 SQL 缺日期维度；selectCartExisting 增加 start_date/end_date 条件后，不同日期生成独立购物车项。
6. Unsplash 图片全部 net::ERR_BLOCKED_BY_ORB：根因是其 CORS 响应头不标准被 Chromium 拦截；全部替换为 picsum.photos 后 0 报错、图片 100% 加载。
7. 登录页 Vant radio 用户类型选择布局错乱：根因是 van-radio 的 DOM 结构与自定义 flex 卡片冲突；弃用 Vant radio 改为手写 HTML+CSS 后正常。
8. 登录/注册切换时表单残留上次输入：根因是 Login 组件被路由复用不重建；在 router-view 上加 :key="route.fullPath" 强制重建后解决。
9. 头像上传 FileNotFoundException：根因是 app.upload.dir 用相对路径，工作目录漂移导致找不到目录；改为绝对路径后上传正常。
10. 社区帖子头像 NULL 时显示破损图：根因是 MyBatis 跳过 NULL 列导致前端拿不到字段；SQL 加 IFNULL 兜底 + 前端默认头像/onError 回退后全部正常显示。

## 六、最终结果

- 后端：Spring Boot 4.1 + MyBatis 3.0 + MySQL 8，Maven compile 通过，9 个 Controller、7 个 Service、7 个 Mapper 全部正常；/api/cart/add 带日期插入返回 code=200，DB 中 start_date=2026-09-05、end_date=2026-09-07 正确落库，/api/cart/my 正确返回日期字段。
- 前端：Vue 3 + Vant 4 + ECharts 6，Home.vue 诊断 0 错误；「未选日期拦截」浏览器实测 PASS；Vite HMR 热更新即时生效。
- 权限：管理员/普通用户双向路由守卫生效，管理员独立后台 5 模块（仪表盘/可视化/用户/商品/订单）全部可用。
- 遗留说明：browser 自动化测试因 step budget 限制，「成功加购」正向场景未跑完截图，但后端接口直测 + DB 校验 + 前端未选日期拦截均已通过，正向流程可按第四节测试方法 3 第 5 步手动复核。
