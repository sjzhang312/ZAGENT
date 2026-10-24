@echo off
chcp 65001 >nul
setlocal
echo [ZAGENT] 正在停止 H2 服务（TCP 9101 / 控制台 8082）...
set "FOUND="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":9101 " ^| findstr "LISTENING"') do (
    set "FOUND=1"
    echo [ZAGENT] 结束进程 PID %%p
    taskkill /f /pid %%p >nul 2>&1
)
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082 " ^| findstr "LISTENING"') do taskkill /f /pid %%p >nul 2>&1
if not defined FOUND (
    echo [ZAGENT] H2 服务未在运行。
) else (
    echo [ZAGENT] H2 服务已停止。
)
pause
