# Генератор пиксель-арта: собирает героев-классов из частей (голова, торс, ноги, оружие) и монстров.
import sys
OUT = sys.argv[1]

def chk(name, rows, w=None, h=None):
    w = w or len(rows[0])
    for i, r in enumerate(rows):
        assert len(r) == w, f"{name} row {i} len {len(r)} != {w}: '{r}'"
    if h: assert len(rows) == h, f"{name} height {len(rows)} != {h}"
    return rows

# ---------------- ГЕРОИ 16x16 ----------------
FACE = ["..ksssssk..", "..ksssksk..", "...ksssk..."]  # rows 3-5 (11 wide)
HEAD = {
 0: ["..kkkkk....", ".krrgggkk..", ".kgggggggk."],           # воин: шлем с плюмажем
 1: ["....kk.....", "...kcck....", ".kkcccckk.."],           # маг: остроконечная шляпа
 2: ["...kkkk....", "..kccccck..", ".kcccccccck"[:11]],     # лучник: капюшон
 3: ["...kkkk....", "..kCCCCk...", ".kCCCCCCk.."],           # вор: капюшон
 4: ["..ww.kk.ww.", "..kwwwwwk..", ".kwwwwwwwk."],           # паладин: шлем с крыльями
}
HEAD[2][2] = ".kccccccck."
for k in HEAD: chk(f"head{k}", HEAD[k], 11)
FACEV = {1: ["..ksssssk..", "..ksssksk..", "...kwwwk..."], 3: ["..ksssssk..", "..ksssksk..", "...kCCCk..."]}
TORSO = {
 0: [".kkbbbbbkk.", "kcbbbbbbbbk", "kcbBBBBBbbk", "kckhhyhhkk.", ".kkbbbbbkk."],
 1: ["..kkbbbkk..", ".kbbbbbbbk.", ".kbbyybbbk.", "kbbbbbbbbbk", "kBBBBBBBBBk"],
 2: [".kkbbbbkk..", "khkbbbbbbk.", "khhbBBBbbk.", "kkkbhhhbbk.", ".kkbbbbbkk."],
 3: [".kkbbbbbkk.", "krrbbbbbbk.", "krrBbbbbBk.", "kkkbhhhbbk.", ".kkbbbbbkk."],
 4: [".kkbbbbbkk.", "kcbbwwwbbbk", "kcbbwywbbbk", "kCkbwwwbbk.", ".kkbbbbbkk."],
}
for k in TORSO: chk(f"torso{k}", TORSO[k], 11, 5)
PANTS_A = ["....kBBkkBBk....", "....kBBk.kBBk...", "....kBBk.kBBk...", "....khhk.khhk...", "....kkkk.kkkk..."]
PANTS_B = ["....kBBkkBBk....", "...kBBk..kBBk...", "..kBBk....kBBk..", "..khhk....khhk..", "..kkkk....kkkk.."]
ROBE_A = ["..kBBBBBBBBBk...", "..kBBBBBBBBBk...", "..kkBBBBBBBkk...", "....khhk.khhk...", "....kkkk.kkkk..."]
ROBE_B = ["...kBBBBBBBBBk..", "...kBBBBBBBBBk..", "...kkBBBBBBBkk..", "..khhk....khhk..", "..kkkk....kkkk.."]
for n, v in [("pa", PANTS_A), ("pb", PANTS_B), ("ra", ROBE_A), ("rb", ROBE_B)]: chk(n, v, 16, 5)

def blank(): return [["."] * 16 for _ in range(16)]
def put(g, pts):
    for x, y, ch in pts:
        if 0 <= x < 16 and 0 <= y < 16: g[y][x] = ch
def ln(x0, y0, x1, y1, ch):
    pts = []; n = max(abs(x1 - x0), abs(y1 - y0))
    for i in range(n + 1):
        t = i / n if n else 0
        pts.append((round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), ch))
    return pts

def weapon(cls, pose):  # pose: 0 idle, 1 windup, 2 strike
    p = []
    if cls == 0:
        if pose == 0: p += ln(13,2,13,8,'x') + [(13,1,'w'),(12,9,'y'),(13,9,'y'),(14,9,'y'),(13,10,'h'),(13,11,'h'),(12,10,'s')]
        if pose == 1: p += ln(14,0,14,7,'x') + [(13,8,'y'),(14,8,'y'),(15,8,'y'),(14,9,'h'),(14,10,'h'),(13,9,'s')]
        if pose == 2: p += ln(11,8,15,8,'x') + [(15,8,'w'),(11,7,'y'),(11,9,'y'),(10,8,'s'),(14,5,'w'),(15,6,'w'),(15,7,'w')]
    if cls == 1:
        if pose == 0: p += ln(13,3,13,13,'h') + [(12,1,'o'),(14,1,'o'),(13,0,'o'),(13,2,'o'),(13,1,'w'),(12,9,'s')]
        if pose == 1: p += ln(14,2,14,12,'h') + [(13,0,'o'),(15,0,'o'),(14,-1,'o'),(14,1,'o'),(14,0,'w'),(13,8,'s')]
        if pose == 2: p += ln(11,8,14,8,'h') + [(15,8,'w'),(15,7,'o'),(15,9,'o'),(14,7,'o'),(14,9,'o'),(10,8,'s')]
    if cls == 2:
        bow = [(13,3,'h'),(14,4,'h'),(14,5,'h'),(14,6,'h'),(14,7,'h'),(14,8,'h'),(13,9,'h'),(12,6,'s')]
        if pose == 0: p += bow + ln(13,4,13,8,'w')
        if pose == 1: p += bow + [(13,4,'w'),(12,5,'w'),(11,6,'w'),(12,7,'w'),(13,8,'w')] + ln(11,6,15,6,'x') + [(15,6,'w')]
        if pose == 2: p += bow + ln(13,4,13,8,'w')
    if cls == 3:
        if pose == 0: p += ln(13,5,13,8,'x') + [(12,9,'y'),(14,9,'y'),(13,9,'h'),(13,10,'h'),(12,10,'s')]
        if pose == 1: p += ln(14,3,14,6,'x') + [(13,7,'y'),(15,7,'y'),(14,7,'h'),(14,8,'h'),(13,8,'s')]
        if pose == 2: p += ln(12,6,15,9,'x') + ln(12,9,15,6,'x') + [(13,7,'w'),(14,8,'w'),(11,7,'s')]
    if cls == 4:
        if pose == 0: p += ln(13,4,13,11,'h') + [(12,1,'x'),(13,1,'x'),(14,1,'x'),(12,2,'x'),(13,2,'y'),(14,2,'x'),(12,3,'x'),(13,3,'x'),(14,3,'x'),(12,9,'s')]
        if pose == 1: p += ln(14,3,14,10,'h') + [(13,0,'x'),(14,0,'x'),(15,0,'x'),(13,1,'x'),(14,1,'y'),(15,1,'x'),(13,2,'x'),(14,2,'x'),(15,2,'x'),(13,9,'s')]
        if pose == 2: p += ln(11,8,14,8,'h') + [(14,6,'x'),(15,6,'x'),(14,7,'x'),(15,7,'y'),(14,9,'x'),(15,9,'x'),(14,10,'x'),(15,10,'x'),(15,8,'x'),(10,8,'s')]
    return p

def hero(cls, legs, pose):
    g = blank()
    head = HEAD[cls]; face = FACEV.get(cls, FACE)
    body = head + face + TORSO[cls]
    for j, row in enumerate(body):
        for i, ch in enumerate(row):
            if ch != '.': g[j][i + 2] = ch
    for j, row in enumerate(legs):
        for i, ch in enumerate(row):
            if ch != '.': g[11 + j][i] = ch
    put(g, weapon(cls, pose))
    return ["".join(r) for r in g]

HEROES = []
for c in range(5):
    A, B = (ROBE_A, ROBE_B) if c == 1 else (PANTS_A, PANTS_B)
    frames = [hero(c, A, 0), hero(c, B, 0), hero(c, A, 1), hero(c, A, 2)]
    for f in frames: chk(f"hero{c}", f, 16, 16)
    HEROES.append(frames)

# ---------------- МОНСТРЫ 14x14 ----------------
def squash(a, rm):  # приплюснуть: убрать строку rm, добавить пустую сверху
    return ["." * len(a[0])] + [r for i, r in enumerate(a) if i != rm]
def repl(a, start, rows):
    return a[:start] + rows

M = {}
slime = ["..............","..............","..............","..............",".....kkkk.....","...kkGGGGkk...","..kGGwwGGGGk..",".kGGGwGGGGGGk.",".kGGGGGGGGGGk.",".kGwkGGGGwkGk.",".kGGGGGGGGGGk.",".kDGGGkkGGGDk.","..kDDDDDDDDk..","...kkkkkkkk..."]
M[0] = [slime, squash(slime, 7)]
bat_a = ["..............","..............","..............",".....k..k.....","k....kppk....k","kk..kppppk..kk","kpkkpwppwpkkpk","kppppPPPPppppk",".kpppPPPPpppk.","..kkppppppkk..","....kppppk....",".....kppk.....","......kk......","..............",]
bat_b = ["..............","..............","..............",".....k..k.....","....kppppk....","...kpwppwpk...","kk.kppppppk.kk","kpkkpPPPPpkkpk","kppkkppppkkppk",".kk.kppppk.kk.","....kppppk....",".....kppk.....","......kk......","..............",]
M[1] = [bat_a, bat_b]
skel = ["..............","....kkkkkk....","...kWWWWWWk...","..kWWWWWWWWk..","..kWkkWWkkWk..","..kWWWWWWWWk..","...kWkWkWkk...","....kkWWkk....","..kkkWWWWkkk..","..kWkWWWWkWk..","...kkWkkWkk...","....kWkkWk....","...kWk..kWk...","..kkk....kkk.."]
M[2] = [skel, repl(skel, 12, ["....kWkkWk....","....kkk.kkk..."])]
gob = ["..............",".kk........kk.","kGGkkkkkkkkGGk",".kGGGGGGGGGGk.","..kGGrGGrGGk..","..kGGGGGGGGk..","...kGGkkGGk...","....kkGGkk....","...knnnnnnk...","..kGnnnnnnGk..","..kGnnNNnnGk..","...knnnnnnk...","...kNk..kNk...","..kkkk..kkkk.."]
M[3] = [gob, repl(gob, 12, ["....kNkkNk....","....kkk.kkk..."])]
orc = ["....kkkkkk....","...kDDDDDDk...","..kDDDDDDDDk..","..kDrDDDDrDk..","..kDDDDDDDDk..","..kDwDkkDwDk..","...kDDDDDDk...",".kkggggggggkk.","kDkggggggggkDk","kDkgwggggwgkDk",".kkggggggggkk.","..kggkkkkggk..","..kDDk..kDDk..",".kkkkk..kkkkk."]
M[4] = [orc, repl(orc, 12, ["...kDDkkDDk...","...kkkkkkkk..."])]
spider = ["..............","..............","..............","..............","....kkkkkk....","...kPPPPPPk...","k..kPrPPrPk..k",".k.kPPPPPPk.k.","..kkPPppPPkk..",".kkPPppppPPkk.","kkPPPPPPPPPPkk","k.kPPPppPPPk.k","k..kPPPPPPk..k","...kkkkkkkk..."]
M[6] = [spider, repl(spider, 11, [".k.kPPPppPk.k.","k.kkPPPPPPkk.k","k..kkkkkkkk..k"])]
ghost = ["..............",".....kkkk.....","...kkhhhhkk...","..khhhhhhhhk..",".khhhhhhhhhhk.",".khhBBhhBBhhk.",".khhBBhhBBhhk.",".khhhhkkhhhhk.",".khhhhhhhhhhk.","kkhhhhhhhhhhkk",".khhHhhhhHhhk.",".kkhhkhhkhhkk.","..kk.kkkk.kk..",".............."]
M[7] = [ghost, repl(ghost, 11, [".khkhhkkhhkhk.","..k.kk..kk.k..","..............",])]
scorp = ["..............",".........kkk..","........koOk..","........kook..",".......kooOk..","..kkkkkkookk..",".koooooooooOk.","koorooooooOOkk","kkoooooooooOkk",".kkoooooooOkk.","k.kkkkkkkkkk.k","k..k..kk..k..k","..............",".............."]
M[8] = [scorp, repl(scorp, 10, ["kk.kkkkkkkk.kk",".k.k..kk..k.k.","..............","..............",])]
golem = ["...kkkkkkkk...","..kSSSSSSSSk..","..kSlSSSSlSk..","..kSSSSSSSSk..","kkkkSSSSSSkkkk","kSSkSSssSSkSSk","kSSkSSSSSSkSSk","kSsSkSsSSskSSk"[:14],"kkkkSSSSSSkkkk","...kSSSSSSk...","...kSSkkSSk...","...kSSk..kSSk.","..kkSSk..kSSkk","..kkkkk..kkkkk"]
golem[7] = "kSSkSsSSsSkSSk"
M[9] = [golem, repl(golem, 11, ["...kSSSkkSSk..","...kkkk..kkkk.","..............",])]
imp = ["..k........k..",".kmk......kmk.",".kmmkkkkkkmmk.","..kmmmmmmmmk..","..kmlmmmmlmk..","..kmmmmmmmmk..","..kmmkkkkmmk..",".kkkmmmmmmkkk.","kMkkmmmmmmkkMk","kMMkmmmmmmkMMk",".kMkkmmmmkkMk.","..kk.kmmk.kk..","....kmk.kmk...","....kkk.kkk..."]
imp_b = repl(imp, 7, [".kkkmmmmmmkkk.","kMk.kmmmmk.kMk","kMMkkmmmmkkMMk",".kMkkmmmmkkMk.","..kk.kmmk.kk..","....kmk.kmk...","....kkk.kkk..."])
M[10] = [imp, imp_b]
wolf = ["..............","..............","..............","..............",".........k..k.","........kgkkgk","..kkkkkkgggggk",".kgggggggrgggk","kgggggggggggkk","ksgggggggggk..",".kkgggggggkk..",".kgkgk..kgkgk.",".kgk.k..kgk.k.","kkkk.k..kkk.kk"]
wolf_b = repl(wolf, 11, [".kgkgk..kgkgk.","kgk.kg..kg.kgk","kk..kk..kk..kk"])
M[13] = [wolf, wolf_b]
mummy = ["..............","....kkkkkk....","...kWWWWWWk...","...kWrWWrWk...","...kWWWWWWk...","....kkWWkk....","..kkWwWWwWkk..","kWkWWWWWWWWkWk","kWkWwWWWWwWkWk",".kkWWWWWWWWkk.","..kWwWWWWwWk..","..kWWWkkWWWk..","..kWWk..kWWk..",".kkkkk..kkkkk."]
M[14] = [mummy, repl(mummy, 12, ["...kWWkkWWk...","...kkkkkkkk...",])]
harpy = ["..............","..............","....kkkk......","...kpppppk....","...kpsssspk...","...kpsksskk...","...kkssssk....","kPkkpppppkkPk.","kPPkpppppkPPk.","kPPPkpppkPPPk.",".kPPkkpppkkPk.","..kkk.kpk.kkk.","......kyk.....","......kkk....."]
harpy_b = repl(harpy, 7, [".kPkkpppppkkPk","kPk.kppppk.kPk","kPPkkppppkkPPk",".kPPkpppppkPPk","..kkkkpppkkkk.","......kyk.....","......kkk....."])
M[15] = [harpy, harpy_b]
for k, fr in M.items():
    for f in fr:
        chk(f"mon{k}", f, 14, 14)

# ---------------- БОССЫ 22x18 ----------------
def mirror(half):
    return [r + r[::-1] for r in half]
def pad(a, h):
    return ["." * len(a[0])] * (h - len(a)) + a

dragon = ["................kk.kk.","...............kRRkRRk","..kk.........kRrrrrrrk",".kRRk........kRrrwrrrk","kRRRRk......kRrrrrrrrk","kRRRRRRkkkkRRrrrrrrkkk",".kRRRRRRRRRRRrrrrryyyk",".kkRRRRRRRRRrrrrrryyk.","..kkRRRRRRRRrrrrryyyk.","...kRRRRRRRRrrrrryyk..","....kkRRRRRRrrrrryyk..","......kRRRRRrrrrryk...",".....kRRkkRRrrrrrkk...","....kRRk..kRRk..kRRk..","...kRRk....kRRk..kkk..","..kkkk......kkkk......"]
# поправка длины строк: подгоняем шириной 22
def fix(r, w=22):
    return (r + "." * w)[:w]
dragon = [fix(r) for r in dragon]
dragon_b = dragon[:13] + [fix("....kRRk...kRRk.kRRk.."), fix("...kRRk.....kRRkkkk..."), fix("..kkkk.......kkkk....")]
M[5] = [pad(dragon, 18), pad(dragon_b, 18)]

golem_half = ["...kkkkkkkk","..kSSSSSSSS","..kSSSSSSSS","..kSllSSSSS","..kSSSSSSSS","..kSSkkkkkk","kkkkSSSSSSS","kSSkSSsSSSS","kSSSkSSsSSS","kSSSkSSSSSS","kSSSkSSsSSS","kkkkkSSSSsS","...kSSSSSSS","...kSSSkkkk","...kSSSk...","..kkSSSk...","..kSSSSk...","..kkkkkk..."]
golem_half_b = golem_half[:14] + ["...kSSSkk..","...kSSSSSk.","..kSSSSSSk.","..kkkkkkkk."]
M[11] = [mirror(golem_half), mirror(golem_half_b)]
demon_half = ["kk.........","kmk........","kmmk.......",".kmmkkkkkkk","..kmmmmmmmm","..kmmlmmmmm","..kmmmmmmmm","..kmmmkkkkk","...kmmmwmwm","kkkkkkmmmmm","kMMMMkmmmmm","kMMMMMkmmmm","kMMMMMMkmmm",".kMMMMMkmmm","..kMMMkkmmm","...kkk.kmmm","......kmmk.","......kkkk."]
demon_half_b = demon_half[:8] + ["..kkkkkmmmm","kMMMMkkmmmm","kMMMMMkmmmm","kMMMMMMkmmm",".kMMMMkkmmm","..kkMMkkmmm","...kkk.kmmm","......kmmk.","......kmmk.","......kkkk."]
M[12] = [mirror(demon_half), mirror(demon_half_b)]
for k in (5, 11, 12):
    for f in M[k]: chk(f"boss{k}", f, 22, 18)

def kt(rows, ind="            "):
    return "arrayOf(\n" + "".join(f'{ind}"{r}",\n' for r in rows) + ind[:-4] + ")"

names = ["HERO_WARRIOR", "HERO_MAGE", "HERO_ARCHER", "HERO_ROGUE", "HERO_PALADIN"]
fn = ["IDLE", "WALK", "WINDUP", "STRIKE"]
src = "package com.pixelhero.widget.core\n\n// Сгенерировано gen.py. Не править руками: правьте генератор.\nobject SpriteData {\n"
all_list = []
for c in range(5):
    for f in range(4):
        nm = f"{names[c]}_{fn[f]}"
        src += f"    val {nm} = {kt(HEROES[c][f], '        ')}\n"
        all_list.append(nm)
src += "\n    /** [класс][кадр: 0 стойка, 1 шаг, 2 замах, 3 удар] */\n    val HEROES: Array<Array<Array<String>>> = arrayOf(\n"
for c in range(5):
    src += "        arrayOf(" + ", ".join(f"{names[c]}_{fn[f]}" for f in range(4)) + "),\n"
src += "    )\n\n"
mnames = {0:"SLIME",1:"BAT",2:"SKEL",3:"GOBLIN",4:"ORC",5:"DRAGON",6:"SPIDER",7:"GHOST",8:"SCORP",9:"GOLEM",10:"IMP",11:"BGOLEM",12:"BDEMON",13:"WOLF",14:"MUMMY",15:"HARPY"}
for t in range(16):
    for f in range(2):
        nm = f"{mnames[t]}_{'AB'[f]}"
        src += f"    val {nm} = {kt(M[t][f], '        ')}\n"
        all_list.append(nm)
src += "\n    val MONSTERS: Array<Array<Array<String>>> = arrayOf(\n"
for t in range(16):
    src += f"        arrayOf({mnames[t]}_A, {mnames[t]}_B),\n"
src += "    )\n\n    val ALL: List<Pair<String, Array<String>>> = listOf(\n"
for nm in all_list:
    src += f'        "{nm}" to {nm},\n'
src += "    )\n}\n"
open(OUT, "w", encoding="utf-8").write(src)
print("ok", len(all_list))
