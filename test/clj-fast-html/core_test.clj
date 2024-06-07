(ns clj-fast-html.core-test
  (:require
   [clj-fast-html.core :as sut]
   [clojure.test       :refer [deftest testing]]
   [matcho.core        :refer [match]]))

(defmacro match!
  [dsl pattern]
  `(let [result# (sut/to-html-string ~dsl)]
     (match result# ~pattern)
     result#))

(deftest content-test
  (testing "Null"
    (match! nil ""))
  (testing "Boolean"
    (match! true  "true")
    (match! false "false"))
  (testing "Integer"
    (match! 1 "1")
    (match! -1 "-1"))
  (testing "Double"
    (match! 1.0  "1.0")
    (match! -1.0 "-1.0"))
  (testing "Float"
    (match! (float 1.1) "1.1")
    (match! (float -1.1) "-1.1"))
  (testing "Ratio"
    (match! (rationalize 0.5) "1/2")
    (match! (rationalize -0.5) "-1/2"))
  (testing "BigDecimal"
    (match! 1M "1")
    (match! -1M "-1"))
  (testing "String"
    (match! "" "")
    (match! "x" "x"))
  (testing "Keyword"
    (match! :a ":a")
    (match! :a/b ":a/b"))
  (testing "Symbol"
    (match! 'a "a")
    (match! 'a/b "a/b"))
  (testing "Map"
    (match! {} "{}")
    (match! {:a true} "{:a true}"))
  (testing "Vector"
    (match! [] "")
    (match! [1 "x"] "1x"))
  (testing "List"
    (match! '() "")
    (match! '(1 "x") "1x"))
  (testing "Set"
    (match! #{} "#{}")
    (match! #{1} #"#{1}")))

(deftest core-test

  (testing "Tag"
    (testing "Simple"
      (match! [:div] "<div></div>"))
    (testing "ID"
      (match! [:div#foo] "<div id='foo'></div>"))
    (testing "Class"
      (match! [:div.foo]     "<div class='foo'></div>")
      (match! [:div.foo.bar] "<div class='foo bar'></div>")))

  (testing "Attributes"
    (testing "Null"
      (match! [:div {:id nil}] "<div></div>"))
    (testing "String"
      (match! [:div {:id "foo"}] "<div id='foo'></div>"))
    (testing "Vector"
      (match! [:div {:class []}] "<div></div>")
      (match! [:div {:class [nil "1" nil "2"]}] "<div class='1 2'></div>")
      (match! [:div {:class ["1"]}] "<div class='1'></div>")
      (match! [:div {:class ["1" "2"]}] "<div class='1 2'></div>"))
    (testing "List"
      (match! [:div {:class '(1)}] "<div class='1'></div>")
      (match! [:div {:class '(1 2)}] "<div class='1 2'></div>"))
    (testing "Set"
      (match! [:div {:class #{1}}]   "<div class='1'></div>")
      (match! [:div {:class #{1 2}}] #"<div class='1 2'></div>|<div class='2 1'></div>"))
    (testing "Map"
      (match! [:div {:style {}}]   "<div></div>")
      (match! [:div {:style {:color "red"}}] "<div style='color:red'></div>") 
      (match! [:div {:style {:color "red" :width 50}}] "<div style='color:red;width:50'></div>"))
    
    (testing "ID"
      (testing "Simple"
        (match! [:div {:id "foo"}] "<div id='foo'></div>"))
      (testing "Tag ID & ID attribute"
        (match! [:div#foo {:id nil}] "<div id='foo'></div>")
        (match! [:div#foo {:id "bar"}] "<div id='bar'></div>")))

    (testing "Class"
      (testing "Simple"
        (match! [:div {:class "foo"}] "<div class='foo'></div>"))
      (testing "Tag class & class attribute"
        (match! [:div.foo {:class nil}]           "<div class='foo'></div>")
        (match! [:div.foo {:class "bar"}]         "<div class='foo bar'></div>")
        (match! [:div.foo {:class []}]            "<div class='foo'></div>")
        (match! [:div.foo {:class ["bar"]}]       "<div class='foo bar'></div>")
        (match! [:div.foo {:class ["bar" "zaz"]}] "<div class='foo bar zaz'></div>")

        (match! [:div.foo.bar {:class nil}]           "<div class='foo bar'></div>")
        (match! [:div.foo.bar {:class "zaz"}]         "<div class='foo bar zaz'></div>")
        (match! [:div.foo.bar {:class []}]            "<div class='foo bar'></div>")
        (match! [:div.foo.bar {:class ["zaz"]}]       "<div class='foo bar zaz'></div>")
        (match! [:div.foo.bar {:class ["zaz" "qux"]}] "<div class='foo bar zaz qux'></div>"))))

  (testing "Nested"
    (match! [:div [:div 1]] "<div><div>1</div></div>"))

  (testing "List of nodes"
    (match! [:div [[:div 1] [:div 2]]] "<div><div>1</div><div>2</div></div>")
    (match! [:div '([:div 1] [:div 2])] "<div><div>1</div><div>2</div></div>")
    (match! [:div [1 2 3]] "<div>123</div>"))

  (testing "Unclosed tags"
    (match! [:input] "<input>") 
    (match! [:input#foo] "<input id='foo'>") 
    (match! [:input#foo.bar {:type "passowrd" :class "zaz"}] "<input type='passowrd' class='bar zaz' id='foo'>"))

  (testing "Raw content"
    (match! [:html/raw "1"] "1")
    (match! [[:html/raw "<!DOCTYPE HTML>"]
             [:head [:title "a"]]]
            "<!DOCTYPE HTML><head><title>a</title></head>")))
