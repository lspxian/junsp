linkcost= read("linkCost.txt",-1,2);
time = linkcost(:,$-1);
linkcost = linkcost(:,$);
plot2d(time,linkcost);
