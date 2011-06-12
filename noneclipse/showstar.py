#!/usr/bin/env python
from xml.etree import ElementTree as ET
file = open("icon.svg")
element = ET.XML(file.read())
for subelement in element:
    for i in subelement.attrib.keys():
        if "inkscape" in i and "label" in i:
            if subelement.attrib[i].startswith("Star"):
                subelement.attrib["style"] = "display:inline"

fileout = open("icon-star.svg", "w")
fileout.write(ET.tostring(element))
