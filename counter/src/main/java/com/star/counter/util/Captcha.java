package com.star.counter.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class Captcha {

    /**
     * 验证码
     */
    private String code;

    /**
     * 图片
     */
    private BufferedImage bufferedImage;

    /**
     * 随机数发生器
     */
    private Random random = new Random();

    public Captcha() {
    }

    public String getCode() {
        return code.toLowerCase();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Captcha(int width, int height, int codeCount, int lineCount) {
        //生成图像
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //背景色
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(getRandColor(200, 250));
        graphics.fillRect(0, 0, width, height);
        Font font = new Font("Fixedsys", Font.BOLD, height - 5);

        //生成干扰线和噪点
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = random.nextInt(width);
            int ye = random.nextInt(height);
            graphics.setColor(getRandColor(1, 255));
            graphics.drawLine(xs, ys, xe, ye);
        }
        float yawpRate = 0.01f;   //噪点的像素数占比
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            bufferedImage.setRGB(x, y, random.nextInt(255));
        }

        //添加字符
        this.code = randomStr(codeCount);
        int fontWidth = width / codeCount;
        int fontHeight = height - 5;
        for (int i = 0; i < codeCount; i++) {
            String str = this.code.substring(i, i + 1);
            graphics.setFont(font);
            graphics.setColor(getRandColor(1, 255));
            graphics.drawString(str, i * fontWidth + 3, fontHeight - 3);
        }
    }

    /**
     * 随机生成字符
     * @param codeCount 需要生成的字符数量
     * @return codeCount个随机字符
     */
    public String randomStr(int codeCount) {
        String dictionary = "ABCDEFGHJKMNOPQRSTUVWXYZabcdefghjkmnopqrstuvwxyz1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < codeCount; i++) {
            int index = ((int) (Math.random() * 1000)) % dictionary.length();
            stringBuilder.append(dictionary.charAt(index));
        }
        return stringBuilder.toString();
    }

    /**
     * 通过给定rgb范围生成随机颜色
     * @param fc rgb左端点
     * @param bc rgb右端点
     * @return 随机色
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    /**
     * 图片转base64
     * @return Captcha图片对应的base64字符串
     */
    public String getBase64ByteStr() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        String s = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        s = s.replaceAll("\n", "").replaceAll("\r", "");
        return "data:/image/jpg;base64," + s;
    }
}
