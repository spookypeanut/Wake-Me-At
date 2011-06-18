CONV := ./makepng
PLAIN := icon-gen-512x512.png icon-gen-72x72.png \
			icon-gen-48x48.png icon-gen-36x36.png
STAR := icon-gen-star-512x512.png icon-gen-star-72x72.png \
			icon-gen-star-48x48.png icon-gen-star-36x36.png


all : plain star
plain : $(PLAIN)
star : $(STAR)

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

icon-gen-star.svg : icon.svg
	./showstar.py

icon-gen-star-512x512.png : icon-gen-star.svg
	$(CONV) icon-gen-star-512x512.png
	optipng -q icon-gen-star-512x512.png
icon-gen-star-72x72.png : icon-gen-star.svg
	$(CONV) icon-gen-star-72x72.png
	optipng -q icon-gen-star-72x72.png
icon-gen-star-48x48.png : icon-gen-star.svg
	$(CONV) icon-gen-star-48x48.png
	optipng -q icon-gen-star-48x48.png
icon-gen-star-36x36.png : icon-gen-star.svg
	$(CONV) icon-gen-star-36x36.png
	optipng -q icon-gen-star-36x36.png

clean : 
	rm $(PLAIN) $(STAR) icon-gen-star.svg
