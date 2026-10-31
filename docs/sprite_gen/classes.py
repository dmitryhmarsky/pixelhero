import math, sys
import numpy as np
from art import Spr, sheet
from knight48 import WW, HH, G, FRAMES, pose as kpose, knight, sword, shield, MATS as KMATS
from heroes import SKIN, STEEL, ARMOR, BLADE, CLS, CLS2, LEATHER, GOLD, HAIR, WOOD, GLOW, EYE

CLASS_NAMES = ['warrior', 'mage', 'archer', 'rogue', 'paladin']


def pose(name):
    P = kpose(name)
    P['name'] = name
    return P


def leg2(s, hx, hy, foot, lift, mat, boot=LEATHER, r=3.0, hide_to=None):
    fx, fy = foot
    fy -= lift
    ax, ay = fx - 0.3, fy - 2.6
    kx, ky = (hx + ax) / 2 + 1.3, (hy + ay) / 2 + 0.5
    s.cap(hx, hy, kx, ky, r + .3, mat, r, gain=G)
    s.cap(kx, ky, ax, ay, r, mat, r - .4, gain=G)
    s.ell(fx + 1.8, fy - 1.2, 5.0, 3.0, boot, rz=2.6, gain=3.4)
    s.rect(fx - 3.0, fy + 1.2, fx + 6.8, fy + 1.9, LEATHER, 0)


def face(s, hx, hy, lx_eye=5.6, skin_r=(3.2, 3.0)):
    s.ell(hx + lx_eye, hy + 2.4, skin_r[0], skin_r[1], SKIN, rz=2.4, gain=1.4, sep=2)
    s.px(int(hx + lx_eye + .2), int(hy + 1.8), EYE, 4)
    s.px(int(hx + lx_eye + 1.2), int(hy + 1.8), EYE, 3)
    s.px(int(hx + lx_eye + .2), int(hy + 2.8), EYE, 2)


# ------------------------------------------------------------------ маг
def mage(P):
    s = Spr(WW, HH)
    by, lx = P['by'], P['lx']
    hip = 36.0 + by
    cx = 22.5 + lx
    for (fx, fy), lift in ((P['ff'], P['liftf']), (P['fn'], P['liftn'])):
        s.ell(fx + 1.8, fy - 1.2 - lift, 5.0, 3.0, LEATHER, rz=2.6, gain=3.4)
    # мантия
    robe = [(cx - 7.5, hip - 13), (cx + 8.5, hip - 13), (cx + 11.5, hip + 7.5), (cx + 4, hip + 8.6), (cx - 4, hip + 8.2), (cx - 11.5, hip + 7.6)]
    s.poly(robe, CLS, bevel=4, rz=4.6, gain=3.6)
    s.line(cx - 2.2, hip - 3, cx - 4.0, hip + 7, CLS, 0, 1.0)
    s.line(cx + 2.6, hip - 4, cx + 4.6, hip + 7.4, CLS, 0, 1.0)
    s.line(cx - 11.0, hip + 6.6, cx + 11.0, hip + 6.8, GOLD, 3, 1.4)
    s.rect(cx - 8.6, hip - 5.6, cx + 9.4, hip - 3.4, CLS2, 2)
    s.rect(cx - 0.8, hip - 6.0, cx + 2.6, hip - 3.0, GOLD, 3)
    s.ell(cx + 0.4, hip - 12.4, 9.4, 3.6, ARMOR, rz=3.0, n=2.4, gain=G, sep=1)
    # голова
    hx, hy = 23.4 + lx + P['head'][0], 19.4 + by + P['head'][1]
    s.ell(hx - 3.0, hy + 2.4, 4.6, 5.2, HAIR, rz=3.2, gain=3.0, sep=1)  # волосы сзади
    s.ell(hx + 1.0, hy + 1.2, 7.0, 6.2, SKIN, rz=5.0, gain=2.4, sep=1)
    s.px(int(hx + 6.4), int(hy + 1.2), EYE, 4)
    s.px(int(hx + 7.4), int(hy + 1.2), EYE, 3)
    s.px(int(hx + 6.4), int(hy + 2.2), EYE, 2)
    s.px(int(hx + 8.2), int(hy + 3.4), SKIN, 3)
    s.ell(hx + 2.6, hy + 6.6, 4.0, 3.2, HAIR, rz=3.4, gain=3.2, sep=1)   # борода
    s.ell(hx + 0.4, hy - 4.2, 11.0, 3.0, CLS2, rz=3.0, gain=G, sep=1)   # поля шляпы
    hat = [(hx - 6.6, hy - 4.6), (hx + 6.8, hy - 4.4), (hx + 4.4, hy - 11.0), (hx - 1.0, hy - 15.0), (hx - 7.2, hy - 17.0), (hx - 3.4, hy - 11.4)]
    s.poly(hat, CLS, bevel=3, rz=3.4, gain=3.4, sep=1)
    s.rect(hx - 6.6, hy - 6.8, hx + 6.8, hy - 4.6, CLS2, 1)
    s.rect(hx - 1.0, hy - 7.0, hx + 2.6, hy - 4.4, GOLD, 3)
    s.px(int(hx + 1), int(hy - 11), GOLD, 4)
    # посох
    a = math.radians(P['mang'])
    u = np.array([math.cos(a), math.sin(a)])
    H = np.array([P['hand'][0] + lx, P['hand'][1] + by])
    b0, b1 = H - u * 9.0, H + u * 18.0
    s.cap(b0[0], b0[1], b1[0], b1[1], 1.5, WOOD, gain=3.0, sep=1)
    orb = b1 + u * 3.6
    for k, (rr, t) in enumerate(((6.2, 0), (5.2, 1))):
        pass
    s.ell(orb[0], orb[1], 4.6, 4.6, BLADE, rz=4.6, gain=3.4, sep=1)
    s.ell(orb[0] - 1.2, orb[1] - 1.2, 1.6, 1.6, BLADE, tone=4, sep=2)
    for dx, dy in ((0, -6), (6, 0), (0, 6), (-6, 0)):
        s.px(int(orb[0] + dx), int(orb[1] + dy), GLOW, 3) if P.get('glow') else None
    # рука
    shd = (cx + 5.4, hip - 9.6)
    elb = ((shd[0] + H[0]) / 2 - 1.2, (shd[1] + H[1]) / 2 + 2.6)
    s.cap(shd[0], shd[1], elb[0], elb[1], 3.0, CLS, 2.7, gain=G, sep=0)
    s.cap(elb[0], elb[1], H[0], H[1], 2.7, CLS, 2.3, gain=G, sep=0)
    s.ell(H[0], H[1], 2.3, 2.3, SKIN, rz=2.3, gain=2.4)
    s.finish()
    return s


# ------------------------------------------------------------------ лучник
def bow(s, bx, by_, draw, pull, R=13.0, bulge=4.0):
    pts = []
    for i in range(9):
        t = -1 + i / 4.0
        pts.append((bx + bulge * (1 - t * t), by_ + t * R))
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        s.cap(x0, y0, x1, y1, 1.25, WOOD, gain=3.0, sep=2)
    top, bot = pts[0], pts[-1]
    if draw:
        s.line(top[0], top[1], pull[0], pull[1], BLADE, 3, 0.9)
        s.line(bot[0], bot[1], pull[0], pull[1], BLADE, 3, 0.9)
        # стрела
        s.line(pull[0], pull[1], bx + bulge + 7, pull[1], WOOD, 3, 1.0)
        s.flatpoly([(bx + bulge + 7, pull[1] - 1.6), (bx + bulge + 11, pull[1]), (bx + bulge + 7, pull[1] + 1.6)], BLADE, 3)
        s.flatpoly([(pull[0], pull[1] - 1.8), (pull[0] + 3, pull[1] - 0.4), (pull[0] + 3, pull[1] + 0.4), (pull[0], pull[1] + 1.8)], CLS2, 2)
    else:
        s.line(top[0], top[1], bot[0], bot[1], BLADE, 3, 0.9)


def archer(P):
    s = Spr(WW, HH)
    by, lx = P['by'], P['lx']
    hip = 36.0 + by
    cx = 22.5 + lx
    n = P['name']
    # колчан (за спиной)
    q = [(cx - 10, hip - 17), (cx - 6, hip - 19), (cx - 0.5, hip - 8), (cx - 4.5, hip - 6)]
    s.poly(q, LEATHER, bevel=2, rz=2.0, gain=3.0)
    for dx in (-8.6, -7.0, -5.4):
        s.line(cx + dx, hip - 18.4 + (dx + 8.6) * -0.2, cx + dx + 1.2, hip - 14.6, WOOD, 3, 1.0)
        s.px(int(cx + dx + 0.4), int(hip - 19.4 + (dx + 8.6) * -0.2), CLS, 3)
        s.px(int(cx + dx + 0.4), int(hip - 18.4 + (dx + 8.6) * -0.2), CLS2, 2)
    leg2(s, 19.0 + lx * 0.3, hip, P['ff'], P['liftf'], CLS2)
    leg2(s, 25.0 + lx * 0.3, hip, P['fn'], P['liftn'], CLS2)
    s.ell(cx, hip + 1.2, 7.4, 4.2, CLS, rz=3.0, n=2.5, gain=G)
    s.rect(cx - 8.0, hip - 2.6, cx + 8.4, hip - 0.6, LEATHER, 2)
    s.rect(cx - 0.6, hip - 3.0, cx + 2.4, hip - 0.2, GOLD, 3)
    s.ell(cx + 0.2, hip - 6.8, 7.6, 6.2, CLS, rz=4.8, n=2.5, gain=G)
    s.ell(cx + 0.6, hip - 6.6, 5.4, 5.0, ARMOR, rz=3.2, n=2.4, gain=G, sep=1)   # кожаный жилет
    s.cap(cx - 5, hip - 11.5, cx + 3, hip - 2.0, 1.0, LEATHER, gain=1.5, sep=2)
    # капюшон и голова
    hx, hy = 23.2 + lx + P['head'][0], 15.4 + by + P['head'][1]
    s.ell(hx - 1.4, hy + 0.6, 9.4, 9.0, CLS, rz=8.0, gain=G, sep=1)
    s.poly([(hx - 6, hy - 6.5), (hx - 10, hy - 9), (hx - 12.5, hy - 5.5), (hx - 9.8, hy + 1.8)], CLS, bevel=2, rz=2.0, gain=G, sep=1)
    s.ell(hx + 4.6, hy + 2.4, 4.6, 4.8, CLS2, rz=1.5, tone=0, sep=2)
    face(s, hx, hy, 5.4, (3.4, 3.2))
    s.ell(hx + 0.2, hy - 6.4, 7.8, 2.8, CLS, rz=3.0, gain=G, bias=.4, sep=1)   # край капюшона
    s.line(hx + 3.0, hy - 3.2, hx + 8.8, hy - 1.6, CLS, 1, 1.4, sep=2)
    for dx, dy, t in ((-4, -6, 4), (-3, -6, 4), (-5, -4, 3)):
        s.px(int(hx + dx), int(hy + dy), CLS, t)
    # лук
    draw = n == 'windup'
    bx = 37.0 + (0 if n != 'strike' else 2.5)
    byc = hip - 8.0 + (1.0 if n == 'strike' else 0)
    pull = (hx + 2.4, hy + 3.0 + by * 0)
    pull = (28.0 + lx, byc)
    bow(s, bx, byc, draw, pull)
    shd = (cx + 6.0, hip - 9.0)
    grip = (bx + 3.4, byc)
    elb = ((shd[0] + grip[0]) / 2 - 0.5, (shd[1] + grip[1]) / 2 + 2.0)
    s.cap(shd[0], shd[1], elb[0], elb[1], 2.7, CLS, 2.4, gain=G, sep=1)
    s.cap(elb[0], elb[1], grip[0], grip[1], 2.4, ARMOR, 2.1, gain=G, sep=0)
    s.ell(grip[0], grip[1], 2.2, 2.2, LEATHER, rz=2.2, gain=3.0)
    if draw:
        s.cap(cx - 3.0, hip - 10.5, pull[0] - 1.0, pull[1] + 0.5, 2.4, CLS, 2.1, gain=G, sep=0)
        s.ell(pull[0] - 0.6, pull[1] + 0.5, 2.0, 2.0, LEATHER, rz=2.0, gain=3.0)
    s.ell(cx + 6.0, hip - 9.2, 4.0, 3.8, ARMOR, rz=3.8, gain=G, sep=1)
    s.px(int(cx + 4.4), int(hip - 11.6), ARMOR, 4); s.px(int(cx + 5.4), int(hip - 11.6), ARMOR, 4)
    s.finish()
    return s


# ------------------------------------------------------------------ вор
def rogue(P):
    s = Spr(WW, HH)
    by, lx = P['by'], P['lx'] + 1.5
    hip = 36.0 + by
    cx = 22.0 + lx
    n = P['name']
    # дальний кинжал (обратный хват)
    fh = (cx - 5.5, hip - 6.0)
    sword(s, fh[0], fh[1], -118.0 if n != 'windup' else -160.0, length=10.0)
    # плащ сзади
    s.poly([(cx - 5, hip - 13), (cx - 11, hip - 9), (cx - 15.5, hip + 2), (cx - 13, hip + 9.5), (cx - 7, hip + 5), (cx - 2, hip + 2)], CLS, bevel=2, rz=2.4, gain=G, sep=1)
    leg2(s, 19.0 + lx * 0.3, hip, P['ff'], P['liftf'], CLS2, r=2.6)
    leg2(s, 25.0 + lx * 0.3, hip, P['fn'], P['liftn'], CLS2, r=2.6)
    s.ell(cx, hip + 0.6, 6.4, 3.6, CLS2, rz=3.0, n=2.5, gain=G)
    s.rect(cx - 6.6, hip - 2.6, cx + 7.0, hip - 0.6, LEATHER, 2)
    s.rect(cx - 0.6, hip - 3.0, cx + 2.4, hip - 0.2, GOLD, 3)
    s.ell(cx + 4.2, hip + 0.4, 2.6, 3.0, LEATHER, rz=2.4, gain=3.0, sep=1)  # подсумок
    s.ell(cx + 0.4, hip - 6.8, 6.8, 6.0, ARMOR, rz=4.6, n=2.5, gain=G)
    s.cap(cx - 4.5, hip - 11.0, cx + 3.5, hip - 2.0, 1.0, LEATHER, gain=1.5, sep=2)
    s.cap(cx + 4.5, hip - 11.0, cx - 3.0, hip - 2.5, 0.9, LEATHER, gain=1.5, sep=2)
    # голова: капюшон + маска-шарф
    hx, hy = 24.0 + lx + P['head'][0], 16.8 + by + P['head'][1]
    s.ell(hx - 0.8, hy + 0.2, 8.6, 8.4, CLS, rz=7.6, gain=G, sep=1)
    s.poly([(hx - 5, hy - 6), (hx - 9.5, hy - 8.4), (hx - 11.5, hy - 3.6), (hx - 9, hy + 2.4)], CLS, bevel=2, rz=2.0, gain=G, sep=1)
    s.ell(hx + 4.4, hy + 1.4, 4.4, 3.4, CLS2, rz=1.5, tone=0, sep=2)
    s.px(int(hx + 5.0), int(hy + 0.8), EYE, 4)
    s.px(int(hx + 6.0), int(hy + 0.8), EYE, 4)
    s.px(int(hx + 5.0), int(hy + 1.8), EYE, 2)
    s.ell(hx + 3.0, hy + 5.4, 5.0, 3.2, ARMOR, rz=2.6, gain=G, sep=1)   # шарф
    s.line(hx - 4.2, hy + 6.6, hx - 9.0, hy + 11.0, ARMOR, 1, 2.0, sep=1)
    for dx, dy, t in ((-4, -5, 4), (-3, -5, 4), (-5, -3, 3)):
        s.px(int(hx + dx), int(hy + dy), CLS, t)
    # ближний кинжал
    hand = (P['hand'][0] + lx, P['hand'][1] + by + 1)
    ang = {'idle': -38.0, 'walkA': -30.0, 'walkB': -46.0, 'windup': -150.0, 'strike': -4.0}[n]
    shd = (cx + 5.8, hip - 8.8)
    elb = ((shd[0] + hand[0]) / 2 - 1.0, (shd[1] + hand[1]) / 2 + 2.4)
    s.cap(shd[0], shd[1], elb[0], elb[1], 2.6, ARMOR, 2.3, gain=G, sep=1)
    s.cap(elb[0], elb[1], hand[0], hand[1], 2.3, CLS2, 2.0, gain=G, sep=0)
    sword(s, hand[0], hand[1], ang, length=12.0)
    s.ell(hand[0], hand[1], 2.2, 2.2, LEATHER, rz=2.4, gain=3.0)
    s.ell(shd[0], shd[1] - 0.2, 4.0, 3.8, ARMOR, rz=3.6, gain=G, sep=1)
    s.finish()
    return s


# ------------------------------------------------------------------ паладин
def mace(s, hx, hy, ang, length=22.0):
    a = math.radians(ang)
    u = np.array([math.cos(a), math.sin(a)])
    v = np.array([-math.sin(a), math.cos(a)])
    H = np.array([hx, hy])
    p0, p1 = H - u * 5.0, H + u * length
    s.cap(p0[0], p0[1], p1[0], p1[1], 1.5, WOOD, gain=3.0, sep=1)
    c = p1 + u * 1.0
    s.ell(p0[0], p0[1], 1.9, 1.9, GOLD, rz=1.9, gain=3.0)
    hd = c + u * 2.4
    s.ell(hd[0], hd[1], 5.0, 5.0, BLADE, rz=4.8, gain=3.6, sep=1)
    for k in range(4):
        t = k * math.pi / 2 + math.pi / 4
        e = hd + np.array([math.cos(t), math.sin(t)]) * 5.2
        s.cap(hd[0], hd[1], e[0], e[1], 1.6, BLADE, 1.0, gain=3.0, sep=2)
    s.ell(hd[0], hd[1], 2.2, 2.2, GOLD, rz=2.2, gain=3.2, sep=2)
    s.px(int(hd[0] - 1), int(hd[1] - 1), GOLD, 4)
    gp = H + u * length * 0.5
    s.cap(H[0] - v[0] * 2.6 + u[0] * 4, H[1] - v[1] * 2.6 + u[1] * 4, H[0] + v[0] * 2.6 + u[0] * 4, H[1] + v[1] * 2.6 + u[1] * 4, 0.9, GOLD, gain=2.4, sep=2)


def paladin(P):
    s = Spr(WW, HH)
    by, lx = P['by'], P['lx']
    hip = 36.0 + by
    cx = 22.5 + lx
    n = P['name']
    # плащ
    s.poly([(cx - 5, hip - 14), (cx - 11.5, hip - 8), (cx - 15.0, hip + 4), (cx - 12, hip + 9.4), (cx - 5, hip + 4), (cx + 1, hip - 3)], CLS, bevel=3, rz=2.6, gain=G, sep=1)
    s.line(cx - 13.4, hip + 6.6, cx - 6.0, hip + 6.0, GOLD, 3, 1.2)
    from knight48 import leg
    leg(s, 19.0 + lx * 0.3, hip, P['ff'], P['liftf'])
    leg(s, 25.0 + lx * 0.3, hip, P['fn'], P['liftn'])
    s.ell(cx, hip + 0.4, 8.2, 4.2, ARMOR, rz=3.4, n=2.6, gain=G)
    s.poly([(cx - 1.5, hip - 1.5), (cx + 3.4, hip - 1.5), (cx + 3.0, hip + 5.2), (cx + 0.8, hip + 6.2), (cx - 1.8, hip + 5.0)], CLS, bevel=2, rz=1.8, sep=0)
    s.rect(cx - 8.0, hip - 3.0, cx + 8.4, hip - 0.9, GOLD, 2)
    s.ell(cx + 0.2, hip - 6.8, 8.8, 7.0, ARMOR, rz=5.6, n=2.5, gain=G)
    # золотой крест на груди
    s.rect(cx - 0.6, hip - 11.4, cx + 2.4, hip - 3.2, GOLD, 3, sep=2)
    s.rect(cx - 3.0, hip - 9.0, cx + 4.8, hip - 6.6, GOLD, 3, sep=2)
    s.px(int(cx + 0), int(hip - 11), GOLD, 4)
    # голова с крылатым шлемом
    hx, hy = 23.2 + lx + P['head'][0], 15.4 + by + P['head'][1]
    s.ell(hx - 0.6, hy + 8.2, 5.4, 2.6, ARMOR, rz=2.2, gain=G, bias=-0.6, sep=1)
    s.poly([(hx - 5, hy - 2), (hx - 13, hy - 8.5), (hx - 12.5, hy - 3), (hx - 14.5, hy + 0.5), (hx - 10.5, hy + 0.6), (hx - 9, hy + 3.5), (hx - 5, hy + 3)], GOLD, bevel=2, rz=1.6, gain=G, sep=1)  # крыло
    s.ell(hx, hy, 9.2, 8.8, ARMOR, rz=8.0, gain=G, sep=1)
    s.ell(hx + 5.4, hy + 2.2, 4.4, 4.0, STEEL, rz=1.5, tone=0, sep=2)
    face(s, hx, hy, 5.9, (3.0, 2.8))
    s.line(hx + 3.4, hy + 0.0, hx + 8.8, hy - 0.4, GOLD, 3, 1.4, sep=2)
    s.line(hx + 1.0, hy - 8.5, hx + 6.2, hy - 0.4, GOLD, 2, 1.2, sep=2)
    s.ell(hx - 0.6, hy + 5.6, 3.8, 3.4, ARMOR, rz=3.0, gain=G, sep=1)
    for dx, dy, t in ((-4, -5, 4), (-3, -5, 4), (-4, -4, 4), (-5, -3, 3)):
        s.px(int(hx + dx), int(hy + dy), ARMOR, t)
    # нимб
    if n != 'windup':
        s.ell(hx - 1.0, hy - 10.6, 5.4, 1.6, GLOW, rz=1.4, tone=4, sep=2) if hy - 10.6 > 1.5 else None
    # башенный щит
    shx, shy = 31.6 + lx + P['sh'][0], hip - 3.0 + P['sh'][1]
    sp = [(shx - 6.0, shy - 10.5), (shx + 6.0, shy - 10.5), (shx + 6.4, shy + 2.0), (shx, shy + 11.5), (shx - 6.4, shy + 2.0)]
    s.poly(sp, STEEL, bevel=3, rz=3.0, gain=G, sep=1)
    s.poly([(shx - 4.2, shy - 8.5), (shx + 4.2, shy - 8.5), (shx + 4.6, shy + 1.6), (shx, shy + 8.8), (shx - 4.6, shy + 1.6)], CLS, bevel=2, rz=1.6, gain=2.6, sep=2)
    s.rect(shx - 0.9, shy - 7.0, shx + 1.1, shy + 5.4, GOLD, 3)
    s.rect(shx - 3.2, shy - 3.4, shx + 3.4, shy - 1.4, GOLD, 3)
    s.px(int(shx - 1.5), int(shy - 8.4), STEEL, 4)
    s.px(int(shx - 0.5), int(shy - 8.4), STEEL, 4)
    # рука с булавой
    shd = (28.2 + lx, hip - 9.2)
    hand = (P['hand'][0] + lx, P['hand'][1] + by)
    ang = {'idle': -66.0, 'walkA': -58.0, 'walkB': -76.0, 'windup': -145.0, 'strike': -8.0}[n]
    elb = ((shd[0] + hand[0]) / 2 - 1.6, (shd[1] + hand[1]) / 2 + 2.4)
    s.cap(shd[0], shd[1], elb[0], elb[1], 2.9, STEEL, 2.6, gain=G)
    s.cap(elb[0], elb[1], hand[0], hand[1], 2.6, STEEL, 2.3, gain=G)
    mace(s, hand[0], hand[1], ang, length=20.0 if n != 'strike' else 17.0)
    s.ell(hand[0], hand[1], 2.6, 2.6, STEEL, rz=2.8, gain=G)
    s.ell(shd[0], shd[1] - 0.2, 4.8, 4.4, ARMOR, rz=4.6, gain=G, sep=1)
    s.px(int(shd[0] - 2), int(shd[1] - 3), ARMOR, 4); s.px(int(shd[0] - 1), int(shd[1] - 3), ARMOR, 4); s.px(int(shd[0] - 3), int(shd[1] - 2), ARMOR, 4)
    s.finish()
    return s


def mage_pose(name):
    P = pose(name)
    P['mang'] = {'idle': -84.0, 'walkA': -80.0, 'walkB': -88.0, 'windup': -118.0, 'strike': -14.0}[name]
    if name == 'windup':
        P['hand'] = (28.0, 24.0)
    if name == 'strike':
        P['hand'] = (34.0, 30.0)
    return P


def build_all():
    out = []
    for cls, fn in enumerate([knight, mage, archer, rogue, paladin]):
        row = []
        for n in FRAMES:
            P = mage_pose(n) if cls == 1 else pose(n)
            row.append(fn(P) if cls != 0 else knight(P))
        out.append(row)
    return out


CMATS = {
    0: [0xF0C8A0, 0x7388A0, 0x7388A0, 0xB8C4D0, 0xB83A32, 0x5A2A30, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x6A4A2A, 0xFFD24A, 0x4AA8FF],
    1: [0xF0C8A0, 0x7388A0, 0x6C5AB0, 0x6AF0FF, 0x7A4AD8, 0x3C2A78, 0x7A5030, 0xE0B040, 0xD8D8E8, 0x6A4A2A, 0x8AF0FF, 0x4AA8FF],
    2: [0xF0C8A0, 0x7388A0, 0x9A6A3A, 0xB8C4D0, 0x3FA34D, 0x2A5A34, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x7A5A32, 0xFFD24A, 0x4AA8FF],
    3: [0xF0C8A0, 0x7388A0, 0x5A5666, 0xB8C4D0, 0x444A58, 0x7A2A3A, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x6A4A2A, 0xFFD24A, 0xFFE070],
    4: [0xF0C8A0, 0x8A9AB0, 0xB4BCD4, 0xE0B040, 0xC8D0F0, 0x6A78C8, 0x7A5030, 0xE0B040, 0x8A5A2B, 0x6A4A2A, 0xFFE9A0, 0x4AA8FF],
}

if __name__ == '__main__':
    sp = build_all()
    ims = []
    for c, row in enumerate(sp):
        for f in (0, 3, 4):
            ims.append(row[f].image(CMATS[c]))
    sheet(ims, scale=5, cols=6).save(sys.argv[1])
