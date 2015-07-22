costrev= read("CostRevenue.txt",-1,2);
time = costrev(:,$-1);
costrev = costrev(:,$);
plot2d(time,costrev);
