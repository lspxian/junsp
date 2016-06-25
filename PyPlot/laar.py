"""
Parameters : 
1 f - file name
"""
import sys

f = open(sys.argv[1],'r')
#metric = sys.argv[2]
metric  = 'Accepted_Ratio'
start=1
myLambda=1+8	#in vne lambda+1
orig = f.read()
temp = orig
heu1=[0.0]*myLambda
heu2=[0.0]*myLambda
heu3=[0.0]*myLambda
exact=[0.0]*myLambda
bw=[0.0]*myLambda
acceptedRatio = [heu1,heu2,heu3,exact,bw]

while temp.find('Number:')!=-1:
    index = temp.find('Number:')
    temp = temp[index+6:]
    if(temp[:temp.find('Number:')]) :
        sim = temp[:temp.find('Number:')-1]
    else:
        sim = temp

    for i in range(start,myLambda):
        index  = sim.find('Accepted_Ratio')
        heu1[i] = heu1[i]+float(sim[index+15:index+21])
        sim = sim[index+25:]

        index  = sim.find('Accepted_Ratio')
        heu2[i] = heu2[i]+float(sim[index+15:index+21])
        sim = sim[index+15:]

        index  = sim.find('Accepted_Ratio')
        heu3[i] = heu3[i]+float(sim[index+15:index+21])
        sim = sim[index+15:]

        index  = sim.find('Accepted_Ratio')
        exact[i] = exact[i]+float(sim[index+15:index+21])
        sim = sim[index+15:]

	index  = sim.find('Accepted_Ratio')
        bw[i] = bw[i]+float(sim[index+15:index+21])
        sim = sim[index+15:]

"""
#calculate average
for i in range(0,myLambda):
    heu1[i] = heu1[i]/10
    heu2[i] = heu2[i]/10
    heu3[i] = heu3[i]/10
    exact[i] = exact[i]/10
    bw[i] = bw[i]/10
"""

print heu1
print heu2
print heu3
print exact
print bw

#write to a file in latex format
fwriter = open('latex.txt','w')
latex = '\\begin{figure}\n\\begin{tikzpicture}[scale=1.0]\n\\begin{axis}[\nxlabel={arrival rate $\lambda$},\nylabel={Accepted Ratio \%},\nxmin=1, xmax=9,\nymin=3, ymax=10,\nxtick={1,2,3,4,5,6,7,8},\nytick={4,5,6,7,8,9,10},\nlegend pos=south east,\nlegend style={font=\\tiny},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

latex = latex + '\\addplot[\n	color=cyan,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(heu1[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=violet,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(heu2[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=blue,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(heu3[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=green,\n	mark=o,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(exact[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=red,\n	mark=triagnle,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(bw[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$heu1$,$heu2$,$heu3$,$exact$,$bw$}\n\\end{axis}\n\\end{tikzpicture}\n\\caption{Acceptance ratio}\n\\label{l-ar}\n\\end{figure}'

fwriter.write(latex)
f.closed

'''
#plot figure in python
import matplotlib.pyplot as plt
import numpy as np
x=list(range(start,myLambda))
plt.plot(x,heu1[start:],'y-')
plt.plot(x,heu2[start:],'g-')
plt.plot(x,heu3[start:],'m-')
plt.plot(x,exact[start:],'b-')
plt.plot(x,bw[start:],'r-')

plt.ylabel('Accepted Ratio')
plt.xlabel('lambda')
plt.show()
'''

