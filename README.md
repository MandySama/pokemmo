## 模块与单机入口

- `server:login`：登录接口、会话、服务器目录和单机登录实现。非空账号密码可进入本地会话，提供一个名为“Live”的本地服务器入口，不访问网络、不注册账号、不保留密码，也不依赖客户端。
- `server:chat`、`server:game`：预留聊天与游戏服务模块，目前没有聊天、角色、地图或战斗实现。
- `client:core`：界面与登录状态管理，调用 `server:login` 提供的服务。
- `client:desktop`、`client:android`：组装入口，通过 `server:login` 的 `LoginServices.create()` 获取服务，当前统一指向单机分支 `SINGLE_PLAYER`，使用 `LocalLoginService`。

登录流程：客户端提交输入 → `server:login` 校验非空账号密码 → 成功后按“记住我”保存 → 显示服务器名称/状态/登录按钮 → 请求选择服务器 → 显示空角色选择窗口。失败时由客户端显示错误提示并处理焦点。
服务器选择页仅显示参考客户端的服务器名称、状态和逐行登录按钮，不额外添加提示文案或返回按钮。
选择结果由 `ClientApplication.selectedServer()` 提供给后续角色/世界入口；目前尚不进入地图。
角色选择窗口沿用参考的 PC/移动端主题及布局，显示“新角色”和“注销”；注销释放会话并返回登录。“新角色”目前仅绘制按钮，尚未接入创建界面或角色存档。
请求采用异步接口，客户端回调回到渲染线程；禁止重复提交，返回登录或销毁界面后忽略旧回调并释放过期会话。

`LoginServices` 保留 `SINGLE_PLAYER` 和 `ONLINE` 两个分支，默认选择单机。`ONLINE` 暂无实现，显式选择时抛出未实现异常，不回退到单机。网络协议、联机状态及其文案在实际接入联机时再实现。

## 主题、语言与 MOD 资源

PC 的 `config/` 与 `data/` 同级，主题目录为 `data/themes/`；Android 使用应用内部 `files/config/`、`files/data/`，安装包中的 `data/` 作为基础资源。两端使用同一套资源解析和 XML 加载代码。

`data/themes/default/`、`data/themes/android/` 完整同步参考项目，保留全部 include、字体、图像和子主题。同步工具为 `tools/import_reference_themes.py <参考项目的 data/themes 目录>`，不裁剪节点。Android 入口保留对 `../default/` 的相对引用。Logo 使用 `logingui.logo`，菜单行使用 `hud-item-button`，不另设按图标编号命名的主题。

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

布局沿用 XML 的边框、对齐、具名间隔以及控件主题路径；自动间隔使用 `smallGap`。渲染支持图像覆盖、旋转、平铺、渐变和主题光标。完整 Android XML 的同名节点有省略 `ref` 的增量定义；重建源码在此会丢失基础参数，本项目保留已有定义，显式 `overwrite="true"` 才重置，并以完整 Android 主题回归验证此行为。

当前已验证范围是现有登录界面的主题、语言、背景纹理和 `sprites/itemicons/` 图标覆盖。地图、战斗、音频及主题 `sprite_atlas` 的游戏渲染消费端尚未实现，不能据此视为所有官方 MOD 都已兼容。菜单仍只负责展开/收起，尚无图形化 MOD 导入或启用功能。

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
