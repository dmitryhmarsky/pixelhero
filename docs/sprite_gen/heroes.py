import math
import numpy as np
from art import Spr

SKIN, STEEL, ARMOR, BLADE, CLS, CLS2, LEATHER, GOLD, HAIR, WOOD, GLOW, EYE = range(12)
SZ = 40

# кадры: idle, walkA, walkB, windup, strike
FRAMES = ['idle', 'walkA', 'walkB', 'windup', 'strike']


def pose(name):
    P = dict(by=0.0, lx=0.0, fn=(22.5, 36.0), ff=(14.5, 36.0), liftn=0.0, liftf=0.0,
             ang=-58.0, hand=(27.5, 27.0), sh=(0.0, 0.0), head=(0.0, 0.0), hip=29.0)
    if name == 'walkA':
        P.update(by=-1.0, fn=(26.0, 36.0), ff=(11.0, 36.0), liftn=1.0, ang=-48.0, hand=(27.0, 26.0), head=(0.0, 0.0))
    elif name == 'walkB':
        P.update(by=-1.0, fn=(18.0, 36.0), ff=(20.0, 35.0), liftf=1.5, ang=-66.0, hand=(26.5, 27.5))
    elif name == 'windup':
        P.update(by=0.0, lx=-1.5, fn=(21.0, 36.0), ff=(13.0, 36.0), ang=-135.0, hand=(23.5, 17.0), head=(-0.5, 0.0), sh=(-1.5, 0.0))
    elif name == 'strike':
        P.update(by=1.0, lx=2.0, fn=(26.5, 36.0), ff=(13.0, 36.0), ang=-4.0, hand=(29.0, 25.0), head=(1.0, 0.5), sh=(2.0, 0.0))
    return P


def leg(s, hx, hy, foot, lift, steel_mat=STEEL, boot=LEATHER, front=True):
    fx, fy = foot
    fy = fy - lift
    ax, ay = fx - 0.5, fy - 2.4
    # колено чуть вперёд
    kx, ky = (hx + ax) / 2 + 1.2, (hy + ay) / 2
    s.cap(hx, hy, kx, ky, 2.7, steel_mat, 2.5)
    s.cap(kx, ky, ax, ay, 2.5, steel_mat, 2.2)
    s.ell(kx + .8, ky + .2, 2.3, 2.1, steel_mat, rz=2.5)
    s.ell(fx + 1.2, fy - 0.6, 4.0, 2.4, boot, rz=2.2, sep=0)
    s.rect(fx - 2.0, fy + 0.3, fx + 4.6, fy + 1.0, LEATHER, 0)


def sword(s, hx, hy, ang, length=15.0, blade=BLADE, guard=GOLD, hilt=LEATHER):
    a = math.radians(ang)
    u = np.array([math.cos(a), math.sin(a)])
    v = np.array([-math.sin(a), math.cos(a)])
    H = np.array([hx, hy])
    # рукоять и навершие
    p0, p1 = H - u * 3.0, H + u * 0.8
    s.cap(p0[0], p0[1], p1[0], p1[1], 1.1, hilt, gain=2.0)
    q = H - u * 3.6
    s.ell(q[0], q[1], 1.5, 1.5, guard, rz=1.5)
    # перекрестие
    g0, g1 = H + u * 1.4 - v * 3.0, H + u * 1.4 + v * 3.0
    s.cap(g0[0], g0[1], g1[0], g1[1], 1.0, guard, gain=2.4)
    # клинок: светлая и тёмная половины
    base = H + u * 2.2
    tip = base + u * length
    w = 1.9
    lit = 1 if np.dot(v, np.array([-0.6, -0.8])) > 0 else -1
    A = base + v * lit * w
    B = base - v * lit * w
    T = tip
    mid_t = base + u * length * 0.86
    s.flatpoly([tuple(base), tuple(A), tuple(mid_t + v * lit * 0.8), tuple(T)], blade, 3, sep=2)
    s.flatpoly([tuple(base), tuple(B), tuple(mid_t - v * lit * 0.8), tuple(T)], blade, 1, sep=2)
    # ребро жёсткости
    e = base + u * 1.0
    f = base + u * length * 0.82
    s.line(e[0], e[1], f[0], f[1], blade, 4, 0.9, sep=2)
    return T


def shield_round(s, cx, cy, rx, ry, emblem=CLS):
    s.ell(cx, cy, rx, ry, STEEL, rz=rx * 0.7, gain=2.6, sep=1)
    s.ell(cx + 0.2, cy + 0.2, rx * 0.74, ry * 0.76, STEEL, rz=rx * 0.4, gain=2.2, bias=-0.15, sep=2)
    # эмблема
    s.ell(cx + 0.3, cy + 0.3, rx * 0.4, ry * 0.32, emblem, rz=2, gain=2.2, sep=2)
    s.ell(cx - 0.3, cy - 0.5, 1.5, 1.7, STEEL, rz=1.8, gain=2.6, bias=0.4, sep=2)
    s.px(int(cx - 1), int(cy - 1), STEEL, 4)


def knight(P, accent=CLS):
    s = Spr(SZ, SZ)
    by = P['by']
    lx = P['lx']
    hip = 30.0 + by
    G = 4.0
    leg(s, 16.0 + lx * 0.3, hip, P['ff'], P['liftf'])
    leg(s, 20.8 + lx * 0.3, hip, P['fn'], P['liftn'])
    cx = 18.5 + lx
    # юбка-набедренник и пояс
    s.ell(cx, hip + 0.5, 6.4, 3.4, ARMOR, rz=3.0, n=2.6, gain=G)
    s.poly([(cx - 1.5, hip - 1.0), (cx + 3.6, hip - 1.0), (cx + 3.2, hip + 5.0), (cx + 0.6, hip + 6.0), (cx - 1.8, hip + 4.6)], accent, bevel=2, rz=1.6, sep=0)
    s.rect(cx - 6.0, hip - 2.2, cx + 6.2, hip - 0.6, LEATHER, 2)
    s.rect(cx - 0.6, hip - 2.6, cx + 2.2, hip - 0.2, GOLD, 3)
    # грудь
    s.ell(cx + 0.2, hip - 5.6, 6.6, 5.6, ARMOR, rz=4.6, n=2.5, gain=G)
    s.cap(cx - 3.2, hip - 10.0, cx + 3.2, hip - 2.0, 0.9, accent, gain=1.5, sep=2)
    # голова
    hx, hy = 19.2 + lx + P['head'][0], 12.0 + by + P['head'][1]
    s.ell(hx - 0.5, hy + 6.4, 4.6, 2.2, STEEL, rz=2.0, gain=G, bias=-0.5, sep=1)  # горжет
    s.ell(hx, hy, 7.6, 7.4, STEEL, rz=7.0, gain=G, sep=1)  # шлем
    s.ell(hx + 4.4, hy + 1.2, 3.9, 2.5, STEEL, rz=1.5, tone=0, sep=2)  # тёмный визор
    s.px(int(hx + 5.0), int(hy + 1.2), EYE, 4)
    s.px(int(hx + 6.0), int(hy + 1.2), EYE, 3)
    s.px(int(hx + 5.0), int(hy + 2.2), EYE, 1)
    s.line(hx + 1.2, hy - 6.8, hx + 4.0, hy + 3.2, STEEL, 0, 1.0, sep=2)  # ребро шлема
    s.ell(hx + 1.2, hy + 3.8, 3.4, 3.2, STEEL, rz=2.6, gain=G, sep=1)  # щека
    # плюм
    s.poly([(hx - 3.5, hy - 6.4), (hx + 2.0, hy - 7.8), (hx + 3.5, hy - 6.6), (hx - 3.0, hy - 5.0), (hx - 8.0, hy - 1.5), (hx - 9.5, hy + 1.8), (hx - 7.4, hy - 3.8)], accent, bevel=2, rz=1.6, sep=0)
    # щит
    shx, shy = 27.0 + lx + P['sh'][0], hip - 3.6 + P['sh'][1]
    shield_round(s, shx, shy, 5.8, 8.0, accent)
    # плечо и рука с мечом
    shd = (22.5 + lx, hip - 9.2)
    hand = (P['hand'][0] + lx, P['hand'][1] + by)
    elb = ((shd[0] + hand[0]) / 2 - 1.5, (shd[1] + hand[1]) / 2 + 2.0)
    s.cap(shd[0], shd[1], elb[0], elb[1], 2.4, STEEL, 2.2)
    s.cap(elb[0], elb[1], hand[0], hand[1], 2.2, STEEL, 2.0)
    sword(s, hand[0], hand[1], P['ang'])
    s.ell(hand[0], hand[1], 2.2, 2.2, STEEL, rz=2.4, gain=G)
    s.ell(shd[0], shd[1] - 0.3, 4.4, 4.2, ARMOR, rz=4.2, gain=G, sep=1)
    s.ell(shd[0] - 1.4, shd[1] - 2.0, 1.6, 1.0, ARMOR, tone=4, sep=2)
    s.finish()
    return s


if __name__ == '__main__':
    import sys
    from art import sheet
    MATS = [0xF0C8A0, 0x7388A0, 0x7388A0, 0xB8C4D0, 0xB83A32, 0x5A2A30, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x6A4A2A, 0xFFD24A, 0x4AA8FF]
    ims = [knight(pose(n)).image(MATS) for n in FRAMES]
    sheet(ims, scale=8, cols=5).save(sys.argv[1])
