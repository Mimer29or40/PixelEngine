package pe;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryUtil;
import pe.font.FontSingle;
import pe.font.Weight;
import rutils.IOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.stb.STBTruetype.stbtt_GetFontNameString;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static rutils.IOUtil.getPath;

public class TrueTypeStats
{
    public static void main(String[] args) throws IOException
    {
        loadFonts("demo/FiraSans");
        loadFonts("demo/ProggyClean");
        loadFonts("font/PressStart2P");
    }
    
    private static void loadFonts(String fileDir) throws IOException
    {
        Path dirPath = getPath(fileDir);
        
        Files.list(dirPath).forEach(path -> {
            String fileName = path.getFileName().toString();
            // if (fileName.startsWith(name) && fileName.endsWith(".ttf"))
            if (fileName.endsWith(".ttf"))
            {
                System.out.println("fileName = " + path);
                loadFont(path.toString());
            }
        });
    }
    
    private static void loadFont(String filePath)
    {
        STBTTFontinfo info     = STBTTFontinfo.malloc();
        ByteBuffer    fileData = IOUtil.readFromFile(filePath);
        
        if (fileData == null || !stbtt_InitFont(info, fileData)) throw new RuntimeException("Font Data could not be loaded: " + filePath);
        
        String fontFamilyName    = nameString(info, 1);  // Font Family name
        String fontSubfamilyName = nameString(info, 2);  // Font Subfamily name
        String typoFamilyName    = nameString(info, 16); // Typographic Family name
        String typoSubfamilyName = nameString(info, 17); // Typographic Subfamily name
        
        String family = typoFamilyName != null ? typoFamilyName : fontFamilyName != null ? fontFamilyName : "Provided Name";
        
        String  subfamily  = (typoSubfamilyName != null ? typoSubfamilyName : fontSubfamilyName != null ? fontSubfamilyName : "Provided Subfamily").toLowerCase();
        Weight  weight     = Weight.get(subfamily);
        boolean italicized = subfamily.contains("italic");
        
        // System.out.println("    fontFamilyName    = " + fontFamilyName);
        // System.out.println("    fontSubfamilyName = " + fontSubfamilyName);
        // System.out.println("    typoFamilyName    = " + typoFamilyName);
        // System.out.println("    typoSubfamilyName = " + typoSubfamilyName);
        System.out.println("    family            = " + family);
        // System.out.println("    subfamily         = " + subfamily);
        System.out.println("    weight            = " + weight);
        System.out.println("    italicized        = " + italicized);
        
        // for (int i = 0; i < Short.MAX_VALUE; i++)
        // {
        //     String value = nameString(info, i);
        //     if (value != null) System.out.println(i + " - " + value);
        // }
        
        info.free();
        MemoryUtil.memFree(fileData);
    }
    
    private static @Nullable String nameString(STBTTFontinfo info, int nameID)
    {
        ByteBuffer value = stbtt_GetFontNameString(info, FontSingle.PLATFORM_ID, FontSingle.ENCODING_ID, FontSingle.LANGUAGE_ID, nameID);
        if (value != null)
        {
            ByteBuffer string = MemoryUtil.memAlloc(value.order(ByteOrder.BIG_ENDIAN).capacity() >> 1);
            while (value.hasRemaining())
            {
                value.get();
                string.put(value.get());
            }
            string.flip();
            String result = MemoryUtil.memUTF8(string);
            MemoryUtil.memFree(string);
            return result;
        }
        return null;
    }
}
