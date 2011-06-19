#!/usr/bin/env python
from xml.etree import ElementTree as ET
file = open("icon.svg")
element = ET.XML(file.read())
for subelement in element:
    for i in subelement.attrib.keys():
        if "inkscape" in i and "label" in i:
            subelement.attrib["style"] = "display:none"
            if subelement.attrib[i] == "Taskbar":
                subelement.attrib["style"] = "display:inline"

fileout = open("generated/icon-gen-taskbarbkg.svg", "w")
fileout.write(ET.tostring(element))
