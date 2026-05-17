#version 330 core
in vec2 v_uv;
out vec4 o_color;

uniform sampler2D u_tex;
uniform vec4 u_tint;

void main() {
    o_color = texture(u_tex, v_uv) * u_tint;
}
