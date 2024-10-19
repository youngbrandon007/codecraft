# While loop
# if x >= 10, escape
execute unless function codecraft:while/while2 run return fail

# else, run the content and loop
function codecraft:lib/print_var {name: x}

# x = x + 1
execute store result storage codecraft:math n1 int 1.0 run data get storage minecraft:var x
data modify storage codecraft:math n2 set value 1
execute store result storage minecraft:var x int 1.0 run function codecraft:lib/add with storage codecraft:math

# Loop
function codecraft:while/while1