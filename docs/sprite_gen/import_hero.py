"""Импорт нарисованного вручную героя: PNG-кадры -> WarriorData.kt (палитра + текстовые строки).
Использование: python3 import_hero.py docs/art_src/warrior app/src/main/java/com/pixelhero/widget/core/WarriorData.kt"""
import sys, glob, os
from PIL import Image

src, out = sys.argv[1], sys.argv[2]
ALPHA = [c for c in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#%&()*+,-/:;<=>?@[]^_{|}~"] + [chr(c) for c in range(0x410, 0x430)]
names = ['attack_%02d' % i for i in range(15)] + ['walk_%02d' % i for i in range(9)] + ['view_east', 'view_west', 'view_north', 'view_south']
ims = {n: Image.open(os.path.join(src, n + '.png')).convert('RGBA') for n in names}

# опорная точка: центр стоп (нижние 4 строки) кадра attack_00; все кадры выравниваются по низу и стопам
def feet(im):
    bb = im.getbbox()
    bottom = bb[3]
    xs = [x for y in range(bottom - 4, bottom) for x in range(im.width) if im.getpixel((x, y))[3] > 0]
    return (min(xs) + max(xs)) / 2.0, bottom

anchor = {'attack': feet(ims['attack_00']), 'walk': feet(ims['walk_00'])}
CW, CH = 64, 50
OX, OBOT = 26, 48
canvas = {}
for n, im in ims.items():
    fx, fb = feet(im)
    c = Image.new('RGBA', (CW, CH), (0, 0, 0, 0))
    grp = n.split('_')[0]
    if grp == 'view':
        dx, dy = int(round(OX - fx)), OBOT - fb
    else:  # кадры уже согласованы между собой: один сдвиг на всю группу
        dx, dy = int(round(OX - anchor[grp][0])), OBOT - anchor[grp][1]
    c.alpha_composite(im, (dx, dy)) if False else c.paste(im, (dx, dy), im)
    canvas[n] = c

# общая палитра
cols = {}
for c in canvas.values():
    for p in c.get_flattened_data() if hasattr(c, "get_flattened_data") else c.getdata():
        if p[3] > 0: cols[p[:3]] = cols.get(p[:3], 0) + 1
pal = list(cols)
if len(pal) > len(ALPHA):
    allim = Image.new('RGB', (CW, CH * len(canvas)))
    for i, c in enumerate(canvas.values()):
        bg = Image.new('RGB', c.size, (255, 0, 255)); bg.paste(c, (0, 0), c); allim.paste(bg, (0, i * CH))
    q = allim.quantize(colors=len(ALPHA) - 1, method=Image.MEDIANCUT, dither=Image.NONE)
    qp = q.getpalette()
    near = lambda rgb: min(range(len(qp) // 3), key=lambda k: sum((qp[3 * k + j] - rgb[j]) ** 2 for j in range(3)))
    pal = [tuple(qp[3 * k:3 * k + 3]) for k in range(len(ALPHA) - 1)]
    cmap = {}
else:
    cmap = {c: i for i, c in enumerate(pal)}
def idx(rgb):
    if rgb in cmap: return cmap[rgb]
    k = min(range(len(pal)), key=lambda k: sum((pal[k][j] - rgb[j]) ** 2 for j in range(3)))
    cmap[rgb] = k
    return k

def rows(c):
    r = []
    for y in range(CH):
        r.append(''.join('.' if c.getpixel((x, y))[3] < 128 else ALPHA[idx(c.getpixel((x, y))[:3])] for x in range(CW)))
    return r

def lit(r): return '"""' + '\n'.join(r) + '"""'
L = ['package com.pixelhero.widget.core', '',
     '// Сгенерировано docs/sprite_gen/import_hero.py из PNG в docs/art_src/warrior. Руками не править.',
     'object WarriorData {',
     '    private fun sp(s: String): Array<String> = s.split(\'\\n\').toTypedArray()',
     '    const val CHARS = "' + ''.join(ALPHA[:len(pal)]) + '"',
     '    val PALETTE = intArrayOf(' + ', '.join('0x%06X' % (r << 16 | g << 8 | b) for r, g, b in pal) + ')', '']
L.append('    /** 15 кадров анимации атаки (замах, удар, возврат). */')
L.append('    val ATTACK = arrayOf(')
for i in range(15): L.append('        sp(' + lit(rows(canvas['attack_%02d' % i])) + '),')
L.append('    )')
L.append('    /** 9 кадров цикла ходьбы вправо. */')
L.append('    val WALK = arrayOf(')
for i in range(9): L.append('        sp(' + lit(rows(canvas['walk_%02d' % i])) + '),')
L.append('    )')
for v in ('east', 'west', 'north', 'south'):
    L.append('    val VIEW_%s = sp(%s)' % (v.upper(), lit(rows(canvas['view_' + v]))))
L.append('    const val W = %d' % CW)
L.append('    const val H = %d' % CH)
L.append('    /** Центр стоп по горизонтали в кадре. */')
L.append('    const val FEET_X = %d' % OX)
L.append('}')
open(out, 'w').write('\n'.join(L) + '\n')
print('colors', len(pal), 'bytes', os.path.getsize(out))
