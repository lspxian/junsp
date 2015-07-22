tempcost= read("tempCostRevenue.txt",-1,2);
time = tempcost(:,$-1);
tempcost= tempcost(:,$);
plot2d(time,tempcost);
