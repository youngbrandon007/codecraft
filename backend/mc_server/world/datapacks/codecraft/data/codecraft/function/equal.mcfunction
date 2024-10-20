$scoreboard players set n1 math $(p0)
$scoreboard players set n2 math $(p1)

execute if score n1 math = n2 math run return 1
return 0