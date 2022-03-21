package pe;

import org.lwjgl.system.MemoryStack;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.texture.Image;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class AnimatedGifEncoder - Encodes a GIF file consisting of one or
 * more frames.
 * <pre>
 * Example:
 *    AnimatedGifEncoder e = new AnimatedGifEncoder();
 *    e.start(outputFileName);
 *    e.setDelay(1000);   // 1 frame per sec
 *    e.addFrame(image1);
 *    e.addFrame(image2);
 *    e.finish();
 * </pre>
 * No copyright asserted on the source code of this class.  May be used
 * for any purpose, however, refer to the Unisys LZW patent for restrictions
 * on use of the associated LZWEncoder class.  Please forward any corrections
 * to questions at fmsware.com.
 *
 * @author Kevin Weiner, FM Software
 * @version 1.03 November 2003
 */

public class GIFEncoder
{
    // -------------------- Per Gif State -------------------- //
    
    protected OutputStream stream;
    
    protected boolean started     = false;
    protected boolean shouldClose = false;
    protected boolean firstFrame  = true;
    
    protected int     width   = 0;
    protected int     height  = 0;
    protected boolean sizeSet = false;
    protected int     repeat  = 0;
    
    // -------------------- Per Frame State -------------------- //
    
    protected int dispose = -1;
    protected int quality = 10;
    
    protected int     transparentColor = -1;
    protected boolean transparentExact = false;
    protected int     backgroundColor  = 0;
    
    // -------------------- Per Gif Methods -------------------- //
    
    /**
     * Initiates GIF file creation on the given stream with the given size and
     * will repeat the given amount of times, or indefinitely. The stream is
     * not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     * @return false if initial write failed.
     */
    public boolean start(OutputStream stream, int width, int height, int repeat)
    {
        try
        {
            this.stream = stream;
            
            this.started     = true;
            this.shouldClose = false;
            
            writeString("GIF89a"); // header
            
            this.width   = width < 1 ? 256 : width;
            this.height  = height < 1 ? 256 : height;
            this.sizeSet = true;
            this.repeat  = Math.max(repeat, 0);
        }
        catch (IOException e)
        {
            this.started = false;
        }
        
        return this.started;
    }
    
    /**
     * Initiates GIF file creation on the given stream with the given size and
     * will repeat indefinitely. The stream is not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @return false if initial write failed.
     */
    public boolean start(OutputStream stream, int width, int height)
    {
        return start(stream, width, height, -1);
    }
    
    /**
     * Initiates GIF file creation on the given stream which will repeat the
     * given amount of times, or indefinitely. The size will be that of the
     * first frame. The stream is not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     * @return false if initial write failed.
     */
    public boolean start(OutputStream stream, int repeat)
    {
        boolean result = start(stream, -1, -1, repeat);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates GIF file creation on the given stream which will repeat
     * indefinitely. The size will be that of the first frame. The stream is
     * not closed automatically.
     *
     * @param stream OutputStream on which GIF images are written.
     * @return false if initial write failed.
     */
    public boolean start(OutputStream stream)
    {
        boolean result = start(stream, -1, -1, -1);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name with the given
     * size and will repeat the given amount of times, or indefinitely.
     *
     * @param file   String containing output file name.
     * @param width  The width in pixels.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     * @param height The height in pixels.
     * @return false if open or initial write failed.
     */
    public boolean start(String file, int width, int height, int repeat)
    {
        try
        {
            boolean result = start(new BufferedOutputStream(new FileOutputStream(file)), width, height, repeat);
            this.shouldClose = true;
            return result;
        }
        catch (IOException ignored) {}
        return false;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name with the given
     * size and will repeat indefinitely.
     *
     * @param file   String containing output file name.
     * @param width  The width in pixels.
     * @param height The height in pixels.
     * @return false if open or initial write failed.
     */
    public boolean start(String file, int width, int height)
    {
        return start(file, width, height, -1);
    }
    
    /**
     * Initiates writing of a GIF file with the specified name which will
     * repeat the given amount of times, or indefinitely. The size will be that
     * of the first frame.
     *
     * @param file   String containing output file name.
     * @param repeat The number of times to repeat, or 0 for indefinitely.
     * @return false if open or initial write failed.
     */
    public boolean start(String file, int repeat)
    {
        boolean result = start(file, -1, -1, repeat);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Initiates writing of a GIF file with the specified name which will
     * repeat indefinitely. The size will be that of the first frame.
     *
     * @param file String containing output file name.
     * @return false if open or initial write failed.
     */
    public boolean start(String file)
    {
        boolean result = start(file, -1, -1, -1);
        this.sizeSet = false;
        return result;
    }
    
    /**
     * Adds next GIF frame.  The frame is not written immediately, but is
     * actually deferred until the next frame is received so that timing
     * data can be inserted.  Invoking <code>finish()</code> flushes all
     * frames.  If <code>setSize</code> was not invoked, the size of the
     * first image is used for all subsequent frames.
     *
     * @param image Image to write.
     * @param delay The time, in milliseconds, before the succeeding frame will be shown
     * @return true if successful.
     */
    public boolean addFrame(Image image, int delay)
    {
        if (image == null || !this.started) return false;
        
        try
        {
            if (!this.sizeSet)
            {
                // use first frame's size
                this.width   = image.width();
                this.height  = image.height();
                this.sizeSet = true;
            }
            
            // convert to correct format if necessary
            byte[] pixels = getImagePixels(image);
            
            // build color table & map pixels
            AnalyzeResults results = analyzePixels(pixels);
            
            if (this.firstFrame)
            {
                // logical screen descriptior
                writeLSD(results.palletSize());
                
                // global color table
                writePalette(results.colorTable());
                
                // use NS app extension to indicate reps
                if (this.repeat >= 0) writeNetscapeExt();
            }
            
            // write graphic control extension
            writeGraphicCtrlExt(delay, results.transparentIndex());
            
            // image descriptor
            writeImageDesc(results.palletSize());
            
            // local color table
            if (!this.firstFrame) writePalette(results.colorTable());
            
            // encode and write pixel data
            LZWEncoder encoder = new LZWEncoder(this.width, this.height, results.pixels(), results.colorDepth());
            encoder.encode(this.stream);
            
            this.firstFrame = false;
            
            return true;
        }
        catch (IOException ignored) {}
        
        return false;
    }
    
    /**
     * Flushes any pending data and closes output file.
     * If writing to an OutputStream, the stream is not
     * closed.
     */
    public boolean finish()
    {
        if (!this.started) return false;
        
        boolean ok = true;
        try
        {
            // gif trailer
            write(0x3B);
            flush();
            if (this.shouldClose) close();
        }
        catch (IOException e)
        {
            ok = false;
        }
        
        // reset for subsequent use
        this.stream = null;
        
        this.started     = false;
        this.shouldClose = false;
        this.firstFrame  = true;
        
        this.width   = 0;
        this.height  = 0;
        this.sizeSet = false;
        this.repeat  = 0;
        
        this.dispose = -1;
        this.quality = 10;
        
        this.transparentColor = -1;
        this.transparentExact = false;
        this.backgroundColor  = 0;
        
        return ok;
    }
    
    // -------------------- Per Frame Methods -------------------- //
    
    /**
     * Sets the GIF frame disposal code for the last added frame
     * and any subsequent frames.  Default is 0 if no transparent
     * color has been set, otherwise 2.
     *
     * @param code int disposal code.
     */
    public void disposalCode(int code)
    {
        this.dispose = Math.max(code, 0);
    }
    
    /**
     * Sets quality of color quantization (conversion of images
     * to the maximum 256 colors allowed by the GIF specification).
     * Lower values (minimum = 1) produce better colors, but slow
     * processing significantly.  10 is the default, and produces
     * good color mapping at reasonable speeds.  Values greater
     * than 20 do not yield significant improvements in speed.
     *
     * @param quality int greater than 0.
     */
    public void quality(int quality)
    {
        this.quality = Math.max(quality, 1);
    }
    
    /**
     * Sets the transparent color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the quantization
     * process, the color in the final palette for each frame closest to the
     * given color becomes the transparent color for that frame. If exactMatch
     * is set to true, transparent color index is search with exact match, and
     * not looking for the closest one.
     *
     * @param r     The r component of the color to be treated as transparent on display.
     * @param g     The g component of the color to be treated as transparent on display.
     * @param b     The b component of the color to be treated as transparent on display.
     * @param exact If only the exact color should be used.
     */
    public void transparentColor(int r, int g, int b, boolean exact)
    {
        this.transparentColor = toInt(r, g, b);
        this.transparentExact = exact;
    }
    
    /**
     * Sets the transparent color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the quantization
     * process, the color in the final palette for each frame closest to the
     * given color becomes the transparent color for that frame.
     *
     * @param r The r component of the color to be treated as transparent on display.
     * @param g The g component of the color to be treated as transparent on display.
     * @param b The b component of the color to be treated as transparent on display.
     */
    public void transparentColor(int r, int g, int b)
    {
        transparentColor(r, g, b, false);
    }
    
    /**
     * Removes the transparent color for the last added frame and any subsequent
     * frames.
     */
    public void removeTransparentColor()
    {
        this.transparentColor = -1;
    }
    
    /**
     * Sets the background color for the last added frame and any subsequent
     * frames. Since all colors are subject to modification in the
     * quantization process, the color in the final palette for each frame
     * closest to the given color becomes the background color for that frame.
     *
     * @param r The r component of the color to be treated as background on display.
     * @param g The g component of the color to be treated as background on display.
     * @param b The b component of the color to be treated as background on display.
     */
    public void backgroundColor(int r, int g, int b)
    {
        this.backgroundColor = toInt(r, g, b);
    }
    
    /**
     * Resets the background color to black for the last added frame and any
     * subsequent frames.
     */
    public void removeBackgroundColor()
    {
        this.backgroundColor = 0;
    }
    
    /**
     * Extracts image pixels into byte array "pixels"
     */
    protected byte[] getImagePixels(Image image)
    {
        int w = image.width();
        int h = image.height();
        
        ColorFormat type = image.format();
        
        Color.Buffer data   = Objects.requireNonNull(image.data());
        boolean      delete = false;
        if (w != this.width || h != this.height || type != ColorFormat.RGB)
        {
            // create new image with right size/format
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                Color background = Color.malloc(stack);
                background.setFromInt(this.backgroundColor);
                
                Image temp = Image.genColor(this.width, this.height, background);
                temp.drawImage(image, 0, 0, w, h, 0, 0, this.width, this.height);
                data = Objects.requireNonNull(temp.data()).copy(ColorFormat.RGB);
                temp.delete();
                delete = true;
            }
        }
        
        byte[] pixels = new byte[data.remaining() * data.sizeof()];
        for (int i = 0; data.hasRemaining(); )
        {
            Color color = data.get();
            pixels[i++] = (byte) color.b();
            pixels[i++] = (byte) color.g();
            pixels[i++] = (byte) color.r();
        }
        if (delete) data.free();
        
        return pixels;
    }
    
    /**
     * Analyzes image colors and creates color map.
     */
    protected AnalyzeResults analyzePixels(byte[] rawPixels)
    {
        int          len    = rawPixels.length;
        int          nPix   = len / 3;
        final byte[] pixels = new byte[nPix];
        
        // initialize quantizer
        NeuQuant nq = new NeuQuant(rawPixels, len, this.quality);
        
        final byte[]    colorTable = nq.process(); // create reduced palette
        final boolean[] usedEntry  = new boolean[256];
        // convert map from BGR to RGB
        for (int i = 0; i < colorTable.length; i += 3)
        {
            byte temp = colorTable[i];
            colorTable[i]     = colorTable[i + 2];
            colorTable[i + 2] = temp;
            usedEntry[i / 3]  = false;
        }
        // map image pixels to new palette
        int k = 0;
        for (int i = 0; i < nPix; i++)
        {
            int index = nq.map(rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF);
            pixels[i]        = (byte) index;
            usedEntry[index] = true;
        }
        final int colorDepth = 8;
        final int palletSize = 7;
        
        // get the closest match to transparent color if specified
        final int transparentIndex;
        if (this.transparentColor > 0)
        {
            if (this.transparentExact)
            {
                transparentIndex = findExact(this.transparentColor, colorTable, usedEntry);
            }
            else
            {
                transparentIndex = findClosest(this.transparentColor, colorTable, usedEntry);
            }
        }
        else
        {
            transparentIndex = 0;
        }
        return new AnalyzeResults()
        {
            @Override
            public byte[] pixels()
            {
                return pixels;
            }
            
            @Override
            public byte[] colorTable()
            {
                return colorTable;
            }
            
            @Override
            public int colorDepth()
            {
                return colorDepth;
            }
            
            @Override
            public int palletSize()
            {
                return palletSize;
            }
            
            @Override
            public int transparentIndex()
            {
                return transparentIndex;
            }
        };
    }
    
    // -------------------- Custom Write Methods -------------------- //
    
    /**
     * Writes Graphic Control Extension
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeGraphicCtrlExt(int delay, int transIndex) throws IOException
    {
        // extension introducer
        write(0x21);
        
        // GCE label
        write(0xF9);
        
        // data block size
        write(4);
        
        int transparency, dispose;
        if (this.transparentColor < 0)
        {
            transparency = 0;
            dispose      = 0; // dispose = no action
        }
        else
        {
            transparency = 1;
            dispose      = 2; // force clear if using transparent color
        }
        
        // user override
        if (this.dispose >= 0) dispose = this.dispose & 7;
        
        // packed fields
        write((((0 & 0b0111) << 5) |       // 1:3 reserved
               ((dispose & 0b0111) << 2) | // 4:6 disposal
               ((0 & 0b0001) << 1) |       // 7   user input - 0 = none
               (transparency & 0b0001)));  // 8   transparency flag
        
        writeShort(delay / 10); // delay x 1/100 sec
        write(transIndex); // transparent color index
        write(0); // block terminator
    }
    
    /**
     * Writes Image Descriptor
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeImageDesc(int palletSize) throws IOException
    {
        // image separator
        write(0x2C);
        
        // image position x,y = 0,0
        writeShort(0);
        writeShort(0);
        
        // image size
        writeShort(this.width);
        writeShort(this.height);
        
        // packed fields
        if (this.firstFrame)
        {
            // no LCT  - GCT is used for first (or only) frame
            write(0);
        }
        else
        {
            // specify normal LCT
            write((((1 & 0b0001) << 7) |    // 1 local color table  1=yes
                   ((0 & 0b0001) << 6) |    // 2 interlace - 0=no
                   ((0 & 0b0001) << 5) |    // 3 sorted - 0=no
                   ((0 & 0b0011) << 3) |    // 4-5 reserved
                   (palletSize & 0b0111))); // 6-8 size of color table
        }
    }
    
    /**
     * Writes Logical Screen Descriptor
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    protected void writeLSD(int palletSize) throws IOException
    {
        // logical screen size
        writeShort(this.width);
        writeShort(this.height);
        
        // packed fields
        write((((1 & 0b0001) << 7) |    // 1  : global color table flag = 1 (gct used)
               ((7 & 0b0111) << 4) |    // 2-4: color resolution = 7
               ((0 & 0b0001) << 3) |    // 5  : gct sort flag = 0
               (palletSize & 0b0111))); // 6-8: gct size
        
        // background color index
        write(0);
        
        // pixel aspect ratio - assume 1:1
        write(0);
    }
    
    /**
     * Writes Netscape application extension to define
     * repeat count.
     */
    protected void writeNetscapeExt() throws IOException
    {
        // extension introducer
        write(0x21);
        
        // app extension label
        write(0xFF);
        
        // block size
        write(11);
        
        // app id + auth code
        writeString("NETSCAPE" + "2.0");
        
        // sub-block size
        write(3);
        
        // loop sub-block id
        write(1);
        
        // loop count (extra iterations, 0=repeat forever)
        writeShort(this.repeat);
        
        // block terminator
        write(0);
    }
    
    /**
     * Writes color table
     */
    protected void writePalette(byte[] colorTab) throws IOException
    {
        write(colorTab);
        int n = (3 * 256) - colorTab.length;
        for (int i = 0; i < n; i++) write(0);
    }
    
    /**
     * Write 16-bit value to output stream, LSB first
     */
    protected void writeShort(int value) throws IOException
    {
        write(value & 0xFF);
        write((value >> 8) & 0xFF);
    }
    
    /**
     * Writes string to output stream
     */
    protected void writeString(String s) throws IOException
    {
        for (int i = 0; i < s.length(); i++) write((byte) s.charAt(i));
    }
    
    // -------------------- Stream Methods -------------------- //
    
    protected void write(int b) throws IOException
    {
        this.stream.write(b);
    }
    
    protected void write(byte[] b) throws IOException
    {
        this.stream.write(b);
    }
    
    protected void flush() throws IOException
    {
        this.stream.flush();
    }
    
    protected void close() throws IOException
    {
        this.stream.close();
    }
    
    // -------------------- Utility Methods -------------------- //
    
    /**
     * Returns true if the exact matching color is existing, and used in the
     * color palette, otherwise, return false. This method has to be called
     * before finishing the image, because after finished the palette is
     * destroyed, and it will always return false.
     */
    protected static boolean isColorUsed(int color, byte[] colorTable, boolean[] usedEntry)
    {
        return findExact(color, colorTable, usedEntry) != -1;
    }
    
    /**
     * Returns index of palette exactly matching to color c or -1 if there is no exact matching.
     */
    protected static int findExact(int color, byte[] colorTable, boolean[] usedEntry)
    {
        if (colorTable == null) return -1;
        
        int[] c = fromInt(color);
        
        int r = c[0];
        int g = c[1];
        int b = c[2];
        for (int i = 0, n = colorTable.length / 3; i < n; ++i)
        {
            int index = i * 3;
            // If the entry is used in colorTab, then check if it is the same exact color we're looking for
            if (usedEntry[i] &&
                r == (colorTable[index] & 0xFF) &&
                g == (colorTable[index + 1] & 0xFF) &&
                b == (colorTable[index + 2] & 0xFF))
            {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns index of palette color closest to c
     */
    protected static int findClosest(int color, byte[] colorTable, boolean[] usedEntry)
    {
        if (colorTable == null) return -1;
        
        int[] c = fromInt(color);
        
        int r = c[0];
        int g = c[1];
        int b = c[2];
        
        int minpos = 0;
        int dmin   = Integer.MAX_VALUE;
        for (int i = 0, n = colorTable.length / 3; i < n; ++i)
        {
            int index = i * 3;
            
            int dr = r - (colorTable[index] & 0xFF);
            int dg = g - (colorTable[index + 1] & 0xFF);
            int db = b - (colorTable[index + 2] & 0xFF);
            int d  = dr * dr + dg * dg + db * db;
            
            if (usedEntry[i] && (d < dmin))
            {
                dmin   = d;
                minpos = i;
            }
        }
        return minpos;
    }
    
    protected static int toInt(int r, int g, int b)
    {
        return ((r << 16) & 0xFF) | ((g << 8) & 0xFF) | ((b) & 0xFF);
    }
    
    protected static int[] fromInt(int c)
    {
        return new int[] {(c >> 16) & 0xFF, (c >> 8) & 0xFF, (c) & 0xFF};
    }
    
    private interface AnalyzeResults
    {
        byte[] pixels();
        
        byte[] colorTable();
        
        int colorDepth();
        
        int palletSize();
        
        int transparentIndex();
    }
    
    private static class LZWEncoder
    {
        private static final int EOF = -1;
        
        private int imgW, imgH;
        private byte[] pixAry;
        private int    initCodeSize;
        private int    remaining;
        private int    curPixel;
        
        // GIFCOMPR.C       - GIF Image compression routines
        //
        // Lempel-Ziv compression based on 'compress'.  GIF modifications by
        // David Rowley (mgardi@watdcsu.waterloo.edu)
        
        // General DEFINEs
        
        static final int BITS = 12;
        
        static final int HSIZE = 5003; // 80% occupancy
        
        // GIF Image compression - modified 'compress'
        //
        // Based on: compress.c - File compression ala IEEE Computer, June 1984.
        //
        // By Authors:  Spencer W. Thomas      (decvax!harpo!utah-cs!utah-gr!thomas)
        //              Jim McKie              (decvax!mcvax!jim)
        //              Steve Davies           (decvax!vax135!petsd!peora!srd)
        //              Ken Turkowski          (decvax!decwrl!turtlevax!ken)
        //              James A. Woods         (decvax!ihnp4!ames!jaw)
        //              Joe Orost              (decvax!vax135!petsd!joe)
        
        int n_bits; // number of bits/code
        int maxbits    = BITS; // user settable max # bits/code
        int maxcode; // maximum code, given n_bits
        int maxmaxcode = 1 << BITS; // should NEVER generate this code
        
        int[] htab    = new int[HSIZE];
        int[] codetab = new int[HSIZE];
        
        int hsize = HSIZE; // for dynamic table sizing
        
        int free_ent = 0; // first unused entry
        
        // block compression parameters -- after all codes are used up,
        // and compression rate changes, start over.
        boolean clear_flg = false;
        
        // Algorithm:  use open addressing double hashing (no chaining) on the
        // prefix code / next character combination.  We do a variant of Knuth's
        // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
        // secondary probe.  Here, the modular division first probe is gives way
        // to a faster exclusive-or manipulation.  Also do block compression with
        // an adaptive reset, whereby the code table is cleared when the compression
        // ratio decreases, but after the table fills.  The variable-length output
        // codes are re-sized at this point, and a special CLEAR code is generated
        // for the decompressor.  Late addition:  construct the table according to
        // file size for noticeable speed improvement on small files.  Please direct
        // questions about this implementation to ames!jaw.
        
        int g_init_bits;
        
        int ClearCode;
        int EOFCode;
        
        // output
        //
        // Output the given code.
        // Inputs:
        //      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
        //              that n_bits =< wordsize - 1.
        // Outputs:
        //      Outputs code to the file.
        // Assumptions:
        //      Chars are 8 bits long.
        // Algorithm:
        //      Maintain a BITS character long buffer (so that 8 codes will
        // fit in it exactly).  Use the VAX insv instruction to insert each
        // code in turn.  When the buffer fills up empty it and start over.
        
        int cur_accum = 0;
        int cur_bits  = 0;
        
        int[] masks = {
                0x0000,
                0x0001, 0x0003, 0x0007, 0x000F,
                0x001F, 0x003F, 0x007F, 0x00FF,
                0x01FF, 0x03FF, 0x07FF, 0x0FFF,
                0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
        };
        
        // Number of characters so far in this 'packet'
        int a_count;
        
        // Define the storage for the packet accumulator
        byte[] accum = new byte[256];
        
        //----------------------------------------------------------------------------
        LZWEncoder(int width, int height, byte[] pixels, int color_depth)
        {
            imgW         = width;
            imgH         = height;
            pixAry       = pixels;
            initCodeSize = Math.max(2, color_depth);
        }
        
        // Add a character to the end of the current packet, and if it is 254
        // characters, flush the packet to disk.
        void char_out(byte c, OutputStream outs) throws IOException
        {
            accum[a_count++] = c;
            if (a_count >= 254) flush_char(outs);
        }
        
        void compress(int init_bits, OutputStream outs) throws IOException
        {
            int fcode;
            int i /* = 0 */;
            int c;
            int ent;
            int disp;
            int hsize_reg;
            int hshift;
            
            // Set up the globals:  g_init_bits - initial number of bits
            g_init_bits = init_bits;
            
            // Set up the necessary values
            clear_flg = false;
            n_bits    = g_init_bits;
            maxcode   = MAXCODE(n_bits);
            
            ClearCode = 1 << (init_bits - 1);
            EOFCode   = ClearCode + 1;
            free_ent  = ClearCode + 2;
            
            a_count = 0; // clear packet
            
            ent = nextPixel();
            
            hshift = 0;
            for (fcode = hsize; fcode < 65536; fcode *= 2) ++hshift;
            hshift = 8 - hshift; // set hash code range bound
            
            hsize_reg = hsize;
            Arrays.fill(htab, -1); // clear hash table
            
            output(ClearCode, outs);
            
            outer_loop:
            while ((c = nextPixel()) != EOF)
            {
                fcode = (c << maxbits) + ent;
                i     = (c << hshift) ^ ent; // xor hashing
                
                if (htab[i] == fcode)
                {
                    ent = codetab[i];
                    continue;
                }
                else if (htab[i] >= 0) // non-empty slot
                {
                    disp = hsize_reg - i; // secondary hash (after G. Knott)
                    if (i == 0) disp = 1;
                    do
                    {
                        if ((i -= disp) < 0)
                        {i += hsize_reg;}
                        
                        if (htab[i] == fcode)
                        {
                            ent = codetab[i];
                            continue outer_loop;
                        }
                    } while (htab[i] >= 0);
                }
                output(ent, outs);
                ent = c;
                if (free_ent < maxmaxcode)
                {
                    codetab[i] = free_ent++; // code -> hashtable
                    htab[i]    = fcode;
                }
                else
                {
                    Arrays.fill(htab, -1);
                    free_ent  = ClearCode + 2;
                    clear_flg = true;
                    
                    output(ClearCode, outs);
                }
            }
            // Put out the final code.
            output(ent, outs);
            output(EOFCode, outs);
        }
        
        //----------------------------------------------------------------------------
        void encode(OutputStream os) throws IOException
        {
            os.write(initCodeSize); // write "initial code size" byte
            
            remaining = imgW * imgH; // reset navigation variables
            curPixel  = 0;
            
            compress(initCodeSize + 1, os); // compress and write the pixel data
            
            os.write(0); // write block terminator
        }
        
        // Flush the packet to disk, and reset the accumulator
        void flush_char(OutputStream outs) throws IOException
        {
            if (a_count > 0)
            {
                outs.write(a_count);
                outs.write(accum, 0, a_count);
                a_count = 0;
            }
        }
        
        final int MAXCODE(int n_bits)
        {
            return (1 << n_bits) - 1;
        }
        
        //----------------------------------------------------------------------------
        // Return the next pixel from the image
        //----------------------------------------------------------------------------
        private int nextPixel()
        {
            if (remaining == 0) return EOF;
            
            --remaining;
            
            byte pix = pixAry[curPixel++];
            
            return pix & 0xFF;
        }
        
        void output(int code, OutputStream outs) throws IOException
        {
            cur_accum &= masks[cur_bits];
            
            if (cur_bits > 0)
            {cur_accum |= (code << cur_bits);}
            else
            {cur_accum = code;}
            
            cur_bits += n_bits;
            
            while (cur_bits >= 8)
            {
                char_out((byte) (cur_accum & 0xFF), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }
            
            // If the next entry is going to be too big for the code size,
            // then increase it, if possible.
            if (free_ent > maxcode || clear_flg)
            {
                if (clear_flg)
                {
                    maxcode   = MAXCODE(n_bits = g_init_bits);
                    clear_flg = false;
                }
                else
                {
                    ++n_bits;
                    if (n_bits == maxbits)
                    {maxcode = maxmaxcode;}
                    else
                    {maxcode = MAXCODE(n_bits);}
                }
            }
            
            if (code == EOFCode)
            {
                // At EOF, write the rest of the buffer.
                while (cur_bits > 0)
                {
                    char_out((byte) (cur_accum & 0xFF), outs);
                    cur_accum >>= 8;
                    cur_bits -= 8;
                }
                
                flush_char(outs);
            }
        }
    }
    
    private static class NeuQuant
    {
        
        protected static final int netsize = 256; /* number of colours used */
        
        /* four primes near 500 - assume no image has a length so large */
        /* that it is divisible by all four primes */
        protected static final int prime1 = 499;
        protected static final int prime2 = 491;
        protected static final int prime3 = 487;
        protected static final int prime4 = 503;
        
        protected static final int minpicturebytes = (3 * prime4);
        /* minimum size for input image */

	    /* Program Skeleton
        ----------------
        [select samplefac in range 1..30]
        [read image from input file]
        pic = (unsigned char*) malloc(3*width*height);
        initnet(pic,3*width*height,samplefac);
        learn();
        unbiasnet();
        [write output image header, using writecolourmap(f)]
        inxbuild();
        write output image using inxsearch(b,g,r)      */

	    /* Network Definitions
        ------------------- */
        
        protected static final int maxnetpos    = (netsize - 1);
        protected static final int netbiasshift = 4; /* bias for colour values */
        protected static final int ncycles      = 100; /* no. of learning cycles */
        
        /* defs for freq and bias */
        protected static final int intbiasshift = 16; /* bias for fractions */
        protected static final int intbias      = (((int) 1) << intbiasshift);
        protected static final int gammashift   = 10; /* gamma = 1024 */
        protected static final int gamma        = (((int) 1) << gammashift);
        protected static final int betashift    = 10;
        protected static final int beta         = (intbias >> betashift); /* beta = 1/1024 */
        protected static final int betagamma    =
                (intbias << (gammashift - betashift));
        
        /* defs for decreasing radius factor */
        protected static final int initrad         = (netsize >> 3); /* for 256 cols, radius starts */
        protected static final int radiusbiasshift = 6; /* at 32.0 biased by 6 bits */
        protected static final int radiusbias      = (((int) 1) << radiusbiasshift);
        protected static final int initradius      = (initrad * radiusbias); /* and decreases by a */
        protected static final int radiusdec       = 30; /* factor of 1/30 each cycle */
        
        /* defs for decreasing alpha factor */
        protected static final int alphabiasshift = 10; /* alpha starts at 1.0 */
        protected static final int initalpha      = (((int) 1) << alphabiasshift);
        
        protected int alphadec; /* biased by 10 bits */
        
        /* radbias and alpharadbias used for radpower calculation */
        protected static final int radbiasshift   = 8;
        protected static final int radbias        = (((int) 1) << radbiasshift);
        protected static final int alpharadbshift = (alphabiasshift + radbiasshift);
        protected static final int alpharadbias   = (((int) 1) << alpharadbshift);

    	/* Types and Global Variables
	-------------------------- */
        
        protected byte[] thepicture; /* the input image itself */
        protected int    lengthcount; /* lengthcount = H*W*3 */
        
        protected int samplefac; /* sampling factor 1..30 */
        
        //   typedef int pixel[4];                /* BGRc */
        protected int[][] network; /* the network itself - [netsize][4] */
        
        protected int[] netindex = new int[256];
        /* for network lookup - really 256 */
        
        protected int[] bias     = new int[netsize];
        /* bias and freq arrays for learning */
        protected int[] freq     = new int[netsize];
        protected int[] radpower = new int[initrad];
        /* radpower for precomputation */
        
        /* Initialise network in range (0,0,0) to (255,255,255) and set parameters
           ----------------------------------------------------------------------- */
        public NeuQuant(byte[] thepic, int len, int sample)
        {
            
            int   i;
            int[] p;
            
            thepicture  = thepic;
            lengthcount = len;
            samplefac   = sample;
            
            network = new int[netsize][];
            for (i = 0; i < netsize; i++)
            {
                network[i] = new int[4];
                p          = network[i];
                p[0]       = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
                freq[i]    = intbias / netsize; /* 1/netsize */
                bias[i]    = 0;
            }
        }
        
        public byte[] colorMap()
        {
            byte[] map   = new byte[3 * netsize];
            int[]  index = new int[netsize];
            for (int i = 0; i < netsize; i++)
            {index[network[i][3]] = i;}
            int k = 0;
            for (int i = 0; i < netsize; i++)
            {
                int j = index[i];
                map[k++] = (byte) (network[j][0]);
                map[k++] = (byte) (network[j][1]);
                map[k++] = (byte) (network[j][2]);
            }
            return map;
        }
        
        /* Insertion sort of network and building of netindex[0..255] (to do after unbias)
           ------------------------------------------------------------------------------- */
        public void inxbuild()
        {
            
            int   i, j, smallpos, smallval;
            int[] p;
            int[] q;
            int   previouscol, startpos;
            
            previouscol = 0;
            startpos    = 0;
            for (i = 0; i < netsize; i++)
            {
                p        = network[i];
                smallpos = i;
                smallval = p[1]; /* index on g */
                /* find smallest in i..netsize-1 */
                for (j = i + 1; j < netsize; j++)
                {
                    q = network[j];
                    if (q[1] < smallval)
                    { /* index on g */
                        smallpos = j;
                        smallval = q[1]; /* index on g */
                    }
                }
                q = network[smallpos];
                /* swap p (i) and q (smallpos) entries */
                if (i != smallpos)
                {
                    j    = q[0];
                    q[0] = p[0];
                    p[0] = j;
                    j    = q[1];
                    q[1] = p[1];
                    p[1] = j;
                    j    = q[2];
                    q[2] = p[2];
                    p[2] = j;
                    j    = q[3];
                    q[3] = p[3];
                    p[3] = j;
                }
                /* smallval entry is now in position i */
                if (smallval != previouscol)
                {
                    netindex[previouscol] = (startpos + i) >> 1;
                    for (j = previouscol + 1; j < smallval; j++)
                    {netindex[j] = i;}
                    previouscol = smallval;
                    startpos    = i;
                }
            }
            netindex[previouscol] = (startpos + maxnetpos) >> 1;
            for (j = previouscol + 1; j < 256; j++)
            {netindex[j] = maxnetpos; /* really 256 */}
        }
        
        /* Main Learning Loop
           ------------------ */
        public void learn()
        {
            
            int    i, j, b, g, r;
            int    radius, rad, alpha, step, delta, samplepixels;
            byte[] p;
            int    pix, lim;
            
            if (lengthcount < minpicturebytes)
            {samplefac = 1;}
            alphadec     = 30 + ((samplefac - 1) / 3);
            p            = thepicture;
            pix          = 0;
            lim          = lengthcount;
            samplepixels = lengthcount / (3 * samplefac);
            delta        = samplepixels / ncycles;
            alpha        = initalpha;
            radius       = initradius;
            
            rad = radius >> radiusbiasshift;
            if (rad <= 1)
            {rad = 0;}
            for (i = 0; i < rad; i++)
            {
                radpower[i] =
                        alpha * (((rad * rad - i * i) * radbias) / (rad * rad));
            }
            
            //fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);
            
            if (lengthcount < minpicturebytes)
            {step = 3;}
            else if ((lengthcount % prime1) != 0)
            {step = 3 * prime1;}
            else
            {
                if ((lengthcount % prime2) != 0)
                {step = 3 * prime2;}
                else
                {
                    if ((lengthcount % prime3) != 0)
                    {step = 3 * prime3;}
                    else
                    {step = 3 * prime4;}
                }
            }
            
            i = 0;
            while (i < samplepixels)
            {
                b = (p[pix + 0] & 0xFF) << netbiasshift;
                g = (p[pix + 1] & 0xFF) << netbiasshift;
                r = (p[pix + 2] & 0xFF) << netbiasshift;
                j = contest(b, g, r);
                
                altersingle(alpha, j, b, g, r);
                if (rad != 0)
                {alterneigh(rad, j, b, g, r); /* alter neighbours */}
                
                pix += step;
                if (pix >= lim)
                {pix -= lengthcount;}
                
                i++;
                if (delta == 0)
                {delta = 1;}
                if (i % delta == 0)
                {
                    alpha -= alpha / alphadec;
                    radius -= radius / radiusdec;
                    rad = radius >> radiusbiasshift;
                    if (rad <= 1)
                    {rad = 0;}
                    for (j = 0; j < rad; j++)
                    {
                        radpower[j] =
                                alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
                    }
                }
            }
            //fprintf(stderr,"finished 1D learning: final alpha=%f !\n",((float)alpha)/initalpha);
        }
        
        /* Search for BGR values 0..255 (after net is unbiased) and return colour index
           ---------------------------------------------------------------------------- */
        public int map(int b, int g, int r)
        {
            
            int   i, j, dist, a, bestd;
            int[] p;
            int   best;
            
            bestd = 1000; /* biggest possible dist is 256*3 */
            best  = -1;
            i     = netindex[g]; /* index on g */
            j     = i - 1; /* start at netindex[g] and work outwards */
            
            while ((i < netsize) || (j >= 0))
            {
                if (i < netsize)
                {
                    p    = network[i];
                    dist = p[1] - g; /* inx key */
                    if (dist >= bestd)
                    {i = netsize; /* stop iter */}
                    else
                    {
                        i++;
                        if (dist < 0)
                        {dist = -dist;}
                        a = p[0] - b;
                        if (a < 0)
                        {a = -a;}
                        dist += a;
                        if (dist < bestd)
                        {
                            a = p[2] - r;
                            if (a < 0)
                            {a = -a;}
                            dist += a;
                            if (dist < bestd)
                            {
                                bestd = dist;
                                best  = p[3];
                            }
                        }
                    }
                }
                if (j >= 0)
                {
                    p    = network[j];
                    dist = g - p[1]; /* inx key - reverse dif */
                    if (dist >= bestd)
                    {j = -1; /* stop iter */}
                    else
                    {
                        j--;
                        if (dist < 0)
                        {dist = -dist;}
                        a = p[0] - b;
                        if (a < 0)
                        {a = -a;}
                        dist += a;
                        if (dist < bestd)
                        {
                            a = p[2] - r;
                            if (a < 0)
                            {a = -a;}
                            dist += a;
                            if (dist < bestd)
                            {
                                bestd = dist;
                                best  = p[3];
                            }
                        }
                    }
                }
            }
            return (best);
        }
        
        public byte[] process()
        {
            learn();
            unbiasnet();
            inxbuild();
            return colorMap();
        }
        
        /* Unbias network to give byte values 0..255 and record position i to prepare for sort
           ----------------------------------------------------------------------------------- */
        public void unbiasnet()
        {
            
            int i, j;
            
            for (i = 0; i < netsize; i++)
            {
                network[i][0] >>= netbiasshift;
                network[i][1] >>= netbiasshift;
                network[i][2] >>= netbiasshift;
                network[i][3] =   i; /* record colour no */
            }
        }
        
        /* Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in radpower[|i-j|]
           --------------------------------------------------------------------------------- */
        protected void alterneigh(int rad, int i, int b, int g, int r)
        {
            
            int   j, k, lo, hi, a, m;
            int[] p;
            
            lo = i - rad;
            if (lo < -1)
            {lo = -1;}
            hi = i + rad;
            if (hi > netsize)
            {hi = netsize;}
            
            j = i + 1;
            k = i - 1;
            m = 1;
            while ((j < hi) || (k > lo))
            {
                a = radpower[m++];
                if (j < hi)
                {
                    p = network[j++];
                    try
                    {
                        p[0] -= (a * (p[0] - b)) / alpharadbias;
                        p[1] -= (a * (p[1] - g)) / alpharadbias;
                        p[2] -= (a * (p[2] - r)) / alpharadbias;
                    }
                    catch (Exception e)
                    {
                    } // prevents 1.3 miscompilation
                }
                if (k > lo)
                {
                    p = network[k--];
                    try
                    {
                        p[0] -= (a * (p[0] - b)) / alpharadbias;
                        p[1] -= (a * (p[1] - g)) / alpharadbias;
                        p[2] -= (a * (p[2] - r)) / alpharadbias;
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        
        /* Move neuron i towards biased (b,g,r) by factor alpha
           ---------------------------------------------------- */
        protected void altersingle(int alpha, int i, int b, int g, int r)
        {
            
            /* alter hit neuron */
            int[] n = network[i];
            n[0] -= (alpha * (n[0] - b)) / initalpha;
            n[1] -= (alpha * (n[1] - g)) / initalpha;
            n[2] -= (alpha * (n[2] - r)) / initalpha;
        }
        
        /* Search for biased BGR values
           ---------------------------- */
        protected int contest(int b, int g, int r)
        {
            
            /* finds closest neuron (min dist) and updates freq */
            /* finds best neuron (min dist-bias) and returns position */
            /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
            /* bias[i] = gamma*((1/netsize)-freq[i]) */
            
            int   i, dist, a, biasdist, betafreq;
            int   bestpos, bestbiaspos, bestd, bestbiasd;
            int[] n;
            
            bestd       = ~(((int) 1) << 31);
            bestbiasd   = bestd;
            bestpos     = -1;
            bestbiaspos = bestpos;
            
            for (i = 0; i < netsize; i++)
            {
                n    = network[i];
                dist = n[0] - b;
                if (dist < 0)
                {dist = -dist;}
                a = n[1] - g;
                if (a < 0)
                {a = -a;}
                dist += a;
                a = n[2] - r;
                if (a < 0)
                {a = -a;}
                dist += a;
                if (dist < bestd)
                {
                    bestd   = dist;
                    bestpos = i;
                }
                biasdist = dist - ((bias[i]) >> (intbiasshift - netbiasshift));
                if (biasdist < bestbiasd)
                {
                    bestbiasd   = biasdist;
                    bestbiaspos = i;
                }
                           betafreq = (freq[i] >> betashift);
                freq[i] -= betafreq;
                bias[i] += (betafreq << gammashift);
            }
            freq[bestpos] += beta;
            bias[bestpos] -= betagamma;
            return (bestbiaspos);
        }
    }
    
    public static void main(String[] args)
    {
        Image image1 = Image.genColor(256 * 2, 256 * 2, Color.RED);
        
        Image image2 = Image.genColor(256 * 2, 256 * 2, Color.GREEN);
        
        Image image3 = Image.genColor(256 * 2, 256 * 2, Color.BLUE);
        
        GIFEncoder encoder = new GIFEncoder();
        System.out.println("encoder.start(\"test.gif\", 256, 256) = " + encoder.start("test.gif", 256, 256));
        System.out.println("encoder.addFrame(image1, 500) = " + encoder.addFrame(image1, 500));
        // encoder.setRepeat(2);
        System.out.println("encoder.addFrame(image2, 500) = " + encoder.addFrame(image2, 500));
        // encoder.setRepeat(1);
        System.out.println("encoder.addFrame(image3, 500) = " + encoder.addFrame(image3, 500));
        // encoder.setRepeat(0);
        System.out.println("encoder.finish() = " + encoder.finish());
        
        System.out.println("encoder.start(\"test2.gif\", 512, 512) = " + encoder.start("test2.gif", 512, 512));
        System.out.println("encoder.addFrame(image1, 500) = " + encoder.addFrame(image1, 500));
        // encoder.setRepeat(2);
        System.out.println("encoder.addFrame(image2, 500) = " + encoder.addFrame(image2, 500));
        // encoder.setRepeat(1);
        System.out.println("encoder.addFrame(image3, 500) = " + encoder.addFrame(image3, 500));
        // encoder.setRepeat(0);
        System.out.println("encoder.finish() = " + encoder.finish());
    }
}
