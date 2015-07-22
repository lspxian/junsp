mapped= read("MappedRevenue.txt",-1,2);
time = mapped(:,$-1);
mapped= mapped(:,$);
plot2d(time,mapped);
