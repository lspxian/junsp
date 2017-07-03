"""
Parameters :
1 f - file name
"""
import sys
import re

f = open(sys.argv[1],'r')
#metric = sys.argv[2]
metric  = 'Accepted_Ratio'
metric2 = 'Accepted ratio'
nodes=[40,45,50,55,60]
number=0
orig = f.read()
temp = orig
baseline=[0.0]*len(nodes)
reinforced=[0.0]*len(nodes)
bw=[0.0]*len(nodes)
exact=[0.0]*len(nodes)

while temp.find('Number:')!=-1:
    number=number+1
    index = temp.find('Number:')
    temp = temp[index+6:]
    if(temp[:temp.find('Number:')]) :
        sim = temp[:temp.find('Number:')-1]
    else:
        sim = temp

    for i in range(0,len(nodes)):
        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	baseline[i] = baseline[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	reinforced[i] = reinforced[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	bw[i] = bw[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	exact[i] = exact[i]+float(m.group(0))

#calculate average
for i in range(0,len(nodes)):
    reinforced[i] = reinforced[i]/number
    baseline[i] = baseline[i]/number
    exact[i] = exact[i]/number
    bw[i] = bw[i]/number

print baseline
print reinforced
print exact
print bw

#write to a file in latex format
fwriter = open(metric+'.tex','w')
latex = '\\begin{tikzpicture}[scale=0.85]\n\\begin{axis}[\nxlabel={node number},\nylabel={'+metric2+' \%},\nxmin=40, xmax=65,\nymin=65, ymax=100,\nxtick={40,45,50,55,60,65},\nytick={65,70,75,80,85,90,95,100},\nlegend pos=south east,\nlegend style={font=\\small},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

latex = latex + '\\addplot[\n	color=violet,\n	mark=square,\n]\ncoordinates{\n'
for i in range(0, len(nodes)):
	latex = latex+'('+str(i)+','+str(reinforced[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=blue,\n	mark=x,\n]\ncoordinates{\n'
for i in range(0, len(nodes)):
	latex = latex+'('+str(i)+','+str(baseline[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=green,\n	mark=o,\n]\ncoordinates{\n'
for i in range(0, len(nodes)):
	latex = latex+'('+str(i)+','+str(exact[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=red,\n	mark=triangle,\n]\ncoordinates{\n'
for i in range(0, len(nodes)):
	latex = latex+'('+str(i)+','+str(bw[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$reinforced$,$baseline$,$exact$,$bw$}\n\\end{axis}\n\\end{tikzpicture}'

fwriter.write(latex)
f.closed

'''
#plot figure in python
import matplotlib.pyplot as plt
import numpy as np
x=list(range(start,myLambda))
plt.plot(x,heu1[start:],'y-')
plt.plot(x,reinforced[start:],'g-')
plt.plot(x,baseline[start:],'m-')
plt.plot(x,exact[start:],'b-')
plt.plot(x,bw[start:],'r-')

plt.ylabel('Accepted Ratio')
plt.xlabel('lambda')
plt.show()
'''
