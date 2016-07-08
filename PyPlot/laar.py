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
start=1
myLambda=1+8	#in vne lambda+1
number=0
orig = f.read()
temp = orig
heu1=[0.0]*myLambda
reinforced=[0.0]*myLambda
baseline=[0.0]*myLambda
exact=[0.0]*myLambda
bw=[0.0]*myLambda
acceptedRatio = [heu1,reinforced,baseline,exact,bw]

while temp.find('Number:')!=-1:
    number=number+1
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
      	reinforced[i] = reinforced[i]+float(m.group(0))

        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	baseline[i] = baseline[i]+float(m.group(0))

        index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	exact[i] = exact[i]+float(m.group(0))

	index  = sim.find(metric)
	sim = sim[index+len(metric):]
	m = re.search('[0-9]*\.[0-9]*',sim)
      	bw[i] = bw[i]+float(m.group(0))


#calculate average
for i in range(0,myLambda):
    heu1[i] = heu1[i]/number
    reinforced[i] = reinforced[i]/number
    baseline[i] = baseline[i]/number
    exact[i] = exact[i]/number
    bw[i] = bw[i]/number

print heu1
print reinforced
print baseline
print exact
print bw

#write to a file in latex format
fwriter = open(metric+'.tex','w')
latex = '\\begin{figure}\n\\begin{tikzpicture}[scale=1.0]\n\\begin{axis}[\nxlabel={arrival rate $\lambda$},\nylabel={'+metric2+' \%},\nxmin=1, xmax=9,\nymin=70, ymax=100,\nxtick={1,2,3,4,5,6,7,8},\nytick={75,80,85,90,95},\nlegend pos=south west,\nlegend style={font=\\tiny},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

latex = latex + '\\addplot[\n	color=violet,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(reinforced[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=blue,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(baseline[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=green,\n	mark=o,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(exact[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=red,\n	mark=triangle,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(bw[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$reinforced$,$baseline$,$exact$,$bw$}\n\\end{axis}\n\\end{tikzpicture}\n\\caption{Acceptance ratio}\n\\label{l-ar}\n\\end{figure}'

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

