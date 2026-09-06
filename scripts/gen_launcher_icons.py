"""Rasterizes legacy (pre-API26) launcher icons from the app's adaptive-icon design.

mipmap-anydpi-v26 only resolves on API 26+; with minSdk 24 the app also needs
plain PNG mipmaps for API 24-25 devices. This mirrors ic_launcher_background.xml
and ic_launcher_foreground.xml (navy field, indigo disc, crescent moon, two
sparkles) using simple circle/diamond math instead of a full vector rasterizer.
"""
import struct
import zlib
import os

BG_OUTER = (0x0D, 0x1B, 0x2A)
BG_CIRCLE = (0x1A, 0x23, 0x7E)
MOON = (0xF5, 0xF5, 0xFA)
SPARKLE = (0xFFD54F >> 16 & 0xFF, 0xFFD54F >> 8 & 0xFF, 0xFFD54F & 0xFF)

VIEWPORT = 108.0


def lerp(a, b, t):
    return a + (b - a) * t


def blend(c1, c2, t):
    return tuple(round(lerp(c1[i], c2[i], t)) for i in range(3))


def in_circle(x, y, cx, cy, r):
    dx, dy = x - cx, y - cy
    return (dx * dx + dy * dy) <= r * r


def in_diamond(x, y, cx, cy, r):
    return abs(x - cx) + abs(y - cy) <= r


def coverage(test, x, y, samples=3):
    """Supersampled anti-aliasing: fraction of sub-pixel samples inside `test`."""
    hits = 0
    step = 1.0 / (samples + 1)
    for sx in range(samples):
        for sy in range(samples):
            px = x + step * (sx + 1)
            py = y + step * (sy + 1)
            if test(px, py):
                hits += 1
    return hits / (samples * samples)


def render(size, round_icon):
    scale = VIEWPORT / size
    pixels = bytearray(size * size * 4)

    cx_bg, cy_bg, r_bg = 54, 54, 34
    cx_moon, cy_moon, r_moon = 58, 52, 23
    cx_bite, cy_bite, r_bite = 67, 47, 20
    sparkle1 = (78, 38, 7)
    sparkle2 = (32, 67, 5)
    r_outer_mask = 54

    for py in range(size):
        for px in range(size):
            vx = (px + 0.5) * scale
            vy = (py + 0.5) * scale

            color = BG_OUTER
            if in_circle(vx, vy, cx_bg, cy_bg, r_bg):
                color = BG_CIRCLE
            if in_circle(vx, vy, cx_moon, cy_moon, r_moon) and not in_circle(vx, vy, cx_bite, cy_bite, r_bite):
                color = MOON
            if in_diamond(vx, vy, sparkle1[0], sparkle1[1], sparkle1[2]):
                color = SPARKLE
            if in_diamond(vx, vy, sparkle2[0], sparkle2[1], sparkle2[2]):
                color = SPARKLE

            alpha = 255
            if round_icon:
                dx, dy = vx - 54, vy - 54
                dist = (dx * dx + dy * dy) ** 0.5
                if dist > r_outer_mask:
                    alpha = 0
                elif dist > r_outer_mask - 1.5:
                    alpha = round(255 * (r_outer_mask - dist) / 1.5)

            idx = (py * size + px) * 4
            pixels[idx:idx + 4] = bytes((*color, alpha))

    return pixels


def write_png(path, size, pixels):
    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF)

    raw = bytearray()
    stride = size * 4
    for y in range(size):
        raw.append(0)
        raw.extend(pixels[y * stride:(y + 1) * stride])

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', size, size, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw), 9)

    with open(path, 'wb') as f:
        f.write(sig)
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))


DENSITIES = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}

if __name__ == '__main__':
    res_dir = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')
    for density, size in DENSITIES.items():
        out_dir = os.path.join(res_dir, f'mipmap-{density}')
        os.makedirs(out_dir, exist_ok=True)
        write_png(os.path.join(out_dir, 'ic_launcher.png'), size, render(size, round_icon=False))
        write_png(os.path.join(out_dir, 'ic_launcher_round.png'), size, render(size, round_icon=True))
        print(f'wrote {density} ({size}x{size})')
