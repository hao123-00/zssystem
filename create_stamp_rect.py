#!/usr/bin/env python3
"""按参考格式生成印章：红矩形边框、黑色内底、红色「注塑部印」两行居中。"""
from pathlib import Path
try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("请安装: pip install Pillow")
    exit(1)

# 横版尺寸（宽 > 高）
W, H = 320, 160
BORDER = 12  # 红框线宽
OUT = Path(__file__).resolve().parent / "印章.png"

def main():
    img = Image.new("RGBA", (W, H), (0, 0, 0, 255))  # 黑色底
    draw = ImageDraw.Draw(img)
    red = (200, 0, 0, 255)
    # 红矩形边框（外框）
    draw.rectangle([0, 0, W - 1, H - 1], outline=red, width=BORDER)
    # 内区留白后写字的区域
    inner = (BORDER + 8, BORDER + 8, W - BORDER - 8, H - BORDER - 8)
    # 字体：尽量用系统中文字体
    try:
        font = ImageFont.truetype("/System/Library/Fonts/PingFang.ttc", 42)
    except Exception:
        try:
            font = ImageFont.truetype("/System/Library/Fonts/Supplemental/Songti.ttc", 42)
        except Exception:
            font = ImageFont.load_default()
    text1, text2 = "注塑", "部印"
    # 两行文字居中
    b1 = draw.textbbox((0, 0), text1, font=font)
    b2 = draw.textbbox((0, 0), text2, font=font)
    w1 = b1[2] - b1[0]
    w2 = b2[2] - b2[0]
    h1 = b1[3] - b1[1]
    h2 = b2[3] - b2[1]
    cx = W / 2
    y1 = (H - h1 - h2 - 16) / 2 + h1
    y2 = y1 + 16 + h2
    draw.text((cx - w1 / 2, y1 - h1), text1, fill=red, font=font)
    draw.text((cx - w2 / 2, y2 - h2), text2, fill=red, font=font)
    img.save(OUT)
    print("已保存:", OUT)

if __name__ == "__main__":
    main()
