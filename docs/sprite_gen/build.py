import sys, os
from classes import build_all, CLASS_NAMES, CMATS
from knight48 import FRAMES
from monsters import MONS
from art import Spr, sheet, ALPHA
import numpy as np


def chest():
    s = Spr(30, 24)
    s.ell(15, 11.6, 12.4, 8.6, 0, rz=5.4, n=2.2, gain=3.4)
    s.poly([(2.6, 11.6), (27.4, 11.6), (27.4, 21.8), (2.6, 21.8)], 0, bevel=3, rz=3.0, gain=3.4, sep=0)
    s.rect(2.6, 11.0, 27.4, 12.4, 1, 1)
    for x0 in (5.4, 21.0):
        s.rect(x0, 4.4, x0 + 3.6, 21.8, 1, 2)
        s.rect(x0, 4.4, x0 + 1.0, 21.8, 1, 3)
    s.poly([(11.5, 10), (18.5, 10), (18.5, 16.4), (15, 18), (11.5, 16.4)], 2, bevel=2, rz=1.6, gain=3.0, sep=1)
    s.px(14, 13, 3, 0); s.px(15, 13, 3, 0); s.px(14, 14, 3, 0); s.px(15, 15, 3, 0)
    s.px(6, 7, 0, 4); s.px(7, 7, 0, 4); s.px(8, 6, 0, 4)
    s.finish()
    return s


def grave():
    s = Spr(26, 30)
    s.ell(13, 26.4, 11, 3.4, 2, rz=2.4, gain=3.0)
    s.poly([(5, 26), (5, 11), (7, 6), (13, 3), (19, 6), (21, 11), (21, 26)], 0, bevel=3, rz=2.8, gain=3.4)
    s.rect(12, 8.4, 14.2, 19, 1, 1)
    s.rect(8.6, 11.6, 17.6, 13.6, 1, 1)
    s.px(7, 9, 0, 4); s.px(8, 8, 0, 4); s.px(7, 12, 0, 3)
    for x in (3, 5, 20, 23):
        s.line(x, 26, x + 0.6, 22.6, 3, 2, 1.0)
    s.finish()
    return s


def lit(rows):
    return '"""' + '\n'.join(rows) + '"""'


def main(out_kt, preview=None):
    spr = build_all()
    L = ['package com.pixelhero.widget.core', '',
         '// Сгенерировано docs/sprite_gen/build.py. Руками не править: правки пропадут при следующей генерации.',
         '// Символ = (материал, тон): ALPHA[материал * 5 + тон]; "k" контур, "." пусто.', '',
         'object SpriteData {',
         f'    const val ALPHA = "{ALPHA}"',
         '    private fun sp(s: String): Array<String> = s.split(\'\\n\').toTypedArray()', '']
    names = ['WARRIOR', 'MAGE', 'ARCHER', 'ROGUE', 'PALADIN']
    for c in range(5):
        L.append(f'    val HERO_{names[c]} = arrayOf(')
        for fi, f in enumerate(FRAMES):
            L.append('        sp(' + lit(spr[c][fi].rows()) + '),')
        L.append('    )')
        L.append('')
    L.append('    val HEROES = arrayOf(' + ', '.join(f'HERO_{n}' for n in names) + ')')
    L.append('    val HERO_FRAMES = listOf(' + ', '.join(f'"{f}"' for f in FRAMES) + ')')
    L.append('')
    monsters = []
    for i, (nm, fn, mats) in enumerate(MONS):
        fr = [fn(0), fn(1)]
        monsters.append(fr)
        L.append(f'    val MON_{i} = arrayOf(sp(' + lit(fr[0].rows()) + '), sp(' + lit(fr[1].rows()) + '))  // ' + nm)
    L.append('')
    L.append('    val MONSTERS = arrayOf(' + ', '.join(f'MON_{i}' for i in range(len(MONS))) + ')')
    L.append('    val MONSTER_NAMES = listOf(' + ', '.join(f'"{m[0]}"' for m in MONS) + ')')
    L.append('    /** Базовые цвета 12 материалов каждого монстра. */')
    L.append('    val MONSTER_MATS = arrayOf(')
    for nm, fn, mats in MONS:
        L.append('        intArrayOf(' + ', '.join('0x%06X' % m for m in mats) + '),')
    L.append('    )')
    ch, gr = chest(), grave()
    L.append('')
    L.append('    val CHEST = sp(' + lit(ch.rows()) + ')')
    L.append('    val GRAVE = sp(' + lit(gr.rows()) + ')')
    L.append('')
    L.append('    val ALL: List<Pair<String, Array<String>>> = buildList {')
    L.append('        for (c in HEROES.indices) for (f in HEROES[c].indices) add("HERO$c.$f" to HEROES[c][f])')
    L.append('        for (m in MONSTERS.indices) for (f in 0..1) add("MON$m.$f" to MONSTERS[m][f])')
    L.append('        add("CHEST" to CHEST); add("GRAVE" to GRAVE)')
    L.append('    }')
    L.append('}')
    open(out_kt, 'w').write('\n'.join(L) + '\n')
    if preview:
        ims = []
        for c in range(5):
            for f in range(5):
                ims.append(spr[c][f].image(CMATS[c]))
        sheet(ims, scale=3, cols=10).save(preview)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)
