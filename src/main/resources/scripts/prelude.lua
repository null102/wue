-- wue prelude — author-facing scenario API on top of engine.* native bindings.
-- Loaded automatically before the main scenario script.

vars = {}  -- author-visible variable scope (persisted by save/load_save).

local _e = engine

function say(speaker, text)
  if text == nil then
    _e.set_dialog("", tostring(speaker))
  else
    _e.set_dialog(tostring(speaker), tostring(text))
  end
  coroutine.yield("click")
end

function choice(options)
  local strs = {}
  for i, v in ipairs(options) do
    if type(v) == "table" then
      strs[i] = tostring(v.text or v[1] or "?")
    else
      strs[i] = tostring(v)
    end
  end
  _e.show_choice(strs)
  return coroutine.yield("choice")
end

function wait(seconds)
  coroutine.yield("wait", tonumber(seconds) or 0)
end

function show_bg(path)         _e.show_bg(path) end
function play_bgm(path, loop)
  if loop == nil then loop = true end
  _e.play_bgm(path, loop)
end
function stop_bgm()            _e.stop_bgm() end
function play_se(path)         _e.play_se(path) end
function log(msg)              _e.log(tostring(msg)) end
function mark(name)            _e.mark(tostring(name)) end

function save(slot)            _e.save(slot or 1) end
function has_save(slot)        return _e.has_save(slot or 1) end
function load_save(slot)       return _e.load_save(slot or 1) end
