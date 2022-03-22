package pe.guiOld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nuklear.NkContext;
import pe.Window;
import pe.util.Property;

public class GUIDebugWindow extends GUIWindow
{
    public GUIWindow target;
    
    private final Property<Integer> propX, propY, propW, propH;
    private final Property<Integer> propSX, propSY;
    
    public GUIDebugWindow(@Nullable String title, int x, int y, int width, int height)
    {
        super(title, x, y, width, height);
        
        GUILayout rows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        {
            rows.add(new GUILabel(() -> "Title: " + (this.target == null ? "NULL" : this.target.title())));
            
            GUITree treeBounds = new GUITree(GUITree.Type.TAB, "Dimensions");
            {
                GUILayout treeBoundsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
                {
                    GUIPropertyInt intProp;
                    
                    intProp    = treeBoundsRows.add(new GUIPropertyInt("X Pos", 0, 0, Window.framebufferWidth(), 10, 1));
                    this.propX = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : Window.framebufferWidth() - this.target.size.x);
                    intProp    = treeBoundsRows.add(new GUIPropertyInt("Y Pos", 0, 0, Window.framebufferHeight(), 10, 1));
                    this.propY = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : Window.framebufferHeight() - this.target.size.y);
                    intProp    = treeBoundsRows.add(new GUIPropertyInt("Width", 0, 0, Window.framebufferWidth(), 10, 1));
                    this.propW = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : Window.framebufferWidth() - this.target.pos.x);
                    intProp    = treeBoundsRows.add(new GUIPropertyInt("Height", 0, 0, Window.framebufferHeight(), 10, 1));
                    this.propH = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : Window.framebufferHeight() - this.target.pos.y);
                    
                    GUITree treeContentBounds = new GUITree(GUITree.Type.TAB, "Content Bounds");
                    {
                        GUILayout treeContentBoundsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
                        {
                            treeContentBoundsRows.add(new GUILabel(() -> "Content Pos:   " + (this.target == null ? "NULL" : this.target.contentPos)));
                            treeContentBoundsRows.add(new GUILabel(() -> "Content Size:  " + (this.target == null ? "NULL" : this.target.contentSize)));
                        }
                        treeContentBounds.add(treeContentBoundsRows);
                    }
                    treeBoundsRows.add(treeContentBounds);
                }
                treeBounds.add(treeBoundsRows);
            }
            rows.add(treeBounds);
            
            GUITree treeScroll = new GUITree(GUITree.Type.TAB, "Scroll");
            {
                GUILayout treeScrollRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
                {
                    GUIPropertyInt intProp;
                    
                    intProp     = treeScrollRows.add(new GUIPropertyInt("X Scroll", 0, 0, 0, 10, 1));
                    this.propSX = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : this.target.size.x);
                    intProp     = treeScrollRows.add(new GUIPropertyInt("Y Scroll", 0, 0, 0, 10, 1));
                    this.propSY = intProp.value;
                    intProp.max.set(() -> this.target == null ? 0 : this.target.size.y);
                }
                treeScroll.add(treeScrollRows);
            }
            rows.add(treeScroll);
            
            GUITree treeState = new GUITree(GUITree.Type.TAB, "State");
            {
                GUILayout treeStateRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
                {
                    treeStateRows.add(new GUILabel(() -> "Focused: " + (this.target == null ? "NULL" : this.target.focused())));
                    treeStateRows.add(new GUILabel(() -> "Hovered: " + (this.target == null ? "NULL" : this.target.hovered())));
                }
                treeState.add(treeStateRows);
            }
            rows.add(treeState);
            
            GUITree treeFlags = new GUITree(GUITree.Type.TAB, "State");
            {
                GUILayout treeFlagsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
                {
                    for (WindowFlag flag : WindowFlag.values())
                    {
                        treeFlagsRows.add(new GUILabel(() -> String.format("%s: %s", flag.name(), this.target == null ? "NULL" : this.target.isEnabled(flag))));
                    }
                }
                treeFlags.add(treeFlagsRows);
            }
            rows.add(treeFlags);
        }
        add(rows);
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        if (this.target != null)
        {
            this.propX.set(this.target.pos.x);
            this.propY.set(this.target.pos.y);
            this.propW.set(this.target.size.x);
            this.propH.set(this.target.size.y);
            this.propSX.set(this.target.scroll.x);
            this.propSY.set(this.target.scroll.y);
        }
        super.layout(ctx);
        if (this.target != null)
        {
            this.target.pos.x    = this.propX.get();
            this.target.pos.y    = this.propY.get();
            this.target.size.x   = this.propW.get();
            this.target.size.y   = this.propH.get();
            this.target.scroll.x = this.propSX.get();
            this.target.scroll.y = this.propSY.get();
        }
    }
}
