package pe;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;

/**
 * Beeper presents a small, loopable tone that can be heard
 * by clicking on the Code Keyboard.  It uses a Clip to loop the sound,
 * as well as for access to the Clip's gain control.
 *
 * @author Andrew Thompson
 * @version 2009-12-19
 */
public class Beeper extends JApplet
{
    
    BeeperPanel bp;
    
    public void init()
    {
        bp = new BeeperPanel();
        getContentPane().add(bp);
        validate();
        
        String sampleRate = getParameter("samplerate");
        if (sampleRate != null)
        {
            try
            {
                int sR = Integer.parseInt(sampleRate);
                bp.setSampleRate(sR);
            }
            catch (NumberFormatException ignored)
            {
            
            }
        }
        
        String fpw = getParameter("fpw");
        if (fpw != null)
        {
            try
            {
                int     fPW    = Integer.parseInt(fpw);
                JSlider slider = bp.getFramesPerWavelengthSlider();
                slider.setValue(fPW);
            }
            catch (NumberFormatException ignored)
            {
            
            }
        }
        
        boolean harmonic = (getParameter("addharmonic") != null);
        bp.setAddHarmonic(harmonic);
        
        bp.setUpSound();
        
        if (getParameter("autoloop") != null)
        {
            String loopcount = getParameter("loopcount");
            if (loopcount != null)
            {
                try
                {
                    int lC = Integer.parseInt(loopcount);
                    bp.loop(lC);
                }
                catch (NumberFormatException ignored)
                {
                
                }
            }
        }
    }
    
    public void stop()
    {
        bp.loopSound(false);
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Beeper");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            BeeperPanel BeeperPanel = new BeeperPanel();
            f.setContentPane(BeeperPanel);
            f.pack();
            f.setMinimumSize(f.getSize());
            f.setLocationByPlatform(true);
            f.setVisible(true);
        });
    }
}

/**
 * The main UI of Beeper.
 */
class BeeperPanel extends JPanel
{
    JComboBox<Integer> sampleRate;
    JSlider            framesPerWavelength;
    JLabel             frequency;
    JCheckBox          harmonic;
    Clip               clip;
    
    DecimalFormat decimalFormat = new DecimalFormat("###00.00");
    
    BeeperPanel()
    {
        super(new BorderLayout());
        // Use current OS look and feel.
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        setPreferredSize(new Dimension(300, 300));
        
        JPanel    options = new JPanel();
        BoxLayout bl      = new BoxLayout(options, BoxLayout.Y_AXIS);
        options.setLayout(bl);
        
        Integer[] rates = {8000, 11025, 16000, 22050, 44100};
        sampleRate = new JComboBox<>(rates);
        sampleRate.setToolTipText("Samples per second");
        sampleRate.setSelectedIndex(1);
        JPanel pSampleRate = new JPanel(new BorderLayout());
        pSampleRate.setBorder(new TitledBorder("Sample Rate"));
        pSampleRate.add(sampleRate);
        sampleRate.addActionListener(ae -> setUpSound());
        options.add(pSampleRate);
        
        framesPerWavelength = new JSlider(JSlider.HORIZONTAL, 10, 200, 25);
        framesPerWavelength.setPaintTicks(true);
        framesPerWavelength.setMajorTickSpacing(10);
        framesPerWavelength.setMinorTickSpacing(5);
        framesPerWavelength.setToolTipText("Frames per Wavelength");
        framesPerWavelength.addChangeListener(ce -> setUpSound());
        
        JPanel pFPW = new JPanel(new BorderLayout());
        pFPW.setBorder(new TitledBorder("Frames per Wavelength"));
        
        pFPW.add(framesPerWavelength);
        options.add(pFPW);
        
        JPanel bottomOption = new JPanel(new BorderLayout(4, 4));
        harmonic = new JCheckBox("Add Harmonic", false);
        harmonic.setToolTipText("Add harmonic to second channel, one octave up");
        harmonic.addActionListener(ae -> setUpSound());
        bottomOption.add(harmonic, BorderLayout.WEST);
        
        frequency = new JLabel();
        bottomOption.add(frequency, BorderLayout.CENTER);
        
        options.add(bottomOption);
        
        add(options, BorderLayout.NORTH);
        
        JPanel play = new JPanel(new BorderLayout(3, 3));
        play.setBorder(new EmptyBorder(4, 4, 4, 4));
        JButton bPlay = new JButton("Code Key");
        bPlay.setToolTipText("Click to make tone!");
        Dimension preferredSize = bPlay.getPreferredSize();
        bPlay.setPreferredSize(new Dimension((int) preferredSize.getWidth(), (int) preferredSize.getHeight() * 3));
        
        // TODO comment out to try KeyListener!
        //bPlay.setFocusable(false);
        bPlay.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent ke)
            {
                loopSound(true);
            }
        });
        bPlay.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent me)
            {
                loopSound(true);
            }
            
            @Override
            public void mouseReleased(MouseEvent me)
            {
                loopSound(false);
            }
        });
        play.add(bPlay);
        
        try
        {
            clip = AudioSystem.getClip();
            
            final FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            final JSlider volume = new JSlider(JSlider.VERTICAL, (int) control.getMinimum(), (int) control.getMaximum(), (int) control.getValue());
            volume.setToolTipText("Volume of beep");
            volume.addChangeListener(ce -> control.setValue(volume.getValue()));
            play.add(volume, BorderLayout.EAST);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        add(play, BorderLayout.CENTER);
        
        setUpSound();
    }
    
    public void loop(int loopcount)
    {
        if (clip != null)
        {
            clip.loop(loopcount);
        }
    }
    
    public void setAddHarmonic(boolean addHarmonic)
    {
        harmonic.setSelected(addHarmonic);
    }
    
    /**
     * Provides the slider for determining the # of frames per wavelength,
     * primarily to allow easy adjustment by host classes.
     */
    public JSlider getFramesPerWavelengthSlider()
    {
        return framesPerWavelength;
    }
    
    /**
     * Sets the sample rate to one of the four
     * allowable rates. Is ignored otherwise.
     */
    public void setSampleRate(int sR)
    {
        switch (sR)
        {
            case 8000:
                sampleRate.setSelectedIndex(0);
                break;
            case 11025:
                sampleRate.setSelectedIndex(1);
                break;
            case 16000:
                sampleRate.setSelectedIndex(2);
                break;
            case 22050:
                sampleRate.setSelectedIndex(3);
                break;
            default:
        }
    }
    
    /**
     * Sets label to current frequency settings.
     */
    public void setFrequencyLabel()
    {
        float freq = getFrequency();
        if (harmonic.isSelected())
        {
            frequency.setText(decimalFormat.format(freq) + "(/" + decimalFormat.format(freq * 2f) + ") Hz");
        }
        else
        {
            frequency.setText(decimalFormat.format(freq) + " Hz");
        }
    }
    
    /**
     * Generate the tone and inform the user of settings.
     */
    public void setUpSound()
    {
        try
        {
            generateTone();
            setFrequencyLabel();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Provides the frequency at current settings for
     * sample rate & frames per wavelength.
     */
    public float getFrequency()
    {
        Object value = sampleRate.getSelectedItem();
        if (value == null) return 0;
        int intST  = (int) value;
        int intFPW = framesPerWavelength.getValue();
    
        return (float) intST / (float) intFPW;
    }
    
    /**
     * Loops the current Clip until a commence false is passed.
     */
    public void loopSound(boolean commence)
    {
        if (commence)
        {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        else
        {
            clip.stop();
        }
    }
    
    /**
     * Generates a tone, and assigns it to the Clip.
     */
    public void generateTone() throws LineUnavailableException
    {
        if (clip != null)
        {
            clip.stop();
            clip.close();
        }
        else
        {
            clip = AudioSystem.getClip();
        }
        boolean addHarmonic = harmonic.isSelected();
    
        Object value = sampleRate.getSelectedItem();
        if (value == null) return;
        int intSR  = (int) value;
        int intFPW = framesPerWavelength.getValue();
    
        float sampleRate = (float) intSR;
    
        // oddly, the sound does not loop well for less than
        // around 5 or so, wavelengths
        int    wavelengths = 20;
        byte[] buf         = new byte[2 * intFPW * wavelengths];
        AudioFormat af = new AudioFormat(sampleRate, 8,  // sample size in bits
                                         2,  // channels
                                         true,  // signed
                                         false  // bigendian
        );
    
        // int maxVol = 127;
        for (int i = 0; i < intFPW * wavelengths; i++)
        {
            double angle = ((float) (i * 2) / ((float) intFPW)) * (Math.PI);
            buf[i * 2]       = getByteValue(angle);
            buf[(i * 2) + 1] = addHarmonic ? getByteValue(2 * angle) : buf[i * 2];
        }
        
        try
        {
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buf), af, buf.length / 2);
            
            clip.open(ais);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Provides the byte value for this point in the sinusoidal wave.
     */
    private static byte getByteValue(double angle)
    {
        int maxVol = 127;
        //return (new Integer((int) Math.round(Math.sin(angle) * maxVol))).byteValue();
        return (byte) Math.round(Math.sin(angle) * maxVol);
    }
}
