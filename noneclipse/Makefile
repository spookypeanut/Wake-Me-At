CONV := ./makepng
PLAIN := icon-gen-512x512.png icon-gen-72x72.png \
			icon-gen-48x48.png icon-gen-36x36.png
STAR := icon-gen-star-512x512.png icon-gen-star-72x72.png \
			icon-gen-star-48x48.png icon-gen-star-36x36.png
TASKBAR := icon-gen-taskbar-100x100.png icon-gen-taskbar-25x25.png\
			icon-gen-taskbar-24x24.png icon-gen-taskbar-16x16.png\
			icon-gen-taskbar-12x12.png

all : plain star taskbar
plain : $(PLAIN)
star : $(STAR)
taskbar : $(TASKBAR)

generated :
	mkdir -p generated

icon-gen-512x512.png : generated icon.svg
	$(CONV) icon-gen-512x512.png
	optipng -q generated/icon-gen-512x512.png
icon-gen-72x72.png : generated icon.svg
	$(CONV) icon-gen-72x72.png
	optipng -q generated/icon-gen-72x72.png
icon-gen-48x48.png : generated icon.svg
	$(CONV) icon-gen-48x48.png
	optipng -q generated/icon-gen-48x48.png
icon-gen-36x36.png : generated icon.svg
	$(CONV) icon-gen-36x36.png
	optipng -q generated/icon-gen-36x36.png

icon-gen-taskbar.svg : generated icon.svg
	./maketaskbar.py

icon-gen-taskbar-100x100.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-100x100.png
	optipng -q generated/icon-gen-taskbar-100x100.png

icon-gen-taskbar-25x25.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-25x25.png
	optipng -q generated/icon-gen-taskbar-25x25.png

icon-gen-taskbar-24x24.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-24x24.png
	optipng -q generated/icon-gen-taskbar-24x24.png

icon-gen-taskbar-16x16.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-16x16.png
	optipng -q generated/icon-gen-taskbar-16x16.png

icon-gen-taskbar-12x12.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-12x12.png
	optipng -q generated/icon-gen-taskbar-12x12.png

icon-gen-star.svg : generated icon.svg
	./showstar.py

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
	rm -rf generated
