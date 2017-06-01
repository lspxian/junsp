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
beta=[0.04,0.06,0.08,0.1,0.12]	#in vne lambda+1
number=0
orig = f.read()
temp = orig
ideal=[0.0]*len(beta)
shen=[0.0]*len(beta)
ciplm=[0.0]*len(beta)
ciplm_up=[0.0]*len(beta)

while temp.find('Number:')!=-1:
    number=number+1
    index = temp.find('Number:')
    temp = temp[index+6:]
    if(temp[:temp.find('Number:')]) :
        sim = temp[:temp.find('Number:')-1]
    else:
        sim = temp

    for i in range(0,len(beta)):

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
for i in range(0,len(beta)):
    ideal[i] = ideal[i]/number
    shen[i] = shen[i]/number
    ciplm[i] = ciplm[i]/number
    ciplm_up[i] = ciplm_up[i]/number

print ideal
print shen
print ciplm
print ciplm_up

#write to a file in latex format
fwriter = open(metric+'2.tex','w')
latex = '\\begin{tikzpicture}[scale=0.85]\n\\begin{axis}[\nxlabel={peering links $\\beta$},\nylabel={'+metric2+' \%},\nxmin=0.04, xmax=0.12,\nymin=65, ymax=100,\nxtick={0.04,0.06,0.08,0.1,0.12},\nytick={70,75,80,85,90,95,100},\nlegend pos=south east,\nlegend style={font=\\small},\nymajorgrids=true,\ngrid style=dashed,\n]\n'

latex = latex + '\\addplot[\n	color=violet,\n	mark=square,\n]\ncoordinates{\n'
for i in range(0, len(beta)):
	latex = latex+'('+str(beta[i])+','+str(ideal[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=blue,\n	mark=x,\n]\ncoordinates{\n'
for i in range(0, len(beta)):
	latex = latex+'('+str(beta[i])+','+str(shen[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=green,\n	mark=o,\n]\ncoordinates{\n'
for i in range(0, len(beta)):
	latex = latex+'('+str(beta[i])+','+str(ciplm[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\addplot[\n	color=red,\n	mark=triangle,\n]\ncoordinates{\n'
for i in range(0, len(beta)):
	latex = latex+'('+str(beta[i])+','+str(ciplm_up[i])+')'
latex = latex + '\n};\n'

latex = latex + '\\legend{$ideal$,$shen$,$ciplm$,$ciplm\_up$}\n\\end{axis}\n\\end{tikzpicture}'

fwriter.write(latex)
f.closed
