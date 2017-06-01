"""
Parameters :
1 f - file name
"""
import sys
import re

f = open(sys.argv[1],'r')
#metric = sys.argv[2]
metric  = 'CostRevenue'
metric2 = 'Cost revenue'
start=2
myLambda=8	#in vne lambda+1
number=0
orig = f.read()
temp = orig
ideal=[0.0]*myLambda
shen=[0.0]*myLambda
ciplm=[0.0]*myLambda
ciplm_up=[0.0]*myLambda

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
      	ideal[i] = ideal[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	shen[i] = shen[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	ciplm[i] = ciplm[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	ciplm_up[i] = ciplm_up[i]+float(m.group(0))

#calculate average
for i in range(0,myLambda):
    ideal[i] = ideal[i]/number
    shen[i] = shen[i]/number
    ciplm[i] = ciplm[i]/number
    ciplm_up[i] = ciplm_up[i]/number

print ideal
print shen
print ciplm
print ciplm_up

#write to a file in latex format
fwriter = open(metric+'1.tex','w')
latex = '\\begin{tikzpicture}[scale=0.85]\n\\begin{axis}[\nxlabel={arrival rate $\lambda$},\nylabel={'+metric2+'},\nxmin=2, xmax=8,\nymin=3.6, ymax=4.5,\nxtick={2,3,4,5,6,7,8},\nytick={3.6,3.8,4.0,4.2,4.4,4.5},\nlegend pos=south east,\nlegend style={font=\\small},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

latex = latex + '\\addplot[\n	color=violet,\n	mark=square,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(ideal[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=blue,\n	mark=x,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(shen[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=green,\n	mark=o,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(ciplm[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=red,\n	mark=triangle,\n]\ncoordinates{\n'
for i in range(start, myLambda):
	latex = latex+'('+str(i)+','+str(ciplm_up[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$ideal$,$shen$,$ciplm$,$ciplm\_up$}\n\\end{axis}\n\\end{tikzpicture}'

fwriter.write(latex)
f.closed
