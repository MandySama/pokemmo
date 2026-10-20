## 模块与单机入口

- `shared:login`：纯 Java 的登录服务契约、会话、服务器目录和服务器选择结果。
- `server:login`：当前的单机登录实现。非空账号密码可进入本地会话，提供一个名为“Live”的本地服务器入口，不访问网络、不注册账号、不保留密码。
- `server:chat`、`server:game`：预留聊天与游戏服务模块，目前没有聊天、角色、地图或战斗实现。
- `client:core`：界面与登录状态管理，只依赖共享契约，不依赖任何 `server` 模块。
- `client:desktop`、`client:android`：组装入口，目前注入 `LocalLoginService`。

登录流程：本地校验 → 请求登录服务 → 成功后按“记住我”保存 → 显示服务器名称/状态/登录按钮 → 请求选择服务器 → 显示空角色选择窗口。
服务器选择页仅显示参考客户端的服务器名称、状态和逐行登录按钮，不额外添加提示文案或返回按钮。
选择结果由 `ClientApplication.selectedServer()` 提供给后续角色/世界入口；目前尚不进入地图。
角色选择窗口沿用参考的 PC/移动端主题及布局，显示“新角色”和“注销”；注销释放会话并返回登录。“新角色”目前仅绘制按钮，尚未接入创建界面或角色存档。
请求采用异步接口，客户端回调回到渲染线程；禁止重复提交，返回登录或销毁界面后忽略旧回调并释放过期会话。

当前只实现单机流程和模块调用边界。网络协议、联机状态及其文案在实际接入联机时再实现，不提前添加。

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
