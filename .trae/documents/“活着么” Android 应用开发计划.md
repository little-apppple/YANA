# “活着么” Android 应用开发计划

本计划旨在从零构建一个基于 Android 原生的安全签到应用。

## 1. 项目初始化 (Project Setup)
*   **目标**: 建立标准的 Android 项目结构，配置 Kotlin 和 Jetpack Compose 环境。
*   **关键文件**:
    *   `build.gradle.kts` (Project & Module): 配置 Compose, Room, DataStore, WorkManager 等依赖。
    *   `AndroidManifest.xml`: 声明 `FOREGROUND_SERVICE`, `SEND_SMS`, `READ_PHONE_STATE` 等权限，**不声明** `INTERNET` 权限。
    *   `app/src/main/java/.../StillAliveApp.kt`: Application 入口。

## 2. 数据层实现 (Data Layer)
*   **目标**: 实现本地数据存储和配置管理。
*   **Room 数据库**:
    *   `SignRecord`: 记录签到时间、方式（手动/自动）。
    *   `SmsRecord`: 记录短信发送时间、接收人、内容。
    *   `AppDatabase` & `Dao`: 数据库访问接口。
*   **配置存储 (DataStore)**:
    *   存储用户名、欢迎语、紧急联系人、断签阈值、模式设置（手动/自动）。
*   **数据导入导出**:
    *   实现将数据库记录导出为 CSV/JSON 到本地存储，以及从文件恢复。

## 3. 核心业务逻辑 (Core Logic)
*   **签到管理器 (CheckInManager)**:
    *   封装签到逻辑，记录当日是否已签到。
    *   提供 `checkIn()` 方法，自动写入数据库。
*   **后台监测服务 (MonitorService)**:
    *   实现前台服务 (`Foreground Service`)，确保应用在后台存活。
    *   注册广播接收器监听 `ACTION_USER_PRESENT` (屏幕解锁)，触发自动签到逻辑。
*   **安全守护 Worker (SafetyWorker)**:
    *   定期任务（每日执行），检查最后一次签到时间。
    *   逻辑：`if (当前时间 - 最后签到时间 > 设定天数)` -> 触发短信发送。
*   **短信助手 (SmsHelper)**:
    *   封装 `SmsManager` 发送逻辑。

## 4. UI 界面实现 (UI Implementation)
*   **技术栈**: Jetpack Compose (Material Design 3)。
*   **页面规划**:
    *   **主页 (HomeScreen)**: 显示当前日期、签到状态、欢迎语。提供巨大的“手动签到”按钮。
    *   **设置页 (SettingsScreen)**: 配置用户名、联系人、阈值、模式开关、数据管理。
    *   **历史页 (HistoryScreen)**: 列表展示签到和报警记录。
    *   **权限页 (PermissionScreen)**: 引导用户授予短信、后台运行等敏感权限。
*   **导航**: 使用 `Navigation Compose` 管理页面跳转。

## 5. 权限与系统集成
*   **权限管理**: 处理 Android 运行时权限请求流程。
*   **保活策略**: 引导用户在设置中开启“忽略电池优化”以保证后台服务不被杀。

## 执行顺序
1.  初始化 Gradle 构建文件和 Manifest。
2.  创建数据模型和数据库。
3.  实现 UI 框架和主页。
4.  集成前台服务和自动签到逻辑。
5.  实现设置页和短信发送逻辑。
6.  测试断签报警功能。
