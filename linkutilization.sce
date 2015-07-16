linkutilization= read("linkutilization.txt",-1,2);
time = linkutilization(:,$-1);
linkutilization = linkutilization(:,$);
plot2d(time,linkutilization);
