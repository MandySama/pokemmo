## 模块与单机入口

- `server:login`：登录接口、会话、服务器目录和单机登录实现。非空账号密码可进入本地会话，提供一个名为“Live”的本地服务器入口，不访问网络、不注册账号、不保留密码，也不依赖客户端。
- `server:game`：单机角色查询、创建、删除、校验和事务，数据保存在 `save/game.db`，不依赖客户端。
- `server:db`：公共数据库执行接口；`server:db:desktop` 提供 JDBC SQLite 适配，`server:db:android` 提供 Android 原生 SQLite 适配。客户端入口仅传入存档路径并组装服务，SQL 和角色业务保留在 `server:game`。JDBC 驱动只随 PC 打包。
- `server:chat`：预留聊天服务模块，目前没有聊天实现。
- `client:core`：界面与登录状态管理，调用 `server:login` 提供的服务。
- `client:desktop`、`client:android`：组装入口，通过 `server:login` 的 `LoginServices.create()` 获取服务，当前统一指向单机分支 `SINGLE_PLAYER`，使用 `LocalLoginService`。

登录流程：客户端提交输入 → `server:login` 校验非空账号密码 → 成功后按“记住我”保存 → 显示服务器名称/状态/登录按钮 → 请求选择服务器 → 显示空角色选择窗口。失败时由客户端显示错误提示并处理焦点。
服务器选择页仅显示参考客户端的服务器名称、状态和逐行登录按钮，不额外添加提示文案或返回按钮。
选择结果由 `ClientApplication.selectedServer()` 提供给后续角色/世界入口；目前尚不进入地图。
角色选择窗口沿用参考的 PC/移动端主题及布局，显示“新角色”和“注销”；注销释放会话并返回登录。“新角色”目前仅绘制按钮，尚未接入创建界面或角色存档。
请求采用异步接口，客户端回调回到渲染线程；禁止重复提交，返回登录或销毁界面后忽略旧回调并释放过期会话。

`LoginServices` 保留 `SINGLE_PLAYER` 和 `ONLINE` 两个分支，默认选择单机。`ONLINE` 暂无实现，显式选择时抛出未实现异常，不回退到单机。网络协议、联机状态及其文案在实际接入联机时再实现。

## 主题、语言与 MOD 资源

PC 的 `config/` 与 `data/` 同级，主题目录为 `data/themes/`；Android 使用外部应用目录 `Android/data/top.totodile.pokemmo/files/config/`、`Android/data/top.totodile.pokemmo/files/data/`，构建时将 `data/` 打成 APK assets 中的 `resources.zip` 和 `manifest.json`。首次启动显示进度并解压到 `files/data/`，之后从该目录读取基础资源；资源版本与文件大小检查通过时跳过解压，缺失或版本变化时重装。两端使用同一套资源解析和 XML 加载代码。

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

当前已验证范围是现有登录界面的主题、语言、背景纹理和 `sprites/itemicons/` 图标覆盖。地图、战斗、音频及主题 `sprite_atlas` 的游戏渲染消费端尚未实现，不能据此视为所有官方 MOD 都已兼容。菜单仍只负责展开/收起，尚无图形化 MOD 导入或启用功能。

## ROM 读取与管理

ROM 放在与 `config/`、`data/` 同级的 `roms/` 目录，开发环境为 `client/roms/`；Android 为应用外部目录 `Android/data/top.totodile.pokemmo/roms/`。启动及打开客户端管理时自动扫描 `.gba`、`.nds`，也可以点击“重新扫描 ROM”。按文件头游戏代码识别火红、绿宝石、黑/白、白金、心金/魂银，不依赖文件名；文件头校验失败、文件被截断或 NDS 文件表损坏时显示错误，并继续加载其他有效文件。

左上角按 NDS/GBA 显示已加载游戏与代码。客户端管理每行显示实际加载状态，点击状态查看标题、修订版本及大小，悬停可查看路径与 NDS 资源数量。“选择文件”使用桌面文件选择器或 Android 系统文档选择器，后台校验并复制到 `roms/`，成功后立即更新界面并保存 `client.roms.<类型编号>` 配置。类型编号为火红 0、绿宝石 1、黑/白 2、白金 3、心金/魂银 4；配置指定文件优先于目录自动扫描。导入失败保留原有选择，取消选择不修改配置。桌面端“打开ROM文件夹”打开实际扫描目录。ROM 不随发行包分发。

`ClientApplication.romResources()` 提供统一读取入口；`RomResources.Rom.readFile(path)` 读取 NDS FNT/FAT 中的原始文件，`files()` 列出路径及范围；GBA 使用 `readGba(address, length)` 读取偏移或 `0x08000000` 起始的卡带地址。读取有边界检查，每次读取关闭文件句柄，不把整个卡带常驻内存。此入口提供原始资源，地图、战斗、音频等格式解码和渲染消费端仍需后续接入。

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
