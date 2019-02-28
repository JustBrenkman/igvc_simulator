package com.byu_igvc.core.render;

import com.byu_igvc.core.scene.Camera;
import com.byu_igvc.core.scene.Model;
import com.byu_igvc.logger.Logger;
import glm.mat._4.Mat4;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.awt.*;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class OpenGLRenderingEngine implements IRenderEngine {
    private static long window;
    private Color backgroundColor;
    private String title = "OpenGL Rendering Engine";
    private static float width;
    private static float height;

    public OpenGLRenderingEngine() {
        backgroundColor = new Color(0x052059);
    }

    public static long getWindow() {
        return window;
    }

    @Override
    public void init() {
        GLFWErrorCallback.create((errorCode, errorMessage) -> Logger.error("Error code: " + errorCode + ", message: " + errorMessage, new GLFWException(errorCode, errorMessage))).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to init glfw");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        OpenGLRenderingEngine.window = glfwCreateWindow(640, 400, title, NULL, NULL);
        OpenGLRenderingEngine.width = 640;
        OpenGLRenderingEngine.height = 400;
        if (window == NULL)
            throw new RuntimeException("Unable to create window");

        glfwSetKeyCallback(window, (window, key, scanCode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            OpenGLRenderingEngine.width = width;
            OpenGLRenderingEngine.height = height;
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        Logger.info("OpenGL version: " + glGetString(GL_VERSION));
        glClearColor(backgroundColor.getRed() / 255.0f, backgroundColor.getGreen() / 255.0f,  backgroundColor.getBlue() / 255.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
    }

    @Override
    public void renderMesh(Mesh mesh, Mat4 modelViewProjection) {
        glUseProgram(mesh.getShader().getProgramID());
        Shader.setUniformMat4(mesh.getShader(), "mvp", modelViewProjection);

        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, mesh.getVertexBuffer());
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, mesh.getNumberOfVerticies());

        // Unbinds everything
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glUseProgram(0);
    }

    @Override
    public void renderModel(Camera camera, Model model) {
        Mat4 mvp = camera.getProjectionMatrix().mul(camera.getViewMatrix()).mul(new Mat4(1.0f));
        renderMesh(model.getMesh(), mvp);
    }

    @Override
    public void startFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void updateWindow() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    @Override
    public boolean shouldShutDown() {
        return glfwWindowShouldClose(window);
    }

    @Override
    public void shutdown() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    @Override
    public float getWidowWidth() {
        return OpenGLRenderingEngine.width;
    }

    @Override
    public float getWindowHeight() {
        return OpenGLRenderingEngine.height;
    }


    //    Getters and Setters
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }
}