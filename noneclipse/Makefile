CONV := ./makepng
NOSTAR := icon-gen-512x512.png icon-gen-72x72.png \
			icon-gen-48x48.png icon-gen-36x36.png

all : $(NOSTAR)


icon-gen-512x512.png : icon.svg
	$(CONV) icon-gen-512x512.png
	optipng -q icon-gen-512x512.png
icon-gen-72x72.png : icon.svg
	$(CONV) icon-gen-72x72.png
	optipng -q icon-gen-72x72.png
icon-gen-48x48.png : icon.svg
	$(CONV) icon-gen-48x48.png
	optipng -q icon-gen-48x48.png
icon-gen-36x36.png : icon.svg
	$(CONV) icon-gen-36x36.png
	optipng -q icon-gen-36x36.png

clean : 
	rm $(NOSTAR)
