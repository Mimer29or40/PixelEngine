package pe;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import rutils.Logger;

import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;

public class ImGUI
{
    private static final Logger LOGGER = new Logger();
    
    static ImGuiImplGl3  imGuiGl3;
    static ImGuiImplGlfw imGuiGlfw;
    
    static void setup()
    {
        // Setup Dear ImGui context
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        
        // Setup Platform/Renderer bindings
        imGuiGl3  = new ImGuiImplGl3();
        imGuiGlfw = new ImGuiImplGlfw();
        
        imGuiGl3.init("#version 330 core");
        imGuiGlfw.init(Window.handle, true);
        
        // Setup Dear ImGui style
        ImGui.styleColorsDark();
    }
    
    static void destroy()
    {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
    
    static void handleEvents()
    {
        // feed inputs to dear imgui, start new frame
        // imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }
    
    static void draw()
    {
        // render your GUI
        ImGui.begin("Demo window");
        ImGui.button("Hello!");
        ImGui.end();
        
        // Render dear imgui into screen
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable))
        {
            final long window = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(window);
        }
    }
}
