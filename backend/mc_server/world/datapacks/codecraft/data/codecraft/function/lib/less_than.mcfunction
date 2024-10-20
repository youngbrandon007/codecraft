scoreboard players set ret math 0
$scoreboard players set n1 math $(n1)
$scoreboard players set n2 math $(n2)
execute if score n1 math < n2 math run scoreboard players set ret math 1
return run scoreboard players get ret math