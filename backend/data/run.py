
i = 0
r = 20
rr = r * 2


def sphere():
    x = i % rr
    y = (i / rr) % rr
    z = ((i / rr) / rr) % rr

    dx = x - r
    dy = y - r
    dz = z - r
    sx = dx * dx
    sy = dy * dy
    sz = dz * dz
    dis = sx + sy + sz
    if dis <= (r * r):
        set_block(dx, dy, dz)


def tick():
    i = i + 1

    x = i % rr
    y = (i / rr) % rr

    dx = x - r
    dy = y - r

    val = (dx + dy) % 8

    if val >= 4:
        val = 8 - val

    set_block(dx, val, dy)