aceptedratio = read("AcceptedRatio.txt",-1,2);
time = aceptedratio(:,$-1);
aceptedratio = aceptedratio(:,$);
plot2d(time,aceptedratio);
