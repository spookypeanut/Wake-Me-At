CONV := convert -background none -antialias

all : icon

icon : icon-gen-512x512.png icon-gen-72x72.png \
		icon-gen-48x48.png icon-gen-36x36.png

icon-gen-512x512.png : icon.svg
	$(CONV) icon.svg icon-gen-512x512.png
	optipng -q icon-gen-512x512.png
icon-gen-72x72.png : icon.svg
	$(CONV) icon.svg -resize 72x72 icon-gen-72x72.png
	optipng -q icon-gen-72x72.png
icon-gen-48x48.png : icon.svg
	$(CONV) icon.svg -resize 48x48 icon-gen-48x48.png
	optipng -q icon-gen-48x48.png
icon-gen-36x36.png : icon.svg
	$(CONV) icon.svg -resize 36x36 icon-gen-36x36.png
	optipng -q icon-gen-36x36.png

clean : 
	rm $(ALL)
