# clj-fast-html
clj-fast-html is a Clojure library designed for fast and efficient HTML generation.

# Usage
You can find the latest version on Clojars:

[![Clojars Project](http://clojars.org/io.github.panthevm/clj-fast-html/latest-version.svg)](https://clojars.org/io.github.panthevm/clj-fast-html) <br>

## Example:
Here is an example of how to use clj-fast-html to generate an HTML string:
``` clj
(clj-fast-html.core/to-html-string
 [[:html/raw "<!DOCTYPE html>"]
  [:html
   [:head
    [:title "Title"]]
   [:body
    [:h1#id.class1.class2 "Header"]
    [:p {:style {:color "red"}} "text"]
    [:p {:class ["class3" "class4"]} "text"]
    [:p {:class "class3 class4"} "text"]
    [:dialog {:open true} "text"]
    [:span {:href (clj-fast-html.core/escape "' onmouseover='alert(1)")}]
    [:span (clj-fast-html.core/escape "<p onmouseover='alert(1)'></p>")]
    (mapv
     (fn [x]
       [:span x])
     [1 2 3 4])]]])
```
``` html
<!DOCTYPE html>
<html>
  <head>
    <title>Title</title>
  </head>
  <body>
    <h1 id="id" class="class1 class2">Header</h1>
    <p style="color: red">text</p>
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

# Benchmark
The performance of clj-fast-html is benchmarked against other popular HTML generation libraries. Below are the benchmark results for generating the [Clojure home page](https://clojure.org/)

#### Single Thread
| Library                | Average (ns)     | %        |
|------------------------|------------------|----------|
| clj-fast-html (latest) | 39434.940 ns/op  | 0%       |
| Hiccup (2.0.0-RC3)     | 306147.614 ns/op | 676.34%  |
| Hiccup (2.0.0-RC1)     | 666406.325 ns/op | 1589.89% |

#### Multithread
| Library                | Average (μs)     | %        |
|------------------------|------------------|----------|
| clj-fast-html (latest) | 176.198   μs/op  | 0%       |
| Hiccup (2.0.0-RC3)     | 1332.627  μs/op  | 656.32%  |
| Hiccup (2.0.0-RC1)     | 2041.248  μs/op  | 1058.5%  |

To run the benchmark yourself, use the following command:
``` bash
make bench
```
