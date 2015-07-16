nodeutilization= read("nodeutilization.txt",-1,2);
time = nodeutilization(:,$-1);
nodeutilization = nodeutilization(:,$);
plot2d(time,nodeutilization);
