# Return if x < 10
execute store result storage codecraft:math n1 int 1.0 run data get storage minecraft:var x
data modify storage codecraft:math n2 set value 10
return run function codecraft:lib/less_than with storage codecraft:math