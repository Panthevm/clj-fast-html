# clj-fast-html

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
    (mapv
     (fn [x]
       [:span x])
     [1 2 3 4])]]]) 

"<!DOCTYPE html><html><head><title>Title</title></head><body><h1 id='id' class='class1 class2'>Header</h1><p style='color:red'>text</p><p class='class3 class4'>text</p><p class='class3 class4'>text</p><dialog open>text</dialog><span>1</span><span>2</span><span>3</span><span>4</span></body></html>"
```
