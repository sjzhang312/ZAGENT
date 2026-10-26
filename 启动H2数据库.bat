@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
title ZAGENT H2 Launcher

echo ==============================================
echo  ZAGENT H2 数据库一键启动
echo ==============================================

rem ---- 1. 定位 h2 jar（版本必须与后端一致，避免数据库文件格式不兼容）----
where java >nul 2>nul
if errorlevel 1 (
    echo [ZAGENT] 未找到 java 命令，请先安装 JDK 17 并配置 PATH。
    pause
    exit /b 1
)
rem 从后端 pom 读取 h2.version，读不到则用兜底版本
set "H2_VERSION=2.3.232"
if exist "%ROOT%backend\pom.xml" (
    for /f "tokens=3 delims=<>" %%v in ('findstr /r /c:"<h2.version>" "%ROOT%backend\pom.xml"') do set "H2_VERSION=%%v"
)
set "H2JAR="
set "H2_LOCAL=%ROOT%backend\lib\h2-%H2_VERSION%.jar"
set "H2_M2=%USERPROFILE%\.m2\repository\com\h2database\h2\%H2_VERSION%\h2-%H2_VERSION%.jar"
if exist "%H2_LOCAL%" set "H2JAR=%H2_LOCAL%"
if not defined H2JAR if exist "%H2_M2%" set "H2JAR=%H2_M2%"
if not defined H2JAR (
    echo [ZAGENT] 未找到 h2-%H2_VERSION%.jar，尝试通过 Maven 下载到 backend\lib ...
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ZAGENT] 未找到 mvn 命令。请先安装 Maven 并构建一次后端，再运行本脚本。
        pause
        exit /b 1
    )
    call mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy -Dartifact=com.h2database:h2:%H2_VERSION% -DoutputDirectory="%ROOT%backend\lib"
    if exist "%H2_LOCAL%" set "H2JAR=%H2_LOCAL%"
)
if not defined H2JAR (
    echo [ZAGENT] h2 jar 获取失败（期望版本 %H2_VERSION%）。
    pause
    exit /b 1
)
rem 校验 jar 确实包含 H2 Server（避免误选 sources/javadoc 包导致进程一闪而过）
java -cp "%H2JAR%" org.h2.tools.Server -help >nul 2>&1
if errorlevel 1 (
    echo [ZAGENT] h2 jar 无效：%H2JAR%
    echo [ZAGENT] 该文件不含 org.h2.tools.Server（可能是 sources/javadoc 包），请删除后重试。
    pause
    exit /b 1
)
echo [ZAGENT] 使用 H2: %H2JAR%

rem ---- 2. 数据目录与日志目录 ----
if not exist "%ROOT%data" mkdir "%ROOT%data"
if not exist "%ROOT%logs" mkdir "%ROOT%logs"

rem ---- 3. 启动 H2 服务（TCP 9101 + Web 控制台 8082，仅本机访问）----
netstat -ano | findstr ":9101 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [ZAGENT] H2 服务已在运行（端口 9101），跳过启动。
) else (
    echo [ZAGENT] 启动 H2 服务进程（窗口标题 ZAGENT-H2-DB，请勿关闭）...
    rem 服务端输出重定向到 logs\h2.log，进程异常退出后仍可查看失败原因
    start "ZAGENT-H2-DB" /min cmd /c "java -cp "%H2JAR%" org.h2.tools.Server -tcp -tcpPort 9101 -web -webPort 8082 -ifNotExists > "%ROOT%logs\h2.log" 2>&1"
)

rem ---- 4. 等待端口就绪 ----
set /a tries=0
:waitloop
netstat -ano | findstr ":9101 " | findstr "LISTENING" >nul
if not errorlevel 1 goto h2_ready
set /a tries+=1
if !tries! lss 15 (
    %SystemRoot%\System32\ping.exe -n 2 127.0.0.1 >nul
    goto waitloop
)
echo [ZAGENT] H2 服务启动超时。最近日志（%ROOT%logs\h2.log）：
if exist "%ROOT%logs\h2.log" type "%ROOT%logs\h2.log"
pause
exit /b 1
:h2_ready

rem ---- 5. 执行初始化脚本（幂等：CREATE IF NOT EXISTS + MERGE，可重复执行）----
echo [ZAGENT] 执行初始化脚本 sql\h2-init.sql ...
java -cp "%H2JAR%" org.h2.tools.RunScript -url "jdbc:h2:tcp://localhost:9101/./data/zagent;MODE=MySQL;DATABASE_TO_LOWER=TRUE" -user sa -password "" -script "%ROOT%sql\h2-init.sql"
if errorlevel 1 (
    echo [ZAGENT] 初始化脚本执行失败，请检查上方错误信息。
    pause
    exit /b 1
)

echo.
echo ==============================================
echo  ZAGENT H2 数据库已就绪
echo  JDBC   : jdbc:h2:tcp://localhost:9101/./data/zagent;MODE=MySQL;DATABASE_TO_LOWER=TRUE
echo  控制台 : http://localhost:8082 （用户 sa，密码为空）
echo  停止   : 运行 停止H2数据库.bat
echo  数据   : %ROOT%data （删除该目录即可重置数据库）
echo ==============================================
pause
