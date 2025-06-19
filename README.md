# clj-fast-html

A fast HTML generation library for Clojure that compiles a Hiccup style data structure into plain HTML strings.

## Features

- Small and dependency free runtime
- Protocol based implementation for high performance
- Compatible with Clojure on the JVM

## Installation

The library is available on [Clojars](https://clojars.org/io.github.panthevm/clj-fast-html).

Add the dependency to your **deps.edn** file:

```clojure
io.github.panthevm/clj-fast-html {:mvn/version "<latest-version>"}
```

or with Maven:

```xml
<dependency>
  <groupId>io.github.panthevm</groupId>
  <artifactId>clj-fast-html</artifactId>
  <version><!-- latest version --></version>
</dependency>
```

## Usage

The API accepts a vector based description of HTML similar to Hiccup and returns the resulting HTML string.

```clj
(clj-fast-html.core/to-html-string
 [[:html/raw "<!DOCTYPE html>"]
  [:html
   [:head [:title "Title"]]
   [:body
    [:h1#id.class1.class2 "Header"]
    [:p {:style {:color "red"}} "text"]
    [:p {:class ["class3" "class4"]} "text"]
    [:p {:class "class3 class4"} "text"]
    [:dialog {:open true} "text"]
    [:span {:href (clj-fast-html.core/escape "' onmouseover='alert(1)")}]
    [:span (clj-fast-html.core/escape "<p onmouseover='alert(1)'></p>")]
    (mapv (fn [x] [:span x]) [1 2 3 4])]])
```

The call above produces the following HTML:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>Title</title>
  </head>
  <body>
    <h1 id="id" class="class1 class2">Header</h1>
    <p style="color:red">text</p>
    <p class="class3 class4">text</p>
    <p class="class3 class4">text</p>
    <dialog open>text</dialog>
    <span href="&#39; onmouseover=&#39;alert(1)"></span>
    <span>&lt;p onmouseover=&#39;alert(1)&#39;&gt;&lt;/p&gt;</span>
    <span>1</span>
    <span>2</span>
    <span>3</span>
    <span>4</span>
  </body>
</html>
```

## Benchmark

The library includes JMH benchmarks. Results for rendering the [Clojure home page](https://clojure.org/) on a single thread are shown below.

| Library                | Average (ns)     | %        |
|------------------------|------------------|----------|
| clj-fast-html (latest) | 39434.940 ns/op  | 0%       |
| Hiccup (2.0.0-RC3)     | 306147.614 ns/op | 676.34%  |
| Hiccup (2.0.0-RC1)     | 666406.325 ns/op | 1589.89% |

For a multithreaded run:

| Library                | Average (μs)     | %        |
|------------------------|------------------|----------|
| clj-fast-html (latest) | 176.198   μs/op  | 0%       |
| Hiccup (2.0.0-RC3)     | 1332.627  μs/op  | 656.32%  |
| Hiccup (2.0.0-RC1)     | 2041.248  μs/op  | 1058.5%  |

![Benchmark Graph](images/benchmark_graph.svg)

The latest benchmark results are also published automatically to
[GitHub Pages](https://panthevm.github.io/clj-fast-html/).

Run the benchmarks locally with:

```bash
make bench
```

## License

Distributed under the [MIT License](https://mit-license.org/).
