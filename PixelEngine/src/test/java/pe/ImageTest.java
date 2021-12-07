package pe;

import pe.color.BlendMode;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.texture.Image;

public class ImageTest
{
    public static void main(String[] args)
    {
        boolean result;
        
        Image solid = Image.genColor(100, 100, Color.LIGHTER_BLUE);
        result = solid.export("out/solid.png");
        assert result;
        solid.delete();
        
        Image grad = Image.genColorGradient(100, 100, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
        result = grad.export("out/grad.png");
        assert result;
        grad.delete();
        
        Image gradV = Image.genColorGradientV(100, 100, Color.BLACK, Color.MAGENTA);
        result = gradV.export("out/gradV.png");
        assert result;
        gradV.delete();
        
        Image gradH = Image.genColorGradientH(100, 100, Color.YELLOW, Color.RED);
        result = gradH.export("out/gradH.png");
        assert result;
        gradH.delete();
        
        Image gradD1 = Image.genColorGradientDiagonalTLBR(100, 100, Color.BLUE, Color.WHITE);
        result = gradD1.export("out/gradD1.png");
        assert result;
        gradD1.delete();
        
        Image gradD2 = Image.genColorGradientDiagonalTRBL(100, 100, Color.CYAN, Color.GREEN);
        result = gradD2.export("out/gradD2.png");
        assert result;
        gradD2.delete();
        
        Image gradRad00 = Image.genColorGradientRadial(100, 100, 0.0, Color.BLACK, Color.LIGHT_GRAY);
        result = gradRad00.export("out/gradRad00.png");
        assert result;
        gradRad00.delete();
        
        Image gradRad025 = Image.genColorGradientRadial(100, 100, 0.25, Color.BLACK, Color.LIGHT_GRAY);
        result = gradRad025.export("out/gradRad025.png");
        assert result;
        gradRad025.delete();
        
        Image gradRad05 = Image.genColorGradientRadial(100, 100, 0.5, Color.BLACK, Color.LIGHT_GRAY);
        result = gradRad05.export("out/gradRad05.png");
        assert result;
        gradRad05.delete();
        
        Image gradRad075 = Image.genColorGradientRadial(100, 100, 0.75, Color.BLACK, Color.LIGHT_GRAY);
        result = gradRad075.export("out/gradRad075.png");
        assert result;
        gradRad075.delete();
        
        Image gradRad10 = Image.genColorGradientRadial(100, 100, 1.0, Color.BLACK, Color.LIGHT_GRAY);
        result = gradRad10.export("out/gradRad10.png");
        assert result;
        gradRad10.delete();
        
        Image checkered = Image.genColorCheckered(100, 100, 10, 10, Color.RED, Color.CYAN);
        result = checkered.export("out/checkered.png");
        assert result;
        checkered.delete();
        
        Image noiseWhite = Image.genNoiseWhite(100, 100, 0.5);
        result = noiseWhite.export("out/noiseWhite.png");
        assert result;
        noiseWhite.delete();
        
        Image noisePerlin = Image.genNoisePerlin(100, 100, 0, 0, 0.5);
        result = noisePerlin.export("out/noisePerlin.png");
        assert result;
        noisePerlin.delete();
        
        Image noiseCellular = Image.genNoiseCellular(100, 100, 10);
        result = noiseCellular.export("out/noiseCellular.png");
        assert result;
        noiseCellular.delete();
        
        Image manipulee = manipulee();
        
        Image copy = manipulee.copy();
        result = copy.export("out/copy.png");
        assert result;
        copy.delete();
        
        Image sub = manipulee.subImage(50, 0, 50, 50);
        result = sub.export("out/sub.png");
        assert result;
        sub.delete();
        
        manipulee.delete();
        
        Image reformat = manipulee().reformat(ColorFormat.GRAY_ALPHA);
        result = reformat.export("out/reformat.png", true);
        assert result;
        reformat.delete();
        
        Image mipmaps = Image.genNoisePerlin(100, 100, 0, 0, 0.5).genMipmaps();
        result = mipmaps.export("out/mipmaps.png", true);
        assert result;
        mipmaps.delete();
        
        Image crop = manipulee().crop(50, 50, 50, 50);
        crop.export("out/crop.png");
        crop.delete();
        
        Image toPOT = manipulee().toPOT(Color.MAGENTA);
        result = toPOT.export("out/toPOT.png");
        assert result;
        toPOT.delete();
        
        Image resizeBigger = manipulee().resize(200, 200);
        result = resizeBigger.export("out/resizeBigger.png");
        assert result;
        resizeBigger.delete();
        
        Image resizeSmaller = manipulee().resize(50, 50);
        result = resizeSmaller.export("out/resizeSmaller.png");
        assert result;
        resizeSmaller.delete();
        
        Image resizeNNBigger = manipulee().resizeNN(200, 200);
        result = resizeNNBigger.export("out/resizeNNBigger.png");
        assert result;
        resizeNNBigger.delete();
        
        Image resizeNNSmaller = manipulee().resizeNN(50, 50);
        result = resizeNNSmaller.export("out/resizeNNSmaller.png");
        assert result;
        resizeNNSmaller.delete();
        
        // TODO - This is accessing the wrong memory
        Image resizeCanvasBigger = manipulee().resizeCanvas(-25, -50, 200, 200, Color.WHITE);
        result = resizeCanvasBigger.export("out/resizeCanvasBigger.png");
        assert result;
        resizeCanvasBigger.delete();
        
        Image resizeCanvasSmaller = manipulee().resizeCanvas(50, 0, 50, 50, Color.WHITE);
        result = resizeCanvasSmaller.export("out/resizeCanvasSmaller.png");
        assert result;
        resizeCanvasSmaller.delete();
        
        Image quantize5650 = manipulee().quantize(5, 6, 5, 0);
        result = quantize5650.export("out/quantize5650.png");
        assert result;
        quantize5650.delete();
        
        Image quantize6666 = manipulee().quantize(6, 6, 6, 6);
        result = quantize6666.export("out/quantize6666.png");
        assert result;
        quantize6666.delete();
        
        Image quantize5551 = manipulee().quantize(5, 5, 5, 1);
        result = quantize5551.export("out/quantize5551.png");
        assert result;
        quantize5551.delete();
        
        Image quantize4444 = manipulee().quantize(4, 4, 4, 4);
        result = quantize4444.export("out/quantize4444.png");
        assert result;
        quantize4444.delete();
        
        Image quantize3320 = manipulee().quantize(3, 3, 2, 0);
        result = quantize3320.export("out/quantize3320.png");
        assert result;
        quantize3320.delete();
        
        Image quantize2420 = manipulee().quantize(2, 4, 2, 0);
        result = quantize2420.export("out/quantize2420.png");
        assert result;
        quantize2420.delete();
        
        Image quantize1610 = manipulee().quantize(1, 6, 1, 0);
        result = quantize1610.export("out/quantize1610.png");
        assert result;
        quantize1610.delete();
        
        Image dither5650 = manipulee().dither(5, 6, 5, 0);
        result = dither5650.export("out/dither5650.png");
        assert result;
        dither5650.delete();
        
        Image dither6666 = manipulee().dither(6, 6, 6, 6);
        result = dither6666.export("out/dither6666.png");
        assert result;
        dither6666.delete();
        
        Image dither5551 = manipulee().dither(5, 5, 5, 1);
        result = dither5551.export("out/dither5551.png");
        assert result;
        dither5551.delete();
        
        Image dither4444 = manipulee().dither(4, 4, 4, 4);
        result = dither4444.export("out/dither4444.png");
        assert result;
        dither4444.delete();
        
        Image dither3320 = manipulee().dither(3, 3, 2, 0);
        result = dither3320.export("out/dither3320.png");
        assert result;
        dither3320.delete();
        
        Image dither2420 = manipulee().dither(2, 4, 2, 0);
        result = dither2420.export("out/dither2420.png");
        assert result;
        dither2420.delete();
        
        Image dither1610 = manipulee().dither(1, 6, 1, 0);
        result = dither1610.export("out/dither1610.png");
        assert result;
        dither1610.delete();
        
        Image flipV = manipulee().flipV();
        result = flipV.export("out/flipV.png");
        assert result;
        flipV.delete();
        
        Image flipH = manipulee().flipH();
        result = flipH.export("out/flipH.png");
        assert result;
        flipH.delete();
        
        Image rotateCW = manipulee().rotateCW();
        result = rotateCW.export("out/rotateCW.png");
        assert result;
        rotateCW.delete();
        
        Image rotateCCW = manipulee().rotateCCW();
        result = rotateCCW.export("out/rotateCCW.png");
        assert result;
        rotateCCW.delete();
        
        Image colorTint = manipulee().colorTint(Color.DARKER_CYAN);
        result = colorTint.export("out/colorTint.png");
        assert result;
        colorTint.delete();
        
        Image colorGrayscale = manipulee().colorGrayscale();
        result = colorGrayscale.export("out/colorGrayscale.png");
        assert result;
        colorGrayscale.delete();
        
        Image colorBrightness50 = manipulee().colorBrightness(50);
        result = colorBrightness50.export("out/colorBrightness50.png");
        assert result;
        colorBrightness50.delete();
        
        Image colorBrightness_50 = manipulee().colorBrightness(-50);
        result = colorBrightness_50.export("out/colorBrightness-50.png");
        assert result;
        colorBrightness_50.delete();
        
        Image colorContrast50 = manipulee().colorContrast(50);
        result = colorContrast50.export("out/colorContrast50.png");
        assert result;
        colorContrast50.delete();
        
        Image colorContrast_50 = manipulee().colorContrast(-50);
        result = colorContrast_50.export("out/colorContrast-50.png");
        assert result;
        colorContrast_50.delete();
        
        Image colorGamma = manipulee().colorGamma(1.5);
        result = colorGamma.export("out/colorGamma.png");
        assert result;
        colorGamma.delete();
        
        Image colorInvert = manipulee().colorInvert();
        result = colorInvert.export("out/colorInvert.png");
        assert result;
        colorInvert.delete();
        
        Image colorBrighter = manipulee().colorBrighter(0.5);
        result = colorBrighter.export("out/colorBrighter.png");
        assert result;
        colorBrighter.delete();
        
        Image colorDarker = manipulee().colorDarker(0.5);
        result = colorDarker.export("out/colorDarker.png");
        assert result;
        colorDarker.delete();
        
        Image colorReplace = manipulee().colorReplace(Color.GREEN, Color.CYAN, 0.5);
        result = colorReplace.export("out/colorReplace.png");
        assert result;
        colorReplace.delete();
        
        Image alphaClear = manipulee().alphaClear(Color.YELLOW, 0.5);
        result = alphaClear.export("out/alphaClear.png");
        assert result;
        alphaClear.delete();
        
        Image mask      = Image.genColorGradientRadial(100, 100, 0.8, Color.WHITE, Color.BLACK);
        Image alphaMask = manipulee().alphaMask(mask);
        result = alphaMask.export("out/alphaMask.png");
        assert result;
        alphaMask.delete();
        mask.delete();
        
        Image alphaPreMultiply = manipulee().alphaPreMultiply();
        result = alphaPreMultiply.export("out/alphaPreMultiply.png");
        assert result;
        alphaPreMultiply.delete();
        
        Image        getPalette = Image.genColorCheckered(100, 100, 10, 10, Color.RED, Color.CYAN);
        Color.Buffer palette    = getPalette.getPalette(10);
        assert palette.capacity() == 10;
        assert palette.remaining() == 2;
        getPalette.delete();
        
        Image drawPixel = manipulee();
        drawPixel.drawPixel(50, 50, Color.WHITE);
        result = drawPixel.export("out/drawPixel.png");
        assert result;
        drawPixel.delete();
        
        Image clear = manipulee();
        clear.clear(Color.DARK_RED);
        result = clear.export("out/clear.png");
        assert result;
        clear.delete();
        
        Image drawLineNONE = manipulee();
        for (int i = 0, lines = 25; i < lines; i++)
        {
            int a = 0;
            int b = i * (100 / lines);
            int c = 99 - a;
            int d = 99 - b;
            drawLineNONE.drawLine(a, b, b, c, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
            drawLineNONE.drawLine(b, c, c, d, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
            drawLineNONE.drawLine(c, d, d, a, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
            drawLineNONE.drawLine(d, a, a, b, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        }
        result = drawLineNONE.export("out/drawLineNONE.png");
        assert result;
        drawLineNONE.delete();
        
        Image drawRectangle = manipulee();
        drawRectangle.drawRectangle(25, 25, 50, 50, 4, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = drawRectangle.export("out/drawRectangle.png");
        assert result;
        drawRectangle.delete();
        
        Image fillRectangle = manipulee();
        fillRectangle.fillRectangle(25, 25, 50, 50, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = fillRectangle.export("out/fillRectangle.png");
        assert result;
        fillRectangle.delete();
        
        Image drawCircle = manipulee();
        drawCircle.drawCircle(50, 50, 50, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = drawCircle.export("out/drawCircle.png");
        assert result;
        drawCircle.delete();
        
        Image fillCircle = manipulee();
        fillCircle.fillCircle(50, 50, 50, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = fillCircle.export("out/fillCircle.png");
        assert result;
        fillCircle.delete();
        
        Image drawTriangle = manipulee();
        drawTriangle.drawTriangle(0, 99, 50, 0, 99, 99, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = drawTriangle.export("out/drawTriangle.png");
        assert result;
        drawTriangle.delete();
        
        Image fillTriangle = manipulee();
        fillTriangle.fillTriangle(0, 99, 50, 0, 99, 99, Color.DARK_MAGENTA, BlendMode.ADDITIVE);
        result = fillTriangle.export("out/fillTriangle.png");
        assert result;
        fillTriangle.delete();
        
        // Image drawnImage = manipulee().flipH().flipV();
        Image drawnImage = Image.genColorGradientRadial(100, 100, 0.8, Color.WHITE, Color.BLANK);
        
        Image drawImageNONE = manipulee();
        drawImageNONE.drawImage(drawnImage,
                                0, 0, 100, 100,
                                25, 25, 50, 50,
                                BlendMode.NONE);
        result = drawImageNONE.export("out/drawImageNONE.png");
        assert result;
        drawImageNONE.delete();
        
        Image drawImageDEFAULT = manipulee();
        drawImageDEFAULT.drawImage(drawnImage,
                                   0, 0, 100, 100,
                                   25, 25, 50, 50,
                                   BlendMode.DEFAULT);
        result = drawImageDEFAULT.export("out/drawImageDEFAULT.png");
        assert result;
        drawImageDEFAULT.delete();
        
        Image drawImageADDITIVE = manipulee();
        drawImageADDITIVE.drawImage(drawnImage,
                                    0, 0, 100, 100,
                                    25, 25, 50, 50,
                                    BlendMode.ADDITIVE);
        result = drawImageADDITIVE.export("out/drawImageADDITIVE.png");
        assert result;
        drawImageADDITIVE.delete();
        
        Image drawImageMULTIPLICATIVE = manipulee();
        drawImageMULTIPLICATIVE.drawImage(drawnImage,
                                          0, 0, 100, 100,
                                          25, 25, 50, 50,
                                          BlendMode.MULTIPLICATIVE);
        result = drawImageMULTIPLICATIVE.export("out/drawImageMULTIPLICATIVE.png");
        assert result;
        drawImageMULTIPLICATIVE.delete();
        
        Image drawImageSTENCIL = manipulee();
        drawImageSTENCIL.drawImage(drawnImage,
                                   0, 0, 100, 100,
                                   25, 25, 50, 50,
                                   BlendMode.STENCIL);
        result = drawImageSTENCIL.export("out/drawImageSTENCIL.png");
        assert result;
        drawImageSTENCIL.delete();
        
        Image drawImageADD = manipulee();
        drawImageADD.drawImage(drawnImage,
                               0, 0, 100, 100,
                               25, 25, 50, 50,
                               BlendMode.ADD_COLORS);
        result = drawImageADD.export("out/drawImageADD.png");
        assert result;
        drawImageADD.delete();
        
        Image drawImageSUB = manipulee();
        drawImageSUB.drawImage(drawnImage,
                               0, 0, 100, 100,
                               25, 25, 50, 50,
                               BlendMode.SUB_COLORS);
        result = drawImageSUB.export("out/drawImageSUB.png");
        assert result;
        drawImageSUB.delete();
        
        Image drawImageILLUMINATE = manipulee();
        drawImageILLUMINATE.drawImage(drawnImage,
                                      0, 0, 100, 100,
                                      25, 25, 50, 50,
                                      BlendMode.ILLUMINATE);
        result = drawImageILLUMINATE.export("out/drawImageILLUMINATE.png");
        assert result;
        drawImageILLUMINATE.delete();
        
        drawnImage.delete();
    }
    
    private static Image manipulee()
    {
        return Image.genColorGradient(100, 100, Color.RED, Color.GREEN, Color.BLUE, Color.BLANK);
    }
}
