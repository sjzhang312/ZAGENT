@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
title ZAGENT H2 Launcher

echo ==============================================
echo  ZAGENT H2 数据库一键启动
echo ==============================================

rem ---- 1. 定位 h2 jar（本地 Maven 仓库优先，否则用 mvn 下载到 backend\lib）----
set "H2JAR="
for /f "delims=" %%f in ('dir /b /s /o:n "%USERPROFILE%\.m2\repository\com\h2database\h2\h2-*.jar" 2^>nul') do set "H2JAR=%%f"
if not defined H2JAR (
    echo [ZAGENT] 本地 Maven 仓库未找到 h2 jar，尝试通过 Maven 下载到 backend\lib ...
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ZAGENT] 未找到 mvn 命令。请先安装 Maven 并构建一次后端，再运行本脚本。
        pause
        exit /b 1
    )
    call mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy -Dartifact=com.h2database:h2:2.3.232 -DoutputDirectory="%ROOT%backend\lib"
    for %%f in ("%ROOT%backend\lib\h2-2.3.232.jar") do set "H2JAR=%%~ff"
)
if not defined H2JAR (
    echo [ZAGENT] h2 jar 获取失败。
    pause
    exit /b 1
)
echo [ZAGENT] 使用 H2: %H2JAR%

rem ---- 2. 数据目录 ----
if not exist "%ROOT%data" mkdir "%ROOT%data"

rem ---- 3. 启动 H2 服务（TCP 9101 + Web 控制台 8082，仅本机访问）----
netstat -ano | findstr ":9101 " | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [ZAGENT] H2 服务已在运行（端口 9101），跳过启动。
) else (
    echo [ZAGENT] 启动 H2 服务进程（窗口标题 ZAGENT-H2-DB，请勿关闭）...
    start "ZAGENT-H2-DB" /min java -cp "%H2JAR%" org.h2.tools.Server -tcp -tcpPort 9101 -web -webPort 8082 -ifNotExists
)

rem ---- 4. 等待端口就绪 ----
set /a tries=0
:waitloop
timeout /t 1 /nobreak >nul
netstat -ano | findstr ":9101 " | findstr "LISTENING" >nul
if errorlevel 1 (
    set /a tries+=1
    if !tries! lss 15 goto waitloop
    echo [ZAGENT] H2 服务启动超时，请查看 ZAGENT-H2-DB 窗口中的日志。
    pause
    exit /b 1
)

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
