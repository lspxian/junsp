cost= read("cost.txt",-1,2);
time = cost(:,$-1);
cost = cost(:,$);
plot2d(time,cost);
