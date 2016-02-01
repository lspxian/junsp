figure();
linkutilization= read("MultiDomainRanking_linkUtilization.txt",-1,2);
time = linkutilization(:,$-1);
linkutilization = linkutilization(:,$);
plot(time,linkutilization, 'r-o');

linkutilization= read("Shen2014_linkUtilization.txt",-1,2);
time = linkutilization(:,$-1);
linkutilization = linkutilization(:,$);
plot(time,linkutilization, 'b-+');

linkutilization= read("MultiDomainAsOneDomain_linkUtilization.txt",-1,2);
time = linkutilization(:,$-1);
linkutilization = linkutilization(:,$);
plot(time,linkutilization, 'k->');

//linkutilization= read("MDasOD2_linkUtilization.txt",-1,2);
//time = linkutilization(:,$-1);
//linkutilization = linkutilization(:,$);
//plot(time,linkutilization, 'g-*');
