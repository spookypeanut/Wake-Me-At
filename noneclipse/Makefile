CONV := ./makepng
PLAIN := icon-gen-512x512.png icon-gen-72x72.png \
			icon-gen-48x48.png icon-gen-36x36.png
STAR := icon-gen-star-512x512.png icon-gen-star-72x72.png \
			icon-gen-star-48x48.png icon-gen-star-36x36.png
TASKBARBKG := icon-gen-taskbarbkg-100x100.png icon-gen-taskbarbkg-25x25.png\
				icon-gen-taskbarbkg-24x24.png icon-gen-taskbarbkg-16x16.png\
				icon-gen-taskbarbkg-12x12.png icon-gen-taskbarbkg-27x27.png\
				icon-gen-taskbarbkg-18x18.png icon-gen-taskbarbkg-13x13.png\
				icon-gen-taskbarbkg-14x14.png
POINTER := icon-gen-pointer-32x64.png
TASKBAR := icon-gen-taskbar-25x25.png icon-gen-taskbar-24x38.png\
			icon-gen-taskbar-12x19.png icon-gen-taskbar-16x25.png

all : plain star taskbarbkg taskbar pointer
plain : $(PLAIN)
star : $(STAR)
taskbar : $(TASKBAR)
taskbarbkg : $(TASKBARBKG)
pointer : $(POINTER)

icon-gen-512x512.png : icon.svg
	$(CONV) icon-gen-512x512.png
	optipng -q generated/icon-gen-512x512.png
icon-gen-72x72.png : icon.svg
	$(CONV) icon-gen-72x72.png
	optipng -q generated/icon-gen-72x72.png
icon-gen-48x48.png : icon.svg
	$(CONV) icon-gen-48x48.png
	optipng -q generated/icon-gen-48x48.png
icon-gen-36x36.png : icon.svg
	$(CONV) icon-gen-36x36.png
	optipng -q generated/icon-gen-36x36.png

icon-gen-pointer.svg : icon.svg
	./makepointer.py

icon-gen-pointer-32x64.png : icon-gen-pointer.svg
	$(CONV) icon-gen-pointer-32x64.png
	optipng -q generated/icon-gen-pointer-32x64.png

icon-gen-taskbarbkg.svg : icon.svg
	./maketaskbar.py

icon-gen-taskbarbkg-100x100.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-100x100.png
	optipng -q generated/icon-gen-taskbarbkg-100x100.png

icon-gen-taskbarbkg-27x27.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-27x27.png
	optipng -q generated/icon-gen-taskbarbkg-27x27.png

icon-gen-taskbarbkg-25x25.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-25x25.png
	optipng -q generated/icon-gen-taskbarbkg-25x25.png

icon-gen-taskbarbkg-24x24.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-24x24.png
	optipng -q generated/icon-gen-taskbarbkg-24x24.png

icon-gen-taskbarbkg-18x18.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-18x18.png
	optipng -q generated/icon-gen-taskbarbkg-18x18.png

icon-gen-taskbarbkg-16x16.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-16x16.png
	optipng -q generated/icon-gen-taskbarbkg-16x16.png

icon-gen-taskbarbkg-13x13.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-13x13.png
	optipng -q generated/icon-gen-taskbarbkg-13x13.png

icon-gen-taskbarbkg-14x14.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-14x14.png
	optipng -q generated/icon-gen-taskbarbkg-14x14.png

icon-gen-taskbarbkg-12x12.png : icon-gen-taskbarbkg.svg
	$(CONV) icon-gen-taskbarbkg-12x12.png
	optipng -q generated/icon-gen-taskbarbkg-12x12.png

icon-gen-taskbar-24x38.png :
	xcf2png icon-taskbar-24x38.xcf > generated/icon-gen-taskbar-24x38.png
	optipng -q generated/icon-gen-taskbar-24x38.png

icon-gen-taskbar-16x25.png :
	xcf2png icon-taskbar-16x25.xcf > generated/icon-gen-taskbar-16x25.png
	optipng -q generated/icon-gen-taskbar-16x25.png

icon-gen-taskbar-12x19.png :
	xcf2png icon-taskbar-12x19.xcf > generated/icon-gen-taskbar-12x19.png
	optipng -q generated/icon-gen-taskbar-12x19.png

icon-gen-taskbar-25x25.png :
	xcf2png icon-taskbar-25x25.xcf > generated/icon-gen-taskbar-25x25.png
	optipng -q generated/icon-gen-taskbar-25x25.png

icon-gen-star.svg : icon.svg
	./makestar.py

icon-gen-star-512x512.png : icon-gen-star.svg
	$(CONV) icon-gen-star-512x512.png
	optipng -q generated/icon-gen-star-512x512.png
icon-gen-star-72x72.png : icon-gen-star.svg
	$(CONV) icon-gen-star-72x72.png
	optipng -q generated/icon-gen-star-72x72.png
icon-gen-star-48x48.png : icon-gen-star.svg
	$(CONV) icon-gen-star-48x48.png
	optipng -q generated/icon-gen-star-48x48.png
icon-gen-star-36x36.png : icon-gen-star.svg
	$(CONV) icon-gen-star-36x36.png
	optipng -q generated/icon-gen-star-36x36.png

clean : 
	rm -rf generated/*
