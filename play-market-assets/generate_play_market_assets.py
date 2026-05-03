from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parent
RAW = ROOT / "raw"
OUT = ROOT / "final"
OUT.mkdir(parents=True, exist_ok=True)

W, H = 1080, 1920
FEATURE_W, FEATURE_H = 1024, 500

CREAM = (247, 240, 230)
CREAM_DARK = (237, 228, 212)
SURFACE = (253, 248, 242)
INK = (28, 18, 8)
MUTED = (122, 102, 82)
BURGUNDY = (139, 26, 26)
BURGUNDY_DARK = (104, 18, 18)
BURGUNDY_DEEP = (69, 10, 12)
ORANGE = (232, 136, 26)
RED = (217, 48, 37)
YELLOW = (245, 197, 24)


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        "C:/Windows/Fonts/bahnschrift.ttf",
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf",
    ]
    for candidate in candidates:
        path = Path(candidate)
        if path.exists():
            return ImageFont.truetype(str(path), size=size)
    return ImageFont.load_default()


FONT_TITLE = font(74, bold=True)
FONT_TITLE_SMALL = font(56, bold=True)
FONT_BODY = font(31)
FONT_BODY_BOLD = font(34, bold=True)
FONT_KICKER = font(22, bold=True)
FONT_FEATURE = font(72, bold=True)
FONT_FEATURE_BODY = font(30)


def vertical_gradient(size: tuple[int, int], top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGB", size, top)
    px = img.load()
    width, height = size
    for y in range(height):
        t = y / max(1, height - 1)
        color = tuple(round(top[i] * (1 - t) + bottom[i] * t) for i in range(3))
        for x in range(width):
            px[x, y] = color
    return img


def radial_glow(base: Image.Image, center: tuple[int, int], radius: int, color: tuple[int, int, int], alpha: int) -> None:
    overlay = Image.new("RGBA", base.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    cx, cy = center
    for r in range(radius, 0, -8):
        a = int(alpha * (1 - r / radius) ** 2)
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(*color, a))
    base.alpha_composite(overlay)


def draw_paw(draw: ImageDraw.ImageDraw, x: float, y: float, scale: float, rotation: float, color: tuple[int, int, int, int]) -> None:
    angle = math.radians(rotation)
    ca, sa = math.cos(angle), math.sin(angle)

    def rot(dx: float, dy: float) -> tuple[float, float]:
        return x + dx * ca - dy * sa, y + dx * sa + dy * ca

    def oval(cx: float, cy: float, rw: float, rh: float) -> None:
        draw.ellipse((cx - rw, cy - rh, cx + rw, cy + rh), fill=color)

    px, py = rot(0, scale * 0.22)
    oval(px, py, scale * 0.29, scale * 0.23)
    for i in range(4):
        f = i / 3 - 0.5
        tx = f * scale * 0.70
        ty = -scale * 0.24 - (1 - (f * f * 4)) * scale * 0.06
        px, py = rot(tx, ty)
        oval(px, py, scale * 0.15, scale * 0.12)


def decorate_background(img: Image.Image) -> None:
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    paws = [
        (110, 210, 76, -18),
        (945, 160, 68, 25),
        (65, 1160, 68, 8),
        (940, 1030, 72, -18),
        (240, 1730, 70, 25),
        (825, 1820, 80, -25),
    ]
    for x, y, s, r in paws:
        draw_paw(draw, x, y, s, r, (62, 31, 0, 18))
    img.alpha_composite(overlay)


def draw_editorial_background(img: Image.Image, accent: tuple[int, int, int]) -> None:
    """Add the branded store-art wrapper around real screenshots."""
    radial_glow(img, (W // 2, 500), 720, accent, 92)
    radial_glow(img, (115, 150), 420, BURGUNDY, 46)
    radial_glow(img, (960, 1720), 520, ORANGE, 42)
    decorate_background(img)

    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)

    # Oversized off-canvas loops give the screenshots a CatLoop-specific frame.
    draw.ellipse((-250, -300, 520, 470), outline=(*BURGUNDY, 34), width=22)
    draw.ellipse((650, 1350, 1280, 1980), outline=(*ORANGE, 42), width=18)
    draw.ellipse((178, 318, 902, 1042), outline=(*BURGUNDY, 30), width=10)

    # A soft top panel keeps store copy legible without feeling boxed-in.
    draw.rounded_rectangle((96, 56, 984, 292), radius=58, fill=(255, 247, 236, 122))
    draw.rounded_rectangle((96, 56, 984, 292), radius=58, outline=(255, 255, 255, 95), width=2)

    # Thin brand ribbons, subtle enough not to compete with the app capture.
    draw.line((130, 302, 950, 302), fill=(*accent, 120), width=4)
    draw.line((220, 316, 860, 316), fill=(*BURGUNDY, 58), width=2)
    img.alpha_composite(overlay)


def rounded_rectangle_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size[0], size[1]), radius=radius, fill=255)
    return mask


def crop_app(raw_name: str) -> Image.Image:
    src = Image.open(RAW / raw_name).convert("RGB")
    # Remove Android status/navigation chrome, then center-crop to a 9:16 frame.
    left, top, right, bottom = 0, 120, src.width, src.height - 135
    src = src.crop((left, top, right, bottom))
    target_ratio = 9 / 16
    src_ratio = src.width / src.height
    if src_ratio < target_ratio:
        new_h = int(src.width / target_ratio)
        y = max(0, (src.height - new_h) // 2)
        src = src.crop((0, y, src.width, y + new_h))
    else:
        new_w = int(src.height * target_ratio)
        x = max(0, (src.width - new_w) // 2)
        src = src.crop((x, 0, x + new_w, src.height))
    return src.resize((820, 1458), Image.Resampling.LANCZOS)


def paste_shadowed_card(base: Image.Image, shot: Image.Image, xy: tuple[int, int], radius: int = 46) -> None:
    x, y = xy
    shadow = Image.new("RGBA", (shot.width + 70, shot.height + 70), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.rounded_rectangle((35, 28, shot.width + 35, shot.height + 28), radius=radius, fill=(72, 26, 16, 78))
    shadow = shadow.filter(ImageFilter.GaussianBlur(24))
    base.alpha_composite(shadow, (x - 35, y - 28))

    mask = rounded_rectangle_mask(shot.size, radius)
    base.paste(shot.convert("RGBA"), xy, mask)
    border = Image.new("RGBA", shot.size, (0, 0, 0, 0))
    bd = ImageDraw.Draw(border)
    bd.rounded_rectangle((1, 1, shot.width - 2, shot.height - 2), radius=radius, outline=(255, 255, 255, 170), width=3)
    bd.rounded_rectangle((4, 4, shot.width - 5, shot.height - 5), radius=radius - 2, outline=(139, 26, 26, 46), width=2)
    base.alpha_composite(border, xy)


def text_center(draw: ImageDraw.ImageDraw, text: str, y: int, fnt: ImageFont.FreeTypeFont, fill: tuple[int, int, int]) -> None:
    box = draw.textbbox((0, 0), text, font=fnt)
    draw.text(((W - (box[2] - box[0])) / 2, y), text, font=fnt, fill=fill)


def draw_header(draw: ImageDraw.ImageDraw, title: str, subtitle: str, kicker: str = "CATLOOP") -> None:
    kbox = draw.textbbox((0, 0), kicker, font=FONT_KICKER)
    draw.text(((W - (kbox[2] - kbox[0])) / 2, 76), kicker, font=FONT_KICKER, fill=ORANGE)
    title_font = FONT_TITLE if len(title) <= 23 else FONT_TITLE_SMALL
    text_center(draw, title, 120, title_font, INK)
    if subtitle:
        text_center(draw, subtitle, 208, FONT_BODY, MUTED)


def make_phone_asset(filename: str, raw_name: str, title: str, subtitle: str, accent: tuple[int, int, int] = ORANGE) -> None:
    base = vertical_gradient((W, H), (255, 247, 235), (229, 211, 187)).convert("RGBA")
    draw_editorial_background(base, accent)

    draw = ImageDraw.Draw(base)
    draw_header(draw, title, subtitle)

    shot = crop_app(raw_name)
    paste_shadowed_card(base, shot, ((W - shot.width) // 2, 365))

    base.convert("RGB").save(OUT / filename, optimize=True)


def draw_cat_sprite(base: Image.Image, center: tuple[int, int], size: int, rotate: float = 0) -> None:
    cat = Image.open(ROOT.parent / "app/src/main/res/drawable/cat.png").convert("RGBA")
    cat.thumbnail((size, size), Image.Resampling.LANCZOS)
    if rotate:
        cat = cat.rotate(rotate, expand=True, resample=Image.Resampling.BICUBIC)
    x = center[0] - cat.width // 2
    y = center[1] - cat.height // 2
    base.alpha_composite(cat, (x, y))


def make_feature_graphic() -> None:
    base = vertical_gradient((FEATURE_W, FEATURE_H), (255, 248, 237), (230, 207, 178)).convert("RGBA")
    radial_glow(base, (766, 245), 450, ORANGE, 132)
    radial_glow(base, (250, 270), 430, BURGUNDY, 72)
    draw = ImageDraw.Draw(base)

    # Deep brand panel, kept centered enough to avoid Play crop/overlay zones.
    draw.rounded_rectangle((46, 52, 500, 448), radius=58, fill=(*BURGUNDY_DEEP, 238))
    draw.rounded_rectangle((62, 68, 484, 432), radius=48, outline=(255, 255, 255, 46), width=2)

    # Large gameplay motif.
    ring = (596, 36, 966, 406)
    draw.ellipse((ring[0] - 24, ring[1] - 24, ring[2] + 24, ring[3] + 24), outline=(*BURGUNDY, 34), width=20)
    draw.ellipse(ring, outline=(*BURGUNDY_DARK, 255), width=24)
    draw.ellipse((ring[0] + 35, ring[1] + 35, ring[2] - 35, ring[3] - 35), outline=(255, 248, 237, 185), width=3)
    for angle in (20, 142, 260):
        cx, cy = 775, 221
        rad = math.radians(angle)
        bx = cx + math.cos(rad) * 179
        by = cy + math.sin(rad) * 179
        px = -math.sin(rad)
        py = math.cos(rad)
        tip = (cx + math.cos(rad) * 132, cy + math.sin(rad) * 132)
        p1 = (bx + px * 20, by + py * 20)
        p2 = (bx - px * 20, by - py * 20)
        draw.polygon([tip, p1, p2], fill=RED, outline=BURGUNDY_DARK)
    draw_cat_sprite(base, (744, 250), 102, rotate=15)

    draw.text((84, 100), "CatLoop", font=FONT_FEATURE, fill=(255, 248, 237))
    draw.text((88, 188), "Bounce through the loop", font=FONT_FEATURE_BODY, fill=(255, 185, 89))
    draw.text((90, 242), "Rotate. Dodge. Bounce.", font=FONT_BODY, fill=(238, 218, 198))
    draw.rounded_rectangle((88, 328, 396, 386), radius=28, fill=(*ORANGE, 242))
    draw.text((126, 342), "by AngryKitten", font=FONT_KICKER, fill=BURGUNDY_DEEP)

    base.convert("RGB").save(OUT / "feature_graphic_1024x500.png", optimize=True)


def make_clean_screenshot(filename: str, raw_name: str) -> None:
    shot = Image.open(RAW / raw_name).convert("RGB")
    # Crop to Google Play's recommended portrait 9:16 without overlays.
    top, bottom = 120, shot.height - 135
    shot = shot.crop((0, top, shot.width, bottom))
    new_h = int(shot.width / (9 / 16))
    y = max(0, (shot.height - new_h) // 2)
    shot = shot.crop((0, y, shot.width, y + new_h)).resize((W, H), Image.Resampling.LANCZOS)
    shot.save(OUT / filename, optimize=True)


def main() -> None:
    make_phone_asset(
        "phone_01_menu_1080x1920.png",
        "late.png",
        "One-tap cat chaos",
        "Jump in fast and chase your best score",
        ORANGE,
    )
    make_phone_asset(
        "phone_02_gameplay_1080x1920.png",
        "gameplay.png",
        "Rotate. Dodge. Bounce.",
        "Guide the cat around a shifting spike loop",
        RED,
    )
    make_phone_asset(
        "phone_03_spikes_1080x1920.png",
        "pause.png",
        "Fresh danger every bounce",
        "The obstacle pattern changes as you score",
        BURGUNDY,
    )
    make_phone_asset(
        "phone_04_settings_1080x1920.png",
        "settings.png",
        "Tune the feel",
        "Music, haptics and score controls",
        ORANGE,
    )
    make_clean_screenshot("clean_menu_1080x1920.png", "late.png")
    make_clean_screenshot("clean_gameplay_1080x1920.png", "gameplay.png")
    make_clean_screenshot("clean_settings_1080x1920.png", "settings.png")
    make_feature_graphic()


if __name__ == "__main__":
    main()
