#!/usr/bin/env python
import random

header = '''<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<svg>
  <g
     id="layer1">
'''
footer = '''
  </g>
</svg>
'''

def printrect(x=0, y=0, width=10, height=10):
    xml = "<rect\n"
    xml += '\tstyle="fill:#000000;stroke:none;fill-opacity:1"\n'
    xml += '\twidth="%s"\n' % width
    xml += '\theight="%s"\n' % height
    xml += '\tx="%s"\n' % x
    xml += '\ty="%s"\n' % y
    xml += '\tid="rect%s" />\n' % random.randint(0,10000000) 
    return xml

startsize = 10
scaleratio = 0.90
dottedratio = 2.0
minimumsize = 0.01

print header
currsize = startsize
currx = 0
while currsize > (startsize * minimumsize):
    print printrect(width=currsize,
                    height=currsize,
                    x = currx,
                    y = startsize-(currsize / 2))
    currsize = currsize * scaleratio
    currx += dottedratio * currsize
print footer
