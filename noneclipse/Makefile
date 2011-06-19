CONV := ./makepng
PLAIN := icon-gen-512x512.png icon-gen-72x72.png \
			icon-gen-48x48.png icon-gen-36x36.png
STAR := icon-gen-star-512x512.png icon-gen-star-72x72.png \
			icon-gen-star-48x48.png icon-gen-star-36x36.png
TASKBAR := icon-gen-taskbar-100x100.png icon-gen-taskbar-25x25.png\
			icon-gen-taskbar-24x24.png icon-gen-taskbar-16x16.png\
			icon-gen-taskbar-12x12.png icon-gen-taskbar-27x27.png\
			icon-gen-taskbar-18x18.png icon-gen-taskbar-13x13.png

all : plain star taskbar
plain : $(PLAIN)
star : $(STAR)
taskbar : $(TASKBAR)

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

icon-gen-taskbar.svg : icon.svg
	./maketaskbar.py

icon-gen-taskbar-100x100.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-100x100.png
	optipng -q generated/icon-gen-taskbar-100x100.png

icon-gen-taskbar-27x27.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-27x27.png
	optipng -q generated/icon-gen-taskbar-27x27.png

icon-gen-taskbar-25x25.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-25x25.png
	optipng -q generated/icon-gen-taskbar-25x25.png

icon-gen-taskbar-24x24.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-24x24.png
	optipng -q generated/icon-gen-taskbar-24x24.png

icon-gen-taskbar-18x18.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-18x18.png
	optipng -q generated/icon-gen-taskbar-18x18.png

icon-gen-taskbar-16x16.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-16x16.png
	optipng -q generated/icon-gen-taskbar-16x16.png

icon-gen-taskbar-13x13.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-13x13.png
	optipng -q generated/icon-gen-taskbar-13x13.png

icon-gen-taskbar-12x12.png : icon-gen-taskbar.svg
	$(CONV) icon-gen-taskbar-12x12.png
	optipng -q generated/icon-gen-taskbar-12x12.png

icon-gen-star.svg : icon.svg
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
	rm -rf generated/*
