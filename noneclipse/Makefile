CONV := convert -background none -antialias

all : icon

icon : icon-512x512.png icon-72x72.png \
		icon-48x48.png icon-36x36.png


icon-512x512.png : icon.svg
	$(CONV) icon.svg icon-512x512.png
	optipng -q icon-512x512.png
icon-72x72.png : icon.svg
	$(CONV) icon.svg -resize 72x72 icon-72x72.png
	optipng -q icon-72x72.png
icon-48x48.png : icon.svg
	$(CONV) icon.svg -resize 48x48 icon-48x48.png
	optipng -q icon-48x48.png
icon-36x36.png : icon.svg
	$(CONV) icon.svg -resize 36x36 icon-36x36.png
	optipng -q icon-36x36.png

clean : 
	rm $(ALL)
