package myau.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ShaderUtil {
    private final int programID;
    private final Map<String, Integer> uniforms = new HashMap<>();
    
    public ShaderUtil(String fragmentShaderPath) {
        int vertexShaderID = createShader("myau/shader/vertex.vsh", GL20.GL_VERTEX_SHADER);
        int fragmentShaderID = createShader(fragmentShaderPath, GL20.GL_FRAGMENT_SHADER);
        this.programID = GL20.glCreateProgram();
        
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        GL20.glLinkProgram(programID);
        
        int status = GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS);
        if (status == 0) {
            System.err.println("Shader program linking failed: " + GL20.glGetProgramInfoLog(programID, 1024));
        }
        
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
    }
    
    private int createShader(String shaderPath, int shaderType) {
        int shaderID = GL20.glCreateShader(shaderType);
        String shaderSource = getShaderSource(shaderPath);
        
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == 0) {
            System.err.println("Shader compilation failed: " + GL20.glGetShaderInfoLog(shaderID, 1024));
        }
        
        return shaderID;
    }
    
    private String getShaderSource(String path) {
        try {
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(path)).getInputStream();
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public void init() {
        GL20.glUseProgram(programID);
    }
    
    public void unload() {
        GL20.glUseProgram(0);
    }
    
    public int getUniform(String name) {
        if (uniforms.containsKey(name)) {
            return uniforms.get(name);
        }
        
        int uniform = GL20.glGetUniformLocation(programID, name);
        uniforms.put(name, uniform);
        return uniform;
    }
    
    public void setUniformf(String name, float... args) {
        int uniform = getUniform(name);
        switch (args.length) {
            case 1:
                GL20.glUniform1f(uniform, args[0]);
                break;
            case 2:
                GL20.glUniform2f(uniform, args[0], args[1]);
                break;
            case 3:
                GL20.glUniform3f(uniform, args[0], args[1], args[2]);
                break;
            case 4:
                GL20.glUniform4f(uniform, args[0], args[1], args[2], args[3]);
                break;
        }
    }
    
    public void setUniformi(String name, int... args) {
        int uniform = getUniform(name);
        switch (args.length) {
            case 1:
                GL20.glUniform1i(uniform, args[0]);
                break;
            case 2:
                GL20.glUniform2i(uniform, args[0], args[1]);
                break;
            case 3:
                GL20.glUniform3i(uniform, args[0], args[1], args[2]);
                break;
            case 4:
                GL20.glUniform4i(uniform, args[0], args[1], args[2], args[3]);
                break;
        }
    }
}