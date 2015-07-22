total= read("TotalRevenue.txt",-1,2);
time = total(:,$-1);
total= total(:,$);
plot2d(time,total);
