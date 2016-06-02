import sys

f = open(sys.argv[1],'r')
#f = open('/home/li/Dropbox/simulation3105','r')
orig = f.read()
temp = orig
one=[0.0]*5
shen=[0.0]*5
rank2=[0.0]*5
rank3=[0.0]*5
acceptedRatio = [one,shen,rank2,rank3]


while temp.find('Number')!=-1:
    index = temp.find('Number')
    temp = temp[index+6:]
    if(temp[:temp.find('Number')]) :
        sim = temp[:temp.find('Number')-1]
    else:
        sim = temp


    for i in range(0,5):
        index  = sim.find('MappedRevenue')
        one[i] = one[i]+float(sim[index+14:index+20])
        sim = sim[index+14:]

        index  = sim.find('MappedRevenue')
        shen[i] = shen[i]+float(sim[index+14:index+20])
        sim = sim[index+14:]

        index  = sim.find('MappedRevenue')
        rank2[i] = rank2[i]+float(sim[index+14:index+20])
        sim = sim[index+14:]

        index  = sim.find('MappedRevenue')
        rank3[i] = rank3[i]+float(sim[index+14:index+20])
        sim = sim[index+14:]


for i in range(0,5):
    one[i] = one[i]/10
    shen[i] = shen[i]/10
    rank2[i] = rank2[i]/10
    rank3[i] = rank3[i]/10

print one
print shen
print rank2
print rank3

import matplotlib.pyplot as plt
import numpy as np
x=[2,3,4,5,6]
plt.plot(x,one)
plt.plot(x,shen)
plt.plot(x,rank2)
plt.plot(x,rank3)

plt.show()
