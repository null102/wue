# wue

一个致敬 (O)NScripter 的跨平台 Galgame（视觉小说）引擎，基于 **Java 17 LTS**。

仅在精神层面致敬：不兼容 NScripter 的脚本格式，剧本一律用 Lua 编写。

---

## 设计原则

| 原则 | 说明 |
|------|------|
| 零配置可运行 | 终端用户只需 JRE 17+，无需安装系统级原生库 |
| 平台无关 | 同一份 fat jar 跑 Windows / macOS (Intel & Apple Silicon) / Linux x64 & arm64 |
| 致敬而非复刻 | 保留"用脚本驱动一切"的极简精神，技术栈全面现代化重写 |
| 稳定优先 | 所有依赖须为成熟、长期维护、广泛部署的方案 |

---

## 技术栈

- **JDK**: Java 17 LTS（用 `--release 17` 编译，本机用 JDK 17+ 任意版本均可）
- **图形/窗口/音频**: [LWJGL 3](https://www.lwjgl.org)（GLFW + OpenGL 3.3 Core + OpenAL Soft + stb_image + stb_vorbis）
- **脚本**: [LuaJ 3.0.1](https://sourceforge.net/projects/luaj/)（纯 Java 实现，无原生依赖）
- **构建**: Gradle 8.10.2 (Kotlin DSL) + [Shadow](https://github.com/GradleUp/shadow) 8.3.10 fat-jar 打包

---

## 构建

```sh
./gradlew run          # 开发态启动
./gradlew distWue      # 生成可发行的运行时目录 ./wue/
./gradlew shadowJar    # 仅产出 build/libs/wue.jar（13 MB，含全平台 natives）
```

首次构建会下载 Gradle 8.10.2 + 全部依赖（~250 MB），之后全部缓存。

### 发行目录结构（`./wue/`）

```
wue/
├── wue.jar          单一 fat jar，13 MB，含 7 个平台的 LWJGL natives
├── run.sh           POSIX 启动器（macOS 自动加 -XstartOnFirstThread）
├── run.bat          Windows 启动器
└── README.md        终端用户向导（控制 / 写剧本 / 资源目录约定）
```

终端用户解压后只需 Java 17+ 在 PATH 上即可。剧本作者把自己的剧本放到 `./wue/assets/scripts/main.lua` 即可覆盖内置占位脚本。

---

## 控制

| 输入 | 行为 |
|------|------|
| 左键 / 空格 / 回车 | 推进对话；正在逐字时立即显示完整 |
| 按住 Ctrl | 快速跳过 |
| 鼠标点击选项 | 选择 |
| ESC | 退出 |

---

## 剧本作者 API

剧本以 Lua 编写。引擎在 `engine` 命名空间下暴露底层 binding，Lua prelude 把它们包装成 `say` / `choice` / `wait` 等顺序式 API。

### 流程控制

```lua
say("说话人", "对话内容。")
say("仅一句独白时可以省略说话人。")

wait(1.5)                    -- 秒；保持渲染循环

local pick = choice({
  "选项 A",
  "选项 B",
  { text = "也可附加字段", goto_label = "branch_c" },
})
if pick == 1 then ... end
```

### 资源 / 音频

```lua
show_bg("bg/<name>.png")          -- PNG / JPEG / BMP / TGA
play_bgm("bgm/<name>.ogg", true)  -- OGG Vorbis
stop_bgm()
play_se("se/<name>.ogg")          -- 自动缓存解码，4 通道复用
```

### 变量与存档

```lua
vars.<key> = value          -- vars 表的内容会被存档系统持久化

mark("chapter_label")       -- 命名当前进度
save(1)                     -- 写入 saves/save_1.properties
if has_save(1) then
  local last_mark = load_save(1)
end
```

### 资产路径解析

1. 文件系统 `./assets/<path>`（作者可实时编辑无须重打包）
2. classpath `<path>`（jar 内置默认资源）

### 全部 API

| API | 说明 |
|-----|------|
| `say(speaker, text)` / `say(text)` | 显示一行对话，等待推进 |
| `choice({...})` | 显示选项，返回 1-based 索引 |
| `wait(seconds)` | 等待 N 秒 |
| `mark(name)` | 命名进度位置（供存档记录） |
| `show_bg(path)` | 切换背景（缺失则程序化回退） |
| `play_bgm(path, loop)` | 播放 BGM |
| `stop_bgm()` | 停止 BGM |
| `play_se(path)` | 播放音效 |
| `save(slot)` | 写入存档槽 |
| `has_save(slot)` | 检查槽是否存在 |
| `load_save(slot)` | 读档；返回 mark 或 nil |
| `log(msg)` | stdout 调试输出 |

---

## 源码布局

```
src/main/java/wue/
├── App.java                   入口 + 主循环
├── core/
│   ├── Window.java            GLFW + OpenGL 3.3 Core 上下文
│   ├── Input.java             键鼠事件队列
│   ├── Shader.java            着色器编译/链接
│   ├── Texture.java           stb_image 解码 + 程序化 fallback
│   ├── SpriteRenderer.java    屏幕像素坐标 2D 渲染器
│   ├── TextRenderer.java      AWT 离屏光栅化 → GL 纹理（CJK 走系统字体）
│   ├── AssetLoader.java       fs → classpath 二级查找
│   └── AudioSystem.java       OpenAL + stb_vorbis；BGM + 4 路 SE pool
├── scene/
│   ├── Stage.java             背景 + 文字框 + 选项 容器
│   ├── TextWindow.java        逐字显示文字框
│   └── ChoiceWidget.java      鼠标选择菜单
├── script/
│   └── ScenarioRunner.java    LuaJ 协程驱动 + 命令队列
└── save/
    └── SaveSystem.java        变量与 mark 的 Properties 持久化

src/main/resources/
├── shaders/sprite.{vert,frag}
└── scripts/{prelude,main}.lua
```

---

## 设计要点

- **零外部字体依赖**：文字渲染通过 AWT 的 `Font.SANS_SERIF` 逻辑字体走系统字体回退链，自动支持中日韩字符。无须打包字体文件。
- **LuaJ 协程是真线程**：LuaJ 把 Lua 协程实现为真实 OS 线程，因此协程内的 binding **不能**直接调 OpenGL / OpenAL。`ScenarioRunner` 把这类 binding 包装成 `Runnable` 入队，主线程在 `LuaThread.resume()` 返回后排空队列再处理状态机。所有 GL/AL 调用始终在主线程。
- **单一 fat jar 全平台**：LWJGL 的 7 个平台 natives 全部打包，运行时 `Platform.get()` 自动选择，一份 13 MB 的 jar 跑全平台。
- **二级资产解析**：`./assets/<path>` 优先于 classpath，作者可在发行目录就地修改剧本/图片/音乐而无须重打包。

---

## 已知限制

- macOS 启动时会输出一行 `JRSAppKitAWT markAppIsDaemon` 警告 —— 这是 `-XstartOnFirstThread` 与 AWT headless 模式的固有冲突，**功能上无影响**，AWT 离屏渲染照常工作。
- 存档目前只持久化 `vars` 表与 `mark()`，**不支持** Lua 协程级状态快照。作者需把可存档边界设计为 `mark()` 之间的章节，加载后从最近 mark 起步。
- 仅支持 **OGG Vorbis** 音频；WAV / MP3 暂未实现。

---

## 路线图

- 文本引擎：Auto 模式、Backlog UI、Ruby（注音）
- 设置菜单：音量 / 文字速度 / 全屏切换 / 跳过模式
- 可视化剧本编辑器（Compose Desktop 或 JavaFX）
- Web 后端（TeaVM 编译至 WASM，浏览器内运行）
- i18n 工作流：剧本与翻译文件分离

---

## 致谢

- **(O)NScripter** —— 设计哲学的源头
- **LWJGL 3** —— JVM 上最稳定的低层图形/音频/输入抽象
- **LuaJ** —— 纯 Java 的 Lua 解释器
- **stb_image / stb_vorbis** —— Sean Barrett 的单文件解码器
