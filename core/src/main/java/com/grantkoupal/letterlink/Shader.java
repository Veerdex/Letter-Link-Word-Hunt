package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shader {

    // Vertex shader (usually just passes through)
    private static final String vertexShader =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = u_projTrans * a_position;\n" +
            "}";

    // Fragment shader (does the blur)
    private static final String fragmentShader =
        "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform float blurSize;\n" +
            "uniform vec4 u_tint;\n" +
            "void main() {\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    for(float x = -2.0; x <= 2.0; x += 1.0) {\n" +
            "        for(float y = -2.0; y <= 2.0; y += 1.0) {\n" +
            "            sum += texture2D(u_texture, v_texCoords + vec2(x, y) * blurSize);\n" +
            "        }\n" +
            "    }\n" +
            "    gl_FragColor = (sum / 25.0) * u_tint;\n" +
            "}";

    private static String fragment =
        "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform float u_brightness;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(u_texture, v_texCoords) * v_color;\n" +
            "  color.rgb *= u_brightness;\n" +
            "  gl_FragColor = color;\n" +
            "}";

    public static ShaderProgram blurShader = new ShaderProgram(vertexShader, fragmentShader);

    public static ShaderProgram glowShader = new ShaderProgram(SpriteBatch.createDefaultShader().getVertexShaderSource(), fragment);
}
