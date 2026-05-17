# wue

一个跨平台 Galgame 引擎，运行于 JVM (Java 17+)。

致敬 (O)NScripter；剧本用 Lua 编写。

---

## 运行

确保系统已安装 Java 17 或更高版本：

```sh
java -version
```

然后：

| 平台 | 命令 |
|------|------|
| macOS / Linux | `./run.sh` |
| Windows | 双击 `run.bat`，或终端运行 `run.bat` |

无需任何额外配置；`wue.jar` 内已含全平台 LWJGL 原生库。

---

## 控制

| 输入 | 行为 |
|------|------|
| 左键 / 空格 / 回车 | 推进对话；正在逐字时立即显示完整 |
| 按住 Ctrl | 快速跳过 |
| 鼠标点击选项 | 选择 |
| ESC | 退出 |

---

## 写自己的剧本

在本文件夹下创建：

```
assets/
└── scripts/
    └── main.lua
```

引擎启动时优先加载 `assets/scripts/main.lua`，找不到才回落到 jar 内的占位脚本。

### 最小例子

```lua
log("我的剧本启动")

show_bg("bg/classroom.png")    -- 资源放在 assets/bg/classroom.png
play_bgm("bgm/morning.ogg")    -- 仅支持 OGG Vorbis

say("旁白", "晨光透过窗户洒入教室。")
say("Alice", "早上好。")

local pick = choice({
  "回应她",
  "假装没看见",
})

if pick == 1 then
  vars.alice_route = true
  say("你", "早上好。")
else
  vars.alice_route = false
  say("你", "（继续看书。）")
end

mark("intro_end")
save(1)   -- 写入 saves/save_1.properties
```

### 完整 API

| API | 说明 |
|-----|------|
| `say(speaker, text)` / `say(text)` | 显示一行对话，等待推进 |
| `choice({...})` | 显示选项，返回 1-based 索引 |
| `wait(seconds)` | 等待 N 秒（保持渲染） |
| `mark(name)` | 标记当前进度（供存档记录） |
| `show_bg(path)` | 切换背景（缺失则程序化回退） |
| `play_bgm(path, loop)` | 播放 BGM（OGG，默认循环） |
| `stop_bgm()` | 停止 BGM |
| `play_se(path)` | 播放音效（自动缓存，4 通道复用） |
| `save(slot)` | 写入存档槽 |
| `has_save(slot)` | 检查槽是否存在 |
| `load_save(slot)` | 读档；返回 `mark` 字符串或 nil |
| `log(msg)` | stdout 调试输出 |
| `vars.<key> = ...` | 作者变量；会随存档持久化 |

### 资源约定

```
assets/
├── scripts/main.lua       本体剧本
├── bg/<name>.png          背景图（PNG / JPEG / BMP / TGA）
├── bgm/<name>.ogg         BGM（OGG Vorbis）
└── se/<name>.ogg          音效（OGG Vorbis）
```

存档写入 `./saves/save_<slot>.properties`，可手工编辑。

---

## 兼容性

- macOS 12+ (Intel & Apple Silicon)
- Windows 10+ (x86_64 / arm64)
- Linux glibc 2.31+ (x86_64 / arm64)

均只需 Java 17+，无其它系统级依赖。

> macOS 启动时可能在终端输出一行 `JRSAppKitAWT markAppIsDaemon` 警告，属 JVM 已知问题，不影响功能。
