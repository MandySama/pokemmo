# 已完成功能复刻核对（2026-10-13）

> 本文为本次改动前的审查快照。后续协议、世界裁决与连接池的调整见 [实现更新](protocol-world-update-2026-10-13.md)。

按本会话可追溯的功能完成阶段排列；同一阶段的界面细节合并列出。核对对象为当前未提交工作区，以及只读的 Pokemmo2Pro-Client、Pokemmo2Pro-Server 源码。本次不修改功能、不提交 Git。

**结论：客户端的资源解析、基本控件和绘制属于有实质内容的复刻；整个客户端／服务端尚不能称为机制一致的复刻。主要差距在游戏协议、服务端移动与传送控制、入服凭据和数据库连接管理。**

判断口径：

- **基本一致**：已核对的核心路径相同，允许框架封装或代码组织不同；不代表逐像素一致。
- **简化／定制**：保留本阶段用途，或用户明确要求改变行为；不按缺陷处理。
- **机制不同**：决定状态的端、协议、算法或生命周期不同，不能只因界面结果相似就称为一致。
- **仅界面／待验证**：没有相应业务链，或本次证据不足，不能认定完整实现。

## 按完成阶段逐项核对

| 顺序 | 功能 | 客户端核对 | 服务端核对 | 结果 |
| --- | --- | --- | --- | --- |
| 1 | 桌面窗口、TWL、主题、字体 | 同为桌面 OpenGL、TWL 控件和外部主题；当前自行实现 GLFW/TWL 渲染适配，原项目有 LibGDX 封装。窗口居中、缩放有实际代码。 | 不涉及。 | **基本一致／框架简化**。并非把截图当背景。未重做所有 DPI、显卡和窗口状态组合的人工验证。 |
| 2 | 语言 XML 与可见文字 | 按 ID、语言发现、主语言回退实现；当前只有简体中文是既定范围。 | 登录请求携带语言；没有完整的服务端本地化系统。 | **基本一致／范围简化**。旧测试要求两种语言已不适用；当前语言发现与 ID 查询新测试通过。 |
| 3 | 登录框、输入、焦点、账号居中、登录状态 | 已保存账号走只读下拉；登录中保留记住我、已保存用户名和清除凭据入口，右下按钮区不禁用；输入使用 TWL 和编辑适配。原登录入口在名称容易误导的 `ChatHistoryViewportComponent` 中。 | 登录异步执行，失败恢复表单。 | **基本一致＋用户指定调整**。未对原客户端逐个输入事件实机回放，不能保证所有右键菜单和焦点边界完全一致。 |
| 4 | 通知、确认弹窗、加载中 | 原通知默认 5000ms、淡出 250ms、到期后 300ms 移除；当前值与路径对应。确认弹窗遮暗并居中；加载使用主题背景。 | 异步任务结果驱动显示／关闭。 | **基本一致**。通知时序、加载背景与居中回归通过。不是所有网络等待都能据此认定与原项目相同。 |
| 5 | Logo、公告、右下菜单 | 主题与字符串入口对应；六条公告目前静态、统一宽度，是已确认范围。右下九按钮中，客户端管理、MOD、设置、关闭有回调；修复客户端、注册、忘记用户名、忘记密码、申请帮助当前仅展示。 | 后五项没有本项目业务接入。 | **部分复刻／部分仅界面**。原登录界面存在网页／注册入口；不能把“显示全部按钮”写成“全部按钮功能完成”。 |
| 6 | 设置窗口、12 个数值输入 | TWL 数值模型、编辑提交／取消、范围和特殊格式有实质实现。当前真正持久化／联动的是音乐音量、失焦静音、FPS 显示、限帧，以及 HGSS 替换设置。多数其他项目仍是控件。 | 不涉及。 | **控件基本一致，业务仅部分接通**。`AllNumberSmoke` 全部 12 项通过；不应声称设置页全部生效。 |
| 7 | FPS 和垂直同步 | 平均 100 帧更新、限帧预算有实现；当前主循环固定 `glfwSwapInterval(1)`。原项目 V-Sync 配置默认 false，设置保存会调用 `glfwSwapInterval`；当前 V-Sync 控件未联动，也未实现原项目独立失焦限帧。 | 不涉及。 | **限帧机制合理简化；V-Sync 明确不同**。可能导致用户设置高 FPS 后仍受刷新率限制。 |
| 8 | 登录音乐、音量、失焦静音 | 从 BW ROM 的 SDAT 经 SSEQ JNI 合成，以 OpenAL 流式输出；原项目同属 ROM 合成播放链，普通登录曲为 1159。当前有线程、缓冲和退出清理，失焦推进而静音。 | 不涉及。 | **核心播放机制基本一致／播放器组织简化**。当前 1/50 备用曲 1157 的准确随机触发条件，本次未重新追到原项目调用端，不能将该概率标为已核实一致；未进行听感对比。 |
| 9 | 黑白标题 3D 场景 | 从 NitroFS/NARC 读取模型、纹理、骨骼／纹理动画和镜头；后台解析，GL 线程上传，图片回退。 | 不涉及。 | **资源机制实质复刻**。本次重新解析两套场景成功；不代表全部模型指令、材质和动画分支与原渲染器等价。 |
| 10 | ROM 信息快照 | 读取 ROM 头的 NDS 标题、GBA 游戏代码和修订号，登录界面创建时保留快照。 | 不涉及。 | **基本一致**。本次快照、占位、追加顺序及重建更新测试通过。 |
| 11 | ROM 校验、导入、关闭提示重启 | 有签名／卡带头检查、临时文件、失败保留旧记录、原子发布与配置保存。原项目按类型和时间命名；当前保留原名并追加冲突序号，是用户明确指定。重启实现自行保留运行参数并排除 IDE 代理。 | 不涉及。 | **导入流程复刻＋授权定制**。不能要求为“保持一致”恢复原命名或原重启问题。本次五类文件导入核心测试通过；不等于具备原 ROM 解析器的全部深度校验。 |
| 12 | MOD 导入、展示、启停、排序、删除、提示、重启 | 原项目读取包元数据、启用顺序并提供管理 UI；当前路径、配置键、列表操作对应，重开保持状态，排序占位和描述提示已实现。实际资源覆盖没有接入。 | 不涉及。 | **管理功能复刻／资源应用按要求省略**。本次元数据、异常包拒绝、配置保留、20 次重开、样式布局通过；不能称为 MOD 已实际生效。 |
| 13 | 登录握手与加密 | ECDSA 验证、ECDH、方向种子、AES-CTR、小端长度帧与原链路相近。**游戏通道校验不同：当前两字节截断 HMAC，原项目 `TlsInfo.create(2)` 是 CRC16。** | 本项目服务端使用同一个 `SecureChannel`，所以本项目两端测试通过无法发现与原项目的差异。 | **部分一致，但存在确定的协议机制偏差**。此前将游戏两字节 HMAC 描述为与原项目一致不准确；登录通道和游戏通道必须分开判断。 |
| 14 | 记住密码／已保存凭据 | UI 的账号列表、上次账号、占位与本地清除方向一致；本地保存凭据而非明文密码。 | 原项目用用户名派生 AES-CBC 密钥，将线上密码摘要加密成可解密凭据；当前是随机数＋服务端私钥、用户名、密码校验值绑定的 HMAC。 | **用户流程复刻，算法明确不同**。当前设计可保留，但应明确是自主实现；本次真实凭据登录和重建存储后验证通过。 |
| 15 | Live、服务器列表、入服与角色选择 | 登录→列表→加载→角色窗口的 UI 顺序相近，默认 Live 已生效。 | 原项目有节点／在线目录、数据库一次性 UUID 会话键及账号上下文；当前定时探测节点并签发 60s ECDSA 票据，消费记录在 game 内存中。原会话键校验含 IP 和约 5min 时效。 | **流程相近，入服机制不同**。本项目有时效、签名、容量、重复账号和一次消费校验；没有复刻原会话上下文／断线续接链。 |
| 16 | 新角色表单、随机外观、下拉、配色、五种预览 | 原 `AvatarCustomizationWindow` 读取外观分类、性别／flags 筛选和配色；当前解析 `addons.pak`，按图层合成并支持帽子遮发。预览居中、下拉最多八行、配色隐藏名称为用户指定优化。 | 初始外观校验由本项目 `AvatarCatalog` 实现。 | **核心外观机制复刻＋授权优化**。新预览与外观交互回归通过；创建提交期间保留左侧可用也属于指定行为。 |
| 17 | PostgreSQL、独立库、账号持久化 | 客户端仍发送线上密码摘要，业务使用稳定账号 ID。 | 原项目 PostgreSQL＋HikariCP＋jOOQ；当前 PostgreSQL＋直接 JDBC＋幂等 SQL。当前每次 `connect()` 新建物理连接。原密码直接比较存储的线上摘要；当前追加随机盐和 PBKDF2。删除结构版本表为用户明确要求。 | **存储职责相同，连接管理与密码算法不同**。PBKDF2 是合理改进；连接池缺失是实际性能差距。不能把直接 JDBC 当成已复刻原连接池。 |
| 18 | 创建角色、数量限制、重名、账号归属 | 当前提交所选外观，防重复提交，成功刷新，失败恢复编辑。 | 两者都有已认证账号归属、事务创建、忽略大小写重名、每账号 3 个、4/1 的 (6,6) 出生点。原项目创建时用全局 advisory lock 并建图鉴等记录；当前锁账号行、唯一索引、仅保存已需要的数据，外观检查更严格。 | **核心业务机制基本一致／事务粒度和数据范围简化**。本次十路并发恰好创建三个、重名、隔离、重连、外观校验通过。双方实际正则均允许 Unicode 字母，不只是 ASCII；旧说明“字母数字”不应被理解为只限英文。 |
| 19 | 角色卡片、人物预览、两步删除、注销 | 原 `AvatarCustomizerComponent` 有卡片、删除名称查找和确认口令；当前对应实现，没有队伍数据就不造占位精灵。 | 当前所属账号事务删除有效。**协议不同：本项目 0x04 是删除、0x05 是入图；原项目 0x04 是选角、0x05 是请求玩家加载。** | **UI／归属业务复刻，封包未复刻**。本次创建、找不到提示、口令门槛、取消、删除最后角色与注销联调通过。 |
| 20 | 点击角色入图、500ms 过渡 | 当前读取外观和位置、准备地图后淡入。原 `GdxParticleSystemRenderer` 的入图路径也有 500ms 过渡。 | 原项目选角确认后还有玩家／地图加载、状态下发和加载确认；当前一个自定义入图响应后由客户端直接创建世界。 | **视觉时序接近，加载状态机简化且机制不同**。不应仅通过本地的黑幕效果判断已复刻服务端入图流程。 |
| 21 | 火红布局、图块、调色板、翻转、透明和增强规则 | 从实际 ROM 解析而非截图拼图；主副图块、BGR555、LZ77、层级和外部修正规则与原 `GbaMapLayout/GbaMapTileset` 路径相对应。独立地图浏览器为开发工具。 | 当前仅共享 CSV 编号和地图尺寸，不解析服务端通行／事件。 | **客户端解析属于实质复刻；服务端地图模型未接通**。本次地图全量渲染通过。天气与暗色按用户要求排除修复。 |
| 22 | 镜头、人物锚点、阴影、层级、服饰 | 正交像素插值、自动倍率阈值、0.75 倍外观、阴影和前后遮挡有对应原代码。当前关都垂直锚点 y+12、部分资源管理和形态组合为简化实现。 | 无在线实体位置广播。 | **常用渲染路径基本一致／部分调整**。不能由本项目 CPU 旧新像素相同推出与原客户端所有动作完全相同。 |
| 23 | WASD 按格移动、碰撞、朝向 | 当前客户端完成地形／高度／NPC 检查、坐标推进与动画。常规动作时长采用原服务端 `SportType` 值。 | 原 `MovePacket`／`CharacterMovementService` 处理移动、跨图、传送和周围玩家广播；当前只接受客户端最终位置并校验所属账号和范围。 | **明确机制不同，是最大差距之一**。服务器无法确认每一步是否可走，也没有完成在线移动同步。 |
| 24 | 二楼→一楼楼梯、门内外、传送淡出淡入 | 原 ROM 事件目标、方向地块、目标索引和门前落点已使用；当前各 500ms 过渡。 | 原 `CharacterEventService.handleWarpEvent` 和 `CharacterWorldLoader` 选择目标、下发地图并等待客户端；当前客户端选目标，之后存位置。 | **事件数据复刻，传送裁决／加载确认机制不同**。这是“本地更快”的重要原因，不能只归因于服务端部署本地。 |
| 25 | NPC、妈妈／真新镇人物、HGSS 替换 | ROM 物件、NDS 替换和回退、映射减一、朝向、季节色及脚底锚点有实际实现。 | 原有 NPC 实体、可见条件和移动／事件机制；当前仅显示隐藏标记为零、固定图形 NPC，客户端阻挡。 | **静态展示复刻／按阶段省略实体逻辑**。不能称为全 NPC 行为已复刻；未执行剧情／对话是明确范围。 |
| 26 | 边界树木铺满、全部 425 地图和连接 | 客户端读取边界格并循环铺设；按 ROM 连接偏移拼接，全部地图按需加载；动态出口由客户端记录返回点。 | 原地图网络、传送及返回由服务端世界状态处理；当前返回点由客户端提交并保存。 | **全量展示已接通，世界状态机制不同**。本次实际绘制 425 图、解析 1275 固定目标及 19 动态事件；不代表手动走遍全部 1275 个传送点或复杂楼层特殊机制都正确。 |
| 27 | K 跑步、2 自行车 | 275／135／80ms，9 帧段分组、形态切换和当前步保留时长有实质实现。 | 原项目还包含形态请求、区域／道具和通行限制；当前开关仅本地会话。 | **形态与常用动画复刻，权限和同步简化**。按用户要求不在本阶段新增道具／剧情限制；本次三形态速度与切换回归通过。 |
| 28 | 全图渲染性能优化、入图时间异常修复 | 当前使用共享图块图集、动画换帧、可见格裁剪、引用计数；原项目按图块资源／层与视域组织，避免每帧整图栅格重建。GL_QUADS 的提交方式与原渲染框架不同。入图重新取时间、非负动画时间是自身修复。 | 不涉及。 | **渲染思路一致，具体批处理／缓存为自主简化**。本次 288 组自身旧新像素一致、共享生命周期、旧帧时间和全图检查通过；没有原项目截图逐像素对比。首次上传仍同步。 |
| 29 | GM 菜单、倍速 | 菜单分组和按钮 UI 对应；当前倍速 1/2/4/8 改本地移动与人物动画，不改地图动画和过渡。多数 GM 按钮仍无回调。 | 原 GM 菜单会发命令并由服务端权限／命令执行链处理；当前没有该链，倍速为新增调试功能。 | **菜单 UI 复刻，倍速为定制；GM 服务端未复刻**。本次倍速与菜单布局通过，不能称为已有 GM 管理能力。 |
| 30 | 自动位置存档与倍速断线修复 | 当前合并待发送快照，至少 200ms 间隔，正常关闭唤醒并保存最后快照；确认表示 DB 已提交，创建／删除／入图命令隔断合并。 | 当前自定义位置封包同步落库；原已核对的移动路径更新内存实体，未找到能证明完整周期／断线位置落库的链，不能认定原项目已有等价可靠存档。 | **本项目独立实现／优化，不能称为原机制复刻**。本次数据库行锁阻塞、100 次/秒更新、命令顺序、退出与重连通过。服务异常或进程强杀仍可能丢失未确认位置；不是零丢失保证。 |
| 31 | GM 地图传送、穿墙 | 当前读取 22 个地点，客户端切图并保存位置；穿墙忽略碰撞但保留边界和合法传送。 | 没有管理员权限判定和管理命令协议，使用普通角色位置保存。 | **当前调试工具可用，机制与原 GM 不同**。若要求机制一致，必须迁入服务端权限／命令处理，不能仅复用位置保存。 |
| 32 | 训练家名片 | 外部背景、训练家视图、图标、服务端创建日期对应；未接通统计 `--`，徽章留空，不填示例数据。 | 只有创建日期／外观来自角色响应，没有完整名片数据状态。 | **窗口和已有数据复刻，统计省略**。本次窗口叠放和纹理清理检查通过；不是完整训练家数据功能。 |
| 33 | 背包分类、图标、搜索框 | 八类图标从 BW ROM 解码，彩灰切换、B、Ctrl+Tab、关闭保留分类、重开清搜索已实现。 | 无物品列表、搜索结果、排序、使用、丢弃等业务。原 `InventoryWindow` 绑定实际背包并筛选物品。 | **空背包窗口复刻；物品系统未实现**。当前“搜索”只是输入框，不能据此称为搜索功能已接通。本次分类、按键、输入和 GL 清理通过。 |
| 34 | 名片／背包并存、拖动、置顶、Esc 顶层关闭 | 已有实际 TWL 拖动、重排和保留位置，不是每帧强行居中。 | 不涉及。 | **常用窗口管理机制基本一致**。本次真实 GL/TWL 叠放、独立快捷键、位置与拖动测试通过；未穷举所有其他窗口组合。 |

## 确定的机制偏差及处理顺序

这部分为建议，不代表本次已获授权修改或已修复。

1. **P1：游戏封包和校验算法。** 先建立协议对照表／固定输入编码测试。游戏两字节校验应与原 CRC16 对齐；0x04／0x05／0x06 语义和角色外观的 mask＋packed 编码目前不一致。必须两端同时改，不能只改单个编号。
2. **P1：服务端主导移动／传送。** 让客户端发移动意图，服务端持有角色世界状态、校验通行和目标事件，再下发结果；保留客户端动画预测与插值。当前范围校验与最终坐标保存不能代替这条机制。
3. **P2：数据库连接池。** 对齐原 HikariCP 的复用和关闭生命周期，避免持续存档反复进行连接／认证。200ms 合并解决背压，但没有消除建立连接的成本。
4. **P2：入图加载、入服令牌和重连状态。** 若要求机制一致，需明确是否沿用原 session key／节点上下文和加载确认。现有签名票据不是同一实现，不能在文档中混称。
5. **P2：设置与登录入口边界。** 原项目 V-Sync 实际联动，当前固定启用；原登录入口会检查最低 ROM 条件，当前 `submit()` 未做同等前置门槛。其余仅 UI 控件／按钮应逐项标清范围。
6. **保留的合理定制。** 原名 ROM、安全的密码校验值、预览居中、配色页隐藏名称、列表收缩、仅管理 MOD、静态 NPC、暂不处理剧情对话和天气暗色，不应为了“像原项目”撤销用户要求。

## 本次验证结果与边界

- Maven 离线 `compile` 退出码 0，无编译诊断；启动器仍打印已知的 `Access is denied.`，未将该行单独作为成功或失败判据。
- 第一轮 24 个 smoke 程序：20 个通过，4 个需要核对／适配测试。`StringsSmoke` 仍要求两种语言；`GameSettingsSmoke` 把现有 EditField 强转 Button；`RomImportSmoke` 的旧 UI fixture 未隔离当前 ROM 配置；`CharacterDeleteFlowUiSmoke` 调用已删除的 `migrate()`。
- 新 `AuditStringsSmoke` 通过当前单语言发现／选取和关键 ID；`RomImportSmoke core` 通过五类文件名／冲突、校验、失败保留与配置恢复。未修改语言包或真实 ROM 配置来迁就旧测试。
- `AuditPostgresAccountCharacterSmoke` 通过真实 PostgreSQL 账号、PBKDF2、凭据、TCP 登录／创建／重连、大小写重名、跨账号删除拒绝和十路并发三角色限制。
- `AuditCharacterDeleteFlowUiSmoke` 改为 `initialize()`，并适配“点卡片直接入图”的现有行为后，通过实际 UI 创建、提交时状态、两步删除、找不到提示、最后角色删除和注销。测试内部选中卡片使用控件方法，因此不把它计作对卡片入图动作的重复验证。
- `SpeedSaveBackpressureSmoke` 通过真实行锁阻塞、每秒 100 次更新、队列有界、命令顺序、最终确认、关闭保存和重连恢复。
- 因此本次得到 **25 个独立测试程序／路径的成功结果**。旧测试适配后的结果没有重复计数；设置旧测试未重跑，相关数值行为由 `AllNumberSmoke` 覆盖，不能等价宣称整张设置页全覆盖。
- 全地图测试验证资源加载、绘制和目标解析；没有逐个手动操作所有门、洞穴、楼梯、跳跃地块或长时间跑图。未运行原客户端，未做跨项目网络互连，也未声称原客户端实测通过。
- 数据库测试使用隔离账号并清理测试业务行；临时 smoke、截图与临时公钥在 `target`，不作为永久测试基础设施。

第一轮通过名单：`SceneParseSmoke`、`AvatarAssetsSmoke`、`ModFilesSmoke`、`WindowStackSmoke`、`BagSmoke`、`AllNumberSmoke`、`AtlasPixelParitySmoke`、`AtlasLifecycleSmoke`、`WorldEntryClockSmoke`、`FullWorldSmoke`、`MovementModeSmoke`、`SpeedMultiplierSmoke`、`GmSpeedMenuSmoke`、`LoadingPanelSmoke`、`RomInfoSnapshotSmoke`、`AvatarCustomizationUiSmoke`、`ModRepeatedOpenSmoke`、`ModStyleSmoke`、`NotificationTimingSmoke`、`SavedAccountAlignmentSmoke`。

## 关键源码证据

| 核对点 | 当前项目 | 原项目 |
| --- | --- | --- |
| 游戏校验 | [SecureChannel](/E:/Wednesday/Java/pokemmo/protocol/src/main/java/top/totodile/pokemmo/protocol/SecureChannel.java:38) | [TlsInfo 的 size=2 分支](/E:/PokeMMO/Pokemmo2Pro-Server/server/src/main/java/org/server/protocol/tls/TlsInfo.java:111)、[Crc16](/E:/PokeMMO/Pokemmo2Pro-Server/server/src/main/java/org/server/protocol/tls/hash/Crc16.java) |
| 角色封包编号／格式 | [CharacterPackets](/E:/Wednesday/Java/pokemmo/protocol/src/main/java/top/totodile/pokemmo/protocol/CharacterPackets.java:77) | [GameProtocol](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/protocol/GameProtocol.java:92)、[CharacterCreationRequest](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/services/character/CharacterCreationRequest.java) |
| 移动／传送裁决 | [KantoWorld](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/world/KantoWorld.java)、[GameServer](/E:/Wednesday/Java/pokemmo/sever/game/src/main/java/top/totodile/pokemmo/game/GameServer.java:162) | [MovePacket](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/protocol/packets/c2s/MovePacket.java)、[CharacterEventService](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/game/character/CharacterEventService.java:138)、[CharacterWorldLoader](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/game/character/CharacterWorldLoader.java) |
| 数据库连接 | [Database.connect](/E:/Wednesday/Java/pokemmo/sever/persistence/src/main/java/top/totodile/pokemmo/persistence/Database.java:55) | [HikariCP Database](/E:/PokeMMO/Pokemmo2Pro-Server/db/src/main/java/org/pokemmo/db/Database.java:27) |
| 保存登录凭据 | [SavedLoginCredential](/E:/Wednesday/Java/pokemmo/sever/login/src/main/java/top/totodile/pokemmo/login/SavedLoginCredential.java) | [LoginService.passwordToCredentialsKey](/E:/PokeMMO/Pokemmo2Pro-Server/server.login/src/main/java/org/pokemmo/loginserver/service/LoginService.java:116) |
| 入服凭据 | [GameTicket](/E:/Wednesday/Java/pokemmo/protocol/src/main/java/top/totodile/pokemmo/protocol/GameTicket.java) | [ServerService.generateSessionKey](/E:/PokeMMO/Pokemmo2Pro-Server/server/src/main/java/org/server/services/ServerService.java:25) |
| 创建角色事务 | [CharacterStore](/E:/Wednesday/Java/pokemmo/sever/persistence/src/main/java/top/totodile/pokemmo/persistence/CharacterStore.java) | [CharacterCreationService](/E:/PokeMMO/Pokemmo2Pro-Server/server.game/src/main/java/org/pokemmo/gameserver/services/character/CharacterCreationService.java:28) |
| 登录表单／最低 ROM 门槛 | [LoginScreen.submit](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/LoginScreen.java:606) | [ChatHistoryViewportComponent.cOm2](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/widget/component/ChatHistoryViewportComponent.java:1063) |
| V-Sync | [Main](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/Main.java:46)、[ClientSettings](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ClientSettings.java) | [GameSettingsWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/settings/GameSettingsWindow.java:1661)、[ClientSettings](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/config/ClientSettings.java:40) |
| ROM／MOD | [RomFiles](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/RomFiles.java)、[ModFiles](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ModFiles.java) | [ClientSettingsWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/settings/ClientSettingsWindow.java)、[ModManagerWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/settings/ModManagerWindow.java) |
| 外观创建／角色选择 | [CharacterCreationWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/CharacterCreationWindow.java)、[CharacterSelectionWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/CharacterSelectionWindow.java) | [AvatarCustomizationWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/settings/AvatarCustomizationWindow.java)、[AvatarCustomizerComponent](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/widget/component/AvatarCustomizerComponent.java:90) |
| 地图／图块／人物层级 | [FireRedMap](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/rom/FireRedMap.java)、[GbaTileAtlas](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/rom/GbaTileAtlas.java) | [GbaMapLayout](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/rom/gba/map/GbaMapLayout.java)、[GbaMapTileset](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/rom/gba/tileset/GbaMapTileset.java)、[GdxTileLayerRenderer](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/world/render/GdxTileLayerRenderer.java:38) |
| 镜头与过渡 | [WorldCamera](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/world/WorldCamera.java)、[KantoWorld](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/world/KantoWorld.java) | [GdxParticleSystemRenderer](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/graphics/gdx/particle/GdxParticleSystemRenderer.java:46)、[GdxParticleEmitterNode](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/graphics/gdx/particle/GdxParticleEmitterNode.java:75) |
| 通知 | [NotificationWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/NotificationWindow.java) | [NotificationWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/misc/NotificationWindow.java) |
| 名片／背包／GM | [TrainerCardWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/TrainerCardWindow.java)、[BagWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/BagWindow.java)、[GmMenuWindow](/E:/Wednesday/Java/pokemmo/client/src/main/java/top/totodile/pokemmo/ui/GmMenuWindow.java) | [TrainerCardWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/social/TrainerCardWindow.java)、[InventoryWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/inventory/InventoryWindow.java)、[GmMenuWindow](/E:/PokeMMO/Pokemmo2Pro-Client/src/main/java/cn/pokemmo/ui/window/admin/GmMenuWindow.java:83) |

本次没有把凭据、用户名的真实密码、数据库连接密码或私钥写入报告。
