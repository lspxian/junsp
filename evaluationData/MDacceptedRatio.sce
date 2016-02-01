figure();
aceptedratio = read("MultiDomainRanking2_AcceptedRatio_l4_c0.txt",-1,2);
time = aceptedratio(:,$-1);
aceptedratio = aceptedratio(:,$);
plot(time,aceptedratio, 'r-o');

aceptedratio = read("Shen2014_AcceptedRatio_l4_c0.txt",-1,2);
time = aceptedratio(:,$-1);
aceptedratio = aceptedratio(:,$);
plot(time,aceptedratio,'b-+');

aceptedratio = read("MultiDomainAsOneDomain_AcceptedRatio_l4_c0.txt",-1,2);
time = aceptedratio(:,$-1);
aceptedratio = aceptedratio(:,$);
plot(time,aceptedratio,'k->');

//aceptedratio = read("MDasOD2_AcceptedRatio.txt",-1,2);
//time = aceptedratio(:,$-1);
//aceptedratio = aceptedratio(:,$);
//plot(time,aceptedratio,'g-*');
