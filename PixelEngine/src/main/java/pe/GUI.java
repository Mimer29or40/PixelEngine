package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.color.Color;
import pe.event.*;
import pe.font.Font;
import pe.gui.GuiStyle;
import pe.gui.GuiWindow;
import pe.render.*;
import rutils.Logger;

import java.util.*;

public class GUI
{
    private static final Logger LOGGER = new Logger();
    
    public static boolean initialized;
    
    // boolean                    FontAtlasOwnedByContext;            // IO.Fonts-> is owned by the ImGuiContext and will be destructed along with it.
    
    // ImGuiIO                 IO;
    // ImVector<ImGuiInputEvent> InputEventsQueue;                 // Input events which will be tricked/written into IO structure.
    // ImVector<ImGuiInputEvent> InputEventsTrail;                 // Past input events processed in NewFrame(). This is to allow domain-specific application to access e.g mouse/pen trail.
    
    static GuiStyle style;
    
    static Font font;         // (Shortcut) == FontStack.empty() ? IO.Font : FontStack.back()
    static int  fontSize;     // (Shortcut) == FontBaseSize * g.CurrentWindow->FontWindowScale == window->FontSize(). Text height for current window.
    static int  fontBaseSize; // (Shortcut) == IO.FontGlobalScale * Font->Scale * Font->FontSize. Base text height.
    
    // ImDrawListSharedData    DrawListSharedData;
    // double                  Time;
    // int                     FrameCount;
    // int                     FrameCountEnded;
    // int                     FrameCountRendered;
    // boolean                 WithinFrameScope;                   // Set by NewFrame(), cleared by EndFrame()
    // boolean                 WithinFrameScopeWithImplicitWindow; // Set by NewFrame(), cleared by EndFrame() when the implicit debug window has been pushed
    // boolean                 WithinEndChild;                     // Set within EndChild()
    // boolean                 GcCompactAll;                       // Request full GC
    // boolean                 TestEngineHookItems;                // Will call test engine hooks: ImGuiTestEngineHook_ItemAdd(), ImGuiTestEngineHook_ItemInfo(), ImGuiTestEngineHook_Log()
    // void*                   TestEngine;                         // Test engine user data
    
    // Windows state
    static final List<GuiWindow> windows       = new LinkedList<>(); // Windows, sorted in focus order
    // ImVector<ImGuiWindow*>  WindowsFocusOrder;                  // Root windows, sorted in focus order, back to front.
    // ImVector<ImGuiWindow*>  WindowsTempSortBuffer;              // Temporary buffer used in EndFrame() to reorder windows so parents are kept before their child
    // ImVector<ImGuiWindowStackData> CurrentWindowStack;
    // ImGuiStorage            WindowsById;                        // Map window's ImGuiID to ImGuiWindow*
    // int                     WindowsActiveCount;                 // Number of unique windows submitted by frame
    // ImVec2                  WindowsHoverPadding;                // Padding around resizable windows for which hovering on counts as hovering the window == ImMax(style.TouchExtraPadding, WINDOWS_HOVER_PADDING)
    // ImGuiWindow*            CurrentWindow;                      // Window being drawn into
    static       GuiWindow       hoveredWindow = null; // Window the mouse is hovering. Will typically catch mouse inputs.
    // ImGuiWindow*            HoveredWindowUnderMovingWindow;     // Hovered window ignoring MovingWindow. Only set if MovingWindow is set.
    // ImGuiWindow*            MovingWindow;                       // Track the window we clicked on (in order to preserve focus). The actual window that is moved is generally MovingWindow->RootWindow.
    // ImGuiWindow*            WheelingWindow;                     // Track the window we started mouse-wheeling on. Until a timer elapse or mouse has moved, generally keep scrolling the same window even if during the course of scrolling the mouse ends up hovering a child window.
    // ImVec2                  WheelingWindowRefMousePos;
    // float                   WheelingWindowTimer;
    
    static final List<GuiWindow> openPopupStack = new ArrayList<>(); // Which popups are open (persistent)
    // static final Deque<GuiWindow> BeginPopupStack = new ArrayDeque<>(); // Which level of BeginPopup() we are in (reset every frame)
    // static       int              BeginMenuCount;
    
    // Item/widgets state and tracking information
    // ImGuiID                 DebugHookIdInfo;                    // Will call core hooks: DebugHookIdInfo() from GetID functions, used by Stack Tool [next HoveredId/ActiveId to not pull in an extra cache-line]
    // ImGuiID                 HoveredId;                          // Hovered widget, filled during the frame
    // ImGuiID                 HoveredIdPreviousFrame;
    // boolean                 HoveredIdAllowOverlap;
    // boolean                 HoveredIdUsingMouseWheel;           // Hovered widget will use mouse wheel. Blocks scrolling the underlying window.
    // boolean                 HoveredIdPreviousFrameUsingMouseWheel;
    // boolean                 HoveredIdDisabled;                  // At least one widget passed the rect test, but has been discarded by disabled flag or popup inhibit. May be true even if HoveredId == 0.
    // float                   HoveredIdTimer;                     // Measure contiguous hovering time
    // float                   HoveredIdNotActiveTimer;            // Measure contiguous hovering time where the item has not been active
    // ImGuiID                 ActiveId;                           // Active widget
    // ImGuiID                 ActiveIdIsAlive;                    // Active widget has been seen this frame (we can't use a boolean as the ActiveId may change within the frame)
    // float                   ActiveIdTimer;
    // boolean                 ActiveIdIsJustActivated;            // Set at the time of activation for one frame
    // boolean                 ActiveIdAllowOverlap;               // Active widget allows another widget to steal active id (generally for overlapping widgets, but not always)
    // boolean                 ActiveIdNoClearOnFocusLoss;         // Disable losing active id if the active id window gets unfocused.
    // boolean                 ActiveIdHasBeenPressedBefore;       // Track whether the active id led to a press (this is to allow changing between PressOnClick and PressOnRelease without pressing twice). Used by range_select branch.
    // boolean                 ActiveIdHasBeenEditedBefore;        // Was the value associated to the widget Edited over the course of the Active state.
    // boolean                 ActiveIdHasBeenEditedThisFrame;
    // boolean                 ActiveIdUsingMouseWheel;            // Active widget will want to read mouse wheel. Blocks scrolling the underlying window.
    // ImU32                   ActiveIdUsingNavDirMask;            // Active widget will want to read those nav move requests (e.g. can activate a button and move away from it)
    // ImU32                   ActiveIdUsingNavInputMask;          // Active widget will want to read those nav inputs.
    // ImBitArrayForNamedKeys  ActiveIdUsingKeyInputMask;          // Active widget will want to read those key inputs. When we grow the ImGuiKey enum we'll need to either to order the enum to make useful keys come first, either redesign this into e.g. a small array.
    // ImVec2                  ActiveIdClickOffset;                // Clicked offset from upper-left corner, if applicable (currently only set by ButtonBehavior)
    // ImGuiWindow*            ActiveIdWindow;
    // ImGuiInputSource        ActiveIdSource;                     // Activating with mouse or nav (gamepad/keyboard)
    // int                     ActiveIdMouseButton;
    // ImGuiID                 ActiveIdPreviousFrame;
    // boolean                 ActiveIdPreviousFrameIsAlive;
    // boolean                 ActiveIdPreviousFrameHasBeenEditedBefore;
    // ImGuiWindow*            ActiveIdPreviousFrameWindow;
    // ImGuiID                 LastActiveId;                       // Store the last non-zero ActiveId, useful for animation.
    // float                   LastActiveIdTimer;                  // Store the last non-zero ActiveId timer since the beginning of activation, useful for animation.
    
    // Next window/item data
    // ImGuiItemFlags          CurrentItemFlags;                      // == g.ItemFlagsStack.back()
    // ImGuiNextItemData       NextItemData;                       // Storage for SetNextItem** functions
    // ImGuiLastItemData       LastItemData;                       // Storage for last submitted item (setup by ItemAdd)
    // ImGuiNextWindowData     NextWindowData;                     // Storage for SetNextWindow** functions
    
    // Shared stacks
    // ImVector<ImGuiColorMod> ColorStack;                         // Stack for PushStyleColor()/PopStyleColor() - inherited by Begin()
    // ImVector<ImGuiStyleMod> StyleVarStack;                      // Stack for PushStyleVar()/PopStyleVar() - inherited by Begin()
    // ImVector<ImFont*>       FontStack;                          // Stack for PushFont()/PopFont() - inherited by Begin()
    // ImVector<ImGuiID>       FocusScopeStack;                    // Stack for PushFocusScope()/PopFocusScope() - not inherited by Begin(), unless child window
    // ImVector<ImGuiItemFlags>ItemFlagsStack;                     // Stack for PushItemFlag()/PopItemFlag() - inherited by Begin()
    // ImVector<ImGuiGroupData>GroupStack;                         // Stack for BeginGroup()/EndGroup() - not inherited by Begin()
    
    // Viewports
    // ImVector<ImGuiViewportP*> Viewports;                        // Active viewports (Size==1 in 'master' branch). Each viewports hold their copy of ImDrawData.
    
    // Gamepad/keyboard Navigation
    // ImGuiWindow*            NavWindow;                          // Focused window for navigation. Could be called 'FocusWindow'
    // ImGuiID                 NavId;                              // Focused item for navigation
    // ImGuiID                 NavFocusScopeId;                    // Identify a selection scope (selection code often wants to "clear other items" when landing on an item of the selection set)
    // ImGuiID                 NavActivateId;                      // ~~ (g.ActiveId == 0) && IsNavInputPressed(ImGuiNavInput_Activate) ? NavId : 0, also set when calling ActivateItem()
    // ImGuiID                 NavActivateDownId;                  // ~~ IsNavInputDown(ImGuiNavInput_Activate) ? NavId : 0
    // ImGuiID                 NavActivatePressedId;               // ~~ IsNavInputPressed(ImGuiNavInput_Activate) ? NavId : 0
    // ImGuiID                 NavActivateInputId;                 // ~~ IsNavInputPressed(ImGuiNavInput_Input) ? NavId : 0; ImGuiActivateFlags_PreferInput will be set and NavActivateId will be 0.
    // ImGuiActivateFlags      NavActivateFlags;
    // ImGuiID                 NavJustMovedToId;                   // Just navigated to this id (result of a successfully MoveRequest).
    // ImGuiID                 NavJustMovedToFocusScopeId;         // Just navigated to this focus scope id (result of a successfully MoveRequest).
    // ImGuiKeyModFlags        NavJustMovedToKeyMods;
    // ImGuiID                 NavNextActivateId;                  // Set by ActivateItem(), queued until next frame.
    // ImGuiActivateFlags      NavNextActivateFlags;
    // ImGuiInputSource        NavInputSource;                     // Keyboard or Gamepad mode? THIS WILL ONLY BE None or NavGamepad or NavKeyboard.
    // ImGuiNavLayer           NavLayer;                           // Layer we are navigating on. For now the system is hard-coded for 0=main contents and 1=menu/title bar, may expose layers later.
    // boolean                 NavIdIsAlive;                       // Nav widget has been seen this frame ~~ NavRectRel is valid
    // boolean                 NavMousePosDirty;                   // When set we will update mouse position if (io.ConfigFlags & ImGuiConfigFlags_NavEnableSetMousePos) if set (NB: this not enabled by default)
    // boolean                 NavDisableHighlight;                // When user starts using mouse, we hide gamepad/keyboard highlight (NB: but they are still available, which is why NavDisableHighlight isn't always != NavDisableMouseHover)
    // boolean                 NavDisableMouseHover;               // When user starts using gamepad/keyboard, we hide mouse hovering highlight until mouse is touched again.
    
    // Navigation: Init & Move Requests
    // boolean                 NavAnyRequest;                      // ~~ NavMoveRequest || NavInitRequest this is to perform early out in ItemAdd()
    // boolean                 NavInitRequest;                     // Init request for appearing window to select first item
    // boolean                 NavInitRequestFromMove;
    // ImGuiID                 NavInitResultId;                    // Init request result (first item of the window, or one for which SetItemDefaultFocus() was called)
    // ImRect                  NavInitResultRectRel;               // Init request result rectangle (relative to parent window)
    // boolean                 NavMoveSubmitted;                   // Move request submitted, will process result on next NewFrame()
    // boolean                 NavMoveScoringItems;                // Move request submitted, still scoring incoming items
    // boolean                 NavMoveForwardToNextFrame;
    // ImGuiNavMoveFlags       NavMoveFlags;
    // ImGuiScrollFlags        NavMoveScrollFlags;
    // ImGuiKeyModFlags        NavMoveKeyMods;
    // ImGuiDir                NavMoveDir;                         // Direction of the move request (left/right/up/down)
    // ImGuiDir                NavMoveDirForDebug;
    // ImGuiDir                NavMoveClipDir;                     // FIXME-NAV: Describe the purpose of this better. Might want to rename?
    // ImRect                  NavScoringRect;                     // Rectangle used for scoring, in screen space. Based of window->NavRectRel[], modified for directional navigation scoring.
    // ImRect                  NavScoringNoClipRect;               // Some nav operations (such as PageUp/PageDown) enforce a region which clipper will attempt to always keep submitted
    // int                     NavScoringDebugCount;               // Metrics for debugging
    // int                     NavTabbingDir;                      // Generally -1 or +1, 0 when tabbing without a nav id
    // int                     NavTabbingCounter;                  // >0 when counting items for tabbing
    // ImGuiNavItemData        NavMoveResultLocal;                 // Best move request candidate within NavWindow
    // ImGuiNavItemData        NavMoveResultLocalVisible;          // Best move request candidate within NavWindow that are mostly visible (when using ImGuiNavMoveFlags_AlsoScoreVisibleSet flag)
    // ImGuiNavItemData        NavMoveResultOther;                 // Best move request candidate within NavWindow's flattened hierarchy (when using ImGuiWindowFlags_NavFlattened flag)
    // ImGuiNavItemData        NavTabbingResultFirst;              // First tabbing request candidate within NavWindow and flattened hierarchy
    
    // Navigation: Windowing (CTRL+TAB for list, or Menu button + keys or directional pads to move/resize)
    // ImGuiWindow*            NavWindowingTarget;                 // Target window when doing CTRL+Tab (or Pad Menu + FocusPrev/Next), this window is temporarily displayed top-most!
    // ImGuiWindow*            NavWindowingTargetAnim;             // Record of last valid NavWindowingTarget until DimBgRatio and NavWindowingHighlightAlpha becomes 0.0f, so the fade-out can stay on it.
    // ImGuiWindow*            NavWindowingListWindow;             // Internal window actually listing the CTRL+Tab contents
    // float                   NavWindowingTimer;
    // float                   NavWindowingHighlightAlpha;
    // boolean                 NavWindowingToggleLayer;
    
    // Render
    // float                   DimBgRatio;                         // 0.0..1.0 animation when fading in a dimming background (for modal window and CTRL+TAB list)
    // ImGuiMouseCursor        MouseCursor;
    
    // Drag and Drop
    // boolean                 DragDropActive;
    // boolean                 DragDropWithinSource;               // Set when within a BeginDragDropXXX/EndDragDropXXX block for a drag source.
    // boolean                 DragDropWithinTarget;               // Set when within a BeginDragDropXXX/EndDragDropXXX block for a drag target.
    // ImGuiDragDropFlags      DragDropSourceFlags;
    // int                     DragDropSourceFrameCount;
    // int                     DragDropMouseButton;
    // ImGuiPayload            DragDropPayload;
    // ImRect                  DragDropTargetRect;                 // Store rectangle of current target candidate (we favor small targets when overlapping)
    // ImGuiID                 DragDropTargetId;
    // ImGuiDragDropFlags      DragDropAcceptFlags;
    // float                   DragDropAcceptIdCurrRectSurface;    // Target item surface (we resolve overlapping targets by prioritizing the smaller surface)
    // ImGuiID                 DragDropAcceptIdCurr;               // Target item id (set at the time of accepting the payload)
    // ImGuiID                 DragDropAcceptIdPrev;               // Target item id from previous frame (we need to store this to allow for overlapping drag and drop targets)
    // int                     DragDropAcceptFrameCount;           // Last time a target expressed a desire to accept the source
    // ImGuiID                 DragDropHoldJustPressedId;          // Set when holding a payload just made ButtonBehavior() return a press.
    // ImVector<unsigned char> DragDropPayloadBufHeap;             // We don't expose the ImVector<> directly, ImGuiPayload only holds pointer+size
    // unsigned char           DragDropPayloadBufLocal[16];        // Local buffer for small payloads
    
    // Clipper
    // int                             ClipperTempDataStacked;
    // ImVector<ImGuiListClipperData>  ClipperTempData;
    //
    // Table
    // ImGuiTable*                     CurrentTable;
    // int                             TablesTempDataStacked;      // Temporary table data size (because we leave previous instances undestructed, we generally don't use TablesTempData.Size)
    // ImVector<ImGuiTableTempData>    TablesTempData;             // Temporary table data (buffers reused/shared across instances, support nesting)
    // ImPool<ImGuiTable>              Tables;                     // Persistent table data
    // ImVector<float>                 TablesLastTimeActive;       // Last used timestamp of each tables (SOA, for efficient GC)
    // ImVector<ImDrawChannel>         DrawChannelsTempMergeBuffer;
    
    // Tab bars
    // ImGuiTabBar*                    CurrentTabBar;
    // ImPool<ImGuiTabBar>             TabBars;
    // ImVector<ImGuiPtrOrIndex>       CurrentTabBarStack;
    // ImVector<ImGuiShrinkWidthItem>  ShrinkWidthBuffer;
    
    // Widget state
    // ImVec2                  MouseLastValidPos;
    // ImGuiInputTextState     InputTextState;
    // ImFont                  InputTextPasswordFont;
    // ImGuiID                 TempInputId;                        // Temporary text input when CTRL+clicking on a slider, etc.
    // ImGuiColorEditFlags     ColorEditOptions;                   // Store user options for color edit widgets
    // float                   ColorEditLastHue;                   // Backup of last Hue associated to LastColor, so we can restore Hue in lossy RGB<>HSV round trips
    // float                   ColorEditLastSat;                   // Backup of last Saturation associated to LastColor, so we can restore Saturation in lossy RGB<>HSV round trips
    // ImU32                   ColorEditLastColor;                 // RGB value with alpha set to 0.
    // ImVec4                  ColorPickerRef;                     // Initial/reference color at the time of opening the color picker.
    // ImGuiComboPreviewData   ComboPreviewData;
    // float                   SliderCurrentAccum;                 // Accumulated slider delta when using navigation controls.
    // boolean                 SliderCurrentAccumDirty;            // Has the accumulated slider delta changed since last time we tried to apply it?
    // boolean                 DragCurrentAccumDirty;
    // float                   DragCurrentAccum;                   // Accumulator for dragging modification. Always high-precision, not rounded by end-user precision settings
    // float                   DragSpeedDefaultRatio;              // If speed == 0.0f, uses (max-min) * DragSpeedDefaultRatio
    // float                   ScrollbarClickDeltaToGrabCenter;    // Distance between mouse and center of grab box, normalized in parent space. Use storage?
    // float                   DisabledAlphaBackup;                // Backup for style.Alpha for BeginDisabled()
    // short                   DisabledStackSize;
    // short                   TooltipOverrideCount;
    // float                   TooltipSlowDelay;                   // Time before slow tooltips appears (FIXME: This is temporary until we merge in tooltip timer+priority work)
    // ImVector<char>          ClipboardHandlerData;               // If no custom clipboard handler is defined
    // ImVector<ImGuiID>       MenusIdSubmittedThisFrame;          // A list of menu IDs that were rendered at least once
    
    // Platform support
    // ImGuiPlatformImeData    PlatformImeData;                    // Data updated by current frame
    // ImGuiPlatformImeData    PlatformImeDataPrev;                // Previous frame data (when changing we will call io.SetPlatformImeDataFn
    // char                    PlatformLocaleDecimalPoint;         // '.' or *localeconv()->decimal_point
    
    // Settings
    // boolean                 SettingsLoaded;
    // float                   SettingsDirtyTimer;                 // Save .ini Settings to memory when time reaches zero
    // ImGuiTextBuffer         SettingsIniData;                    // In memory .ini settings
    // ImVector<ImGuiSettingsHandler>      SettingsHandlers;       // List of .ini settings handlers
    // ImChunkStream<ImGuiWindowSettings>  SettingsWindows;        // ImGuiWindow .ini settings entries
    // ImChunkStream<ImGuiTableSettings>   SettingsTables;         // ImGuiTable .ini settings entries
    // ImVector<ImGuiContextHook>          Hooks;                  // Hooks for extensions (e.g. test engine)
    // ImGuiID                             HookIdNext;             // Next available HookId
    
    // Capture/Logging
    // boolean                 LogEnabled;                         // Currently capturing
    // ImGuiLogType            LogType;                            // Capture target
    // ImFileHandle            LogFile;                            // If != NULL log to stdout/ file
    // ImGuiTextBuffer         LogBuffer;                          // Accumulation buffer when log to clipboard. This is pointer so our GImGui static constructor doesn't call heap allocators.
    // final char*             LogNextPrefix;
    // final char*             LogNextSuffix;
    // float                   LogLinePosY;
    // boolean                 LogLineFirstItem;
    // int                     LogDepthRef;
    // int                     LogDepthToExpand;
    // int                     LogDepthToExpandDefault;            // Default/stored value for LogDepthMaxExpand if not specified in the LogXXX function call.
    
    // Debug Tools
    // boolean                 DebugItemPickerActive;              // Item picker is active (started with DebugStartItemPicker())
    // ImGuiID                 DebugItemPickerBreakId;             // Will call IM_DEBUG_BREAK() when encountering this ID
    // ImGuiMetricsConfig      DebugMetricsConfig;
    // ImGuiStackTool          DebugStackTool;
    
    // Misc
    // float                   FramerateSecPerFrame[120];          // Calculate estimate of framerate for user over the last 2 seconds.
    // int                     FramerateSecPerFrameIdx;
    // int                     FramerateSecPerFrameCount;
    // float                   FramerateSecPerFrameAccum;
    // int                     WantCaptureMouseNextFrame;          // Explicit capture via CaptureKeyboardFromApp()/CaptureMouseFromApp() sets those flags
    // int                     WantCaptureKeyboardNextFrame;
    // int                     WantTextInputNextFrame;
    // char                    TempBuffer[1024 * 3 + 1];           // Temporary text buffer
    
    static void setup()
    {
        GUI.LOGGER.fine("Setup");
        
        GUI.initialized = true;
        
        GUI.style = new GuiStyle();
        
        setFont(defaultFont(), 12);
        
        // windows.remove(1)
        // windows.add();
        
        GuiWindow window;
        
        window = new GuiWindow();
        window.pos.set(10, 10);
        window.size.set(100, 100);
        windows.add(window);
        
        window = new GuiWindow();
        window.pos.set(200, 200);
        window.size.set(100, 100);
        windows.add(window);
    }
    
    static void destroy()
    {
        GUI.LOGGER.fine("Destroy");
    }
    
    @SuppressWarnings("StatementWithEmptyBody")
    static void handleEvents()
    {
        for (Event event : Engine.Events.get())
        {
            if (event instanceof EventMouseMoved mMoved)
            {
                int x = (int) mMoved.x();
                int y = (int) mMoved.y();
                
                int hoveredIndex = -1;
                for (int i = 0; i < windows.size(); i++)
                {
                    GuiWindow window = windows.get(i);
                    if (hoveredIndex < 0 && window.rect().contains(x, y)) hoveredIndex = i;
                    // window.hovered = false;
                }
                if (hoveredIndex >= 0)
                {
                    hoveredWindow = windows.get(hoveredIndex);
                    // hoveredWindow.hovered = true;
                    mMoved.consume();
                }
                else
                {
                    hoveredWindow = null;
                }
            }
            else if (event instanceof EventMouseScrolled)
            {
                // NO-OP
            }
            else if (event instanceof EventMouseButtonDown mbDown)
            {
                // int x = (int) mbDown.x();
                // int y = (int) mbDown.y();
                //
                // int focusedIndex = -1;
                // for (int i = 0; i < windows.size(); i++)
                // {
                //     GuiWindow window = windows.get(i);
                //     if (focusedIndex < 0 && window.rect().contains(x, y)) focusedIndex = i;
                //     // window.focused = false;
                // }
                // if (focusedIndex >= 0)
                // {
                //     focusedWindow = windows.remove(focusedIndex);
                //     // focusedWindow.focused = true;
                //     windows.add(0, focusedWindow);
                //     mbDown.consume();
                // }
                // else
                // {
                //     focusedWindow = null;
                // }
            }
            else if (event instanceof EventMouseButtonUp mbUp)
            {
                // NO-OP
            }
            else if (event instanceof EventMouseButtonDragged mbDragged)
            {
                // if (hoveredWindow != null)
                // {
                //     int dx = (int) mbDragged.dx();
                //     int dy = (int) mbDragged.dy();
                //     hoveredWindow.rect().pos().add(dx, dy);
                //     mbDragged.consume();
                // }
            }
            else if (event instanceof EventKeyboardKeyDown kkDown)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardKeyUp kkUp)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardKeyRepeated kkRepeated)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardTyped kTyped)
            {
                // NO-OP
            }
        }
    }
    
    static void draw()
    {
        GLFramebuffer.bind(null);
        GLProgram.bind(null);
        
        GL.defaultState();
        GL.depthMode(DepthMode.NONE);
        
        GLBatch.bind(null);
        
        int r = pe.Window.framebufferWidth() >> 1;
        int l = -r;
        int b = pe.Window.framebufferHeight() >> 1;
        int t = -b;
        
        GLBatch.projection().setOrtho(l, r, b, t, 1.0, -1.0);
        GLBatch.view().identity().translate(l, t, 0.0);
        GLBatch.model().identity();
        GLBatch.normal().identity();
        
        GLBatch.diffuse().set(Color.WHITE);
        GLBatch.specular().set(Color.WHITE);
        GLBatch.ambient().set(Color.WHITE);
        
        for (int i = windows.size() - 1; i >= 0; i--) windows.get(i).layout();
        
        for (int i = windows.size() - 1; i >= 0; i--) windows.get(i).draw();
        
        GLBatch.stats();
    }
    
    private GUI() {}
    
    public static @NotNull GuiStyle style()
    {
        return GUI.style;
    }
    
    public static int fontBaseSize()
    {
        return GUI.fontBaseSize;
    }
    
    public static int fontSize()
    {
        return GUI.fontSize;
    }
    
    public static void setFont(@NotNull Font font, int size)
    {
        setFont(font);
        setFontSize(size);
    }
    
    public static void setFont(@NotNull Font font)
    {
        GUI.font = font;
    }
    
    public static void setFontSize(int size)
    {
        GUI.fontBaseSize = Math.max(1, size);
        GUI.fontSize     = GUI.windows.size() > 0 ? GUI.windows.get(0).calcFontSize() : 0;
    }
    
    public static @NotNull Font defaultFont()
    {
        return Font.get(null, null, null);
    }
    
    public static void focusWindow(@Nullable GuiWindow window)
    {
        // TODO
        // if (g.NavWindow != window)
        // {
        //     g.NavWindow = window;
        //     if (window && g.NavDisableMouseHover)
        //         g.NavMousePosDirty = true;
        //     g.NavId = window ? window->NavLastIds[0] : 0; // Restore NavId
        //     g.NavFocusScopeId = 0;
        //     g.NavIdIsAlive = false;
        //     g.NavLayer = ImGuiNavLayer_Main;
        //     g.NavInitRequest = g.NavMoveSubmitted = g.NavMoveScoringItems = false;
        //     NavUpdateAnyRequestFlag();
        //     //IMGUI_DEBUG_LOG("FocusWindow(\"%s\")\n", window ? window->Name : NULL);
        // }
    
        // TODO
        // Close popups if any
        // closePopupsOverWindow(window, false);
        
        // Move the root window to the top of the pile
        assert window == null || window.RootWindow != null;
        GuiWindow focus_front_window   = window != null ? window.RootWindow : null; // NB: In docking branch this is window->RootWindowDockStop
        GuiWindow display_front_window = window != null ? window.RootWindow : null;
        
        // Steal active widgets. Some of the cases it triggers includes:
        // - Focus a window while an InputText in another window is active, if focus happens before the old InputText can run.
        // - When using Nav to activate menu items (due to timing of activating on press->new window appears->losing ActiveId)
        // if (g.ActiveId != 0 && g.ActiveIdWindow && g.ActiveIdWindow->RootWindow != focus_front_window)
        // if (!g.ActiveIdNoClearOnFocusLoss) ClearActiveID();
        
        // Passing NULL allow to disable keyboard focus
        if (window == null) return;
        
        // Bring to front
        GUI.windows.remove(focus_front_window);
        GUI.windows.add(0, focus_front_window);
        
        // if (!(window.isEnabled(GuiWindow.Flag.NO_BRING_TO_FRONT_ON_FOCUS) ||
        //       display_front_window.isEnabled(GuiWindow.Flag.NO_BRING_TO_FRONT_ON_FOCUS)))
        // {
        //     ImGuiContext& g = *GImGui;
        //     ImGuiWindow* current_front_window = g.Windows.back();
        //     if (current_front_window == window || current_front_window->RootWindow == window) // Cheap early out (could be better)
        //         return;
        //     for (int i = g.Windows.Size - 2; i >= 0; i--) // We can ignore the top-most window
        //         if (g.Windows[i] == window)
        //         {
        //             memmove(&g.Windows[i], &g.Windows[i + 1], (size_t)(g.Windows.Size - i - 1) * sizeof(ImGuiWindow*));
        //             g.Windows[g.Windows.Size - 1] = window;
        //             break;
        //         }
        // }
    }
}
