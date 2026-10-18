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
