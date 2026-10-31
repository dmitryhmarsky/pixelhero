import math, sys
import numpy as np
from art import Spr, sheet
from heroes import SKIN, STEEL, ARMOR, BLADE, CLS, CLS2, LEATHER, GOLD, HAIR, WOOD, GLOW, EYE

WW, HH = 56, 48
FRAMES = ['idle', 'walkA', 'walkB', 'windup', 'strike']
G = 4.2


def pose(name):
    P = dict(by=0.0, lx=0.0, fn=(26.0, 44.8), ff=(17.0, 44.8), liftn=0.0, liftf=0.0,
             ang=-62.0, hand=(34.5, 33.0), sh=(0.0, 0.0), head=(0.0, 0.0))
    if name == 'walkA':
        P.update(by=-1.0, fn=(30.0, 44.8), ff=(13.0, 44.8), liftn=1.5, ang=-52.0, hand=(34.0, 32.0))
    elif name == 'walkB':
        P.update(by=-1.0, fn=(21.0, 44.8), ff=(24.0, 44.8), liftf=2.0, ang=-72.0, hand=(33.5, 33.5))
    elif name == 'windup':
        P.update(lx=-2.0, fn=(25.0, 44.8), ff=(15.0, 44.8), ang=-140.0, hand=(27.5, 23.0), sh=(-2.0, 0.0), head=(-0.5, 0.0))
    elif name == 'strike':
        P.update(by=1.0, lx=3.0, fn=(31.0, 44.8), ff=(15.0, 44.8), ang=-3.0, hand=(34.0, 31.0), sh=(2.5, 0.0), head=(1.2, 0.5))
    return P


def leg(s, hx, hy, foot, lift):
    fx, fy = foot
    fy -= lift
    ax, ay = fx - 0.3, fy - 2.6
    kx, ky = (hx + ax) / 2 + 1.5, (hy + ay) / 2 + 0.5
    s.cap(hx, hy, kx, ky, 3.4, STEEL, 3.0, gain=G)
    s.cap(kx, ky, ax, ay, 3.0, STEEL, 2.6, gain=G)
    s.ell(kx + 1.0, ky - 0.3, 2.9, 2.6, STEEL, rz=3.0, gain=G, bias=0.2)
    s.ell(fx + 1.8, fy - 1.2, 5.2, 3.0, LEATHER, rz=2.6, gain=3.4)
    s.rect(fx - 3.2, fy + 1.2, fx + 7.0, fy + 1.9, LEATHER, 0)


def sword(s, hx, hy, ang, length=19.0):
    a = math.radians(ang)
    u = np.array([math.cos(a), math.sin(a)])
    v = np.array([-math.sin(a), math.cos(a)])
    H = np.array([hx, hy])
    p0, p1 = H - u * 3.4, H + u * 1.0
    s.cap(p0[0], p0[1], p1[0], p1[1], 1.3, LEATHER, gain=2.0)
    q = H - u * 4.2
    s.ell(q[0], q[1], 1.9, 1.9, GOLD, rz=1.9, gain=3.0)
    g0, g1 = H + u * 1.8 - v * 3.8, H + u * 1.8 + v * 3.8
    s.cap(g0[0], g0[1], g1[0], g1[1], 1.2, GOLD, gain=3.0)
    base = H + u * 3.0
    tip = base + u * length
    w = 2.3
    lit = 1 if np.dot(v, np.array([-0.6, -0.8])) > 0 else -1
    mid_t = base + u * length * 0.88
    s.flatpoly([tuple(base), tuple(base + v * lit * w), tuple(mid_t + v * lit * 0.9), tuple(tip)], BLADE, 3, sep=2)
    s.flatpoly([tuple(base), tuple(base - v * lit * w), tuple(mid_t - v * lit * 0.9), tuple(tip)], BLADE, 1, sep=2)
    e = base + u * 1.2
    f = base + u * length * 0.84
    s.line(e[0], e[1], f[0], f[1], BLADE, 4, 1.0, sep=2)
    return tip


def shield(s, cx, cy, rx, ry):
    s.ell(cx, cy, rx, ry, STEEL, rz=rx * 0.8, gain=3.0, sep=1)
    s.ell(cx + 0.3, cy + 0.3, rx * 0.78, ry * 0.8, STEEL, rz=rx * 0.35, gain=2.4, bias=-0.25, sep=2)
    # красная эмблема-кольцо и бугор
    s.ell(cx + 0.4, cy + 0.5, rx * 0.46, ry * 0.40, CLS, rz=2.2, gain=2.4, sep=2)
    s.ell(cx - 0.2, cy - 0.2, 2.2, 2.4, STEEL, rz=2.2, gain=3.2, sep=2)
    s.px(int(cx - 1.5), int(cy - 1.5), STEEL, 4)
    s.px(int(cx - 0.5), int(cy - 1.5), STEEL, 4)


def knight(P):
    s = Spr(WW, HH)
    by, lx = P['by'], P['lx']
    hip = 36.0 + by
    leg(s, 19.0 + lx * 0.3, hip, P['ff'], P['liftf'])
    leg(s, 25.0 + lx * 0.3, hip, P['fn'], P['liftn'])
    cx = 22.5 + lx
    # дальнее плечо
    s.ell(cx - 6.8, hip - 9.6, 4.4, 4.2, ARMOR, rz=4.0, gain=G, bias=-0.5)
    # тасеты и ремень
    s.ell(cx, hip + 0.4, 7.8, 4.0, ARMOR, rz=3.2, n=2.6, gain=G)
    s.poly([(cx - 1.5, hip - 1.5), (cx + 3.2, hip - 1.5), (cx + 2.8, hip + 4.2), (cx + 0.6, hip + 5.0), (cx - 1.8, hip + 4.0)], CLS, bevel=2, rz=1.8, sep=0)
    s.rect(cx - 8.0, hip - 3.0, cx + 8.4, hip - 0.9, LEATHER, 2)
    s.rect(cx - 0.8, hip - 3.5, cx + 2.6, hip - 0.4, GOLD, 3)
    # нагрудник
    s.ell(cx + 0.2, hip - 6.6, 8.4, 6.8, ARMOR, rz=5.4, n=2.5, gain=G)
    s.cap(cx - 4.5, hip - 12.5, cx + 3.5, hip - 2.5, 1.1, CLS, gain=1.5, sep=2)
    s.px(int(cx - 4), int(hip - 9), ARMOR, 4); s.px(int(cx - 3), int(hip - 10), ARMOR, 4); s.px(int(cx - 5), int(hip - 8), ARMOR, 3)
    # голова
    hx, hy = 23.2 + lx + P['head'][0], 15.2 + by + P['head'][1]
    s.ell(hx - 0.6, hy + 8.2, 5.4, 2.6, STEEL, rz=2.2, gain=G, bias=-0.6, sep=1)
    s.ell(hx, hy, 9.4, 9.0, STEEL, rz=8.4, gain=G, sep=1)
    s.ell(hx + 5.4, hy + 2.2, 4.4, 4.0, STEEL, rz=1.5, tone=0, sep=2)        # проём шлема
    s.ell(hx + 5.9, hy + 2.6, 3.0, 2.8, SKIN, rz=2.2, gain=1.4, sep=2)       # лицо
    s.px(int(hx + 6.0), int(hy + 2.0), EYE, 4)
    s.px(int(hx + 7.0), int(hy + 2.0), EYE, 3)
    s.px(int(hx + 6.0), int(hy + 3.0), EYE, 2)
    s.px(int(hx + 7.0), int(hy + 3.0), CLS2, 0)
    s.line(hx + 3.4, hy + 0.0, hx + 8.8, hy - 0.4, STEEL, 2, 1.4, sep=2)    # козырёк
    s.line(hx + 4.6, hy - 8.0, hx + 6.0, hy - 0.4, STEEL, 0, 1.0, sep=2)     # ребро
    s.ell(hx - 0.6, hy + 5.6, 3.8, 3.4, STEEL, rz=3.0, gain=G, sep=1)        # наушник-щека
    for (dx, dy, t) in ((-4, -5, 4), (-3, -5, 4), (-4, -4, 4), (-5, -3, 3), (-2, -6, 3)):
        s.px(int(hx + dx), int(hy + dy), STEEL, t)
    # плюм
    s.poly([(hx - 4.0, hy - 7.8), (hx + 2.5, hy - 9.4), (hx + 4.5, hy - 8.0), (hx - 3.0, hy - 6.0), (hx - 9.0, hy - 2.2), (hx - 11.5, hy + 2.2), (hx - 8.6, hy - 4.4)], CLS, bevel=2, rz=1.8, sep=0)
    # щит
    shx, shy = 31.5 + lx + P['sh'][0], hip - 3.0 + P['sh'][1]
    shield(s, shx, shy, 6.6, 9.0)
    # рука с мечом
    shd = (28.2 + lx, hip - 9.2)
    hand = (P['hand'][0] + lx, P['hand'][1] + by)
    elb = ((shd[0] + hand[0]) / 2 - 1.6, (shd[1] + hand[1]) / 2 + 2.4)
    s.cap(shd[0], shd[1], elb[0], elb[1], 2.9, STEEL, 2.6, gain=G)
    s.cap(elb[0], elb[1], hand[0], hand[1], 2.6, STEEL, 2.3, gain=G)
    sword(s, hand[0], hand[1], P['ang'])
    s.ell(hand[0], hand[1], 2.6, 2.6, STEEL, rz=2.8, gain=G)
    s.ell(shd[0], shd[1] - 0.2, 4.6, 4.2, ARMOR, rz=4.4, gain=G, sep=1)
    s.px(int(shd[0] - 2), int(shd[1] - 3), ARMOR, 4); s.px(int(shd[0] - 1), int(shd[1] - 3), ARMOR, 4); s.px(int(shd[0] - 3), int(shd[1] - 2), ARMOR, 4)
    s.finish()
    return s


MATS = [0xF0C8A0, 0x7388A0, 0x7388A0, 0xB8C4D0, 0xB83A32, 0x5A2A30, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x6A4A2A, 0xFFD24A, 0x4AA8FF]
if __name__ == '__main__':
    ims = [knight(pose(n)).image(MATS) for n in FRAMES]
    sheet(ims, scale=6, cols=5).save(sys.argv[1])
