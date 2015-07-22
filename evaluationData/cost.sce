cost= read("Cost.txt",-1,2);
time = cost(:,$-1);
cost = cost(:,$);
plot2d(time,cost);
