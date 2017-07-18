"""
Parameters :
1 f - file name
"""
import sys
import re

f = open(sys.argv[1],'r')
#metric = sys.argv[2]
metric  = 'AcceptedRatio'
metric2 = 'Accepted ratio'
start=5
myLambda=11	#in vne lambda+1
number=0
orig = f.read()
temp = orig
shen=[0.0]*myLambda
ciplm=[0.0]*myLambda
ciplm_r=[0.0]*myLambda

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
        ciplm_r[i] = ciplm_r[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	shen[i] = shen[i]+float(m.group(0))

        index  = sim.find(metric)
        sim = sim[index+len(metric):]
        m = re.search('[0-9]*\.[0-9]*',sim)
      	ciplm[i] = ciplm[i]+float(m.group(0))



#calculate average
for i in range(0,myLambda):
    shen[i] = shen[i]/number
    ciplm[i] = ciplm[i]/number
    ciplm_r[i] = ciplm_r[i]/number

print shen
print ciplm
print ciplm_r

#write to a file in latex format
fwriter = open(metric+'.tex','w')
latex = '\\begin{tikzpicture}[scale=0.85]\n\\begin{axis}[\nxlabel={arrival rate $\lambda$},\nylabel={'+metric2+' \%},\nxmin=5, xmax=10,\nymin=80, ymax=100,\nxtick={5,6,7,8,9,10},\nytick={80,85,90,95,100},\nlegend pos=south west,\nlegend style={font=\\small},\nymajorgrids=true,\ngrid style=dashed,\n]\n'


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
	latex = latex+'('+str(i)+','+str(ciplm_r[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$shen$,$ciplm$,$ciplm\_r$}\n\\end{axis}\n\\end{tikzpicture}'

fwriter.write(latex)
f.closed
