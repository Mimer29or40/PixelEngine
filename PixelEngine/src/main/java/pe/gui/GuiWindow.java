package pe.gui;

import pe.shape.AABB2i;

public class GuiWindow extends GuiElement
{
    public String title;
    
    public final AABB2i bounds = new AABB2i();
    
    public boolean hovered = false;
    public boolean focused = false;
    
    // ImGuiWindowFlags Flags;                              // See enum ImGuiWindowFlags_
    // ImVec2           Pos;                                // Position (always rounded-up to nearest pixel)
    // ImVec2           Size;                               // Current size (==SizeFull or collapsed title bar size)
    // ImVec2           SizeFull;                           // Size when non collapsed
    // ImVec2           ContentSize;                        // Size of contents/scrollable client area (calculated from the extents reach of the cursor) from previous frame. Does not include window decoration or window padding.
    // ImVec2           ContentSizeIdeal;
    // ImVec2           ContentSizeExplicit;                // Size of contents/scrollable client area explicitly request by the user via SetNextWindowContentSize().
    // ImVec2           WindowPadding;                      // Window padding at the time of Begin().
    // float            WindowRounding;                     // Window rounding at the time of Begin(). May be clamped lower to avoid rendering artifacts with title bar, menu bar etc.
    // float            WindowBorderSize;                   // Window border size at the time of Begin().
    // int              NameBufLen;                         // Size of buffer storing Name. May be larger than strlen(Name)!
    // ImGuiID          MoveId;                             // == window->GetID("#MOVE")
    // ImGuiID          ChildId;                            // ID of corresponding item in parent window (for navigation to return from child window to parent window)
    // ImVec2           Scroll;
    // ImVec2           ScrollMax;
    // ImVec2           ScrollTarget;                       // target scroll position. stored as cursor position with scrolling canceled out, so the highest point is always 0.0f. (FLT_MAX for no change)
    // ImVec2           ScrollTargetCenterRatio;            // 0.0f = scroll so that target position is at top, 0.5f = scroll so that target position is centered
    // ImVec2           ScrollTargetEdgeSnapDist;           // 0.0f = no snapping, >0.0f snapping threshold
    // ImVec2           ScrollbarSizes;                     // Size taken by each scrollbars on their smaller axis. Pay attention! ScrollbarSizes.x == width of the vertical scrollbar, ScrollbarSizes.y = height of the horizontal scrollbar.
    // bool             ScrollbarX, ScrollbarY;             // Are scrollbars visible?
    // bool Active;                             // Set to true on Begin(), unless Collapsed
    // bool WasActive;
    // bool WriteAccessed;                      // Set to true when any widget access the current window
    // bool Collapsed;                          // Set when collapsing window to become only title-bar
    // bool WantCollapseToggle;
    // bool SkipItems;                          // Set when items can safely be all clipped (e.g. window not visible or collapsed)
    // bool Appearing;                          // Set during the frame where the window is appearing (or re-appearing)
    // bool Hidden;                             // Do not display (== HiddenFrames*** > 0)
    // bool IsFallbackWindow;                   // Set on the "Debug##Default" window.
    // bool IsExplicitChild;                    // Set when passed _ChildWindow, left to false by BeginDocked()
    // bool HasCloseButton;                     // Set when the window has a close button (p_open != NULL)
    // signed
    // char ResizeBorderHeld;                   // Current border being held for resize (-1: none, otherwise 0-3)
    // short   BeginCount;                         // Number of Begin() during the current frame (generally 0 or 1, 1+ if appending via multiple Begin/End pairs)
    // short   BeginOrderWithinParent;             // Begin() order within immediate parent window, if we are a child window. Otherwise 0.
    // short   BeginOrderWithinContext;            // Begin() order within entire imgui context. This is mostly used for debugging submission order related issues.
    // short   FocusOrder;                         // Order within WindowsFocusOrder[], altered when windows are focused.
    // ImGuiID PopupId;                            // ID in the popup stack when this window is used as a popup/menu (because we use generic Name/ID for recycling)
    // ImS8    AutoFitFramesX, AutoFitFramesY;
    // ImS8      AutoFitChildAxises;
    // bool      AutoFitOnlyGrows;
    // ImGuiDir  AutoPosLastDirection;
    // ImS8      HiddenFramesCanSkipItems;           // Hide the window for N frames
    // ImS8      HiddenFramesCannotSkipItems;        // Hide the window for N frames while allowing items to be submitted so we can measure their size
    // ImS8      HiddenFramesForRenderOnly;          // Hide the window until frame N at Render() time only
    // ImS8      DisableInputsFrames;                // Disable window interactions for N frames
    // ImGuiCond SetWindowPosAllowFlags :8;         // store acceptable condition flags for SetNextWindowPos() use.
    // ImGuiCond SetWindowSizeAllowFlags :8;        // store acceptable condition flags for SetNextWindowSize() use.
    // ImGuiCond SetWindowCollapsedAllowFlags :8;   // store acceptable condition flags for SetNextWindowCollapsed() use.
    // ImVec2 SetWindowPosVal;                    // store window position when using a non-zero Pivot (position set needs to be processed when we know the window size)
    // ImVec2 SetWindowPosPivot;                  // store window pivot for positioning. ImVec2(0, 0) when positioning from top-left corner; ImVec2(0.5f, 0.5f) for centering; ImVec2(1, 1) for bottom right.
    //
    // ImVector<ImGuiID>   IDStack;                            // ID stack. ID are hashes seeded with the value at the top of the stack. (In theory this should be in the TempData structure)
    // ImGuiWindowTempData DC;                                 // Temporary per-window data, reset at the beginning of the frame. This used to be called ImGuiDrawContext, hence the "DC" variable name.
    //
    // // The best way to understand what those rectangles are is to use the 'Metrics->Tools->Show Windows Rectangles' viewer.
    // // The main 'OuterRect', omitted as a field, is window->Rect().
    // ImRect   OuterRectClipped;                   // == Window->Rect() just after setup in Begin(). == window->Rect() for root window.
    // ImRect   InnerRect;                          // Inner rectangle (omit title bar, menu bar, scroll bar)
    // ImRect   InnerClipRect;                      // == InnerRect shrunk by WindowPadding*0.5f on each side, clipped within viewport or parent clip rect.
    // ImRect   WorkRect;                           // Initially covers the whole scrolling region. Reduced by containers e.g columns/tables when active. Shrunk by WindowPadding*1.0f on each side. This is meant to replace ContentRegionRect over time (from 1.71+ onward).
    // ImRect   ParentWorkRect;                     // Backup of WorkRect before entering a container such as columns/tables. Used by e.g. SpanAllColumns functions to easily access. Stacked containers are responsible for maintaining this. // FIXME-WORKRECT: Could be a stack?
    // ImRect   ClipRect;                           // Current clipping/scissoring rectangle, evolve as we are using PushClipRect(), etc. == DrawList->clip_rect_stack.back().
    // ImRect   ContentRegionRect;                  // FIXME: This is currently confusing/misleading. It is essentially WorkRect but not handling of scrolling. We currently rely on it as right/bottom aligned sizing operation need some size to rely on.
    // ImVec2ih HitTestHoleSize;                    // Define an optional rectangular hole where mouse will pass-through the window.
    // ImVec2ih HitTestHoleOffset;
    //
    // int                       LastFrameActive;                    // Last frame number the window was Active.
    // float                     LastTimeActive;                     // Last timestamp the window was Active (using float as we don't need high precision there)
    // float                     ItemWidthDefault;
    // ImGuiStorage              StateStorage;
    // ImVector<ImGuiOldColumns> ColumnsStorage;
    // float                     FontWindowScale;                    // User scale multiplier per-window, via SetWindowFontScale()
    // int                       SettingsOffset;                     // Offset into SettingsWindows[] (offsets are always valid as we only grow the array from the back)
    //
    // ImDrawList*DrawList;                           // == &DrawListInst (for backward compatibility reason with code using imgui_internal.h we keep this a pointer)
    // ImDrawList DrawListInst;
    // ImGuiWindow*ParentWindow;                       // If we are a child _or_ popup _or_ docked window, this is pointing to our parent. Otherwise NULL.
    // ImGuiWindow*ParentWindowInBeginStack;
    // ImGuiWindow*RootWindow;                         // Point to ourself or first ancestor that is not a child window. Doesn't cross through popups/dock nodes.
    // ImGuiWindow*RootWindowPopupTree;                // Point to ourself or first ancestor that is not a child window. Cross through popups parent<>child.
    // ImGuiWindow*RootWindowForTitleBarHighlight;     // Point to ourself or first ancestor which will display TitleBgActive color when this window is active.
    // ImGuiWindow*RootWindowForNav;                   // Point to ourself or first ancestor which doesn't have the NavFlattened flag.
    //
    // ImGuiWindow*NavLastChildNavWindow;              // When going to the menu bar, we remember the child window we came from. (This could probably be made implicit if we kept g.Windows sorted by last focused including child window.)
    // ImGuiID NavLastIds[
    // ImGuiNavLayer_COUNT];    // Last known NavId for this window, per layer (0/1)
    // ImRect NavRectRel[
    // ImGuiNavLayer_COUNT];    // Reference rectangle, in window relative space
    //
    // int  MemoryDrawListIdxCapacity;          // Backup of last idx/vtx count, so when waking up the window we can preallocate and avoid iterative alloc/copy
    // int  MemoryDrawListVtxCapacity;
    // bool MemoryCompacted;                    // Set when window extraneous data have been garbage collected
    
    @Override
    public void layout()
    {
    
    }
}