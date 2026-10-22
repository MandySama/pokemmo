## 模块与单机入口

- `server:login`：登录接口、会话、服务器目录和单机登录实现。非空账号密码可进入本地会话，提供一个名为“Live”的本地服务器入口，不访问网络、不注册账号、不保留密码，也不依赖客户端。
- `server:game`：单机角色查询、创建、删除、进入、位置保存、校验和事务，数据保存在 `save/game.db`，不依赖客户端。
- `server:db`：公共数据库执行接口；`server:db:desktop` 提供 JDBC SQLite 适配，`server:db:android` 提供 Android 原生 SQLite 适配。客户端入口仅传入存档路径并组装服务，SQL 和角色业务保留在 `server:game`。JDBC 驱动只随 PC 打包。
- `server:chat`：预留聊天服务模块，目前没有聊天实现。
- `client:core`：界面与登录状态管理，调用 `server:login` 提供的服务。
- `client:desktop`、`client:android`：组装入口，通过 `server:login` 的 `LoginServices.create()` 获取服务，当前统一指向单机分支 `SINGLE_PLAYER`，使用 `LocalLoginService`。

`client:core` 在 `top.totodile.pokemmo.client` 下按职责分包，根包的 `ClientApplication` 负责应用生命周期和组装：

- `ui`：根包只保留 `ClientUi`，负责界面组装、页面导航和输入生命周期。
- `resources`：`ClientResources` 管理资源搜索、主题和 MOD；`ClientStrings` 处理语言；`AssetUrlStreamHandler` 提供主题资源 URL 访问。
- `config`：`ClientSettings` 读写客户端设置，`SavedAccounts` 管理已保存账号及加密凭据。
- `rom`：ROM 信息模型、扫描导入、校验和资源读取。
- `rom.gba`：火红地图组、布局、LZ10 图块、调色板和元图块解码。
- `world`：GBA 地图绘制、遮挡、镜头、移动与地图切换；移动端使用参考十字键资源。
- `twl`：LibGDX 与 TWL 的渲染、字体适配。

界面通过资源和配置的公开入口访问数据；资源、配置和 ROM 包不依赖窗口控件。平台文件选择、资源安装和启动组装仍放在桌面/Android 模块。

UI 再按功能分一层：`ui.login` 包含登录、账号选择、服务器选择、登录状态及公告；`ui.character` 包含角色列表、创建、删除、外观及预览；`ui.settings` 包含设置窗口与页签；`ui.management` 包含客户端及 MOD 管理；`ui.menu` 包含桌面菜单、移动端侧栏和电量/菜单入口；`ui.startup` 包含资源安装进度、滚动背景及启动背景；`ui.common` 放置公共窗口基类、退出确认和帧率计数器。功能窗口公开组装、输入和生命周期所需的入口；账号下拉框、角色预览、外观调色板和设置页签等内部实现保持包内可见。公共窗口基类向子类提供受保护的宿主访问和打开方法。

登录流程：客户端提交输入 → `server:login` 校验非空账号密码 → 成功后按“记住我”保存 → 显示服务器名称/状态/登录按钮 → 请求选择服务器 → 显示空角色选择窗口。失败时由客户端显示错误提示并处理焦点。
服务器选择页仅显示参考客户端的服务器名称、状态和逐行登录按钮，不额外添加提示文案或返回按钮。
选择结果由 `ClientApplication.selectedServer()` 提供给后续角色/世界入口；目前尚不进入地图。
角色选择窗口沿用参考的 PC/移动端主题及布局，显示“新角色”和“注销”；注销释放会话并返回登录。“新角色”目前仅绘制按钮，尚未接入创建界面或角色存档。
请求采用异步接口，客户端回调回到渲染线程；禁止重复提交，返回登录或销毁界面后忽略旧回调并释放过期会话。

`LoginServices` 保留 `SINGLE_PLAYER` 和 `ONLINE` 两个分支，默认选择单机。`ONLINE` 暂无实现，显式选择时抛出未实现异常，不回退到单机。网络协议、联机状态及其文案在实际接入联机时再实现。

## 主题、语言与 MOD 资源

Windows x64 的 PC 登录背景从已加载的黑白 ROM 读取：`a/1/6/0` 内的莱希拉姆/捷克罗姆主模型、独立倒影模型、棋盘背景及光束、光晕，连同骨骼动画、眨眼贴图和镜头轨迹，启动时随机选择一套，按参考的 30 FPS 时间轴循环播放，随窗口尺寸调整镜头。倒影使用 ROM 自带模型和独立动画，光效沿用 ROM 贴图、透明度与节点动画。Android 保留原有滚动背景。PC 未加载黑白 ROM 时使用滚动背景；已加载时仅显示 ROM 场景，模型准备或读取失败期间保留黑色底色，不切回滚动背景。在客户端管理中更换 ROM 后会重新加载。

骨骼动画由 `RomModelAnimation` 直接解析 NSBCA，按参考 `NdsNitroModelParser.Xy` 及其曲线通道展开完整的 30 FPS 关键帧。转换器只提供网格和骨骼层级，生成的动画全部丢弃，避免长动画边界字段及采样宽度解码错误。当前验证了黑白标题所用的逐帧、隔帧采样，其它采样格式明确报错。

PC 与 Android ARM64 登录背景音乐使用黑白 ROM 的 `wb_sound_data.sdat`，按参考 `AudioManager.bk0` 的登录默认曲目 1159 合成 48kHz 立体声并循环播放。沿用音乐音量及“窗口失焦时静音”设置；失焦静音默认开启，与参考一致：后台音量归零但合成时间轴继续，回到前台恢复当前进度，不暂停或重播。音量采用参考的平方曲线并在 Sseqj 合成阶段应用。每批 960 帧，混音前清零 PCM；Android 通过 AudioTrack 播放，保留滚动背景，切后台按失焦静音设置处理，进入地图或退出时释放播放器。桌面输出按参考 `Client.main` 配置为 1920 字节、6 个缓冲，退出释放音频设备和播放器。模型和音频从用户本机 ROM 读取，不随安装包分发；模型转换在后台执行，缓存位于 `cache/login/`。

PC 包随附模型转换器与 Sseqj 原生合成器，运行时不依赖 `E:\PokeMMO` 或联网下载工具。第三方组件来源、版本、许可和转换器重建步骤见 [native-tools/README.md](client/desktop/native-tools/README.md)。当前随附的原生工具针对 Windows x64，其他 PC 平台保留原背景。

PC 的 `config/` 与 `data/` 同级，主题目录为 `data/themes/`；Android 使用外部应用目录 `Android/data/top.totodile.pokemmo/files/config/`、`Android/data/top.totodile.pokemmo/files/data/`，构建时将背景和解压弹窗必需的 XML、字体、图集放入 APK assets 的独立 `startup/` 目录，其余 `data/` 打成 `resources.zip` 和 `manifest.json`，不重复存放资源。首次启动先显示滚动背景，再使用参考 ROM 解压弹窗的 TWL/XML 主题显示绿色进度条，并解压到 `files/data/`，之后从该目录读取基础资源；资源版本与文件大小检查通过时跳过解压，缺失或版本变化时重装。两端使用同一套资源解析和 XML 加载代码。

`data/themes/default/`、`data/themes/android/` 只包含当前登录、服务器选择、角色入口、公告和菜单使用的主题及其依赖，不提前导入地图、战斗等页面。Android 入口保留对 `../default/` 的相对引用。Logo 使用 `logingui.logo`，菜单行使用 `hud-item-button`，不另设按图标编号命名的主题。XML 引用、覆盖、字体和资源解析机制保留，后续 MOD 可以替换当前界面的样式和资源。

资源顺序：可写 `data/` → 安装包基础资源；先应用内置 `data/resources.zip`（不存在则使用 `data/resources/`），再按配置顺序应用 `data/mods/` 下的目录、`.zip` 或 `.mod`。后启用的包优先覆盖。归档在客户端关闭时释放；APK 内的 ZIP 复制到内部缓存后读取。压缩包需保留目录条目，与参考加载器要求一致。

在 `config/main.properties` 中配置，修改后重启：

```properties
client.mods.enabled_mods=first.mod/second.zip
client.ui.theme=mytheme
client.ui.chat.language.current_language=zh
```

MOD 根目录放置 `info.xml`，主题和语言按参考元数据注册：

```xml
<resource>
  <themes theme_revision="3">
    <theme name="mytheme" path="data/themes/mytheme/" is_mobile="false"/>
  </themes>
  <strings string_revision="1">
    <string path="translations/zh.xml"/>
  </strings>
</resource>
```

主题入口为 `data/themes/mytheme/theme.xml`，可相对 include 基础主题，再覆盖所需节点。移动端主题需声明 `is_mobile="true"`。`default`、`android` 是保留名称；重复名称、缺失文件和过高 revision 会使该包加载失败；旧 revision 不注册对应主题或语言。自定义主题解析失败时记录日志并回退平台默认主题。普通语言目录、主语言、翻译和 MOD 覆盖容器仍按参考顺序合并，支持 STRING 引用、字体地区、归档文本、preload 和十六进制参数占位。

布局使用 XML 的边框、对齐、具名间隔以及控件主题路径；自动间隔使用 `smallGap`。同名节点增量覆盖已有定义，显式 `overwrite="true"` 才重置。输入框内部文字区域使用零边距、垂直居中，保证文字、选区和插入光标坐标一致；PC 默认使用系统箭头和输入区域的系统 I 形指针，MOD 显式指定光标时才使用主题光标。

MOD 管理窗口支持导入 `.mod` 和 `.zip`：校验根目录的 `info.xml`，按原文件名保存到可写 `data/mods/`，并显示名称、作者及版本。同名文件拒绝覆盖，校验或复制失败会清理临时文件；导入过程显示进度及参考客户端的结果提示。PC 使用文件选择器，Android 支持系统文档流。当前仅实现导入，不自动启用或应用 MOD，也不修改启用配置。

当前 MOD 已验证范围是现有登录界面的主题、语言、背景纹理和 `sprites/itemicons/` 图标覆盖。地图、战斗、音频 MOD 及主题 `sprite_atlas` 的游戏渲染覆盖尚未实现，不能据此视为所有官方 MOD 都已兼容。

## ROM 读取与管理

ROM 放在与 `config/`、`data/` 同级的 `roms/` 目录，开发环境为 `client/roms/`；Android 为应用外部目录 `Android/data/top.totodile.pokemmo/roms/`。启动时自动扫描 `.gba`、`.nds`。按文件头游戏代码识别火红、绿宝石、黑/白、白金、心金/魂银，不依赖文件名；文件头校验失败、文件被截断或 NDS 文件表损坏时记录错误，并继续加载其他有效文件。

左上角按参考客户端的固定顺序显示：NDS 使用卡带标题，GBA 使用游戏代码及 `v1.<修订号>`。客户端管理未加载时显示“必需/可选”，已加载时显示“信息”；点击打开参考样式的模态弹窗，显示 ROM 名称和文件路径。“选择文件”使用桌面文件选择器或 Android 系统文档选择器；PC 校验后直接引用所选文件，Android 文档流复制到应用 `roms/`，成功后更新界面并保存 `client.roms.<类型编号>` 配置。类型编号为火红 0、绿宝石 1、黑/白 2、白金 3、心金/魂银 4；配置指定文件优先于目录自动扫描。导入失败保留原有选择，取消选择不修改配置。桌面端“打开ROM文件夹”打开实际扫描目录。ROM 不随发行包分发。

ROM 代码位于 `client:core` 的 `top.totodile.pokemmo.client.rom` 包：`RomManager` 负责扫描、加载状态和导入，通过 `Configuration` 接口保存选择；`RomReader` 负责识别、校验、文件表解析及按需读取；`RomInfo`、`RomType`、`RomEntry` 保存元信息与资源索引；`RomFilePicker` 定义平台文件选择接口，具体选择器留在桌面和 Android 模块。

`ClientApplication.romResources()` 返回 `RomManager`，通过 `get(RomType)` 获取 `RomInfo`。`RomReader.readFile(rom, path)` 读取 NDS FNT/FAT 中的原始文件，`rom.files()` 列出路径及范围；GBA 使用 `RomReader.readGba(rom, address, length)` 读取偏移或 `0x08000000` 起始的卡带地址。原始读取有边界检查，每次读取关闭文件句柄；具体格式解码由消费端处理。

## 关都地图与角色进入

加载火红 ROM 后，登录本地服务器并点击已有角色卡片即可进入地图。新角色按参考服务端从真新镇自家二楼（地图组 4、地图 1、坐标 6/6）开始；以后从 SQLite 中保存的位置继续。进入前校验账号与服务器归属，地图加载失败返回角色选择并显示已有系统错误提示，进入成功后释放登录 3D 场景及背景音乐。

`FireRedMaps` 根据参考客户端的指令特征定位地图组表，读取地图头、布局、边框、两套图块、BGR555 调色板、4bpp 图形、LZ10 压缩、传送点和连接。本项目的 BPRE v1.0 ROM 已验证 43 个地图组、425 张地图可解码。加载期间在后台读取整份 GBA ROM；地图按需解析，解码图块和 GPU 图集有数量上限，退出应用释放资源，不导出或分发 ROM 图像。

PC 使用 WASD 移动，同时支持方向键和设置中的 `client.controls.gdx.key_up/down/left/right`。K 切换跑鞋（保存为自动奔跑设置），2 或小键盘 2 上下自行车。步行、跑步、骑车分别采用参考每格 275/135/80 毫秒和三套外观动画；移动时间轴保留跨格剩余时间，渲染帧率不会改变速度。角色落点扣除素材的实际透明留白，地图按参考的覆盖标志和高程分层绘制。Android 使用参考 `buttons/controller.png` 的左下十字键，支持现有缩放、死区及显示配置。地图沿用 GBA 缩放设置，显示角色保存的服装、图层遮挡及相邻道路。已经验证自家二楼下楼、出门到真新镇和连接 1 号道路，以及角色位置保存、重新进入和账号隔离。

进入本地地图后，按反引号键（`~` 所在键）打开半屏控制台，回车执行，Esc 或反引号关闭，上下方向键浏览历史。支持 `noclip` 切换穿墙、`noclip on/off` 明确开关、`speed 2` 两倍移动速度、`speed 1` 恢复正常；`timescale` 为 `speed` 的别名。倍率范围为大于 0、不超过 16，仅影响本地移动及对应角色动画。穿墙不越过无地图数据的边界，仍可正常传送；墙内位置不会覆盖保存的正常位置，需走回正常地面才能关闭穿墙。控制台输入期间暂停方向键和 K/2 的动作响应。

这是地图进入和基本行走阶段：尚未执行 NPC、剧情脚本、战斗、冲浪、跳台、特殊动态传送和地图图块动画。地图资源可解码不代表全部关都玩法已实现。二进制结构同时核对了 [pret/pokefirered 的地图定义](https://github.com/pret/pokefirered/blob/master/include/global.fieldmap.h) 与 [图块属性](https://github.com/pret/pokefirered/blob/master/src/fieldmap.c)。游戏内尚未与已登录的官方角色画面逐项对照。

## 运行与打包

```powershell
# 启动桌面客户端
.\gradlew.bat :client:run

# 打包全部：JAR、EXE、ZIP、APK
.\gradlew.bat :client:build

# 打包桌面端：JAR、EXE、ZIP
.\gradlew.bat :client:desktop:build

# 仅打 JAR 包
.\gradlew.bat :client:desktop:build-jar

# 打 EXE 及 ZIP 包
.\gradlew.bat :client:desktop:build-exe

# 打 Android APK 包
.\gradlew.bat :client:android:build

# 自动构建并安装到全部在线设备（跳过离线、未授权设备）
.\gradlew.bat :client:android:install
```

## ADB

```powershell
# 查看设备
adb devices -l

# 无线调试：将占位符替换为手机显示的地址和端口
adb pair "<IP>:<配对端口>"
adb connect "<IP>:<连接端口>"
```

Android 配置和存档分别位于 `/storage/emulated/0/Android/data/top.totodile.pokemmo/files/config/` 和同一 `files/` 下的 `save/game.db`；实际存储卷以系统返回的外部应用目录为准。使用应用自己的外部目录，无需所有文件访问权限。文件管理器是否能打开 `Android/data` 仍取决于其系统授权。

## Android 签名

Debug 和 Release 统一使用 `client/android/signing/pokemmo.keystore`，密码及别名从 `client/android/signing.properties` 读取（格式见同目录的 `signing.properties.example`）。当前密钥沿用原电脑的调试证书，以保持现有安装的覆盖更新能力。

签名密钥和凭据随工程提交到 Git，其他电脑拉取后即可使用同一签名构建。缺少签名文件或凭据时构建失败，不使用各电脑自动生成的默认密钥。

当前证书 SHA-256：`754db2b31e653e7b85365ef69d49221474e0c5d3ec3f40843122869814d291d4`。可通过 Android SDK 的 `apksigner verify --print-certs <APK路径>` 核对，构建内容变化不会改变这个证书指纹。
