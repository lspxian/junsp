"""
Parameters : 
1 f - file name
"""
import sys
import re

f = open(sys.argv[1],'r')
#metric = sys.argv[2]
metric = 'Link_Utilization'
metric2 = 'Average link utilization'
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
        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	heu1[i] = heu1[i]+float(m.group(0))

        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	heu2[i] = heu2[i]+float(m.group(0))

        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	heu3[i] = heu3[i]+float(m.group(0))

        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	exact[i] = exact[i]+float(m.group(0))

	index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	bw[i] = bw[i]+float(m.group(0))

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
latex = '\\begin{figure}\n\\begin{tikzpicture}[scale=1.0]\n\\begin{axis}[\nxlabel={arrival rate $\lambda$},\nylabel={'+metric2+' \%},\nxmin=1, xmax=9,\nymin=0.07, ymax=0.67,\nxtick={1,2,3,4,5,6,7,8},\nytick={0.1,0.2,0.3,0.4,0.5,0.6},\nlegend pos=south east,\nlegend style={font=\\tiny},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

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

latex = latex + '\\addplot[\n	color=red,\n	mark=triangle,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(bw[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$heu1$,$heu2$,$heu3$,$exact$,$bw$}\n\\end{axis}\n\\end{tikzpicture}\n\\caption{'+metric2+'}\n\\label{l-lu}\n\\end{figure}'

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
