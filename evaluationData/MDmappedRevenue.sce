figure();
mapped= read("MultiDomainRanking_MappedRevenue_l2.txt",-1,2);
time = mapped(:,$-1);
mapped= mapped(:,$);
plot(time,mapped,'r-o');

mapped= read("Shen2014_MappedRevenue_l2.txt",-1,2);
time = mapped(:,$-1);
mapped= mapped(:,$);
plot(time,mapped,'b-+');

mapped= read("MultiDomainAsOneDomain_MappedRevenue_l2.txt",-1,2);
time = mapped(:,$-1);
mapped= mapped(:,$);
plot(time,mapped,'k->');

//mapped= read("MDasOD2_MappedRevenue.txt",-1,2);
//time = mapped(:,$-1);
//mapped= mapped(:,$);
//plot(time,mapped,'g-*');
