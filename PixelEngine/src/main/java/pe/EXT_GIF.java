package pe;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.render.GLState;
import pe.texture.Image;
import rutils.Logger;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class EXT_GIF extends Extension
{
    private static final Logger LOGGER = new Logger();
    
    private static GIFEncoder encoder;
    private static boolean    recording;
    private static long       lastFrame;
    private static int        frameCounter;
    private static String     timestamp;
    
    @StageMethod(stage = Stage.RENDER_SETUP)
    static void renderSetup()
    {
        EXT_GIF.encoder = new GIFEncoder();
    }
    
    @StageMethod(stage = Stage.POST_EVENTS)
    static void postEvents()
    {
        if (Keyboard.down(Keyboard.Key.F12) && Modifier.all(Modifier.CONTROL, Modifier.SHIFT))
        {
            if (EXT_GIF.recording)
            {
                stopRecording();
            }
            else
            {
                startRecording();
            }
        }
    }
    
    @StageMethod(stage = Stage.POST_FRAME)
    static void postFrame()
    {
        if (EXT_GIF.recording)
        {
            final int GIF_RECORD_FRAMERATE = 10;
            EXT_GIF.frameCounter++;
            
            // NOTE: We record one gif frame every 10 game frames
            if ((EXT_GIF.frameCounter % GIF_RECORD_FRAMERATE) == 0)
            {
                long time  = Time.getNS();
                long delta = time - EXT_GIF.lastFrame;
                EXT_GIF.lastFrame = time;
                
                // Get image data for the current frame (from back buffer)
                // NOTE: This process is quite slow... :(
                
                int w = Window.framebufferWidth();
                int h = Window.framebufferHeight();
                
                Color.Buffer data  = GLState.readFrontBuffer(0, 0, w, h);
                Image        image = Image.load(data, w, h, 1, data.format());
                
                boolean result = EXT_GIF.encoder.addFrame(image, (int) (delta / 1_000_000));
                
                if (!result) EXT_GIF.LOGGER.warning("Could not add frame to", EXT_GIF.timestamp);
                
                image.delete(); // Free image data
            }
        }
    }
    
    @StageMethod(stage = Stage.RENDER_DESTROY)
    static void renderDestroy()
    {
        stopRecording();
    }
    
    public static void startRecording()
    {
        if (EXT_GIF.recording) return;
        
        EXT_GIF.recording    = true;
        EXT_GIF.lastFrame    = Time.getNS();
        EXT_GIF.frameCounter = 0;
        EXT_GIF.timestamp    = String.format("Recording - %s.gif", Time.timeStamp());
        
        boolean result = EXT_GIF.encoder.start(EXT_GIF.timestamp, Window.framebufferWidth(), Window.framebufferHeight());
        
        if (result)
        {
            EXT_GIF.LOGGER.info("Started GIF Recording: %s", EXT_GIF.timestamp);
        }
        else
        {
            EXT_GIF.LOGGER.warning("Could not start GIF recording");
        }
    }
    
    public static void stopRecording()
    {
        if (!EXT_GIF.recording) return;
        
        EXT_GIF.recording = false;
        
        boolean result = EXT_GIF.encoder.finish();
        
        EXT_GIF.LOGGER.info("Finished GIF Recording. Result:", result ? "Success" : "Failure");
    }
    
    @SuppressWarnings("unused")
    private static class GIFEncoder
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
                
                int colorDepth = 8;
                int palletSize = 7;
                
                if (this.firstFrame)
                {
                    // logical screen descriptior
                    writeLSD(palletSize);
                    
                    // global color table
                    writePalette(results.colorTable());
                    
                    // use NS app extension to indicate reps
                    if (this.repeat >= 0) writeNetscapeExt();
                }
                
                // write graphic control extension
                writeGraphicCtrlExt(delay, results.transparentIndex());
                
                // image descriptor
                writeImageDesc(palletSize);
                
                // local color table
                if (!this.firstFrame) writePalette(results.colorTable());
                
                // encode and write pixel data
                LZW.encode(results.pixels(), colorDepth, this.stream);
                
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
                this.stream.write(0x3B);
                this.stream.flush();
                if (this.shouldClose) this.stream.close();
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
            final byte[]    colorTable = NeuQuantize.quantize(rawPixels, this.quality); // create reduced palette
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
                int index = NeuQuantize.map(rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF, rawPixels[k++] & 0xFF);
                pixels[i]        = (byte) index;
                usedEntry[index] = true;
            }
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
            this.stream.write(0x21);
            
            // GCE label
            this.stream.write(0xF9);
            
            // data block size
            this.stream.write(4);
            
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
            this.stream.write((((0 & 0b0111) << 5) |       // 1:3 reserved
                               ((dispose & 0b0111) << 2) | // 4:6 disposal
                               ((0 & 0b0001) << 1) |       // 7   user input - 0 = none
                               (transparency & 0b0001)));  // 8   transparency flag
            
            writeShort(delay / 10); // delay x 1/100 sec
            this.stream.write(transIndex); // transparent color index
            this.stream.write(0); // block terminator
        }
        
        /**
         * Writes Image Descriptor
         */
        @SuppressWarnings("PointlessBitwiseExpression")
        protected void writeImageDesc(int palletSize) throws IOException
        {
            // image separator
            this.stream.write(0x2C);
            
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
                this.stream.write(0);
            }
            else
            {
                // specify normal LCT
                this.stream.write((((1 & 0b0001) << 7) |    // 1 local color table  1=yes
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
            this.stream.write((((1 & 0b0001) << 7) |    // 1  : global color table flag = 1 (gct used)
                               ((7 & 0b0111) << 4) |    // 2-4: color resolution = 7
                               ((0 & 0b0001) << 3) |    // 5  : gct sort flag = 0
                               (palletSize & 0b0111))); // 6-8: gct size
            
            // background color index
            this.stream.write(0);
            
            // pixel aspect ratio - assume 1:1
            this.stream.write(0);
        }
        
        /**
         * Writes Netscape application extension to define
         * repeat count.
         */
        protected void writeNetscapeExt() throws IOException
        {
            // extension introducer
            this.stream.write(0x21);
            
            // app extension label
            this.stream.write(0xFF);
            
            // block size
            this.stream.write(11);
            
            // app id + auth code
            writeString("NETSCAPE" + "2.0");
            
            // sub-block size
            this.stream.write(3);
            
            // loop sub-block id
            this.stream.write(1);
            
            // loop count (extra iterations, 0=repeat forever)
            writeShort(this.repeat);
            
            // block terminator
            this.stream.write(0);
        }
        
        /**
         * Writes color table
         */
        protected void writePalette(byte[] colorTab) throws IOException
        {
            this.stream.write(colorTab);
            int n = (3 * 256) - colorTab.length;
            for (int i = 0; i < n; i++) this.stream.write(0);
        }
        
        /**
         * Write 16-bit value to output stream, LSB first
         */
        protected void writeShort(int value) throws IOException
        {
            this.stream.write(value & 0xFF);
            this.stream.write((value >> 8) & 0xFF);
        }
        
        /**
         * Writes string to output stream
         */
        protected void writeString(String s) throws IOException
        {
            for (int i = 0; i < s.length(); i++) this.stream.write((byte) s.charAt(i));
        }
        
        // -------------------- Utility Methods -------------------- //
        
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
    }
    
    private interface AnalyzeResults
    {
        byte[] pixels();
        
        byte[] colorTable();
        
        int transparentIndex();
    }
    
    private static class LZW
    {
        /**
         * General Defines
         *
         * <ul>
         *     <li>{@link #HASH_SIZE}: 80% occupancy</li>
         *     <li>{@link #MAX_BIT_DEPTH}</li>
         *     <li>{@link #MAX_MAX_CODE}: Should <b>NEVER</b> generate this code</li>
         * </ul>
         *
         * @noinspection JavaDoc
         */
        private static final int
                HASH_SIZE     = 5003,
                MAX_BIT_DEPTH = 12,
                MAX_MAX_CODE  = 1 << MAX_BIT_DEPTH;
        
        private static final int[] MASKS = {
                0x0000,
                0x0001, 0x0003, 0x0007, 0x000F,
                0x001F, 0x003F, 0x007F, 0x00FF,
                0x01FF, 0x03FF, 0x07FF, 0x0FFF,
                0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
        };
        
        /**
         * Initial Number of Bits
         */
        private static int initBitDepth;
        
        /**
         * Number of bits/code
         */
        private static int bitDepth;
        
        /**
         * Maximum Code, given n_bits
         */
        private static int maxCode;
        
        private static int[] hashTable;
        private static int[] codeTable;
        
        /**
         * Block Compression Parameters. After all codes are used up, and
         * compression rate changes, start over.
         */
        private static boolean clearFlag = false;
        
        private static int clearCode;
        private static int eofCode;
        
        /**
         * First unused entry
         */
        private static int freeEntry;
        
        private static int curBitDepth;
        private static int curAccum;
        
        /**
         * Number of characters so far in this 'packet'
         */
        private static int accumulatorIndex;
        
        /**
         * Define the storage for the packet accumulator
         */
        private static byte[] accumulator;
        
        /**
         * Algorithm: Use open addressing double hashing (no chaining) on the
         * prefix code / next character combination. We do a variant of Knuth's
         * algorithm D (vol. 3, sec. 6.4) along with G. Knott's
         * relatively-prime secondary probe.  Here, the modular division first
         * probe is gives way to a faster exclusive-or manipulation. Also do
         * block compression with an adaptive reset, whereby the code table is
         * cleared when the compression ratio decreases, but after the table
         * fills. The variable-length output codes are re-sized at this point,
         * and a special CLEAR code is generated for the decompressor. Late
         * addition: construct the table according to file size for noticeable
         * speed improvement on small files. Please direct questions about this
         * implementation to ames!jaw.
         * <p>
         * Maintain a BITS character long buffer (so that 8 codes will fit in
         * it exactly). Use the VAX insv instruction to insert each code in
         * turn. When the buffer fills up empty it and start over.
         *
         * @param data     The array of data formatted to encode.
         * @param bitDepth The bit depth. Usually 8.
         * @param stream   The stream to output the data.
         * @throws IOException Writing to the stream failed.
         */
        public static void encode(byte[] data, int bitDepth, OutputStream stream) throws IOException
        {
            LZW.initBitDepth = Math.max(2, bitDepth) + 1;
            
            LZW.bitDepth = LZW.initBitDepth;
            LZW.maxCode  = (1 << LZW.bitDepth) - 1;
            
            LZW.hashTable = new int[LZW.HASH_SIZE];
            LZW.codeTable = new int[LZW.HASH_SIZE];
            
            // Set up the necessary values
            LZW.clearFlag = false;
            
            LZW.clearCode = 1 << (LZW.initBitDepth - 1);
            LZW.eofCode   = LZW.clearCode + 1;
            LZW.freeEntry = LZW.clearCode + 2;
            
            LZW.curAccum    = 0;
            LZW.curBitDepth = 0;
            
            LZW.accumulatorIndex = 0; // clear packet
            LZW.accumulator      = new byte[256];
            
            // compress and write the pixel data
            
            int hashShift = 0;
            for (int i = LZW.HASH_SIZE; i < 65536; i *= 2) ++hashShift;
            hashShift = 8 - hashShift; // set hash code range bound
            
            Arrays.fill(LZW.hashTable, -1); // clear hash table
            
            stream.write(LZW.initBitDepth - 1); // write "initial code size" byte
            
            output(LZW.clearCode, stream);
            
            int byteIndex = 0;
            int prevByte  = data[byteIndex] & 0xFF;
            outer_loop:
            for (int n = data.length; byteIndex < n; ++byteIndex)
            {
                int curByte = data[byteIndex] & 0xFF;
                
                int hash      = (curByte << LZW.MAX_BIT_DEPTH) + prevByte;
                int hashIndex = (curByte << hashShift) ^ prevByte; // xor hashing
                
                if (LZW.hashTable[hashIndex] == hash)
                {
                    prevByte = LZW.codeTable[hashIndex];
                    continue;
                }
                else if (LZW.hashTable[hashIndex] >= 0) // non-empty slot
                {
                    int secondaryHash = hashIndex == 0 ? 1 : LZW.HASH_SIZE - hashIndex; // secondary hash (after G. Knott)
                    do
                    {
                        if ((hashIndex -= secondaryHash) < 0) hashIndex += LZW.HASH_SIZE;
                        
                        if (LZW.hashTable[hashIndex] == hash)
                        {
                            prevByte = LZW.codeTable[hashIndex];
                            continue outer_loop;
                        }
                    } while (LZW.hashTable[hashIndex] >= 0);
                }
                output(prevByte, stream);
                if (LZW.freeEntry < LZW.MAX_MAX_CODE)
                {
                    LZW.codeTable[hashIndex] = LZW.freeEntry++; // code -> hashtable
                    LZW.hashTable[hashIndex] = hash;
                }
                else
                {
                    Arrays.fill(LZW.hashTable, -1);
                    LZW.freeEntry = LZW.clearCode + 2;
                    LZW.clearFlag = true;
                    
                    output(LZW.clearCode, stream);
                }
                prevByte = curByte;
            }
            // Put out the final code.
            output(prevByte, stream);
            output(LZW.eofCode, stream);
            
            // write block terminator
            stream.write(0);
        }
        
        /**
         * Add a character to the end of the current packet, and if it is 254
         * characters, flush the packet to disk.
         */
        private static void writeAccumulator(byte c, OutputStream stream) throws IOException
        {
            LZW.accumulator[LZW.accumulatorIndex++] = c;
            if (LZW.accumulatorIndex >= 254) flushAccumulator(stream);
        }
        
        /**
         * Flush the packet to disk, and reset the accumulator.
         */
        private static void flushAccumulator(OutputStream stream) throws IOException
        {
            if (LZW.accumulatorIndex > 0)
            {
                stream.write(LZW.accumulatorIndex);
                stream.write(LZW.accumulator, 0, LZW.accumulatorIndex);
                LZW.accumulatorIndex = 0;
            }
        }
        
        private static void output(int code, OutputStream stream) throws IOException
        {
            LZW.curAccum &= LZW.MASKS[LZW.curBitDepth];
            LZW.curAccum = LZW.curBitDepth > 0 ? LZW.curAccum | (code << LZW.curBitDepth) : code;
            LZW.curBitDepth += LZW.bitDepth;
            
            while (LZW.curBitDepth >= 8)
            {
                writeAccumulator((byte) (LZW.curAccum & 0xFF), stream);
                LZW.curAccum >>= 8;
                LZW.curBitDepth -= 8;
            }
            
            // If the next entry is going to be too big for the code size,
            // then increase it, if possible.
            if (LZW.freeEntry > LZW.maxCode || LZW.clearFlag)
            {
                if (LZW.clearFlag)
                {
                    LZW.bitDepth  = LZW.initBitDepth;
                    LZW.maxCode   = (1 << LZW.bitDepth) - 1;
                    LZW.clearFlag = false;
                }
                else
                {
                    ++LZW.bitDepth;
                    LZW.maxCode = LZW.bitDepth == LZW.MAX_BIT_DEPTH ? LZW.MAX_MAX_CODE : (1 << LZW.bitDepth) - 1;
                }
            }
            
            if (code == LZW.eofCode)
            {
                // At EOF, write the rest of the buffer.
                while (LZW.curBitDepth > 0)
                {
                    writeAccumulator((byte) (LZW.curAccum & 0xFF), stream);
                    LZW.curAccum >>= 8;
                    LZW.curBitDepth -= 8;
                }
                
                flushAccumulator(stream);
            }
        }
    }
    
    private static class NeuQuantize
    {
        /**
         * Number of Colors Used
         */
        protected static final int NET_SIZE = 256;
        
        /**
         * Four Primes near 500. Assume no image has a length so large that it is
         * divisible by all four primes.
         */
        protected static final int PRIME1 = 499;
        protected static final int PRIME2 = 491;
        protected static final int PRIME3 = 487;
        protected static final int PRIME4 = 503;
        
        /**
         * Minimum Size for Input Image.
         */
        protected static final int MIN_PICTURE_BYTES = 3 * PRIME4;
        
        /**
         * Network Definitions
         *
         * <ul>
         *     <li>{@link #MAX_NET_POS}</li>
         *     <li>{@link #NET_BIAS_SHIFT}: Bias for Color Values</li>
         *     <li>{@link #N_CYCLES}: Number of Learning Cycles</li>
         * </ul>
         *
         * @noinspection JavaDoc
         */
        protected static final int
                MAX_NET_POS    = NET_SIZE - 1,
                NET_BIAS_SHIFT = 4,
                N_CYCLES       = 100;
        
        /**
         * Frequency and Bias Definitions
         *
         * <ul>
         *     <li>{@link #INT_BIAS_SHIFT}</li>
         *     <li>{@link #INT_BIAS}: Bias for Fractions</li>
         *     <li>{@link #GAMMA_SHIFT}</li>
         *     <li>{@link #GAMMA} {@code = 1024}</li>
         *     <li>{@link #BETA_SHIFT}</li>
         *     <li>{@link #BETA} {@code = 1/1024}</li>
         *     <li>{@link #BETA_GAMMA}</li>
         * </ul>
         *
         * @noinspection JavaDoc
         */
        @SuppressWarnings("unused")
        protected static final int
                INT_BIAS_SHIFT = 16,
                INT_BIAS       = 1 << INT_BIAS_SHIFT,
                GAMMA_SHIFT    = 10,
                GAMMA          = 1 << GAMMA_SHIFT,
                BETA_SHIFT     = 10,
                BETA           = INT_BIAS >> BETA_SHIFT,
                BETA_GAMMA     = INT_BIAS << (GAMMA_SHIFT - BETA_SHIFT);
        
        /**
         * Decreasing Radius Factor Definitions
         * <p>
         * For 256 columns, radius starts at {@code 32.0} bias by {@code 6} bits
         * and decreases by a factor of {@code 1/30} each cycle
         *
         * <ul>
         *     <li{@link #INIT_RAD}</li>
         *     <li{@link #RADIUS_BIAS_SHIFT}</li>
         *     <li{@link #RADIUS_BIAS}</li>
         *     <li{@link #INIT_RADIUS}</li>
         *     <li{@link #RADIUS_DEC}</li>
         * </ul>
         *
         * @noinspection JavaDoc
         */
        protected static final int
                INIT_RAD          = NET_SIZE >> 3,
                RADIUS_BIAS_SHIFT = 6,
                RADIUS_BIAS       = 1 << RADIUS_BIAS_SHIFT,
                INIT_RADIUS       = INIT_RAD * RADIUS_BIAS,
                RADIUS_DEC        = 30;
        
        /**
         * Decreasing Alpha Factor Definitions
         * <p>
         * For 256 columns, radius starts at {@code 32.0} bias by {@code 6} bits
         * and decreases by a factor of {@code 1/30} each cycle
         *
         * <ul>
         *     <li{@link #ALPHA_BIAS_SHIFT}: Alpha starts at {@code 1.0}</li>
         *     <li{@link #INIT_ALPHA}</li>
         * </ul>
         *
         * @noinspection JavaDoc
         */
        protected static final int
                ALPHA_BIAS_SHIFT = 10,
                INIT_ALPHA       = 1 << ALPHA_BIAS_SHIFT;
        
        /**
         * {@link #RAD_BIAS} and {@link #ALPHA_RAD_BIAS} used for {@link #radPower} calculation
         *
         * @noinspection JavaDoc, RedundantSuppression
         */
        protected static final int
                RAD_BIAS_SHIFT       = 8,
                RAD_BIAS             = 1 << RAD_BIAS_SHIFT,
                ALPHA_RAD_BIAS_SHIFT = ALPHA_BIAS_SHIFT + RAD_BIAS_SHIFT,
                ALPHA_RAD_BIAS       = 1 << ALPHA_RAD_BIAS_SHIFT;
        
        /*
         * Types and Global Variables
         * --------------------------
         */
        
        /**
         * The input image.
         */
        protected static byte[] pixels;
        
        /**
         * Sampling Factor {@code [1..30]}
         */
        protected static int sampleFactor;
        
        /**
         * The Network Itself
         * <p>
         * {@code network = new int[}{@link #NET_SIZE}{@code ][4]}
         */
        protected static int[][] network;
        
        /**
         * For Network Lookup - Really 256
         * <p>
         * {@code netIndex = new int[256]}
         */
        protected static int[] netIndex;
        
        /**
         * Bias and Frequency Arrays for Learning
         * <p>
         * {@code bias = new int[}{@link #NET_SIZE}{@code ]}
         * <p>
         * {@code freq = new int[}{@link #NET_SIZE}{@code ]}
         */
        protected static int[] bias, freq;
        
        /**
         * radPower for pre-computation.
         * <p>
         * {@code radPower = new int[}{@link #INIT_RAD}{@code ]}
         */
        protected static int[] radPower;
        
        /**
         * biased by 10 bits
         */
        protected static int alphaDec;
        
        /**
         * Initialise network in range (0,0,0) to (255,255,255), set parameters,
         * and create reduced palette
         */
        public static byte @NotNull [] quantize(byte @NotNull [] pixels, int sampleFactor)
        {
            NeuQuantize.pixels       = pixels;
            NeuQuantize.sampleFactor = sampleFactor;
            
            NeuQuantize.network  = new int[NeuQuantize.NET_SIZE][];
            NeuQuantize.netIndex = new int[256];
            NeuQuantize.bias     = new int[NeuQuantize.NET_SIZE];
            NeuQuantize.freq     = new int[NeuQuantize.NET_SIZE];
            NeuQuantize.radPower = new int[NeuQuantize.INIT_RAD];
            
            for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
            {
                int initial = (i << (NeuQuantize.NET_BIAS_SHIFT + 8)) / NeuQuantize.NET_SIZE;
                
                NeuQuantize.network[i] = new int[] {initial, initial, initial, 0};
            }
            Arrays.fill(NeuQuantize.netIndex, 0);
            Arrays.fill(NeuQuantize.bias, 0);
            Arrays.fill(NeuQuantize.freq, NeuQuantize.INT_BIAS / NeuQuantize.NET_SIZE); // 1/NET_SIZE
            Arrays.fill(NeuQuantize.radPower, 0);
            
            learn();
            
            // Unbias network to give byte values 0..255 and record position i to prepare for sort
            for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
            {
                NeuQuantize.network[i][0] >>= NeuQuantize.NET_BIAS_SHIFT;
                NeuQuantize.network[i][1] >>= NeuQuantize.NET_BIAS_SHIFT;
                NeuQuantize.network[i][2] >>= NeuQuantize.NET_BIAS_SHIFT;
                NeuQuantize.network[i][3] =   i; /* record color no */
            }
            
            inxBuild();
            
            byte[] map   = new byte[3 * NeuQuantize.NET_SIZE];
            int[]  index = new int[NeuQuantize.NET_SIZE];
            
            for (int i = 0; i < NeuQuantize.NET_SIZE; i++) index[NeuQuantize.network[i][3]] = i;
            
            for (int i = 0, k = 0; i < NeuQuantize.NET_SIZE; i++)
            {
                int j = index[i];
                map[k++] = (byte) (NeuQuantize.network[j][0]);
                map[k++] = (byte) (NeuQuantize.network[j][1]);
                map[k++] = (byte) (NeuQuantize.network[j][2]);
            }
            return map;
        }
        
        /**
         * Search for BGR values 0..255 (after net is unbiased) and return color
         * index
         */
        public static int map(int b, int g, int r)
        {
            int bestDist = 1000; /* biggest possible dist is 256*3 */
            int best     = -1;
            
            // i: index on g
            // j: start at netIndex[g] and work outwards
            for (int i = NeuQuantize.netIndex[g], j = i - 1; i < NeuQuantize.NET_SIZE || j >= 0; )
            {
                if (i < NeuQuantize.NET_SIZE)
                {
                    int[] p    = NeuQuantize.network[i];
                    int   dist = p[1] - g; /* inx key */
                    if (dist >= bestDist)
                    {
                        i = NeuQuantize.NET_SIZE; /* stop iter */
                    }
                    else
                    {
                        i++;
                        dist = Math.abs(dist) + Math.abs(p[0] - b);
                        if (dist < bestDist)
                        {
                            dist += Math.abs(p[2] - r);
                            if (dist < bestDist)
                            {
                                bestDist = dist;
                                best     = p[3];
                            }
                        }
                    }
                }
                if (j >= 0)
                {
                    int[] p    = NeuQuantize.network[j];
                    int   dist = g - p[1]; /* inx key - reverse dif */
                    if (dist >= bestDist)
                    {
                        j = -1; /* stop iter */
                    }
                    else
                    {
                        j--;
                        dist = Math.abs(dist) + Math.abs(p[0] - b);
                        if (dist < bestDist)
                        {
                            dist += Math.abs(p[2] - r);
                            if (dist < bestDist)
                            {
                                bestDist = dist;
                                best     = p[3];
                            }
                        }
                    }
                }
            }
            return (best);
        }
        
        /**
         * Main Learning Loop
         * ------------------
         */
        private static void learn()
        {
            int j, b, g, r;
            int radius, rad, alpha, samplePixels;
            
            if (NeuQuantize.pixels.length < NeuQuantize.MIN_PICTURE_BYTES) NeuQuantize.sampleFactor = 1;
            
            NeuQuantize.alphaDec = 30 + ((NeuQuantize.sampleFactor - 1) / 3);
            samplePixels         = NeuQuantize.pixels.length / (3 * NeuQuantize.sampleFactor);
            alpha                = NeuQuantize.INIT_ALPHA;
            radius               = NeuQuantize.INIT_RADIUS;
            
            rad = radius >> NeuQuantize.RADIUS_BIAS_SHIFT;
            // if (rad <= 1) rad = 0;
            
            for (int i = 0; i < rad; i++)
            {
                NeuQuantize.radPower[i] = alpha * (((rad * rad - i * i) * NeuQuantize.RAD_BIAS) / (rad * rad));
            }
            
            int step;
            if (NeuQuantize.pixels.length < NeuQuantize.MIN_PICTURE_BYTES)
            {
                step = 3;
            }
            else if ((NeuQuantize.pixels.length % NeuQuantize.PRIME1) != 0)
            {
                step = 3 * NeuQuantize.PRIME1;
            }
            else if ((NeuQuantize.pixels.length % NeuQuantize.PRIME2) != 0)
            {
                step = 3 * NeuQuantize.PRIME2;
            }
            else if ((NeuQuantize.pixels.length % NeuQuantize.PRIME3) != 0)
            {
                step = 3 * NeuQuantize.PRIME3;
            }
            else
            {
                step = 3 * NeuQuantize.PRIME4;
            }
            
            int delta = samplePixels / NeuQuantize.N_CYCLES;
            if (delta == 0) delta = 1;
            for (int i = 0, pix = 0; i < samplePixels; )
            {
                b = (NeuQuantize.pixels[pix] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
                g = (NeuQuantize.pixels[pix + 1] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
                r = (NeuQuantize.pixels[pix + 2] & 0xFF) << NeuQuantize.NET_BIAS_SHIFT;
                j = contest(b, g, r);
                
                alterSingle(alpha, j, b, g, r);
                if (rad != 0) alterNeighbours(rad, j, b, g, r); /* alter neighbours */
                
                pix += step;
                if (pix >= NeuQuantize.pixels.length) pix -= NeuQuantize.pixels.length;
                
                i++;
                
                if (i % delta == 0)
                {
                    alpha -= alpha / NeuQuantize.alphaDec;
                    radius -= radius / NeuQuantize.RADIUS_DEC;
                    rad = radius >> NeuQuantize.RADIUS_BIAS_SHIFT;
                    if (rad <= 1) rad = 0;
                    for (j = 0; j < rad; j++)
                    {
                        NeuQuantize.radPower[j] = alpha * (((rad * rad - j * j) * NeuQuantize.RAD_BIAS) / (rad * rad));
                    }
                }
            }
        }
        
        /**
         * Insertion sort of network and building of netIndex[0..255] (to do after unbias)
         */
        private static void inxBuild()
        {
            int prevCol  = 0;
            int startPos = 0;
            for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
            {
                int[] p        = NeuQuantize.network[i];
                int   smallPos = i;
                int   smallVal = p[1]; /* index on g */
                /* find smallest in [i..NET_SIZE-1] */
                for (int j = i + 1; j < NeuQuantize.NET_SIZE; j++)
                {
                    int[] q = NeuQuantize.network[j];
                    if (q[1] < smallVal)
                    { /* index on g */
                        smallPos = j;
                        smallVal = q[1]; /* index on g */
                    }
                }
                int[] q = NeuQuantize.network[smallPos];
                /* swap p(i) and q(smallPos) entries */
                if (i != smallPos)
                {
                    int temp;
                    
                    temp = q[0];
                    q[0] = p[0];
                    p[0] = temp;
                    temp = q[1];
                    q[1] = p[1];
                    p[1] = temp;
                    temp = q[2];
                    q[2] = p[2];
                    p[2] = temp;
                    temp = q[3];
                    q[3] = p[3];
                    p[3] = temp;
                }
                /* smallVal entry is now in position i */
                if (smallVal != prevCol)
                {
                    NeuQuantize.netIndex[prevCol] = (startPos + i) >> 1;
                    for (int j = prevCol + 1; j < smallVal; j++) NeuQuantize.netIndex[j] = i;
                    prevCol  = smallVal;
                    startPos = i;
                }
            }
            
            NeuQuantize.netIndex[prevCol] = (startPos + NeuQuantize.MAX_NET_POS) >> 1;
            Arrays.fill(NeuQuantize.netIndex, prevCol + 1, NeuQuantize.netIndex.length, NeuQuantize.MAX_NET_POS); // really 256
        }
        
        /**
         * Move adjacent neurons by precomputed {@code alpha*(1-((i-j)^2/[r]^2))}
         * in {@link #radPower}{@code [|i-j|]}
         */
        private static void alterNeighbours(int rad, int i, int b, int g, int r)
        {
            int lo = Math.max(i - rad, -1);
            int hi = Math.min(i + rad, NeuQuantize.NET_SIZE);
            
            int j = i + 1;
            int k = i - 1;
            int m = 1;
            while (j < hi || k > lo)
            {
                int a = NeuQuantize.radPower[m++];
                if (j < hi)
                {
                    int[] p = NeuQuantize.network[j++];
                    try
                    {
                        p[0] -= (a * (p[0] - b)) / NeuQuantize.ALPHA_RAD_BIAS;
                        p[1] -= (a * (p[1] - g)) / NeuQuantize.ALPHA_RAD_BIAS;
                        p[2] -= (a * (p[2] - r)) / NeuQuantize.ALPHA_RAD_BIAS;
                    }
                    catch (Exception ignored) {}
                }
                if (k > lo)
                {
                    int[] p = NeuQuantize.network[k--];
                    try
                    {
                        p[0] -= (a * (p[0] - b)) / NeuQuantize.ALPHA_RAD_BIAS;
                        p[1] -= (a * (p[1] - g)) / NeuQuantize.ALPHA_RAD_BIAS;
                        p[2] -= (a * (p[2] - r)) / NeuQuantize.ALPHA_RAD_BIAS;
                    }
                    catch (Exception ignored) {}
                }
            }
        }
        
        /**
         * Move neuron i towards biased (b,g,r) by factor alpha
         */
        private static void alterSingle(int alpha, int i, int b, int g, int r)
        {
            // alter hit neuron
            int[] n = NeuQuantize.network[i];
            n[0] -= (alpha * (n[0] - b)) / NeuQuantize.INIT_ALPHA;
            n[1] -= (alpha * (n[1] - g)) / NeuQuantize.INIT_ALPHA;
            n[2] -= (alpha * (n[2] - r)) / NeuQuantize.INIT_ALPHA;
        }
        
        /**
         * Search for biased BGR values
         * <p>
         * Finds the closest neuron (minimum distance) and updates frequency.
         * Finds the best neuron (minimum distance-bias) and returns position.
         * For frequently chosen neurons, {@code freq[i]} is high and
         * {@code bias[i]} is negative.
         * <p>
         * {@code bias[i] = }{@link #GAMMA}{@code *((1/}{@link #NET_SIZE}{@code )-freq[i])}
         */
        private static int contest(int b, int g, int r)
        {
            int bestDist     = Integer.MAX_VALUE;
            int bestBiasDist = bestDist;
            int bestPos      = -1;
            int bestBiasPos  = bestPos;
            
            for (int i = 0; i < NeuQuantize.NET_SIZE; i++)
            {
                int[] n = NeuQuantize.network[i];
                
                int dist = Math.abs(n[0] - b) +
                           Math.abs(n[1] - g) +
                           Math.abs(n[2] - r);
                
                if (dist < bestDist)
                {
                    bestDist = dist;
                    bestPos  = i;
                }
                
                int biasDist = dist - (NeuQuantize.bias[i] >> (NeuQuantize.INT_BIAS_SHIFT - NeuQuantize.NET_BIAS_SHIFT));
                if (biasDist < bestBiasDist)
                {
                    bestBiasDist = biasDist;
                    bestBiasPos  = i;
                }
                
                int betaFreq = NeuQuantize.freq[i] >> NeuQuantize.BETA_SHIFT;
                NeuQuantize.bias[i] += betaFreq << NeuQuantize.GAMMA_SHIFT;
                NeuQuantize.freq[i] -= betaFreq;
            }
            NeuQuantize.bias[bestPos] -= NeuQuantize.BETA_GAMMA;
            NeuQuantize.freq[bestPos] += NeuQuantize.BETA;
            return bestBiasPos;
        }
    }
}
