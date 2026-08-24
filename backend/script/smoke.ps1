#requires -Version 7
# 接口冒烟测试：登录 -> 全量读接口 -> 各域写操作 -> 导出/上传 -> 刷新令牌/登出
# 用法：pwsh -NoProfile -File backend/script/smoke.ps1 [端口]
param([int]$Port = 48080)

$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
$origin = "http://127.0.0.1:$Port"
$base = "$origin/admin-api"
$script:tok = $null
$script:pass = 0
$script:fail = 0

function Ok($name) { $script:pass++; Write-Host "OK   $name" -ForegroundColor Green }
function Bad($name, $why) { $script:fail++; Write-Host "FAIL $name -> $why" -ForegroundColor Red }

# expect: 'ok' 期望业务成功；'biz-error' 期望 HTTP 200 + code != 200；'http-401' 期望 401
function Call($name, $method, $path, $body, $expect = 'ok') {
  $headers = @{ 'tenant-id' = '1' }
  if ($script:tok) { $headers['Authorization'] = "Bearer $script:tok" }
  try {
    $params = @{ Method = $method; Uri = "$base$path"; Headers = $headers; TimeoutSec = 30 }
    if ($null -ne $body) {
      $params['Body'] = ($body | ConvertTo-Json -Depth 6 -Compress)
      $params['ContentType'] = 'application/json; charset=utf-8'
    }
    $res = Invoke-RestMethod @params
    if ($res.code -eq 200) {
      if ($expect -eq 'ok') { Ok $name; return $res.data }
      Bad $name "本应失败，却返回成功"; return $null
    }
    if ($expect -eq 'biz-error') { Ok "$name（code=$($res.code) $($res.msg)）"; return $null }
    Bad $name "code=$($res.code) msg=$($res.msg)"; return $null
  } catch {
    $status = $_.Exception.Response.StatusCode.value__
    if ($expect -eq "http-$status") { Ok "$name（HTTP $status）"; return $null }
    Bad $name "HTTP $status $($_.Exception.Message)"; return $null
  }
}

Write-Host '=== 1. 鉴权 ===' -ForegroundColor Cyan
Call '未登录访问受保护接口' 'GET' '/system/user/page?pageNo=1&pageSize=10' $null 'http-401' | Out-Null
Call '错误密码登录' 'POST' '/system/auth/login' @{ tenantName = '默认租户'; username = 'admin'; password = 'wrong-pwd' } 'biz-error' | Out-Null
Call '不存在的租户' 'POST' '/system/auth/login' @{ tenantName = '压根没这租户'; username = 'admin'; password = 'admin123' } 'biz-error' | Out-Null
$login = Call '登录' 'POST' '/system/auth/login' @{ tenantName = '默认租户'; username = 'admin'; password = 'admin123' }
if (-not $login) { Write-Host '登录失败，终止' -ForegroundColor Red; exit 1 }
$script:tok = $login.accessToken
$refresh = $login.refreshToken
Write-Host "     userId=$($login.userId) tenantId=$($login.tenantId)"
$info = Call '权限信息' 'GET' '/system/auth/get-permission-info' $null
if ($info) { Write-Host "     roles=$($info.roles -join ',') 权限数=$($info.permissions.Count) 顶层菜单=$($info.menus.Count)" }

Write-Host '=== 2. 读接口 ===' -ForegroundColor Cyan
@(
  @('用户分页', '/system/user/page?pageNo=1&pageSize=10'),
  @('用户精简列表', '/system/user/simple-list'),
  @('用户详情', '/system/user/get?id=1'),
  @('部门树', '/system/dept/list'),
  @('部门精简列表', '/system/dept/simple-list'),
  @('岗位分页', '/system/post/page?pageNo=1&pageSize=10'),
  @('岗位精简列表', '/system/post/simple-list'),
  @('角色分页', '/system/role/page?pageNo=1&pageSize=10'),
  @('角色精简列表', '/system/role/simple-list'),
  @('菜单列表', '/system/menu/list'),
  @('菜单精简列表', '/system/menu/simple-list'),
  @('字典类型分页', '/system/dict-type/page?pageNo=1&pageSize=10'),
  @('字典类型精简列表', '/system/dict-type/simple-list'),
  @('字典数据分页', '/system/dict-data/page?pageNo=1&pageSize=10'),
  @('字典数据精简列表', '/system/dict-data/simple-list'),
  @('按类型取字典数据', '/system/dict-data/type?type=common_status'),
  @('公告分页', '/system/notice/page?pageNo=1&pageSize=10'),
  @('租户分页', '/system/tenant/page?pageNo=1&pageSize=10'),
  @('租户精简列表', '/system/tenant/simple-list'),
  @('登录日志分页', '/system/login-log/page?pageNo=1&pageSize=10'),
  @('操作日志分页', '/system/operate-log/page?pageNo=1&pageSize=10'),
  @('参数配置分页', '/infra/config/page?pageNo=1&pageSize=10'),
  @('按键名取参数值', '/infra/config/get-value-by-key?configKey=ui.footer.text'),
  @('文件分页', '/infra/file/page?pageNo=1&pageSize=10'),
  @('个人中心', '/system/user/profile/get'),
  @('角色的菜单', '/system/permission/list-role-menus?roleId=2'),
  @('用户的角色', '/system/permission/list-user-roles?userId=1')
) | ForEach-Object { Call $_[0] 'GET' $_[1] $null | Out-Null }
Call '不可见参数不外泄' 'GET' '/infra/config/get-value-by-key?configKey=sys.user.init-password' $null 'biz-error' | Out-Null

Write-Host '=== 3. 岗位全流程（含唯一性校验） ===' -ForegroundColor Cyan
$postId = Call '新建岗位' 'POST' '/system/post/create' @{ code = 'smoke'; name = '烟雾测试岗'; sort = 99; status = 0; remark = '自动化冒烟' }
if ($postId) {
  Call '修改岗位' 'PUT' '/system/post/update' @{ id = $postId; code = 'smoke'; name = '烟雾测试岗2'; sort = 98; status = 0 } | Out-Null
  Call '岗位详情' 'GET' "/system/post/get?id=$postId" $null | Out-Null
  Call '岗位编码重复' 'POST' '/system/post/create' @{ code = 'smoke'; name = '重复岗'; sort = 1; status = 0 } 'biz-error' | Out-Null
  Call '删除岗位' 'DELETE' "/system/post/delete?id=$postId" $null | Out-Null
}

Write-Host '=== 4. 各域写操作 ===' -ForegroundColor Cyan
$userId = Call '新建用户' 'POST' '/system/user/create' @{ username = 'smoke_user'; password = 'smoke123456'; nickname = '冒烟用户'; deptId = 101; status = 0; sex = 1; mobile = '13900000001'; email = 'smoke@example.com'; postIds = @(4) }
if ($userId) {
  Call '账号重复' 'POST' '/system/user/create' @{ username = 'smoke_user'; password = 'smoke123456'; nickname = '重复账号'; deptId = 101; status = 0 } 'biz-error' | Out-Null
  Call '修改用户' 'PUT' '/system/user/update' @{ id = $userId; username = 'smoke_user'; nickname = '冒烟用户2'; deptId = 102; status = 0; sex = 2 } | Out-Null
  Call '停用用户' 'PUT' '/system/user/update-status' @{ id = $userId; status = 1 } | Out-Null
  Call '重置密码' 'PUT' '/system/user/update-password' @{ id = $userId; password = 'newpwd123456' } | Out-Null
  Call '分配角色' 'POST' '/system/permission/assign-user-role' @{ userId = $userId; roleIds = @(2) } | Out-Null
  Call '删除用户' 'DELETE' "/system/user/delete?id=$userId" $null | Out-Null
}
$roleId = Call '新建角色' 'POST' '/system/role/create' @{ name = '冒烟角色'; code = 'smoke_role'; sort = 9; status = 0; remark = '自动化冒烟' }
if ($roleId) {
  Call '分配菜单' 'POST' '/system/permission/assign-role-menu' @{ roleId = $roleId; menuIds = @(1, 100, 1001) } | Out-Null
  $menus = Call '查询角色菜单' 'GET' "/system/permission/list-role-menus?roleId=$roleId" $null
  if ($menus -and $menus.Count -ne 3) { Bad '角色菜单条数' "期望 3，实际 $($menus.Count)" }
  Call '删除内置角色' 'DELETE' '/system/role/delete?id=1' $null 'biz-error' | Out-Null
  Call '删除角色' 'DELETE' "/system/role/delete?id=$roleId" $null | Out-Null
}
$dictTypeId = Call '新建字典类型' 'POST' '/system/dict-type/create' @{ name = '冒烟字典'; type = 'smoke_dict'; status = 0 }
if ($dictTypeId) {
  $dictDataId = Call '新建字典数据' 'POST' '/system/dict-data/create' @{ label = '选项一'; value = '1'; dictType = 'smoke_dict'; sort = 1; status = 0 }
  if ($dictDataId) { Call '删除字典数据' 'DELETE' "/system/dict-data/delete?id=$dictDataId" $null | Out-Null }
  Call '删除字典类型' 'DELETE' "/system/dict-type/delete?id=$dictTypeId" $null | Out-Null
}
$noticeId = Call '新建公告' 'POST' '/system/notice/create' @{ title = '冒烟公告'; content = '<p>hello</p>'; type = 1; status = 0 }
if ($noticeId) { Call '删除公告' 'DELETE' "/system/notice/delete?id=$noticeId" $null | Out-Null }
$configId = Call '新建参数' 'POST' '/infra/config/create' @{ category = 'smoke'; name = '冒烟参数'; configKey = 'smoke.key'; configValue = 'v1'; type = 2; visible = $true; remark = '' }
if ($configId) {
  Call '修改参数' 'PUT' '/infra/config/update' @{ id = $configId; category = 'smoke'; name = '冒烟参数'; configKey = 'smoke.key'; configValue = 'v2'; type = 2; visible = $true } | Out-Null
  $v = Call '读回参数值' 'GET' '/infra/config/get-value-by-key?configKey=smoke.key' $null
  if ($v -ne 'v2') { Bad '参数值不一致' "期望 v2，实际 $v" }
  Call '删除内置参数' 'DELETE' '/infra/config/delete?id=1' $null 'biz-error' | Out-Null
  Call '删除参数' 'DELETE' "/infra/config/delete?id=$configId" $null | Out-Null
}
$deptId = Call '新建部门' 'POST' '/system/dept/create' @{ name = '冒烟部门'; parentId = 100; sort = 9; status = 0 }
if ($deptId) { Call '删除部门' 'DELETE' "/system/dept/delete?id=$deptId" $null | Out-Null }
Call '删除有下级的部门' 'DELETE' '/system/dept/delete?id=100' $null 'biz-error' | Out-Null
$menuId = Call '新建菜单' 'POST' '/system/menu/create' @{ name = '冒烟菜单'; permission = ''; type = 2; sort = 99; parentId = 1; path = 'smoke'; component = 'smoke/index'; status = 0; visible = $true; keepAlive = $true; alwaysShow = $true }
if ($menuId) { Call '删除菜单' 'DELETE' "/system/menu/delete?id=$menuId" $null | Out-Null }
$tenantId = Call '新建租户' 'POST' '/system/tenant/create' @{ name = '冒烟租户'; contactName = '张三'; contactMobile = '13900000002'; status = 0; accountCount = 10; username = 'smoke_admin'; password = 'smoke123456' }
if ($tenantId) {
  Call '租户详情' 'GET' "/system/tenant/get?id=$tenantId" $null | Out-Null
  Call '删除租户' 'DELETE' "/system/tenant/delete?id=$tenantId" $null | Out-Null
}
Call '修改个人资料' 'PUT' '/system/user/profile/update' @{ nickname = '管理员'; email = 'admin@basepro.com'; mobile = '15888888888'; sex = 1 } | Out-Null

Write-Host '=== 5. 导出 / 上传 ===' -ForegroundColor Cyan
$headers = @{ Authorization = "Bearer $script:tok"; 'tenant-id' = '1' }
@(
  '/system/user/export-excel?pageNo=1&pageSize=10',
  '/system/post/export-excel?pageNo=1&pageSize=10',
  '/system/role/export-excel?pageNo=1&pageSize=10',
  '/system/dict-type/export-excel?pageNo=1&pageSize=10',
  '/system/dict-data/export-excel?pageNo=1&pageSize=10',
  '/system/login-log/export-excel?pageNo=1&pageSize=10',
  '/system/operate-log/export-excel?pageNo=1&pageSize=10',
  '/infra/config/export-excel?pageNo=1&pageSize=10',
  '/system/tenant/export-excel?pageNo=1&pageSize=10',
  '/system/user/get-import-template'
) | ForEach-Object {
  try {
    $r = Invoke-WebRequest -Uri "$base$_" -Headers $headers -TimeoutSec 30
    $ct = "$($r.Headers['Content-Type'])"
    if ($r.RawContentLength -gt 0 -and $ct.Contains('spreadsheetml')) { Ok "导出 $_ （$($r.RawContentLength) 字节）" }
    else { Bad "导出 $_" "content-type=$ct 长度=$($r.RawContentLength)" }
  } catch { Bad "导出 $_" $_.Exception.Message }
}
$tmp = Join-Path $env:TEMP 'smoke-upload.txt'
Set-Content -Path $tmp -Value 'basepro smoke upload' -NoNewline
try {
  $r = Invoke-RestMethod -Uri "$base/infra/file/upload" -Method Post -Headers $headers -Form @{ file = Get-Item $tmp } -TimeoutSec 30
  if ($r.code -eq 200 -and $r.data) {
    Ok "文件上传（$($r.data)）"
    $v = Invoke-WebRequest -Uri ($origin + $r.data) -TimeoutSec 30
    if ($v.Content -like '*smoke upload*') { Ok '文件读取（免登录）' } else { Bad '文件读取' $v.Content }
  } else { Bad '文件上传' ($r | ConvertTo-Json -Compress) }
} catch { Bad '文件上传' $_.Exception.Message }

Write-Host '=== 6. 令牌刷新 / 登出 ===' -ForegroundColor Cyan
try {
  $r = Invoke-RestMethod -Uri "$base/system/auth/refresh-token?refreshToken=$refresh" -Method Post -Headers @{ 'tenant-id' = '1' } -TimeoutSec 30
  if ($r.code -eq 200 -and $r.data.accessToken) { Ok '刷新令牌'; $script:tok = $r.data.accessToken }
  else { Bad '刷新令牌' ($r | ConvertTo-Json -Compress) }
} catch { Bad '刷新令牌' $_.Exception.Message }
Call '新令牌可用' 'GET' '/system/auth/get-permission-info' $null | Out-Null
Call '登出' 'POST' '/system/auth/logout' $null | Out-Null
# 访问令牌是无状态 JWT，登出只吊销刷新令牌，旧访问令牌到期前仍可用（预期行为）；
# 用被吊销的刷新令牌换新令牌应当拿到 401
try {
  $r = Invoke-RestMethod -Uri "$base/system/auth/refresh-token?refreshToken=$refresh" -Method Post -Headers @{ 'tenant-id' = '1' } -TimeoutSec 30
  Bad '登出后刷新令牌' "本应失效，却返回 code=$($r.code)"
} catch {
  $status = $_.Exception.Response.StatusCode.value__
  if ($status -eq 401) { Ok '登出后刷新令牌失效（HTTP 401）' } else { Bad '登出后刷新令牌' "HTTP $status" }
}

Write-Host ''
$color = 'Green'
if ($script:fail -gt 0) { $color = 'Yellow' }
Write-Host "通过 $script:pass 项，失败 $script:fail 项" -ForegroundColor $color
