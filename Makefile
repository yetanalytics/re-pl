.phony: clean site

clean:
	rm -rf docs
docs:
	cp -rv resources/public docs
	clojure -A:fig:min
site: clean docs
