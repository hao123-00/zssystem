#!/usr/bin/env python3
"""从文档图片中提取红色印章（红框+红字），保存为透明背景 PNG。"""
import sys
from pathlib import Path

try:
    from PIL import Image
    import numpy as np
except ImportError:
    print("请先安装: pip install Pillow numpy")
    sys.exit(1)

# 源图片路径（优先使用 Cursor 保存的 assets 路径，否则用命令行参数）
_default_src = Path(__file__).resolve().parent / ".cursor" / "projects" / "Users-czd-zssystem" / "assets" / "_cgi-bin_mmwebwx-bin_webwxgetmsgimg___MsgID_8486665879372198142_skey__crypt_1f3b7bb8_4f400deb187b7f2143024b9707e5aa3f_mmweb_appid_wx_webfilehelper-02bed406-713c-4be8-abda-5cda79d22b6b.png"
_alt_src = Path.home() / ".cursor" / "projects" / "Users-czd-zssystem" / "assets" / "_cgi-bin_mmwebwx-bin_webwxgetmsgimg___MsgID_8486665879372198142_skey__crypt_1f3b7bb8_4f400deb187b7f2143024b9707e5aa3f_mmweb_appid_wx_webfilehelper-02bed406-713c-4be8-abda-5cda79d22b6b.png"
# 输出到项目根目录，文件名：印章.png
OUT = Path(__file__).resolve().parent / "印章.png"


def is_red(r, g, b, a=255):
    """判断是否为红色系（含淡红、半透明红）。"""
    if a < 30:
        return False
    r, g, b = int(r), int(g), int(b)
    # 红色：R 明显大于 G、B
    if r < 60:
        return False
    return r > g and r > b and (r - g) + (r - b) >= 40


def extract_red(img: Image.Image) -> Image.Image:
    w, h = img.size
    img = img.convert("RGBA")
    arr = np.array(img)
    out = np.zeros((h, w, 4), dtype=np.uint8)
    for y in range(h):
        for x in range(w):
            r, g, b, a = arr[y, x]
            if is_red(r, g, b, a):
                # 保留红色，略微加深并设不透明
                out[y, x] = (min(255, r + 20), g, b, 220)
            else:
                out[y, x] = (0, 0, 0, 0)
    return Image.fromarray(out, "RGBA")


def main():
    SRC = _default_src if _default_src.exists() else _alt_src
    if len(sys.argv) > 1:
        SRC = Path(sys.argv[1])
    if not SRC.exists():
        print(f"源图片不存在: {SRC}")
        sys.exit(1)
    img = Image.open(SRC).convert("RGBA")
    stamp = extract_red(img)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    stamp.save(OUT, "PNG")
    print(f"已保存: {OUT}")


if __name__ == "__main__":
    main()
