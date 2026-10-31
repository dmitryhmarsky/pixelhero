"""Мини-рендерер пиксель-арта: части тела с объёмным освещением, внутренний и внешний контур.
Спрайт = сетка (материал, тон 0..4). Материал задаёт цвет, тон 0..4 строит рампу от тени к блику.
Всё рисуется лицом ВПРАВО."""
import numpy as np
from PIL import Image

LIGHT = np.array([-0.50, -0.66, 0.56])
LIGHT = LIGHT / np.linalg.norm(LIGHT)
LZ = float(LIGHT[2])

SHADOW = (0x1B, 0x1A, 0x3A)
WARM = (0xFF, 0xF1, 0xD0)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def hexrgb(v):
    return ((v >> 16) & 255, (v >> 8) & 255, v & 255)


def ramp(base):
    """5 тонов: тень (холодная), полутень, база, свет (тёплый), блик."""
    if isinstance(base, int):
        base = hexrgb(base)
    return [mix(base, SHADOW, .70), mix(base, SHADOW, .38), base, mix(base, WARM, .32), mix(base, (255, 255, 255), .68)]


OUT_RGB = (0x12, 0x10, 0x1C)


class Spr:
    def __init__(s, w, h):
        s.w, s.h = w, h
        s.mat = np.full((h, w), -1, int)
        s.tone = np.zeros((h, w), int)
        s.part = np.zeros((h, w), int)
        s.sep = np.zeros((h, w), int)
        s.pid = 0
        Y, X = np.mgrid[0:h, 0:w]
        s.X = X + .5
        s.Y = Y + .5

    # ---- низкоуровневое ----
    def _commit(s, mask, H, mat, gain, bias, sep, tone, zs):
        if not mask.any():
            return
        s.pid += 1
        if tone is None:
            Hz = np.where(mask, H, 0.0) * zs
            gy, gx = np.gradient(Hz)
            nx, ny, nz = -gx, -gy, np.ones_like(Hz)
            n = np.sqrt(nx * nx + ny * ny + nz * nz)
            lam = (nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2]) / n
            t = np.floor(2.5 + (lam - LZ) * gain + bias).clip(0, 4).astype(int)
        elif np.isscalar(tone):
            t = np.full(mask.shape, int(tone))
        else:
            t = np.asarray(tone).astype(int)
        s.mat[mask] = mat
        s.tone[mask] = t[mask]
        s.part[mask] = s.pid
        s.sep[mask] = sep

    # ---- примитивы ----
    def ell(s, cx, cy, rx, ry, mat, rz=None, n=2.0, gain=3.0, bias=0.0, sep=0, tone=None, zs=1.0, expo=None):
        nx = (s.X - cx) / rx
        ny = (s.Y - cy) / ry
        sc = np.abs(nx) ** n + np.abs(ny) ** n
        mask = sc <= 1.0
        if rz is None:
            rz = min(rx, ry)
        if expo is None:
            expo = 0.5 if n <= 2.0 else 0.33
        H = rz * np.clip(1 - sc, 0, 1) ** expo
        s._commit(mask, H, mat, gain, bias, sep, tone, zs)

    def cap(s, x0, y0, x1, y1, r0, mat, r1=None, gain=3.0, bias=0.0, sep=0, tone=None, zs=1.0):
        if r1 is None:
            r1 = r0
        dx, dy = x1 - x0, y1 - y0
        L2 = dx * dx + dy * dy
        t = np.clip(((s.X - x0) * dx + (s.Y - y0) * dy) / (L2 if L2 > 0 else 1), 0, 1)
        px, py = x0 + t * dx, y0 + t * dy
        d = np.hypot(s.X - px, s.Y - py)
        r = r0 + (r1 - r0) * t
        mask = d <= r
        H = r * np.sqrt(np.clip(1 - (d / r) ** 2, 0, 1))
        s._commit(mask, H, mat, gain, bias, sep, tone, zs)

    def _inpoly(s, pts):
        inside = np.zeros((s.h, s.w), bool)
        n = len(pts)
        for i in range(n):
            x0, y0 = pts[i]
            x1, y1 = pts[(i + 1) % n]
            if y0 == y1:
                continue
            cond = (y0 > s.Y) != (y1 > s.Y)
            xint = (x1 - x0) * (s.Y - y0) / (y1 - y0) + x0
            inside ^= cond & (s.X < xint)
        return inside

    def poly(s, pts, mat, bevel=2, rz=2.0, gain=3.0, bias=0.0, sep=0, tone=None, zs=1.0):
        mask = s._inpoly(pts)
        H = np.zeros((s.h, s.w))
        cur = mask.copy()
        for k in range(bevel):
            H += cur
            e = cur.copy()
            e[1:, :] &= cur[:-1, :]
            e[:-1, :] &= cur[1:, :]
            e[:, 1:] &= cur[:, :-1]
            e[:, :-1] &= cur[:, 1:]
            cur = e
        H = H / max(bevel, 1) * rz
        s._commit(mask, H, mat, gain, bias, sep, tone, zs)

    def flatpoly(s, pts, mat, tone, sep=0):
        mask = s._inpoly(pts)
        s._commit(mask, None, mat, 0, 0, sep, tone, 1.0)

    def rect(s, x0, y0, x1, y1, mat, tone=2, sep=2):
        mask = (s.X > x0) & (s.X < x1) & (s.Y > y0) & (s.Y < y1)
        s._commit(mask, None, mat, 0, 0, sep, tone, 1.0)

    def px(s, x, y, mat, tone=2):
        x, y = int(x), int(y)
        if 0 <= x < s.w and 0 <= y < s.h:
            s.pid += 1
            s.mat[y, x] = mat
            s.tone[y, x] = tone
            s.part[y, x] = s.pid
            s.sep[y, x] = 2

    def line(s, x0, y0, x1, y1, mat, tone=2, w=1.0, sep=2):
        dx, dy = x1 - x0, y1 - y0
        L2 = dx * dx + dy * dy
        t = np.clip(((s.X - x0) * dx + (s.Y - y0) * dy) / (L2 if L2 > 0 else 1), 0, 1)
        d = np.hypot(s.X - (x0 + t * dx), s.Y - (y0 + t * dy))
        s._commit(d <= w / 2 + 0.01, None, mat, 0, 0, sep, tone, 1.0)

    # ---- финализация ----
    def finish(s, outline=True):
        m = s.mat
        filled = m >= 0
        tone = s.tone.copy()
        mat = m.copy()
        # внутренний шов: пиксель задней части рядом с передней становится темнее
        for dy, dx in ((0, 1), (0, -1), (1, 0), (-1, 0)):
            nb_part = np.zeros_like(s.part)
            nb_fill = np.zeros_like(filled)
            ys0, ys1 = max(0, -dy), s.h - max(0, dy)
            xs0, xs1 = max(0, -dx), s.w - max(0, dx)
            nb_part[ys0:ys1, xs0:xs1] = s.part[ys0 + dy:ys1 + dy, xs0 + dx:xs1 + dx]
            nb_fill[ys0:ys1, xs0:xs1] = filled[ys0 + dy:ys1 + dy, xs0 + dx:xs1 + dx]
            front = filled & nb_fill & (nb_part > s.part)
            dark = front & (s.sep == 0)
            line = front & (s.sep == 1)
            tone[dark] = np.minimum(tone[dark], 0)
            mat[line] = -2
        out = np.zeros_like(filled)
        if outline:
            f2 = (mat != -1)
            e = np.zeros_like(f2)
            e[1:, :] |= f2[:-1, :]
            e[:-1, :] |= f2[1:, :]
            e[:, 1:] |= f2[:, :-1]
            e[:, :-1] |= f2[:, 1:]
            out = e & ~f2
            mat[out] = -2
        # чистка шума: одиночный тон среди соседей той же части подменяется соседним
        for _ in range(2):
            t2 = tone.copy()
            for y in range(1, s.h - 1):
                for x in range(1, s.w - 1):
                    if mat[y, x] < 0:
                        continue
                    p = s.part[y, x]
                    ns = [tone[y + dy, x + dx] for dy, dx in ((0, 1), (0, -1), (1, 0), (-1, 0))
                          if s.part[y + dy, x + dx] == p and mat[y + dy, x + dx] == mat[y, x]]
                    if len(ns) >= 3 and tone[y, x] not in ns:
                        vals, cnt = np.unique(ns, return_counts=True)
                        if cnt.max() >= 2 and abs(int(vals[cnt.argmax()]) - tone[y, x]) <= 2:
                            t2[y, x] = vals[cnt.argmax()]
            tone = t2
        s.fmat, s.ftone = mat, tone

    def rows(s):
        """Текстовые строки: '.' пусто, 'k' контур, остальное = символ (материал, тон)."""
        res = []
        for y in range(s.h):
            r = []
            for x in range(s.w):
                mm = s.fmat[y, x]
                if mm == -1:
                    r.append('.')
                elif mm == -2:
                    r.append('k')
                else:
                    r.append(ALPHA[mm * 5 + s.ftone[y, x]])
            res.append(''.join(r))
        return res

    def image(s, mats):
        """mats: список из базовых цветов материалов (int 0xRRGGBB)."""
        ramps = [ramp(b) for b in mats]
        img = np.zeros((s.h, s.w, 4), np.uint8)
        for y in range(s.h):
            for x in range(s.w):
                mm = s.fmat[y, x]
                if mm == -1:
                    continue
                if mm == -2:
                    img[y, x] = (*OUT_RGB, 255)
                else:
                    img[y, x] = (*ramps[mm][s.ftone[y, x]], 255)
        return Image.fromarray(img, 'RGBA')


# 60 символов: 12 материалов x 5 тонов ('k' зарезервирован под контур, '.' под пустоту)
ALPHA = "ABCDEFGHIJLMNOPQRSTUVWXYZabcdefghijlmnopqrstuvwxyz0123456789"
assert len(ALPHA) == 60 and len(set(ALPHA)) == 60


def sheet(images, scale=6, cols=8, bg=(86, 106, 92)):
    w, h = images[0].size
    rows = (len(images) + cols - 1) // cols
    out = Image.new('RGBA', (cols * (w + 2) * scale, rows * (h + 2) * scale), (*bg, 255))
    for i, im in enumerate(images):
        big = im.resize((w * scale, h * scale), Image.NEAREST)
        out.alpha_composite(big, ((i % cols) * (w + 2) * scale + scale, (i // cols) * (h + 2) * scale + scale))
    return out
