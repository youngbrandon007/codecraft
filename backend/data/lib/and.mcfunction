$scoreboard players set n1 math $(p0)
$scoreboard players set n2 math $(p1)

execute unless score n1 math matches 0 run execute unless score n2 math matches 0 run return 1
return 0