import math, sys
import numpy as np
from art import Spr, sheet

G = 3.8


def eye(s, x, y, white=2, pupil=3, r=1.9):
    s.ell(x, y, r, r * 1.1, white, rz=1.5, tone=3, sep=2)
    s.px(int(x + .6), int(y), pupil, 1)
    s.px(int(x + .6), int(y + 1), pupil, 0)


# ---------------------------------------------------------------- 0 слизень
def slime(t):
    s = Spr(30, 24)
    ry, rx = 9.2 - t * 1.2, 12.4 + t * 1.2
    cy = 22.6 - ry
    s.ell(15, cy, rx, ry, 0, rz=9, gain=3.4)
    s.ell(15, cy + ry * .5, rx * .85, ry * .4, 1, tone=1, sep=2)
    for dx, dy in ((-6, -4), (-5, -5), (-4, -5), (-6, -3)):
        s.px(int(15 + dx), int(cy + dy), 4, 4)
    s.ell(23, cy - 4.2, 1.8, 1.6, 1, tone=3, sep=2)
    eye(s, 19.5, cy - 1.2, 2, 3, 2.6)
    eye(s, 25.0, cy - 0.8, 2, 3, 2.2)
    s.line(18.5, cy + 3.8, 24.5, cy + 3.8, 3, 1, 1.0)
    s.px(int(26), int(cy + 3), 2, 4)
    s.finish()
    return s


# ---------------------------------------------------------------- 1 летучая мышь
def bat(t):
    s = Spr(36, 30)
    up = t == 0
    wy = -9 if up else 6
    # дальнее крыло
    s.poly([(15, 13), (6, 13 + wy * .6), (1.5, 12 + wy), (7, 13 + wy * .5), (4, 17 + wy * .4), (10, 16.5 + wy * .3), (10, 19)], 1, bevel=2, rz=1.5, sep=0)
    s.ell(17, 15.5, 5.6, 6.8, 0, rz=5.5, gain=3.4)
    s.ell(17.8, 17.5, 3.4, 4.0, 4, rz=2, gain=2.6, sep=2)
    # ближнее крыло
    s.poly([(18, 12), (27, 12 + wy * .6), (33, 11 + wy), (28, 13 + wy * .5), (31, 17 + wy * .4), (24, 16 + wy * .3), (21, 19)], 1, bevel=2, rz=1.5, sep=0)
    s.line(18, 13, 33, 11 + wy, 0, 1, 1.0)
    s.line(18, 14, 31, 17 + wy * .4, 0, 1, 1.0)
    s.ell(21.5, 10.0, 5.0, 4.6, 0, rz=4.4, gain=3.4, sep=1)
    s.poly([(18, 7), (17.5, 1.6), (21.5, 5.6)], 0, bevel=1, rz=1.2, sep=1)
    s.poly([(23, 6.2), (25.5, 1.4), (26.5, 7.2)], 0, bevel=1, rz=1.2, sep=1)
    s.px(23, 9, 2, 4); s.px(24, 9, 2, 3); s.px(26, 9, 2, 4)
    s.px(25, 12, 3, 4); s.px(23, 12, 3, 4)
    s.px(18, 22, 0, 0); s.px(16, 22, 0, 0)
    s.cap(15.5, 21, 15.5, 24, .9, 0, gain=2); s.cap(18.5, 21, 18.5, 24, .9, 0, gain=2)
    s.finish()
    return s


# ---------------------------------------------------------------- 2 скелет
def skeleton(t):
    s = Spr(34, 44)
    st = 1.2 if t else -1.2
    bone = 0
    # ноги
    for dx, k in ((-3.5, 1), (3.5, -1)):
        x = 17 + dx
        s.cap(x, 28, x + st * k * 0.8, 36, 1.7, bone, gain=3)
        s.cap(x + st * k * .8, 36, x + st * k * 1.4, 41, 1.4, bone, gain=3)
        s.ell(x + st * k * 1.4 + 1.6, 42, 3.4, 1.8, bone, rz=1.8)
    s.ell(17, 27, 6.4, 3.2, bone, rz=2.4, gain=3)   # таз
    # позвоночник и рёбра
    s.cap(17, 14, 17, 27, 1.3, 1, gain=2.4)
    for i, y in enumerate((16.5, 19.5, 22.5)):
        s.ell(17, y, 6.6 - i * .6, 1.5, bone, rz=1.5, gain=3, sep=1)
    s.ell(17, 18, 7.2, 6.0, 1, tone=0, sep=0) if False else None
    # рука с мечом
    hand = (27.5, 25 + (-1 if t else 0))
    s.cap(21.5, 14, 25, 20, 1.5, bone, gain=3)
    s.cap(25, 20, hand[0], hand[1], 1.4, bone, gain=3)
    a = math.radians(-70 if not t else -50)
    u = np.array([math.cos(a), math.sin(a)])
    tip = np.array(hand) + u * 15
    s.cap(hand[0], hand[1], tip[0], tip[1], 1.2, 2, gain=2.6, sep=1)
    s.line(hand[0] - 2, hand[1], hand[0] + 2, hand[1] - 1.2, 3, 2, 1.4)
    # череп
    s.ell(17, 9, 7.0, 6.6, bone, rz=6, gain=3.4, sep=1)
    s.ell(17, 15, 4.4, 2.0, bone, rz=2, gain=3, sep=1)
    s.ell(20.4, 8.6, 2.4, 2.6, 1, tone=0, sep=2)
    s.ell(14.2, 8.6, 2.0, 2.4, 1, tone=0, sep=2)
    s.px(21, 8, 3, 4); s.px(21, 9, 3, 3)
    s.px(23, 11, 1, 0); s.px(24, 11, 1, 0)
    for x in (15, 17, 19, 21):
        s.px(x, 14, bone, 4)
    s.finish()
    return s


# ---------------------------------------------------------------- 3 гоблин
def goblin(t):
    s = Spr(34, 38)
    st = 1.5 if t else -1.5
    for dx, k in ((-3, 1), (3.5, -1)):
        x = 16 + dx
        s.cap(x, 27, x + st * k * .5, 34, 2.2, 1, gain=3)
        s.ell(x + st * k * .5 + 1.4, 35, 3.8, 2.2, 2, rz=2.2)
    s.ell(16, 27, 7, 3.4, 2, rz=3, gain=3.2)    # набедренная повязка
    s.ell(16, 21, 7.2, 6.8, 1, rz=5.4, gain=3.4)   # тело
    s.ell(16.4, 21.4, 5.4, 5.2, 2, rz=3.6, gain=3.2, sep=1)  # жилет
    s.rect(9.4, 25, 22.6, 26.8, 2, 0)
    s.rect(15, 24.6, 17.8, 27, 3, 3)
    # ухо
    s.poly([(12, 10), (1, 5), (3.6, 9.5), (1.6, 13), (12, 14)], 0, bevel=2, rz=1.6, gain=3.2, sep=1)
    # голова
    s.ell(17.5, 12, 7.6, 6.8, 0, rz=6, gain=3.4, sep=1)
    s.poly([(24, 12.4), (28.6, 14.6), (24.4, 16.0)], 0, bevel=1, rz=1.2, sep=1)   # нос
    s.ell(21.8, 10.6, 2.0, 2.0, 4, tone=3, sep=2)
    s.px(22, 10, 5, 0)
    s.px(18, 10, 5, 3)
    s.line(19, 16.2, 25, 15.6, 5, 1, 1.0)
    s.px(21, 17, 4, 4); s.px(23, 16, 4, 4)
    s.ell(15, 5.6, 4, 1.4, 5, tone=2, sep=1)  # брови
    # рука с кинжалом
    hand = (26.5, 22 + (-1 if t else 0))
    s.cap(21, 17, 24, 21, 2.1, 0, gain=3)
    s.cap(24, 21, hand[0], hand[1], 2.0, 0, gain=3)
    a = math.radians(-60 if not t else -35)
    u = np.array([math.cos(a), math.sin(a)])
    tip = np.array(hand) + u * 11
    s.flatpoly([(hand[0] - u[1] * 1.6, hand[1] + u[0] * 1.6), (hand[0] + u[1] * 1.6, hand[1] - u[0] * 1.6), tuple(tip)], 6, 3, sep=1)
    s.finish()
    return s


# ---------------------------------------------------------------- 4 орк
def orc(t):
    s = Spr(44, 48)
    st = 1.6 if t else -1.6
    for dx, k in ((-4.5, 1), (4.5, -1)):
        x = 22 + dx
        s.cap(x, 33, x + st * k * .5, 41, 3.4, 1, gain=3)
        s.ell(x + st * k * .5 + 1.6, 43, 5.2, 3.0, 2, rz=2.6)
    s.ell(22, 33, 10, 4.4, 2, rz=3.4, gain=3.2)
    s.ell(22, 25, 11.4, 9.4, 0, rz=8, gain=3.4)
    s.ell(23, 22, 8.6, 5.0, 0, rz=4, gain=3.4, sep=2)
    s.ell(31, 17.6, 6.0, 5.2, 3, rz=5.6, gain=3.6, sep=1)    # наплечник
    s.px(29, 14, 3, 4); s.px(30, 14, 3, 4)
    s.rect(11.6, 30, 32.6, 32.4, 2, 0)
    s.rect(21, 29.6, 24.6, 32.8, 3, 3)
    # голова
    s.ell(21, 13, 8.4, 7.4, 0, rz=6.4, gain=3.4, sep=1)
    s.ell(26, 15.5, 5, 4, 0, rz=3, gain=3, sep=2)
    s.ell(25.4, 11, 2.0, 1.8, 4, tone=3, sep=2); s.px(26, 11, 5, 0)
    s.px(21, 11, 4, 3)
    s.line(21.5, 8.6, 28, 9.4, 5, 1, 1.4)
    s.poly([(24, 17.4), (26, 21.5), (27, 17.4)], 3, bevel=1, rz=1, sep=2)
    s.poly([(28.6, 17.4), (30.6, 21), (31, 17.2)], 3, bevel=1, rz=1, sep=2)
    s.line(25, 18.2, 30.4, 18.2, 5, 1, 1.0)
    s.poly([(14, 8), (10.6, 4), (16.6, 6.4)], 0, bevel=1, rz=1.2, sep=1)
    # топор
    hand = (34, 27 + (-1 if t else 0))
    s.cap(31, 20, 33, 24, 3.2, 0, gain=3)
    s.cap(33, 24, hand[0], hand[1], 3.0, 0, gain=3)
    a = math.radians(-75 if not t else -45)
    u = np.array([math.cos(a), math.sin(a)])
    top = np.array(hand) + u * 14
    s.cap(hand[0] - u[0] * 5, hand[1] - u[1] * 5, top[0], top[1], 1.4, 4, gain=3, sep=1)
    c = np.array(hand) + u * 10
    v = np.array([-u[1], u[0]])
    s.flatpoly([tuple(c), tuple(c + v * 8 + u * 2.6), tuple(c + v * 8 - u * 5), tuple(c - u * 3.6)], 6, 3, sep=1)
    s.flatpoly([tuple(c), tuple(c + v * 4 + u * 1.4), tuple(c + v * 4 - u * 3), tuple(c - u * 3.4)], 6, 2, sep=2)
    s.ell(hand[0], hand[1], 3, 3, 0, rz=2.6)
    s.finish()
    return s


# ---------------------------------------------------------------- 5 дракон (босс)
def dragon(t):
    s = Spr(70, 52)
    flap = -9 if t == 0 else 4
    # дальнее крыло
    s.poly([(30, 18), (22, 8 + flap), (12, 5 + flap), (17, 14 + flap * .6), (10, 16 + flap * .5), (20, 22), (30, 26)], 1, bevel=2, rz=1.8, gain=3.0, sep=0)
    # хвост
    s.cap(20, 36, 9, 40, 5, 0, 1.6, gain=3.4)
    s.cap(9, 40, 3, 34, 1.6, 0, 1.0, gain=3)
    s.poly([(2, 30), (6, 33.6), (1, 38)], 3, bevel=1, rz=1, sep=2)
    # задние и передние лапы
    for x, k in ((24, 1), (38, -1)):
        o = 1.4 if t else -1.4
        s.cap(x, 38, x + o * k, 44, 3.6, 0, 3.0, gain=3.4)
        s.ell(x + o * k + 2.2, 46.2, 5.6, 2.6, 0, rz=2.8, gain=3.2)
        for cxx in (2.0, 4.4, 6.6):
            s.px(int(x + o * k + cxx), 47, 4, 4)
    # тело
    s.ell(32, 33, 17, 9.4, 0, rz=8, gain=3.4)
    s.ell(33, 38, 14, 5, 3, rz=3, gain=2.6, sep=2)
    # шея и голова
    s.cap(42, 28, 49, 17, 5.4, 0, 4.6, gain=3.4, sep=1)
    s.ell(54, 15, 8, 6, 0, rz=5.6, gain=3.4, sep=1)
    s.poly([(58, 13), (68, 14.5), (68, 17.4), (60, 19.4)], 0, bevel=2, rz=2, gain=3.2, sep=1)
    s.poly([(58, 19), (67, 18), (67, 20.5), (59, 22.6)], 3 if False else 0, bevel=1, rz=1.4, gain=3, sep=1)
    s.px(54, 11, 4, 4); s.px(55, 11, 4, 4); s.px(56, 11, 5, 3)
    s.px(62, 13, 5, 0); s.px(66, 14, 5, 0)
    s.poly([(50, 10), (46, 2.4), (53, 8.6)], 3, bevel=1, rz=1.3, sep=1)
    s.poly([(55, 8.4), (54.6, 1.8), (58.6, 8.6)], 3, bevel=1, rz=1.3, sep=1)
    for x in (62, 64, 66):
        s.px(x, 19, 4, 4)
    if t:
        s.ell(69, 21, 2.4, 2.0, 5, tone=3, sep=2)
    # шипы на спине
    for i in range(6):
        x = 44 - i * 4.6
        s.poly([(x, 27 - i * 0.4 + 1), (x - 2, 21 + i * .7 - 1.4), (x - 4, 27 - i * 0.4 + 1)], 3, bevel=1, rz=1.2, sep=1)
    # ближнее крыло
    s.poly([(34, 24), (40, 8 + flap), (52, 3 + flap), (47, 13 + flap * .6), (55, 15 + flap * .5), (44, 21), (36, 29)], 1, bevel=2, rz=1.8, gain=3.0, sep=1)
    s.line(34, 25, 52, 3 + flap, 0, 0, 1.2)
    s.line(36, 27, 55, 15 + flap * .5, 0, 0, 1.2)
    s.finish()
    return s


# ---------------------------------------------------------------- 6 паук
def spider(t):
    s = Spr(40, 30)
    sw = 1.6 if t else -1.6
    def legs(side, mat):
        for i, (x0, ang) in enumerate(((16, 150), (19, 125), (22, 95), (25, 70))):
            a = math.radians(ang + (sw * 6 if i % 2 == 0 else -sw * 6))
            kx = x0 + (math.cos(a) * 9) * side
            ky = 14 - 7 + abs(math.sin(a)) * 2
            ex = kx + math.cos(a) * 3 * side + (3 if side > 0 else -3) * (1 if i > 1 else -1) * .4
            s.cap(x0, 16, kx, ky - 3, 1.2, mat, 0.9, gain=2.6, sep=0)
            s.cap(kx, ky - 3, ex, 25.6, 0.9, mat, 0.7, gain=2.6, sep=0)
    legs(-1, 1)
    s.ell(12, 16, 9, 7.4, 0, rz=7, gain=3.4)
    s.ell(10, 13, 3.4, 1.6, 2, tone=3, sep=2); s.px(14, 13, 2, 3); s.px(8, 16, 2, 3)
    s.cap(20, 20, 20, 16, 2, 1, gain=2)
    legs(1, 0)
    s.ell(24, 17, 6, 5.4, 1, rz=5, gain=3.4, sep=1)
    s.ell(28.5, 15.6, 5.4, 4.6, 1, rz=4.4, gain=3.4, sep=1)
    for (x, y, r) in ((30, 14, 1.4), (33, 14.8, 1.1), (28, 12.4, 1.0)):
        s.ell(x, y, r, r, 3, tone=3, sep=2)
    s.cap(31.4, 18.6, 32.6, 22.6, .9, 2, gain=2); s.cap(29.6, 19, 30.4, 22.6, .9, 2, gain=2)
    s.finish()
    return s


# ---------------------------------------------------------------- 7 призрак
def ghost(t):
    s = Spr(34, 42)
    w = 1.8 if t else -1.8
    pts = [(6, 18), (7, 12), (11, 6), (17, 3.4), (23, 6), (27, 12), (28, 18), (28, 30)]
    tail = [(28, 36 + w), (24, 32 - w), (20, 38 + w), (16, 33 - w), (12, 39 + w), (8, 34 - w), (6, 37), (6, 24)]
    s.poly(pts + tail, 0, bevel=5, rz=6, gain=3.0, sep=0)
    s.ell(17, 13, 8, 7.5, 1, rz=4, gain=2, tone=None, sep=2) if False else None
    # руки
    s.cap(26, 22, 31, 25 + w * .5, 2.2, 0, 1.6, gain=3)
    s.cap(7, 22, 3, 26 - w * .5, 2.2, 0, 1.6, gain=3)
    # лицо
    s.ell(21, 14, 2.4, 3.2, 2, tone=0, sep=2)
    s.ell(13, 14, 2.2, 3.0, 2, tone=0, sep=2)
    s.px(21, 14, 3, 4); s.px(13, 14, 3, 4)
    s.ell(19, 21, 3.0, 3.6, 2, tone=0, sep=2)
    for dx, dy in ((-3, -6), (-2, -6), (-4, -5)):
        s.px(int(14 + dx + 3), int(14 + dy), 0, 4)
    s.finish()
    return s


# ---------------------------------------------------------------- 8 скорпион
def scorpion(t):
    s = Spr(46, 32)
    sw = 1.5 if t else -1.5
    # лапы
    for x in (14, 19, 24, 29):
        k = 1 if x % 2 else -1
        s.cap(x, 21, x + 2 * k * sw * .3, 26, 1.1, 1, gain=2.6)
        s.cap(x + 2 * k * sw * .3, 26, x + 4 + k * sw * .3, 29.6, .9, 1, gain=2.6)
    # хвост (дугой над спиной)
    tp = [(10, 19), (5.5, 14), (5, 8), (9, 3.6), (16, 3.2), (21, 6)]
    for (x0, y0), (x1, y1) in zip(tp[:-1], tp[1:]):
        r = 3.0 - 0.2 * tp.index((x0, y0))
        s.cap(x0, y0, x1 + (sw * .3), y1, max(r, 1.8), 0, gain=3.2, sep=1)
    ex, ey = tp[-1][0] + sw * .3, tp[-1][1]
    s.poly([(ex - 1, ey - 2), (ex + 5, ey + 4.6), (ex - 2.6, ey + 3.4)], 3, bevel=1, rz=1.2, sep=1)
    s.px(int(ex + 4), int(ey + 4), 4, 4)
    # тело
    s.ell(19, 20, 11, 5.8, 0, rz=6, gain=3.4, sep=1)
    for x in (13, 17, 21):
        s.line(x, 15.6, x + 1, 24, 0, 0, 1.0)
    s.ell(30.5, 19.5, 6.6, 5.2, 0, rz=5, gain=3.4, sep=1)
    s.px(33, 17, 4, 4); s.px(34, 17, 4, 3)
    s.px(36, 17, 5, 3)
    # клешни
    for (y0, k) in ((16, 0), (23, 1)):
        ax, ay = 35.6, y0 + 1
        bx, by_ = 41 + sw * .8, y0 - 3 + k * 3
        s.cap(ax, ay, bx, by_, 1.8, 0, gain=3, sep=1)
        s.ell(bx + 2.2, by_ - .4, 3.8, 3.0, 0, rz=3, gain=3.2, sep=1)
        s.poly([(bx + 4.4, by_ - 1.6), (bx + 7.4, by_ - 3.8 + k * 3), (bx + 5.6, by_ + .4)], 3, bevel=1, rz=1.2, sep=1)
    s.finish()
    return s


# ---------------------------------------------------------------- 9 голем (камень)
def golem(t):
    s = Spr(44, 46)
    st = 1.2 if t else -1.2
    for dx, k in ((-6, 1), (6.5, -1)):
        x = 22 + dx
        s.ell(x + st * k * .5, 38, 5.8, 6.4, 1, rz=5.4, n=3.0, gain=3.6, sep=1)
    s.ell(22, 28, 12, 9, 0, rz=7, n=3.0, gain=3.8)
    s.ell(22, 30.4, 8.4, 5.8, 1, rz=3, n=3.0, gain=3, sep=2)
    s.ell(8.5, 28, 5.4, 8.6, 0, rz=5.4, n=3.0, gain=3.8, sep=1)    # дальняя рука
    s.ell(8.5, 37, 5, 4.2, 1, rz=4, n=3.0, gain=3.6, sep=1)
    s.ell(34.6, 28, 5.8, 8.8, 0, rz=5.6, n=3.0, gain=3.8, sep=1)
    s.ell(35.4, 37.4 + (-1 if t else 0), 5.6, 4.6, 1, rz=4.4, n=3.0, gain=3.6, sep=1)
    s.ell(23, 15, 8.4, 7.2, 0, rz=6.4, n=2.8, gain=3.8, sep=1)
    s.ell(21.6, 14.4, 6.6, 2.2, 1, tone=0, sep=2)
    s.px(23, 14, 3, 4); s.px(24, 14, 3, 4); s.px(28, 14, 3, 4); s.px(29, 14, 3, 4)
    s.line(24, 19.4, 29, 19.4, 1, 0, 1.2)
    # трещины и мох
    s.line(17, 24, 20, 31, 1, 0, 1.0); s.line(20, 31, 17, 36, 1, 0, 1.0)
    s.line(28, 26, 26, 32, 1, 0, 1.0)
    s.ell(15, 22.4, 3.0, 1.2, 2, tone=2, sep=2); s.ell(30, 21.4, 2.6, 1.0, 2, tone=2, sep=2)
    s.px(26, 12, 4, 4); s.px(27, 12, 4, 4)
    s.finish()
    return s


# ---------------------------------------------------------------- 10 бес
def imp(t):
    s = Spr(36, 38)
    fl = 3 if t else -2
    s.poly([(12, 18), (3, 11 + fl), (1.6, 17 + fl * .5), (6.4, 17.6), (7, 23), (12, 22)], 1, bevel=2, rz=1.6, gain=3.2, sep=0)
    # хвост
    s.cap(12, 26, 6, 30, 1.4, 0, 1.0, gain=2.6)
    s.cap(6, 30, 3, 25, 1.0, 0, 1.0, gain=2.6)
    s.poly([(0.6, 24), (4.6, 20.6), (5.4, 26)], 2, bevel=1, rz=1, sep=1)
    for dx, k in ((-2.5, 1), (3.5, -1)):
        x = 17 + dx
        s.cap(x, 28, x + k * .5 * fl * .3, 33.4, 2.2, 0, gain=3)
        s.ell(x + 1.2, 34.6, 3.8, 2.0, 3, rz=2, gain=3)
    s.ell(17, 22, 6.4, 7.0, 0, rz=5.6, gain=3.4)
    s.ell(18.4, 24, 3.8, 4.4, 4, rz=2.4, gain=2.8, sep=2)
    # крыло
    s.poly([(17, 17), (24, 10 + fl), (30, 10.4 + fl), (27, 14 + fl * .6), (31, 17), (24, 20.4)], 1, bevel=2, rz=1.6, gain=3.2, sep=1)
    s.ell(19, 12, 7.6, 6.6, 0, rz=6, gain=3.4, sep=1)
    s.poly([(14, 8), (11, 2), (17, 6.6)], 3, bevel=1, rz=1.2, sep=1)
    s.poly([(21, 6.2), (23.6, 1), (25.6, 7.2)], 3, bevel=1, rz=1.2, sep=1)
    s.ell(22.8, 11.4, 2.2, 1.9, 2, tone=3, sep=2); s.px(23, 11, 5, 0)
    s.ell(17.6, 11.6, 1.6, 1.6, 2, tone=3, sep=2)
    s.line(18, 16, 25, 15.4, 5, 1, 1.0)
    s.px(20, 17, 2, 4); s.px(23, 16, 2, 4)
    # трезубец
    hx, hy = 28, 25 + (-1 if t else 0)
    s.cap(hx - 1, hy + 7, hx + 1, hy - 12, 0.9, 2, gain=2.4, sep=1)
    s.line(hx - 3, hy - 11, hx + 4, hy - 12, 3, 3, 1.0)
    for x in (hx - 3, hx + 1, hx + 4.6):
        s.line(x, hy - 11, x, hy - 15, 3, 3, 1.0)
    s.cap(22, 18, hx, hy, 2.0, 0, gain=3)
    s.ell(hx, hy, 2.2, 2.2, 0, rz=2.2)
    s.finish()
    return s


# ---------------------------------------------------------------- 11 голем-босс (лава)
def golem_boss(t):
    s = Spr(60, 56)
    st = 1.4 if t else -1.4
    for dx, k in ((-8, 1), (9, -1)):
        x = 30 + dx
        s.ell(x + st * k * .5, 47, 7.6, 7.4, 1, rz=6.6, n=3.0, gain=3.8, sep=1)
    s.ell(30, 33, 16, 12, 0, rz=9, n=3.0, gain=3.8)
    s.ell(30, 36, 11, 7, 1, rz=3, n=3.0, gain=3, sep=2)
    s.ell(10.5, 34, 7, 11, 0, rz=6.6, n=3.0, gain=3.8, sep=1)
    s.ell(10.5, 45, 6.4, 5, 1, rz=5, n=3.0, gain=3.6, sep=1)
    s.ell(49.6, 34, 7.4, 11.4, 0, rz=7, n=3.0, gain=3.8, sep=1)
    s.ell(50.6, 45.4 + (-1.4 if t else 0), 7, 5.6, 1, rz=5.4, n=3.0, gain=3.6, sep=1)
    # лава-ядро и трещины
    s.ell(30, 33, 5.2, 5.2, 2, rz=2, tone=3, sep=2)
    s.ell(30, 33, 2.8, 2.8, 2, rz=2, tone=4, sep=2)
    for (x0, y0, x1, y1) in ((30, 28, 26, 22), (30, 38, 34, 44), (24, 33, 18, 31), (36, 32, 42, 28), (50, 28, 50, 38), (11, 30, 9, 40)):
        s.line(x0, y0, x1, y1, 2, 3, 1.2, sep=2)
    # голова с рогами
    s.ell(31, 17, 10, 8.6, 0, rz=7.4, n=2.8, gain=3.8, sep=1)
    s.poly([(24, 12), (18, 3), (26, 9)], 1, bevel=1, rz=1.4, sep=1)
    s.poly([(34, 9.4), (36, 1.6), (40, 11)], 1, bevel=1, rz=1.4, sep=1)
    s.ell(30, 16.4, 8, 2.8, 1, tone=0, sep=2)
    s.px(33, 16, 2, 4); s.px(34, 16, 2, 4); s.px(38, 16, 2, 4); s.px(39, 16, 2, 4)
    s.line(32, 22, 38, 22, 1, 0, 1.4)
    for x in (33, 35, 37):
        s.px(x, 22, 2, 3)
    s.ell(17, 22, 4, 3.2, 1, rz=3, n=3.0, gain=3.6, sep=1); s.ell(45, 22, 4.4, 3.4, 1, rz=3, n=3.0, gain=3.6, sep=1)
    s.finish()
    return s


# ---------------------------------------------------------------- 12 демон (босс)
def demon(t):
    s = Spr(62, 58)
    fl = -8 if t == 0 else 5
    s.poly([(24, 24), (10, 14 + fl), (1.6, 18 + fl), (6, 26), (3, 34 + fl * .5), (12, 32), (14, 38), (24, 36)], 1, bevel=2, rz=2, gain=3.2, sep=0)
    s.cap(22, 42, 10, 49, 3.6, 0, 1.4, gain=3)
    s.cap(10, 49, 3, 42, 1.4, 0, 1.0, gain=3)
    s.poly([(0.6, 38), (5.4, 40), (3, 45)], 3, bevel=1, rz=1, sep=1)
    for dx, k in ((-6, 1), (7, -1)):
        x = 31 + dx
        o = 1.4 if t else -1.4
        s.cap(x, 40, x + o * k * .4, 48, 4.4, 0, 3.6, gain=3.4)
        s.ell(x + o * k * .4 + 2, 50.4, 6.2, 3.0, 1, rz=3, gain=3.2)
    s.ell(31, 38, 11, 6.4, 1, rz=4, gain=3.4)
    s.ell(31, 29, 13.4, 11, 0, rz=9, gain=3.6)
    s.ell(32, 30, 9.4, 8, 3, rz=3, gain=2.6, sep=2)
    for y in (26, 31, 36):
        s.line(26, y, 38, y, 0, 0, 1.0)
    s.ell(31, 40.2, 11.4, 2.4, 1, rz=2, tone=2, sep=1)
    s.poly([(44, 26), (52, 40), (47, 42), (41, 34)], 0, bevel=2, rz=2, gain=3.2, sep=1)
    # голова и рога
    s.ell(32, 14, 9, 8.2, 0, rz=7, gain=3.6, sep=1)
    s.poly([(25, 10), (19, 1.6), (28, 6)], 3, bevel=1, rz=1.4, sep=1)
    s.poly([(37, 6.4), (42, 1.2), (41, 10)], 3, bevel=1, rz=1.4, sep=1)
    s.poly([(22, 13), (14, 9), (21, 16.6)], 3, bevel=1, rz=1.2, sep=1)
    s.ell(37, 13, 3, 2.4, 4, tone=3, sep=2); s.px(38, 13, 3, 0); s.px(38, 12, 3, 4)
    s.ell(30, 13.2, 2.4, 2.0, 4, tone=3, sep=2)
    s.ell(36, 19.6, 4.6, 3.0, 0, rz=3, gain=3, sep=1)
    s.line(32, 19.6, 41, 19.4, 4, 1, 1.0)
    for x in (34, 36, 38, 40):
        s.px(x, 20, 4, 4)
    # рука с огненным шаром
    hx, hy = 50, 31 + (-2 if t else 0)
    s.cap(41, 25, 46, 30, 3.6, 0, 3.0, gain=3.4, sep=1)
    s.cap(46, 30, hx, hy, 3.0, 0, 2.6, gain=3.4)
    s.ell(hx + 4.4, hy - 3.6, 5.2, 5.2, 2, rz=5, gain=3.4, sep=1)
    s.ell(hx + 4.4, hy - 3.6, 2.8, 2.8, 2, tone=4, sep=2)
    s.ell(hx, hy, 3.2, 3.2, 0, rz=3, gain=3)
    s.finish()
    return s


# ---------------------------------------------------------------- 13 волк
def wolf(t):
    s = Spr(48, 32)
    sw = 3.0 if t else -3.0
    # дальние лапы
    for x, k in ((14, 1), (30, -1)):
        s.cap(x, 20, x - sw * k * .5, 27, 2.4, 1, gain=3)
        s.ell(x - sw * k * .5 + 1, 28.4, 3.2, 1.6, 1, rz=1.6)
    # хвост
    s.cap(8, 17, 2, 22 + (1 if t else 0), 2.6, 0, 1.2, gain=3.2)
    s.ell(21, 17.4, 14, 6.8, 0, rz=6, gain=3.4)
    s.ell(21, 21.4, 10, 3, 2, rz=2.4, tone=3, sep=2)
    s.ell(34, 12.6, 6.0, 7, 0, rz=5.4, gain=3.4, sep=1)   # холка
    # ближние лапы
    for x, k in ((10, -1), (28, 1)):
        s.cap(x, 20, x - sw * k * .5, 27, 2.8, 0, 2.4, gain=3.2, sep=1)
        s.ell(x - sw * k * .5 + 1, 28.6, 3.6, 1.8, 0, rz=1.8)
    # голова
    s.ell(38, 12, 6, 5.6, 0, rz=5, gain=3.4, sep=1)
    s.poly([(40, 9.6), (47, 13), (46.6, 16.4), (39, 17)], 0, bevel=2, rz=2, gain=3.2, sep=1)
    s.px(47, 12, 3, 0); s.px(46, 12, 3, 2)
    s.poly([(34.4, 8), (33.4, 2.4), (38, 6.4)], 0, bevel=1, rz=1.2, sep=1)
    s.poly([(38.6, 6.8), (40.4, 2.6), (42, 7.6)], 1, bevel=1, rz=1.2, sep=1)
    s.px(40, 10, 2, 4); s.px(41, 10, 2, 4); s.px(41, 11, 3, 1)
    for x in (42, 44, 46):
        s.px(x, 17, 4, 4)
    s.line(40, 17, 46, 16.6, 3, 1, 1.0)
    s.finish()
    return s


# ---------------------------------------------------------------- 14 мумия
def mummy(t):
    s = Spr(36, 46)
    st = 1.2 if t else -1.2
    for dx, k in ((-3.6, 1), (3.8, -1)):
        x = 17 + dx
        s.cap(x, 29, x + st * k * .5, 40, 3.2, 0, gain=3.2)
        s.ell(x + st * k * .5 + 1.4, 42, 4.4, 2.4, 0, rz=2.2)
    s.ell(17, 27, 7.4, 4, 0, rz=3.2, gain=3.4)
    s.ell(17, 20, 8.2, 8.0, 0, rz=6.6, gain=3.4)
    for i, y in enumerate((14.5, 17.5, 20.5, 23.5, 26.5)):
        s.line(9.6, y + (i % 2) * .6, 24.4, y - 1.0 + (i % 2) * .6, 1, 1, 1.0)
    for i, y in enumerate((31, 34, 37)):
        s.line(12.6, y, 21.4, y - .8, 1, 1, 1.0)
    # руки вперёд
    for (sx, sy, ex, ey, m, sp) in ((12, 15, 24, 18 + (-1 if t else 0), 0, 1), (21, 14, 31, 17 + (-1 if t else 0), 0, 1)):
        s.cap(sx, sy, ex, ey, 2.7, m, 2.3, gain=3.2, sep=1)
        for k in range(2):
            xx = sx + (ex - sx) * (.35 + k * .35)
            yy = sy + (ey - sy) * (.35 + k * .35)
            s.line(xx - 1.4, yy - 2.4, xx + 1.4, yy + 2.4, 1, 1, 1.0)
    # голова
    s.ell(18, 9, 7.2, 7, 0, rz=6, gain=3.4, sep=1)
    for i, y in enumerate((5, 8, 11.6)):
        s.line(11.4, y + i * .4, 24.6, y - 1.2 + i * .4, 1, 1, 1.0)
    s.ell(22.4, 8.6, 2.6, 1.6, 1, tone=0, sep=2)
    s.px(22, 8, 3, 4); s.px(23, 8, 3, 4); s.px(19, 8.4, 3, 3)
    s.poly([(10, 15), (4.4, 22), (7, 24), (11, 19)], 0, bevel=1, rz=1, gain=3, sep=1)
    s.line(5, 23, 4, 29, 0, 1, 1.2)
    s.finish()
    return s


# ---------------------------------------------------------------- 15 гарпия
def harpy(t):
    s = Spr(44, 42)
    fl = -10 if t == 0 else 4
    s.poly([(17, 17), (9, 10 + fl), (1.6, 9 + fl), (6, 15 + fl * .6), (2, 19 + fl * .4), (9, 21 + fl * .3), (14, 25)], 1, bevel=2, rz=1.8, gain=3.0, sep=0)
    s.line(16, 18, 2, 10 + fl, 0, 1, 1.0)
    # лапы
    s.cap(17, 28, 16, 35, 1.7, 3, gain=2.8); s.cap(22, 28, 22, 35, 1.7, 3, gain=2.8)
    for x in (16, 22):
        s.cap(x, 35, x + 4, 37, 1.0, 3, gain=2); s.cap(x, 35, x + 2, 39, 1.0, 3, gain=2)
    # хвост и перья
    s.poly([(14, 28), (6, 36), (12, 38), (18, 33)], 1, bevel=2, rz=1.6, gain=3.2, sep=1)
    s.ell(19, 22, 6.4, 8.6, 0, rz=6, gain=3.4)
    s.ell(19.6, 26, 5, 5.6, 1, rz=2.6, gain=3.0, sep=2)
    # тело человека
    s.ell(21, 15.4, 5.4, 4.6, 2, rz=4, gain=3.4, sep=1)
    # голова
    s.ell(23, 8.6, 5.4, 5.6, 2, rz=4.6, gain=3.2, sep=1)
    s.poly([(18, 6), (14, 11), (11, 20), (16, 18), (18, 14)], 3, bevel=2, rz=1.6, gain=3.2, sep=1)   # волосы
    s.ell(23, 4.2, 5.4, 2.6, 3, rz=2.4, gain=3.2, sep=1)
    s.px(26, 8, 4, 4); s.px(27, 8, 4, 3); s.px(25, 8, 3, 2)
    s.line(25, 11.6, 28, 11.4, 1, 1, 1.0)
    # ближнее крыло
    s.poly([(22, 15), (30, 8 + fl), (39, 6 + fl), (35, 13 + fl * .6), (41, 17 + fl * .4), (33, 20 + fl * .3), (26, 24)], 1, bevel=2, rz=1.8, gain=3.0, sep=1)
    s.line(23, 16, 39, 6 + fl, 0, 1, 1.0)
    s.line(24, 18, 41, 17 + fl * .4, 0, 1, 1.0)
    s.finish()
    return s


# имя, функция, материалы (12 цветов, не используемые = 0)
def P(*c):
    return list(c) + [0x808080] * (12 - len(c))

MONS = [
    ('slime', slime, P(0x4CCB5C, 0x2E8F45, 0xF4F8F0, 0x1A1A28, 0xA8F5A8)),
    ('bat', bat, P(0x5A4A8A, 0x3C2E66, 0xFF4A4A, 0xFFFFFF, 0x9A7ACA)),
    ('skeleton', skeleton, P(0xE4DDC8, 0x6A6458, 0xB0B6C0, 0xFF5040)),
    ('goblin', goblin, P(0x6CB04A, 0x4A7A32, 0x8A5A2B, 0xD8B050, 0xF4F0D8, 0x2A2A18, 0xC8D0D8)),
    ('orc', orc, P(0x5A8A3C, 0x3C5E28, 0x5A4A3A, 0xA0A8B4, 0xF0E8D0, 0x2A2418, 0xB8C0C8)),
    ('dragon', dragon, P(0x3AB05A, 0x2A7A44, 0xFFB030, 0xD8D0A0, 0xFFF0B0, 0xFF6A20)),
    ('spider', spider, P(0x5A3A8A, 0x3A2468, 0xFF3A3A, 0xFFE0E0, 0xB8A0E0)),
    ('ghost', ghost, P(0xDCE8FF, 0x9AB0E0, 0x2A2A48, 0x80F0FF)),
    ('scorpion', scorpion, P(0xC8742A, 0x8A4A1A, 0xA84F12, 0xF0E0A0, 0xFFF4C0, 0x2A1A10)),
    ('golem', golem, P(0x8A8F9C, 0x5E6370, 0x5A9A4A, 0x9AF0FF, 0xE0E8F0)),
    ('imp', imp, P(0xD83A3A, 0x8A1F1F, 0xFFE05A, 0x5A3A2A, 0xF08A6A)),
    ('golem_boss', golem_boss, P(0x5A5560, 0x3A363E, 0xFF7A1A, 0x2A2A30)),
    ('demon', demon, P(0xA83A5A, 0x5A2A4A, 0xFF8A1A, 0x8A2A3A, 0xFFE05A)),
    ('wolf', wolf, P(0x8A94A4, 0x5A6272, 0x1A1A28, 0xF4F4F4, 0xFFD84A)),
    ('mummy', mummy, P(0xD8CCA0, 0x9A8A60, 0x1A1A28, 0xFFD84A)),
    ('harpy', harpy, P(0x8A5ABE, 0x5A3A8A, 0xF0C8A0, 0x4A2A60, 0xFFD84A)),
]

if __name__ == '__main__':
    ims = []
    for nm, fn, mats in MONS:
        for t in (0, 1):
            im = fn(t).image(mats)
            ims.append(im)
    # приводим к одному размеру листа
    W = max(i.width for i in ims); H = max(i.height for i in ims)
    from PIL import Image
    padded = []
    for im in ims:
        c = Image.new('RGBA', (W, H), (0, 0, 0, 0)); c.alpha_composite(im, (0, H - im.height)); padded.append(c)
    sheet(padded, scale=4, cols=8).save(sys.argv[1])
