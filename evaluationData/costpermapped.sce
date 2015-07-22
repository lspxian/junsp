costpermapped= read("CostPerMapped.txt",-1,2);
time = costpermapped(:,$-1);
costpermapped = costpermapped(:,$);
plot2d(time,costpermapped);
