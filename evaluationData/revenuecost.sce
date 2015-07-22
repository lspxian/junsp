revcost= read("RevenueCost.txt",-1,2);
time = revcost(:,$-1);
revcost= revcost(:,$);
plot2d(time,revcost);
