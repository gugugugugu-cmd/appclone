#!/usr/bin/env python3
"""Chinese UI strings for the App Clone fork.

Applied over the upstream sources by tools/translate.py. Keys are the exact Java
string literals; anything absent stays English, so log tags, class names and
shell commands are never touched.
"""

# Grouped only for readability; the table is flat.
T = {
    # ===== navigation and headings =====
    '"Profiles"': '"分身"',
    '"Profile"': '"分身"',
    '"Apps"': '"应用"',
    '"Settings"': '"设置"',
    '"About"': '"关于"',
    '"Back"': '"返回"',
    '"App details"': '"应用详情"',
    '"Clone app"': '"分身应用"',
    '"App Clone"': '"App Clone"',

    # ===== main screen =====
    '"No profiles yet"': '"还没有分身"',
    '"Create a profile to keep another set of app data."': '"新建一个分身，用它保留另一套应用数据。"',
    '"Add a clone app to get started. System apps are available from the menu."':
        '"先添加一个分身应用，系统应用可以从菜单里选。"',
    '"Add an app or show system apps from the menu."': '"从菜单里添加应用，或显示系统应用。"',
    '"No apps to show"': '"没有可显示的应用"',
    '"No cloned apps yet"': '"还没有分身应用"',
    '"No clones for this app"': '"这个应用还没有分身"',
    '"No clones to show"': '"没有可显示的分身"',
    '"No matches"': '"没有匹配结果"',
    '"Return to Apps to add a clone."': '"返回「应用」去添加分身。"',
    '"Try another name or user ID, or clear the search."': '"换个名称或用户 ID，或清空搜索。"',
    '"Add app"': '"添加应用"',
    '"Add app to profile"': '"把应用加到分身"',
    '"Add another clone"': '"再加一个分身"',
    '"Add clone app"': '"添加分身应用"',
    '"View apps"': '"查看应用"',
    '"View apps in "': '"查看应用："',
    '"View profile"': '"查看分身"',
    '"Profile actions"': '"分身操作"',
    '"Actions for "': '"操作："',
    '"App actions"': '"应用操作"',
    '"New profile"': '"新建分身"',
    '"Pin shortcut"': '"固定快捷方式"',
    '"Export diagnostics"': '"导出诊断信息"',
    '"Delete all profiles"': '"删除全部分身"',
    '"Delete all profiles?"': '"删除全部分身？"',
    '"Delete all"': '"全部删除"',
    '"Deletes all "': '"将删除全部 "',
    '"profiles created."': '"个分身已创建。"',

    # ===== search =====
    '"Search apps or packages"': '"搜索应用或包名"',
    '"Search apps or profiles"': '"搜索应用或分身"',
    '"Search profiles or user IDs"': '"搜索分身或用户 ID"',

    # ===== profile state =====
    '"Running"': '"运行中"',
    '"Stopped"': '"已停止"',
    '"Profile running"': '"分身运行中"',
    '"Profile stopped"': '"分身已停止"',
    '"Profile unavailable"': '"分身不可用"',
    '"Start"': '"启动"',
    '"Stop"': '"停止"',
    '"Start profile"': '"启动分身"',
    '"Stop profile"': '"停止分身"',
    '"Stop profile?"': '"停止分身？"',
    '"Start / stop profile"': '"启动 / 停止分身"',
    '"Starting profile…"': '"正在启动分身…"',
    '"Stopping profile…"': '"正在停止分身…"',
    '"Opening an app starts it again. Reboot behavior is controlled in Settings."':
        '"打开应用就会重新启动。重启后的行为可在设置里控制。"',
    '"Unlock the main user before starting a clone."': '"请先解锁主用户，再启动分身。"',
    '"Profile is no longer unlocked. Refresh before retrying."': '"分身已不再处于解锁状态，请刷新后重试。"',
    '"Profile no longer exists."': '"分身已不存在。"',
    '"Profile removed or replaced. Return to Profiles and refresh."':
        '"分身已被删除或替换，请返回「分身」页刷新。"',
    '"Profile still exists. Refresh before retrying."': '"分身仍然存在，请刷新后重试。"',
    '"Profile identity changed. Refresh before retrying."': '"分身身份已变化，请刷新后重试。"',
    '"Stopped at "': '"停止于 "',
    '"Disabled · "': '"已停用 · "',

    # ===== app actions =====
    '"Add clone?"': '"添加分身？"',
    '"Add "': '"添加 "',
    '"Include system apps"': '"包含系统应用"',
    '"Including system apps"': '"包含系统应用"',
    '"Show system apps"': '"显示系统应用"',
    '"System apps hidden"': '"已隐藏系统应用"',
    '"Disable app"': '"停用应用"',
    '"Disable app (freeze)"': '"停用应用（冻结）"',
    '"Enable app"': '"启用应用"',
    '"Force stop app"': '"强制停止应用"',
    '"Remove clone app"': '"移除分身应用"',
    '"Remove from this profile"': '"从这个分身移除"',
    '"Use manifest default"': '"使用清单默认状态"',
    '"Use manifest default state"': '"使用清单默认状态"',
    '"App is disabled. Enable it explicitly before opening."': '"应用已停用，请先手动启用再打开。"',
    '"App is not installed in this profile."': '"该应用没有安装在这个分身里。"',
    '"App is no longer in this profile."': '"该应用已不在这个分身里。"',
    '"Already in this profile."': '"该应用已在此分身中。"',
    '"No enabled launcher activity in this profile."': '"这个分身里该应用没有可用的启动入口。"',
    '"App icon unavailable; refresh and retry"': '"应用图标不可用，请刷新后重试"',
    '"App changed while opening. Refresh before retrying."': '"打开时应用发生了变化，请刷新后重试。"',
    '"App identity changed. Refresh before retrying."': '"应用身份已变化，请刷新后重试。"',
    '"App enabled state did not change."': '"应用启用状态没有变化。"',
    '"App installation state did not change.\\n"': '"应用安装状态没有变化。\\n"',
    '"Stops this app now without disabling it or deleting data. Notifications may stop until it is opened again. This is not a permanent freeze."':
        '"立即停止该应用，不停用也不删数据。通知会暂停直到再次打开它。这不是永久冻结。"',
    '"Stops this app and its notifications until you explicitly enable it. Data is kept. Other apps in this profile keep running."':
        '"停止该应用及其通知，直到你手动启用。数据保留，分身里的其他应用继续运行。"',
    '"Explicitly enables this app. This does not automatically start a stopped profile."':
        '"手动启用该应用。不会自动启动已停止的分身。"',
    '"Removes the enabled/disabled override and uses the app manifest\u0027s default. It does not reconstruct a previous override."':
        '"清除启用/停用的覆盖设置，回到应用清单的默认值。不会恢复之前某个覆盖值。"',
    '"Deletes this app\u0027s data in this profile. The profile, its other apps and the main user\u0027s copy are kept."':
        '"删除该应用在这个分身里的数据。分身、其他应用和主用户的副本都保留。"',

    # ===== background controls =====
    '"Background activity"': '"后台活动"',
    '"Allowed (Android-managed)"': '"允许（由系统管理）"',
    '"Restricted"': '"受限"',
    '"Restrict"': '"限制"',
    '"Restore previous"': '"恢复之前设置"',
    '"Forget saved setting"': '"忘记已保存的设置"',
    '"Keep current"': '"保持当前"',
    '"Forget"': '"忘记"',
    '"Restricting background activity…"': '"正在限制后台活动…"',
    '"Restoring background setting…"': '"正在恢复后台设置…"',
    '"Forgetting saved setting…"': '"正在清除已保存设置…"',
    '"Reading background setting…"': '"正在读取后台设置…"',
    '"It does not stop this profile, disable the app or freeze its processes. No setting changes until you choose an action."':
        '"这不会停止分身、停用应用或冻结其进程。在你选择一个操作前不会改动任何设置。"',
    '"Already restricted; nothing changed."': '"已受限，没有改动。"',
    '"Background restricted; previous setting saved"': '"后台已受限，之前的设置已保存"',
    '"Previous background setting restored"': '"已恢复之前的后台设置"',
    '"Background setting belongs to another profile."': '"后台设置属于另一个分身。"',
    '"Background setting changed. Reopen its controls."': '"后台设置已改变，请重新打开控制面板。"',
    '"Could not read background setting."': '"无法读取后台设置。"',
    '"Could not read the exact background setting."': '"无法读取准确的后台设置。"',
    '"Could not verify background setting. The saved previous mode is retained."':
        '"无法确认后台设置，已保存的之前模式仍保留。"',
    '"Could not verify the setting. Previous mode is still saved."': '"无法确认该设置，之前的模式仍已保存。"',
    '"Could not save the background restore record. No further changes applied."':
        '"无法保存后台恢复记录，后续改动均未执行。"',
    '"Restore or forget the saved setting first."': '"请先恢复或清除已保存的设置。"',
    '"Saved setting changed. Reopen background controls."': '"已保存的设置发生了变化，请重新打开后台控制。"',
    '"Forget previous setting?"': '"忘记之前的设置？"',
    '"No previous setting is saved."': '"没有保存过之前的设置。"',
    '"Shared/system UID apps cannot be restricted independently."': '"共享或系统 UID 的应用无法单独限制。"',
    '"Unknown background mode."': '"未知的后台模式。"',
    '"Unsupported background setting."': '"不支持的后台设置。"',
    '"The app or its setting changed elsewhere. Nothing restored; you can forget the saved record without changing Android."':
        '"应用或其设置已在别处改变。未做任何恢复；你可以直接清除该记录而不改动系统。"',
    '"Foreground only"': '"仅前台"',

    # ===== settings =====
    '"Shortcut labels"': '"快捷方式名称"',
    '"Shortcut label format"': '"快捷方式命名格式"',
    '"Prefix clone app names"': '"在应用名前加分身编号"',
    '"Put the clone id before the app name, so cloned shortcuts are distinguishable."':
        '"在应用名前面加上分身编号，方便区分各个分身的快捷方式。"',
    '"Format"': '"格式"',
    '"Tokens: {id} clone user id \u00b7 {name} app name \u00b7 {clone} clone name. "':
        '"可用变量：{id} 分身编号 · {name} 应用名 · {clone} 分身名称。"',
    '"Colons and other characters are allowed."': '"冒号和其他字符都可以直接使用。"',
    '"Automation"': '"自动化"',
    '"Auto-pin new clone apps"': '"自动固定新装的分身应用"',
    '"Request a home-screen shortcut as each app is added to a clone. "':
        '"每往分身里加一个应用就请求一个桌面快捷方式。"',
    '"The launcher still asks you to confirm."': '"仍需在桌面上确认一次。"',
    '"Labels apply to shortcuts created from now on. Re-pin an existing shortcut "':
        '"名称只对之后创建的快捷方式生效，已有的需要重新固定一次。"',
    '"Existing shortcut labels stay unchanged; re-pin them if you want new labels."':
        '"已有快捷方式的名称不变，想要新名称请重新固定。"',
    '"Material You · Wallpaper colours and system light/dark mode"':
        '"Material You · 跟随壁纸取色与系统深浅色"',

    # ===== reboot policy =====
    '"After reboot"': '"重启后"',
    '"System default"': '"系统默认"',
    '"System default leaves profiles alone. Stop all preserves data but pauses notifications. "':
        '"系统默认不干预分身。全部停止会保留数据但暂停通知。"',
    '"Root must already be granted and available at boot. Start and shortcuts still work afterward."':
        '"开机时 Root 必须已授权可用，之后启动和快捷方式照常能用。"',
    '"Reboot policy"': '"重启策略"',
    '"Reboot action failed"': '"重启操作失败"',
    '"Could not save reboot setting"': '"无法保存重启设置"',
    '"Could not save reboot error"': '"无法保存重启错误"',
    '"Could not schedule reboot action"': '"无法调度重启操作"',
    '"Reboot setting could not be saved. Select it again to retry."':
        '"重启设置保存失败，请重新选择。"',

    # ===== create / rename / delete =====
    '"Profile name"': '"分身名称"',
    '"How many profiles (1–500)"': '"分身数量（1–500）"',
    '"Append -1, -2, … to names"': '"在名称后追加 -1、-2 …"',
    '"Create"': '"创建"',
    '"Creating profiles…"': '"正在创建分身…"',
    '"Creates and starts the requested profiles. Names are kept exactly as entered unless numbering is selected."':
        '"创建并启动指定数量的分身。名称按输入原样保留，除非勾选了自动编号。"',
    '"Choose between 1 and 500 profiles."': '"分身数量请在 1 到 500 之间。"',
    '"Rename"': '"重命名"',
    '"Rename profile"': '"重命名分身"',
    '"Renaming profile…"': '"正在重命名…"',
    '"Profile renamed."': '"分身已重命名。"',
    '"Could not rename profile: "': '"无法重命名分身："',
    '"Could not verify the new name. Refresh before retrying."': '"无法确认新名称，请刷新后重试。"',
    '"Enter a nonempty single-line name without NUL characters."': '"请输入非空的单行名称，不能包含 NUL 字符。"',
    '"Use at most 100 characters (emoji may count as two)."': '"最多 100 个字符（表情可能按两个计算）。"',
    '"Existing name suffix is too large."': '"已有名称的编号过大。"',
    '"Name suffix would overflow."': '"名称编号会溢出。"',
    '"Delete"': '"删除"',
    '"Delete profile"': '"删除分身"',
    '"Delete profile?"': '"删除分身？"',
    '"Deleting profile…"': '"正在删除分身…"',
    '"Deleting profiles…"': '"正在删除分身…"',
    '"Type DELETE exactly"': '"请精确输入 DELETE"',
    '"Type DELETE to confirm"': '"输入 DELETE 以确认"',

    # ===== dialogs and generic =====
    '"Cancel"': '"取消"',
    '"Close"': '"关闭"',
    '"Done"': '"完成"',
    '"Open"': '"打开"',
    '"Refresh"': '"刷新"',
    '"Remove"': '"移除"',
    '"Retry"': '"重试"',
    '"Save"': '"保存"',
    '"None"': '"无"',
    '"Success"': '"成功"',
    '"Denied"': '"已拒绝"',
    '"Preview: "': '"预览："',
    '"Insert "': '"插入 "',
    '"Open "': '"打开 "',
    '"Package "': '"包名 "',
    '"User "': '"用户 "',
    '"App: "': '"应用："',
    '"Profile "': '"分身 "',

    # ===== reading / progress =====
    '"Refreshing… Showing previous list."': '"正在刷新… 先显示之前的列表。"',
    '"Refresh failed. Previous list kept."': '"刷新失败，保留之前的列表。"',
    '"Refresh failed; showing previous list. Tap for details."': '"刷新失败，显示之前的列表。点按查看详情。"',
    '"Reading available apps…"': '"正在读取可用应用…"',
    '"Reading clone apps…"': '"正在读取分身应用…"',
    '"Opening clone app…"': '"正在打开分身应用…"',
    '"Collecting diagnostics…"': '"正在收集诊断信息…"',
    '"Diagnostics saved"': '"诊断信息已保存"',
    '"Could not save diagnostics"': '"诊断信息保存失败"',
    '"Report no longer available"': '"报告已不可用"',
    '"The app was restarted. Export diagnostics again to collect a fresh report."':
        '"应用已重启，请重新导出诊断信息以获取最新报告。"',
    '"Review this file for personal data before attaching it.\\n"': '"附加到 issue 前请先检查其中是否含个人数据。\\n"',

    # ===== errors =====
    '"Root is required."': '"需要 Root 权限。"',
    '"Root needed: allow App Clone in your root manager"': '"需要 Root：请在 Root 管理器里允许 App Clone"',
    '"Root command failed."': '"Root 命令执行失败。"',
    '"Root 命令超时，请刷新分身状态后重试。"': '"Root 命令超时，请刷新分身状态后重试。"',
    '"Root 命令被中断，请刷新分身状态后重试。"': '"Root 命令被中断，请刷新分身状态后重试。"',
    '"Cannot start the root shell. Check that root is active and App Clone is allowed in your root manager, "':
        '"无法启动 root shell。请确认 Root 已生效、且 Root 管理器里允许了 App Clone，"',
    '"Root access (su from PATH; /system/bin/su only if not found)"':
        '"Root 访问（优先 PATH 里的 su，找不到才用 /system/bin/su）"',
    '"Could not refresh"': '"无法刷新"',
    '"Refresh the profile list first."': '"请先刷新分身列表。"',
    '"Return to Profiles and refresh before taking any action."': '"请返回「分身」页刷新后再操作。"',
    '"Could not read apps for user "': '"无法读取该用户的应用："',
    '"Could not read clone apps. Tap for details."': '"无法读取分身应用，点按查看详情。"',
    '"Could not start clone: "': '"无法启动分身："',
    '"Could not verify app state.\\n"': '"无法确认应用状态。\\n"',
    '"Could not verify force-stop. Refresh before retrying."': '"无法确认强制停止结果，请刷新后重试。"',
    '"Could not read the owner profile. Refresh and retry.\\n"': '"无法读取归属分身，请刷新后重试。\\n"',
    '"Cannot identify profile "': '"无法识别分身："',
    '"Cannot read metadata for "': '"无法读取元数据："',
    '"Missing Android service: "': '"缺少 Android 服务："',
    '"No output stream"': '"无法创建输出流"',
    '"Invalid app action."': '"无效的应用操作。"',
    '"Invalid app command."': '"无效的应用命令。"',
    '"Invalid app shortcut."': '"无效的应用快捷方式。"',
    '"Invalid clone app identity."': '"无效的分身应用标识。"',
    '"Invalid clone identity."': '"无效的分身标识。"',
    '"Invalid clone profile."': '"无效的分身。"',
    '"Expected action, user, serial and package."': '"需要操作、用户、序号和包名。"',
    '"Expected one clone user ID."': '"需要且仅需要一个分身用户 ID。"',
    '"Expected user, serial and name."': '"需要用户、序号和名称。"',
    '"Clone commands are unavailable."': '"分身命令不可用。"',
    '"Clone app command failed: "': '"分身应用命令失败："',
    '"Clone app command timed out. Refresh before retrying."': '"分身应用命令超时，请刷新后重试。"',
    '"Not a clone profile: "': '"不是分身："',
    '"Not a non-ephemeral clone of user 0: "': '"不是主用户的常驻分身："',
    '"Only clone profiles belonging to user 0 can be started or stopped."':
        '"只能启动或停止属于主用户（用户 0）的分身。"',
    '"Cannot control an ephemeral clone: stopping it can delete its data."':
        '"无法控制临时分身：停止它可能删除其数据。"',
    '"Android could not start profile "': '"系统无法启动分身："',
    '"This launcher cannot pin shortcuts"': '"当前桌面不支持固定快捷方式"',
    '"Source code \u00b7 github.com/doccstat/appclone"': '"源码 · github.com/gugugugugu-cmd/appclone"',

    # ===== diagnostics sheet titles =====
    '"App Clone diagnostics\\n"': '"App Clone 诊断信息\\n"',
    '"Last create error"': '"最近一次创建错误"',
    '"Device: "': '"设备："',
    '"Android: "': '"Android："',
    '"Build: "': '"构建："',
    '"Fingerprint: "': '"指纹："',
    '"Generated: "': '"生成时间："',
    '"App PATH: "': '"应用 PATH："',
    '"App UID: "': '"应用 UID："',
}
