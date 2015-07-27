figure();
mapped= read("MappedRevenue.txt",-1,2);
time = mapped(20:1500,$-1);
average= mapped(20:1500,$) ./ time;
plot2d(time,average);
