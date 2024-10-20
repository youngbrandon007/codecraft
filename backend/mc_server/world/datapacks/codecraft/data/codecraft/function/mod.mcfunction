$scoreboard players set n1 math $(p0)
$scoreboard players set n2 math $(p1)

scoreboard players operation n1 math %= n2 math
return run scoreboard players get n1 math