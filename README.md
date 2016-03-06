# re-pl

A toy [re-frame](https://github.com/Day8/re-frame) application that presents the user with a [bootstrapped cljs](https://github.com/clojure/clojurescript/wiki/Bootstrapping-the-Compiler) repl.

Uses [re-plumb](https://github.com/Lambda-X/replumb) to make read/eval super easy, and [CodeMirror](https://codemirror.net/) for editing/highlighting.

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein cljsbuild once min
```
