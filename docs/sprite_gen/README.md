# Генератор спрайтов

Герои и монстры не рисуются вручную по пикселям: `art.py` это мини-рендерер (части тела с освещением сверху слева,
5 тонов на материал, внутренние швы и тёмный внешний контур), `knight48.py` и `classes.py` собирают из него героев,
`monsters.py` монстров. `build.py` выгружает всё в `SpriteData.kt`.

    pip install numpy pillow
    cd docs/sprite_gen
    python3 build.py ../../app/src/main/java/com/pixelhero/widget/core/SpriteData.kt preview.png

- Один символ спрайта = (материал, тон). Цвета материалов задаёт `Sprites.kt` (класс, тир брони/оружия, зона), поэтому
  перекрашивать спрайты не нужно.
- Новый монстр: функция `def имя(t)` в `monsters.py` (t = 0/1 кадр анимации) и строка в списке `MONS`.
  Порядок в `MONS` совпадает с индексами типов в `Balance.ZONE_TYPES`.
- Новый класс героя: функция в `classes.py`, добавить в `build_all`, цвета в `Sprites.kt` (CLASS_MAIN и т.д.).
